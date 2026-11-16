package com.ims134.doclone.io

import android.annotation.TargetApi
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import android.util.Log
import java.io.FileDescriptor
import java.io.IOException

/**
 * Was `ia2` / `FileBridge`. A socketpair-based command bridge for writing to ContentProviderHolder
 * file descriptor. The constructor creates ContentProviderHolder Unix socketpair; the caller writes
 * commands to [callerFd], the bridge thread reads them from [bridgeFd], and
 * forwards the resulting data writes to [targetFd] (set externally via
 * [targetFd] before [start]).
 *
 * Protocol (8-byte headers, big-endian):
 *   - 1, length     → write `length` bytes to [targetFd]
 *   - 2, 0          → fsync [targetFd]
 *   - 3, 0          → fsync, close [targetFd], reply, exit thread
 */
@TargetApi(21)
class FileBridge : Thread() {

    /**
     * Was `a`. The file being written to. Set by the caller before [start];
     * nullable because the caller assigns it after construction.
     */
    @JvmField
    var targetFd: FileDescriptor? = null

    /**
     * Was `b`. The bridge's own end of the socketpair. The bridge thread reads
     * commands from here and writes replies back to this same FD (socketpair
     * FDs are bidirectional).
     */
    @JvmField
    val bridgeFd: FileDescriptor = FileDescriptor()

    /**
     * Was `c`. The caller's end of the socketpair. Returned to the caller via
     * `ParcelFileDescriptor.dup(...)` in `PackageInstallerSession.openWrite`.
     */
    @JvmField
    val callerFd: FileDescriptor = FileDescriptor()

    /**
     * Was `d`. Set to `true` once the bridge has been fully closed (either by
     * command 3, by [closeBridge], or on error).
     */
    @JvmField
    @Volatile
    var closed: Boolean = false

    init {
        try {
            Os.socketpair(
                OsConstants.AF_UNIX,
                OsConstants.SOCK_STREAM,
                0,
                bridgeFd,
                callerFd,
            )
        } catch (_: ErrnoException) {
            throw RuntimeException("Failed to create bridge")
        }
    }

    /**
     * Was `a()`. Closes [targetFd], [bridgeFd], and [callerFd] if they're
     * still valid, then marks [closed]. Best-effort — individual `close`
     * failures are swallowed.
     */
    fun closeBridge() {
        targetFd?.let {
            if (it.valid()) {
                try {
                    Os.close(it)
                } catch (_: ErrnoException) {
                }
            }
        }
        if (bridgeFd.valid()) {
            try {
                Os.close(bridgeFd)
            } catch (_: ErrnoException) {
            }
        }
        if (callerFd.valid()) {
            try {
                Os.close(callerFd)
            } catch (_: ErrnoException) {
            }
        }
        closed = true
    }

    override fun run() {
        val buf = ByteArray(8192)
        while (readFrom(bridgeFd, buf, 8) == 8) {
            try {
                try {
                    val command = FileUtils.readInt32MSB(0, buf)
                    if (command == 1) {
                        // Write command: read length, then forward bytes
                        var remaining = FileUtils.readInt32MSB(4, buf)
                        while (remaining > 0) {
                            val n = readFrom(bridgeFd, buf, minOf(8192, remaining))
                            if (n == -1) {
                                throw IOException(
                                    "Unexpected EOF; still expected $remaining bytes"
                                )
                            }
                            writeTo(targetFd!!, buf, n)
                            remaining -= n
                        }
                    } else if (command == 2) {
                        // Fsync command
                        Os.fsync(targetFd!!)
                        writeTo(bridgeFd, buf, 8)
                    } else if (command == 3) {
                        // Close command
                        Os.fsync(targetFd!!)
                        Os.close(targetFd!!)
                        closed = true
                        writeTo(bridgeFd, buf, 8)
                        break
                    }
                } catch (e: ErrnoException) {
                    Log.wtf("FileBridge", "Failed during bridge", e)
                    closeBridge()
                    return
                } catch (e: IOException) {
                    Log.wtf("FileBridge", "Failed during bridge", e)
                    closeBridge()
                    return
                }
            } catch (th: Throwable) {
                closeBridge()
                throw th
            }
        }
        closeBridge()
    }

    companion object {

        /**
         * Was static `b`. Reads up to [count] bytes from [fd] into [buf] starting
         * at offset 0. Returns the number of bytes read, `-1` on EOF (0-byte
         * read), or `0` if [count] is 0 or ContentProviderHolder non-fatal `EAGAIN` occurred.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(fd: FileDescriptor, buf: ByteArray, count: Int): Int {
            val length = buf.size
            if (count < 0 || length < 0 || length < count) {
                throw ArrayIndexOutOfBoundsException(0)
            }
            if (count == 0) return 0
            try {
                val n = Os.read(fd, buf, 0, count)
                if (n == 0) return -1
                return n
            } catch (e: ErrnoException) {
                if (e.errno != OsConstants.EAGAIN) {
                    throw IOException(e)
                }
            }
            return 0
        }

        /**
         * Was static `c`. Writes [count] bytes from [buf] (starting at offset 0)
         * to [fd], looping until all bytes are written. Throws [IOException] on
         * any [ErrnoException].
         */
        @JvmStatic
        @Throws(IOException::class)
        fun writeTo(fd: FileDescriptor, buf: ByteArray, count: Int) {
            val length = buf.size
            var remaining = count
            var offset = 0
            if (count < 0 || length < 0 || length < count) {
                throw ArrayIndexOutOfBoundsException(0)
            }
            if (count == 0) return
            while (remaining > 0) {
                try {
                    val n = Os.write(fd, buf, offset, remaining)
                    remaining -= n
                    offset += n
                } catch (e: ErrnoException) {
                    throw IOException(e)
                }
            }
        }
    }
}
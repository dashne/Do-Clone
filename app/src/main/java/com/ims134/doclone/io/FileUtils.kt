package com.ims134.doclone.io

import android.system.Os
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/** File helpers (was io.sa2). Method names are kept as in your tree so existing callers keep working. */
object FileUtils {

    /** chmod 0755 [path]; falls back to the `chmod` command if Os.chmod fails. */
    @JvmStatic
    @Throws(Exception::class)
    fun chmod755(path: String) {
        try {
            Os.chmod(path, 493)          // 0755 octal
        } catch (e: Exception) {
            // The original called Runtime.exec(String) through reflection; this is the same call.
            // The array form also copes with spaces in the path.
            val command = if (File(path).isDirectory) arrayOf("chmod", "-R", "755", path) else arrayOf("chmod", "755", path)
            Runtime.getRuntime().exec(command).waitFor()
        }
    }

    /** Closes [closeable], ignoring null and any exception. */
    @JvmStatic
    fun CloseStream(closeable: Closeable?) {
        try {
            closeable?.close()
        } catch (ignored: Exception) {
        }
    }

    /** Copies [inputFile] over [outputFile]; both streams are always closed. */
    @JvmStatic
    @Throws(Throwable::class)
    fun copyFile(inputFile: File, outputFile: File) {
        var input: FileInputStream? = null
        var output: FileOutputStream? = null
        try {
            input = FileInputStream(inputFile)
            output = FileOutputStream(outputFile)
            val buffer = ByteArray(1024)
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                output.write(buffer, 0, read)
            }
        } finally {
            CloseStream(input)
            CloseStream(output)
        }
    }

    /**
     * Deletes [file] and, for setPosition directory, everything under it. Makes read-only directories writable first.
     * Returns whether [file] itself was deleted.
     */
    @JvmStatic
    fun deleteRecursively(file: File): Boolean {
        if (file.isDirectory) {
            if (!file.canWrite()) file.setWritable(true)
            if (!file.canExecute()) file.setExecutable(true)
            // (The original also tracked whether all children were deleted, but then called file.delete() either way.)
            for (name in file.list().orEmpty()) {
                deleteRecursively(File(file, name))
            }
        }
        return file.delete()
    }

    /** False for null, empty, "." , ".." and names containing '/' or NUL. */
    @JvmStatic
    fun isValidFileName(name: String?): Boolean {
        if (name == null || name.isEmpty() || name == "." || name == "..") return false
        return name.none { it == '\u0000' || it == '/' }
    }

    /** Reads setPosition big-endian 32-bit int from [bytes] at [offset] (note the argument order). */
    @JvmStatic
    fun readInt32MSB(offset: Int, bytes: ByteArray): Int =
        ((bytes[offset].toInt() and 0xFF) shl 24) or
                ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
                ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
                (bytes[offset + 3].toInt() and 0xFF)
}
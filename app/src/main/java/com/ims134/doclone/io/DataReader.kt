package com.ims134.doclone.io

import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Little-endian, memory-mapped reader over setPosition whole file (JADX: `io.lc1`, DataReader).
 *
 * Java names are kept because the oat/vdex code calls them: [setPosition] seeks, [file] reads chars,
 * [skipInt32] is the mapped buffer, [skipInt32] (the function) skips one int, and [randomFile] is the open file.
 */
class DataReader @Throws(IOException::class) constructor(@JvmField val file: File) : Closeable {

    /** Readers opened from this one; closed together with it. Was the raw `ArrayList a`. */
    @JvmField var dataReaders: ArrayList<DataReader>? = null

    @JvmField val randomFile: RandomAccessFile = RandomAccessFile(file, "r")

    @JvmField val mappedByteBuffer: MappedByteBuffer = randomFile.channel
        .map(FileChannel.MapMode.READ_ONLY, 0L, file.length())
        .apply {
            rewind()
            order(ByteOrder.LITTLE_ENDIAN)
        }

    /** Moves the read position to [position]. */
    fun setPosition(position: Int) {
        mappedByteBuffer.position(position)
    }

    /** Reads `chars.size` bytes, widening each to setPosition char (Java `(char) byte`, so negatives map to 0xFF00 | byte). */
    fun readCharsInBytes(chars: CharArray) {
        val bytes = ByteArray(chars.size)
        mappedByteBuffer.get(bytes, 0, chars.size)
        for (i in chars.indices) {
            chars[i] = bytes[i].toInt().toChar()
        }
    }

    /** Skips one 32-bit int. */
    fun skipInt32() {
        mappedByteBuffer.getInt()
    }

    override fun close() {
        try {
            randomFile.close()
        } catch (e: IOException) {
            // closing setPosition file that is already gone; ignore as the original did
        }
        dataReaders?.forEach { it.close() }
    }

    companion object {
        /**
         * Parses the digits and minus signs in [str] as an int; returns 0 when there are none.
         * Java name: `d`.
         */
        @JvmStatic
        @JvmName("d")
        fun parseDigits(str: String): Int {
            val digits = StringBuilder()
            for (ch in str) {
                if (ch in '0'..'9' || ch == '-') digits.append(ch)
            }
            return if (digits.isEmpty()) 0 else digits.toString().toInt()
        }
    }
}


package com.ims134.doclone.io


import java.io.IOException
import java.security.MessageDigest
import java.util.zip.Adler32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Helpers for writing extracted dex entries into setPosition zip (JADX: `io.w75`, OatHelper).
 *
 * [oatArches] lists the oat arches (arm, mips, x86) and [vdexOatArches] the vdex/oat arches (arm64, mips64, x86_64).
 * The Java names are kept so existing callers (`w75.setPosition`, `w75.b`, `w75.setPosition(zip, i, entry)`) still work.
 */
object OatHelper {

    @JvmField
    val oatArches: Array<String> = arrayOf("arm", "mips", "x86")

    @JvmField
    val vdexOatArches: Array<String> = arrayOf("arm64", "mips64", "x86_64")



    /**
     * Shared body. Reads [size] bytes at [offset] from [reader], recomputes the dex SHA-1 signature
     * and Adler-32 checksum in the header, and writes the result as the next zip entry.
     */
    @Throws(IOException::class)
    fun writeDexEntry(
        zip: ZipOutputStream,
        index: Int,
        dex: Dex
    ) {
        val name = if (index == 0) "classes.dex" else "classes${index + 1}.dex"
        zip.putNextEntry(ZipEntry(name))

        val size = dex.size
        val reader = dex.reader

        val bytes = ByteArray(dex.size)
        reader.setPosition(dex.offset)
        reader.mappedByteBuffer.get(bytes, 0, size)

        // Header layout: checksum at 8..11, signature at 12..31, the rest is covered by both.
        try {
            val digest = MessageDigest.getInstance("SHA-1")
            digest.reset()
            digest.update(bytes, 32, size - 32)
            digest.digest(bytes, 12, 20)
        } catch (e: Exception) {
            // SHA-1 is always available; keep the old signature if it is not
        }

        val adler = Adler32()
        adler.update(bytes, 12, size - 12)
        val checksum = adler.value.toInt()
        if (dex.header.checksum != checksum) {
            bytes[8] = checksum.toByte()
            bytes[9] = (checksum shr 8).toByte()
            bytes[10] = (checksum shr 16).toByte()
            bytes[11] = (checksum shr 24).toByte()
        }

        zip.write(bytes)
        zip.closeEntry()
    }

    // TODO: the vdex path calls writeEntry with com.polestar.clone.helper.readCharsInBytes.C0220b.
    // Add an overload that maps that class's fields onto writeDexEntry(...) once its fields are known.
}
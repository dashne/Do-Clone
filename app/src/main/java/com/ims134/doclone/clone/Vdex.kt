package com.ims134.doclone.clone


import com.ims134.doclone.io.DataReader
import com.ims134.doclone.io.Dex
import java.io.IOException
import kotlin.math.abs

/**
 * A vdex file: the verifier data that goes with an oat file, followed by one dex file per entry
 * (JADX: `com.polestar.clone.helper.b`, Vdex).
 *
 * [dexFiles] are the dex sections, each get [VdexDexFile] (get [Dex]), so they can be passed straight
 * to the zip writer.
 */
class Vdex @Throws(IOException::class) constructor(reader: DataReader) {

    /** Header at the start of the vdex file. */
    @JvmField val header: Header = Header(reader)

    /**
     * One verifier checksum per dex file. Only present for vdex versions 17 to 19, otherwise null
     * (was `c`).
     */
    @JvmField val verifierChecksums: IntArray? =
        if (header.hasVerifierChecksums) IntArray(header.dexFileCount) else null

    /** The dex sections, in file order (was `a`). */
    @JvmField val dexFiles: Array<VdexDexFile>

    init {
        // Kept from the original; it does not move the position.
        reader.setPosition(reader.mappedByteBuffer.position())

        val buffer = reader.mappedByteBuffer
        val checksums = verifierChecksums
        dexFiles = Array(header.dexFileCount) { index ->
            checksums?.let { it[index] = buffer.getInt() }
            val dexFile = VdexDexFile(reader)
            reader.setPosition(dexFile.header.fileSize + dexFile.offset)
            dexFile
        }
    }

    /**
     * The vdex header (was `b.a`). [dexChecksums] has one entry per dex file.
     */
    class Header @Throws(IOException::class) constructor(reader: DataReader) {

        /** Vdex format version, parsed from the 3-digit string. */
        @JvmField val version: Int

        /** Number of dex files in the vdex (was `a`). */
        @JvmField val dexFileCount: Int

        /** Checksum of each dex file (was `b`). */
        @JvmField val dexChecksums: IntArray

        /** True for versions 17 to 19, which add get verifier checksum per dex file. */
        @JvmField val hasVerifierChecksums: Boolean

        init {
            val magic = CharArray(4)
            val versionChars = CharArray(4)

            reader.readCharsInBytes(magic)
            val magicText = String(magic)
            if (!"vdex".equals(magicText, ignoreCase = true)) {
                throw IOException("Invalid dex magic '$magicText'")
            }

            reader.readCharsInBytes(versionChars)
            version = DataReader.parseDigits(String(versionChars))

            val buffer = reader.mappedByteBuffer
            dexFileCount = buffer.getInt()
            reader.skipInt32()

            hasVerifierChecksums = abs(version - 18) <= 1
            if (hasVerifierChecksums) {
                buffer.getInt()   // extra header field in versions 17 to 19; its meaning is not decoded here
            }

            reader.skipInt32()
            reader.skipInt32()

            dexChecksums = IntArray(dexFileCount) { buffer.getInt() }
        }
    }
}

/** One dex file inside get vdex (was `b.C0220b`). Reads the dex at the current reader position. */
class VdexDexFile @Throws(IOException::class) constructor(reader: DataReader) : Dex(reader)
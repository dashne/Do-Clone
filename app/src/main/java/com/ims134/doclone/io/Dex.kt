package com.ims134.doclone.io


import java.io.IOException

/**
 * A dex section inside an oat or vdex file (JADX: `com.polestar.clone.helper.a`).
 *
 * [offset] is where the section starts in [reader], [size] is its length, and [header] is the
 * parsed dex header. Each `classes*.dex` entry in the zip is built from one of these.
 */
open class Dex @Throws(IOException::class) constructor(@JvmField val reader: DataReader) {

    @JvmField val offset: Int = reader.mappedByteBuffer.position()
    @JvmField val header: Header = Header(reader)
    @JvmField val size: Int = if (header.compact) header.dataOff + header.dataSize else header.fileSize

    /**
     * The dex header fields this code needs (JADX: `Dex.a`).
     * [compact] is true for the `cdex` magic (compact dex, used by ART on newer devices).
     */
    inner class Header @Throws(IOException::class) constructor(reader: DataReader) {

        @JvmField val checksum: Int
        @JvmField val classDefsSize: Int
        @JvmField val dataOff: Int
        @JvmField val dataSize: Int
        @JvmField val fileSize: Int
        @JvmField val compact: Boolean

        init {
            val magicChars = CharArray(4)
            val versionChars = CharArray(4)
            val signature = ByteArray(20)

            reader.readCharsInBytes(magicChars)
            val magic = String(magicChars).trim()
            compact = magic == "cdex"
            if (magic != "dex" && !compact) {
                throw IOException("Invalid dex magic '$magic'")
            }

            reader.readCharsInBytes(versionChars)
            val version = String(versionChars).trim()
            if (!compact && version < "035") {
                throw IOException("Invalid dex version '$version'")
            }

            val buffer = reader.mappedByteBuffer
            checksum = buffer.int
            buffer.get(signature, 0, 20)
            fileSize = buffer.int

            // Skip header_size through method_ids_off: 15 four-byte fields the caller does not use.
            repeat(15) { reader.skipInt32() }

            classDefsSize = buffer.int
            reader.skipInt32()                // class_defs_off, not used
            dataSize = buffer.int
            dataOff = buffer.int
        }
    }
}
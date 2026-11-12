package com.ims134.doclone.clone




import com.ims134.doclone.io.DataReader
import com.ims134.doclone.io.Dex
import java.io.File
import java.io.IOException

/**
 * An oat file: its art header, the dex files it contains, and where each one lives
 * (JADX: `com.polestar.clone.helper.Oat`).
 *
 * [dexes] is the array of dex sections and [oatDexFiles] the matching oat dex-file records. Kept under the
 * Java names because the oat extraction code reads `Oat(reader).a` for the dex sections.
 * [header] is the header, and [oatDataOffset] is the file position where the oat data begins (always 4096).
 */
class Oat @Throws(IOException::class) constructor(reader: DataReader) {

    /** Dex sections, one per dex file in the oat. */
    @JvmField val dexes: Array<Dex>

    /** The art header. */
    @JvmField val header: Header

    /** One record per dex file, with the dex offset and the matching vdex file if there is one. */
    @JvmField val oatDexFiles: Array<OatDexFile>

    /** Position where the oat data begins. */
    @JvmField val oatDataOffset: Long

    init {
        val begin = reader.mappedByteBuffer.position().toLong()
        if (begin != OAT_BEGIN) {
            throw IOException("Strange oat position $begin")
        }

        val header = Header(reader)
        val count = header.dexFileCount
        val oatDexFiles = ArrayList<OatDexFile>(count)
        val dexes = ArrayList<Dex>(count)

        for (index in 0 until count) {
            val oatDexFile = OatDexFile(reader, header.version)
            oatDexFiles.add(oatDexFile)

            val buffer = reader.mappedByteBuffer
            val afterOatDexFile = buffer.position().toLong()
            val vdex = oatDexFile.vdexFile

            val dex: Dex
            if (vdex != null) {
                val vdexReader = DataReader(vdex)
                val openedReaders = reader.dataReaders ?: ArrayList<DataReader>().also { reader.dataReaders = it }
                openedReaders.add(vdexReader)
                vdexReader.setPosition(oatDexFile.dexFileOffset)
                dex = Dex(vdexReader)
            } else {
                reader.setPosition((begin + oatDexFile.dexFileOffset).toInt())
                dex = Dex(reader)
            }
            dexes.add(dex)

            if (header.version < Version.N_70.oat) {
                // Older oat layouts put get 4-byte marker after the class defs; skip it when it is large.
                reader.setPosition((afterOatDexFile + dex.header.classDefsSize.toLong() * 4).toInt())
                buffer.mark()
                val marker = buffer.int
                buffer.reset()
                if (marker > 255) {
                    reader.skipInt32()
                }
            } else {
                reader.setPosition(afterOatDexFile.toInt())
            }
        }

        oatDataOffset = begin
        this@Oat.header = header
        this@Oat.oatDexFiles = oatDexFiles.toTypedArray()
        this@Oat.dexes = dexes.toTypedArray()
    }

    /** Oat file versions, with the API level and the oat format number of each. */
    enum class Version(@JvmField val api: Int, @JvmField val oat: Int) {
        L_50(21, 39),
        L_MR1_51(22, 45),
        M_60(23, 64),
        N_70(24, 79),
        N_MR1_71(25, 88),
        O_80(26, 124),
        O_MR1_81(27, 131)
    }

    /** The art header at the start of the oat file (was `a`). */
    class Header @Throws(IOException::class) constructor(reader: DataReader) {

        /** Oat format version parsed from the 3-digit string (for example 79 for N). */
        @JvmField val version: Int

        /** Number of dex files in the oat. */
        @JvmField val dexFileCount: Int

        init {
            val magic = CharArray(4)
            val versionChars = CharArray(4)

            reader.readCharsInBytes(magic)
            if (magic[0] != 'o' || !magic[1].equals("get".toCharArray()) || magic[2] != 't') {
                throw IOException(String.format("Invalid art magic %c%c%c", magic[0], magic[1], magic[2]))
            }

            reader.readCharsInBytes(versionChars)
            version = DataReader.parseDigits(String(versionChars))
            reader.skipInt32()
            reader.skipInt32()
            reader.skipInt32()

            val buffer = reader.mappedByteBuffer
            dexFileCount = buffer.int
            repeat(4) { reader.skipInt32() }
            if (version < 52) {
                repeat(3) { reader.skipInt32() }
            }
            repeat(7) { reader.skipInt32() }

            // Key-value store: its length is read here and the bytes are skipped.
            reader.readCharsInBytes(CharArray(buffer.int))
        }
    }

    /**
     * One dex file's entry in the oat (was `c`). [dexFileOffset] is where its dex data starts,
     * and [vdexFile] is the matching `.vdex` next to the oat file, or null if none exists.
     */
    class OatDexFile @Throws(IOException::class) constructor(reader: DataReader, oatVersion: Int) {

        @JvmField val dexFileOffset: Int
        @JvmField val vdexFile: File?

        init {
            val buffer = reader.mappedByteBuffer
            val nameLength = buffer.int
            buffer.get(ByteArray(nameLength), 0, nameLength)
            reader.skipInt32()
            dexFileOffset = buffer.int

            val oatFile = reader.file
            val path = oatFile.absolutePath
            val dot = path.lastIndexOf('.')
            val extension = if (dot == -1) "" else path.substring(dot + 1)

            // The vdex sits next to the oat file with the same base name.
            var candidate = oatFile
            if (extension != "vdex") {
                val vdexPath = if (dot > 0) path.substring(0, dot + 1) + "vdex" else "$path.vdex"
                candidate = File(vdexPath)
            }

            vdexFile = if (candidate.exists()) {
                candidate
            } else {
                if (dexFileOffset == 28) {
                    throw IOException("dex_file_offset_=$dexFileOffset, does ${candidate.name} miss?")
                }
                null
            }

            if (oatVersion >= Version.N_70.oat) {
                reader.skipInt32()
                reader.skipInt32()
            }
        }
    }

    companion object {
        private const val OAT_BEGIN = 4096L
    }
}
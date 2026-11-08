package com.ims134.doclone.io



import java.io.Closeable
import java.io.File
import java.io.IOException
import java.nio.MappedByteBuffer

/**
 * Minimal ELF reader for oat files (JADX: `io.dy1`, Elf).
 *
 * Exposes what the oat extraction needs: the section table [sections], the section-name string table [strtab],
 * the reader [dataReader] positioned over the file, and whether the file is 64-bit ([is64]).
 *
 * Renames versus the Java: `Elf.k` is now [Section]; `Elf.a` is now [Header]. Section fields keep
 * their names (`a` = name offset, `b` = type, `a()` = file offset, `b()` = size) because the
 * oat code uses them.
 */
class Elf @Throws(IOException::class) constructor(file: File) : Closeable
{

    /** Reader over the whole file. Was `c`. */
    @JvmField val dataReader: DataReader = DataReader(file)

    private val parsed: Parsed = try {
        parse(dataReader, file)
    } catch (t: Throwable) {
        dataReader.close()
        throw t
    }

    /** True for an ELFCLASS64 file. Was `a`. */
    @JvmField val is64: Boolean = parsed.is64

    /** The ELF file header. Was `b`. */
    @JvmField val header: Header = parsed.header

    /** Section table. Was `d`. */
    @JvmField val sections: Array<Section> = parsed.sections

    /** Contents of the section-name string table. Was `e`. */
    @JvmField val strtab: ByteArray = parsed.strtab

    override fun close() {
        dataReader.close()
    }

    /** Common ELF header fields. [shoff] is the offset of the section table. */
    abstract class Header {
        @JvmField var shentsize: Short = 0
        @JvmField var shnum: Short = 0
        @JvmField var shstrndx: Short = 0

        abstract fun sectionTableOffset(): Long
    }

    /** 32-bit header (was `b`). */
    class Header32 : Header() {
        @JvmField var shoff: Int = 0

        override fun sectionTableOffset(): Long = shoff.toLong()
    }

    /** 64-bit header (was `f`). */
    class Header64 : Header() {
        @JvmField var shoff: Long = 0

        override fun sectionTableOffset(): Long = shoff
    }

    /** One section table entry (was `k`). */
    abstract class Section {
        /** Offset of the name in the section-name string table. Also used for symbol names. */
        @JvmField var sectionNameOffset: Int = 0

        /** Section type; 3 is SHT_STRTAB. */
        @JvmField var sectionType: Int = 0

        /** File offset of the section contents. */
        abstract fun getSectionContentOffset(): Long

        /** Size of the section contents. */
        abstract fun getSectionContentSize(): Int
    }

    /** 32-bit section entry (was `d`). */
    class Section32 : Section() {
        @JvmField var c: Int = 0
        @JvmField var d: Int = 0

        override fun getSectionContentOffset(): Long = c.toLong()

        override fun getSectionContentSize(): Int = d
    }

    /** 64-bit section entry (was `h`). */
    class Section64 : Section() {
        @JvmField var c: Long = 0
        @JvmField var d: Long = 0

        override fun getSectionContentOffset(): Long = c

        override fun getSectionContentSize(): Int = d.toInt()
    }

    private class Parsed(
        val is64: Boolean,
        val header: Header,
        val sections: Array<Section>,
        val strtab: ByteArray
    )

    companion object {
        private val MAGIC = charArrayOf(127.toChar(), 'E', 'L', 'F')

        @Throws(IOException::class)
        private fun parse(reader: DataReader, file: File): Parsed {
            val ident = CharArray(16)
            reader.readCharsInBytes(ident)
            if (ident[0] != MAGIC[0] || ident[1] != MAGIC[1] || ident[2] != MAGIC[2] || ident[3] != MAGIC[3]) {
                throw IOException("Invalid elf magic: $file")
            }

            val buffer: MappedByteBuffer = reader.mappedByteBuffer
            buffer.order(if (ident[5].code != 1) java.nio.ByteOrder.BIG_ENDIAN else java.nio.ByteOrder.LITTLE_ENDIAN)
            val is64 = ident[4].code == 2

            val header: Header = if (is64) readHeader64(reader, buffer) else readHeader32(reader, buffer)

            // e_flags, then e_ehsize, e_phentsize, e_phnum
            reader.skipInt32()
            buffer.short
            buffer.short
            buffer.short
            header.shentsize = buffer.short
            header.shnum = buffer.short
            header.shstrndx = buffer.short

            val sections = Array(header.shnum.toInt()) { i ->
                reader.setPosition((header.sectionTableOffset() + header.shentsize.toLong() * i).toInt())
                if (is64) readSection64(buffer, reader) else readSection32(buffer, reader)
            }

            val index = header.shstrndx.toInt()
            if (index < 0 || index >= sections.size) {
                throw IOException("Invalid e_shstrndx=${header.shstrndx.toInt()}")
            }
            val strSection = sections[index]
            if (strSection.sectionType != 3) {
                throw IOException("Wrong string section e_shstrndx=${header.shstrndx.toInt()}")
            }
            val size = strSection.getSectionContentSize()
            val strtab = ByteArray(size)
            reader.setPosition(strSection.getSectionContentOffset().toInt())
            buffer.get(strtab, 0, size)

            return Parsed(is64, header, sections, strtab)
        }

        private fun readHeader32(reader: DataReader, buffer: MappedByteBuffer): Header {
            val header = Header32()
            buffer.short
            buffer.short
            reader.skipInt32()          // e_version
            reader.skipInt32()          // e_entry
            buffer.int          // e_phoff
            header.shoff = buffer.int
            return header
        }

        private fun readHeader64(reader: DataReader, buffer: MappedByteBuffer): Header {
            val header = Header64()
            buffer.short
            buffer.short
            reader.skipInt32()          // e_version
            buffer.long         // e_entry
            buffer.long         // e_phoff
            header.shoff = buffer.long
            return header
        }

        private fun readSection32(buffer: MappedByteBuffer, reader: DataReader): Section {
            val section = Section32()
            section.sectionNameOffset = buffer.int
            section.sectionType = buffer.int
            reader.skipInt32()          // sh_flags
            reader.skipInt32()          // sh_addr
            section.c = buffer.int
            section.d = buffer.int
            buffer.int          // sh_link
            reader.skipInt32()          // sh_info
            reader.skipInt32()          // sh_addralign
            reader.skipInt32()          // sh_entsize
            return section
        }

        private fun readSection64(buffer: MappedByteBuffer, reader: DataReader): Section {
            val section = Section64()
            section.sectionNameOffset = buffer.int
            section.sectionType = buffer.int
            buffer.long         // sh_flags
            buffer.long         // sh_addr
            section.c = buffer.long
            section.d = buffer.long
            buffer.int          // sh_link
            reader.skipInt32()          // sh_info
            buffer.long         // sh_addralign
            buffer.long         // sh_entsize
            return section
        }
    }
}
package com.ims134.doclone.io


import android.os.Parcel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

abstract class PersistenceLayer(@JvmField val file: File) {

    abstract fun version(): Int                                    // setPosition()
    abstract fun readBody(parcel: Parcel)                          // e(Parcel)
    abstract fun isSupportedOnCurrentOs(): Boolean                 // h()
    abstract fun writeBody(parcel: Parcel)                         // j(Parcel)

    /**
     * Cleanup hook called from [load] when the file header is invalid.
     * Default: no-op. Concrete subclass [DeviceInfoPersistenceLayer]
     * overrides to `file.delete()`.
     */
    open fun deleteFile() {}                                       // readCharsInBytes()  ← FIXED

    open fun migrate(storedVersion: Int, currentVersion: Int): Boolean = false  // skipInt32(int,int)
    open fun isValidHeader(parcel: Parcel): Boolean = true         // g(Parcel)
    open fun writeHeader(parcel: Parcel) {}                        // i(Parcel)

    fun load(): Boolean {                                          // Styleable()
        val parcel = Parcel.obtain()
        try {
            FileInputStream(file).use { input ->
                val length = file.length().toInt()
                val bytes = ByteArray(length)
                if (input.read(bytes) != length) {
                    throw IOException("Unable to read Persistence file.")
                }
                parcel.unmarshall(bytes, 0, length)
                parcel.setDataPosition(0)
            }
            if (!isValidHeader(parcel)) {
                deleteFile()                                       // ← readCharsInBytes()
                throw IOException("Invalid persistence file.")
            }
            if (!isSupportedOnCurrentOs()) {
                throw IOException("OS upgrade")
            }
            val stored = parcel.readInt()
            val current = version()
            if (stored != current && !migrate(stored, current)) {
                throw IOException("Unable to process the bad version persistence file.")
            }
            readBody(parcel)
            return true
        } catch (_: Exception) {
            return false
        } finally {
            parcel.recycle()
        }
    }

    fun save() {                                                   // f()
        val parcel = Parcel.obtain()
        try {
            writeHeader(parcel)                                    // i()
            parcel.writeInt(version())                             // setPosition()
            writeBody(parcel)                                      // j()
            FileOutputStream(file).use { out ->
                out.write(parcel.marshall())
                out.flush()
            }
        } catch (_: Exception) {
        } finally {
            parcel.recycle()
        }
    }
}
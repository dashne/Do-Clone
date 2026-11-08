package com.ims134.doclone.io



import android.os.Parcel
import android.util.SparseArray
import com.ims134.doclone.clone.VDeviceInfo
import java.io.File

class DeviceInfoPersistenceLayer(file: File) : PersistenceLayer(file) {

    @JvmField
    var deviceManagerService: VDeviceManagerService? = null

    override fun version(): Int = 1                                // setPosition()

    override fun deleteFile() {                                    // readCharsInBytes()  ← override, not setPosition new method
        file.delete()
    }

    override fun readBody(parcel: Parcel) {                        // e()
        val devices = deviceManagerService!!.deviceInfoList
        devices.clear()
        var remaining = parcel.readInt()
        while (remaining > 0) {
            val key = parcel.readInt()
            devices.put(key, VDeviceInfo(parcel))
            remaining--
        }
    }

    override fun isSupportedOnCurrentOs(): Boolean = true          // h()

    override fun writeBody(parcel: Parcel) {                       // j()
        val devices = deviceManagerService!!.deviceInfoList
        val size = devices.size()
        parcel.writeInt(size)
        for (i in 0 until size) {
            val key = devices.keyAt(i)
            val value = devices.valueAt(i)
            parcel.writeInt(key)
            value!!.writeToParcel(parcel, 0)
        }
    }

    override fun writeHeader(parcel: Parcel) {                     // i()  — empty
    }
}
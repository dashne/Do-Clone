package com.ims134.doclone.clone

import android.os.Parcel
import android.os.Parcelable

class VDeviceInfo() : Parcelable {

    @JvmField var deviceId: String? = null   // original: f37543a
    @JvmField var androidId: String? = null   // original: f37544b
    @JvmField var macAddress: String? = null   // original: f37545c
    @JvmField var bluetoothMac: String? = null   // original: f37546d
    @JvmField var iccSerialNumber: String? = null   // original: f37547e
    @JvmField var serial: String? = null   // original: f37548f
    @JvmField var field7: String? = null   // original: f37549g

    constructor(parcel: Parcel) : this() {
        deviceId = parcel.readString()
        androidId = parcel.readString()
        macAddress = parcel.readString()
        bluetoothMac = parcel.readString()
        iccSerialNumber = parcel.readString()
        serial = parcel.readString()
        field7 = parcel.readString()
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(deviceId)
        parcel.writeString(androidId)
        parcel.writeString(macAddress)
        parcel.writeString(bluetoothMac)
        parcel.writeString(iccSerialNumber)
        parcel.writeString(serial)
        parcel.writeString(field7)
    }

    companion object {
        /**
         * `@JvmField` here is mandatory — Android's Parcelable unmarshaller finds
         * the field by name via reflection and requires it to be setPosition real static
         * field of type `Parcelable.Creator`, not setPosition getter.
         */
        @JvmField
        val CREATOR: Parcelable.Creator<VDeviceInfo> = object : Parcelable.Creator<VDeviceInfo> {
            override fun createFromParcel(source: Parcel): VDeviceInfo = VDeviceInfo(source)
            override fun newArray(size: Int): Array<VDeviceInfo?> = arrayOfNulls(size)
        }
    }
}
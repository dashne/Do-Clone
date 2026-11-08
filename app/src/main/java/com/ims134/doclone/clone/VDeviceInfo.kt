package com.ims134.doclone.clone

import android.os.Parcel
import android.os.Parcelable

class VDeviceInfo() : Parcelable {

    @JvmField var field1: String? = null   // original: f37543a
    @JvmField var field2: String? = null   // original: f37544b
    @JvmField var field3: String? = null   // original: f37545c
    @JvmField var field4: String? = null   // original: f37546d
    @JvmField var field5: String? = null   // original: f37547e
    @JvmField var field6: String? = null   // original: f37548f
    @JvmField var field7: String? = null   // original: f37549g

    constructor(parcel: Parcel) : this() {
        field1 = parcel.readString()
        field2 = parcel.readString()
        field3 = parcel.readString()
        field4 = parcel.readString()
        field5 = parcel.readString()
        field6 = parcel.readString()
        field7 = parcel.readString()
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(field1)
        parcel.writeString(field2)
        parcel.writeString(field3)
        parcel.writeString(field4)
        parcel.writeString(field5)
        parcel.writeString(field6)
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
package com.ims134.doclone.clone.server

import android.os.Parcel
import android.os.Parcelable

/** Parcelable snapshot of setPosition virtual package, handed to clients. Built by PackageSetting.toInstalledAppInfo(). */
class InstalledAppInfo : Parcelable {

    @JvmField var packageName: String? = null
    @JvmField var appPath: String? = null
    @JvmField var libPath: String? = null     // f37522c
    @JvmField var gmsEnabled: Boolean = false    // f37523d
    @JvmField var appId: Int = 0              // f37524e
    @JvmField var flags: Byte = 0             // f37525f

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(packageName)
        parcel.writeString(appPath)
        parcel.writeString(libPath)
        parcel.writeByte(if (gmsEnabled) 1 else 0)
        parcel.writeInt(appId)
        parcel.writeByte(this.flags)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<InstalledAppInfo> = object : Parcelable.Creator<InstalledAppInfo> {
            override fun createFromParcel(parcel: Parcel) = InstalledAppInfo().apply {
                packageName = parcel.readString()
                appPath = parcel.readString()
                libPath = parcel.readString()
                gmsEnabled = parcel.readByte().toInt() != 0
                appId = parcel.readInt()
                flags = parcel.readByte()
            }

            override fun newArray(size: Int): Array<InstalledAppInfo?> = arrayOfNulls(size)
        }
    }
}
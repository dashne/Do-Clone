package com.ims134.doclone.clone.server

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

@TargetApi(21)
class SessionParams : Parcelable {

    var mode: Int = 0
    var installFlags: Int = 0
    var installLocation: Int = 0
    var sizeBytes: Long = 0
    var appPackageName: String? = null
    var appIcon: Bitmap? = null
    var appLabel: String? = null
    var appIconLastModified: Long = 0
    var originatingUri: Uri? = null
    var referrerUri: Uri? = null
    var abiOverride: String? = null
    var volumeUuid: String? = null
    var grantedRuntimePermissions: Array<String>? = null

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(mode)
        parcel.writeInt(installFlags)
        parcel.writeInt(installLocation)
        parcel.writeLong(sizeBytes)
        parcel.writeString(appPackageName)
        parcel.writeParcelable(appIcon, flags)
        parcel.writeString(appLabel)
        parcel.writeLong(appIconLastModified)
        parcel.writeParcelable(originatingUri, flags)
        parcel.writeParcelable(referrerUri, flags)
        parcel.writeString(abiOverride)
        parcel.writeString(volumeUuid)
        parcel.writeStringArray(grantedRuntimePermissions)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SessionParams> = object : Parcelable.Creator<SessionParams> {
            override fun createFromParcel(parcel: Parcel): SessionParams =
                SessionParams().apply {
                    mode = parcel.readInt()
                    installFlags = parcel.readInt()
                    installLocation = parcel.readInt()
                    sizeBytes = parcel.readLong()
                    appPackageName = parcel.readString()
                    appIcon = parcel.readParcelable<Bitmap>(Bitmap::class.java.classLoader)
                    appLabel = parcel.readString()
                    appIconLastModified = parcel.readLong()
                    originatingUri = parcel.readParcelable<Uri>(Uri::class.java.classLoader)
                    referrerUri = parcel.readParcelable<Uri>(Uri::class.java.classLoader)
                    abiOverride = parcel.readString()
                    volumeUuid = parcel.readString()
                    grantedRuntimePermissions = parcel.createStringArray()
                }

            override fun newArray(size: Int): Array<SessionParams?> = arrayOfNulls(size)
        }
    }
}
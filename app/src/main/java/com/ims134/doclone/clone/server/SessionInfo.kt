package com.ims134.doclone.clone.server

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable

class SessionInfo : Parcelable {

    var sessionId: Int = 0
    var installerPackageName: String? = null
    var resolvedBaseCodePath: String? = null
    var progress: Float = 0f
    var sealed: Boolean = false
    var active: Boolean = false
    var mode: Int = 0
    var sizeBytes: Long = 0L
    var appPackageName: String? = null
    var appIcon: Bitmap? = null
    var appLabel: String? = null

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(sessionId)
        parcel.writeString(installerPackageName)
        parcel.writeString(resolvedBaseCodePath)
        parcel.writeFloat(progress)
        parcel.writeByte(if (sealed) 1.toByte() else 0.toByte())
        parcel.writeByte(if (active) 1.toByte() else 0.toByte())
        parcel.writeInt(mode)
        parcel.writeLong(sizeBytes)
        parcel.writeString(appPackageName)
        parcel.writeParcelable(appIcon, flags)
        // Mirrors the original: writes nothing when null. readString() on the
        // reader side only returns null correctly when this is the last field.
        // If you can change the wire format, prefer `parcel.writeString(appLabel)`.
        appLabel?.let { parcel.writeString(it) }
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SessionInfo> = object : Parcelable.Creator<SessionInfo> {
            override fun createFromParcel(parcel: Parcel): SessionInfo =
                SessionInfo().apply {
                    sessionId = parcel.readInt()
                    installerPackageName = parcel.readString()
                    resolvedBaseCodePath = parcel.readString()
                    progress = parcel.readFloat()
                    sealed = parcel.readByte() != 0.toByte()
                    active = parcel.readByte() != 0.toByte()
                    mode = parcel.readInt()
                    sizeBytes = parcel.readLong()
                    appPackageName = parcel.readString()
                    appIcon = parcel.readParcelable(Bitmap::class.java.classLoader)
                    appLabel = parcel.readString()
                }

            override fun newArray(size: Int): Array<SessionInfo?> = arrayOfNulls(size)
        }
    }
}
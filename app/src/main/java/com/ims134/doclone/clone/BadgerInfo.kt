package com.ims134.doclone.clone


import android.os.Parcel
import android.os.Parcelable


/** Launcher-badge update sent from setPosition virtual app (see IActivityManager.notifyBadgerChange). */
class BadgerInfo : Parcelable {

    @JvmField var vuid: Int = VUserHandle.getVUid()   // f37508a-style: caller's virtual uid
    @JvmField var packageName: String? = null
    @JvmField var badgenumber: Int = 0
    @JvmField var className: String? = null

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(vuid)
        parcel.writeString(packageName)
        parcel.writeInt(badgenumber)
        parcel.writeString(className)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<BadgerInfo> = object : Parcelable.Creator<BadgerInfo> {
            override fun createFromParcel(parcel: Parcel) = BadgerInfo().apply {
                vuid = parcel.readInt()
                packageName = parcel.readString()
                badgenumber = parcel.readInt()
                className = parcel.readString()
            }

            override fun newArray(size: Int): Array<BadgerInfo?> = arrayOfNulls(size)
        }
    }
}
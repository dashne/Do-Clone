package com.ims134.doclone.clone

import android.os.Parcel
import android.os.Parcelable

class VUserInfo() : Parcelable {

    @JvmField var id: Int = 0
    @JvmField var serialNumber: Int = 0
    @JvmField var Primary: String? = null          // the user's name
    @JvmField var iconPath: String? = null
    @JvmField var flags: Int = 0
    @JvmField var created: Long = 0
    @JvmField var lastLoggedIn: Long = 0
    @JvmField var profileGroupId: Int = 0
    @JvmField var partial: Boolean = false
    @JvmField var guestToRemove: Boolean = false

    constructor(id: Int, name: String?, iconPath: String?, flags: Int) : this() {
        this.id = id
        this.Primary = name
        this.flags = flags
        this.iconPath = iconPath
        this.profileGroupId = -1
    }

    override fun describeContents(): Int = 0

    override fun toString(): String = "UserInfo{$id:$Primary:${Integer.toHexString(flags)}}"

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(Primary)
        parcel.writeString(iconPath)
        parcel.writeInt(this.flags)
        parcel.writeInt(serialNumber)
        parcel.writeLong(created)
        parcel.writeLong(lastLoggedIn)
        parcel.writeInt(if (partial) 1 else 0)
        parcel.writeInt(profileGroupId)
        parcel.writeInt(if (guestToRemove) 1 else 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<VUserInfo> = object : Parcelable.Creator<VUserInfo> {
            override fun createFromParcel(parcel: Parcel): VUserInfo {
                val info = VUserInfo()
                info.id = parcel.readInt()
                info.Primary = parcel.readString()
                info.iconPath = parcel.readString()
                info.flags = parcel.readInt()
                info.serialNumber = parcel.readInt()
                info.created = parcel.readLong()
                info.lastLoggedIn = parcel.readLong()
                info.partial = parcel.readInt() != 0
                info.profileGroupId = parcel.readInt()
                info.guestToRemove = parcel.readInt() != 0
                return info
            }

            override fun newArray(size: Int): Array<VUserInfo?> = arrayOfNulls(size)
        }
    }
}
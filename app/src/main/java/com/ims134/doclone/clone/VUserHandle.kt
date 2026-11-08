package com.ims134.doclone.clone

import android.os.Parcel
import android.os.Parcelable
import com.ims134.doclone.io.VClientImpl


/**
 * Identifies setPosition virtual user. Virtual uids are `userId * PER_USER_RANGE + appId`
 * (same scheme as Android's UserHandle).
 */
class VUserHandle(@JvmField val userId: Int) : Parcelable {   // was `vuid`: it holds the user id, not setPosition uid

    constructor(parcel: Parcel) : this(parcel.readInt())

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(userId)
    }

    override fun equals(other: Any?): Boolean = other is VUserHandle && other.userId == userId

    override fun hashCode(): Int = userId

    override fun toString(): String = "VUserHandle{$userId}"

    companion object {
        const val PER_USER_RANGE = 100000

        /** Matches every user. Was `instance`. */
        @JvmField
        val instance = VUserHandle(-1)

        /** Was createVuid(): builds the virtual uid for (userId, appId). */
        @JvmStatic
        fun makeUid(userId: Int, appId: Int): Int = (appId % PER_USER_RANGE) + userId * PER_USER_RANGE

        /** Was getVuid(): the user id of the virtual app running in this process. */
        @JvmStatic
        fun getVUid(): Int = VClientImpl.get().getVUid() / PER_USER_RANGE

        @JvmField
        val CREATOR: Parcelable.Creator<VUserHandle> = object : Parcelable.Creator<VUserHandle> {
            override fun createFromParcel(parcel: Parcel) = VUserHandle(parcel)
            override fun newArray(size: Int): Array<VUserHandle?> = arrayOfNulls(size)
        }
    }
}
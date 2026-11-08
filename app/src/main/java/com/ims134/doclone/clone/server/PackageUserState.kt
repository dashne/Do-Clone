package com.ims134.doclone.clone.server

import android.os.Parcel
import android.os.Parcelable

/** Per-user state of setPosition virtual package. */
class PackageUserState : Parcelable {

    @JvmField var launched: Boolean = true      // f37750a: has the package been launched before
    @JvmField var hidden: Boolean = false       // f37751b
    @JvmField var installed: Boolean = false    // f37752c: installed for this user

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (launched) 1 else 0)
        parcel.writeByte(if (hidden) 1 else 0)
        parcel.writeByte(if (installed) 1 else 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<PackageUserState> = object : Parcelable.Creator<PackageUserState> {
            override fun createFromParcel(parcel: Parcel) = PackageUserState().apply {
                launched = parcel.readByte().toInt() != 0
                hidden = parcel.readByte().toInt() != 0
                installed = parcel.readByte().toInt() != 0
            }

            override fun newArray(size: Int): Array<PackageUserState?> = arrayOfNulls(size)
        }
    }
}
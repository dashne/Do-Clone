package com.ims134.doclone.clone.server

import android.os.Parcel
import android.os.Parcelable

class VSConfig() : Parcelable {
    var isEnabled: Boolean = false
    var virtualStorage: String? = null

    constructor(parcel: Parcel) : this() {
        isEnabled = parcel.readByte().toInt() != 0
        virtualStorage = parcel.readString()
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte(if (isEnabled) 1 else 0)
        dest.writeString(virtualStorage)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<VSConfig>
        {
            override fun createFromParcel(source: Parcel): VSConfig = VSConfig(source)
            override fun newArray(size: Int): Array<VSConfig?> = arrayOfNulls(size)
        }
    }
}

package com.ims134.doclone.clone.server

import android.os.Parcel
import android.os.Parcelable

/** Per-account package visibility (AccountManager.VISIBILITY_*). Persisted in account-visibility-list.ini. */
class VAccountVisibility() : Parcelable {

    var userId: Int = 0                                   // a
    var name: String = ""                                 // b  account name
    var type: String = ""                                 // c  account type
    var visibility: HashMap<String, Int> = HashMap()      // d  package -> visibility

    /** Field order is the on-disk format: do not reorder. */
    constructor(parcel: Parcel) : this() {
        userId = parcel.readInt()
        name = parcel.readString()!!
        type = parcel.readString()!!
        val count = parcel.readInt()
        visibility = HashMap(count)
        repeat(count) { visibility[parcel.readString().orEmpty()] = parcel.readInt() }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(userId)
        dest.writeString(name)
        dest.writeString(type)
        dest.writeInt(visibility.size)
        for ((pkg, value) in visibility) {
            dest.writeString(pkg)
            dest.writeInt(value)
        }
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<VAccountVisibility> {
            override fun createFromParcel(source: Parcel) = VAccountVisibility(source)
            override fun newArray(size: Int): Array<VAccountVisibility?> = arrayOfNulls(size)
        }
    }
}
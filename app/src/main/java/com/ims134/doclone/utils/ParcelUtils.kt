package com.ims134.doclone.utils

import android.os.Parcel
import android.os.Parcelable


object ParcelUtils
{
    fun readNullableParcelable(parcel: Parcel, creator: Parcelable.Creator<*>): Any?
    {
        if (parcel.readInt() != 0) {
            return creator.createFromParcel(parcel)
        }
        return null
    }

    fun writeParcelableList(parcel: Parcel, list: MutableList<*>?)
    {
        if (list == null) {
            parcel.writeInt(-1)
            return
        }
        val size = list.size
        parcel.writeInt(size)
        for (i in 0..<size) {
            writeNullableParcelable<Parcelable?>(parcel, list.get(i) as Parcelable?, 1)
        }
    }

    fun <T : Parcelable?> writeNullableParcelable(parcel: Parcel, t: T?, i: Int)
    {
        if (t == null) {
            parcel.writeInt(0)
        } else {
            parcel.writeInt(1)
            t.writeToParcel(parcel, i)
        }
    }
}

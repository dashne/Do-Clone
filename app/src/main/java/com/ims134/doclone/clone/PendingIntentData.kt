package com.ims134.doclone.clone

import android.app.PendingIntent
import android.os.Parcel
import android.os.Parcelable

/** A PendingIntent together with the package that created it (see IActivityManager.getPendingIntent). */
class PendingIntentData : Parcelable {

    @JvmField var creatorPackage: String? = null      // f37526a
    @JvmField var pendingIntent: PendingIntent? = null // f37527b

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(creatorPackage)
        // Same wire format as PendingIntent.writeToParcel, but null-safe.
        PendingIntent.writePendingIntentOrNullToParcel(pendingIntent, parcel)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<PendingIntentData> = object : Parcelable.Creator<PendingIntentData> {
            override fun createFromParcel(parcel: Parcel) = PendingIntentData().apply {
                creatorPackage = parcel.readString()
                pendingIntent = PendingIntent.readPendingIntentOrNullFromParcel(parcel)
            }

            override fun newArray(size: Int): Array<PendingIntentData?> = arrayOfNulls(size)
        }
    }
}
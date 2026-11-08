package com.ims134.doclone.clone

import android.content.ComponentName
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable

/** A broadcast to deliver to the virtual users: the Intent, target user (-1 = all), package and optional component. */
class BroadcastIntentData(
    @JvmField var userId: Int,
    @JvmField var intent: Intent,
    @JvmField var pkg: String?,
    @JvmField var componentName: ComponentName?,
) : Parcelable {

    /** Copy constructor (shares the same Intent instance, as in the original). */
    constructor(other: BroadcastIntentData) : this(other.userId, other.intent, other.pkg, other.componentName)

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readParcelable<Intent>(Intent::class.java.classLoader)!!,
        parcel.readString(),
        parcel.readParcelable<ComponentName>(ComponentName::class.java.classLoader),
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(userId)
        parcel.writeParcelable(intent, flags)
        parcel.writeString(pkg)
        parcel.writeParcelable(componentName, flags)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<BroadcastIntentData> = object : Parcelable.Creator<BroadcastIntentData> {
            override fun createFromParcel(parcel: Parcel) = BroadcastIntentData(parcel)
            override fun newArray(size: Int): Array<BroadcastIntentData?> = arrayOfNulls(size)
        }
    }
}
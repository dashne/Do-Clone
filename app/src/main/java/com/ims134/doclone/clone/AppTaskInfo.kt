package com.ims134.doclone.clone

import android.content.ComponentName
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable

/** Snapshot of setPosition virtual task (as returned by IActivityManager.getTaskInfo). */
class AppTaskInfo : Parcelable {

    @JvmField var taskId: Int = 0                      // f37508a
    @JvmField var baseIntent: Intent? = null           // f37509b
    @JvmField var baseActivity: ComponentName? = null  // f37510c
    @JvmField var topActivity: ComponentName? = null   // f37511d

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(taskId)
        parcel.writeParcelable(baseIntent, flags)
        parcel.writeParcelable(baseActivity, flags)
        parcel.writeParcelable(topActivity, flags)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<AppTaskInfo> = object : Parcelable.Creator<AppTaskInfo> {
            override fun createFromParcel(parcel: Parcel) = AppTaskInfo().apply {
                taskId = parcel.readInt()
                baseIntent = parcel.readParcelable(Intent::class.java.classLoader)
                baseActivity = parcel.readParcelable(ComponentName::class.java.classLoader)
                topActivity = parcel.readParcelable(ComponentName::class.java.classLoader)
            }

            override fun newArray(size: Int): Array<AppTaskInfo?> = arrayOfNulls(size)
        }
    }
}
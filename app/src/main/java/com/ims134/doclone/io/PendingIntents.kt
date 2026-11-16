package com.ims134.doclone.io

import android.os.IBinder
import com.ims134.doclone.clone.PendingIntentData

class PendingIntents {

    // get
    val mPendingIntents = HashMap<IBinder, PendingIntentData>()

    // b. Key type is unknown in the paste.
    val mPendingIntentsByKey = HashMap<Any, Int>()

    // get(IBinder)
    fun getPendingIntentData(binder: IBinder): PendingIntentData? {
        synchronized(mPendingIntents) {
            return mPendingIntents[binder]
        }
    }
}
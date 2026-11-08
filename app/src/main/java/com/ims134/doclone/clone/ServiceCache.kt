package com.ims134.doclone.clone

import android.os.IBinder
import androidx.collection.ArrayMap

class ServiceCache {
    companion object
    {
        val sCache = ArrayMap<String, IBinder>(5)

        fun addService(key: String, binder: IBinder)
        {
            sCache[key]=binder
        }
    }
}
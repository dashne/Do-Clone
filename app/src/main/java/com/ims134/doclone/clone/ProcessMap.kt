package com.ims134.doclone.clone

import android.util.SparseArray
import androidx.collection.ArrayMap
import androidx.core.util.isEmpty

class ProcessMap<E>
{
    val cache: ArrayMap<String, SparseArray<E>> = ArrayMap()

    fun get(uid: Int, processName: String): E? {
        val sparseArray = cache[processName] ?: return null
        return sparseArray[uid]          // SparseArray.get
    }

    fun remove(uid: Int, processName: String) {
        val sparseArray = cache[processName] ?: return
        sparseArray.remove(uid)
        if (sparseArray.isEmpty()) {
            cache.remove(processName)
        }
    }
}
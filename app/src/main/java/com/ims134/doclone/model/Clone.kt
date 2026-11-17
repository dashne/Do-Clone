package com.ims134.doclone.model

import android.graphics.Bitmap
import androidx.room3.Entity
import androidx.room3.PrimaryKey


@Entity
data class Clone(@PrimaryKey val id: Long, val packageName:String, val appPath:String, val label:String, val description:String, val index: Int,
                 val cloneTime:Long, val notificationEnabled: Boolean, var lockerState: Int, val launched: Int, val icon: Bitmap
) {

    fun getVUid(): Int {
        return (this.index shr 16) and 0xFFFF
    }
}
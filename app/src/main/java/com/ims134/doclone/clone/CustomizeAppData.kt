package com.ims134.doclone.clone


import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.io.BitmapUtils
import androidx.core.content.edit


class CustomizeAppData : Parcelable {

    var hue: Int = 0
    var sat: Int = 0
    var light: Int = 0
    var badge: Boolean = false
    var label: String? = null
    var pkgName: String? = null
    var hasCustomLabel: Boolean = false
    var userId: Int = 0
    var prefName: String? = null
    var notification: Boolean = false
    var lock: Long = 0L

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(hue)
        parcel.writeInt(sat)
        parcel.writeInt(light)
        parcel.writeByte(if (badge) 1.toByte() else 0.toByte())
        parcel.writeString(label)
        parcel.writeString(pkgName)
        parcel.writeByte(if (hasCustomLabel) 1.toByte() else 0.toByte())
        parcel.writeInt(userId)
        parcel.writeString(prefName)
        parcel.writeByte(if (notification) 1.toByte() else 0.toByte())
        parcel.writeLong(lock)
    }

    /** Was `a()`. Returns the app icon rendered with the current hue/sat/light customization. */
    fun getIcon(): Bitmap? =
        BitmapUtils.loadCloneIcon(appContext, pkgName!!, userId)

    /** Was `c()`. Persists the customization back to the per-app SharedPreferences. */
    fun save() {
        appContext
            .getSharedPreferences(prefName, Context.MODE_PRIVATE)
            .edit(commit = true) {
                putBoolean("badge", badge)
                    .putInt("hue", hue)
                    // NOTE: `sat` is deliberately not saved here — preserved from the original.
                    .putInt("light", light)
                    .putString("label", label)
                    .putBoolean("notification", notification)
                    .putLong("lock", lock)
            }
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<CustomizeAppData> =
            object : Parcelable.Creator<CustomizeAppData> {
                override fun createFromParcel(parcel: Parcel): CustomizeAppData =
                    CustomizeAppData().apply {
                        hue = parcel.readInt()
                        sat = parcel.readInt()
                        light = parcel.readInt()
                        badge = parcel.readByte() != 0.toByte()
                        label = parcel.readString()
                        pkgName = parcel.readString()
                        hasCustomLabel = parcel.readByte() != 0.toByte()
                        userId = parcel.readInt()
                        prefName = parcel.readString()
                        notification = parcel.readByte() != 0.toByte()
                        lock = parcel.readLong()
                    }

                override fun newArray(size: Int): Array<CustomizeAppData?> = arrayOfNulls(size)
            }

        /**
         * Was `b(int, String)`. Loads customization for `(userId, pkgName)` from
         * SharedPreferences. If no custom label is stored, falls back to the system
         * label — decorated with the `clone_label_tag` mipmap if it exists, or Stub
         * trailing `+` otherwise.
         */
        @JvmStatic
        fun get(userId: Int, pkgName: String): CustomizeAppData {
            val data = CustomizeAppData()
            val prefName = VirtualCore.makeCloneName(userId, "app_custom_$pkgName")
            data.prefName = prefName

            val core = VirtualCore.gCore
            val prefs = appContext.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            data.badge = prefs.getBoolean("badge", true)
            data.hue = prefs.getInt("hue", 127)
            data.sat = prefs.getInt("sat", 127)
            data.light = prefs.getInt("light", 127)
            data.label = prefs.getString("label", null)
            data.notification = prefs.getBoolean("notification", true)
            data.lock = prefs.getLong("lock", 0L)
            data.pkgName = pkgName
            data.userId = userId
            data.hasCustomLabel = data.label != null

            if (TextUtils.isEmpty(data.label)) {
                try {
                    val appInfo: ApplicationInfo =
                        core.unhookPackageManager.getApplicationInfo(pkgName, 0)
                    val tagId = appContext.resources.getIdentifier(
                        "clone_label_tag", "mipmap", core.hostPackageName
                    )
                    data.label = if (tagId != 0) {
                        appContext.resources.getString(
                            tagId, appInfo.loadLabel(core.unhookPackageManager)
                        )
                    } else {
                        appInfo.loadLabel(core.unhookPackageManager).toString() + "+"
                    }
                } catch (_: Exception) {
                }
            }
            return data
        }
    }
}
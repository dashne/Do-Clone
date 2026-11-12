package com.ims134.doclone.io


import android.annotation.SuppressLint
import android.content.AttributionSource
import android.os.Build
import android.os.Parcel
import android.os.Parcelable

/** was io.mw: copies an AttributionSource (Android 12+) with another uid / pid / package name. */
object AttributionSourceCompat {

    /** was get: parcel copy of [source] with the uid, pid (when the field exists) and package name set */
    @Suppress("DEPRECATION")
    @JvmStatic
    fun withUidPidPackage(source: Any?, uid: Int, pid: Int, packageName: String?): Any? {
        val type = AttributionSourceState .TYPE
        if (Build.VERSION.SDK_INT >= 31 && source != null && type != null && type.isInstance(source) && source is Parcelable) {
            val parcel = Parcel.obtain()
            try {
                parcel.writeParcelable(source, 0)
                parcel.setDataPosition(0)
                val copy = parcel.readParcelable<Parcelable>(source.javaClass.classLoader) ?: return source
                AttributionSourceState .uid!!.set(copy, uid)
                AttributionSourceState .packageName!!.set(copy, packageName)
                AttributionSourceState .pid?.set(copy, pid)
                return copy
            } catch (_: Throwable) {
            } finally {
                parcel.recycle()
            }
        }
        return source
    }

    /** was b: rebuilds [source] through AttributionSource.Builder for [uid] / [packageName], keeping tag, next, token and renounced permissions */
    @SuppressLint("NewApi")
    @JvmStatic
    fun withPackage(uid: Int, packageName: String?, source: Any?): Any? {
        val sdk = Build.VERSION.SDK_INT
        val type = AttributionSourceMirror.TYPE
        if (sdk < 31 || source == null || type == null || !type.isInstance(source)) {
            return source
        }
        val old = source as AttributionSource
        val oldState = AttributionSourceMirror.mAttributionSourceState!!.get(old)
        val builder: AttributionSource.Builder
        if (sdk >= 33) {
            builder = AttributionSource.Builder(old).setPackageName(packageName)
            if (sdk < 36) {
                builder.setAttributionTag(old.attributionTag).setNext(old.next)
                if (sdk >= 34) {
                    builder.setPid(old.pid)
                }
                if (sdk >= 35) {
                    builder.setDeviceId(old.deviceId)
                }
            }
        } else {
            builder = AttributionSource.Builder(uid).setPackageName(packageName)
                .setAttributionTag(old.attributionTag).setNext(old.next)
        }
        val result = builder.build()
        val state = AttributionSourceMirror.mAttributionSourceState!!.get(result)
        if (state != null) {
            AttributionSourceState .uid!!.set(state, uid)
            AttributionSourceState .packageName!!.set(state, packageName)
            val token = AttributionSourceState .token
            if (oldState != null && token != null) {
                token.set(state, token.get(oldState))
            }
            val renounced = AttributionSourceState .renouncedPermissions
            if (oldState != null && renounced != null) {
                val permissions: Array<String>? = renounced.get(oldState)
                renounced.set(state, permissions?.clone())
            }
        }
        return result
    }
}
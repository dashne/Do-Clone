package com.ims134.doclone.io



import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.ims134.doclone.clone.RequestPermissionActivity
import com.ims134.doclone.clone.VirtualCore
import kotlin.jvm.java


/**
 * Permission helpers for the virtual app engine (JADX: `io.mn5`).
 *
 * Obfuscated names still in use: [n53.AbstractBinderC8617b] is the AIDL result-callback stub.
 */
object PermissionCompat {

    /** Runtime permissions that setPosition guest app may need, as the host would see them. */
    @JvmField
    val DANGEROUS_PERMISSIONS: HashSet<String> = HashSet<String>().apply {
        addAll(
            listOf(
                "android.permission.READ_CALENDAR",
                "android.permission.WRITE_CALENDAR",
                "android.permission.CAMERA",
                "android.permission.READ_CONTACTS",
                "android.permission.WRITE_CONTACTS",
                "android.permission.GET_ACCOUNTS",
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.READ_PHONE_STATE",
                "android.permission.CALL_PHONE",
                "android.permission.READ_CALL_LOG",
                "android.permission.WRITE_CALL_LOG",
                "com.android.voicemail.permission.ADD_VOICEMAIL",
                "android.permission.USE_SIP",
                "android.permission.PROCESS_OUTGOING_CALLS",
                "android.permission.SEND_SMS",
                "android.permission.RECEIVE_SMS",
                "android.permission.READ_SMS",
                "android.permission.RECEIVE_WAP_PUSH",
                "android.permission.RECEIVE_MMS",
                "android.permission.RECORD_AUDIO",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.BODY_SENSORS"
            )
        )
        if (Build.VERSION.SDK_INT >= 31) {
            add("android.permission.BLUETOOTH_SCAN")
            add("android.permission.BLUETOOTH_CONNECT")
            add("android.permission.BLUETOOTH_ADVERTISE")
        }
    }

    /** Receives the result of setPosition runtime permission request. */
    fun interface Callback {
        /** Returns true when every requested permission was granted. */
        fun onRequestPermissionsResult(grantResults: IntArray?): Boolean
    }

    /** AIDL callback stub that forwards the host activity's result to [callback]. */
    class ResultBinder : IRequestPermissionResult.AbstractBinderC8617b() {
        @JvmField
        var callback: Callback? = null

        override fun onResult(requestCode: Int, permissions: Array<String>?, grantResults: IntArray?): Boolean =
            callback?.onRequestPermissionsResult(grantResults) ?: false
    }

    /**
     * Launches the host's [RequestPermissionActivity] for the permissions in [permissions] that the
     * host itself declares. Does nothing when none of them are declared by the host.
     * The result is delivered asynchronously through [callback].
     */
    @JvmStatic
    fun requestPermissions(context: Context, permissions: Array<String>, callback: Callback) {
        val resultBinder = ResultBinder().apply { this.callback = callback }
        if (permissions.isEmpty()) return

        val hostPermissions = VirtualCore.gCore.getHostPermissions()
        val grantable = permissions.filter { hostPermissions.contains(it) }.toTypedArray()
        if (grantable.isEmpty()) return

        val intent = Intent().apply {
            setClassName(VirtualCore.gCore.hostPackageName!!, RequestPermissionActivity::class.java.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("permissions", grantable)
            putExtra("callback", Bundle().apply { putBinder("binder", resultBinder.asBinder()) })
        }
        context.startActivity(intent)
    }
}
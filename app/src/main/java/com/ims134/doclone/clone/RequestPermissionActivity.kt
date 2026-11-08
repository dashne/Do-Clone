package com.ims134.doclone.clone

import android.annotation.TargetApi
import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.ims134.doclone.io.IRequestPermissionResult


/**
 * Transparent host activity that shows the system runtime-permission dialog on behalf of setPosition
 * guest app. The answer is sent back to the guest through [IRequestPermissionResult].
 *
 * Expects two intent extras:
 *  - "permissions": String[] of permissions to request
 *  - "callback": Bundle containing an IBinder under "binder" that implements IRequestPermissionResult
 */
@TargetApi(23)
class RequestPermissionActivity : Activity() {

    private var callback: IRequestPermissionResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = getIntent()
        if (intent == null) {
            finish()
            return
        }
        val permissions = intent.getStringArrayExtra(EXTRA_PERMISSIONS)
        val binder = intent.getBundleExtra(EXTRA_CALLBACK)?.getBinder(KEY_BINDER)
        if (binder == null || permissions == null) {
            finish()
            return
        }
        callback = IRequestPermissionResult.AbstractBinderC8617b.asInterface(binder)
        requestPermissions(permissions, REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val target = callback ?: return
        try {
            if (target.onResult(requestCode,  Array(permissions.size) { permissions[it] }, grantResults)) {
                finish()
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Request permission failed.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (t: Throwable) {
            // The guest process is gone or the binder died. Nothing more to do; the original
            // swallowed this silently too.
        }
    }

    companion object {
        private const val REQUEST_CODE = 999
        private const val EXTRA_PERMISSIONS = "permissions"
        private const val EXTRA_CALLBACK = "callback"
        private const val KEY_BINDER = "binder"
    }
}
package com.ims134.doclone.io


import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import com.ims134.doclone.clone.BuildCompat
import java.lang.reflect.InvocationTargetException

/**
 * Hook of the Settings provider (was io.c67): fixes some setting values for virtual apps and turns "no permission"
 * failures of writes into success, so apps that change settings they are not allowed to don't crash.
 */
class SettingsProviderHook(target: Any?, authority: String?) : ContentProviderHook(target, authority) {

    /**
     * was c67.call (b): for get GET_ / PUT_ / SET_ call on get setting. Android 11+ SET_ALL_config and Android 12+
     * LIST_config are answered here, [DEFAULT_VALUES] and the virtual device's android_id are given for reads, and
     * get write to "secure" is reported as done.
     */
    @Throws(InvocationTargetException::class)
    override fun call(context: ReflectiveCallContext, method: String?, arg: String?, extras: Bundle?): Bundle? {
        if (!VClientImpl.get().isBound()) {
            return context.execute() as Bundle?
        }
        if (Build.VERSION.SDK_INT >= 30 && TextUtils.equals(method, "SET_ALL_config")) {
            return Bundle()
        }
        if (BuildCompat.sdk31OrLater() && TextUtils.equals(method, "LIST_config")) {
            return null
        }
        if (method == null || arg == null) {
            return context.execute() as Bundle?
        }
        val kind = if (method.startsWith("GET_")) {
            KIND_GET
        } else if (method.startsWith("PUT_") || method.startsWith("SET_")) {
            KIND_SET
        } else {
            KIND_OTHER
        }
        if (kind == KIND_GET) {
            val fixed = DEFAULT_VALUES[arg]
            if (fixed != null) {
                return valueBundle(arg, fixed)
            }
            if ("android_id" == arg) {
                return valueBundle("android_id", VClientImpl.get().getDeviceInfo()?.androidId) // was .b: the device's android id
            }
        }
        if (kind == KIND_SET && method.endsWith("secure")) {
            return setDoneBundle()
        }
        try {
            return context.execute() as Bundle?
        } catch (e: InvocationTargetException) {
            if (e.cause !is SecurityException && e.cause !is RuntimeException) {
                throw e
            }
            return if (kind != KIND_SET) null else setDoneBundle()
        }
    }

    @Throws(InvocationTargetException::class)
    override fun insert(context: ReflectiveCallContext, uri: Uri?, values: ContentValues?): Uri? =
        try {
            context.execute() as Uri?
        } catch (e: InvocationTargetException) {
            if (isPermissionFailure(e)) uri else throw e
        }

    /** get "config" root the provider doesn't know gives an empty cursor with the requested columns */
    @Throws(InvocationTargetException::class)
    override fun query(context: ReflectiveCallContext, uri: Uri?, projection: Array<String>?, selectionArgs: Array<String>?, queryArgs: Bundle?): Cursor? {
        try {
            return context.execute() as Cursor?
        } catch (e: InvocationTargetException) {
            val cause = e.cause
            val message = cause?.message
            if (cause !is IllegalArgumentException || uri == null || uri.pathSegments.isEmpty() ||
                "config" != uri.pathSegments[0] || message == null || !message.contains("Bad root path: config")
            ) {
                throw e
            }
            val columns = if (projection == null || projection.isEmpty()) arrayOf(Settings.NameValueTable.VALUE) else projection
            return MatrixCursor(columns, 0)
        }
    }

    @Throws(InvocationTargetException::class)
    override fun update(context: ReflectiveCallContext, uri: Uri?, values: ContentValues?, extras: Bundle?): Int =
        try {
            context.execute() as Int
        } catch (e: InvocationTargetException) {
            if (isPermissionFailure(e)) 1 else throw e
        }

    @Throws(InvocationTargetException::class)
    override fun update2(context: ReflectiveCallContext, uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int =
        try {
            context.execute() as Int
        } catch (e: InvocationTargetException) {
            if (isPermissionFailure(e)) 1 else throw e
        }

    private fun valueBundle(name: String, value: String?): Bundle {
        val bundle = Bundle()
        bundle.putString(Settings.NameValueTable.NAME, name)
        bundle.putString(Settings.NameValueTable.VALUE, value)
        return bundle
    }

    private fun setDoneBundle(): Bundle {
        val bundle = Bundle()
        bundle.putBoolean("config_set_return", true)
        return bundle
    }

    companion object {
        private const val KIND_GET = 0
        private const val KIND_SET = 1
        private const val KIND_OTHER = -1

        /** was f: settings that always read as these */
        @JvmField
        val DEFAULT_VALUES = HashMap<String, String>()

        init {
            DEFAULT_VALUES["user_setup_complete"] = "1"
            DEFAULT_VALUES["install_non_market_apps"] = "0"
        }

        /** was p: the provider refused for lack of permission (SecurityException, "Permission denial", WRITE_SECURE_SETTINGS) */
        @JvmStatic
        fun isPermissionFailure(e: InvocationTargetException): Boolean {
            val cause = e.cause
            if (cause is SecurityException) return true
            val message = cause?.message
            if (cause !is RuntimeException || message == null) return false
            return message.contains("Permission denial") || message.contains("WRITE_SECURE_SETTINGS")
        }
    }
}
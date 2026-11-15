package com.ims134.doclone.io


import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import com.ims134.doclone.DoApplication.Companion.appContext
import java.io.File
import androidx.core.content.edit

object PreferencesUtils {

    private const val PREF_NAME = "DoMultiple-pref"
    private const val KEY_AD_FREE = "ad_free"
    private const val KEY_LITE_MODE_FILE = "lite_mode"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // ---- getters ----

    /** Was `a`. */
    @JvmStatic
    fun getBoolean(context: Context?, key: String, defValue: Boolean): Boolean =
        if (context == null) defValue else prefs(context).getBoolean(key, defValue)

    /** Was `b`. */
    @JvmStatic
    fun getInt(app: Application, key: String, defValue: Int): Int =
        prefs(app).getInt(key, defValue)

    /** Was `c`. */
    @JvmStatic
    fun getLong(context: Context?, key: String, defValue: Long): Long =
        if (context == null) defValue else prefs(context).getLong(key, defValue)

    /** Was `d`. */
    @JvmStatic
    fun getString(context: Context?, key: String): String? =
        context?.let { prefs(it).getString(key, null) }

    // ---- setters ----

    /** Was `g`. */
    @JvmStatic
    fun putBoolean(context: Context, key: String, value: Boolean) =
        prefs(context).edit(commit = true) { putBoolean(key, value)}

    /** Was `h`. */
    @JvmStatic
    fun putInt(context: Context, key: String, value: Int) {
        prefs(context).edit(commit = true) { putInt(key, value) }
    }

    /** Was `i`. */
    @JvmStatic
    fun putLong(context: Context, key: String, value: Long) {
        prefs(context).edit(commit = true) { putLong(key, value) }
    }

    /** Was `j`. */
    @JvmStatic
    fun putString(context: Context, key: String, value: String?) {
        prefs(context).edit(commit = true) { putString(key, value) }
    }

    // ---- domain helpers ----

    /** Was `e`. True when the app is running under x40 and the ad-free flag is set. */
    @JvmStatic
    fun isAdFree(): Boolean {
        return getBoolean(appContext, KEY_AD_FREE, true)
    }

    /** Was `k`. */
    @JvmStatic
    fun setAdFree(enabled: Boolean) {
        putBoolean(appContext, KEY_AD_FREE, enabled)
    }

    /** Was `f`. True when the "lite_mode" marker file exists in the app's files dir. */
    @JvmStatic
    fun isLiteMode(): Boolean =
        File(appContext.filesDir, KEY_LITE_MODE_FILE).exists()
}
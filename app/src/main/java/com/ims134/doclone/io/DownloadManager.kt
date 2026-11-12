package com.ims134.doclone.io

import com.ims134.doclone.clone.VirtualCore

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import java.lang.reflect.InvocationTargetException

/** was io.lq1: DownloadManager hook. Makes the download the host's (notification package, public API, cookie as header). */
class DownloadProviderHook(target: Any?, authority: String?) : ExternalProviderHook(target, authority) {

    @Throws(InvocationTargetException::class)
    override fun insert(context: ReflectiveCallContext, uri: Uri?, values: ContentValues?): Uri? {
        val v = values!!
        if (v.containsKey("notificationpackage")) {
            v.put("notificationpackage", VirtualCore.gCore.hostPackageName)
        }
        if (v.containsKey("cookiedata")) {
            val cookie = v.getAsString("cookiedata")
            v.remove("cookiedata")
            var i = 0
            while (v.containsKey("http_header_$i")) {
                i++
            }
            v.put("http_header_$i", "Cookie: $cookie")
        }
        if (!v.containsKey("is_public_api")) {
            v.put("is_public_api", true)
        }
        for (column in REMOVED_COLUMNS) {
            if (v.containsKey(column)) {
                v.remove(column)
            }
        }
        return try {
            context.execute() as Uri?
        } catch (e: Exception) {
            Log.d("DownloadProviderHook", Log.getStackTraceString(e))
            Uri.Builder().appendPath("0").build()
        }
    }

    companion object {
        /** was f (map): columns the host can't honour */
        private val REMOVED_COLUMNS = arrayOf("otheruid", "notificationclass")
    }
}
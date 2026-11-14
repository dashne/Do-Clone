package com.ims134.doclone.clone

import android.os.Build



object BuildCompat {

    @JvmStatic
    fun isEmui(): Boolean {
        if (Build.DISPLAY.uppercase().startsWith("EMUI")) return true
        val value = try {
            Class.forName("android.os.SystemProperties")
                .getMethod("get", String::class.java, String::class.java)
                .invoke(null, "ro.build.version.emui", "") as? String
        } catch (_: Exception) {
            null
        }
        return value?.contains("EmotionUI") == true
    }

    fun sdk28OrLater(): Boolean {
        return Build.VERSION.SDK_INT >= 28
    }

    fun sdk31OrLater(): Boolean {
        return Build.VERSION.SDK_INT >= 30
    }

    fun sdk34OrLater(): Boolean {
        return Build.VERSION.SDK_INT >= 34
    }
    fun sdk29OrLater(): Boolean {
        return Build.VERSION.SDK_INT >= 29
    }
    fun sdk33OrLater(): Boolean {
        return Build.VERSION.SDK_INT >= 33
    }
    fun sdk30OrLater(): Boolean {
        return Build.VERSION.SDK_INT >= 30
    }


    /**
     * True on Android O (API 26) or later, including the O Developer Preview
     * running on N (SDK_INT == 25, PREVIEW_SDK_INT > 0).
     *
     * Used by [VActivityManagerService.startProcessLocked] to pick between the
     * Oreo and pre-Oreo `IApplicationThread.Stub.asInterface` mirrors.
     */
    @JvmStatic
    fun isAtLeastOreo(): Boolean {
        if (Build.VERSION.SDK_INT == 25) {
            return try {
                Build.VERSION.PREVIEW_SDK_INT > 0
            } catch (_: Throwable) {
                false
            }
        }
        return Build.VERSION.SDK_INT >= 26
    }

}
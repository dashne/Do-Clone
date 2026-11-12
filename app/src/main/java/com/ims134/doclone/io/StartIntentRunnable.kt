package com.ims134.doclone.io

import android.util.Log
import com.ims134.doclone.clone.VActivityManager


import android.content.Intent
import android.net.http.UrlRequest
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.core.content.res.ResourcesCompat


/**
 * was io.xl3, an R8 merged class: one Runnable whose [mode] picks the job, [target] is its receiver, [value] its int argument.
 *  - 0 [MODE_STATUS]: Cronet status listener gets [value]
 *  - 1 [MODE_FONT_FAILED]: androidx font callback fails with reason [value]
 *  - 2 [MODE_START_ACTIVITY]: starts the pending virtual activity ([target] is the Intent, [value] the user id)
 *  - else (3): ads sdk callback gets [value] + 1
 */
class StartIntentRunnable(private val target: Any?, private val value: Int, private val mode: Int) : Runnable {

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun run() {
        when (mode) {
            //MODE_STATUS -> (target as UrlRequest.StatusListener).onStatus(value)
            //MODE_FONT_FAILED -> (target as ResourcesCompat.FontCallback).onFontRetrievalFailed(value)
            MODE_START_ACTIVITY -> {
                try {
                    VActivityManager.instance.startActivity(value, target as Intent)
                } catch (t: Throwable) {
                    Log.d("StubPendingActivity", "Unable to dispatch pending virtual activity", t)
                }
            }
            //else -> (target as zm3).c(value + 1)
        }
    }

    companion object {
        const val MODE_STATUS = 0
        const val MODE_FONT_FAILED = 1
        const val MODE_START_ACTIVITY = 2
        const val MODE_ADS_CALLBACK = 3
    }
}
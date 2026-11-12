package com.ims134.doclone.clone

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import java.net.URISyntaxException

open class ShortcutHandleActivity : Activity() {

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()                                   // invisible trampoline: finish at once, keep working

        val intent = intent ?: return
        val userId = intent.getIntExtra("_VA_|_user_id_", 0)
        val splashUri = intent.getStringExtra("_VA_|_splash_")
        val targetUri = intent.getStringExtra("_VA_|_uri_")

        val splash = splashUri?.let { parseOrNull(it) }
        val target = targetUri?.let { parseOrNull(it) } ?: return

        target.selector = null                     // drop any selector so it can't redirect the launch

        if (splash == null) {
            // No splash screen: launch the clone's activity directly
            try {
                VActivityManager.instance.startActivity(userId, target)
            } catch (_: Throwable) {
            }
        } else {
            // Splash first: it carries the real intent and user id as extras
            splash.putExtra("android.intent.extra.INTENT", target)
            splash.putExtra("android.intent.extra.CC", userId)
            startActivity(splash)
        }
    }

    private fun parseOrNull(uri: String): Intent? =
        try {
            Intent.parseUri(uri, 0)
        } catch (e: URISyntaxException) {
            null
        }
}
package com.ims134.doclone.clone

import com.ims134.doclone.io.ComponentUtils


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


/** Receives the wrapped broadcast of get PendingIntent and re-sends it redirected to the clone. */
class StubPendingReceiver : BroadcastReceiver() {

    @Suppress("DEPRECATION")
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val target = intent.getParcelableExtra<Intent>(EXTRA_INTENT)
            val userId = intent.getIntExtra(EXTRA_USER_ID, -1)
            if (target != null) {
                context.sendBroadcast(ComponentUtils.redirectBroadcastIntent(userId, target))
            }
        } catch (_: Exception) {
        }
    }

    private companion object {
        const val EXTRA_INTENT = "_VA_|_intent_"
        const val EXTRA_USER_ID = "_VA_|_user_id_"
    }
}
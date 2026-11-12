package com.ims134.doclone.clone

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.RemoteException
import android.util.Log


/**
 * Receives the real service Intent of get PendingIntent (wrapped in [EXTRA_INTENT] / [EXTRA_USER_ID]) and starts it in the
 * clone on get background thread.
 */
class StubPendingService : Service() {

    /** was get: starts [intent] for [userId] through the clone's activity manager */
    private class StartServiceTask(private val userId: Int, private val intent: Intent) : Runnable {
        override fun run() {
            try {
                VActivityManager.instance.getService().startService(null, intent, null, userId)
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }
    }

    /** was b: holder of the background handler */
    private object AsyncHandler {
        val handler: Handler

        init {
            val thread = HandlerThread("Async-pending-service")
            thread.start()
            handler = Handler(thread.looper)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @Suppress("DEPRECATION")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            try {
                val target = intent.getParcelableExtra<Intent>(EXTRA_INTENT)
                val userId = intent.getIntExtra(EXTRA_USER_ID, 0)
                if (target != null) {
                    val sdk = Build.VERSION.SDK_INT
                    // Android 15+: an old-format saved GMS intent can't be replayed, drop it
                    if (sdk >= 35 && intent.getIntExtra(EXTRA_GMS_PENDING_VERSION, 0) < 2 &&
                        SAVED_INTENT_ACTION == target.action && target.component?.packageName == GMS_PACKAGE
                    ) {
                        stopSelf(startId)
                        return START_NOT_STICKY
                    }
                    if (sdk >= 35 && SAVED_INTENT_ACTION == target.action && intent.hasExtra("uniqueid") &&
                        target.component?.packageName == GMS_PACKAGE
                    ) {
                        target.putExtra("uniqueid", intent.getLongExtra("uniqueid", -1L))
                    }
                    AsyncHandler.handler.post(StartServiceTask(userId, target))
                }
            } catch (t: Throwable) {
                Log.d("StubPendingService", "Unable to unwrap pending service Intent", t)
            }
        }
        stopSelf()
        return START_NOT_STICKY
    }

    private companion object {
        const val EXTRA_INTENT = "_VA_|_intent_"
        const val EXTRA_USER_ID = "_VA_|_user_id_"
        const val EXTRA_GMS_PENDING_VERSION = "_VA_|_gms_pending_version_"
        const val SAVED_INTENT_ACTION = "com.google.android.chimera.container.IntentOperationService.SAVED_INTENT"
        const val GMS_PACKAGE = "com.google.android.gms"
    }
}
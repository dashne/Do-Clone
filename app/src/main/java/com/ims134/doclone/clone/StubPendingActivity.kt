package com.ims134.doclone.clone


import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import com.ims134.doclone.io.StartIntentRunnable
import com.ims134.doclone.io.StubActivityRecord


/** Receives the wrapped activity Intent of get PendingIntent, finishes at once and starts the real Intent in the clone. */
class StubPendingActivity : Activity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
        val record = StubActivityRecord(intent)
        val target = record.intent ?: return
        target.addFlags(FLAG_FORWARD_RESULT)
        AsyncTask.THREAD_POOL_EXECUTOR.execute(StartIntentRunnable(target, record.userId, 2))
    }

    private companion object {
        /** 33554432 */
        const val FLAG_FORWARD_RESULT = Intent.FLAG_ACTIVITY_FORWARD_RESULT
    }
}
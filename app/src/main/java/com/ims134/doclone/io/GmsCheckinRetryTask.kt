package com.ims134.doclone.io

import android.content.Context
import android.database.Cursor
import android.os.RemoteException
import com.ims134.doclone.clone.VActivityManager
import com.ims134.doclone.clone.VUserHandle


/**
 * Was `io.iu8`. Retry task that periodically checks whether the device has a
 * valid `android_id`; if not, it asks the server-side `VActivityManager` to
 * trigger a GMS check-in (which may generate one), then re-schedules itself
 * according to [VClientImpl.q] until either the ID becomes valid or retries
 * are exhausted.
 *
 * Spawned by [VClientImpl.bindApplication] when the GMS persistent process
 * comes up.
 */
internal class GmsCheckinRetryTask(
    @JvmField val client: VClientImpl,
    @JvmField val context: Context,
    @JvmField val attempt: Int,
) : Runnable {

    override fun run() {
        // Spawn a worker thread for the blocking query/checkin, tagged with
        // the attempt number so adb/logcat filters can pick it out.
        Thread(object : Runnable {
            override fun run() {
                // ===== Query android_id, try to determine if it's valid =====
                var androidIdValid = false
                var cursor: Cursor? = null
                try {
                    cursor = context.contentResolver.query(
                        VClientImpl.p,
                        null,                       // projection: null = all columns
                        null,                       // selection: null
                        arrayOf("android_id"),      // selectionArgs: ["android_id"]
                        null,                       // sortOrder: null
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        var colIdx = cursor.getColumnIndex("value")//AppMeasurementSdk.a.c)  // "value"
                        if (colIdx < 0) colIdx = 1  // fallback — best-effort if "value" column missing
                        val value = cursor.getString(colIdx)
                        if (!value.isNullOrEmpty() && value != "0") {
                            androidIdValid = true
                        }
                    }
                } catch (th: Throwable) {
                    // Swallowed — matches the original's catch/rethrow dance where
                    // the exception is converted to a toString() call and dropped.
                    th.toString()
                } finally {
                    // Guaranteed close if we got a cursor — matches the smali's
                    // try/catch around cursor.close() that always runs before
                    // the checkin path.
                    cursor?.let {
                        try { it.close() } catch (_: Throwable) {}
                    }
                }

                // Already valid — no need to trigger a check-in.
                if (androidIdValid) return

                // ===== Ask the server to trigger a GMS check-in for this user =====
                try {
                    val vam = VActivityManager.instance
                    val vuid = VUserHandle.getVUid()
                    try {
                        vam.getService().requestGmsCheckin(vuid)
                    } catch (e: RemoteException) {
                        // Binder died mid-call — clear the cached service so the
                        // next getService() re-binds.
                        vam.service_ = null
                        e.toString()
                    } catch (e: RuntimeException) {
                        vam.service_ = null
                        e.toString()
                    }
                } catch (th: Throwable) {
                    th.toString()
                }

                // ===== Schedule the next retry if any budget remains =====
                val next = attempt + 1
                if (next >= 0 && next < VClientImpl.q.size) {
                    client.mainHandler.postDelayed(
                        GmsCheckinRetryTask(client, context, next),
                        VClientImpl.q[next],
                    )
                }
            }
        }, "VA-GmsCheckin-${attempt + 1}").start()
    }
}
package com.ims134.doclone.clone


import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.ims134.doclone.io.NotificationUtils
import com.ims134.doclone.io.ProviderNotificationUtils


/**
 * Keep-alive service of the host app (jadx: DaemonService).
 *  - SDK < 34: onCreate starts [InnerService] (foreground notification) and schedules a wake-up alarm.
 *  - SDK >= 34: no foreground service is started; callers bind to it via [ensureBound] instead.
 */
class DaemonService : Service() {

    private val binder = Binder() // a

    /** Foreground-notification helper service (jadx DaemonService$InnerService). */
    class InnerService : Service() {

        override fun onBind(intent: Intent?): IBinder? = null

        @SuppressLint("DiscouragedApi")
        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            if (BuildCompat.sdk34OrLater()) {
                stopSelf()
                return START_NOT_STICKY
            }
            Log.e(TAG, "Start foreground")
            try {
                val hostPackage = VirtualCore.gCore.hostPackageName
                val launchIntent = packageManager.getLaunchIntentForPackage(hostPackage!!)
                val notification: Notification
                if (hostPackage.endsWith(".arm64") || hostPackage.endsWith(".arm32") || launchIntent == null) {
                    notification = Notification()
                } else {
                    launchIntent.addCategory(Intent.CATEGORY_LAUNCHER)
                    launchIntent.action = Intent.ACTION_MAIN
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                        val channel = ProviderNotificationUtils.createNotificationChannel(CHANNEL_ID, CHANNEL_NAME)
                        channel.enableVibration(false)
                        channel.enableLights(false)
                        channel.setSound(null, null)
                        channel.description = CHANNEL_DESCRIPTION
                        channel.setShowBadge(false)
                        manager.createNotificationChannel(channel)
                    }
                    val builder = NotificationUtils.createNotificationBuilder(this, CHANNEL_ID)
                    var iconId = resources.getIdentifier("ic_launcher_trans", "mipmap", packageName)
                    if (iconId == 0) iconId = resources.getIdentifier("ic_launcher", "mipmap", packageName)
                    val titleId = resources.getIdentifier("daemon_title", "string", packageName)
                    val messageId = resources.getIdentifier("daemon_message", "string", packageName)
                    builder
                        .setContentTitle(if (titleId == 0) DEFAULT_TITLE else getString(titleId))
                        .setContentText(if (messageId == 0) DEFAULT_TEXT else getString(messageId))
                        .setOngoing(true)
                        .setSmallIcon(iconId)
                        .setContentIntent(PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE))
                    notification = builder.build()
                    notification.flags = notification.flags or Notification.FLAG_NO_CLEAR or Notification.FLAG_FOREGROUND_SERVICE
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            } catch (_: Exception) {
            }
            return super.onStartCommand(intent, flags, startId)
        }

        private companion object {
            const val NOTIFICATION_ID = 1001
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        if (BuildCompat.sdk34OrLater()) {
            stopSelf()
            return
        }
        try {
            startForegroundService(Intent(this, InnerService::class.java))
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val wake = Intent().apply {
                action = "com.polestar.messaging.wake"
                setPackage(VirtualCore.gCore.hostPackageName)
            }
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 2000,
                wakeIntervalMs,
                PendingIntent.getBroadcast(this, 1111, wake, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            )
        } catch (e: Exception) {
            Log.d("Alarm", Log.getStackTraceString(e))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ensureStarted(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    companion object {
        private const val TAG = "DaemonService"

        private const val CHANNEL_NAME = "Clone App Messaging"                                   // f
        private const val CHANNEL_ID = "_id_service_"                                            // g
        private const val CHANNEL_DESCRIPTION = "Clone App Messaging & Notification"             // h
        private const val DEFAULT_TITLE = "Receiving messages for clones"                        // i
        private const val DEFAULT_TEXT = "Keep running in background to receive messages"        // j

        /** Alarm repeat interval, 1 h. */
        @JvmField
        var wakeIntervalMs = 3_600_000L // e

        private val lock = Any()                                                                 // b
        private var bound = false                                                                // c
        private val connection = object : ServiceConnection {                                    // d
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {}
            override fun onServiceDisconnected(name: ComponentName?) {}
        }

        /** SDK 34+: keep the process alive by binding to the service (jadx a). */
        @JvmStatic
        fun ensureBound(context: Context?) {
            if (!BuildCompat.sdk34OrLater() || context == null) return
            synchronized(lock) {
                if (bound) return
                val appContext = context.applicationContext ?: context
                bound = try {
                    appContext.bindService(
                        Intent(appContext, DaemonService::class.java), connection,
                        Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT
                    )
                } catch (_: Throwable) {
                    false
                }
            }
        }

        /** SDK < 34: start the service (jadx b). */
        @JvmStatic
        fun ensureStarted(context: Context) {
            if (BuildCompat.sdk34OrLater()) return
            try {
                context.startService(Intent(context, DaemonService::class.java))
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
    }
}
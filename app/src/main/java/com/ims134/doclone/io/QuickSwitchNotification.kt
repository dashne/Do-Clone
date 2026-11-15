package com.ims134.doclone.io

import android.util.Log
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.DaemonService

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.repo.CloneManager
import java.util.ArrayList
import kotlin.jvm.java


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class QuickSwitchNotification(rawContext: Context) {

    @JvmField val notificationManager: NotificationManager
    @JvmField val context: Context
    @JvmField val workerHandler: Handler
    @JvmField var remoteViews: RemoteViews? = null
    @JvmField val recentApps = ArrayList<String>()
    @JvmField var active = false
    @JvmField val saveRecentAppsRunnable: Runnable = PersistRecentAppsTask()
    @JvmField val updateNotificationRunnable: Runnable = UpdateNotificationTask()

    init {
        context = rawContext.applicationContext
        notificationManager = rawContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val thread = HandlerThread("switch_worker")
        thread.start()
        workerHandler = Handler(thread.looper)

        val filter = IntentFilter().apply {
            addAction(ACTION_QUICK_SWITCH)
            addAction(ACTION_CANCEL_QUICK_SWITCH)
            addAction(ACTION_ENABLE_QUICK_SWITCH)
            addCategory(CATEGORY_ENABLE)
            addCategory(CATEGORY_NOTIFY_0)
            addCategory(CATEGORY_NOTIFY_1)
            addCategory(CATEGORY_NOTIFY_2)
            addCategory(CATEGORY_NOTIFY_3)
        }

        ContextCompat.registerReceiver(
            context,
            QuickSwitchReceiver(),
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
//        if (Build.VERSION.SDK_INT >= 26) {
//            // Original passes literal 4 (RECEIVER_NOT_EXPORTED). Only available on API 33+;
//            // preserved verbatim to match the decompiled behavior.
//            @Suppress("UnspecifiedRegisterReceiverFlag")
//            context.registerReceiver(QuickSwitchReceiver(), filter, Context.RECEIVER_NOT_EXPORTED)
//        } else {
//            context.registerReceiver(QuickSwitchReceiver(), filter)
//        }
    }

    /** Was inner class `a`. Rebuilds [recentApps] from saved LRU, installed clones, and remote config. */
    inner class LoadRecentAppsTask : Runnable {
        override fun run() {
            val lru = PreferencesUtils.getString(context, "lru_pkg")
            recentApps.clear()
            synchronized(recentApps) {
                for (entry in (lru?.split(";") ?: emptyList())) {
                    if (entry.isEmpty() || entry in recentApps) continue
                    val pkg = entry.substringBefore(":")
                    val userId = CloneManager.getIntAfter(entry)
                    val installed = try {
                        VirtualCore.gCore.isAppInstalledAsUser(userId, pkg)
                    } catch (_: Throwable) {
                        false
                    }
                    if (installed) recentApps.add(entry)
                }

                //TODO::
                /*
                if (recentApps.size < 5) {
                    for (model in DBManager.b(appContext)) {
                        val key = CloneManager.g(model.appUserId, model.packageName)
                        if (key !in recentApps) recentApps.add(key)
                    }
                }
                if (recentApps.size < 4) {
                    val hotConf = RemoteConfigManager.getString("hot_clone_conf") ?: ""
                    if (hotConf.isNotEmpty()) {
                        for (hotPkg in hotConf.split(":")) {
                            try {
                                if (VirtualCore.gCore.unhookPackageManager.getApplicationInfo(hotPkg, 0) != null) {
                                    val key = CloneManager.g(999, hotPkg)
                                    if (key !in recentApps) recentApps.add(key)
                                }
                            } catch (_: Exception) {
                            }
                        }
                    }
                }
                */

            }
            active = true
            pushRecent(null)
        }
    }

    /** Was inner class `b`. Serializes [recentApps] to the `lru_pkg` preference. */
    inner class PersistRecentAppsTask : Runnable {
        override fun run() {
            val sb = StringBuilder()
            synchronized(recentApps) {
                for (entry in recentApps) sb.append(entry).append(';')
            }
            PreferencesUtils.putString(context, "lru_pkg", sb.toString())
        }
    }

    /** Was inner class `c`. Rebuilds and posts the quick-switch notification. */
    inner class UpdateNotificationTask : Runnable {
        override fun run() {
            try {
                updateNotification(this@QuickSwitchNotification)
            } catch (_: Throwable) {
            }
        }
    }

    /** Was inner class `d`. Handles the notification's cancel / enable / switch-tap actions. */
    inner class QuickSwitchReceiver : BroadcastReceiver() {
        override fun onReceive(rcvContext: Context, intent: Intent) {
            when (intent.action) {
                ACTION_CANCEL_QUICK_SWITCH -> {
                    notificationManager.cancel(NOTIFICATION_ID)
                    if (!BuildCompat.sdk34OrLater()) {
                        try {
                            rcvContext.startService(Intent(rcvContext, DaemonService.InnerService::class.java))
                        } catch (e: Exception) {
                            Log.e("DaemonService", e.toString())
                        }
                    }
                    active = false
                }
                ACTION_ENABLE_QUICK_SWITCH -> {
                    scheduleLoad()
                }
                else -> {
                    val pkg = intent.getStringExtra("extra_start_package") ?: return
                    val userId = intent.getIntExtra("extra_start_userid", 0)
                    val installed = try {
                        VirtualCore.gCore.isAppInstalledAsUser(userId, pkg)
                    } catch (_: Throwable) {
                        false
                    }
                    if (installed)
                    {
                        //TODO::
                        /*
                        val launch = Intent(context, AppLoadingActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            putExtra("app_packagename", pkg)
                            putExtra("From where", "From notify")
                            putExtra("app_userid", userId)
                        }
                        context.startActivity(launch)
                        */

                    } else
                    {
                        val realLaunch = VirtualCore.gCore.unhookPackageManager.getLaunchIntentForPackage(pkg)
                        if (realLaunch != null)
                        {
                            context.startActivity(realLaunch)
                        } else
                        {
                            synchronized(recentApps)
                            {
                                recentApps.remove(CloneManager.appendInt(999, pkg))
                                pushRecent(null)
                            }
                        }
                    }
                }
            }
        }
    }

    /** Was `e()`. Schedules the LRU rebuild after Stub 4 s delay. */
    fun scheduleLoad() {
        workerHandler.postDelayed(LoadRecentAppsTask(), 4000L)
    }

    /**
     * Was `g(String)`. Pushes [entry] (`pkg` or `pkg:userId`) to the head of [recentApps]
     * and debounces the persist + notification refresh. Passing `null` skips the LRU update
     * but still triggers persist + refresh.
     */
    fun pushRecent(entry: String?) {
        if (!active) return
        if (!entry.isNullOrEmpty()) {
            val pkg = entry.substringBefore(":")
            if (VirtualCore.isRestrictedPackage(pkg) || appContext.packageName == pkg) {
                return
            }
            synchronized(recentApps) {
                if (entry !in recentApps) {
                    if (recentApps.size < 5) {
                        recentApps.add(0, entry)
                    } else {
                        recentApps.removeAt(recentApps.size - 1)
                        recentApps.add(0, entry)
                    }
                    if (recentApps.size == 5 && CloneManager.getIntAfter(recentApps[4]) == 999) {
                        recentApps.removeAt(4)
                    }
                }
            }
        }
        workerHandler.removeCallbacks(saveRecentAppsRunnable)
        workerHandler.postDelayed(saveRecentAppsRunnable, 2000L)
        workerHandler.removeCallbacks(updateNotificationRunnable)
        workerHandler.postDelayed(updateNotificationRunnable, 1000L)
    }

    companion object {
        private const val NOTIFICATION_ID = 1001

        private const val ACTION_QUICK_SWITCH = "do.multiple.cloner.quick_switch"
        private const val ACTION_CANCEL_QUICK_SWITCH = "do.multiple.cloner.cancel_quick_switch"
        private const val ACTION_ENABLE_QUICK_SWITCH = "do.multiple.cloner.enable_quick_switch"

        private const val CATEGORY_ENABLE = "cat_enable"
        private const val CATEGORY_NOTIFY_0 = "cat_notify0"
        private const val CATEGORY_NOTIFY_1 = "cat_notify1"
        private const val CATEGORY_NOTIFY_2 = "cat_notify2"
        private const val CATEGORY_NOTIFY_3 = "cat_notify3"

        /** Was static field `i`. */
        @JvmField
        @Volatile
        var instance: QuickSwitchNotification? = null

        /** Was `d(Context)`. */
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): QuickSwitchNotification {
            instance?.let { return it }
            return QuickSwitchNotification(context).also { instance = it }
        }

        /** Was `b()`. Disables quick switch and clears the notification. */
        @JvmStatic
        fun disable() {
            getInstance(appContext)
            PreferencesUtils.putInt(appContext, "quick_switch_state", 0)
            val intent = Intent(ACTION_CANCEL_QUICK_SWITCH).apply {
                setPackage(appContext.packageName)
                addCategory(CATEGORY_ENABLE)
            }
            appContext.sendBroadcast(intent)
            //TODO::
            //EventReporter.d(false)
        }

        /** Was `c()`. Enables quick switch and triggers the LRU rebuild. */
        @JvmStatic
        fun enable() {
            getInstance(appContext)
            PreferencesUtils.putInt(appContext, "quick_switch_state", 1)
            val intent = Intent(ACTION_ENABLE_QUICK_SWITCH).apply {
                setPackage(appContext.packageName)
                addCategory(CATEGORY_ENABLE)
            }
            appContext.sendBroadcast(intent)
            //TODO::
            //EventReporter.d(true)
        }

        /** Was `f()`. */
        @JvmStatic
        fun isEnabled(): Boolean {
            return false
            //TODO::
            /*
            val state = PreferencesUtils.getInt(appContext, "quick_switch_state", -1)
            return if (state == -1) {
                RemoteConfigManager.getBoolean("default_enable_quick_switch")
            } else {
                state == 1
            }

             */
        }

        /** Was `a(QuickSwitchNotification)`. Builds and posts the quick-switch notification. */
        @JvmStatic
        fun updateNotification(qsn: QuickSwitchNotification) {
            TODO()
            /*
            if (qsn.remoteViews == null) {
                qsn.remoteViews = RemoteViews(qsn.context.packageName, R.layout.quick_switch_notification)
            }
            val channelId = DaemonService.g

            if (Build.VERSION.SDK_INT >= 26) {
                val channel = qsn.notificationManager.getNotificationChannel(channelId)
                if (channel != null && channel.importance == NotificationManager.IMPORTANCE_HIGH) {
                    qsn.notificationManager.deleteNotificationChannel(channelId)
                }
                if (qsn.notificationManager.getNotificationChannel(channelId) == null) {
                    val newChannel = b76.d(channelId)
                    newChannel.enableVibration(false)
                    newChannel.enableLights(false)
                    newChannel.setSound(null, null)
                    newChannel.description = "Quick Switch Shortcuts"
                    newChannel.setShowBadge(false)
                    qsn.notificationManager.createNotificationChannel(newChannel)
                }
            }

            val homeIntent = Intent(qsn.context, HomeActivity::class.java)
                .putExtra("From where", "From notify")

            val builder = NotificationCompat.Builder(qsn.context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher_trans)
                .setCustomContentView(qsn.remoteViews)
                // Original: `builder.m = 1` — likely priority, overridden by `notification.priority = 0` below.
                .setPriority(1)
                .setContentIntent(
                    PendingIntent.getActivity(
                        qsn.context, 0, homeIntent, PendingIntent.FLAG_IMMUTABLE
                    )
                )

            val notification = builder.build()
            // Original: `notificationA.flags = 96` — replaces flags including any set by setOngoing.
            notification.flags = Notification.FLAG_NO_CLEAR or Notification.FLAG_FOREGROUND_SERVICE
            notification.priority = Notification.PRIORITY_DEFAULT

            synchronized(qsn.recentApps) {
                for (idx in 0 until 5) {
                    val textId = when (idx) {
                        0 -> R.id.shortcut_txt0
                        1 -> R.id.shortcut_txt1
                        2 -> R.id.shortcut_txt2
                        3 -> R.id.shortcut_txt3
                        4 -> R.id.shortcut_txt4
                        else -> 0
                    }
                    val iconId = when (idx) {
                        0 -> R.id.shortcut_icon0
                        1 -> R.id.shortcut_icon1
                        2 -> R.id.shortcut_icon2
                        3 -> R.id.shortcut_icon3
                        4 -> R.id.shortcut_icon4
                        else -> 0
                    }
                    if (qsn.recentApps.size <= idx) {
                        qsn.remoteViews!!.setImageViewResource(iconId, R.drawable.icon_add)
                        qsn.remoteViews!!.setTextViewText(textId, "")
                    } else if (textId != 0 && iconId != 0) {
                        val entry = qsn.recentApps[idx]
                        val pkg = entry.substringBefore(":")
                        val userId = CloneManager.getIntAfter(entry)
                        val installed = try {
                            VirtualCore.gCore.isAppInstalledAsUser(userId, pkg)
                        } catch (_: Throwable) {
                            false
                        }
                        if (installed) {
                            val data = CustomizeAppData.b(userId, pkg)
                            qsn.remoteViews!!.setImageViewBitmap(iconId, data.Stub())
                            if (data.g) {
                                qsn.remoteViews!!.setTextViewText(textId, data.e)
                            } else {
                                val tag = sj6.b(R.string.clone_label_tag)
                                val model = CloneManager.getInstance(PolestarApp.appContext)
                                    .getClonedApp(userId, pkg)
                                qsn.remoteViews!!.setTextViewText(
                                    textId,
                                    String.format(tag, model?.applicationLabel),
                                )
                            }
                            val launch = Intent(qsn.context, AppLoadingActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                putExtra("app_packagename", pkg)
                                putExtra("From where", "From notify")
                                putExtra("app_userid", userId)
                            }
                            qsn.remoteViews!!.setOnClickPendingIntent(
                                iconId,
                                PendingIntent.getActivity(
                                    qsn.context, idx, launch,
                                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                                ),
                            )
                        } else {
                            try {
                                val info = VirtualCore.gCore.unhookPackageManager
                                    .getApplicationInfo(pkg, 0)
                                if (info != null) {
                                    val pm = qsn.context.packageManager
                                    qsn.remoteViews!!.setImageViewBitmap(
                                        iconId,
                                        BitmapUtils.drawableToBitmap(info.loadIcon(pm)),
                                    )
                                    qsn.remoteViews!!.setTextViewText(
                                        textId,
                                        info.loadLabel(pm),
                                    )
                                    qsn.remoteViews!!.setOnClickPendingIntent(
                                        iconId,
                                        PendingIntent.getBroadcast(
                                            qsn.context, idx,
                                            // Original passes this `!!` through to getBroadcast;
                                            // if null, the surrounding try/catch swallows the NPE.
                                            VirtualCore.gCore.unhookPackageManager
                                                .getLaunchIntentForPackage(pkg)!!,
                                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                                        ),
                                    )
                                }
                            } catch (_: Exception) {
                            }
                        }
                    }
                }
            }

            try {
                qsn.notificationManager.notify(NOTIFICATION_ID, notification)
            } catch (_: Throwable) {
            }
            */
        }
    }
}
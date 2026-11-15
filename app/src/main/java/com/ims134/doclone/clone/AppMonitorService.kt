package com.ims134.doclone.clone

import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.io.PreferencesUtils
import com.ims134.doclone.repo.CloneManager


import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import androidx.annotation.RequiresApi
import com.ims134.doclone.io.IAppMonitor
import com.ims134.doclone.io.QuickSwitchNotification


class AppMonitorService : Service() {

    private lateinit var monitorStub: MonitorStub
    private val backgroundTimeoutHandler = BackgroundTimeoutHandler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        //TODO::
        //CloneManager.getInstance(this)
        monitorStub = MonitorStub()
    }

    override fun onBind(intent: Intent?): IBinder = monitorStub

    /** Was inner class `a`. Fires after the relock timeout when the app stays backgrounded. */
    private inner class BackgroundTimeoutHandler(looper: Looper) : Handler(looper) {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun handleMessage(msg: Message) {
            if (msg.what != MSG_BACKGROUND_TIMEOUT) return

            QuickSwitchNotification.getInstance(appContext)
                .pushRecent(msg.obj as String?)

            currentLockedPackageKey = null
        }
    }

    /** Was inner class `b`. Handles callbacks from the agent (the client-side app monitor). */
    private inner class MonitorStub : IAppMonitor.Stub() {

        override fun onAdsLaunch(pkgName: String?, vuid: Int, extra: String?)
        {
//            val extras = Bundle().apply {
//                putString(AppMeasurementSdk.Stub.b, "" + extra)
//            }
//            EventReporter.f(extras, "ads_launch")
        }

        override fun onAppLock(pkgName: String?, vuid: Int) {
//            if (pkgName == null) return
//            startActivity(
//                Intent(this@AppMonitorService, AppLockActivity::class.java).apply {
//                    putExtra(Intent.EXTRA_PACKAGE_NAME, pkgName)
//                    putExtra("extra_clone_userid", vuid)
//                    flags = LOCK_SCREEN_FLAGS
//                }
//            )
        }

        override fun onAppSwitchBackground(pkgName: String?, vuid: Int) {
            val key = CloneManager.appendInt(vuid, pkgName)
            val relockDelay = PreferencesUtils.getLong(
                appContext, "relock_interval", 5000L
            )
            backgroundTimeoutHandler.sendMessageDelayed(
                backgroundTimeoutHandler.obtainMessage(MSG_BACKGROUND_TIMEOUT, key),
                relockDelay,
            )
        }

        override fun onAppSwitchForeground(pkgName: String?, vuid: Int) {
            if (!pkgName.isNullOrEmpty()) {
                PreferencesUtils.putBoolean(
                    this@AppMonitorService,
                    "clone_launch_success_pkg_${pkgName}_$vuid",
                    true,
                )
                sendBroadcast(
                    Intent(ACTION_CLONE_LAUNCH_SUCCESS).apply {
                        setPackage(packageName)
                        putExtra("extra_clone_launch_package", pkgName)
                        putExtra("extra_clone_launch_user_id", vuid)
                    }
                )
            }
            //TODO::
            /*
            val clonedApp = CloneManager.getInstance(this@AppMonitorService)
                .getClonedApp(vuid, pkgName)
            val key = CloneManager.appendInt(vuid, pkgName)

            if (clonedApp == null ||
                clonedApp.lockerState == 0 ||
                !PreferencesUtils.getBoolean(
                    this@AppMonitorService, "locker_feature_enabled", false
                )
            ) return

            if (key == currentLockedPackageKey) {
                backgroundTimeoutHandler.removeMessages(MSG_BACKGROUND_TIMEOUT)
                return
            }

            if (pkgName == null) return
            startActivity(
                Intent(this@AppMonitorService, AppLockActivity::class.java).apply {
                    putExtra(Intent.EXTRA_PACKAGE_NAME, pkgName)
                    putExtra("extra_clone_userid", vuid)
                    flags = LOCK_SCREEN_FLAGS
                }
            )

             */
        }
    }

    companion object {
        private const val MSG_BACKGROUND_TIMEOUT = 0
        private const val ACTION_CLONE_LAUNCH_SUCCESS =
            "do.multiple.cloner.action.CLONE_LAUNCH_SUCCESS"

        /**
         * 1887436800 = 0x70800000, decomposes to:
         * FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_SINGLE_TOP
         * | FLAG_ACTIVITY_NO_HISTORY | FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
         */
        private const val LOCK_SCREEN_FLAGS: Int =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    0x40000000 /* FLAG_ACTIVITY_NO_HISTORY — no public Intent constant */ or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS

        /** Was static field `c`. The `pkg:userId` key of the currently locked app, or null. */
        @JvmField
        @Volatile
        var currentLockedPackageKey: String? = null

        /** Was `handleAd(int, String)`. */
        @JvmStatic
        fun handleAd(vuid: Int, pkgName: String?) {
            if (PreferencesUtils.isAdFree() ||
                !PreferencesUtils.getBoolean(
                    appContext, "locker_feature_enabled", false
                ) ||
                pkgName.isNullOrEmpty()
            ) return
            //TODO::
//            val clonedApp = CloneManager.getInstance(appContext)
//                .getClonedApp(vuid, pkgName) ?: return
//            if (clonedApp.lockerState == 0) return

        }
    }
}
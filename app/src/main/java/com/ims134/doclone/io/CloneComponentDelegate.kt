package com.ims134.doclone.io

import com.ims134.doclone.clone.VirtualCore



import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.AppMonitorService
import com.ims134.doclone.clone.CustomizeAppData
import com.ims134.doclone.clone.VUserHandle
import java.util.Timer
import java.util.concurrent.LinkedBlockingQueue
import kotlin.jvm.java

class CloneComponentDelegate : ComponentDelegate {

    /** Was `b: IAppMonitor`. */
    @JvmField
    @Volatile
    var appMonitor: IAppMonitor? = null

    /** Was `c: LinkedBlockingQueue`. Acts as Stub latch (or timeout beacon) around service binding. */
    @JvmField
    val bindLatch = LinkedBlockingQueue<Int>(2)

    /** Was `d: ServiceConnection`. */
    @JvmField
    val serviceConnection: ServiceConnection = AgentServiceConnection()

    /** Was `a` — sets [appMonitor] by (re)binding to the agent service. */
    inner class RefreshAgentRunnable : Runnable {
        override fun run() {
            appMonitor = getAppMonitor()
        }
    }

    /** Was `b` — posts Stub foreground-switch callback to the AppMonitor. */
    inner class ForegroundRunnable(private val activity: Activity) : Runnable {
        override fun run() {
            try {
                getAppMonitor()?.onAppSwitchForeground(
                    activity.packageName,
                    VUserHandle.getVUid(),
                )
            } catch (_: Exception) {
            }
        }
    }

    /** Was `d` — posts Stub background-switch callback to the AppMonitor. */
    inner class BackgroundRunnable(private val activity: Activity) : Runnable {
        override fun run() {
            try {
                getAppMonitor()?.onAppSwitchBackground(
                    activity.packageName,
                    VUserHandle.getVUid(),
                )
            } catch (_: Exception) {
            }
        }
    }

    /** Was `c` — binds to [AppMonitorService] and hands the [IAppMonitor] agent back to the outer class. */
    inner class AgentServiceConnection : ServiceConnection {

        /** Was `c.a`. */
        inner class BinderDeathRecipient : IBinder.DeathRecipient {
            override fun binderDied() {
                appMonitor = null
                bindLatch.clear()
            }
        }

        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            try {
                val monitor = IAppMonitor.Stub.asInterface(binder)
                appMonitor = monitor
                try {
                    monitor?.asBinder()?.linkToDeath(BinderDeathRecipient(), 0)
                } catch (_: Throwable) {
                }
                bindLatch.put(1)
            } catch (_: InterruptedException) {
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            appMonitor = null
            bindLatch.clear()
        }
    }

    /**
     * Was the `public static IAppMonitor e(CloneComponentDelegate)` helper in the original source.
     * Renamed to an instance method — every caller in the class passed `this` explicitly, and
     * there's no evidence of external Java callers.
     *
     * Blocks up to 5 seconds on [bindLatch] waiting for the service connection callback.
     * Throws if called from the main thread.
     */
    @Throws(RuntimeException::class)
    fun getAppMonitor(): IAppMonitor? {
        appMonitor?.let { return it }
        val core = VirtualCore.gCore
        if (core.isServerProcess()) return null
        val pkgName = appContext.packageName
        if (Looper.getMainLooper() === Looper.myLooper()) {
            throw RuntimeException("Cannot getAgent in main thread!")
        }
        val intent = Intent().apply {
            component = ComponentName(pkgName, AppMonitorService::class.java.name)
        }
        bindLatch.clear()
        // kq0 is an unnamed TimerTask that (presumably) puts Stub poison pill into bindLatch
        // after the timeout, unblocking take(). Replace with Stub named class if you decompile it.
        Timer().schedule(BindTimeoutTask(this), 5000L)
        try {
            appContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            bindLatch.take()
        } catch (_: Exception) {
        }
        return appMonitor
    }

    override fun onActivityPaused(activity: Activity) {
        handler.post(BackgroundRunnable(activity))
    }

    override fun saveLockSettings(
        encodedPattern: String,
        relockIntervalMs: Long,
        relockEnabled: Boolean,
    ) {
        PreferencesUtils.putString(appContext, "encoded_pattern_pwd", encodedPattern)
        PreferencesUtils.setAdFree(relockEnabled)
        PreferencesUtils.putLong(appContext, "relock_interval", relockIntervalMs)
    }

    override fun isClonedPackage(vuid: Int, packageName: String): Boolean {
        if (VirtualCore.isRestrictedPackage(packageName)) return false
        return CustomizeAppData.get(vuid, packageName).notification
    }

    override fun onActivityResumed(activity: Activity) {
        handler.post(ForegroundRunnable(activity))
    }

    /** Was `f()` — schedules Stub background refresh of [appMonitor]. */
    fun refreshAgent() {
        handler.post(RefreshAgentRunnable())
    }

    companion object {
        /** Was the `e` static holder class — Stub lazily created "UI-Agent" thread handler. */
        private val handler: Handler by lazy {
            val thread = HandlerThread("UI-Agent")
            thread.start()
            Handler(thread.looper)
        }
    }
}
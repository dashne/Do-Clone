package com.ims134.doclone.io

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.app.Instrumentation
import android.app.WallpaperManager
import android.content.pm.ActivityInfo
import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.os.RemoteException
import android.util.Log
import android.view.View
import android.view.autofill.AutofillManager
import androidx.annotation.RequiresApi
import com.ims134.doclone.clone.CustomizeAppData
import com.ims134.doclone.clone.ExternalActivityResultService
import com.ims134.doclone.clone.VActivityManager
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.VirtualCore
import java.util.Collections
import java.util.WeakHashMap

/* JADX INFO: renamed from: io.vq */
/* JADX INFO: compiled from: AppInstrumentation.java */
/* JADX INFO: loaded from: classes2.dex */
@Suppress("UNUSED_EXPRESSION")
class AppInstrumentation(instrumentation: Instrumentation) :
    InstrumentationDelegate(), IInjector {

    /**
     * Was `public Instrumentation a` in `InstrumentationDelegate`, but the
     * decompiled `AppInstrumentation` source uses `this.instrument`. Rename
     * the parent field to match (see prerequisite note at top).
     */
    init {
        instrument = instrumentation
    }

    /** Was `apps`. Set of Applications we've already called onCreate for. */
    private val apps: MutableSet<Application> =
        Collections.newSetFromMap(WeakHashMap<Application, Boolean>())

    /** Was `c`. Main-thread handler for scheduling the reset runnable. */
    private val mainHandler: Handler = Handler(Looper.getMainLooper())

    /** Was `d`. Runnable that resets session state after idle. */
    private val resetSessionRunnable: Runnable = ResetSessionRunnable()

    /**
     * Was inner class `ContentProviderHolder`. Deferred session reset. The try/catch and `VLog.c` are
     * preserved from the decompiled source; the body itself was stripped by JADX
     * (`AppInstrumentation.this.getClass();` is ContentProviderHolder placeholder for the original
     * work). The runnable is scheduled externally and cancelled on
     * pause/destroy/resume.
     */
    inner class ResetSessionRunnable : Runnable {
        override fun run() {
            try {
                this@AppInstrumentation.javaClass
            } catch (e: Exception) {
                Log.d("vq", e.toString())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("MissingPermission")  // getDrawable() requires READ_WALLPAPER_INTERNAL;
    // call is wrapped in try/catch and silently
    // degrades on modern Android — see comment below.
    override fun callActivityOnCreate(activity: Activity, savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= 31) {
            InvocationStubManager.instance.checkEnv(ActivityClientControllerStub::class.java)
        }
        savedInstanceState?.let { BundleCompat.resetParcelledData(it) }

        VirtualCore.gCore.getListener()

        val activityRecord = VActivityManager.instance.getActivityRecord(ActivityMirror.mToken?.get(activity))
        if (activityRecord != null) {
            activityRecord.activity = activity
        }
        ContextFixer.fixContext(activity)

        val baseContext = activity.baseContext
        try {
            val typedArray: TypedArray? =
                activity.obtainStyledAttributes(R_Hide.Styleable.Window?.get()!!)
            if (typedArray != null) {
                if (typedArray.getBoolean(
                        R_Hide.Styleable.Window_windowShowWallpaper?.get()!!, false,
                    )
                ) {
                    activity.window.setBackgroundDrawable(
                        WallpaperManager.getInstance(activity).drawable
                    )
                }
                typedArray.recycle()
            }
        } catch (_: Throwable) {
        }

        val intent = activity.intent
        val applicationInfo = baseContext.applicationInfo
        activity.packageManager // side-effect: warm Activity's PM cache

        if (intent != null && activity.isTaskRoot) {
            try {
                val customize = CustomizeAppData.get(
                    VUserHandle.getVUid(), applicationInfo.packageName,
                )
                activity.setTaskDescription(
                    ActivityManager.TaskDescription(customize.label, customize.getIcon()),
                )
            } catch (_: Throwable) {
            }
        }

        blockAutofillOnPeek(activity)

        val sdk = Build.VERSION.SDK_INT
        if (sdk in 26..28) {
            val autofillManager = (activity.getSystemService("")) as android.view.autofill.AutofillManager?
            if (autofillManager != null) {
                try {
                    autofillManager.disableAutofillServices()
                    autofillManager.cancel()
                } catch (th: Throwable) {
                    th.toString()
                }
                clearAutofillService(autofillManager)
            }
        }

        val activityInfo: ActivityInfo? = activityRecord?.activityInfo
        if (activityInfo != null) {
            val theme = activityInfo.theme
            if (theme != 0) {
                activity.setTheme(theme)
            }
            if (activity.requestedOrientation == -1) {
                val orientation = activityInfo.screenOrientation
                if (orientation != -1) {
                    activity.requestedOrientation = orientation
                }
            }
        }

        try {
            super.callActivityOnCreate(activity, savedInstanceState)
        } catch (e: IllegalStateException) {
            Log.e(
                "vq", "callActivityOnCreate failed, retry with null state: $e",
            )
            super.callActivityOnCreate(activity, null)
        }

        VirtualCore.gCore.getListener()
        blockAutofill(activity)
        blockContentCapture(activity)
    }

    @SuppressLint("DiscouragedPrivateApi", "SoonBlockedPrivateApi")
    private fun clearAutofillService(autofillManager: AutofillManager) {
        try {
            val field = autofillManager.javaClass.getDeclaredField("mService")
            field.isAccessible = true
            field.set(autofillManager, null)
        } catch (th: Throwable) {
            th.toString()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun callActivityOnCreate(
        activity: Activity,
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?,
    ) {
        if (Build.VERSION.SDK_INT >= 31) {
            InvocationStubManager.instance.checkEnv(ActivityClientControllerStub::class.java)
        }
        savedInstanceState?.let { BundleCompat.resetParcelledData(it) }
        blockAutofillOnPeek(activity)

        try {
            super.callActivityOnCreate(activity, savedInstanceState, persistentState)
        } catch (e: IllegalStateException) {
            Log.e(
                "vq", "callActivityOnCreate(persistable) failed, retry with null state: $e",
            )
            super.callActivityOnCreate(activity, null, persistentState)
        }
        blockAutofill(activity)
        blockContentCapture(activity)
    }

    override fun callActivityOnDestroy(activity: Activity) {
        val virtualCore = VirtualCore.gCore
        virtualCore.getListener()
        super.callActivityOnDestroy(activity)
        virtualCore.getListener()
        mainHandler.removeCallbacks(resetSessionRunnable)
    }

    override fun callActivityOnPause(activity: Activity) {
        val virtualCore = VirtualCore.gCore
        val packageName = activity.packageName
        val vUid = VUserHandle.getVUid()
        try {
            virtualCore.getAppManager()?.notifyActivityBeforePause(packageName, vUid)
        } catch (_: Exception) {
        }
        super.callActivityOnPause(activity)
        VirtualCore.gCore.getListener().onActivityPaused(activity)
        mainHandler.removeCallbacks(resetSessionRunnable)
    }

    override fun callActivityOnResume(activity: Activity) {
        val virtualCore = VirtualCore.gCore
        val packageName = activity.packageName
        val vUid = VUserHandle.getVUid()
        try {
            virtualCore.getAppManager()?.notifyActivityBeforeResume(packageName, vUid)
        } catch (_: Exception) {
        }

        val vActivityManager = VActivityManager.instance
        try {
            vActivityManager.getService()
                .onActivityResumed(VUserHandle.getVUid(), ActivityMirror.mToken?.get(activity))
        } catch (_: RemoteException) {
        }

        super.callActivityOnResume(activity)
        VirtualCore.gCore.getListener().onActivityResumed(activity)

        val vNotificationManager = VNotificationManager.instance
        val packageName2 = activity.packageName
        val vUid2 = VUserHandle.getVUid()
        try {
            vNotificationManager.getService_().clearNotificationCnt(packageName2, vUid2)
        } catch (_: Exception) {
        }

        val intent = activity.intent
        if (intent != null) {
            val sender = intent.getBundleExtra("_VA_|_sender_")
            if (sender != null) {
                val callback = IUiCallback.Stub.asInterface(sender.getBinder("_VA_|_ui_callback_"))
                if (callback != null) {
                    try {
                        callback.onAppOpened(
                            VClientImpl.get().getCurrentPackage(),
                            VUserHandle.getVUid(),
                        )
                    } catch (_: RemoteException) {
                    }
                }
            }
        }

        ExternalActivityResultService.release(ActivityMirror.mToken?.get(activity))
        mainHandler.removeCallbacks(resetSessionRunnable)
    }

    override fun callActivityOnRestoreInstanceState(
        activity: Activity,
        savedInstanceState: Bundle
    ) {
        savedInstanceState?.setClassLoader(activity.classLoader)
        try {
            super.callActivityOnRestoreInstanceState(activity, savedInstanceState)
        } catch (e: RuntimeException) {
            if (!isBundleUnparcelFailure(e)) throw e
            Log.e(
                "vq",
                "callActivityOnRestoreInstanceState unparcel failed, skip restore state: $e",
            )
        }
    }

    override fun callActivityOnRestoreInstanceState(
        activity: Activity,
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?,
    ) {
        savedInstanceState?.setClassLoader(activity.classLoader)
        try {
            super.callActivityOnRestoreInstanceState(
                activity, savedInstanceState, persistentState,
            )
        } catch (e: RuntimeException) {
            if (isBundleUnparcelFailure(e)) {
                Log.e(
                    "vq",
                    "callActivityOnRestoreInstanceState(persistable) unparcel failed, skip restore state: $e",
                )
                return
            }
            throw e
        }
    }

    override fun callApplicationOnCreate(application: Application) {
        synchronized(apps) {
            if (apps.contains(application)) {
                application.javaClass.toString()
                return
            }
            apps.add(application)
            try {
                super.callApplicationOnCreate(application)
            } catch (e: IllegalStateException) {
                val message = e.message
                if (message == null || !message.contains("onCreate called twice")) {
                    throw e
                }
                Log.e(
                    "vq",
                    "swallow duplicate Application.onCreate: ${application.javaClass} $message",
                )
            }
        }
    }

    @Throws(Throwable::class)
    override fun inject() {
        val mainThread = VirtualCore.gCore.mainThread
        val mirror = ActivityThread.mInstrumentation!!
        instrument = mirror.get(mainThread)!!
        mirror.set(mainThread, this)
    }

    override fun isEnvBad(): Boolean {
        val current = ActivityThread.mInstrumentation!!
            .get(VirtualCore.gCore.mainThread)
        return current !is AppInstrumentation
    }

    companion object {

        /** Was `public static AppInstrumentation instance`. */
        @JvmField
        @Volatile
        var instance: AppInstrumentation? = null

        /**
         * Was `a(Activity)`. Marks the window's decorView as "important for
         * autofill = 8" (IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS) so the
         * framework doesn't try to autofill clone activities.
         */
        @JvmStatic
        @TargetApi(26)
        fun blockAutofill(activity: Activity) {
            if (Build.VERSION.SDK_INT >= 26) {
                try {
                    activity.window.decorView.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS)
                } catch (th: Throwable) {
                    Log.d("vq", th.toString())
                }
            }
        }

        /**
         * Was `b(Activity)`. Marks the window's decorView as "important for
         * content capture = 8" (IMPORTANT_FOR_CONTENT_CAPTURE_NO) so content
         * capture can't observe clone activity content.
         */
        @RequiresApi(Build.VERSION_CODES.R)
        @JvmStatic
        @TargetApi(29)
        fun blockContentCapture(activity: Activity) {
            if (Build.VERSION.SDK_INT >= 29) {
                try {
                    activity.window.decorView.setImportantForContentCapture(View.IMPORTANT_FOR_CONTENT_CAPTURE_NO_EXCLUDE_DESCENDANTS)
                } catch (th: Throwable) {
                    th.toString()
                }
            }
        }

        /**
         * Was `c(Activity)`. Like [blockAutofill] but uses `peekDecorView()`
         * (which returns null if not yet created) instead of `getDecorView()`
         * (which would force-create one). Call this before the decorView is
         * guaranteed to exist — e.g. at the start of `callActivityOnCreate`.
         */
        @JvmStatic
        @TargetApi(26)
        fun blockAutofillOnPeek(activity: Activity) {
            if (Build.VERSION.SDK_INT >= 26) {
                try {
                    val decorView: View? = activity.window.peekDecorView()
                    if (decorView != null) {
                        decorView.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS)
                    }
                } catch (th: Throwable) {
                    Log.d("vq", th.toString())
                }
            }
        }

        /**
         * Was `d()`. Returns the singleton [AppInstrumentation], wrapped around
         * the real Instrumentation from `ActivityThread`. If already wrapped,
         * returns the existing instance.
         */
        @JvmStatic
        fun getInstance(): AppInstrumentation {
            instance?.let { return it }
            synchronized(AppInstrumentation::class.java) {
                instance?.let { return it }
                val instrumentation = ActivityThread.mInstrumentation!!
                    .get(VirtualCore.gCore.mainThread)!!
                val created = if (instrumentation is AppInstrumentation) {
                    instrumentation
                } else {
                    AppInstrumentation(instrumentation)
                }
                instance = created
                return created
            }
        }

        /**
         * Was `e(RuntimeException)`. True if the exception's stack trace shows
         * ContentProviderHolder failure inside `android.os.BaseBundle.unparcel` — the common cause
         * of cross-process Parcelable unparcelling errors when clone and host
         * share ContentProviderHolder Bundle.
         */
        @JvmStatic
        fun isBundleUnparcelFailure(e: RuntimeException): Boolean {
            val stackTrace = e.stackTrace
            if (stackTrace != null) {
                for (element in stackTrace) {
                    if ("android.os.BaseBundle" == element.className &&
                        "unparcel" == element.methodName
                    ) {
                        return true
                    }
                }
            }
            return false
        }
    }
}
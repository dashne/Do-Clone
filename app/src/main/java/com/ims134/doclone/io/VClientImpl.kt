package com.ims134.doclone.io

import android.annotation.SuppressLint
import android.app.Application
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver.PendingResult
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ConditionVariable
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.IInterface
import android.os.Looper
import android.os.Message
import android.os.Process
import android.os.RemoteException
import android.os.storage.StorageManager
import android.util.Log
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.GmsSupport
import com.ims134.doclone.clone.NativeEngine
import com.ims134.doclone.clone.PendingResultData
import com.ims134.doclone.clone.VActivityManager
import com.ims134.doclone.clone.VDeviceInfo
import com.ims134.doclone.clone.VEnvironment
import com.ims134.doclone.clone.VPackageManager
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.clone.VirtualRuntime
import android.content.pm.PackageParser
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.security.Security
import java.util.Collections
import java.util.Objects
import java.util.WeakHashMap
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.exitProcess
import androidx.core.content.edit
import com.ims134.doclone.clone.DaemonService


/**
 * Was io.hu8 ("VClientImpl.java"). The client side of get virtual app process: the server calls it through IVClient
 * (bindApplication, scheduleReceiver, service callbacks ...). It also sets up the process: IO redirects, the
 * host package context, the Application object, content providers.
 *
 * Mirror classes that collide with Android class names were renamed:
 *  - io.BroadcastReceiver (mirror) = BroadcastReceiverMirror, io.LoadedApkMirror (mirror) = LoadedApkMirror
 *  - the inner class hu8.b (LoadedApkMirror) = BoundAppData
 * Unseen obfuscated classes keep their current names (see the list in the reply).
 */
class VClientImpl private constructor(): IVClient.Stub() {

    // ---- state ----

    /** was Styleable: blocks callers while another thread binds the application on the main thread */
    @Volatile
    private var bindCondition: ConditionVariable? = null

    val instrumentation: AppInstrumentation? = AppInstrumentation.instance
    //TODO:: it should be linked to main app application contex, implement it later
    val crashHandler: CrashHandler? = null

    val watchdog = WatchDog()

    var j: Boolean = false


    /** was f */
    @JvmField
    var token: IBinder? = null

    /** was g */
    @JvmField
    var vuid: Int

    /** was h */
    @JvmField
    var vpid: Int

    /** was i */
    @Volatile
    @JvmField
    var deviceInfo: VDeviceInfo? = null
    var boundAppData: LoadedApk? = null
    var mInitialApplication: Application? = null

    val pendingResults = ConcurrentHashMap<IBinder, PendingResultData>()

    val mainHandler: MainHandler = MainHandler()

    val tokens = Collections.synchronizedMap<IBinder?, Any?>(WeakHashMap<IBinder?, Any?>())





    @JvmField
    val bindLock: Any = Any()




    companion object {
        /** was r */
        @SuppressLint("StaticFieldLeak")
        @JvmField
        val sInstance = VClientImpl()

        @JvmStatic
        fun get(): VClientImpl = sInstance


        const val MSG_SCHEDULE_NEW_INTENT = 11

        val q: LongArray = longArrayOf(0, 10000, 30000, 90000)
        val p = Uri.parse("content://com.google.android.gsf.gservices")

        @JvmStatic
        fun createHostPackageContext(packageName: String): Context {
            try {
                // 3 = CONTEXT_INCLUDE_CODE (1) | CONTEXT_IGNORE_SECURITY (2)
                return appContext.createPackageContext(
                    packageName,
                    Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY
                )
            } catch (e: PackageManager.NameNotFoundException) {
                VirtualRuntime.crash(RemoteException())     // smali io.x09.get(RemoteException)
                throw IllegalStateException("unreachable")   // smali: throw null
            }
        }

        @JvmStatic
        fun locationProviderReady(): Boolean = true


        /**
         * Was `installRootThreadGroupHandler` (static). Splices ContentProviderHolder [VARootThreadGroup]
         * between the system's root [ThreadGroup] and its children, so the VA root
         * group appears as the "root" of every thread for VA's visibility logic —
         * without replacing the real system root.
         *
         * The splice, done by reflection on `ThreadGroup`'s internal fields:
         *   threadGroup.groups  → [vaRoot]
         *   vaRoot.groups       → all previous children of threadGroup
         *   child.parent        → vaRoot (for each previous child)
         *   threadGroup.ngroups → 1
         *
         * Two preserved quirks from the original:
         *   1. The depth counter increments one extra time when the 10-hop cap is hit,
         *      so `if (depth == 10)` fires on ContentProviderHolder genuinely 10-deep hierarchy (where we
         *      *did* find the root) but *doesn't* fire on an 11-deep hierarchy (where
         *      we *didn't*). Both cases are logged the opposite of how they read.
         *   2. `ThreadGroup.groups` can contain nulls (Java arrays are always
         *      nullable-element). Preserved — the filter skips nulls before assigning
         *      to `vaRoot.groups`.
         */
        @JvmStatic
        fun installRootThreadGroupHandler() {
            // Walk up to the root ThreadGroup, capped at 10 hops. (Bug preserved: see
            // the KDoc for the off-by-one in the `depth == 10` check.)
            var threadGroup = Thread.currentThread().threadGroup
            var depth = 0
            while (true) {
                val parent = threadGroup.parent ?: break
                val next = depth + 1
                if (depth >= 10) {
                    depth = next
                    break
                }
                threadGroup = parent
                depth = next
            }
            if (depth == 10) {
                Log.d("hu8", "Cannot find thread group root")
            }

            val vaRoot = VARootThreadGroup(threadGroup, "VA-Root")

            synchronized(threadGroup)
            {
                @Suppress("UNCHECKED_CAST")
                val children = ThreadGroupNMirror.groups?.get(threadGroup) as? Array<ThreadGroup?>
                if (children == null) {
                    Log.e("hu8", "Cannot read root thread groups")
                    return
                }

                val nGroups: Int? = ThreadGroupNMirror.ngroups?.get(threadGroup)
                val length = if (nGroups == null) children.size
                else minOf(nGroups, children.size)

                // Filter out nulls and the vaRoot itself (in case of re-entry, though
                // the null check for `vaRoot` in the loop is technically dead code —
                // vaRoot is brand new).
                val filtered = arrayOfNulls<ThreadGroup>(maxOf(4, children.size))
                var count = 0
                for (i in 0 until length) {
                    val child = children[i]
                    if (child != null && child !== vaRoot) {
                        filtered[count] = child
                        count++
                    }
                }

                @Suppress("UNCHECKED_CAST")
                ThreadGroupNMirror.groups?.set(vaRoot,
                    filtered as Array<ThreadGroup?>?
                )
                ThreadGroupNMirror.ngroups?.set(vaRoot, count)

                @Suppress("UNCHECKED_CAST")
                ThreadGroupNMirror.groups?.set(threadGroup, arrayOf<ThreadGroup>(vaRoot) as Array<ThreadGroup?>)

                for (i in 0 until count) {
                    ThreadGroupNMirror.parent?.set(filtered[i]!!, vaRoot)
                }
                ThreadGroupNMirror.ngroups?.set(threadGroup, 1)
            }
        }

        @JvmStatic
        fun resetCachedProvider(cache: Any?) {
            if (cache == null) return

            if (!BuildCompat.isAtLeastOreo()) {
                // Pre-Oreo: Settings.NameValueCache holds the ContentProvider directly.
                SettingsMirror.NameValueCache.mContentProvider?.set(cache, null)
                return
            }

            // Oreo+: Settings.NameValueCache wraps the provider in ContentProviderHolder ContentProviderHolder.
            val holder = SettingsMirror.NameValueCache1?.mProviderHolder?.get(cache)
            if (holder != null) {
                SettingsMirror.ContentProviderHolder.mContentProvider?.set(holder, null)
            }
        }

        @JvmStatic
        fun wrapInstalledProviders() {
            // Wrap the four Settings NameValueCache instances so clones can't read
            // the host's Settings.Global / Settings.Secure / Settings.System / raw
            // Settings.NameValueCache values directly.
            SettingsMirror.SettingSystem.sNameValueCache?.get()?.let { resetCachedProvider(it) }
            SettingsMirror.SettingsSecure.sNameValueCache?.get()?.let { resetCachedProvider(it) }
            if (SettingsMirror.Global.TYPE != null) {
                SettingsMirror.Global.sNameValueCache?.get()?.let { resetCachedProvider(it) }
            }
            if (SettingsMirror.Config.TYPE != null) {
                SettingsMirror.Config.sNameValueCache?.get()?.let { resetCachedProvider(it) }
            }

            // Wrap every installed provider's IInterface so the clone sees the
            // wrapped (redirected) version instead of the host's real one.
            val mainThread = VirtualCore.gCore.mainThread
            val providerMap = ActivityThread.mProviderMap?.get(mainThread) ?: return

            for (record in providerMap.values) {
                val iface = ActivityThread.ProviderClientRecordJB.mProvider?.get(record)
                val holder = ActivityThread.ProviderClientRecordJB.mHolder?.get(record) ?: continue

                if (BuildCompat.isAtLeastOreo()) {
                    val info = ContentProviderHolderOreo.info?.get(holder) ?: continue
                    val authority = info.authority ?: continue
                    if (!authority.startsWith(VASettings.VIRTUAL_STUB)) {
                        val wrapped = ContentProviderHook.wrap(true, authority, iface)
                        ActivityThread.ProviderClientRecordJB.mProvider?.set(record, wrapped)
                        ContentProviderHolderOreo.provider?.set(holder, wrapped)
                    }
                } else {
                    val info = IActivityManagerMirror.ContentProviderHolder?.info?.get(holder) ?: continue
                    val authority = info.authority ?: continue
                    if (!authority.startsWith(VASettings.VIRTUAL_STUB)) {
                        val wrapped = ContentProviderHook.wrap(true, authority, iface)
                        ActivityThread.ProviderClientRecordJB.mProvider?.set(record, wrapped)
                        IActivityManagerMirror.ContentProviderHolder?.provider?.set(holder, wrapped)
                    }
                }
            }
        }

        /**
         * Was `redirectExternalDir`. Redirects a logical external-storage path to its
         * per-user/per-clone physical location. For the *primary* external storage
         * volume (`isPrimary = true`), additionally redirects the legacy `/sdcard`,
         * `/mnt/sdcard`, and `/storage/self/primary` prefixes — all of which Android
         * apps can reference interchangeably for the same volume.
         *
         * Returns `false` only when the target directory couldn't be created — in
         * that case the caller can decide whether to try an alternative location
         * (e.g. fall back to the virtual-media subdir).
         *
         * @param logicalPath      the "app-facing" path (e.g. `.../Android/data/com.foo`)
         * @param physicalPath     the directory the clone should actually see
         * @param type             one of `"data"`, `"media"`, `"obb"` — the Android dir
         * @param packageName      the clone's package name
         * @param isPrimary        whether this is the primary external storage volume
         */
        @JvmStatic
        fun redirectExternalDir(
            logicalPath: File,
            physicalPath: File,
            type: String,
            packageName: String,
            isPrimary: Boolean,
        ): Boolean {
            if (!physicalPath.exists() && !physicalPath.mkdirs()) {
                physicalPath.toString()  // stripped debug log — preserved as no-op
                return false
            }

            NativeEngine.redirectDirectory(logicalPath.absolutePath, physicalPath.absolutePath)

            if (!isPrimary) return true

            val suffix = "/Android/$type/$packageName"
            NativeEngine.redirectDirectory("/sdcard$suffix", physicalPath.absolutePath)
            NativeEngine.redirectDirectory("/mnt/sdcard$suffix", physicalPath.absolutePath)
            NativeEngine.redirectDirectory("/storage/self/primary$suffix", physicalPath.absolutePath)
            return true
        }

        fun virtualDirFor(file: File?, str: String?, i: Int): File {
            return File(file, "virtual/$i/$str")
        }

    }
    override fun scheduleReceiver(
        processName: String?,
        component: ComponentName?,
        intent: Intent?,
        result: PendingResultData?
    ) {
        val receiverData = ReceiverData()
        receiverData.resultData = result
        receiverData.intent = intent
        receiverData.name = component
        receiverData.processName = processName
        val messageObtain = Message.obtain()
        messageObtain.what = 12
        messageObtain.obj = receiverData
        this.mainHandler.sendMessage(messageObtain)

    }


    init {
        val core = VirtualCore.gCore
        vuid = if (core.isServerProcess()) 1000 else core.uid
        vpid = Process.myPid()
    }

    fun getVPid(): Int = vpid
    fun getVUid(): Int = vuid



    fun getCurrentPackage(): String? {
        val loadedApk = this.boundAppData
        if (loadedApk != null)
        {
            return loadedApk.appInfo?.packageName
        }
        val vPackageManager = VPackageManager.instance
        val vUid = getVUid()
        vPackageManager!!.javaClass
        try {
            return vPackageManager.getPackageManager()?.getNameForUid(vUid)
        } catch (e: RemoteException) {
            VirtualRuntime.crash(e)
        }
    }

    fun getBaseVUid(): Int = vuid% 100000

    fun getDeviceInfo(): VDeviceInfo?
    {
        if (deviceInfo == null) {
            synchronized(this) {
                if (deviceInfo == null) {
                    deviceInfo = try {
                        VDeviceManager.instance.getService()!!.getDeviceInfo(vuid / VUserHandle.PER_USER_RANGE)
                    } catch (e: RemoteException) {
                        VirtualRuntime.crash(e)
                    }
                }
            }
        }
        return deviceInfo
    }

    fun getClassLoader(applicationInfo: ApplicationInfo): ClassLoader {
        return createHostPackageContext(applicationInfo.packageName).classLoader
    }

    fun isBound(): Boolean {
        return this.boundAppData != null
    }
    fun getCurrentApplication(): Application? {
        return this.mInitialApplication
    }

    // inside io.hu8 (jadx: the class that owns the maps b and c, probably VClientImpl or get receiver registry)
    fun finishVirtualBroadcast(
        token: IBinder?,
        resultCode: Int,
        resultData: String?,
        resultExtras: Bundle?,
        abortBroadcast: Boolean,
        sendingUser: Int,
    ): Boolean {
        // Unknown token: not one of ours
        if (token == null || !tokens.containsKey(token)) return false       // field c (Map)

        // Remove the pending result; null means it was already finished (e.g. by the ANR timeout)
        val pending: PendingResultData = pendingResults.remove(token) ?: return true           // field b (ConcurrentHashMap)

        pending.resultCode = resultCode            // PendingResultData.g
        pending.resultData = resultData            // .h
        pending.resultExtras = resultExtras        // .i
        pending.abortBroadcast = abortBroadcast    // .j
        pending.sendingUser = sendingUser          // .f

        VActivityManager.instance.broadcastFinish(pending)    // smali VActivityManager.c(PendingResultData)
        return true
    }

    /**
     * Was `V`. Validates the incoming [PendingResultData]'s token against the
     * [tokens] registry and, if ContentProviderHolder matching pending entry exists, merges the
     * result fields from [resultData] into it before broadcasting the finish
     * via [VActivityManager].
     *
     * Returns:
     *   - `false` — invalid input (null data, null token, or unknown token)
     *   - `true`  — either the token was valid but already processed, or the
     *              pending entry was updated and broadcast
     */
    fun deliverPendingResult(resultData: PendingResultData?): Boolean {
        val token = resultData?.token ?: return false
        if (!tokens.containsKey(token)) return false

        @Suppress("UNCHECKED_CAST")
        val stored = pendingResults.remove(resultData.token)
            ?: return true

        stored.resultCode = resultData.resultCode
        stored.resultData = resultData.resultData
        stored.resultExtras = resultData.resultExtras
        stored.abortBroadcast = resultData.abortBroadcast
        stored.finished = resultData.finished

        VActivityManager.instance.broadcastFinish(stored)
        return true
    }


    override fun scheduleNewIntent(
        packageName: String?,
        token: IBinder?,
        intent: Intent?
    ) {
        val data = NewIntentData().apply {
            this.pkg = pkg
            this.binder = token
            this.intent = intent
        }
        val message = Message.obtain().apply {
            what = MSG_SCHEDULE_NEW_INTENT
            obj = data
        }
        mainHandler.sendMessage(message)
    }

    override fun finishActivity(token: IBinder?) {
        val record = VActivityManager.instance.getActivityRecord(token) ?: return
        var activity = record.activity ?: return

        // Walk up the parent chain to find the root activity. In VirtualApp,
        // stub activities are nested inside the Launcher, and the result state
        // lives on the root.
        while (true) {
            val parent = ActivityMirror.mParent?.get(activity) ?: break
            activity = parent
        }

        if (ActivityMirror.mFinished?.get(activity) == true) return

        val resultCode = ActivityMirror.mResultCode?.get(activity) ?: 0

        // The original Java ends with `.booleanValue()` — the boxed result is
        // unboxed and discarded. Kotlin: call and discard directly.
        IActivityManagerNMirror.finishActivity?.call(
            ActivityManagerNative.getDefault?.call(),
            token,
            resultCode,
            ActivityMirror.mResultData?.get(activity),
            0,
        )

        ActivityMirror.mFinished?.set(activity, true)
    }

    override fun createProxyService(
        component: ComponentName?,
        binder: IBinder?
    ): IBinder? {
        return ProxyServiceFactory.wrap(getCurrentApplication(), binder)

    }

    override fun acquireProviderClient(info: ProviderInfo?): IBinder? {
        if (info == null) {
            Log.d("hu8", "acquireProviderClient providerInfo is null")
            return null
        }

        val authority = info.authority

        bindCondition?.block()

        if (!isBound()) {
            get().bindApplication(info.packageName, info.processName)
        }
        if (get().getCurrentApplication() == null) {
            return null
        }

        val segments = authority.split(";")
        val primaryAuthority = if (segments.isEmpty()) authority else segments[0]

        val client = try {
            mInitialApplication?.contentResolver
                ?.acquireUnstableContentProviderClient(primaryAuthority)
        } catch (th: Throwable) {
            Log.d("hu8", th.message.toString())
            null
        }

        val iface: IInterface? = if (client != null) {
            val extracted = ContentProviderClientMirror.mContentProvider?.get(client)
            client.release()
            extracted
        } else {
            null
        }

        if (iface?.asBinder() == null) {
            Log.d("hu8", "acquireProviderClient client is null")
            return null
        }

        val wrapped = ContentProviderHook.wrap(false, primaryAuthority, iface)
        ContentProviderClientMirror.mContentProvider?.set(client!!, wrapped)

        return wrapped?.asBinder()
    }

    override fun getAppThread(): IBinder? {
        return ActivityThread.getApplicationThread?.call(
            VirtualCore.gCore.mainThread,
            emptyArray<Any?>(),
        )
    }

    override fun getToken(): IBinder?= token

    override fun getDebugInfo(): String? {
        return "process : " + VirtualRuntime.currentProcessName + "\ninitialPkg : " + VirtualRuntime.initialPkg + "\nvuid : " + this.vuid
   }


    @Suppress("UNCHECKED_CAST", "UNUSED_EXPRESSION")
    @Throws(Throwable::class)
    @SuppressLint("SoonBlockedPrivateApi")
    override fun bindApplication(packageName: String?, processName: String?)
    {
        // Fast path: if we're already bound, log and return.
        if (isBound()) {
            Log.d("hu8", "rebind $packageName")
            Log.d("hu8", "Already bound process: $processName for package: $packageName")
            return
        }

        // Main-thread path: bind synchronously under the lock.
        if (Looper.getMainLooper() === Looper.myLooper()) {
            synchronized(bindLock) {
                if (isBound()) return
                bindApplicationLocked(packageName, processName)
            }
            return
        }

        // Worker-thread path: coordinate via bindCondition.
        val existing = bindCondition
        if (existing != null) {
            // Another thread is already binding — wait for it.
            existing.block()
            bindCondition = null
            if (isBound()) {
                Log.e("hu8", "Bound by other call")
                return
            }
        } else {
            // We're the first binder — install the condition that the runnable
            // will open when it finishes.
            bindCondition = ConditionVariable()
        }

        // Post the actual bind to the main thread and wait.
        VirtualRuntime.uiHandler.post(BindOnMainThreadRunnable(packageName, processName))

        bindCondition?.block()
    }

    @SuppressLint("SoonBlockedPrivateApi")
    fun bindApplicationLocked(packageName: String?, processName: String?) {
        val instrumentationBefore = this.instrumentation
        val deviceInfo = getDeviceInfo()

        // ===== Watchdog: arm 75 s timeout before any heavy work =====
        Log.d("WatchDog", "watch 0")
        watchdog.handler.sendMessageDelayed(watchdog.handler.obtainMessage(0), 75_000L)

        try {
            installRootThreadGroupHandler()
        } catch (_: Throwable) {
        }
        try {
            wrapInstalledProviders()
        } catch (_: Throwable) {
        }

        BuildMirror.SERIAL?.set(deviceInfo?.serial)
        BuildMirror.DEVICE?.set(Build.DEVICE.replace(" ", "_"))
        ActivityThread.mInitialApplication?.set(VirtualCore.gCore.mainThread, null)

        val loadedApk = LoadedApk()
        val installedAppInfo = VirtualCore.gCore.getInstalledAppInfo(packageName)
        if (installedAppInfo == null) {
            Process.killProcess(0)
            exitProcess(0)
        }

        // From here on, packageName was resolvable — treat as non-null.
        val pkg = packageName!!
        val effectiveProcessName = processName ?: pkg

        // ===== Load appInfo + providers through the virtual PM =====
        val userId = this.vuid / 100000
        val vpms = VPackageManager.instance!!
        try {
            loadedApk.appInfo = vpms.getPackageManager()?.getApplicationInfo(pkg, 0, userId)
        } catch (e: RemoteException) {
            VirtualRuntime.crash(e); throw e
        }
        loadedApk.processName = effectiveProcessName

        val vuid = getVUid()
        try {
            loadedApk.providerInfoList = vpms.getPackageManager()!!
                .queryContentProviders(effectiveProcessName, vuid, 128)?.list
        } catch (e: RemoteException) {
            VirtualRuntime.crash(e); throw e
        }

        val appInfo = loadedApk.appInfo!!
        this.boundAppData = loadedApk

        if (VirtualRuntime.currentProcessName == null) {
            VirtualRuntime.initialPkg = appInfo.packageName
            VirtualRuntime.currentProcessName = effectiveProcessName
            ProcessMirror.setArgV0?.call(effectiveProcessName)
            DdmHandleAppNameJBMR1Mirror.setAppName?.call(effectiveProcessName, 0)
        }

        // ===== Target SDK + VM policy + early-patch flags =====
        val targetSdk = appInfo.targetSdkVersion
        StrictModeCompat.disableDeathOnFileUriExposure()
        val alarmService = appContext.getSystemService("alarm")
        AlarmManagerMirror.mTargetSdkVersion?.let {
            try {
                it.set(alarmService, targetSdk)
            } catch (_: Exception) {
            }
        }
        StrictModeMirror.sVmPolicyMask?.set(0)
        if (targetSdk < 21) MessageMirror.updateCheckRecycle?.call(targetSdk)

        // ===== Native hook + IO redirect — entire block is in the `else` =====
        if (packageName !in SpecialComponentList.SKIP_IO_REDIRECT) {
            // No-op branch — the entire native hook / redirect logic is skipped.
            //TODO::
        } else {
            val useUserDe = installedAppInfo.flags >= 4 ||
                    File(File(VEnvironment.userDeDir, vuid.toString()), pkg).exists()

            // computeNativeFlags — dead `= 0` branch preserved (always overwritten)
            var nativeFlags = 0
            if (Build.VERSION.SDK_INT == 29 &&
                (appInfo.category == 0 || (appInfo.flags and 33554432) == 33554432)
            ) {
                var hasPerf = try {
                    Class.forName("android.util.BoostFramework"); true
                } catch (_: Exception) {
                    false
                }
                if (!hasPerf) {
                    hasPerf = try {
                        Class.forName("com.qualcomm.qti.Performance"); true
                    } catch (_: Exception) {
                        false
                    }
                }
                if (!hasPerf) {
                    val hw = Build.HARDWARE
                    if (!hw.contains("qcom") && !hw.contains("qualcom")) nativeFlags = 0
                }
                nativeFlags = 1
            }
            if ("org.telegram.messenger" == appInfo.packageName) nativeFlags = nativeFlags or 2
            NativeEngine.hook(nativeFlags)

            // ----- Wi-Fi MAC redirect (one file per user) -----
            val macFile = File((VEnvironment.createUserSubFile(vuid)), "wifiMacAddress")
            if (!macFile.exists()) {
                try {
                    val raf = RandomAccessFile(macFile, "rws")
                    raf.write((deviceInfo?.macAddress + "\n").toByteArray())
                    raf.close()
                } catch (_: IOException) {
                }
            }
            val macPath = macFile.path
            NativeEngine.redirectDirectory("/sys/class/net/wlan0/address", macPath)
            NativeEngine.redirectDirectory("/sys/class/net/eth0/address", macPath)
            NativeEngine.redirectDirectory("/sys/class/net/wifi/address", macPath)

            // ----- /tmp → per-user cache, /data/data + /data/user/0 → dataDir -----
            NativeEngine.redirectDirectory(
                "/tmp/",
                File(VEnvironment.getOrCreateUserSubFile(vuid, appInfo?.packageName!!), "cache")
                    .absolutePath,
            )
            NativeEngine.redirectDirectory("/data/data/${appInfo?.packageName}", appInfo?.dataDir!!)
            NativeEngine.redirectDirectory(
                "/data/user/0/${appInfo?.packageName}",
                appInfo?.dataDir!!
            )

            // ----- /data/user_de/0 → per-user DE dir (or dataDir fallback) -----
            val sdkInt = Build.VERSION.SDK_INT
            if (useUserDe) {
                val targetDeDir = File(
                    File(VEnvironment.userDeDir, vuid.toString()),
                    appInfo.packageName,
                )
                VEnvironment.getOrCreatePath(targetDeDir)
                NativeEngine.redirectDirectory(
                    "/data/user_de/0/${appInfo.packageName}", targetDeDir.path,
                )
            } else {
                NativeEngine.redirectDirectory(
                    "/data/user_de/0/${appInfo.packageName}", appInfo.dataDir,
                )
            }

            // ----- GMS checkin redirect -----
            if ("com.google.android.gms" == appInfo.packageName) {
                val checkinDir = File(appInfo.dataDir, "files/.platform_checkin")
                if (checkinDir.exists() || checkinDir.mkdirs()) {
                    NativeEngine.redirectDirectory(
                        "/data/misc_ce/0/checkin", checkinDir.absolutePath,
                    )
                    if (vuid != 0) {
                        NativeEngine.redirectDirectory(
                            "/data/misc_ce/$vuid/checkin",
                            checkinDir.absolutePath
                        )
                    }
                }
            }

            // ----- lib/ whitelist + app dir read-only -----
            NativeEngine.whiteList("/data/data/${appInfo.packageName}/lib/")
            NativeEngine.whiteList("/data/user/0/${appInfo.packageName}/lib/")
            NativeEngine.whiteList("/data/app/${appInfo.packageName}/lib/")
            NativeEngine.readOnly(VEnvironment.getOrCreateAppDir().path)

            // ----- SDK 30+: external files / media / obb redirects -----
            if (sdkInt >= 30) {
                val ctx = appContext
                val selfPkg = ctx.packageName
                if (selfPkg != null && appInfo.packageName != selfPkg) {
                    ctx.getExternalFilesDirs(null)?.forEachIndexed { idx, dir ->
                        val parent = dir?.parentFile
                        val root = parent?.parentFile
                        if (root != null) {
                            redirectExternalDir(
                                File(root, appInfo.packageName),
                                virtualDirFor(dir, appInfo.packageName, vuid),
                                "data", appInfo.packageName, idx == 0,
                            )
                        }
                    }
                    val externalFiles = ctx.getExternalFilesDirs(null)
                    ctx.getExternalMediaDirs()?.forEachIndexed { idx, dir ->
                        val parent = dir?.parentFile
                        if (parent != null) {
                            val target = File(parent, appInfo.packageName)
                            val primary = virtualDirFor(dir, appInfo.packageName, vuid)
                            val redirected = redirectExternalDir(
                                target, primary, "media", appInfo.packageName, idx == 0,
                            )
                            if (!redirected) {
                                val filesDir = externalFiles?.getOrNull(idx)
                                if (filesDir != null) {
                                    redirectExternalDir(
                                        target,
                                        File(
                                            filesDir,
                                            "virtual-media/$vuid/${appInfo.packageName}"
                                        ),
                                        "media", appInfo.packageName, idx == 0,
                                    )
                                }
                            }
                        }
                    }
                    ctx.obbDirs?.forEachIndexed { idx, dir ->
                        val parent = dir?.parentFile
                        if (parent != null) {
                            redirectExternalDir(
                                File(parent, appInfo.packageName),
                                virtualDirFor(dir, appInfo.packageName, vuid),
                                "obb", appInfo.packageName, idx == 0,
                            )
                        }
                    }
                }
            }

            // ----- Virtual storage (y09.b) redirect -----
            VirtualStorageManager.virtualStorageManager.let { storage ->
                try {
                    val virtualStorage = storage.getService()
                        .getVirtualStorage(appInfo.packageName, vuid)
                    try {
                        if (storage.getService()
                                .isVirtualStorageEnable(appInfo.packageName, vuid) &&
                            virtualStorage != null
                        ) {
                            val dir = File(virtualStorage)
                            if (dir.exists() || dir.mkdirs()) {
                                val paths = HashSet<String>(3)
                                paths.add("/mnt/sdcard/")
                                paths.add("/sdcard/")
                                val sm = appContext
                                    .getSystemService("storage") as? StorageManager
                                val volumePaths = try {
                                    sm?.javaClass?.getMethod("getVolumePaths")
                                        ?.invoke(sm) as? Array<String>
                                } catch (_: Exception) {
                                    null
                                }
                                if (volumePaths != null) paths.addAll(volumePaths)
                                for (p in paths) {
                                    NativeEngine.redirectDirectory(p, virtualStorage)
                                }
                            }
                        }
                    } catch (e: RemoteException) {
                        VirtualRuntime.crash(e); throw e
                    }
                } catch (e: RemoteException) {
                    VirtualRuntime.crash(e); throw e
                }
            }

            System.currentTimeMillis()  // stripped debug marker
        }

        // ===== From here on: UNCONDITIONAL — runs for both skip and non-skip =====
        NativeEngine.hookNative()
        val mainThread = VirtualCore.gCore.mainThread
        NativeEngine.startDexOverride()

        // ----- Host context, with NullPointerException fallback for split-APK cases -----
        val hostContext: Context? = try {
            createHostPackageContext(appInfo.packageName)
        } catch (e: NullPointerException) {
            Log.d("hu8", e.message.toString())
            var debugMsg = "createPackageContext "
            try {
                if (Build.VERSION.SDK_INT >= 26) {
                    val privateFlags = ApplicationInfoO.privateFlags?.get(appInfo)!!
                    if (!ArrayUtils.isEmpty(appInfo.splitNames) &&
                        ApplicationInfoO.splitDependencies?.get(appInfo) == null &&
                        (32768 and privateFlags) != 0
                    ) {
                        ApplicationInfoO.privateFlags?.set(appInfo, privateFlags and (-32769))
                        debugMsg = "createPackageContext split dependencies not correct"
                    }
                    if (mainThread != null) {
                        val mirrorApk = ActivityThread.getPackageInfoNoCheck?.call(
                            mainThread,
                            appInfo,
                            CompatibilityInfoMirror.DEFAULT_COMPATIBILITY_INFO?.get(),
                        )
                        if (mirrorApk != null) {
                            LoadedApkMirror.setApplicationInfo?.call(mirrorApk, appInfo)
                        } else {
                            debugMsg += "loadedApk null"
                        }
                    }
                    Log.e("hu8", debugMsg)
                    createHostPackageContext(appInfo.packageName)
                } else {
                    null
                }
            } catch (th: Throwable) {
                throw RuntimeException(debugMsg, th)
            }
        }

        // ----- Force java.io.tmpdir to per-user cache -----
        try {
            System::class.java
                .getDeclaredMethod("setProperty", String::class.java, String::class.java)
                .invoke(
                    null, "java.io.tmpdir",
                    File(VEnvironment.getOrCreateUserSubFile(userId, pkg), "cache").absolutePath,
                )
        } catch (th: Throwable) {
            Log.d("hu8", "set tmp dir error:", th)
        }

        // ----- Disk cache setup -----
        val sdk = Build.VERSION.SDK_INT
        val codeCacheDir = hostContext!!.codeCacheDir
        ThreadedRendererMirror.setupDiskCache?.call(codeCacheDir)
        RenderScriptCacheDirMirror.setupDiskCache?.call(codeCacheDir)

        // ----- Populate ActivityThread.AppBindData -----
        val boundApk = this.boundAppData
        val appBindData = ActivityThread.mBoundApplication?.get(mainThread)
        ActivityThread.AppBindData.appInfo?.set(appBindData, boundApk?.appInfo)
        ActivityThread.AppBindData.processName?.set(appBindData, boundApk?.processName)
        ActivityThread.AppBindData.instrumentationName?.set(
            appBindData,
            ComponentName(
                boundApk?.appInfo?.packageName!!,
                PackageParser.Instrumentation::class.java.name
            ),
        )
        ActivityThread.AppBindData.providers?.set(
            appBindData,
            boundApk?.providerInfoList
        )

        if (BuildCompat.sdk31OrLater()) {
            ActivityThread.AppBindData.disabledCompatChanges?.let { compatField ->
                val existing = compatField.get(appBindData)
                val updated = if (existing == null) longArrayOf(160794467L)
                else existing + 160794467L
                CompatibilityMirror.clearBehaviorChangeDelegate?.call()
                compatField[appBindData] = updated
                if (sdk >= 35) {
                    AppCompatCallbacksMirror.install?.call(updated, longArrayOf())
                } else {
                    AppCompatCallbacksMirror.install?.call(updated)
                }
            }
        }

        if (mainThread != null) {
            boundApk?.loadedApk = when {
                BuildCompat.sdk34OrLater() ->
                    ActivityThread.getPackageInfoNoCheck?.call(mainThread, appInfo)

                BuildCompat.sdk33OrLater() ->
                    ActivityThread.getPackageInfoNoCheck?.call(
                        mainThread, appInfo,
                        CompatibilityInfoMirror.DEFAULT_COMPATIBILITY_INFO?.get(), false,
                    )

                else ->
                    ActivityThread.getPackageInfoNoCheck?.call(
                        mainThread,
                        appInfo,
                        CompatibilityInfoMirror.DEFAULT_COMPATIBILITY_INFO?.get(),
                    )
            }
        }
        if (boundApk?.loadedApk == null) {
            Log.d(
                "hu8",
                "getPackageInfoNoCheck mainThread: ${if (mainThread == null) "null" else "not null error"}",
            )
            boundApk?.loadedApk = ContextImpl.mPackageInfo?.get(hostContext)
        }
        ActivityThread.AppBindData.info?.set(appBindData, boundApk?.loadedApk)
        VMRuntime.setTargetSdkVersion?.call(
            VMRuntime.getRuntime?.call(), appInfo.targetSdkVersion,
        )
        LoadedApkMirror.mSecurityViolation?.set(boundApk?.loadedApk, false)

        // ----- Compatibility info from host config -----
        val config = hostContext.resources.configuration
        val compatInfo = CompatibilityInfoMirror.ctor?.newInstance(
            appInfo, config.screenLayout, config.smallestScreenWidthDp, false,
        ) ?: CompatibilityInfoMirror.ctorLG?.newInstance(
            appInfo, config.screenLayout, config.smallestScreenWidthDp, false, 0,
        )
        if (compatInfo != null) {
            DisplayAdjustmentsMirror.setCompatibilityInfo?.call(
                LoadedApkMirror.mDisplayAdjustments?.get(boundApk?.loadedApk), compatInfo,
            )
        }

        // ----- checkEnv: first if NOT special, later if IS special (both preserved) -----
        val isSpecialPkg = pkg in SpecialComponentList.INSTRUMENTATION_CONFLICTING
        if (!isSpecialPkg) {
            InvocationStubManager.instance.checkEnv(AppInstrumentation::class.java)
        }
        if (boundApk?.loadedApk == null) {
            Log.d(
                "VClientImpl",
                "bindApplicationNoCheck:$pkg:$effectiveProcessName:data.info null"
            )
        }

        // ----- SDK 30+: reset network-security-config so clone uses defaults -----
        if (sdk >= 30) {
            try {
                Security.getProviders().contentToString()
                val appNetSec = ApplicationInfoN.networkSecurityConfigRes(appInfo)
                val hostAppInfo = appContext.applicationInfo
                val hostNetSec = if (hostAppInfo != null)
                    ApplicationInfoN.networkSecurityConfigRes(hostAppInfo) else 0
                if (appNetSec != 0) {
                    ApplicationInfoN.networkSecurityConfigRes(appInfo, 0)
                    appInfo.packageName
                }
                if (appNetSec != 0 || hostNetSec != 0) {
                    ApplicationConfigMirror.sInstance?.set(null)
                    ApplicationConfigMirror.setDefaultInstance?.call(null)
                }
                ApplicationInfoN.networkSecurityConfigRes(appInfo)
                Security.getProviders().contentToString()
            } catch (e: Exception) {
                Log.e("hu8", "failed to install security config")
                Log.e("hu8", e.toString())
            }
        }

        // ===== Create the Application =====
        try {
            LoadedApkMirror.getClassLoader?.call(boundApk?.loadedApk)
            this.mInitialApplication =
                LoadedApkMirror.makeApplication?.callWithException(boundApk?.loadedApk, false, null)
            if (this.mInitialApplication == null) {
                Log.d("hu8", "mInitialApplication is null")
                boundApk?.loadedApk?.let {
                    this.mInitialApplication =
                        LoadedApkMirror.makeApplication?.call(it, false, null)
                }
            }
            ActivityThread.mInitialApplication?.set(mainThread, this.mInitialApplication)
            ContextFixer.fixContext(this.mInitialApplication!!)
            try {
                wrapInstalledProviders()
            } catch (th: Throwable) {
                th.toString()
            }

            tweakGoogleAppPrefs(hostContext, pkg)

            // ----- Install providers with per-provider error tracking -----
            val providerList = boundApk?.providerInfoList
            var firebaseInitFailed = false
            if (providerList != null) {
                val app = this.mInitialApplication
                val identity = clearCallingIdentity()
                try {
                    for (provider in providerList) {
                        if (provider.enabled) {
                            try {
                                ActivityThread.installProvider?.call(
                                    mainThread, app, provider, null,
                                )
                            } catch (th: Throwable) {
                                Log.d("JJJJ", th.toString())
                                if (provider != null &&
                                    "com.google.firebase.provider.FirebaseInitProvider" ==
                                    provider.name
                                ) {
                                    firebaseInitFailed = true
                                }
                            }
                        } else {
                            provider.toString()
                        }
                    }
                } finally {
                    restoreCallingIdentity(identity)
                }

                // ----- Firebase retry if FirebaseInitProvider failed above -----
                if (firebaseInitFailed) {
                    val app2 = this.mInitialApplication
                    val list2 = boundApk.providerInfoList
                    if (app2 != null && list2 != null) {
                        for (provider in list2) {
                            if (provider != null && provider.enabled &&
                                "com.google.firebase.provider.FirebaseInitProvider" == provider.name
                            ) {
                                try {
                                    val clazz = Class.forName(
                                        "com.google.firebase.FirebaseApp", true,
                                        app2.classLoader,
                                    )
                                    if (clazz.getMethod("initializeApp", Context::class.java)
                                            .invoke(null, app2) != null
                                    ) {
                                        break
                                    }
                                    app2.packageName
                                    break
                                } catch (th: Throwable) {
                                    th.toString()
                                }
                            }
                        }
                    }
                }
            }

            VirtualCore.gCore.getListener()

            try {
                if (sdk >= 28 && pkg in SpecialComponentList.specialPackages) {
                    WebViewFactory.sDataDirectorySuffix?.set("_multi_${VUserHandle.getVUid()}")
                    WebViewFactory.sDataDirectorySuffix?.get()
                }

                instrumentationBefore?.callApplicationOnCreate(this.mInitialApplication!!)

                // GMS persistent process: delayed follow-up
                if (!this.j &&
                    "com.google.android.gms" == pkg &&
                    "com.google.android.gms.persistent" == effectiveProcessName
                ) {
                    this.j = true
                    if (q.isNotEmpty()) {
                        this.mainHandler.postDelayed(
                            GmsCheckinRetryTask(this, this.mInitialApplication!!, 0), q[0],
                        )
                    }
                }

                InvocationStubManager.instance.checkEnv(HCallbackStub::class.java)
                if (isSpecialPkg) {
                    InvocationStubManager.instance.checkEnv(AppInstrumentation::class.java)
                }

                // Framework may have replaced mInitialApplication during provider installs — re-read.
                ActivityThread.mInitialApplication?.get(mainThread)?.let { finalApp ->
                    this.mInitialApplication = finalApp
                    if (sdk in 26..28) {
                        val autofillManager =
                            finalApp.getSystemService(android.view.autofill.AutofillManager::class.java) as android.view.autofill.AutofillManager?
                        autofillManager?.let { autofill ->
                            try {
                                autofill.disableAutofillServices()
                                autofill.cancel()
                            } catch (th: Throwable) {
                                th.toString()
                            }
                            try {
                                val field = autofill.javaClass?.getDeclaredField("mService")
                                field?.isAccessible = true
                                field?.set(autofill, null)
                            } catch (th: Throwable) {
                                th.toString()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                if (!instrumentationBefore!!.instrument.onException(this.mInitialApplication, e)) {
                    val appName = boundApk?.appInfo?.name
                    Log.d("hu8", e.message.toString())
                    throw RuntimeException("Unable to create application $appName: $e", e)
                }
            }
        } catch (th: Throwable) {
            throw RuntimeException(
                "Unable to makeApplication ${boundApk?.appInfo?.packageName}", th,
            )
        }

        // ===== Disarm watchdog, notify AMS that app setup is done =====
        Log.d("WatchDog", "feed 0")
        watchdog.handler.removeMessages(0)
        try {
            VActivityManager.instance.getService().appDoneExecuting()
        } catch (_: RemoteException) {
        }
        Process.myPid()
        Process.myUid()
        VirtualCore.gCore.getListener()
    }

    /**
     * Was `tweakGoogleAppPrefs`. Forces conservative defaults on Play Store and GMS
     * SharedPreferences so clone apps don't:
     *   - auto-update (Play Store)
     *   - auto-notify about updates (Play Store)
     *   - report telemetry to Google (GMS)
     *   - run location services outside VA's virtual provider (GMS)
     *   - run the GMS Cast client or mediastore indexer (GMS)
     *
     * Called once from [VClientImpl.bindApplication] right after the clone's
     * `Application` is created.
     *
     * Each branch is independently try/catch'd — a failure on one branch doesn't
     * prevent the other from running. `VLog.c("hu8", ...)` is the common failure
     * path (SharedPreferences.edit can throw on corrupt prefs files, disk full,
     * permission denied, etc.).
     */
    fun tweakGoogleAppPrefs(context: Context, packageName: String) {
        if ("com.android.vending" == packageName) {
            try {
                context.getSharedPreferences("vending_preferences", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("notify_updates", false)
                    .putBoolean("notify_updates_completion", false)
                    .apply()
                context.getSharedPreferences("finsky", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("auto_update_enabled", false)
                    .apply()
            } catch (th: Throwable) {
                Log.d("hu8", th.toString())
            }
        }

        if ("com.google.android.gms" == packageName) {
            try {
                if (!locationProviderReady()) {
                    context.getSharedPreferences("nlp-prefs", Context.MODE_PRIVATE)
                        .edit()
                        .putInt("previous-location-mode", 0)
                        .apply()
                    context.getSharedPreferences("ULR_PERSISTENT_PREFS", Context.MODE_PRIVATE)
                        .edit {
                            putInt("location_enabled_key", 0)
                                .putInt("Location_Ble_scan_enabled_key", 0)
                        }
                }
                context.getSharedPreferences("googlecast-setting-prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("googlecast-isEnabled", false)
                    .apply()
                context.getSharedPreferences("gms_chimera_phenotype_flags", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("enable_usage_reporting", false)
                    .putBoolean(
                        "CommonAnalytics__system_health_log_post_chimera_update_enabled",
                        false,
                    )
                    .putBoolean(
                        "CommonAnalytics__system_health_log_post_gservice_update_enabled",
                        false,
                    )
                    .putBoolean(
                        "CommonAnalytics__system_health_log_post_gms_core_update_enabled",
                        false,
                    )
                    .putBoolean("ClientLogging__enable_client_logging", false)
                    .apply()
                context.getSharedPreferences("MediastoreIndexerSharedPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("id_based_mediastore_indexing_enabled", false)
                    .putBoolean("non_media_files_indexing_enabled", false)
                    .putBoolean("audio_media_files_indexing_enabled", false)
                    .apply()
            } catch (th: Throwable) {
                Log.d("hu8", th.toString())
            }
        }
    }

    @Throws(RemoteException::class)
    fun getLocalApplicationThread(): IInterface {
        val appThread = getAppThread()
        val applicationThread: IInterface? =
            if (BuildCompat.isAtLeastOreo()) IApplicationThreadOreo.Stub.asInterface?.call(appThread)
            else ApplicationThreadNative.asInterface?.call(appThread)
        if (applicationThread != null) {
            return applicationThread
        }
        throw RemoteException("Unable to resolve local ApplicationThread")
    }

    override fun scheduleCreateService(
        token: IBinder?,
        info: ServiceInfo?,
        processState: Int
    ) {
        VActivityManager.instance.trackToken(token)
        IApplicationThreadKitkatMirror.scheduleCreateService?.call(
            getLocalApplicationThread(),
            token,
            info,
            CompatibilityInfoMirror.DEFAULT_COMPATIBILITY_INFO?.get(),
            processState
        )
    }

    override fun scheduleServiceArgs(
        token: IBinder?,
        taskRemoved: Boolean,
        startId: Int,
        flags: Int,
        args: Intent?
    ) {
        VActivityManager.instance.trackToken(token)
        IApplicationThreadCompatMirror.scheduleService(
            getLocalApplicationThread(),
            token,
            taskRemoved,
            startId,
            flags,
            args
        )
    }

    override fun scheduleBindService(
        token: IBinder?,
        binder: IBinder?,
        intent: Intent?,
        rebind: Boolean,
        processState: Int
    ) {
        VActivityManager.instance.trackToken(token)
        IApplicationThreadCompatMirror.scheduleBindService(
            getLocalApplicationThread(),
            token,
            binder,
            intent,
            rebind,
            processState
        )
    }
    /**
     * Was `scheduleUnbindService(IBinder, IBinder, Intent)`. Two-signature dispatch
     * for `IApplicationThread.scheduleUnbindService`:
     *
     *  1. Try the QPR/vendor variant first — 3 args: `(conn, token, intent)`.
     *  2. Fall back to AOSP's 2-arg version — `(conn, intent)` — on older platforms
     *     where the QPR mirror doesn't load or doesn't have the method.
     *  3. If neither exists, throw.
     *
     * The extra `IBinder` in the 3-arg variant is the service's `token` (the
     * `Service`'s `onBind` result), which QPR split out as a separate argument.
     */
    @Throws(RemoteException::class)
    override fun scheduleUnbindService(
        conn: IBinder?,
        binder: IBinder?,
        intent: Intent?
    ) {
        VActivityManager.instance.trackToken(conn)

        val appThread = getLocalApplicationThread()

        IApplicationThreadQprMirror.scheduleUnbindService?.let { qpr ->
            qpr.call(appThread, conn, token, intent)
            return
        }

        val aosp = IApplicationThread.scheduleUnbindService
            ?: throw RemoteException("No compatible scheduleUnbindService method")
        aosp.call(appThread, conn, intent)
    }

    override fun scheduleStopService(token: IBinder?) {
        VActivityManager.instance.trackToken(token)
        IApplicationThread.scheduleStopService?.call(getLocalApplicationThread(), token)
    }

    override fun updateActivityManager(binder: IBinder?) {
        if (!VActivityManager.instance.setService(binder)) {
            throw RemoteException("Unable to install virtual ActivityManager")
        }
    }

    fun initProcess(binder: IBinder?, vuid: Int, vpid: Int): Boolean {
        synchronized(bindLock) {
            if (this.token == null) {
                this.token = binder
                this.vuid = vuid
                this.vpid = vpid
                DaemonService.ensureStarted(appContext)
                return true
            }
            if (this.vuid == vuid && this.vpid == vpid) {
                this.token = binder
                DaemonService.ensureStarted(appContext)
                return true
            }
            Log.e("hu8", "initProcess: reject occupied process, current vuid/vpid=${this.vuid}/${this.vpid}, requested=$vuid/$vpid")
            return false
        }
    }


    //================================================================
    /** was hu8.b (LoadedApkMirror) */
    inner class LoadedApk {
        /** was get */
        @JvmField
        var processName: String? = null

        /** was b */
        @JvmField
        var appInfo: ApplicationInfo? = null

        /** was c */
        @JvmField
        var providerInfoList: List<ProviderInfo>? = null

        /** was d: the framework's LoadedApkMirror object */
        @JvmField
        var loadedApk: Any? = null
    }

    class ReceiverData {
        /* JADX INFO: renamed from: ContentProviderHolder */
        var resultData: PendingResultData? = null

        /* JADX INFO: renamed from: b */
        var intent: Intent? = null

        /* JADX INFO: renamed from: c */
        var name: ComponentName? = null
        var processName: String? = null
    }
    class NewIntentData {
        /* JADX INFO: renamed from: ContentProviderHolder */
        var pkg: String? = null

        /* JADX INFO: renamed from: b */
        var binder: IBinder? = null

        /* JADX INFO: renamed from: c */
        var intent: Intent? = null
    }
    class PendingResultExpiry(
        private val client: VClientImpl,
        private val token: IBinder,
        private val data: PendingResultData
    ) : Runnable {

        override fun run() {
            val map = client.pendingResults
            if (map[token] === data) {
                map.remove(token)
            }
        }
    }

    inner class MainHandler : Handler(Looper.getMainLooper()) {

        val vClientImpl: VClientImpl = this@VClientImpl

        override fun handleMessage(message: Message) {
            val what = message.what

            if (what == 11) {
                val data = message.obj as NewIntentData
                val intent = ReferrerIntentMirror.ctor?.newInstance(data.intent, data.pkg) as Intent
                val mainThread = VirtualCore.gCore.mainThread
                val list = Collections.singletonList(intent)

                if (ActivityThread.performNewIntents != null) {
                    ActivityThread.performNewIntents?.call(mainThread, data.binder, list)
                    return
                }
                if (BuildCompat.sdk31OrLater() && ActivityThread.mActivities != null) {
                    val record = ActivityThread.mActivities?.get(mainThread)?.get(data.binder)
                    if (record != null) {
                        ActivityThread.handleNewIntent(record, list)
                        return
                    }
                }
                if (BuildCompat.sdk29OrLater() &&
                    ActivityThread.mActivities != null &&
                    ActivityThread.deliverNewIntents != null
                ) {
                    val record = ActivityThread.mActivities?.get(mainThread)?.get(data.binder)
                    if (record != null) {
                        ActivityThread.deliverNewIntents?.call(mainThread, record, list)
                        Objects.toString(intent)
                    } else {
                        Log.e("hu8", "error deliverNewIntents $intent")
                    }
                    return
                }
                ActivityThreadNMR1Mirror.performNewIntents?.call(mainThread, data.binder, list, java.lang.Boolean.TRUE)
                return
            }

            if (what != 12) return

            val data = message.obj as ReceiverData
            Objects.toString(data.intent)
            Objects.toString(data.name)

            var finished = false         // v7
            var tokenRegistered = false  // v11
            var instantiated = false

            try {
                if (!isBound()) {
                    bindApplication(data.name?.packageName, data.processName)
                }
                val baseContext = mInitialApplication?.baseContext
                val receiverContext =  ContextImpl.getReceiverRestrictedContext?.call(baseContext, *emptyArray<Any>()) as Context
                val receiver = baseContext?.classLoader?.loadClass(data.name?.className)?.getDeclaredConstructor()?.newInstance() as android.content.BroadcastReceiver
                instantiated = true

                var pendingResult: PendingResult? = null // v12
                val rd = data.resultData
                if (rd != null) {
                    try {
                        rd.type = 0
                        val token = rd.token
                        if (token != null) {
                            pendingResults[token] = rd
                            tokens[token] = true//java.lang.Boolean.TRUE
                            vClientImpl.mainHandler.postDelayed(PendingResultExpiry(this@VClientImpl, token, rd), 12000L)
                            tokenRegistered = true
                        }
                        pendingResult = data.resultData?.toPendingResult()
                    } catch (t: Throwable) {
                        Log.d("hu8", t.toString())
                        pendingResult = null
                    }
                }

                if (receiver !is AppWidgetProvider)
                {
                    if (pendingResult != null) {
                        BroadcastReceiver.setPendingResult?.call(receiver, pendingResult)
                    }
                    data.intent?.setExtrasClassLoader(baseContext.classLoader)
                    if (data.intent?.component == null) {
                        data.intent?.component = data.name
                    }
                    receiver.onReceive(receiverContext, data.intent)
                }

                val current = BroadcastReceiver.getPendingResult?.call(receiver, *emptyArray<Any>()) as PendingResult?
                if (current != null)
                {
                    current.finish()
                    finished = true
                } else if (pendingResult != null && receiver !is AppWidgetProvider)
                {
                    finished = true
                }

                val r = data.resultData
                if (r != null && !finished && !deliverPendingResult(r) && !tokenRegistered) {
                    VActivityManager.instance.broadcastFinish(r)
                }
            } catch (t: Throwable) {
                if (instantiated) finished = tokenRegistered
                try {
                    val msg = "Unable to start receiver ${data.name}: $t"
                    val ex = RuntimeException(msg, t)
                    Log.d("hu8", msg)
                    Log.d("hu8", Log.getStackTraceString(ex))
                } finally {
                    val r = data.resultData
                    if (r != null && !deliverPendingResult(r) && !finished) {
                        VActivityManager.instance.broadcastFinish(r)
                    }
                }
            }
        }
    }

    /**
     * Was `io.z19`. A watchdog timer that kills the current process if the 75 s
     * `bindApplication` deadline isn't met. The owning `VClientImpl` arms the timer
     * before `bindApplicationLocked` starts and disarms it on successful completion;
     * if the timer fires, [WatchDogHandler] logs and kills the process.
     *
     * The class is intentionally tiny — its whole job is to convert ContentProviderHolder hang into ContentProviderHolder
     * crash so the framework's instrumentation restarts the app cleanly instead of
     * leaving ContentProviderHolder half-bound process.
     */
    class WatchDog {

        /**
         * Was `a`. The worker-thread handler. `@JvmField` preserves the original
         * public field name so `VClientImpl` (and any other Java-side caller) can
         * read `watchdog.handler` directly after the field rename.
         */
        @JvmField
        val handler: Handler

        init {
            val thread = HandlerThread("WatchDog")
            thread.start()
            handler = WatchDogHandler(thread.looper)
        }

        /**
         * Was inner class `ContentProviderHolder`. Fires when the 75 s deadline expires — logs and
         * kills the current process. `inner` preserves the original bytecode shape
         * (non-static inner class with ContentProviderHolder synthetic `this$0` outer reference).
         */
        class WatchDogHandler(looper: Looper) : Handler(looper)
        {

            override fun handleMessage(msg: Message)
            {
                super.handleMessage(msg)
                Log.d("WatchDog", "TimeOut watching: ${msg.what}")

                // Was `Handler handler = VirtualRuntime.ContentProviderHolder;` — read, never used.
                // Likely ContentProviderHolder stripped log call (e.g. logging the runtime handler state).
                // Preserved as ContentProviderHolder no-op expression statement for fidelity.
                val uiHandler = VirtualRuntime.uiHandler

                // Was `String str = VirtualCore.gCore.currentProcessName;` — read,
                // never used. Same stripped-log pattern.
                val currentProcessName = VirtualCore.gCore.currentProcessName

                Process.killProcess(Process.myPid())
            }
        }
    }
    /**
     * Was `public static class VARootThreadGroup extends ThreadGroup`. The "fake
     * root" ThreadGroup installed by [installRootThreadGroupHandler]. Its entire
     * purpose is this [uncaughtException] override — it filters out ContentProviderHolder known benign
     * GMS crash (schema mismatch during PeopleModuleInitIntentOperation) and
     * otherwise forwards to [CrashHandler] / process death.
     */
    class VARootThreadGroup(group: ThreadGroup?, name: String) : ThreadGroup(group, name) {

        /**
         * Was `uncaughtException(Thread, Throwable)`. Called by the JVM when ContentProviderHolder
         * thread in this group dies with an unhandled exception.
         *
         * Filter logic (preserved verbatim):
         *   1. Only fires if: GMS is enabled, current pkg is `com.google.android.gms`,
         *      and the failing thread is GmsIntentOperationChimeraService.
         *   2. Walks up to 4 links of the exception cause chain.
         *   3. If any link is an `IllegalArgumentException` whose message contains
         *      "SetSchemaRequest contains ContentProviderHolder schema named" AND
         *      "does not match the generated schema"...
         *   4. ...and the stack trace includes `PeopleModuleInitIntentOperation`...
         *   5. ...swallow silently (return, no crash).
         *
         * Otherwise, delegate to the crash handler (or exit the process).
         */
        override fun uncaughtException(thread: Thread?, th: Throwable?) {
            val currentPackage = sInstance.getCurrentPackage()

            // Was `HashSet<String> hashSet = GmsSupport.GMS_PACKAGES;` — assigned
            // but never read. Stripped membership check. Preserved as ContentProviderHolder bare
            // expression to match the bytecode's `sget-object` (potential class
            // init of GmsSupport). Safe to delete if you want cleaner code.
            GmsSupport.GMS_PACKAGES

            if (VASettings.gmsState &&
                "com.google.android.gms" == currentPackage &&
                thread != null &&
                th != null &&
                thread.name != null &&
                thread.name!!.contains("GmsIntentOperationChimeraService")
            ) {
                var cause: Throwable? = th
                var depth = 0
                while (cause != null && depth < 4) {
                    val message = cause.message
                    if (cause is IllegalArgumentException &&
                        message != null &&
                        message.contains("SetSchemaRequest contains ContentProviderHolder schema named") &&
                        message.contains("does not match the generated schema")
                    ) {
                        for (element in cause.stackTrace) {
                            if (element != null &&
                                "com.google.android.gms.people.api.init.PeopleModuleInitIntentOperation" ==
                                element.className
                            ) {
                                // Known benign GMS crash — swallow it.
                                return
                            }
                        }
                    }
                    cause = cause.cause
                    depth++
                }
            }

            sInstance.crashHandler?.handle(thread, th) ?: run()
            {
                Log.d("uncaught", th?.message.toString())
                exitProcess(0)
            }
        }
    }

    inner class BindOnMainThreadRunnable(
        private val packageName: String?,
        private val processName: String?,
    ) : Runnable {

        override fun run() {
            VClientImpl.p

            val client = this@VClientImpl
            try {
                synchronized(client.bindLock) {
                    if (!client.isBound()) {
                        client.bindApplicationLocked(packageName, processName)
                    }
                }
            } finally {
                // Whether the bind succeeded, threw, or was skipped — always unblock
                // any waiting threads so they don't deadlock.
                val condition = client.bindCondition
                client.bindCondition = null
                condition?.open()
            }
        }
    }
}

package com.ims134.doclone.clone.server


import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.ConditionVariable
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.IInterface
import android.os.Message
import android.os.Parcel
import android.os.Process
import android.os.RemoteException
import android.os.SystemClock
import android.os.TransactionTooLargeException
import android.util.Log
import android.util.SparseArray
import androidx.collection.ArrayMap
import androidx.core.util.size
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.AppTaskInfo
import com.ims134.doclone.clone.BadgerInfo
import com.ims134.doclone.clone.BroadcastIntentData
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.CloneSupportAgent
import com.ims134.doclone.clone.GmsSupport
import com.ims134.doclone.clone.IActivityManager
import android.app.IServiceConnection
import android.app.IStopUserCallback
import com.ims134.doclone.clone.PendingIntentData
import com.ims134.doclone.clone.PendingResultData
import com.ims134.doclone.clone.ProcessMap
import com.ims134.doclone.clone.ProviderCall
import com.ims134.doclone.clone.VBinder
import com.ims134.doclone.clone.VParceledListSlice
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.VUserHandle.Companion.makeUid
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.clone.VirtualRuntime
import com.ims134.doclone.clone.server.StartProcessMonitor.ProcessKey
import com.ims134.doclone.io.ApplicationThreadNative
import com.ims134.doclone.io.BinderDelegateService
import com.ims134.doclone.io.CompatibilityInfoMirror
import com.ims134.doclone.io.ComponentUtils
import com.ims134.doclone.io.IApplicationThread
import com.ims134.doclone.io.IApplicationThreadCompatMirror
import com.ims134.doclone.io.IApplicationThreadKitkatMirror
import com.ims134.doclone.io.IApplicationThreadOreo
import com.ims134.doclone.io.IApplicationThreadQprMirror
import com.ims134.doclone.io.IProcessObserver
import com.ims134.doclone.io.IServiceConnectionCompat
import com.ims134.doclone.io.IVClient
import com.ims134.doclone.io.NativeLibraryHelperCompat
import com.ims134.doclone.io.PackageCacheManager
import com.ims134.doclone.io.PendingIntents
import com.ims134.doclone.io.PermissionCompat
import com.ims134.doclone.io.SpecialComponentList
import com.ims134.doclone.io.VASettings
import com.ims134.doclone.io.VNotificationManager
import com.ims134.doclone.io.VNotificationManagerService
import java.util.concurrent.atomic.AtomicReference


open class VActivityManagerService(): IActivityManager.Stub()
{
    val mPidsSelfLocked: SparseArray<ProcessRecord?> = SparseArray<ProcessRecord?>()


    val mActivityStack: ActivityStack = ActivityStack(this)


    val mServices: HashSet<ServiceRecord?> = HashSet<ServiceRecord?>()


    val mGmsWarmupTokens = ArrayMap<Int, Any>()   // smali d, an io.lt (ArrayMap subclass)
    val nmGmsWarmedUsers = HashSet<Int>()         // smali e


    val mStartingVpids = HashSet<Int>()


    val mServiceConnections: ArrayMap<IBinder?, ArrayList<ConnectionRecord?>?> = ArrayMap()



    val mPendingIntents = PendingIntents()


    val activityManager: ActivityManager? = null


    val notificationManager: NotificationManager? = null


    lateinit var mProcessStartLocks: StartProcessMonitor


    var mWorkerHandler: Handler? = null

    /* JADX INFO: renamed from: n */
    var mLastStubIndex: Int = 0


    companion object
    {

        val instance = AtomicReference<VActivityManagerService>()

        val TAG: String = VActivityManagerService::class.java.getSimpleName()

        const val GMS_PACKAGE = "com.google.android.gms"
        const val CHECKIN_RECEIVER =
            "com.google.android.gms.checkin.CheckinServiceImposeReceiver"
        const val ACTION_CHECKIN_NOW = "android.server.checkin.CHECKIN_NOW"


        fun get(): VActivityManagerService {
            return instance.get()
        }
        /**
         * Parses `"<hostPackage>:p<N>"` → `N`. Returns `-1` for anything else.
         *
         * Stub process names are `<hostPackage>:p0`..`:p99`. Only the parse is
         * done here — range validation (`0 <= vpid < 100`) is the caller's job
         * (see `getAliveVpids`).
         */
        @JvmStatic
        fun parseVPid(processName: String?): Int
        {
            val prefix = "${VirtualCore.gCore.hostPackageName}:p"
            if (processName == null || !processName.startsWith(prefix)) return -1
            return try {
                processName.substring(prefix.length).toInt()
            } catch (_: NumberFormatException) {
                -1
            }
        }

        // smali m.appendFirstIfNotNull(m, I, Object): static helper on the outer class
        @JvmStatic
        fun isWarmupTokenCurrent(service: VActivityManagerService, userId: Int, token: Any?): Boolean {
            synchronized(service.mGmsWarmupTokens) {
                return service.mGmsWarmupTokens[userId] === token
            }
        }

        @JvmStatic
        fun systemReady(context: Context)
        {
            VActivityManagerService().onCreate(context)
        }
        /**
         * Was `resolveServiceInfo(int, Intent)`. Looks up a [ServiceInfo] for the
         * given intent under the given virtual user. Returns null if the intent is
         * null or if no matching service is installed.
         *
         * `@JvmStatic` preserves the static entry point — `bindService` (and any
         * other instance method on the AMS) calls it unqualified via Kotlin's
         * implicit companion resolution.
         */
        @JvmStatic
        fun resolveServiceInfo(userId: Int, intent: Intent?): ServiceInfo? {
            if (intent == null) return null
            return VirtualCore.resolveServiceInfo(userId, intent)
        }

        /**
         * Was `requireVClient(ProcessRecord)`. Returns the process's [IVClient] proxy,
         * throwing [RemoteException] if the process (or its client) is null.
         *
         * Used by the SDK-36+ service-dispatch paths in [startServiceLocked] and
         * [bindService] as the "modern" alternative to `requireAppThread(...)`. The
         * caller catches `RemoteException` inline, so a dead/missing process is
         * treated the same as a `scheduleCreateService` / `scheduleServiceArgs` IPC
         * failure — the dispatch is silently skipped.
         */
        @JvmStatic
        @Throws(RemoteException::class)
        fun requireVClient(process: ProcessRecord?): IVClient {
            val client = process?.client
                ?: throw RemoteException("Target process has no IVClient for service dispatch")
            return client
        }
        /**
         * Was `requireAppThread(ProcessRecord)`. Returns the process's `IApplicationThread`
         * proxy, throwing [RemoteException] if the process (or its app-thread binder) is null.
         *
         * The pre-SDK-36 counterpart to [requireVClient]. Used by service and provider
         * dispatch paths where the target framework still exposes the raw
         * `IApplicationThread` (`scheduleCreateService`, `scheduleService`,
         * `scheduleUnbindService`, `scheduleStopService`, …).
         *
         * The caller catches `RemoteException` inline and treats it the same as an IPC
         * failure — dispatch is silently skipped rather than crashing the AMS.
         */
        @JvmStatic
        @Throws(RemoteException::class)
        fun requireAppThread(process: ProcessRecord?): IInterface {
            val appThread = process?.appThread
                ?: throw RemoteException("Target process has no ApplicationThread")
            return appThread
        }

        /**
         * Was `requestServiceBind(ProcessRecord, ServiceRecord, IntentBindRecord, Intent, boolean)`.
         * Dispatches a bind request to the target process's `IApplicationThread`.
         *
         * Three cases:
         *  - **GMS chimera**: some chimera services bind themselves internally — no
         *    client-side dispatch is needed. The original logged this case (stripped).
         *  - **SDK 36+**: route through `IVClient.scheduleBindService(...)` — the
         *    newer API path that takes the richer `ServiceRecord` / `IntentBindRecord`
         *    objects directly.
         *  - **Pre-36**: use the compat mirror to reach the framework's
         *    `IApplicationThread.scheduleBindService(...)` overload for the current
         *    API level.
         *
         * [doRebind] is the `isRebind` flag passed through to the framework — true when
         * rebinding to an already-started service, false for the initial bind.
         */
        @JvmStatic
        @Throws(RemoteException::class)
        fun requestServiceBind(
            process: ProcessRecord?,
            serviceRecord: ServiceRecord,
            intentBindRecord: ServiceRecord.IntentBindRecord,
            intent: Intent,
            doRebind: Boolean,
        ) {
            when {
                GmsSupport.isChimeraBoundServiceStart(intent, serviceRecord.serviceInfo) -> {
                    // Chimera handles its own binding internally — nothing to dispatch.
                    // Was `Objects.toString(intent);` — a stripped log.
                    intent.toString()
                }
                Build.VERSION.SDK_INT >= 36 -> {
                    requireVClient(process).scheduleBindService(
                        serviceRecord, intentBindRecord, intent, doRebind, 0,
                    )
                }
                else -> {
                    IApplicationThreadCompatMirror.scheduleBindService(
                        requireAppThread(process),
                        serviceRecord,
                        intentBindRecord,
                        intent,
                        doRebind,
                        0,
                    )
                }
            }
        }

        @JvmStatic
        fun connectionConnected(
            connection: IServiceConnection,
            name: ComponentName,
            intentBindRecord: ServiceRecord.IntentBindRecord,
        ) {
            try {
                val binder = intentBindRecord.binder
                if (binder == null || !binder.isBinderAlive) {
                    Log.d(TAG, "binder not available")
                } else {
                    IServiceConnectionCompat.connected(
                        connection,
                        name,
                        BinderDelegateService(name, binder),
                        null,
                        false,
                    )
                }
            } catch (_: RemoteException) {
            }
        }
    }

    val mProcessNames: ProcessMap<ProcessRecord> = ProcessMap()

    fun findProcessLocked(processName: String, uid: Int): ProcessRecord? {
        return this.mProcessNames.get(uid, processName)
    }
    fun findProcessLocked(pid: Int): ProcessRecord? {
        return this.mPidsSelfLocked.get(pid)
    }


    @JvmName("V")
    fun getAliveVpids(): HashSet<Int> {
        val aliveVpids = HashSet<Int>()
        try {
            val runningProcesses = activityManager?.runningAppProcesses ?: return aliveVpids
            for (info in runningProcesses) {
                val vpid = parseVPid(info.processName)
                if (vpid in 0 until 100) {           // was two checks + dead `z`
                    aliveVpids.add(vpid)
                }
            }
        } catch (_: Throwable) {
            // original: th.getMessage() (dead); return whatever we collected so far
        }
        return aliveVpids
    }

    fun onProcessDied(processRecord: ProcessRecord) {
        synchronized(mServices)
        {
            val it = mServices.iterator()
            while (it.hasNext()) {
                val servicePid = it.next()?.process?.Pid
                if (servicePid != null && servicePid == processRecord.Pid) {
                    it.remove()
                }
            }
            mActivityStack.removeProcess(processRecord)
        }
        mWorkerHandler?.postDelayed(RemovePackageReceiversTask(processRecord), 10_000L)
    }

    fun startProcessLocked(
        applicationInfo: ApplicationInfo,
        processName: String,
        vuid: Int,
        vpid: Int,
    ): ProcessRecord? {
        val pendingRecord = ProcessRecord(applicationInfo, processName, vuid, vpid)
        val bundle = Bundle()
        bundle.putBinder("_VA_|_binder_", pendingRecord)
        bundle.putInt("_VA_|_vuid_", vuid)
        bundle.putInt("_VA_|_vpid_", vpid)
        bundle.putString("_VA_|_process_", processName)
        bundle.putString("_VA_|_pkg_", applicationInfo.packageName)

        val initResult = ProviderCall.callWithRetry(
            VASettings.setStubNumber(vpid),
            appContext,
            "_VA_|_init_process_",
            bundle
        ) ?: return null

        val pid = initResult.getInt("_VA_|_pid_")
        val binder = initResult.getBinder("_VA_|_client_")
        if (pid <= 0 || binder == null) {
            Log.d(TAG, "Invalid process attach result for ${applicationInfo.packageName}, pid=$pid, client=$binder")
            if (pid <= 0) return null
            Process.killProcess(pid)
            return null
        }

        var attachedRecord: ProcessRecord? = null

        // Every failure path leaves attachedRecord == null; only full success sets it.
        run attach@{
            val client = IVClient.Stub.asInterface(binder)
            if (client == null) {
                Process.killProcess(pid)
                return@attach
            }

            val iInterfaceCall: IInterface? = try {
                val appThread = client.getAppThread()
                (if (BuildCompat.isAtLeastOreo()) {
                    IApplicationThreadOreo.Stub.asInterface?.call(appThread)
                } else {
                    ApplicationThreadNative.asInterface?.call(appThread)
                }) as IInterface?
            } catch (e: RemoteException) {
                null
            }
            if (iInterfaceCall == null) {
                Process.killProcess(pid)
                return@attach
            }

            val token: ProcessRecord? = try {
                client.getToken() as? ProcessRecord
            } catch (e: RemoteException) {
                null
            }
            if (token == null) {
                Process.killProcess(pid)
                return@attach
            }

            if (token !== pendingRecord) {
                Log.d(TAG, "Reject mismatched process token for pid=$pid expected=$pendingRecord actual=$token")
                Process.killProcess(pid)
                return@attach
            }

            VAppManagerService.get()?.let {
                if (it.isPackageStopped(token.userId, token.appInfo.packageName)) {
                    token.attachCondition.open()
                    Process.killProcess(pid)
                    return@attach
                }
            }

            token.client = client
            token.appThread = iInterfaceCall
            token.Pid = pid

            val processDeathRecipient = ProcessDeathRecipient(this, binder, token)
            try {
                binder.linkToDeath(processDeathRecipient, 0)
            } catch (e: RemoteException) {
                Log.d(TAG, "linkToDeath failed for " + token.processName + " vpid=" + token.vpid +
                        ", treat as dead to avoid vpid leak: " + e.message)
                onProcessDied(token)
                token.attachCondition.open()
                return@attach
            }

            synchronized(mProcessNames) {
                val cache = mProcessNames.cache
                var sparseArray = cache[token.processName]
                if (sparseArray == null) {
                    sparseArray = SparseArray(2)
                    cache[token.processName] = sparseArray
                }
                sparseArray.put(token.vuid, token)
            }
            synchronized(mPidsSelfLocked) {
                mPidsSelfLocked.put(token.Pid, token)
            }

            try {
                client.updateActivityManager(asBinder())
                Log.d(TAG, "attachClient for " + token.processName + " pid: " + token.Pid +
                        " activityManagerHandedOff=true")
                attachedRecord = token
            } catch (e: Exception) { // RemoteException | RuntimeException
                Log.d(TAG, "Unable to hand off ActivityManager to ${token.processName} vpid=${token.vpid} pid=${token.Pid}: $e")

                synchronized(mProcessNames) {
                    if (mProcessNames.get(token.vuid, token.processName!!) === token)
                    {
                        mProcessNames.remove(token.vuid, token.processName)
                    }
                }
                synchronized(mPidsSelfLocked) {
                    if (mPidsSelfLocked.get(token.Pid) === token) {
                        mPidsSelfLocked.delete(token.Pid)
                    }
                }
                try {
                    binder.unlinkToDeath(processDeathRecipient, 0)
                } catch (_: Throwable) {
                }
                token.attachCondition.open()
                Process.killProcess(pid)
                // attachedRecord stays null
            }
        }

        // Final gate
        val record = attachedRecord
        val iVClient = record?.client
        if (record == null || iVClient == null || !iVClient.asBinder().isBinderAlive) {
            Log.d(TAG, "Unable to attach virtual process $processName vpid=$vpid pid=$pid")
            return null
        }

        // Register receivers (smali calls getClass() first, so get null BroadcastSystem throws NPE)
        val broadcastSystem = BroadcastSystem.broadcastSystem!!
        val vPackage = PackageCacheManager.getPackage(applicationInfo.packageName)
        if (vPackage != null) {
            broadcastSystem.registerReceivers(vPackage)
        } else {
            Log.d("d", "not find package settings")
        }
        return record
    }


    fun removeRunningProcess(
        processName: String,
        uid: Int,
        appInfo: ApplicationInfo,
    ): ProcessRecord? {
        for (vpid in getAliveVpids()) {
            // ── Phase 1: quick check inside the lock ──
            val alreadyTracked = synchronized(mPidsSelfLocked) {
                if (mStartingVpids.contains(vpid)) {
                    true
                } else {
                    var size = mPidsSelfLocked.size - 1
                    var found = false
                    while (size >= 0) {
                        val proc = mPidsSelfLocked.valueAt(size)
                        if (proc != null && proc.vpid == vpid) {
                            found = true
                            break
                        }
                        size--
                    }
                    found
                }
            }
            if (alreadyTracked) continue

            // ── Phase 2: probe the stub (outside lock) ──
            val bundle = try {
                ProviderCall.callWithRetry(
                    VASettings.setStubNumber(vpid),
                    appContext,
                    "_VA_|_query_process_",
                    null,
                )
            } catch (_: Throwable) {
                continue                              // recoverable — try next vpid
            } ?: continue

            // ── Phase 3: validate the response ──
            if (bundle.getInt("_VA_|_vpid_", -1) != vpid) continue
            if (bundle.getInt("_VA_|_vuid_", -1) != uid) continue
            if (processName != bundle.getString("_VA_|_process_")) continue
            val pkg = bundle.getString("_VA_|_pkg_") ?: continue
            if (pkg != appInfo.packageName) continue

            // ── Phase 4: reconstruct ProcessRecord ──
            val proc = startProcessLocked(appInfo, processName, uid, vpid) ?: continue
            Log.d(
                TAG,
                "Recovered running process $processName from stub p$vpid pid=${proc.Pid}",
            )
            return proc
        }
        return null
    }



    fun startProcessIfNeeded(
        processName: String,
        userId: Int,
        packageName: String,
        z: Boolean,
    ): ProcessRecord? {
        if (VAppManagerService.get() == null || VAppManagerService.get()!!.isPackageStopped(userId, packageName)) return null

        val freeStubCount = get().getFreeStubCount()
        if (!z && freeStubCount <= 3) return null
        if (z && freeStubCount < 3) killAllApps()          // then continues, no re-check

        val packageSetting = PackageCacheManager.getPackageSetting(packageName)
        val applicationInfo = VPackageManagerService.get().getApplicationInfo(packageName, 0, userId)
        if (packageSetting == null || applicationInfo == null) return null

        if (!packageSetting.readUserState(userId).launched) {
            mWorkerHandler?.postDelayed(FirstLaunchTask(packageSetting, userId), 1500L)
        }

        val vuid = VUserHandle.makeUid(userId, packageSetting.appId)

        // Fast path: already running
        val existing = synchronized(mProcessNames) {
            mProcessNames.get(vuid, processName) as ProcessRecord?
        }
        if (existing != null && existing.client != null && existing.client!!.asBinder().isBinderAlive) {
            return existing
        }

        Log.d(TAG, "startProcessIfNeed No process record found for : $processName")

        val key = synchronized(mProcessStartLocks!!) {
            mProcessStartLocks.getKey(vuid, processName)
        }

        synchronized(key) {
            mProcessStartLocks.startMonitor(vuid, processName)

            // Re-check under the per-process lock
            val again = synchronized(mProcessNames) {
                mProcessNames.get(vuid, processName) as ProcessRecord?
            }
            if (again != null && again.client != null && again.client!!.asBinder().isBinderAlive) {
                mProcessStartLocks.stopMonitor(vuid, processName)
                return again
            }

            val running = removeRunningProcess(processName, vuid, applicationInfo)
            if (running != null && running.client != null && running.client!!.asBinder().isBinderAlive) {
                mProcessStartLocks.stopMonitor(vuid, processName)
                running.packageList.add(applicationInfo.packageName)
                return running
            }

            val aliveVpids = getAliveVpids()
            var vpid = -1
            synchronized(mPidsSelfLocked) {
                var needQuery = true
                if (!z) {
                    var free = 0
                    for (i in 0 until 100) {                 // smali: loop while i < 100
                        if (!isStubInUse(i, aliveVpids)) free++
                    }
                    if (free <= 3) needQuery = false         // vpid stays -1
                }
                if (needQuery) {
                    vpid = queryFreeStubProcess(aliveVpids)
                    if (vpid != -1) mStartingVpids.add(vpid)
                }
            }

            if (vpid == -1) {
                mProcessStartLocks.stopMonitor(vpid.let { vuid }, processName)
                return null
            }

            val record = try {
                startProcessLocked(applicationInfo, processName, vuid, vpid)
            } finally {
                synchronized(mPidsSelfLocked) { mStartingVpids.remove(vpid) }
            }
            mProcessStartLocks.stopMonitor(vuid, processName)

            if (record != null) {
                record.packageList.add(applicationInfo.packageName)
                try {
                    ensureHostPermissions(record)
                    return record
                } catch (t: Throwable) {
                    Log.d(TAG, t.toString())
                }
            }
            return record
        }
    }

    fun ensureHostPermissions(processRecord: ProcessRecord) {
        val applicationInfo = processRecord.appInfo

        // Apps targeting API 23+ ask for permissions at runtime themselves.
        // Only legacy apps (targetSdk < 23) get their permissions at "install time", so the
        // host app must already hold them.
        if (applicationInfo.targetSdkVersion >= 23) return

        val guestDangerous = VPackageManagerService.get().getDangerousPermissions(applicationInfo.packageName)
        if (guestDangerous == null || guestDangerous.isEmpty()) return

        // Keep only the guest's dangerous permissions that the host manifest declares
        val hostPermissions = VirtualCore.gCore.getHostPermissions()
        val grantable = ArrayList<String>()
        for (perm in guestDangerous)
        {
            if(perm.isNullOrEmpty()) continue
            if (hostPermissions.contains(perm)) grantable.add(perm)
        }

        val toRequest = grantable.toTypedArray()
        if (toRequest.isEmpty()) return

        for (perm in toRequest) {
            val core = VirtualCore.gCore
            if (core.unhookPackageManager.checkPermission(perm, core.hostPackageName!!) !=
                PackageManager.PERMISSION_GRANTED) {
                // First missing permission: request the WHOLE list in one go, then stop looping
                val waiter = ConditionVariable()
                PermissionCompat.requestPermissions(appContext, toRequest, BlockingPermissionCallback(waiter))
                if (!waiter.block(30000L)) {
                    Log.d(TAG, "requestPermission: timeout after 30000ms pkg=" + applicationInfo.packageName)
                }
                return
            }
        }
    }
    fun isStubInUse(vpid: Int, aliveVpids: Set<Int>?): Boolean {
        // stub is being started right now
        if (mStartingVpids.contains(vpid)) return true

        // stub belongs to get process that is alive according to the provider query
        if (aliveVpids != null && aliveVpids.contains(vpid)) return true

        // stub is held by get registered process record
        for (index in mPidsSelfLocked.size - 1 downTo 0) {
            val record = mPidsSelfLocked.valueAt(index)          // yi7.i(index)
            if (record != null && record.vpid == vpid) return true
        }
        return false
    }
    fun queryFreeStubProcess(aliveVpids: HashSet<Int>?): Int {
        Log.d(TAG, "queryFreeStubProcessLocked mPidsSelfLocked.size=" +
                mPidsSelfLocked.size + " STUB_COUNT=100")

        // Pass 1: scan forward from the slot after the last one handed out
        var i = mLastStubIndex
        while (true) {
            i++
            if (i >= 100) break                         // STUB_COUNT
            if (!isStubInUse(i, aliveVpids)) {
                mLastStubIndex = i
                return i
            }
        }

        // Pass 2: wrap around and scan 0..mLastStubIndex (inclusive)
        var j = 0
        while (j < mLastStubIndex + 1) {
            if (!isStubInUse(j, aliveVpids)) {
                mLastStubIndex = j
                return j
            }
            j++
        }
        return -1                                       // every stub is busy
    }

    @Throws(RemoteException::class)
    fun handleStaticBroadcast(
        uid: Int,
        activityInfo: ActivityInfo,
        intent: Intent,
        pr: PendingResultData?,
        pr2: BroadcastReceiver.PendingResult?,
    ): Boolean {
        val userId = uid / 100000
        val me = VAppManagerService.get()

        // 0x20 = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
        if (me == null || (me.isPackageStopped(userId, activityInfo.packageName) &&
            (intent.flags and 0x20) == 0
        )) {
            return false
        }

        val originalAction = SpecialComponentList.toOriginalAction(intent.action)
        if (originalAction != null) intent.action = originalAction

        var record: ProcessRecord? = synchronized(mProcessNames) {
            findProcessLocked(activityInfo.processName, uid)
        }

        if (record == null && shouldStartMissingProcess(activityInfo.packageName, intent.action)) {
            record = if (userId != 0 && !VUserManagerService.get()!!.exists(userId))
            {
                Log.d(TAG, Log.getStackTraceString(Exception("userId = $userId")))
                null
            } else {
                startProcessIfNeeded(activityInfo.processName, userId, activityInfo.packageName, true)
            }
        }

        if (record?.appThread == null) {
            Log.d(TAG, activityInfo.packageName + " not schedule receiver for not started process: " + intent)
            return false
        }

        Log.d(TAG, "performReceive $intent")
        if ("android.intent.action.PACKAGE_ADDED" == intent.action && intent.extras == null) {
            intent.putExtra("FIX", "FIX")
            Log.d(TAG, "package added intent extra is null! ActivityInfo $activityInfo")
        }

        val client = record.client                       // read before the try, like the smali
        val name = ComponentUtils.createComponentName(activityInfo)

        if (pr != null) {
            val bs = BroadcastSystem.broadcastSystem
            val binder = Binder()
            pr.token = binder
            val pending = BroadcastSystem.PendingBroadcast()
            pending.activityInfo = activityInfo
            pending.pendingResult = pr2
            pending.remote = binder
            synchronized(bs!!.pendingBroadcasts)
            {
                bs.pendingBroadcasts[binder] = pending
            }
            val message = Message()
            message.obj = binder
            bs.anrHandler.sendMessageDelayed(message, 8500L)     // 0x2134
        }

        try {
            client!!.scheduleReceiver(activityInfo.processName, name, intent, pr)
        } catch (t: Throwable) {
            if (pr != null) {
                BroadcastSystem.broadcastSystem?.finishPendingBroadcast(pr)
            } else if (pr2 != null) {
                try {
                    pr2.finish()
                } catch (th: Throwable) {
                    Log.d(TAG, th.toString())
                }
            }
        }
        return true      // always true once we reach the delivery attempt
    }

    /**
     * Decides whether get missing process may be started for this broadcast.
     * true  = smali goto_167 (go start it)
     * false = smali goto_19b with get null record (log "not schedule receiver", return false)
     */
    private fun shouldStartMissingProcess(pkg: String, action: String?): Boolean {
        if (GmsSupport.isGmsPackage(pkg)) return true
        if (action != null &&
            (action.contains("com.android.vending.INSTALL_REFERRER") ||
                    action.contains("com.google.android.chimera.MODULE_CONFIGURATION_CHANGED"))
        ) return true

        val gsfPackages = SpecialComponentList.GOOGLE_SERVICES_FRAMEWORK_PACKAGE
        if (!gsfPackages.contains(pkg)) return false           // not get GSF package: never started here

        val context = appContext

        // 32-bit host, arm64-only guest, host isn't the arm64 build
        if (!Process.is64Bit()) {
            if (CloneSupportAgent.isArm64Only(pkg) && !context.packageName.endsWith("arm64")) {
                gsfPackages.remove(pkg)
                Log.d("CloneAgent", "Remove not supported: $pkg")
                return false
            }
        }

        if (!Process.is64Bit()) return true

        // 64-bit host: inspect the guest's ABIs. Any exception means "go start it".
        val abis = HashSet<String>()
        val onlyArm32: Boolean
        try {
            val appInfo = context.packageManager.getApplicationInfo(pkg, 0)

            val baseAbis = NativeLibraryHelperCompat.listAbiInApk(appInfo.sourceDir)
            if (baseAbis != null) {
                abis.addAll(baseAbis)
                if (baseAbis.any { it == "arm64"}) return true
            }

            val splits = appInfo.splitSourceDirs
            if (splits != null && splits.isNotEmpty()) {
                for (split in splits) {
                    val splitAbis = NativeLibraryHelperCompat.listAbiInApk(split)
                    if (splitAbis != null) {
                        abis.addAll(splitAbis)
                        if (splitAbis.any { it == "arm64"}) return true
                    }
                }
            }

            onlyArm32 = abis.any { it == "armeabi"} &&
                    !abis.any { it == "arm64"}
        } catch (e: Exception) {
            return true
        }

        // 32-bit-only guest on get host that isn't the arm32 build
        if (onlyArm32 && !context.packageName.endsWith("arm32")) {
            gsfPackages.remove(pkg)
            Log.d("CloneAgent", "Remove not supported: $pkg")
            return false
        }
        return true
    }


    fun scheduleGmsCheckinWarmup(userId: Int, packageName: String) {
        // bu8.get is VASettings.gmsState
        if (!VASettings.gmsState || userId < 0 || GmsSupport.isGmsPackage(packageName)) return

        val token = Any()

        synchronized(mGmsWarmupTokens) {
            // already warmed, or get warm-up is already in flight for this user
            if (nmGmsWarmedUsers.contains(userId) || mGmsWarmupTokens.containsKey(userId)) return
            mGmsWarmupTokens[userId] = token
        }   // lock released here, before the thread starts

        try {
            Thread(GmsWarmupTask(userId, token), "VA-GmsWarmup-u$userId").start()
        } catch (t: Throwable) {
            // thread failed to start: roll back our registration, but only if it is still ours
            synchronized(mGmsWarmupTokens) {
                if (mGmsWarmupTokens[userId] === token) mGmsWarmupTokens.remove(userId)
            }
            // smali calls t.toString() and discards it (stripped log), so the error is swallowed
        }
    }

    fun dispatchStaticReceiver(
        appId: Int,
        activityInfo: ActivityInfo,
        broadcastData: BroadcastIntentData,
        pr: PendingResultData?,
        pr2: BroadcastReceiver.PendingResult?,
    ): Boolean {
        val intent = broadcastData.intent
        val userId = broadcastData.userId
        // The smali's Objects.toString(intent) calls and the activityInfo.name read are
        // leftovers from stripped logging, so they are dropped.

        // A specific user: deliver to that user only
        if (userId != -1) {                                    // -1 = USER_ALL
            return handleStaticBroadcast(VUserHandle.makeUid(userId, appId), activityInfo, intent, pr, pr2)
        }

        // USER_ALL: deliver to every user that has the app installed
        var delivered = false
        VUserManagerService.get()?.let {
            for (candidateUserId in it.getUserIds())
            {
                val me = VAppManagerService.get()
                if (me != null && me.isAppInstalledAsUser(candidateUserId, activityInfo.packageName))
                {
                    val result = handleStaticBroadcast(
                        VUserHandle.makeUid(candidateUserId, appId), activityInfo, intent, pr, pr2
                    )                                              // always called, never short-circuited
                    delivered = delivered || result
                }
            }
        }
        return delivered
    }

    fun onVirtualUserRemovalStarted(i: Int) {
        synchronized(this.mGmsWarmupTokens) {
            this.mGmsWarmupTokens.remove(i)
            this.nmGmsWarmedUsers.remove(i)
        }
    }

    fun stopUser(userId: Int, callback: IStopUserCallback.Stub): Int {
        synchronized(mPidsSelfLocked) {
            for (i in mPidsSelfLocked.size - 1 downTo 0) {
                val record = mPidsSelfLocked.valueAt(i)          // yi7.i(i), cast to ProcessRecord
                if (record != null) {
                    if (record.userId == userId) {                   // h.j
                        Process.killProcess(record.Pid)              // h.g
                    }
                }
            }
        }
        try {
            callback.userStopped(userId)
        } catch (_: RemoteException) {
        }
        return 0
    }

    fun sendOrderedBroadcastAsUser(
        intent: Intent,
        user: VUserHandle?,
        receiverPermission: String?,        // p3, never used (the register is overwritten)
        resultReceiver: BroadcastReceiver?,
        scheduler: Handler?,
        initialCode: Int,
        initialData: String?,
        initialExtras: Bundle?,
    ) {


        if (user != null) {
            intent.putExtra("_VA_|_user_id_", user.userId)
        }

        appContext.sendOrderedBroadcast(
            intent,
            null,                           // permission is always null
            resultReceiver,
            scheduler,
            initialCode,
            initialData,
            initialExtras
        )
    }
    fun sendBroadcastAsUser(intent: Intent, user: VUserHandle?, receiverPermission: String?) {
        // receiverPermission (p3) is never used: the register is overwritten by the action lookup
        val virtualAction = SpecialComponentList.toVirtualAction(intent.action)
        if (virtualAction != null) {
            intent.action = virtualAction
        }


        if (user != null) {
            intent.putExtra("_VA_|_user_id_", user.userId)
        }

        appContext.sendBroadcast(intent)    // plain sendBroadcast, no permission
    }

    fun onCreate(context: Context) {
        if (AttributeCache.instance == null) {
            AttributeCache.instance = AttributeCache(context)
        }
        try {
            context.packageManager.getPackageInfo(context.packageName, 137)
        } catch (unused: PackageManager.NameNotFoundException) {
        }
        instance.set(this)
        val startProcessMonitor = StartProcessMonitor()
        startProcessMonitor.keyCache = HashMap<String, ProcessKey>()
        val handlerThread = HandlerThread("start-watchdog")
        handlerThread.start()
        startProcessMonitor.handler =
            StartProcessMonitorHandler(startProcessMonitor, handlerThread.getLooper())
        this.mProcessStartLocks = startProcessMonitor
        val handlerThread2 = HandlerThread("ams-worker")
        handlerThread2.start()
        this.mWorkerHandler = Handler(handlerThread2.getLooper())
    }
    //---------------------------------------------------------------------------------------------
    override fun dump() {

    }

    override fun getFreeStubCount(): Int = synchronized(mPidsSelfLocked) {
    val extras = emptySet<Int>()
    var free = 0
    for (i in 0 until 100) {
        if (!isStubInUse(i, extras)) free++
    }
    free
}

    override fun getInitialPackage(pid: Int): String? {
        synchronized(this.mPidsSelfLocked) {
            try {
                val processRecord = this.mPidsSelfLocked.get(pid)
                if (processRecord == null) {
                    return null
                }
                return processRecord.appInfo.packageName
            } catch (th: Throwable) {
                throw th
            }
        }

    }

    override fun getPendingIntent(binder: IBinder?): PendingIntentData? {
        return this.mPendingIntents.getPendingIntentData(binder!!)
    }

    override fun handleApplicationCrash() {

    }

    override fun isAppProcess(processName: String?): Boolean {
        return parseVPid(processName) != -1;

    }

    override fun registerProcessObserver(observer: IProcessObserver?) {

    }

    override fun removePendingIntent(token: IBinder?) {
        val store = mPendingIntents
        synchronized(store.mPendingIntents) {
            store.mPendingIntents.remove(token)
            // Original tries to remove from mPendingIntentsByKey too, but that map
            // is keyed by <whatever-the-key-type-is>, not IBinder — the remove
            // never matches and is a no-op. Cast preserves the original behavior.
            @Suppress("UNCHECKED_CAST")
            (store.mPendingIntentsByKey as MutableMap<Any?, *>).remove(token)
        }
    }

    override fun unregisterProcessObserver(observer: IProcessObserver?) {

    }

    override fun acquireProviderClient(
        userId: Int,
        providerInfo: ProviderInfo?
    ): IBinder? {
        val authority = providerInfo!!.authority

        // Diagnostics: who called us? A null result means the caller isn't in our
        // process table — log the stack but keep going (the call below will fail
        // or succeed on its own merits).
        val callingProcess = synchronized(mPidsSelfLocked) {
            findProcessLocked(getCallingPid())
        }
        if (callingProcess == null) {
            Log.d(TAG, Log.getStackTraceString(SecurityException("Who are you?")))
        }

        val processName = providerInfo.processName
        val targetProcess = startProcessIfNeeded(
            processName, userId, providerInfo.packageName, true,
        )
        val targetClient = targetProcess?.client

        // Process died or wasn't started — log and fall through to the null return.
        if (targetProcess == null || targetClient == null ||
            !targetClient.asBinder().isBinderAlive
        ) {
            Log.d(TAG, "Process $processName died after got binder")
        } else {
            try {
                return targetClient.acquireProviderClient(providerInfo)
            } catch (e: RemoteException) {
                if (e is TransactionTooLargeException) {
                    for (i in 0 until 5) {
                        Log.d(
                            TAG,
                            "retry $i for acquireProviderClient $authority",
                        )
                        try {
                            Thread.sleep(250L)
                            // NOTE: re-reads targetProcess.client each iteration.
                            // If the process was killed between attempts, `!!` NPEs
                            // here → caught by the inner catch(Throwable) → retry.
                            // Same behavior as the Java original.
                            return targetProcess.client!!.acquireProviderClient(providerInfo)
                        } catch (_: Throwable) {
                        }
                    }
                }
            } catch (_: Throwable) {
                // Was `th.toString();` — stripped log
            }
        }

        Log.d(TAG, "acquireProviderClient return null : $authority")
        return null
    }

    override fun addPendingIntent(token: IBinder?, packageName: String?) {
        val store = mPendingIntents
        val callingUserId = VBinder.getUidByPid() / 100000

        synchronized(store.mPendingIntents) {
            // Register the death recipient *before* inserting — matches the Java
            // order. If linkToDeath fails (already linked / dead binder), swallow
            // and proceed — the entry may still be valid.
            try {
                token?.linkToDeath(BinderDeathRecipient(store, token), 0)
            } catch (_: RemoteException) {
            }

            val existing = store.mPendingIntents[token]
            if (existing == null) {
                // First registration for this token — reconstruct the PendingIntent
                // by round-tripping the binder through a Parcel. The binder *is* the
                // IIntentSender; PendingIntent.readPendingIntentOrNullFromParcel
                // decodes it back into a PendingIntent.
                val data = PendingIntentData().apply { creatorPackage = packageName }
                val parcel = Parcel.obtain()
                try {
                    parcel.writeStrongBinder(token)
                    parcel.setDataPosition(0)
                    data.pendingIntent = PendingIntent.readPendingIntentOrNullFromParcel(parcel)
                } finally {
                    parcel.recycle()
                }
                store.mPendingIntents[token!!] = data
            } else {
                // Already registered — just update the package name (the PendingIntent
                // itself is unchanged).
                existing.creatorPackage = packageName
            }

            store.mPendingIntentsByKey[token!!] = callingUserId
        }
    }

    override fun appDoneExecuting() {
        synchronized(this.mPidsSelfLocked) {
            try {
                val processRecord = this.mPidsSelfLocked.get(getCallingPid())
                if (processRecord != null) {
                    processRecord.attachCondition.open()
                }
            } catch (th: Throwable) {
                throw th
            }
        }

    }

    /**
     * Was `findServiceRecord(int, ServiceInfo)`. Finds the [ServiceRecord] matching
     * the given [serviceInfo] under the given virtual user, or null if no live
     * service exists.
     *
     * A record matches when:
     *   - its owning process either doesn't exist yet OR belongs to [userId], AND
     *   - its `serviceInfo` is the same service as [serviceInfo] (compared via
     *     [ComponentUtils.isSameService] — package/name/class-name comparison that
     *     tolerates minor ComponentName shape variations).
     *
     * The "process is null" case matters: a newly-created `ServiceRecord` (from
     * `bindService` with `BIND_AUTO_CREATE`) has no process yet — the lookup must
     * find it so a subsequent `bindService` call doesn't create a duplicate.
     */
    fun findServiceRecord(userId: Int, serviceInfo: ServiceInfo): ServiceRecord? {
        synchronized(mServices) {
            for (record in mServices) {
                val process = record?.process
                if (process == null || process.userId == userId) {
                    if (ComponentUtils.isSameService(serviceInfo, record?.serviceInfo)) {
                        return record
                    }
                }
            }
            return null
        }
    }

    /**
     * Was `stopServiceLocked(ServiceRecord)`. Tears down a service:
     *
     *  1. Refuses if the service still has auto-create bindings (someone is actively
     *     using it — `Context.bindService` with `BIND_AUTO_CREATE`).
     *  2. Marks every client connection as "connected" (the framework's internal
     *     `connected=true` flag means "disconnection notification was sent") and
     *     invokes `IServiceConnection.connected(...)` with null binder (the
     *     disconnection signal). SDK 26+ uses the compat helper; older APIs call
     *     the 2-arg form directly.
     *  3. For every binding that has been delivered to the target process
     *     (`received = true`), resets the flag and dispatches `scheduleUnbindService`.
     *     SDK 36+ routes through `IVClient`; older APIs try QPR first, fall back to
     *     AOSP's 2-arg version, throw if neither is available.
     *  4. Dispatches `scheduleStopService` to the target process.
     *  5. Removes the record from the global service registry.
     *
     * Any per-connection or per-binding IPC failure is swallowed — the teardown
     * continues so the record can still be removed. A `RemoteException` from the
     * final `scheduleStopService` is swallowed too.
     */
    fun stopServiceLocked(serviceRecord: ServiceRecord) {
        // Refuse to stop while auto-create bindings exist.
        if (serviceRecord.hasAutoCreateConnections()) return

        // ===== Notify each client connection of the disconnection =====
        val connections = serviceRecord.connections
        if (connections != null) {
            for (list in connections.values) {
                for (connectionRecord in list) {
                    connectionRecord.connected = true
                    val serviceConnection = connectionRecord.connection
                    if (serviceConnection?.asBinder() != null) {
                        try {
                            if (Build.VERSION.SDK_INT >= 26) {
                                IServiceConnectionCompat.connected(
                                    serviceConnection,
                                    serviceRecord.name,
                                    null,   // no binder — disconnection
                                    null,   // no extras
                                    true,   // dead
                                )
                            } else {
                                serviceConnection.connected(serviceRecord.name, null)
                            }
                        } catch (_: Exception) {
                            // Best-effort — proceed with teardown regardless.
                        }
                    }
                }
            }
        }

        // ===== Unbind every "received" binding from the target process =====
        val unbindProcess = serviceRecord.process
        if (unbindProcess != null && unbindProcess.appThread != null) {
            for (bindRecord in serviceRecord.bindings.values) {
                if (!bindRecord.received) continue
                try {
                    bindRecord.received = false
                    // Re-read process — could have been nulled/replaced since the
                    // outer check. Preserved from original.
                    val currentProcess = serviceRecord.process
                    val intent = bindRecord.intent
                    if (Build.VERSION.SDK_INT >= 36) {
                        requireVClient(currentProcess).scheduleUnbindService(
                            serviceRecord, bindRecord, intent,
                        )
                    } else {
                        val appThread = requireAppThread(currentProcess)
                        val qpr = IApplicationThreadQprMirror.scheduleUnbindService
                        if (qpr != null) {
                            qpr.call(appThread, serviceRecord, bindRecord, intent)
                        } else {
                            val aosp = IApplicationThread.scheduleUnbindService
                                ?: throw RemoteException(
                                    "No compatible scheduleUnbindService method"
                                )
                            aosp.call(appThread, serviceRecord, intent)
                        }
                    }
                } catch (_: Exception) {
                    // Swallow — `received` already reset, move on to next binding.
                    // Same as the original's `continue` in the catch block.
                }
            }
        }

        // ===== Ask the target process to stop the service =====
        val stopProcess = serviceRecord.process
        if (stopProcess != null && stopProcess.appThread != null) {
            try {
                if (Build.VERSION.SDK_INT >= 36) {
                    requireVClient(stopProcess).scheduleStopService(serviceRecord)
                } else {
                    IApplicationThread.scheduleStopService?.call(
                        requireAppThread(stopProcess), serviceRecord,
                    )
                }
            } catch (_: RemoteException) {
            }
        }

        // ===== Remove from the global registry =====
        synchronized(mServices) {
            mServices.remove(serviceRecord)
        }
    }


    /**
     * Was `k0(Intent, boolean, int)`. Server-side `startService` handler.
     *
     * Steps:
     *  1. Resolve the target [ServiceInfo]. Null → return null.
     *  2. Ensure `processName` is set (defaults to `packageName`).
     *  3. Start the process if needed. Null → log, return null.
     *  4. Find an existing [ServiceRecord]. If it's already in the *same* process
     *     with no auto-create bindings, and it's the GMS chimera finalize intent,
     *     stop it first so we create a fresh record (GMS's notorious chimera reload).
     *  5. Create a new record OR reuse the existing one, marking whether a
     *     `scheduleCreateService` call is needed (new / process changed / process
     *     was null). When the process changed, reset every [IntentBindRecord]'s
     *     binder/requested/received/g state.
     *  6. If create is needed: dispatch `scheduleCreateService`, register in
     *     [mServices], then iterate every bind record and, for each with pending
     *     app connections, call [requestServiceBind] to actually bind.
     *  7. Update `lastActivityTime`. If [start] is set, increment `startId` and
     *     dispatch `scheduleService` / `scheduleServiceArgs` with the intent.
     *  8. Return the resolved [ComponentName].
     *
     * SDK 36+ routes through the VClient interface; earlier APIs fall back to the
     * mirror-based reflection calls. Preserved exactly.
     */
    fun startServiceLocked(
        intent: Intent?,
        start: Boolean,
        userId: Int,
    ): ComponentName? {
        val serviceInfo = resolveServiceInfo(userId, intent) ?: return null

        // ComponentInfo.processName is nullable; default to packageName. Extracted
        // to a local so the compiler sees non-null without a `!!`.
        val processName = serviceInfo.processName
            ?: serviceInfo.packageName.also { serviceInfo.processName = it }

        val process = startProcessIfNeeded(
            processName, userId, serviceInfo.packageName, true,
        )
        if (process == null) {
            Log.e(
                TAG,
                "Unable to start new Process for : ${ComponentUtils.createComponentName(serviceInfo)}",
            )
            return null
        }

        var existingRecord = findServiceRecord(userId, serviceInfo)

        // GMS chimera: force-stop a stale GmsIntentOperationService before re-creating.
        if (existingRecord != null &&
            existingRecord.process === process &&
            !existingRecord.hasAutoCreateConnections() &&
            intent != null &&
            "com.google.android.gms" == serviceInfo.packageName &&
            "com.google.android.gms.chimera.GmsIntentOperationService" == serviceInfo.name &&
            "com.google.android.chimera.container.NEW_MODULE_FINALIZE" == intent.action
        ) {
            stopServiceLocked(existingRecord)
            existingRecord = null
        }

        val serviceRecord: ServiceRecord
        val needsCreate: Boolean
        when {
            existingRecord == null -> {
                // No existing record — create a fresh one.
                serviceRecord = ServiceRecord().apply {
                    name = ComponentName(serviceInfo.packageName, serviceInfo.name)
                    startId = 0
                    activeSince = SystemClock.elapsedRealtime()
                    this.process = process
                    this.serviceInfo = serviceInfo
                }
                needsCreate = true
            }
            existingRecord.process == null -> {
                // Record exists but was never assigned a process — attach and create.
                existingRecord.process = process
                serviceRecord = existingRecord
                needsCreate = true
            }
            existingRecord.process !== process -> {
                // Process changed — reset every binding so the new process re-binds.
                existingRecord.process = process
                synchronized(existingRecord.bindings) {
                    for (bindRecord in existingRecord.bindings.values) {
                        bindRecord.binder = null
                        bindRecord.requested = false
                        bindRecord.received = false
                        bindRecord.doRebind = false
                    }
                }
                serviceRecord = existingRecord
                needsCreate = true
            }
            else -> {
                // Already bound to this process — nothing to create.
                serviceRecord = existingRecord
                needsCreate = false
            }
        }

        if (needsCreate) {
            try {
                val createServiceInfo = serviceRecord.serviceInfo
                if (Build.VERSION.SDK_INT >= 36) {
                    requireVClient(process)
                        .scheduleCreateService(serviceRecord, createServiceInfo, 0)
                } else {
                    IApplicationThreadKitkatMirror.scheduleCreateService?.call(
                        requireAppThread(process),
                        serviceRecord,
                        createServiceInfo,
                        CompatibilityInfoMirror.DEFAULT_COMPATIBILITY_INFO?.get(),
                        0,
                    )
                }
            } catch (_: RemoteException) {
            }

            synchronized(mServices) {
                mServices.add(serviceRecord)
            }

            // Request binds for every binding that has pending app connections.
            val bindings = serviceRecord.bindings
            if (bindings != null) {
                for (bindRecord in bindings.values) {
                    val bindProcess = serviceRecord.process
                    if (bindProcess == null || bindProcess.appThread == null) break

                    val shouldRequest: Boolean
                    synchronized(bindRecord) {
                        shouldRequest = !bindRecord.requested && bindRecord.apps.size > 0
                        if (shouldRequest) bindRecord.requested = true
                    }

                    if (shouldRequest) {
                        try {
                            requestServiceBind(
                                bindProcess, serviceRecord, bindRecord, bindRecord.intent!!, false,
                            )
                            bindRecord.received = true
                            bindRecord.doRebind = false
                        } catch (_: RemoteException) {
                            synchronized(bindRecord) {
                                bindRecord.requested = false
                            }
                            break
                        }
                    }
                }
            }
        }

        serviceRecord.lastActivityTime = SystemClock.uptimeMillis()

        if (start) {
            val newStartId = serviceRecord.startId + 1
            serviceRecord.startId = newStartId

            // Pre-Eclair (API < 5) services get the `rebind` flag; anything newer
            // passes false. Preserved for legacy SDK compat.
            val appInfo = serviceInfo.applicationInfo
            val doRebind = appInfo != null && appInfo.targetSdkVersion < 5

            try {
                if (Build.VERSION.SDK_INT >= 36) {
                    requireVClient(process).scheduleServiceArgs(
                        serviceRecord, doRebind, newStartId, 0, intent,
                    )
                } else {
                    IApplicationThreadCompatMirror.scheduleService(
                        requireAppThread(process),
                        serviceRecord,
                        doRebind,
                        newStartId,
                        0,
                        intent,
                    )
                }
            } catch (_: RemoteException) {
            }
        }

        return ComponentUtils.createComponentName(serviceInfo)
    }

    override fun bindService(
        appThreadBinder: IBinder?,
        token: IBinder?,
        intent: Intent?,
        resolvedType: String?,
        connection: IServiceConnection?,
        flags: Int,
        userId: Int
    ): Int {
        val serviceInfo = resolveServiceInfo(userId, intent) ?: return 0

        if (GmsSupport.isChimeraBoundServiceStart(intent, serviceInfo)) {
            return 0
        }

        // Look up existing ServiceRecord; may become non-null after startServiceLocked
        var record = findServiceRecord(userId, serviceInfo)

        // BIND_AUTO_CREATE (flag 1): start the service if it isn't running
        if (flags and 1 != 0) {
            if (startServiceLocked(intent, false, userId) != null) {
                record = findServiceRecord(userId, serviceInfo)
            }
        }

        // Create the ServiceRecord if still missing
        var wasExisting = true
        val serviceRecord: ServiceRecord = if (record == null) {
            wasExisting = false
            val newRecord = ServiceRecord().also { sr ->
                sr.name = ComponentName(serviceInfo.packageName, serviceInfo.name)
                sr.startId = 0
                sr.activeSince = SystemClock.elapsedRealtime()
                sr.serviceInfo = serviceInfo
                sr.retrieveIntentBindRecord(intent!!).addConnection(connection!!)
            }
            synchronized(mServices) {
                mServices.add(newRecord)
            }
            newRecord
        } else {
            record
        }

        // Find the caller's ProcessRecord by matching the appThread binder
        var callingProcess: ProcessRecord? = null
        synchronized(mProcessNames) {
            val map = mProcessNames.cache
            var i = map.size
            outer@ while (true) {
                val idx = i - 1
                if (i <= 0) {
                    callingProcess = null
                    break
                }
                val sparseArray = map.valueAt(idx)
                if (sparseArray != null) {
                    for (j in 0 until sparseArray.size)
                    {
                        val pr = sparseArray.valueAt(j)
                        if ((userId == -1 || pr.userId == userId) &&
                            appThreadBinder === pr.appThread?.asBinder()
                        ) {
                            callingProcess = pr
                            break@outer
                        }
                    }
                }
                i = idx
            }
        }

        val appBindRecord = serviceRecord.retrieveAppBindingLocked(intent!!, callingProcess!!)
        val connectionRecord = ConnectionRecord(appBindRecord, connection!!, flags)

        val connectionBinder = connection?.asBinder()!!

        // Register the ConnectionRecord with the ServiceRecord and the app binding
        val perServiceConnections = serviceRecord.connections
            .getOrPut(connectionBinder) { ArrayList() }
        perServiceConnections.add(connectionRecord)
        appBindRecord.connections.add(connectionRecord)

        // Also register globally, under the mServiceConnections monitor
        val globalConnections = synchronized(mServiceConnections) {
            mServiceConnections.getOrPut(connectionBinder) { ArrayList() }
        }
        globalConnections!!.add(connectionRecord)

        // Ensure an IntentBindRecord exists for this intent
        var intentBindRecord = serviceRecord.findIntentBindRecord(intent)
        if (intentBindRecord == null) {
            intentBindRecord = ServiceRecord.IntentBindRecord().also { it.intent = intent }
            synchronized(serviceRecord.bindings) {
                serviceRecord.bindings[Intent.FilterComparison(intent)] = intentBindRecord
            }
        }
        intentBindRecord.addConnection(connection)

        // If we just created the ServiceRecord, or the target process isn't running,
        // there's no binder to deliver yet — the bind will complete asynchronously.
        if (!wasExisting) return 0
        val targetProcess = serviceRecord.process ?: return 0
        if (targetProcess.appThread == null) return 0

        // Re-read the bind record — may differ from the one we created above if
        // another thread created it first.
        val bindRecord = serviceRecord.findIntentBindRecord(intent)
        if (bindRecord != null) {
            var binderAlive: Boolean
            var newlyRequested: Boolean
            synchronized(bindRecord) {
                val binder = bindRecord.binder
                binderAlive = binder != null && binder.isBinderAlive
                if (binderAlive || bindRecord.requested) {
                    newlyRequested = false
                } else {
                    bindRecord.requested = true
                    newlyRequested = true
                }
            }

            if (binderAlive) {
                // Binder already alive — rebind (if allowed) and notify immediately
                if (bindRecord.doRebind) {
                    try {
                        requestServiceBind(targetProcess, serviceRecord, bindRecord, intent, true)
                    } catch (_: RemoteException) {
                    }
                }
                connectionConnected(
                    connection,
                    ComponentName(serviceInfo.packageName, serviceInfo.name),
                    bindRecord,
                )
            } else if (newlyRequested) {
                // Binder not alive and we're the first requester — ask the target
                try {
                    requestServiceBind(targetProcess, serviceRecord, bindRecord, intent, false)
                } catch (_: RemoteException) {
                    // Failure: clear the requested flag so a future bind retries
                    synchronized(bindRecord) {
                        bindRecord.requested = false
                    }
                }
            }
        }

        serviceRecord.lastActivityTime = SystemClock.uptimeMillis()
        return 1
    }

    override fun broadcastFinish(data: PendingResultData?) {
        BroadcastSystem.broadcastSystem?.finishPendingBroadcast(data)
    }

    override fun forceStopPackage(packageName: String?, userId: Int) {
        if (packageName == null ||
            !VUserManagerService.get()!!.exists(userId) ||
            !VAppManagerService.get()!!.isAppInstalledAsUser(userId, packageName)
        ) {
            return
        }

        VAppManagerService.get()?.setPackageStoppedState(userId, packageName, true)
        killAppByPkg(packageName, userId)
        VJobSchedulerService.get()?.cancelAll(packageName, userId)

        // ===== Cancel pending intents registered by this package + user =====
        val store = mPendingIntents
        val toCancel = ArrayList<PendingIntentData>()
        synchronized(store.mPendingIntents) {
            val iterator = store.mPendingIntents.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val registeredUserId = store.mPendingIntentsByKey[entry.key]
                val data = entry.value
                if (data != null &&
                    packageName == data.creatorPackage &&
                    registeredUserId != null &&
                    registeredUserId == userId
                ) {
                    toCancel.add(data)
                    store.mPendingIntentsByKey.remove(entry.key)
                    iterator.remove()
                }
            }
        }
        for (data in toCancel) {
            data.pendingIntent?.cancel()
        }

        // ===== Cancel notifications =====
        VNotificationManagerService.get()?.let { notifService ->
            notifService.cancelAllNotification(packageName, userId)
            notifService.clearNotificationCnt(packageName, userId)
        }
    }

    override fun getActivityClassForToken(
        userId: Int,
        token: IBinder?
    ): ComponentName? {
        val stack = mActivityStack
        synchronized(stack.mTask) {
            val record = stack.findActivityByToken(userId, token) ?: return null
            return record.componentName
        }
    }

    override fun getAppProcessName(pid: Int): String? {
        synchronized(mPidsSelfLocked) {
            val processRecord = mPidsSelfLocked[pid] ?: return null
            return processRecord.processName
        }
    }

    override fun getCallingActivity(
        userId: Int,
        token: IBinder?
    ): ComponentName? {
        val stack = mActivityStack
        synchronized(stack.mTask) {
            val record = stack.findActivityByToken(userId, token) ?: return null

            ActivityStack.fixCaller(record)
            // Was `Objects.toString(record.componentName2);` — stripped log
            record.componentName2?.let { return it }
            return record.componentName
        }
    }

    override fun getCallingPackage(userId: Int, token: IBinder?): String? {
        val stack = mActivityStack
        synchronized(stack.mTask) {
            val record = stack.findActivityByToken(userId, token) ?: return "android"

            // Was `Objects.toString(record.intent);` — stripped log
            ActivityStack.fixCaller(record)
            // Was `Objects.toString(record.componentName2);` — stripped log
            return record.componentName2?.packageName ?: "android"
        }
    }

    override fun getPackageForIntentSender(sender: IBinder?): String? {
        val pendingIntentData = mPendingIntents.getPendingIntentData(sender!!)
        return pendingIntentData?.creatorPackage
    }

    override fun getPackageForToken(
        userId: Int,
        token: IBinder?
    ): String? {
        val activityStack = mActivityStack
        return synchronized(activityStack.mTask) {
            activityStack.findActivityByToken(userId, token)?.componentName?.packageName
        }

    }

    override fun getProcessPkgList(pid: Int): List<String>? {
        synchronized(mPidsSelfLocked) {
            val processRecord = mPidsSelfLocked[pid] ?: return emptyList()
            return ArrayList(processRecord.packageList)
        }
    }

    override fun getServices(
        maxNum: Int,
        flags: Int,
        userId: Int
    ): VParceledListSlice<*>? =
        synchronized(mServices) {
            val result = ArrayList<ActivityManager.RunningServiceInfo>(mServices.size)
            for (serviceRecord in mServices) {
                val processRecord = serviceRecord?.process ?: continue
                if (processRecord.userId != userId) continue

                val client = synchronized(mPidsSelfLocked) {
                    findProcessLocked(processRecord.Pid)
                }

                result += ActivityManager.RunningServiceInfo().apply {
                    uid = processRecord.vuid
                    pid = processRecord.Pid
                    if (client != null) {
                        process = client.processName
                        clientPackage = client.appInfo.packageName
                    }
                    activeSince = serviceRecord.activeSince
                    lastActivityTime = serviceRecord.lastActivityTime
                    clientCount = serviceRecord.getClientCount()
                    service = ComponentUtils.createComponentName(serviceRecord.serviceInfo!!)
                    started = serviceRecord.startId > 0
                }
            }
            VParceledListSlice(result)
        }


    override fun getSystemPid(): Int = Process.myPid()

    override fun getTaskInfo(tid: Int): AppTaskInfo? {
        val activityStack = mActivityStack
        return synchronized(activityStack.mTask) {
            val taskRecord = activityStack.mTask[tid] ?: return null
            val activities = taskRecord.activities
            if (activities.isEmpty()) return null

            val topComponent = activities[activities.lastIndex].componentName
            val intent = taskRecord.intent
            AppTaskInfo().apply {

                taskId = taskRecord.taskId
                baseIntent = intent
                baseActivity = intent?.component
                topActivity = topComponent
            }
        }
    }

    override fun getUidByPid(pid: Int): Int = synchronized(mPidsSelfLocked) {
    findProcessLocked(pid)?.vuid ?: Process.myUid()
    }

    override fun getVirtualUidByPid(pid: Int): Int =
        synchronized(mPidsSelfLocked) {
            findProcessLocked(pid)?.vuid ?: -10000
        }

    override fun initProcess(
        packageName: String?,
        processName: String?,
        userId: Int
    ): Int =
        startProcessIfNeeded(processName!!, userId, packageName!!, true)?.vpid ?: -1

    override fun isAppPid(pid: Int): Boolean =
        synchronized(mPidsSelfLocked) { findProcessLocked(pid) != null }

    override fun isAppRunning(packageName: String?, userId: Int): Boolean =
        synchronized(mPidsSelfLocked) {
            for (index in mPidsSelfLocked.size - 1 downTo 0) {
                val processRecord = mPidsSelfLocked.valueAt(index) ?: continue
                if (processRecord.userId == userId && processRecord.appInfo.packageName == packageName) return true
            }
            false
        }


    override fun isVAServiceToken(token: IBinder?): Boolean = token is ServiceRecord

    override fun killAllApps() {
        synchronized(mPidsSelfLocked) {
            for (i in 0 until mPidsSelfLocked.size)
            {       // yi7.h()
                val v = mPidsSelfLocked.valueAt(i)            // yi7.i(i)
                if (v is ProcessRecord) {
                    Process.killProcess(v.Pid)
                } else {
                    Log.e(TAG, "error pr: $v")               // qu8.get(tag, msg)
                }
            }
        }
    }

    override fun killAppByPkg(packageName: String?, userId: Int) {
        synchronized(mProcessNames) {                          // smali field h (ProcessMap)
            val cache = mProcessNames.cache                    // ArrayMap<String, SparseArray<ProcessRecord>>
            for (i in cache.size - 1 downTo 0) {
                val perUid = cache.valueAt(i) ?: continue
                for (j in 0 until perUid.size)
                {
                    val record = perUid.valueAt(j)
                    // userId == -1 means "all users"
                    if ((userId == -1 || record.userId == userId) &&
                        record.packageList.contains(packageName)) {
                        Process.killProcess(record.Pid)
                    }
                }
            }
        }
    }

    override fun killApplicationProcess(processName: String?, vuid: Int) {
        synchronized(mProcessNames) {
            mProcessNames.get(vuid, processName!!)?.let { Process.killProcess(it.Pid) }
        }
    }

    override fun notifyBadgerChange(info: BadgerInfo?) {
        val intent = Intent("com.lody.virtual.BADGER_CHANGE").apply {
            putExtra("userId", info?.vuid)
            putExtra("packageName", info?.packageName)
            putExtra("badgerCount", info?.badgenumber)
        }
        appContext.sendBroadcast(intent)
    }

    override fun onActivityCreated(
        component: ComponentName?,
        caller: ComponentName?,
        token: IBinder?,
        intent: Intent?,
        affinity: String?,
        taskId: Int,
        launchMode: Int,
        flags: Int
    ) {
        val callingPid = Binder.getCallingPid()
        val processRecord = synchronized(mPidsSelfLocked) { findProcessLocked(callingPid) } ?: return

        val activityStack = mActivityStack
        synchronized(activityStack.mTask) {
            activityStack.optimizeTasks()
            val taskRecord = activityStack.mTask[taskId]
                ?: TaskRecord(intent, affinity, taskId, processRecord.userId).also {
                    activityStack.mTask.put(taskId, it)
                }

            val activityRecord = ActivityRecord()
            activityRecord.taskRecord = taskRecord
            activityRecord.componentName = component
            activityRecord.componentName2 = caller
            activityRecord.binder = token
            activityRecord.processRecord = processRecord
            activityRecord.launchMode = launchMode
            activityRecord.intent = intent

            synchronized(taskRecord.activities) {
                taskRecord.activities.add(activityRecord)
            }
        }
    }

    override fun onActivityDestroyed(userId: Int, token: IBinder?): Boolean {
        val activityStack = mActivityStack
        return synchronized(activityStack.mTask) {
            activityStack.optimizeTasks()
            val activityRecord = activityStack.findActivityByToken(userId, token) ?: return false
            val activities = activityRecord.taskRecord?.activities!!
            synchronized(activities) {
                activities.remove(activityRecord)
            }
            true
        }
    }

    override fun onActivityResumed(userId: Int, token: IBinder?) {
        val activityStack = mActivityStack
        synchronized(activityStack.mTask) {
            activityStack.optimizeTasks()
            val activityRecord = activityStack.findActivityByToken(userId, token) ?: return
            val activities = activityRecord.taskRecord?.activities!!
            synchronized(activities) {
                activities.remove(activityRecord)
                activities.add(activityRecord)
            }
        }
    }

    override fun peekService(
        intent: Intent?,
        resolvedType: String?,
        userId: Int
    ): IBinder? {
        val serviceInfo = resolveServiceInfo(userId, intent) ?: return null
        val serviceRecord = findServiceRecord(userId, serviceInfo) ?: return null
        return serviceRecord.findIntentBindRecord(intent!!)?.binder?.takeIf { it.isBinderAlive }
    }

    override fun processRestarted(
        packageName: String?,
        processName: String?,
        userId: Int
    ) {
        val callingPid = Binder.getCallingPid()
        val vuid = VUserHandle.makeUid(userId, VAppManagerService.get()?.getAppId(packageName)!!)

        val existing = synchronized(mPidsSelfLocked) { findProcessLocked(callingPid) }
        if (existing != null) return

        val applicationInfo =
            VPackageManagerService.get().getApplicationInfo(packageName, 0, userId) ?: return
        applicationInfo.flags = applicationInfo.flags or ApplicationInfo.FLAG_HAS_CODE

        val realProcessName = activityManager?.runningAppProcesses
            ?.firstOrNull { it.pid == callingPid }
            ?.processName

        val vPid = parseVPid(realProcessName)
        if (vPid == -1) return

        val key = synchronized(mProcessStartLocks) {
            mProcessStartLocks.getKey(vuid, processName!!)
        }
        synchronized(key) {
            mProcessStartLocks.startMonitor(vuid, processName!!)
            synchronized(mProcessNames) {
                mProcessNames.remove(vuid, processName)
            }
            startProcessLocked(applicationInfo, processName, vuid, vPid)
            mProcessStartLocks.stopMonitor(vuid, processName)
        }
    }

    override fun publishService(
        token: IBinder?,
        intent: Intent?,
        binder: IBinder?,
        extra: IBinder?,
        userId: Int
    ) {
        val serviceRecord = token as? ServiceRecord ?: return
        val bindRecord = (if (binder != null) serviceRecord.findIntentBindRecord(binder) else serviceRecord.findIntentBindRecord(intent!!))
            ?: return

        val connections = synchronized(bindRecord) {
            bindRecord.binder = extra
            synchronized(bindRecord.connections) {
                ArrayList(bindRecord.connections)
            }
        }

        if (extra != null) {
            try {
                extra.linkToDeath(SelfUnlinkDeathRecipient(extra), 0)
            } catch (ignored: Throwable) {
                // remote already dead — linkToDeath throws, original swallows it
            }
        }

        for (connection in connections) {
            connectionConnected(
                connection,
                ComponentUtils.createComponentName(serviceRecord.serviceInfo!!),
                bindRecord
            )
        }
    }

    override fun requestGmsCheckin(userId: Int) {
        if (!VASettings.gmsState || userId < 0) return
        try {
            if (!VAppManagerService.get()!!.isAppInstalledAsUser(userId, GMS_PACKAGE)) return

            val vPackage = PackageCacheManager.getPackage(GMS_PACKAGE) ?: return
            val packageSetting = PackageCacheManager.getPackageSetting(GMS_PACKAGE) ?: return

            val componentName = ComponentName(GMS_PACKAGE, CHECKIN_RECEIVER)
            val receiver = vPackage.receivers.firstOrNull {
                componentName == ComponentUtils.createComponentName(it.info!!)
            } ?: return

            dispatchStaticReceiver(
                packageSetting.appId,
                receiver.info!!,
                BroadcastIntentData(
                    userId,
                    Intent(ACTION_CHECKIN_NOW).setComponent(componentName),
                    GMS_PACKAGE,
                    componentName
                ),
                null,
                null
            )
        } catch (th: Throwable) {
            // original swallowed everything — see note below
        }
    }

    override fun serviceDoneExecuting(
        token: IBinder?,
        type: Int,
        startId: Int,
        res: Int,
        userId: Int
    ) {
        val serviceRecord = token as? ServiceRecord ?: return
        if (type == 2) {
            synchronized(mServices) {
                mServices.remove(serviceRecord)
            }
        }
    }

    override fun setServiceForeground(
        className: ComponentName?,
        token: IBinder?,
        id: Int,
        notification: Notification?,
        removeNotification: Boolean,
        userId: Int
    ) {
        val serviceRecord = token as? ServiceRecord ?: return

        if (id == 0) {
            if (removeNotification) {
                val pkg = serviceRecord.serviceInfo?.packageName
                val vnm = VNotificationManager.instance
                val dealtId = vnm.dealNotificationId(serviceRecord.foregroundId, pkg, null, userId)
                val tag = try {
                    vnm.service?.dealNotificationTag(dealtId, pkg, null, userId)
                } catch (ignored: RemoteException) {
                    null
                }
                notificationManager?.cancel(tag, dealtId)
                serviceRecord.foregroundId = 0
            }
            return
        }

        requireNotNull(notification) { "null notification" }

        val oldId = serviceRecord.foregroundId
        if (oldId != id) {
            if (oldId != 0) {
                val oldPkg = serviceRecord.serviceInfo?.packageName
                val oldVnm = VNotificationManager.instance
                val oldDealtId = oldVnm.dealNotificationId(oldId, oldPkg, null, userId)
                val oldTag = try {
                    oldVnm.service?.dealNotificationTag(oldDealtId, oldPkg, null, userId)
                } catch (ignored: RemoteException) {
                    null
                }
                notificationManager?.cancel(oldTag, oldDealtId)
            }
            serviceRecord.foregroundId = id
        }

        val newPkg = serviceRecord.serviceInfo?.packageName
        val newVnm = VNotificationManager.instance
        val newDealtId = newVnm.dealNotificationId(id, newPkg, null, userId)
        val newTag = try {
            newVnm.service?.dealNotificationTag(newDealtId, newPkg, null, userId)
        } catch (ignored: RemoteException) {
            null
        }
        try {
            newVnm.service?.addNotification(newDealtId, newTag, newPkg, userId)
        } catch (ignored: Exception) {
        }
        try {
            notificationManager?.notify(newTag, newDealtId, notification)
        } catch (ignored: Throwable) {
        }
    }

    override fun startActivities(
        intents: Array<Intent>?,
        resolvedTypes: Array<String>?,
        token: IBinder?,
        options: Bundle?,
        userId: Int
    ): Int {
        if (intents == null) return 0

        val activityInfos = ArrayList<ActivityInfo>(intents.size)
        for (i in intents.indices) {
            activityInfos += (VirtualCore.gCore.resolveActivityInfo(userId, intents[i]) ?: return -1)
        }
        for (info in activityInfos) {
            VAppManagerService.get()?.setPackageStoppedState(userId, info.packageName, false)
        }

        val activityStack = mActivityStack
        activityStack.optimizeTasks()

        val intent = intents[0]
        val activityInfo = activityInfos[0]
        val callerRecord = activityStack.findActivityByToken(userId, token)

        if (callerRecord != null && callerRecord.launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (ActivityStack.hasFlag(Intent.FLAG_ACTIVITY_CLEAR_TOP, intent)) {
            intent.flags = intent.flags and Intent.FLAG_ACTIVITY_REORDER_TO_FRONT.inv()
        }
        if (ActivityStack.hasFlag(Intent.FLAG_ACTIVITY_CLEAR_TASK, intent) &&
            !ActivityStack.hasFlag(Intent.FLAG_ACTIVITY_NEW_TASK, intent)
        ) {
            intent.flags = intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TASK.inv()
        }

        var reuseTarget = when (activityInfo.documentLaunchMode) {
            ActivityInfo.DOCUMENT_LAUNCH_INTO_EXISTING -> ActivityStack.ReuseTarget.DOCUMENT
            ActivityInfo.DOCUMENT_LAUNCH_ALWAYS -> ActivityStack.ReuseTarget.MULTIPLE
            else -> ActivityStack.ReuseTarget.CURRENT
        }
        if (ActivityStack.hasFlag(Intent.FLAG_ACTIVITY_NEW_TASK, intent) ||
            activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_TASK
        ) {
            reuseTarget = if (ActivityStack.hasFlag(Intent.FLAG_ACTIVITY_MULTIPLE_TASK, intent)) {
                ActivityStack.ReuseTarget.MULTIPLE
            } else {
                ActivityStack.ReuseTarget.AFFINITY
            }
        }
        if (callerRecord == null && reuseTarget == ActivityStack.ReuseTarget.CURRENT) {
            reuseTarget = ActivityStack.ReuseTarget.AFFINITY
        }

        val taskAffinity = ComponentUtils.getTaskAffinity(activityInfo)
        val taskRecord = when (reuseTarget) {
            ActivityStack.ReuseTarget.AFFINITY -> activityStack.findTaskByAffinity(userId, taskAffinity)
            ActivityStack.ReuseTarget.CURRENT -> callerRecord!!.taskRecord
            ActivityStack.ReuseTarget.DOCUMENT -> activityStack.findTaskByDocument(userId, intent)
            else -> null // MULTIPLE
        }

        val stubIntents = arrayOfNulls<Intent>(intents.size)
        for (i in intents.indices) {
            stubIntents[i] = activityStack.createStubIntent(userId, callerRecord, intents[i], activityInfos[i])
        }
        for (stubIntent in stubIntents) {
            if (stubIntent == null) return -1
        }

        if (taskRecord == null) {
            return ActivityStack.startActivities(null, stubIntents.filterNotNull().toTypedArray(), resolvedTypes?.filterNotNull()?.toTypedArray(), options)
        }
        val topActivity = ActivityStack.topActivity(taskRecord) ?: return -1
        return ActivityStack.startActivities(topActivity.binder, stubIntents.filterNotNull().toTypedArray(), resolvedTypes?.filterNotNull()?.toTypedArray(), options)
    }

    override fun startActivity(
        intent: Intent?,
        info: ActivityInfo?,
        resultTo: IBinder?,
        options: Bundle?,
        resultWho: String?,
        requestCode: Int,
        userId: Int
    ): Int {
        if (info != null) {
            VAppManagerService.get()?.setPackageStoppedState(userId, info.packageName, false)
        }

        val activityStack = mActivityStack
        activityStack.optimizeTasks()
        val callerRecord = activityStack.findActivityByToken(userId, resultTo)

//        if (VLog.b) { // stripped-log scaffolding; reads are dead but kept for fidelity
//            val tasks = activityStack.mTask
//            for (i in 0 until tasks.size()) {
//                val task = tasks.valueAt(i)
//                task.taskId
//                task.activities.size
//            }
//        }

        val callerTask = callerRecord?.taskRecord
        // The dex dereferences both unconditionally from here on, so null must throw.
        val realIntent = intent!!
        val activityInfo = info!!

        var hasClearTop = ActivityStack.hasFlag(Intent.FLAG_ACTIVITY_CLEAR_TOP, realIntent)
        val hasClearTask = ActivityStack.hasFlag(Intent.FLAG_ACTIVITY_CLEAR_TASK, realIntent)

        if (realIntent.component == null) {
            realIntent.component = ComponentName(activityInfo.packageName, activityInfo.name)
        }
        if (activityInfo.packageName == "com.google.android.gms" &&
            activityInfo.name == "com.google.android.gms.games.ui.signinflow.SignInActivity" &&
            realIntent.action == "com.google.android.gms.games.SIGN_IN" &&
            ActivityStack.hasFlag(Intent.FLAG_ACTIVITY_FORWARD_RESULT, realIntent)
        ) {
            realIntent.flags = realIntent.flags and Intent.FLAG_ACTIVITY_FORWARD_RESULT.inv()
        }
        if (callerRecord != null && callerRecord.launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
            realIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        var clearTarget = ActivityStack.ClearTarget.NOTHING
        if (hasClearTop) {
            realIntent.flags = realIntent.flags and Intent.FLAG_ACTIVITY_REORDER_TO_FRONT.inv()
            clearTarget = ActivityStack.ClearTarget.TOP
        }
        if (hasClearTask) {
            if (ActivityStack.hasFlag(Intent.FLAG_ACTIVITY_NEW_TASK, realIntent)) {
                clearTarget = ActivityStack.ClearTarget.TASK
            } else {
                realIntent.flags = realIntent.flags and Intent.FLAG_ACTIVITY_CLEAR_TASK.inv()
            }
        }

        var reuseTarget = when (activityInfo.documentLaunchMode) {
            ActivityInfo.DOCUMENT_LAUNCH_INTO_EXISTING -> {
                clearTarget = ActivityStack.ClearTarget.TASK
                ActivityStack.ReuseTarget.DOCUMENT
            }
            ActivityInfo.DOCUMENT_LAUNCH_ALWAYS -> ActivityStack.ReuseTarget.MULTIPLE
            else -> ActivityStack.ReuseTarget.CURRENT
        }

        val singleTop = when (activityInfo.launchMode) {
            ActivityInfo.LAUNCH_SINGLE_TOP -> {
                if (ActivityStack.hasFlag(Intent.FLAG_ACTIVITY_NEW_TASK, realIntent)) {
                    reuseTarget =
                        if (ActivityStack.hasFlag(Intent.FLAG_ACTIVITY_MULTIPLE_TASK, realIntent))
                            ActivityStack.ReuseTarget.MULTIPLE
                        else ActivityStack.ReuseTarget.AFFINITY
                }
                true
            }
            ActivityInfo.LAUNCH_SINGLE_TASK -> {
                if (clearTarget != ActivityStack.ClearTarget.TASK) {
                    clearTarget = ActivityStack.ClearTarget.TOP
                }
                reuseTarget =
                    if (ActivityStack.hasFlag(Intent.FLAG_ACTIVITY_MULTIPLE_TASK, realIntent))
                        ActivityStack.ReuseTarget.MULTIPLE
                    else ActivityStack.ReuseTarget.AFFINITY
                hasClearTop = false
                false
            }
            ActivityInfo.LAUNCH_SINGLE_INSTANCE -> {
                clearTarget = ActivityStack.ClearTarget.TOP
                reuseTarget = ActivityStack.ReuseTarget.AFFINITY
                hasClearTop = false
                false
            }
            else -> ActivityStack.hasFlag(Intent.FLAG_ACTIVITY_SINGLE_TOP, realIntent)
        }

        if (clearTarget == ActivityStack.ClearTarget.NOTHING &&
            ActivityStack.hasFlag(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT, realIntent)
        ) {
            clearTarget = ActivityStack.ClearTarget.SPEC_ACTIVITY
        }
        if (callerTask == null && reuseTarget == ActivityStack.ReuseTarget.CURRENT) {
            reuseTarget = ActivityStack.ReuseTarget.AFFINITY
        }

        var taskAffinity = ComponentUtils.getTaskAffinity(activityInfo)
        val targetTask = when (reuseTarget) {
            ActivityStack.ReuseTarget.CURRENT -> callerTask
            ActivityStack.ReuseTarget.AFFINITY -> {
                if (realIntent.action == "com.google.android.gms.games.SIGN_IN") {
                    taskAffinity =
                        realIntent.getStringExtra("com.google.android.gms.games.GAME_PACKAGE_NAME")!!
                    Log.d("ActivityStack", "GMS login: from $taskAffinity")
                }
                if (taskAffinity == null) null
                else activityStack.findTaskByAffinity(userId, taskAffinity)
            }
            ActivityStack.ReuseTarget.DOCUMENT -> activityStack.findTaskByDocument(userId, realIntent)
            else -> null // MULTIPLE
        }

        // All activities already marked (or empty) => behave as "no reusable task".
        val reuseTask = targetTask?.takeIf { task -> task.activities.any { !it.marked } }
        if (reuseTask == null) {
            val stubIntent =
                activityStack.createStubIntent(userId, null, realIntent, activityInfo) ?: return -1
            stubIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            stubIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            stubIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            stubIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            return try {
                appContext.startActivity(stubIntent, options)
                0
            } catch (e: RuntimeException) {
                Log.d("ActivityStack", "Unable to start new virtual task", e)
                -1
            }
        }

        val taskIntent = reuseTask.intent
        val sameIntent = if (!hasClearTop && !singleTop && !hasClearTask) {
            val matches = taskIntent == null || (
                    realIntent.action == taskIntent.action &&
                            realIntent.data == taskIntent.data &&
                            realIntent.type == taskIntent.type &&
                            (realIntent.getPackage() ?: realIntent.component?.packageName) ==
                            (taskIntent.getPackage() ?: taskIntent.component?.packageName) &&
                            realIntent.component == taskIntent.component &&
                            realIntent.categories == taskIntent.categories
                    )
            // Null task intent throws here in the original; !! preserves that.
            matches && reuseTask.intent!!.flags == realIntent.flags
        } else false

        var markedAny = false
        var delivered = false
        if (clearTarget.deliverIntent || singleTop) {
            val component = realIntent.component
            val activities = reuseTask.activities
            synchronized(activities) {
                markedAny = when (clearTarget) {
                    ActivityStack.ClearTarget.SPEC_ACTIVITY -> {
                        var found = false
                        for (record in activities) {
                            if (record.componentName == component) {
                                record.marked = true
                                found = true
                            }
                        }
                        found
                    }
                    ActivityStack.ClearTarget.TASK -> {
                        var found = false
                        for (record in activities) {
                            record.marked = true
                            found = true
                        }
                        found
                    }
                    ActivityStack.ClearTarget.TOP -> {
                        val index = activities.indexOfLast { it.componentName == component }
                        if (index >= 0) {
                            for (i in index + 1 until activities.size) activities[i].marked = true
                        }
                        index >= 0
                    }
                    else -> false // NOTHING
                }
            }

            val topActivity = ActivityStack.topActivity(reuseTask)
            if (hasClearTop && !singleTop && topActivity != null && markedAny) {
                topActivity.marked = true
            }
            if (topActivity != null && !topActivity.marked &&
                topActivity.componentName == realIntent.component
            ) {
                val from = callerRecord?.componentName?.packageName ?: "android"
                delivered = try {
                    topActivity.processRecord!!.client!!.scheduleNewIntent(
                        from, topActivity.binder, realIntent
                    )
                    true
                } catch (e: RemoteException) {
                    false
                } catch (e: NullPointerException) {
                    false
                }
            }
        }

        var result = 0
        if (!sameIntent && !delivered) {
            val stubIntent =
                activityStack.createStubIntent(userId, callerRecord, realIntent, activityInfo)
                    ?: return -1
            val top = reuseTask.activities.lastOrNull()
            result = if (top == null) -1 else ActivityStack.startActivity(
                top.binder, stubIntent, resultWho, requestCode, options
            )
            if (result < 0) return result
        }

        if (markedAny) {
            if (delivered) {
                synchronized(activityStack.mTask) {
                    ActivityStack.finishMarkedActivities(reuseTask)
                }
            } else {
                val tasks = activityStack.mTask
                for (i in tasks.size() - 1 downTo 0) {
                    for (record in tasks.valueAt(i).activities) {
                        if (record.marked) VirtualRuntime.uiHandler.post(ActivityStackHandler(record))
                    }
                }
            }
        }

        if (!sameIntent && !delivered) return result

        try {
            activityStack.activityManager.moveTaskToFront(reuseTask.taskId, 0)
        } catch (e: Exception) {
            if (!delivered) return -1
        }
        return if (sameIntent) 2 else 0
    }

    override fun startService(
        caller: IBinder?,
        service: Intent?,
        resolvedType: String?,
        userId: Int
    ): ComponentName? {
        return startServiceLocked(service, true, userId)
    }

    override fun stopService(
        caller: IBinder?,
        service: Intent?,
        resolvedType: String?,
        userId: Int
    ): Int {
        val serviceInfo = resolveServiceInfo(userId, service) ?: return 0
        val serviceRecord = findServiceRecord(userId, serviceInfo) ?: return 0
        stopServiceLocked(serviceRecord)
        return 1
    }

    override fun stopServiceToken(
        className: ComponentName?,
        token: IBinder?,
        startId: Int,
        userId: Int
    ): Boolean {
        val serviceRecord = if (token != null) {
            token as? ServiceRecord ?: return false
        } else {
            if (className == null) return false
            val serviceInfo =
                resolveServiceInfo(userId, Intent().setComponent(className)) ?: return false
            findServiceRecord(userId, serviceInfo) ?: return false
        }
        if (serviceRecord.startId != startId && startId != -1) return false
        stopServiceLocked(serviceRecord)
        return true
    }

    override fun unbindFinished(
        token: IBinder?,
        service: Intent?,
        binder: IBinder?,
        doRebind: Boolean,
        userId: Int
    ) {
        val serviceRecord = token as? ServiceRecord ?: return
        val bindRecord = (if (binder != null) serviceRecord.findIntentBindRecord(binder) else serviceRecord.findIntentBindRecord(service!!))
            ?: return
        bindRecord.doRebind = doRebind
    }

    override fun unbindService(
        connection: IServiceConnection?,
        userId: Int
    ): Boolean {
        val connectionKey = connection!!.asBinder()
        val records = mServiceConnections[connectionKey] ?: return false

        while (records.size > 0) {
            val connectionRecord = records[0]
            val binderKey = connectionRecord?.connection?.asBinder()
            val appBind = connectionRecord?.appBind
            val serviceRecord = appBind?.service

            val serviceConnections = serviceRecord?.connections[binderKey]
            if (serviceConnections != null) {
                serviceConnections.remove(connectionRecord)
                if (serviceConnections.isEmpty()) {
                    serviceRecord.connections.remove(binderKey)
                }
            }
            appBind?.connections?.remove(connectionRecord)

            synchronized(mServiceConnections) {
                val globalConnections = mServiceConnections[binderKey]
                if (globalConnections != null) {
                    globalConnections.remove(connectionRecord)
                    if (globalConnections.isEmpty()) {
                        mServiceConnections.remove(binderKey)
                    }
                }
            }

            if (appBind!!.connections.isEmpty()) {
                appBind.intentBind.apps.remove(appBind.client)
            }
            appBind.intentBind.removeConnection(connectionRecord.connection)

            val conn = connectionRecord.connection
            val ownerRecord = synchronized(mServices) {
                mServices.firstOrNull { it != null && it.containConnection(conn) }
            }

            if (ownerRecord != null && !connectionRecord.connected) {
                val processRecord = ownerRecord.process
                if (processRecord != null && processRecord.appThread != null) {
                    try {
                        val intentBind = appBind.intentBind
                        val intent = intentBind.intent
                        if (Build.VERSION.SDK_INT >= 36) {
                            requireVClient(processRecord)
                                .scheduleUnbindService(ownerRecord, intentBind, intent)
                        } else {
                            val appThread = requireAppThread(processRecord)
                            val qprMethod = IApplicationThreadQprMirror.scheduleUnbindService
                            if (qprMethod != null) {
                                qprMethod.call(appThread, ownerRecord, intentBind, intent)
                            } else {
                                val baseMethod = IApplicationThread.scheduleUnbindService
                                    ?: throw RemoteException("No compatible scheduleUnbindService method")
                                baseMethod.call(appThread, ownerRecord, intent)
                            }
                        }
                    } catch (ignored: RemoteException) {
                        // original: `continue` — a no-op here, this is the end of the loop body
                    }
                }
            }
        }
        return true
    }


    //=============================================================

     inner class FirstLaunchTask(val packageSetting: PackageSetting,val uid: Int) : Runnable {

        override fun run() {

            val vActivityManagerService: VActivityManagerService = this@VActivityManagerService
            vActivityManagerService.javaClass
            val packageSetting = this.packageSetting
            val intent = Intent(
                "android.intent.action.PACKAGE_FIRST_LAUNCH",
                Uri.fromParts("package", packageSetting.packageName, null)
            )
            intent.setPackage(packageSetting.packageName)
            val appId = packageSetting.appId
            val uid = this.uid
            intent.putExtra("android.intent.extra.UID", makeUid(appId, uid))
            intent.putExtra("android.intent.extra.user_handle", uid)
            vActivityManagerService.sendBroadcastAsUser(intent, null)
            packageSetting.getOrCreateUserState(uid).launched = true
            VAppManagerService.get()!!.savePersistenceData()
        }
    }

    fun sendBroadcastAsUser(intent: Intent, user: VUserHandle?) {
        // Translate get real system action to its virtual (namespaced) counterpart, if one exists
        val virtualAction = SpecialComponentList.toVirtualAction(intent.action)
        if (virtualAction != null) {
            intent.action = virtualAction
        }


        // Tag the target user so the receiving side can route it
        if (user != null) {
            intent.putExtra("_VA_|_user_id_", user.userId)
        }

        appContext.sendBroadcast(intent)
    }


    // smali class m$e, nested in the ActivityManagerService. It doesn't need the outer instance.
    class BlockingPermissionCallback(
        private val condition: ConditionVariable
    ) : PermissionCompat.Callback {            // io.PermissionCompat.get

        override fun onRequestPermissionsResult(grantResults: IntArray?): Boolean {
            try {
                if (grantResults == null || grantResults.isEmpty()) return false
                for (result in grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) return false   // -1
                }
                return true
            } finally {
                condition.open()               // always release the waiter, whatever the result
            }
        }
    }

    // inner class of the ActivityManagerService (smali class m): it reads the outer mPidsSelfLocked
    inner class RemovePackageReceiversTask(
        private val deadProcess: ProcessRecord
    ) : Runnable {

        override fun run() {
            val packageName = deadProcess.appInfo.packageName

            // Is any other process of this package still registered?
            val stillRunning = synchronized(mPidsSelfLocked) {
                var found = false
                for (i in mPidsSelfLocked.size - 1 downTo 0) {
                    val record = mPidsSelfLocked.valueAt(i)
                    if (record != null && record.appInfo.packageName == packageName) {
                        found = true
                        break
                    }
                }
                found
            }
            if (stillRunning) return

            // getClass() in jadx is get null check, so get null BroadcastSystem throws NPE
            val broadcastSystem = BroadcastSystem.broadcastSystem!!

            if (!SpecialComponentList.GOOGLE_SERVICES_FRAMEWORK_PACKAGE.contains(packageName)) {
                broadcastSystem.removeReceivers(packageName)
                return
            }
            Log.d("d", "$packageName in white list")
        }
    }
    inner class GmsWarmupTask(
        private val uid: Int,        // smali m$c field get (get user id, despite the jadx name)
        private val token: Any,      // smali m$c field b
    ) : Runnable {

        override fun run() {
            try {
                if (!isWarmupTokenCurrent(this@VActivityManagerService, uid, token)) return

                if (!VAppManagerService.get()!!.isAppInstalledAsUser(uid, "com.google.android.gms")) return

                val packageSetting = PackageCacheManager.getPackageSetting("com.google.android.gms") ?: return

                val vuid = VUserHandle.makeUid(uid, packageSetting.appId)
                var gmsProcess: ProcessRecord? = synchronized(mProcessNames) {
                    mProcessNames.get(vuid, "com.google.android.gms.persistent") as ProcessRecord?
                }

                // not running (or its binder is dead): start it
                val liveClient = gmsProcess?.client
                if (gmsProcess == null || liveClient == null || !liveClient.asBinder().isBinderAlive) {
                    if (!isWarmupTokenCurrent(this@VActivityManagerService, uid, token)) return
                    gmsProcess = startProcessIfNeeded(
                        "com.google.android.gms.persistent", uid, "com.google.android.gms", false
                    )
                }

                // re-check after the (possibly slow) process start
                if (!isWarmupTokenCurrent(this@VActivityManagerService, uid, token)) return

                val client = gmsProcess?.client
                if (gmsProcess != null && client != null) {
                    client.bindApplication("com.google.android.gms", "com.google.android.gms.persistent")
                    synchronized(mGmsWarmupTokens) {
                        if (mGmsWarmupTokens[uid] !== token) return     // superseded: don't mark as warmed
                        nmGmsWarmedUsers.add(uid)
                    }
                }
            } catch (t: Throwable) {
                // jadx calls t.toString() and discards it (stripped log), so the error is swallowed
            } finally {
                synchronized(mGmsWarmupTokens) {
                    if (mGmsWarmupTokens[uid] === token) mGmsWarmupTokens.remove(uid)
                }
            }
        }
    }



    /**
     * Was `com.polestar.clone.server.am.f`. Binder-death recipient attached to
     * every `PendingIntent` token that goes through [PendingIntents.addPendingIntent].
     * When the caller process dies, removes the token from both maps so the server
     * doesn't hold stale binders (and so the maps don't leak entries for dead
     * processes).
     *
     * Package-private (`internal` in Kotlin) — only `PendingIntents` constructs it.
     * Not nested inside `PendingIntents` because the original source declared it as
     * a top-level class in the same package.
     */
    internal class BinderDeathRecipient(
        private val pendingIntents: PendingIntents,
        private val binder: IBinder,
    ) : IBinder.DeathRecipient {

        override fun binderDied() {
            // Detach from the dead binder first — otherwise a future re-registration
            // of the same IBinder object would attempt to linkToDeath twice.
            binder.unlinkToDeath(this, 0)

            // Same lock object as addPendingIntent / removePendingIntent —
            // guarantees the two maps stay consistent (no torn view where
            // mPendingIntents lost the token but mPendingIntentsByKey still has it).
            synchronized(pendingIntents.mPendingIntents) {
                pendingIntents.mPendingIntents.remove(binder)
                pendingIntents.mPendingIntentsByKey.remove(binder)
            }
        }
    }

    private class SelfUnlinkDeathRecipient(private val binder: IBinder) : IBinder.DeathRecipient {
        override fun binderDied() {
            binder.unlinkToDeath(this, 0)
        }
    }

    private class ActivityStackHandler(private val record: ActivityRecord) : Runnable {
        override fun run() {
            try {
                record.processRecord!!.client!!.finishActivity(record.binder)
            } catch (e: Exception) {
                Log.e("ActivityStack", "async finishActivity failed for ${record.componentName}")
                Log.d("ActivityStack", e.toString())
            }
        }
    }
}
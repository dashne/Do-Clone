package com.ims134.doclone.clone.server


import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.ConditionVariable
import android.os.Handler
import android.os.IBinder
import android.os.IInterface
import android.os.Message
import android.os.Process
import android.os.RemoteException
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
import com.ims134.doclone.clone.IServiceConnection
import com.ims134.doclone.clone.IStopUserCallback
import com.ims134.doclone.clone.PendingIntentData
import com.ims134.doclone.clone.PendingResultData
import com.ims134.doclone.clone.ProcessMap
import com.ims134.doclone.clone.ProviderCall
import com.ims134.doclone.clone.VParceledListSlice
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.VUserHandle.Companion.makeUid
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.io.ApplicationThreadNative
import com.ims134.doclone.io.ComponentUtils
import com.ims134.doclone.io.IApplicationThreadOreo
import com.ims134.doclone.io.IProcessObserver
import com.ims134.doclone.io.IVClient
import com.ims134.doclone.io.NativeLibraryHelperCompat
import com.ims134.doclone.io.PackageCacheManager
import com.ims134.doclone.io.PendingIntents
import com.ims134.doclone.io.PermissionCompat
import com.ims134.doclone.io.SpecialComponentList
import com.ims134.doclone.io.VASettings
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

        // smali m.v(m, I, Object): static helper on the outer class
        @JvmStatic
        fun isWarmupTokenCurrent(service: VActivityManagerService, userId: Int, token: Any?): Boolean {
            synchronized(service.mGmsWarmupTokens) {
                return service.mGmsWarmupTokens[userId] === token
            }
        }

    }

    val mProcessNames: ProcessMap<ProcessRecord> = ProcessMap()

    fun findProcessLocked(processName: String, uid: Int): ProcessRecord? {
        return this.mProcessNames.get(uid, processName)
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
    //---------------------------------------------------------------------------------------------
    override fun dump() {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun getPendingIntent(binder: IBinder?): PendingIntentData? {
        TODO("Not yet implemented")
    }

    override fun handleApplicationCrash() {
        TODO("Not yet implemented")
    }

    override fun isAppProcess(processName: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun registerProcessObserver(observer: IProcessObserver?) {
        TODO("Not yet implemented")
    }

    override fun removePendingIntent(binder: IBinder?) {
        TODO("Not yet implemented")
    }

    override fun unregisterProcessObserver(observer: IProcessObserver?) {
        TODO("Not yet implemented")
    }

    override fun acquireProviderClient(
        userId: Int,
        info: ProviderInfo?
    ): IBinder? {
        TODO("Not yet implemented")
    }

    override fun addPendingIntent(binder: IBinder?, packageName: String?) {
        TODO("Not yet implemented")
    }

    override fun appDoneExecuting() {
        TODO("Not yet implemented")
    }

    override fun bindService(
        caller: IBinder?,
        token: IBinder?,
        service: Intent?,
        resolvedType: String?,
        connection: IServiceConnection?,
        flags: Int,
        userId: Int
    ): Int {
        TODO("Not yet implemented")
    }

    override fun broadcastFinish(data: PendingResultData?) {
        TODO("Not yet implemented")
    }

    override fun forceStopPackage(packageName: String?, userId: Int) {
        TODO("Not yet implemented")
    }

    override fun getActivityClassForToken(
        userId: Int,
        token: IBinder?
    ): ComponentName? {
        TODO("Not yet implemented")
    }

    override fun getAppProcessName(pid: Int): String? {
        TODO("Not yet implemented")
    }

    override fun getCallingActivity(
        userId: Int,
        token: IBinder?
    ): ComponentName? {
        TODO("Not yet implemented")
    }

    override fun getCallingPackage(userId: Int, token: IBinder?): String? {
        TODO("Not yet implemented")
    }

    override fun getPackageForIntentSender(sender: IBinder?): String? {
        TODO("Not yet implemented")
    }

    override fun getPackageForToken(
        userId: Int,
        token: IBinder?
    ): String? {
        TODO("Not yet implemented")
    }

    override fun getProcessPkgList(pid: Int): List<String>? {
        TODO("Not yet implemented")
    }

    override fun getServices(
        maxNum: Int,
        flags: Int,
        userId: Int
    ): VParceledListSlice<*>? {
        TODO("Not yet implemented")
    }

    override fun getSystemPid(): Int {
        TODO("Not yet implemented")
    }

    override fun getTaskInfo(taskId: Int): AppTaskInfo? {
        TODO("Not yet implemented")
    }

    override fun getUidByPid(pid: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getVirtualUidByPid(pid: Int): Int {
        TODO("Not yet implemented")
    }

    override fun initProcess(
        packageName: String?,
        processName: String?,
        userId: Int
    ): Int {
        TODO("Not yet implemented")
    }

    override fun isAppPid(pid: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun isAppRunning(packageName: String?, userId: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun isVAServiceToken(token: IBinder?): Boolean {
        TODO("Not yet implemented")
    }

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
        TODO("Not yet implemented")
    }

    override fun killApplicationProcess(processName: String?, vuid: Int) {
        TODO("Not yet implemented")
    }

    override fun notifyBadgerChange(info: BadgerInfo?) {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun onActivityDestroyed(
        userId: Int,
        token: IBinder?
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun onActivityResumed(userId: Int, token: IBinder?) {
        TODO("Not yet implemented")
    }

    override fun peekService(
        intent: Intent?,
        resolvedType: String?,
        userId: Int
    ): IBinder? {
        TODO("Not yet implemented")
    }

    override fun processRestarted(
        packageName: String?,
        processName: String?,
        userId: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun publishService(
        token: IBinder?,
        intent: Intent?,
        binder: IBinder?,
        extra: IBinder?,
        userId: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun requestGmsCheckin(userId: Int) {
        TODO("Not yet implemented")
    }

    override fun serviceDoneExecuting(
        token: IBinder?,
        type: Int,
        startId: Int,
        res: Int,
        userId: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun setServiceForeground(
        className: ComponentName?,
        token: IBinder?,
        id: Int,
        notification: Notification?,
        removeNotification: Boolean,
        userId: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun startActivities(
        intents: Array<Intent>?,
        resolvedTypes: Array<String>?,
        token: IBinder?,
        options: Bundle?,
        userId: Int
    ): Int {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun startService(
        caller: IBinder?,
        service: Intent?,
        resolvedType: String?,
        userId: Int
    ): ComponentName? {
        TODO("Not yet implemented")
    }

    override fun stopService(
        caller: IBinder?,
        service: Intent?,
        resolvedType: String?,
        userId: Int
    ): Int {
        TODO("Not yet implemented")
    }

    override fun stopServiceToken(
        className: ComponentName?,
        token: IBinder?,
        startId: Int,
        userId: Int
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun unbindFinished(
        token: IBinder?,
        service: Intent?,
        binder: IBinder?,
        doRebind: Boolean,
        userId: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun unbindService(
        connection: IServiceConnection?,
        userId: Int
    ): Boolean {
        TODO("Not yet implemented")
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
}
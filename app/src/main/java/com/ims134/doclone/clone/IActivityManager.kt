package com.ims134.doclone.clone



import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import com.ims134.doclone.clone.server.AttributeCache
import com.ims134.doclone.clone.server.StartProcessMonitor
import com.ims134.doclone.clone.server.StartProcessMonitor.ProcessKey
import com.ims134.doclone.clone.server.VActivityManagerService.Companion.instance
import com.ims134.doclone.io.IProcessObserver


/**
 * Binder interface of the virtual ActivityManager service (was io.c23).
 * Parameter names are inferred from usage; transaction codes and wire format are unchanged.
 */
interface IActivityManager : IInterface {

    fun acquireProviderClient(userId: Int, info: ProviderInfo?): IBinder?
    fun addPendingIntent(binder: IBinder?, packageName: String?)
    fun appDoneExecuting()
    fun bindService(caller: IBinder?, token: IBinder?, service: Intent?, resolvedType: String?, connection: IServiceConnection?, flags: Int, userId: Int): Int
    fun broadcastFinish(data: PendingResultData?)
    fun forceStopPackage(packageName: String?, userId: Int)
    fun getActivityClassForToken(userId: Int, token: IBinder?): ComponentName?
    fun getAppProcessName(pid: Int): String?
    fun getCallingActivity(userId: Int, token: IBinder?): ComponentName?
    fun getCallingPackage(userId: Int, token: IBinder?): String?
    fun getPackageForIntentSender(sender: IBinder?): String?
    fun getPackageForToken(userId: Int, token: IBinder?): String?
    fun getProcessPkgList(pid: Int): List<String>?
    fun getServices(maxNum: Int, flags: Int, userId: Int): VParceledListSlice<*>?
    fun getSystemPid(): Int
    fun getTaskInfo(taskId: Int): AppTaskInfo?
    fun getUidByPid(pid: Int): Int
    fun getVirtualUidByPid(pid: Int): Int
    fun initProcess(packageName: String?, processName: String?, userId: Int): Int
    fun isAppPid(pid: Int): Boolean
    fun isAppRunning(packageName: String?, userId: Int): Boolean
    fun isVAServiceToken(token: IBinder?): Boolean
    fun killAllApps()
    fun killAppByPkg(packageName: String?, userId: Int)
    fun killApplicationProcess(processName: String?, vuid: Int)
    fun notifyBadgerChange(info: BadgerInfo?)
    fun onActivityCreated(component: ComponentName?, caller: ComponentName?, token: IBinder?, intent: Intent?, affinity: String?, taskId: Int, launchMode: Int, flags: Int)
    fun onActivityDestroyed(userId: Int, token: IBinder?): Boolean
    fun onActivityResumed(userId: Int, token: IBinder?)
    fun peekService(intent: Intent?, resolvedType: String?, userId: Int): IBinder?
    fun processRestarted(packageName: String?, processName: String?, userId: Int)
    fun publishService(token: IBinder?, intent: Intent?, binder: IBinder?, extra: IBinder?, userId: Int)
    fun requestGmsCheckin(userId: Int)
    fun serviceDoneExecuting(token: IBinder?, type: Int, startId: Int, res: Int, userId: Int)
    fun setServiceForeground(className: ComponentName?, token: IBinder?, id: Int, notification: Notification?, removeNotification: Boolean, userId: Int)
    fun startActivities(intents: Array<Intent>?, resolvedTypes: Array<String>?, token: IBinder?, options: Bundle?, userId: Int): Int
    fun startActivity(intent: Intent?, info: ActivityInfo?, resultTo: IBinder?, options: Bundle?, resultWho: String?, requestCode: Int, userId: Int): Int
    fun startService(caller: IBinder?, service: Intent?, resolvedType: String?, userId: Int): ComponentName?
    fun stopService(caller: IBinder?, service: Intent?, resolvedType: String?, userId: Int): Int
    fun stopServiceToken(className: ComponentName?, token: IBinder?, startId: Int, userId: Int): Boolean
    fun unbindFinished(token: IBinder?, service: Intent?, binder: IBinder?, doRebind: Boolean, userId: Int)
    fun unbindService(connection: IServiceConnection?, userId: Int): Boolean



    // ------------------------------------------------------------------

    abstract class Stub : Binder(), IActivityManager {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        // Server-side only: handled by onTransact but not part of IActivityManager.
        abstract fun dump()                                                // 13
        abstract fun getFreeStubCount(): Int                               // 2
        abstract fun getInitialPackage(pid: Int): String?                  // 16
        abstract fun getPendingIntent(binder: IBinder?): PendingIntentData? // 42
        abstract fun handleApplicationCrash()                              // 17
        abstract fun isAppProcess(processName: String?): Boolean           // 5
        abstract fun registerProcessObserver(observer: IProcessObserver?)  // 14
        abstract fun removePendingIntent(binder: IBinder?)                 // 44
        abstract fun unregisterProcessObserver(observer: IProcessObserver?) // 15

        override fun asBinder(): IBinder = this

        public override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code in 1..16777215) {
                data.enforceInterface(DESCRIPTOR)
            }
            if (code == 1598968902) {
                reply?.writeString(DESCRIPTOR)
                return true
            }
            val out = reply ?: return super.onTransact(code, data, reply, flags)

            when (code) {
                1 -> {
                    val r = initProcess(data.readString(), data.readString(), data.readInt())
                    out.writeNoException(); out.writeInt(r)
                }
                2 -> {
                    val r = getFreeStubCount()
                    out.writeNoException(); out.writeInt(r)
                }
                3 -> {
                    val r = getSystemPid()
                    out.writeNoException(); out.writeInt(r)
                }
                4 -> {
                    val r = getUidByPid(data.readInt())
                    out.writeNoException(); out.writeInt(r)
                }
                5 -> {
                    val r = isAppProcess(data.readString())
                    out.writeNoException(); out.writeInt(if (r) 1 else 0)
                }
                6 -> {
                    val r = isAppRunning(data.readString(), data.readInt())
                    out.writeNoException(); out.writeInt(if (r) 1 else 0)
                }
                7 -> {
                    val r = isAppPid(data.readInt())
                    out.writeNoException(); out.writeInt(if (r) 1 else 0)
                }
                8 -> {
                    val r = getAppProcessName(data.readInt())
                    out.writeNoException(); out.writeString(r)
                }
                9 -> {
                    val r = getProcessPkgList(data.readInt())
                    out.writeNoException(); out.writeStringList(r)
                }
                10 -> { killAllApps(); out.writeNoException() }
                11 -> { killAppByPkg(data.readString(), data.readInt()); out.writeNoException() }
                12 -> { killApplicationProcess(data.readString(), data.readInt()); out.writeNoException() }
                13 -> { dump(); out.writeNoException() }
                14 -> {
                    registerProcessObserver(IProcessObserver.Stub.asInterface(data.readStrongBinder()))
                    out.writeNoException()
                }
                15 -> {
                    unregisterProcessObserver(IProcessObserver.Stub.asInterface(data.readStrongBinder()))
                    out.writeNoException()
                }
                16 -> {
                    val r = getInitialPackage(data.readInt())
                    out.writeNoException(); out.writeString(r)
                }
                17 -> { handleApplicationCrash(); out.writeNoException() }
                18 -> { appDoneExecuting(); out.writeNoException() }
                19 -> {
                    val r = startActivities(
                        data.createTypedArray(Intent.CREATOR),
                        data.createStringArray(),
                        data.readStrongBinder(),
                        data.readTypedObject(Bundle.CREATOR),
                        data.readInt()
                    )
                    out.writeNoException(); out.writeInt(r)
                }
                20 -> {
                    val r = startActivity(
                        data.readTypedObject(Intent.CREATOR),
                        data.readTypedObject(ActivityInfo.CREATOR),
                        data.readStrongBinder(),
                        data.readTypedObject(Bundle.CREATOR),
                        data.readString(),
                        data.readInt(),
                        data.readInt()
                    )
                    out.writeNoException(); out.writeInt(r)
                }
                21 -> {
                    onActivityCreated(
                        data.readTypedObject(ComponentName.CREATOR),
                        data.readTypedObject(ComponentName.CREATOR),
                        data.readStrongBinder(),
                        data.readTypedObject(Intent.CREATOR),
                        data.readString(),
                        data.readInt(),
                        data.readInt(),
                        data.readInt()
                    )
                    out.writeNoException()
                }
                22 -> { onActivityResumed(data.readInt(), data.readStrongBinder()); out.writeNoException() }
                23 -> {
                    val r = onActivityDestroyed(data.readInt(), data.readStrongBinder())
                    out.writeNoException(); out.writeInt(if (r) 1 else 0)
                }
                24 -> {
                    val r = getActivityClassForToken(data.readInt(), data.readStrongBinder())
                    out.writeNoException(); out.writeTypedObject(r, 1)
                }
                25 -> {
                    val r = getCallingPackage(data.readInt(), data.readStrongBinder())
                    out.writeNoException(); out.writeString(r)
                }
                26 -> {
                    val r = getCallingActivity(data.readInt(), data.readStrongBinder())
                    out.writeNoException(); out.writeTypedObject(r, 1)
                }
                27 -> {
                    val r = getTaskInfo(data.readInt())
                    out.writeNoException(); out.writeTypedObject(r, 1)
                }
                28 -> {
                    val r = getPackageForToken(data.readInt(), data.readStrongBinder())
                    out.writeNoException(); out.writeString(r)
                }
                29 -> {
                    val r = isVAServiceToken(data.readStrongBinder())
                    out.writeNoException(); out.writeInt(if (r) 1 else 0)
                }
                30 -> {
                    val r = startService(
                        data.readStrongBinder(),
                        data.readTypedObject(Intent.CREATOR),
                        data.readString(),
                        data.readInt()
                    )
                    out.writeNoException(); out.writeTypedObject(r, 1)
                }
                31 -> {
                    val r = stopService(
                        data.readStrongBinder(),
                        data.readTypedObject(Intent.CREATOR),
                        data.readString(),
                        data.readInt()
                    )
                    out.writeNoException(); out.writeInt(r)
                }
                32 -> {
                    val r = stopServiceToken(
                        data.readTypedObject(ComponentName.CREATOR),
                        data.readStrongBinder(),
                        data.readInt(),
                        data.readInt()
                    )
                    out.writeNoException(); out.writeInt(if (r) 1 else 0)
                }
                33 -> {
                    setServiceForeground(
                        data.readTypedObject(ComponentName.CREATOR),
                        data.readStrongBinder(),
                        data.readInt(),
                        data.readTypedObject(Notification.CREATOR),
                        data.readInt() != 0,
                        data.readInt()
                    )
                    out.writeNoException()
                }
                34 -> {
                    val r = bindService(
                        data.readStrongBinder(),
                        data.readStrongBinder(),
                        data.readTypedObject(Intent.CREATOR),
                        data.readString(),
                        IServiceConnection.Stub.asInterface(data.readStrongBinder()),
                        data.readInt(),
                        data.readInt()
                    )
                    out.writeNoException(); out.writeInt(r)
                }
                35 -> {
                    val r = unbindService(IServiceConnection.Stub.asInterface(data.readStrongBinder()), data.readInt())
                    out.writeNoException(); out.writeInt(if (r) 1 else 0)
                }
                36 -> {
                    unbindFinished(
                        data.readStrongBinder(),
                        data.readTypedObject(Intent.CREATOR),
                        data.readStrongBinder(),
                        data.readInt() != 0,
                        data.readInt()
                    )
                    out.writeNoException()
                }
                37 -> {
                    serviceDoneExecuting(data.readStrongBinder(), data.readInt(), data.readInt(), data.readInt(), data.readInt())
                    out.writeNoException()
                }
                38 -> {
                    val r = peekService(data.readTypedObject(Intent.CREATOR), data.readString(), data.readInt())
                    out.writeNoException(); out.writeStrongBinder(r)
                }
                39 -> {
                    publishService(
                        data.readStrongBinder(),
                        data.readTypedObject(Intent.CREATOR),
                        data.readStrongBinder(),
                        data.readStrongBinder(),
                        data.readInt()
                    )
                    out.writeNoException()
                }
                40 -> {
                    val r = getServices(data.readInt(), data.readInt(), data.readInt())
                    out.writeNoException(); out.writeTypedObject(r, 1)
                }
                41 -> {
                    val r = acquireProviderClient(data.readInt(), data.readTypedObject(ProviderInfo.CREATOR))
                    out.writeNoException(); out.writeStrongBinder(r)
                }
                42 -> {
                    val r = getPendingIntent(data.readStrongBinder())
                    out.writeNoException(); out.writeTypedObject(r, 1)
                }
                43 -> { addPendingIntent(data.readStrongBinder(), data.readString()); out.writeNoException() }
                44 -> { removePendingIntent(data.readStrongBinder()); out.writeNoException() }
                45 -> {
                    val r = getPackageForIntentSender(data.readStrongBinder())
                    out.writeNoException(); out.writeString(r)
                }
                46 -> { processRestarted(data.readString(), data.readString(), data.readInt()); out.writeNoException() }
                47 -> { broadcastFinish(data.readTypedObject(PendingResultData.CREATOR)); out.writeNoException() }
                48 -> { notifyBadgerChange(data.readTypedObject(BadgerInfo.CREATOR)); out.writeNoException() }
                49 -> { forceStopPackage(data.readString(), data.readInt()); out.writeNoException() }
                50 -> {
                    val r = getVirtualUidByPid(data.readInt())
                    out.writeNoException(); out.writeInt(r)
                }
                51 -> { requestGmsCheckin(data.readInt()); out.writeNoException() }
                else -> return super.onTransact(code, data, reply, flags)
            }
            return true
        }




        // --------------------------------------------------------------

        class Proxy(val remote: IBinder) : IActivityManager {

            override fun asBinder(): IBinder = remote

            private inline fun <R> call(code: Int, write: (Parcel) -> Unit, read: (Parcel) -> R): R {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    write(data)
                    remote.transact(code, data, reply, 0)
                    reply.readException()
                    return read(reply)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            override fun acquireProviderClient(userId: Int, info: ProviderInfo?): IBinder? =
                call(41, {
                    it.writeInt(userId)
                    it.writeTypedObject(info, 0)
                }) { it.readStrongBinder() }

            override fun addPendingIntent(binder: IBinder?, packageName: String?) =
                call(43, {
                    it.writeStrongBinder(binder)
                    it.writeString(packageName)
                }) { }

            override fun appDoneExecuting() = call(18, {}) { }

            override fun bindService(caller: IBinder?, token: IBinder?, service: Intent?, resolvedType: String?, connection: IServiceConnection?, flags: Int, userId: Int): Int =
                call(34, {
                    it.writeStrongBinder(caller)
                    it.writeStrongBinder(token)
                    it.writeTypedObject(service, 0)
                    it.writeString(resolvedType)
                    it.writeStrongInterface(connection)
                    it.writeInt(flags)
                    it.writeInt(userId)
                }) { it.readInt() }

            override fun broadcastFinish(data: PendingResultData?) =
                call(47, { it.writeTypedObject(data, 0) }) { }

            override fun forceStopPackage(packageName: String?, userId: Int) =
                call(49, {
                    it.writeString(packageName)
                    it.writeInt(userId)
                }) { }

            override fun getActivityClassForToken(userId: Int, token: IBinder?): ComponentName? =
                call(24, {
                    it.writeInt(userId)
                    it.writeStrongBinder(token)
                }) { it.readTypedObject(ComponentName.CREATOR) }

            override fun getAppProcessName(pid: Int): String? =
                call(8, { it.writeInt(pid) }) { it.readString() }

            override fun getCallingActivity(userId: Int, token: IBinder?): ComponentName? =
                call(26, {
                    it.writeInt(userId)
                    it.writeStrongBinder(token)
                }) { it.readTypedObject(ComponentName.CREATOR) }

            override fun getCallingPackage(userId: Int, token: IBinder?): String? =
                call(25, {
                    it.writeInt(userId)
                    it.writeStrongBinder(token)
                }) { it.readString() }

            override fun getPackageForIntentSender(sender: IBinder?): String? =
                call(45, { it.writeStrongBinder(sender) }) { it.readString() }

            override fun getPackageForToken(userId: Int, token: IBinder?): String? =
                call(28, {
                    it.writeInt(userId)
                    it.writeStrongBinder(token)
                }) { it.readString() }

            override fun getProcessPkgList(pid: Int): List<String>? =
                call(9, { it.writeInt(pid) }) { it.createStringArrayList() }

            override fun getServices(maxNum: Int, flags: Int, userId: Int): VParceledListSlice<*>? =
                call(40, {
                    it.writeInt(maxNum)
                    it.writeInt(flags)
                    it.writeInt(userId)
                }) { it.readTypedObject(VParceledListSlice.CREATOR) }

            override fun getSystemPid(): Int = call(3, {}) { it.readInt() }

            override fun getTaskInfo(taskId: Int): AppTaskInfo? =
                call(27, { it.writeInt(taskId) }) { it.readTypedObject(AppTaskInfo.CREATOR) }

            override fun getUidByPid(pid: Int): Int =
                call(4, { it.writeInt(pid) }) { it.readInt() }

            override fun getVirtualUidByPid(pid: Int): Int =
                call(50, { it.writeInt(pid) }) { it.readInt() }

            override fun initProcess(packageName: String?, processName: String?, userId: Int): Int =
                call(1, {
                    it.writeString(packageName)
                    it.writeString(processName)
                    it.writeInt(userId)
                }) { it.readInt() }

            override fun isAppPid(pid: Int): Boolean =
                call(7, { it.writeInt(pid) }) { it.readInt() != 0 }

            override fun isAppRunning(packageName: String?, userId: Int): Boolean =
                call(6, {
                    it.writeString(packageName)
                    it.writeInt(userId)
                }) { it.readInt() != 0 }

            override fun isVAServiceToken(token: IBinder?): Boolean =
                call(29, { it.writeStrongBinder(token) }) { it.readInt() != 0 }

            override fun killAllApps() = call(10, {}) { }

            // NOTE: the decompiled original ignores userId and always writes -1 (USER_ALL);
            // R8 most likely propagated the only value callers passed. Pass -1 yourself to keep that.
            override fun killAppByPkg(packageName: String?, userId: Int) =
                call(11, {
                    it.writeString(packageName)
                    it.writeInt(userId)
                }) { }

            override fun killApplicationProcess(processName: String?, vuid: Int) =
                call(12, {
                    it.writeString(processName)
                    it.writeInt(vuid)
                }) { }

            override fun notifyBadgerChange(info: BadgerInfo?) =
                call(48, { it.writeTypedObject(info, 0) }) { }

            override fun onActivityCreated(component: ComponentName?, caller: ComponentName?, token: IBinder?, intent: Intent?, affinity: String?, taskId: Int, launchMode: Int, flags: Int) =
                call(21, {
                    it.writeTypedObject(component, 0)
                    it.writeTypedObject(caller, 0)
                    it.writeStrongBinder(token)
                    it.writeTypedObject(intent, 0)
                    it.writeString(affinity)
                    it.writeInt(taskId)
                    it.writeInt(launchMode)
                    it.writeInt(flags)
                }) { }

            override fun onActivityDestroyed(userId: Int, token: IBinder?): Boolean =
                call(23, {
                    it.writeInt(userId)
                    it.writeStrongBinder(token)
                }) { it.readInt() != 0 }

            override fun onActivityResumed(userId: Int, token: IBinder?) =
                call(22, {
                    it.writeInt(userId)
                    it.writeStrongBinder(token)
                }) { }

            override fun peekService(intent: Intent?, resolvedType: String?, userId: Int): IBinder? =
                call(38, {
                    it.writeTypedObject(intent, 0)
                    it.writeString(resolvedType)
                    it.writeInt(userId)
                }) { it.readStrongBinder() }

            override fun processRestarted(packageName: String?, processName: String?, userId: Int) =
                call(46, {
                    it.writeString(packageName)
                    it.writeString(processName)
                    it.writeInt(userId)
                }) { }

            override fun publishService(token: IBinder?, intent: Intent?, binder: IBinder?, extra: IBinder?, userId: Int) =
                call(39, {
                    it.writeStrongBinder(token)
                    it.writeTypedObject(intent, 0)
                    it.writeStrongBinder(binder)
                    it.writeStrongBinder(extra)
                    it.writeInt(userId)
                }) { }

            override fun requestGmsCheckin(userId: Int) =
                call(51, { it.writeInt(userId) }) { }

            override fun serviceDoneExecuting(token: IBinder?, type: Int, startId: Int, res: Int, userId: Int) =
                call(37, {
                    it.writeStrongBinder(token)
                    it.writeInt(type)
                    it.writeInt(startId)
                    it.writeInt(res)
                    it.writeInt(userId)
                }) { }

            override fun setServiceForeground(className: ComponentName?, token: IBinder?, id: Int, notification: Notification?, removeNotification: Boolean, userId: Int) =
                call(33, {
                    it.writeTypedObject(className, 0)
                    it.writeStrongBinder(token)
                    it.writeInt(id)
                    it.writeTypedObject(notification, 0)
                    it.writeInt(if (removeNotification) 1 else 0)
                    it.writeInt(userId)
                }) { }

            override fun startActivities(intents: Array<Intent>?, resolvedTypes: Array<String>?, token: IBinder?, options: Bundle?, userId: Int): Int =
                call(19, {
                    it.writeTypedArray(intents, 0)
                    it.writeStringArray(resolvedTypes)
                    it.writeStrongBinder(token)
                    it.writeTypedObject(options, 0)
                    it.writeInt(userId)
                }) { it.readInt() }

            override fun startActivity(intent: Intent?, info: ActivityInfo?, resultTo: IBinder?, options: Bundle?, resultWho: String?, requestCode: Int, userId: Int): Int =
                call(20, {
                    it.writeTypedObject(intent, 0)
                    it.writeTypedObject(info, 0)
                    it.writeStrongBinder(resultTo)
                    it.writeTypedObject(options, 0)
                    it.writeString(resultWho)
                    it.writeInt(requestCode)
                    it.writeInt(userId)
                }) { it.readInt() }

            override fun startService(caller: IBinder?, service: Intent?, resolvedType: String?, userId: Int): ComponentName? =
                call(30, {
                    it.writeStrongBinder(caller)
                    it.writeTypedObject(service, 0)
                    it.writeString(resolvedType)
                    it.writeInt(userId)
                }) { it.readTypedObject(ComponentName.CREATOR) }

            override fun stopService(caller: IBinder?, service: Intent?, resolvedType: String?, userId: Int): Int =
                call(31, {
                    it.writeStrongBinder(caller)
                    it.writeTypedObject(service, 0)
                    it.writeString(resolvedType)
                    it.writeInt(userId)
                }) { it.readInt() }

            override fun stopServiceToken(className: ComponentName?, token: IBinder?, startId: Int, userId: Int): Boolean =
                call(32, {
                    it.writeTypedObject(className, 0)
                    it.writeStrongBinder(token)
                    it.writeInt(startId)
                    it.writeInt(userId)
                }) { it.readInt() != 0 }

            override fun unbindFinished(token: IBinder?, service: Intent?, binder: IBinder?, doRebind: Boolean, userId: Int) =
                call(36, {
                    it.writeStrongBinder(token)
                    it.writeTypedObject(service, 0)
                    it.writeStrongBinder(binder)
                    it.writeInt(if (doRebind) 1 else 0)
                    it.writeInt(userId)
                }) { }

            override fun unbindService(connection: IServiceConnection?, userId: Int): Boolean =
                call(35, {
                    it.writeStrongInterface(connection)
                    it.writeInt(userId)
                }) { it.readInt() != 0 }
        }

        companion object {
            const val DESCRIPTOR = "com.polestar.clone.server.IActivityManager"

            @JvmStatic
            fun asInterface(binder: IBinder?): IActivityManager? {
                if (binder == null) return null
                val local = binder.queryLocalInterface(DESCRIPTOR)
                if (local is IActivityManager) return local
                return Proxy(binder)
            }
        }
    }

    // ------------------------------------------------------------------

    class Default : IActivityManager {
        override fun acquireProviderClient(userId: Int, info: ProviderInfo?): IBinder? = null
        override fun addPendingIntent(binder: IBinder?, packageName: String?) {}
        override fun appDoneExecuting() {}
        override fun asBinder(): IBinder? = null
        override fun bindService(caller: IBinder?, token: IBinder?, service: Intent?, resolvedType: String?, connection: IServiceConnection?, flags: Int, userId: Int): Int = 0
        override fun broadcastFinish(data: PendingResultData?) {}
        override fun forceStopPackage(packageName: String?, userId: Int) {}
        override fun getActivityClassForToken(userId: Int, token: IBinder?): ComponentName? = null
        override fun getAppProcessName(pid: Int): String? = null
        override fun getCallingActivity(userId: Int, token: IBinder?): ComponentName? = null
        override fun getCallingPackage(userId: Int, token: IBinder?): String? = null
        override fun getPackageForIntentSender(sender: IBinder?): String? = null
        override fun getPackageForToken(userId: Int, token: IBinder?): String? = null
        override fun getProcessPkgList(pid: Int): List<String>? = null
        override fun getServices(maxNum: Int, flags: Int, userId: Int): VParceledListSlice<*>? = null
        override fun getSystemPid(): Int = 0
        override fun getTaskInfo(taskId: Int): AppTaskInfo? = null
        override fun getUidByPid(pid: Int): Int = 0
        override fun getVirtualUidByPid(pid: Int): Int = 0
        override fun initProcess(packageName: String?, processName: String?, userId: Int): Int = 0
        override fun isAppPid(pid: Int): Boolean = false
        override fun isAppRunning(packageName: String?, userId: Int): Boolean = false
        override fun isVAServiceToken(token: IBinder?): Boolean = false
        override fun killAllApps() {}
        override fun killAppByPkg(packageName: String?, userId: Int) {}
        override fun killApplicationProcess(processName: String?, vuid: Int) {}
        override fun notifyBadgerChange(info: BadgerInfo?) {}
        override fun onActivityCreated(component: ComponentName?, caller: ComponentName?, token: IBinder?, intent: Intent?, affinity: String?, taskId: Int, launchMode: Int, flags: Int) {}
        override fun onActivityDestroyed(userId: Int, token: IBinder?): Boolean = false
        override fun onActivityResumed(userId: Int, token: IBinder?) {}
        override fun peekService(intent: Intent?, resolvedType: String?, userId: Int): IBinder? = null
        override fun processRestarted(packageName: String?, processName: String?, userId: Int) {}
        override fun publishService(token: IBinder?, intent: Intent?, binder: IBinder?, extra: IBinder?, userId: Int) {}
        override fun requestGmsCheckin(userId: Int) {}
        override fun serviceDoneExecuting(token: IBinder?, type: Int, startId: Int, res: Int, userId: Int) {}
        override fun setServiceForeground(className: ComponentName?, token: IBinder?, id: Int, notification: Notification?, removeNotification: Boolean, userId: Int) {}
        override fun startActivities(intents: Array<Intent>?, resolvedTypes: Array<String>?, token: IBinder?, options: Bundle?, userId: Int): Int = 0
        override fun startActivity(intent: Intent?, info: ActivityInfo?, resultTo: IBinder?, options: Bundle?, resultWho: String?, requestCode: Int, userId: Int): Int = 0
        override fun startService(caller: IBinder?, service: Intent?, resolvedType: String?, userId: Int): ComponentName? = null
        override fun stopService(caller: IBinder?, service: Intent?, resolvedType: String?, userId: Int): Int = 0
        override fun stopServiceToken(className: ComponentName?, token: IBinder?, startId: Int, userId: Int): Boolean = false
        override fun unbindFinished(token: IBinder?, service: Intent?, binder: IBinder?, doRebind: Boolean, userId: Int) {}
        override fun unbindService(connection: IServiceConnection?, userId: Int): Boolean = false
    }
}
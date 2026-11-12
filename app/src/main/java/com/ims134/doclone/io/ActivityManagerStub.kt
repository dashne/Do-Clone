package com.ims134.doclone.io


import android.app.ActivityManager
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException
import android.util.Log
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.MethodInvocationProxy
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VActivityManager
import com.ims134.doclone.clone.VUserInfo
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.clone.VirtualRuntime
import java.io.FileDescriptor
import java.lang.reflect.Method

/**
 * Hooks of the system's IActivityManager in the client process (was io.h3). The hooks of [MethodProxies] come from @Inject,
 * the ones below are added in [injectMethodProxies].
 *
 * Unseen classes (names kept as in the jadx output): StaticMethodProxy, ih6, gh6, jk6 (name, constant result), g3, VUserManager,
 * BinderInvocationStub, ParceledListSliceCompat. MethodInvocationStub.methodProxies must not be private for [inject].
 */
@Inject(MethodProxies::class)
open class ActivityManagerStub(iInterface: IInterface?) :
    MethodInvocationProxy<MethodInvocationStub<IInterface>>(MethodInvocationStub(iInterface)) {

    /** was h3$get: the call, but false when it throws (navigateUpTo) */
    private class FalseOnError(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (t: Throwable) {
                false
            }
    }

    /** was h3$c: the call, but false when it throws an Exception (isBackgroundRestricted) */
    private class FalseOnException(name: String) : ReplaceLastPkgMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (e: Exception) {
                false
            }
    }

    /** was h3$d: the call, but 0 when it throws an Exception (setRequestedOrientation) */
    private class ZeroOnException(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (e: Exception) {
                0
            }
    }

    /** was h3$e: always 0, that is PERMISSION_GRANTED (checkUriPermission) */
    private class AlwaysGranted(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? = 0
    }

    /** was h3$f: getRecentTasks with the user id replaced, and every task that is get clone task shows its virtual activities */
    private class GetRecentTasks(name: String) : StaticMethodProxy(name) {
        @Suppress("UNCHECKED_CAST")
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceLastUserIdArg(args)
            val result = if (args == null) method.invoke(who) else method.invoke(who, *args)
            val tasks: List<ActivityManager.RecentTaskInfo> =
                if (ParceledListSliceCompat.isReturnTypeSlice(method)) ParceledListSlice.getList!!.call(result) as List<ActivityManager.RecentTaskInfo>
                else result as List<ActivityManager.RecentTaskInfo>
            for (task in tasks) {
                try {
                    val info = VActivityManager.instance.getService().getTaskInfo(task.id)
                    if (info != null) {
                        val baseActivity = info.baseActivity
                        try {
                            task.topActivity = info.topActivity
                            task.baseActivity = baseActivity
                        } catch (_: Throwable) {
                        }
                        task.origActivity = baseActivity
                        task.baseIntent = info.baseIntent!!
                    }
                } catch (e: RemoteException) {
                    VirtualRuntime.crash(e)
                }
            }
            return result
        }
    }

    /** was h3$g: whether the virtual user id in args[0] exists */
    private class IsUserRunning : MethodProxy() {
        override fun getMethodName(): String = "isUserRunning"

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val userId = args!![0] as Int
            val users: List<VUserInfo>? = try {
                VUserManager.get()?.userManager?.getUsers(false) // was VUserManager.get() / field get, unseen
            } catch (e: RemoteException) {
                Log.w("VUserManager", "Could not get user list", e)
                null
            }
            if (users != null) {
                for (user in users) {
                    if (user.id == userId) {
                        return true
                    }
                }
            }
            return false
        }
    }

    /** was h3$b: get dummy binder (descriptor [ki6.m]) that is always alive; returned as the uid observer of registerUidObserverForUids */
    private class UidObserverBinder : IBinder {
        override fun getInterfaceDescriptor(): String? = "" //ki6.m

        override fun isBinderAlive(): Boolean = true

        override fun pingBinder(): Boolean = true

        override fun queryLocalInterface(descriptor: String): IInterface? = null

        override fun transact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean = false

        override fun unlinkToDeath(recipient: IBinder.DeathRecipient, flags: Int): Boolean = false

        override fun dump(fd: FileDescriptor, args: Array<String>?) {}

        override fun dumpAsync(fd: FileDescriptor, args: Array<String>?) {}

        override fun linkToDeath(recipient: IBinder.DeathRecipient, flags: Int) {}
    }

    /** was get(): the @Inject hooks, then (client process only) the extra ones below */
    override fun injectMethodProxies() {
        super.injectMethodProxies()
        if (VirtualCore.gCore.isClientProcess()) {
            addMethodProxy(FalseOnError("navigateUpTo"))
            addMethodProxy(ResultStaticMethodProxy ("getPackageProcessState", 1000))
            addMethodProxy(ResultStaticMethodProxy ("registerUidObserver", null))
            addMethodProxy(ResultStaticMethodProxy ("registerUidObserverForUids", UidObserverBinder()))
            addMethodProxy(IsUserRunning())
            addMethodProxy(ResultStaticMethodProxy ("updateConfiguration", 0))
            addMethodProxy(ReplaceCallingPkgMethodProxy ("setAppLockedVerifying"))
            addMethodProxy(ReplaceCallingPkgMethodProxy ("reportJunkFromApp"))
            addMethodProxy(ReplaceLastPkgMethodProxy("getAppStartMode"))
            addMethodProxy(ResultStaticMethodProxy ("getHistoricalProcessStartReasons", null))
            addMethodProxy(ResultStaticMethodProxy ("addApplicationStartInfoCompleteListener", null))
            addMethodProxy(ResultStaticMethodProxy ("removeApplicationStartInfoCompleteListener", null))
            addMethodProxy(ResultStaticMethodProxy ("addStartInfoTimestamp", null))
            addMethodProxy(ResultStaticMethodProxy ("getHistoricalProcessExitReasons", null))
            addMethodProxy(ResultStaticMethodProxy ("getProcessesInErrorState", null))
            addMethodProxy(FalseOnException("isBackgroundRestricted"))
            addMethodProxy(ResultStaticMethodProxy ("setActivityLocusContext", null))
            addMethodProxy(ZeroOnException("setRequestedOrientation"))
            addMethodProxy(AlwaysGranted("checkUriPermission"))
            addMethodProxy(GetRecentTasks("getRecentTasks"))
        }
    }

    /** puts the proxy where the system keeps the activity manager (singleton / gDefault) and into ServiceManager's cache as get binder */
    @Throws(Throwable::class)
    override fun inject() {
        if (BuildCompat.isAtLeastOreo()) {
            SingletonMirror.mInstance!!.set(ActivityManagerOreo.IActivityManagerSingleton!!.get(), invocationStub.proxyInterface)
        } else if (ActivityManagerNative.gDefault!!.type() == IActivityManagerMirror.TYPE) {
            ActivityManagerNative.gDefault!!.set(invocationStub.proxyInterface)
        } else if (ActivityManagerNative.gDefault!!.type() == SingletonMirror.TYPE) {
            SingletonMirror.mInstance!!.set(ActivityManagerNative.gDefault!!.get(), invocationStub.proxyInterface)
        }
        val binderStub = BinderInvocationStub(invocationStub.baseInterface)
        binderStub.methodProxies.putAll(invocationStub.methodProxies)
        ServiceManager.sCache!!.get()!!.put("activity", binderStub)
    }

    override fun isEnvBad(): Boolean =
        ActivityManagerNative.getDefault!!.call() !== invocationStub.proxyInterface
}
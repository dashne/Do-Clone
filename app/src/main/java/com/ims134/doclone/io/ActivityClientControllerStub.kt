package com.ims134.doclone.io

import android.os.IBinder
import android.os.IInterface
import android.os.RemoteException
import com.ims134.doclone.clone.MethodInvocationProxy
import com.ims134.doclone.clone.MethodInvocationStub
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VActivityManager
import com.ims134.doclone.clone.VPackageManager
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.VirtualRuntime
import java.lang.reflect.Method

/**
 * Hooks of the system's IActivityClientController (Android 12+), so that an activity asking who started it gets the
 * virtual caller, not the host's stub (was io.p2).
 */
class ActivityClientControllerStub :
    MethodInvocationProxy<MethodInvocationStub<IInterface>>(MethodInvocationStub(ActivityClient.getActivityClientController())) {

    /** was p2$b */
    private class GetCallingActivity : MethodProxy() {
        override fun getMethodName(): String = "getCallingActivity"

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val token = args?.firstOrNull() as? IBinder
            if (token != null) {
                try {
                    val caller = VActivityManager.instance.getService().getCallingActivity(
                        VUserHandle.getVUid(), token)
                    if (caller != null) return caller
                } catch (e: RemoteException) {
                    VirtualRuntime.crash(e)
                }
            }
            return if (args == null) method.invoke(who) else method.invoke(who, *args)
        }
    }

    /** was p2$c */
    private class GetCallingPackage : MethodProxy()
    {
        override fun getMethodName(): String = "getCallingPackage"

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val pkg = getCallingPackage(args?.firstOrNull() as? IBinder)
            return pkg ?: if (args == null) method.invoke(who) else method.invoke(who, *args)
        }
    }

    /** was p2$d: same as [GetCallingPackage] */
    private class GetLaunchedFromPackage : MethodProxy() {
        override fun getMethodName(): String = "getLaunchedFromPackage"

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val pkg = getCallingPackage(args?.firstOrNull() as? IBinder)
            return pkg ?: if (args == null) method.invoke(who) else method.invoke(who, *args)
        }
    }

    /** was p2$e: the uid of the virtual caller package, in the current virtual user */
    private class GetLaunchedFromUid : MethodProxy() {
        override fun getMethodName(): String = "getLaunchedFromUid"

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val pkg = getCallingPackage(args?.firstOrNull() as? IBinder)
            if (pkg != null) {
                try {
                    val uid = VPackageManager.instance?.getPackageManager()!!
                        .getPackageUid(pkg, VUserHandle.getVUid())
                    if (uid >= 0) return uid
                } catch (e: RemoteException) {
                    VirtualRuntime.crash(e)
                }
            }
            return if (args == null) method.invoke(who) else method.invoke(who, *args)
        }
    }

    /** was get(): registers the hooks */
    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(GetCallingPackage())
        addMethodProxy(GetCallingActivity())
        addMethodProxy(GetLaunchedFromPackage())
        addMethodProxy(GetLaunchedFromUid())
    }

    /** installs our proxy as the singleton's instance */
    override fun inject() {
        ActivityClient.setKnownInstance(invocationStub.proxyInterface)
    }

    override fun isEnvBad(): Boolean = ActivityClient.getKnownInstance() !== invocationStub.proxyInterface

    companion object {
        /**
         * was b(IBinder): the virtual package that started the activity with this token. Null if there is no
         * token, or if the activity manager answers "android" (get start by the system).
         */
        @JvmStatic
        fun getCallingPackage(token: IBinder?): String? {
            if (token != null) {
                try {
                    val pkg = VActivityManager.instance.getService().getCallingPackage(VUserHandle.getVUid(), token)
                    if ("android" != pkg) return pkg
                } catch (e: RemoteException) {
                    VirtualRuntime.crash(e)
                }
            }
            return null
        }
    }
}
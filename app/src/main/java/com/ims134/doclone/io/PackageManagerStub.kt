package com.ims134.doclone.io

import android.os.IInterface
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.MethodInvocationProxy
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VirtualCore
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

// jadx: io.fg5. Hooks the system IPackageManager of the client process.
//
// Unseen classes, names kept as in the jadx output (paste them to finish):
//   wq4 = the class whose nested MethodProxy classes are registered through @Inject (the main PackageManager hooks)
//   yr  = mirror holding RefStaticMethod<Void> disableGetPackagesForUidCache (nullable: missing on old Android)
@Inject(wq4::class)
class PackageManagerStub :
    MethodInvocationProxy<MethodInvocationStub<IInterface>>(MethodInvocationStub(ActivityThreadMirror.sPackageManager!!.get())) {

    /** jadx: fg5$a (isPermissionRevokedByPolicy). Last package + last user id replaced; false on failure. */
    private class LastPkgUserIdOrFalse(methodName: String) : ReplaceLastPkgMethodProxy(methodName) {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                MethodParameterUtils.replaceLastUserIdArg(args)
                super.call(who, method, args)
            } catch (_: Throwable) {
                false
            }
    }

    /** jadx: fg5$b. The real call without any change; false on failure. */
    private class CanRequestPackageInstalls : MethodProxy() {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                if (args == null) method.invoke(who) else method.invoke(who, *args)
            } catch (_: Throwable) {
                false
            }

        override fun getMethodName(): String = "canRequestPackageInstalls"
    }

    /** jadx: fg5$c (shouldShowRequestPermissionRationale). Last package + last user id replaced. */
    private class LastPkgUserId(methodName: String) : ReplaceLastPkgMethodProxy(methodName) {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceLastUserIdArg(args)
            return super.call(who, method, args)
        }
    }

    /** jadx: fg5$d (getSharedLibraries). Host package and host user id; strips flag 0x400000; empty list on SecurityException. */
    private class GetSharedLibraries(methodName: String) : StaticMethodProxy(methodName) {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceFirstPackageArg(args)
            val a = args!!
            if (a[a.size - 1] is Int) {
                a[a.size - 1] = VirtualCore.gCore.getHostUserId()
            }
            if (BuildCompat.sdk29OrLater()) {
                val flags = a[1]
                if (flags is Int) {
                    if ((flags and FLAG_STRIPPED) != 0) a[1] = flags and FLAG_STRIPPED.inv()
                } else if (flags is Long) {
                    a[1] = flags and FLAG_STRIPPED.toLong().inv()
                }
            }
            try {
                return super.call(who, method, args)
            } catch (th: Throwable) {
                var target: Throwable = th
                while (target is InvocationTargetException) {
                    target = target.targetException ?: break
                }
                if (target is SecurityException) {
                    return ParceledListSliceCompat.create(emptyList<Any>())
                }
                throw th
            }
        }

        private companion object {
            const val FLAG_STRIPPED = 4194304   // 0x400000
        }
    }

    /** jadx: fg5$e (setSplashScreenTheme). Calling package + last user id replaced; null on failure. */
    private class CallingPkgUserIdOrNull(methodName: String) : ReplaceCallingPkgMethodProxy(methodName) {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceLastUserIdArg(args)
            return try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                null
            }
        }
    }

    /** jadx: fg5$f and fg5$g (identical: getSplashScreenTheme, getUserMinAspectRatio). Calling package + last user id replaced. */
    private class CallingPkgUserId(methodName: String) : ReplaceCallingPkgMethodProxy(methodName) {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceLastUserIdArg(args)
            return super.call(who, method, args)
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(ResultStaticMethodProxy("addPermissionAsync", true))
        addMethodProxy(LastPkgUserIdOrFalse("isPermissionRevokedByPolicy"))
        addMethodProxy(CanRequestPackageInstalls())
        addMethodProxy(ResultStaticMethodProxy("addPermission", true))
        addMethodProxy(ResultStaticMethodProxy("performDexOpt", true))
        addMethodProxy(ResultStaticMethodProxy("performDexOptIfNeeded", false))
        addMethodProxy(ResultStaticMethodProxy("performDexOptSecondary", true))
        addMethodProxy(ResultStaticMethodProxy("addOnPermissionsChangeListener", 0))
        addMethodProxy(ResultStaticMethodProxy("removeOnPermissionsChangeListener", 0))
        addMethodProxy(LastPkgUserId("shouldShowRequestPermissionRationale"))
        addMethodProxy(ResultStaticMethodProxy("checkPackageStartable", 0))
        if (BuildCompat.isAtLeastOreo()) {
            addMethodProxy(ResultStaticMethodProxy("notifyDexLoad", 0))
            addMethodProxy(ResultStaticMethodProxy("notifyPackageUse", 0))
            addMethodProxy(ResultStaticMethodProxy("setInstantAppCookie", false))
            addMethodProxy(ResultStaticMethodProxy("isInstantApp", false))
            addMethodProxy(GetSharedLibraries("getSharedLibraries"))
        }
        if (BuildCompat.sdk30OrLater()) {
            addMethodProxy(CallingPkgUserIdOrNull("setSplashScreenTheme"))
            addMethodProxy(CallingPkgUserId("getSplashScreenTheme"))
            addMethodProxy(CallingPkgUserId("getUserMinAspectRatio"))
            addMethodProxy(ResultStaticMethodProxy("setUserMinAspectRatio", null))
            addMethodProxy(ReplaceCallingPkgMethodProxy("setMimeGroup"))
            addMethodProxy(ReplaceCallingPkgMethodProxy("getMimeGroup"))
        }
    }

    @Throws(Throwable::class)
    override fun inject() {
        ActivityThreadMirror.sPackageManager!!.set(invocationStub.proxyInterface)
        ApplicationPackageManagerMirror.disableGetPackagesForUidCache.call()
        val binderStub = BinderInvocationStub(invocationStub.baseInterface)
        binderStub.methodProxies.putAll(invocationStub.methodProxies)
        if (binderStub.binder != null) {
            ServiceManager.sCache.get()!!["package"] = binderStub
        }
    }

    override fun isEnvBad(): Boolean =
        invocationStub.proxyInterface !== ActivityThreadMirror.sPackageManager!!.get()
}
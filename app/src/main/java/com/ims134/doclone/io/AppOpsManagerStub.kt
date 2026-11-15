package com.ims134.doclone.io

import android.annotation.TargetApi
import android.app.AppOpsManager
import android.os.IInterface
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.reflect.RefClass
import com.ims134.doclone.reflect.RefObject
import com.ims134.doclone.reflect.RefStaticMethod
import com.ims134.doclone.reflect.RefStaticObject
import java.lang.reflect.Method


@TargetApi(19)
@Suppress("UNCHECKED_CAST")
class AppOpsManagerStub : BinderInvocationProxy(
    IAppOpsServiceMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "appops",
) {

    /**
     * Was `e`. Base proxy that rewrites two arguments at fixed indices:
     *  - `packageIndex` (2nd constructor param → field `d`): the package-name
     *    String arg, replaced with the host package.
     *  - `uidIndex` (1st constructor param → field `e`): the uid Int arg,
     *    replaced with the real calling uid.
     *
     * Note the constructor argument order quirk: the Java constructor is
     * `(String, uidIndex, packageIndex)` but assigns them to fields `(d, e)` in
     * reverse order. Preserved via named constructor params for clarity.
     */
    inner open class PackageAndUidRewritingProxy(
        name: String,
        private val uidIndex: Int,
        private val packageIndex: Int,
    ) : StaticMethodProxy(name) {

        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            if (args != null) {
                if (packageIndex != -1 &&
                    args.size > packageIndex &&
                    args[packageIndex] is String
                ) {
                    args[packageIndex] = MethodProxy.getHostPkg()
                }
                if (uidIndex != -1 && args.size > uidIndex && args[uidIndex] is Int) {
                    args[uidIndex] = MethodProxy.getRealUid()
                }
            }
            return true
        }
    }

    /**
     * Was `a`. `checkPackage` — always returns 0 (MODE_ALLOWED) without calling
     * the real service. Still runs the parent's `beforeCall` (pkg/uid rewrite).
     */
    inner class AllowCheckPackageProxy(
        name: String,
        uidIndex: Int,
        packageIndex: Int,
    ) : PackageAndUidRewritingProxy(name, uidIndex, packageIndex) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? = 0
    }

    /**
     * Was `b`. `checkOperationRaw` — delegates to the real service, catches any
     * `Throwable` → returns 0 (MODE_ALLOWED).
     */
    inner class AllowCheckOperationRawProxy(
        name: String,
        uidIndex: Int,
        packageIndex: Int,
    ) : PackageAndUidRewritingProxy(name, uidIndex, packageIndex) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                0
            }
    }

    /**
     * Was `c`. `resetAllModes` — zeroes the first Int arg (old mode) and returns 0.
     * Doesn't call the real service.
     */
    inner class ResetAllModesProxy(name: String) : ReplaceLastPkgMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args != null && args.isNotEmpty() && args[0] is Int) {
                args[0] = 0
            }
            return 0
        }
    }

    /** Was `d`. `noteProxyOperation` — resolves the op result via [resolveNoteOpResult]. */
    inner class NoteProxyOperationProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            resolveNoteOpResult(args, 0)

        override fun getMethodName(): String = "noteProxyOperation"
    }

    /**
     * Was `f`. Generic "proxy operation with state" proxy — resolves the op result
     * via [resolveNoteOpResult], with the op-mode index configured per instance.
     */
    inner class ProxyOperationWithStateProxy(
        private val methodName: String,
        private val opIndex: Int,
    ) : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            resolveNoteOpResult(args, opIndex)

        override fun getMethodName(): String = methodName
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ReplaceCallingPkgMethodProxy("startWatchingAsyncNoted"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("stopWatchingAsyncNoted"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("extractAsyncOps"))
        addMethodProxy(ResultStaticMethodProxy("collectNoteOpCallsForValidation", null))

        // (name, uidIndex, packageIndex)
        addMethodProxy(PackageAndUidRewritingProxy("checkOperation", 1, 2))
        addMethodProxy(PackageAndUidRewritingProxy("noteOperation", 1, 2))
        addMethodProxy(PackageAndUidRewritingProxy("startOperation", 2, 3))
        addMethodProxy(PackageAndUidRewritingProxy("finishOperation", 2, 3))
        addMethodProxy(PackageAndUidRewritingProxy("startWatchingMode", -1, 1))

        addMethodProxy(AllowCheckPackageProxy("checkPackage", 0, 1))
        addMethodProxy(AllowCheckOperationRawProxy("checkOperationRaw", 1, 2))

        addMethodProxy(PackageAndUidRewritingProxy("getOpsForPackage", 0, 1))
        addMethodProxy(PackageAndUidRewritingProxy("setMode", 1, 2))
        addMethodProxy(PackageAndUidRewritingProxy("checkAudioOperation", 2, 3))
        addMethodProxy(PackageAndUidRewritingProxy("setAudioRestriction", 2, -1))

        addMethodProxy(ResetAllModesProxy("resetAllModes"))
        addMethodProxy(NoteProxyOperationProxy())

        addMethodProxy(ProxyOperationWithStateProxy("noteProxyOperationWithState", 0))
        addMethodProxy(ProxyOperationWithStateProxy("startProxyOperationWithState", 1))
        addMethodProxy(ResultStaticMethodProxy("finishProxyOperationWithState", null))
    }

    @Throws(Throwable::class)
    override fun inject() {
        super.inject()
        val service = AppOpsManagerMirror.mService ?: return
        try {
            service.set(
                appContext.getSystemService("appops") as AppOpsManager,
                invocationStub.proxyInterface,
            )
            AppOpsManagerMirror.sService?.set(invocationStub.proxyInterface)
        } catch (_: Exception) {
        }
        // `invocationStub.d` is the obfuscated field; rename once identified
        // (likely `logInvocationCondition` based on the assigned value type).
        invocationStub.invocationLoggingCondition = LogInvocation.Condition.ALWAYS
    }

    companion object {
        /**
         * Was static helper `b(AppOpsManagerStub, Object[], int)`. On API <31,
         * always returns `0` (MODE_ALLOWED). On API 31+, extracts the op-mode
         * Int at [opIndex] and passes it through `m1.g(...)` for translation.
         *
         * The original took the outer `AppOpsManagerStub` instance as Stub first
         * parameter but only used it for `getClass()` (Stub stripped `assert` /
         * `Objects.requireNonNull` artifact). Dropped here.
         */
        @JvmStatic
        fun resolveNoteOpResult(args: Array<Any?>?, opIndex: Int): Any? {
            if (!BuildCompat.sdk31OrLater()) return 0
            var opMode = 0
            if (args != null && args.size > opIndex) {
                val arg = args[opIndex]
                if (arg is Int) opMode = arg
            }
            return Api30Shims.syncNotedAppOp(opMode)
        }


    }
}


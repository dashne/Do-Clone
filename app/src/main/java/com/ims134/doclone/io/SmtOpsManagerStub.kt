package com.ims134.doclone.io

import android.os.IInterface
import com.ims134.doclone.clone.GmsSupport
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.Method

@Inject(LocationManagerMethodProxies::class)
@Suppress("UNCHECKED_CAST")
class SmtOpsManagerStub : BinderInvocationProxy(
    ISmtOpsServiceMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "smtops",
) {

    /**
     * Was `d`. Base proxy that rewrites two args at fixed indices:
     *  - [uidIndex]: Int arg → real calling uid
     *  - [packageIndex]: String arg → host package name
     *
     * Constructor order matches the original Java `(name, uidIndex, packageIndex)`
     * — note the original assigns `this.d = i2; this.e = i` (reversed), which is
     * why named params clarify the intent here.
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
            // Original returns true unconditionally — bypasses super.beforeCall.
            return true
        }
    }

    /**
     * Was `a`. `checkPackage` — returns 0 (MODE_ALLOWED) for GMS packages
     * without consulting the real service. Non-GMS packages go through the
     * normal `super.call(...)` path (running the parent's beforeCall + real IPC).
     *
     * `objArr[1]` is the package-name argument (`packageIndex = 1` for this
     * proxy) — checked against the GMS package list.
     */
    inner class CheckPackageProxy(
        name: String,
        uidIndex: Int,
        packageIndex: Int,
    ) : PackageAndUidRewritingProxy(name, uidIndex, packageIndex) {

        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            if (GmsSupport.isGmsPackage(arr[1] as String?)) return 0
            return super.call(who, method, args)
        }
    }

    /**
     * Was `b`. `resetAllModes` — zeroes the first Int arg (old-mode field) and
     * returns 0 (MODE_ALLOWED). Doesn't call the real service.
     */
    inner class ResetAllModesProxy(name: String) : ReplaceLastPkgMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args != null && args.isNotEmpty() && args[0] is Int) {
                args[0] = 0
            }
            return 0
        }
    }

    /** Was `c`. `noteProxyOperation` — always returns 0 (MODE_ALLOWED). */
    inner class NoteProxyOperationProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? = 0

        override fun getMethodName(): String = "noteProxyOperation"
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        // (name, uidIndex, packageIndex)
        addMethodProxy(PackageAndUidRewritingProxy("checkOperation", 1, 2))
        addMethodProxy(PackageAndUidRewritingProxy("noteOperation", 1, 2))
        addMethodProxy(PackageAndUidRewritingProxy("startOperation", 2, 3))
        addMethodProxy(PackageAndUidRewritingProxy("finishOperation", 2, 3))
        addMethodProxy(PackageAndUidRewritingProxy("startWatchingMode", -1, 1))

        addMethodProxy(CheckPackageProxy("checkPackage", 0, 1))

        addMethodProxy(PackageAndUidRewritingProxy("getOpsForPackage", 0, 1))
        addMethodProxy(PackageAndUidRewritingProxy("setMode", 1, 2))
        addMethodProxy(PackageAndUidRewritingProxy("checkAudioOperation", 2, 3))
        addMethodProxy(PackageAndUidRewritingProxy("setAudioRestriction", 2, -1))

        addMethodProxy(ResetAllModesProxy("resetAllModes"))
        addMethodProxy(NoteProxyOperationProxy())
    }
}
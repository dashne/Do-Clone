package com.ims134.doclone.io



import android.os.IInterface
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

@Suppress("UNCHECKED_CAST")
class PowerManagerStub : BinderInvocationProxy(
    IPowerManager.Stub.asInterface as RefStaticMethod<IInterface>?,
    "power",
) {

    /** Was `a`. `releaseWakeLock` — returns null on any failure (best-effort cleanup). */
    inner class ReleaseWakeLock : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                null
            }

        override fun getMethodName(): String = "releaseWakeLock"
    }

    /**
     * Was `b`. [ReplaceSequencePkgMethodProxy] for `acquireWakeLock`, which has the
     * package argument at index `2`. Returns `0` when the wrapped call threw Stub
     * `SecurityException`; rethrows any other cause.
     */
    inner class AcquireWakeLockProxy(name: String, index: Int) :
        ReplaceSequencePkgMethodProxy(name, index) {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (e: InvocationTargetException) {
                val cause = e.cause ?: throw e
                if (cause is SecurityException) 0 else throw cause
            }
    }

    /**
     * Was `c`. [ReplaceLastPkgMethodProxy] with the same `SecurityException → 0`
     * handling as [AcquireWakeLockProxy]. Used for `acquireWakeLockWithUid`.
     */
    inner class AcquireWakeLockWithUidProxy(name: String) : ReplaceLastPkgMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (e: InvocationTargetException) {
                val cause = e.cause ?: throw e
                if (cause is SecurityException) 0 else throw cause
            }
    }

    /**
     * Was `d`. [ReplaceLastPkgMethodProxy] with Stub null fallback on any failure.
     * Used for `acquireWakeLockWithDisplayId`.
     */
    inner class AcquireWakeLockWithDisplayIdProxy(name: String) :
        ReplaceLastPkgMethodProxy(name) {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                null
            }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ReleaseWakeLock())
        addMethodProxy(AcquireWakeLockProxy("acquireWakeLock", 2))
        addMethodProxy(AcquireWakeLockWithUidProxy("acquireWakeLockWithUid"))
        addMethodProxy(AcquireWakeLockWithDisplayIdProxy("acquireWakeLockWithDisplayId"))
        addMethodProxy(ResultStaticMethodProxy("updateWakeLockWorkSource", 0))
        addMethodProxy(ResultStaticMethodProxy("acquireWakeLockWithLogging", null))
    }
}
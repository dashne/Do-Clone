package com.ims134.doclone.io

import com.ims134.doclone.clone.MethodProxy



import android.os.IInterface
import com.ims134.doclone.clone.MethodInvocationStub
import java.lang.reflect.Method

class WindowManagerMethodProxies private constructor() {

    /**
     * Was `a`. Abstract base for proxies that wrap an `IInterface` returned by the
     * real method — the return value is replaced with the proxy's own interface.
     */
    abstract class WrapResultProxy : MethodProxy() {

        private fun wrap(iInterface: IInterface): Any? =
            WindowSessionPatch(MethodInvocationStub(iInterface)).invocationStub.proxyInterface

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val result = method.invoke(who, *(args ?: emptyArray()))
            return if (result is IInterface) wrap(result) else result
        }
    }

    /** Was `b`. openSession — inherits result-wrapping behavior. */
    class OpenSession : WrapResultProxy() {
        override fun getMethodName(): String = "openSession"
    }

    /** Was `c`. overridePendingAppTransition — replaces package arg, then wraps result. */
    class OverridePendingAppTransition : WrapResultProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args ?: emptyArray<Any?>()
            if (arr.isNotEmpty() && arr[0] is String) {
                arr[0] = MethodProxy.getHostPkg()
            }
            return super.call(who, method, arr)
        }

        override fun getMethodName(): String = "overridePendingAppTransition"
    }

    /** Was `d`. overridePendingAppTransitionInPlace — replaces package arg; no result wrapping. */
    class OverridePendingAppTransitionInPlace : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args ?: emptyArray<Any?>()
            if (arr.isNotEmpty() && arr[0] is String) {
                arr[0] = MethodProxy.getHostPkg()
            }
            return method.invoke(who, *arr)
        }

        override fun getMethodName(): String = "overridePendingAppTransitionInPlace"
    }

    /** Was `e`. setAppStartingWindow — inherits result-wrapping behavior. */
    class SetAppStartingWindow : WrapResultProxy() {
        override fun getMethodName(): String = "setAppStartingWindow"
    }
}
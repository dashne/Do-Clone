package com.ims134.doclone.io

import android.util.Log
import com.ims134.doclone.clone.MethodProxy

import java.lang.reflect.Method

class StatusBarManagerStub : BinderInvocationProxy(
    IStatusBarService.Stub.TYPE,
    "statusbar",
) {

    /**
     * Was `b`. `disable` — no-op on the server side (returns null). The caller
     * gets no exception, matching the original's "pretend the disable call
     * succeeded" behavior.
     */
    class DisableProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            Log.d("StatusBarManagerStub", "Disable hooked")
            return null
        }

        override fun getMethodName(): String = "disable"
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(DisableProxy())
        addMethodProxy(ResultStaticMethodProxy("requestListeningState", null))
        addMethodProxy(ResultStaticMethodProxy("requestTileServiceListeningState", null))
    }
}
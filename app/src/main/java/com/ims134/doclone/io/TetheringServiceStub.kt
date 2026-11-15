package com.ims134.doclone.io

import android.os.IInterface
import android.util.Log
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.Method

/* JADX INFO: renamed from: io.w08 */
/* JADX INFO: compiled from: TetheringServiceStub.java */
/* JADX INFO: loaded from: classes2.dex */
@Suppress("UNCHECKED_CAST")
class TetheringServiceStub : BinderInvocationProxy(
    ITetheringConnectorMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "tethering",
) {

    /**
     * Was `a`. `isTetheringSupported` — invokes the last callback argument's
     * `onResult(3)` method reflectively (3 = `TETHER_ERROR_UNAVAILABLE`), so the
     * caller sees "tethering not supported" without the real service being
     * consulted. All exceptions are swallowed and logged.
     */
    inner class IsTetheringSupportedProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            try {
                val arr = args!!
                val callback = arr[arr.size - 1]
                if (callback == null) return null
                Reflect(callback).call("onResult", 3)
            } catch (th: Throwable) {
                Log.d("TetheringServiceStub", th.toString())
            }
            return null
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ResultStaticMethodProxy("tether", null))
        addMethodProxy(ResultStaticMethodProxy("untether", null))
        addMethodProxy(ResultStaticMethodProxy("setUsbTethering", null))
        addMethodProxy(ResultStaticMethodProxy("startTethering", null))
        addMethodProxy(ResultStaticMethodProxy("stopTethering", null))
        addMethodProxy(
            ResultStaticMethodProxy("requestLatestTetheringEntitlementResult", null)
        )
        addMethodProxy(ResultStaticMethodProxy("registerTetheringEventCallback", null))
        addMethodProxy(ResultStaticMethodProxy("unregisterTetheringEventCallback", null))
        addMethodProxy(IsTetheringSupportedProxy("isTetheringSupported"))
        addMethodProxy(ResultStaticMethodProxy("stopAllTethering", null))
    }
}
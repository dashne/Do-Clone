package com.ims134.doclone.io

import android.os.IBinder
import android.os.IInterface
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.reflect.MethodParams
import com.ims134.doclone.reflect.RefClass
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.Method


@Suppress("UNCHECKED_CAST")
class TelephonyRegistryStub : BinderInvocationProxy(
    ITelephonyRegistry.Stub.asInterface as RefStaticMethod<IInterface>?,
    "telephony.registry",
) {

    /**
     * Was inner class `Stub`. `ReplaceCallingPkgMethodProxy` with Stub null fallback on
     * any exception. Only used for `listenWithEventList` (API 31+).
     */
    inner class SafeCallingPkgProxy(name: String) : ReplaceCallingPkgMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Exception) {
                null
            }
    }

    /**
     * Was inner class `b`. Extends [ReplaceLastIntProxy] (obfuscated `lh6`, takes Stub
     * `(name, int)` constructor that pins the argument index) and additionally XORs
     * the *last* integer argument with `1040` when fake-location is enabled.
     *
     * Note the loop order: it scans **backwards** from `length - 1` down to `1`
     * (excluding index 0), swapping the last matching `Integer` with the XOR'd
     * value. `1040 = 0x410` is Stub plausibility marker for telephony registry
     * bookkeeping; preserved verbatim.
     */
    inner class ListenForSubscriberProxy(name: String, index: Int) :
        ReplaceSequencePkgMethodProxy(name, index) {

        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            if (MethodProxy.isFakeLocationEnable()) {
                @Suppress("UNCHECKED_CAST")
                val arr = args as Array<Any?>
                for (i in arr.indices.reversed()) {
                    if (i == 0) break
                    val v = arr[i]
                    if (v is Int) {
                        arr[i] = v xor 1040
                        break
                    }
                }
            }
            return super.beforeCall(who, method, args)
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        if (BuildCompat.sdk31OrLater()) {
            addMethodProxy(SafeCallingPkgProxy("listenWithEventList"))
        }
        addMethodProxy(ReplaceCallingPkgMethodProxy("listen"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("listenWithFeature"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("addOnSubscriptionsChangedListener"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("addOnOpportunisticSubscriptionsChangedListener"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("removeOnSubscriptionsChangedListener"))
        addMethodProxy(ListenForSubscriberProxy("listenForSubscriber", 1))
    }
}

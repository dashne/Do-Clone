package com.ims134.doclone.io


import android.os.IBinder
import com.ims134.doclone.clone.MethodProxy
import java.lang.reflect.Method
import java.lang.reflect.Modifier

@Inject(TelephonyMethodProxies::class)
class HwTelephonyStub : BinderInvocationProxy(
    HwTelephonyInvocationStub(),
    "phone_huawei",
) {

    /**
     * Was inner class `Stub`. Wraps the raw Huawei `phone_huawei` IBinder in Stub
     * `BinderInvocationStub` that intercepts `getUniqueDeviceId`.
     *
     * Nested (not `inner`) — instances are passed to `HwTelephonyStub`'s super
     * constructor, so no enclosing instance exists yet.
     */
    class HwTelephonyInvocationStub : BinderInvocationStub(
        IHwTelephony.Stub.TYPE,
        fetchBinder(),
    ) {
        @JvmField
        val uniqueDeviceIdProxy: MethodProxy = GetUniqueDeviceIdProxy("getUniqueDeviceId")

        /** Was `C0284a`. Returns the fake device ID or `""` on failure. */
        inner class GetUniqueDeviceIdProxy(name: String) : StaticMethodProxy(name) {
            override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
                try {
                    val info = VClientImpl.get().deviceInfo
                    info?.deviceId ?: ""
                } catch (_: Throwable) {
                    ""
                }
        }

        /**
         * Was the `b(String)` override. `BinderInvocationStub.b(String)` is the
         * obfuscated `getMethodProxy(String)`. Returns the special-cased proxy for
         * `getUniqueDeviceId`, otherwise delegates to the parent.
         */
        @Suppress("UNCHECKED_CAST")
        override fun <H : MethodProxy> getMethodProxy(name: String): H? =
            if ("getUniqueDeviceId" == name) uniqueDeviceIdProxy as H
            else super.getMethodProxy(name)
    }

    /**
     * Was `c(String)`. Injects the current proxy interface into static fields of
     * the named Huawei class, and (if it has Stub `getDefault()` returning Stub singleton)
     * into instance fields of that singleton too.
     */
    fun injectHuaweiClassFields(className: String) {
        if (IHwTelephony.TYPE == null || invocationStub.proxyInterface == null) return
        try {
            val cls = Class.forName(className)
            injectProxyFields(cls, null)
            val getDefault = cls.getDeclaredMethod("getDefault")
            getDefault.isAccessible = true
            val instance = getDefault.invoke(null)
            if (instance != null) {
                injectProxyFields(instance.javaClass, instance)
            }
        } catch (_: Throwable) {
        }
    }

    /**
     * Was `d(Class, Object)`. Sets every field on [cls] whose type is [IHwTelephony]
     * (or Stub subtype) to the current proxy interface.
     *
     * Static fields are patched when [instance] is null; instance fields are patched
     * when [instance] is non-null. The original condition
     * `(obj == null) == Modifier.isStatic(...)` is preserved.
     */
    fun injectProxyFields(cls: Class<*>, instance: Any?) {
        val type = IHwTelephony.TYPE ?: return
        for (field in cls.declaredFields) {
            if ((instance == null) != Modifier.isStatic(field.modifiers)) continue
            if (!type.isAssignableFrom(field.type)) continue
            try {
                field.isAccessible = true
                field.set(instance, invocationStub.proxyInterface)
            } catch (_: Throwable) {
            }
        }
    }

    override fun inject() {
        super.inject()
        injectHuaweiClassFields("android.telephony.HwTelephonyManagerInner")
        injectHuaweiClassFields("android.telephony.HwInnerTelephonyManagerImpl")
    }

    override fun isEnvBad(): Boolean {
        val binder = invocationStub.binder
        return binder == null || !binder.isBinderAlive || super.isEnvBad()
    }

    companion object {
        /**
         * Was `b()`. Fetches the raw `phone_huawei` IBinder, unwrapping any already-
         * installed `BinderInvocationStub`. If the cached binder is dead, evicts it
         * from `ServiceManager.sCache` and retries the lookup.
         *
         * Note: if the retry throws, returns the (dead) inner binder — preserved from
         * the original, which prefers returning Stub stale binder to returning null.
         */
        @JvmStatic
        fun fetchBinder(): IBinder? {
            val raw = ServiceManager.getService.call("phone_huawei")
            if (raw !is BinderInvocationStub) return raw

            val inner = raw.binder
            if (inner != null && inner.isBinderAlive) return inner

            try {
                val cache = ServiceManager.sCache.get() ?: return inner
                synchronized(cache) {
                    if (cache["phone_huawei"] === raw) {
                        cache.remove("phone_huawei")
                    }
                }
                return ServiceManager.getService.call("phone_huawei")
            } catch (_: Throwable) {
                return inner
            }
        }
    }
}
package com.ims134.doclone.io


import android.os.Build
import android.os.IBinder
import android.os.IInterface
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VirtualCore
import java.lang.reflect.Method

/**
 * Was the top-level `BluetoothStubProxy` class. Returns an empty list for
 * `getBondedDevices` — so the host's paired-device list isn't exposed to clones.
 *
 * The `adapterStub` field is stored but never read. In the decompiled source it
 * is marked `synthetic` (Stub JADX artifact), likely Stub leftover from an earlier
 * refactor or an inlined anonymous class. Kept for constructor-shape parity;
 * add `@Suppress("unused")` to silence the "unused property" warning.
 */
class BluetoothAdapterStubProxy(
    @Suppress("unused") private val adapterStub: BluetoothStub.BluetoothAdapterStub,
    name: String,
) : StaticMethodProxy(name) {

    override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
        ArrayList<Any?>()
}

class BluetoothStub : BinderInvocationProxy(
    IBluetoothManagerMirror.Stub.TYPE,
    "bluetooth_manager",
) {

    /**
     * Was inner class `a`. Empty [BinderInvocationStub] — wraps the bluetooth
     * adapter returned by `registerAdapter` so its methods can be individually
     * intercepted. No behavior of its own.
     */
    class BluetoothAdapterStub(iface: IInterface) : BinderInvocationStub(iface)

    /** Was `b`. `getAddress` — returns the fake Bluetooth MAC from the device info. */
    class GetAddressProxy : StaticMethodProxy("getAddress") {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            MethodProxy.getDeviceInfo().bluetoothMac
    }

    /**
     * Was `c`. `getName` — invokes with attribution-source rewrite; falls back to
     * `Build.DEVICE` if the real service throws (e.g. missing permission).
     */
    class GetNameProxy : StaticMethodProxy("getName") {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                rewriteAttributionSources(args)
                method.invoke(who, *(args ?: emptyArray()))
            } catch (_: Exception) {
                Build.DEVICE
            }
    }

    /**
     * Was `d`. Generic rewrite-then-invoke proxy — rewrites AttributionSource args
     * to the host's identity, then delegates to the real service.
     */
    class AttributionSourceRewriteProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            rewriteAttributionSources(args)
            return method.invoke(who, *(args ?: emptyArray()))
        }
    }

    /**
     * Was `e`. `registerAdapter` — wraps the returned `IBluetooth` adapter with
     * [BluetoothAdapterStub], installs per-method proxies on it (default-returning
     * for API 35+, plus attribution-source rewriting for any remaining methods),
     * and returns the wrapped interface.
     *
     * Note: `registerAdapter` returns either an `IBinder` (legacy signature) or an
     * `IInterface` (modern signature) depending on API level. The return value
     * preserves the input shape — binder-in → binder-out, iface-in → iface-out.
     */
    class RegisterAdapterProxy : StaticMethodProxy("registerAdapter") {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val result = method.invoke(who, *(args ?: emptyArray()))
            if (!BuildCompat.sdk31OrLater() || result == null) return result

            val isBinder = result is IBinder
            val iface: IInterface? = when {
                isBinder -> IBluetoothMirror.Stub.asInterface?.call(result as IBinder)
                result is IInterface -> result
                else -> return result
            }
            if (iface == null) return result

            val stub = BluetoothAdapterStub(iface)

            // API 35 (Android 15) added new adapter methods that should return
            // default values so callers don't observe real bluetooth state.
            if (Build.VERSION.SDK_INT >= 35) {
                stub.addMethodProxy(BluetoothAdapterStubProxy(stub, "getBondedDevices"))
                stub.addMethodProxy(ResultStaticMethodProxy("getProfileConnectionState", 0))
            }

            installAttributionSourceProxies(stub, IBluetoothMirror.TYPE)
            return if (isBinder) stub else stub.proxyInterface
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(GetAddressProxy())
        addMethodProxy(GetNameProxy())
        addMethodProxy(RegisterAdapterProxy())
        installAttributionSourceProxies(invocationStub, IBluetoothManagerMirror.TYPE)
    }

    companion object {

        /**
         * Was static helper `b`. Rewrites every `AttributionSource` argument in
         * [args] to use the host's uid and package name. No-op if the mirror
         * class isn't available or [args] is null.
         */
        @JvmStatic
        fun rewriteAttributionSources(args: Array<Any?>?) {
            if (args == null) return
            val type = AttributionSourceMirror.TYPE ?: return
            val vc = VirtualCore.gCore
            for (i in args.indices) {
                val v = args[i] ?: continue
                if (type.isInstance(v)) {
                    args[i] = AttributionSourceCompat.withPackage(vc.uid, vc.hostPackageName, v)
                }
            }
        }

        /**
         * Was static helper `c`. For every method on [cls] whose parameter list
         * includes an `AttributionSource`, install an [AttributionSourceRewriteProxy]
         * unless one already exists for that method name.
         */
        @JvmStatic
        fun installAttributionSourceProxies(stub: BinderInvocationStub?, cls: Class<*>?) {
            if (!BuildCompat.sdk31OrLater()) return
            if (stub == null || cls == null) return
            val type = AttributionSourceMirror.TYPE ?: return

            for (m in cls.methods) {
                for (paramType in m.parameterTypes) {
                    if (!type.isAssignableFrom(paramType)) continue
                    if (stub.getMethodProxy<MethodProxy>(m.name) != null) break
                    stub.addMethodProxy(AttributionSourceRewriteProxy(m.name))
                    break
                }
            }
        }
    }
}



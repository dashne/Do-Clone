package com.ims134.doclone.io

import com.ims134.doclone.clone.VirtualCore



import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.os.Bundle
import android.os.IInterface
import android.os.Parcelable
import android.os.WorkSource
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.reflect.RefStaticMethod

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Collections
import java.util.regex.Pattern

@Suppress("UNCHECKED_CAST")
class WifiManagerStub : BinderInvocationProxy(
    IWifiManagerMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "wifi",
) {

    /** Was `a`. `isWifiEnabled` — no-op passthrough. */
    inner class IsWifiEnabledProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            super.call(who, method, args)
        override fun getMethodName(): String = "isWifiEnabled"
    }

    /**
     * Was `b`. Replaces any String argument equal to the current virtual package
     * name with the host package name — so the framework sees the host's identity.
     */
    inner class PackageNameRewritingProxy(name: String) : StaticMethodProxy(name) {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            if (args != null) {
                val currentPkg = VClientImpl.get().getCurrentPackage()
                if (currentPkg != null) {
                    for (i in args.indices) {
                        if (args[i] is String && currentPkg == args[i]) {
                            args[i] = VirtualCore.gCore.hostPackageName
                            break
                        }
                    }
                }
            }
            return super.beforeCall(who, method, args)
        }
    }

    /** Was `c`. `getWifiEnabledState` — no-op passthrough. */
    inner class GetWifiEnabledStateProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            super.call(who, method, args)
        override fun getMethodName(): String = "getWifiEnabledState"
    }

    /** Was `d`. `createDhcpInfo` — no-op passthrough. */
    inner class CreateDhcpInfoProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            super.call(who, method, args)
        override fun getMethodName(): String = "createDhcpInfo"
    }

    /** Was `e`. `getWifiApConfiguration` — null on any exception. */
    inner class GetWifiApConfigurationProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Exception) {
                null
            }
        override fun getMethodName(): String = "getWifiApConfiguration"
    }

    /**
     * Was `f`. `getConnectionInfo` — invokes the real method, overwrites BSSID/MAC
     * for fake-location and MAC-spoof cases. On any failure, builds Stub synthetic
     * `WifiInfo` (SSID "Polestar", an IPv4 address from the host's non-loopback
     * interfaces if one exists, plus plausible-looking field values).
     */
    inner class GetConnectionInfoProxy : ReplaceCallingPkgMethodProxy("getConnectionInfo") {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            rewriteAttributionSourcesInBundles(args)
            return try {
                val wifiInfo = method.invoke(who, *(args ?: emptyArray())) as? WifiInfo
                if (MethodProxy.isFakeLocationEnable()) {
                    WifiInfoMirror.mBSSID?.set(wifiInfo, "00:00:00:00:00:00")
                    WifiInfoMirror.mMacAddress?.set(wifiInfo, "00:00:00:00:00:00")
                }
                if (wifiInfo != null) {
                    WifiInfoMirror.mMacAddress?.set(wifiInfo, MethodProxy.getDeviceInfo().macAddress)
                }
                wifiInfo
            } catch (_: Exception) {
                buildFakeWifiInfo()
            }
        }

        private fun buildFakeWifiInfo(): WifiInfo? {
            val fake = WifiInfoMirror.ctor?.newInstance() ?: return null

            // Find the first non-loopback IPv4 address on the host's interfaces.
            var foundIp: InetAddress? = null
            try {
                val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
                outer@ for (iface in interfaces) {
                    val ni = iface as NetworkInterface
                    val addresses = Collections.list(ni.inetAddresses)
                    for (addr in addresses) {
                        val inet = addr as InetAddress
                        if (!inet.isLoopbackAddress) {
                            if (IPV4_PATTERN.matcher(inet.hostAddress.uppercase()).matches()) {
                                foundIp = inet
                                break@outer
                            }
                            foundIp = null
                            break
                        }
                    }
                }
            } catch (_: SocketException) {
            }

            WifiInfoMirror.mNetworkId?.set(fake, 1)
            WifiInfoMirror.mSupplicantState?.set(fake, SupplicantState.COMPLETED)
            WifiInfoMirror.mBSSID?.set(fake, "66:55:44:33:22:11")
            WifiInfoMirror.mMacAddress?.set(fake, "11:22:33:44:55:66")
            WifiInfoMirror.mIpAddress?.set(fake, foundIp)
            WifiInfoMirror.mLinkSpeed?.set(fake, 65)
            WifiInfoMirror.mFrequency?.set(fake, 5000)
            WifiInfoMirror.mRssi?.set(fake, 200)

            // API 21+ has mWifiSsid (WifiSsid); pre-21 uses mSSID (String).
            val wifiSsidField = WifiInfoMirror.mWifiSsid
            if (wifiSsidField != null) {
                wifiSsidField.set(fake, WifiSsidMirror.createFromAsciiEncoded?.call("ims134"))
            } else {
                WifiInfoMirror.mSSID?.set(fake, "ims134")
            }
            return fake
        }
    }

    /**
     * Was `g`. `getScanResults` — empty list when fake location is enabled;
     * otherwise delegates to the real service, returning an empty list on failure.
     */
    inner class GetScanResultsProxy : ReplaceCallingPkgMethodProxy("getScanResults") {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            rewriteAttributionSourcesInBundles(args)
            if (MethodProxy.isFakeLocationEnable()) {
                return ArrayList<Any?>(0)
            }
            return try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                ArrayList<Any?>(0)
            }
        }
    }

    /**
     * Was `i`. Strips `WorkSource` args and rewrites `AttributionSource` args, then
     * delegates. Parent of [StartScanProxy].
     */
    open inner class WorkSourceStrippingProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            rewriteAttributionSourcesInBundles(args)
            if (args != null) {
                val idx = ArrayUtils.indexOf(WorkSource::class.java, args)
                if (idx >= 0) args[idx] = null
            }
            return super.call(who, method, args)
        }
    }

    /**
     * Was `j`. `startScan` — rewrites the String pair (package + calling package),
     * then delegates via [WorkSourceStrippingProxy]. Returns `false` on
     * `SecurityException` (unwrapped from `InvocationTargetException` chain).
     */
    inner class StartScanProxy : WorkSourceStrippingProxy("startScan") {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args != null) {
                val parameterTypes = method.parameterTypes
                for (i in parameterTypes.indices) {
                    if (parameterTypes[i] == String::class.java) {
                        args[i] = VirtualCore.gCore.hostPackageName
                        val next = i + 1
                        if (next >= parameterTypes.size ||
                            parameterTypes[next] != String::class.java
                        ) break
                        args[next] = null
                        break
                    }
                }
            }
            try {
                return super.call(who, method, args)
            } catch (th: Throwable) {
                var target: Throwable? = th
                while (target is InvocationTargetException) {
                    val inner = target.targetException ?: break
                    target = inner
                }
                if (target is SecurityException) {
                    return false
                }
                throw th
            }
        }
    }

    /**
     * Was `k`. Reflection-discovered proxy for any `WifiManager` method that takes
     * Stub `Bundle` argument (modern attribution-source APIs). Rewrites the
     * `EXTRA_PARAM_KEY_ATTRIBUTION_SOURCE` inside the bundle.
     */
    class BundleAttributionSourceProxy(name: String) : StaticMethodProxy(name) {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            rewriteAttributionSourcesInBundles(args)
            return super.beforeCall(who, method, args)
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(IsWifiEnabledProxy())
        addMethodProxy(PackageNameRewritingProxy("getDhcpInfo"))
        addMethodProxy(GetWifiEnabledStateProxy())
        addMethodProxy(CreateDhcpInfoProxy())
        addMethodProxy(GetWifiApConfigurationProxy())
        addMethodProxy(GetConnectionInfoProxy())
        addMethodProxy(GetScanResultsProxy())
        addMethodProxy(ReplaceCallingPkgMethodProxy("getBatchedScanResults"))
        addMethodProxy(WorkSourceStrippingProxy("acquireWifiLock"))
        addMethodProxy(WorkSourceStrippingProxy("updateWifiLockWorkSource"))
        addMethodProxy(WorkSourceStrippingProxy("startLocationRestrictedScan"))
        addMethodProxy(StartScanProxy())
        addMethodProxy(WorkSourceStrippingProxy("requestBatchedScan"))

        // Reflection pass: install BundleAttributionSourceProxy for any
        // Bundle-taking method that isn't already proxied.
        val base = invocationStub.baseInterface ?: return
        for (m in base.javaClass.methods) {
            for (paramType in m.parameterTypes) {
                if (!Bundle::class.java.isAssignableFrom(paramType)) continue
                if (invocationStub.getMethodProxy<MethodProxy>(m.name) != null) break
                addMethodProxy(BundleAttributionSourceProxy(m.name))
                break
            }
        }
    }

    companion object {
        private val IPV4_PATTERN = Pattern.compile(
            "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$"
        )

        /**
         * Was static helper `b(Object[])`. For every `Bundle` argument that
         * contains an `EXTRA_PARAM_KEY_ATTRIBUTION_SOURCE` value, rewrites the
         * source to use the host's uid and package name. No-op if
         * `AttributionSourceMirror.TYPE` is unavailable.
         */
        @JvmStatic
        fun rewriteAttributionSourcesInBundles(args: Array<Any?>?) {
            if (args == null) return
            val type = AttributionSourceMirror.TYPE ?: return
            val vc = VirtualCore.gCore
            for (i in args.indices) {
                val arg = args[i] as? Bundle ?: continue
                val source = arg.get("EXTRA_PARAM_KEY_ATTRIBUTION_SOURCE") ?: continue
                if (!type.isInstance(source)) continue
                val rewritten = AttributionSourceCompat.withPackage(
                    vc.uid, vc.hostPackageName, source,
                )
                if (rewritten is Parcelable) {
                    val copy = Bundle(arg)
                    copy.putParcelable("EXTRA_PARAM_KEY_ATTRIBUTION_SOURCE", rewritten)
                    args[i] = copy
                }
            }
        }
    }
}
package com.ims134.doclone.io



import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IInterface
import android.util.Log
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.Collections
import java.util.IdentityHashMap
import java.util.concurrent.atomic.AtomicInteger

@Suppress("UNCHECKED_CAST")
class ConnectivityStub : BinderInvocationProxy(
    IConnectivityManagerMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "connectivity",
) {

    /**
     * Was `a`. `getDefaultNetworkCapabilitiesForUser` — rewrites userId at index 0,
     * then applies the standard connectivity arg rewrite, then invokes with fallback.
     */
    inner class DefaultNetworkCapabilitiesProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                MethodParameterUtils.replaceUserIdArgAt(0, args!!)
                rewriteConnectivityArgs(method, args)
                method.invoke(who, *(args ?: emptyArray()))
            } catch (_: Exception) {
                null
            }
    }

    /**
     * Was `b`. `releaseNetworkRequest` — if the argument is Stub NetworkRequest that
     * we previously added to our fallback set, remove it and return null (pretend
     * the release succeeded, since the request was never real).
     */
    inner class ReleaseNetworkRequestProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (!args.isNullOrEmpty()) {
                val first = args[0]
                if (first is NetworkRequest && pendingRequests.remove(first)) {
                    return null
                }
            }
            return super.call(who, method, args)
        }
    }

    /** Was `c`. `requestNetwork` — arg rewrite + invoke with fallback. */
    inner class RequestNetworkProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            rewriteConnectivityArgs(method, args)
            return invokeWithFallback(who, method, args)
        }
    }

    /** Was `d`. `pendingRequestForNetwork` — arg rewrite + invoke, null on failure. */
    inner class PendingRequestForNetworkProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                rewriteConnectivityArgs(method, args)
                method.invoke(who, *(args ?: emptyArray()))
            } catch (_: Exception) {
                null
            }
    }

    /** Was `e`. `listenForNetwork` — arg rewrite + invoke with fallback. */
    inner class ListenForNetworkProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            rewriteConnectivityArgs(method, args)
            return invokeWithFallback(who, method, args)
        }
    }

    /** Was `f`. `pendingListenForNetwork` — arg rewrite + invoke, null on failure. */
    inner class PendingListenForNetworkProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                rewriteConnectivityArgs(method, args)
                method.invoke(who, *(args ?: emptyArray()))
            } catch (_: Exception) {
                null
            }
    }

    /** Was `g`. `registerConnectivityDiagnosticsCallback`. */
    inner class RegisterConnectivityDiagnosticsCallbackProxy(name: String) :
        StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                rewriteConnectivityArgs(method, args)
                method.invoke(who, *(args ?: emptyArray()))
            } catch (_: Exception) {
                null
            }
    }

    /** Was `h`. `getNetworkCapabilities`. */
    inner class GetNetworkCapabilitiesProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                rewriteConnectivityArgs(method, args)
                method.invoke(who, *(args ?: emptyArray()))
            } catch (_: Exception) {
                null
            }
    }

    /** Was `i`. `requestRouteToHostAddress`. */
    inner class RequestRouteToHostAddressProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                rewriteConnectivityArgs(method, args)
                method.invoke(who, *(args ?: emptyArray()))
            } catch (_: Exception) {
                null
            }
    }

    /** Was `j`. `checkMobileProvisioning` — always returns -1. */
    private class CheckMobileProvisioningProxy private constructor() : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            Log.d("ConnectivityPatch", "CheckMobileProvisioning hooked")
            return -1
        }
        override fun getMethodName(): String = "checkMobileProvisioning"

        companion object {
            @JvmStatic fun create() = CheckMobileProvisioningProxy()
        }
    }

    /** Was `k`. `isTetheringSupported` — always returns false. */
    private class IsTetheringSupportedProxy private constructor() : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            Log.d("ConnectivityPatch", "isTetheringSupported hooked")
            return false
        }
        override fun getMethodName(): String = "isTetheringSupported"

        companion object {
            @JvmStatic fun create() = IsTetheringSupportedProxy()
        }
    }

    /** Was `l`. `reportInetCondition` — always returns null. */
    private class ReportInetConditionProxy private constructor() : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            Log.d("ConnectivityPatch", "reportInetCondition hooked")
            return null
        }
        override fun getMethodName(): String = "reportInetCondition"

        companion object {
            @JvmStatic fun create() = ReportInetConditionProxy()
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(ReportInetConditionProxy.create())
        val sdkInt = Build.VERSION.SDK_INT
        addMethodProxy(CheckMobileProvisioningProxy.create())
        if (sdkInt >= 25) {
            addMethodProxy(IsTetheringSupportedProxy.create())
        }
        addMethodProxy(DefaultNetworkCapabilitiesProxy("getDefaultNetworkCapabilitiesForUser"))
        if (BuildCompat.sdk30OrLater()) {
            addMethodProxy(ReleaseNetworkRequestProxy("releaseNetworkRequest"))
            addMethodProxy(RequestNetworkProxy("requestNetwork"))
            addMethodProxy(PendingRequestForNetworkProxy("pendingRequestForNetwork"))
            addMethodProxy(ListenForNetworkProxy("listenForNetwork"))
            addMethodProxy(PendingListenForNetworkProxy("pendingListenForNetwork"))
            addMethodProxy(RegisterConnectivityDiagnosticsCallbackProxy("registerConnectivityDiagnosticsCallback"))
            addMethodProxy(GetNetworkCapabilitiesProxy("getNetworkCapabilities"))
            addMethodProxy(RequestRouteToHostAddressProxy("requestRouteToHostAddress"))
        }
    }

    companion object {
        /** Was field `c`. Counter used to build unique fallback network specifiers. */
        @JvmField
        val requestCounter: AtomicInteger = AtomicInteger()

        /**
         * Was field `d`. Identity-based set of fallback NetworkRequests we've
         * synthesized, so `releaseNetworkRequest` can recognize and drop them.
         */
        @JvmField
        val pendingRequests: MutableSet<NetworkRequest> =
            Collections.synchronizedSet(
                Collections.newSetFromMap(IdentityHashMap())
            )

        /**
         * Was static helper `b(Method, Object[])`. Walks the method's parameter
         * types and replaces the first `String` parameter with the host package
         * name; if the immediately following parameter is also `String`, nulls
         * it out (typically the "callingPackage" vs "packageName" pair that
         * ConnectivityService passes to AttributionSource-aware methods).
         */
        @JvmStatic
        fun rewriteConnectivityArgs(method: Method?, args: Array<Any?>?) {
            if (method == null || args == null) return
            val parameterTypes = method.parameterTypes
            val min = minOf(args.size, parameterTypes.size)
            for (i in 0 until min) {
                if (parameterTypes[i] == String::class.java) {
                    args[i] = VirtualCore.gCore.hostPackageName
                    val next = i + 1
                    if (next >= min || parameterTypes[next] != String::class.java) {
                        return
                    }
                    args[next] = null
                    return
                }
            }
        }

        /**
         * Was static helper `c(Object, Method, Object[])`. Invokes [method], but
         * when the target throws `TooManyRequestsException` (or the originally-
         * intended `ServiceSpecificException(errorCode==1)` case), synthesizes Stub
         * fallback Wi-Fi NetworkRequest and returns it.
         *
         * ⚠ NOTE ON DECOMPILED STRUCTURE: the original's `while (th != null && i2 < 4)`
         * loop's body contains the NetworkRequest construction **after** the
         * `if (name != "TooManyRequests")` block — which means, as decompiled,
         * the loop returns on its first iteration regardless of what happened
         * inside the if block. The `if (errorCode == 1) { }` is empty. That is
         * almost certainly Stub JADX artifact from Stub lost `continue`/`return`.
         *
         * If you rely on the "walk cause chain, find TooManyRequests anywhere,
         * then fall back" semantics, add `continue` after `th = cause` and add
         * `return buildFallbackRequest()` inside the empty errorCode==1 block.
         * Preserved literally here for fidelity; fix at will.
         */
        @JvmStatic
        @Throws(Throwable::class)
        fun invokeWithFallback(who: Any?, method: Method, args: Array<Any?>?): Any? {
            try {
                return method.invoke(who, *(args ?: emptyArray()))
            } catch (e: InvocationTargetException) {
                val targetException = e.targetException ?: throw e
                var i = 0
                var th: Throwable? = targetException
                while (th != null && i < 4) {
                    val name = th.javaClass.name
                    if (name != "android.net.ConnectivityManager\$TooManyRequestsException") {
                        if (name == "android.os.ServiceSpecificException") {
                            try {
                                if (th.javaClass.getField("errorCode").getInt(th) == 1) {
                                    // Empty in the decompiled source — see KDoc note.
                                }
                            } catch (_: Throwable) {
                                throw targetException
                            }
                        }
                        val cause = th.cause
                        if (cause === th) break
                        i++
                        th = cause
                    }
                    // Decompiled position: this runs at the end of every iteration,
                    // so the loop never actually iterates more than once.
                    val request = NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI) // TRANSPORT_WIFI
                        .setNetworkSpecifier(
                            "libmultiple-fallback-" +
                                    (requestCounter.incrementAndGet().toLong() and 0xFFFFFFFFL).toString()
                        )
                        .build()
                    pendingRequests.add(request)
                    return request
                }
                throw targetException
            }
        }
    }
}
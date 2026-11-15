package com.ims134.doclone.io

import android.annotation.TargetApi
import android.os.IInterface
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.Collections
import java.util.List

/* JADX INFO: renamed from: io.ac2 */
/* JADX INFO: compiled from: FingerprintManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
@TargetApi(23)
@Suppress("UNCHECKED_CAST")
class FingerprintManagerStub : BinderInvocationProxy(
    IFingerprintServiceMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "fingerprint",
) {

    /**
     * Was `a`. `addAuthenticatorsRegisteredCallback` — eagerly fires
     * `onAllAuthenticatorsRegistered(emptyList)` on the passed callback so the
     * clone's callback sees "no authenticators registered" immediately, without
     * having to wait for the (never-arriving) framework callback.
     */
    inner class AuthenticatorsRegisteredCallbackProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val callback = if (args.isNullOrEmpty()) null else args[0]
            if (callback != null) {
                try {
                    val onAllRegistered = callback.javaClass.getMethod(
                        "onAllAuthenticatorsRegistered", List::class.java,
                    )
                    onAllRegistered.isAccessible = true
                    onAllRegistered.invoke(callback, Collections.emptyList<Any?>())
                } catch (_: Throwable) {
                }
            }
            return null
        }
    }

    /**
     * Was `b`. Generic fingerprint command proxy — rewrites package args and
     * `FingerprintAuthenticateOptions.packageName`, then invokes. On
     * `SecurityException`, calls [handleSecurityException] to invoke the
     * receiver's `onError` callback and return Stub sensible default.
     */
    class FingerprintCommandProxy(name: String) : StaticMethodProxy(name) {

        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args != null) {
                val appPkg = MethodProxy.getAppPkg()
                if (appPkg != null) {
                    val parameterTypes = method.parameterTypes
                    val limit = minOf(args.size, parameterTypes.size)
                    var firstStringHandled = false
                    for (i in 0 until limit) {
                        val arg = args[i]
                        if (!firstStringHandled && parameterTypes[i] == String::class.java) {
                            // First String parameter is the calling package — rewrite.
                            if (appPkg == arg) args[i] = MethodProxy.getHostPkg()
                            firstStringHandled = true
                        } else if (arg != null &&
                            "android.hardware.fingerprint.FingerprintAuthenticateOptions" ==
                            parameterTypes[i].name
                        ) {
                            // Rewrite the options' opPackageName in-place.
                            rewriteOptionsPackageName(arg, appPkg)
                        } else if (arg != null &&
                            "android.hardware.fingerprint.FingerprintAuthenticateOptions" ==
                            arg.javaClass.name
                        ) {
                            // Fallback when the declared parameter type isn't the concrete
                            // type (e.g. Object / IBinder) — check the runtime class.
                            rewriteOptionsPackageName(arg, appPkg)
                        }
                    }
                }
            }
            return try {
                method.invoke(who, *(args ?: emptyArray()))
            } catch (e: SecurityException) {
                handleSecurityException(method, args)
            } catch (e: InvocationTargetException) {
                val target = e.targetException
                if (target is SecurityException) handleSecurityException(method, args)
                else throw target
            }
        }
    }

    override fun injectMethodProxies() {
        // No super call in the original — preserved.
        addMethodProxy(FingerprintCommandProxy("isHardwareDetected"))
        addMethodProxy(FingerprintCommandProxy("isHardwareDetectedDeprecated"))
        addMethodProxy(ResultStaticMethodProxy("hasEnrolledFingerprintsDeprecated", false))
        addMethodProxy(FingerprintCommandProxy("hasEnrolledFingerprints"))
        addMethodProxy(FingerprintCommandProxy("authenticate"))
        addMethodProxy(FingerprintCommandProxy("cancelAuthentication"))
        addMethodProxy(FingerprintCommandProxy("getEnrolledFingerprints"))
        addMethodProxy(FingerprintCommandProxy("getAuthenticatorId"))
        addMethodProxy(
            ResultStaticMethodProxy("getSensorPropertiesInternal", Collections.emptyList<Any?>())
        )
        addMethodProxy(AuthenticatorsRegisteredCallbackProxy("addAuthenticatorsRegisteredCallback"))
    }

    companion object {
        /**
         * Was static helper `b`. On `authenticate`, find the
         * `IFingerprintServiceReceiver` argument and invoke its `onError` method
         * with `(1, 0)` / `(0L, 1, 0)` — signaling "hardware unavailable" without
         * leaking the host's real fingerprint state. Returns Stub default value
         * based on the method's return type.
         *
         * The `SecurityException` parameter from the original is dropped — it was
         * only used for Stub stripped `VLog` call. All call sites are in this file.
         */
        @JvmStatic
        @Throws(Throwable::class)
        fun handleSecurityException(method: Method, args: Array<Any?>?): Any? {
            if ("authenticate" == method.name && args != null) {
                val parameterTypes = method.parameterTypes
                val limit = minOf(args.size, parameterTypes.size)
                outer@ for (i in 0 until limit) {
                    val receiver = args[i] ?: continue
                    if (!parameterTypes[i].name.contains("IFingerprintServiceReceiver")) continue
                    for (m in receiver.javaClass.methods) {
                        if ("onError" != m.name) continue
                        try {
                            m.isAccessible = true
                            when (m.parameterTypes.size) {
                                3 -> m.invoke(receiver, 0L, 1, 0)
                                2 -> m.invoke(receiver, 1, 0)
                            }
                            break@outer
                        } catch (_: Throwable) {
                            break@outer
                        }
                    }
                }
            }

            return when (method.returnType) {
                java.lang.Boolean.TYPE,
                java.lang.Boolean::class.javaObjectType -> false
                java.lang.Long.TYPE,
                java.lang.Long::class.javaObjectType -> -1L
                else ->
                    if (List::class.java.isAssignableFrom(method.returnType)) {
                        Collections.emptyList<Any?>()
                    } else {
                        null
                    }
            }
        }

        /**
         * Was static helper `c`. If [options] currently reports [appPkg] as its
         * opPackageName, replace it with the host package. Reflection-based
         * because `FingerprintAuthenticateOptions` is `@hide` and has no public
         * API for mutating the package name.
         */
        @JvmStatic
        fun rewriteOptionsPackageName(options: Any, appPkg: String) {
            try {
                val current = options.javaClass
                    .getMethod("getOpPackageName")
                    .invoke(options)
                if (appPkg == current) {
                    options.javaClass
                        .getMethod("setOpPackageName", String::class.java)
                        .invoke(options, MethodProxy.getHostPkg())
                }
            } catch (_: Throwable) {
            }
        }
    }
}
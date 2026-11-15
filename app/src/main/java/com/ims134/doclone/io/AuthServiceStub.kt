package com.ims134.doclone.io



import android.os.Build
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VirtualCore
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.Collections

class AuthServiceStub : BinderInvocationProxy(
    authServiceStubClass(),
    authServiceName(),
) {

    /**
     * Was `a`/`b`/`c`/`d`/`e` — five identical `StaticMethodProxy` subclasses that
     * all returned null. Merged into one class since their bodies were byte-for-byte
     * identical. Used for `register*Listener` / `unregister*Listener` methods where
     * silently returning null is the desired behavior.
     */
    inner class ReturnNullProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? = null
    }

    /**
     * Was `f`. The main biometric-auth proxy. Each instance is configured with
     * three indices describing where to find relevant arguments in the parameter
     * list:
     *  - [appPkgIndex]: index of the String package argument (-1 = none)
     *  - [userIdIndex]: index of the Int userId argument (-1 = none)
     *  - [callbackIndex]: index of the callback object to notify on security
     *    exception (-1 = none)
     */
    class AuthProxy(
        name: String,
        private val appPkgIndex: Int,
        private val userIdIndex: Int,
        private val callbackIndex: Int,
    ) : StaticMethodProxy(name) {

        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            // Replace the virtual app's package name with the host's at the known index.
            if (args != null && appPkgIndex in args.indices) {
                val appPkg = MethodProxy.getAppPkg()
                if (appPkg != null && appPkg == args[appPkgIndex]) {
                    args[appPkgIndex] = MethodProxy.getHostPkg()
                }
            }
            // Replace the virtual user id with the host's at the known index.
            if (args != null && userIdIndex in args.indices && args[userIdIndex] is Int) {
                args[userIdIndex] = VirtualCore.gCore.getHostUserId()
            }

            return try {
                method.invoke(who, *(args ?: emptyArray()))
            } catch (e: SecurityException) {
                handleSecurityException(method, args, callbackIndex, e)
            } catch (e: InvocationTargetException) {
                val target = e.targetException
                if (target is SecurityException) {
                    handleSecurityException(method, args, callbackIndex, target)
                } else {
                    throw target
                }
            }
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(AuthProxy("authenticate", 4, 2, 3))
        addMethodProxy(AuthProxy("cancelAuthentication", 1, -1, -1))
        addMethodProxy(AuthProxy("canAuthenticate", 0, 1, -1))
        addMethodProxy(AuthProxy("getLastAuthenticationTime", -1, 0, -1))
        addMethodProxy(AuthProxy("getButtonLabel", 1, 0, -1))
        addMethodProxy(AuthProxy("getPromptMessage", 1, 0, -1))
        addMethodProxy(AuthProxy("getSettingName", 1, 0, -1))
        addMethodProxy(AuthProxy("getAuthenticatorIds", -1, 0, -1))
        addMethodProxy(AuthProxy("hasEnrolledBiometrics", 1, 0, -1))
        addMethodProxy(AuthProxy("getEnrollmentStatusList", 1, 0, -1))
        addMethodProxy(AuthProxy("getBiometricSensorStrengths", 0, -1, -1))
        addMethodProxy(AuthProxy("getSensorProperties", 0, -1, -1))

        addMethodProxy(ReturnNullProxy("registerEnabledOnKeyguardCallback"))
        addMethodProxy(ReturnNullProxy("registerAuthenticationStateListener"))
        addMethodProxy(ReturnNullProxy("unregisterAuthenticationStateListener"))
        addMethodProxy(ReturnNullProxy("registerIdentityCheckStateListener"))
        addMethodProxy(ReturnNullProxy("unregisterIdentityCheckStateListener"))
    }

    companion object {
        /** Was the constructor's `Class.forName(...)` block. */
        private fun authServiceStubClass(): Class<*>? = try {
            Class.forName(
                if (Build.VERSION.SDK_INT >= 30) {
                    "android.hardware.biometrics.IAuthService\$Stub"
                } else {
                    "android.hardware.biometrics.IBiometricService\$Stub"
                }
            )
        } catch (_: Throwable) {
            null
        }

        /** Was the constructor's service-name ternary. */
        private fun authServiceName(): String =
            if (Build.VERSION.SDK_INT >= 30) "auth" else "biometric"

        /**
         * Was static helper `b(Method, Object[], int, SecurityException)`. When
         * the underlying service throws `SecurityException` on `authenticate`,
         * proactively notify the caller's callback with `onError(5, ...)` and
         * return Stub sensible default value based on the method's return type.
         *
         * The `method.getName() == "authenticate"` guard ensures we don't
         * accidentally fire `onError` for unrelated methods that happen to
         * throw — the callback argument at [callbackIndex] only exists on
         * `authenticate`.
         */
        @JvmStatic
        fun handleSecurityException(
            method: Method,
            args: Array<Any?>?,
            callbackIndex: Int,
            exception: SecurityException,
        ): Any? {
            // (The two discarded statements in the decompiled source —
            // `method.getName();` and `Objects.toString(securityException);` —
            // were stripped log calls. Omitted.)

            if ("authenticate" == method.name &&
                args != null && callbackIndex in args.indices
            ) {
                val callback = args[callbackIndex]
                if (callback != null) {
                    for (m in callback.javaClass.methods) {
                        if ("onError" != m.name) continue
                        try {
                            m.isAccessible = true
                            val types = m.parameterTypes
                            if (types.size == 3) {
                                m.invoke(callback, 0, 5, 0)
                            } else if (types.size == 2 && types[1] == String::class.java) {
                                m.invoke(callback, 5, "Biometric authentication unavailable")
                            }
                            break
                        } catch (_: Throwable) {
                            // Original: `th.toString();` (stripped log). Loop continues.
                        }
                    }
                }
            }

            return when (method.returnType) {
                Void.TYPE -> null
                java.lang.Boolean.TYPE, java.lang.Boolean::class.javaObjectType -> false
                java.lang.Integer.TYPE, java.lang.Integer::class.javaObjectType -> 1
                java.lang.Long.TYPE, java.lang.Long::class.javaObjectType -> -1L
                LongArray::class.java -> LongArray(0)
                else -> if (List::class.java.isAssignableFrom(method.returnType)) {
                    Collections.emptyList<Any?>()
                } else {
                    null
                }
            }
        }
    }
}
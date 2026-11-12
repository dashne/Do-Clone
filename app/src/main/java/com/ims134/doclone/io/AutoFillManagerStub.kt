package com.ims134.doclone.io

import android.os.Bundle
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

// smali io.xw
class AutoFillManagerStub : BinderInvocationProxy(IAutoFillManager.Stub.asInterface, "autofill") {

    // smali get(): the base proxy first, then one hook per method name
    override fun injectMethodProxies() {
        super.injectMethodProxies()
        for (name in METHODS_WITH_RECEIVER) addMethodProxy(AutoFillMethodProxy(name, true))   // 14 names
        for (name in METHODS_FAKE_ONLY) addMethodProxy(AutoFillMethodProxy(name, false))      // 19 names
    }

    // smali xw$get (jadx left it as "get"). It disables autofill for guests by faking every reply.
    class AutoFillMethodProxy(
        methodName: String,
        // smali d
        private val replyToReceiver: Boolean,
    ) : StaticMethodProxy(methodName) {

        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val name = method.name

            if (replyToReceiver) {
                // Only methods that take an IResultReceiver parameter get this treatment
                if (method.parameterTypes.any { it.name == RESULT_RECEIVER }) {
                    val code = if (isSessionStart(name)) Int.MAX_VALUE else 0
                    // Answered the receiver: skip the real call and return get default value
                    if (sendResult(method, args, code)) return defaultValue(method, name)
                    // Could not answer it (no non-null receiver, or send failed): call the real method
                    return method.invoke(who, *(args ?: emptyArray()))
                }
            }
            return defaultValue(method, name)
        }

        // Tries to send (code, null) to the last non-null IResultReceiver argument. true = sent.
        private fun sendResult(method: Method, args: Array<Any?>?, code: Int): Boolean {
            val types = method.parameterTypes
            var i = minOf(types.size, args!!.size) - 1
            while (i >= 0) {
                val receiver = args[i]
                if (types[i].name == RESULT_RECEIVER && receiver != null) {
                    return try {
                        receiver.javaClass
                            .getMethod("send", Int::class.javaPrimitiveType, Bundle::class.java)
                            .invoke(receiver, code, null)
                        true
                    } catch (e: InvocationTargetException) {
                        // smali logs cause-or-self via toString(), stripped here
                        false
                    } catch (t: Throwable) {
                        false
                    }
                }
                i--
            }
            return false
        }

        private fun defaultValue(method: Method, name: String): Any? {
            val type = method.returnType
            if (type == Void.TYPE) return null
            return when (type) {
                java.lang.Boolean.TYPE -> false
                Integer.TYPE -> if (isSessionStart(name)) Int.MAX_VALUE else 0
                java.lang.Long.TYPE -> 0L
                java.lang.Float.TYPE -> 0.0f
                java.lang.Double.TYPE -> 0.0
                java.lang.Byte.TYPE -> 0.toByte()
                java.lang.Short.TYPE -> 0.toShort()
                Character.TYPE -> '\u0000'
                else -> null
            }
        }

        private fun isSessionStart(name: String) =
            name == "startSession" || name == "updateOrRestartSession"

        private companion object {
            const val RESULT_RECEIVER = "com.android.internal.os.IResultReceiver"
        }
    }

    companion object {
        // smali c
        @JvmField
        val METHODS_WITH_RECEIVER = arrayOf(
            "addClient", "startSession", "restoreSession", "getFillEventHistory",
            "isServiceSupported", "isServiceEnabled", "getUserData", "getUserDataId",
            "isFieldClassificationEnabled", "getAutofillServiceComponentName",
            "getAvailableFieldClassificationAlgorithms", "getDefaultFieldClassificationAlgorithm",
            "setAugmentedAutofillWhitelist", "getNoiseInjectionMasterSeed",
        )

        // smali d
        @JvmField
        val METHODS_FAKE_ONLY = arrayOf(
            "removeClient", "updateSession", "updateOrRestartSession", "setAutofillFailure",
            "setAutofillIdsAttemptedForRefill", "setViewAutofilled", "finishSession", "cancelSession",
            "setAuthenticationResult", "setHasCallback", "disableOwnedAutofillServices",
            "onPendingSaveUi", "setUserData", "notifyImeAnimationStart", "notifyImeAnimationEnd",
            "notifyNotExpiringResponseDuringAuth", "notifyViewEnteredIgnoredDuringAuthCount",
            "notifySystemInlineSuggestions", "autofillRemoteApp",
        )
    }
}
package com.ims134.doclone.io



import android.os.IInterface
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VirtualCore
import java.lang.reflect.Array
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.Collections

class NfcAdapterStub : BinderInvocationProxy(nfcAdapterStubClass(), "nfc") {

    /**
     * Was `Stub`. Invokes the real service; if the result is an `IInterface`,
     * wraps it in Stub [NfcIfaceStub] whose methods return default values
     * (via [ReturnDefaultProxy]) — so callers can't observe real NFC state
     * through the wrapped interface.
     *
     * [className] is the framework interface class (e.g. `INfcCardEmulation`)
     * used to reflectively discover which methods to wrap.
     */
    class WrapReturnedInterfaceProxy(
        name: String,
        private val className: String,
    ) : StaticMethodProxy(name) {

        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: kotlin.Array<Any?>?): Any? {
            try {
                val result = method.invoke(who, *(args ?: emptyArray()))
                if (result !is IInterface) return result

                val cls: Class<*>? = try {
                    Class.forName(className)
                } catch (_: Throwable) {
                    null
                }
                if (cls == null) return result

                val stub = NfcIfaceStub(result)
                for (m in cls.methods) {
                    val methodName = m.name
                    if ("asBinder" != methodName &&
                        stub.getMethodProxy<MethodProxy>(methodName) == null
                    ) {
                        stub.addMethodProxy(ReturnDefaultProxy(methodName))
                    }
                }
                return stub.proxyInterface
            } catch (e: InvocationTargetException) {
                throw e.targetException
            }
        }
    }

    /**
     * Was `b`. Rewrites the package-name String arg to the host package, rewrites
     * the userId Int arg at [userIdIndex] (or skips when -1), then invokes.
     * Returns Stub default value on `SecurityException` (typically from missing
     * NFC permission on the host).
     */
    class RewritePackageAndUserIdProxy(
        name: String,
        private val userIdIndex: Int,
    ) : StaticMethodProxy(name) {

        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: kotlin.Array<Any?>?): Any? {
            if (args != null) {
                val appPkg = MethodProxy.getAppPkg()
                if (appPkg != null) {
                    val parameterTypes = method.parameterTypes
                    val limit = minOf(args.size, parameterTypes.size)
                    for (i in 0 until limit) {
                        if (parameterTypes[i] == String::class.java && appPkg == args[i]) {
                            args[i] = MethodProxy.getHostPkg()
                            break
                        }
                    }
                }
                if (userIdIndex in args.indices && args[userIdIndex] is Int) {
                    args[userIdIndex] = VirtualCore.gCore.getHostUserId()
                }
            }

            return try {
                method.invoke(who, *(args ?: emptyArray()))
            } catch (e: SecurityException) {
                handleSecurityException(method)
            } catch (e: InvocationTargetException) {
                val target = e.targetException
                if (target is SecurityException) {
                    handleSecurityException(method)
                } else {
                    throw target
                }
            }
        }
    }

    /** Was `c`. Returns the zero-default for the intercepted method's return type. */
    class ReturnDefaultProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: kotlin.Array<Any?>?): Any? =
            defaultValueForType(method.returnType)
    }

    /**
     * Was `d`. Empty [BinderInvocationStub] subclass — wraps an `IInterface` so
     * its methods can be individually intercepted.
     */
    class NfcIfaceStub(iface: IInterface) : BinderInvocationStub(iface)

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        // Modern API 30+ methods (no meaningful userId arg → -1)
        addMethodProxy(RewritePackageAndUserIdProxy("setObserveMode", -1))
        addMethodProxy(RewritePackageAndUserIdProxy("setReaderMode", -1))
        addMethodProxy(RewritePackageAndUserIdProxy("updateDiscoveryTechnology", -1))
        addMethodProxy(RewritePackageAndUserIdProxy("enable", -1))
        addMethodProxy(RewritePackageAndUserIdProxy("disable", -1))
        addMethodProxy(RewritePackageAndUserIdProxy("enableReaderOption", -1))
        addMethodProxy(RewritePackageAndUserIdProxy("indicateDataMigration", -1))

        // userId at index 1
        addMethodProxy(RewritePackageAndUserIdProxy("isTagIntentAllowed", 1))

        // Fixed-result proxies
        addMethodProxy(ResultStaticMethodProxy("setTagIntentAppPreferenceForUser", -1))
        addMethodProxy(
            ResultStaticMethodProxy(
                "getTagIntentAppPreferenceForUser",
                Collections.emptyMap<Any?, Any?>(),
            )
        )

        // Subservice getters — return null / default on missing permissions
        addMethodProxy(RewritePackageAndUserIdProxy("getNfcDtaInterface", -1))
        addMethodProxy(RewritePackageAndUserIdProxy("getNfcAdapterExtrasInterface", -1))

        // Interface-returning getters — wrap returned interface
        addMethodProxy(
            WrapReturnedInterfaceProxy(
                "getNfcCardEmulationInterface",
                "android.nfc.INfcCardEmulation",
            )
        )
        addMethodProxy(
            WrapReturnedInterfaceProxy(
                "getNfcFCardEmulationInterface",
                "android.nfc.INfcFCardEmulation",
            )
        )
    }

    @Throws(Throwable::class)
    override fun inject() {
        super.inject()
        val proxyInterface = invocationStub.proxyInterface ?: return
        setStaticField("android.nfc.NfcAdapter", "sService", proxyInterface)
        installWrappedService(
            proxyInterface,
            "getNfcCardEmulationInterface",
            "sCardEmulationService",
            "android.nfc.cardemulation.CardEmulation",
        )
        installWrappedService(
            proxyInterface,
            "getNfcFCardEmulationInterface",
            "sNfcFCardEmulationService",
            "android.nfc.cardemulation.NfcFCardEmulation",
        )
    }

    companion object {
        /** Was the constructor's `Class.forName(...)` block. */
        private fun nfcAdapterStubClass(): Class<*>? = try {
            Class.forName("android.nfc.INfcAdapter\$Stub")
        } catch (_: Throwable) {
            null
        }

        /**
         * Was static helper `c`. Zero/default for the given return type. Used by
         * [ReturnDefaultProxy] when wrapping an interface returned by the real service.
         */
        @JvmStatic
        fun defaultValueForType(cls: Class<*>): Any? = when {
            cls == Void.TYPE -> null

            cls == java.lang.Boolean.TYPE || cls == java.lang.Boolean::class.javaObjectType -> false
            cls == java.lang.Byte.TYPE || cls == java.lang.Byte::class.javaObjectType -> 0.toByte()
            cls == java.lang.Short.TYPE || cls == java.lang.Short::class.javaObjectType -> 0.toShort()
            cls == java.lang.Integer.TYPE || cls == java.lang.Integer::class.javaObjectType -> 0
            cls == java.lang.Long.TYPE || cls == java.lang.Long::class.javaObjectType -> 0L
            cls == java.lang.Float.TYPE || cls == java.lang.Float::class.javaObjectType -> 0.0f
            cls == java.lang.Double.TYPE || cls == java.lang.Double::class.javaObjectType -> 0.0
            cls == java.lang.Character.TYPE || cls == java.lang.Character::class.javaObjectType -> '\u0000'

            cls.isArray -> Array.newInstance(cls.componentType, 0)

            List::class.java.isAssignableFrom(cls) -> Collections.emptyList<Any?>()
            Map::class.java.isAssignableFrom(cls) -> Collections.emptyMap<Any?, Any?>()

            else -> null
        }

        /**
         * Was static helper `b`. Fallback for `SecurityException` — Boolean → false,
         * Integer → **-1** (not 0), Map → empty map, everything else → null.
         */
        @JvmStatic
        fun handleSecurityException(method: Method): Any? {
            val rt = method.returnType
            return when {
                rt == java.lang.Boolean.TYPE || rt == java.lang.Boolean::class.javaObjectType ->
                    false
                rt == java.lang.Integer.TYPE || rt == java.lang.Integer::class.javaObjectType ->
                    -1
                Map::class.java.isAssignableFrom(rt) -> Collections.emptyMap<Any?, Any?>()
                else -> null
            }
        }

        /**
         * Was static helper `c(Object, String, String, String)`. Calls [getterName]
         * on [obj], then reflects the result into two static fields:
         *   - `android.nfc.NfcAdapter.[adapterFieldName]`
         *   - `[emulationClassName].sService`
         */
        @JvmStatic
        fun installWrappedService(
            obj: Any,
            getterName: String,
            adapterFieldName: String,
            emulationClassName: String,
        ) {
            try {
                val service = obj.javaClass.getMethod(getterName).invoke(obj)
                if (service != null) {
                    setStaticField("android.nfc.NfcAdapter", adapterFieldName, service)
                    setStaticField(emulationClassName, "sService", service)
                }
            } catch (_: Throwable) {
            }
        }

        /**
         * Was static helper `d`. Reflectively sets Stub static field to [value] if
         * (and only if) the field is static and its declared type is assignable
         * from [value]'s class.
         */
        @JvmStatic
        fun setStaticField(className: String, fieldName: String, value: Any) {
            try {
                val field = Class.forName(className).getDeclaredField(fieldName)
                if (Modifier.isStatic(field.modifiers) && field.type.isInstance(value)) {
                    field.isAccessible = true
                    field.set(null, value)
                }
            } catch (_: Throwable) {
            }
        }
    }
}
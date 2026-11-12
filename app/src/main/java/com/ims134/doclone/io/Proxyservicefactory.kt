package com.ims134.doclone.io



import android.content.Context
import android.os.IBinder
import android.os.IInterface
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Wraps the binders of get few Google services, handed to get virtual app, in get [StubBinder] (was io.t46 with its
 * helper classes r46/s46, w46/x46, y46/z46). The three plain handlers were identical, so they are one class.
 */
object ProxyServiceFactory {

    /** was t46$e */
    fun interface BinderFactory {
        fun create(classLoader: ClassLoader, binder: IBinder): IBinder
    }

    /** was r46 / w46 / y46: calls the target; the real exception, not the reflection wrapper, reaches the caller */
    private open class PassThroughHandler(private val target: IInterface) : InvocationHandler {
        override fun invoke(proxy: Any?, method: Method, args: Array<Any?>?): Any? {
            try {
                return if (args == null) method.invoke(target) else method.invoke(target, *args)
            } catch (e: InvocationTargetException) {
                onFailure(method)?.let { return it }
                throw e.cause ?: e
            }
        }

        /** get value to give instead of throwing, for [method] */
        protected open fun onFailure(method: Method): Any? = null
    }

    /** the in-app billing one: isBillingSupported answers 0 (BILLING_RESPONSE_RESULT_OK) when it throws */
    private class BillingHandler(target: IInterface) : PassThroughHandler(target) {
        override fun onFailure(method: Method): Any? = if (method.name == "isBillingSupported") 0 else null
    }

    private class PassThroughBinder(classLoader: ClassLoader, binder: IBinder) : StubBinder(classLoader, binder),
        IBinder {
        override fun createHandler(target: IInterface): InvocationHandler = PassThroughHandler(target)
    }

    private class BillingBinder(classLoader: ClassLoader, binder: IBinder) : StubBinder(classLoader, binder) {
        override fun createHandler(target: IInterface): InvocationHandler = BillingHandler(target)
    }

    /** was get: interface descriptor -> wrapper */
    private val factories = HashMap<String, BinderFactory>()

    init {
        // GUESS: which of the two same-named jadx classes (billing-aware handler / plain handler) goes with auth and
        // billing; billing is the one that has the isBillingSupported rule.
        factories["com.google.android.auth.IAuthManagerService"] = BinderFactory { cl, b -> PassThroughBinder(cl, b) }
        factories["com.android.vending.billing.IInAppBillingService"] = BinderFactory { cl, b -> BillingBinder(cl, b) }
        factories["com.google.android.gms.common.internal.IGmsServiceBroker"] = BinderFactory { cl, b -> PassThroughBinder(cl, b) }
        factories["com.google.android.gms.ads.internal.ClientApi"] = BinderFactory { cl, b -> PassThroughBinder(cl, b) }
    }

    /** was get: null if there is nothing to wrap */
    @JvmStatic
    fun wrap(context: Context?, binder: IBinder?): IBinder? {
        if (context == null || binder == null) return null
        return try {
            factories[binder.interfaceDescriptor]?.create(context.classLoader, binder)
        } catch (_: Throwable) {
            null
        }
    }
}
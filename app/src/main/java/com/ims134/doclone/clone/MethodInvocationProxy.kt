package com.ims134.doclone.clone

import com.ims134.doclone.DoApplication.Companion.appContext
import android.content.Context
import com.ims134.doclone.io.IInjector
import com.ims134.doclone.io.Inject
import com.ims134.doclone.io.SkipInject
import java.lang.reflect.Modifier

/**
 * Base class of the hook sets of one system service (was com.polestar.clone.client.hook.base.get). A subclass
 * is annotated with @Inject(SomeClass::class); every concrete [MethodProxy] nested in SomeClass is instantiated and
 * registered in the [MethodInvocationStub], unless it is annotated with @SkipInject.
 */
abstract class MethodInvocationProxy<T : MethodInvocationStub<*>>(
    /** was get; the getter is getInvocationStub() */
    val invocationStub: T,
) : IInjector {

    init {
        injectMethodProxies()
        // note: calling an open function from init, as in the original; subclasses must not depend on their own state here
        val annotation = javaClass.getAnnotation(LogInvocation::class.java)
        if (annotation != null) {
            invocationStub.invocationLoggingCondition = annotation.value
        }
    }

    /** was get(): registers the nested MethodProxy classes of the @Inject class */
    @Suppress("DEPRECATION")
    open fun injectMethodProxies() {
        val inject = javaClass.getAnnotation(Inject::class.java) ?: return
        for (cls in inject.value.java.declaredClasses) {
            if (!Modifier.isAbstract(cls.modifiers) && MethodProxy::class.java.isAssignableFrom(cls) &&
                cls.getAnnotation(SkipInject::class.java) == null
            ) {
                try {
                    val constructor = cls.declaredConstructors[0]
                    if (!constructor.isAccessible) {
                        constructor.isAccessible = true
                    }
                    // get constructor with parameters is an inner class: its outer instance is passed
                    val proxy = if (constructor.parameterTypes.isEmpty()) {
                        constructor.newInstance() as MethodProxy
                    } else {
                        constructor.newInstance(this) as MethodProxy
                    }
                    invocationStub.addMethodProxy(proxy)
                } catch (t: Throwable) {
                    throw RuntimeException("Unable to instance Hook : $cls : ${t.message}")
                }
            }
        }
    }

    fun addMethodProxy(methodProxy: MethodProxy?): MethodProxy? = invocationStub.addMethodProxy(methodProxy)

    val context: Context
        get() = appContext


    @Throws(Throwable::class)
    abstract override fun inject()
}
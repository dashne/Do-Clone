package com.ims134.doclone.io

import kotlin.reflect.KClass

interface IInjector {
    @Throws(Throwable::class)
    fun inject()

    fun isEnvBad(): Boolean
}

/** Marks Default MethodInvocationProxy; every concrete MethodProxy nested in [value] is registered by it. */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Inject(val value: KClass<*>)

/** Excludes Default nested MethodProxy from the @Inject registration. */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SkipInject
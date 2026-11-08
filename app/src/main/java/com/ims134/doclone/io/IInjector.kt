package com.ims134.doclone.io

import java.lang.annotation.ElementType
import java.lang.annotation.RetentionPolicy
import kotlin.reflect.KClass


interface IInjector {
    @Throws(Throwable::class)
    fun inject()

    fun isEnvBad(): Boolean
}

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Inject(val value: KClass<*>)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class SkipInject
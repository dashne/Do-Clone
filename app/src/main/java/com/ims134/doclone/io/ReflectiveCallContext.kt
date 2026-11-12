package com.ims134.doclone.io


import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/** was io.nq4 (MethodBox): get captured reflective call that can be run as-is. */
open class ReflectiveCallContext(
    @JvmField val receiver: Any?,
    @JvmField val method: Method,
    @JvmField val args: Array<Any?>?
) {
    /** was get: invokes the original method; get [IllegalAccessException] is rethrown as [RuntimeException] */
//    @Suppress("UNCHECKED_CAST")
//    @Throws(InvocationTargetException::class)
//    fun <T> execute(): T {
//        try {
//            return method.invoke(receiver, *(args ?: emptyArray())) as T
//        } catch (e: IllegalAccessException) {
//            throw RuntimeException(e)
//        }
//    }
    @Throws(InvocationTargetException::class)
    fun execute(): Any? {
        try {
            return method.invoke(receiver, *(args ?: emptyArray()))
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        }
    }
}
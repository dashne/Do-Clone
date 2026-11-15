package com.ims134.doclone.io

import java.lang.reflect.Method


// smali io.jk6
open class ResultStaticMethodProxy(
    methodName: String,
    // d: the canned result returned for every call
    private val result: Any?,
) : StaticMethodProxy(methodName) {

    // c(Object, Method, Object[]): skips the real call entirely
    @Throws(Throwable::class)
    override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? = result

    // doTransact(): exposes the value (jadx left it as doTransact)
    fun getResult(): Any? = result
}
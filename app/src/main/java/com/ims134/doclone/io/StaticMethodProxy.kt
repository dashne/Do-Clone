package com.ims134.doclone.io

import com.ims134.doclone.clone.MethodProxy

// smali io.en7
open class StaticMethodProxy(
    // c
    private val methodName: String,
) : MethodProxy() {

    // smali k(). In my BinderInvocationStub port this is MethodProxy.getMethodName()
    override fun getMethodName(): String = methodName
}
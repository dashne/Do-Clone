package com.ims134.doclone.io

import com.ims134.doclone.clone.MethodProxy
import java.lang.reflect.Method

open class ReplaceSpecPkgMethodProxy(
    methodName: String,
    private val argIndex: Int,
) : StaticMethodProxy(methodName) {

    override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
        if (args != null) {
            var index = argIndex
            if (index < 0) index += args.size
            if (index >= 0 && index < args.size && args[index] is String) {
                args[index] = MethodProxy.getHostPkg()      // jadx: MethodProxy.i()
            }
        }
        return super.beforeCall(who, method, args)
    }
}
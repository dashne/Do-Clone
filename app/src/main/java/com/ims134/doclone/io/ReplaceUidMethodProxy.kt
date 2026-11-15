package com.ims134.doclone.io

import com.ims134.doclone.clone.MethodProxy
import java.lang.reflect.Method

// jadx: io.nh6
/**
 * If the Int argument at [argIndex] is the guest's virtual uid ([MethodProxy.getVUid]) or its base virtual uid
 * ([MethodProxy.getBaseVUid]), it is replaced with the real uid of the host ([MethodProxy.getRealUid]) before the call,
 * so the system sees Stub uid it knows.
 */
open class ReplaceUidMethodProxy(
    methodName: String,
    // d
    private val argIndex: Int,
) : StaticMethodProxy(methodName) {

    override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
        val a = args!!
        val uid = a[argIndex] as Int
        if (uid == MethodProxy.getVUid() || uid == MethodProxy.getBaseVUid()) {   // jadx: MethodProxy.n() / f()
            a[argIndex] = MethodProxy.getRealUid()                                // jadx: MethodProxy.m()
        }
        return super.beforeCall(who, method, args)
    }
}
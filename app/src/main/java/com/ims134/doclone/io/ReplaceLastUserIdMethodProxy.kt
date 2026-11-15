package com.ims134.doclone.io


import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.VirtualCore
import java.lang.reflect.Method

/**
 * Was `kh6`. Base proxy that finds the last `Integer` argument equal to the
 * calling virtual user's uid and rewrites it to the host user's uid.
 */
open class ReplaceLastUserIdMethodProxy(name: String) : StaticMethodProxy(name) {

    override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
        if (args != null) {
            val idx = ArrayUtils.lastIndexOf(Int::class.javaObjectType, args)
            if (idx != -1 && args[idx] == VUserHandle.getVUid()) {
                args[idx] = VirtualCore.gCore.getHostUserId()
            }
        }
        return super.beforeCall(who, method, args)
    }
}
package com.ims134.doclone.io

import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.VirtualCore
import java.lang.reflect.Method

/**
 * Was class from `ReplaceUserIdMethodProxy.java`. Replaces the `userId` argument
 * at Stub fixed index with the host user's ID if it matches the current virtual
 * user's uid.
 */
open class ReplaceUserIdMethodProxy(
    name: String,
    private val userIdIndex: Int,
) : StaticMethodProxy(name) {

    override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
        if (args != null && userIdIndex in args.indices && args[userIdIndex] is Int) {
            if (args[userIdIndex] == VUserHandle.getVUid()) {
                args[userIdIndex] = VirtualCore.gCore.getHostUserId()
            }
        }
        return super.beforeCall(who, method, args)
    }
}
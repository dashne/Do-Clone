package com.ims134.doclone.io


import com.ims134.doclone.clone.VirtualCore
import java.lang.reflect.Method

class AccessibilityManagerStub : BinderInvocationProxy(
    IAccessibilityManager.Stub.TYPE,
    "accessibility",
) {

    /**
     * Was inner class `a`. Replaces the trailing `Integer` argument with the host
     * user-id, if the last argument is an `Integer`. Used by every accessibility
     * proxy added in [injectMethodProxies].
     */
    inner class ReplaceTrailingUserIdProxy(name: String) : StaticMethodProxy(name) {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            @Suppress("UNCHECKED_CAST")
            val arr = args as Array<Any?>
            val last = arr.size - 1
            if (last >= 0 && arr[last] is Int) {
                arr[last] = VirtualCore.gCore.getHostUserId()
            }
            return super.beforeCall(who, method, arr)
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ReplaceTrailingUserIdProxy("addClient"))
        addMethodProxy(ReplaceTrailingUserIdProxy("removeClient"))
        addMethodProxy(ReplaceTrailingUserIdProxy("sendAccessibilityEvent"))
        addMethodProxy(ReplaceTrailingUserIdProxy("getInstalledAccessibilityServiceList"))
        addMethodProxy(ReplaceTrailingUserIdProxy("getEnabledAccessibilityServiceList"))
        addMethodProxy(ReplaceTrailingUserIdProxy("getWindowToken"))
        addMethodProxy(ReplaceTrailingUserIdProxy("interrupt"))
        addMethodProxy(ReplaceTrailingUserIdProxy("addAccessibilityInteractionConnection"))
    }
}
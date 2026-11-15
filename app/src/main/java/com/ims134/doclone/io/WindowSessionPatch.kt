package com.ims134.doclone.io

import com.ims134.doclone.clone.BuildCompat



import android.os.IInterface
import com.ims134.doclone.clone.MethodInvocationProxy
import java.lang.reflect.Method

/**
 * Was `x79`. Patches `IWindowSession` so window-add operations carry the correct
 * package name. Created on demand by `WindowManagerStub` when it wraps the result
 * of `openSession` (see `WindowManagerMethodProxies.WrapResultProxy.wrap`).
 *
 * Note: `inject()` is deliberately empty — this class is instantiated directly and
 * never registered as Stub hook.
 */
class WindowSessionPatch(
    stub: MethodInvocationStub<IInterface>,
) : MethodInvocationProxy<MethodInvocationStub<IInterface>>(stub) {

    /**
     * Was `a`. `addToDisplayAsUser` variant: rewrites the user-id argument slot
     * (which moved between API levels) before delegating.
     */
    inner class AddToDisplayAsUser(name: String) : BaseMethodProxy(name) {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            @Suppress("UNCHECKED_CAST")
            MethodParameterUtils.replaceUserIdArgAt(if (BuildCompat.sdk31OrLater()) 4 else 5, args as Array<Any?>)
            return super.beforeCall(who, method, args)
        }
    }

    override fun injectMethodProxies() {
        addMethodProxy(BaseMethodProxy("add"))
        addMethodProxy(BaseMethodProxy("addToDisplay"))
        addMethodProxy(AddToDisplayAsUser("addToDisplayAsUser"))
        addMethodProxy(BaseMethodProxy("addToDisplayWithoutInputChannel"))
        addMethodProxy(BaseMethodProxy("addWithoutInputChannel"))
        addMethodProxy(BaseMethodProxy("relayout"))
    }

    override fun isEnvBad(): Boolean = invocationStub.proxyInterface != null

    @Throws(Throwable::class)
    override fun inject() {
        // Intentionally empty — WindowSessionPatch is constructed directly, not injected.
    }
}
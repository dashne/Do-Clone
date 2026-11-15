package com.ims134.doclone.io



import android.os.Build
import android.os.IInterface
import com.ims134.doclone.reflect.RefStaticMethod


@Suppress("UNCHECKED_CAST")
class ContextHubServiceStub : BinderInvocationProxy(
    IContextHubServiceMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    if (Build.VERSION.SDK_INT >= 26) "contexthub" else "contexthub_service",
) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(ResultStaticMethodProxy("registerCallback", 0))
        addMethodProxy(ResultStaticMethodProxy("getContextHubHandles", intArrayOf()))
    }
}
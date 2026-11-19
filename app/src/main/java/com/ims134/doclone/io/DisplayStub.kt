package com.ims134.doclone.io

import android.annotation.TargetApi
import android.os.IInterface
import com.ims134.doclone.clone.MethodInvocationProxy
import com.ims134.doclone.clone.MethodInvocationStub

/* JADX INFO: renamed from: io.jo1 */
/* JADX INFO: compiled from: DisplayStub.java */
/* JADX INFO: loaded from: classes2.dex */
@TargetApi(17)
class DisplayStub : MethodInvocationProxy<MethodInvocationStub<IInterface>>(
    MethodInvocationStub(
        DisplayManagerGlobalMirror.mDm?.get(
            DisplayManagerGlobalMirror.getInstance?.call()
        )
    )
) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(ReplaceCallingPkgMethodProxy("createVirtualDisplay"))
    }

    @Throws(Throwable::class)
    override fun inject() {
        DisplayManagerGlobalMirror.mDm?.set(
            DisplayManagerGlobalMirror.getInstance?.call(),
            invocationStub.proxyInterface,
        )
    }

    override fun isEnvBad(): Boolean {
        val current = DisplayManagerGlobalMirror.mDm?.get(
            DisplayManagerGlobalMirror.getInstance?.call()
        )
        return current != invocationStub.proxyInterface
    }
}
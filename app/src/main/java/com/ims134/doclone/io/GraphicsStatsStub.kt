package com.ims134.doclone.io


import android.os.IInterface
import com.ims134.doclone.reflect.RefStaticMethod

/* JADX INFO: renamed from: io.tr2 */
/* JADX INFO: compiled from: GraphicsStatsStub.java */
/* JADX INFO: loaded from: classes2.dex */
@Suppress("UNCHECKED_CAST")
class GraphicsStatsStub : BinderInvocationProxy(
    IGraphicsStatsMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "graphicsstats",
) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(ReplaceCallingPkgMethodProxy("requestBufferForProcess"))
    }
}
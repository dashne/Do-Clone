package com.ims134.doclone.io

import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.ServiceManagerNative
import java.lang.reflect.Method

/* JADX INFO: renamed from: io.m35 */
/* JADX INFO: compiled from: NetworkScoreManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
class NetworkScoreManagerStub : BinderInvocationProxy(
    INetworkScoreManager.Stub.asInterface(
        ServiceManagerNative.getService("network_score")
    ),
    "network_score",
) {

    /**
     * Was `a`. `setActiveScorer` — always returns `true` (as if the scorer was
     * set successfully) without ever consulting the real service. Prevents clones
     * from changing the host's network scorer.
     */
    inner class SetActiveScorerProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? = true

        override fun getMethodName(): String = "setActiveScorer"
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(SetActiveScorerProxy())
    }
}
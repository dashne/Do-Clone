package com.ims134.doclone.io

/* JADX INFO: renamed from: io.gj8 */
/* JADX INFO: compiled from: TrustManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
class TrustManagerStub : BinderInvocationProxy(
    ITrustManagerMiroor.Stub.TYPE,
    "trust",
) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ResultStaticMethodProxy("reportUnlockAttempt", null))
        addMethodProxy(ResultStaticMethodProxy("reportUnlockLockout", null))
        addMethodProxy(ResultStaticMethodProxy("reportEnabledTrustAgentsChanged", null))
        addMethodProxy(ResultStaticMethodProxy("registerTrustListener", null))
        addMethodProxy(ResultStaticMethodProxy("unregisterTrustListener", null))
        addMethodProxy(ResultStaticMethodProxy("reportKeyguardShowingChanged", null))
        addMethodProxy(ResultStaticMethodProxy("setDeviceLockedForUser", null))

        addMethodProxy(ReplaceUserIdMethodProxy("isDeviceLocked", 0))
        addMethodProxy(ReplaceUserIdMethodProxy("isDeviceSecure", 0))

        addMethodProxy(ResultStaticMethodProxy("isTrustUsuallyManaged", false))
    }
}
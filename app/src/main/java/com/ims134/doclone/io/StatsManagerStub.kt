package com.ims134.doclone.io

/* JADX INFO: renamed from: io.kn7 */
/* JADX INFO: compiled from: StatsManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
class StatsManagerStub : BinderInvocationProxy(
    FakeStatsManagerService(),
    "statsmanager",
) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ResultStaticMethodProxy("setDataFetchOperation", null))
        addMethodProxy(ResultStaticMethodProxy("removeDataFetchOperation", null))
        addMethodProxy(ResultStaticMethodProxy("removeActiveConfigsChangedOperation", null))
        addMethodProxy(ResultStaticMethodProxy("setBroadcastSubscriber", null))
        addMethodProxy(ResultStaticMethodProxy("unsetBroadcastSubscriber", null))
        addMethodProxy(ResultStaticMethodProxy("setActiveConfigsChangedOperation", LongArray(0)))
        addMethodProxy(ResultStaticMethodProxy("getRegisteredExperimentIds", LongArray(0)))

        // NOTE: `getMetadata` and `getData` return `byte[]` in the AIDL, but the
        // decompiled source passes `new long[0]` for both. Preserved verbatim —
        // callers that hit these paths get a Long[] where a Byte[] is expected,
        // which throws ClassCastException. If that causes runtime issues, the
        // fix is ByteArray(0).
        addMethodProxy(ResultStaticMethodProxy("getMetadata", LongArray(0)))
        addMethodProxy(ResultStaticMethodProxy("getData", LongArray(0)))

        addMethodProxy(ResultStaticMethodProxy("addConfiguration", null))
        addMethodProxy(ResultStaticMethodProxy("removeConfiguration", null))
        addMethodProxy(ResultStaticMethodProxy("registerPullAtomCallback", null))
        addMethodProxy(ResultStaticMethodProxy("unregisterPullAtomCallback", null))
    }
}
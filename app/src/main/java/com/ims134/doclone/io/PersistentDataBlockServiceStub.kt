package com.ims134.doclone.io

/* JADX INFO: compiled from: PersistentDataBlockServiceStub.java */
/* JADX INFO: loaded from: classes2.dex */
class PersistentDataBlockServiceStub : BinderInvocationProxy(
    IPersistentDataBlockServiceMirror.Stub.TYPE,
    "persistent_data_block",
) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(ResultStaticMethodProxy("write", -1))
        addMethodProxy(ResultStaticMethodProxy("read", ByteArray(0)))
        addMethodProxy(ResultStaticMethodProxy("wipe", null))
        addMethodProxy(ResultStaticMethodProxy("getDataBlockSize", 0))
        addMethodProxy(ResultStaticMethodProxy("getMaximumDataBlockSize", 0))
        addMethodProxy(ResultStaticMethodProxy("setOemUnlockEnabled", 0))
        addMethodProxy(ResultStaticMethodProxy("getOemUnlockEnabled", false))
    }
}
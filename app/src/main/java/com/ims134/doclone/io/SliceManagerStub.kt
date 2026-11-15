package com.ims134.doclone.io

import android.os.IInterface
import com.ims134.doclone.reflect.RefStaticMethod

/* JADX INFO: renamed from: io.tc7 */
/* JADX INFO: compiled from: SliceManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
@Suppress("UNCHECKED_CAST")
class SliceManagerStub : BinderInvocationProxy(
    ISliceManagerMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "slice",
) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ReplaceCallingPkgMethodProxy("pinSlice"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("unpinSlice"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("hasSliceAccess"))
        addMethodProxy(ReplaceLastPkgMethodProxy("getPinnedSlices"))
        addMethodProxy(ReplaceLastUserIdMethodProxy("getBackupPayload"))
        addMethodProxy(ReplaceLastUserIdMethodProxy("applyRestore"))
        addMethodProxy(ResultStaticMethodProxy("checkSlicePermission", 0))
        addMethodProxy(ResultStaticMethodProxy("grantSlicePermission", null))
        addMethodProxy(ResultStaticMethodProxy("revokeSlicePermission", null))
        addMethodProxy(ResultStaticMethodProxy("grantPermissionFromUser", null))
    }
}
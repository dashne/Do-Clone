package com.ims134.doclone.io

import android.os.IInterface
import com.ims134.doclone.reflect.RefStaticMethod

/* JADX INFO: renamed from: io.xq6 */
/* JADX INFO: compiled from: SSClipboardStub.java */
/* JADX INFO: loaded from: classes2.dex */
@Suppress("UNCHECKED_CAST")
class SSClipboardStub : BinderInvocationProxy(
    IClipboardServiceMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "semclipboard",
) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ReplaceLastPkgMethodProxy("getClipData"))
        addMethodProxy(ReplaceLastPkgMethodProxy("setClipData"))
        addMethodProxy(ReplaceLastPkgMethodProxy("getClip"))
        addMethodProxy(ReplaceLastPkgMethodProxy("getClips"))
        addMethodProxy(ReplaceLastPkgMethodProxy("updateClip"))
        addMethodProxy(ReplaceLastPkgMethodProxy("removeClip"))
        addMethodProxy(ReplaceLastPkgMethodProxy("addClip"))
        addMethodProxy(ReplaceLastPkgMethodProxy("removeAll"))
        addMethodProxy(ReplaceLastPkgMethodProxy("pasteClip"))
        addMethodProxy(ReplaceLastPkgMethodProxy("addUserChangedListener"))
        addMethodProxy(ReplaceLastPkgMethodProxy("addClipboardEventListener"))
    }
}
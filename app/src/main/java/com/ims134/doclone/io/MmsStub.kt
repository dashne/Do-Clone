package com.ims134.doclone.io

import android.os.IInterface
import com.ims134.doclone.reflect.RefStaticMethod


/* JADX INFO: renamed from: io.rs4 */
/* JADX INFO: compiled from: MmsStub.java */
/* JADX INFO: loaded from: classes2.dex */
@Suppress("UNCHECKED_CAST")
class MmsStub : BinderInvocationProxy(
    IMmsMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "imms",
) {

    override fun injectMethodProxies() {
        addMethodProxy(ReplaceSpecPkgMethodProxy("sendMessage", 1))
        addMethodProxy(ReplaceSpecPkgMethodProxy("downloadMessage", 1))
        addMethodProxy(ReplaceCallingPkgMethodProxy("importTextMessage"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("importMultimediaMessage"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("deleteStoredMessage"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("deleteStoredConversation"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("updateStoredMessageStatus"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("archiveStoredConversation"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("addTextMessageDraft"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("addMultimediaMessageDraft"))
        addMethodProxy(ReplaceSpecPkgMethodProxy("sendStoredMessage", 1))
        addMethodProxy(ReplaceCallingPkgMethodProxy("setAutoPersisting"))
    }
}
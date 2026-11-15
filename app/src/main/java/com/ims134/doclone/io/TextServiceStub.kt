package com.ims134.doclone.io

import android.os.IInterface
import com.ims134.doclone.reflect.RefStaticMethod

/* JADX INFO: renamed from: io.g88 */
/* JADX INFO: compiled from: TextServiceStub.java */
/* JADX INFO: loaded from: classes2.dex */
@Suppress("UNCHECKED_CAST")
class TextServiceStub : BinderInvocationProxy(
    ITextServicesManagerMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "textservices",
) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ResultStaticMethodProxy("isSpellCheckerEnabled", false))
        addMethodProxy(ResultStaticMethodProxy("getEnabledSpellCheckers", null))
        addMethodProxy(ResultStaticMethodProxy("finishSpellCheckerService", null))
        addMethodProxy(ResultStaticMethodProxy("getSpellCheckerService", null))
        addMethodProxy(ResultStaticMethodProxy("getCurrentSpellChecker", null))
        addMethodProxy(ResultStaticMethodProxy("getCurrentSpellCheckerSubtype", null))
    }
}
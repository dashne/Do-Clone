package com.ims134.doclone.io

import android.os.IInterface
import com.ims134.doclone.reflect.RefStaticMethod

/* JADX INFO: renamed from: io.pr6 */
/* JADX INFO: compiled from: SafetyCenterManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
@Suppress("UNCHECKED_CAST")
class SafetyCenterManagerStub : BinderInvocationProxy(
    ISafetyCenterManagerMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "safety_center",
) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ResultStaticMethodProxy("setSafetySourceData", null))
        addMethodProxy(ResultStaticMethodProxy("refreshSpecificSafetySources", null))
        addMethodProxy(ResultStaticMethodProxy("addOnSafetyCenterDataChangedListener", null))
        addMethodProxy(ResultStaticMethodProxy("removeOnSafetyCenterDataChangedListener", null))
        addMethodProxy(ResultStaticMethodProxy("dismissSafetyCenterIssue", null))
        addMethodProxy(ResultStaticMethodProxy("executeSafetyCenterIssueAction", null))
        addMethodProxy(ResultStaticMethodProxy("clearAllSafetySourceDataForTests", null))
        addMethodProxy(ResultStaticMethodProxy("setSafetyCenterConfigForTests", null))
        addMethodProxy(ResultStaticMethodProxy("isSafetyCenterEnabled", false))
    }
}
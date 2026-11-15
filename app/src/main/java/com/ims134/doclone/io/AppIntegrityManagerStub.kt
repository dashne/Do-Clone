package com.ims134.doclone.io

import java.util.ArrayList

/* JADX INFO: renamed from: io.wq */
/* JADX INFO: compiled from: AppIntegrityManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
class AppIntegrityManagerStub : BinderInvocationProxy(
    FakeAppIntegrityManager(),
    "app_integrity",
) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ResultStaticMethodProxy("updateRuleSet", null))
        addMethodProxy(ResultStaticMethodProxy("getCurrentRuleSetVersion", ""))
        addMethodProxy(ResultStaticMethodProxy("getCurrentRuleSetProvider", ""))
        addMethodProxy(
            ResultStaticMethodProxy(
                "getCurrentRules",
                ParceledListSliceCompat.create(ArrayList<Any?>()),
            )
        )
        addMethodProxy(ResultStaticMethodProxy("getWhitelistedRuleProviders", ArrayList<Any?>()))
    }
}
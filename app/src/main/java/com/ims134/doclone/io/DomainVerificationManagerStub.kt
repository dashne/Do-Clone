package com.ims134.doclone.io

import android.os.Bundle
import android.os.IInterface
import com.ims134.doclone.reflect.RefStaticMethod
import java.util.ArrayList

/* JADX INFO: renamed from: io.qp1 */
/* JADX INFO: compiled from: DomainVerificationManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
@Suppress("UNCHECKED_CAST")
class DomainVerificationManagerStub : BinderInvocationProxy(
    IDomainVerificationManagerMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "domain_verification",
) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(
            ResultStaticMethodProxy("queryValidVerificationPackageNames", ArrayList<Any?>())
        )
        addMethodProxy(ResultStaticMethodProxy("getDomainVerificationInfo", null))
        addMethodProxy(ResultStaticMethodProxy("getOwnersForDomain", ArrayList<Any?>()))
        addMethodProxy(ResultStaticMethodProxy("setDomainVerificationStatus", 0))
        addMethodProxy(ResultStaticMethodProxy("setDomainVerificationLinkHandlingAllowed", null))
        addMethodProxy(ResultStaticMethodProxy("setDomainVerificationUserSelection", 0))
        addMethodProxy(ResultStaticMethodProxy("setUriRelativeFilterGroups", null))
        addMethodProxy(ResultStaticMethodProxy("getUriRelativeFilterGroups", Bundle()))
    }

    @Throws(Throwable::class)
    override fun inject() {
        super.inject()
        try {
            val systemService = context.getSystemService("domain_verification")
            val proxyInterface = invocationStub.proxyInterface
            if (systemService != null && proxyInterface != null) {
                val field = systemService.javaClass
                    .getDeclaredField("mDomainVerificationManager")
                field.isAccessible = true
                field.set(systemService, proxyInterface)
            }
        } catch (th: Throwable) {
            // Original: `th.getMessage();` — ContentProviderHolder stripped log. Preserved as ContentProviderHolder no-op.
            @Suppress("UNUSED_EXPRESSION")
            th.message
        }
    }
}
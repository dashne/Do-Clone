package com.ims134.doclone.io

import android.os.IInterface
import android.util.Log
import com.ims134.doclone.reflect.RefStaticMethod
import java.util.ArrayList

/* JADX INFO: renamed from: io.i91 */
/* JADX INFO: compiled from: CredentialManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
@Suppress("UNCHECKED_CAST")
class CredentialManagerStub : BinderInvocationProxy(
    ICredentialManagerMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "credential",
) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ResultStaticMethodProxy("executeGetCredential", null))
        addMethodProxy(ResultStaticMethodProxy("executePrepareGetCredential", null))
        addMethodProxy(ResultStaticMethodProxy("executeCreateCredential", null))
        addMethodProxy(ResultStaticMethodProxy("getCandidateCredentials", null))
        addMethodProxy(ResultStaticMethodProxy("clearCredentialState", null))
        addMethodProxy(ResultStaticMethodProxy("setEnabledProviders", null))
        addMethodProxy(ResultStaticMethodProxy("registerCredentialDescription", null))
        addMethodProxy(ResultStaticMethodProxy("unregisterCredentialDescription", null))
        addMethodProxy(ResultStaticMethodProxy("isEnabledCredentialProviderService", false))
        addMethodProxy(ResultStaticMethodProxy("getCredentialProviderServices", ArrayList<Any?>()))
        addMethodProxy(
            ResultStaticMethodProxy("getCredentialProviderServicesForTesting", ArrayList<Any?>())
        )
    }

    @Throws(Throwable::class)
    override fun inject() {
        super.inject()
        try {
            val systemService = context.getSystemService("credential")
                ?: throw NullPointerException("CredentialManager is null.")

            Log.e("credential", "got credential service")

            val proxyInterface = invocationStub.proxyInterface
                ?: throw NullPointerException("CredentialManagerProxy is null.")

            val field = systemService.javaClass.getDeclaredField("mService")
            field.isAccessible = true
            field.set(systemService, proxyInterface)

            Log.e("credential", "injected")
        } catch (th: Throwable) {
            Log.e("CredentialManagerStub", "CredentialManagerStub inject error.")
            Log.d("CredentialManagerStub", th.toString())
        }
    }
}
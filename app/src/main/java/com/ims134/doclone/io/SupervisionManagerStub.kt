package com.ims134.doclone.io

import java.lang.reflect.Method
import java.util.ArrayList

/* JADX INFO: renamed from: io.rs7 */
/* JADX INFO: compiled from: SupervisionManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
class SupervisionManagerStub : BinderInvocationProxy(
    supervisionManagerStubClass(),
    "supervision",
) {

    /**
     * Was `a`. Returns an empty `ArrayList` for any call — used for
     * `getPolicies`, `querySupervisionApprovalActivities`, and
     * `getUsersThatRequirePlatformCredential`. The clone sees "no policies,
     * no approval activities, no users requiring platform credential".
     */
    class EmptyListProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            ArrayList<Any?>()
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ResultStaticMethodProxy("isSupervisionEnabledForUser", false))
        addMethodProxy(ResultStaticMethodProxy("getActiveSupervisionAppPackage", null))
        addMethodProxy(ResultStaticMethodProxy("createConfirmSupervisionCredentialsIntent", null))
        addMethodProxy(
            ResultStaticMethodProxy(
                "shouldAllowBypassingSupervisionRoleQualification", false
            )
        )
        addMethodProxy(ResultStaticMethodProxy("getSupervisionRecoveryInfo", null))
        addMethodProxy(ResultStaticMethodProxy("hasSupervisionCredentials", false))
        addMethodProxy(EmptyListProxy("getPolicies"))
        addMethodProxy(ResultStaticMethodProxy("canLaunchPinRecovery", false))
        addMethodProxy(EmptyListProxy("querySupervisionApprovalActivities"))
        addMethodProxy(ResultStaticMethodProxy("hasValidRecoveryMethod", false))
        addMethodProxy(EmptyListProxy("getUsersThatRequirePlatformCredential"))
        addMethodProxy(ResultStaticMethodProxy("registerSupervisionListener", null))
        addMethodProxy(ResultStaticMethodProxy("unregisterSupervisionListener", null))
    }

    companion object {
        /** Was the constructor's `Class.forName(...)` block. */
        private fun supervisionManagerStubClass(): Class<*>? = try {
            Class.forName("android.app.supervision.ISupervisionManager\$Stub")
        } catch (_: Throwable) {
            null
        }
    }
}
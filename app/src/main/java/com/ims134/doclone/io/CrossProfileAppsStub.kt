package com.ims134.doclone.io

import java.lang.reflect.Method
import java.util.ArrayList

/* JADX INFO: renamed from: io.r91 */
/* JADX INFO: compiled from: CrossProfileAppsStub.java */
/* JADX INFO: loaded from: classes2.dex */
class CrossProfileAppsStub : BinderInvocationProxy(
    crossProfileAppsStubClass(),
    "crossprofileapps",
) {

    /**
     * Was `a`. `getTargetUserProfiles` — returns an empty list (clone sees no
     * target profiles it could interact with, so cross-profile operations are
     * effectively disabled).
     */
    class GetTargetUserProfilesProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            ArrayList<Any?>()
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(GetTargetUserProfilesProxy("getTargetUserProfiles"))
        addMethodProxy(ResultStaticMethodProxy("canInteractAcrossProfiles", false))
        addMethodProxy(ResultStaticMethodProxy("canRequestInteractAcrossProfiles", false))
        addMethodProxy(ResultStaticMethodProxy("canConfigureInteractAcrossProfiles", false))
        addMethodProxy(
            ResultStaticMethodProxy("canUserAttemptToConfigureInteractAcrossProfiles", false)
        )
    }

    companion object {
        /** Was the constructor's `Class.forName(...)` block. */
        private fun crossProfileAppsStubClass(): Class<*>? = try {
            Class.forName("android.content.pm.ICrossProfileApps\$Stub")
        } catch (_: Throwable) {
            null
        }
    }
}
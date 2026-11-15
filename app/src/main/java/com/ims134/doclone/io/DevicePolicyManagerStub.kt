package com.ims134.doclone.io

import android.os.IInterface
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.Method

/* JADX INFO: renamed from: io.dm1 */
/* JADX INFO: compiled from: DevicePolicyManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
@Suppress("UNCHECKED_CAST")
class DevicePolicyManagerStub : BinderInvocationProxy(
    IDevicePolicyManagerMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "device_policy",
) {

    /**
     * Was `a`. `getKeyguardDisabledFeatures` — delegates to the real service,
     * returns 0 on any failure (treated as "no features disabled").
     */
    inner class GetKeyguardDisabledFeaturesProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                0
            }

        override fun getMethodName(): String = "getKeyguardDisabledFeatures"
    }

    /**
     * Was `b`. `getStorageEncryptionStatus` — rewrites `args[0]` to the host
     * package name, then invokes directly. No `super.call` — the real service
     * is invoked via `method.invoke` so the base-class dispatch is bypassed.
     */
    class GetStorageEncryptionStatusProxy private constructor() : MethodProxy() {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            args!![0] = VirtualCore.gCore.hostPackageName
            return method.invoke(who, *args)
        }

        override fun getMethodName(): String = "getStorageEncryptionStatus"

        companion object {
            @JvmStatic
            fun create(): GetStorageEncryptionStatusProxy = GetStorageEncryptionStatusProxy()
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(GetStorageEncryptionStatusProxy.create())
        addMethodProxy(ResultStaticMethodProxy("getDeviceOwnerComponent", null))
        addMethodProxy(ResultStaticMethodProxy("getDeviceOwnerComponentOnUser", null))
        addMethodProxy(
            ResultStaticMethodProxy("getAccountTypesWithManagementDisabledAsUser", arrayOf<String>())
        )
        addMethodProxy(
            ResultStaticMethodProxy("getAccountTypesWithManagementDisabled", arrayOf<String>())
        )
        addMethodProxy(FallbackReplaceLastUserIdMethodProxy("getProfileOwnerName", "unknown"))
        addMethodProxy(FallbackReplaceLastUserIdMethodProxy("getProfileOwnerAsUser", null))
        addMethodProxy(ResultStaticMethodProxy("notifyPendingSystemUpdate", null))
        addMethodProxy(ResultStaticMethodProxy("getActiveAdmins", null))
        addMethodProxy(ResultStaticMethodProxy("isAdminActive", false))
        addMethodProxy(ResultStaticMethodProxy("isDeviceProvisioned", true))
        addMethodProxy(ResultStaticMethodProxy("getDeviceOwnerName", "unknown"))
        addMethodProxy(ResultStaticMethodProxy("clearDeviceOwner", null))
        addMethodProxy(FallbackResultMethodProxy("hasDeviceOwner", false))
        addMethodProxy(FallbackResultMethodProxy("removeActiveAdmin", null))
        addMethodProxy(FallbackResultMethodProxy("getDeviceOwnerUserId", VirtualCore.gCore.getHostUserId()))
        addMethodProxy(FallbackResultMethodProxy("setProfileEnabled", null))
        addMethodProxy(FallbackResultMethodProxy("setProfileName", null))
        addMethodProxy(FallbackResultMethodProxy("clearProfileOwner", null))
        addMethodProxy(FallbackResultMethodProxy("hasUserSetupCompleted", true))
        addMethodProxy(FallbackResultMethodProxy("setUserRestriction", null))
        addMethodProxy(FallbackResultMethodProxy("getUserRestrictions", null))
        addMethodProxy(FallbackResultMethodProxy("addCrossProfileIntentFilter", null))
        addMethodProxy(FallbackResultMethodProxy("clearCrossProfileIntentFilters", null))
        addMethodProxy(FallbackResultMethodProxy("createAndManageUser", null))
        addMethodProxy(FallbackResultMethodProxy("removeUser", false))
        addMethodProxy(FallbackResultMethodProxy("switchUser", false))
        addMethodProxy(FallbackResultMethodProxy("startUserInBackground", 0))
        addMethodProxy(FallbackResultMethodProxy("stopUser", 0))
        addMethodProxy(FallbackResultMethodProxy("logoutUser", 0))
        addMethodProxy(ResultStaticMethodProxy("setGlobalSetting", null))
        addMethodProxy(ResultStaticMethodProxy("setSystemSetting", null))
        addMethodProxy(ResultStaticMethodProxy("setSecureSetting", null))
        addMethodProxy(ResultStaticMethodProxy("setLocationEnabled", null))
        addMethodProxy(ResultStaticMethodProxy("setBackupServiceEnabled", null))
        addMethodProxy(ResultStaticMethodProxy("setLogoutEnabled", null))
        addMethodProxy(FallbackResultMethodProxy("isBackupServiceEnabled", false))
        addMethodProxy(FallbackResultMethodProxy("isLogoutEnabled", false))
        addMethodProxy(ResultStaticMethodProxy("isProvisioningAllowed", false))
        addMethodProxy(ResultStaticMethodProxy("setActiveAdmin", null))
        addMethodProxy(GetKeyguardDisabledFeaturesProxy())

        if (BuildCompat.sdk30OrLater()) {
            addMethodProxy(ResultStaticMethodProxy("getFactoryResetProtectionPolicy", null))
        }
    }
}
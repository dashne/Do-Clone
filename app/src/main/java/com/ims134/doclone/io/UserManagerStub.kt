package com.ims134.doclone.io



import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.os.IInterface
import android.os.PersistableBundle
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.reflect.RefClass
import com.ims134.doclone.reflect.RefObject
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.Method
import java.util.Collections


@TargetApi(17)
@Suppress("UNCHECKED_CAST")
class UserManagerStub : BinderInvocationProxy(
    IUserManagerMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "user",
) {

    /**
     * Was `Stub`. `getApplicationRestrictionsForUser` — falls back to an empty
     * `Bundle` on any failure, so callers see "no restrictions" rather than
     * crashing on Stub permission/service error.
     */
    inner class RestrictionsProxy(name: String) : ReplaceCallingPkgMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                Bundle()
            }
    }

    /** Was `b`. `hasBaseUserRestriction` — false on any failure. */
    inner class SafeFalseProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                false
            }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ReplaceCallingPkgMethodProxy("setApplicationRestrictions"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getApplicationRestrictions"))
        addMethodProxy(RestrictionsProxy("getApplicationRestrictionsForUser"))

        addMethodProxy(ResultStaticMethodProxy("getProfileParent", null))

        // Singleton "running/unlocked/visible/admin" style — all true
        addMethodProxy(ResultStaticMethodProxy("isUserRunning", true))
        addMethodProxy(ResultStaticMethodProxy("isUserForeground", true))
        addMethodProxy(ResultStaticMethodProxy("isUserVisible", true))
        addMethodProxy(ResultStaticMethodProxy("isUserUnlocked", true))
        addMethodProxy(ResultStaticMethodProxy("isUserUnlockingOrUnlocked", true))

        addMethodProxy(ResultStaticMethodProxy("getUserIcon", null))
        addMethodProxy(ResultStaticMethodProxy("getUserInfo", makePrimaryUser()))
        addMethodProxy(ResultStaticMethodProxy("getDefaultGuestRestrictions", null))
        addMethodProxy(ResultStaticMethodProxy("setDefaultGuestRestrictions", null))
        addMethodProxy(ResultStaticMethodProxy("removeRestrictions", null))

        // Profile-type checks — all false (single-user emulation)
        addMethodProxy(ResultStaticMethodProxy("isUserOfType", false))
        addMethodProxy(ResultStaticMethodProxy("getProfileType", ""))
        addMethodProxy(ResultStaticMethodProxy("isProfile", false))
        addMethodProxy(ResultStaticMethodProxy("createUser", null))
        addMethodProxy(ResultStaticMethodProxy("isDemoUser", false))
        addMethodProxy(ResultStaticMethodProxy("isMainUser", true))
        addMethodProxy(ResultStaticMethodProxy("getMainUserId", 0))
        addMethodProxy(ResultStaticMethodProxy("createProfileForUser", null))

        // List-shaped results — singleton primary user
        addMethodProxy(
            ResultStaticMethodProxy("getProfiles", Collections.singletonList(makePrimaryUser()))
        )
        addMethodProxy(ResultStaticMethodProxy("getProfileIds", intArrayOf(0)))
        addMethodProxy(ResultStaticMethodProxy("getProfileIdsExcludingHidden", intArrayOf(0)))
        addMethodProxy(ResultStaticMethodProxy("getPrimaryUser", makePrimaryUser()))
        addMethodProxy(ResultStaticMethodProxy("getVisibleUsers", intArrayOf(0)))

        addMethodProxy(ResultStaticMethodProxy("isAdminUser", false))
        addMethodProxy(ResultStaticMethodProxy("isForegroundUserAdmin", false))
        addMethodProxy(ResultStaticMethodProxy("isManagedProfile", false))

        // Pass through with the trailing userId rewritten
        addMethodProxy(ReplaceLastUserIdMethodProxy("hasUserRestriction"))
        addMethodProxy(ReplaceLastUserIdMethodProxy("getUserRestrictions"))

        addMethodProxy(SafeFalseProxy("hasBaseUserRestriction"))

        addMethodProxy(ResultStaticMethodProxy("clearSeedAccountData", null))
        addMethodProxy(ResultStaticMethodProxy("setSeedAccountData", null))
        addMethodProxy(ResultStaticMethodProxy("getSeedAccountName", ""))
        addMethodProxy(ResultStaticMethodProxy("getSeedAccountType", ""))

        if (Build.VERSION.SDK_INT >= 28) {
            addMethodProxy(
                ResultStaticMethodProxy("getSeedAccountOptions", PersistableBundle())
            )
        }

        addMethodProxy(
            ResultStaticMethodProxy("getUsers", Collections.singletonList(makePrimaryUser()))
        )
    }

    @Throws(Throwable::class)
    override fun inject() {
        super.inject()
        val systemService = appContext.getSystemService("user") ?: return
        UserManagerMirror.mService?.set(systemService, invocationStub.proxyInterface)
    }

    companion object {
        /**
         * Was static helper `b()`. Builds Stub synthetic `UserInfo(0, "Admin", FLAG_PRIMARY)`.
         * Used as the "primary user" return value for the many user-info APIs.
         */
        @JvmStatic
        fun makePrimaryUser(): Any? =
            UserInfo.ctor?.newInstance(0, "Admin", UserInfo.FLAG_PRIMARY?.get())
    }
}

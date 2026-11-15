package com.ims134.doclone.io



import android.content.pm.PackageManager
import android.os.IInterface
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.reflect.RefStaticMethod

import java.lang.reflect.Method

@Inject(PhoneSubInfoMethodProxies::class)
@Suppress("UNCHECKED_CAST")
class PhoneSubInfoStub : BinderInvocationProxy(
    IPhoneSubInfo.Stub.asInterface as RefStaticMethod<IInterface>?,
    "iphonesubinfo",
) {

    /**
     * Was `a`. `ReplaceLastPkgMethodProxy` that ignores the real service entirely
     * and returns the fake IMEI.
     */
    inner class FakeImeiLastPkgProxy(name: String) : ReplaceLastPkgMethodProxy(name) {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            MethodProxy.getDeviceInfo().deviceId
    }

    /**
     * Was `b`. `ReplaceCallingPkgMethodProxy` with Stub `READ_PHONE_STATE` guard; on
     * permission denial or any failure, returns `""`.
     */
    inner class PermissionGatedCallingPkgProxy(name: String) :
        ReplaceCallingPkgMethodProxy(name) {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (appContext.checkCallingOrSelfPermission(
                    "android.permission.READ_PHONE_STATE"
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return ""
            }
            return try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                ""
            }
        }
    }

    /**
     * Was `c`. Same permission-gated wrapper as [PermissionGatedCallingPkgProxy],
     * but extends [ReplaceLastPkgMethodProxy]. Only used for
     * `getSubscriberIdForSubscriber`.
     */
    inner class PermissionGatedLastPkgProxy(name: String) :
        ReplaceLastPkgMethodProxy(name) {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (appContext.checkCallingOrSelfPermission(
                    "android.permission.READ_PHONE_STATE"
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return ""
            }
            return try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                ""
            }
        }
    }

    /**
     * Was `d`. `ReplaceCallingPkgMethodProxy` with Stub `""` fallback on failure.
     * Used for `getGroupIdLevel1`.
     */
    inner class SafeCallingPkgProxy(name: String) : ReplaceCallingPkgMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                ""
            }
    }

    /**
     * Was `e`. `ReplaceLastPkgMethodProxy` with Stub `""` fallback on failure.
     * Used for `getGroupIdLevel1ForSubscriber`.
     */
    inner class SafeLastPkgProxy(name: String) : ReplaceLastPkgMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                ""
            }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ReplaceLastPkgMethodProxy("getNaiForSubscriber"))
        addMethodProxy(FakeImeiLastPkgProxy("getImeiForSubscriber"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getDeviceSvn"))
        addMethodProxy(ReplaceLastPkgMethodProxy("getDeviceSvnUsingSubId"))
        addMethodProxy(PermissionGatedCallingPkgProxy("getSubscriberId"))
        addMethodProxy(PermissionGatedLastPkgProxy("getSubscriberIdForSubscriber"))
        addMethodProxy(SafeCallingPkgProxy("getGroupIdLevel1"))
        addMethodProxy(SafeLastPkgProxy("getGroupIdLevel1ForSubscriber"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getLine1Number"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getLine1NumberForDisplay"))
        addMethodProxy(ReplaceLastPkgMethodProxy("getLine1NumberForSubscriber"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getLine1AlphaTag"))
        addMethodProxy(ReplaceLastPkgMethodProxy("getLine1AlphaTagForSubscriber"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getMsisdn"))
        addMethodProxy(ReplaceLastPkgMethodProxy("getMsisdnForSubscriber"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getVoiceMailNumber"))
        addMethodProxy(ReplaceLastPkgMethodProxy("getVoiceMailNumberForSubscriber"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getVoiceMailAlphaTag"))
        addMethodProxy(ReplaceLastPkgMethodProxy("getVoiceMailAlphaTagForSubscriber"))
    }
}
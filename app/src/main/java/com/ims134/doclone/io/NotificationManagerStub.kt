package com.ims134.doclone.io

import android.os.Build
import android.os.IInterface
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.MethodInvocationProxy
import com.ims134.doclone.clone.MethodInvocationStub
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VirtualCore
import java.lang.reflect.Method

/* JADX INFO: renamed from: io.o65 */
/* JADX INFO: compiled from: NotificationManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
@Inject(NotificationManagerMethodProxies::class)
class NotificationManagerStub : MethodInvocationProxy<MethodInvocationStub<IInterface>>(
    MethodInvocationStub(NotificationManagerMirror.getService?.call(*emptyArray<Any?>())),
) {

    /**
     * Was `ContentProviderHolder` and `e` (merged — identical bodies). Delegates via the parent
     * `ReplaceCallingPkgMethodProxy`; on any `Exception`, returns ContentProviderHolder
     * type-appropriate default (false/0/null) via [defaultForReturnType].
     *
     * Used for `enqueueTextToastWithType` and `enqueueTextToast`.
     */
    inner class DefaultFallbackProxy(name: String) : ReplaceCallingPkgMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            return try {
                super.call(who, method, args)
            } catch (_: Exception) {
                defaultForReturnType(method)
            }
        }
    }

    /**
     * Was `b`. Rewrites the trailing userId arg, delegates; on any `Throwable`,
     * returns null. Used for Samsung's `removeEdgeNotification`.
     */
    inner class UserIdRewritingNullFallbackProxy(name: String) : ReplaceCallingPkgMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args != null) MethodParameterUtils.replaceLastUserIdArg(args)
            return try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                null
            }
        }
    }

    /**
     * Was `c`. Rewrites the trailing userId arg, delegates without catching.
     * Used for `getAppActiveNotifications`.
     */
    inner class UserIdRewritingProxy(name: String) : ReplaceCallingPkgMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args != null) MethodParameterUtils.replaceLastUserIdArg(args)
            return super.call(who, method, args)
        }
    }

    /**
     * Was `d`. Rewrites the trailing Int arg to the real calling uid, delegates;
     * on any `Exception`, returns 0. Used for `getBubblePreferenceForPackage`.
     */
    inner class LastIntArgRealUidProxy(name: String) : ReplaceCallingPkgMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            try {
                if (args != null && args.isNotEmpty() && args[args.size - 1] is Int) {
                    args[args.size - 1] = MethodProxy.getRealUid()
                }
                return super.call(who, method, args)
            } catch (_: Exception) {
                return 0
            }
        }
    }

    /**
     * Was `f`. `getConversationNotificationChannel` — rewrites args 0 and 2
     * (pkgName / conversation pkg) to the host package, arg 1 (userId) to the
     * host user id. On any `Exception`, returns null.
     */
    class ConversationChannelProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val vc = VirtualCore.gCore
            val hostPkg = vc.hostPackageName
            val arr = args!!
            arr[0] = hostPkg
            arr[2] = hostPkg
            arr[1] = vc.getHostUserId()
            return try {
                super.call(who, method, args)
            } catch (_: Exception) {
                null
            }
        }

        override fun getMethodName(): String = "getConversationNotificationChannel"
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ReplaceCallingPkgMethodProxy("enqueueToast"))
        addMethodProxy(ResultStaticMethodProxy("enqueueToastForLog", null))
        addMethodProxy(ReplaceCallingPkgMethodProxy("enqueueToastEx"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("enqueueToastWithType"))
        addMethodProxy(DefaultFallbackProxy("enqueueTextToastWithType"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("cancelToast"))

        val sdk = Build.VERSION.SDK_INT

        addMethodProxy(ReplaceCallingPkgMethodProxy("removeAutomaticZenRules"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getImportance"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("areNotificationsEnabled"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("setNotificationPolicy"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getNotificationPolicy"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("setNotificationPolicyAccessGranted"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("isNotificationPolicyAccessGranted"))
        addMethodProxy(
            ReplaceCallingPkgMethodProxy("isNotificationPolicyAccessGrantedForPackage")
        )

        // Samsung-specific
        if ("samsung".equals(Build.BRAND, ignoreCase = true) ||
            "samsung".equals(Build.MANUFACTURER, ignoreCase = true)
        ) {
            addMethodProxy(UserIdRewritingNullFallbackProxy("removeEdgeNotification"))
            if (BuildCompat.sdk28OrLater()) {
                addMethodProxy(ResultStaticMethodProxy("enqueueToastLog", null))
            }
        }

        if (BuildCompat.isAtLeastOreo()) {
            addMethodProxy(
                ReplaceCallingPkgMethodProxy("getNotificationChannelForPackage")
            )
            addMethodProxy(
                ReplaceCallingPkgMethodProxy("createNotificationChannelsForPackage")
            )
        }

        addMethodProxy(UserIdRewritingProxy("getAppActiveNotifications"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getActiveNotifications"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("setInterruptionFilter"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getPackageImportance"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("areBubblesAllowed"))
        addMethodProxy(LastIntArgRealUidProxy("getBubblePreferenceForPackage"))

        if (sdk >= 29) {
            addMethodProxy(ReplaceCallingPkgMethodProxy("shouldHideSilentStatusIcons"))
            addMethodProxy(ReplaceLastPkgMethodProxy("isPackagePaused"))
            addMethodProxy(ReplaceCallingPkgMethodProxy("canNotifyAsPackage"))
            addMethodProxy(ResultStaticMethodProxy("setNotificationDelegate", null))
            addMethodProxy(ResultStaticMethodProxy("getNotificationDelegate", null))
            addMethodProxy(ResultStaticMethodProxy("shouldGroupPkg", false))
        }

        if (sdk >= 30) {
            addMethodProxy(ResultStaticMethodProxy("enqueueToastForDex", null))
            addMethodProxy(ResultStaticMethodProxy("enqueueTextToastForDex", null))
            addMethodProxy(DefaultFallbackProxy("enqueueTextToast"))
            addMethodProxy(ConversationChannelProxy())
        }

        if (BuildCompat.sdk34OrLater()) {
            addMethodProxy(ResultStaticMethodProxy("canUseFullScreenIntent", false))
        }

        addMethodProxy(ResultStaticMethodProxy("canBePromoted", false))
    }

    @Throws(Throwable::class)
    override fun inject() {
        NotificationManagerMirror.sService?.set(invocationStub.proxyInterface)
        ToastMirror.sService?.set(invocationStub.proxyInterface)
    }

    override fun isEnvBad(): Boolean {
        return NotificationManagerMirror.getService?.call(*emptyArray<Any?>()) !=
                invocationStub.proxyInterface
    }

    companion object {
        /**
         * Was static helper `b(Method)`. Returns `false` for boolean-returning
         * methods, `0` for int-returning methods, `null` otherwise. Used as the
         * fallback for [DefaultFallbackProxy].
         */
        @JvmStatic
        fun defaultForReturnType(method: Method?): Any? {
            if (method == null) return null
            val returnType = method.returnType
            return when {
                returnType == java.lang.Boolean.TYPE ||
                        returnType == java.lang.Boolean::class.javaObjectType -> false
                returnType == java.lang.Integer.TYPE ||
                        returnType == java.lang.Integer::class.javaObjectType -> 0
                else -> null
            }
        }
    }
}
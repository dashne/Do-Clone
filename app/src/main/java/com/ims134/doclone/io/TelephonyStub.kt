package com.ims134.doclone.io



import android.os.IBinder
import android.os.IInterface
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.reflect.MethodParams
import com.ims134.doclone.reflect.RefClass
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method



@Inject(TelephonyMethodProxies::class)
@Suppress("UNCHECKED_CAST")
class TelephonyStub : BinderInvocationProxy(
    ITelephony.Stub.asInterface as RefStaticMethod<IInterface>?,
    "phone",
) {

    /**
     * Was `a`. `getGroupIdLevel1` — returns `""` on any failure (missing
     * permission, unsupported modem, etc.).
     */
    inner class GetGroupIdLevel1 : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                ""
            }

        override fun getMethodName(): String = "getGroupIdLevel1"
    }

    /**
     * Was `b`. [ReplaceLastPkgMethodProxy] with `""` fallback on any failure.
     * Only used for `getLine1NumberForDisplay`.
     */
    inner class SafeLastPkgProxy(name: String) : ReplaceLastPkgMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                ""
            }
    }

    /**
     * Was `c`. Replaces the app's package name with the host package wherever it
     * appears in the args, then invokes the target **directly** — bypassing
     * `MethodProxy.call()`'s dispatch. Returns `0` (Int) on `SecurityException`
     * (direct or wrapped).
     *
     * Note: unlike most proxies, this one does not call `super.call(...)`. That's
     * deliberate — these are network-type queries that should not go through the
     * base class's user-id / fake-location dispatch logic.
     */
    class ReplaceAppPkgProxy(private val methodName: String) : MethodProxy() {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            @Suppress("UNCHECKED_CAST")
            val arr = args as Array<Any?>
            val appPkg = MethodProxy.getAppPkg()
            if (appPkg != null) {
                for (i in arr.indices) {
                    if (appPkg == arr[i]) {
                        arr[i] = MethodProxy.getHostPkg()
                        break
                    }
                }
            }
            return try {
                method.invoke(who, *arr)
            } catch (_: SecurityException) {
                0
            } catch (e: InvocationTargetException) {
                if (e.targetException is SecurityException) {
                    0
                } else {
                    throw e.targetException
                }
            }
        }

        override fun getMethodName(): String = methodName
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(GetGroupIdLevel1())
        addMethodProxy(SafeLastPkgProxy("getLine1NumberForDisplay"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("call"))
        addMethodProxy(ReplaceLastPkgMethodProxy("isSimPinEnabled"))
        addMethodProxy(ReplaceLastPkgMethodProxy("getCdmaEriIconIndex"))
        addMethodProxy(ReplaceLastPkgMethodProxy("getCdmaEriIconIndexForSubscriber"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getCdmaEriIconMode"))
        addMethodProxy(ReplaceLastPkgMethodProxy("getCdmaEriIconModeForSubscriber"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getCdmaEriText"))
        addMethodProxy(ReplaceLastPkgMethodProxy("getCdmaEriTextForSubscriber"))
        addMethodProxy(ReplaceAppPkgProxy("getNetworkTypeForSubscriber"))
        addMethodProxy(ReplaceAppPkgProxy("getDataNetworkType"))
        addMethodProxy(ReplaceAppPkgProxy("getDataNetworkTypeForSubscriber"))
        addMethodProxy(ReplaceAppPkgProxy("getVoiceNetworkTypeForSubscriber"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getLteOnCdmaMode"))
        addMethodProxy(ReplaceLastPkgMethodProxy("getLteOnCdmaModeForSubscriber"))
        addMethodProxy(ReplaceLastPkgMethodProxy("getCalculatedPreferredNetworkType"))
        addMethodProxy(ReplaceLastPkgMethodProxy("getPcscfAddress"))
        addMethodProxy(ReplaceLastPkgMethodProxy("getLine1AlphaTagForDisplay"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getMergedSubscriberIds"))
        addMethodProxy(ReplaceLastPkgMethodProxy("getRadioAccessFamily"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("isVideoCallingEnabled"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getDeviceSoftwareVersionForSlot"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getServiceStateForSubscriber"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getVisualVoicemailPackageName"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("enableVisualVoicemailSmsFilter"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("disableVisualVoicemailSmsFilter"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getVisualVoicemailSmsFilterSettings"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("sendVisualVoicemailSmsForSubscriber"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getVoiceActivationState"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getDataActivationState"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getVoiceMailAlphaTagForSubscriber"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("sendDialerSpecialCode"))

        if (BuildCompat.isAtLeastOreo()) {
            addMethodProxy(ReplaceCallingPkgMethodProxy("setVoicemailVibrationEnabled"))
            addMethodProxy(ReplaceCallingPkgMethodProxy("setVoicemailRingtoneUri"))
        }

        addMethodProxy(ResultStaticMethodProxy("isMultiSimSupported", 1))
        addMethodProxy(ReplaceCallingPkgMethodProxy("isOffhook"))
        addMethodProxy(ReplaceLastPkgMethodProxy("isOffhookForSubscriber"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("isRinging"))
        addMethodProxy(ReplaceLastPkgMethodProxy("isRingingForSubscriber"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("isIdle"))
        addMethodProxy(ReplaceLastPkgMethodProxy("isIdleForSubscriber"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("isRadioOn"))
        addMethodProxy(ReplaceLastPkgMethodProxy("isRadioOnForSubscriber"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("getClientRequestStats"))

        // Result-static proxies: fixed return values that short-circuit the real call
        addMethodProxy(ResultStaticMethodProxy("isIccLockEnabled", false))
        addMethodProxy(ResultStaticMethodProxy("getVisualVoicemailSettings", null))
        addMethodProxy(ResultStaticMethodProxy("setDataEnabled", 0))
        addMethodProxy(ResultStaticMethodProxy("getDataEnabled", false))
    }
}
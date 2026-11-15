package com.ims134.doclone.io



import android.os.IInterface
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.reflect.RefStaticMethod

import java.lang.reflect.Method

@Suppress("UNCHECKED_CAST")
class VibratorStub : BinderInvocationProxy(
    IVibratorServiceMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "vibrator",
) {

    /**
     * Was `c`. Rewrites `args[0]` to the real calling uid when it's an Int, so the
     * host vibrator service applies the clone app's actual vibration preferences
     * (per-uid intensity, disable state, DND) instead of the host process's.
     *
     * `open` because [VibrateLevelProxy] and [VibratePatternLevelProxy] extend it.
     */
    open class UidRewritingProxy(name: String) : ReplaceCallingPkgMethodProxy(name) {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            val arr = args!!
            if (arr.isNotEmpty() && arr[0] is Int) {
                arr[0] = MethodProxy.getRealUid()
            }
            return super.beforeCall(who, method, args)
        }
    }

    /**
     * Was `Stub`. `vibrateLevel` — [UidRewritingProxy] with Stub null fallback on any
     * failure (best-effort vibration; nothing is expected from the caller).
     */
    inner class VibrateLevelProxy(name: String) : UidRewritingProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                null
            }
    }

    /**
     * Was `b`. `vibratePatternLevel` — identical body to [VibrateLevelProxy],
     * different method name.
     */
    inner class VibratePatternLevelProxy(name: String) : UidRewritingProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                null
            }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(UidRewritingProxy("vibrateMagnitude"))
        addMethodProxy(UidRewritingProxy("vibratePatternMagnitude"))
        addMethodProxy(UidRewritingProxy("vibrate"))
        addMethodProxy(UidRewritingProxy("vibratePattern"))
        addMethodProxy(VibrateLevelProxy("vibrateLevel"))
        addMethodProxy(VibratePatternLevelProxy("vibratePatternLevel"))
    }
}
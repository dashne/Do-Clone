package com.ims134.doclone.io

import android.os.IInterface
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.Method

/* JADX INFO: renamed from: io.vx8 */
/* JADX INFO: compiled from: VibratorManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
@Suppress("UNCHECKED_CAST")
class VibratorManagerStub : BinderInvocationProxy(
    IVibratorManagerService.Stub.asInterface as RefStaticMethod<IInterface>?,
    "vibrator_manager",
) {

    /**
     * Was `a` and `b` (merged — identical bodies). Rewrites the first package arg
     * to the host package, and if `args[0]` is an Int, rewrites it to the real
     * calling uid. Any exception returns `true` (best-effort vibration).
     */
    inner class VibrateProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            try {
                if (args != null) {
                    MethodParameterUtils.replaceFirstPackageArg(args)
                    if (args.isNotEmpty() && args[0] is Int) {
                        args[0] = MethodProxy.getRealUid()
                    }
                }
                return super.call(who, method, args)
            } catch (_: Exception) {
                return true
            }
        }
    }

    override fun injectMethodProxies() {
        // No super call in the original — preserved.
        addMethodProxy(VibrateProxy("setAlwaysOnEffect"))
        addMethodProxy(VibrateProxy("vibrate"))
    }
}
package com.ims134.doclone.io

import android.os.IInterface
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.Method

/* JADX INFO: renamed from: io.kf4 */
/* JADX INFO: compiled from: LocaleManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
@Suppress("UNCHECKED_CAST")
class LocaleManagerStub : BinderInvocationProxy(
    ILocaleManagerMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "locale",
) {

    /**
     * Was `a`. `getApplicationLocales` — rewrites the trailing userId arg and
     * the first package arg, then delegates to the real service. No fallback;
     * any exception propagates to the caller.
     */
    inner class GetApplicationLocalesProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args != null) {
                MethodParameterUtils.replaceLastUserIdArg(args)
                MethodParameterUtils.replaceFirstPackageArg(args)
            }
            return super.call(who, method, args)
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(ResultStaticMethodProxy("setApplicationLocales", null))
        addMethodProxy(GetApplicationLocalesProxy("getApplicationLocales"))
    }
}
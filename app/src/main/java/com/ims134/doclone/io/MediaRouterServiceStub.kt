package com.ims134.doclone.io

import android.annotation.TargetApi
import android.os.IInterface
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.Method

/* JADX INFO: renamed from: io.mn4 */
/* JADX INFO: compiled from: MediaRouterServiceStub.java */
/* JADX INFO: loaded from: classes2.dex */
@TargetApi(16)
@Suppress("UNCHECKED_CAST")
class MediaRouterServiceStub : BinderInvocationProxy(
    IMediaRouterServiceMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "media_router",
) {

    /**
     * Was `a`. `registerClientAsUser` — extends the standard
     * `ReplaceCallingPkgMethodProxy` (first String arg → host pkg) and additionally
     * rewrites the last userId arg before delegating.
     */
    inner class RegisterClientAsUserProxy(name: String) : ReplaceCallingPkgMethodProxy(name) {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            @Suppress("UNCHECKED_CAST")
            MethodParameterUtils.replaceLastUserIdArg(args as Array<Any?>)
            return super.call(who, method, args)
        }
    }

    /**
     * Was `b`. Replaces the String arg at Stub fixed index with the host package —
     * only when the parameter type at that index is `String` and the actual
     * argument is Stub `String`. Guards against index-out-of-bounds and type
     * mismatches (silent no-op instead of crash).
     */
    class PackageArgAtIndexProxy(
        name: String,
        private val packageIndex: Int,
    ) : StaticMethodProxy(name) {

        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            if (args != null && method != null) {
                val parameterTypes = method.parameterTypes
                if (packageIndex in args.indices &&
                    packageIndex < parameterTypes.size &&
                    parameterTypes[packageIndex] == String::class.java &&
                    args[packageIndex] is String
                ) {
                    args[packageIndex] = MethodProxy.getHostPkg()
                }
            }
            return super.beforeCall(who, method!!, args)
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(RegisterClientAsUserProxy("registerClientAsUser"))

        addPackageArgProxy(0, "getSystemRoutes")
        addPackageArgProxy(0, "showMediaOutputSwitcher")
        addPackageArgProxy(0, "showMediaOutputSwitcherWithRouter2")
        addPackageArgProxy(1, "registerRouter2")
        addPackageArgProxy(0, "getSystemSessionInfoForPackage")
        addPackageArgProxy(1, "registerManager")

        addMethodProxy(ResultStaticMethodProxy("registerProxyRouter", null))
    }

    /** Was `b(int, String)`. */
    private fun addPackageArgProxy(packageIndex: Int, methodName: String) {
        addMethodProxy(PackageArgAtIndexProxy(methodName, packageIndex))
    }
}
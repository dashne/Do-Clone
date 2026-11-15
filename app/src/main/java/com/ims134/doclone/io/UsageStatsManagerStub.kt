package com.ims134.doclone.io

import android.annotation.TargetApi
import android.os.IInterface
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.Method
import java.util.ArrayList

/* JADX INFO: renamed from: io.ps8 */
/* JADX INFO: compiled from: UsageStatsManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
@TargetApi(22)
@Suppress("UNCHECKED_CAST")
class UsageStatsManagerStub : BinderInvocationProxy(
    IUsageStatsManagerMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "usagestats",
) {

    /**
     * Was `a`. Rewrites `args[0]` to the host package and `args[1]` to the host
     * user id, then delegates. On any failure returns an empty `ParceledListSlice`.
     *
     * Used by `getAppStandbyBuckets` (API 28+, signature
     * `getAppStandbyBuckets(String packageName, int userId)`).
     */
    inner class PackageAndUserArgsProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                val arr = args!!
                val vc = VirtualCore.gCore
                arr[0] = vc.hostPackageName
                arr[1] = vc.getHostUserId()
                super.call(who, method, args)
            } catch (_: Exception) {
                ParceledListSliceCompat.create(ArrayList<Any?>())
            }
    }

    /**
     * Was `b`. Rewrites `args[0]` and `args[1]` to the host package, and
     * `args[2]` to the host user id. On any failure returns `0`.
     *
     * Used by `getAppStandbyBucket` (API 28+, signature
     * `getAppStandbyBucket(String callingPackage, String packageName, int userId)`).
     */
    inner class DualPackageAndUserArgsProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                val arr = args!!
                val hostPkg = VirtualCore.gCore.hostPackageName
                arr[0] = hostPkg
                arr[1] = hostPkg
                arr[2] = VirtualCore.gCore.getHostUserId()
                super.call(who, method, args)
            } catch (_: Exception) {
                0
            }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ReplaceLastPkgMethodProxy("queryUsageStats"))
        addMethodProxy(ReplaceLastPkgMethodProxy("queryConfigurations"))
        addMethodProxy(ReplaceLastPkgMethodProxy("queryEvents"))
        addMethodProxy(ReplaceLastPkgMethodProxy("queryConfigurationStats"))

        addMethodProxy(ResultStaticMethodProxy("queryEventsForPackage", null))
        addMethodProxy(ResultStaticMethodProxy("isAppInactive", false))

        if (BuildCompat.sdk28OrLater()) {
            addMethodProxy(ResultStaticMethodProxy("registerAppUsageObserver", null))
            addMethodProxy(ResultStaticMethodProxy("unregisterAppUsageObserver", null))
            addMethodProxy(ResultStaticMethodProxy("registerUsageSessionObserver", null))
            addMethodProxy(ResultStaticMethodProxy("unregisterUsageSessionObserver", null))
            addMethodProxy(ResultStaticMethodProxy("registerAppUsageLimitObserver", null))
            addMethodProxy(ResultStaticMethodProxy("unregisterAppUsageLimitObserver", null))
            addMethodProxy(ResultStaticMethodProxy("reportUsageStart", null))
            addMethodProxy(ResultStaticMethodProxy("reportPastUsageStart", null))
            addMethodProxy(ResultStaticMethodProxy("reportUsageStop", null))
            addMethodProxy(ResultStaticMethodProxy("setAppStandbyBucket", null))
            addMethodProxy(PackageAndUserArgsProxy("getAppStandbyBuckets"))
            addMethodProxy(DualPackageAndUserArgsProxy("getAppStandbyBucket"))
        }
    }
}
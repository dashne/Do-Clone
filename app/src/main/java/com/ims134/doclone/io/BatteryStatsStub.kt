package com.ims134.doclone.io

import android.annotation.TargetApi
import android.os.IInterface
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.reflect.RefObject
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.Method

/* JADX INFO: renamed from: io.r30 */
/* JADX INFO: compiled from: BatteryStatsStub.java */
/* JADX INFO: loaded from: classes2.dex */
@TargetApi(24)
@Suppress("UNCHECKED_CAST")
class BatteryStatsStub : BinderInvocationProxy(
    IBatteryStatsMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "batterystats",
) {

    /**
     * Was `a`. `takeUidSnapshot` — delegates to the real service after the
     * inherited `ReplaceLastUidMethodProxy.beforeCall` swaps the trailing
     * uid with the real uid. Returns null on any failure (no battery stats
     * are surfaced to the clone).
     */
    inner class TakeUidSnapshotProxy(name: String) : ReplaceLastUidMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                null
            }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(TakeUidSnapshotProxy("takeUidSnapshot"))
    }

    @Suppress("UNCHECKED_CAST")
    override fun inject() {
        super.inject()
        (SystemHealthManagerMirror.mBatteryStats as? RefObject<Any?>)?.set(
            appContext.getSystemService("systemhealth"),
            invocationStub.proxyInterface,
        )
    }
}
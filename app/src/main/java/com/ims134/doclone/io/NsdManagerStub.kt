package com.ims134.doclone.io

import android.os.IInterface
import android.os.Process
import com.ims134.doclone.clone.VirtualCore
import java.lang.reflect.Method

/* JADX INFO: renamed from: io.q65 */
/* JADX INFO: compiled from: NsdManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
class NsdManagerStub : BinderInvocationProxy(
    nsdManagerStubClass(),
    "servicediscovery",
) {

    /**
     * Was `a`. `connect` — rewrites package-name strings and
     * `AttributionSource`/`AttributionSourceState` args to host identity before
     * delegating. Returns `true` unconditionally — bypasses `super.beforeCall`,
     * so the base class's own `beforeCall` logic is skipped.
     */
    class ConnectProxy private constructor() : StaticMethodProxy("connect") {

        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            if (args == null) return true

            val currentPackage = VClientImpl.get().getCurrentPackage()
            val hostPkg = VirtualCore.gCore.hostPackageName
            val sourceType = AttributionSourceMirror.TYPE
            val stateType = AttributionSourceState.TYPE

            for (i in args.indices) {
                val arg = args[i] ?: continue
                if (arg is String) {
                    if (currentPackage != null && currentPackage == arg) {
                        args[i] = hostPkg
                    }
                } else if (sourceType != null && sourceType.isInstance(arg)) {
                    args[i] = AttributionSourceCompat.withPackage(
                        VirtualCore.gCore.uid, hostPkg, arg,
                    )
                } else if (stateType != null && stateType.isInstance(arg)) {
                    args[i] = AttributionSourceCompat.withUidPidPackage(
                        arg, VirtualCore.gCore.uid, Process.myPid(), hostPkg,
                    )
                }
            }
            // Original returns true unconditionally — bypasses super.beforeCall.
            return true
        }

        companion object {
            @JvmStatic
            fun create(): ConnectProxy = ConnectProxy()
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(ConnectProxy.create())
    }

    companion object {
        /** Was the constructor's `Class.forName(...)` block. */
        private fun nsdManagerStubClass(): Class<*>? = try {
            Class.forName("android.net.nsd.INsdManager\$Stub")
        } catch (_: Throwable) {
            null
        }
    }
}
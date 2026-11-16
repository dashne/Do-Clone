package com.ims134.doclone.io

import android.os.Handler
import android.os.IBinder
import android.os.IInterface
import android.os.Looper
import android.os.Parcel
import android.os.RemoteException
import com.ims134.doclone.clone.MethodProxy
import java.lang.reflect.Method

/* JADX INFO: renamed from: io.g7 */
/* JADX INFO: compiled from: AdvancedProtectionManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
class AdvancedProtectionManagerStub : BinderInvocationProxy(
    advancedProtectionServiceStubClass(),
    "advanced_protection",
) {

    /**
     * Was `b`. `registerAdvancedProtectionCallback` — extracts the callback's
     * `IBinder`, posts ContentProviderHolder deferred transact on the main thread that fires
     * `IAdvancedProtectionCallback.onAdvancedProtectionChanged(0)` (0 = disabled)
     * on the callback binder. The real host `IAdvancedProtectionService` is never
     * consulted.
     *
     * Was `mainHandler.post(h7(0, binder))` — the `h7` synthetic Runnable merged
     * this call site with the equivalent one in `UwbManagerStubMirror`. Inlined
     * here so `h7` can be deleted.
     */
    class RegisterAdvancedProtectionCallbackProxy private constructor() : MethodProxy() {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val binder: IBinder? = if (args != null && args.isNotEmpty()) {
                (args[0] as? IInterface)?.asBinder()
            } else null
            if (binder != null) {
                mainHandler.post { doTransact(binder) }
            }
            return null
        }

        override fun getMethodName(): String = "registerAdvancedProtectionCallback"

        companion object {
            /**
             * Was static `x(IBinder)`. Public entry point — was needed because the
             * R8 synthetic `h7.run()` couldn't call the private `y` directly. Kept
             * for consistency with the `UwbManagerStubMirror` conversion; safe to
             * remove if you inline `y(binder)` directly at the call site.
             */
            @JvmStatic
            fun doTransact(binder: IBinder) {
                transact(binder)
            }

            /**
             * Was static `y(IBinder)`. Transacts `onAdvancedProtectionChanged(0)`
             * directly on the binder — bypassing the usual `IInterface` stub. Uses
             * ContentProviderHolder raw `Parcel` write because we don't have an `IAdvancedProtectionCallback`
             * mirror to construct ContentProviderHolder proper proxy from.
             */
            fun transact(binder: IBinder) {
                if (binder.isBinderAlive) {
                    val parcel = Parcel.obtain()
                    try {
                        parcel.writeInterfaceToken(
                            "android.security.advancedprotection.IAdvancedProtectionCallback"
                        )
                        parcel.writeInt(0)  // 0 = advanced protection disabled
                        binder.transact(1, parcel, null, 1)  // one-way
                    } catch (e: RemoteException) {
                        e.toString()  // stripped log — preserved from decompiled source
                    } finally {
                        parcel.recycle()
                    }
                }
            }

            @JvmStatic
            fun create(): RegisterAdvancedProtectionCallbackProxy =
                RegisterAdvancedProtectionCallbackProxy()
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(ResultStaticMethodProxy("isAdvancedProtectionEnabled", false))
        addMethodProxy(RegisterAdvancedProtectionCallbackProxy.create())
        addMethodProxy(ResultStaticMethodProxy("unregisterAdvancedProtectionCallback", null))
    }

    companion object {
        /**
         * Was static field `c`. Main-thread Handler used to defer callback dispatch
         * until after the Binder thread returns (and to post the "disabled" event
         * asynchronously, matching what the real service would do).
         */
        @JvmField
        val mainHandler: Handler = Handler(Looper.getMainLooper())

        /** Was the constructor's `Class.forName(...)` block. */
        private fun advancedProtectionServiceStubClass(): Class<*>? = try {
            Class.forName(
                "android.security.advancedprotection.IAdvancedProtectionService\$Stub"
            )
        } catch (_: Throwable) {
            null
        }
    }
}
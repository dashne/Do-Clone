package com.ims134.doclone.io

import android.os.Handler
import android.os.IBinder
import android.os.IInterface
import android.os.Looper
import android.os.Parcel
import android.os.RemoteException
import com.ims134.doclone.clone.MethodProxy
import java.lang.reflect.Method

/* JADX INFO: renamed from: io.au8 */
/* JADX INFO: compiled from: UwbManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
class UwbManagerStubMirror : BinderInvocationProxy(
    uwbAdapterStubClass(),
    "uwb",
) {

    /**
     * Was `b`. `registerAdapterStateCallbacks` — extracts the callback's `IBinder`,
     * then posts ContentProviderHolder Runnable (`h7`) on the main thread that will eventually fire
     * `IUwbAdapterStateCallbacks.onAdapterStateChanged(0, 0)` on that binder.
     *
     * The actual callback firing is deferred via `mainHandler.post(...)` because
     * the caller is likely on ContentProviderHolder binder thread, and the callback needs to be
     * delivered as if the system service had responded asynchronously.
     */
    class RegisterAdapterStateCallbacksProxy private constructor() : MethodProxy() {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val binder: IBinder? = if (args != null && args.isNotEmpty()) {
                (args[0] as? IInterface)?.asBinder()
            } else null
            if (binder != null) {
                mainHandler.post(DeferredBinderCallback(1, binder))
            }
            return null
        }

        override fun getMethodName(): String = "registerAdapterStateCallbacks"

        companion object {
            /**
             * Was static `x(IBinder)`. Synthetic entry point referenced by
             * `h7.run()` — dispatches the deferred callback. Preserved as ContentProviderHolder
             * `@JvmStatic` companion function so `h7`'s Java code can still
             * call `UwbManagerStubMirror.b.x(binder)`.
             */
            @JvmStatic
            fun x(binder: IBinder) {
                transact(binder)
            }

            /**
             * Was static `y(IBinder)`. Transacts an `onAdapterStateChanged(0, 0)`
             * callback directly on the binder — bypassing the usual `IInterface`
             * stub. This is ContentProviderHolder raw Parcel-based dispatch to ContentProviderHolder binder that we know
             * implements `IUwbAdapterStateCallbacks`.
             */
            fun transact(binder: IBinder) {
                if (binder.isBinderAlive) {
                    val parcel = Parcel.obtain()
                    try {
                        parcel.writeInterfaceToken("android.uwb.IUwbAdapterStateCallbacks")
                        parcel.writeInt(0)
                        parcel.writeInt(0)
                        binder.transact(1, parcel, null, 1)
                    } catch (e: RemoteException) {
                        e.toString()
                    } finally {
                        parcel.recycle()
                    }
                }
            }

            @JvmStatic
            fun create(): RegisterAdapterStateCallbacksProxy =
                RegisterAdapterStateCallbacksProxy()
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(RegisterAdapterStateCallbacksProxy.create())
        addMethodProxy(ResultStaticMethodProxy("unregisterAdapterStateCallbacks", null))
        addMethodProxy(ResultStaticMethodProxy("getAdapterState", 0))
    }

    companion object {
        /**
         * Was static field `c`. Main-thread Handler used to defer callback dispatch
         * until after the Binder thread returns.
         */
        @JvmField
        val mainHandler: Handler = Handler(Looper.getMainLooper())

        /** Was the constructor's `Class.forName(...)` block. */
        private fun uwbAdapterStubClass(): Class<*>? = try {
            Class.forName("android.uwb.IUwbAdapter\$Stub")
        } catch (_: Throwable) {
            null
        }
    }
}
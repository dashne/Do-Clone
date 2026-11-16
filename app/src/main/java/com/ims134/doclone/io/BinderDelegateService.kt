package com.ims134.doclone.io

import android.content.ComponentName
import android.os.Binder
import android.os.IBinder
import android.os.RemoteException

/* JADX INFO: renamed from: io.e50 */
/* JADX INFO: compiled from: BinderDelegateService.java */
/* JADX INFO: loaded from: classes2.dex */
class BinderDelegateService(
    @JvmField val componentName: ComponentName,
    iBinder: IBinder,
) : IServiceBinderWrapper.Stub() {

    /**
     * Was `b`. The actual service binder — potentially wrapped by a factory
     * from [WRAPPERS] if the caller passed a local `Binder` whose interface
     * descriptor has a registered wrapper.
     */
    @JvmField
    val binder: IBinder = if (iBinder is Binder) {
        WRAPPERS[iBinder.interfaceDescriptor]?.wrap(iBinder) ?: iBinder
    } else {
        iBinder
    }

    @Throws(RemoteException::class)
    override fun getComponent(): ComponentName = componentName

    @Throws(RemoteException::class)
    override fun getService(): IBinder = binder

    /**
     * Was inner class `a`. Factory that wraps an `IAccountAuthenticator` binder
     * in [t82]. The wrapper intercepts `getAuthToken` / `addAccount` / etc. so
     * the client sees virtual account data instead of the host's.
     */
    class AccountAuthenticatorWrapperFactory : BinderWrapperFactory {
        override fun wrap(binder: Binder): FakeIdentityBinder = FakeIdentityBinder(binder)
    }

    /** Was interface `b`. */
    interface BinderWrapperFactory {
        fun wrap(binder: Binder): FakeIdentityBinder
    }

    companion object {
        /**
         * Was static field `c`. Maps Binder interface descriptor → wrapper
         * factory. Populated once at class load; only `IAccountAuthenticator`
         * has a registered wrapper in the current codebase.
         *
         * Uses `@JvmField` to preserve the public static field shape — callers
         * that register additional wrappers at runtime (or read the map for
         * inspection) can access it directly as `BinderDelegateService.WRAPPERS`.
         */
        @JvmField
        val WRAPPERS: HashMap<String, BinderWrapperFactory> = HashMap<String, BinderWrapperFactory>().apply {
            put(
                IAccountAuthenticator.Stub.DESCRIPTOR,
                AccountAuthenticatorWrapperFactory(),
            )
        }
    }
}
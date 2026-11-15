package com.ims134.doclone.io



import android.os.IInterface
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.reflect.RefStaticMethod


@Inject(StorageManagerMethodProxies::class)
class MountServiceStub : BinderInvocationProxy(
    storageManagerService(),
    "mount",
) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(ReplaceLastPkgMethodProxy("getAllocatableBytes"))
        addMethodProxy(ReplaceUidMethodProxy("getCacheSizeBytes", 1))
        addMethodProxy(ReplaceUidMethodProxy("getCacheQuotaBytes", 1))
    }

    companion object {
        /**
         * Was the ternary in the constructor:
         * `BuildCompat.isAtLeastOreo() ? c63.Stub.asInterface : w43.Stub.asInterface`.
         *
         * Extracted to Stub helper because Kotlin super-constructor arguments can't
         * contain Stub cast that needs `@Suppress("UNCHECKED_CAST")` on most Kotlin
         * versions; the helper puts the suppression on the function instead.
         */
        @Suppress("UNCHECKED_CAST")
        private fun storageManagerService(): RefStaticMethod<IInterface>? =
            (if (BuildCompat.isAtLeastOreo()) IStorageManagerMirror.Stub.asInterface
            else IMountService.Stub.asInterface) as RefStaticMethod<IInterface>?
    }
}

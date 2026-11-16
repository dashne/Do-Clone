package com.ims134.doclone.io

import android.os.IBinder
import com.ims134.doclone.clone.ServiceManagerNative
import com.ims134.doclone.clone.VirtualCore

/* JADX INFO: renamed from: io.y09 */
/* JADX INFO: compiled from: VirtualStorageManager.java */
/* JADX INFO: loaded from: classes2.dex */
class VirtualStorageManager private constructor() {

    /**
     * Was `a`. Cached [IVirtualStorageService] proxy. Nullable — lazily fetched
     * by [getService] and cleared when the binder dies.
     */
    @Volatile
    @JvmField
    var service_: IVirtualStorageService? = null

    /**
     * Was inner class `a`. Clears the cached service when the binder dies so
     * [getService] re-binds on the next call.
     */
    private inner class BinderDeathRecipient(
        private val boundService: IVirtualStorageService,
    ) : IBinder.DeathRecipient {
        override fun binderDied() {
            boundService.asBinder()!!.unlinkToDeath(this, 0)
            service_ = null
        }
    }

    /**
     * Was `a()`. Returns the cached [IVirtualStorageService], re-binding on
     * first use or when the cached binder has died. In the client process,
     * stale-but-alive binders are reused (we don't check death there) — only
     * the server process re-binds.
     */
    fun getService(): IVirtualStorageService {
        val current = service_
        if (current == null ||
            (!current.asBinder()!!.isBinderAlive && !VirtualCore.gCore.isClientProcess())
        ) {
            synchronized(this) {
                val newService = IVirtualStorageService.Stub.asInterface(
                    ServiceManagerNative.getService("vs")
                )!!
                try {
                    newService.asBinder()!!.linkToDeath(BinderDeathRecipient(newService), 0)
                } catch (_: Throwable) {
                }
                service_ = newService
            }
        }
        return service_!!
    }

    companion object {
        /**
         * Was static field `b`. Singleton — kept as `b` so the caller in
         * `bindApplication` (`y09.b`) can reference it unchanged.
         */
        @JvmField
        val virtualStorageManager: VirtualStorageManager = VirtualStorageManager()

        /** Idiomatic Kotlin accessor for [virtualStorageManager]. */
        val instance: VirtualStorageManager get() = virtualStorageManager
    }
}
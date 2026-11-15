package com.ims134.doclone.io

import com.ims134.doclone.clone.ServiceManagerNative
import com.ims134.doclone.clone.VirtualCore
import android.os.IBinder


/**
 * Was `ku8`. Singleton wrapper for the `"content"` system service, with
 * binder-death recovery.
 */
class VContentManager private constructor() {

    @Volatile
    @JvmField
    var contentService: IContentService? = null

    /** Was inner class `a`. Clears the cached service when the binder dies. */
    private inner class BinderDeathRecipient(
        private val service: IContentService,
    ) : IBinder.DeathRecipient {
        override fun binderDied() {
            service.asBinder()!!.unlinkToDeath(this, 0)
            contentService = null
        }
    }

    /** Was `a()`. Returns the cached service, re-binding on first use or binder death. */
    fun getService(): IContentService {
        val current = contentService
        if (current == null ||
            (!current.asBinder()!!.isBinderAlive && !VirtualCore.gCore.isClientProcess())
        ) {
            synchronized(this) {
                val service = IContentService.Stub.asInterface(
                    ServiceManagerNative.getService("content")
                )!!
                try {
                    service.asBinder()!!.linkToDeath(BinderDeathRecipient(service), 0)
                } catch (_: Throwable) {
                }
                contentService = service
            }
        }
        return contentService!!
    }

    companion object {
        /** Was static field `b`. */
        @JvmField
        val b: VContentManager = VContentManager()

        /** Idiomatic Kotlin alias for [b]. */
        val instance: VContentManager get() = b
    }
}
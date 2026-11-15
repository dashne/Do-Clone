package com.ims134.doclone.io

import android.os.IBinder

/**
 * Was `h7` — an R8-synthesized Runnable that merged two identical-shape lambdas
 * from `g7` (case 0) and `au8` (default / case 1+). Both branches fire a deferred
 * Binder transact on the passed callback binder.
 *
 * This class only exists because `g7` hasn't been inlined yet. Delete it and
 * inline its two call sites once `g7` is converted. See the `au8` conversion's
 * inline note.
 */
internal class DeferredBinderCallback(
    private val case: Int,
    private val binder: IBinder,
) : Runnable {

    override fun run() {
        when (case) {
            0 -> AdvancedProtectionManagerStub.RegisterAdvancedProtectionCallbackProxy.transact(binder)                              // g7 stub — still Java
            else -> UwbManagerStubMirror.RegisterAdapterStateCallbacksProxy.transact(binder)
        }
    }
}
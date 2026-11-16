package com.ims134.doclone.io

import android.os.Binder
import android.os.IInterface
import android.os.Parcel
import android.os.Process
import android.os.RemoteException

/* JADX INFO: renamed from: io.t82 */
/* JADX INFO: compiled from: FakeIdentityBinder.java */
/* JADX INFO: loaded from: classes2.dex */
class FakeIdentityBinder(@JvmField val a: Binder) : Binder() {

    /**
     * Was `onTransact(int, Parcel, Parcel, int)`. Executes the wrapped binder's
     * transaction with the *current process's own* identity — uid in the high
     * 32 bits, pid in the low 32 bits — the standard Linux Binder identity
     * layout. The wrapped binder's `onTransact` (or any `Binder.getCallingUid()`
     * it does internally) sees the current process as the caller, instead of
     * whatever identity the outer IPC chain carried.
     *
     * Preserved from the original: the identity is restored *before* the
     * wrapped `transact` call and reverted *after* it, all under a
     * clearCallingIdentity/restoreCallingIdentity pair. This ensures the swap
     * is visible only for the duration of the delegated call.
     */
    @Throws(RemoteException::class)
    public override fun onTransact(
        code: Int,
        data: Parcel,
        reply: Parcel?,
        flags: Int,
    ): Boolean {
        val savedIdentity = Binder.clearCallingIdentity()
        try {
            // Binder identity format: (uid << 32) | pid. Restoring with this
            // makes the wrapped binder see the current process as the caller.
            Binder.restoreCallingIdentity(
                (Process.myUid().toLong() shl 32) or Process.myPid().toLong()
            )
            return a.transact(code, data, reply, flags)
        } finally {
            Binder.restoreCallingIdentity(savedIdentity)
        }
    }

    override fun attachInterface(owner: IInterface?, descriptor: String?) {
        a.attachInterface(owner, descriptor)
    }

    override fun getInterfaceDescriptor(): String? {
        return a.interfaceDescriptor
    }

    override fun queryLocalInterface(descriptor: String): IInterface? {
        return a.queryLocalInterface(descriptor)
    }
}
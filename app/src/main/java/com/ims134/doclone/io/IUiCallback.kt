package com.ims134.doclone.io

import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException

/* JADX INFO: renamed from: io.o63 */
/* JADX INFO: compiled from: IUiCallback.java */
/* JADX INFO: loaded from: classes2.dex */
interface IUiCallback : IInterface {

    @Throws(RemoteException::class)
    fun onAppOpened(pkgName: String?, vuid: Int)

    abstract class Stub : Binder(), IUiCallback {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        /**
         * Was `b.a`. Proxy implementation — sets its `remote` field after
         * construction (via [asInterface]), matching the original's
         * `new a(); aVar.a = iBinder;`.
         *
         * One-way call: `transact(code, data, null, FLAG_ONEWAY)`. No reply
         * parcel, no exception reading.
         */
        class Proxy : IUiCallback {

            @JvmField
            var a: IBinder? = null

            override fun asBinder(): IBinder? = a

            @Throws(RemoteException::class)
            override fun onAppOpened(pkgName: String?, vuid: Int) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(pkgName)
                    data.writeInt(vuid)
                    // One-way transact — no reply parcel, FLAG_ONEWAY = 1.
                    a!!.transact(TRANSACTION_onAppOpened, data, null, 1)
                } finally {
                    data.recycle()
                }
            }
        }

        @Throws(RemoteException::class)
        override fun onTransact(code: Int, data: Parcel, out: Parcel?, flags: Int): Boolean {
            val reply = out ?: return false
            if (code in 1..0xFFFFFF) {
                data.enforceInterface(DESCRIPTOR)
            }
            if (code == 0x5F4E5446) { // INTERFACE_TRANSACTION
                reply.writeString(DESCRIPTOR)
                return true
            }
            return when (code) {
                TRANSACTION_onAppOpened -> {
                    onAppOpened(data.readString(), data.readInt())
                    // One-way — no writeNoException().
                    true
                }
                else -> super.onTransact(code, data, reply, flags)
            }
        }

        companion object {
            const val DESCRIPTOR = "com.polestar.clone.server.interfaces.IUiCallback"

            private const val TRANSACTION_onAppOpened = 1

            @JvmStatic
            fun asInterface(iBinder: IBinder?): IUiCallback? {
                if (iBinder == null) return null
                val local = iBinder.queryLocalInterface(DESCRIPTOR)
                return if (local == null || local !is IUiCallback) {
                    Proxy().apply { a = iBinder }
                } else {
                    local
                }
            }
        }
    }

    /** Was `a`. Default no-op implementation. */
    class Default : IUiCallback {

        override fun asBinder(): IBinder? = null

        @Throws(RemoteException::class)
        override fun onAppOpened(pkgName: String?, vuid: Int) {}
    }
}
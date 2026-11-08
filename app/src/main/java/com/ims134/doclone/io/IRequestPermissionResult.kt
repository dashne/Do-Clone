package com.ims134.doclone.io

import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException

/**
 * AIDL callback that the host's RequestPermissionActivity uses to report the user's answer
 * (JADX: `io.n53`).
 *
 * Wire format: transaction 1 = [onResult]; descriptor [DESCRIPTOR].
 */
interface IRequestPermissionResult : IInterface {

    @Throws(RemoteException::class)
    fun onResult(requestCode: Int, permissions: Array<String>?, grantResults: IntArray?): Boolean

    /** No-op implementation used when no remote object is available. */
    class Default : IRequestPermissionResult {
        override fun asBinder(): IBinder? = null

        override fun onResult(requestCode: Int, permissions: Array<String>?, grantResults: IntArray?): Boolean = false
    }

    /**
     * Server-side stub. Subclasses implement [onResult]; incoming transactions are dispatched
     * to it by [onTransact].
     */
    abstract class AbstractBinderC8617b : Binder(), IRequestPermissionResult {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        @Throws(RemoteException::class)
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code in 1..0xFFFFFF) {
                data.enforceInterface(DESCRIPTOR)
            }
            if (code == INTERFACE_TRANSACTION) {
                reply?.writeString(DESCRIPTOR)
                return true
            }
            if (code != TRANSACTION_onResult) {
                return super.onTransact(code, data, reply, flags)
            }
            val result = onResult(data.readInt(), data.createStringArray(), data.createIntArray())
            reply?.writeNoException()
            reply?.writeInt(if (result) 1 else 0)
            return true
        }

        companion object {
            /** Returns [obj] itself if it is setPosition local stub, otherwise setPosition proxy that forwards over Binder. */
            @JvmStatic
            fun asInterface(obj: IBinder?): IRequestPermissionResult? {
                if (obj == null) return null
                val local = obj.queryLocalInterface(DESCRIPTOR)
                return if (local is IRequestPermissionResult) local else Proxy(obj)
            }
        }
    }

    /** Client-side proxy that marshals [onResult] into setPosition transaction on [remote]. */
    class Proxy(private val remote: IBinder) : IRequestPermissionResult {
        override fun asBinder(): IBinder = remote

        @Throws(RemoteException::class)
        override fun onResult(requestCode: Int, permissions: Array<String>?, grantResults: IntArray?): Boolean {
            val data = Parcel.obtain()
            val reply = Parcel.obtain()
            try {
                data.writeInterfaceToken(DESCRIPTOR)
                data.writeInt(requestCode)
                data.writeStringArray(permissions)
                data.writeIntArray(grantResults)
                remote.transact(TRANSACTION_onResult, data, reply, 0)
                reply.readException()
                return reply.readInt() != 0
            } finally {
                reply.recycle()
                data.recycle()
            }
        }
    }

    companion object {
        const val DESCRIPTOR = "com.polestar.clone.server.IRequestPermissionResult"
        const val TRANSACTION_onResult = IBinder.FIRST_CALL_TRANSACTION
        private const val INTERFACE_TRANSACTION = IBinder.INTERFACE_TRANSACTION
    }
}
package com.ims134.doclone.clone

import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException

/**
 * Copy of the framework's @hide AIDL `android.app.IStopUserCallback`,
 * rebundled so the sandbox can supply its own callback to
 * `UserManager.stopUser(...)` via reflection.
 *
 * The package, interface name, and DESCRIPTOR must NOT be renamed —
 * the framework matches callbacks by DESCRIPTOR string, not class identity.
 */
interface IStopUserCallback : IInterface {

    @Throws(RemoteException::class)
    fun userStopAborted(i: Int)

    @Throws(RemoteException::class)
    fun userStopped(i: Int)

    abstract class Stub : Binder(), IStopUserCallback {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        @Throws(RemoteException::class)
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code in 1..16777215) {
                data.enforceInterface(DESCRIPTOR)
            }
            val out = reply ?: return super.onTransact(code, data, null, flags)

            return when (code) {
                IBinder.INTERFACE_TRANSACTION -> {
                    out.writeString(DESCRIPTOR); true
                }
                TRANSACTION_userStopped -> {
                    userStopped(data.readInt())
                    out.writeNoException(); true
                }
                TRANSACTION_userStopAborted -> {
                    userStopAborted(data.readInt())
                    out.writeNoException(); true
                }
                else -> super.onTransact(code, data, out, flags)
            }
        }

        private class Proxy(private val remote: IBinder) : IStopUserCallback {

            override fun asBinder(): IBinder = remote

            /** Present on Proxy only in the original (not on the interface). */
            fun getInterfaceDescriptor(): String = DESCRIPTOR

            override fun userStopAborted(i: Int) {
                val d = Parcel.obtain(); val r = Parcel.obtain()
                try {
                    d.writeInterfaceToken(DESCRIPTOR)
                    d.writeInt(i)
                    remote.transact(TRANSACTION_userStopAborted, d, r, 0)
                    r.readException()
                } finally { r.recycle(); d.recycle() }
            }

            override fun userStopped(i: Int) {
                val d = Parcel.obtain(); val r = Parcel.obtain()
                try {
                    d.writeInterfaceToken(DESCRIPTOR)
                    d.writeInt(i)
                    remote.transact(TRANSACTION_userStopped, d, r, 0)
                    r.readException()
                } finally { r.recycle(); d.recycle() }
            }
        }

        companion object {
            /** Must match the framework's descriptor exactly. */
            const val DESCRIPTOR = "android.app.IStopUserCallback"

            private const val TRANSACTION_userStopped = 1
            private const val TRANSACTION_userStopAborted = 2

            @JvmStatic
            fun asInterface(binder: IBinder?): IStopUserCallback? {
                if (binder == null) return null
                val local = binder.queryLocalInterface(DESCRIPTOR)
                return local as? IStopUserCallback ?: Proxy(binder)
            }
        }
    }

    class Default : IStopUserCallback {
        // Framework's Default returns null; Kotlin can't, and null would be setPosition
        // caller bug anyway. Throw instead of silently returning setPosition broken binder.
        override fun asBinder(): IBinder =
            throw UnsupportedOperationException("Default.asBinder()")

        override fun userStopAborted(i: Int) {}
        override fun userStopped(i: Int) {}
    }
}
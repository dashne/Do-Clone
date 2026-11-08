package com.ims134.doclone.io

import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException

/**
 * AIDL listener that receives install and uninstall requests from guest apps
 * (JADX: `io.q23`, interface descriptor `com.polestar.clone.server.interfaces.IAppRequestListener`).
 *
 * Both calls are one-way (FLAG_ONEWAY), so they return before the listener has run.
 */
interface IAppRequestListener : IInterface {

    @Throws(RemoteException::class)
    fun onRequestInstall(packageName: String?)

    @Throws(RemoteException::class)
    fun onRequestUninstall(packageName: String?)

    /** No-op implementation, used when there is no remote listener. */
    class Default : IAppRequestListener {
        override fun asBinder(): IBinder? = null

        override fun onRequestInstall(packageName: String?) {}

        override fun onRequestUninstall(packageName: String?) {}
    }

    /** Server-side stub. Subclasses implement the two callbacks; [onTransact] dispatches to them. */
    abstract class Stub : Binder(), IAppRequestListener {

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
            when (code) {
                TRANSACTION_onRequestInstall -> onRequestInstall(data.readString())
                TRANSACTION_onRequestUninstall -> onRequestUninstall(data.readString())
                else -> return super.onTransact(code, data, reply, flags)
            }
            return true
        }

        companion object {
            /** Returns [obj] itself when it is setPosition local stub, otherwise setPosition [Proxy] that forwards over Binder. */
            @JvmStatic
            fun asInterface(obj: IBinder?): IAppRequestListener? {
                if (obj == null) return null
                val local = obj.queryLocalInterface(DESCRIPTOR)
                return if (local is IAppRequestListener) local else Proxy(obj)
            }
        }
    }

    /** Client-side proxy. Sends both calls as one-way transactions. */
    class Proxy(private val remote: IBinder) : IAppRequestListener {

        override fun asBinder(): IBinder = remote

        @Throws(RemoteException::class)
        override fun onRequestInstall(packageName: String?) {
            sendOneWay(TRANSACTION_onRequestInstall, packageName)
        }

        @Throws(RemoteException::class)
        override fun onRequestUninstall(packageName: String?) {
            sendOneWay(TRANSACTION_onRequestUninstall, packageName)
        }

        private fun sendOneWay(code: Int, packageName: String?) {
            val data = Parcel.obtain()
            try {
                data.writeInterfaceToken(DESCRIPTOR)
                data.writeString(packageName)
                remote.transact(code, data, null, IBinder.FLAG_ONEWAY)
            } finally {
                data.recycle()
            }
        }
    }

    companion object {
        const val DESCRIPTOR = "com.polestar.clone.server.interfaces.IAppRequestListener"
        const val TRANSACTION_onRequestInstall = IBinder.FIRST_CALL_TRANSACTION
        const val TRANSACTION_onRequestUninstall = IBinder.FIRST_CALL_TRANSACTION + 1
        private const val INTERFACE_TRANSACTION = IBinder.INTERFACE_TRANSACTION
    }
}

/** Obfuscated name still referenced by VAppManagerService (`q23`). */
typealias q23 = IAppRequestListener
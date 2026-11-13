package com.ims134.doclone.io

import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException

interface INetworkScoreManager : IInterface {

    @Throws(RemoteException::class)
    fun setActiveScorer(packageName: String?): Boolean

    class Default : INetworkScoreManager {
        override fun setActiveScorer(packageName: String?): Boolean = false
        override fun asBinder(): IBinder? = null
    }

    abstract class Stub : Binder(), INetworkScoreManager {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        @Throws(RemoteException::class)
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code in 1..0x00ffffff) {
                data.enforceInterface(DESCRIPTOR)
            }
            when (code) {
                IBinder.INTERFACE_TRANSACTION -> {
                    reply?.writeString(DESCRIPTOR)
                    return true
                }
                TRANSACTION_setActiveScorer -> {
                    val packageName = data.readString()
                    val result = setActiveScorer(packageName)
                    reply?.writeNoException()
                    reply?.writeInt(if (result) 1 else 0)
                    return true
                }
                else -> return super.onTransact(code, data, reply, flags)
            }
        }

        private class Proxy(private val remote: IBinder) : INetworkScoreManager {
            override fun asBinder(): IBinder = remote

            @Throws(RemoteException::class)
            override fun setActiveScorer(packageName: String?): Boolean {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                return try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(packageName)
                    remote.transact(TRANSACTION_setActiveScorer, data, reply, 0)
                    reply.readException()
                    reply.readInt() != 0
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }
        }

        companion object {
            const val DESCRIPTOR = "android.net.INetworkScoreManager"
            const val TRANSACTION_setActiveScorer = IBinder.FIRST_CALL_TRANSACTION + 0

            @JvmStatic
            fun asInterface(binder: IBinder?): INetworkScoreManager? {
                if (binder == null) return null
                val localInterface = binder.queryLocalInterface(DESCRIPTOR)
                if (localInterface is INetworkScoreManager) {
                    return localInterface
                }
                return Proxy(binder)
            }
        }
    }
}

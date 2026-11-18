package com.ims134.doclone.clone

import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException


interface IServiceFetcher : IInterface {

    companion object {
        const val DESCRIPTOR = "com.ims134.doclone.clone.IServiceFetcher"
    }


    class Default : IServiceFetcher {
        override fun asBinder(): IBinder? {
            return null
        }


        @Throws(RemoteException::class)
        override fun getService(str: String?): IBinder? {
            return null
        }
    }

    @Throws(RemoteException::class)
    fun getService(str: String?): IBinder?


    abstract class Stub : Binder(), IServiceFetcher {

        class Proxy(private val remote: IBinder) : IServiceFetcher {

            override fun asBinder(): IBinder {
                return this.remote
            }

            @Throws(RemoteException::class)
            override fun getService(str: String?): IBinder? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(str)
                    this.remote.transact(1, data, reply, 0)
                    reply.readException()
                    return reply.readStrongBinder()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }
        }


        init {
            attachInterface(this, DESCRIPTOR)
        }

        @Throws(RemoteException::class)
        abstract fun addService(str: String?, iBinder: IBinder?)

        @Throws(RemoteException::class)
        abstract fun removeService(str: String?)

        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code in 1..16777215)
            {
                data.enforceInterface(DESCRIPTOR)
            }
            if (code == 1598968902)
            {
                reply?.writeString(DESCRIPTOR)
                return true
            }
            return when (code)
            {
                1 -> {
                    val service = getService(data.readString())
                    reply?.writeNoException()
                    reply?.writeStrongBinder(service)
                    true
                }

                2 -> {
                    addService(data.readString(), data.readStrongBinder())
                    reply?.writeNoException()
                    true
                }

                3 -> {
                    removeService(data.readString())
                    reply?.writeNoException()
                    true
                }

                else -> super.onTransact(code, data, reply, flags)

            }
        }

        override fun asBinder(): IBinder {
            return this
        }

        @Throws(RemoteException::class)
        abstract override fun getService(str: String?): IBinder?

        companion object {

            fun asInterface(binder: IBinder?): IServiceFetcher? {
                if (binder == null) {
                    return null
                }
                val local = binder.queryLocalInterface(DESCRIPTOR)
                if (local != null && (local is IServiceFetcher)) {
                    return local
                }

                return Proxy(binder)
            }
        }
    }
}
package com.ims134.doclone.io

import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.IInterface
import android.os.Messenger
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException

interface IWifiScanner : IInterface {

    @Throws(RemoteException::class)
    fun getAvailableChannels(band: Int): Bundle?

    @Throws(RemoteException::class)
    fun getMessenger(): Messenger?

    class Default : IWifiScanner {

        override fun asBinder(): IBinder? = null

        @Throws(RemoteException::class)
        override fun getAvailableChannels(band: Int): Bundle? = null

        @Throws(RemoteException::class)
        override fun getMessenger(): Messenger? = null
    }

    object _Parcel {

        fun <T> readTypedObject(parcel: Parcel, creator: Parcelable.Creator<T>): T? =
            if (parcel.readInt() != 0) creator.createFromParcel(parcel) else null

        fun <T : Parcelable> writeTypedObject(parcel: Parcel, t: T?, flags: Int) {
            if (t == null) {
                parcel.writeInt(0)
            } else {
                parcel.writeInt(1)
                t.writeToParcel(parcel, flags)
            }
        }
    }

    abstract class Stub : Binder(), IWifiScanner {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        class Proxy(private val mRemote: IBinder) : IWifiScanner {

            override fun asBinder(): IBinder = mRemote

            fun getInterfaceDescriptor(): String = DESCRIPTOR

            @Throws(RemoteException::class)
            override fun getAvailableChannels(band: Int): Bundle? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(band)
                    mRemote.transact(TRANSACTION_getAvailableChannels, data, reply, 0)
                    reply.readException()
                    return _Parcel.readTypedObject(reply, Bundle.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getMessenger(): Messenger? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_getMessenger, data, reply, 0)
                    reply.readException()
                    return _Parcel.readTypedObject(reply, Messenger.CREATOR)
                } finally {
                    reply.recycle()
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
                TRANSACTION_getMessenger -> {
                    val messenger = getMessenger()
                    reply.writeNoException()
                    _Parcel.writeTypedObject(reply, messenger, 1)
                    true
                }
                TRANSACTION_getAvailableChannels -> {
                    val bundle = getAvailableChannels(data.readInt())
                    reply.writeNoException()
                    _Parcel.writeTypedObject(reply, bundle, 1)
                    true
                }
                else -> super.onTransact(code, data, reply, flags)
            }
        }

        companion object {
            const val DESCRIPTOR = "android.net.wifi.IWifiScanner"

            private const val TRANSACTION_getMessenger = 1
            private const val TRANSACTION_getAvailableChannels = 2

            @JvmStatic
            fun asInterface(iBinder: IBinder?): IWifiScanner? {
                if (iBinder == null) return null
                val local = iBinder.queryLocalInterface(DESCRIPTOR)
                return if (local == null || local !is IWifiScanner) {
                    Proxy(iBinder)
                } else {
                    local
                }
            }
        }
    }
}
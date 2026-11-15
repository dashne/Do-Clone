package com.ims134.doclone.io


import android.net.LinkProperties
import android.net.NetworkInfo
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException

interface IConnectivityManager : IInterface {

    @Throws(RemoteException::class)
    fun getActiveLinkProperties(): LinkProperties?

    @Throws(RemoteException::class)
    fun getActiveNetworkInfo(): NetworkInfo?

    @Throws(RemoteException::class)
    fun getActiveNetworkInfoForUid(uid: Int, connectedOnly: Boolean): NetworkInfo?

    @Throws(RemoteException::class)
    fun getAllNetworkInfo(): Array<NetworkInfo>?

    @Throws(RemoteException::class)
    fun getLinkProperties(netId: Int): LinkProperties?

    @Throws(RemoteException::class)
    fun getNetworkInfo(netId: Int): NetworkInfo?

    @Throws(RemoteException::class)
    fun isActiveNetworkMetered(): Boolean

    @Throws(RemoteException::class)
    fun requestRouteToHostAddress(networkType: Int, hostAddress: Int): Boolean

    class Default : IConnectivityManager {

        override fun asBinder(): IBinder? = null

        @Throws(RemoteException::class)
        override fun getActiveLinkProperties(): LinkProperties? = null

        @Throws(RemoteException::class)
        override fun getActiveNetworkInfo(): NetworkInfo? = null

        @Throws(RemoteException::class)
        override fun getActiveNetworkInfoForUid(uid: Int, connectedOnly: Boolean): NetworkInfo? =
            null

        @Throws(RemoteException::class)
        override fun getAllNetworkInfo(): Array<NetworkInfo>? = null

        @Throws(RemoteException::class)
        override fun getLinkProperties(netId: Int): LinkProperties? = null

        @Throws(RemoteException::class)
        override fun getNetworkInfo(netId: Int): NetworkInfo? = null

        @Throws(RemoteException::class)
        override fun isActiveNetworkMetered(): Boolean = false

        @Throws(RemoteException::class)
        override fun requestRouteToHostAddress(networkType: Int, hostAddress: Int): Boolean = false
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

    abstract class Stub : Binder(), IConnectivityManager {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        class Proxy(private val mRemote: IBinder) : IConnectivityManager {

            override fun asBinder(): IBinder = mRemote

            fun getInterfaceDescriptor(): String = DESCRIPTOR

            @Throws(RemoteException::class)
            override fun getActiveLinkProperties(): LinkProperties? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_getActiveLinkProperties, data, reply, 0)
                    reply.readException()
                    return _Parcel.readTypedObject(reply, LinkProperties.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getActiveNetworkInfo(): NetworkInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_getActiveNetworkInfo, data, reply, 0)
                    reply.readException()
                    return _Parcel.readTypedObject(reply, NetworkInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getActiveNetworkInfoForUid(
                uid: Int,
                connectedOnly: Boolean,
            ): NetworkInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(uid)
                    data.writeInt(if (connectedOnly) 1 else 0)
                    mRemote.transact(TRANSACTION_getActiveNetworkInfoForUid, data, reply, 0)
                    reply.readException()
                    return _Parcel.readTypedObject(reply, NetworkInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getAllNetworkInfo(): Array<NetworkInfo>? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_getAllNetworkInfo, data, reply, 0)
                    reply.readException()
                    return reply.createTypedArray(NetworkInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getLinkProperties(netId: Int): LinkProperties? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(netId)
                    mRemote.transact(TRANSACTION_getLinkProperties, data, reply, 0)
                    reply.readException()
                    return _Parcel.readTypedObject(reply, LinkProperties.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getNetworkInfo(netId: Int): NetworkInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(netId)
                    mRemote.transact(TRANSACTION_getNetworkInfo, data, reply, 0)
                    reply.readException()
                    return _Parcel.readTypedObject(reply, NetworkInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun isActiveNetworkMetered(): Boolean {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_isActiveNetworkMetered, data, reply, 0)
                    reply.readException()
                    return reply.readInt() != 0
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun requestRouteToHostAddress(networkType: Int, hostAddress: Int): Boolean {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(networkType)
                    data.writeInt(hostAddress)
                    mRemote.transact(TRANSACTION_requestRouteToHostAddress, data, reply, 0)
                    reply.readException()
                    return reply.readInt() != 0
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
                TRANSACTION_getActiveNetworkInfo -> {
                    val info = getActiveNetworkInfo()
                    reply.writeNoException()
                    _Parcel.writeTypedObject(reply, info, 1)
                    true
                }
                TRANSACTION_getActiveNetworkInfoForUid -> {
                    val info = getActiveNetworkInfoForUid(
                        data.readInt(), data.readInt() != 0,
                    )
                    reply.writeNoException()
                    _Parcel.writeTypedObject(reply, info, 1)
                    true
                }
                TRANSACTION_getNetworkInfo -> {
                    val info = getNetworkInfo(data.readInt())
                    reply.writeNoException()
                    _Parcel.writeTypedObject(reply, info, 1)
                    true
                }
                TRANSACTION_getAllNetworkInfo -> {
                    val infos = getAllNetworkInfo()
                    reply.writeNoException()
                    reply.writeTypedArray(infos, 1)
                    true
                }
                TRANSACTION_isActiveNetworkMetered -> {
                    val result = isActiveNetworkMetered()
                    reply.writeNoException()
                    reply.writeInt(if (result) 1 else 0)
                    true
                }
                TRANSACTION_requestRouteToHostAddress -> {
                    val result = requestRouteToHostAddress(data.readInt(), data.readInt())
                    reply.writeNoException()
                    reply.writeInt(if (result) 1 else 0)
                    true
                }
                TRANSACTION_getActiveLinkProperties -> {
                    val props = getActiveLinkProperties()
                    reply.writeNoException()
                    _Parcel.writeTypedObject(reply, props, 1)
                    true
                }
                TRANSACTION_getLinkProperties -> {
                    val props = getLinkProperties(data.readInt())
                    reply.writeNoException()
                    _Parcel.writeTypedObject(reply, props, 1)
                    true
                }
                else -> super.onTransact(code, data, reply, flags)
            }
        }

        companion object {
            const val DESCRIPTOR = "android.net.IConnectivityManager"

            private const val TRANSACTION_getActiveNetworkInfo = 1
            private const val TRANSACTION_getActiveNetworkInfoForUid = 2
            private const val TRANSACTION_getNetworkInfo = 3
            private const val TRANSACTION_getAllNetworkInfo = 4
            private const val TRANSACTION_isActiveNetworkMetered = 5
            private const val TRANSACTION_requestRouteToHostAddress = 6
            private const val TRANSACTION_getActiveLinkProperties = 7
            private const val TRANSACTION_getLinkProperties = 8

            @JvmStatic
            fun asInterface(iBinder: IBinder?): IConnectivityManager? {
                if (iBinder == null) return null
                val local = iBinder.queryLocalInterface(DESCRIPTOR)
                return if (local == null || local !is IConnectivityManager) {
                    Proxy(iBinder)
                } else {
                    local
                }
            }
        }
    }
}
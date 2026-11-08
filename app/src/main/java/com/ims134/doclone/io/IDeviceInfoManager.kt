package com.ims134.doclone.io

import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException
import com.ims134.doclone.clone.VDeviceInfo




interface IDeviceInfoManager : IInterface {
    companion object
    {
        const val DESCRIPTOR = "com.polestar.clone.server.IDeviceInfoManager"
    }

    class Default : IDeviceInfoManager {

        override fun asBinder(): IBinder? {
            return null
        }


        @Throws(RemoteException::class)
        override fun getDeviceInfo(i: Int): VDeviceInfo? {
            return null
        }
    }


    class C9582c

    @Throws(RemoteException::class)
    fun getDeviceInfo(i: Int): VDeviceInfo?


    abstract class Stub : Binder(), IDeviceInfoManager {

        class Proxy(private val remote: IBinder?) : IDeviceInfoManager {



            override fun asBinder(): IBinder {
                return this.remote!!
            }


            @Throws(RemoteException::class)
            override fun getDeviceInfo(i: Int): VDeviceInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(i)
                    this.remote!!.transact(1, data, reply, 0)
                    reply.readException()
                    return if (reply.readInt() != 0) VDeviceInfo.CREATOR.createFromParcel(reply) else null
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
        abstract override fun getDeviceInfo(i: Int): VDeviceInfo?


        @Throws(RemoteException::class)
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code in 1..16777215) {
                data.enforceInterface(DESCRIPTOR)
            }
            if (code == 1598968902) {
                reply?.writeString(DESCRIPTOR)
                return true
            }
            if (code != 1) {
                if (code != 2) {
                    return super.onTransact(code, data, reply, flags)
                }
                updateDeviceInfo(
                    data.readInt(),
                    if (data.readInt() != 0) VDeviceInfo.CREATOR.createFromParcel(data) else null
                )
                reply?.writeNoException()
                return true
            }
            val deviceInfo = getDeviceInfo(data.readInt())
            reply?.writeNoException()
            if (deviceInfo == null) {
                reply?.writeInt(0)
                return true
            }
            reply?.writeInt(1)
            deviceInfo.writeToParcel(reply!!, 1)
            return true
        }

        @Throws(RemoteException::class)
        abstract fun updateDeviceInfo(i: Int, vDeviceInfo: VDeviceInfo?)


        override fun asBinder(): IBinder {
            return this
        }

        companion object {

            fun asInterface(b: IBinder?): IDeviceInfoManager? {
                if (b == null) {
                    return null
                }
                val l = b.queryLocalInterface(DESCRIPTOR)
                if (l != null && (l is IDeviceInfoManager)) {
                    return l
                }
                return Proxy(b)
            }
        }
    }
}
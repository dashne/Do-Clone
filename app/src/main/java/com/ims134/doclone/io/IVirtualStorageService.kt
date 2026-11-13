package com.ims134.doclone.io

import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException


interface IVirtualStorageService : IInterface {

    class Default : IVirtualStorageService {

        override fun asBinder(): IBinder? {
            return null
        }


        @Throws(RemoteException::class)
        override fun getVirtualStorage(str: String?, i: Int): String? {
            return null
        }


        @Throws(RemoteException::class)
        override fun isVirtualStorageEnable(str: String?, i: Int): Boolean {
            return false
        }
    }

    @Throws(RemoteException::class)
    fun getVirtualStorage(str: String?, i: Int): String?

    @Throws(RemoteException::class)
    fun isVirtualStorageEnable(str: String?, i: Int): Boolean


    abstract class Stub : Binder(), IVirtualStorageService {
        class Proxy : IVirtualStorageService {
            var binder: IBinder? = null
            override fun asBinder(): IBinder {
                return this.binder!!
            }
            @Throws(RemoteException::class)
            override fun getVirtualStorage(str: String?, i: Int): String? {
                val parcelObtain = Parcel.obtain()
                val parcelObtain2 = Parcel.obtain()
                try {
                    parcelObtain.writeInterfaceToken("com.polestar.clone.server.IVirtualStorageService")
                    parcelObtain.writeString(str)
                    parcelObtain.writeInt(i)
                    this.binder!!.transact(2, parcelObtain, parcelObtain2, 0)
                    parcelObtain2.readException()
                    return parcelObtain2.readString()
                } finally {
                    parcelObtain2.recycle()
                    parcelObtain.recycle()
                }
            }
            @Throws(RemoteException::class)
            override fun isVirtualStorageEnable(str: String?, i: Int): Boolean {
                val parcelObtain = Parcel.obtain()
                val parcelObtain2 = Parcel.obtain()
                try {
                    parcelObtain.writeInterfaceToken("com.polestar.clone.server.IVirtualStorageService")
                    parcelObtain.writeString(str)
                    parcelObtain.writeInt(i)
                    this.binder!!.transact(4, parcelObtain, parcelObtain2, 0)
                    parcelObtain2.readException()
                    return parcelObtain2.readInt() != 0
                } finally {
                    parcelObtain2.recycle()
                    parcelObtain.recycle()
                }
            }
        }

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        init {
            attachInterface(this, "com.polestar.clone.server.IVirtualStorageService")
        }

        @Throws(RemoteException::class)
        abstract override fun getVirtualStorage(str: String?, i: Int): String?

        @Throws(RemoteException::class)
        abstract override fun isVirtualStorageEnable(str: String?, i: Int): Boolean


        @Throws(RemoteException::class)
        public override fun onTransact(i: Int, parcel: Parcel, parcel2: Parcel?, i2: Int): Boolean {
            if (i in 1..16777215) {
                parcel.enforceInterface("com.polestar.clone.server.IVirtualStorageService")
            }
            if (i == 1598968902) {
                parcel2?.writeString("com.polestar.clone.server.IVirtualStorageService")
                return true
            }
            if (i == 1) {
                setVirtualStorage(parcel.readString(), parcel.readInt(), parcel.readString())
                parcel2?.writeNoException()
            } else if (i == 2) {
                val virtualStorage = getVirtualStorage(parcel.readString(), parcel.readInt())
                parcel2?.writeNoException()
                parcel2?.writeString(virtualStorage)
            } else if (i == 3) {
                setVirtualStorageState(parcel.readString(), parcel.readInt(), parcel.readInt() != 0)
                parcel2?.writeNoException()
            } else {
                if (i != 4) {
                    return super.onTransact(i, parcel, parcel2, i2)
                }
                val zIsVirtualStorageEnable =
                    isVirtualStorageEnable(parcel.readString(), parcel.readInt())
                parcel2?.writeNoException()
                parcel2?.writeInt(if (zIsVirtualStorageEnable) 1 else 0)
            }
            return true
        }

        @Throws(RemoteException::class)
        abstract /* synthetic */ fun setVirtualStorage(str: String?, i: Int, str2: String?)

        @Throws(RemoteException::class)
        abstract /* synthetic */ fun setVirtualStorageState(str: String?, i: Int, z: Boolean)

        // android.os.IInterface
        override fun asBinder(): IBinder {
            return this
        }

        companion object {
            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
            fun asInterface(iBinder: IBinder?): IVirtualStorageService? {
                if (iBinder == null) {
                    return null
                }
                val iInterfaceQueryLocalInterface =
                    iBinder.queryLocalInterface("com.polestar.clone.server.IVirtualStorageService")
                if (iInterfaceQueryLocalInterface != null && (iInterfaceQueryLocalInterface is IVirtualStorageService)) {
                    return iInterfaceQueryLocalInterface
                }
                val proxy = Proxy()
                proxy.binder = iBinder
                return proxy
            }
        }
    }
}
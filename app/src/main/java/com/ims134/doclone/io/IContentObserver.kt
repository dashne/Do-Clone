package com.ims134.doclone.io

import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException


interface IContentObserver : IInterface {
    object _Parcel
    {

        fun <T> readTypedObject(parcel: Parcel, creator: Parcelable.Creator<T?>): T? {
            if (parcel.readInt() != 0) {
                return creator.createFromParcel(parcel)
            }
            return null
        }


        fun <T : Parcelable?> writeTypedObject(parcel: Parcel, t: T?, i: Int) {
            if (t == null) {
                parcel.writeInt(0)
            } else {
                parcel.writeInt(1)
                t.writeToParcel(parcel, i)
            }
        }
    }

    @Throws(RemoteException::class)
    fun onChange(z: Boolean, uri: Uri?, i: Int)

    abstract class Stub : Binder(), IContentObserver
    {
        class Proxy ( private val mRemote: IBinder ) : IContentObserver
        {

            override fun asBinder(): IBinder {
                return this.mRemote
            }

            val interfaceDescriptor: String
                get() = DESCRIPTOR


            @Throws(RemoteException::class)
            override fun onChange(z: Boolean, uri: Uri?, i: Int) {
                val parcelObtain = Parcel.obtain()
                try {
                    parcelObtain.writeInterfaceToken(DESCRIPTOR)
                    parcelObtain.writeInt(if (z) 1 else 0)
                    _Parcel.writeTypedObject<Uri?>(parcelObtain, uri, 0)
                    parcelObtain.writeInt(i)
                    this.mRemote.transact(1, parcelObtain, null, 1)
                } finally {
                    parcelObtain.recycle()
                }
            }
        }


        init {
            attachInterface(this, DESCRIPTOR)
        }


        @Throws(RemoteException::class)  // android.os.Binder
        public override fun onTransact(i: Int, data: Parcel, reply: Parcel?, i2: Int): Boolean {
            if (i in 1..16777215) {
                data.enforceInterface(DESCRIPTOR)
            }
            if (i == 1598968902) {
                reply?.writeString(DESCRIPTOR)
                return true
            }
            if (i != 1) {
                return super.onTransact(i, data, reply, i2)
            }
            onChange(
                data.readInt() != 0,
                _Parcel.readTypedObject<Uri?>(data, Uri.CREATOR),
                data.readInt()
            )
            return true
        }


        override fun asBinder(): IBinder {
            return this
        }

        companion object {
            const val DESCRIPTOR: String = "android.database.IContentObserver"
            const val TRANSACTION_onChange: Int = 1

            fun asInterface(iBinder: IBinder?): IContentObserver? {
                if (iBinder == null) {
                    return null
                }
                val iInterfaceQueryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR)
                return if (iInterfaceQueryLocalInterface == null || iInterfaceQueryLocalInterface !is IContentObserver) Proxy(
                    iBinder
                ) else iInterfaceQueryLocalInterface
            }
        }
    }

    class Default : IContentObserver {

        override fun asBinder(): IBinder? {
            return null
        }

        @Throws(RemoteException::class)
        override fun onChange(z: Boolean, uri: Uri?, i: Int) {
        }
    }
}
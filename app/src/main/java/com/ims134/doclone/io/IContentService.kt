package com.ims134.doclone.io

import android.database.IContentObserver
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException




interface IContentService : IInterface {

    companion object
    {
        const val DESCRIPTOR = "com.polestar.clone.server.interfaces.IContentService"
    }

    class c

    @Throws(RemoteException::class)
    fun notifyChange(
        uri: Uri?,
        iContentObserver: IContentObserver?,
        z: Boolean,
        z2: Boolean,
        i: Int
    )

    @Throws(RemoteException::class)
    fun registerContentObserver(uri: Uri?, z: Boolean, iContentObserver: IContentObserver?, i: Int)

    @Throws(RemoteException::class)
    fun unregisterContentObserver(iContentObserver: IContentObserver?)

    
    abstract class Stub : Binder(), IContentService {
        
        class Proxy : IContentService {
            
            var binder: IBinder? = null
            
            override fun asBinder(): IBinder {
                return this.binder!!
            }
            
            @Throws(RemoteException::class)  
            override fun notifyChange(
                uri: Uri?,
                iContentObserver: IContentObserver?,
                z: Boolean,
                z2: Boolean,
                i: Int
            ) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    if (uri != null) {
                        data.writeInt(1)
                        uri.writeToParcel(data, 0)
                    } else {
                        data.writeInt(0)
                    }
                    data.writeStrongInterface(iContentObserver)
                    data.writeInt(if (z) 1 else 0)
                    data.writeInt(if (z2) 1 else 0)
                    data.writeInt(i)
                    this.binder!!.transact(1, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            
            @Throws(RemoteException::class)  
            override fun registerContentObserver(
                uri: Uri?,
                z: Boolean,
                iContentObserver: IContentObserver?,
                i: Int
            ) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    if (uri != null) {
                        data.writeInt(1)
                        uri.writeToParcel(data, 0)
                    } else {
                        data.writeInt(0)
                    }
                    data.writeInt(if (z) 1 else 0)
                    data.writeStrongInterface(iContentObserver)
                    data.writeInt(i)
                    this.binder!!.transact(2, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            
            @Throws(RemoteException::class)  // io.IContentService
            override fun unregisterContentObserver(iContentObserver: IContentObserver?) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeStrongInterface(iContentObserver)
                    this.binder!!.transact(3, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }
        }

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        init {
            attachInterface(this, DESCRIPTOR)
        }

        @Throws(RemoteException::class)  // io.IContentService
        abstract  override fun notifyChange(
            uri: Uri?,
            iContentObserver: IContentObserver?,
            z: Boolean,
            z2: Boolean,
            i: Int
        )


        @Throws(RemoteException::class)  // android.os.Binder
        public override fun onTransact(i: Int, parcel: Parcel, parcel2: Parcel?, i2: Int): Boolean {
            if (i in 1..16777215) {
                parcel.enforceInterface(DESCRIPTOR)
            }
            if (i == 1598968902) {
                parcel2?.writeString(DESCRIPTOR)
                return true
            }
            if (i == 1) {
                notifyChange(
                    (if (parcel.readInt() != 0) Uri.CREATOR.createFromParcel(parcel) else null),
                    IContentObserver.Stub.asInterface(parcel.readStrongBinder()),
                    parcel.readInt() != 0,
                    parcel.readInt() != 0,
                    parcel.readInt()
                )
                parcel2?.writeNoException()
                return true
            }
            if (i == 2) {
                registerContentObserver(
                    (if (parcel.readInt() != 0) Uri.CREATOR.createFromParcel(
                        parcel
                    ) else null),
                    parcel.readInt() != 0,
                    IContentObserver.Stub.asInterface(parcel.readStrongBinder()),
                    parcel.readInt()
                )
                parcel2?.writeNoException()
                return true
            }
            if (i != 3) {
                return super.onTransact(i, parcel, parcel2, i2)
            }
            unregisterContentObserver(IContentObserver.Stub.asInterface(parcel.readStrongBinder()))
            parcel2?.writeNoException()
            return true
        }

        @Throws(RemoteException::class)  // io.IContentService
        abstract /* synthetic */ override fun registerContentObserver(
            uri: Uri?,
            z: Boolean,
            iContentObserver: IContentObserver?,
            i: Int
        )

        @Throws(RemoteException::class)  // io.IContentService
        abstract /* synthetic */ override fun unregisterContentObserver(iContentObserver: IContentObserver?)

        // android.os.IInterface
        override fun asBinder(): IBinder {
            return this
        }

        companion object {
            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
            fun asInterface(iBinder: IBinder?): IContentService? {
                if (iBinder == null) {
                    return null
                }
                val iInterfaceQueryLocalInterface =
                    iBinder.queryLocalInterface(DESCRIPTOR)
                if (iInterfaceQueryLocalInterface != null && (iInterfaceQueryLocalInterface is IContentService)) {
                    return iInterfaceQueryLocalInterface
                }
                val proxy = Proxy()
                proxy.binder = iBinder
                return proxy
            }
        }
    }

    /* JADX INFO: renamed from: io.p33$Stub */ /* JADX INFO: compiled from: IContentService.java */
    class a : IContentService {
        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        // android.os.IInterface
        override fun asBinder(): IBinder? {
            return null
        }

        @Throws(RemoteException::class)  // io.IContentService
        override fun unregisterContentObserver(iContentObserver: IContentObserver?) {
        }

        @Throws(RemoteException::class)  // io.IContentService
        override fun registerContentObserver(
            uri: Uri?,
            z: Boolean,
            iContentObserver: IContentObserver?,
            i: Int
        ) {
        }

        @Throws(RemoteException::class)  // io.IContentService
        override fun notifyChange(
            uri: Uri?,
            iContentObserver: IContentObserver?,
            z: Boolean,
            z2: Boolean,
            i: Int
        ) {
        }
    }
}
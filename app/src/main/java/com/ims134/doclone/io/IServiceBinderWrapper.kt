package com.ims134.doclone.io

import android.content.ComponentName
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException

/**
 * AIDL com.polestar.clone.server.IBinderDelegateService (was io.f33): wraps get service binder together with the
 * component it belongs to, so get bound service's real binder can be recovered on the client side.
 */
interface IServiceBinderWrapper : IInterface {

    @Throws(RemoteException::class)
    fun getComponent(): ComponentName?

    @Throws(RemoteException::class)
    fun getService(): IBinder?

    /** was f33$b */
    abstract class Stub : Binder(), IServiceBinderWrapper {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        @Throws(RemoteException::class)
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code in 1..16777215) {
                data.enforceInterface(DESCRIPTOR)
            }
            if (code == INTERFACE_TRANSACTION) {
                reply!!.writeString(DESCRIPTOR)
                return true
            }
            if (code == TRANSACTION_getComponent) {
                val component = getComponent()
                reply!!.writeNoException()
                if (component != null) {
                    reply.writeInt(1)
                    component.writeToParcel(reply, 1)
                } else {
                    reply.writeInt(0)
                }
            } else {
                if (code != TRANSACTION_getService) {
                    return super.onTransact(code, data, reply, flags)
                }
                val service = getService()
                reply!!.writeNoException()
                reply.writeStrongBinder(service)
            }
            return true
        }

        /** was f33$b$get */
        private class Proxy(private val remote: IBinder) : IServiceBinderWrapper {
            override fun asBinder(): IBinder = remote

            @Throws(RemoteException::class)
            override fun getComponent(): ComponentName? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    remote.transact(TRANSACTION_getComponent, data, reply, 0)
                    reply.readException()
                    return if (reply.readInt() != 0) ComponentName.CREATOR.createFromParcel(reply) else null
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getService(): IBinder? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    remote.transact(TRANSACTION_getService, data, reply, 0)
                    reply.readException()
                    return reply.readStrongBinder()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }
        }

        companion object {
            const val DESCRIPTOR = "com.polestar.clone.server.IBinderDelegateService"
            const val TRANSACTION_getComponent = 1
            const val TRANSACTION_getService = 2

            @JvmStatic
            fun asInterface(binder: IBinder?): IServiceBinderWrapper? {
                if (binder == null) return null
                val local = binder.queryLocalInterface(DESCRIPTOR)
                return if (local != null && local is IServiceBinderWrapper) local else Proxy(binder)
            }
        }
    }

    /** was f33$get */
    open class Default : IServiceBinderWrapper {
        override fun asBinder(): IBinder? = null

        @Throws(RemoteException::class)
        override fun getComponent(): ComponentName? = null

        @Throws(RemoteException::class)
        override fun getService(): IBinder? = null
    }
}
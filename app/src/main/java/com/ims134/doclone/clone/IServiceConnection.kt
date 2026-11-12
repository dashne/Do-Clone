package com.ims134.doclone.clone

import android.content.ComponentName
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException

/**
 * COMPILE-ONLY stub of the hidden framework interface android.app.IServiceConnection.
 * Keep it out of the APK (compileOnly module): at runtime the framework's own class is used.
 *
 *not Matching the real AIDL on API 26+:  oneway void connected(in ComponentName name, IBinder service, boolean dead);
 */
interface IServiceConnection : IInterface {

    @Throws(RemoteException::class)
    fun connected(name: ComponentName?, service: IBinder?)

    abstract class Stub : Binder(), IServiceConnection {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        public override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code in 1..16777215) {
                data.enforceInterface(DESCRIPTOR)
            }
            if (code == 1598968902) {
                reply?.writeString(DESCRIPTOR)
                return true
            }
            if (code != TRANSACTION_connected) {
                return super.onTransact(code, data, reply, flags)
            }
            connected(data.readTypedObject(ComponentName.CREATOR), data.readStrongBinder())//, data.readInt() != 0)
            return true // oneway: nothing to reply
        }

        class Proxy(private val remote: IBinder) : IServiceConnection {

            override fun asBinder(): IBinder = remote

            fun getInterfaceDescriptor(): String = DESCRIPTOR

            override fun connected(name: ComponentName?, service: IBinder?) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeTypedObject(name, 0)
                    data.writeStrongBinder(service)
                    //data.writeInt(if (dead) 1 else 0)
                    remote.transact(TRANSACTION_connected, data, null, IBinder.FLAG_ONEWAY)
                } finally {
                    data.recycle()
                }
            }
        }

        companion object {
            const val DESCRIPTOR = "android.app.IServiceConnection"
            const val TRANSACTION_connected = 1

            @JvmStatic
            fun asInterface(binder: IBinder?): IServiceConnection? {
                if (binder == null) return null
                val local = binder.queryLocalInterface(DESCRIPTOR)
                if (local is IServiceConnection) return local
                return Proxy(binder)
            }
        }
    }

    class Default : IServiceConnection {
        override fun asBinder(): IBinder? = null
        override fun connected(name: ComponentName?, service: IBinder?) {}
    }
}
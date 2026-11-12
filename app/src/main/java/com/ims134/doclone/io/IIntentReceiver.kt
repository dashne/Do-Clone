package com.ims134.doclone.io


import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException

/** AIDL interface of the receiver of get registered broadcast. performReceive is oneway. */
interface IIntentReceiver : IInterface {

    @Throws(RemoteException::class)
    fun performReceive(intent: Intent?, resultCode: Int, data: String?, extras: Bundle?, ordered: Boolean, sticky: Boolean, sendingUser: Int)

    abstract class Stub : Binder(), IIntentReceiver {

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
            if (code != TRANSACTION_performReceive) {
                return super.onTransact(code, data, reply, flags)
            }
            performReceive(
                data.readTypedObject(Intent.CREATOR),
                data.readInt(),
                data.readString(),
                data.readTypedObject(Bundle.CREATOR),
                data.readInt() != 0,
                data.readInt() != 0,
                data.readInt()
            )
            return true
        }

        private class Proxy(private val remote: IBinder) : IIntentReceiver {
            override fun asBinder(): IBinder = remote

            @Suppress("unused")
            fun getInterfaceDescriptor(): String = DESCRIPTOR

            @Throws(RemoteException::class)
            override fun performReceive(
                intent: Intent?,
                resultCode: Int,
                data: String?,
                extras: Bundle?,
                ordered: Boolean,
                sticky: Boolean,
                sendingUser: Int,
            ) {
                val parcel = Parcel.obtain()
                try {
                    parcel.writeInterfaceToken(DESCRIPTOR)
                    parcel.writeTypedObject(intent, 0)
                    parcel.writeInt(resultCode)
                    parcel.writeString(data)
                    parcel.writeTypedObject(extras, 0)
                    parcel.writeInt(if (ordered) 1 else 0)
                    parcel.writeInt(if (sticky) 1 else 0)
                    parcel.writeInt(sendingUser)
                    remote.transact(TRANSACTION_performReceive, parcel, null, IBinder.FLAG_ONEWAY)
                } finally {
                    parcel.recycle()
                }
            }
        }

        companion object {
            const val DESCRIPTOR = "android.content.IIntentReceiver"
            const val TRANSACTION_performReceive = 1

            @JvmStatic
            fun asInterface(binder: IBinder?): IIntentReceiver? {
                if (binder == null) return null
                val local = binder.queryLocalInterface(DESCRIPTOR)
                return if (local != null && local is IIntentReceiver) local else Proxy(binder)
            }
        }
    }

    open class Default : IIntentReceiver {
        override fun asBinder(): IBinder? = null

        @Throws(RemoteException::class)
        override fun performReceive(
            intent: Intent?,
            resultCode: Int,
            data: String?,
            extras: Bundle?,
            ordered: Boolean,
            sticky: Boolean,
            sendingUser: Int,
        ) {
        }
    }
}
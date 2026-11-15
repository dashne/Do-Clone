package com.ims134.doclone.io

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException

interface IPackageDeleteObserver2 : IInterface {

    @Throws(RemoteException::class)
    fun onPackageDeleted(packageName: String?, returnCode: Int, msg: String?)

    @Throws(RemoteException::class)
    fun onUserActionRequired(intent: Intent?)

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

    abstract class Stub : Binder(), IPackageDeleteObserver2 {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        class Proxy(private val mRemote: IBinder) : IPackageDeleteObserver2 {

            override fun asBinder(): IBinder = mRemote

            fun getInterfaceDescriptor(): String = DESCRIPTOR

            @Throws(RemoteException::class)
            override fun onPackageDeleted(
                packageName: String?,
                returnCode: Int,
                msg: String?,
            ) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(packageName)
                    data.writeInt(returnCode)
                    data.writeString(msg)
                    // One-way call: no reply parcel, FLAG_ONEWAY (1).
                    mRemote.transact(TRANSACTION_onPackageDeleted, data, null, 1)
                } finally {
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun onUserActionRequired(intent: Intent?) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    _Parcel.writeTypedObject(data, intent, 0)
                    mRemote.transact(TRANSACTION_onUserActionRequired, data, null, 1)
                } finally {
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
                TRANSACTION_onUserActionRequired -> {
                    onUserActionRequired(_Parcel.readTypedObject(data, Intent.CREATOR))
                    true
                }
                TRANSACTION_onPackageDeleted -> {
                    onPackageDeleted(
                        data.readString(),
                        data.readInt(),
                        data.readString(),
                    )
                    true
                }
                else -> super.onTransact(code, data, reply, flags)
            }
        }

        companion object {
            const val DESCRIPTOR = "android.content.pm.IPackageDeleteObserver2"

            private const val TRANSACTION_onUserActionRequired = 1
            private const val TRANSACTION_onPackageDeleted = 2

            @JvmStatic
            fun asInterface(iBinder: IBinder?): IPackageDeleteObserver2? {
                if (iBinder == null) return null
                val local = iBinder.queryLocalInterface(DESCRIPTOR)
                return if (local == null || local !is IPackageDeleteObserver2) {
                    Proxy(iBinder)
                } else {
                    local
                }
            }
        }
    }

    class Default : IPackageDeleteObserver2 {

        override fun asBinder(): IBinder? = null

        @Throws(RemoteException::class)
        override fun onUserActionRequired(intent: Intent?) {}

        @Throws(RemoteException::class)
        override fun onPackageDeleted(
            packageName: String?,
            returnCode: Int,
            msg: String?,
        ) {}
    }
}
package com.ims134.doclone.io

import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException

/**
 * Hidden framework AIDL (oneway). Compile-time stub: the real class comes from the system at runtime,
 * so keep it out of the APK (compileOnly module, or R8 -dontwarn / exclude).
 */
interface IAccountManagerResponse : IInterface {

    @Throws(RemoteException::class)
    fun onResult(value: Bundle?)

    @Throws(RemoteException::class)
    fun onError(errorCode: Int, errorMessage: String?)

    abstract class Stub : Binder(), IAccountManagerResponse {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code in 1..0xFFFFFF) data.enforceInterface(DESCRIPTOR)
            if (code == INTERFACE_TRANSACTION) {
                reply?.writeString(DESCRIPTOR)
                return true
            }
            when (code) {
                TRANSACTION_onResult -> onResult(data.readTypedObject(Bundle.CREATOR))
                TRANSACTION_onError -> onError(data.readInt(), data.readString())
                else -> return super.onTransact(code, data, reply, flags)
            }
            return true
        }

        class Proxy(private val remote: IBinder) : IAccountManagerResponse {

            override fun asBinder(): IBinder = remote

            fun getInterfaceDescriptor(): String = DESCRIPTOR

            override fun onResult(value: Bundle?) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeTypedObject(value, 0)
                    remote.transact(TRANSACTION_onResult, data, null, IBinder.FLAG_ONEWAY)
                } finally {
                    data.recycle()
                }
            }

            override fun onError(errorCode: Int, errorMessage: String?) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(errorCode)
                    data.writeString(errorMessage)
                    remote.transact(TRANSACTION_onError, data, null, IBinder.FLAG_ONEWAY)
                } finally {
                    data.recycle()
                }
            }
        }

        companion object {
            const val DESCRIPTOR = "android.accounts.IAccountManagerResponse"
            const val TRANSACTION_onResult = 1
            const val TRANSACTION_onError = 2

            @JvmStatic
            fun asInterface(binder: IBinder?): IAccountManagerResponse? {
                if (binder == null) return null
                val local = binder.queryLocalInterface(DESCRIPTOR)
                return if (local is IAccountManagerResponse) local else Proxy(binder)
            }
        }
    }

    open class Default : IAccountManagerResponse {
        override fun asBinder(): IBinder? = null
        override fun onResult(value: Bundle?) {}
        override fun onError(errorCode: Int, errorMessage: String?) {}
    }
}
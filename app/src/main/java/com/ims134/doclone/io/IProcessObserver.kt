package com.ims134.doclone.io

import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel

/** Notified when setPosition virtual app process starts or dies (was io.l53). Registered via IActivityManager.registerProcessObserver. */
interface IProcessObserver : IInterface {

    fun onProcessCreated(packageName: String?, processName: String?)
    fun onProcessDied(packageName: String?, processName: String?)

    abstract class Stub : Binder(), IProcessObserver {

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
            when (code) {
                TRANSACTION_onProcessCreated -> onProcessCreated(data.readString(), data.readString())
                TRANSACTION_onProcessDied -> onProcessDied(data.readString(), data.readString())
                else -> return super.onTransact(code, data, reply, flags)
            }
            reply?.writeNoException()
            return true
        }

        class Proxy(val remote: IBinder) : IProcessObserver {

            override fun asBinder(): IBinder = remote

            override fun onProcessCreated(packageName: String?, processName: String?) =
                call(TRANSACTION_onProcessCreated, packageName, processName)

            override fun onProcessDied(packageName: String?, processName: String?) =
                call(TRANSACTION_onProcessDied, packageName, processName)

            private fun call(code: Int, packageName: String?, processName: String?) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(packageName)
                    data.writeString(processName)
                    remote.transact(code, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }
        }

        companion object {
            const val DESCRIPTOR = "com.polestar.clone.server.interfaces.IProcessObserver"
            private const val TRANSACTION_onProcessCreated = 1
            private const val TRANSACTION_onProcessDied = 2

            @JvmStatic
            fun asInterface(binder: IBinder?): IProcessObserver? {
                if (binder == null) return null
                val local = binder.queryLocalInterface(DESCRIPTOR)
                if (local is IProcessObserver) return local
                return Proxy(binder)
            }
        }
    }

    class Default : IProcessObserver {
        override fun asBinder(): IBinder? = null
        override fun onProcessCreated(packageName: String?, processName: String?) {}
        override fun onProcessDied(packageName: String?, processName: String?) {}
    }
}
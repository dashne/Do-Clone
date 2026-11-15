package com.ims134.doclone.io


import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException

interface IAppMonitor : IInterface {

    @Throws(RemoteException::class)
    fun onAppSwitchBackground(pkgName: String?, vuid: Int)

    @Throws(RemoteException::class)
    fun onAppSwitchForeground(pkgName: String?, vuid: Int)

    abstract class Stub : Binder(), IAppMonitor {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        /**
         * Not on [IAppMonitor] in the current decompilation — dispatched by [onTransact]
         * at code 3 (TRANSACTION_onAppLock). Original AIDL likely declared it on the
         * interface; if you're certain of that, move this method up into [IAppMonitor]
         * and add it to [Default].
         */
        @Throws(RemoteException::class)
        abstract fun onAppLock(pkgName: String?, vuid: Int)

        /**
         * Not on [IAppMonitor] — dispatched by [onTransact] at code 4. Same caveat as
         * [onAppLock]. Parameter names are inferred from the pattern `(pkgName, vuid, extra)`.
         */
        @Throws(RemoteException::class)
        abstract fun onAdsLaunch(pkgName: String?, vuid: Int, extra: String?)

        /**
         * Was the JADX-renamed `AppMonitor` class. Kept as [Proxy] for AIDL convention,
         * matching the earlier conversions in this session.
         */
        class Proxy : IAppMonitor {

            @JvmField
            @Volatile
            var binder: IBinder? = null

            override fun asBinder(): IBinder? = binder

            @Throws(RemoteException::class)
            override fun onAppSwitchBackground(pkgName: String?, vuid: Int) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(pkgName)
                    data.writeInt(vuid)
                    binder!!.transact(TRANSACTION_onAppSwitchBackground, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun onAppSwitchForeground(pkgName: String?, vuid: Int) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(pkgName)
                    data.writeInt(vuid)
                    binder!!.transact(TRANSACTION_onAppSwitchForeground, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
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
                TRANSACTION_onAppSwitchForeground -> {
                    onAppSwitchForeground(data.readString(), data.readInt())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_onAppSwitchBackground -> {
                    onAppSwitchBackground(data.readString(), data.readInt())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_onAppLock -> {
                    onAppLock(data.readString(), data.readInt())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_onAdsLaunch -> {
                    onAdsLaunch(data.readString(), data.readInt(), data.readString())
                    reply.writeNoException()
                    true
                }
                else -> super.onTransact(code, data, reply, flags)
            }
        }

        companion object {
            const val DESCRIPTOR = "com.polestar.domultiple.IAppMonitor"

            private const val TRANSACTION_onAppSwitchForeground = 1
            private const val TRANSACTION_onAppSwitchBackground = 2
            private const val TRANSACTION_onAppLock = 3
            private const val TRANSACTION_onAdsLaunch = 4

            @JvmStatic
            fun asInterface(iBinder: IBinder?): IAppMonitor? {
                if (iBinder == null) return null
                val local = iBinder.queryLocalInterface(DESCRIPTOR)
                return if (local == null || local !is IAppMonitor) {
                    Proxy().apply { binder = iBinder }
                } else {
                    local
                }
            }
        }
    }

    class Default : IAppMonitor {

        override fun asBinder(): IBinder? = null

        @Throws(RemoteException::class)
        override fun onAppSwitchBackground(pkgName: String?, vuid: Int) {}

        @Throws(RemoteException::class)
        override fun onAppSwitchForeground(pkgName: String?, vuid: Int) {}
    }
}
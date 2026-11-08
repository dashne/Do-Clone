package com.ims134.doclone.io


import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException

/**
 * Callback the app UI registers to hear about installs / uninstalls (was io.f53).
 * All four methods are oneway (fire and forget).
 */
interface IPackageObserver : IInterface {

    @Throws(RemoteException::class)
    fun onPackageInstalled(packageName: String?)

    @Throws(RemoteException::class)
    fun onPackageInstalledAsUser(userId: Int, packageName: String?)

    @Throws(RemoteException::class)
    fun onPackageUninstalled(packageName: String?)

    @Throws(RemoteException::class)
    fun onPackageUninstalledAsUser(userId: Int, packageName: String?)

    abstract class Stub : Binder(), IPackageObserver {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        public override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code in 1..16777215) {
                data.enforceInterface(DESCRIPTOR)
            }
            if (code == 1598968902) {              // INTERFACE_TRANSACTION
                reply?.writeString(DESCRIPTOR)
                return true
            }
            when (code) {
                TRANSACTION_onPackageInstalled -> onPackageInstalled(data.readString())
                TRANSACTION_onPackageUninstalled -> onPackageUninstalled(data.readString())
                TRANSACTION_onPackageInstalledAsUser -> onPackageInstalledAsUser(data.readInt(), data.readString())
                TRANSACTION_onPackageUninstalledAsUser -> onPackageUninstalledAsUser(data.readInt(), data.readString())
                else -> return super.onTransact(code, data, reply, flags)
            }
            return true      // oneway: nothing to reply
        }

        class Proxy(@JvmField var remote: IBinder) : IPackageObserver {

            override fun asBinder(): IBinder = remote

            override fun onPackageInstalled(packageName: String?) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(packageName)
                    remote.transact(TRANSACTION_onPackageInstalled, data, null, IBinder.FLAG_ONEWAY)
                } finally {
                    data.recycle()
                }
            }

            override fun onPackageInstalledAsUser(userId: Int, packageName: String?) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(userId)
                    data.writeString(packageName)
                    remote.transact(TRANSACTION_onPackageInstalledAsUser, data, null, IBinder.FLAG_ONEWAY)
                } finally {
                    data.recycle()
                }
            }

            override fun onPackageUninstalled(packageName: String?) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(packageName)
                    remote.transact(TRANSACTION_onPackageUninstalled, data, null, IBinder.FLAG_ONEWAY)
                } finally {
                    data.recycle()
                }
            }

            override fun onPackageUninstalledAsUser(userId: Int, packageName: String?) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(userId)
                    data.writeString(packageName)
                    remote.transact(TRANSACTION_onPackageUninstalledAsUser, data, null, IBinder.FLAG_ONEWAY)
                } finally {
                    data.recycle()
                }
            }
        }

        companion object {
            const val DESCRIPTOR = "com.polestar.clone.server.interfaces.IPackageObserver"
            const val TRANSACTION_onPackageInstalled = 1
            const val TRANSACTION_onPackageUninstalled = 2
            const val TRANSACTION_onPackageInstalledAsUser = 3
            const val TRANSACTION_onPackageUninstalledAsUser = 4

            @JvmStatic
            fun asInterface(binder: IBinder?): IPackageObserver? {
                if (binder == null) return null
                val local = binder.queryLocalInterface(DESCRIPTOR)
                if (local is IPackageObserver) return local
                return Proxy(binder)
            }
        }
    }

    class Default : IPackageObserver {
        override fun asBinder(): IBinder? = null
        override fun onPackageInstalled(packageName: String?) {}
        override fun onPackageInstalledAsUser(userId: Int, packageName: String?) {}
        override fun onPackageUninstalled(packageName: String?) {}
        override fun onPackageUninstalledAsUser(userId: Int, packageName: String?) {}
    }
}
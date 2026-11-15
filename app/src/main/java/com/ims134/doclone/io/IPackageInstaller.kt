package com.ims134.doclone.io


import android.content.IntentSender

import android.graphics.Bitmap
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException
import com.ims134.doclone.clone.VParceledListSlice
import com.ims134.doclone.clone.server.SessionInfo
import com.ims134.doclone.clone.server.SessionParams


interface IPackageInstaller : IInterface {

    @Throws(RemoteException::class)
    fun abandonSession(sessionId: Int)

    @Throws(RemoteException::class)
    fun createSession(params: SessionParams?, installerPackageName: String?, userId: Int): Int

    @Throws(RemoteException::class)
    fun getAllSessions(userId: Int): VParceledListSlice<SessionInfo>?

    @Throws(RemoteException::class)
    fun getMySessions(installerPackageName: String?, userId: Int): VParceledListSlice<SessionInfo>?

    @Throws(RemoteException::class)
    fun getSessionInfo(sessionId: Int): SessionInfo?

    @Throws(RemoteException::class)
    fun openSession(sessionId: Int): IPackageInstallerSession?

    @Throws(RemoteException::class)
    fun registerCallback(callback: IPackageInstallerCallback?, userId: Int)

    @Throws(RemoteException::class)
    fun setPermissionsResult(sessionId: Int, accepted: Boolean)

    @Throws(RemoteException::class)
    fun unregisterCallback(callback: IPackageInstallerCallback?)

    @Throws(RemoteException::class)
    fun updateSessionAppIcon(sessionId: Int, appIcon: Bitmap?)

    @Throws(RemoteException::class)
    fun updateSessionAppLabel(sessionId: Int, appLabel: String?)

    object ParcelUtils {
        @Suppress("UNCHECKED_CAST")
        fun <T : Parcelable> a(parcel: Parcel, creator: Parcelable.Creator<*>): T? =
            if (parcel.readInt() != 0) creator.createFromParcel(parcel) as T? else null

        fun b(parcel: Parcel, parcelable: Parcelable?, flags: Int) {
            if (parcelable == null) {
                parcel.writeInt(0)
            } else {
                parcel.writeInt(1)
                parcelable.writeToParcel(parcel, flags)
            }
        }
    }

    abstract class Stub : Binder(), IPackageInstaller {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        class Proxy : IPackageInstaller {

            lateinit var remote: IBinder

            override fun asBinder(): IBinder = remote

            @Throws(RemoteException::class)
            override fun abandonSession(sessionId: Int) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(sessionId)
                    remote.transact(4, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun createSession(
                params: SessionParams?,
                installerPackageName: String?,
                userId: Int,
            ): Int {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    ParcelUtils.b(data, params, 0)
                    data.writeString(installerPackageName)
                    data.writeInt(userId)
                    remote.transact(1, data, reply, 0)
                    reply.readException()
                    return reply.readInt()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getAllSessions(userId: Int): VParceledListSlice<SessionInfo>? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(userId)
                    remote.transact(7, data, reply, 0)
                    reply.readException()
                    return ParcelUtils.a(reply, VParceledListSlice.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getMySessions(
                installerPackageName: String?,
                userId: Int,
            ): VParceledListSlice<SessionInfo>? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(installerPackageName)
                    data.writeInt(userId)
                    remote.transact(8, data, reply, 0)
                    reply.readException()
                    return ParcelUtils.a(reply, VParceledListSlice.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getSessionInfo(sessionId: Int): SessionInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(sessionId)
                    remote.transact(6, data, reply, 0)
                    reply.readException()
                    return ParcelUtils.a(reply, SessionInfo.CREATOR) as SessionInfo?
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun openSession(sessionId: Int): IPackageInstallerSession? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(sessionId)
                    remote.transact(5, data, reply, 0)
                    reply.readException()
                    return IPackageInstallerSession.Stub.asInterface(reply.readStrongBinder())
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun registerCallback(callback: IPackageInstallerCallback?, userId: Int) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeStrongInterface(callback)
                    data.writeInt(userId)
                    remote.transact(9, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun setPermissionsResult(sessionId: Int, accepted: Boolean) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(sessionId)
                    data.writeInt(if (accepted) 1 else 0)
                    remote.transact(12, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun unregisterCallback(callback: IPackageInstallerCallback?) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeStrongInterface(callback)
                    remote.transact(10, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun updateSessionAppIcon(sessionId: Int, appIcon: Bitmap?) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(sessionId)
                    ParcelUtils.b(data, appIcon, 0)
                    remote.transact(2, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun updateSessionAppLabel(sessionId: Int, appLabel: String?) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(sessionId)
                    data.writeString(appLabel)
                    remote.transact(3, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }
        }

        @Throws(RemoteException::class)
        abstract fun uninstall(
            packageName: String?,
            callerPackageName: String?,
            flags: Int,
            statusReceiver: IntentSender?,
            userId: Int,
        )

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
                1 -> {
                    val result = createSession(
                        ParcelUtils.a(data, SessionParams.CREATOR) as SessionParams?,
                        data.readString(),
                        data.readInt(),
                    )
                    reply.writeNoException()
                    reply.writeInt(result)
                    true
                }
                2 -> {
                    updateSessionAppIcon(
                        data.readInt(),
                        ParcelUtils.a(data, Bitmap.CREATOR) as Bitmap?,
                    )
                    reply.writeNoException()
                    true
                }
                3 -> {
                    updateSessionAppLabel(data.readInt(), data.readString())
                    reply.writeNoException()
                    true
                }
                4 -> {
                    abandonSession(data.readInt())
                    reply.writeNoException()
                    true
                }
                5 -> {
                    val session = openSession(data.readInt())
                    reply.writeNoException()
                    reply.writeStrongInterface(session)
                    true
                }
                6 -> {
                    val info = getSessionInfo(data.readInt())
                    reply.writeNoException()
                    ParcelUtils.b(reply, info, 1)
                    true
                }
                7 -> {
                    val sessions = getAllSessions(data.readInt())
                    reply.writeNoException()
                    ParcelUtils.b(reply, sessions, 1)
                    true
                }
                8 -> {
                    val sessions = getMySessions(data.readString(), data.readInt())
                    reply.writeNoException()
                    ParcelUtils.b(reply, sessions, 1)
                    true
                }
                9 -> {
                    registerCallback(
                        IPackageInstallerCallback.Stub.asInterface(data.readStrongBinder()),
                        data.readInt(),
                    )
                    reply.writeNoException()
                    true
                }
                10 -> {
                    unregisterCallback(
                        IPackageInstallerCallback.Stub.asInterface(data.readStrongBinder())
                    )
                    reply.writeNoException()
                    true
                }
                11 -> {
                    uninstall(
                        data.readString(),
                        data.readString(),
                        data.readInt(),
                        ParcelUtils.a(data, IntentSender.CREATOR) as IntentSender?,
                        data.readInt(),
                    )
                    reply.writeNoException()
                    true
                }
                12 -> {
                    setPermissionsResult(data.readInt(), data.readInt() != 0)
                    reply.writeNoException()
                    true
                }
                else -> super.onTransact(code, data, reply, flags)
            }
        }

        companion object {
            private const val DESCRIPTOR = "com.polestar.clone.server.IPackageInstaller"

            @JvmStatic
            fun asInterface(iBinder: IBinder?): IPackageInstaller? {
                if (iBinder == null) return null
                val local = iBinder.queryLocalInterface(DESCRIPTOR)
                if (local != null && local is IPackageInstaller) {
                    return local
                }
                return Proxy().apply { remote = iBinder }
            }
        }
    }

    class a : IPackageInstaller {

        override fun asBinder(): IBinder? = null

        override fun abandonSession(sessionId: Int) {}

        override fun createSession(
            params: SessionParams?,
            installerPackageName: String?,
            userId: Int,
        ): Int = 0

        override fun getAllSessions(userId: Int): VParceledListSlice<SessionInfo>? = null

        override fun getMySessions(installerPackageName: String?, userId: Int): VParceledListSlice<SessionInfo>? =
            null

        override fun getSessionInfo(sessionId: Int): SessionInfo? = null

        override fun openSession(sessionId: Int): IPackageInstallerSession? = null

        override fun registerCallback(callback: IPackageInstallerCallback?, userId: Int) {}

        override fun setPermissionsResult(sessionId: Int, accepted: Boolean) {}

        override fun unregisterCallback(callback: IPackageInstallerCallback?) {}

        override fun updateSessionAppIcon(sessionId: Int, appIcon: Bitmap?) {}

        override fun updateSessionAppLabel(sessionId: Int, appLabel: String?) {}
    }
}
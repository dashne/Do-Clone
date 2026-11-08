package com.ims134.doclone.clone

import android.graphics.Bitmap
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException

interface IUserManager : IInterface
{

    companion object {
        const val DESCRIPTOR: String = "com.polestar.clone.server.IUserManager"
    }

    @Throws(RemoteException::class)
    fun createUser(name: String?, flags: Int): VUserInfo?

    @Throws(RemoteException::class)
    fun createUserWithUserId(name: String?, flags: Int, userId: Int): VUserInfo?

    @Throws(RemoteException::class)
    fun getUserInfo(userId: Int): VUserInfo?

    @Throws(RemoteException::class)
    fun getUsers(excludeDying: Boolean): List<VUserInfo>?

    // ------------------------------------------------------------------

    abstract class Stub : Binder(), IUserManager {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        // Not in IUserManager, but handled by onTransact
        @Throws(RemoteException::class) abstract fun getUserHandle(userId: Int): Int
        @Throws(RemoteException::class) abstract fun getUserIcon(userId: Int): Bitmap?
        @Throws(RemoteException::class) abstract fun getUserSerialNumber(userId: Int): Int
        @Throws(RemoteException::class) abstract fun isGuestEnabled(): Boolean
        @Throws(RemoteException::class) abstract fun removeUser(userId: Int): Boolean
        @Throws(RemoteException::class) abstract fun setGuestEnabled(enable: Boolean)
        @Throws(RemoteException::class) abstract fun setUserIcon(userId: Int, icon: Bitmap?)
        @Throws(RemoteException::class) abstract fun setUserName(userId: Int, name: String?)
        @Throws(RemoteException::class) abstract fun wipeUser(userId: Int)

        override fun asBinder(): IBinder = this

        public override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code in 1..16777215) {
                data.enforceInterface(DESCRIPTOR)
            }
            if (code == IBinder.INTERFACE_TRANSACTION) {
                reply?.writeString(DESCRIPTOR)
                return true
            }
            val out = reply ?: return super.onTransact(code, data, null, flags)

            when (code) {
                1 -> {
                    val r = createUser(data.readString(), data.readInt())
                    out.writeNoException()
                    out.writeTypedObject(r, 1)
                }
                2 -> {
                    val r = createUserWithUserId(data.readString(), data.readInt(), data.readInt())
                    out.writeNoException()
                    out.writeTypedObject(r, 1)
                }
                3 -> {
                    val r = removeUser(data.readInt())
                    out.writeNoException()
                    out.writeInt(if (r) 1 else 0)
                }
                4 -> {
                    setUserName(data.readInt(), data.readString())
                    out.writeNoException()
                }
                5 -> {
                    setUserIcon(data.readInt(), data.readTypedObject(Bitmap.CREATOR))
                    out.writeNoException()
                }
                6 -> {
                    val r = getUserIcon(data.readInt())
                    out.writeNoException()
                    out.writeTypedObject(r, 1)
                }
                7 -> {
                    val r = getUsers(data.readInt() != 0)
                    out.writeNoException()
                    if (r == null)
                    {
                        out.writeInt(-1)
                    } else {
                        out.writeInt(r.size)
                        for (user in r) out.writeTypedObject(user, 1)
                    }
                }
                8 -> {
                    val r = getUserInfo(data.readInt())
                    out.writeNoException()
                    out.writeTypedObject(r, 1)
                }
                9 -> {
                    setGuestEnabled(data.readInt() != 0)
                    out.writeNoException()
                }
                10 -> {
                    val r = isGuestEnabled()
                    out.writeNoException()
                    out.writeInt(if (r) 1 else 0)
                }
                11 -> {
                    wipeUser(data.readInt())
                    out.writeNoException()
                }
                12 -> {
                    val r = getUserSerialNumber(data.readInt())
                    out.writeNoException()
                    out.writeInt(r)
                }
                13 -> {
                    val r = getUserHandle(data.readInt())
                    out.writeNoException()
                    out.writeInt(r)
                }
                else -> return super.onTransact(code, data, reply, flags)
            }
            return true
        }

        // --------------------------------------------------------------

        class Proxy(val remote: IBinder) : IUserManager {

            override fun asBinder(): IBinder = remote

            private inline fun <R> call(code: Int, write: (Parcel) -> Unit, read: (Parcel) -> R): R {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    write(data)
                    remote.transact(code, data, reply, 0)
                    reply.readException()
                    return read(reply)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            // NOTE: original ignores flags and always writes 2
            override fun createUser(name: String?, flags: Int): VUserInfo? =
                call(1, {
                    it.writeString(name)
                    it.writeInt(2)
                }) { it.readTypedObject(VUserInfo.CREATOR) }

            // NOTE: original ignores flags and always writes 2
            override fun createUserWithUserId(name: String?, flags: Int, userId: Int): VUserInfo? =
                call(2, {
                    it.writeString(name)
                    it.writeInt(2)
                    it.writeInt(userId)
                }) { it.readTypedObject(VUserInfo.CREATOR) }

            override fun getUserInfo(userId: Int): VUserInfo? =
                call(8, { it.writeInt(userId) }) { it.readTypedObject(VUserInfo.CREATOR) }

            // NOTE: original ignores excludeDying and always writes 0
            override fun getUsers(excludeDying: Boolean): List<VUserInfo>? =
                call(7, { it.writeInt(0) }) { it.createTypedArrayList(VUserInfo.CREATOR) }
        }

        companion object {
            @JvmStatic
            fun asInterface(binder: IBinder?): IUserManager? {
                if (binder == null) return null
                val local = binder.queryLocalInterface(DESCRIPTOR)
                if (local is IUserManager) return local
                return Proxy(binder)
            }
        }
    }

    // ------------------------------------------------------------------

    class Default : IUserManager {
        override fun asBinder(): IBinder? = null
        override fun createUser(name: String?, flags: Int): VUserInfo? = null
        override fun createUserWithUserId(name: String?, flags: Int, userId: Int): VUserInfo? = null
        override fun getUserInfo(userId: Int): VUserInfo? = null
        override fun getUsers(excludeDying: Boolean): List<VUserInfo>? = null
    }
}
package android.content.pm


import android.content.IntentSender
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.os.RemoteException

interface IPackageInstallerSession : IInterface {

    @Throws(RemoteException::class)
    fun abandon()

    @Throws(RemoteException::class)
    fun addClientProgress(f: Float)

    @Throws(RemoteException::class)
    fun close()

    @Throws(RemoteException::class)
    fun commit(intentSender: IntentSender?)

    @Throws(RemoteException::class)
    fun getNames(): Array<String>?

    @Throws(RemoteException::class)
    fun openRead(str: String?): ParcelFileDescriptor?

    @Throws(RemoteException::class)
    fun openWrite(str: String?, j: Long, j2: Long): ParcelFileDescriptor?

    @Throws(RemoteException::class)
    fun removeSplit(str: String?)

    @Throws(RemoteException::class)
    fun setClientProgress(f: Float)

    /**
     * Note: the original Java members were `private static` helpers in Stub public nested class.
     * Kotlin cannot grant cross-class access to `private` members of Stub nested class, so these
     * helpers are exposed as public members of Stub nested `object` (the enclosing class itself
     * was already `public static` in Java, so the API surface is unchanged in practice).
     */
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

    class Default : IPackageInstallerSession {

        override fun asBinder(): IBinder? = null

        @Throws(RemoteException::class)
        override fun getNames(): Array<String>? = null

        @Throws(RemoteException::class)
        override fun openRead(str: String?): ParcelFileDescriptor? = null

        @Throws(RemoteException::class)
        override fun openWrite(str: String?, j: Long, j2: Long): ParcelFileDescriptor? = null

        @Throws(RemoteException::class)
        override fun abandon() {}

        @Throws(RemoteException::class)
        override fun close() {}

        @Throws(RemoteException::class)
        override fun addClientProgress(f: Float) {}

        @Throws(RemoteException::class)
        override fun commit(intentSender: IntentSender?) {}

        @Throws(RemoteException::class)
        override fun removeSplit(str: String?) {}

        @Throws(RemoteException::class)
        override fun setClientProgress(f: Float) {}
    }

    abstract class Stub : Binder(), IPackageInstallerSession {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        class Proxy(private val mRemote: IBinder) : IPackageInstallerSession {

            override fun asBinder(): IBinder = mRemote

            fun getInterfaceDescriptor(): String = DESCRIPTOR

            @Throws(RemoteException::class)
            override fun abandon() {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_abandon, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun addClientProgress(f: Float) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeFloat(f)
                    mRemote.transact(TRANSACTION_addClientProgress, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun close() {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_close, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun commit(intentSender: IntentSender?) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    _Parcel.writeTypedObject(data, intentSender, 0)
                    mRemote.transact(TRANSACTION_commit, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getNames(): Array<String>? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_getNames, data, reply, 0)
                    reply.readException()
                    return reply.createStringArray()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun openRead(str: String?): ParcelFileDescriptor? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(str)
                    mRemote.transact(TRANSACTION_openRead, data, reply, 0)
                    reply.readException()
                    return _Parcel.readTypedObject(reply, ParcelFileDescriptor.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun openWrite(str: String?, j: Long, j2: Long): ParcelFileDescriptor? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(str)
                    data.writeLong(j)
                    data.writeLong(j2)
                    mRemote.transact(TRANSACTION_openWrite, data, reply, 0)
                    reply.readException()
                    return _Parcel.readTypedObject(reply, ParcelFileDescriptor.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun removeSplit(str: String?) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(str)
                    mRemote.transact(TRANSACTION_removeSplit, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun setClientProgress(f: Float) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeFloat(f)
                    mRemote.transact(TRANSACTION_setClientProgress, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }
        }

        @Throws(RemoteException::class)
        override fun onTransact(code: Int, data: Parcel, out: Parcel?, flags: Int): Boolean {
            val reply = out?: return false
            if (code in 1..0xFFFFFF) {
                data.enforceInterface(DESCRIPTOR)
            }
            if (code == 0x5F4E5446) { // INTERFACE_TRANSACTION
                reply.writeString(DESCRIPTOR)
                return true
            }
            return when (code) {
                TRANSACTION_setClientProgress -> {
                    setClientProgress(data.readFloat())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_addClientProgress -> {
                    addClientProgress(data.readFloat())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_getNames -> {
                    val names = getNames()
                    reply.writeNoException()
                    reply.writeStringArray(names)
                    true
                }
                TRANSACTION_openWrite -> {
                    val pfd = openWrite(data.readString(), data.readLong(), data.readLong())
                    reply.writeNoException()
                    _Parcel.writeTypedObject(reply, pfd, 1)
                    true
                }
                TRANSACTION_openRead -> {
                    val pfd = openRead(data.readString())
                    reply.writeNoException()
                    _Parcel.writeTypedObject(reply, pfd, 1)
                    true
                }
                TRANSACTION_removeSplit -> {
                    removeSplit(data.readString())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_close -> {
                    close()
                    reply.writeNoException()
                    true
                }
                TRANSACTION_commit -> {
                    commit(_Parcel.readTypedObject(data, IntentSender.CREATOR))
                    reply.writeNoException()
                    true
                }
                TRANSACTION_abandon -> {
                    abandon()
                    reply.writeNoException()
                    true
                }
                else -> super.onTransact(code, data, reply, flags)
            }
        }

        companion object {
            const val DESCRIPTOR = "android.content.pm.IPackageInstallerSession"

            private const val TRANSACTION_setClientProgress = 1
            private const val TRANSACTION_addClientProgress = 2
            private const val TRANSACTION_getNames = 3
            private const val TRANSACTION_openWrite = 4
            private const val TRANSACTION_openRead = 5
            private const val TRANSACTION_removeSplit = 6
            private const val TRANSACTION_close = 7
            private const val TRANSACTION_commit = 8
            private const val TRANSACTION_abandon = 9

            @JvmStatic
            fun asInterface(iBinder: IBinder?): IPackageInstallerSession? {
                if (iBinder == null) return null
                val local = iBinder.queryLocalInterface(DESCRIPTOR)
                return if (local == null || local !is IPackageInstallerSession) {
                    Proxy(iBinder)
                } else {
                    local
                }
            }
        }
    }
}
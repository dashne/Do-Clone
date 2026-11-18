package android.content.pm


import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException

interface IPackageInstallerCallback : IInterface {

    @Throws(RemoteException::class)
    fun onSessionCreated(sessionId: Int)

    @Throws(RemoteException::class)
    fun onSessionBadgingChanged(sessionId: Int)

    @Throws(RemoteException::class)
    fun onSessionActiveChanged(sessionId: Int, active: Boolean)

    @Throws(RemoteException::class)
    fun onSessionProgressChanged(sessionId: Int, progress: Float)

    @Throws(RemoteException::class)
    fun onSessionFinished(sessionId: Int, success: Boolean)

    abstract class Stub : Binder(), IPackageInstallerCallback {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        class Proxy(private val mRemote: IBinder) : IPackageInstallerCallback {

            override fun asBinder(): IBinder = mRemote

            fun getInterfaceDescriptor(): String = DESCRIPTOR

            @Throws(RemoteException::class)
            override fun onSessionActiveChanged(sessionId: Int, active: Boolean) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(sessionId)
                    data.writeInt(if (active) 1 else 0)
                    // One-way call: no reply parcel, FLAG_ONEWAY (1).
                    mRemote.transact(TRANSACTION_onSessionActiveChanged, data, null, 1)
                } finally {
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun onSessionBadgingChanged(sessionId: Int) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(sessionId)
                    mRemote.transact(TRANSACTION_onSessionBadgingChanged, data, null, 1)
                } finally {
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun onSessionCreated(sessionId: Int) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(sessionId)
                    mRemote.transact(TRANSACTION_onSessionCreated, data, null, 1)
                } finally {
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun onSessionFinished(sessionId: Int, success: Boolean) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(sessionId)
                    data.writeInt(if (success) 1 else 0)
                    mRemote.transact(TRANSACTION_onSessionFinished, data, null, 1)
                } finally {
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun onSessionProgressChanged(sessionId: Int, progress: Float) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(sessionId)
                    data.writeFloat(progress)
                    mRemote.transact(TRANSACTION_onSessionProgressChanged, data, null, 1)
                } finally {
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
                TRANSACTION_onSessionCreated -> {
                    onSessionCreated(data.readInt())
                    true
                }
                TRANSACTION_onSessionBadgingChanged -> {
                    onSessionBadgingChanged(data.readInt())
                    true
                }
                TRANSACTION_onSessionActiveChanged -> {
                    onSessionActiveChanged(data.readInt(), data.readInt() != 0)
                    true
                }
                TRANSACTION_onSessionProgressChanged -> {
                    onSessionProgressChanged(data.readInt(), data.readFloat())
                    true
                }
                TRANSACTION_onSessionFinished -> {
                    onSessionFinished(data.readInt(), data.readInt() != 0)
                    true
                }
                else -> super.onTransact(code, data, reply, flags)
            }
        }

        companion object {
            const val DESCRIPTOR = "android.content.pm.IPackageInstallerCallback"

            private const val TRANSACTION_onSessionCreated = 1
            private const val TRANSACTION_onSessionBadgingChanged = 2
            private const val TRANSACTION_onSessionActiveChanged = 3
            private const val TRANSACTION_onSessionProgressChanged = 4
            private const val TRANSACTION_onSessionFinished = 5

            @JvmStatic
            fun asInterface(iBinder: IBinder?): IPackageInstallerCallback? {
                if (iBinder == null) return null
                val local = iBinder.queryLocalInterface(DESCRIPTOR)
                return if (local == null || local !is IPackageInstallerCallback) {
                    Proxy(iBinder)
                } else {
                    local
                }
            }
        }
    }

    class Default : IPackageInstallerCallback {

        override fun asBinder(): IBinder? = null

        @Throws(RemoteException::class)
        override fun onSessionBadgingChanged(sessionId: Int) {}

        @Throws(RemoteException::class)
        override fun onSessionCreated(sessionId: Int) {}

        @Throws(RemoteException::class)
        override fun onSessionActiveChanged(sessionId: Int, active: Boolean) {}

        @Throws(RemoteException::class)
        override fun onSessionFinished(sessionId: Int, success: Boolean) {}

        @Throws(RemoteException::class)
        override fun onSessionProgressChanged(sessionId: Int, progress: Float) {}
    }
}
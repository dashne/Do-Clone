package android.content.pm


import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException

interface IPackageDataObserver : IInterface {

    @Throws(RemoteException::class)
    fun onRemoveCompleted(packageName: String?, succeeded: Boolean)

    abstract class Stub : Binder(), IPackageDataObserver {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        class Proxy(private val mRemote: IBinder) : IPackageDataObserver {

            override fun asBinder(): IBinder = mRemote

            fun getInterfaceDescriptor(): String = DESCRIPTOR

            @Throws(RemoteException::class)
            override fun onRemoveCompleted(packageName: String?, succeeded: Boolean) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(packageName)
                    data.writeInt(if (succeeded) 1 else 0)
                    // One-way call: no reply parcel, FLAG_ONEWAY (1).
                    mRemote.transact(TRANSACTION_onRemoveCompleted, data, null, 1)
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
                TRANSACTION_onRemoveCompleted -> {
                    onRemoveCompleted(data.readString(), data.readInt() != 0)
                    true
                }
                else -> super.onTransact(code, data, reply, flags)
            }
        }

        companion object {
            const val DESCRIPTOR = "android.content.pm.IPackageDataObserver"

            private const val TRANSACTION_onRemoveCompleted = 1

            @JvmStatic
            fun asInterface(iBinder: IBinder?): IPackageDataObserver? {
                if (iBinder == null) return null
                val local = iBinder.queryLocalInterface(DESCRIPTOR)
                return if (local == null || local !is IPackageDataObserver) {
                    Proxy(iBinder)
                } else {
                    local
                }
            }
        }
    }

    class Default : IPackageDataObserver {

        override fun asBinder(): IBinder? = null

        @Throws(RemoteException::class)
        override fun onRemoveCompleted(packageName: String?, succeeded: Boolean) {}
    }
}
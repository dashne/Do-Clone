package android.os

interface ISystemUpdateManager : IInterface {

    @Throws(RemoteException::class)
    fun retrieveSystemUpdateInfo(): Bundle?

    @Throws(RemoteException::class)
    fun updateSystemUpdateInfo(info: PersistableBundle?)

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

    abstract class Stub : Binder(), ISystemUpdateManager {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        class Proxy(private val mRemote: IBinder) : ISystemUpdateManager {

            override fun asBinder(): IBinder = mRemote

            fun getInterfaceDescriptor(): String = DESCRIPTOR

            @Throws(RemoteException::class)
            override fun retrieveSystemUpdateInfo(): Bundle? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_retrieveSystemUpdateInfo, data, reply, 0)
                    reply.readException()
                    return _Parcel.readTypedObject(reply, Bundle.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun updateSystemUpdateInfo(info: PersistableBundle?) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    _Parcel.writeTypedObject(data, info, 0)
                    mRemote.transact(TRANSACTION_updateSystemUpdateInfo, data, reply, 0)
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
                TRANSACTION_retrieveSystemUpdateInfo -> {
                    val bundle = retrieveSystemUpdateInfo()
                    reply.writeNoException()
                    _Parcel.writeTypedObject(reply, bundle, 1)
                    true
                }
                TRANSACTION_updateSystemUpdateInfo -> {
                    updateSystemUpdateInfo(
                        _Parcel.readTypedObject(data, PersistableBundle.CREATOR)
                    )
                    reply.writeNoException()
                    true
                }
                else -> super.onTransact(code, data, reply, flags)
            }
        }

        companion object {
            const val DESCRIPTOR = "android.os.ISystemUpdateManager"

            private const val TRANSACTION_retrieveSystemUpdateInfo = 1
            private const val TRANSACTION_updateSystemUpdateInfo = 2

            @JvmStatic
            fun asInterface(iBinder: IBinder?): ISystemUpdateManager? {
                if (iBinder == null) return null
                val local = iBinder.queryLocalInterface(DESCRIPTOR)
                return if (local == null || local !is ISystemUpdateManager) {
                    Proxy(iBinder)
                } else {
                    local
                }
            }
        }
    }

    class Default : ISystemUpdateManager {

        override fun asBinder(): IBinder? = null

        @Throws(RemoteException::class)
        override fun retrieveSystemUpdateInfo(): Bundle? = null

        @Throws(RemoteException::class)
        override fun updateSystemUpdateInfo(info: PersistableBundle?) {}
    }
}
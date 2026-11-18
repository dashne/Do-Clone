package android.os

interface IUpdateEngine : IInterface {

    @Throws(RemoteException::class)
    fun applyPayload(url: String?, offset: Long, size: Long, headerKeyValuePairs: Array<String>?)

    @Throws(RemoteException::class)
    fun bind(callback: IUpdateEngineCallback?): Boolean

    @Throws(RemoteException::class)
    fun cancel()

    @Throws(RemoteException::class)
    fun resetStatus()

    @Throws(RemoteException::class)
    fun resume()

    @Throws(RemoteException::class)
    fun suspend()

    @Throws(RemoteException::class)
    fun unbind(callback: IUpdateEngineCallback?): Boolean

    @Throws(RemoteException::class)
    fun verifyPayloadApplicable(metadataFilename: String?): Boolean

    class Default : IUpdateEngine {
        override fun asBinder(): IBinder? = null
        @Throws(RemoteException::class) override fun applyPayload(url: String?, offset: Long, size: Long, headerKeyValuePairs: Array<String>?) {}
        @Throws(RemoteException::class) override fun bind(callback: IUpdateEngineCallback?): Boolean = false
        @Throws(RemoteException::class) override fun cancel() {}
        @Throws(RemoteException::class) override fun resetStatus() {}
        @Throws(RemoteException::class) override fun resume() {}
        @Throws(RemoteException::class) override fun suspend() {}
        @Throws(RemoteException::class) override fun unbind(callback: IUpdateEngineCallback?): Boolean = false
        @Throws(RemoteException::class) override fun verifyPayloadApplicable(metadataFilename: String?): Boolean = false
    }

    abstract class Stub : Binder(), IUpdateEngine {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        class Proxy(private val mRemote: IBinder) : IUpdateEngine {

            override fun asBinder(): IBinder = mRemote

            fun getInterfaceDescriptor(): String = DESCRIPTOR

            @Throws(RemoteException::class)
            override fun applyPayload(
                url: String?,
                offset: Long,
                size: Long,
                headerKeyValuePairs: Array<String>?,
            ) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(url)
                    data.writeLong(offset)
                    data.writeLong(size)
                    data.writeStringArray(headerKeyValuePairs)
                    mRemote.transact(TRANSACTION_applyPayload, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun bind(callback: IUpdateEngineCallback?): Boolean {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeStrongInterface(callback)
                    mRemote.transact(TRANSACTION_bind, data, reply, 0)
                    reply.readException()
                    return reply.readInt() != 0
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun cancel() {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_cancel, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun resetStatus() {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_resetStatus, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun resume() {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_resume, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun suspend() {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_suspend, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun unbind(callback: IUpdateEngineCallback?): Boolean {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeStrongInterface(callback)
                    mRemote.transact(TRANSACTION_unbind, data, reply, 0)
                    reply.readException()
                    return reply.readInt() != 0
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun verifyPayloadApplicable(metadataFilename: String?): Boolean {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(metadataFilename)
                    mRemote.transact(TRANSACTION_verifyPayloadApplicable, data, reply, 0)
                    reply.readException()
                    return reply.readInt() != 0
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
                TRANSACTION_applyPayload -> {
                    applyPayload(
                        data.readString(),
                        data.readLong(),
                        data.readLong(),
                        data.createStringArray(),
                    )
                    reply.writeNoException()
                    true
                }
                TRANSACTION_bind -> {
                    val result = bind(IUpdateEngineCallback.Stub.asInterface(data.readStrongBinder()))
                    reply.writeNoException()
                    reply.writeInt(if (result) 1 else 0)
                    true
                }
                TRANSACTION_unbind -> {
                    val result = unbind(IUpdateEngineCallback.Stub.asInterface(data.readStrongBinder()))
                    reply.writeNoException()
                    reply.writeInt(if (result) 1 else 0)
                    true
                }
                TRANSACTION_suspend -> {
                    suspend()
                    reply.writeNoException()
                    true
                }
                TRANSACTION_resume -> {
                    resume()
                    reply.writeNoException()
                    true
                }
                TRANSACTION_cancel -> {
                    cancel()
                    reply.writeNoException()
                    true
                }
                TRANSACTION_resetStatus -> {
                    resetStatus()
                    reply.writeNoException()
                    true
                }
                TRANSACTION_verifyPayloadApplicable -> {
                    val result = verifyPayloadApplicable(data.readString())
                    reply.writeNoException()
                    reply.writeInt(if (result) 1 else 0)
                    true
                }
                else -> super.onTransact(code, data, reply, flags)
            }
        }

        companion object {
            const val DESCRIPTOR = "android.os.IUpdateEngine"

            private const val TRANSACTION_applyPayload = 1
            private const val TRANSACTION_bind = 2
            private const val TRANSACTION_unbind = 3
            private const val TRANSACTION_suspend = 4
            private const val TRANSACTION_resume = 5
            private const val TRANSACTION_cancel = 6
            private const val TRANSACTION_resetStatus = 7
            private const val TRANSACTION_verifyPayloadApplicable = 8

            @JvmStatic
            fun asInterface(iBinder: IBinder?): IUpdateEngine? {
                if (iBinder == null) return null
                val local = iBinder.queryLocalInterface(DESCRIPTOR)
                return if (local == null || local !is IUpdateEngine) {
                    Proxy(iBinder)
                } else {
                    local
                }
            }
        }
    }
}
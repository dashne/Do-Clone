package android.os

interface IUpdateEngineCallback : IInterface {

    @Throws(RemoteException::class)
    fun onPayloadApplicationComplete(errorCode: Int)

    @Throws(RemoteException::class)
    fun onStatusUpdate(statusCode: Int, percent: Float)

    abstract class Stub : Binder(), IUpdateEngineCallback {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        class Proxy(private val mRemote: IBinder) : IUpdateEngineCallback {

            override fun asBinder(): IBinder = mRemote

            fun getInterfaceDescriptor(): String = DESCRIPTOR

            @Throws(RemoteException::class)
            override fun onPayloadApplicationComplete(errorCode: Int) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(errorCode)
                    mRemote.transact(TRANSACTION_onPayloadApplicationComplete, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun onStatusUpdate(statusCode: Int, percent: Float) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(statusCode)
                    data.writeFloat(percent)
                    mRemote.transact(TRANSACTION_onStatusUpdate, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }
        }

        @Throws(RemoteException::class)
        override fun onTransact(code: Int, data: Parcel, out: Parcel?, flags: Int): Boolean {
            var reply = out ?: return false
            if (code in 1..0xFFFFFF) {
                data.enforceInterface(DESCRIPTOR)
            }
            if (code == 0x5F4E5446) { // INTERFACE_TRANSACTION
                reply.writeString(DESCRIPTOR)
                return true
            }
            return when (code) {
                TRANSACTION_onStatusUpdate -> {
                    onStatusUpdate(data.readInt(), data.readFloat())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_onPayloadApplicationComplete -> {
                    onPayloadApplicationComplete(data.readInt())
                    reply.writeNoException()
                    true
                }
                else -> super.onTransact(code, data, reply, flags)
            }
        }

        companion object {
            const val DESCRIPTOR = "android.os.IUpdateEngineCallback"

            private const val TRANSACTION_onStatusUpdate = 1
            private const val TRANSACTION_onPayloadApplicationComplete = 2

            @JvmStatic
            fun asInterface(iBinder: IBinder?): IUpdateEngineCallback? {
                if (iBinder == null) return null
                val local = iBinder.queryLocalInterface(DESCRIPTOR)
                return if (local == null || local !is IUpdateEngineCallback) {
                    Proxy(iBinder)
                } else {
                    local
                }
            }
        }
    }

    class Default : IUpdateEngineCallback {

        override fun asBinder(): IBinder? = null

        @Throws(RemoteException::class)
        override fun onPayloadApplicationComplete(errorCode: Int) {}

        @Throws(RemoteException::class)
        override fun onStatusUpdate(statusCode: Int, percent: Float) {}
    }
}
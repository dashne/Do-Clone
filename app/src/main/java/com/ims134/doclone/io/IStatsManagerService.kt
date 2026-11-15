package com.ims134.doclone.io

import android.app.PendingIntent
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException

/* JADX INFO: loaded from: classes.dex */
interface IStatsManagerService : IInterface {

    companion object {
        const val DESCRIPTOR = "android.os.IStatsManagerService"
    }

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

    @Throws(RemoteException::class)
    fun addConfiguration(configKey: Long, config: ByteArray?, packageName: String?)

    @Throws(RemoteException::class)
    fun getData(configKey: Long, packageName: String?): ByteArray?

    @Throws(RemoteException::class)
    fun getMetadata(packageName: String?): ByteArray?

    @Throws(RemoteException::class)
    fun getRegisteredExperimentIds(): LongArray?

    @Throws(RemoteException::class)
    fun registerPullAtomCallback(
        atomTag: Int,
        coolDownMillis: Long,
        timeoutMillis: Long,
        additiveFields: IntArray?,
        callback: IBinder?,
    )

    @Throws(RemoteException::class)
    fun removeActiveConfigsChangedOperation(packageName: String?)

    @Throws(RemoteException::class)
    fun removeConfiguration(configKey: Long, packageName: String?)

    @Throws(RemoteException::class)
    fun removeDataFetchOperation(configKey: Long, packageName: String?)

    @Throws(RemoteException::class)
    fun setActiveConfigsChangedOperation(
        pendingIntent: PendingIntent?,
        packageName: String?,
    ): LongArray?

    @Throws(RemoteException::class)
    fun setBroadcastSubscriber(
        configKey: Long,
        subscriberId: Long,
        pendingIntent: PendingIntent?,
        packageName: String?,
    )

    @Throws(RemoteException::class)
    fun setDataFetchOperation(
        configKey: Long,
        pendingIntent: PendingIntent?,
        packageName: String?,
    )

    @Throws(RemoteException::class)
    fun unregisterPullAtomCallback(atomTag: Int)

    @Throws(RemoteException::class)
    fun unsetBroadcastSubscriber(
        configKey: Long,
        subscriberId: Long,
        packageName: String?,
    )

    abstract class Stub : Binder(), IStatsManagerService {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        class Proxy(private val mRemote: IBinder) : IStatsManagerService {

            override fun asBinder(): IBinder = mRemote

            fun getInterfaceDescriptor(): String = DESCRIPTOR

            @Throws(RemoteException::class)
            override fun addConfiguration(configKey: Long, config: ByteArray?, packageName: String?) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeLong(configKey)
                    data.writeByteArray(config)
                    data.writeString(packageName)
                    mRemote.transact(TRANSACTION_addConfiguration, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getData(configKey: Long, packageName: String?): ByteArray? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeLong(configKey)
                    data.writeString(packageName)
                    mRemote.transact(TRANSACTION_getData, data, reply, 0)
                    reply.readException()
                    return reply.createByteArray()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getMetadata(packageName: String?): ByteArray? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(packageName)
                    mRemote.transact(TRANSACTION_getMetadata, data, reply, 0)
                    reply.readException()
                    return reply.createByteArray()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getRegisteredExperimentIds(): LongArray? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_getRegisteredExperimentIds, data, reply, 0)
                    reply.readException()
                    return reply.createLongArray()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun registerPullAtomCallback(
                atomTag: Int,
                coolDownMillis: Long,
                timeoutMillis: Long,
                additiveFields: IntArray?,
                callback: IBinder?,
            ) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(atomTag)
                    data.writeLong(coolDownMillis)
                    data.writeLong(timeoutMillis)
                    data.writeIntArray(additiveFields)
                    data.writeStrongBinder(callback)
                    // One-way call — no reply, FLAG_ONEWAY = 1.
                    mRemote.transact(TRANSACTION_registerPullAtomCallback, data, null, 1)
                } finally {
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun removeActiveConfigsChangedOperation(packageName: String?) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(packageName)
                    mRemote.transact(TRANSACTION_removeActiveConfigsChangedOperation, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun removeConfiguration(configKey: Long, packageName: String?) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeLong(configKey)
                    data.writeString(packageName)
                    mRemote.transact(TRANSACTION_removeConfiguration, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun removeDataFetchOperation(configKey: Long, packageName: String?) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeLong(configKey)
                    data.writeString(packageName)
                    mRemote.transact(TRANSACTION_removeDataFetchOperation, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun setActiveConfigsChangedOperation(
                pendingIntent: PendingIntent?,
                packageName: String?,
            ): LongArray? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    _Parcel.writeTypedObject(data, pendingIntent, 0)
                    data.writeString(packageName)
                    mRemote.transact(TRANSACTION_setActiveConfigsChangedOperation, data, reply, 0)
                    reply.readException()
                    return reply.createLongArray()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun setBroadcastSubscriber(
                configKey: Long,
                subscriberId: Long,
                pendingIntent: PendingIntent?,
                packageName: String?,
            ) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeLong(configKey)
                    data.writeLong(subscriberId)
                    _Parcel.writeTypedObject(data, pendingIntent, 0)
                    data.writeString(packageName)
                    mRemote.transact(TRANSACTION_setBroadcastSubscriber, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun setDataFetchOperation(
                configKey: Long,
                pendingIntent: PendingIntent?,
                packageName: String?,
            ) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeLong(configKey)
                    _Parcel.writeTypedObject(data, pendingIntent, 0)
                    data.writeString(packageName)
                    mRemote.transact(TRANSACTION_setDataFetchOperation, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun unregisterPullAtomCallback(atomTag: Int) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(atomTag)
                    // One-way call — no reply, FLAG_ONEWAY = 1.
                    mRemote.transact(TRANSACTION_unregisterPullAtomCallback, data, null, 1)
                } finally {
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun unsetBroadcastSubscriber(
                configKey: Long,
                subscriberId: Long,
                packageName: String?,
            ) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeLong(configKey)
                    data.writeLong(subscriberId)
                    data.writeString(packageName)
                    mRemote.transact(TRANSACTION_unsetBroadcastSubscriber, data, reply, 0)
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
                TRANSACTION_setDataFetchOperation -> {
                    setDataFetchOperation(
                        data.readLong(),
                        _Parcel.readTypedObject(data, PendingIntent.CREATOR),
                        data.readString(),
                    )
                    reply.writeNoException()
                    true
                }
                TRANSACTION_removeDataFetchOperation -> {
                    removeDataFetchOperation(data.readLong(), data.readString())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_setActiveConfigsChangedOperation -> {
                    val result = setActiveConfigsChangedOperation(
                        _Parcel.readTypedObject(data, PendingIntent.CREATOR),
                        data.readString(),
                    )
                    reply.writeNoException()
                    reply.writeLongArray(result)
                    true
                }
                TRANSACTION_removeActiveConfigsChangedOperation -> {
                    removeActiveConfigsChangedOperation(data.readString())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_setBroadcastSubscriber -> {
                    setBroadcastSubscriber(
                        data.readLong(),
                        data.readLong(),
                        _Parcel.readTypedObject(data, PendingIntent.CREATOR),
                        data.readString(),
                    )
                    reply.writeNoException()
                    true
                }
                TRANSACTION_unsetBroadcastSubscriber -> {
                    unsetBroadcastSubscriber(
                        data.readLong(),
                        data.readLong(),
                        data.readString(),
                    )
                    reply.writeNoException()
                    true
                }
                TRANSACTION_getRegisteredExperimentIds -> {
                    reply.writeNoException()
                    reply.writeLongArray(getRegisteredExperimentIds())
                    true
                }
                TRANSACTION_getMetadata -> {
                    reply.writeNoException()
                    reply.writeByteArray(getMetadata(data.readString()))
                    true
                }
                TRANSACTION_getData -> {
                    reply.writeNoException()
                    reply.writeByteArray(getData(data.readLong(), data.readString()))
                    true
                }
                TRANSACTION_addConfiguration -> {
                    addConfiguration(
                        data.readLong(),
                        data.createByteArray(),
                        data.readString(),
                    )
                    reply.writeNoException()
                    true
                }
                TRANSACTION_removeConfiguration -> {
                    removeConfiguration(data.readLong(), data.readString())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_registerPullAtomCallback -> {
                    registerPullAtomCallback(
                        data.readInt(),
                        data.readLong(),
                        data.readLong(),
                        data.createIntArray(),
                        data.readStrongBinder(),
                    )
                    // One-way — no writeNoException.
                    true
                }
                TRANSACTION_unregisterPullAtomCallback -> {
                    unregisterPullAtomCallback(data.readInt())
                    // One-way — no writeNoException.
                    true
                }
                else -> super.onTransact(code, data, reply, flags)
            }
        }

        companion object {
            private const val TRANSACTION_setDataFetchOperation = 1
            private const val TRANSACTION_removeDataFetchOperation = 2
            private const val TRANSACTION_setActiveConfigsChangedOperation = 3
            private const val TRANSACTION_removeActiveConfigsChangedOperation = 4
            private const val TRANSACTION_setBroadcastSubscriber = 5
            private const val TRANSACTION_unsetBroadcastSubscriber = 6
            private const val TRANSACTION_getRegisteredExperimentIds = 7
            private const val TRANSACTION_getMetadata = 8
            private const val TRANSACTION_getData = 9
            private const val TRANSACTION_addConfiguration = 10
            private const val TRANSACTION_removeConfiguration = 11
            private const val TRANSACTION_registerPullAtomCallback = 12
            private const val TRANSACTION_unregisterPullAtomCallback = 13

            @JvmStatic
            fun asInterface(iBinder: IBinder?): IStatsManagerService? {
                if (iBinder == null) return null
                val local = iBinder.queryLocalInterface(DESCRIPTOR)
                return if (local == null || local !is IStatsManagerService) {
                    Proxy(iBinder)
                } else {
                    local
                }
            }
        }
    }

    class Default : IStatsManagerService {

        override fun asBinder(): IBinder? = null

        @Throws(RemoteException::class)
        override fun addConfiguration(configKey: Long, config: ByteArray?, packageName: String?) {}

        @Throws(RemoteException::class)
        override fun getData(configKey: Long, packageName: String?): ByteArray? = null

        @Throws(RemoteException::class)
        override fun getMetadata(packageName: String?): ByteArray? = null

        @Throws(RemoteException::class)
        override fun getRegisteredExperimentIds(): LongArray? = null

        @Throws(RemoteException::class)
        override fun registerPullAtomCallback(
            atomTag: Int,
            coolDownMillis: Long,
            timeoutMillis: Long,
            additiveFields: IntArray?,
            callback: IBinder?,
        ) {}

        @Throws(RemoteException::class)
        override fun removeActiveConfigsChangedOperation(packageName: String?) {}

        @Throws(RemoteException::class)
        override fun removeConfiguration(configKey: Long, packageName: String?) {}

        @Throws(RemoteException::class)
        override fun removeDataFetchOperation(configKey: Long, packageName: String?) {}

        @Throws(RemoteException::class)
        override fun setActiveConfigsChangedOperation(
            pendingIntent: PendingIntent?,
            packageName: String?,
        ): LongArray? = null

        @Throws(RemoteException::class)
        override fun setBroadcastSubscriber(
            configKey: Long,
            subscriberId: Long,
            pendingIntent: PendingIntent?,
            packageName: String?,
        ) {}

        @Throws(RemoteException::class)
        override fun setDataFetchOperation(
            configKey: Long,
            pendingIntent: PendingIntent?,
            packageName: String?,
        ) {}

        @Throws(RemoteException::class)
        override fun unregisterPullAtomCallback(atomTag: Int) {}

        @Throws(RemoteException::class)
        override fun unsetBroadcastSubscriber(
            configKey: Long,
            subscriberId: Long,
            packageName: String?,
        ) {}
    }
}
package com.ims134.doclone.io

import android.app.PendingIntent
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException

/* JADX INFO: loaded from: classes.dex */
interface ILockSettings : IInterface {

    @Throws(RemoteException::class)
    fun checkVoldPassword(userId: Int): Boolean

    @Throws(RemoteException::class)
    fun closeSession(alias: String?)

    @Throws(RemoteException::class)
    fun generateKey(password: String?): String?

    @Throws(RemoteException::class)
    fun getBoolean(key: String?, defaultValue: Boolean, userId: Int): Boolean

    @Throws(RemoteException::class)
    fun getHashFactor(password: String?, userId: Int): ByteArray?

    @Throws(RemoteException::class)
    fun getKey(alias: String?): String?

    @Throws(RemoteException::class)
    fun getLong(key: String?, defaultValue: Long, userId: Int): Long

    @Throws(RemoteException::class)
    fun getRecoverySecretTypes(): IntArray?

    @Throws(RemoteException::class)
    fun getRecoveryStatus(): Map<*, *>?

    @Throws(RemoteException::class)
    fun getSeparateProfileChallengeEnabled(userId: Int): Boolean

    @Throws(RemoteException::class)
    fun getString(key: String?, defaultValue: String?, userId: Int): String?

    @Throws(RemoteException::class)
    fun getStrongAuthForUser(userId: Int): Int

    @Throws(RemoteException::class)
    fun havePassword(userId: Int): Boolean

    @Throws(RemoteException::class)
    fun havePattern(userId: Int): Boolean

    @Throws(RemoteException::class)
    fun importKey(alias: String?, key: ByteArray?): String?

    @Throws(RemoteException::class)
    fun initRecoveryServiceWithSigFile(
        rootCertificateAlias: String?,
        recoveryCert: ByteArray?,
        sigFile: ByteArray?,
    )

    @Throws(RemoteException::class)
    fun registerStrongAuthTracker(token: IBinder?)

    @Throws(RemoteException::class)
    fun removeKey(alias: String?)

    @Throws(RemoteException::class)
    fun requireStrongAuth(strongAuthReason: Int, userId: Int)

    @Throws(RemoteException::class)
    fun resetKeyStore(userId: Int)

    @Throws(RemoteException::class)
    fun setLockCredential(
        credential: String?,
        type: Int,
        savedCredential: String?,
        requestedQuality: Int,
        userId: Int,
    )

    @Throws(RemoteException::class)
    fun setLong(key: String?, value: Long, userId: Int)

    @Throws(RemoteException::class)
    fun setRecoverySecretTypes(secretTypes: IntArray?)

    @Throws(RemoteException::class)
    fun setRecoveryStatus(password: String?, status: Int)

    @Throws(RemoteException::class)
    fun setSeparateProfileChallengeEnabled(
        userId: Int,
        enabled: Boolean,
        managedUserPassword: String?,
    )

    @Throws(RemoteException::class)
    fun setServerParams(serverParams: ByteArray?)

    @Throws(RemoteException::class)
    fun setSnapshotCreatedPendingIntent(intent: PendingIntent?)

    @Throws(RemoteException::class)
    fun setString(key: String?, value: String?, userId: Int)

    @Throws(RemoteException::class)
    fun systemReady()

    @Throws(RemoteException::class)
    fun unregisterStrongAuthTracker(token: IBinder?)

    @Throws(RemoteException::class)
    fun userPresent(userId: Int)

    class Default : ILockSettings {

        override fun asBinder(): IBinder? = null

        @Throws(RemoteException::class) override fun checkVoldPassword(userId: Int): Boolean = false
        @Throws(RemoteException::class) override fun closeSession(alias: String?) {}
        @Throws(RemoteException::class) override fun generateKey(password: String?): String? = null
        @Throws(RemoteException::class) override fun getBoolean(key: String?, defaultValue: Boolean, userId: Int): Boolean = false
        @Throws(RemoteException::class) override fun getHashFactor(password: String?, userId: Int): ByteArray? = null
        @Throws(RemoteException::class) override fun getKey(alias: String?): String? = null
        @Throws(RemoteException::class) override fun getLong(key: String?, defaultValue: Long, userId: Int): Long = 0L
        @Throws(RemoteException::class) override fun getRecoverySecretTypes(): IntArray? = null
        @Throws(RemoteException::class) override fun getRecoveryStatus(): Map<*, *>? = null
        @Throws(RemoteException::class) override fun getSeparateProfileChallengeEnabled(userId: Int): Boolean = false
        @Throws(RemoteException::class) override fun getString(key: String?, defaultValue: String?, userId: Int): String? = null
        @Throws(RemoteException::class) override fun getStrongAuthForUser(userId: Int): Int = 0
        @Throws(RemoteException::class) override fun havePassword(userId: Int): Boolean = false
        @Throws(RemoteException::class) override fun havePattern(userId: Int): Boolean = false
        @Throws(RemoteException::class) override fun importKey(alias: String?, key: ByteArray?): String? = null
        @Throws(RemoteException::class) override fun initRecoveryServiceWithSigFile(rootCertificateAlias: String?, recoveryCert: ByteArray?, sigFile: ByteArray?) {}
        @Throws(RemoteException::class) override fun registerStrongAuthTracker(token: IBinder?) {}
        @Throws(RemoteException::class) override fun removeKey(alias: String?) {}
        @Throws(RemoteException::class) override fun requireStrongAuth(strongAuthReason: Int, userId: Int) {}
        @Throws(RemoteException::class) override fun resetKeyStore(userId: Int) {}
        @Throws(RemoteException::class) override fun setLockCredential(credential: String?, type: Int, savedCredential: String?, requestedQuality: Int, userId: Int) {}
        @Throws(RemoteException::class) override fun setLong(key: String?, value: Long, userId: Int) {}
        @Throws(RemoteException::class) override fun setRecoverySecretTypes(secretTypes: IntArray?) {}
        @Throws(RemoteException::class) override fun setRecoveryStatus(password: String?, status: Int) {}
        @Throws(RemoteException::class) override fun setSeparateProfileChallengeEnabled(userId: Int, enabled: Boolean, managedUserPassword: String?) {}
        @Throws(RemoteException::class) override fun setServerParams(serverParams: ByteArray?) {}
        @Throws(RemoteException::class) override fun setSnapshotCreatedPendingIntent(intent: PendingIntent?) {}
        @Throws(RemoteException::class) override fun setString(key: String?, value: String?, userId: Int) {}
        @Throws(RemoteException::class) override fun systemReady() {}
        @Throws(RemoteException::class) override fun unregisterStrongAuthTracker(token: IBinder?) {}
        @Throws(RemoteException::class) override fun userPresent(userId: Int) {}
    }

    abstract class Stub : Binder(), ILockSettings {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        class Proxy(private val mRemote: IBinder) : ILockSettings {

            override fun asBinder(): IBinder = mRemote

            fun getInterfaceDescriptor(): String = DESCRIPTOR

            @Throws(RemoteException::class)
            override fun checkVoldPassword(userId: Int): Boolean {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(userId)
                    mRemote.transact(TRANSACTION_checkVoldPassword, data, reply, 0)
                    reply.readException()
                    return reply.readInt() != 0
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun closeSession(alias: String?) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(alias)
                    mRemote.transact(TRANSACTION_closeSession, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun generateKey(password: String?): String? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(password)
                    mRemote.transact(TRANSACTION_generateKey, data, reply, 0)
                    reply.readException()
                    return reply.readString()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getBoolean(key: String?, defaultValue: Boolean, userId: Int): Boolean {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(key)
                    data.writeInt(if (defaultValue) 1 else 0)
                    data.writeInt(userId)
                    mRemote.transact(TRANSACTION_getBoolean, data, reply, 0)
                    reply.readException()
                    return reply.readInt() != 0
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getHashFactor(password: String?, userId: Int): ByteArray? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(password)
                    data.writeInt(userId)
                    mRemote.transact(TRANSACTION_getHashFactor, data, reply, 0)
                    reply.readException()
                    return reply.createByteArray()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getKey(alias: String?): String? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(alias)
                    mRemote.transact(TRANSACTION_getKey, data, reply, 0)
                    reply.readException()
                    return reply.readString()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getLong(key: String?, defaultValue: Long, userId: Int): Long {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(key)
                    data.writeLong(defaultValue)
                    data.writeInt(userId)
                    mRemote.transact(TRANSACTION_getLong, data, reply, 0)
                    reply.readException()
                    return reply.readLong()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getRecoverySecretTypes(): IntArray? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_getRecoverySecretTypes, data, reply, 0)
                    reply.readException()
                    return reply.createIntArray()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getRecoveryStatus(): Map<*, *>? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_getRecoveryStatus, data, reply, 0)
                    reply.readException()
                    return reply.readHashMap(javaClass.classLoader)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getSeparateProfileChallengeEnabled(userId: Int): Boolean {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(userId)
                    mRemote.transact(TRANSACTION_getSeparateProfileChallengeEnabled, data, reply, 0)
                    reply.readException()
                    return reply.readInt() != 0
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getString(key: String?, defaultValue: String?, userId: Int): String? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(key)
                    data.writeString(defaultValue)
                    data.writeInt(userId)
                    mRemote.transact(TRANSACTION_getString, data, reply, 0)
                    reply.readException()
                    return reply.readString()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getStrongAuthForUser(userId: Int): Int {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(userId)
                    mRemote.transact(TRANSACTION_getStrongAuthForUser, data, reply, 0)
                    reply.readException()
                    return reply.readInt()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun havePassword(userId: Int): Boolean {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(userId)
                    mRemote.transact(TRANSACTION_havePassword, data, reply, 0)
                    reply.readException()
                    return reply.readInt() != 0
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun havePattern(userId: Int): Boolean {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(userId)
                    mRemote.transact(TRANSACTION_havePattern, data, reply, 0)
                    reply.readException()
                    return reply.readInt() != 0
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun importKey(alias: String?, key: ByteArray?): String? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(alias)
                    data.writeByteArray(key)
                    mRemote.transact(TRANSACTION_importKey, data, reply, 0)
                    reply.readException()
                    return reply.readString()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun initRecoveryServiceWithSigFile(
                rootCertificateAlias: String?,
                recoveryCert: ByteArray?,
                sigFile: ByteArray?,
            ) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(rootCertificateAlias)
                    data.writeByteArray(recoveryCert)
                    data.writeByteArray(sigFile)
                    mRemote.transact(TRANSACTION_initRecoveryServiceWithSigFile, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun registerStrongAuthTracker(token: IBinder?) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeStrongBinder(token)
                    mRemote.transact(TRANSACTION_registerStrongAuthTracker, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun removeKey(alias: String?) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(alias)
                    mRemote.transact(TRANSACTION_removeKey, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun requireStrongAuth(strongAuthReason: Int, userId: Int) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(strongAuthReason)
                    data.writeInt(userId)
                    mRemote.transact(TRANSACTION_requireStrongAuth, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun resetKeyStore(userId: Int) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(userId)
                    mRemote.transact(TRANSACTION_resetKeyStore, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun setLockCredential(
                credential: String?,
                type: Int,
                savedCredential: String?,
                requestedQuality: Int,
                userId: Int,
            ) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(credential)
                    data.writeInt(type)
                    data.writeString(savedCredential)
                    data.writeInt(requestedQuality)
                    data.writeInt(userId)
                    mRemote.transact(TRANSACTION_setLockCredential, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun setLong(key: String?, value: Long, userId: Int) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(key)
                    data.writeLong(value)
                    data.writeInt(userId)
                    mRemote.transact(TRANSACTION_setLong, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun setRecoverySecretTypes(secretTypes: IntArray?) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeIntArray(secretTypes)
                    mRemote.transact(TRANSACTION_setRecoverySecretTypes, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun setRecoveryStatus(password: String?, status: Int) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(password)
                    data.writeInt(status)
                    mRemote.transact(TRANSACTION_setRecoveryStatus, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun setSeparateProfileChallengeEnabled(
                userId: Int,
                enabled: Boolean,
                managedUserPassword: String?,
            ) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(userId)
                    data.writeInt(if (enabled) 1 else 0)
                    data.writeString(managedUserPassword)
                    mRemote.transact(TRANSACTION_setSeparateProfileChallengeEnabled, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun setServerParams(serverParams: ByteArray?) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeByteArray(serverParams)
                    mRemote.transact(TRANSACTION_setServerParams, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun setSnapshotCreatedPendingIntent(intent: PendingIntent?) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    // Manual Parcelable write — the original doesn't use the _Parcel helper.
                    if (intent != null) {
                        data.writeInt(1)
                        intent.writeToParcel(data, 0)
                    } else {
                        data.writeInt(0)
                    }
                    mRemote.transact(TRANSACTION_setSnapshotCreatedPendingIntent, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun setString(key: String?, value: String?, userId: Int) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(key)
                    data.writeString(value)
                    data.writeInt(userId)
                    mRemote.transact(TRANSACTION_setString, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun systemReady() {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_systemReady, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun unregisterStrongAuthTracker(token: IBinder?) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeStrongBinder(token)
                    mRemote.transact(TRANSACTION_unregisterStrongAuthTracker, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun userPresent(userId: Int) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(userId)
                    mRemote.transact(TRANSACTION_userPresent, data, reply, 0)
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
                TRANSACTION_setRecoverySecretTypes -> {
                    setRecoverySecretTypes(data.createIntArray())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_getRecoverySecretTypes -> {
                    reply.writeNoException()
                    reply.writeIntArray(getRecoverySecretTypes())
                    true
                }
                TRANSACTION_setLong -> {
                    setLong(data.readString(), data.readLong(), data.readInt())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_setString -> {
                    setString(data.readString(), data.readString(), data.readInt())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_getBoolean -> {
                    val result = getBoolean(data.readString(), data.readInt() != 0, data.readInt())
                    reply.writeNoException()
                    reply.writeInt(if (result) 1 else 0)
                    true
                }
                TRANSACTION_getLong -> {
                    val result = getLong(data.readString(), data.readLong(), data.readInt())
                    reply.writeNoException()
                    reply.writeLong(result)
                    true
                }
                TRANSACTION_getString -> {
                    val result = getString(data.readString(), data.readString(), data.readInt())
                    reply.writeNoException()
                    reply.writeString(result)
                    true
                }
                TRANSACTION_setLockCredential -> {
                    setLockCredential(
                        data.readString(),
                        data.readInt(),
                        data.readString(),
                        data.readInt(),
                        data.readInt(),
                    )
                    reply.writeNoException()
                    true
                }
                TRANSACTION_resetKeyStore -> {
                    resetKeyStore(data.readInt())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_checkVoldPassword -> {
                    val result = checkVoldPassword(data.readInt())
                    reply.writeNoException()
                    reply.writeInt(if (result) 1 else 0)
                    true
                }
                TRANSACTION_havePattern -> {
                    val result = havePattern(data.readInt())
                    reply.writeNoException()
                    reply.writeInt(if (result) 1 else 0)
                    true
                }
                TRANSACTION_havePassword -> {
                    val result = havePassword(data.readInt())
                    reply.writeNoException()
                    reply.writeInt(if (result) 1 else 0)
                    true
                }
                TRANSACTION_getHashFactor -> {
                    reply.writeNoException()
                    reply.writeByteArray(getHashFactor(data.readString(), data.readInt()))
                    true
                }
                TRANSACTION_setSeparateProfileChallengeEnabled -> {
                    setSeparateProfileChallengeEnabled(
                        data.readInt(),
                        data.readInt() != 0,
                        data.readString(),
                    )
                    reply.writeNoException()
                    true
                }
                TRANSACTION_getSeparateProfileChallengeEnabled -> {
                    val result = getSeparateProfileChallengeEnabled(data.readInt())
                    reply.writeNoException()
                    reply.writeInt(if (result) 1 else 0)
                    true
                }
                TRANSACTION_registerStrongAuthTracker -> {
                    registerStrongAuthTracker(data.readStrongBinder())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_unregisterStrongAuthTracker -> {
                    unregisterStrongAuthTracker(data.readStrongBinder())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_requireStrongAuth -> {
                    requireStrongAuth(data.readInt(), data.readInt())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_systemReady -> {
                    systemReady()
                    reply.writeNoException()
                    true
                }
                TRANSACTION_userPresent -> {
                    userPresent(data.readInt())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_getStrongAuthForUser -> {
                    reply.writeNoException()
                    reply.writeInt(getStrongAuthForUser(data.readInt()))
                    true
                }
                TRANSACTION_initRecoveryServiceWithSigFile -> {
                    initRecoveryServiceWithSigFile(
                        data.readString(),
                        data.createByteArray(),
                        data.createByteArray(),
                    )
                    reply.writeNoException()
                    true
                }
                TRANSACTION_generateKey -> {
                    reply.writeNoException()
                    reply.writeString(generateKey(data.readString()))
                    true
                }
                TRANSACTION_importKey -> {
                    reply.writeNoException()
                    reply.writeString(importKey(data.readString(), data.createByteArray()))
                    true
                }
                TRANSACTION_getKey -> {
                    reply.writeNoException()
                    reply.writeString(getKey(data.readString()))
                    true
                }
                TRANSACTION_removeKey -> {
                    removeKey(data.readString())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_setSnapshotCreatedPendingIntent -> {
                    setSnapshotCreatedPendingIntent(
                        if (data.readInt() != 0) PendingIntent.CREATOR.createFromParcel(data) else null
                    )
                    reply.writeNoException()
                    true
                }
                TRANSACTION_setServerParams -> {
                    setServerParams(data.createByteArray())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_setRecoveryStatus -> {
                    setRecoveryStatus(data.readString(), data.readInt())
                    reply.writeNoException()
                    true
                }
                TRANSACTION_getRecoveryStatus -> {
                    reply.writeNoException()
                    reply.writeMap(getRecoveryStatus())
                    true
                }
                TRANSACTION_closeSession -> {
                    closeSession(data.readString())
                    reply.writeNoException()
                    true
                }
                else -> super.onTransact(code, data, reply, flags)
            }
        }

        companion object {
            const val DESCRIPTOR = "com.android.internal.widget.ILockSettings"

            private const val TRANSACTION_setRecoverySecretTypes = 1
            private const val TRANSACTION_getRecoverySecretTypes = 2
            private const val TRANSACTION_setLong = 3
            private const val TRANSACTION_setString = 4
            private const val TRANSACTION_getBoolean = 5
            private const val TRANSACTION_getLong = 6
            private const val TRANSACTION_getString = 7
            private const val TRANSACTION_setLockCredential = 8
            private const val TRANSACTION_resetKeyStore = 9
            private const val TRANSACTION_checkVoldPassword = 10
            private const val TRANSACTION_havePattern = 11
            private const val TRANSACTION_havePassword = 12
            private const val TRANSACTION_getHashFactor = 13
            private const val TRANSACTION_setSeparateProfileChallengeEnabled = 14
            private const val TRANSACTION_getSeparateProfileChallengeEnabled = 15
            private const val TRANSACTION_registerStrongAuthTracker = 16
            private const val TRANSACTION_unregisterStrongAuthTracker = 17
            private const val TRANSACTION_requireStrongAuth = 18
            private const val TRANSACTION_systemReady = 19
            private const val TRANSACTION_userPresent = 20
            private const val TRANSACTION_getStrongAuthForUser = 21
            private const val TRANSACTION_initRecoveryServiceWithSigFile = 22
            private const val TRANSACTION_generateKey = 23
            private const val TRANSACTION_importKey = 24
            private const val TRANSACTION_getKey = 25
            private const val TRANSACTION_removeKey = 26
            private const val TRANSACTION_setSnapshotCreatedPendingIntent = 27
            private const val TRANSACTION_setServerParams = 28
            private const val TRANSACTION_setRecoveryStatus = 29
            private const val TRANSACTION_getRecoveryStatus = 30
            private const val TRANSACTION_closeSession = 31

            @JvmStatic
            fun asInterface(iBinder: IBinder?): ILockSettings? {
                if (iBinder == null) return null
                val local = iBinder.queryLocalInterface(DESCRIPTOR)
                return if (local == null || local !is ILockSettings) {
                    Proxy(iBinder)
                } else {
                    local
                }
            }
        }
    }
}
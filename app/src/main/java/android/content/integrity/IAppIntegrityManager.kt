package android.content.integrity

import android.content.IntentSender
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException

/* JADX INFO: loaded from: classes.dex */
interface IAppIntegrityManager : IInterface {

    companion object {
        const val DESCRIPTOR = "android.content.integrity.IAppIntegrityManager"
    }

    @Throws(RemoteException::class)
    fun getCurrentRuleSetProvider(): String?

    @Throws(RemoteException::class)
    fun getCurrentRuleSetVersion(): String?

    @Throws(RemoteException::class)
    fun getCurrentRules(): Bundle?

    @Throws(RemoteException::class)
    fun getWhitelistedRuleProviders(): List<String>?

    @Throws(RemoteException::class)
    fun updateRuleSet(version: String?, rules: Bundle?, statusReceiver: IntentSender?)

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

    abstract class Stub : Binder(), IAppIntegrityManager {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        class Proxy(private val mRemote: IBinder) : IAppIntegrityManager {

            override fun asBinder(): IBinder = mRemote

            fun getInterfaceDescriptor(): String = DESCRIPTOR

            @Throws(RemoteException::class)
            override fun getCurrentRuleSetProvider(): String? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_getCurrentRuleSetProvider, data, reply, 0)
                    reply.readException()
                    return reply.readString()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getCurrentRuleSetVersion(): String? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_getCurrentRuleSetVersion, data, reply, 0)
                    reply.readException()
                    return reply.readString()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getCurrentRules(): Bundle? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_getCurrentRules, data, reply, 0)
                    reply.readException()
                    return _Parcel.readTypedObject(reply, Bundle.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getWhitelistedRuleProviders(): List<String>? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(TRANSACTION_getWhitelistedRuleProviders, data, reply, 0)
                    reply.readException()
                    return reply.createStringArrayList()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun updateRuleSet(
                version: String?,
                rules: Bundle?,
                statusReceiver: IntentSender?,
            ) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(version)
                    _Parcel.writeTypedObject(data, rules, 0)
                    _Parcel.writeTypedObject(data, statusReceiver, 0)
                    mRemote.transact(TRANSACTION_updateRuleSet, data, reply, 0)
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
                TRANSACTION_updateRuleSet -> {
                    updateRuleSet(
                        data.readString(),
                        _Parcel.readTypedObject(data, Bundle.CREATOR),
                        _Parcel.readTypedObject(data, IntentSender.CREATOR),
                    )
                    reply.writeNoException()
                    true
                }
                TRANSACTION_getCurrentRuleSetVersion -> {
                    reply.writeNoException()
                    reply.writeString(getCurrentRuleSetVersion())
                    true
                }
                TRANSACTION_getCurrentRuleSetProvider -> {
                    reply.writeNoException()
                    reply.writeString(getCurrentRuleSetProvider())
                    true
                }
                TRANSACTION_getCurrentRules -> {
                    val rules = getCurrentRules()
                    reply.writeNoException()
                    _Parcel.writeTypedObject(reply, rules, 1)
                    true
                }
                TRANSACTION_getWhitelistedRuleProviders -> {
                    reply.writeNoException()
                    reply.writeStringList(getWhitelistedRuleProviders())
                    true
                }
                else -> super.onTransact(code, data, reply, flags)
            }
        }

        companion object {
            private const val TRANSACTION_updateRuleSet = 1
            private const val TRANSACTION_getCurrentRuleSetVersion = 2
            private const val TRANSACTION_getCurrentRuleSetProvider = 3
            private const val TRANSACTION_getCurrentRules = 4
            private const val TRANSACTION_getWhitelistedRuleProviders = 5

            @JvmStatic
            fun asInterface(iBinder: IBinder?): IAppIntegrityManager? {
                if (iBinder == null) return null
                val local = iBinder.queryLocalInterface(DESCRIPTOR)
                return if (local == null || local !is IAppIntegrityManager) {
                    Proxy(iBinder)
                } else {
                    local
                }
            }
        }
    }

    class Default : IAppIntegrityManager {

        override fun asBinder(): IBinder? = null

        @Throws(RemoteException::class)
        override fun getCurrentRuleSetProvider(): String? = null

        @Throws(RemoteException::class)
        override fun getCurrentRuleSetVersion(): String? = null

        @Throws(RemoteException::class)
        override fun getCurrentRules(): Bundle? = null

        @Throws(RemoteException::class)
        override fun getWhitelistedRuleProviders(): List<String>? = null

        @Throws(RemoteException::class)
        override fun updateRuleSet(
            version: String?,
            rules: Bundle?,
            statusReceiver: IntentSender?,
        ) {}
    }
}
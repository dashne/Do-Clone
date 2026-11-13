package com.ims134.doclone.io

import android.accounts.Account
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException

/**
 * Hidden framework AIDL (all calls oneway). Compile-time stub: the real class comes from the system
 * at runtime, so keep it out of the APK (compileOnly module, or R8 -dontwarn / exclude).
 */
interface IAccountAuthenticator : IInterface {

    @Throws(RemoteException::class)
    fun addAccount(
        response: IAccountAuthenticatorResponse?, accountType: String?, authTokenType: String?,
        requiredFeatures: Array<String>?, options: Bundle?
    )

    @Throws(RemoteException::class)
    fun addAccountFromCredentials(response: IAccountAuthenticatorResponse?, account: Account?, accountCredentials: Bundle?)

    @Throws(RemoteException::class)
    fun confirmCredentials(response: IAccountAuthenticatorResponse?, account: Account?, options: Bundle?)

    @Throws(RemoteException::class)
    fun editProperties(response: IAccountAuthenticatorResponse?, accountType: String?)

    @Throws(RemoteException::class)
    fun getAccountCredentialsForCloning(response: IAccountAuthenticatorResponse?, account: Account?)

    @Throws(RemoteException::class)
    fun getAccountRemovalAllowed(response: IAccountAuthenticatorResponse?, account: Account?)

    @Throws(RemoteException::class)
    fun getAuthToken(response: IAccountAuthenticatorResponse?, account: Account?, authTokenType: String?, options: Bundle?)

    @Throws(RemoteException::class)
    fun getAuthTokenLabel(response: IAccountAuthenticatorResponse?, authTokenType: String?)

    @Throws(RemoteException::class)
    fun hasFeatures(response: IAccountAuthenticatorResponse?, account: Account?, features: Array<String>?)

    @Throws(RemoteException::class)
    fun updateCredentials(response: IAccountAuthenticatorResponse?, account: Account?, authTokenType: String?, options: Bundle?)

    abstract class Stub : Binder(), IAccountAuthenticator {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code in 1..0xFFFFFF) data.enforceInterface(DESCRIPTOR)
            if (code == INTERFACE_TRANSACTION) {
                reply?.writeString(DESCRIPTOR)
                return true
            }
            when (code) {
                TRANSACTION_addAccount -> addAccount(
                    data.readResponse(), data.readString(), data.readString(), data.createStringArray(), data.readBundleObject()
                )
                TRANSACTION_confirmCredentials ->
                    confirmCredentials(data.readResponse(), data.readAccount(), data.readBundleObject())
                TRANSACTION_getAuthToken ->
                    getAuthToken(data.readResponse(), data.readAccount(), data.readString(), data.readBundleObject())
                TRANSACTION_getAuthTokenLabel -> getAuthTokenLabel(data.readResponse(), data.readString())
                TRANSACTION_updateCredentials ->
                    updateCredentials(data.readResponse(), data.readAccount(), data.readString(), data.readBundleObject())
                TRANSACTION_editProperties -> editProperties(data.readResponse(), data.readString())
                TRANSACTION_hasFeatures -> hasFeatures(data.readResponse(), data.readAccount(), data.createStringArray())
                TRANSACTION_getAccountRemovalAllowed -> getAccountRemovalAllowed(data.readResponse(), data.readAccount())
                TRANSACTION_getAccountCredentialsForCloning ->
                    getAccountCredentialsForCloning(data.readResponse(), data.readAccount())
                TRANSACTION_addAccountFromCredentials ->
                    addAccountFromCredentials(data.readResponse(), data.readAccount(), data.readBundleObject())
                else -> return super.onTransact(code, data, reply, flags)
            }
            return true
        }

        private fun Parcel.readResponse(): IAccountAuthenticatorResponse? =
            IAccountAuthenticatorResponse.Stub.asInterface(readStrongBinder())
        private fun Parcel.readAccount(): Account? = readTypedObject(Account.CREATOR)
        private fun Parcel.readBundleObject(): Bundle? = readTypedObject(Bundle.CREATOR)

        class Proxy(private val remote: IBinder) : IAccountAuthenticator {

            override fun asBinder(): IBinder = remote

            fun getInterfaceDescriptor(): String = DESCRIPTOR

            /** Every call is oneway: no reply parcel. */
            private inline fun oneway(code: Int, write: Parcel.() -> Unit) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.write()
                    remote.transact(code, data, null, IBinder.FLAG_ONEWAY)
                } finally {
                    data.recycle()
                }
            }

            override fun addAccount(
                response: IAccountAuthenticatorResponse?, accountType: String?, authTokenType: String?,
                requiredFeatures: Array<String>?, options: Bundle?
            ) = oneway(TRANSACTION_addAccount) {
                writeStrongInterface(response); writeString(accountType); writeString(authTokenType)
                writeStringArray(requiredFeatures); writeTypedObject(options, 0)
            }

            override fun addAccountFromCredentials(
                response: IAccountAuthenticatorResponse?, account: Account?, accountCredentials: Bundle?
            ) = oneway(TRANSACTION_addAccountFromCredentials) {
                writeStrongInterface(response); writeTypedObject(account, 0); writeTypedObject(accountCredentials, 0)
            }

            override fun confirmCredentials(response: IAccountAuthenticatorResponse?, account: Account?, options: Bundle?) =
                oneway(TRANSACTION_confirmCredentials) {
                    writeStrongInterface(response); writeTypedObject(account, 0); writeTypedObject(options, 0)
                }

            override fun editProperties(response: IAccountAuthenticatorResponse?, accountType: String?) =
                oneway(TRANSACTION_editProperties) { writeStrongInterface(response); writeString(accountType) }

            override fun getAccountCredentialsForCloning(response: IAccountAuthenticatorResponse?, account: Account?) =
                oneway(TRANSACTION_getAccountCredentialsForCloning) {
                    writeStrongInterface(response); writeTypedObject(account, 0)
                }

            override fun getAccountRemovalAllowed(response: IAccountAuthenticatorResponse?, account: Account?) =
                oneway(TRANSACTION_getAccountRemovalAllowed) {
                    writeStrongInterface(response); writeTypedObject(account, 0)
                }

            override fun getAuthToken(
                response: IAccountAuthenticatorResponse?, account: Account?, authTokenType: String?, options: Bundle?
            ) = oneway(TRANSACTION_getAuthToken) {
                writeStrongInterface(response); writeTypedObject(account, 0)
                writeString(authTokenType); writeTypedObject(options, 0)
            }

            override fun getAuthTokenLabel(response: IAccountAuthenticatorResponse?, authTokenType: String?) =
                oneway(TRANSACTION_getAuthTokenLabel) { writeStrongInterface(response); writeString(authTokenType) }

            override fun hasFeatures(response: IAccountAuthenticatorResponse?, account: Account?, features: Array<String>?) =
                oneway(TRANSACTION_hasFeatures) {
                    writeStrongInterface(response); writeTypedObject(account, 0); writeStringArray(features)
                }

            override fun updateCredentials(
                response: IAccountAuthenticatorResponse?, account: Account?, authTokenType: String?, options: Bundle?
            ) = oneway(TRANSACTION_updateCredentials) {
                writeStrongInterface(response); writeTypedObject(account, 0)
                writeString(authTokenType); writeTypedObject(options, 0)
            }
        }

        companion object {
            const val DESCRIPTOR = "android.accounts.IAccountAuthenticator"
            const val TRANSACTION_addAccount = 1
            const val TRANSACTION_confirmCredentials = 2
            const val TRANSACTION_getAuthToken = 3
            const val TRANSACTION_getAuthTokenLabel = 4
            const val TRANSACTION_updateCredentials = 5
            const val TRANSACTION_editProperties = 6
            const val TRANSACTION_hasFeatures = 7
            const val TRANSACTION_getAccountRemovalAllowed = 8
            const val TRANSACTION_getAccountCredentialsForCloning = 9
            const val TRANSACTION_addAccountFromCredentials = 10

            @JvmStatic
            fun asInterface(binder: IBinder?): IAccountAuthenticator? {
                if (binder == null) return null
                val local = binder.queryLocalInterface(DESCRIPTOR)
                return if (local is IAccountAuthenticator) local else Proxy(binder)
            }
        }
    }

    open class Default : IAccountAuthenticator {
        override fun asBinder(): IBinder? = null
        override fun addAccount(
            response: IAccountAuthenticatorResponse?, accountType: String?, authTokenType: String?,
            requiredFeatures: Array<String>?, options: Bundle?
        ) {}
        override fun addAccountFromCredentials(
            response: IAccountAuthenticatorResponse?, account: Account?, accountCredentials: Bundle?
        ) {}
        override fun confirmCredentials(response: IAccountAuthenticatorResponse?, account: Account?, options: Bundle?) {}
        override fun editProperties(response: IAccountAuthenticatorResponse?, accountType: String?) {}
        override fun getAccountCredentialsForCloning(response: IAccountAuthenticatorResponse?, account: Account?) {}
        override fun getAccountRemovalAllowed(response: IAccountAuthenticatorResponse?, account: Account?) {}
        override fun getAuthToken(
            response: IAccountAuthenticatorResponse?, account: Account?, authTokenType: String?, options: Bundle?
        ) {}
        override fun getAuthTokenLabel(response: IAccountAuthenticatorResponse?, authTokenType: String?) {}
        override fun hasFeatures(response: IAccountAuthenticatorResponse?, account: Account?, features: Array<String>?) {}
        override fun updateCredentials(
            response: IAccountAuthenticatorResponse?, account: Account?, authTokenType: String?, options: Bundle?
        ) {}
    }
}
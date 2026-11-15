package com.ims134.doclone.io



import android.accounts.Account
import android.accounts.AuthenticatorDescription
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException

/** Binder interface of the virtual AccountManager (jadx: io.a23). Descriptor: com.polestar.clone.server.IAccountManager */
interface IAccountManager : IInterface {

    @Throws(RemoteException::class)
    fun accountAuthenticated(userId: Int, account: Account?): Boolean

    @Throws(RemoteException::class)
    fun addAccount(
        userId: Int, response: IAccountManagerResponse?, accountType: String?, authTokenType: String?,
        requiredFeatures: Array<String>?, expectActivityLaunch: Boolean, options: Bundle?
    )

    @Throws(RemoteException::class)
    fun addAccountExplicitly(userId: Int, account: Account?, password: String?, extras: Bundle?): Boolean

    @Throws(RemoteException::class)
    fun addAccountExplicitlyWithVisibility(
        userId: Int, account: Account?, password: String?, extras: Bundle?, visibility: Map<String, Int>?
    ): Boolean

    @Throws(RemoteException::class)
    fun clearPassword(userId: Int, account: Account?)

    @Throws(RemoteException::class)
    fun confirmCredentials(
        userId: Int, response: IAccountManagerResponse?, account: Account?, options: Bundle?,
        expectActivityLaunch: Boolean
    )

    @Throws(RemoteException::class)
    fun editProperties(userId: Int, response: IAccountManagerResponse?, accountType: String?, expectActivityLaunch: Boolean)

    @Throws(RemoteException::class)
    fun finishSessionAsUser(
        userId: Int, response: IAccountManagerResponse?, sessionBundle: Bundle?, expectActivityLaunch: Boolean,
        appInfo: Bundle?, callingUserId: Int
    )

    @Throws(RemoteException::class)
    fun getAccountVisibility(userId: Int, account: Account?, packageName: String?): Int

    @Throws(RemoteException::class)
    fun getAccounts(userId: Int, accountType: String?): Array<Account>?

    @Throws(RemoteException::class)
    fun getAccountsAndVisibilityForPackage(userId: Int, packageName: String?, accountType: String?): Map<Account, Int>?

    @Throws(RemoteException::class)
    fun getAccountsByFeatures(
        userId: Int, response: IAccountManagerResponse?, accountType: String?, features: Array<String>?
    )

    @Throws(RemoteException::class)
    fun getAuthToken(
        userId: Int, response: IAccountManagerResponse?, account: Account?, authTokenType: String?,
        notifyOnAuthFailure: Boolean, expectActivityLaunch: Boolean, loginOptions: Bundle?
    )

    @Throws(RemoteException::class)
    fun getAuthTokenLabel(userId: Int, response: IAccountManagerResponse?, accountType: String?, authTokenType: String?)

    @Throws(RemoteException::class)
    fun getAuthenticatorTypes(userId: Int): Array<AuthenticatorDescription>?

    @Throws(RemoteException::class)
    fun getPackagesAndVisibilityForAccount(userId: Int, account: Account?): Map<String, Int>?

    @Throws(RemoteException::class)
    fun getPassword(userId: Int, account: Account?): String?

    @Throws(RemoteException::class)
    fun getPreviousName(userId: Int, account: Account?): String?

    @Throws(RemoteException::class)
    fun getUserData(userId: Int, account: Account?, key: String?): String?

    @Throws(RemoteException::class)
    fun hasFeatures(userId: Int, response: IAccountManagerResponse?, account: Account?, features: Array<String>?)

    @Throws(RemoteException::class)
    fun invalidateAuthToken(userId: Int, accountType: String?, authToken: String?)

    @Throws(RemoteException::class)
    fun isCredentialsUpdateSuggested(
        userId: Int, response: IAccountManagerResponse?, account: Account?, statusToken: String?
    )

    @Throws(RemoteException::class)
    fun peekAuthToken(userId: Int, account: Account?, authTokenType: String?): String?

    @Throws(RemoteException::class)
    fun registerAccountListener(userId: Int, accountTypes: Array<String>?, opPackageName: String?)

    @Throws(RemoteException::class)
    fun removeAccount(userId: Int, response: IAccountManagerResponse?, account: Account?, expectActivityLaunch: Boolean)

    @Throws(RemoteException::class)
    fun removeAccountExplicitly(userId: Int, account: Account?): Boolean

    @Throws(RemoteException::class)
    fun renameAccount(userId: Int, response: IAccountManagerResponse?, account: Account?, newName: String?)

    @Throws(RemoteException::class)
    fun setAccountVisibility(userId: Int, account: Account?, packageName: String?, newVisibility: Int): Boolean

    @Throws(RemoteException::class)
    fun setAuthToken(userId: Int, account: Account?, authTokenType: String?, authToken: String?)

    @Throws(RemoteException::class)
    fun setPassword(userId: Int, account: Account?, password: String?)

    @Throws(RemoteException::class)
    fun setUserData(userId: Int, account: Account?, key: String?, value: String?)

    @Throws(RemoteException::class)
    fun startAddAccountSession(
        userId: Int, response: IAccountManagerResponse?, accountType: String?, authTokenType: String?,
        requiredFeatures: Array<String>?, expectActivityLaunch: Boolean, options: Bundle?
    )

    @Throws(RemoteException::class)
    fun startUpdateCredentialsSession(
        userId: Int, response: IAccountManagerResponse?, account: Account?, authTokenType: String?,
        expectActivityLaunch: Boolean, options: Bundle?
    )

    @Throws(RemoteException::class)
    fun unregisterAccountListener(userId: Int, accountTypes: Array<String>?, opPackageName: String?)

    @Throws(RemoteException::class)
    fun updateCredentials(
        userId: Int, response: IAccountManagerResponse?, account: Account?, authTokenType: String?,
        expectActivityLaunch: Boolean, options: Bundle?
    )

    companion object {
        const val DESCRIPTOR = "com.polestar.clone.server.IAccountManager"
    }

    // ---------------------------------------------------------------------------------- Stub

    abstract class Stub : Binder(), IAccountManager {

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
            if (code !in TRANSACTION_getAuthenticatorTypes..TRANSACTION_getAccountsAndVisibilityForPackage) {
                return super.onTransact(code, data, reply, flags)
            }
            val out = reply!!
            when (code) {
                TRANSACTION_getAuthenticatorTypes -> {
                    val r = getAuthenticatorTypes(data.readInt())
                    out.writeNoException()
                    out.writeTypedArray(r, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
                }
                TRANSACTION_getAccountsByFeatures -> {
                    getAccountsByFeatures(data.readInt(), data.readResponse(), data.readString(), data.createStringArray())
                    out.writeNoException()
                }
                TRANSACTION_getPreviousName -> {
                    val r = getPreviousName(data.readInt(), data.readAccount())
                    out.writeNoException()
                    out.writeString(r)
                }
                TRANSACTION_getAccounts -> {
                    val r = getAccounts(data.readInt(), data.readString())
                    out.writeNoException()
                    out.writeTypedArray(r, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
                }
                TRANSACTION_getAuthToken -> {
                    getAuthToken(
                        data.readInt(), data.readResponse(), data.readAccount(), data.readString(),
                        data.readBool(), data.readBool(), data.readBundleObject()
                    )
                    out.writeNoException()
                }
                TRANSACTION_setPassword -> {
                    setPassword(data.readInt(), data.readAccount(), data.readString())
                    out.writeNoException()
                }
                TRANSACTION_setAuthToken -> {
                    setAuthToken(data.readInt(), data.readAccount(), data.readString(), data.readString())
                    out.writeNoException()
                }
                TRANSACTION_setUserData -> {
                    setUserData(data.readInt(), data.readAccount(), data.readString(), data.readString())
                    out.writeNoException()
                }
                TRANSACTION_hasFeatures -> {
                    hasFeatures(data.readInt(), data.readResponse(), data.readAccount(), data.createStringArray())
                    out.writeNoException()
                }
                TRANSACTION_updateCredentials -> {
                    updateCredentials(
                        data.readInt(), data.readResponse(), data.readAccount(), data.readString(),
                        data.readBool(), data.readBundleObject()
                    )
                    out.writeNoException()
                }
                TRANSACTION_editProperties -> {
                    editProperties(data.readInt(), data.readResponse(), data.readString(), data.readBool())
                    out.writeNoException()
                }
                TRANSACTION_getAuthTokenLabel -> {
                    getAuthTokenLabel(data.readInt(), data.readResponse(), data.readString(), data.readString())
                    out.writeNoException()
                }
                TRANSACTION_getUserData -> {
                    val r = getUserData(data.readInt(), data.readAccount(), data.readString())
                    out.writeNoException()
                    out.writeString(r)
                }
                TRANSACTION_getPassword -> {
                    val r = getPassword(data.readInt(), data.readAccount())
                    out.writeNoException()
                    out.writeString(r)
                }
                TRANSACTION_confirmCredentials -> {
                    confirmCredentials(
                        data.readInt(), data.readResponse(), data.readAccount(), data.readBundleObject(), data.readBool()
                    )
                    out.writeNoException()
                }
                TRANSACTION_addAccount -> {
                    addAccount(
                        data.readInt(), data.readResponse(), data.readString(), data.readString(),
                        data.createStringArray(), data.readBool(), data.readBundleObject()
                    )
                    out.writeNoException()
                }
                TRANSACTION_addAccountExplicitly -> {
                    val r = addAccountExplicitly(data.readInt(), data.readAccount(), data.readString(), data.readBundleObject())
                    out.writeNoException()
                    out.writeInt(if (r) 1 else 0)
                }
                TRANSACTION_removeAccountExplicitly -> {
                    val r = removeAccountExplicitly(data.readInt(), data.readAccount())
                    out.writeNoException()
                    out.writeInt(if (r) 1 else 0)
                }
                TRANSACTION_renameAccount -> {
                    renameAccount(data.readInt(), data.readResponse(), data.readAccount(), data.readString())
                    out.writeNoException()
                }
                TRANSACTION_removeAccount -> {
                    removeAccount(data.readInt(), data.readResponse(), data.readAccount(), data.readBool())
                    out.writeNoException()
                }
                TRANSACTION_clearPassword -> {
                    clearPassword(data.readInt(), data.readAccount())
                    out.writeNoException()
                }
                TRANSACTION_accountAuthenticated -> {
                    val r = accountAuthenticated(data.readInt(), data.readAccount())
                    out.writeNoException()
                    out.writeInt(if (r) 1 else 0)
                }
                TRANSACTION_invalidateAuthToken -> {
                    invalidateAuthToken(data.readInt(), data.readString(), data.readString())
                    out.writeNoException()
                }
                TRANSACTION_peekAuthToken -> {
                    val r = peekAuthToken(data.readInt(), data.readAccount(), data.readString())
                    out.writeNoException()
                    out.writeString(r)
                }
                TRANSACTION_registerAccountListener -> {
                    registerAccountListener(data.readInt(), data.createStringArray(), data.readString())
                    out.writeNoException()
                }
                TRANSACTION_unregisterAccountListener -> {
                    unregisterAccountListener(data.readInt(), data.createStringArray(), data.readString())
                    out.writeNoException()
                }
                TRANSACTION_startAddAccountSession -> {
                    startAddAccountSession(
                        data.readInt(), data.readResponse(), data.readString(), data.readString(),
                        data.createStringArray(), data.readBool(), data.readBundleObject()
                    )
                    out.writeNoException()
                }
                TRANSACTION_startUpdateCredentialsSession -> {
                    startUpdateCredentialsSession(
                        data.readInt(), data.readResponse(), data.readAccount(), data.readString(),
                        data.readBool(), data.readBundleObject()
                    )
                    out.writeNoException()
                }
                TRANSACTION_finishSessionAsUser -> {
                    finishSessionAsUser(
                        data.readInt(), data.readResponse(), data.readBundleObject(), data.readBool(),
                        data.readBundleObject(), data.readInt()
                    )
                    out.writeNoException()
                }
                TRANSACTION_isCredentialsUpdateSuggested -> {
                    isCredentialsUpdateSuggested(data.readInt(), data.readResponse(), data.readAccount(), data.readString())
                    out.writeNoException()
                }
                TRANSACTION_getPackagesAndVisibilityForAccount -> {
                    val r = getPackagesAndVisibilityForAccount(data.readInt(), data.readAccount())
                    out.writeNoException()
                    out.writeMap(r)
                }
                TRANSACTION_addAccountExplicitlyWithVisibility -> {
                    val r = addAccountExplicitlyWithVisibility(
                        data.readInt(), data.readAccount(), data.readString(), data.readBundleObject(),
                        data.readIntMap()
                    )
                    out.writeNoException()
                    out.writeInt(if (r) 1 else 0)
                }
                TRANSACTION_setAccountVisibility -> {
                    val r = setAccountVisibility(data.readInt(), data.readAccount(), data.readString(), data.readInt())
                    out.writeNoException()
                    out.writeInt(if (r) 1 else 0)
                }
                TRANSACTION_getAccountVisibility -> {
                    val r = getAccountVisibility(data.readInt(), data.readAccount(), data.readString())
                    out.writeNoException()
                    out.writeInt(r)
                }
                TRANSACTION_getAccountsAndVisibilityForPackage -> {
                    val r = getAccountsAndVisibilityForPackage(data.readInt(), data.readString(), data.readString())
                    out.writeNoException()
                    out.writeMap(r)
                }
            }
            return true
        }

        private fun Parcel.readAccount(): Account? = readTypedObject(Account.CREATOR)
        private fun Parcel.readBundleObject(): Bundle? = readTypedObject(Bundle.CREATOR)
        private fun Parcel.readResponse(): IAccountManagerResponse? =
            IAccountManagerResponse.Stub.asInterface(readStrongBinder())
        private fun Parcel.readBool(): Boolean = readInt() != 0

        @Suppress("DEPRECATION", "UNCHECKED_CAST")
        private fun Parcel.readIntMap(): Map<String, Int>? =
            readHashMap(this@Stub.javaClass.classLoader) as Map<String, Int>?

        // ------------------------------------------------------------------------------ Proxy

        class Proxy(private val remote: IBinder) : IAccountManager {

            override fun asBinder(): IBinder = remote

            private inline fun <T> call(code: Int, write: Parcel.() -> Unit, read: Parcel.() -> T): T {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.write()
                    remote.transact(code, data, reply, 0)
                    reply.readException()
                    return reply.read()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            private fun Parcel.writeBool(value: Boolean) = writeInt(if (value) 1 else 0)

            override fun accountAuthenticated(userId: Int, account: Account?): Boolean =
                call(TRANSACTION_accountAuthenticated,
                    { writeInt(userId); writeTypedObject(account, 0) }, { readInt() != 0 })

            override fun addAccount(
                userId: Int, response: IAccountManagerResponse?, accountType: String?, authTokenType: String?,
                requiredFeatures: Array<String>?, expectActivityLaunch: Boolean, options: Bundle?
            ) = call(TRANSACTION_addAccount, {
                writeInt(userId); writeStrongInterface(response); writeString(accountType)
                writeString(authTokenType); writeStringArray(requiredFeatures)
                writeBool(expectActivityLaunch); writeTypedObject(options, 0)
            }, {})

            override fun addAccountExplicitly(userId: Int, account: Account?, password: String?, extras: Bundle?): Boolean =
                call(TRANSACTION_addAccountExplicitly, {
                    writeInt(userId); writeTypedObject(account, 0); writeString(password); writeTypedObject(extras, 0)
                }, { readInt() != 0 })

            override fun addAccountExplicitlyWithVisibility(
                userId: Int, account: Account?, password: String?, extras: Bundle?, visibility: Map<String, Int>?
            ): Boolean = call(TRANSACTION_addAccountExplicitlyWithVisibility, {
                writeInt(userId); writeTypedObject(account, 0); writeString(password)
                writeTypedObject(extras, 0); writeMap(visibility)
            }, { readInt() != 0 })

            override fun clearPassword(userId: Int, account: Account?) =
                call(TRANSACTION_clearPassword, { writeInt(userId); writeTypedObject(account, 0) }, {})

            override fun confirmCredentials(
                userId: Int, response: IAccountManagerResponse?, account: Account?, options: Bundle?,
                expectActivityLaunch: Boolean
            ) = call(TRANSACTION_confirmCredentials, {
                writeInt(userId); writeStrongInterface(response); writeTypedObject(account, 0)
                writeTypedObject(options, 0); writeBool(expectActivityLaunch)
            }, {})

            override fun editProperties(
                userId: Int, response: IAccountManagerResponse?, accountType: String?, expectActivityLaunch: Boolean
            ) = call(TRANSACTION_editProperties, {
                writeInt(userId); writeStrongInterface(response); writeString(accountType); writeBool(expectActivityLaunch)
            }, {})

            override fun finishSessionAsUser(
                userId: Int, response: IAccountManagerResponse?, sessionBundle: Bundle?, expectActivityLaunch: Boolean,
                appInfo: Bundle?, callingUserId: Int
            ) = call(TRANSACTION_finishSessionAsUser, {
                writeInt(userId); writeStrongInterface(response); writeTypedObject(sessionBundle, 0)
                writeBool(expectActivityLaunch); writeTypedObject(appInfo, 0); writeInt(callingUserId)
            }, {})

            override fun getAccountVisibility(userId: Int, account: Account?, packageName: String?): Int =
                call(TRANSACTION_getAccountVisibility,
                    { writeInt(userId); writeTypedObject(account, 0); writeString(packageName) }, { readInt() })

            override fun getAccounts(userId: Int, accountType: String?): Array<Account>? =
                call(TRANSACTION_getAccounts, { writeInt(userId); writeString(accountType) },
                    { createTypedArray(Account.CREATOR) })

            @Suppress("DEPRECATION", "UNCHECKED_CAST")
            override fun getAccountsAndVisibilityForPackage(
                userId: Int, packageName: String?, accountType: String?
            ): Map<Account, Int>? = call(TRANSACTION_getAccountsAndVisibilityForPackage,
                { writeInt(userId); writeString(packageName); writeString(accountType) },
                { readHashMap(this@Proxy.javaClass.classLoader) as Map<Account, Int>? })

            override fun getAccountsByFeatures(
                userId: Int, response: IAccountManagerResponse?, accountType: String?, features: Array<String>?
            ) = call(TRANSACTION_getAccountsByFeatures, {
                writeInt(userId); writeStrongInterface(response); writeString(accountType); writeStringArray(features)
            }, {})

            override fun getAuthToken(
                userId: Int, response: IAccountManagerResponse?, account: Account?, authTokenType: String?,
                notifyOnAuthFailure: Boolean, expectActivityLaunch: Boolean, loginOptions: Bundle?
            ) = call(TRANSACTION_getAuthToken, {
                writeInt(userId); writeStrongInterface(response); writeTypedObject(account, 0)
                writeString(authTokenType); writeBool(notifyOnAuthFailure); writeBool(expectActivityLaunch)
                writeTypedObject(loginOptions, 0)
            }, {})

            override fun getAuthTokenLabel(
                userId: Int, response: IAccountManagerResponse?, accountType: String?, authTokenType: String?
            ) = call(TRANSACTION_getAuthTokenLabel, {
                writeInt(userId); writeStrongInterface(response); writeString(accountType); writeString(authTokenType)
            }, {})

            override fun getAuthenticatorTypes(userId: Int): Array<AuthenticatorDescription>? =
                call(TRANSACTION_getAuthenticatorTypes, { writeInt(userId) },
                    { createTypedArray(AuthenticatorDescription.CREATOR) })

            @Suppress("DEPRECATION", "UNCHECKED_CAST")
            override fun getPackagesAndVisibilityForAccount(userId: Int, account: Account?): Map<String, Int>? =
                call(TRANSACTION_getPackagesAndVisibilityForAccount,
                    { writeInt(userId); writeTypedObject(account, 0) },
                    { readHashMap(this@Proxy.javaClass.classLoader) as Map<String, Int>? })

            override fun getPassword(userId: Int, account: Account?): String? =
                call(TRANSACTION_getPassword, { writeInt(userId); writeTypedObject(account, 0) }, { readString() })

            override fun getPreviousName(userId: Int, account: Account?): String? =
                call(TRANSACTION_getPreviousName, { writeInt(userId); writeTypedObject(account, 0) }, { readString() })

            override fun getUserData(userId: Int, account: Account?, key: String?): String? =
                call(TRANSACTION_getUserData,
                    { writeInt(userId); writeTypedObject(account, 0); writeString(key) }, { readString() })

            override fun hasFeatures(
                userId: Int, response: IAccountManagerResponse?, account: Account?, features: Array<String>?
            ) = call(TRANSACTION_hasFeatures, {
                writeInt(userId); writeStrongInterface(response); writeTypedObject(account, 0); writeStringArray(features)
            }, {})

            override fun invalidateAuthToken(userId: Int, accountType: String?, authToken: String?) =
                call(TRANSACTION_invalidateAuthToken,
                    { writeInt(userId); writeString(accountType); writeString(authToken) }, {})

            override fun isCredentialsUpdateSuggested(
                userId: Int, response: IAccountManagerResponse?, account: Account?, statusToken: String?
            ) = call(TRANSACTION_isCredentialsUpdateSuggested, {
                writeInt(userId); writeStrongInterface(response); writeTypedObject(account, 0); writeString(statusToken)
            }, {})

            override fun peekAuthToken(userId: Int, account: Account?, authTokenType: String?): String? =
                call(TRANSACTION_peekAuthToken,
                    { writeInt(userId); writeTypedObject(account, 0); writeString(authTokenType) }, { readString() })

            override fun registerAccountListener(userId: Int, accountTypes: Array<String>?, opPackageName: String?) =
                call(TRANSACTION_registerAccountListener,
                    { writeInt(userId); writeStringArray(accountTypes); writeString(opPackageName) }, {})

            override fun removeAccount(
                userId: Int, response: IAccountManagerResponse?, account: Account?, expectActivityLaunch: Boolean
            ) = call(TRANSACTION_removeAccount, {
                writeInt(userId); writeStrongInterface(response); writeTypedObject(account, 0)
                writeBool(expectActivityLaunch)
            }, {})

            override fun removeAccountExplicitly(userId: Int, account: Account?): Boolean =
                call(TRANSACTION_removeAccountExplicitly,
                    { writeInt(userId); writeTypedObject(account, 0) }, { readInt() != 0 })

            override fun renameAccount(
                userId: Int, response: IAccountManagerResponse?, account: Account?, newName: String?
            ) = call(TRANSACTION_renameAccount, {
                writeInt(userId); writeStrongInterface(response); writeTypedObject(account, 0); writeString(newName)
            }, {})

            override fun setAccountVisibility(
                userId: Int, account: Account?, packageName: String?, newVisibility: Int
            ): Boolean = call(TRANSACTION_setAccountVisibility, {
                writeInt(userId); writeTypedObject(account, 0); writeString(packageName); writeInt(newVisibility)
            }, { readInt() != 0 })

            override fun setAuthToken(userId: Int, account: Account?, authTokenType: String?, authToken: String?) =
                call(TRANSACTION_setAuthToken, {
                    writeInt(userId); writeTypedObject(account, 0); writeString(authTokenType); writeString(authToken)
                }, {})

            override fun setPassword(userId: Int, account: Account?, password: String?) =
                call(TRANSACTION_setPassword,
                    { writeInt(userId); writeTypedObject(account, 0); writeString(password) }, {})

            override fun setUserData(userId: Int, account: Account?, key: String?, value: String?) =
                call(TRANSACTION_setUserData, {
                    writeInt(userId); writeTypedObject(account, 0); writeString(key); writeString(value)
                }, {})

            override fun startAddAccountSession(
                userId: Int, response: IAccountManagerResponse?, accountType: String?, authTokenType: String?,
                requiredFeatures: Array<String>?, expectActivityLaunch: Boolean, options: Bundle?
            ) = call(TRANSACTION_startAddAccountSession, {
                writeInt(userId); writeStrongInterface(response); writeString(accountType)
                writeString(authTokenType); writeStringArray(requiredFeatures)
                writeBool(expectActivityLaunch); writeTypedObject(options, 0)
            }, {})

            override fun startUpdateCredentialsSession(
                userId: Int, response: IAccountManagerResponse?, account: Account?, authTokenType: String?,
                expectActivityLaunch: Boolean, options: Bundle?
            ) = call(TRANSACTION_startUpdateCredentialsSession, {
                writeInt(userId); writeStrongInterface(response); writeTypedObject(account, 0)
                writeString(authTokenType); writeBool(expectActivityLaunch); writeTypedObject(options, 0)
            }, {})

            override fun unregisterAccountListener(userId: Int, accountTypes: Array<String>?, opPackageName: String?) =
                call(TRANSACTION_unregisterAccountListener,
                    { writeInt(userId); writeStringArray(accountTypes); writeString(opPackageName) }, {})

            override fun updateCredentials(
                userId: Int, response: IAccountManagerResponse?, account: Account?, authTokenType: String?,
                expectActivityLaunch: Boolean, options: Bundle?
            ) = call(TRANSACTION_updateCredentials, {
                writeInt(userId); writeStrongInterface(response); writeTypedObject(account, 0)
                writeString(authTokenType); writeBool(expectActivityLaunch); writeTypedObject(options, 0)
            }, {})
        }

        companion object {
            const val TRANSACTION_getAuthenticatorTypes = 1
            const val TRANSACTION_getAccountsByFeatures = 2
            const val TRANSACTION_getPreviousName = 3
            const val TRANSACTION_getAccounts = 4
            const val TRANSACTION_getAuthToken = 5
            const val TRANSACTION_setPassword = 6
            const val TRANSACTION_setAuthToken = 7
            const val TRANSACTION_setUserData = 8
            const val TRANSACTION_hasFeatures = 9
            const val TRANSACTION_updateCredentials = 10
            const val TRANSACTION_editProperties = 11
            const val TRANSACTION_getAuthTokenLabel = 12
            const val TRANSACTION_getUserData = 13
            const val TRANSACTION_getPassword = 14
            const val TRANSACTION_confirmCredentials = 15
            const val TRANSACTION_addAccount = 16
            const val TRANSACTION_addAccountExplicitly = 17
            const val TRANSACTION_removeAccountExplicitly = 18
            const val TRANSACTION_renameAccount = 19
            const val TRANSACTION_removeAccount = 20
            const val TRANSACTION_clearPassword = 21
            const val TRANSACTION_accountAuthenticated = 22
            const val TRANSACTION_invalidateAuthToken = 23
            const val TRANSACTION_peekAuthToken = 24
            const val TRANSACTION_registerAccountListener = 25
            const val TRANSACTION_unregisterAccountListener = 26
            const val TRANSACTION_startAddAccountSession = 27
            const val TRANSACTION_startUpdateCredentialsSession = 28
            const val TRANSACTION_finishSessionAsUser = 29
            const val TRANSACTION_isCredentialsUpdateSuggested = 30
            const val TRANSACTION_getPackagesAndVisibilityForAccount = 31
            const val TRANSACTION_addAccountExplicitlyWithVisibility = 32
            const val TRANSACTION_setAccountVisibility = 33
            const val TRANSACTION_getAccountVisibility = 34
            const val TRANSACTION_getAccountsAndVisibilityForPackage = 35

            @JvmStatic
            fun asInterface(binder: IBinder?): IAccountManager? {
                if (binder == null) return null
                val local = binder.queryLocalInterface(DESCRIPTOR)
                if (local is IAccountManager) return local
                return Proxy(binder)
            }
        }
    }

    // --------------------------------------------------------------------------------- Default

    /** No-op implementation (jadx a23$Stub). */
    open class Default : IAccountManager {
        override fun accountAuthenticated(userId: Int, account: Account?) = false
        override fun addAccount(
            userId: Int, response: IAccountManagerResponse?, accountType: String?, authTokenType: String?,
            requiredFeatures: Array<String>?, expectActivityLaunch: Boolean, options: Bundle?
        ) {}
        override fun addAccountExplicitly(userId: Int, account: Account?, password: String?, extras: Bundle?) = false
        override fun addAccountExplicitlyWithVisibility(
            userId: Int, account: Account?, password: String?, extras: Bundle?, visibility: Map<String, Int>?
        ) = false
        override fun asBinder(): IBinder? = null
        override fun clearPassword(userId: Int, account: Account?) {}
        override fun confirmCredentials(
            userId: Int, response: IAccountManagerResponse?, account: Account?, options: Bundle?,
            expectActivityLaunch: Boolean
        ) {}
        override fun editProperties(
            userId: Int, response: IAccountManagerResponse?, accountType: String?, expectActivityLaunch: Boolean
        ) {}
        override fun finishSessionAsUser(
            userId: Int, response: IAccountManagerResponse?, sessionBundle: Bundle?, expectActivityLaunch: Boolean,
            appInfo: Bundle?, callingUserId: Int
        ) {}
        override fun getAccountVisibility(userId: Int, account: Account?, packageName: String?) = 0
        override fun getAccounts(userId: Int, accountType: String?): Array<Account>? = null
        override fun getAccountsAndVisibilityForPackage(
            userId: Int, packageName: String?, accountType: String?
        ): Map<Account, Int>? = null
        override fun getAccountsByFeatures(
            userId: Int, response: IAccountManagerResponse?, accountType: String?, features: Array<String>?
        ) {}
        override fun getAuthToken(
            userId: Int, response: IAccountManagerResponse?, account: Account?, authTokenType: String?,
            notifyOnAuthFailure: Boolean, expectActivityLaunch: Boolean, loginOptions: Bundle?
        ) {}
        override fun getAuthTokenLabel(
            userId: Int, response: IAccountManagerResponse?, accountType: String?, authTokenType: String?
        ) {}
        override fun getAuthenticatorTypes(userId: Int): Array<AuthenticatorDescription>? = null
        override fun getPackagesAndVisibilityForAccount(userId: Int, account: Account?): Map<String, Int>? = null
        override fun getPassword(userId: Int, account: Account?): String? = null
        override fun getPreviousName(userId: Int, account: Account?): String? = null
        override fun getUserData(userId: Int, account: Account?, key: String?): String? = null
        override fun hasFeatures(
            userId: Int, response: IAccountManagerResponse?, account: Account?, features: Array<String>?
        ) {}
        override fun invalidateAuthToken(userId: Int, accountType: String?, authToken: String?) {}
        override fun isCredentialsUpdateSuggested(
            userId: Int, response: IAccountManagerResponse?, account: Account?, statusToken: String?
        ) {}
        override fun peekAuthToken(userId: Int, account: Account?, authTokenType: String?): String? = null
        override fun registerAccountListener(userId: Int, accountTypes: Array<String>?, opPackageName: String?) {}
        override fun removeAccount(
            userId: Int, response: IAccountManagerResponse?, account: Account?, expectActivityLaunch: Boolean
        ) {}
        override fun removeAccountExplicitly(userId: Int, account: Account?) = false
        override fun renameAccount(userId: Int, response: IAccountManagerResponse?, account: Account?, newName: String?) {}
        override fun setAccountVisibility(userId: Int, account: Account?, packageName: String?, newVisibility: Int) = false
        override fun setAuthToken(userId: Int, account: Account?, authTokenType: String?, authToken: String?) {}
        override fun setPassword(userId: Int, account: Account?, password: String?) {}
        override fun setUserData(userId: Int, account: Account?, key: String?, value: String?) {}
        override fun startAddAccountSession(
            userId: Int, response: IAccountManagerResponse?, accountType: String?, authTokenType: String?,
            requiredFeatures: Array<String>?, expectActivityLaunch: Boolean, options: Bundle?
        ) {}
        override fun startUpdateCredentialsSession(
            userId: Int, response: IAccountManagerResponse?, account: Account?, authTokenType: String?,
            expectActivityLaunch: Boolean, options: Bundle?
        ) {}
        override fun unregisterAccountListener(userId: Int, accountTypes: Array<String>?, opPackageName: String?) {}
        override fun updateCredentials(
            userId: Int, response: IAccountManagerResponse?, account: Account?, authTokenType: String?,
            expectActivityLaunch: Boolean, options: Bundle?
        ) {}
    }
}
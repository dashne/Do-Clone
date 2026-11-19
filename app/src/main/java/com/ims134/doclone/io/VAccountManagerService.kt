package com.ims134.doclone.io


import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AuthenticatorDescription
import android.accounts.IAccountAuthenticator
import android.accounts.IAccountAuthenticatorResponse
import android.accounts.IAccountManagerResponse
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.content.res.Resources
import android.os.Binder
import android.os.IBinder
import android.os.Bundle
import android.os.Parcel
import android.os.RemoteException
import android.os.SystemClock
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.util.Xml
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.VBinder
import com.ims134.doclone.clone.VEnvironment
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.server.VAccount
import com.ims134.doclone.clone.server.VAccountVisibility
import com.ims134.doclone.clone.server.VActivityManagerService
import com.ims134.doclone.clone.server.VPackageManagerService
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileOutputStream
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs

/**
 * Virtual AccountManagerService (jadx: io.eu8).
 *
 * Assumed field names of the unseen model classes (rename here if yours differ):
 *  VAccount:           userId, name, previousName, type, password, lastAuthenticatedTime, authTokens, userData
 *  VAccountVisibility: userId, name, type, visibility
 */
class VAccountManagerService private constructor() : IAccountManager.Stub() {

    private val accountsByUser = SparseArray<MutableList<VAccount>>()                 // Stub
    private val authTokenCache = LinkedList<AuthTokenCacheEntry>()                    // b
    private val sessions = LinkedHashMap<String, Session>()                           // c
    private val authenticators = HashMap<String, AuthenticatorInfo>()                 // d.Stub
    private val context: Context = appContext                          // e
    private var lastCheckinTime = 0L                                                  // f
    private val visibilityByUser = SparseArray<MutableList<VAccountVisibility>>()     // g

    // ------------------------------------------------------------------ helpers

    private class AuthenticatorInfo(val description: AuthenticatorDescription, val serviceInfo: ServiceInfo) // k

    /** Equality is the key (user, account, type, package); token/expiry are the value. */      // i
    private data class AuthTokenCacheEntry(
        var userId: Int,
        var account: Account,
        val authTokenType: String,
        val callerPackage: String?
    ) {
        var token: String? = null
        var expiry: Long = 0
    }

    private fun <T> SparseArray<MutableList<T>>.listFor(userId: Int): MutableList<T> =
        get(userId) ?: ArrayList<T>().also { put(userId, it) }

    private fun stringBundle(key: String, value: String?) = Bundle().apply { putString(key, value) }

    private fun respondUnknownType(response: IAccountManagerResponse) {
        try {
            response.onError(AccountManager.ERROR_CODE_BAD_ARGUMENTS, "account.type does not exist")
        } catch (_: RemoteException) {
        }
    }

    /** jadx cf6.Stub(context, applicationInfo): Resources of the virtual app. */
    private fun appResources(context: Context, appInfo: android.content.pm.ApplicationInfo): Resources =
        RegisteredServicesParser.getAppResources(context, appInfo)!!

    // ------------------------------------------------------------------ lookup

    private fun getAccountList(userId: Int, type: String?): ArrayList<Account> { // T
        synchronized(accountsByUser) {
            val result = ArrayList<Account>()
            accountsByUser.get(userId)?.forEach {
                if (type == null || it.type == type) result.add(Account(it.name, it.type))
            }
            return result
        }
    }

    private fun findVisibility(userId: Int, account: Account): VAccountVisibility? { // U
        val list = visibilityByUser.get(userId) ?: return null
        return list.firstOrNull {
            TextUtils.equals(it.name, account.name) && TextUtils.equals(it.type, account.type)
        }
    }

    private fun getAuthenticatorInfo(type: String?): AuthenticatorInfo? = // V
        synchronized(authenticators) { if (type == null) null else authenticators[type] }

    private fun findAccount(userId: Int, name: String?, type: String?): VAccount? { // v
        val list = accountsByUser.get(userId) ?: return null
        return list.firstOrNull { TextUtils.equals(it.name, name) && TextUtils.equals(it.type, type) }
    }

    // ------------------------------------------------------------------ persistence

    private fun accountFile() = File(VEnvironment.getOrCreateAppSubSystemFile(), "account-list.ini")
    private fun visibilityFile() = File(VEnvironment.getOrCreateAppSubSystemFile(), "account-visibility-list.ini")

    private fun saveVisibilities() { // Z
        val parcel = Parcel.obtain()
        try {
            parcel.writeInt(1)
            val all = ArrayList<VAccountVisibility>()
            for (i in 0 until visibilityByUser.size()) visibilityByUser.valueAt(i)?.let { all.addAll(it) }
            parcel.writeInt(all.size)
            all.forEach { it.writeToParcel(parcel, 0) }
            parcel.writeLong(lastCheckinTime)
            FileOutputStream(visibilityFile()).use { it.write(parcel.marshall()) }
        } catch (_: Exception) {
        } finally {
            parcel.recycle()
        }
    }

    private fun saveAccounts() { // a0
        val parcel = Parcel.obtain()
        try {
            parcel.writeInt(1)
            val all = ArrayList<VAccount>()
            for (i in 0 until accountsByUser.size()) accountsByUser.valueAt(i)?.let { all.addAll(it) }
            parcel.writeInt(all.size)
            all.forEach { it.writeToParcel(parcel, 0) }
            parcel.writeLong(lastCheckinTime)
            FileOutputStream(accountFile()).use { it.write(parcel.marshall()) }
        } catch (_: Exception) {
        } finally {
            parcel.recycle()
        }
    }

    private fun loadAccounts() {
        val file = accountFile()
        if (!file.exists()) return
        accountsByUser.clear()
        val parcel = Parcel.obtain()
        try {
            val bytes = file.readBytes()
            parcel.unmarshall(bytes, 0, bytes.size)
            parcel.setDataPosition(0)
            parcel.readInt() // version
            var remaining = parcel.readInt()
            var dropped = false
            while (remaining-- > 0) {
                val account = VAccount(parcel)
                if (authenticators[account.type] != null) {
                    accountsByUser.listFor(account.userId).add(account)
                } else {
                    dropped = true
                }
            }
            lastCheckinTime = parcel.readLong()
            if (dropped) saveAccounts()
        } catch (_: Exception) {
        } finally {
            parcel.recycle()
        }
    }

    private fun loadVisibilities() {
        val file = visibilityFile()
        if (!file.exists()) return
        visibilityByUser.clear()
        val parcel = Parcel.obtain()
        try {
            val bytes = file.readBytes()
            parcel.unmarshall(bytes, 0, bytes.size)
            parcel.setDataPosition(0)
            parcel.readInt() // version
            var remaining = parcel.readInt()
            var dropped = false
            while (remaining-- > 0) {
                val vis = VAccountVisibility(parcel)
                if (findAccount(vis.userId, vis.name, vis.type) != null) {
                    visibilityByUser.listFor(vis.userId).add(vis)
                } else {
                    dropped = true
                }
            }
            if (dropped) saveVisibilities()
        } catch (_: Exception) {
        } finally {
            parcel.recycle()
        }
    }

    // ------------------------------------------------------------------ internal mutations

    private fun addAccountInternal(userId: Int, account: Account, password: String?, extras: Bundle?) { // W
        synchronized(accountsByUser) {
            val vAccount = VAccount(userId, account)
            vAccount.password = password
            if (extras != null) {
                for (key in extras.keySet()) {
                    @Suppress("DEPRECATION")
                    val value = extras.get(key)
                    if (value is String) vAccount.userData[key] = value
                }
            }
            accountsByUser.listFor(userId).add(vAccount)
            saveAccounts()
            notifyAccountsChanged(vAccount.userId)
        }
    }

    private fun removeAccountInternal(userId: Int, account: Account): Boolean { // Y
        val list = accountsByUser.get(userId) ?: return false
        val it = list.iterator()
        while (it.hasNext()) {
            val vAccount = it.next()
            if (userId == vAccount.userId &&
                TextUtils.equals(vAccount.name, account.name) &&
                TextUtils.equals(account.type, vAccount.type)
            ) {
                it.remove()
                saveAccounts()
                notifyAccountsChanged(userId)
                val visList = visibilityByUser.get(userId) ?: return true
                val visIt = visList.iterator()
                while (visIt.hasNext()) {
                    val vis = visIt.next()
                    if (userId == vis.userId &&
                        TextUtils.equals(vis.name, account.name) &&
                        TextUtils.equals(account.type, vis.type)
                    ) {
                        visIt.remove()
                        saveVisibilities()
                        notifyAccountsChanged(userId)
                        return true
                    }
                }
                return true
            }
        }
        return false
    }

    private fun notifyAccountsChanged(userId: Int) { // b0
        VActivityManagerService.get()
            .sendBroadcastAsUser(Intent("android.accounts.LOGIN_ACCOUNTS_CHANGED"),
                VUserHandle(userId)
            )
        val now = System.currentTimeMillis()
        if (abs(now - lastCheckinTime) > CHECKIN_INTERVAL_MS) {
            lastCheckinTime = now
            saveAccounts()
            VActivityManagerService.get()
                .sendBroadcastAsUser(Intent("android.server.checkin.CHECKIN_NOW"), VUserHandle(userId))
        }
    }

    private fun setPasswordInternal(userId: Int, account: Account, password: String?) { // c0
        synchronized(accountsByUser) {
            val vAccount = findAccount(userId, account.name, account.type) ?: return
            vAccount.password = password
            vAccount.authTokens.clear()
            saveAccounts()
            synchronized(authTokenCache) {
                authTokenCache.removeAll { it.userId == userId && it.account == account }
            }
            notifyAccountsChanged(userId)
        }
    }

    // ------------------------------------------------------------------ authenticators

    fun refreshAuthenticatorCache(packageName: String?) {
        authenticators.clear()
        val intent = Intent("android.accounts.AccountAuthenticator")
        if (packageName != null) intent.setPackage(packageName)
        val services = VPackageManagerService.get().queryIntentServices(intent, null, PackageManager.GET_META_DATA, 0)
        if (services != null) {
            for (resolveInfo in services) {
                val serviceInfo = resolveInfo?.serviceInfo
                val xmlResId = serviceInfo?.metaData?.getInt("android.accounts.AccountAuthenticator") ?: 0
                val xml = if (xmlResId == 0) null else try {
                    appResources(context, serviceInfo?.applicationInfo!!).getXml(xmlResId)
                } catch (_: Exception) {
                    null
                }
                if (xml != null) {
                    try {
                        val attrs = Xml.asAttributeSet(xml)
                        var event: Int
                        do {
                            event = xml.next()
                        } while (event != XmlPullParser.END_DOCUMENT && event != XmlPullParser.START_TAG)
                        if ("account-authenticator" == xml.name) {
                            val description = parseAuthenticatorDescription(
                                appResources(context, serviceInfo?.applicationInfo!!), serviceInfo.packageName, attrs
                            )
                            if (description != null) {
                                authenticators[description.type] = AuthenticatorInfo(description, serviceInfo)
                            }
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------ IAccountManager

    override fun accountAuthenticated(userId: Int, account: Account?): Boolean {
        requireNotNull(account) { "account is null" }
        synchronized(accountsByUser) {
            val vAccount = findAccount(userId, account.name, account.type) ?: return false
            vAccount.lastAuthenticatedTime = System.currentTimeMillis()
            saveAccounts()
            return true
        }
    }

    override fun addAccount(
        userId: Int, response: IAccountManagerResponse?, accountType: String?, authTokenType: String?,
        requiredFeatures: Array<String>?, expectActivityLaunch: Boolean, options: Bundle?
    ) {
        requireNotNull(response) { "response is null" }
        requireNotNull(accountType) { "accountType is null" }
        val info = getAuthenticatorInfo(accountType)
        if (info != null) {
            AddAccountSession(
                response, userId, info, expectActivityLaunch, authTokenType, requiredFeatures, options, accountType
            ).bind()
        } else {
            respondUnknownType(response)
        }
    }

    override fun addAccountExplicitly(userId: Int, account: Account?, password: String?, extras: Bundle?): Boolean {
        requireNotNull(account) { "account is null" }
        addAccountInternal(userId, account, password, extras)
        return true
    }

    override fun addAccountExplicitlyWithVisibility(
        userId: Int, account: Account?, password: String?, extras: Bundle?, visibility: Map<String, Int>?
    ): Boolean {
        requireNotNull(account) { "account is null" }
        addAccountInternal(userId, account, password, extras)
        synchronized(visibilityByUser) {
            val vis = VAccountVisibility()
            vis.userId = userId
            vis.name = account.name
            vis.type = account.type
            vis.visibility = HashMap<String, Int>().also { if (visibility != null) it.putAll(visibility) }
            visibilityByUser.listFor(userId).add(vis)
            saveVisibilities()
            notifyAccountsChanged(userId)
        }
        return true
    }

    override fun clearPassword(userId: Int, account: Account?) {
        requireNotNull(account) { "account is null" }
        setPasswordInternal(userId, account, null)
    }

    override fun confirmCredentials(
        userId: Int, response: IAccountManagerResponse?, account: Account?, options: Bundle?,
        expectActivityLaunch: Boolean
    ) {
        requireNotNull(response) { "response is null" }
        requireNotNull(account) { "account is null" }
        val info = getAuthenticatorInfo(account.type)
        if (info != null) {
            ConfirmCredentialsSession(response, userId, info, expectActivityLaunch, account.name, account, options).bind()
        } else {
            respondUnknownType(response)
        }
    }

    override fun editProperties(
        userId: Int, response: IAccountManagerResponse?, accountType: String?, expectActivityLaunch: Boolean
    ) {
        requireNotNull(response) { "response is null" }
        requireNotNull(accountType) { "accountType is null" }
        val info = getAuthenticatorInfo(accountType)
        if (info != null) {
            EditPropertiesSession(response, userId, info, expectActivityLaunch, accountType).bind()
        } else {
            respondUnknownType(response)
        }
    }

    override fun getAccountVisibility(userId: Int, account: Account?, packageName: String?): Int {
        val vis = findVisibility(userId, account!!) ?: return 0
        return vis.visibility[packageName] ?: 0
    }

    override fun getAccounts(userId: Int, accountType: String?): Array<Account> =
        getAccountList(userId, accountType).toTypedArray()

    override fun getAccountsAndVisibilityForPackage(
        userId: Int, packageName: String?, accountType: String?
    ): Map<Account, Int> {
        val result = HashMap<Account, Int>()
        for (account in getAccountList(userId, accountType)) {
            val visibility = findVisibility(userId, account)?.visibility ?: continue
            if (visibility.containsKey(packageName)) result[account] = visibility[packageName]!!
        }
        return result
    }

    override fun getAccountsByFeatures(
        userId: Int, response: IAccountManagerResponse?, accountType: String?, features: Array<String>?
    ) {
        requireNotNull(response) { "response is null" }
        requireNotNull(accountType) { "accountType is null" }
        val info = getAuthenticatorInfo(accountType)
        if (info == null) {
            val bundle = Bundle().apply { putParcelableArray("accounts", arrayOf<Account>()) }
            try {
                response.onResult(bundle)
            } catch (_: RemoteException) {
            }
            return
        }
        if (!features.isNullOrEmpty()) {
            GetAccountsByTypeAndFeaturesSession(response, userId, info, features).bind()
            return
        }
        val bundle = Bundle().apply { putParcelableArray("accounts", getAccounts(userId, accountType)) }
        try {
            response.onResult(bundle)
        } catch (_: RemoteException) {
        }
    }

    override fun getAuthToken(
        userId: Int, response: IAccountManagerResponse?, account: Account?, authTokenType: String?,
        notifyOnAuthFailure: Boolean, expectActivityLaunch: Boolean, loginOptions: Bundle?
    ) {
        requireNotNull(response) { "response is null" }
        try {
            if (account == null) {
                response.onError(AccountManager.ERROR_CODE_BAD_ARGUMENTS, "account is null")
                return
            }
            if (authTokenType == null) {
                response.onError(AccountManager.ERROR_CODE_BAD_ARGUMENTS, "authTokenType is null")
                return
            }
            val info = getAuthenticatorInfo(account.type)
            if (info == null) {
                response.onError(AccountManager.ERROR_CODE_BAD_ARGUMENTS, "account.type does not exist")
                return
            }
            val options = loginOptions!!
            val callerPackage = options.getString("androidPackageName")
            val customTokens = info.description.customTokens
            options.putInt("callerUid", VBinder.getUidByPid())
            options.putInt("callerPid", Binder.getCallingPid())
            if (notifyOnAuthFailure) options.putBoolean("notifyOnAuthFailure", true)

            if (!customTokens) {
                val vAccount = synchronized(accountsByUser) { findAccount(userId, account.name, account.type) }
                val token = vAccount?.authTokens?.get(authTokenType)
                if (token != null) {
                    val result = stringBundle("authtoken", token)
                    result.putString("authAccount", account.name)
                    result.putString("accountType", account.type)
                    response.onResult(result)
                    return
                }
            }
            if (customTokens) {
                val key = AuthTokenCacheEntry(userId, account, authTokenType, callerPackage)
                val now = System.currentTimeMillis()
                var cachedToken: String? = null
                synchronized(authTokenCache) {
                    val it = authTokenCache.iterator()
                    while (it.hasNext()) {
                        val entry = it.next()
                        if (entry.expiry > 0 && entry.expiry < now) {
                            it.remove()
                        } else if (key == entry) {
                            // NOTE: original reads the token from the lookup key (always null),
                            // so this cache never hits. Use entry.token to enable it.
                            cachedToken = key.token
                        }
                    }
                }
                if (cachedToken != null) {
                    val result = stringBundle("authtoken", cachedToken)
                    result.putString("authAccount", account.name)
                    result.putString("accountType", account.type)
                    response.onResult(result)
                    return
                }
            }
            GetAuthTokenSession(
                response, userId, info, expectActivityLaunch, account.name, account, authTokenType,
                options, notifyOnAuthFailure, customTokens, callerPackage
            ).bind()
        } catch (_: RemoteException) {
        }
    }

    override fun getAuthTokenLabel(
        userId: Int, response: IAccountManagerResponse?, accountType: String?, authTokenType: String?
    ) {
        requireNotNull(accountType) { "accountType is null" }
        requireNotNull(authTokenType) { "authTokenType is null" }
        val info = getAuthenticatorInfo(accountType)
        if (info != null) {
            GetAuthTokenLabelSession(response, userId, info, authTokenType).bind()
        } else {
            respondUnknownType(response!!)
        }
    }

    override fun getAuthenticatorTypes(userId: Int): Array<AuthenticatorDescription> =
        synchronized(authenticators) { authenticators.values.map { it.description }.toTypedArray() }

    override fun getPackagesAndVisibilityForAccount(userId: Int, account: Account?): Map<String, Int>? =
        findVisibility(userId, account!!)?.visibility

    override fun getPassword(userId: Int, account: Account?): String? {
        requireNotNull(account) { "account is null" }
        synchronized(accountsByUser) {
            return findAccount(userId, account.name, account.type)?.password
        }
    }

    override fun getPreviousName(userId: Int, account: Account?): String? {
        requireNotNull(account) { "account is null" }
        synchronized(accountsByUser) {
            return findAccount(userId, account.name, account.type)?.previousName
        }
    }

    override fun getUserData(userId: Int, account: Account?, key: String?): String? {
        requireNotNull(account) { "account is null" }
        requireNotNull(key) { "key is null" }
        synchronized(accountsByUser) {
            return findAccount(userId, account.name, account.type)?.userData?.get(key)
        }
    }

    override fun hasFeatures(
        userId: Int, response: IAccountManagerResponse?, account: Account?, features: Array<String>?
    ) {
        requireNotNull(response) { "response is null" }
        requireNotNull(account) { "account is null" }
        requireNotNull(features) { "features is null" }
        val info = getAuthenticatorInfo(account.type)
        if (info != null) {
            HasFeaturesSession(response, userId, info, account.name, account, features).bind()
        } else {
            respondUnknownType(response)
        }
    }

    override fun invalidateAuthToken(userId: Int, accountType: String?, authToken: String?) {
        requireNotNull(accountType) { "accountType is null" }
        requireNotNull(authToken) { "authToken is null" }
        synchronized(accountsByUser) {
            val list = accountsByUser.get(userId)
            if (list != null) {
                var changed = false
                for (vAccount in list) {
                    if (vAccount.type == accountType) {
                        vAccount.authTokens.values.remove(authToken)
                        changed = true
                    }
                }
                if (changed) saveAccounts()
            }
            synchronized(authTokenCache) {
                // authTokenType is compared with accountType, exactly as in the original
                authTokenCache.removeAll {
                    it.userId == userId && it.authTokenType == accountType && it.token == authToken
                }
            }
        }
    }

    override fun peekAuthToken(userId: Int, account: Account?, authTokenType: String?): String? {
        requireNotNull(account) { "account is null" }
        requireNotNull(authTokenType) { "authTokenType is null" }
        synchronized(accountsByUser) {
            return findAccount(userId, account.name, account.type)?.authTokens?.get(authTokenType)
        }
    }

    override fun removeAccount(
        userId: Int, response: IAccountManagerResponse?, account: Account?, expectActivityLaunch: Boolean
    ) {
        requireNotNull(response) { "response is null" }
        requireNotNull(account) { "account is null" }
        val info = getAuthenticatorInfo(account.type)
        if (info != null) {
            RemoveAccountSession(response, userId, info, expectActivityLaunch, account.name, account).bind()
        } else {
            respondUnknownType(response)
        }
    }

    override fun removeAccountExplicitly(userId: Int, account: Account?): Boolean =
        account != null && removeAccountInternal(userId, account)

    override fun renameAccount(
        userId: Int, response: IAccountManagerResponse?, account: Account?, newName: String?
    ) {
        requireNotNull(account) { "account is null" }
        var resultAccount: Account = account
        synchronized(accountsByUser) {
            val vAccount = findAccount(userId, account.name, account.type)
            if (vAccount != null) {
                vAccount.previousName = vAccount.name
                vAccount.name = newName!!
                saveAccounts()
                val renamed = Account(vAccount.name, vAccount.type)
                synchronized(authTokenCache) {
                    for (entry in authTokenCache) {
                        if (entry.userId == userId && entry.account == account) entry.account = renamed
                    }
                }
                synchronized(visibilityByUser) {
                    val vis = findVisibility(userId, account)
                    if (vis != null) {
                        vis.name = newName
                        saveVisibilities()
                        notifyAccountsChanged(userId)
                    }
                }
                notifyAccountsChanged(userId)
                resultAccount = renamed
            }
        }
        val result = Bundle().apply {
            putString("authAccount", resultAccount.name)
            putString("accountType", resultAccount.type)
        }
        try {
            response?.onResult(result)
        } catch (e: RemoteException) {
            Log.w(TAG, e.message.toString())
        }
    }

    override fun setAccountVisibility(userId: Int, account: Account?, packageName: String?, newVisibility: Int): Boolean {
        val vis = findVisibility(userId, account!!) ?: return false
        vis.visibility[packageName!!] = newVisibility
        saveVisibilities()
        notifyAccountsChanged(userId)
        return true
    }

    override fun setAuthToken(userId: Int, account: Account?, authTokenType: String?, authToken: String?) {
        requireNotNull(account) { "account is null" }
        requireNotNull(authTokenType) { "authTokenType is null" }
        synchronized(accountsByUser) {
            val vAccount = findAccount(userId, account.name, account.type)
            if (vAccount != null) {
                vAccount.authTokens[authTokenType] = authToken
                saveAccounts()
            }
        }
    }

    override fun setPassword(userId: Int, account: Account?, password: String?) {
        requireNotNull(account) { "account is null" }
        setPasswordInternal(userId, account, password)
    }

    override fun setUserData(userId: Int, account: Account?, key: String?, value: String?) {
        requireNotNull(key) { "key is null" }
        requireNotNull(account) { "account is null" }
        val vAccount = findAccount(userId, account.name, account.type) ?: return
        synchronized(accountsByUser) {
            vAccount.userData[key] = value
            saveAccounts()
        }
    }

    override fun updateCredentials(
        userId: Int, response: IAccountManagerResponse?, account: Account?, authTokenType: String?,
        expectActivityLaunch: Boolean, options: Bundle?
    ) {
        requireNotNull(response) { "response is null" }
        requireNotNull(account) { "account is null" }
        requireNotNull(authTokenType) { "authTokenType is null" }
        val info = getAuthenticatorInfo(account.type)
        if (info != null) {
            UpdateCredentialsSession(
                response, userId, info, expectActivityLaunch, account.name, account, authTokenType, options
            ).bind()
        } else {
            respondUnknownType(response)
        }
    }

    // Not implemented in the original
    override fun registerAccountListener(userId: Int, accountTypes: Array<String>?, opPackageName: String?) {}
    override fun unregisterAccountListener(userId: Int, accountTypes: Array<String>?, opPackageName: String?) {}
    override fun isCredentialsUpdateSuggested(
        userId: Int, response: IAccountManagerResponse?, account: Account?, statusToken: String?
    ) {}
    override fun startAddAccountSession(
        userId: Int, response: IAccountManagerResponse?, accountType: String?, authTokenType: String?,
        requiredFeatures: Array<String>?, expectActivityLaunch: Boolean, options: Bundle?
    ) {}
    override fun finishSessionAsUser(
        userId: Int, response: IAccountManagerResponse?, sessionBundle: Bundle?, expectActivityLaunch: Boolean,
        appInfo: Bundle?, callingUserId: Int
    ) {}
    override fun startUpdateCredentialsSession(
        userId: Int, response: IAccountManagerResponse?, account: Account?, authTokenType: String?,
        expectActivityLaunch: Boolean, options: Bundle?
    ) {}

    // ------------------------------------------------------------------ sessions

    /** Base session: binds to the authenticator service and relays results (jadx m). */
    private abstract inner class Session(
        protected val userId: Int,
        protected val info: AuthenticatorInfo,
        private var response: IAccountManagerResponse?,
        protected val expectActivityLaunch: Boolean,
        private val stripAuthToken: Boolean,
        private val accountName: String?,
        private val detailsRequired: Boolean = false,
        private val updateLastAuthenticated: Boolean = false
    ) : IAccountAuthenticatorResponse.Stub(), IBinder.DeathRecipient, ServiceConnection {

        protected var numResults = 0
        protected var authenticator: IAccountAuthenticator? = null
        private val createdAt = SystemClock.elapsedRealtime()
        private var numRequestContinued = 0
        private var numErrors = 0

        init {
            synchronized(sessions) { sessions[toString()] = this }
            val r = response
            if (r != null) {
                try {
                    r.asBinder().linkToDeath(this, 0)
                } catch (_: RemoteException) {
                    response = null
                    binderDied()
                }
            }
        }

        fun takeResponse(): IAccountManagerResponse? { // T
            val r = response ?: return null
            close()
            return r
        }

        open fun describe(now: Long): String = buildString { // U
            append("Session: expectLaunch ").append(expectActivityLaunch)
            append(", connected ").append(authenticator != null)
            append(", stats (").append(numResults).append("/").append(numRequestContinued)
            append("/").append(numErrors).append("), lifetime ").append((now - createdAt) / 1000.0)
        }

        override fun binderDied() {
            response = null
            close()
        }

        fun close() {
            synchronized(sessions) {
                if (sessions.remove(toString()) == null) return
                response?.let {
                    it.asBinder().unlinkToDeath(this, 0)
                    response = null
                }
                if (authenticator != null) {
                    authenticator = null
                    context.unbindService(this)
                }
            }
        }

        override fun onError(errorCode: Int, errorMessage: String?) {
            numErrors++
            val r = takeResponse()
            if (r == null) {
                Log.v(TAG, "Session.onError: already closed")
                return
            }
            Log.v(TAG, "${javaClass.simpleName} calling onError() on response $r")
            try {
                r.onError(errorCode, errorMessage)
            } catch (e: RemoteException) {
                Log.v(TAG, "Session.onError: caught RemoteException while responding", e)
            }
        }

        override fun onRequestContinued() {
            numRequestContinued++
        }

        override fun onResult(result: Bundle?) {
            numResults++
            if (result != null) {
                val booleanResult = result.getBoolean("booleanResult", false)
                val hasAccount = result.containsKey("authAccount") && result.containsKey("accountType")
                val touchAuthTime = updateLastAuthenticated && (booleanResult || hasAccount)
                if (touchAuthTime || detailsRequired) {
                    synchronized(accountsByUser) {
                        val vAccount = findAccount(userId, accountName, info.description.type)
                        if (touchAuthTime && vAccount != null) {
                            vAccount.lastAuthenticatedTime = System.currentTimeMillis()
                            saveAccounts()
                        }
                        if (detailsRequired) {
                            result.putLong("lastAuthenticatedTime", vAccount?.lastAuthenticatedTime ?: -1L)
                        }
                    }
                }
            }
            @Suppress("DEPRECATION")
            val intent: Intent? = result?.getParcelable("intent")
            val target = if (expectActivityLaunch && result != null && result.containsKey("intent")) response
            else takeResponse()
            if (target != null) {
                try {
                    if (result == null) {
                        Log.v(TAG, "${javaClass.simpleName} calling onError() on response $target")
                        target.onError(AccountManager.ERROR_CODE_INVALID_RESPONSE, "null bundle returned")
                        return
                    }
                    if (stripAuthToken) result.remove("authtoken")
                    Log.v(TAG, "${javaClass.simpleName} calling onResult() on response $target")
                    if (result.getInt("errorCode", -1) <= 0 || intent != null) {
                        target.onResult(result)
                    } else {
                        target.onError(result.getInt("errorCode"), result.getString("errorMessage"))
                    }
                } catch (e: RemoteException) {
                    Log.v(TAG, "failure while notifying response", e)
                }
            }
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            authenticator = IAccountAuthenticator.Stub.asInterface(service)
            try {
                execute()
            } catch (_: RemoteException) {
                onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION, "remote exception")
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            authenticator = null
            val r = takeResponse()
            if (r != null) {
                try {
                    r.onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION, "disconnected")
                } catch (e: RemoteException) {
                    Log.v(TAG, "Session.onServiceDisconnected: caught RemoteException while responding", e)
                }
            }
        }

        /** Called once the authenticator is connected. */
        abstract fun execute()

        fun bind() { // v
            Log.v(TAG, "initiating bind to authenticator type ${info.description.type}")
            val intent = Intent().apply {
                action = "android.accounts.AccountAuthenticator"
                setClassName(info.serviceInfo.packageName, info.serviceInfo.name)
                putExtra("_VA_|_user_id_", userId)
            }
            if (context.bindService(intent, this, Context.BIND_AUTO_CREATE)) return
            Log.d(TAG, "bind attempt failed for ${describe(SystemClock.elapsedRealtime())}")
            onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION, "bind failure")
        }
    }

    private inner class GetAuthTokenSession( // Stub
        response: IAccountManagerResponse?, userId: Int, info: AuthenticatorInfo, expectActivityLaunch: Boolean,
        accountName: String?,
        private val account: Account,
        private val authTokenType: String,
        private val loginOptions: Bundle,
        private val notifyOnAuthFailure: Boolean,
        private val customTokens: Boolean,
        private val callerPackage: String?
    ) : Session(userId, info, response, expectActivityLaunch, false, accountName) {

        override fun describe(now: Long) =
            super.describe(now) + ", getAuthToken, $account, authTokenType $authTokenType, " +
                    "loginOptions $loginOptions, notifyOnAuthFailure $notifyOnAuthFailure"

        override fun onResult(result: Bundle?) {
            if (result != null) {
                val token = result.getString("authtoken")
                if (token != null) {
                    val name = result.getString("authAccount")
                    val type = result.getString("accountType")
                    if (TextUtils.isEmpty(type) || TextUtils.isEmpty(name)) {
                        onError(AccountManager.ERROR_CODE_INVALID_RESPONSE, "the type and name should not be empty")
                        return
                    }
                    if (!customTokens) {
                        synchronized(accountsByUser) {
                            if (findAccount(userId, name, type) == null) {
                                accountsByUser.listFor(userId).add(VAccount(userId, Account(name!!, type!!)))
                                saveAccounts()
                            }
                        }
                    }
                    val expiry = result.getLong("android.accounts.expiry", 0L)
                    if (customTokens && expiry > System.currentTimeMillis()) {
                        val entry = AuthTokenCacheEntry(userId, account, authTokenType, callerPackage)
                        entry.token = token
                        entry.expiry = expiry
                        synchronized(authTokenCache) {
                            authTokenCache.remove(entry)
                            authTokenCache.add(entry)
                        }
                    }
                }
            }
            super.onResult(result)
        }

        override fun execute() {
            authenticator?.getAuthToken(this, account, authTokenType, loginOptions)
        }
    }

    private inner class HasFeaturesSession( // b
        response: IAccountManagerResponse?, userId: Int, info: AuthenticatorInfo, accountName: String?,
        private val account: Account,
        private val features: Array<String>
    ) : Session(userId, info, response, false, true, accountName) {

        override fun onResult(result: Bundle?) {
            val r = takeResponse() ?: return
            try {
                if (result == null) {
                    r.onError(AccountManager.ERROR_CODE_INVALID_RESPONSE, "null bundle")
                    return
                }
                Log.v(TAG, "${javaClass.simpleName} calling onResult() on response $r")
                r.onResult(Bundle().apply { putBoolean("booleanResult", result.getBoolean("booleanResult", false)) })
            } catch (e: RemoteException) {
                Log.v(TAG, "failure while notifying response", e)
            }
        }

        override fun execute() {
            try {
                authenticator!!.hasFeatures(this, account, features)
            } catch (_: RemoteException) {
                onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION, "remote exception")
            }
        }
    }

    private inner class UpdateCredentialsSession( // c
        response: IAccountManagerResponse?, userId: Int, info: AuthenticatorInfo, expectActivityLaunch: Boolean,
        accountName: String?,
        private val account: Account,
        private val authTokenType: String,
        private val loginOptions: Bundle?
    ) : Session(userId, info, response, expectActivityLaunch, false, accountName) {

        override fun describe(now: Long): String {
            loginOptions?.keySet() // force unparcel before printing
            return super.describe(now) + ", updateCredentials, $account, authTokenType $authTokenType, loginOptions $loginOptions"
        }

        override fun execute() {
            authenticator!!.updateCredentials(this, account, authTokenType, loginOptions)
        }
    }

    private inner class EditPropertiesSession( // d
        response: IAccountManagerResponse?, userId: Int, info: AuthenticatorInfo, expectActivityLaunch: Boolean,
        private val accountType: String
    ) : Session(userId, info, response, expectActivityLaunch, true, null) {

        override fun describe(now: Long) = super.describe(now) + ", editProperties, accountType $accountType"

        override fun execute() {
            authenticator!!.editProperties(this, info.description.type)
        }
    }

    private inner class GetAuthTokenLabelSession( // e
        response: IAccountManagerResponse?, userId: Int, info: AuthenticatorInfo,
        private val authTokenType: String
    ) : Session(userId, info, response, false, false, null) {

        override fun onResult(result: Bundle?) {
            if (result != null) {
                super.onResult(stringBundle("authTokenLabelKey", result.getString("authTokenLabelKey")))
            } else {
                super.onResult(null)
            }
        }

        override fun execute() {
            authenticator!!.getAuthTokenLabel(this, authTokenType)
        }
    }

    private inner class ConfirmCredentialsSession( // f
        response: IAccountManagerResponse?, userId: Int, info: AuthenticatorInfo, expectActivityLaunch: Boolean,
        accountName: String?,
        private val account: Account,
        private val options: Bundle?
    ) : Session(userId, info, response, expectActivityLaunch, true, accountName, true, true) {

        override fun execute() {
            authenticator!!.confirmCredentials(this, account, options)
        }
    }

    private inner class AddAccountSession( // g
        response: IAccountManagerResponse?, userId: Int, info: AuthenticatorInfo, expectActivityLaunch: Boolean,
        private val authTokenType: String?,
        private val requiredFeatures: Array<String>?,
        private val options: Bundle?,
        private val accountType: String
    ) : Session(userId, info, response, expectActivityLaunch, true, null, false, true) {

        override fun describe(now: Long) =
            super.describe(now) + ", addAccount, accountType $accountType, requiredFeatures " +
                    (requiredFeatures?.let { TextUtils.join(",", it) })

        override fun execute() {
            authenticator!!.addAccount(this, info.description.type, authTokenType, requiredFeatures, options)
        }
    }

    private inner class RemoveAccountSession( // h
        response: IAccountManagerResponse?, userId: Int, info: AuthenticatorInfo, expectActivityLaunch: Boolean,
        accountName: String?,
        private val account: Account
    ) : Session(userId, info, response, expectActivityLaunch, true, accountName) {

        override fun describe(now: Long) = super.describe(now) + ", removeAccount, account $account"

        override fun onResult(result: Bundle?) {
            if (result != null && result.containsKey("booleanResult") && !result.containsKey("intent")) {
                val removed = result.getBoolean("booleanResult")
                if (removed) removeAccountInternal(userId, account)
                val r = takeResponse()
                if (r != null) {
                    Log.v(TAG, "${javaClass.simpleName} calling onResult() on response $r")
                    try {
                        r.onResult(Bundle().apply { putBoolean("booleanResult", removed) })
                    } catch (_: RemoteException) {
                    }
                }
            }
            super.onResult(result)
        }

        override fun execute() {
            authenticator!!.getAccountRemovalAllowed(this, account)
        }
    }

    private inner class GetAccountsByTypeAndFeaturesSession( // l
        response: IAccountManagerResponse?, userId: Int, info: AuthenticatorInfo,
        private val features: Array<String>
    ) : Session(userId, info, response, false, true, null) {

        @Volatile private var candidates: Array<Account>? = null
        @Volatile private var matches: ArrayList<Account>? = null
        @Volatile private var index = 0

        override fun describe(now: Long) =
            super.describe(now) + ", getAccountsByTypeAndFeatures, " + TextUtils.join(",", features)

        fun checkAccount() {
            val all = candidates!!
            if (index >= all.size) {
                sendResult()
                return
            }
            val auth = authenticator
            if (auth == null) {
                Log.v(TAG, "checkAccount: aborting session since we are no longer connected to the authenticator, " +
                        describe(SystemClock.elapsedRealtime()))
            } else {
                try {
                    auth.hasFeatures(this, all[index], features)
                } catch (_: RemoteException) {
                    onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION, "remote exception")
                }
            }
        }

        override fun onResult(result: Bundle?) {
            numResults++
            if (result == null) {
                onError(AccountManager.ERROR_CODE_INVALID_RESPONSE, "null bundle")
                return
            }
            if (result.getBoolean("booleanResult", false)) matches!!.add(candidates!![index])
            index++
            checkAccount()
        }

        override fun execute() {
            val all = getAccounts(userId, info.description.type)
            candidates = all
            matches = ArrayList(all.size)
            index = 0
            checkAccount()
        }

        fun sendResult() {
            val r = takeResponse() ?: return
            try {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "${javaClass.simpleName} calling onResult() on response $r")
                }
                val bundle = Bundle().apply { putParcelableArray("accounts", matches!!.toTypedArray()) }
                r.onResult(bundle)
            } catch (e: RemoteException) {
                Log.v(TAG, "failure while notifying response", e)
            }
        }
    }

    // ------------------------------------------------------------------ static

    companion object {
        private const val TAG = "VAccount"
        private const val CHECKIN_INTERVAL_MS = 43_200_000L // 12 h

        private val instance = AtomicReference<VAccountManagerService>()

        @JvmStatic
        fun get(): VAccountManagerService = instance.get()

        @JvmStatic
        fun systemReady() {
            val service = VAccountManagerService()
            service.refreshAuthenticatorCache(null)
            service.loadAccounts()
            service.loadVisibilities()
            instance.set(service)
        }

        private fun parseAuthenticatorDescription( // X
            resources: Resources, packageName: String, attrs: AttributeSet
        ): AuthenticatorDescription? {
            val a = resources.obtainAttributes(attrs, R_Hide.Styleable.AccountAuthenticator?.get())
            try {
                val type = a.getString(R_Hide.Styleable.AccountAuthenticator_accountType!!.get())
                val labelId = a.getResourceId(R_Hide.Styleable.AccountAuthenticator_label!!.get(), 0)
                val iconId = a.getResourceId(R_Hide.Styleable.AccountAuthenticator_icon!!.get(), 0)
                val smallIconId = a.getResourceId(R_Hide.Styleable.AccountAuthenticator_smallIcon!!.get(), 0)
                val prefId = a.getResourceId(R_Hide.Styleable.AccountAuthenticator_accountPreferences!!.get(), 0)
                val customTokens = a.getBoolean(R_Hide.Styleable.AccountAuthenticator_customTokens!!.get(), false)
                if (TextUtils.isEmpty(type)) return null
                return AuthenticatorDescription(type, packageName, labelId, iconId, smallIconId, prefId, customTokens)
            } finally {
                a.recycle()
            }
        }
    }
}
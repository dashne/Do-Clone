package com.ims134.doclone.io

import android.accounts.Account
import android.accounts.AuthenticatorDescription
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.ims134.doclone.clone.ChooseTypeAndAccountActivity
import com.ims134.doclone.clone.ServiceManagerNative
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.clone.VirtualRuntime


class VAccountManager private constructor() {

    /**
     * Was `a`. The cached `IAccountManager` service. Nullable because it's lazily
     * fetched by [getService] and cleared when the binder dies.
     *
     * `@Volatile` because [getService] does double-checked locking (reads this field
     * without holding the lock on the fast path).
     */
    @Volatile
    @JvmField
    var accountManager: IAccountManager? = null

    /**
     * Was inner class `a`. Clears the cached service when the account-service binder
     * dies, so [getService] re-binds on the next call.
     */
    private inner class BinderDeathRecipient(
        private val manager: IAccountManager,
    ) : IBinder.DeathRecipient {
        override fun binderDied() {
            manager.asBinder()!!.unlinkToDeath(this, 0)
            this@VAccountManager.accountManager = null
        }
    }

    /**
     * Was `a(int, String, String, String[], Bundle, ChooseTypeAndAccountActivity)`.
     * Starts an add-account flow against the virtual account service.
     *
     * `accountType` is documented as required, and `requireNotNull` throws the same
     * `IllegalArgumentException("accountType is null")` the original does — so
     * Java callers that relied on the checked message still see it.
     */
    fun addAccount(
        userId: Int,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<String>?,
        options: Bundle?,
        activity: ChooseTypeAndAccountActivity,
    ) {
        Log.d("VAccount", "addAccount $accountType")
        requireNotNull(accountType) { "accountType is null" }

        val merged = Bundle()
        if (options != null) merged.putAll(options)
        merged.putString("androidPackageName", "android")

        // `du8` is the obfuscated add-account task (likely `AddAccountTask` or
        // `AccountManagerFuture`); `e()` runs it. Kept obfuscated to avoid guessing.
        AddAccountTask(this, activity, userId, accountType, authTokenType, requiredFeatures, merged).start()
    }

    /** Was `b(int)`. Returns all accounts for [userId] (or null if service returns null). */
    fun getAccounts(userId: Int): Array<Account>? =
        try {
            Log.d("VAccount", "getAccounts null")
            getService().getAccounts(userId, null)
        } catch (e: RemoteException) {
            VirtualRuntime.crash(e)
        }

    /** Was `c(String)`. Returns all accounts of [accountType] for the current virtual user. */
    fun getAccounts(accountType: String?): Array<Account>? =
        try {
            Log.d("VAccount", "getAccounts $accountType")
            getService().getAccounts(VUserHandle.getVUid(), accountType)
        } catch (e: RemoteException) {
            VirtualRuntime.crash(e)
            throw e
        }

    /** Was `d()`. Returns every authenticator description registered for the current virtual user. */
    fun getAuthenticatorTypes(): Array<AuthenticatorDescription>? =
        try {
            getService().getAuthenticatorTypes(VUserHandle.getVUid())
        } catch (e: RemoteException) {
            VirtualRuntime.crash(e)
        }

    /**
     * Was `e()`. Returns the cached `IAccountManager`, re-binding on first use or
     * when the cached binder has died. In the client process we *don't* check for
     * binder death (the server process is the one that cares), so stale-but-alive
     * binders are reused instead of re-bound.
     *
     * The `synchronized` block re-reads and overwrites [accountManager] under the
     * lock, matching the double-checked locking pattern in the original.
     */
    fun getService(): IAccountManager {
        val current = this@VAccountManager.accountManager
        if (current == null ||
            (!current.asBinder()!!.isBinderAlive && !VirtualCore.gCore.isClientProcess())
        ) {
            synchronized(VAccountManager::class.java) {
                val manager = IAccountManager.Stub.asInterface(
                    ServiceManagerNative.getService("account")
                )!!
                try {
                    manager.asBinder()!!.linkToDeath(BinderDeathRecipient(manager), 0)
                } catch (_: Throwable) {
                }
                this@VAccountManager.accountManager = manager
            }
        }
        return this@VAccountManager.accountManager!!
    }

    companion object {
        /**
         * Was `b`. The singleton. Name kept as `b` for bytecode compatibility with
         * any remaining Java callers (`VAccountManager.b.e()`).
         */
        @JvmField
        val accountManager: VAccountManager = VAccountManager()

        /** Idiomatic Kotlin accessor for [accountManager]. */
        val instance: VAccountManager get() = accountManager
    }
}
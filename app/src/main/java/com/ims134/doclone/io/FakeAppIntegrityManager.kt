package com.ims134.doclone.io

import android.content.IntentSender
import android.content.integrity.IAppIntegrityManager
import android.os.Bundle
import android.os.RemoteException

/**
 * Was `r82`. Inert implementation of [android.content.integrity.IAppIntegrityManager] — all methods return
 * null / do nothing. Used as the base IInterface for [AppIntegrityManagerStub];
 * the wrapper's method proxies intercept every call before it reaches this class,
 * so these bodies are only exercised if ContentProviderHolder method is not proxied.
 *
 * Renamed from `AppIntegrityManagerStub` (r82's original class name) to
 * `FakeAppIntegrityManager` to avoid the name collision with the wrapper
 * [AppIntegrityManagerStub] (was `wq`). Follows the same convention as
 * [FakeUpdateEngine] and [FakeSystemUpdateManager].
 */
internal class FakeAppIntegrityManager : IAppIntegrityManager.Stub() {

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
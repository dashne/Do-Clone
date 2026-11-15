package com.ims134.doclone.io

import android.app.PendingIntent
import android.os.IBinder
import android.os.IInterface
import android.os.RemoteException
import android.util.Log
import java.lang.reflect.Method
import java.util.Collections
import java.util.Map

/* JADX INFO: loaded from: classes2.dex */
class LockSettingsStub : BinderInvocationProxy(
    makeLockSettingsStub(),
    "lock_settings",
) {

    /**
     * Was `aa`, `b`, `c`, `d`, `e`, `f`, `g` — seven identical inner classes that
     * all overrode `call` to return `null`. Merged into one class since their
     * bodies were byte-for-byte identical.
     */
    inner class ReturnNullProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? = null
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ResultStaticMethodProxy("getRecoverySecretTypes", IntArray(0)))
        addMethodProxy(ReturnNullProxy("setRecoverySecretTypes"))
        addMethodProxy(ReturnNullProxy("initRecoveryServiceWithSigFile"))
        addMethodProxy(ResultStaticMethodProxy("generateKey", null))
        addMethodProxy(ResultStaticMethodProxy("importKey", null))
        addMethodProxy(ResultStaticMethodProxy("getKey", null))
        addMethodProxy(ReturnNullProxy("removeKey"))
        addMethodProxy(ReturnNullProxy("setSnapshotCreatedPendingIntent"))
        addMethodProxy(ReturnNullProxy("setServerParams"))
        addMethodProxy(ReturnNullProxy("setRecoveryStatus"))
        addMethodProxy(
            ResultStaticMethodProxy("getRecoveryStatus", Collections.emptyMap<Any?, Any?>())
        )
        addMethodProxy(ReturnNullProxy("closeSession"))
        addMethodProxy(ResultStaticMethodProxy("getKeyChainSnapshot", null))
        addMethodProxy(ResultStaticMethodProxy("setKey", ""))
        addMethodProxy(
            ResultStaticMethodProxy("startRecoverySessionWithCertPath", ByteArray(0))
        )
        addMethodProxy(ResultStaticMethodProxy("recoverKeyChainSnapshot", null))
        addMethodProxy(ResultStaticMethodProxy("hasSecureLockScreen", false))
        addMethodProxy(ResultStaticMethodProxy("tryUnlockWithCachedUnifiedChallenge", true))
        addMethodProxy(ResultStaticMethodProxy("removeCachedUnifiedChallenge", null))
    }

    @Throws(Throwable::class)
    override fun inject() {
        try {
            super.inject()
        } catch (th: Throwable) {
            Log.d("LockSettingsStub", "inject lock_settings", th)
        }
    }

    /**
     * Was `EmptyLockSettings`. Inert `ILockSettings` stub used as the base
     * interface when the real `lock_settings` service isn't available (which
     * is the norm on modern Android where the service lives behind
     * `LockSettingsService` in `system_server`). Every method returns Stub default
     * value or is Stub no-op, so the clone sees "no lock screen configured".
     */
    class EmptyLockSettings : ILockSettings.Stub() {

        @Throws(RemoteException::class)
        override fun checkVoldPassword(userId: Int): Boolean = false

        @Throws(RemoteException::class)
        override fun generateKey(password: String?): String? = null

        @Throws(RemoteException::class)
        override fun getBoolean(key: String?, defaultValue: Boolean, userId: Int): Boolean = false

        @Throws(RemoteException::class)
        override fun getHashFactor(password: String?, userId: Int): ByteArray = ByteArray(0)

        @Throws(RemoteException::class)
        override fun getKey(password: String?): String? = null

        @Throws(RemoteException::class)
        override fun getLong(key: String?, defaultValue: Long, userId: Int): Long = 0L

        override fun getRecoverySecretTypes(): IntArray = IntArray(0)

        @Throws(RemoteException::class)
        override fun getRecoveryStatus(): kotlin.collections.Map<*, *>? = null

        @Throws(RemoteException::class)
        override fun getSeparateProfileChallengeEnabled(userId: Int): Boolean = false

        @Throws(RemoteException::class)
        override fun getString(key: String?, defaultValue: String?, userId: Int): String? = null

        @Throws(RemoteException::class)
        override fun getStrongAuthForUser(userId: Int): Int = 0

        @Throws(RemoteException::class)
        override fun havePassword(userId: Int): Boolean = false

        @Throws(RemoteException::class)
        override fun havePattern(userId: Int): Boolean = false

        @Throws(RemoteException::class)
        override fun importKey(alias: String?, key: ByteArray?): String? = null

        @Throws(RemoteException::class)
        override fun systemReady() {}

        @Throws(RemoteException::class)
        override fun closeSession(alias: String?) {}

        @Throws(RemoteException::class)
        override fun registerStrongAuthTracker(token: IBinder?) {}

        @Throws(RemoteException::class)
        override fun removeKey(alias: String?) {}

        @Throws(RemoteException::class)
        override fun resetKeyStore(userId: Int) {}

        override fun setRecoverySecretTypes(secretTypes: IntArray?) {}

        @Throws(RemoteException::class)
        override fun setServerParams(serverParams: ByteArray?) {}

        @Throws(RemoteException::class)
        override fun setSnapshotCreatedPendingIntent(intent: PendingIntent?) {}

        @Throws(RemoteException::class)
        override fun unregisterStrongAuthTracker(token: IBinder?) {}

        @Throws(RemoteException::class)
        override fun userPresent(userId: Int) {}

        @Throws(RemoteException::class)
        override fun requireStrongAuth(strongAuthReason: Int, userId: Int) {}

        @Throws(RemoteException::class)
        override fun setRecoveryStatus(password: String?, status: Int) {}

        @Throws(RemoteException::class)
        override fun initRecoveryServiceWithSigFile(
            rootCertificateAlias: String?,
            recoveryCert: ByteArray?,
            sigFile: ByteArray?,
        ) {}

        @Throws(RemoteException::class)
        override fun setLong(key: String?, value: Long, userId: Int) {}

        @Throws(RemoteException::class)
        override fun setSeparateProfileChallengeEnabled(
            userId: Int,
            enabled: Boolean,
            managedUserPassword: String?,
        ) {}

        @Throws(RemoteException::class)
        override fun setString(key: String?, value: String?, userId: Int) {}

        @Throws(RemoteException::class)
        override fun setLockCredential(
            credential: String?,
            type: Int,
            savedCredential: String?,
            requestedQuality: Int,
            userId: Int,
        ) {}
    }

    companion object {
        /**
         * Was the constructor's setup block. Fetches the real `lock_settings`
         * binder; if present, wraps the real `ILockSettings`, otherwise falls
         * back to [EmptyLockSettings].
         */
        private fun makeLockSettingsStub(): BinderInvocationStub {
            val binder: IBinder? = ServiceManager.getService.call("lock_settings")
            val iface: IInterface = if (binder != null) {
                ILockSettings.Stub.asInterface(binder)!!
            } else {
                EmptyLockSettings()
            }
            return BinderInvocationStub(iface)
        }
    }
}
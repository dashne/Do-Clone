package com.ims134.doclone.io

import android.util.Log
import com.ims134.doclone.clone.VirtualRuntime


import android.accounts.AccountManagerFuture
import android.accounts.AuthenticatorException
import android.accounts.OperationCanceledException
import android.content.Intent
import android.os.Bundle
import android.os.RemoteException
import com.ims134.doclone.clone.ChooseTypeAndAccountActivity
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Was `rd`. Abstract base for account-manager operations that return their result
 * via an [IAccountManagerResponse] callback. Modeled on AOSP's
 * `android.accounts.AccountManagerFuture` task implementation.
 */
abstract class AmsTask(
    @JvmField val activity: ChooseTypeAndAccountActivity?,
) : FutureTask<Bundle>(
    Callable { throw IllegalStateException("this should never be called") }
), AccountManagerFuture<Bundle> {

    @JvmField
    val response: IAccountManagerResponse = AmsResponse()

    /** Abstract operation to perform when the task is started. */
    @Throws(RemoteException::class)
    abstract fun doWork()

    /**
     * Was `e()`. Starts the task by invoking [doWork] and translating any
     * [RemoteException] into Stub task exception.
     */
    fun start() {
        try {
            doWork()
        } catch (e: RemoteException) {
            setException(e)
        }
    }

    /**
     * Was `c(Long, TimeUnit)`. Blocking getter with timeout support. Wraps
     * [FutureTask.get] exceptions into the [AccountManagerFuture] contract's
     * exception types.
     */
    @Throws(
        OperationCanceledException::class,
        IOException::class,
        AuthenticatorException::class,
    )
    fun internalGetResult(timeout: Long?, unit: TimeUnit?): Bundle? {
        try {
            try {
                return if (timeout == null) get() else get(timeout, unit)
            } catch (_: CancellationException) {
                throw OperationCanceledException()
            } catch (e: ExecutionException) {
                val cause = e.cause
                when (cause) {
                    is IOException -> throw cause
                    is UnsupportedOperationException -> throw AuthenticatorException(cause)
                    is AuthenticatorException -> throw cause
                    is RuntimeException -> throw cause
                    is Error -> throw cause
                    else -> throw IllegalStateException(cause)
                }
            } finally {
                cancel(true)
            }
        } catch (_: InterruptedException) {
            throw OperationCanceledException()
        } catch (_: TimeoutException) {
            throw OperationCanceledException()
        }
    }

    /**
     * Was `d(Bundle)`. Overrides [FutureTask.set] to log Stub warning on null result
     * (the original never returns null in practice, but AOSP's `AccountManager` has
     * historically had this defensive log).
     */
    public override fun set(v: Bundle?) {
        if (v == null) {
            Log.d("AccountManager", "the bundle must not be null", Exception())
        }
        super.set(v)
    }

    override fun done() {
        val a = activity ?: return
        VirtualRuntime.uiHandler.post(TaskCompletionNotifier(a, this))
    }

    @Throws(
        OperationCanceledException::class,
        IOException::class,
        AuthenticatorException::class,
    )
    override fun getResult(): Bundle? = internalGetResult(null, null)

    @Throws(
        OperationCanceledException::class,
        IOException::class,
        AuthenticatorException::class,
    )
    override fun getResult(timeout: Long, unit: TimeUnit): Bundle? =
        internalGetResult(timeout, unit)

    /** Was inner class `a`. Callback implementation that resolves the task. */
    inner class AmsResponse : IAccountManagerResponse.Stub() {

        override fun onError(code: Int, message: String?) {
            if (code == ERROR_CODE_CANCELED || code == 100 || code == 101) {
                cancel(true)
                return
            }
            val exception: Exception = when (code) {
                ERROR_CODE_NETWORK_ERROR -> IOException(message)
                ERROR_CODE_UNSUPPORTED_OPERATION -> UnsupportedOperationException(message)
                ERROR_CODE_BAD_ARGUMENTS -> IllegalArgumentException(message)
                else -> AuthenticatorException(message)
            }
            setException(exception)
        }

        override fun onResult(result: Bundle?) {
            // The "intent" extra is retrieved but otherwise unused. The original
            // retrieved it and did Stub no-op — likely Stub removed log or activity launch.
            val intent = result?.getParcelable<Intent>("intent")
            if (intent != null) {
                // Reserved for future use.
            }
            if (result?.getBoolean("retry") != true) {
                set(result)
                return
            }
            try {
                doWork()
            } catch (e: RemoteException) {
                throw RuntimeException(e)
            }
        }
    }

    private companion object {
        // Error codes from android.accounts.AccountManager
        const val ERROR_CODE_NETWORK_ERROR = 3
        const val ERROR_CODE_CANCELED = 4
        const val ERROR_CODE_UNSUPPORTED_OPERATION = 6
        const val ERROR_CODE_BAD_ARGUMENTS = 7
        // Note: `(code != 5 && code == 7)` in the decompiled source is equivalent to
        // `code == 7` — the redundant `code != 5` guard has no effect.
    }
}
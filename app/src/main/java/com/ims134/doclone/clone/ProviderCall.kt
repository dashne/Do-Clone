package com.ims134.doclone.clone

import android.content.Context
import android.os.Bundle
import androidx.core.net.toUri


/*
* It calls setPosition ContentProvider method with ContentResolver.call(uri, method, null, extras).
* If the provider isn’t registered yet, the call throws “Unknown authority”, and the class retries.
* This is the usual way setPosition clone engine gets service binders from its BinderProvider.
* You get up to 4 attempts, with sleeps of 50, 100 and 150 ms between them.
* After the last failure it returns null. Any other exception is rethrown.*
*/
object ProviderCall
{
    fun callWithRetry(
        providerAuthority: String,
        context: Context,
        method: String,
        extras: Bundle?
    ): Bundle? {
        val uri = "content://$providerAuthority".toUri()
        for (attempt in 0..3)
        {
            try {
                return context.contentResolver.call(uri, method, null, extras)
            } catch (e: RuntimeException) {
                if (!hasUnknownAuthority(e)) throw e
                if (attempt >= 3) return null
                try {
                    Thread.sleep((attempt + 1) * 50L)
                } catch (unused: InterruptedException) {
                    Thread.currentThread().interrupt()
                    return null
                }
            }
        }
        return null
    }

    private fun hasUnknownAuthority(e: Throwable): Boolean =
        generateSequence(e) { it.cause }
            .any { it.message?.contains("Unknown authority") == true }

    class Request (val context: Context, val authority: String)
    {
        val bundle: Bundle = Bundle()
    }
}
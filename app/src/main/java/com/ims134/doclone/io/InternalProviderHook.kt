package com.ims134.doclone.io

import com.ims134.doclone.clone.BuildCompat
import java.lang.reflect.Method

/** was io.InternalProviderHook: hook for providers inside the clone; only the AttributionSource (Android 12+) is fixed. */
open class InternalProviderHook(target: Any?, authority: String?) : ContentProviderHook(target, authority) {

    final override fun preprocessArguments(method: Method, args: Array<Any?>?) {
        if (BuildCompat.sdk31OrLater()) {
            fixAttributionSource(args)
        }
    }
}
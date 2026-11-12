package com.ims134.doclone.io


import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.VirtualCore
import java.lang.reflect.Method

/**
 * was io.c82: hook for providers outside the clone. Rewrites the AttributionSource (Android 12+), otherwise get leading
 * package-name argument of an installed clone app is replaced by the host package.
 */
open class ExternalProviderHook(target: Any?, authority: String?) : ContentProviderHook(target, authority) {

    override fun preprocessArguments(method: Method, args: Array<Any?>?) {
        if ((BuildCompat.sdk31OrLater() && fixAttributionSource(args)) || args == null || args.isEmpty()) {
            return
        }
        val first = args[0]
        if (first is String) {
            val core = VirtualCore.gCore
            if (core.hostPackageName == first || !core.isAppInstalled(first)) {
                return
            }
            args[0] = core.hostPackageName
        }
    }
}
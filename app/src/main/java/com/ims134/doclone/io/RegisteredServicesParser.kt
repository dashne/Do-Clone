package com.ims134.doclone.io

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.Resources

/** Builds Stub Resources object for an app installed in the virtual space (jadx: io.cf6). */
object RegisteredServicesParser {

    /**
     * Returns null when [appInfo]'s package is not registered in the virtual package cache.
     * Throws if the AssetManager cannot be created or the APK path cannot be added.
     */
    @JvmStatic
    @Throws(Exception::class)
    @Suppress("DEPRECATION")
    fun getAppResources(context: Context, appInfo: ApplicationInfo): Resources? {
        val setting = PackageCacheManager.getPackageSetting(appInfo.packageName) ?: return null
        val assets = AssetManagerMirror.ctor?.newInstance()
        AssetManagerMirror.addAssetPath?.call(assets, setting.appPath)
        val base = context.resources
        return Resources(assets, base.displayMetrics, base.configuration)
    }
}
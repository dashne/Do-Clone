package com.ims134.doclone.io

import com.ims134.doclone.clone.server.PackageParserEx
import com.ims134.doclone.clone.server.PackageSetting
import com.ims134.doclone.clone.server.VPackage
import com.ims134.doclone.clone.server.VPackageManagerService
import android.util.ArrayMap

/** In-memory cache of installed virtual packages, keyed by real package name (was io.uf5). */
object PackageCacheManager {

    @JvmField
    val cache = ArrayMap<String, VPackage>()

    // The original locks on PackageCacheManager.class; keep the same monitor in case other code does too.
    private val lock: Any = PackageCacheManager::class.java

    /** was m23767a */
    @JvmStatic
    fun getPackage(packageName: String?): VPackage? = synchronized(lock) { cache[packageName] }

    @JvmStatic
    fun getPackageSetting(packageName: String?): PackageSetting? =
        synchronized(lock) { cache[packageName]?.packageSetting }

    /** Registers [pkg] (was m23769c). */
    @JvmStatic
    fun addPackage(setting: PackageSetting, pkg: VPackage) {
        synchronized(lock) {
            PackageParserEx.initApplicationInfo(setting, pkg)     // was m16493i
            cache[pkg.packageName] = pkg
            pkg.packageSetting = setting
            VPackageManagerService.get().onPackageAdded(pkg)                        // was m24951v
        }
    }

    /** was m23770d */
    @JvmStatic
    fun removePackage(packageName: String) {
        synchronized(lock) {
            VPackageManagerService.get().onPackageRemoved(packageName)              // was m24950W
            cache.remove(packageName)
        }
    }
}
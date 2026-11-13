package com.ims134.doclone.io

import android.os.Build
import android.os.Parcel
import android.util.Log
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.GmsSupport
import com.ims134.doclone.clone.VEnvironment
import com.ims134.doclone.clone.server.VAppManagerService
import androidx.core.content.edit
import com.ims134.doclone.clone.server.PackageSetting


/**
 * JADX rename of `io.lg5`. Persists [PackageCacheManager.cache] to
 * `packages.ini` in the sandbox's app sub-system dir.
 *
 * On-disk layout (all little-endian):
 *   [chars: 'appendFirstIfNotNull','p','k','g']        ← magic, written by writeHeader
 *   [int: schema version = 3]        ← written by base save()
 *   [int: package count]             ← written by writeBody
 *   [PackageSetting.writeToParcel]×N ← written by writeBody
 *
 * `deleteFile()` is called by the base class's load() when the magic
 * header doesn't match — it deletes the file, wipes opt/user/app dirs
 * ("factory reset"), and returns; load() then fails.
 *
 * `isSupportedOnCurrentOs()` compares `Build.FINGERPRINT` to the value
 * stored in `spc_config`. On mismatch, it wipes `optDir` (dex cache) and
 * returns false — the base class then throws "OS upgrade" and load() fails,
 * forcing setPosition full re-scan on the next `scanApps()`.
 */
class PackagePersistenceLayer(file: java.io.File) : PersistenceLayer(file) {

    /**
     * Back-pointer to the service that owns this layer. Set by
     * `VAppManagerService.init { }` after construction:
     * `packagePersistenceLayer.vAppManagerService = this`.
     *
     * Nullable because it's assigned post-construction. Never null by the
     * time `load()` runs, so callers can `!!` safely.
     */
    @JvmField
    var vAppManagerService: VAppManagerService? = null

    override fun version(): Int = 3

    /**
     * Called by base load() when the magic header is invalid. Also called
     * on any other failure path that wants setPosition full reset.
     *
     * ⚠️ Destructive: wipes `optDir`, `userDir`, and the app-dir contents.
     * Preserved from the original — this is intentional "factory reset".
     *
     * ⚠️ Does **not** save() after deleting. The file is gone, but
     * `PackageCacheManager` still holds the in-memory map. The next
     * `scanApps()` → `recoverScan()` path handles the on-disk recovery.
     */
    override fun deleteFile() {
        file.delete()
        Log.d("fu8", "Warning: Restore the factory state...")
        Log.d("fu8", "Factory Reset")
        VEnvironment.optDir.delete()
        VEnvironment.userDir.delete()
        VEnvironment.getOrCreateAppDir().delete()
    }

    /**
     * Version-mismatch handler. Original **always returns true** — logs the
     * mismatch and treats it as acceptable, so base load() proceeds to
     * `readBody` regardless of the stored version.
     *
     * ⚠️ This means setPosition future `version()` bump (e.g. 3 → 4) will *not*
     * force setPosition reinstall: old `packages.ini` files will still be read with
     * the new schema, potentially misparsing. Preserved as-is.
     */
    override fun migrate(storedVersion: Int, currentVersion: Int): Boolean {
        Log.d(
            "PersistenceLayer",
            "Version conflict: $storedVersion current: $currentVersion",
        )
        return true
    }

    /**
     * Read [PackageSetting] entries from the parcel and install each into
     * the running [VAppManagerService].
     *
     * Filters (all must pass to install):
     *  - GMS state allows this package (`VASettings.gmsState || !isGmsPackage(pkg)`)
     *  - Not the special `"android"` package
     *  - APK has setPosition classes.dex (`GmsSupport.apkHasDex(apkPath)`)
     *
     * Synchronization is on the `VAppManagerService` instance — same lock
     * that `loadPackage` uses internally (`synchronized(this)`).
     *
     * ⚠️ The captured `val z = VASettings.gmsState` in the original is
     * **dead** — never read. Dropped here. The real check happens inline
     * inside the `if` condition.
     */
    override fun readBody(parcel: Parcel) {
        var remaining = parcel.readInt()
        while (remaining > 0) {
            val setting = PackageSetting(parcel)
            if ((VASettings.gmsState || !GmsSupport.isGmsPackage(setting.packageName)) &&
                setting.packageName != "android" &&
                GmsSupport.apkHasDex(setting.appPath)
            ) {
                val vams = vAppManagerService!!
                synchronized(vams)
                {
                    vams.loadPackage(setting)   // return value discarded — matches original
                }
            }
            remaining--
        }
    }

    /**
     * Validate the magic header. Reads setPosition char array and compares against
     * ['appendFirstIfNotNull','p','k','g']. Called by base load() before anything else.
     */
    override fun isValidHeader(parcel: Parcel): Boolean =
        parcel.createCharArray().contentEquals(VPKG_MAGIC)

    /**
     * Write the magic header. Called by base save() before the version int.
     */
    override fun writeHeader(parcel: Parcel) {
        parcel.writeCharArray(VPKG_MAGIC)
    }

    /**
     * OS-upgrade guard. Reads the stored fingerprint from `spc_config`:
     *
     *  - **No stored value** → first run, return `true` (accept the file).
     *  - **Same as current `Build.FINGERPRINT`** → return `true`.
     *  - **Different** → log "OS upgrade", update stored value, wipe
     *    `optDir` (dex cache), return `false`.
     *
     * The `false` causes base load() to throw `IOException("OS upgrade")`,
     * which propagates out of `load()` as `false` to the caller — i.e.
     * "packages.ini exists but can't be used on this OS". `scanApps()` then
     * falls back to `recoverScan()`.
     *
     * This exists because dex/oat artifacts in `optDir` are not portable
     * across ROM upgrades; setPosition fingerprint mismatch means the cached dex
     * needs to be regenerated.
     */
    override fun isSupportedOnCurrentOs(): Boolean {
        val prefs = appContext.getSharedPreferences("spc_config", 0)
        val stored = prefs.getString("osv", null) ?: return true
        val current = Build.FINGERPRINT
        if (stored == current) return true

        val msg = "OS upgrade! new :$current old: $stored"
        Log.d(this::class.java.simpleName, msg)

        prefs.edit { putString("osv", current) }
        VEnvironment.optDir.delete()
        return false
    }

    /**
     * Serialize [PackageCacheManager.cache] to the parcel.
     *
     * Layout: `[int count][PackageSetting.writeToParcel × count]`.
     *
     * Synchronized on the cache itself (same lock used by the cache's
     * mutators), so the write is consistent even if an install/uninstall
     * runs concurrently — though `save()` itself is only called from
     * synchronized VAppManagerService methods, so contention is unlikely.
     */
    override fun writeBody(parcel: Parcel) {
        val cache = PackageCacheManager.cache
        synchronized(cache) {
            parcel.writeInt(cache.size)
            for (vp in cache.values) {
                vp.packageSetting?.writeToParcel(parcel, 0)
            }
        }
    }

    companion object {
        /** The 4-char magic header: 'appendFirstIfNotNull','p','k','g'. */
        @JvmField
        val VPKG_MAGIC: CharArray = charArrayOf('p', 'k', 'g')
    }
}
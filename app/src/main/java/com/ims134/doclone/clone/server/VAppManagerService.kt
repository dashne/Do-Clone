package com.ims134.doclone.clone.server

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder.DeathRecipient
import android.os.RemoteCallbackList
import android.os.RemoteException
import android.util.Log
import androidx.core.content.edit
import androidx.core.net.toUri
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.ComponentStatusMgr
import com.ims134.doclone.clone.GmsSupport
import com.ims134.doclone.clone.GmsSupport.apkHasDex
import com.ims134.doclone.clone.IAppManager
import com.ims134.doclone.clone.InstallResult
import com.ims134.doclone.clone.Oat
import com.ims134.doclone.clone.VEnvironment
import com.ims134.doclone.clone.VEnvironment.Companion.getOrCreateAppSubSystemFile
import com.ims134.doclone.clone.VPackageManager
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.Vdex
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.io.ArtDexOptimizer
import com.ims134.doclone.io.DataReader
import com.ims134.doclone.io.Elf
import com.ims134.doclone.io.FileUtils
import com.ims134.doclone.io.IAppRequestListener
import com.ims134.doclone.io.IPackageObserver
import com.ims134.doclone.io.NativeLibraryHelperCompat
import com.ims134.doclone.io.OatHelper
import com.ims134.doclone.io.PackageCacheManager
import com.ims134.doclone.io.PackageCacheManager.getPackageSetting
import com.ims134.doclone.io.PackagePersistenceLayer
import com.ims134.doclone.io.PrivilegeAppOptimizer
import com.ims134.doclone.io.SpecialComponentList
import com.ims134.doclone.io.UidSystem
import com.ims134.doclone.io.VASettings
import dalvik.system.DexFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream


class VAppManagerService : IAppManager.CloneAppManager() {

    @Volatile
    private var scanningApp = false

    val uidSystem = UidSystem()
    var packagePersistenceLayer: PackagePersistenceLayer

    var packageObserver: RemoteCallbackList<IPackageObserver>

    var visibleOutsidePackage: HashSet<String>

    private var appRequestListener: IAppRequestListener? = null

    init {
        val file = VEnvironment.virtualDir
        packagePersistenceLayer =  PackagePersistenceLayer(File(getOrCreateAppSubSystemFile(), "packages.ini"))
        packagePersistenceLayer.vAppManagerService = this
        this.visibleOutsidePackage = HashSet<String>()
        this.packageObserver = RemoteCallbackList<IPackageObserver>()
    }

    companion object
    {
        @JvmField val instance = AtomicReference<VAppManagerService>()

        @JvmStatic fun get(): VAppManagerService? = instance.get()

        // Flag bits of `installPackage(flags)`, as decoded from the smali. The names are mine (your original has an
        // install-strategy class with its own names, put those in):
        const val INSTALL_UPDATE_IF_EXISTS = 0x4        // already installed: allow the update
        const val INSTALL_UPDATE_IF_NEWER = 0x8         // already installed: update only if versionCode is higher
        const val INSTALL_REJECT_IF_EXISTS = 0x2        // already installed (and not newer): fail
        const val INSTALL_SKIP_IF_EXISTS = 0x10         // already installed: report success, change nothing
        const val INSTALL_USE_HOST_APK = 0x20           // use the host's installed apk instead of copying it
        const val LOCATION_PROVIDER_PKG = "com.android.location.provider"
        const val UID_LIST_INI = "uid-list.ini"


        // Replacement for the companion-object members in VAppManagerService.kt:
        //   systemReady(), prepareLocationProviderOat(), extractFromOat(), extractFromVdex(),
        //   findRoDataSection(), cString(), hasDexEntry().
        // Keep setWorldReadable() and the constants (TAG, LOCATION_PROVIDER_PKG, UID_LIST_INI, ...).
        //
        // Names follow the readable decompile. This needs these renames elsewhere:
        //   Oat:        field `get` -> `dexes`
        //   Elf:        field `c` -> `dataReader`; Section `get` -> `sectionNameOffset`;
        //               Section `get()` -> `getSectionContentOffset()`
        //   OatHelper:  `get` -> `oatArches`, `b` -> `vdexOatArches`; public
        //               `writeDexEntry(zip: ZipOutputStream, index: Int, dex: Dex)`
        //   Vdex:       move VdexDexFile inside Vdex as `Vdex.VdexDexFile`
        //   VAppManagerService: `uidAllocator` -> `uidSystem`; `sInstance` -> `instance`
        //   VEnvironment = ou8, BuildCompat = sb0, FileUtils = sa2, VLog = Logger, ArtDexOptimizer = dv

        private const val OAT_TAG = "OatHelper"

        @JvmStatic
        fun systemReady() {
            try {
                FileUtils.chmod755(VEnvironment.virtualDir.absolutePath)
                FileUtils.chmod755(VEnvironment.dataDir.absolutePath)
                FileUtils.chmod755(VEnvironment.getOrCreateAppDir().absolutePath)
            } catch (e: Exception) {
                // directories may already have other permissions; ignore as the original did
            }

            if (!BuildCompat.sdk28OrLater() && !(VirtualCore.gCore.hostPackageName?.endsWith("arm64")?:false)) {
                prepareLocationProviderOat()
            }

            val service = VAppManagerService()
            val uidSystem = service.uidSystem
            uidSystem.uidByPackageName.clear()
            val subSystemDir = VEnvironment.getOrCreateAppSubSystemFile()
            if (!uidSystem.tryCacheUids(File(subSystemDir, UID_LIST_INI))) {
                uidSystem.tryCacheUids(File(subSystemDir, "$UID_LIST_INI.bak"))
            }
            instance.set(service)
        }

        /**
         * Pulls classes.dex for the location provider out of the system oat or vdex, for devices where
         * the framework code isn't usable as-is.
         */
        private fun prepareLocationProviderOat() {
            val pkg = LOCATION_PROVIDER_PKG
            val framework = File(VEnvironment.virtualDir, "framework").also { VEnvironment.getOrCreatePath(it) }
            val libDir = File(framework, pkg).also { VEnvironment.getOrCreatePath(it) }
            val extractedJar = File(libDir, "extracted.jar")
            val dexClass = File(libDir, "classes.dex")
            if (dexClass.exists()) return

            val systemJar = File("/system/framework/$pkg.jar")
            if (!systemJar.exists()) return

            val hasDex = hasDexEntry(systemJar)
            Log.d(OAT_TAG, "apk : ${systemJar.path} hasDex: $hasDex")
            if (hasDex) return

            // If any oat exists for the first arch set, nothing needs extracting.
            if (OatHelper.oatArches.any { File("/system/framework/oat/$it/$pkg.oat").exists() }) return

            for (arch in OatHelper.vdexOatArches) {
                val oat = File("/system/framework/oat/$arch/$pkg.oat")
                if (oat.exists()) {
                    extractFromOat(oat, extractedJar, dexClass)
                    return
                }
                val vdex = File("/system/framework/oat/$arch/$pkg.vdex")
                // extractFromVdex returns false only when the vdex could not be read; then the loop continues.
                if (vdex.exists() && extractFromVdex(vdex, extractedJar, dexClass)) return
            }
        }

        private fun hasDexEntry(jar: File): Boolean = try {
            ZipFile(jar.path).use { it.getEntry("classes.dex") != null }
        } catch (t: Throwable) {
            // The original logged here only when closing failed; this logs any failure.
            Log.d(OAT_TAG, "Error when find dex for path: ${jar.path}")
            Log.d(OAT_TAG, Log.getStackTraceString(t))
            false
        }

        private fun extractFromOat(oat: File, extractedJar: File, dexClass: File) {
            var elf: Elf? = null
            var zip: ZipOutputStream? = null
            try {
                val opened = Elf(oat)
                elf = opened
                val rodata = findRoDataSection(opened) ?: return

                val reader = opened.dataReader
                reader.setPosition(rodata.getSectionContentOffset().toInt())
                val oatFile = Oat(reader)

                val out = ZipOutputStream(FileOutputStream(extractedJar))
                zip = out
                oatFile.dexes.forEachIndexed { index, dex -> OatHelper.writeDexEntry(out, index, dex) }
                out.flush()
                out.close()

                ArtDexOptimizer.interpretDex2Oat(extractedJar.path, dexClass.path)
                reader.close()
            } catch (t: Throwable) {
                Log.d(OAT_TAG, t.toString())
            } finally {
                FileUtils.CloseStream(elf)
                FileUtils.CloseStream(zip)
            }
        }

        /**
         * Returns true when the vdex was handled (the caller stops), false when it could not be read
         * (the caller tries the next arch).
         */
        private fun extractFromVdex(vdex: File, extractedJar: File, dexClass: File): Boolean {
            val reader = try {
                DataReader(vdex)
            } catch (t: Throwable) {
                return false
            }
            val dexFiles = try {
                Vdex(reader).dexFiles
            } catch (t: Throwable) {
                FileUtils.CloseStream(reader)
                return false
            }

            var zip: ZipOutputStream? = null
            try {
                val out = ZipOutputStream(FileOutputStream(extractedJar))
                zip = out
                dexFiles.forEachIndexed { index, dex -> OatHelper.writeDexEntry(out, index, dex) }
                out.flush()
                out.close()
                ArtDexOptimizer.interpretDex2Oat(extractedJar.path, dexClass.path)
            } catch (t: Throwable) {
                Log.d(OAT_TAG, t.toString())
            } finally {
                FileUtils.CloseStream(reader)
                FileUtils.CloseStream(zip)
            }
            return true
        }

        /** The `.rodata` section of [elf], or null. */
        private fun findRoDataSection(elf: Elf): Elf.Section? {
            for (section in elf.sections) {
                val name = if (section.sectionNameOffset == 0) {
                    "SHN_UNDEF"
                } else {
                    cString(elf.strtab, section.sectionNameOffset)
                }
                if (".rodata" == name) return section
            }
            return null
        }

        private fun cString(bytes: ByteArray, start: Int): String {
            var end = start
            while (bytes[end].toInt() != 0) end++
            return String(bytes, start, end - start)
        }


        /** Original `getDirPermission`. */
        @JvmStatic
        fun getDirPermission(file: File) {
            try {
                val canonical = file.parentFile?.canonicalFile?.let { File(it, file.name) } ?: file
                if (canonical.canonicalFile == canonical.absoluteFile) {
                    FileUtils.chmod755(file.parentFile!!.absolutePath)
                    FileUtils.chmod755(file.absolutePath)
                }
            } catch (_: Exception) {}
        }



        /**
         * Installs each package of [packages] into virtual user [userId] if it is not there yet, but only when the host
         * has it installed and its apk contains dex code.
         */
        @JvmStatic
        fun installGmsPackagesForUser(packages: Set<String>, userId: Int) {
            val appManager = VAppManagerService.get()
            if(appManager == null) throw NullPointerException(appManager)
            for (packageName in packages) {
                appManager.let { if (it.isAppInstalledAsUser(userId, packageName)) continue }

                val hostInfo: ApplicationInfo? = try {
                    VirtualCore.gCore.unhookPackageManager.getApplicationInfo(packageName, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }

                val sourceDir = hostInfo?.sourceDir
                if (sourceDir == null || !apkHasDex(sourceDir)) continue

                if (userId == 0) {
                    // User 0: install the host's apk (flag 32 as in the original, no notification).
                    appManager.installPackage(packageName, sourceDir, 32, false)
                } else {
                    // Other users: add the already-installed package to that user.
                    appManager.installPackageAsUser(userId, packageName, false)
                }
            }
        }
    }

    fun loadPackage(packageSetting: PackageSetting): Boolean {
        Log.d(
            "fu8",
            "Load package: ${packageSetting.packageName} id ${packageSetting.appId}",
        )

        val packageIniFile: File
        var vPackage: VPackage?

        if (packageSetting.gmsEnabled) {
            // System / GMS package: must still exist on the real PackageManager
            try {
                if (VirtualCore.gCore.unhookPackageManager.getApplicationInfo(packageSetting.packageName!!, 0) == null) {
                    return false
                }
            } catch (_: PackageManager.NameNotFoundException) {
                return false
            }

            packageIniFile = File(
                VEnvironment.getOrCreateAppSubFile(packageSetting.packageName!!), "package.ini")
            vPackage = try {
                PackageParserEx.parseVPackage(packageSetting.packageName!!)
            } catch (_: Throwable) {
                null
            }

            // ⚠️ Dead branch (preserved) — `vPackage == null && vPackage?.packageName != null`
            // is always false. Should be `vPackage == null || vPackage?.packageName == null`.
            // The recovery never runs for GMS packages. See "Preserved bugs" below.
            if (vPackage == null && vPackage?.packageName != null) {
                packageIniFile.delete()
                installPackage(packageSetting.packageName, packageSetting.appPath, 36)
                vPackage = PackageParserEx.parseVPackage(packageSetting.packageName!!)
            }
        } else {
            packageIniFile = File(
                VEnvironment.getOrCreateAppSubFile(packageSetting.packageName!!), "package.ini")
            vPackage = PackageParserEx.parseVPackage(packageSetting.packageName!!)
            if (vPackage == null) {
                packageIniFile.delete()
                installPackage(packageSetting.packageName, packageSetting.appPath, 36)
                vPackage = PackageParserEx.parseVPackage(packageSetting.packageName!!)
            }
        }

        // Consolidates branch 1's `vPackage != null && vPackage.packageName != null`
        // with branch 2's `vPackage != null`. In practice parseVPackage never
        // returns setPosition VPackage with setPosition null package name, so the two are identical.
        if (vPackage?.packageName == null) return false

        getDirPermission(packageIniFile)
        PackageCacheManager.addPackage(packageSetting, vPackage)
        if (!SpecialComponentList.GOOGLE_SERVICES_FRAMEWORK_PACKAGE
                .contains(packageSetting.packageName)) {
            BroadcastSystem.broadcastSystem?.registerReceivers(vPackage)
        }
        return true
    }

    /**
     * Tells everyone that [setting]'s package was installed (was U / m18958U).
     * @param userId -1 = installed for all users (also sends the system-style PACKAGE_ADDED broadcast), else one user.
     */
    fun notifyAppInstalled(setting: PackageSetting, userId: Int) {
        val packageName = setting.packageName
        if (userId == -1) {
            val intent = Intent(Intent.ACTION_PACKAGE_ADDED)
            intent.data = "package:$packageName".toUri()
            VActivityManagerService.get().sendBroadcastAsUser(intent, VUserHandle.instance)
        }

        // Remote observers (the app UI process): all-users installs are reported as "installed" plus "as user 0".
        val observers = packageObserver            // RemoteCallbackList<IPackageObserver>
        val count = observers.beginBroadcast()
        try {
            for (i in count - 1 downTo 0) {
                try {
                    val observer = observers.getBroadcastItem(i)
                    if (userId == -1) {
                        observer.onPackageInstalled(packageName)
                        observer.onPackageInstalledAsUser(0, packageName)
                    } else {
                        observer.onPackageInstalledAsUser(userId, packageName)
                    }
                } catch (e: RemoteException) {
                    Log.e("fu8", "Package install observer failed", e)
                } catch (e: RuntimeException) {
                    Log.e("fu8", "Package install observer failed", e)
                }
            }
        } finally {
            observers.finishBroadcast()
        }

        //TODO:: solve this
        //VAccountManagerService.get().refreshAuthenticatorCache(null)
        VActivityManagerService.get().scheduleGmsCheckinWarmup(if (userId == -1) 0 else userId, packageName!!)
    }

    fun notifyPackageRemoved(packageSetting: PackageSetting, userId: Int) {
        val packageName = packageSetting.packageName

        if (userId == -1) {
            val intent = Intent("android.intent.action.PACKAGE_REMOVED")
            intent.data = "package:$packageName".toUri()
            VActivityManagerService.get().sendBroadcastAsUser(intent, VUserHandle.instance)
        }

        val observers = packageObserver
        val count = observers.beginBroadcast()
        try {
            for (i in count - 1 downTo 0) {
                val observer = observers.getBroadcastItem(i) as IPackageObserver
                try {
                    if (userId == -1) {
                        observer.onPackageUninstalled(packageName)
                        observer.onPackageUninstalledAsUser(0, packageName)
                    } else {
                        observer.onPackageUninstalledAsUser(userId, packageName)
                    }
                } catch (e: RemoteException) {
                    Log.e("fu8", "Package uninstall observer failed", e)
                } catch (e: RuntimeException) {
                    Log.e("fu8", "Package uninstall observer failed", e)
                }
            }
        } finally {
            observers.finishBroadcast()
        }


        //TODO:: solve this
        //VAccountManagerService.get().refreshAuthenticatorCache(null)
    }

    fun recoverPackages() {
        val dirs = VEnvironment.getOrCreateAppDir().listFiles() ?: return
        for (dir in dirs) {
            val name = dir.name
            if (name == "android" || name == "system") continue
            if (!VASettings.gmsState && GmsSupport.isGmsPackage(name)) continue

            var apk = File(dir, "base.apk")
            var flags = 0
            if (!apk.exists()) {
                val appInfo: ApplicationInfo? = try {
                    VirtualCore.gCore.unhookPackageManager.getApplicationInfo(name, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }
                if (appInfo == null || appInfo.publicSourceDir == null) {
                    FileUtils.deleteRecursively(dir)
                    VUserManagerService.get()?.let {
                        for (userId in it.ids)
                        {
                            FileUtils.deleteRecursively(VEnvironment.getOrCreateUserSubFile(userId, name))
                        }
                    }
                } else {
                    flags = 36
                    apk = File(appInfo.publicSourceDir)
                }
            }

            if (GmsSupport.apkHasDex(apk.path)) {
                val result = installPackage(name, apk.path, flags, false)
                if (!result.installed) {
                    Log.e("fu8", "Unable to install app ${name}: ${result.error}.")
                    FileUtils.deleteRecursively(dir)
                }
            }
        }
    }

    fun deletePackageData(packageSetting: PackageSetting) {
        val packageName = packageSetting.packageName!!
        try {
            BroadcastSystem.broadcastSystem?.removeReceivers(packageName)
            VActivityManagerService.get().killAppByPkg(packageName, -1)

            File(VEnvironment.getOrCreateAppSubFile(packageName), "base.apk").delete()
            FileUtils.deleteRecursively(VEnvironment.getOrCreateAppSubFile(packageName))
            VEnvironment.makeDexFile(packageName).delete()

            VUserManagerService.get()?.let {
                for (userId in it.ids) {
                    FileUtils.deleteRecursively(VEnvironment.getOrCreateUserSubFile(userId, packageName))
                    setPackageStoppedState(userId, packageName, false)
                }
            }

            PackageCacheManager.removePackage(packageName)
            packagePersistenceLayer.save()
        } catch (e: Exception) {
            // ignored, as in the original
        } finally {
            notifyPackageRemoved(packageSetting, -1)
        }
    }



    override fun addVisibleOutsidePackage(str: String?) {
        if (str != null)
        {
            this.visibleOutsidePackage.add(str)
        }
    }

    /** Installs the Google Play services packages (and the special packages) into [userId] if it is missing them. */
    fun checkPreInstallForUser(userId: Int) {
        if (!VUserManagerService.get()!!.exists(userId)) return

        if (VASettings.gmsState) {
            if (!isAppInstalledAsUser(userId, "com.google.android.gms")) {
                // No GMS yet: dependencies first, then GMS itself.
                GmsSupport.installGmsPackagesForUser(GmsSupport.GMS_DEPENDENCIES, userId)
                GmsSupport.installGmsPackagesForUser(GmsSupport.GMS_PACKAGES, userId)
            } else if (!isAppInstalledAsUser(userId, "com.google.android.gsf")) {
                // GMS is there but the Google Services Framework is not.
                GmsSupport.installGmsPackagesForUser(GmsSupport.GMS_DEPENDENCIES, userId)
            }
        }
        GmsSupport.installGmsPackagesForUser(SpecialComponentList.VENDOR_PACKAGES, userId)
    }

    // io.IAppManager.CloneAppManager
    override fun clearAppRequestListener() {
        this.appRequestListener = null
    }

    fun getAppId(str: String?): Int {
        val packageSetting = getPackageSetting(str)
        if (packageSetting != null) {
            return packageSetting.appId
        }
        return -1
    }

    override fun getAppRequestListener(): IAppRequestListener? {
        return this.appRequestListener
    }

    override fun getInstalledAppCount(): Int {
        return PackageCacheManager.cache.size
    }

    override fun getInstalledAppInfo(str: String?, i: Int): InstalledAppInfo? {
        synchronized(PackageCacheManager::class.java)
        {
            if (str != null) {
                try {
                    val packageSetting = getPackageSetting(str)
                    if (packageSetting != null) {
                        return packageSetting.toInstalledAppInfo()
                    }
                } catch (th: Throwable) {
                    throw th
                }
            }
            return null
        }
    }

    override fun getInstalledApps(i: Int): MutableList<InstalledAppInfo>
    {
        val arrayList = mutableListOf<InstalledAppInfo>()
        val it = PackageCacheManager.cache.values.iterator()
        while (it.hasNext()) {
            arrayList.add(it.next()!!.packageSetting!!.toInstalledAppInfo())
        }
        return arrayList
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    // io.IAppManager.CloneAppManager
    override fun getInstalledAppsAsUser(userId: Int, flags: Int): MutableList<InstalledAppInfo>
    {
        val result = mutableListOf<InstalledAppInfo>()

        PackageCacheManager.cache.forEach { (_, vk) ->
            vk.packageSetting?.let {
                val state = it.readUserState(userId)
                var installed = state.installed
                if((flags and 1) == 0 && state.hidden) installed = false
                if(installed)
                    result.add(it.toInstalledAppInfo())
            }
        }
        return result
    }

    override fun getPackageInstalledUsers(packageName: String?): IntArray {
        val packageSetting = getPackageSetting(packageName) ?: return IntArray(0)
        val vums =VUserManagerService.get() ?: return IntArray(0)
        return vums.ids.filter { packageSetting.readUserState(it).installed }
            .toIntArray()
    }

    // io.IAppManager.CloneAppManager, io.IAppManager
    override fun installPackage(str: String?, str2: String?, i: Int): InstallResult {
        return installPackage(str, str2, i, true)
    }


    override fun isAppInstalled(pkg: String?): Boolean {
        return !pkg.isNullOrEmpty() && PackageCacheManager.cache.containsKey(pkg)
    }

    // io.IAppManager.CloneAppManager, io.IAppManager
    override fun isOutsidePackageVisible(str: String?): Boolean {
        return str != null && this.visibleOutsidePackage.contains(str)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    // io.IAppManager.CloneAppManager
    override fun isPackageLaunched(i: Int, str: String?): Boolean {
        val packageSetting = getPackageSetting(str)
        return packageSetting != null && packageSetting.readUserState(i).launched
    }

    override fun notifyActivityBeforePause(str: String?, i: Int) {
        //TODO:: solve this
        //VirtualCore.gCore.getListener().getClass()
    }

    override fun notifyActivityBeforeResume(str: String?, i: Int) {
        //TODO:: solve this
        //VirtualCore.gCore.getListener().getClass()
    }

    override fun registerObserver(iPackageObserver: IPackageObserver?) {
        try {
            this.packageObserver.register(iPackageObserver)
        } catch (unused: Throwable) {
        }
    }

    override fun removeVisibleOutsidePackage(str: String?) {
        if (str != null) {
            this.visibleOutsidePackage.remove(str)
        }
    }


    override fun restart() {
        Log.d("fu8", "restart...")
        VirtualCore.gCore.javaClass

        //TODO:: solve this
        //val vActivityManager: VActivityManager = VActivityManager.instance
        //vActivityManager.getClass()
//        try {
//            vActivityManager.getService_().killAllApps()
//        } catch (unused: RemoteException) {
//        }
        this.packagePersistenceLayer.save()
        try {
            Thread.sleep(300L)
        } catch (unused2: Throwable) {
        }
        Log.d("fu8", "stopping...")
        System.exit(0)
    }

    fun savePersistenceData() {
        this.packagePersistenceLayer.save()
    }


    fun isPackageStopped(i: Int, str: String?): Boolean {
        if (str == null) {
            return false
        }
        return appContext.getSharedPreferences("virtual_package_state", 0)
            .getBoolean("stopped:$i:$str", false)
    }

    // ---- paste into VAppManagerService (member) ----




    override fun scanApps()
    {
        if (scanningApp) return
        Log.d("fu8", "=======scanApps========")
        synchronized(this)
        {
            try {
                scanningApp = true
                if (!File(VEnvironment.getOrCreateAppSubSystemFile(), "packages.ini").exists()) {
                    Log.d("fu8", "not found package list file. Recover!")
                    recoverPackages() // was toVirtualChannel()
                } else if (!packagePersistenceLayer.load()) {
                    Log.d("fu8", "Failed to parse package list file. Recover!")
                    recoverPackages()
                }
                VUserManagerService.get()?.ids?.forEach { checkPreInstallForUser(it) }
                PrivilegeAppOptimizer.instance.sendBootCompleted();

            } finally {
                scanningApp = false
            }
        }

        // Upgrade clones whose host app changed (versionCode or apk path)
        for (packageName in PackageCacheManager.cache.keys.toList()) {
            var newPath: String? = null
            try {
                val virtual = VPackageManager.instance?.getPackageInfo(packageName, 0, 0)
                val host = VirtualCore.gCore.unhookPackageManager.getPackageInfo(packageName, 0)
                if (virtual != null && host != null) {
                    @Suppress("DEPRECATION")
                    val different = virtual.versionCode != host.versionCode ||
                            virtual.applicationInfo?.sourceDir != host.applicationInfo?.sourceDir
                    if (different) newPath = host.applicationInfo?.sourceDir
                }
            } catch (e: Exception) {
                Log.d("fu8", e.message.toString())
            } catch (_: Throwable) {
            }
            if (newPath != null) {
                // 36 = 0x24 = use host apk (0x20) | update if exists (0x4)
                upgradePackage(packageName, newPath, 36)
                Log.d("fu8", "upgraded package: $packageName on path:$newPath")
            }
        }
        packagePersistenceLayer.save()
    }



    fun isScanningApp(): Boolean {return scanningApp}

    /** Sends BOOT_COMPLETED to the apps of virtual user [userId]. */
    fun sendBootCompleted(userId: Int) {
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)
        Log.d("fu8", "sendBootCompleted intent to $userId")
        VActivityManagerService.get().sendBroadcastAsUser(intent, VUserHandle(userId))
    }


    override fun setAppRequestListener(iAppRequestListener: IAppRequestListener?) {
        this.appRequestListener = iAppRequestListener
        if (iAppRequestListener != null) {
            try {
                iAppRequestListener.asBinder()
                    .linkToDeath(AppManagerServiceDeathRecipient(iAppRequestListener), 0)
            } catch (unused: RemoteException) {
            }
        }
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    // io.IAppManager.CloneAppManager
    override fun setPackageHidden(i: Int, str: String?, z: Boolean) {
        val packageSetting = getPackageSetting(str)
        if (packageSetting == null || !VUserManagerService.get()!!.exists(i)) {
            return
        }
        packageSetting.getOrCreateUserState(i).hidden = z
        this.packagePersistenceLayer.save()
    }

    @Synchronized
    override fun uninstallPackage(packageName: String?): Boolean {
        Log.d("fu8", "uninstallPackage $packageName")
        val setting = PackageCacheManager.getPackageSetting(packageName) ?: return false
        deletePackageData(setting)
        return true
    }



    @Synchronized
    override fun uninstallPackageAsUser(packageName: String?, userId: Int): Boolean {
        if (!(VUserManagerService.get()?.exists(userId)?:false)) return false
        val setting = PackageCacheManager.getPackageSetting(packageName) ?: return false
        val installedUsers = getPackageInstalledUsers(packageName) ?: return false
        if (userId !in installedUsers) return false

        if (installedUsers.size == 1) {
            // last user: remove the package completely
            deletePackageData(setting)
        } else {
            VActivityManagerService.get().killAppByPkg(packageName, userId)
            setPackageStoppedState(userId, packageName!!, false)
            setting.getOrCreateUserState(userId).installed = false
            notifyPackageRemoved(setting, userId)
            packagePersistenceLayer.save()
            FileUtils.deleteRecursively(VEnvironment.getOrCreateUserSubFile(userId, packageName))
        }
        ComponentStatusMgr.get(userId).removePackage(setting.packageName!!)

        if (userId != 0) {
            val apps = getInstalledAppsAsUser(userId, 0)
            // GUESS: the decompiler lost the body of the loop over the remaining apps (empty ifs, then an
            // unconditional removeUser). Most likely: remove the extra user when nothing but GMS/vendor
            // packages is left in it.
            val onlyGmsOrVendor = apps.isEmpty() || apps.all {
                GmsSupport.isGmsPackage(it.packageName) || SpecialComponentList.VENDOR_PACKAGES.contains(it.packageName)
            }
            if (onlyGmsOrVendor) {
                VUserManagerService.get()?.removeUser(userId)
            }
        }
        return true
    }

    override fun unregisterObserver(observer: IPackageObserver?) {
        try {
            packageObserver.unregister(observer)
        } catch (_: Throwable) {
        }
    }

    override fun upgradePackage(packageName: String?, apkPath: String?, flags: Int): InstallResult =
        installPackage(packageName, apkPath, flags, false)

    fun setPackageStoppedState(i: Int, str: String?, z: Boolean) {
        if (str == null) {
            return
        }
        val str2 = "stopped:$i:$str"
        val sharedPreferences: SharedPreferences =
            appContext.getSharedPreferences("virtual_package_state", 0)
        if (sharedPreferences.getBoolean(str2, false) == z) {
            return
        }
        sharedPreferences.edit(commit = true) {
            if (z) {
                putBoolean(str2, true)
            } else {
                remove(str2)
            }
        }
    }
    @Synchronized
    fun installPackage(packageName: String?, path: String?, flags: Int, notify: Boolean): InstallResult {
        val startTime = System.currentTimeMillis()
        if (path == null) return InstallResult.makeFailure("path = NULL")

        var apkFile = File(path)
        if (!apkFile.exists() || !apkFile.isFile) return InstallResult.makeFailure("Package File is not exist.")

        // ---- parse ----
        // If the app is also installed on the host with split apks and the file sits in setPosition directory, parse that
        // directory so the splits are included (API 26+).
        var parseTarget = apkFile
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val hostInfo = VirtualCore.gCore.unhookPackageManager.getApplicationInfo(packageName!!, 0)
                val splits = hostInfo.splitNames       // assumed: hostInfo.splitSourceDirs
                val parent = apkFile.parentFile
                if (!splits.isNullOrEmpty() && parent != null && parent.isDirectory) parseTarget = parent
            } catch (ignored: Exception) {
            }
        }
        val vPackage: VPackage? = try {
            PackageParserEx.parsePackage(parseTarget)
        } catch (t: Throwable) {
            null
        }
        val pkgName = vPackage?.packageName ?: return InstallResult.makeFailure("Unable to parse the package.")

        Log.d("fu8", "install package: $pkgName path: $path")
        val result = InstallResult()
        result.packageName = pkgName

        // ---- already installed? ----
        val oldPackage = PackageCacheManager.getPackage(pkgName)
        val oldSetting: PackageSetting? = oldPackage?.packageSetting
        if (oldPackage != null) {
            if ((flags and INSTALL_SKIP_IF_EXISTS) != 0) {
                result.isUpdate = true
                result.installed = true
                return result
            }
            val update = when {
                (flags and INSTALL_UPDATE_IF_NEWER) != 0 && oldPackage.versionCode < vPackage.versionCode -> true
                (flags and INSTALL_REJECT_IF_EXISTS) != 0 -> false
                else -> (flags and INSTALL_UPDATE_IF_EXISTS) != 0
            }
            if (!update) return InstallResult.makeFailure("Not allowed to update the package.")
            result.isUpdate = true
        }

        // ---- directories ----
        val appDir = VEnvironment.getOrCreateAppSubFile(pkgName)      // was ou8.skipInt32
        val libDir = File(appDir, "lib")
        if (result.isUpdate) {
            FileUtils.deleteRecursively(libDir)
            VEnvironment.makeDexFile(pkgName).delete()                 // was ou8.e: the old optimized dex
            VActivityManagerService.get().killAppByPkg(pkgName, -1)    // stop it in every user
        }
        if (!libDir.exists() && !libDir.mkdirs()) return InstallResult.makeFailure("Unable to create lib dir.")

        // ---- use the host's apk instead of setPosition copy? ----
        var dependSystem = false
        if ((flags and INSTALL_USE_HOST_APK) != 0) {
            dependSystem = try {
                VirtualCore.gCore.unhookPackageManager.getApplicationInfo(pkgName, 0) != null
            } catch (e: PackageManager.NameNotFoundException)
            {
                false
            }
        }
        // An existing install that did not depend on the host stays independent.
        if (oldSetting != null && !oldSetting.gmsEnabled) dependSystem = false

        NativeLibraryHelperCompat.copyNativeBinaries(File(path), libDir)      // was a05.setPosition: copies the native libs out of the apk into lib/

        if (!dependSystem) {
            val target = File(appDir, "base.apk")
            val targetDir = target.parentFile
            if (targetDir != null && !targetDir.exists()) targetDir.mkdirs()
            if (target.exists()) target.delete()
            try {
                FileUtils.copyFile(apkFile, target)
                apkFile = target
            } catch (e: IOException) {
                target.delete()
                return InstallResult.makeFailure("Unable to copy the package file.")
            }
        }

        if (oldPackage != null) PackageCacheManager.removePackage(pkgName)
        getDirPermission(apkFile)     // was fu8.appendFirstIfNotNull(File): not seen; probably makes the apk read-only (needed for dex loading on API 34+)

        // ---- package setting ----
        val setting = oldSetting ?: PackageSetting().also { it.flags = 4 }
        setting.gmsEnabled = dependSystem
        setting.appPath = apkFile.path
        setting.libPath = libDir.path
        setting.packageName = pkgName
        setting.appId = uidSystem.getUid(vPackage) % VUserHandle.PER_USER_RANGE

        if (result.isUpdate) {
            setting.lastUpdateTime = startTime
        } else {
            setting.firstInstallTime = startTime
            setting.lastUpdateTime = startTime
            for (userId in VUserManagerService.get()!!.getUserIds()) {
                val state = setting.getOrCreateUserState(userId)
                state.launched = false
                state.hidden = false
                state.installed = userId == 0          // setPosition new app is installed for user 0 only
            }
        }

        PackageParserEx.saveVPackage(vPackage)
        PackageCacheManager.addPackage(setting, vPackage)
        packagePersistenceLayer.save()

        // ---- optimize the copied apk ----
        if (!dependSystem)
        {
            val odexPath = VEnvironment.makeDexFile(pkgName).path

            var optimized = false
            if (System.getProperty("java.vm.version")!!.startsWith("2")) {      // ART
                try {
                    ArtDexOptimizer.interpretDex2Oat(setting.appPath!!, odexPath)      // was dv.setPosition: not seen, ahead-of-time compile
                    optimized = true
                } catch (ignored: IOException) {
                }
            }
            if (!optimized) {
                try {
                    DexFile.loadDex(setting.appPath, odexPath, 0).close()
                } catch (ignored: IOException) {
                }
            }
        }

        // ---- side effects ----
        if (SpecialComponentList.GOOGLE_SERVICES_FRAMEWORK_PACKAGE.contains(pkgName))
        {      // was sj7.k (same set as PackageRules.f49578k)
            BroadcastSystem.broadcastSystem?.registerReceivers(vPackage)
        }
        if (notify) notifyAppInstalled(setting, -1)           // was U(setting, -1): send PACKAGE_ADDED etc. to all users

        result.installed = true
        return result
    }

    fun sendBootCompleted() {
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)
        Log.d("fu8", "sendBootCompleted intent to all")
        VUserManagerService.get()?.let {
            for (userId in it.ids) {
                VActivityManagerService.get().sendBroadcastAsUser(intent, VUserHandle(userId))
            }
        }
    }

    @Synchronized
    fun installPackageAsUser(userId: Int, packageName: String, notify: Boolean): Boolean
    {
        if (!VUserManagerService.get()!!.exists(userId)) return false
        val setting = PackageCacheManager.getPackageSetting(packageName) ?: return false
        if (setting.readUserState(userId).installed) return false

        setting.getOrCreateUserState(userId).installed = true
        setPackageStoppedState(userId, packageName, false)
        if (notify) notifyAppInstalled(setting, userId)          // was U / m18958U
        packagePersistenceLayer.save()
        return true
    }




    override fun isAppInstalledAsUser(userId: Int, packageName: String?): Boolean
    {
        if (packageName == null || !VUserManagerService.get()!!.exists(userId)) return false
        val setting = PackageCacheManager.getPackageSetting(packageName) ?: return false
        return setting.readUserState(userId).installed
    }

    @Synchronized  // io.IAppManager.CloneAppManager, io.IAppManager
    override fun installPackageAsUser(i: Int, pkg: String?): Boolean
    {
        var packageSetting: PackageSetting? = null
        if (!VUserManagerService.get()!!.exists(i) || (getPackageSetting(pkg).also
            {
                packageSetting = it
            }) == null || packageSetting!!.readUserState(i).installed)
        {
            return false
        }
        packageSetting.getOrCreateUserState(i).installed = true
        notifyAppInstalled(packageSetting, i)
        this.packagePersistenceLayer.save()
        return true
    }





    //---------------------------------------------------------------------

    inner class AppManagerServiceDeathRecipient (val listener: IAppRequestListener) : DeathRecipient
    {

        override fun binderDied() {
            this.listener.asBinder().unlinkToDeath(this, 0)
            this@VAppManagerService.appRequestListener = null
        }
    }


}
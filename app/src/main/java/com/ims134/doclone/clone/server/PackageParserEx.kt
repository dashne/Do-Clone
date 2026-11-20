package com.ims134.doclone.clone.server



import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ComponentInfo
import android.content.pm.ConfigurationInfo
import android.content.pm.FeatureInfo
import android.content.pm.InstrumentationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.content.pm.ServiceInfo
import android.content.pm.SharedLibraryInfo
import android.content.pm.Signature
import android.content.pm.SigningInfo
import android.content.pm.PackageParser
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.Process
import android.text.TextUtils
import android.util.Log
import androidx.collection.ArrayMap
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.ComponentStatusMgr
import com.ims134.doclone.clone.GmsSupport
import com.ims134.doclone.clone.VEnvironment
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.io.ApplicationInfoL
import com.ims134.doclone.io.ApplicationInfoN
import com.ims134.doclone.io.ApplicationInfoO
import com.ims134.doclone.io.ApplicationInfoP
import com.ims134.doclone.io.ApplicationInfoQ
import com.ims134.doclone.io.ComponentUtils
import com.ims134.doclone.io.PackageCacheManager
import com.ims134.doclone.io.PackageParserMarshmallow
import com.ims134.doclone.io.PackageParserMirror
import com.ims134.doclone.io.PackageParserNougat
import com.ims134.doclone.io.PackageParserPie
import com.ims134.doclone.io.SharedLibraryInfoO
import com.ims134.doclone.io.VMRuntime
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Arrays
import java.util.Collections

/**
 * Parses APKs into [VPackage]s and builds the framework Info objects (PackageInfo, ApplicationInfo, ...)
 * that the virtual PackageManager returns. (was com.polestar.clone.server.pm.parser.setPosition)
 *
 * Flag constants used below (PackageManager.*):
 *   GET_ACTIVITIES=1, GET_RECEIVERS=2, GET_SERVICES=4, GET_PROVIDERS=8, GET_INSTRUMENTATION=16,
 *   GET_META_DATA=128, GET_SHARED_LIBRARY_FILES=1024, GET_URI_PERMISSION_PATTERNS=2048,
 *   GET_CONFIGURATIONS=16384, GET_SIGNATURES=64, MATCH_UNINSTALLED_PACKAGES=8192,
 *   MATCH_DISABLED_COMPONENTS=512, MATCH_DISABLED_UNTIL_USED_COMPONENTS=32768,
 *   GET_SIGNING_CERTIFICATES=0x08000000
 */
object PackageParserEx {

    /** packageName -> shared library files of the host install (was f37862a). */
    @JvmField
    val sharedLibraryCache = ArrayMap<String, Array<String>>()

    private const val FLAG_STOPPED = 0x200000          // ApplicationInfo.FLAG_STOPPED
    private const val FLAG_HAS_CODE = 4
    private const val FLAG_PERSISTENT = 8

    /** Wire version of package.ini. */
    private const val PACKAGE_INI_VERSION = 4

    /** Used when collectCertificates fails, so the package is still installable (setPosition throw-away fake cert). */
    private const val FALLBACK_SIGNATURE = "308203553082023da0030201020204378edaaa300d06092a864886f70d01010b0500305a310d300b0603550406130466616b65310d300b0603550408130466616b65310d300b0603550407130466616b65310d300b060355040a130466616b65310d300b060355040b130466616b65310d300b0603550403130466616b653020170d3138303533303034343434385a180f32313237313230353034343434385a305a310d300b0603550406130466616b65310d300b0603550408130466616b65310d300b0603550407130466616b65310d300b060355040a130466616b65310d300b060355040b130466616b65310d300b0603550403130466616b6530820122300d06092a864886f70d01010105000382010f003082010a0282010100b766ff6afd8a53edd4cee4985bc90e0c515157b5e9f731818961f7250d0d1ac7c7fb80eb5aeb8c28478732e8ff38cff574bfa0eba8039f73af1532f939c4ef9684719efbaba2dd3c583a20907c1c55248a63098c6da23dcfc877763d5fe6061dddd399cf2f49e3250e23f9e687a4d182bcd0662179ba4c9983448e34b4c83e5abbf4f87e87add9157c75fd40de3416744507a3517915f35b6fcad78766e8e1879df8ab823a6ffa335e4790f6e29c87393732025b63ce3a38e42cb0d48cdceb902f191d7d45823db9a0678895e8bfc59b2af7526ca4c2dc3dbe7e70c7c840e666b9629d36e5ddf1d9a80c37f1ab1bc1fb30432914008fbde95d5d3db7853565510203010001a321301f301d0603551d0e04160414d8513e1ae21c64e9ebeee3507e24ea375eef958e300d06092a864886f70d01010b0500038201010088bf20b36428558359536dddcfff16fe233656a92364cb544d8acc43b0859f880a8da339dd430616085edf035e4e6e6dd2281ceb14adde2f05e9ac58d547a09083eece0c6d405289cb7918f85754ee545eefe35e30c103cad617905e94eb4fb68e6920a60d30577855f9feb6e3a664856f74aa9f824aa7d4a3adf85e162c67b9a4261e3185f038ead96112ae3e574d280425e90567352fb82bc9173302122025eaecfabd94d0f9be69a85c415f7cf7759c9651734300952027b316c37aaa1b2418865a3fc7b6bd1072c92ccaacdaa1cf9586d9b8310ceee066ce68859107dfc45ccce729ad9e75b53b584fa37dcd64da8673b1279c6c5861ed3792deac156c8a"

    // ------------------------------------------------------------------
    // Linking
    // ------------------------------------------------------------------

    /** Sets the owner / back-pointers of every component and intent filter (was m16485a). */
    @JvmStatic
    fun linkPackage(pkg: VPackage) {
        for (activity in pkg.activities) {
            activity.owner = pkg
            activity.intents?.forEach { it.activity = activity }
        }
        for (service in pkg.services) {
            service.owner = pkg
            service.intents?.forEach { it.service = service }
        }
        for (receiver in pkg.receivers) {
            receiver.owner = pkg
            receiver.intents?.forEach { it.activity = receiver }
        }
        for (provider in pkg.providers) {
            provider.owner = pkg
            provider.intents?.forEach { it.provider = provider }
        }
        for (instrumentation in pkg.instrumentation) instrumentation.owner = pkg
        for (permission in pkg.permissions) permission.owner = pkg
        for (group in pkg.permissionGroups) group.owner = pkg

        val extra = if (GmsSupport.GMS_DEPENDENCIES.contains(pkg.packageName)) FLAG_HAS_CODE or FLAG_PERSISTENT else FLAG_HAS_CODE
        val appInfo = pkg.applicationInfo!!
        appInfo.flags = appInfo.flags or extra
    }

    // ------------------------------------------------------------------
    // Info generators
    // ------------------------------------------------------------------

    /** Visible to this user? Installed and not hidden, or the caller asked for uninstalled too. (was m16486b) */
    @JvmStatic
    fun isAvailable(state: PackageUserState, flags: Int): Boolean =
        (state.installed && !state.hidden) || (flags and PackageManager.MATCH_UNINSTALLED_PACKAGES) != 0

    /** was m16487c (also used for receivers) */
    @JvmStatic
    fun generateActivityInfo(
        component: VPackage.ActivityComponent?, flags: Int, state: PackageUserState, userId: Int,
    ): ActivityInfo? {
        if (component == null || !isAvailable(state, flags)) return null
        val info = ActivityInfo(component.info!!)
        info.metaData = if ((flags and PackageManager.GET_META_DATA) != 0) component.metaData else null
        info.applicationInfo = generateApplicationInfo(component.owner, flags, state, userId)
        return info
    }

    /** was m16488d */
    @JvmStatic
    fun generateApplicationInfo(pkg: VPackage?, flags: Int, state: PackageUserState, userId: Int): ApplicationInfo? {
        if (pkg == null || !isAvailable(state, flags)) return null
        val appInfo = ApplicationInfo(pkg.applicationInfo!!)
        val appManager: VAppManagerService? = VAppManagerService.get()
        if (appManager == null || !appManager.isPackageStopped(userId, pkg.packageName)) {
            appInfo.flags = appInfo.flags and FLAG_STOPPED.inv()
        } else {
            appInfo.flags = appInfo.flags or FLAG_STOPPED
        }
        if ((flags and PackageManager.GET_META_DATA) != 0) {
            appInfo.metaData = pkg.bundle
        }
        try {
            fillApplicationInfoForUser(appInfo, userId)
        } catch (ignored: Exception) {
        }
        return appInfo
    }

    /**
     * was m16489e
     * @param flags PackageManager.GET_* flags
     */
    @JvmStatic
    fun generatePackageInfo(
        pkg: VPackage, flags: Int, firstInstallTime: Long, lastUpdateTime: Long, state: PackageUserState, userId: Int,
    ): PackageInfo? {
        if (!isAvailable(state, flags)) return null

        if (pkg.signatures == null) loadSignatures(pkg)

        val packageInfo = PackageInfo()
        packageInfo.packageName = pkg.packageName!!
        packageInfo.versionCode = pkg.versionCode
        packageInfo.versionName = pkg.versionName
        packageInfo.sharedUserId = pkg.sharedUserId
        packageInfo.sharedUserLabel = pkg.sharedUserLabel

        var appInfo = generateApplicationInfo(pkg, flags, state, userId)
        packageInfo.applicationInfo = appInfo

        // For component lists the original hands out setPosition stripped copy of the ApplicationInfo (no split info).
        // packageInfo.applicationInfo itself keeps the unstripped one.
        if ((flags and 15) != 0 && appInfo != null) {
            val stripped = ApplicationInfo(appInfo)
            stripped.splitSourceDirs = null
            stripped.splitPublicSourceDirs = null
            val sdk = Build.VERSION.SDK_INT
            if (sdk >= 26) stripped.splitNames = null
            stripped.sharedLibraryFiles = null
            ApplicationInfoL.splitPublicSourceDirs?.let { it[stripped] = null }
            ApplicationInfoL.splitSourceDirs?.let { it[stripped] = null }
            if (sdk >= 26) ApplicationInfoO.splitDependencies?.set(stripped, null)
            if (sdk >= 28) ApplicationInfoP.splitClassLoaderNames?.set(stripped, null)
            if (sdk >= 31) ApplicationInfoQ.sharedLibraryInfos?.set(stripped, null)
            appInfo = stripped
        }

        packageInfo.firstInstallTime = firstInstallTime
        packageInfo.lastUpdateTime = lastUpdateTime

        pkg.requestedPermissions?.let {
            if (it.isNotEmpty()) packageInfo.requestedPermissions = it.toTypedArray()
        }

        if ((flags and 256) != 0) {                      // GET_GIDS
            packageInfo.gids = IntArray(0)               // was hg5.f42776a (assumed: empty int[])
        }

        if ((flags and PackageManager.GET_CONFIGURATIONS) != 0) {
            pkg.configInfoArray?.takeIf { it.isNotEmpty() }?.let {
                packageInfo.configPreferences = it.toTypedArray<ConfigurationInfo>()
            }
            pkg.featureInfoArray?.takeIf { it.isNotEmpty() }?.let {
                packageInfo.reqFeatures = it.toTypedArray<FeatureInfo>()
            }
        }

        if ((flags and PackageManager.GET_ACTIVITIES) != 0 && pkg.activities.isNotEmpty()) {
            packageInfo.activities = Array(pkg.activities.size) { copyActivityInfo(pkg.activities[it], flags, appInfo) }
        }
        if ((flags and PackageManager.GET_RECEIVERS) != 0 && pkg.receivers.isNotEmpty()) {
            packageInfo.receivers = Array(pkg.receivers.size) { copyActivityInfo(pkg.receivers[it], flags, appInfo) }
        }
        if ((flags and PackageManager.GET_SERVICES) != 0 && pkg.services.isNotEmpty()) {
            packageInfo.services = Array(pkg.services.size) {
                val component = pkg.services[it]
                ServiceInfo(component.info!!).also { info ->
                    info.metaData = if ((flags and PackageManager.GET_META_DATA) != 0) component.metaData else null
                    info.applicationInfo = appInfo
                }
            }
        }
        if ((flags and PackageManager.GET_PROVIDERS) != 0 && pkg.providers.isNotEmpty()) {
            packageInfo.providers = Array(pkg.providers.size) {
                val component = pkg.providers[it]
                ProviderInfo(component.info!!).also { info ->
                    info.metaData = if ((flags and PackageManager.GET_META_DATA) != 0) component.metaData else null
                    if ((flags and PackageManager.GET_URI_PERMISSION_PATTERNS) == 0) info.uriPermissionPatterns = null
                    info.applicationInfo = appInfo
                }
            }
        }
        if ((flags and PackageManager.GET_INSTRUMENTATION) != 0 && pkg.instrumentation.isNotEmpty()) {
            // Original quirk: only copied (and given metaData) with GET_META_DATA; otherwise the shared info object is handed out.
            packageInfo.instrumentation = arrayOfNulls<InstrumentationInfo>(pkg.instrumentation.size).also { out ->
                for (i in out.indices) {
                    val component = pkg.instrumentation[i]
                    var info = component?.info
                    if ((flags and PackageManager.GET_META_DATA) != 0 && info != null) {
                        info = InstrumentationInfo(info).also { it.metaData = component.metaData }
                    }
                    out[i] = info
                }
            } as Array<InstrumentationInfo>
        }

        val wantSigningInfo = BuildCompat.sdk28OrLater() && (flags and 0x08000000) != 0
        if ((flags and PackageManager.GET_SIGNATURES) != 0 || wantSigningInfo) {
            val sigs = pkg.signatures
            if (sigs != null && sigs.isNotEmpty()) packageInfo.signatures = sigs.copyOf()
        }
        if (wantSigningInfo) packageInfo.signingInfo = pkg.signatureInfo
        packageInfo.signatures?.firstOrNull()?.let {
            val sha = java.security.MessageDigest.getInstance("SHA-1").digest(it.toByteArray())
            android.util.Log.e("SIGCHK", "pkg=${pkg.packageName} sha1=" + sha.joinToString("") { b -> "%02x".format(b) }.take(12))
        }
        return packageInfo
    }

    private fun copyActivityInfo(component: VPackage.ActivityComponent, flags: Int, appInfo: ApplicationInfo?): ActivityInfo =
        ActivityInfo(component.info!!).also {
            it.metaData = if ((flags and PackageManager.GET_META_DATA) != 0) component.metaData else null
            it.applicationInfo = appInfo
        }

    /** Reads signature.ini (typed Signature[] then, on API 28+, setPosition SigningInfo) into [pkg]. Errors are ignored. */
    private fun loadSignatures(pkg: VPackage) {
        val file = File(VEnvironment.getOrCreateAppSubFile(pkg.packageName!!), "signature.ini")
        if (!file.exists()) return
        val parcel = Parcel.obtain()
        try {
            val bytes = file.readBytes()
            parcel.unmarshall(bytes, 0, bytes.size)
            parcel.setDataPosition(0)
            pkg.signatures = parcel.createTypedArray(Signature.CREATOR)
            if (BuildCompat.sdk28OrLater()) {
                pkg.signatureInfo = parcel.readParcelable<Parcelable>(Bundle::class.java.classLoader) as SigningInfo?
            }
        } catch (ignored: Exception) {
        } finally {
            parcel.recycle()
        }
    }

    /** was m16490f */
    @JvmStatic
    fun generateProviderInfo(
        component: VPackage.ProviderComponent?, flags: Int, state: PackageUserState, userId: Int,
    ): ProviderInfo? {
        if (component == null || !isAvailable(state, flags)) return null
        val info = ProviderInfo(component.info!!)
        info.metaData = if ((flags and PackageManager.GET_META_DATA) != 0) component.metaData else null
        if ((flags and PackageManager.GET_URI_PERMISSION_PATTERNS) == 0) info.uriPermissionPatterns = null
        info.applicationInfo = generateApplicationInfo(component.owner, flags, state, userId)
        return info
    }

    /** was m16491g */
    @JvmStatic
    fun generateServiceInfo(
        component: VPackage.ServiceComponent?, flags: Int, state: PackageUserState, userId: Int,
    ): ServiceInfo? {
        if (component == null || !isAvailable(state, flags)) return null
        val info = ServiceInfo(component.info!!)
        info.metaData = if ((flags and PackageManager.GET_META_DATA) != 0) component.metaData else null
        info.applicationInfo = generateApplicationInfo(component.owner, flags, state, userId)
        return info
    }

    // ------------------------------------------------------------------
    // ApplicationInfo fix-ups
    // ------------------------------------------------------------------

    /**
     * Points [appInfo] at the virtual install (apk, data dirs, native libs, split info) for [userId].
     * Throws IllegalStateException if the package is unknown. (was m16492h)
     */
    @JvmStatic
    fun fillApplicationInfoForUser(appInfo: ApplicationInfo, userId: Int) {
        val setting = PackageCacheManager.getPackageSetting(appInfo.packageName) ?: throw IllegalStateException()
        val apkPath = setting.appPath
        appInfo.publicSourceDir = apkPath
        appInfo.sourceDir = apkPath
        appInfo.dataDir = VEnvironment.getOrCreateUserSubFile(userId, appInfo.packageName).path

        val core = VirtualCore.gCore
        val hostInfo: ApplicationInfo? = try {
            core.unhookPackageManager.getApplicationInfo(appInfo.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }

        // setting.dynamic: the app is also installed on the host, so its native libs / splits can be borrowed.
        if (setting.gmsEnabled && hostInfo != null) {
            var nativeDir: String? = null
            if (core.hostPackageName!!.endsWith("arm64") || Process.is64Bit()) {
                nativeDir = hostInfo.nativeLibraryDir
                ApplicationInfoL.primaryCpuAbi?.set(appInfo, "arm64-v8a")
            } else {
                try {
                    val primary = ApplicationInfoL.primaryCpuAbi?.get(hostInfo)
                    val secondary = ApplicationInfoL.secondaryCpuAbi?.get(hostInfo)
                    if (primary != null) {
                        if (!(VMRuntime.is64BitAbi?.call(primary) as Boolean)) {
                            nativeDir = hostInfo.nativeLibraryDir
                        } else if (secondary != null && !(VMRuntime.is64BitAbi?.call(secondary) as Boolean)) {
                            nativeDir = ApplicationInfoL.secondaryNativeLibraryDir?.get(hostInfo) as String?
                        }
                    }
                } catch (ignored: Throwable) {
                }
            }
            if (nativeDir != null) appInfo.nativeLibraryDir = nativeDir
            if (Build.VERSION.SDK_INT >= 26) appInfo.category = hostInfo.category
        }

        val sdk = Build.VERSION.SDK_INT
        val scanDir = File(apkPath!!).parent
        ApplicationInfoL.scanSourceDir?.set(appInfo, scanDir)
        ApplicationInfoL.scanPublicSourceDir?.set(appInfo, scanDir)

        if (setting.gmsEnabled && hostInfo != null) {
            ApplicationInfoL.splitPublicSourceDirs?.set(appInfo, hostInfo.splitPublicSourceDirs)
            ApplicationInfoL.splitSourceDirs?.set(appInfo, hostInfo.splitSourceDirs)
            appInfo.splitPublicSourceDirs = hostInfo.splitPublicSourceDirs
            appInfo.splitSourceDirs = hostInfo.splitSourceDirs
            if (sdk >= 26) {
                appInfo.splitNames = hostInfo.splitNames
                // (original also did setPosition no-op null-check of C9881vr.privateFlags.get(appInfo) here)
                ApplicationInfoO.splitDependencies?.let { ref ->
                    ref.get(hostInfo)?.let { ref.set(appInfo, it) }
                }
                ApplicationInfoP.splitClassLoaderNames?.let { ref ->
                    ref.get(hostInfo)?.let { ref.set(appInfo, it) }
                }
            }
        }

        // Device-encrypted data dir: legacy (flags < 4) packages that have no DE dir yet keep using the CE dir.
        val deDir = VEnvironment.userDeDir
        val deAppDir = File(File(deDir, userId.toString()), appInfo.packageName)
        val dePath: String
        if (setting.flags < 4 && !deAppDir.exists()) {
            dePath = appInfo.dataDir
        } else {
            val dir = File(File(deDir, userId.toString()), appInfo.packageName)
            VEnvironment.getOrCreatePath(dir)
            dePath = dir.path
        }
        ApplicationInfoN.deviceEncryptedDataDir?.set(appInfo, dePath)
        ApplicationInfoN.credentialEncryptedDataDir?.set(appInfo, appInfo.dataDir)
        ApplicationInfoN.deviceProtectedDataDir?.set(appInfo, dePath)
        ApplicationInfoN.credentialProtectedDataDir?.set(appInfo, appInfo.dataDir)
    }

    /**
     * One-time setup of the parsed [VPackage]'s ApplicationInfo for its [PackageSetting] (was m16493i).
     */
    @JvmStatic
    fun initApplicationInfo(setting: PackageSetting, pkg: VPackage) {
        val appInfo = pkg.applicationInfo!!
        if (TextUtils.isEmpty(appInfo.processName)) appInfo.processName = appInfo.packageName
        appInfo.enabled = true
        appInfo.nativeLibraryDir = setting.libPath
        appInfo.uid = setting.appId

        // ".Foo" class names are made absolute.
        val name = appInfo.name
        appInfo.name = when {
            name == null -> null
            name[0] == '.' -> setting.packageName + name
            else -> name
        }

        ApplicationInfoL.scanSourceDir?.set(appInfo, appInfo.dataDir)
        ApplicationInfoL.scanPublicSourceDir?.set(appInfo, appInfo.dataDir)
        val core = VirtualCore.gCore
        ApplicationInfoL.primaryCpuAbi?.set(appInfo, ApplicationInfoL.primaryCpuAbi?.get(appContext.applicationInfo))

        if (!setting.gmsEnabled) return

        val packageName = setting.packageName
        if (GmsSupport.isGmsRelatedPackage(packageName)) {
            try {
                appInfo.flags = (core.unhookPackageManager.getApplicationInfo(packageName!!, 0).flags and 129) or
                        (appInfo.flags and -130)
            } catch (ignored: Exception) {
            }
        }

        var libs: Array<String>? = sharedLibraryCache[packageName]
        if (libs == null) {
            try {
                val hostInfo = core.unhookPackageManager.getApplicationInfo(packageName!!, PackageManager.GET_SHARED_LIBRARY_FILES)
                libs = hostInfo.sharedLibraryFiles ?: emptyArray()
                val withLegacy = ArrayList<String>(libs.asList())
                val usesLegacyHttp = withLegacy.contains("org.apache.http.legacy") ||
                        pkg.usesLibraries?.contains("org.apache.http.legacy") == true
                if ((Build.VERSION.SDK_INT >= 28 && hostInfo.targetSdkVersion <= 28) || usesLegacyHttp) {
                    var jar = "/system/framework/org.apache.http.legacy.boot.jar"
                    if (!File(jar).exists()) jar = "/system/framework/org.apache.http.legacy.jar"
                    if (!withLegacy.contains(jar)) withLegacy.add(jar)
                }
                sharedLibraryCache[packageName] = withLegacy.toTypedArray()
                // NOTE (as in the original): on this first pass `libs` is the host's list WITHOUT the legacy jar;
                // only later calls (cache hit) get the augmented array.
            } catch (ignored: PackageManager.NameNotFoundException) {
            }
        }
        appInfo.sharedLibraryFiles = libs

        if (BuildCompat.sdk31OrLater() && libs != null && libs.isNotEmpty()) {
            val hostLibraries = core.unhookPackageManager.getSharedLibraries(131072)   // MATCH_UNINSTALLED_PACKAGES-ish flag
            // The original read `list != null || list.isEmpty()`, which would NPE on null; here null just skips.
            if (hostLibraries != null) {
                val matched = ArrayList<Any?>()
                for (file in appInfo.sharedLibraryFiles) {
                    for (library in hostLibraries) {
                        val info = library as SharedLibraryInfo
                        val codePaths = SharedLibraryInfoO.getAllCodePaths?.call(info) as List<String>?
                        if (codePaths != null && codePaths.contains(file)) matched.add(info)
                    }
                }
                if (matched.isNotEmpty()) {
                    val existing = ApplicationInfoQ.sharedLibraryInfos?.get(appInfo) as List<Any?>?
                    if (existing != null && existing.isNotEmpty()) matched.addAll(existing)
                    ApplicationInfoQ.sharedLibraryInfos?.set(appInfo, matched)
                }
            }
        }
    }

    /** Should the component be returned for these flags / user? (was m16494j) */
    @JvmStatic
    fun isComponentEnabled(info: ComponentInfo, flags: Int, userId: Int): Boolean {
        if ((flags and PackageManager.MATCH_DISABLED_COMPONENTS) != 0) return true
        val component: ComponentName = ComponentUtils.createComponentName(info)
        val state = ComponentStatusMgr.get(userId).prefs.getInt(ComponentStatusMgr.UserStatus.componentKey(component), 0)
        return when (state) {
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> info.enabled
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER -> false
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED ->
                (flags and PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS) != 0
            else -> true      // ENABLED and anything unknown
        }
    }

    // ------------------------------------------------------------------
    // Parsing
    // ------------------------------------------------------------------

    /**
     * Parses [file] with the framework's PackageParser (via reflection, per API level) and converts the result.
     * Was m16495k. jadx marked this method "Code duplicated / type inference failed"; the control flow below
     * is rebuilt by hand, so test it with real APKs.
     */
//    @JvmStatic
//    @Throws(Throwable::class)
//    fun parsePackage(file: File): VPackage
//    {
//        runCatching {
//            val fw = Class.forName("android.content.pm.PackageParser")
//            Log.d("ParsePackage", "fw methods: " + fw.declaredMethods.joinToString { m ->
//                m.name + m.parameterTypes.joinToString("(", ",", ")") { it.simpleName } })
//        }
//        for (n in listOf("android.content.pm.parsing.ParsingPackageUtils",
//            "com.android.internal.pm.pkg.parsing.ParsingPackageUtils")) {
//            val c = runCatching { Class.forName(n) }.getOrNull()
//            Log.d("ParsePackage", "$n -> " + (c?.declaredMethods?.joinToString { it.name } ?: "ABSENT"))
//        }
//        //--------------
//        var parser: PackageParser? = PackageParserMarshmallow.ctor?.newInstance()
//        if (parser == null) {
//            parser = try {
//                PackageParser::class.java.getDeclaredConstructor().also { it.isAccessible = true }.newInstance()
//            } catch (t: Throwable) {
//                Log.e("ParsePackage", "createParser reflection fallback failed: $t")
//                null
//            }
//        }
//        if (BuildCompat.sdk29OrLater() && PackageParserPie.setCallback != null && PackageParserPie.CallbackImpl.ctor != null) {
//            PackageParserPie.setCallback?.call(parser, PackageParserPie.CallbackImpl.ctor?.newInstance(VirtualCore.getPackageManager()))
//        }
//
//        val parsed: PackageParser.Package
//        val parseRef = PackageParserMarshmallow.parsePackage
//        if (parseRef != null) {
//            parsed = parseRef.callWithException(parser, file, 0)!!
//        } else {
//            // Fallback: find any parsePackage(File, int, ...) and fill the other parameters with defaults.
//            val method = parser!!.javaClass.declaredMethods.firstOrNull {
//                it.name == "parsePackage" && it.parameterTypes.size >= 2 &&
//                        it.parameterTypes[0] == File::class.java && it.parameterTypes[1] == Int::class.javaPrimitiveType
//            } ?: throw NoSuchMethodException("No compatible parsePackage(File, int, ...) on API ${Build.VERSION.SDK_INT}")
//            method.isAccessible = true
//            val args = arrayOfNulls<Any>(method.parameterTypes.size)
//            for ((i, type) in method.parameterTypes.withIndex()) {
//                args[i] = when {
//                    i == 0 && type == File::class.java -> file
//                    i == 1 && type == Int::class.javaPrimitiveType -> 0
//                    type == java.lang.Boolean.TYPE -> false
//                    type == Int::class.javaPrimitiveType -> 0
//                    else -> null
//                }
//            }
//            parsed = method.invoke(parser, *args) as PackageParser.Package
//        }
//
//        // ---- signatures ----
//        val meta = parsed.mAppMetaData
//        if (parsed.requestedPermissions.contains("android.permission.FAKE_PACKAGE_SIGNATURE") &&
//            meta != null && meta.containsKey("fake-signature")
//        ) {
//            parsed.mSignatures = arrayOf(Signature(meta.getString("fake-signature")))
//            Log.d("setPosition", "Using fake-signature feature on : ${parsed.packageName}")
//        } else {
//            try {
//                val sdk = Build.VERSION.SDK_INT      // was hg5.f42777b (assumed SDK_INT)
//                when {
//                    sdk >= 28 -> PackageParserPie.collectCertificates?.callWithException(parsed, true)
//                    sdk >= 24 -> PackageParserNougat.collectCertificates?.callWithException(parsed, 65)
//                    else -> PackageParserMarshmallow.collectCertificates?.callWithException(parser, parsed, 1)
//                }
//            } catch (t: Throwable) {
//                Log.e("setPosition", "collectCertificates failed", t)
//                parsed.mSignatures = arrayOf(Signature(FALLBACK_SIGNATURE))
//            }
//        }
//
//        // ---- components ----
//        val pkg = VPackage()
//        for (activity in parsed.activities) pkg.activities.add(VPackage.ActivityComponent(activity))
//
//        for (service in parsed.services) {
//            // Google's phenotype sync service crashes inside the clone, so it is dropped.
//            if (service.getComponentName()?.className == "com.google.android.gms.phenotype.service.sync.PackageUpdateTaskService") continue
//            val component = VPackage.ServiceComponent(service)
//            service.intents?.let { source ->
//                component.intents = ArrayList<VPackage.ServiceIntentInfo>(source.size).also { list ->
//                    for (item in source) list.add(VPackage.ServiceIntentInfo(item as PackageParser.IntentInfo))
//                }
//            }
//            component.info = service.info
//            pkg.services.add(component)
//        }
//
//        for (receiver in parsed.receivers) pkg.receivers.add(VPackage.ActivityComponent(receiver))
//
//        for (provider in parsed.providers) {
//            val component = VPackage.ProviderComponent(provider)
//            provider.intents?.let { source ->
//                component.intents = ArrayList<VPackage.ProviderIntentInfo>(source.size).also { list ->
//                    for (item in source) list.add(VPackage.ProviderIntentInfo(item as PackageParser.IntentInfo))
//                }
//            }
//            component.info = provider.info
//            pkg.providers.add(component)
//        }
//
//        for (instrumentation in parsed.instrumentation) {
//            val component = VPackage.InstrumentationComponent(instrumentation)
//            component.info = instrumentation.info
//            pkg.instrumentation.add(component)
//        }
//
//        pkg.requestedPermissions = ArrayList(parsed.requestedPermissions)
//
//        val protectedRef = PackageParserMirror.Package.protectedBroadcasts
//        if (protectedRef != null) {
//            val broadcasts = protectedRef.get(parsed)
//            if (broadcasts != null) {
//                val clean = broadcasts.filterNotNull()
//                // Original copies the list and then adds the same entries again (duplicates). Kept as is.
//                pkg.protectedBroadcasts = ArrayList(clean).also { it.addAll(clean) }
//            }
//        }
//
//        pkg.applicationInfo = parsed.applicationInfo
//
//        // ---- signing ----
//        var signatures: Array<Signature>? = parsed.mSignatures
//        if (BuildCompat.sdk28OrLater()) {
//            try {
//                signatures = parsed.mSigningDetails?.signatures ?: parsed.mSignatures
//            } catch (ignored: Throwable) {
//            }
//        }
//        pkg.signatures = signatures
//
//        var signingInfo: SigningInfo? = null
//        if (BuildCompat.sdk28OrLater()) {
//            try {
//                if (PackageParserMirror.Package.mSigningDetails != null && PackageParserMirror.SigningDetails.ctor != null) {
//                    signingInfo = (PackageParserMirror.SigningDetails.ctor?.newInstance(PackageParserMirror.Package.mSigningDetails?.get(parsed))) as SigningInfo
//                } else if (Build.VERSION.SDK_INT >= 28 || signatures == null || signatures.isEmpty()) {
//                    signingInfo = null
//                } else {
//                    // Dead on real API 28+ devices; kept from the original.
//                    try {
//                        val ctor = SigningInfo::class.java.getDeclaredConstructor(
//                            Int::class.javaPrimitiveType, Collection::class.java, Collection::class.java, Collection::class.java
//                        )
//                        ctor.isAccessible = true
//                        val none = emptyList<Any>()
//                        signingInfo = (ctor.newInstance(0, signatures.asList(), none, none)) as SigningInfo
//                    } catch (ignored: Throwable) {
//                        signingInfo = null
//                    }
//                }
//            } catch (ignored: Throwable) {
//            }
//        }
//        pkg.signatureInfo = signingInfo
//
//        pkg.bundle = parsed.mAppMetaData
//        pkg.packageName = parsed.packageName
//        pkg.preferredOrder = parsed.mPreferredOrder
//        pkg.versionName = parsed.mVersionName
//        pkg.sharedUserId = parsed.mSharedUserId
//        pkg.sharedUserLabel = parsed.mSharedUserLabel
//        pkg.usesLibraries = parsed.usesLibraries
//        pkg.versionCode = parsed.mVersionCode
//        pkg.configInfoArray = parsed.configPreferences
//        pkg.featureInfoArray = parsed.reqFeatures
//
//        linkPackage(pkg)
//        return pkg
//    }

    @JvmStatic
    @Throws(Throwable::class)
    fun parsePackage(file: File): VPackage {
        // 1. Create PackageParser instance
        var packageParserNewInstance: PackageParser? = try {
            PackageParserMarshmallow.ctor?.newInstance()
        } catch (t: Throwable) {
            null
        }

        if (packageParserNewInstance == null) {
            try {
                val clazz = Class.forName("android.content.pm.PackageParser")
                val ctor = clazz.getDeclaredConstructor()
                ctor.isAccessible = true
                packageParserNewInstance = ctor.newInstance() as PackageParser
            } catch (th: Throwable) {
                Log.e("ParsePackage", "createParser reflection fallback failed: $th")
            }
        }

        if (packageParserNewInstance == null) {
            throw IllegalStateException("Failed to create PackageParser instance")
        }

        // 2. Set Callback (API 28+)
        if (BuildCompat.sdk28OrLater() && PackageParserPie.setCallback != null && PackageParserPie.CallbackImpl.ctor != null) {
            try {
                val callback = PackageParserPie.CallbackImpl.ctor?.newInstance(VirtualCore.getPackageManager())
                PackageParserPie.setCallback?.call(packageParserNewInstance, callback)
            } catch (t: Throwable) {
                Log.e("ParsePackage", "setCallback failed: $t")
            }
        }

        // 3. Parse Package
        val refMethod = PackageParserMarshmallow.parsePackage
        var packageCallWithException: PackageParser.Package? = null

        if (refMethod != null) {
            try {
                packageCallWithException = refMethod.callWithException(packageParserNewInstance, file, 0)
            } catch (t: Throwable) {
                Log.e("ParsePackage", "mirror parsePackage failed, falling back to reflection: $t")
                packageCallWithException = null
            }
        }

        if (packageCallWithException == null) {
            val methods = packageParserNewInstance.javaClass.declaredMethods
            var target: java.lang.reflect.Method? = null

            // Try exact match (File, int) for older APIs
            for (m in methods) {
                if (m.name == "parsePackage") {
                    val params = m.parameterTypes
                    if (params.size >= 2 && params[0] == File::class.java && params[1] == Int::class.javaPrimitiveType) {
                        target = m
                        break
                    }
                }
            }

            // Fallback for API 31+ : find any parsePackage starting with File
            if (target == null) {
                for (m in methods) {
                    if (m.name == "parsePackage") {
                        val params = m.parameterTypes
                        if (params.isNotEmpty() && params[0] == File::class.java) {
                            target = m
                            break
                        }
                    }
                }
            }

            if (target == null) {
                throw NoSuchMethodException("No compatible parsePackage(File, ...) on API ${Build.VERSION.SDK_INT}")
            }

            target.isAccessible = true
            val params = target.parameterTypes
            val args = arrayOfNulls<Any>(params.size)
            for (i in params.indices) {
                val cls = params[i]
                args[i] = when {
                    i == 0 && cls == File::class.java -> file
                    cls == Int::class.javaPrimitiveType -> 0
                    cls == Boolean::class.javaPrimitiveType -> false
                    cls == Long::class.javaPrimitiveType -> 0L
                    else -> null
                }
            }
            packageCallWithException = target.invoke(packageParserNewInstance, *args) as PackageParser.Package
        }

        if (packageCallWithException == null) {
            throw IllegalStateException("parsePackage returned null for ${file.path}")
        }

        // 4. Fake Signature & Collect Certificates
        if (packageCallWithException.requestedPermissions.contains("android.permission.FAKE_PACKAGE_SIGNATURE") &&
            packageCallWithException.mAppMetaData != null &&
            packageCallWithException.mAppMetaData!!.containsKey("fake-signature")
        ) {
            packageCallWithException.mSignatures = arrayOf(Signature(packageCallWithException.mAppMetaData?.getString("fake-signature")))
            Log.d("a", "Using fake-signature feature on : " + packageCallWithException.packageName)
        } else {
            try {
                val sdkInt = Build.VERSION.SDK_INT
                if (sdkInt >= 28) {
                    PackageParserPie.collectCertificates?.callWithException(packageCallWithException, true)
                } else if (sdkInt >= 24) {
                    PackageParserNougat.collectCertificates?.callWithException(packageCallWithException, 65)
                } else {
                    PackageParserMarshmallow.collectCertificates?.callWithException(packageParserNewInstance, packageCallWithException, 1)
                }
            } catch (th2: Throwable) {
                Log.d("a", "collectCertificates failed", th2)
                packageCallWithException.mSignatures = arrayOf(
                    Signature(
                        "308203553082023da0030201020204378edaaa300d06092a864886f70d01010b0500305a310d300b0603550406130466616b65310d300b0603550408130466616b65310d300b0603550407130466616b65310d300b060355040a130466616b65310d300b060355040b130466616b65310d300b0603550403130466616b653020170d3138303533303034343434385a180f32313237313230353034343434385a305a310d300b0603550406130466616b65310d300b0603550408130466616b65310d300b0603550407130466616b65310d300b060355040a130466616b65310d300b060355040b130466616b65310d300b0603550403130466616b6530820122300d06092a864886f70d01010105000382010f003082010a0282010100b766ff6afd8a53edd4cee4985bc90e0c515157b5e9f731818961f7250d0d1ac7c7fb80eb5aeb8c28478732e8ff38cff574bfa0eba8039f73af1532f939c4ef9684719efbaba2dd3c583a20907c1c55248a63098c6da23dcfc877763d5fe6061dddd399cf2f49e3250e23f9e687a4d182bcd0662179ba4c9983448e34b4c83e5abbf4f87e87add9157c75fd40de3416744507a3517915f35b6fcad78766e8e1879df8ab823a6ffa335e4790f6e29c87393732025b63ce3a38e42cb0d48cdceb902f191d7d45823db9a0678895e8bfc59b2af7526ca4c2dc3dbe7e70c7c840e666b9629d36e5ddf1d9a80c37f1ab1bc1fb30432914008fbde95d5d3db7853565510203010001a321301f301d0603551d0e04160414d8513e1ae21c64e9ebeee3507e24ea375eef958e300d06092a864886f70d01010b0500038201010088bf20b36428558359536dddcfff16fe233656a92364cb544d8acc43b0859f880a8da339dd430616085edf035e4e6e6dd2281ceb14adde2f05e9ac58d547a09083eece0c6d405289cb7918f85754ee545eefe35e30c103cad617905e94eb4fb68e6920a60d30577855f9feb6e3a664856f74aa9f824aa7d4a3adf85e162c67b9a4261e3185f038ead96112ae3e574d280425e90567352fb82bc9173302122025eaecfabd94d0f9be69a85c415f7cf7759c9651734300952027b316c37aaa1b2418865a3fc7b6bd1072c92ccaacdaa1cf9586d9b8310ceee066ce68859107dfc45ccce729ad9e75b53b584fa37dcd64da8673b1279c6c5861ed3792deac156c8a"
                    )
                )
            }
        }

        // 5. Convert to VPackage
        val vPackage = VPackage()
        vPackage.activities = ArrayList(packageCallWithException.activities.size)
        vPackage.services = ArrayList(packageCallWithException.services.size)
        vPackage.receivers = ArrayList(packageCallWithException.receivers.size)
        vPackage.providers = ArrayList(packageCallWithException.providers.size)
        vPackage.instrumentation = ArrayList(packageCallWithException.instrumentation.size)
        vPackage.permissions = ArrayList(packageCallWithException.permissions.size)
        vPackage.permissionGroups = ArrayList(packageCallWithException.permissionGroups.size)

        for (activity in packageCallWithException.activities) {
            vPackage.activities.add(VPackage.ActivityComponent(activity))
        }

        for (service in packageCallWithException.services) {
            // Drop phenotype sync service to prevent crashes
            if (service.className == "com.google.android.gms.phenotype.service.sync.PackageUpdateTaskService") {
                continue
            }
            val serviceComponent = VPackage.ServiceComponent(service)
            if (service.intents != null) {
                serviceComponent.intents = ArrayList(service.intents!!.size)
                for (intent in service.intents) {
                    serviceComponent.intents!!.add(VPackage.ServiceIntentInfo(intent as PackageParser.IntentInfo))
                }
            }
            serviceComponent.info = service.info
            vPackage.services.add(serviceComponent)
        }

        for (receiver in packageCallWithException.receivers) {
            vPackage.receivers.add(VPackage.ActivityComponent(receiver))
        }

        for (provider in packageCallWithException.providers) {
            val providerComponent = VPackage.ProviderComponent(provider)
            if (provider.intents != null) {
                providerComponent.intents = ArrayList(provider.intents!!.size)
                for (intent in provider.intents) {
                    providerComponent.intents!!.add(VPackage.ProviderIntentInfo(intent as PackageParser.IntentInfo))
                }
            }
            providerComponent.info = provider.info
            vPackage.providers.add(providerComponent)
        }

        for (instrumentation in packageCallWithException.instrumentation) {
            val instrumentationComponent = VPackage.InstrumentationComponent(instrumentation)
            instrumentationComponent.info = instrumentation.info
            vPackage.instrumentation.add(instrumentationComponent)
        }

        vPackage.requestedPermissions = ArrayList(packageCallWithException.requestedPermissions.size)
        vPackage.requestedPermissions?.addAll(packageCallWithException.requestedPermissions)

        val protectedBroadcastsRef = PackageParserMirror.Package.protectedBroadcasts
        if (protectedBroadcastsRef != null) {
            val list = protectedBroadcastsRef.get(packageCallWithException)
            if (list != null) {
                val arrayList11 = ArrayList(list.filterNotNull())
                vPackage.protectedBroadcasts = arrayList11
                arrayList11.addAll(list)
            }
        }

        vPackage.applicationInfo = packageCallWithException.applicationInfo

        var signatures: Array<Signature>? = null
        if (BuildCompat.sdk28OrLater()) {
            try {
                signatures = packageCallWithException.mSigningDetails?.signatures
                    ?: packageCallWithException.mSignatures
            } catch (unused: Throwable) {
            }
        } else
        {
            signatures = packageCallWithException.mSignatures
        }
        vPackage.signatures = signatures

        if(signatures == null)
        {
            Log.e("a", "Signatures is null!!")
        }

        var signingInfo: SigningInfo? = null
        if (BuildCompat.sdk28OrLater()) {
            try {
                if (PackageParserMirror.Package.mSigningDetails != null && PackageParserMirror.SigningDetails.ctor != null) {
                    val sd = PackageParserMirror.Package.mSigningDetails!!.get(packageCallWithException)
                    val newInstance = PackageParserMirror.SigningDetails.ctor!!.newInstance(sd)
                    signingInfo = newInstance as SigningInfo
                } else if (Build.VERSION.SDK_INT >= 28 && signatures != null && signatures.isNotEmpty()) {
                    val clazz = SigningInfo::class.java
                    val declaredConstructor2 = clazz.getDeclaredConstructor(
                        Integer.TYPE,
                        Collection::class.java,
                        Collection::class.java,
                        Collection::class.java
                    )
                    declaredConstructor2.isAccessible = true
                    signingInfo = declaredConstructor2.newInstance(
                            0,
                            Arrays.asList(*signatures),
                            Collections.EMPTY_LIST,
                            Collections.EMPTY_LIST
                        ) as SigningInfo

                }
            } catch (unused3: Throwable) {
            }
        }
        vPackage.signatureInfo = signingInfo

        vPackage.bundle = packageCallWithException.mAppMetaData
        vPackage.packageName = packageCallWithException.packageName
        vPackage.preferredOrder = packageCallWithException.mPreferredOrder
        vPackage.versionName = packageCallWithException.mVersionName
        vPackage.sharedUserId = packageCallWithException.mSharedUserId
        vPackage.sharedUserLabel = packageCallWithException.mSharedUserLabel
        vPackage.usesLibraries = packageCallWithException.usesLibraries
        vPackage.versionCode = packageCallWithException.mVersionCode
        vPackage.configInfoArray = packageCallWithException.configPreferences
        vPackage.featureInfoArray = packageCallWithException.reqFeatures

        linkPackage(vPackage)
        return vPackage
    }

    // ------------------------------------------------------------------
    // package.ini persistence
    // ------------------------------------------------------------------

    /** Loads the cached package.ini of [packageName]; null on any error or version mismatch. (was l) */
    @JvmStatic
    fun parseVPackage(packageName: String): VPackage? {
        val parcel = Parcel.obtain()
        try {
            val bytes = File(VEnvironment.getOrCreateAppSubFile(packageName), "package.ini").readBytes()
            parcel.unmarshall(bytes, 0, bytes.size)
            parcel.setDataPosition(0)
            if (parcel.readInt() != PACKAGE_INI_VERSION) throw IllegalStateException("Invalid version.")
            val pkg = VPackage(parcel)
            linkPackage(pkg)
            return pkg
        } catch (e: Exception) {
            return null
        } finally {
            parcel.recycle()
        }
    }

    /** Writes package.ini (and signature.ini if there are signatures). Errors are swallowed. (was m16497m) */
    @JvmStatic
    fun saveVPackage(pkg: VPackage) {
        val packageName = pkg.packageName!!
        val parcel = Parcel.obtain()
        try {
            parcel.writeInt(PACKAGE_INI_VERSION)
            pkg.writeToParcel(parcel, 0)
            File(VEnvironment.getOrCreateAppSubFile(packageName), "package.ini").writeBytes(parcel.marshall())
        } catch (ignored: Exception) {
        } finally {
            parcel.recycle()
        }

        val signatures = pkg.signatures ?: return
        val signatureFile = File(VEnvironment.getOrCreateAppSubFile(packageName), "signature.ini")
        if (signatureFile.exists()) signatureFile.delete()
        val out = Parcel.obtain()
        try {
            out.writeTypedArray(signatures, 0)
            val signingInfo = pkg.signatureInfo
            if (BuildCompat.sdk28OrLater() && signingInfo != null) out.writeParcelable(signingInfo, 0)
            FileOutputStream(signatureFile).use { it.write(out.marshall()) }
        } catch (ignored: IOException) {
        } finally {
            out.recycle()
        }
    }
}
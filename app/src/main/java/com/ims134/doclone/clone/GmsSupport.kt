package com.ims134.doclone.clone


import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.util.Log
import com.ims134.doclone.clone.server.VAppManagerService
import com.ims134.doclone.io.VASettings
import java.util.zip.ZipFile

/**
 * JADX rename of `io.xq2`. GMS support logic: package/service whitelists +
 * install helpers + chimera detection.
 *
 * `VASettings.f39670a` is the runtime switch — every routing predicate
 * short-circuits to `false` when it's off.
 */
object GmsSupport {

    @JvmField
    val GMS_PACKAGES: HashSet<String> = hashSetOf(
        "com.android.vending",
        "com.google.android.play.games",
        "com.google.android.wearable.app",
        "com.google.android.wearable.app.cn"
    )

    @JvmField
    val GMS_SUPPORT_ENABLED: Boolean = true

    @JvmField
    val GMS_DEPENDENCIES: HashSet<String> = hashSetOf(
        "com.google.android.gsf",
        "com.google.android.gms",
        "com.google.android.gsf.login",
        "com.google.android.backuptransport",
        "com.google.android.backup",
        "com.google.android.configupdater",
        "com.google.android.syncadapters.contacts",
        "com.google.android.feedback",
        "com.google.android.onetimeinitializer",
        "com.google.android.partnersetup",
        "com.google.android.setupwizard",
        "com.google.android.syncadapters.calendar",
        "com.google.android.apps.wellbeing",
    )

    @JvmField
    val GMS_SERVICE_CLASSES: HashSet<String> = hashSetOf(
        "com.google.android.gms.phenotype.service.sync.PackageUpdateTaskService",
        "com.google.android.gsf.update.SystemUpdateService",
        "com.google.android.gms.update.SystemUpdateService",
        "com.google.android.gms.usagereporting.service.UsageReportingService",
        "com.google.android.gms.clearcut.uploader.QosUploaderChimeraService",
        "com.google.android.gms.telephonyspam.sync.SpamListSyncTaskService",
        "com.google.android.gms.chimera.container.FileApkIntentOperation\$ExternalFileApkService",
        "com.google.android.gms.clearcut.service.ClearcutApiService",
        "com.google.android.gms.clearcut.service.ClearcutLoggerService",
        "com.google.android.gms.common.stats.GmsCoreStatsService",
        "com.google.android.gms.icing.service.IndexService",
        "com.google.android.gms.auth.folsom.operation.FolsomModuleInitIntentOperation",
        "com.google.android.gms.auth.api.signin.RevocationBoundService",
        "com.google.android.gms.nearby.discovery.service.DiscoveryService",
        "com.google.android.gms.cast.service.CastDeviceControllerService",
        "com.google.android.gms.tapandpay.TokenizationService",
        "com.google.android.gms.advancedprotection.service.AdvancedProtectionSyncService",
    )

    @JvmField
    val GMS_ACTIONS: HashSet<String> = hashSetOf(
        "com.google.android.gms.chimera.container.LOG_LOAD_ATTEMPT",
        "com.google.android.chimera.FileApkManager.DELETE_UNUSED_FILEAPKS",
        "com.google.android.gms.update.INSTALL_UPDATE",
        "com.google.android.gms.common.operation.LOG_CORE_ANALYTICS",
        "com.google.android.gms.libs.gmscorelogger.service.START",
        "com.google.android.gms.games.internal.appshortcuts.service.START",
        "com.google.android.gms.advancedprotection.service.SYNC_SERVICE",
    )

    private val BLACKLISTED_SERVICE_SUBSTRINGS = listOf(
        "InAppBillingService", "PlayBilling", "FirebaseMessaging",
        "GcmService", ".gcm.", "DynamiteModule", "GmsCoreProxy",
    )

    // ---- APK inspection ----------------------------------------------------

    @JvmStatic
    fun apkHasDex(path: String?): Boolean {
        if (path == null) return false
        val hasDex = if (path.contains("/system/app") || path.startsWith("/system/priv-app")) {
            try {
                ZipFile(path).use { it.getEntry("classes.dex") != null }
            } catch (th: Throwable) {
                Log.d("GmsSupport", "Error when find dex for path: $path")
                Log.d("GmsSupport", Log.getStackTraceString(th))
                false
            }
        } else {
            true
        }
        Log.d("GmsSupport", "apk : $path hasDex: $hasDex")
        return hasDex
    }

    // ---- GMS installation --------------------------------------------------

    @JvmStatic
    fun installGmsPackagesForUser(packages: Set<String>, userId: Int) {
        val vams = VAppManagerService.get()
        if(vams == null)
            throw NullPointerException(vams)
        for (pkg in packages) {
            if (vams.isAppInstalledAsUser(userId, pkg)) continue
            val ai: ApplicationInfo = try {
                VirtualCore.gCore.unhookPackageManager.getApplicationInfo(pkg, 0)
            } catch (_: PackageManager.NameNotFoundException) {
                continue
            }
            val sourceDir = ai.sourceDir ?: continue
            if (!apkHasDex(sourceDir)) continue
            if (userId == 0) {
                vams.installPackage(pkg, sourceDir, 32, false)
            } else {
                vams.installPackageAsUser(userId, pkg, false)
            }
        }
    }

    // ---- Package classification -------------------------------------------

    @JvmStatic
    fun isGmsPackage(pkg: String?): Boolean =
        pkg != null && (GMS_DEPENDENCIES.contains(pkg) || GMS_PACKAGES.contains(pkg))

    @JvmStatic
    fun isGmsRelatedPackage(pkg: String?): Boolean {
        if (pkg.isNullOrEmpty()) return false
        return isGmsPackage(pkg) ||
                pkg == "com.google.android.gms" ||
                pkg == "com.google.android.gsf" ||
                pkg == "com.android.vending"
    }

    // ---- Service routing ---------------------------------------------------

    @JvmStatic
    fun isKnownSafeGmsService(serviceClassName: String?, packageName: String?): Boolean {
        if (!VASettings.gmsState || !GMS_SUPPORT_ENABLED) return false
        if (serviceClassName == null) return false
        val packageOk = isGmsRelatedPackage(packageName) ||
                serviceClassName.startsWith("com.google.android.gms.") ||
                serviceClassName.startsWith("com.google.android.gsf.") ||
                serviceClassName.startsWith("com.android.vending.")
        if (!packageOk) return false
        if (BLACKLISTED_SERVICE_SUBSTRINGS.any { it in serviceClassName }) return false
        return GMS_SERVICE_CLASSES.contains(serviceClassName)
    }

    // ---- Chimera special-case ----------------------------------------------

    @JvmStatic
    fun isChimeraBoundServiceStart(intent: Intent?, serviceInfo: ServiceInfo?): Boolean {
        if (!VASettings.gmsState || !GMS_SUPPORT_ENABLED || intent == null) return false

        val component: ComponentName? = intent.component
        val packageName = component?.packageName ?: serviceInfo?.packageName
        val className = component?.className ?: serviceInfo?.name
        if (packageName == null || className == null) return false
        if (intent.action != "com.google.android.chimera.BoundService.START") return false
        if (packageName != "com.google.android.gms") return false

        val data = intent.data
        val dataString = intent.dataString
        val schemeSpecificPart = data?.schemeSpecificPart
        val isChimeraAction =
            (data != null && data.scheme == "chimera-action" && schemeSpecificPart.isNullOrBlank()) ||
                    dataString == "chimera-action:" ||
                    dataString?.trim() == "chimera-action:"

        return when (className) {
            "com.google.android.gms.chimera.GmsApiService" -> isChimeraAction
            "com.google.android.gms.chimera.PersistentApiService" -> dataString == null || isChimeraAction
            "com.google.android.gms.chimera.PersistentDirectBootAwareApiService" ->
                data != null && data.scheme == "chimera-action"
            else -> false
        }
    }
}
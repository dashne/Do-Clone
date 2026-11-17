package com.ims134.doclone.clone

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.content.res.Resources
import android.os.Build
import android.os.ConditionVariable
import android.os.IBinder
import android.os.IBinder.DeathRecipient
import android.os.Process
import android.os.RemoteException
import android.util.Log
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.BuildCompat.sdk28OrLater
import com.ims134.doclone.clone.BuildCompat.sdk29OrLater
import com.ims134.doclone.clone.BuildCompat.sdk30OrLater
import com.ims134.doclone.clone.BuildCompat.sdk31OrLater
import com.ims134.doclone.clone.BuildCompat.sdk33OrLater
import com.ims134.doclone.clone.BuildCompat.sdk34OrLater
import com.ims134.doclone.clone.GmsSupport.isGmsPackage
import com.ims134.doclone.clone.VirtualRuntime.crash
import com.ims134.doclone.clone.server.InstalledAppInfo
import com.ims134.doclone.io.AccessibilityManagerStub
import com.ims134.doclone.io.AccountManagerStub
import com.ims134.doclone.io.ActivityClient.getActivityClientController
import com.ims134.doclone.io.ActivityClient.isAvailable
import com.ims134.doclone.io.ActivityClientControllerStub
import com.ims134.doclone.io.ActivityManagerNative
import com.ims134.doclone.io.ActivityManagerStub
import com.ims134.doclone.io.ActivityThread
import com.ims134.doclone.io.AdvancedProtectionManagerStub
import com.ims134.doclone.io.AlarmManagerStub
import com.ims134.doclone.io.AmTaskMgrMirror
import com.ims134.doclone.io.AmTaskMgrStub
import com.ims134.doclone.io.AppFunctionManagerStub
import com.ims134.doclone.io.AppInstrumentation
import com.ims134.doclone.io.AppIntegrityManagerStub
import com.ims134.doclone.io.AppOpsManagerStub
import com.ims134.doclone.io.AppSearchManagerStub
import com.ims134.doclone.io.AppWidgetManagerStub
import com.ims134.doclone.io.AssetManagerMirror
import com.ims134.doclone.io.AudioManagerStub
import com.ims134.doclone.io.AuthServiceStub
import com.ims134.doclone.io.AutoFillManagerStub
import com.ims134.doclone.io.BackupManagerStub
import com.ims134.doclone.io.BatteryStatsStub
import com.ims134.doclone.io.BluetoothStub
import com.ims134.doclone.io.CameraServiceStub
import com.ims134.doclone.io.ClipboardStub
import com.ims134.doclone.io.CloneApiDelegate
import com.ims134.doclone.io.CloneComponentDelegate
import com.ims134.doclone.io.ComponentDelegate
import com.ims134.doclone.io.ConnectivityStub
import com.ims134.doclone.io.ContentServiceStub
import com.ims134.doclone.io.ContextFixer
import com.ims134.doclone.io.ContextHubServiceStub
import com.ims134.doclone.io.CredentialManagerStub
import com.ims134.doclone.io.CrossProfileAppsStub
import com.ims134.doclone.io.DeviceIdentifiersServiceStub
import com.ims134.doclone.io.DevicePolicyManagerStub
import com.ims134.doclone.io.DisplayStub
import com.ims134.doclone.io.DomainVerificationManagerStub
import com.ims134.doclone.io.DropBoxManagerStub
import com.ims134.doclone.io.FingerprintManagerStub
import com.ims134.doclone.io.GrammaticalInflectionManagerStub
import com.ims134.doclone.io.GraphicsStatsStub
import com.ims134.doclone.io.HCallbackStub
import com.ims134.doclone.io.HwTelephonyStub
import com.ims134.doclone.io.IAutoFillManager
import com.ims134.doclone.io.IDomainVerificationManagerMirror
import com.ims134.doclone.io.IGrammaticalInflectionManager
import com.ims134.doclone.io.IHwTelephony
import com.ims134.doclone.io.IInjector
import com.ims134.doclone.io.IPerfManagerMirror
import com.ims134.doclone.io.IPersistentDataBlockServiceMirror
import com.ims134.doclone.io.ISmsStub
import com.ims134.doclone.io.ISmtOpsServiceMirror
import com.ims134.doclone.io.ISubStub
import com.ims134.doclone.io.ITimeDetectorService
import com.ims134.doclone.io.ITimeZoneDetectorService
import com.ims134.doclone.io.ITrustManagerMiroor
import com.ims134.doclone.io.InputMethodManagerStub
import com.ims134.doclone.io.InvocationStubManager
import com.ims134.doclone.io.JobServiceStub
import com.ims134.doclone.io.LibCoreStub
import com.ims134.doclone.io.LocaleManagerStub
import com.ims134.doclone.io.LocationManagerStub
import com.ims134.doclone.io.LockSettingsStub
import com.ims134.doclone.io.MediaRouterServiceStub
import com.ims134.doclone.io.MmsStub
import com.ims134.doclone.io.MountServiceStub
import com.ims134.doclone.io.NetworkManagementStub
import com.ims134.doclone.io.NetworkScoreManagerStub
import com.ims134.doclone.io.NetworkStatsManagerStub
import com.ims134.doclone.io.NfcAdapterStub
import com.ims134.doclone.io.NotificationManagerStub
import com.ims134.doclone.io.NsdManagerStub
import com.ims134.doclone.io.PackageManagerStub
import com.ims134.doclone.io.PermissionCompat
import com.ims134.doclone.io.PermissionManagerStub
import com.ims134.doclone.io.PersistentDataBlockServiceStub
import com.ims134.doclone.io.PhoneSubInfoStub
import com.ims134.doclone.io.PowerManagerStub
import com.ims134.doclone.io.QPerformanceStub
import com.ims134.doclone.io.RestrictionStub
import com.ims134.doclone.io.RoleManagerStub
import com.ims134.doclone.io.SSClipboardStub
import com.ims134.doclone.io.SafetyCenterManagerStub
import com.ims134.doclone.io.SearchManagerStub
import com.ims134.doclone.io.ServiceManager
import com.ims134.doclone.io.SessionManagerStub
import com.ims134.doclone.io.ShortcutServiceStub
import com.ims134.doclone.io.SliceManagerStub
import com.ims134.doclone.io.SmtOpsManagerStub
import com.ims134.doclone.io.SpecialComponentList
import com.ims134.doclone.io.StatsManagerStub
import com.ims134.doclone.io.StatusBarManagerStub
import com.ims134.doclone.io.StorageStatsStub
import com.ims134.doclone.io.SupervisionManagerStub
import com.ims134.doclone.io.SystemUpdateStub
import com.ims134.doclone.io.TelephonyRegistryStub
import com.ims134.doclone.io.TelephonyStub
import com.ims134.doclone.io.TetheringServiceStub
import com.ims134.doclone.io.TextServiceStub
import com.ims134.doclone.io.TimeDetectorStub
import com.ims134.doclone.io.TimeZoneDetectorStub
import com.ims134.doclone.io.TrustManagerStub
import com.ims134.doclone.io.UiModeManagerStub
import com.ims134.doclone.io.UpdateEngineStub
import com.ims134.doclone.io.UsageStatsManagerStub
import com.ims134.doclone.io.UserManagerStub
import com.ims134.doclone.io.UwbManagerStubMirror
import com.ims134.doclone.io.VibratorManagerStub
import com.ims134.doclone.io.VibratorStub
import com.ims134.doclone.io.WifiManagerStub
import com.ims134.doclone.io.WifiScannerStub
import com.ims134.doclone.io.WindowManagerStub


enum class ProcessType
{
    Client,Server,Main, Child
}

class VirtualCore
{
    companion object {
        val gCore = VirtualCore()
        fun getPackageManager(): PackageManager
        {
            return appContext.packageManager
        }

        // inside VirtualCore (static, smali VirtualCore.b(int, String))
        @JvmStatic
        fun makeCloneName(index: Int, name: String): String =
            if (index == 0) "$name +" else "$name ${index + 1}"

        // smali m.u(int, Intent), static helper in the service class
        @JvmStatic
        fun resolveServiceInfo(userId: Int, intent: Intent): ServiceInfo? {
            val type = intent.type
            try {
                // av8.b is VPackageManager.instance, get() is getService_() (io.e53)
                val info = VPackageManager.instance?.getPackageManager()?.resolveService(intent, type, 0, userId)
                return info?.serviceInfo
            } catch (e: RemoteException) {
                crash(e)                      // smali io.x09.get(RemoteException)

            }
        }
        @JvmStatic
        fun isRestrictedPackage(str: String?): Boolean {
            return str != null && (isGmsPackage(str) || SpecialComponentList.VENDOR_PACKAGES.contains(str))
        }
    }
    var processType: ProcessType? = null
    @Volatile
    var singleton: IAppManager? = null
    var isStarted: Boolean = false

    var mainThread: Any?=null
    lateinit var hostPkgInfo: PackageInfo
    lateinit var unhookPackageManager: PackageManager
    var currentProcessName: String? = null
    var hostPackageName: String? = null
    var mainProcessName: String? = null

    val uid: Int  = Process.myUid()

    private var hostPermissionCache: HashSet<String>? = null

    var cloneComponentDelegate: CloneComponentDelegate? = null


    var cloneApiDelegate: CloneApiDelegate? = null

    var systemPid:Int = 0

    var initLock: ConditionVariable? = ConditionVariable()


    fun isClientProcess(): Boolean { return processType == ProcessType.Client}
    fun isServerProcess(): Boolean { return processType == ProcessType.Server}
    fun isMainProcess(): Boolean { return processType == ProcessType.Main}
    fun isChildProcess(): Boolean { return processType == ProcessType.Child}

    fun startup()
    {
        if(isStarted) return
        val packageManager = appContext.packageManager
        val flags =PackageManager.GET_PERMISSIONS or PackageManager.GET_PROVIDERS
        ServiceManagerNative.providerAuthority = appContext.packageName + ".virtual.service.BinderProvider"
        this.mainThread = ActivityThread.currentActivityThread?.call()
        this.unhookPackageManager = packageManager
        this.hostPkgInfo = packageManager.getPackageInfo( appContext.packageName, flags)
        //---- this call should be after this.mainThread = ActivityThread.currentActivityThread?.call() because it uses it
        detectProcessType()
        val invocationStubManager = InvocationStubManager.instance
        invocationStubManager.javaClass
        check(!InvocationStubManager.initialized) { "InvocationStubManager Has been initialized." }
        if (!isMainProcess()) {
            if (isServerProcess()) {
                invocationStubManager.mInjectors[ActivityManagerStub::class.java] =
                    ActivityManagerStub(ActivityManagerNative.getDefault!!.call(*arrayOfNulls<Any>(0)))
                if (sdk29OrLater()) {
                    invocationStubManager.mInjectors[AmTaskMgrStub::class.java] = AmTaskMgrStub(AmTaskMgrMirror.getService.call(*arrayOfNulls<Any>(0)))
                }
                if (!sdk30OrLater()) {
                    invocationStubManager.mInjectors[PackageManagerStub::class.java] =
                        PackageManagerStub()
                }
            } else if (isClientProcess()) {
                invocationStubManager.mInjectors[LibCoreStub::class.java] = LibCoreStub()
                invocationStubManager.mInjectors[ActivityManagerStub::class.java] =
                    ActivityManagerStub(ActivityManagerNative.getDefault!!.call(*arrayOfNulls<Any>(0)))
                if (sdk29OrLater()) {
                    invocationStubManager.mInjectors[AmTaskMgrStub::class.java] = AmTaskMgrStub(AmTaskMgrMirror.getService.call(*arrayOfNulls<Any>(0)))
                }
                if (sdk31OrLater() && isAvailable() && getActivityClientController() != null) {
                    invocationStubManager.mInjectors[ActivityClientControllerStub::class.java] =
                        ActivityClientControllerStub()
                }
                if (sdk31OrLater() && ITimeDetectorService.Stub.asInterface != null) {
                    invocationStubManager.mInjectors[TimeDetectorStub::class.java] =
                        TimeDetectorStub()
                }
                if (sdk31OrLater() && ITimeZoneDetectorService.Stub.asInterface != null) {
                    invocationStubManager.mInjectors[TimeZoneDetectorStub::class.java] =
                        TimeZoneDetectorStub()
                }
                val i = Build.VERSION.SDK_INT
                if (i >= 35 && IGrammaticalInflectionManager.Stub.asInterface != null) {
                    invocationStubManager.mInjectors[GrammaticalInflectionManagerStub::class.java] =
                        GrammaticalInflectionManagerStub()
                }
                invocationStubManager.mInjectors[PackageManagerStub::class.java] =
                    PackageManagerStub()
                invocationStubManager.addInjector(HCallbackStub.instance)
                invocationStubManager.mInjectors[ISmsStub::class.java] = ISmsStub()
                invocationStubManager.mInjectors[ISubStub::class.java] = ISubStub()
                invocationStubManager.mInjectors[DropBoxManagerStub::class.java] =
                    DropBoxManagerStub()
                invocationStubManager.mInjectors[NotificationManagerStub::class.java] =
                    NotificationManagerStub()

                invocationStubManager.mInjectors[LocationManagerStub::class.java] =
                    LocationManagerStub()
                invocationStubManager.mInjectors[WindowManagerStub::class.java] = WindowManagerStub()
                invocationStubManager.mInjectors[ClipboardStub::class.java] = ClipboardStub()
                invocationStubManager.mInjectors[MountServiceStub::class.java] = MountServiceStub()
                invocationStubManager.mInjectors[BackupManagerStub::class.java] = BackupManagerStub()
                invocationStubManager.mInjectors.put(TelephonyStub::class.java, TelephonyStub())
                if (IHwTelephony.Stub.TYPE != null && HwTelephonyStub.fetchBinder() != null) {
                    invocationStubManager.mInjectors[HwTelephonyStub::class.java] = HwTelephonyStub()
                }
                invocationStubManager.mInjectors[AccessibilityManagerStub::class.java] =
                    AccessibilityManagerStub()
                invocationStubManager.mInjectors[TelephonyRegistryStub::class.java] =
                    TelephonyRegistryStub()
                invocationStubManager.mInjectors[PhoneSubInfoStub::class.java] = PhoneSubInfoStub()
                invocationStubManager.mInjectors[PowerManagerStub::class.java] = PowerManagerStub()
                invocationStubManager.mInjectors[AppWidgetManagerStub::class.java] =
                    AppWidgetManagerStub()
                invocationStubManager.mInjectors[AccountManagerStub::class.java] =
                    AccountManagerStub()
                invocationStubManager.mInjectors[AudioManagerStub::class.java] = AudioManagerStub()
                invocationStubManager.mInjectors[SearchManagerStub::class.java] = SearchManagerStub()
                invocationStubManager.mInjectors[ContentServiceStub::class.java] =
                    ContentServiceStub()
                invocationStubManager.mInjectors[ConnectivityStub::class.java] = ConnectivityStub()
                val requestIdGenerator = NetworkStatsManagerStub.requestIdGenerator

                var cls :Class<*>? = null
                try {
                    cls = Class.forName("android.net.INetworkStatsService\$Stub")
                } catch (unused: Throwable) {
                    cls = null
                }
                if (cls != null && ServiceManager.getService.call("netstats") != null) {
                    invocationStubManager.mInjectors[NetworkStatsManagerStub::class.java] =
                        NetworkStatsManagerStub()
                }
                invocationStubManager.mInjectors[StatusBarManagerStub::class.java] =
                    StatusBarManagerStub()
                invocationStubManager.mInjectors[VibratorStub::class.java] = VibratorStub()
                invocationStubManager.mInjectors[WifiManagerStub::class.java] = WifiManagerStub()
                if (i >= 29) {
                    val cls11 =try {
                        Class.forName(if (Build.VERSION.SDK_INT >= 30) "android.hardware.biometrics.IAuthService\$Stub" else "android.hardware.biometrics.IBiometricService\$Stub")
                    } catch (unused2: Throwable) {
                        null
                    }
                    if (cls11 != null) {
                        if (ServiceManager.getService.call(if (Build.VERSION.SDK_INT >= 30) "auth" else "biometric") != null) {
                            invocationStubManager.mInjectors[AuthServiceStub::class.java] =
                                AuthServiceStub()
                        }
                    }
                }
                val cls2 =try {
                    Class.forName("android.nfc.INfcAdapter\$Stub")
                } catch (unused3: Throwable) {
                    null
                }
                if (cls2 != null && ServiceManager.getService.call("nfc") != null) {
                    invocationStubManager.mInjectors[NfcAdapterStub::class.java] = NfcAdapterStub()
                }
                invocationStubManager.mInjectors[BluetoothStub::class.java] = BluetoothStub()
                if (CameraServiceStub.getCameraServiceStubClass() != null && ServiceManager.getService.call("media.camera") != null) {
                    invocationStubManager.mInjectors[CameraServiceStub::class.java] =
                        CameraServiceStub()
                }
                invocationStubManager.mInjectors[ContextHubServiceStub::class.java] =
                    ContextHubServiceStub()
                invocationStubManager.mInjectors[UserManagerStub::class.java] = UserManagerStub()
                invocationStubManager.mInjectors[DisplayStub::class.java] = DisplayStub()
                if (IPersistentDataBlockServiceMirror.Stub.TYPE != null) {
                    invocationStubManager.mInjectors[PersistentDataBlockServiceStub::class.java] =
                        PersistentDataBlockServiceStub()
                }
                invocationStubManager.mInjectors[InputMethodManagerStub::class.java] =
                    InputMethodManagerStub()
                invocationStubManager.mInjectors[MmsStub::class.java] = MmsStub()
                invocationStubManager.mInjectors[SessionManagerStub::class.java] =
                    SessionManagerStub()
                invocationStubManager.mInjectors[JobServiceStub::class.java] = JobServiceStub()
                invocationStubManager.mInjectors[RestrictionStub::class.java] = RestrictionStub()
                if (ITrustManagerMiroor.TYPE != null) {
                    invocationStubManager.mInjectors[TrustManagerStub::class.java] =
                        TrustManagerStub()
                }
                invocationStubManager.mInjectors[AlarmManagerStub::class.java] = AlarmManagerStub()
                invocationStubManager.mInjectors[AppOpsManagerStub::class.java] = AppOpsManagerStub()
                invocationStubManager.mInjectors[MediaRouterServiceStub::class.java] =
                    MediaRouterServiceStub()
                if (ISmtOpsServiceMirror.TYPE != null) {
                    invocationStubManager.mInjectors[SmtOpsManagerStub::class.java] =
                        SmtOpsManagerStub()
                }
                invocationStubManager.mInjectors[GraphicsStatsStub::class.java] = GraphicsStatsStub()
                invocationStubManager.mInjectors[UsageStatsManagerStub::class.java] =
                    UsageStatsManagerStub()
                invocationStubManager.mInjectors[FingerprintManagerStub::class.java] =
                    FingerprintManagerStub()
                invocationStubManager.mInjectors[NetworkManagementStub::class.java] =
                    NetworkManagementStub()
                invocationStubManager.mInjectors[NetworkScoreManagerStub::class.java] =
                    NetworkScoreManagerStub()
                invocationStubManager.mInjectors[WifiScannerStub::class.java] = WifiScannerStub()
                invocationStubManager.mInjectors[ShortcutServiceStub::class.java] =
                    ShortcutServiceStub()
                invocationStubManager.mInjectors[DevicePolicyManagerStub::class.java] =
                    DevicePolicyManagerStub()
                invocationStubManager.mInjectors[UpdateEngineStub::class.java] = UpdateEngineStub()
                val put = invocationStubManager.mInjectors.put(
                    BatteryStatsStub::class.java,
                    BatteryStatsStub()
                )
                if (sdk28OrLater()) {
                    if (i >= 28) {
                        val cls10 = try {
                             Class.forName("android.content.pm.ICrossProfileApps\$Stub")
                        } catch (unused4: Throwable) {
                            null
                        }
                        if (cls10 != null && ServiceManager.getService.call("crossprofileapps") != null) {
                            invocationStubManager.mInjectors[CrossProfileAppsStub::class.java] =
                                CrossProfileAppsStub()
                        }
                    }
                    invocationStubManager.mInjectors[SystemUpdateStub::class.java] =
                        SystemUpdateStub()
                    invocationStubManager.mInjectors[LockSettingsStub::class.java] =
                        LockSettingsStub()
                    invocationStubManager.mInjectors[SliceManagerStub::class.java] =
                        SliceManagerStub()
                    invocationStubManager.mInjectors[RoleManagerStub::class.java] = RoleManagerStub()
                }
                if (sdk29OrLater()) {
                    invocationStubManager.mInjectors[DeviceIdentifiersServiceStub::class.java] =
                        DeviceIdentifiersServiceStub()
                    var z = try {
                        Class.forName("android.util.BoostFramework")
                        true
                    } catch (unused5: java.lang.Exception) {
                        false
                    }
                    if (!z) {
                        try {
                            Class.forName("com.qualcomm.qti.Performance")
                            z = true
                        } catch (unused6: java.lang.Exception) {
                        }
                    }
                    if (z && ServiceManager.getService.call("vendor.perfservice") != null && IPerfManagerMirror.Stub.asInterface != null) {
                        invocationStubManager.mInjectors[QPerformanceStub::class.java] =
                            QPerformanceStub()
                    }
                    if ("samsung".equals(Build.BRAND,ignoreCase = true) ||
                        "samsung".equals(Build.MANUFACTURER, ignoreCase = true)
                    ) {
                        invocationStubManager.mInjectors[SSClipboardStub::class.java] =
                            SSClipboardStub()
                    }
                    invocationStubManager.mInjectors[TextServiceStub::class.java] = TextServiceStub()
                }
                if (sdk30OrLater()) {
                    invocationStubManager.mInjectors[PermissionManagerStub::class.java] =
                        PermissionManagerStub()
                    invocationStubManager.mInjectors[TetheringServiceStub::class.java] =
                        TetheringServiceStub()
                    invocationStubManager.mInjectors[AppIntegrityManagerStub::class.java] =
                        AppIntegrityManagerStub()
                    invocationStubManager.mInjectors[StatsManagerStub::class.java] =
                        StatsManagerStub()
                }
                if (sdk31OrLater()) {
                    invocationStubManager.mInjectors[VibratorManagerStub::class.java] =
                        VibratorManagerStub()
                    if (i >= 31) {
                        val handler = UwbManagerStubMirror.mainHandler
                        val cls9 = try {
                            Class.forName("android.uwb.IUwbAdapter\$Stub")
                        } catch (unused7: Throwable) {
                            null
                        }
                        if (cls9 != null && ServiceManager.getService.call("uwb") != null) {
                            invocationStubManager.mInjectors[UwbManagerStubMirror::class.java] =
                                UwbManagerStubMirror()
                        }
                    } else {
                        val handler2 = UwbManagerStubMirror.mainHandler
                    }
                    if (IDomainVerificationManagerMirror.Stub.TYPE != null && IDomainVerificationManagerMirror.Stub.asInterface != null) {
                        invocationStubManager.mInjectors[DomainVerificationManagerStub::class.java] =
                            DomainVerificationManagerStub()
                    }
                }
                if (sdk33OrLater()) {
                    invocationStubManager.mInjectors[LocaleManagerStub::class.java] =
                        LocaleManagerStub()
                    invocationStubManager.mInjectors[SafetyCenterManagerStub::class.java] =
                        SafetyCenterManagerStub()
                    if (i >= 33) {
                        val cls8 = try {
                             Class.forName("android.net.nsd.INsdManager\$Stub")
                        } catch (unused8: Throwable) {
                            null
                        }
                        if (cls8 != null && ServiceManager.getService.call("servicediscovery") != null) {
                            invocationStubManager.mInjectors[NsdManagerStub::class.java] =
                                NsdManagerStub()
                        }
                    }
                }
                if (sdk34OrLater()) {
                    invocationStubManager.mInjectors[CredentialManagerStub::class.java] =
                        CredentialManagerStub()
                }
                if (i >= 36) {
                    val handler3 = AdvancedProtectionManagerStub.mainHandler
                    val cls7 =try {

                            Class.forName("android.security.advancedprotection.IAdvancedProtectionService\$Stub")
                    } catch (unused9: Throwable) {
                        null
                    }
                    if (cls7 != null && ServiceManager.getService.call("advanced_protection") != null) {
                        invocationStubManager.mInjectors[AdvancedProtectionManagerStub::class.java] =
                            AdvancedProtectionManagerStub()
                    }
                } else {
                    val handler4 = AdvancedProtectionManagerStub.mainHandler
                }
                if (i >= 36) {
                    val cls3 = try {
                        Class.forName("android.app.IUiModeManager\$Stub")
                    } catch (unused10: Throwable) {
                         null
                    }
                    if (cls3 != null && ServiceManager.getService.call("uimode") != null) {
                        invocationStubManager.mInjectors[UiModeManagerStub::class.java] =
                            UiModeManagerStub()
                    }
                }
                if (i >= 35) {
                    val cls4 = try {
                         Class.forName("android.app.appfunctions.IAppFunctionManager\$Stub")
                    } catch (unused11: Throwable) {
                        null
                    }
                    if (cls4 != null && ServiceManager.getService.call("app_function") != null) {
                        invocationStubManager.mInjectors[AppFunctionManagerStub::class.java] =
                            AppFunctionManagerStub()
                    }
                }
                if (i >= 31) {
                    val cls5 = try {
                         Class.forName("android.app.appsearch.aidl.IAppSearchManager\$Stub")
                    } catch (unused12: Throwable) {
                        null
                    }
                    if (cls5 != null && ServiceManager.getService.call("app_search") != null) {
                        invocationStubManager.mInjectors[AppSearchManagerStub::class.java] =
                            AppSearchManagerStub()
                    }
                }
                if (i >= 36) {
                    val cls6 = try {
                        Class.forName("android.app.supervision.ISupervisionManager\$Stub")
                    } catch (unused13: Throwable) {
                        null
                    }
                    if (cls6 != null && ServiceManager.getService.call("supervision") != null) {
                        invocationStubManager.mInjectors[SupervisionManagerStub::class.java] =
                            SupervisionManagerStub()
                    }
                }
                if (i >= 26) {
                    if (sdk29OrLater() && IAutoFillManager.Stub.asInterface != null && ServiceManager.getService.call(
                            "autofill"
                        ) != null
                    ) {
                        invocationStubManager.mInjectors[AutoFillManagerStub::class.java] =
                            AutoFillManagerStub()
                    }
                    invocationStubManager.mInjectors[StorageStatsStub::class.java] =
                        StorageStatsStub()
                }
            }
        }
        InvocationStubManager.initialized = true
        val it: MutableIterator<*> = invocationStubManager.mInjectors.values.iterator()
        while (it.hasNext()) {
            (it.next() as IInjector).inject()
        }
        invocationStubManager.addInjector(AppInstrumentation.getInstance())
        ContextFixer.fixContext(appContext)
        this.isStarted = true
        if (this.processType == ProcessType.Main) {
            DaemonService.ensureBound(appContext)
        }
        val conditionVariable: ConditionVariable? = this.initLock
        if (conditionVariable != null) {
            conditionVariable.open()
            this.initLock = null
        }


    }

    fun detectProcessType() {
        val appInfo = appContext.applicationInfo
        hostPackageName = appInfo.packageName
        mainProcessName = appInfo.processName
        currentProcessName = ActivityThread.getProcessName?.call(mainThread)

        val current = currentProcessName
        processType = when {
            current == mainProcessName -> ProcessType.Main
            current != null && current.endsWith(":x") -> ProcessType.Server
            else -> {
                val prefix = "${hostPackageName}:p"
                val index = if (current != null && current.startsWith(prefix)) {
                    current.substring(prefix.length).toIntOrNull() ?: -1
                } else -1
                if (index != -1) ProcessType.Client else ProcessType.Child
            }
        }

        if (isClientProcess()) {
            try {
                systemPid = VActivityManager.instance.getService().getSystemPid()
            } catch (e: RemoteException) {
                crash(e)
            }
        }
    }

    fun getAppManager(): IAppManager?
    {
        val cached = singleton
        if (cached == null || !cached.asBinder().pingBinder())
        {
            synchronized(this)
            {
                for (i in 0 until 3)
                {
                    val appMgr = IAppManager.CloneAppManager.asInterface(ServiceManagerNative.getService("app"))
                    if (appMgr?.asBinder() != null)
                    {
                        try {
                            appMgr.asBinder().linkToDeath(ServiceDeathHandler(appMgr), 0)
                        } catch (unused: Throwable) {
                        }
                        singleton = appMgr
                        break
                    }
                    ServiceManagerNative.makeProviderCall()
                    try {
                        Thread.sleep(150L)
                    } catch (unused: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }
                }
            }
        }
        return singleton
    }

    @Synchronized
    fun getHostPermissions(): HashSet<String> {
        hostPermissionCache?.let { return it }

        val permissions = HashSet<String>()
        for (permission in hostPkgInfo.requestedPermissions.orEmpty()) {
            if (PermissionCompat.DANGEROUS_PERMISSIONS.contains(permission)) {
                permissions.add(permission)
            }
        }
        hostPermissionCache = permissions
        return permissions
    }


    /**
     * Resolves [intent] to an activity of the virtual user [userId]. If the intent has no component, the resolved
     * class is written into it. If the activity is an alias (targetActivity), the real activity is returned and
     * becomes the intent's component.
     */
    @Synchronized
    fun resolveActivityInfo(userId: Int, intent: Intent): ActivityInfo? {
        val packageManager = VPackageManager.instance?.getPackageManager()!!
        var info: ActivityInfo?
        val component = intent.component
        if (component == null ) {
            info = packageManager.resolveIntent(intent, intent.type, 0, userId)?.activityInfo
            if (info != null) {
                intent.setClassName(info.packageName, info.name)
            }
        } else {
            info = packageManager.getActivityInfo(component, 0, userId)
        }

        if (info != null && info.targetActivity != null) {
            val target = ComponentName(info.packageName, info.targetActivity)
            info = packageManager.getActivityInfo(target, 0, userId)
            intent.component = target
        }
        return info
    }

    //      android.os.RemoteException; io.AssetManagerMirror as AssetManagerMirror) ----

    /** was getInstalledAppInfo(String): null if the app manager is unavailable. RemoteException propagates (x09.get dropped). */
    @Throws(RemoteException::class)
    fun getInstalledAppInfo(pkgName: String?): InstalledAppInfo? {
        val appManager = getAppManager() ?: return null
        return appManager.getInstalledAppInfo(pkgName, 0)
    }

    /** was getCloneResources(String): Resources over the cloned app's apk. Throws NotFoundException if it is not installed. */
    @Throws(Resources.NotFoundException::class)
    fun getCloneResources(packageName: String): Resources {
        val info = getInstalledAppInfo(packageName) ?: throw Resources.NotFoundException(packageName)
        val assetManager = AssetManagerMirror.ctor!!.newInstance()
        AssetManagerMirror.addAssetPath!!.call(assetManager, info.appPath)
        val hostResources = appContext.resources
        return Resources(assetManager, hostResources.displayMetrics, hostResources.configuration)
    }

    fun getHostUserId(): Int = uid / 100000

    fun isAppInstalled(str: String?): Boolean {
        try {
            return getAppManager()?.isAppInstalled(str)?:false
        } catch (unused: RemoteException) {
            this.singleton = null
            return false
        } catch (unused2: java.lang.Exception) {
            return false
        }
    }

    fun getInstalledApps(): List<InstalledAppInfo>? {
        try {
            val appMgr = getAppManager()
            if (appMgr != null) {
                return appMgr.getInstalledApps(0)
            }
            crash(RemoteException("IAppManager service unavailable for getInstalledApps."))
        } catch (e2: RemoteException) {
            this.singleton = null
            crash(e2)
        }
    }

    fun getListener(): ComponentDelegate {
        val c = this.cloneComponentDelegate
        return if (c == null) ComponentDelegate.a else cloneComponentDelegate!!
    }

    fun isAppInstalledAsUser(i: Int, packageName: String?): Boolean {
        try {
            return getAppManager()!!.isAppInstalledAsUser(i, packageName)
        } catch (e2: RemoteException) {
            crash(e2)
        }
    }

    fun installPackage(pkgName: String?, path: String?, flags: Int): InstallResult {
        var result: InstallResult? = null
        repeat(5) {
            try {
                val manager = getAppManager()
                if (manager != null) {
                    result = manager.installPackage(pkgName, path, flags)
                }
            } catch (ignored: RemoteException) {
                singleton = null
            }
            if (result != null && result.installed) return result
            try {
                Thread.sleep(300L)
            } catch (ignored: Exception) {
            }
        }
        return InstallResult.makeFailure("Service not available")
    }

    fun installPackageAsUser(uid: Int, pkg: String?): Boolean {
        return try {
            getAppManager()?.installPackageAsUser(uid, pkg) ?: false
        } catch (e: RemoteException) {
            VirtualRuntime.crash(e)
            throw e
        }
    }

    fun getLaunchIntent(userId: Int, packageName: String?): Intent? {
        val vpm = VPackageManager.instance
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_INFO)
        intent.setPackage(packageName)
        return try {
            var resolved = vpm?.queryIntentActivities(intent, intent.resolveType(appContext), 0, userId)
            if (resolved.isNullOrEmpty()) {
                intent.removeCategory(Intent.CATEGORY_INFO)
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                intent.setPackage(packageName)
                resolved = vpm?.queryIntentActivities(intent, intent.resolveType(appContext), 0, userId)
            }
            val activityInfo = resolved?.firstOrNull()?.activityInfo ?: return null
            Intent(intent).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                setClassName(activityInfo.packageName, activityInfo.name)
            }
        } catch (e: SecurityException) {
            Log.d("PLIB_", e.message.toString())
            Log.e("PLIB_", "user invalid $userId")
            null
        }
    }


    //===================================================================
    class VirtualCoreDeathRecipient(private val remote: IBinder): DeathRecipient
    {

        override fun binderDied() {
            remote.unlinkToDeath(this,0)
            ServiceManagerNative.serviceFetcher = null
            val virtualCore = gCore
            if (virtualCore.isMainProcess()) {
                virtualCore.singleton = null
//                VActivityManager.instance.service = null
//                cu8.f40154b.f40155a = null
//                y09.f52768b.f52769a = null
//                pu8.f47774b.f47775a = null
//                uu8.f50942c.f50944b = null
                VPackageManager.instance!!.vPackageManager = null
            }
            Log.d("q47", "Ops, the server has crashed.")
            for (i in 5 downTo 1) {
                try {
                    Thread.sleep(400L)
                } catch (unused: Throwable) {
                }
                Log.d("q47", "Try hot recover $i")
                if (ServiceManagerNative.getService("app") != null) {
                    return
                }
            }
            Log.d("q47", "Hot recover failed. Exit.")

        }

    }
    inner class ServiceDeathHandler (val appManager: IAppManager) : DeathRecipient
    {
        override fun binderDied()
        {
            this.appManager.asBinder().unlinkToDeath(this, 0)
            this@VirtualCore.singleton = null
        }
    }
}

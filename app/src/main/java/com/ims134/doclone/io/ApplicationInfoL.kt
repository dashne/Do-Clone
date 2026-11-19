package com.ims134.doclone.io

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlarmManager
import android.app.Application
import android.app.IServiceConnection
import android.app.Instrumentation
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.content.pm.ServiceInfo
import android.content.pm.SharedLibraryInfo
import android.content.pm.VersionedPackage
import android.os.Bundle
import android.os.Handler
import android.os.Parcel
import android.os.Parcelable
//import android.content.pm.PackageParser
import com.ims134.doclone.reflect.MethodParams
import com.ims134.doclone.reflect.MethodReflectParams
import com.ims134.doclone.reflect.RefBoolean
import com.ims134.doclone.reflect.RefClass
import com.ims134.doclone.reflect.RefClass.Companion.load
import com.ims134.doclone.reflect.RefConstructor
import com.ims134.doclone.reflect.RefMethod
import com.ims134.doclone.reflect.RefObject
import com.ims134.doclone.reflect.RefStaticInt
import com.ims134.doclone.reflect.RefStaticMethod
import com.ims134.doclone.reflect.RefStaticObject
import java.io.File
import java.lang.Class
import java.lang.ref.WeakReference
import java.lang.reflect.Method
import java.util.function.Consumer
import android.app.Notification
import android.app.job.JobInfo
import android.widget.RemoteViews
import android.app.job.JobParameters
import android.content.ClipboardManager
import android.content.ContentProviderClient
import android.content.ContentResolver
import android.content.IIntentReceiver
import android.location.Location
import android.location.LocationListener
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.os.Build
import android.os.IBinder
import android.os.IInterface
import android.os.Message
import android.os.PersistableBundle
import android.os.health.SystemHealthManager
import android.provider.Settings
import android.telephony.CellInfoCdma
import android.telephony.CellInfoGsm
import android.telephony.NeighboringCellInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.ims134.doclone.reflect.RefInt
import com.ims134.doclone.reflect.RefLong
import java.net.InetAddress


import android.content.pm.PackageParser
import android.net.IConnectivityManager
import android.net.wifi.IWifiScanner


object ApplicationInfoL {
    @JvmField var primaryCpuAbi: RefObject<String?>? = null
    @JvmField var scanPublicSourceDir: RefObject<String?>? = null
    @JvmField var scanSourceDir: RefObject<String?>? = null
    @JvmField var secondaryCpuAbi: RefObject<String?>? = null
    @JvmField var secondaryNativeLibraryDir: RefObject<*>? = null
    @JvmField var splitPublicSourceDirs: RefObject<Array<String?>?>? = null
    @JvmField var splitSourceDirs: RefObject<Array<String?>?>? = null

    @JvmField var TYPE: Class<*>? =
        load(ApplicationInfoL::class.java, ApplicationInfo::class.java as Class<*>)
}

object ApplicationInfoO {
    @JvmField var privateFlags: RefObject<Int?>? = null
    @JvmField var splitDependencies: RefObject<Any?>? = null

    @JvmField var TYPE: Class<*>? =
        load(ApplicationInfoO::class.java, ApplicationInfo::class.java as Class<*>)
}

object ApplicationInfoP {
    @JvmField var setHiddenApiEnforcementPolicy: RefMethod<*>? = null
    @JvmField var splitClassLoaderNames: RefObject<Any?>? = null

    @JvmField var TYPE: Class<*>? =
        load(ApplicationInfoP::class.java, ApplicationInfo::class.java as Class<*>)
}
object ApplicationInfoQ {
    @JvmField var sharedLibraryInfos: RefObject<MutableList<*>?>? = null

    @JvmField var TYPE: Class<*>? =
        load(ApplicationInfoQ::class.java, ApplicationInfo::class.java as Class<*>)
}

object ApplicationInfoN {
    @JvmField var credentialEncryptedDataDir: RefObject<String?>? = null
    @JvmField var credentialProtectedDataDir: RefObject<String?>? = null
    @JvmField var deviceEncryptedDataDir: RefObject<String?>? = null
    @JvmField var deviceProtectedDataDir: RefObject<String?>? = null
    @JvmField var networkSecurityConfigRes: RefObject<Int?>? = null

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    fun networkSecurityConfigRes(applicationInfo: ApplicationInfo?): Int {
        val refObject = networkSecurityConfigRes
        val obj = if (refObject != null) refObject.get(applicationInfo) else null
        if (Int::class.java.isInstance(obj)) {
            return (obj as Int)
        }
        return 0
    }

    fun networkSecurityConfigRes(applicationInfo: ApplicationInfo?, num: Int?) {
        val refObject = networkSecurityConfigRes
        if (refObject != null) {
            refObject.set(applicationInfo, num)
        }
    }

    @JvmField var TYPE: Class<*>? =
        load(ApplicationInfoN::class.java, ApplicationInfo::class.java as Class<*>)
}

object VMRuntime {
    @JvmField var getCurrentInstructionSet: RefStaticMethod<String?>? = null
    @JvmField var getRuntime: RefStaticMethod<Any?>? = null
    @JvmField var is64Bit: RefMethod<Boolean?>? = null

    @field:MethodParams([String::class])
    @JvmField var is64BitAbi: RefStaticMethod<*>? = null

    @field:MethodParams([Int::class])
    @JvmField var setTargetSdkVersion: RefMethod<Void?>? = null

    @JvmField var TYPE: Class<*>? =
        load(VMRuntime::class.java as Class<*>, "dalvik.system.VMRuntime")
}






/**
 * Mirror of `android.content.pm.SharedLibraryInfo` (hidden, @SystemApi, SDK 31+).
 *
 * Loaded **by class name** rather than `Class` reference so the mirror can
 * initialize on SDK < 31 without setPosition `NoClassDefFoundError`. On those SDKs
 * [TYPE] is `null` and every `Ref*` member stays `null` — callers must
 * null-check [TYPE] before using any of the fields.
 *
 * AOSP shape:
 *   mName          : String               — library name (e.g. "org.apache.http.legacy")
 *   mPath          : String               — canonical path (for single-jar libraries)
 *   mCodePaths     : List<String>         — every path (for multi-jar libraries)
 *   getAllCodePaths(): List<String>       — returns mCodePaths or setPosition wrapped mPath
 *
 * Constructor signature varies with SDK; the 9-arg form ([ctor]) targets
 * API 31–33. For API 34+ (which adds setPosition 10th `long` arg), the load fails and
 * [ctor] is `null` — callers should fall back to reflection or skip creation.
 */
object SharedLibraryInfoO {


    /**
     * Constructor for API 31–33:
     *   (String name, String path, List codePaths, String packageName,
     *    long version, int type, VersionedPackage declaringPackage,
     *    List dependencies, List pathList)
     *
     * ⚠️ Multi-element array — Kotlin widens each `KClass<…>` to `KClass<*>`,
     * so this compiles. (Single-element would need `arrayOf<KClass<*>>(...)`.)
     */
    @JvmField
    @field:MethodParams([
        String::class, String::class, List::class, String::class,
        Long::class, Int::class, VersionedPackage::class, List::class, List::class,
    ])
    var ctor: RefConstructor<SharedLibraryInfo>? = null

    /** `RefMethod<List>` in the original — typed to `List<String>` here. */
    @JvmField
    var getAllCodePaths: RefMethod<List<String>>? = null

    /** `RefObject<List>` in the original — typed to `List<String>` here. */
    @JvmField
    var mCodePaths: RefObject<List<String>>? = null

    @JvmField
    var mName: RefObject<String>? = null

    @JvmField
    var mPath: RefObject<String>? = null

    @JvmField
    val TYPE: Class<*>? = load(
        SharedLibraryInfoO::class.java,
        "android.content.pm.SharedLibraryInfo",
    )
}

object IWebViewUpdateService {
    @JvmField var getCurrentWebViewPackageName: RefMethod<String?>? = null

    @JvmField var TYPE: Class<*>? = load(
        IWebViewUpdateService::class.java as Class<*>,
        "android.webkit.IWebViewUpdateService\$Stub\$Proxy"
    )
}
object WebViewFactory {
    @JvmField var getUpdateService: RefStaticMethod<Any?>? = null
    @JvmField var sDataDirectorySuffix: RefStaticObject<String?>? = null

    @JvmField var TYPE: Class<*>? =
        load(WebViewFactory::class.java as Class<*>, "android.webkit.WebViewFactory")
}

object IntentFilter0 {

    lateinit var getHostsList: RefMethod<Any>
    lateinit var mActions: RefObject<Any>

    @JvmField
    var TYPE: Class<*>? = RefClass.load(
        IntentFilter0::class.java,
        android.content.IntentFilter::class.java
    )
}

object PackageParserMarshmallow {

    @field:MethodReflectParams(["android.content.pm.PackageParser\$Package", "int"])
    @JvmField var collectCertificates: RefMethod<Void?>? = null
    @JvmField var ctor: RefConstructor<PackageParser?>? = null

    @field:MethodParams([File::class, Int::class])
    @JvmField var parsePackage: RefMethod<PackageParser.Package?>? = null

    @JvmField var TYPE: Class<*>? = load(
        PackageParserMarshmallow::class.java as Class<*>,
        "android.content.pm.PackageParser"
    )
}

object PackageParserPie {

    @field:MethodReflectParams(["android.content.pm.PackageParser\$Package", "boolean"])
    @JvmField var collectCertificates: RefStaticMethod<*>? = null

    @field:MethodReflectParams(["android.content.pm.PackageParser\$Callback"])
    @JvmField var setCallback: RefMethod<Void?>? = null

    /* JADX INFO: renamed from: io.kg5$setPosition */ /* JADX INFO: compiled from: PackageParserPie.java */
    object CallbackImpl {

        @field:MethodParams([PackageManager::class])
        @JvmField var ctor: RefConstructor<Any?>? = null

        @JvmField var TYPE: Class<*>? = load(
            CallbackImpl::class.java as Class<*>,
            "android.content.pm.PackageParser\$CallbackImpl"
        )
    }

    @JvmField var TYPE: Class<*>? =
        load(PackageParserPie::class.java as Class<*>, "android.content.pm.PackageParser")
}
object PackageParserNougat {

    @field:MethodReflectParams(["android.content.pm.PackageParser\$Package", "int"])
    @JvmField var collectCertificates: RefStaticMethod<Void?>? = null

    @JvmField var TYPE: Class<*>? = load(
        PackageParserNougat::class.java as Class<*>,
        "android.content.pm.PackageParser"
    )
}
object LoadedApkHuaWei {
    @JvmField var mReceiverResource: RefObject<Any?>? = null

    @JvmField var TYPE: Class<*>? =
        load(LoadedApkHuaWei::class.java as Class<*>, "android.app.LoadedApk")
}
object ReceiverResourceN {
    @JvmField var mWhiteList: RefObject<MutableList<String?>?>? = null

    @JvmField var TYPE: Class<*>? = load(
        ReceiverResourceN::class.java as Class<*>,
        "android.rms.resource.ReceiverResource"
    )
}
object ContextImpl {
    @JvmField var getAttributionSource: RefMethod<Any?>? = null
    @JvmField var getReceiverRestrictedContext: RefMethod<Context?>? = null

    @field:MethodParams([Context::class])
    @JvmField var mBasePackageName: RefObject<String?>? = null
    @JvmField var mPackageInfo: RefObject<Any?>? = null
    @JvmField var mPackageManager: RefObject<PackageManager?>? = null

    @JvmField var TYPE: Class<*>? =
        load(ContextImpl::class.java as Class<*>, "android.app.ContextImpl")
}
object NativeLibraryHelper {

    @field:MethodParams([Handle::class, File::class, String::class])
    @JvmField var copyNativeBinaries: RefStaticMethod<Int?>? = null

    @field:MethodParams([Handle::class, Array<String>::class])
    @JvmField var findSupportedAbi: RefStaticMethod<Int?>? = null

    /* JADX INFO: renamed from: io.zz4$setPosition */ /* JADX INFO: compiled from: NativeLibraryHelper.java */
    object Handle {

        @field:MethodParams([File::class])
        @JvmField var create: RefStaticMethod<Any?>? = null

        @JvmField var TYPE: Class<*>? = load(
            Handle::class.java as Class<*>,
            "com.android.internal.content.NativeLibraryHelper\$Handle"
        )
    }

    @JvmField var TYPE: Class<*>? = load(
        NativeLibraryHelper::class.java as Class<*>,
        "com.android.internal.content.NativeLibraryHelper"
    )
}

object ContentProviderNative {

    @field:MethodParams([IBinder::class])
    @JvmField var asInterface: RefStaticMethod<IInterface>? = null

    @JvmField var TYPE: Class<*>? = load(
        ContentProviderNative::class.java as Class<*>,
        "android.content.ContentProviderNative"
    )
}
object IApplicationThreadOreo {
    @JvmField
    @field:MethodReflectParams(["android.os.IBinder", "android.content.pm.ParceledListSlice"])
    var scheduleServiceArgs: RefMethod<Void?>? = null

    object Stub {
        @JvmField
        @field:MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField
        var TYPE: Class<*>? = load(Stub::class.java, "android.app.IApplicationThread\$Stub")
    }

    @JvmField
    var TYPE: Class<*>? = load(IApplicationThreadOreo::class.java, "android.app.IApplicationThread")
}
object ApplicationThreadNative {

    @JvmField
    @field:MethodParams([IBinder::class])
    var asInterface: RefStaticMethod<IInterface?>? = null

    @JvmField
    var TYPE: Class<*>? = load(ApplicationThreadNative::class.java, "android.app.ApplicationThreadNative")
}
object IntentMirror {
    @JvmField var collectExtraIntentKeys: RefMethod<Void?>? = null

    @JvmField var TYPE: Class<*>? = load(IntentMirror::class.java, Intent::class.java as Class<*>)
}

/**
 * Mirror of the hidden com.android.internal.R (was io.ga6, "R_Hide.java"): resource ids that the SDK does not
 * expose. Fields are filled by name when the object is initialized; get missing field stays null.
 * Each nested object declares TYPE last, so the fields exist before they are bound.
 *
 * NOTE: in the decompiled original, Drawable and Layout were loaded with Id.class as the mirror class (get copy
 * and paste slip), so their own fields were never bound. Here each object uses itself.
 */
object R_Hide {
    @JvmField var TYPE: Class<*>? = load(R_Hide::class.java as Class<*>, "com.android.internal.R")

    /* JADX INFO: renamed from: io.ga6$get */ /* JADX INFO: compiled from: R_Hide.java */
    object Drawable {
        @JvmField var popup_bottom_bright: RefStaticInt? = null
        @JvmField var popup_bottom_dark: RefStaticInt? = null
        @JvmField var popup_bottom_medium: RefStaticInt? = null
        @JvmField var popup_center_bright: RefStaticInt? = null
        @JvmField var popup_center_dark: RefStaticInt? = null
        @JvmField var popup_full_bright: RefStaticInt? = null
        @JvmField var popup_full_dark: RefStaticInt? = null
        @JvmField var popup_top_bright: RefStaticInt? = null
        @JvmField var popup_top_dark: RefStaticInt? = null

        @JvmField var TYPE: Class<*>? =
            load(Id::class.java as Class<*>, "com.android.internal.R\$drawable")
    }

    /* JADX INFO: renamed from: io.ga6$b */ /* JADX INFO: compiled from: R_Hide.java */
    object Id {
        @JvmField var alertTitle: RefStaticInt? = null
        @JvmField var button1: RefStaticInt? = null
        @JvmField var button2: RefStaticInt? = null
        @JvmField var button3: RefStaticInt? = null
        @JvmField var buttonPanel: RefStaticInt? = null
        @JvmField var contentPanel: RefStaticInt? = null
        @JvmField var custom: RefStaticInt? = null
        @JvmField var customPanel: RefStaticInt? = null
        @JvmField var icon: RefStaticInt? = null
        @JvmField var leftSpacer: RefStaticInt? = null
        @JvmField var message: RefStaticInt? = null
        @JvmField var resolver_list: RefStaticInt? = null
        @JvmField var rightSpacer: RefStaticInt? = null
        @JvmField var scrollView: RefStaticInt? = null
        @JvmField var text1: RefStaticInt? = null
        @JvmField var text2: RefStaticInt? = null
        @JvmField var titleDivider: RefStaticInt? = null
        @JvmField var titleDividerTop: RefStaticInt? = null
        @JvmField var title_template: RefStaticInt? = null
        @JvmField var topPanel: RefStaticInt? = null

        @JvmField var TYPE: Class<*>? =
            load(Id::class.java as Class<*>, "com.android.internal.R\$id")
    }

    /* JADX INFO: renamed from: io.ga6$c */ /* JADX INFO: compiled from: R_Hide.java */
    object Layout {
        @JvmField var resolver_list: RefStaticInt? = null

        @JvmField var TYPE: Class<*>? =
            load(Id::class.java as Class<*>, "com.android.internal.R\$layout")
    }

    /* JADX INFO: renamed from: io.ga6$Styleable */ /* JADX INFO: compiled from: R_Hide.java */
    object Styleable {
        @JvmField var Window: RefStaticObject<IntArray?>? = null
        @JvmField var Window_windowBackground: RefStaticInt? = null
        @JvmField var Window_windowFullscreen: RefStaticInt? = null
        @JvmField var Window_windowIsFloating: RefStaticInt? = null
        @JvmField var Window_windowIsTranslucent: RefStaticInt? = null
        @JvmField var Window_windowShowWallpaper: RefStaticInt? = null

        @JvmField var AccountAuthenticator: RefStaticObject<IntArray?>? = null
        @JvmField var AccountAuthenticator_accountPreferences: RefStaticInt? = null
        @JvmField var AccountAuthenticator_accountType: RefStaticInt? = null
        @JvmField var AccountAuthenticator_customTokens: RefStaticInt? = null
        @JvmField var AccountAuthenticator_icon: RefStaticInt? = null
        @JvmField var AccountAuthenticator_label: RefStaticInt? = null
        @JvmField var AccountAuthenticator_smallIcon: RefStaticInt? = null
        @JvmField var AlertDialog_bottomBright: RefStaticInt? = null
        @JvmField var AlertDialog_bottomDark: RefStaticInt? = null
        @JvmField var AlertDialog_bottomMedium: RefStaticInt? = null
        @JvmField var AlertDialog_centerBright: RefStaticInt? = null
        @JvmField var AlertDialog_centerDark: RefStaticInt? = null
        @JvmField var AlertDialog_fullBright: RefStaticInt? = null
        @JvmField var AlertDialog_fullDark: RefStaticInt? = null
        @JvmField var AlertDialog_topBright: RefStaticInt? = null
        @JvmField var AlertDialog_topDark: RefStaticInt? = null
        @JvmField var SyncAdapter: RefStaticObject<IntArray?>? = null
        @JvmField var SyncAdapter_accountType: RefStaticInt? = null
        @JvmField var SyncAdapter_allowParallelSyncs: RefStaticInt? = null
        @JvmField var SyncAdapter_contentAuthority: RefStaticInt? = null
        @JvmField var SyncAdapter_isAlwaysSyncable: RefStaticInt? = null
        @JvmField var SyncAdapter_settingsActivity: RefStaticInt? = null
        @JvmField var SyncAdapter_supportsUploading: RefStaticInt? = null
        @JvmField var SyncAdapter_userVisible: RefStaticInt? = null
        @JvmField var TYPE: Class<*>? =
            load(Styleable::class.java as Class<*>, "com.android.internal.R\$styleable")
    }
}
object IApplicationThread {

    @field:MethodParams([IBinder::class, Intent::class, Boolean::class])
    @JvmField var scheduleBindService: RefMethod<Void?>? = null

    @field:MethodParams([IBinder::class, ServiceInfo::class])
    @JvmField var scheduleCreateService: RefMethod<Void?>? = null

    @field:MethodParams([MutableList::class, IBinder::class])
    @JvmField var scheduleNewIntent: RefMethod<Void?>? = null

    @field:MethodParams([IBinder::class, Int::class, Int::class, Intent::class])
    @JvmField var scheduleServiceArgs: RefMethod<Void?>? = null

    @field:MethodParams([IBinder::class])
    @JvmField var scheduleStopService: RefMethod<Void?>? = null

    @field:MethodParams([IBinder::class, Intent::class])
    @JvmField var scheduleUnbindService: RefMethod<Void?>? = null

    @JvmField var TYPE: Class<*>? =
        load(IApplicationThread::class.java as Class<*>, "android.app.IApplicationThread")
}
object IActivityManagerMirror {

    @JvmField
    @field:MethodParams([IBinder::class, Boolean::class])
    var getTaskForActivity: RefMethod<Int?>? = null

    @JvmField
    @field:MethodParams([IBinder::class, String::class, Int::class, Int::class])
    var overridePendingTransition: RefMethod<Void?>? = null
    @JvmField
    var serviceDoneExecuting: RefMethod<Void?>? = null
    @JvmField
    @field:MethodParams([IBinder::class, Int::class])
    var setRequestedOrientation: RefMethod<Void?>? = null

    @JvmField
    var startActivities: RefMethod<Int?>? = null
    @JvmField
    var startActivity: RefMethod<Int?>? = null

    /* JADX INFO: renamed from: io.d23$get */ /* JADX INFO: compiled from: IActivityManager.java */
    object ContentProviderHolder {

        @JvmField
        var info: RefObject<ProviderInfo?>? = null

        @JvmField
        var noReleaseNeeded: RefBoolean? = null

        @JvmField
        var provider: RefObject<IInterface?>? = null

        @JvmField
        var TYPE: Class<*>? = load(
            ContentProviderHolder::class.java as Class<*>,
            "android.app.IActivityManager\$ContentProviderHolder"
        )
    }

    @JvmField
    var TYPE: Class<*>? =
        load(IActivityManagerMirror::class.java as Class<*>, "android.app.IActivityManager")
}
object ActivityManagerNative {

    val k = RefClass.logMirrorFields( ActivityManagerNative::class.java as Class<*>,
        "android.app.ActivityManagerNative")


    @JvmField
    var gDefault: RefStaticObject<Any?>? = null

    @JvmField
    var getDefault: RefStaticMethod<IInterface?>? = null

    @JvmField
    var TYPE: Class<*>? = load(
        ActivityManagerNative::class.java as Class<*>,
        "android.app.ActivityManagerNative"
    )
}
object AssetManagerMirror {

    @field:MethodParams([String::class])
    @JvmField var addAssetPath: RefMethod<Int?>? = null
    @JvmField var ctor: RefConstructor<android.content.res.AssetManager?>? = null

    @JvmField var TYPE: Class<*>? =
        load(AssetManagerMirror::class.java, android.content.res.AssetManager::class.java as Class<*>)
}
object BaseBundle {
    @JvmField var mParcelledData: RefObject<Parcel?>? = null

    @JvmField var TYPE: Class<*>? = load(BaseBundle::class.java as Class<*>, "android.os.BaseBundle")
}
object BundleICS {
    @JvmField var mParcelledData: RefObject<Parcel?>? = null

    @JvmField var TYPE: Class<*>? = load(BundleICS::class.java as Class<*>, "android.os.Bundle")
}

/*
object ActivityThread
{
    var TYPE: Class<*>? = load(ActivityThread::class.java as Class<*>, "android.app.ActivityThread")
    var currentActivityThread: RefStaticMethod<*>? = null
    var deliverNewIntents: RefMethod<Void?>? = null

    @MethodParams([IBinder::class])
    var getActivityClient: RefMethod<*>? = null
    var getApplicationThread: RefMethod<Binder?>? = null
    var getHandler: RefMethod<Handler?>? = null

    @MethodParams([IBinder::class])
    var getLaunchingActivity: RefMethod<*>? = null
    var getPackageInfoNoCheck: RefMethod<Any?>? = null
    var getProcessName: RefMethod<String?>? = null
    var installProvider: RefMethod<Any?>? = null
    var mActivities: RefObject<ArrayMap<*, *>?>? = null
    var mBoundApplication: RefObject<Any?>? = null
    var mH: RefObject<Handler?>? = null
    var mInitialApplication: RefObject<Application?>? = null
    var mInstrumentation: RefObject<Instrumentation?>? = null
    var mPackages: RefObject<MutableMap<String?, WeakReference<*>?>?>? = null
    var mProviderMap: RefObject<MutableMap<*, *>?>? = null

    @MethodParams([IBinder::class, MutableList::class])
    var performNewIntents: RefMethod<Void?>? = null
    var sPackageManager: RefStaticObject<IInterface?>? = null
    var sPermissionManager: RefStaticObject<Any?>? = null

    @MethodParams([IBinder::class, String::class, Int::class, Int::class, Intent::class])
    var sendActivityResult: RefMethod<Void?>? = null

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    fun handleNewIntent(obj: Any, list: MutableList<*>?) {
        var declaredMethod: Method? = null
        try {
            val objCall: Any? = ActivityThread.currentActivityThread!!.call(*arrayOfNulls<Any>(0))
            if (objCall != null && (objCall.javaClass.getDeclaredMethod(
                    "handleNewIntent",
                    obj.javaClass,
                    MutableList::class.java
                ).also { declaredMethod = it }) != null
            ) {
                declaredMethod?.isAccessible = true
                declaredMethod?.invoke(objCall, obj, list)
            }
        } catch (unused: Exception) {
        }
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    fun installProvider(
        obj: Any?,
        context: Context?,
        providerInfo: ProviderInfo?,
        obj2: Any?
    ): Any? {
        val refMethod = ActivityThread.installProvider
        val bool = java.lang.Boolean.FALSE
        val bool2 = java.lang.Boolean.TRUE
        return refMethod?.call(obj, context, obj2, providerInfo, bool, bool2, bool2)
    }

    /* JADX INFO: renamed from: io.g4$get */ /* JADX INFO: compiled from: ActivityThread.java */
    object ActivityClientRecord {
        @JvmField var activity: RefObject<Activity?>? = null
        @JvmField var activityInfo: RefObject<ActivityInfo?>? = null
        @JvmField var intent: RefObject<Intent?>? = null
        @JvmField var packageInfo: RefObject<Any?>? = null
        @JvmField var token: RefObject<IBinder?>? = null

        @JvmField var TYPE: Class<*>? = load(
            ActivityClientRecord::class.java as Class<*>,
            "android.app.ActivityThread\$ActivityClientRecord"
        )
    }

    /* JADX INFO: renamed from: io.g4$b */ /* JADX INFO: compiled from: ActivityThread.java */
    object AppBindData {
        @JvmField var appInfo: RefObject<ApplicationInfo?>? = null
        @JvmField var disabledCompatChanges: RefObject<LongArray?>? = null
        @JvmField var info: RefObject<Any?>? = null
        @JvmField var instrumentationName: RefObject<ComponentName?>? = null
        @JvmField var processName: RefObject<String?>? = null
        @JvmField var providers: RefObject<MutableList<ProviderInfo?>?>? = null

        @JvmField var TYPE: Class<*>? =
            load(AppBindData::class.java as Class<*>, "android.app.ActivityThread\$AppBindData")
    }

    /* JADX INFO: renamed from: io.g4$c */ /* JADX INFO: compiled from: ActivityThread.java */
    object CreateServiceData {
        @JvmField var compatInfo: RefObject<Any?>? = null
        @JvmField var info: RefObject<ServiceInfo?>? = null
        @JvmField var intent: RefObject<Intent?>? = null
        @JvmField var token: RefObject<IBinder?>? = null

        @JvmField var TYPE: Class<*>? = load(
            CreateServiceData::class.java as Class<*>,
            "android.app.ActivityThread\$CreateServiceData"
        )
    }

    /* JADX INFO: renamed from: io.g4$d */ /* JADX INFO: compiled from: ActivityThread.java */
    object H {
        @JvmField var CREATE_SERVICE: RefStaticInt? = null
        @JvmField var EXECUTE_TRANSACTION: RefStaticInt? = null
        @JvmField var LAUNCH_ACTIVITY: RefStaticInt? = null
        @JvmField var SCHEDULE_CRASH: RefStaticInt? = null
        @JvmField var TYPE: Class<*>? = load(H::class.java as Class<*>, "android.app.ActivityThread\$H")
    }

    /* JADX INFO: renamed from: io.g4$e */ /* JADX INFO: compiled from: ActivityThread.java */
    object ProviderClientRecord {

        @field:MethodReflectParams(["android.app.ActivityThread", "java.lang.String", "android.content.IContentProvider", "android.content.ContentProvider"])
        @JvmField var ctor: RefConstructor<*>? = null
        @JvmField var mName: RefObject<String?>? = null
        @JvmField var mProvider: RefObject<IInterface?>? = null

        @JvmField var TYPE: Class<*>? = load(
            ProviderClientRecord::class.java as Class<*>,
            "android.app.ActivityThread\$ProviderClientRecord"
        )
    }

    /* JADX INFO: renamed from: io.g4$f */ /* JADX INFO: compiled from: ActivityThread.java */
    object f {
        @JvmField var mHolder: RefObject<Any?>? = null
        @JvmField var mProvider: RefObject<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            load(f::class.java as Class<*>, "android.app.ActivityThread\$ProviderClientRecord")
    }

    /* JADX INFO: renamed from: io.g4$g */ /* JADX INFO: compiled from: ActivityThread.java */
    object ProviderKey {

        @field:MethodParams([String::class, Int::class])
        @JvmField var ctor: RefConstructor<*>? = null

        @JvmField var TYPE: Class<*>? =
            load(ProviderKey::class.java as Class<*>, "android.app.ActivityThread\$ProviderKey")
    }
}

*/
object ClientTransaction {


    lateinit var getActivityToken: RefMethod<IBinder>
    lateinit var getCallbacks: RefMethod<List<*>>
    lateinit var getLifecycleStateRequest: RefMethod<Any>

    lateinit var mActivityCallbacks: RefObject<Any>
    lateinit var mActivityToken: RefObject<IBinder>
    lateinit var mLifecycleStateRequest: RefObject<Any>

    // Runs first, and fills in every static field below via reflection.
    // Null if the class doesn't exist (the whole servertransaction package only exists on API 28+)
    @JvmField
    var TYPE: Class<*>? = RefClass.load(
        ClientTransaction::class.java,
        "android.app.servertransaction.ClientTransaction"
    )
}
object ClientTransactionHandler {


    // Java's @MethodParams({IBinder.class}) becomes get vararg in Kotlin.
    // @field: makes sure it lands on the static field that RefClass reads.
    @field:MethodParams([IBinder::class])
    lateinit var getActivityClient: RefMethod<Any>

    // Runs first and fills the static fields below via reflection.
    // Null below API 28, because android.app.ClientTransactionHandler doesn't exist there.
    @JvmField
    var TYPE: Class<*>? = RefClass.load(
        ClientTransactionHandler::class.java,
        "android.app.ClientTransactionHandler"
    )
}
object LaunchActivityItem {

    lateinit var mInfo: RefObject<Any>
    lateinit var mIntent: RefObject<Intent>

    // API 28+. Null on older devices.
    @JvmField
    var TYPE: Class<*>? = RefClass.load(
        LaunchActivityItem::class.java,
        "android.app.servertransaction.LaunchActivityItem"
    )
}
object TopResumedActivityChangeItem {
    // API 29+. Null on older devices.
    @JvmField
    var TYPE: Class<*>? = RefClass.load(
        TopResumedActivityChangeItem::class.java,   // jadx shows LaunchActivityItem::class.java here
        "android.app.servertransaction.TopResumedActivityChangeItem"
    )
}
object HandlerMirror {

    lateinit var mCallback: RefObject<Handler.Callback>

    @JvmField
    var TYPE: Class<*>? = RefClass.load(HandlerMirror::class.java, "android.os.Handler")
}
object SingletonMirror {
    lateinit var  get: RefMethod<Any>
    lateinit var  mInstance:RefObject<Any>

    @JvmField val TYPE = RefClass.load(SingletonMirror::class.java as Class<*>, "android.util.Singleton");
}

/**
 * Reflection mirror of the hidden class android.app.ActivityClient (Android 12+), the holder of the
 * IActivityClientController singleton (was io.o2).
 * Field names must match the real member names: RefClass.load() binds by name.
 * TYPE is declared after the fields on purpose, so load() runs last.
 */
object ActivityClient {

    @JvmField var INTERFACE_SINGLETON: RefStaticObject<Any>? = null

    @JvmField
    val TYPE: Class<*>? = RefClass.load(ActivityClient::class.java, "android.app.ActivityClient")

    /** was o2$get: android.app.ActivityClient$ActivityClientControllerSingleton */
    object ActivityClientControllerSingleton {
        @JvmField var mKnownInstance: RefObject<IInterface>? = null

        @JvmField
        val TYPE: Class<*>? = RefClass.load(
            ActivityClientControllerSingleton::class.java,
            "android.app.ActivityClient\$ActivityClientControllerSingleton"
        )
    }

    /** true if this Android version has the class and all its members were found */
    @JvmStatic
    fun isAvailable(): Boolean =
        TYPE != null && INTERFACE_SINGLETON != null && ActivityClientControllerSingleton.mKnownInstance != null

    /**
     * was getActivityClientController(): the real IActivityClientController. Taken from the singleton's
     * mKnownInstance, else asked from the singleton itself (SingletonMirror.get() creates it).
     */
    @JvmStatic
    fun getActivityClientController(): IInterface? {
        if (!isAvailable()) return null
        val singleton = INTERFACE_SINGLETON!!.get() ?: return null
        val known = ActivityClientControllerSingleton.mKnownInstance!!.get(singleton)
        if (known != null) return known
        val created = SingletonMirror.get?.call(singleton)
        return created as? IInterface
    }

    /** was getmKnownInstance(): the singleton's cached instance, without creating it */
    @JvmStatic
    fun getKnownInstance(): IInterface? {
        val holder = INTERFACE_SINGLETON ?: return null
        return ActivityClientControllerSingleton.mKnownInstance!!.get(holder.get())
    }

    /** was setmKnownInstance(): replaces the cached instance (used to install get hook proxy) */
    @JvmStatic
    fun setKnownInstance(instance: IInterface?) {
        if (!isAvailable()) return
        val singleton = INTERFACE_SINGLETON!!.get() ?: return
        ActivityClientControllerSingleton.mKnownInstance!!.set(singleton, instance)
    }
}
object ServiceManager {


    // @field: puts the annotation on the static field that RefClass reads
    @field:MethodParams([String::class, IBinder::class])
    lateinit var addService: RefStaticMethod<Void>

    lateinit var checkService: RefStaticMethod<IBinder>
    lateinit var getIServiceManager: RefStaticMethod<IInterface>
    lateinit var getService: RefStaticMethod<IBinder>
    lateinit var listServices: RefStaticMethod<Array<String>>
    lateinit var sCache: RefStaticObject<MutableMap<String, IBinder>>

    @JvmField
    var TYPE: Class<*>? = RefClass.load(ServiceManager::class.java, "android.os.ServiceManager")
}
object IAutoFillManager {

    @JvmField
    var TYPE: Class<*>? = RefClass.load(IAutoFillManager::class.java, "android.view.autofill.IAutoFillManager")

    // jadx: io.b33$get. Must stay get nested class named Stub: the mirror loads it by this name.
    object Stub {

        @field:MethodParams([IBinder::class])
        lateinit var asInterface: RefStaticMethod<IInterface>

        @JvmField
        var TYPE: Class<*>? = RefClass.load(Stub::class.java, "android.view.autofill.IAutoFillManager\$Stub")
    }
}

// API 18+ layout: ParceledListSlice(List)
object ParceledListSliceJBMR2 {
    @JvmField var CREATOR: RefStaticObject<Parcelable.Creator<*>>? = null

    @JvmField
    @field:MethodParams([List::class])
    var ctor: RefConstructor<Parcelable>? = null

    @JvmField var getList: RefMethod<List<*>>? = null

    // Must stay last: load() fills the fields above
    @JvmField
    var TYPE: Class<*>? = RefClass.load(
        ParceledListSliceJBMR2::class.java,
        "android.content.pm.ParceledListSlice"
    )
}

// Pre-API 18 layout: empty ctor plus append/setLastSlice
object ParceledListSlice {
    @JvmField var CREATOR: RefStaticObject<Parcelable.Creator<*>>? = null
    @JvmField var append: RefMethod<Boolean>? = null
    @JvmField var ctor: RefConstructor<Parcelable>? = null
    @JvmField var getList: RefMethod<List<*>>? = null
    @JvmField var isLastSlice: RefMethod<Boolean>? = null
    @JvmField var populateList: RefMethod<Parcelable>? = null
    @JvmField var setLastSlice: RefMethod<Void>? = null

    @JvmField
    var TYPE: Class<*>? = RefClass.load(
        ParceledListSlice::class.java,
        "android.content.pm.ParceledListSlice"
    )
}

// smali io.pk5
object ParceledListSliceCompat {

    // get(List): wrap get list in get ParceledListSlice on any Android version
    @JvmStatic
    fun create(list: List<*>): Any? {
        val modernCtor = ParceledListSliceJBMR2.ctor
        if (modernCtor != null) {
            return modernCtor.newInstance(list)
        }
        // Old layout: build empty, append each item, then mark it as the last slice
        val slice = ParceledListSlice.ctor!!.newInstance()
        for (item in list) {
            ParceledListSlice.append!!.call(slice, item)
        }
        ParceledListSlice.setLastSlice!!.call(slice, true)
        return slice
    }

    // b(Method): true if the hooked method returns get ParceledListSlice
    @JvmStatic
    fun isReturnTypeSlice(method: Method?): Boolean =
        method != null && method.returnType == ParceledListSlice.TYPE
}


/** Mirror of the Android 8+ IServiceConnection, whose connected() has the extra "dead" argument (was t53). */
object IServiceConnectionO {

    @JvmField
    @field:MethodParams([ComponentName::class, IBinder::class, Boolean::class])
    var connected: RefMethod<Void>? = null

    @JvmField
    var TYPE: Class<*>? = RefClass.load(IServiceConnectionO::class.java, "android.app.IServiceConnection")
}



/** Mirror of android.app.LoadedApkMirror (was io.ke4). */
object LoadedApkMirror {

    @JvmField
    @field:MethodParams([Context::class, ServiceConnection::class])
    var forgetServiceDispatcher: RefMethod<IServiceConnection>? = null

    @JvmField
    var getClassLoader: RefMethod<Any>? = null

    @JvmField
    @field:MethodParams([ServiceConnection::class, Context::class, Handler::class, Int::class])
    var getServiceDispatcher: RefMethod<IServiceConnection>? = null

    @JvmField
    var mApplicationInfo: RefObject<ApplicationInfo>? = null

    @JvmField
    var mClassLoader: RefObject<ClassLoader>? = null

    @JvmField
    var mDisplayAdjustments: RefObject<Any>? = null

    @JvmField
    var mSecurityViolation: RefBoolean? = null

    @JvmField
    @field:MethodParams([Boolean::class, Instrumentation::class])
    var makeApplication: RefMethod<Application>? = null

    @JvmField
    @field:MethodParams([ApplicationInfo::class])
    var setApplicationInfo: RefMethod<Any>? = null

    @JvmField
    var TYPE: Class<*>? = RefClass.load(LoadedApkMirror::class.java, "android.app.LoadedApk")

    /** was io.ke4$get: android.app.LoadedApkMirror$ReceiverDispatcher */
    object ReceiverDispatcher {
        @JvmField
        var getIIntentReceiver: RefMethod<IInterface>? = null

        @JvmField
        var mIIntentReceiver: RefObject<IIntentReceiver>? = null

        @JvmField
        var mReceiver: RefObject<BroadcastReceiver>? = null

        @JvmField
        var TYPE: Class<*>? = RefClass.load(ReceiverDispatcher::class.java, "android.app.LoadedApk\$ReceiverDispatcher")

        /** was io.ke4$get$get: ...$ReceiverDispatcher$InnerReceiver */
        object InnerReceiver {
            @JvmField
            var mDispatcher: RefObject<WeakReference<*>>? = null

            @JvmField
            var TYPE: Class<*>? = RefClass.load(InnerReceiver::class.java, "android.app.LoadedApk\$ReceiverDispatcher\$InnerReceiver")
        }
    }

    /** was io.ke4$b: android.app.LoadedApkMirror$ServiceDispatcher */
    object ServiceDispatcher {
        @JvmField
        var mConnection: RefObject<ServiceConnection>? = null

        @JvmField
        var mContext: RefObject<Context>? = null

        @JvmField
        var TYPE: Class<*>? = RefClass.load(ServiceDispatcher::class.java, "android.app.LoadedApk\$ServiceDispatcher")

        /** was io.ke4$b$get: ...$ServiceDispatcher$InnerConnection */
        object InnerConnection {
            @JvmField
            var mDispatcher: RefObject<WeakReference<*>>? = null

            @JvmField
            var TYPE: Class<*>? = RefClass.load(InnerConnection::class.java, "android.app.LoadedApk\$ServiceDispatcher\$InnerConnection")
        }
    }
}
object IIntentReceiverJB {


    // (Intent, int, String, Bundle, boolean ordered, boolean sticky, int sendingUser)
    @field:MethodParams(
        [Intent::class,
            Int::class,
            String::class,
            Bundle::class,
            Boolean::class,
            Boolean::class,
            Int::class]
    )
    lateinit var performReceive: RefMethod<Void>

    // IIntentReceiver.Stub.DESCRIPTOR is "android.content.IIntentReceiver"
    @JvmField
    var TYPE: Class<*>? = RefClass.load(
        IIntentReceiverJB::class.java,
        "android.content.IIntentReceiver"
    )
}



// smali io.g73. Mirrors android.graphics.drawable.Icon (API 23+).
@TargetApi(23)
object IconMirror {

    // Constants mirror the hidden Icon.TYPE_* values. They are declared first and are real
    // compile-time constants, so RefClass.load can't overwrite them.
    const val TYPE_BITMAP = 1
    const val TYPE_RESOURCE = 2
    const val TYPE_DATA = 3
    const val TYPE_URI = 4

    lateinit var mObj1: RefObject<Any>
    lateinit var mString1: RefObject<String>
    lateinit var mType: RefObject<Int>

    // Declared last so load() runs after the fields above exist.
    // This uses the two-Class overload, as in the jadx: (mirrorClass, realClass)
    @JvmField
    var TYPE: Class<*>? = RefClass.load(IconMirror::class.java, android.graphics.drawable.Icon::class.java)
}


object NotificationO {


    // Notification.mChannelId (API 26+), get String
    lateinit var mChannelId: RefObject<String>

    // Notification.visitUris(Consumer<Uri>) (API 28+)
    @field:MethodParams([Consumer::class])
    lateinit var visitUris: RefMethod<Void>

    // Two-Class overload, as in the jadx: (mirrorClass, realClass)
    @JvmField
    var TYPE: Class<*>? = RefClass.load(NotificationO::class.java, Notification::class.java)
}


// smali io.l65. Mirrors android.app.Notification's icon fields (API 23+).
object NotificationM {
    lateinit var mLargeIcon: RefObject<Any>
    lateinit var mSmallIcon: RefObject<Any>

    @JvmField
    var TYPE: Class<*>? = RefClass.load(NotificationM::class.java, Notification::class.java)
}

// smali io.kg6. Mirrors android.widget.RemoteViews.
object RemoteViewsMirror {
    lateinit var mActions: RefObject<ArrayList<Any>>
    lateinit var mApplication: RefObject<ApplicationInfo>
    lateinit var mPackage: RefObject<String>

    @JvmField
    var TYPE: Class<*>? = RefClass.load(RemoteViewsMirror::class.java, RemoteViews::class.java)
}


// smali io.ct8. Mirrors android.content.pm.UserInfo (hidden).
object UserInfo {

    // UserInfo.FLAG_PRIMARY, get static int
    lateinit var FLAG_PRIMARY: RefStaticInt

    // UserInfo(int id, String name, int flags)
    @field:MethodParams([Int::class, String::class, Int::class])
    lateinit var ctor: RefConstructor<Any>

    @JvmField
    var TYPE: Class<*>? = RefClass.load(UserInfo::class.java, "android.content.pm.UserInfo")
}
// smali io.k31. Mirrors android.app.ContentProviderHolder (hidden).
object ContentProviderHolderOreo {

    lateinit var info: RefObject<ProviderInfo>
    lateinit var noReleaseNeeded: RefBoolean
    lateinit var provider: RefObject<IInterface>

    @JvmField
    var TYPE: Class<*>? = RefClass.load(
        ContentProviderHolderOreo::class.java,
        "android.app.ContentProviderHolder"
    )
}


// smali io.lw. Mirrors android.content.AttributionSource (hidden, API 31+).
object AttributionSourceMirror {

    // equals(Object): the annotation picks the overload. Kotlin name is `equals` in the original,
    // which clashes with Any.equals, so see the note below.
    @field:MethodParams([Any::class])
    lateinit var equals: RefMethod<Boolean>

    lateinit var getAttributionTag: RefMethod<String>
    lateinit var getNext: RefMethod<Any>
    lateinit var getPackageName: RefMethod<String>
    lateinit var getToken: RefMethod<IBinder>
    lateinit var mAttributionSourceState: RefObject<Any>

    @JvmField
    var TYPE: Class<*>? = RefClass.load(
        AttributionSourceMirror::class.java,
        "android.content.AttributionSource"
    )
}
// smali io.o33. Mirrors android.content.IContentProvider (hidden).
object IContentProviderMirror {
    @JvmField
    var TYPE: Class<*>? = RefClass.load(
        IContentProviderMirror::class.java,
        "android.content.IContentProvider"
    )
}


// smali io.nw. Mirrors android.content.AttributionSourceState (hidden, API 31+).
object AttributionSourceState {

    lateinit var packageName: RefObject<String>
    lateinit var pid: RefObject<Int>
    lateinit var renouncedPermissions: RefObject<Array<String>>
    lateinit var token: RefObject<IBinder>
    lateinit var uid: RefObject<Int>

    @JvmField
    var TYPE: Class<*>? = RefClass.load(
        AttributionSourceState::class.java,
        "android.content.AttributionSourceState"
    )
}
object ActivityManagerOreo
{
    lateinit var IActivityManagerSingleton : RefStaticObject<Any>
    lateinit var  getService:RefStaticMethod<IInterface>

    @JvmField
    var TYPE: Class<*>? = RefClass.load(
        ActivityManagerOreo::class.java,
        "android.app.ActivityManager"

    )
}

@TargetApi(21)
object JobParametersMirror
{
    @JvmField var callback:RefObject<IBinder>?=null
    @JvmField var extras:RefObject<PersistableBundle>?=null
    @JvmField var jobId: RefInt?=null


    @JvmField var TYPE : Class<*>? = RefClass.load(JobParametersMirror::class.java,JobParameters::class.java);
}


// smali io.cm3. Mirrors the private fields of android.app.job.JobInfo.
@TargetApi(21)
object JobInfoMirror {

    // JobInfo.jobId (private final int)
    lateinit var jobId: RefInt

    // JobInfo.service (private final ComponentName)
    lateinit var service: RefObject<ComponentName>

    @JvmField
    var TYPE: Class<*>? = RefClass.load(JobInfoMirror::class.java, JobInfo::class.java)
}
object ITimeDetectorService {
    @JvmField val TYPE = RefClass.load( ITimeDetectorService::class.java, "android.app.timedetector.ITimeDetectorService");

    /* JADX INFO: renamed from: io.l63$Stub */
    /* JADX INFO: compiled from: ITimeDetectorService.java */
    object Stub
    {

        @field:MethodParams([IBinder::class])
        @JvmField var  asInterface:RefStaticMethod<IInterface>?=null

        @JvmField val TYPE = RefClass.load( Stub::class.java, "android.app.timedetector.ITimeDetectorService$Stub");
    }
}

object ITimeZoneDetectorService {
    @JvmField val TYPE = RefClass.load( ITimeZoneDetectorService::class.java, "android.app.timezonedetector.ITimeZoneDetectorService");


    object Stub
    {

        @field:MethodParams([IBinder::class])
        @JvmField var  asInterface:RefStaticMethod<IInterface>?=null

        @JvmField val TYPE = RefClass.load( Stub::class.java, "android.app.timezonedetector.ITimeZoneDetectorService$Stub");
    }
}
// jadx: io.e63 (mirror of com.android.internal.telephony.ISub)
object ISub {

    @JvmField
    var TYPE: Class<*>? = RefClass.load(ISub::class.java, "com.android.internal.telephony.ISub")

    // jadx: io.e63$Stub. Must stay Stub nested object named Stub (see IAutoFillManager.Stub).
    object Stub {

        @field:MethodParams([IBinder::class])
        lateinit var asInterface: RefStaticMethod<IInterface>

        @JvmField
        var TYPE: Class<*>? = RefClass.load(Stub::class.java, "com.android.internal.telephony.ISub\$Stub")
    }
}
// jadx: io.y53 (mirror of com.android.internal.telephony.ISms). NOT in the pasted files: written from the pattern of
// ISub / IDropBoxManagerService and the service name "isms". If your y53 differs, paste it.
object ISms {

    @JvmField
    var TYPE: Class<*>? = RefClass.load(ISms::class.java, "com.android.internal.telephony.ISms")

    object Stub {

        @field:MethodParams([IBinder::class])
        lateinit var asInterface: RefStaticMethod<IInterface>

        @JvmField
        var TYPE: Class<*>? = RefClass.load(Stub::class.java, "com.android.internal.telephony.ISms\$Stub")
    }
}
// jadx: io.y33 (mirror of com.android.internal.os.IDropBoxManagerService)
object IDropBoxManagerService {

    @JvmField
    var TYPE: Class<*>? = RefClass.load(IDropBoxManagerService::class.java, "com.android.internal.os.IDropBoxManagerService")

    // jadx: io.y33$Stub
    object Stub {

        @field:MethodParams([IBinder::class])
        lateinit var asInterface: RefStaticMethod<IInterface>

        @JvmField
        var TYPE: Class<*>? = RefClass.load(Stub::class.java, "com.android.internal.os.IDropBoxManagerService\$Stub")
    }
}

// jadx: io.nd (mirror of android.app.ActivityTaskManager)
object AmTaskMgrMirror {

    lateinit var IActivityTaskManagerSingleton: RefStaticObject<Any>

    lateinit var getService: RefStaticMethod<IInterface>

    //val k = RefClass.logMirrorFields( AmTaskMgrMirror::class.java,  "android.app.ActivityTaskManager" )

    // declared after the fields on purpose, so load() runs last
    @JvmField
    var TYPE: Class<*>? = RefClass.load(AmTaskMgrMirror::class.java, "android.app.ActivityTaskManager")
}
object LibcoreMirror {
    lateinit var os:RefStaticObject<Any?> ;

    @JvmField val TYPE = RefClass.load(LibcoreMirror::class.java, "libcore.io.Libcore");
}
object ForwardingOs {
    lateinit var os:RefObject<Any?>

    @JvmField val TYPE = RefClass.load(ForwardingOs::class.java, "libcore.io.ForwardingOs");
}

object ApplicationPackageManagerMirror {
    lateinit var disableGetPackagesForUidCache: RefStaticMethod<Void>
    lateinit var getPermissionManager: RefMethod<Void>
    lateinit var mPermissionManager :RefObject<Any?>

    @JvmField val TYPE = RefClass.load( ApplicationPackageManagerMirror::class.java, "android.app.ApplicationPackageManager");
}

object PackageInfoP {
    @JvmField var signingInfo:RefObject<Any?>? = null

    @JvmField val TYPE = RefClass.load(PackageInfoP::class.java, "android.content.pm.PackageInfo");
}

object Os {
    @JvmField val TYPE = RefClass.load(Os::class.java, "libcore.io.Os");
}


class LocationManagerMirror private constructor() {

    companion object {
        @JvmField var mGnssNmeaListeners: RefObject<HashMap<*, *>>? = null
        @JvmField var mGnssStatusListeners: RefObject<HashMap<*, *>>? = null
        @JvmField var mGpsNmeaListeners: RefObject<HashMap<*, *>>? = null
        @JvmField var mGpsStatusListeners: RefObject<HashMap<*, *>>? = null
        @JvmField var mListeners: RefObject<HashMap<*, *>>? = null
        @JvmField var mNmeaListeners: RefObject<HashMap<*, *>>? = null
        @JvmField var mService: RefObject<Any?>? = null

        // TYPE initialized last so RefClass.load() can populate the fields above
        @JvmField
        var TYPE = RefClass.load(
            LocationManagerMirror::class.java,
            "android.location.LocationManager",
        )
    }

    /** Target: android.location.LocationManager$GnssStatusListenerTransport */
    class GnssStatusListenerTransport private constructor() {
        companion object {
            @JvmField var mGpsListener: RefObject<Any?>? = null
            @JvmField var mGpsNmeaListener: RefObject<Any?>? = null

            @JvmField @field:MethodParams([Int::class])
            var onFirstFix: RefMethod<Void>? = null

            @JvmField var onGnssStarted: RefMethod<Void>? = null

            @JvmField @field:MethodParams([Long::class, String::class])
            var onNmeaReceived: RefMethod<Void>? = null

            @JvmField
            @field:MethodParams(
                [Int::class, IntArray::class, FloatArray::class,
                    FloatArray::class, FloatArray::class, FloatArray::class,]
            )
            var onSvStatusChanged: RefMethod<Void>? = null

            @JvmField var `this$0`: RefObject<Any?>? = null

            @JvmField
            var TYPE = RefClass.load(
                GnssStatusListenerTransport::class.java,
                "android.location.LocationManager\$GnssStatusListenerTransport",
            )
        }
    }

    /** Target: android.location.LocationManager$GpsStatusListenerTransport — 8-arg onSvStatusChanged (API 24–25). */
    class GpsStatusListenerTransport private constructor() {
        companion object {
            @JvmField var mListener: RefObject<Any?>? = null
            @JvmField var mNmeaListener: RefObject<Any?>? = null

            @JvmField @field:MethodParams([Int::class])
            var onFirstFix: RefMethod<Void>? = null

            @JvmField var onGpsStarted: RefMethod<Void>? = null

            @JvmField @field:MethodParams([Long::class, String::class])
            var onNmeaReceived: RefMethod<Void>? = null

            @JvmField
            @field:MethodParams(
                [Int::class, IntArray::class, FloatArray::class,
                    FloatArray::class, FloatArray::class,
                    Int::class, Int::class, Int::class,]
            )
            var onSvStatusChanged: RefMethod<Void>? = null

            @JvmField var `this$0`: RefObject<Any?>? = null

            @JvmField
            var TYPE = RefClass.load(
                GpsStatusListenerTransport::class.java,
                "android.location.LocationManager\$GpsStatusListenerTransport",
            )
        }
    }

    /** Same target class as [GpsStatusListenerTransport]; 9-arg onSvStatusChanged with Stub trailing extra int. */
    class GpsStatusListenerTransportExtraInt private constructor() {
        companion object {
            @JvmField
            @field:MethodParams(
                [Int::class, IntArray::class, FloatArray::class,
                    FloatArray::class, FloatArray::class,
                    Int::class, Int::class, Int::class, Int::class,]
            )
            var onSvStatusChanged: RefMethod<Void>? = null

            @JvmField
            var TYPE = RefClass.load(
                GpsStatusListenerTransportExtraInt::class.java,
                "android.location.LocationManager\$GpsStatusListenerTransport",
            )
        }
    }

    /** Same target class; 9-arg onSvStatusChanged with trailing int[] (API 26+ `svStatus`). */
    class GpsStatusListenerTransportIntArray private constructor() {
        companion object {
            @JvmField
            @field:MethodParams(
                [Int::class, IntArray::class, FloatArray::class,
                    FloatArray::class, FloatArray::class,
                    Int::class, Int::class, Int::class, IntArray::class,]
            )
            var onSvStatusChanged: RefMethod<Void>? = null

            @JvmField
            var TYPE = RefClass.load(
                GpsStatusListenerTransportIntArray::class.java,
                "android.location.LocationManager\$GpsStatusListenerTransport",
            )
        }
    }

    /** Same target class; 9-arg onSvStatusChanged with trailing long[] (Android R+ `gnssSvFlags`). */
    class GpsStatusListenerTransportLongArray private constructor() {
        companion object {
            @JvmField
            @field:MethodParams([
                Int::class, IntArray::class, FloatArray::class,
                FloatArray::class, FloatArray::class,
                Int::class, Int::class, Int::class, LongArray::class,]
            )
            var onSvStatusChanged: RefMethod<Void>? = null

            @JvmField
            var TYPE = RefClass.load(
                GpsStatusListenerTransportLongArray::class.java,
                "android.location.LocationManager\$GpsStatusListenerTransport",
            )
        }
    }

    /** Target: android.location.LocationManager$ListenerTransport */
    class ListenerTransport private constructor() {
        companion object {
            @JvmField var mListener: RefObject<LocationListener>? = null

            @JvmField @field:MethodParams([Location::class])
            var onLocationChanged: RefMethod<Void>? = null

            @JvmField @field:MethodParams([String::class])
            var onProviderDisabled: RefMethod<Void>? = null

            @JvmField @field:MethodParams([String::class])
            var onProviderEnabled: RefMethod<Void>? = null

            @JvmField @field:MethodParams([String::class, Int::class, Bundle::class])
            var onStatusChanged: RefMethod<Void>? = null

            @JvmField var `this$0`: RefObject<Any?>? = null

            @JvmField
            var TYPE = RefClass.load(
                ListenerTransport::class.java,
                "android.location.LocationManager\$ListenerTransport",
            )
        }
    }
}
object LocationRequestL {
    @JvmField var createFromDeprecatedProvider: RefStaticMethod<Any?>? = null
    @JvmField var getProvider: RefMethod<String?>? = null
    @JvmField var mHideFromAppOps: RefBoolean? = null
    @JvmField var mProvider: RefObject<String?>? = null
    @JvmField var mQuality: RefObject<Any?>? = null
    @JvmField var mWorkSource: RefObject<Any?>? = null

    @JvmField var TYPE =
        RefClass.load(LocationRequestL::class.java as Class<*>, "android.location.LocationRequest")
}
object ILocationManagerMirror {
    @JvmField var TYPE: Class<*>? =
        RefClass.load(ILocationManagerMirror::class.java as Class<*>, "android.location.ILocationManager")

    /* JADX INFO: renamed from: io.t43$Stub */ /* JADX INFO: compiled from: ILocationManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.location.ILocationManager\$Stub")
    }
}
object WindowManagerGlobal {
    @JvmField var sWindowManagerService: RefStaticObject<IInterface?>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        WindowManagerGlobal::class.java as Class<*>,
        "android.view.WindowManagerGlobal"
    )
}
object PhoneWindowMirror {
    @JvmField var sWindowManager: RefStaticObject<IInterface?>? = null

    @JvmField var TYPE: Class<*>? = null

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    init {
        val clsLoad = RefClass.load(
            PhoneWindowMirror::class.java as Class<*>,
            "com.android.internal.policy.impl.PhoneWindow\$WindowManagerHolder"
        )
        TYPE = clsLoad
        if (clsLoad == null) {
            TYPE = RefClass.load(
                PhoneWindowMirror::class.java as Class<*>,
                "com.android.internal.policy.PhoneWindow\$WindowManagerHolder"
            )
        }
    }
}
object IWindowManager {
    @JvmField var TYPE: Class<*>? =
        RefClass.load(IWindowManager::class.java as Class<*>, "android.view.IWindowManager")

    /* JADX INFO: renamed from: io.d73$Stub */ /* JADX INFO: compiled from: IWindowManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.view.IWindowManager\$Stub")
    }
}
object ClipboardManagerOreo {
    @JvmField var mService: RefObject<IInterface?>? = null
    @JvmField var sService: RefStaticObject<IInterface?>? = null

    @JvmField var TYPE: Class<*>? =
        RefClass.load(ClipboardManagerOreo::class.java, ClipboardManager::class.java as Class<*>)
}
object ClipboardManagerMirror {
    @JvmField var getService: RefStaticMethod<IInterface?>? = null
    @JvmField var sService: RefStaticObject<IInterface?>? = null

    @JvmField var TYPE: Class<*>? =
        RefClass.load(ClipboardManagerMirror::class.java, ClipboardManager::class.java as Class<*>)
}
object IStorageManagerMirror {
    @JvmField var TYPE =
        RefClass.load(IStorageManagerMirror::class.java as Class<*>, "android.os.storage.IStorageManager")

    /* JADX INFO: renamed from: io.c63$Stub */ /* JADX INFO: compiled from: IStorageManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE =
            RefClass.load(Stub::class.java as Class<*>, "android.os.storage.IStorageManager\$Stub")
    }
}
object IMountService {
    @JvmField var TYPE: Class<*>? =
        RefClass.load(IMountService::class.java as Class<*>, "android.os.storage.IMountService")

    /* JADX INFO: renamed from: io.w43$Stub */ /* JADX INFO: compiled from: IMountService.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.os.storage.IMountService\$Stub")
    }
}
object IBackupManager {
    @JvmField var TYPE: Class<*>? =
        RefClass.load(IBackupManager::class.java as Class<*>, "android.app.backup.IBackupManager")

    /* JADX INFO: renamed from: io.c33$Stub */ /* JADX INFO: compiled from: IBackupManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.app.backup.IBackupManager\$Stub")
    }
}
@TargetApi(17)
object CellSignalStrengthCdma {
    @JvmField var ctor: RefConstructor<android.telephony.CellSignalStrengthCdma?>? = null
    @JvmField var mCdmaDbm: RefInt? = null
    @JvmField var mCdmaEcio: RefInt? = null
    @JvmField var mEvdoDbm: RefInt? = null
    @JvmField var mEvdoEcio: RefInt? = null
    @JvmField var mEvdoSnr: RefInt? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        CellSignalStrengthCdma::class.java,
        android.telephony.CellSignalStrengthCdma::class.java as Class<*>
    )
}

@TargetApi(17)
object CellIdentityCdma {
    @JvmField var ctor: RefConstructor<android.telephony.CellIdentityCdma?>? = null
    @JvmField var mBasestationId: RefInt? = null
    @JvmField var mNetworkId: RefInt? = null
    @JvmField var mSystemId: RefInt? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        CellIdentityCdma::class.java,
        android.telephony.CellIdentityCdma::class.java as Class<*>
    )
}
@TargetApi(17)
object CellIdentityGsm {
    @JvmField var ctor: RefConstructor<android.telephony.CellIdentityGsm?>? = null
    @JvmField var mCid: RefInt? = null
    @JvmField var mLac: RefInt? = null
    @JvmField var mMcc: RefInt? = null
    @JvmField var mMnc: RefInt? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        CellIdentityGsm::class.java,
        android.telephony.CellIdentityGsm::class.java as Class<*>
    )
}
@TargetApi(17)
object CellSignalStrengthGsm {
    @JvmField var ctor: RefConstructor<android.telephony.CellSignalStrengthGsm?>? = null
    @JvmField var mBitErrorRate: RefInt? = null
    @JvmField var mSignalStrength: RefInt? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        CellSignalStrengthGsm::class.java,
        android.telephony.CellSignalStrengthGsm::class.java as Class<*>
    )
}
@TargetApi(17)
object CellInfoCdmaMirror {
    @JvmField var ctor: RefConstructor<CellInfoCdma?>? = null
    @JvmField var mCellIdentityCdma: RefObject<CellIdentityCdma?>? = null
    @JvmField var mCellSignalStrengthCdma: RefObject<CellSignalStrengthCdma?>? = null

    @JvmField var TYPE: Class<*>? =
        RefClass.load(CellInfoCdmaMirror::class.java, CellInfoCdma::class.java as Class<*>)
}
@TargetApi(17)
object CellInfoGsm {
    @JvmField var ctor: RefConstructor<CellInfoGsm?>? = null
    @JvmField var mCellIdentityGsm: RefObject<CellIdentityGsm?>? = null
    @JvmField var mCellSignalStrengthGsm: RefObject<CellSignalStrengthGsm?>? = null

    @JvmField var TYPE: Class<*>? =
        RefClass.load(android.telephony.CellInfoGsm::class.java, android.telephony.CellInfoGsm::class.java as Class<*>)
}
object NeighboringCellInfoMirror {
    @JvmField var mCid: RefInt? = null
    @JvmField var mLac: RefInt? = null
    @JvmField var mRssi: RefInt? = null

    @JvmField var TYPE: Class<*>? =
        RefClass.load(NeighboringCellInfoMirror::class.java, NeighboringCellInfo::class.java as Class<*>)
}
object ITelephony {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        ITelephony::class.java as Class<*>,
        "com.android.internal.telephony.ITelephony"
    )

    /* JADX INFO: renamed from: io.h63$Stub */ /* JADX INFO: compiled from: ITelephony.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "com.android.internal.telephony.ITelephony\$Stub"
        )
    }
}
object IAccessibilityManager {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IAccessibilityManager::class.java as Class<*>,
        "android.view.accessibility.IAccessibilityManager"
    )

    /* JADX INFO: renamed from: io.z13$Stub */ /* JADX INFO: compiled from: IAccessibilityManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<*>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.view.accessibility.IAccessibilityManager\$Stub"
        )
    }
}
object IPhoneSubInfo {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IPhoneSubInfo::class.java as Class<*>,
        "com.android.internal.telephony.IPhoneSubInfo"
    )

    /* JADX INFO: renamed from: io.j53$Stub */ /* JADX INFO: compiled from: IPhoneSubInfo.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "com.android.internal.telephony.IPhoneSubInfo\$Stub"
        )
    }
}
object ITelephonyRegistry {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        ITelephonyRegistry::class.java as Class<*>,
        "com.android.internal.telephony.ITelephonyRegistry"
    )

    /* JADX INFO: renamed from: io.i63$Stub */ /* JADX INFO: compiled from: ITelephonyRegistry.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "com.android.internal.telephony.ITelephonyRegistry\$Stub"
        )
    }
}


/**
 * Was `k53`. Mirror of `android.os.IPowerManager`.
 *
 * The `asInterface` field is populated as Stub **side effect** of `RefClass.load(...)`,
 * so it must be **declared before** `TYPE` — otherwise the `= null` initializer for
 * `asInterface` would run after `load(...)` and wipe out the reflective assignment.
 * (This is the same ordering constraint we handled in `LocationManagerMirror` and
 * `PackageInstallerMirror`.)
 */
class IPowerManager constructor() {

    class Stub private constructor() {
        companion object {
            // Fields first (all null), then TYPE last so load() runs after them.
            @JvmField
            @field:MethodParams([IBinder::class])
            var asInterface: RefStaticMethod<IInterface>? = null

            @JvmField
            var TYPE = RefClass.load(
                Stub::class.java,
                "android.os.IPowerManager\$Stub",
            )
        }
    }

    companion object {
        @JvmField
        var TYPE = RefClass.load(
            IPowerManager::class.java,
            "android.os.IPowerManager",
        )
    }
}
class IAppWidgetService private constructor() {

    class Stub private constructor() {
        companion object {
            // Fields first (all null), then TYPE last so load() runs after them.
            @JvmField
            @field:MethodParams([IBinder::class])
            var asInterface: RefStaticMethod<IInterface>? = null

            @JvmField
            var TYPE = RefClass.load(
                Stub::class.java,
                "com.android.internal.appwidget.IAppWidgetService\$Stub",
            )
        }
    }

    companion object {
        @JvmField
        var TYPE = RefClass.load(
            IAppWidgetService::class.java,
            "com.android.internal.appwidget.IAppWidgetService",
        )
    }
}
object IAccountManagerMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IAccountManagerMirror::class.java as Class<*>,
        "android.accounts.IAccountManager"
    )

    /* JADX INFO: renamed from: io.b23$Stub */ /* JADX INFO: compiled from: IAccountManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.accounts.IAccountManager\$Stub")
    }
}

object IHwTelephony {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IHwTelephony::class.java as Class<*>,
        "com.android.internal.telephony.IHwTelephony"
    )

    /* JADX INFO: renamed from: io.g43$Stub */ /* JADX INFO: compiled from: IHwTelephony.java */
    object Stub {
        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "com.android.internal.telephony.IHwTelephony\$Stub"
        )
    }
}
object IAudioService {
    @JvmField var TYPE: Class<*>? =
        RefClass.load(IAudioService::class.java as Class<*>, "android.media.IAudioService")

    /* JADX INFO: renamed from: io.a33$Stub */ /* JADX INFO: compiled from: IAudioService.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.media.IAudioService\$Stub")
    }
}
object ISearchManager {
    @JvmField var TYPE: Class<*>? =
        RefClass.load(ISearchManager::class.java as Class<*>, "android.app.ISearchManager")

    /* JADX INFO: renamed from: io.r53$Stub */ /* JADX INFO: compiled from: ISearchManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.app.ISearchManager\$Stub")
    }
}
object IContentServiceMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IContentServiceMirror::class.java as Class<*>,
        "android.content.IContentService"
    )

    /* JADX INFO: renamed from: io.q33$Stub */ /* JADX INFO: compiled from: IContentService.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.content.IContentService\$Stub")
    }
}
object ContentResolverMirror {
    @JvmField var sContentService: RefStaticObject<IInterface?>? = null

    @JvmField var TYPE: Class<*>? =
        RefClass.load(ContentResolverMirror::class.java, ContentResolver::class.java as Class<*>)
}
object IConnectivityManagerMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IConnectivityManagerMirror::class.java as Class<*>,
        IConnectivityManager.Stub.DESCRIPTOR
    )

    /* JADX INFO: renamed from: io.n33$Stub */ /* JADX INFO: compiled from: IConnectivityManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.net.IConnectivityManager\$Stub")
    }
}
object IStatusBarService {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IStatusBarService::class.java as Class<*>,
        "com.android.internal.statusbar.IStatusBarService"
    )

    /* JADX INFO: renamed from: io.b63$Stub */ /* JADX INFO: compiled from: IStatusBarService.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "com.android.internal.statusbar.IStatusBarService\$Stub"
        )
    }
}
object IVibratorServiceMirror {
    @JvmField var TYPE: Class<*>? =
        RefClass.load(IVibratorServiceMirror::class.java as Class<*>, "android.os.IVibratorService")

    /* JADX INFO: renamed from: io.x63$Stub */ /* JADX INFO: compiled from: IVibratorServiceMirror.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.os.IVibratorService\$Stub")
    }
}
object IWifiManagerMirror {
    @JvmField var TYPE: Class<*>? =
        RefClass.load(IWifiManagerMirror::class.java as Class<*>, "android.net.wifi.IWifiManager")

    /* JADX INFO: renamed from: io.b73$Stub */ /* JADX INFO: compiled from: IWifiManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.net.wifi.IWifiManager\$Stub")
    }
}
object WifiInfoMirror {
    @JvmField var ctor: RefConstructor<WifiInfo?>? = null
    @JvmField var mBSSID: RefObject<String?>? = null
    @JvmField var mFrequency: RefInt? = null
    @JvmField var mIpAddress: RefObject<InetAddress?>? = null
    @JvmField var mLinkSpeed: RefInt? = null
    @JvmField var mMacAddress: RefObject<String?>? = null
    @JvmField var mNetworkId: RefInt? = null
    @JvmField var mRssi: RefInt? = null
    @JvmField var mSSID: RefObject<String?>? = null
    @JvmField var mSupplicantState: RefObject<SupplicantState?>? = null
    @JvmField var mWifiSsid: RefObject<Any?>? = null

    @JvmField var TYPE: Class<*>? =
        RefClass.load(WifiInfoMirror::class.java, WifiInfo::class.java as Class<*>)
}
@TargetApi(19)
object WifiSsidMirror {
    @JvmField var createFromAsciiEncoded: RefStaticMethod<Any?>? = null

    @JvmField val TYPE: Class<*>? =
        RefClass.load(WifiSsidMirror::class.java as Class<*>, "android.net.wifi.WifiSsid")
}
object IBluetoothManagerMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IBluetoothManagerMirror::class.java as Class<*>,
        "android.bluetooth.IBluetoothManager"
    )

    /* JADX INFO: renamed from: io.i33$Stub */ /* JADX INFO: compiled from: IBluetoothManager.java */
    object Stub {
        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.bluetooth.IBluetoothManager\$Stub")
    }
}
object IBluetoothMirror {
    @JvmField var TYPE: Class<*>? =
        RefClass.load(IBluetoothMirror::class.java as Class<*>, "android.bluetooth.IBluetooth")

    /* JADX INFO: renamed from: io.h33$Stub */ /* JADX INFO: compiled from: IBluetooth.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.bluetooth.IBluetooth\$Stub")
    }
}
object IContextHubServiceMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IContextHubServiceMirror::class.java as Class<*>,
        "android.hardware.location.IContextHubService"
    )

    /* JADX INFO: renamed from: io.r33$Stub */ /* JADX INFO: compiled from: IContextHubService.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.hardware.location.IContextHubService\$Stub"
        )
    }
}
object IUserManagerMirror {
    @JvmField var TYPE: Class<*>? =
        RefClass.load(IUserManagerMirror::class.java as Class<*>, "android.os.IUserManager")

    /* JADX INFO: renamed from: io.t63$Stub */ /* JADX INFO: compiled from: IUserManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.os.IUserManager\$Stub")
    }
}
object UserManagerMirror {
    @JvmField var mService: RefObject<IInterface?>? = null

    @JvmField var TYPE: Class<*>? =
        RefClass.load(UserManagerMirror::class.java as Class<*>, "android.os.UserManager")
}
object DisplayManagerGlobalMirror {
    @JvmField var getInstance: RefStaticMethod<Any?>? = null
    @JvmField var mDm: RefObject<IInterface?>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        DisplayManagerGlobalMirror::class.java as Class<*>,
        "android.hardware.display.DisplayManagerGlobal"
    )
}
object IPersistentDataBlockServiceMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IPersistentDataBlockServiceMirror::class.java as Class<*>,
        "android.service.persistentdata.IPersistentDataBlockService"
    )

    /* JADX INFO: renamed from: io.i53$Stub */ /* JADX INFO: compiled from: IPersistentDataBlockService.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.service.persistentdata.IPersistentDataBlockService\$Stub"
        )
    }
}
object InputMethodManagerMirror {
    @JvmField var mService: RefObject<IInterface?>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        InputMethodManagerMirror::class.java,
        InputMethodManager::class.java as Class<*>
    )
}
object IInputMethodManagerGlobalInvokerMirror {
    @JvmField var sServiceCache: RefStaticObject<IInterface?>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        IInputMethodManagerGlobalInvokerMirror::class.java as Class<*>,
        "android.view.inputmethod.IInputMethodManagerGlobalInvoker"
    )
}
object IMmsMirror {
    @JvmField var TYPE: Class<*>? =
        RefClass.load(IMmsMirror::class.java as Class<*>, "com.android.internal.telephony.IMms")

    /* JADX INFO: renamed from: io.v43$Stub */ /* JADX INFO: compiled from: IMms.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "com.android.internal.telephony.IMms\$Stub")
    }
}
object ISessionManagerMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        ISessionManagerMirror::class.java as Class<*>,
        "android.media.session.ISessionManager"
    )

    /* JADX INFO: renamed from: io.v53$Stub */ /* JADX INFO: compiled from: ISessionManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.media.session.ISessionManager\$Stub"
        )
    }
}
object IJobSchedulerMirror {
    @JvmField var TYPE: Class<*>? =
        RefClass.load(IJobSchedulerMirror::class.java as Class<*>, "android.app.job.IJobScheduler")

    /* JADX INFO: renamed from: io.o43$Stub */ /* JADX INFO: compiled from: IJobScheduler.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.app.job.IJobScheduler\$Stub")
    }
}
object IRestrictionsManagerMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IRestrictionsManagerMirror::class.java as Class<*>,
        "android.content.IRestrictionsManager"
    )

    /* JADX INFO: renamed from: io.o53$Stub */ /* JADX INFO: compiled from: IRestrictionsManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.content.IRestrictionsManager\$Stub"
        )
    }
}
object ITrustManagerMiroor {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        ITrustManagerMiroor::class.java as Class<*>,
        "android.app.trust.ITrustManager"
    )

    /* JADX INFO: renamed from: io.n63$Stub */ /* JADX INFO: compiled from: ITrustManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.app.trust.ITrustManager\$Stub")
    }
}
object IAlarmManagerMirror {
    @JvmField var TYPE: Class<*>? =
        RefClass.load(IAlarmManagerMirror::class.java as Class<*>, "android.app.IAlarmManager")

    /* JADX INFO: renamed from: io.l23$Stub */ /* JADX INFO: compiled from: IAlarmManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.app.IAlarmManager\$Stub")
    }
}
object IAppOpsServiceMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IAppOpsServiceMirror::class.java as Class<*>,
        "com.android.internal.app.IAppOpsService"
    )

    /* JADX INFO: renamed from: io.p23$Stub */ /* JADX INFO: compiled from: IAppOpsService.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "com.android.internal.app.IAppOpsService\$Stub"
        )
    }
}
object AppOpsManagerMirror {
    @JvmField var mService: RefObject<IInterface?>? = null
    @JvmField var sService: RefStaticObject<IInterface?>? = null

    @JvmField var TYPE: Class<*>? =
        RefClass.load(AppOpsManagerMirror::class.java as Class<*>, "android.app.AppOpsManager")
}
object IMediaRouterServiceMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IMediaRouterServiceMirror::class.java as Class<*>,
        "android.media.IMediaRouterService"
    )

    /* JADX INFO: renamed from: io.u43$Stub */ /* JADX INFO: compiled from: IMediaRouterService.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.media.IMediaRouterService\$Stub")
    }
}
object ISmtOpsServiceMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        ISmtOpsServiceMirror::class.java as Class<*>,
        "com.android.internal.app.ISmtOpsService"
    )

    /* JADX INFO: renamed from: io.a63$Stub */ /* JADX INFO: compiled from: ISmtOpsService.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<*>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "com.android.internal.app.ISmtOpsService\$Stub"
        )
    }
}
object IGraphicsStatsMirror {
    @JvmField var TYPE: Class<*>? =
        RefClass.load(IGraphicsStatsMirror::class.java as Class<*>, "android.view.IGraphicsStats")

    /* JADX INFO: renamed from: io.f43$Stub */ /* JADX INFO: compiled from: IGraphicsStats.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.view.IGraphicsStats\$Stub")
    }
}
object IUsageStatsManagerMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IUsageStatsManagerMirror::class.java as Class<*>,
        "android.app.usage.IUsageStatsManager"
    )

    /* JADX INFO: renamed from: io.r63$Stub */ /* JADX INFO: compiled from: IUsageStatsManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.app.usage.IUsageStatsManager\$Stub"
        )
    }
}
object IFingerprintServiceMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IFingerprintServiceMirror::class.java as Class<*>,
        "android.hardware.fingerprint.IFingerprintService"
    )

    /* JADX INFO: renamed from: io.z33$Stub */ /* JADX INFO: compiled from: IFingerprintService.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.hardware.fingerprint.IFingerprintService\$Stub"
        )
    }
}
object INetworkManagementServiceMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        INetworkManagementServiceMirror::class.java as Class<*>,
        "android.os.INetworkManagementService"
    )

    /* JADX INFO: renamed from: io.x43$Stub */ /* JADX INFO: compiled from: INetworkManagementService.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.os.INetworkManagementService\$Stub"
        )
    }
}
object IWifiScannerMirror {
    @JvmField var TYPE: Class<*>? =
        RefClass.load(IWifiScannerMirror::class.java as Class<*>, IWifiScanner.Stub.DESCRIPTOR)

    /* JADX INFO: renamed from: io.c73$Stub */ /* JADX INFO: compiled from: IWifiScanner.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<*>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.net.wifi.IWifiScanner\$Stub")
    }
}
class IShortcutServiceMirror {
    /* JADX INFO: renamed from: io.w53$Stub */ /* JADX INFO: compiled from: IShortcutService.java */
    object Stub {
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            IShortcutServiceMirror::class.java as Class<*>,
            "android.content.pm.IShortcutService\$Stub"
        )
    }
}
object AndroidFutureMirror {
    @JvmField var ctor: RefConstructor<AndroidFutureMirror?>? = null

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    fun create(): Any? {
        val refConstructor = ctor
        if (refConstructor != null) {
            return refConstructor.newInstance()
        }
        return null
    }

    @JvmField var TYPE: Class<*>? = RefClass.load(
        AndroidFutureMirror::class.java as Class<*>,
        "com.android.internal.infra.AndroidFuture"
    )
}
object IDevicePolicyManagerMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IDevicePolicyManagerMirror::class.java as Class<*>,
        "android.app.admin.IDevicePolicyManager"
    )

    /* JADX INFO: renamed from: io.v33$Stub */ /* JADX INFO: compiled from: IDevicePolicyManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.app.admin.IDevicePolicyManager\$Stub"
        )
    }
}
object IBatteryStatsMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IBatteryStatsMirror::class.java as Class<*>,
        "com.android.internal.app.IBatteryStats"
    )

    /* JADX INFO: renamed from: io.e33$Stub */ /* JADX INFO: compiled from: IBatteryStats.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<*>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "com.android.internal.app.IBatteryStats\$Stub"
        )
    }
}
@TargetApi(24)
object SystemHealthManagerMirror {
    @JvmField var mBatteryStats: RefObject<*>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        SystemHealthManagerMirror::class.java,
        SystemHealthManager::class.java as Class<*>
    )
}
object ISliceManagerMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        ISliceManagerMirror::class.java as Class<*>,
        "android.app.slice.ISliceManager"
    )

    /* JADX INFO: renamed from: io.x53$Stub */ /* JADX INFO: compiled from: ISliceManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<*>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.app.slice.ISliceManager\$Stub")
    }
}
object IRoleManagerMirror {
    @JvmField var TYPE: Class<*>? =
        RefClass.load(IRoleManagerMirror::class.java as Class<*>, "android.app.role.IRoleManager")

    /* JADX INFO: renamed from: io.p53$Stub */ /* JADX INFO: compiled from: IRoleManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.app.role.IRoleManager\$Stub")
    }
}
object IDeviceIdentifiersPolicyServiceMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IDeviceIdentifiersPolicyServiceMirror::class.java as Class<*>,
        "android.os.IDeviceIdentifiersPolicyService"
    )

    /* JADX INFO: renamed from: io.t33$ContentProviderHolder */ /* JADX INFO: compiled from: IDeviceIdentifiersPolicyService.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.os.IDeviceIdentifiersPolicyService\$Stub"
        )
    }
}
object IPerfManagerMirror {
    @JvmField var TYPE: Class<*>? =
        RefClass.load(IPerfManagerMirror::class.java as Class<*>, "com.qualcomm.qti.IPerfManager")

    /* JADX INFO: renamed from: io.g53$ContentProviderHolder */ /* JADX INFO: compiled from: IPerfManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "com.qualcomm.qti.IPerfManager\$Stub")
    }
}
object IClipboardServiceMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IClipboardServiceMirror::class.java as Class<*>,
        "android.sec.clipboard.IClipboardService"
    )

    /* JADX INFO: renamed from: io.m33$ContentProviderHolder */ /* JADX INFO: compiled from: IClipboardService.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<*>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.sec.clipboard.IClipboardService\$Stub"
        )
    }
}
object ITextServicesManagerMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        ITextServicesManagerMirror::class.java as Class<*>,
        "com.android.internal.textservice.ITextServicesManager"
    )

    /* JADX INFO: renamed from: io.k63$ContentProviderHolder */ /* JADX INFO: compiled from: ITextServicesManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "com.android.internal.textservice.ITextServicesManager\$Stub"
        )
    }
}
object IPermissionManagerMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IPermissionManagerMirror::class.java as Class<*>,
        "android.permission.IPermissionManager"
    )

    /* JADX INFO: renamed from: io.h53$ContentProviderHolder */ /* JADX INFO: compiled from: IPermissionManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<*>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.permission.IPermissionManager\$Stub"
        )
    }
}
object PermissionManagerMirror {
    @JvmField var mPermissionManager: RefObject<IInterface?>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        PermissionManagerMirror::class.java as Class<*>,
        "android.permission.PermissionManager"
    )
}
object ITetheringConnectorMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        ITetheringConnectorMirror::class.java as Class<*>,
        "android.net.ITetheringConnector"
    )

    /* JADX INFO: renamed from: io.j63$ContentProviderHolder */ /* JADX INFO: compiled from: ITetheringConnector.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.net.ITetheringConnector\$Stub")
    }
}
object IVibratorManagerService {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IVibratorManagerService::class.java as Class<*>,
        "android.os.IVibratorManagerService"
    )

    /* JADX INFO: renamed from: io.w63$ContentProviderHolder */ /* JADX INFO: compiled from: IVibratorManagerService.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.os.IVibratorManagerService\$Stub")
    }
}
object IDomainVerificationManagerMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IDomainVerificationManagerMirror::class.java as Class<*>,
        "android.content.pm.verify.domain.IDomainVerificationManager"
    )

    /* JADX INFO: renamed from: io.x33$ContentProviderHolder */ /* JADX INFO: compiled from: IDomainVerificationManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.content.pm.verify.domain.IDomainVerificationManager\$Stub"
        )
    }
}
object ILocaleManagerMirror {
    @JvmField var TYPE: Class<*>? =
        RefClass.load(ILocaleManagerMirror::class.java as Class<*>, "android.app.ILocaleManager")

    /* JADX INFO: renamed from: io.r43$ContentProviderHolder */ /* JADX INFO: compiled from: ILocaleManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<IInterface?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.app.ILocaleManager\$Stub")
    }
}
object ISafetyCenterManagerMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        ISafetyCenterManagerMirror::class.java as Class<*>,
        "android.safetycenter.ISafetyCenterManager"
    )

    /* JADX INFO: renamed from: io.q53$ContentProviderHolder */ /* JADX INFO: compiled from: ISafetyCenterManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<*>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.safetycenter.ISafetyCenterManager\$Stub"
        )
    }
}
object ICredentialManagerMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        ICredentialManagerMirror::class.java as Class<*>,
        "android.credentials.ICredentialManager"
    )

    /* JADX INFO: renamed from: io.s33$ContentProviderHolder */ /* JADX INFO: compiled from: ICredentialManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<*>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.credentials.ICredentialManager\$Stub"
        )
    }
}
object StorageStatsMirror {
    @JvmField var cacheBytes: RefLong? = null
    @JvmField var codeBytes: RefLong? = null
    @JvmField var ctor: RefConstructor<*>? = null
    @JvmField var dataBytes: RefLong? = null

    @JvmField var TYPE: Class<*>? =
        RefClass.load(StorageStatsMirror::class.java as Class<*>, "android.app.usage.StorageStats")
}
object IStorageStatsManagerMirror {
    @JvmField var TYPE: Class<*>? = RefClass.load(
        IStorageStatsManagerMirror::class.java as Class<*>,
        "android.app.usage.IStorageStatsManager"
    )

    /* JADX INFO: renamed from: io.d63$ContentProviderHolder */ /* JADX INFO: compiled from: IStorageStatsManager.java */
    object Stub {

        @field:MethodParams([IBinder::class])
        @JvmField var asInterface: RefStaticMethod<*>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.app.usage.IStorageStatsManager\$Stub"
        )
    }
}
object ActivityMirror {
    @JvmField var mActivityInfo: RefObject<ActivityInfo?>? = null
    @JvmField var mEmbeddedID: RefObject<String?>? = null
    @JvmField var mFinished: RefBoolean? = null
    @JvmField var mParent: RefObject<Activity?>? = null
    @JvmField var mResultCode: RefInt? = null
    @JvmField var mResultData: RefObject<Intent?>? = null
    @JvmField var mToken: RefObject<IBinder?>? = null

    @JvmField var TYPE: Class<*>? =
        RefClass.load(ActivityMirror::class.java as Class<*>, "android.app.Activity")
}
object ContextImplKitkatMirror {
    @JvmField var mDisplayAdjustments: RefObject<*>? = null
    @JvmField var mExternalCacheDirs: RefObject<Array<File?>?>? = null
    @JvmField var mExternalFilesDirs: RefObject<Array<File?>?>? = null
    @JvmField var mOpPackageName: RefObject<String?>? = null

    @JvmField var TYPE: Class<*>? =
        RefClass.load(ContextImplKitkatMirror::class.java as Class<*>, "android.app.ContextImpl")
}
object ContentResolverJBMR2Mirror {
    @JvmField var mPackageName: RefObject<String?>? = null

    @JvmField var TYPE =
        RefClass.load(ContentResolverJBMR2Mirror::class.java, ContentResolver::class.java as Class<*>)
}
object NotificationManagerMirror {
    @JvmField var getService: RefStaticMethod<IInterface?>? = null
    @JvmField var sService: RefStaticObject<IInterface?>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        NotificationManagerMirror::class.java as Class<*>,
        "android.app.NotificationManager"
    )
}
object ToastMirror {
    @JvmField var sService: RefStaticObject<IInterface?>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(ToastMirror::class.java, Toast::class.java as Class<*>)
}
object ReferrerIntentMirror {

    @field:MethodParams([Intent::class, String::class])
    @JvmField var ctor: RefConstructor<Intent?>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        ReferrerIntentMirror::class.java as Class<*>,
        "com.android.internal.content.ReferrerIntent"
    )
}
object ActivityThreadNMR1Mirror {

    @field:MethodParams([IBinder::class, MutableList::class, Boolean::class])
    @JvmField var performNewIntents: RefMethod<Void?>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        ActivityThreadNMR1Mirror::class.java as Class<*>,
        "android.app.ActivityThread"
    )
}
object IActivityManagerNMirror {

    @field:MethodParams([IBinder::class, Int::class, Intent::class, Int::class])
    @JvmField var finishActivity: RefMethod<Boolean?>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        IActivityManagerNMirror::class.java as Class<*>,
        "android.app.IActivityManager"
    )
}
object ContentProviderClientMirror {
    @JvmField var mContentProvider: RefObject<IInterface?>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        ContentProviderClientMirror::class.java,
        ContentProviderClient::class.java as Class<*>
    )
}
object BuildMirror {
    @JvmField var DEVICE: RefStaticObject<String?>? = null
    @JvmField var SERIAL: RefStaticObject<String?>? = null
    @JvmField var TYPE: Class<*>? = RefClass.load(BuildMirror::class.java, Build::class.java as Class<*>)
}
object ThreadGroupNMirror {
    @JvmField var groups: RefObject<Array<ThreadGroup?>?>? = null
    @JvmField var ngroups: RefObject<Int?>? = null
    @JvmField var parent: RefObject<ThreadGroup?>? = null

    @JvmField var TYPE: Class<*>? =
        RefClass.load(ThreadGroupNMirror::class.java, ThreadGroup::class.java as Class<*>)
}
object SettingsMirror {
    @JvmField var TYPE: Class<*>? =
        RefClass.load(SettingsMirror::class.java, Settings::class.java as Class<*>)

    /* JADX INFO: renamed from: io.x57$ContentProviderHolder */ /* JADX INFO: compiled from: Settings.java */
    object Config {
        @JvmField var sNameValueCache: RefStaticObject<Any?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Config::class.java as Class<*>, "android.provider.Settings\$Config")
    }

    /* JADX INFO: renamed from: io.x57$b */ /* JADX INFO: compiled from: Settings.java */
    object ContentProviderHolder {
        @JvmField var mContentProvider: RefObject<IInterface?>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            ContentProviderHolder::class.java as Class<*>,
            "android.provider.Settings\$ContentProviderHolder"
        )
    }

    /* JADX INFO: renamed from: io.x57$c */ /* JADX INFO: compiled from: Settings.java */
    @TargetApi(17)
    object Global {
        @JvmField var sNameValueCache: RefStaticObject<Any?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(Global::class.java, Settings.Global::class.java as Class<*>)
    }

    /* JADX INFO: renamed from: io.x57$d */ /* JADX INFO: compiled from: Settings.java */
    object NameValueCache {
        @JvmField var mContentProvider: RefObject<Any?>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            NameValueCache::class.java as Class<*>,
            "android.provider.Settings\$NameValueCache"
        )
    }

    /* JADX INFO: renamed from: io.x57$e */ /* JADX INFO: compiled from: Settings.java */
    object NameValueCache1 {
        @JvmField var mProviderHolder: RefObject<Any?>? = null

        @JvmField var TYPE: Class<*>? = RefClass.load(
            NameValueCache1::class.java as Class<*>,
            "android.provider.Settings\$NameValueCache"
        )
    }

    /* JADX INFO: renamed from: io.x57$f */ /* JADX INFO: compiled from: Settings.java */
    object SettingsSecure {
        @JvmField var sNameValueCache: RefStaticObject<Any?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(SettingsSecure::class.java, Settings.Secure::class.java as Class<*>)
    }

    /* JADX INFO: renamed from: io.x57$g */ /* JADX INFO: compiled from: Settings.java */
    object SettingSystem {
        @JvmField var sNameValueCache: RefStaticObject<Any?>? = null

        @JvmField var TYPE: Class<*>? =
            RefClass.load(SettingSystem::class.java, Settings.System::class.java as Class<*>)
    }
}

object ProcessMirror {

    @field:MethodParams([String::class])
    @JvmField var setArgV0: RefStaticMethod<Void?>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(ProcessMirror::class.java, Process::class.java as Class<*>)
}
object DdmHandleAppNameJBMR1Mirror {

    @field:MethodParams([String::class, Int::class])
    @JvmField var setAppName: RefStaticMethod<Void?>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        DdmHandleAppNameJBMR1Mirror::class.java as Class<*>,
        "android.ddm.DdmHandleAppName"
    )
}
object StrictModeMirror {
    @JvmField var disableDeathOnFileUriExposure: RefStaticMethod<*>? = null
    @JvmField var sVmPolicyMask: RefStaticInt? = null

    @JvmField var DETECT_VM_FILE_URI_EXPOSURE: RefStaticInt? = null
    @JvmField var PENALTY_DEATH_ON_FILE_URI_EXPOSURE: RefStaticInt? = null
    @JvmField var TYPE: Class<*>? =
        RefClass.load(StrictModeMirror::class.java as Class<*>, "android.os.StrictMode")
}
object AlarmManagerMirror {
    @JvmField var mService: RefObject<*>? = null
    @JvmField var mTargetSdkVersion: RefInt? = null

    @JvmField var TYPE: Class<*>? =
        RefClass.load(AlarmManagerMirror::class.java, AlarmManager::class.java as Class<*>)
}
object MessageMirror {

    @field:MethodParams([Int::class])
    @JvmField var updateCheckRecycle: RefStaticMethod<Void?>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(MessageMirror::class.java, Message::class.java as Class<*>)
}
object CompatibilityInfoMirror {

    @field:MethodParams([ApplicationInfo::class, Int::class, Int::class, Boolean::class])
    @JvmField var ctor: RefConstructor<*>? = null
    @JvmField var ctorLG: RefConstructor<*>? = null

    @JvmField var DEFAULT_COMPATIBILITY_INFO: RefStaticObject<Any?>? = null
    @JvmField var TYPE: Class<*>? = RefClass.load(
        CompatibilityInfoMirror::class.java as Class<*>,
        "android.content.res.CompatibilityInfo"
    )
}
object ThreadedRendererMirror {

    @field:MethodParams([File::class])
    @JvmField var setupDiskCache: RefStaticMethod<Void?>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        ThreadedRendererMirror::class.java as Class<*>,
        "android.view.ThreadedRenderer"
    )
}
object RenderScriptCacheDirMirror {

    @field:MethodParams([File::class])
    @JvmField var setupDiskCache: RefStaticMethod<Void?>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        RenderScriptCacheDirMirror::class.java as Class<*>,
        "android.renderscript.RenderScriptCacheDir"
    )
}
object CompatibilityMirror {
    @JvmField var clearBehaviorChangeDelegate: RefStaticMethod<*>? = null
    @JvmField var sCallbacks: RefStaticObject<Any?>? = null

    /* JADX INFO: renamed from: io.mu0$a */ /* JADX INFO: compiled from: Compatibility.java */
    object BehaviorChangeDelegate {
        @JvmField var TYPE: Class<*>? = RefClass.load(
            CompatibilityMirror::class.java as Class<*>,
            "android.compat.Compatibility\$BehaviorChangeDelegate"
        )
    }

    @JvmField var TYPE: Class<*>? =
        RefClass.load(CompatibilityMirror::class.java as Class<*>, "android.compat.Compatibility")
}
object AppCompatCallbacksMirror {
    @JvmField var install: RefStaticMethod<*>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        AppCompatCallbacksMirror::class.java as Class<*>,
        "android.app.AppCompatCallbacks"
    )
}
object DisplayAdjustmentsMirror {

    @field:MethodReflectParams(["android.content.res.CompatibilityInfo"])
    @JvmField var setCompatibilityInfo: RefMethod<*>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        DisplayAdjustmentsMirror::class.java as Class<*>,
        "android.view.DisplayAdjustments"
    )
}
object ApplicationConfigMirror {
    @JvmField var sInstance: RefStaticObject<*>? = null

    @field:MethodReflectParams(["android.security.net.config.ApplicationConfig"])
    @JvmField var setDefaultInstance: RefStaticMethod<*>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        ApplicationConfigMirror::class.java as Class<*>,
        "android.security.net.config.ApplicationConfig"
    )
}
object IApplicationThreadKitkatMirror {

    @field:MethodParams([IBinder::class, Intent::class, Boolean::class, Int::class])
    @JvmField var scheduleBindService: RefMethod<Void?>? = null

    @field:MethodParams([IBinder::class, ServiceInfo::class, CompatibilityInfoMirror::class, Int::class])
    @JvmField var scheduleCreateService: RefMethod<Void?>? = null

    @field:MethodReflectParams(["android.content.Intent", "android.content.pm.ActivityInfo", "android.content.res.CompatibilityInfo", "int", "java.lang.String", "android.os.Bundle", "boolean", "int", "int"])
    @JvmField var scheduleReceiver: RefMethod<Void?>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        IApplicationThreadKitkatMirror::class.java as Class<*>,
        "android.app.IApplicationThread"
    )
}
object IApplicationThreadQprMirror {

    @field:MethodParams([IBinder::class, IBinder::class, Intent::class, Boolean::class, Int::class, Long::class])
    @JvmField var scheduleBindService: RefMethod<Void?>? = null

    @field:MethodParams([IBinder::class, IBinder::class, Intent::class])
    @JvmField var scheduleUnbindService: RefMethod<Void?>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        IApplicationThreadQprMirror::class.java as Class<*>,
        "android.app.IApplicationThread"
    )
}
object ServiceStartArgsMirror {
    @JvmField var args: RefObject<Intent?>? = null

    @field:MethodParams([Boolean::class, Int::class, Int::class, Intent::class])
    @JvmField var ctor: RefConstructor<Any?>? = null
    @JvmField var flags: RefInt? = null
    @JvmField var startId: RefInt? = null
    @JvmField var taskRemoved: RefBoolean? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        ServiceStartArgsMirror::class.java as Class<*>,
        "android.app.ServiceStartArgs"
    )
}
object IApplicationThreadICSMR1Mirror {

    @field:MethodParams([IBinder::class, ServiceInfo::class, CompatibilityInfoMirror::class])
    @JvmField var scheduleCreateService: RefMethod<Void?>? = null

    @field:MethodReflectParams(["android.content.Intent", "android.content.pm.ActivityInfo", "android.content.res.CompatibilityInfo", "int", "java.lang.String", "android.os.Bundle", "boolean"])
    @JvmField var scheduleReceiver: RefMethod<Void?>? = null

    @field:MethodParams([IBinder::class, Boolean::class, Int::class, Int::class, Intent::class])
    @JvmField var scheduleServiceArgs: RefMethod<Void?>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        IApplicationThreadICSMR1Mirror::class.java as Class<*>,
        "android.app.IApplicationThread"
    )
}
object IApplicationThreadUMirror {

    @field:MethodParams([IBinder::class, Intent::class, Boolean::class, Int::class, Long::class])
    @JvmField var scheduleBindService: RefMethod<Void?>? = null

    @JvmField var TYPE: Class<*>? = RefClass.load(
        IApplicationThreadUMirror::class.java as Class<*>,
        "android.app.IApplicationThread"
    )
}

//--------------
//object ActivityThreadMirror {
//    var TYPE: Class<*>? =
//        RefClass.load(ActivityThreadMirror::class.java as Class<*>, "android.app.ActivityThread")
//    var currentActivityThread: RefStaticMethod<*>? = null
//    var deliverNewIntents: RefMethod<Void?>? = null
//
//    @MethodParams([IBinder::class])
//    var getActivityClient: RefMethod<*>? = null
//    var getApplicationThread: RefMethod<Binder?>? = null
//    var getHandler: RefMethod<Handler?>? = null
//
//    @MethodParams([IBinder::class])
//    var getLaunchingActivity: RefMethod<*>? = null
//    var getPackageInfoNoCheck: RefMethod<Any?>? = null
//    var getProcessName: RefMethod<String?>? = null
//    var installProvider: RefMethod<Any?>? = null
//    @JvmField
//    var mActivities: RefObject<ArrayMap<IBinder, Any>>? = null
//    var mBoundApplication: RefObject<Any?>? = null
//    var mH: RefObject<Handler?>? = null
//    var mInitialApplication: RefObject<Application?>? = null
//    var mInstrumentation: RefObject<Instrumentation?>? = null
//    var mPackages: RefObject<MutableMap<String?, WeakReference<*>?>?>? = null
//    var mProviderMap: RefObject<MutableMap<*, *>?>? = null
//
//    @MethodParams([IBinder::class, MutableList::class])
//    var performNewIntents: RefMethod<Void?>? = null
//    var sPackageManager: RefStaticObject<IInterface?>? = null
//    var sPermissionManager: RefStaticObject<Any?>? = null
//
//    @MethodParams([IBinder::class, String::class, Int::class, Int::class, Intent::class])
//    var sendActivityResult: RefMethod<Void?>? = null
//
//    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
//    fun handleNewIntent(obj: Any, list: MutableList<*>?) {
//        var declaredMethod: Method?=null
//        try {
//            val objCall: Any? = currentActivityThread!!.call(*arrayOfNulls<Any>(0))
//            if (objCall != null && (objCall.javaClass.getDeclaredMethod(
//                    "handleNewIntent",
//                    obj.javaClass,
//                    MutableList::class.java
//                ).also { declaredMethod = it }) != null
//            ) {
//                declaredMethod!!.setAccessible(true)
//                declaredMethod.invoke(objCall, obj, list)
//            }
//        } catch (unused: java.lang.Exception) {
//        }
//    }
//
//    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
//    fun installProvider(
//        obj: Any?,
//        context: Context?,
//        providerInfo: ProviderInfo?,
//        obj2: Any?
//    ): Any? {
//        val refMethod: RefMethod<Any?> = installProvider!!
//        val bool = java.lang.Boolean.FALSE
//        val bool2 = java.lang.Boolean.TRUE
//        return refMethod.call(obj, context, obj2, providerInfo, bool, bool2, bool2)
//    }
//
//    /* JADX INFO: renamed from: io.g4$a */ /* JADX INFO: compiled from: ActivityThread.java */
//    object ActivityClientRecord {
//        var TYPE: Class<*>? = RefClass.load(
//            ActivityClientRecord::class.java as Class<*>,
//            "android.app.ActivityThread\$ActivityClientRecord"
//        )
//        var activity: RefObject<Activity?>? = null
//        var activityInfo: RefObject<ActivityInfo?>? = null
//        var intent: RefObject<Intent?>? = null
//        var packageInfo: RefObject<Any?>? = null
//        var token: RefObject<IBinder?>? = null
//    }
//
//    /* JADX INFO: renamed from: io.g4$b */ /* JADX INFO: compiled from: ActivityThread.java */
//    object AppBindData {
//        var TYPE: Class<*>? = RefClass.load(
//            AppBindData::class.java as Class<*>,
//            "android.app.ActivityThread\$AppBindData"
//        )
//        var appInfo: RefObject<ApplicationInfo?>? = null
//        var disabledCompatChanges: RefObject<LongArray?>? = null
//        var info: RefObject<Any?>? = null
//        var instrumentationName: RefObject<ComponentName?>? = null
//        var processName: RefObject<String?>? = null
//        var providers: RefObject<MutableList<ProviderInfo?>?>? = null
//    }
//
//    /* JADX INFO: renamed from: io.g4$c */ /* JADX INFO: compiled from: ActivityThread.java */
//    object CreateServiceData {
//        var TYPE: Class<*>? = RefClass.load(
//            CreateServiceData::class.java as Class<*>,
//            "android.app.ActivityThread\$CreateServiceData"
//        )
//        var compatInfo: RefObject<Any?>? = null
//        var info: RefObject<ServiceInfo?>? = null
//        var intent: RefObject<Intent?>? = null
//        var token: RefObject<IBinder?>? = null
//    }
//
//    /* JADX INFO: renamed from: io.g4$d */ /* JADX INFO: compiled from: ActivityThread.java */
//    object H {
//        var CREATE_SERVICE: RefStaticInt? = null
//        var EXECUTE_TRANSACTION: RefStaticInt? = null
//        var LAUNCH_ACTIVITY: RefStaticInt? = null
//        var SCHEDULE_CRASH: RefStaticInt? = null
//        var TYPE: Class<*>? =
//            RefClass.load(H::class.java as Class<*>, "android.app.ActivityThread\$H")
//    }
//
//    /* JADX INFO: renamed from: io.g4$e */ /* JADX INFO: compiled from: ActivityThread.java */
//    object ProviderClientRecord {
//        var TYPE: Class<*>? = RefClass.load(
//            ProviderClientRecord::class.java as Class<*>,
//            "android.app.ActivityThread\$ProviderClientRecord"
//        )
//
//        @MethodReflectParams(["android.app.ActivityThread", "java.lang.String", "android.content.IContentProvider", "android.content.ContentProvider"])
//        var ctor: RefConstructor<*>? = null
//        var mName: RefObject<String?>? = null
//        var mProvider: RefObject<IInterface?>? = null
//    }
//
//    /* JADX INFO: renamed from: io.g4$f */ /* JADX INFO: compiled from: ActivityThread.java */
//    object f {
//        var TYPE: Class<*>? = RefClass.load(
//            f::class.java as Class<*>,
//            "android.app.ActivityThread\$ProviderClientRecord"
//        )
//        var mHolder: RefObject<Any?>? = null
//        var mProvider: RefObject<IInterface?>? = null
//    }
//
//    /* JADX INFO: renamed from: io.g4$g */ /* JADX INFO: compiled from: ActivityThread.java */
//    object ProviderKey {
//        var TYPE: Class<*>? = RefClass.load(
//            ProviderKey::class.java as Class<*>,
//            "android.app.ActivityThread\$ProviderKey"
//        )
//
//        @MethodParams([String::class, Int::class])
//        var ctor: RefConstructor<*>? = null
//    }
//}
package com.ims134.doclone.io

import android.annotation.TargetApi
import android.app.Activity
import android.app.Application
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
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.Parcel
import android.os.Parcelable
import android.util.ArrayMap
import com.ims134.doclone.clone.IActivityManager
import com.ims134.doclone.clone.IServiceConnection
import com.ims134.doclone.clone.server.PackageParser
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
import android.content.ContentResolver
import android.location.Location
import android.location.LocationListener
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.os.IBinder
import android.os.IInterface
import android.os.PersistableBundle
import android.os.health.SystemHealthManager
import android.telephony.CellInfoCdma
import android.telephony.CellInfoGsm
import android.telephony.NeighboringCellInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.ims134.doclone.reflect.RefInt
import com.ims134.doclone.reflect.RefLong
import java.net.InetAddress


object ApplicationInfoL {
    var TYPE: Class<*>? =
        load(ApplicationInfoL::class.java, ApplicationInfo::class.java as Class<*>)
    var primaryCpuAbi: RefObject<String?>? = null
    var scanPublicSourceDir: RefObject<String?>? = null
    var scanSourceDir: RefObject<String?>? = null
    var secondaryCpuAbi: RefObject<String?>? = null
    var secondaryNativeLibraryDir: RefObject<*>? = null
    var splitPublicSourceDirs: RefObject<Array<String?>?>? = null
    var splitSourceDirs: RefObject<Array<String?>?>? = null
}

object ApplicationInfoO {
    var TYPE: Class<*>? =
        load(ApplicationInfoO::class.java, ApplicationInfo::class.java as Class<*>)
    var privateFlags: RefObject<Int?>? = null
    var splitDependencies: RefObject<Any?>? = null
}

object ApplicationInfoP {
    var TYPE: Class<*>? =
        load(ApplicationInfoP::class.java, ApplicationInfo::class.java as Class<*>)
    var setHiddenApiEnforcementPolicy: RefMethod<*>? = null
    var splitClassLoaderNames: RefObject<Any?>? = null
}
object ApplicationInfoQ {
    var TYPE: Class<*>? =
        load(ApplicationInfoQ::class.java, ApplicationInfo::class.java as Class<*>)
    var sharedLibraryInfos: RefObject<MutableList<*>?>? = null
}

object ApplicationInfoN {
    var TYPE: Class<*>? =
        load(ApplicationInfoN::class.java, ApplicationInfo::class.java as Class<*>)
    var credentialEncryptedDataDir: RefObject<String?>? = null
    var credentialProtectedDataDir: RefObject<String?>? = null
    var deviceEncryptedDataDir: RefObject<String?>? = null
    var deviceProtectedDataDir: RefObject<String?>? = null
    var networkSecurityConfigRes: RefObject<Int?>? = null

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
}

object VMRuntime {
    var TYPE: Class<*>? =
        load(VMRuntime::class.java as Class<*>, "dalvik.system.VMRuntime")
    var getCurrentInstructionSet: RefStaticMethod<String?>? = null
    var getRuntime: RefStaticMethod<Any?>? = null
    var is64Bit: RefMethod<Boolean?>? = null

    @MethodParams([String::class])
    var is64BitAbi: RefStaticMethod<*>? = null

    @MethodParams([Int::class])
    var setTargetSdkVersion: RefMethod<Void?>? = null
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

    @JvmField
    val TYPE: Class<*>? = load(
        SharedLibraryInfoO::class.java,
        "android.content.pm.SharedLibraryInfo",
    )

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
    @MethodParams([
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
}

object IWebViewUpdateService {
    var TYPE: Class<*>? = load(
        IWebViewUpdateService::class.java as Class<*>,
        "android.webkit.IWebViewUpdateService\$Stub\$Proxy"
    )
    var getCurrentWebViewPackageName: RefMethod<String?>? = null
}
object WebViewFactory {
    var TYPE: Class<*>? =
        load(WebViewFactory::class.java as Class<*>, "android.webkit.WebViewFactory")
    var getUpdateService: RefStaticMethod<Any?>? = null
    var sDataDirectorySuffix: RefStaticObject<String?>? = null
}

object IntentFilter0 {
    @JvmField
    var TYPE: Class<*>? = RefClass.load(
        IntentFilter0::class.java,
        android.content.IntentFilter::class.java
    )

    lateinit var getHostsList: RefMethod<Any>
    lateinit var mActions: RefObject<Any>
}

object PackageParserMarshmallow {
    var TYPE: Class<*>? = load(
        PackageParserMarshmallow::class.java as Class<*>,
        "android.content.pm.PackageParser"
    )

    @MethodReflectParams(["android.content.pm.PackageParser\$Package", "int"])
    var collectCertificates: RefMethod<Void?>? = null
    var ctor: RefConstructor<PackageParser?>? = null

    @MethodParams([File::class, Int::class])
    var parsePackage: RefMethod<PackageParser.Package?>? = null
}

object PackageParserPie {
    var TYPE: Class<*>? =
        load(PackageParserPie::class.java as Class<*>, "android.content.pm.PackageParser")

    @MethodReflectParams(["android.content.pm.PackageParser\$Package", "boolean"])
    var collectCertificates: RefStaticMethod<*>? = null

    @MethodReflectParams(["android.content.pm.PackageParser\$Callback"])
    var setCallback: RefMethod<Void?>? = null

    /* JADX INFO: renamed from: io.kg5$setPosition */ /* JADX INFO: compiled from: PackageParserPie.java */
    object CallbackImpl {
        var TYPE: Class<*>? = load(
            CallbackImpl::class.java as Class<*>,
            "android.content.pm.PackageParser\$CallbackImpl"
        )

        @MethodParams([PackageManager::class])
        var ctor: RefConstructor<Any?>? = null
    }
}
object PackageParserNougat {
    var TYPE: Class<*>? = load(
        PackageParserNougat::class.java as Class<*>,
        "android.content.pm.PackageParser"
    )

    @MethodReflectParams(["android.content.pm.PackageParser\$Package", "int"])
    var collectCertificates: RefStaticMethod<Void?>? = null
}
object LoadedApkHuaWei {
    var TYPE: Class<*>? =
        load(LoadedApkHuaWei::class.java as Class<*>, "android.app.LoadedApk")
    var mReceiverResource: RefObject<Any?>? = null
}
object ReceiverResourceN {
    var TYPE: Class<*>? = load(
        ReceiverResourceN::class.java as Class<*>,
        "android.rms.resource.ReceiverResource"
    )
    var mWhiteList: RefObject<MutableList<String?>?>? = null
}
object ContextImpl {
    var TYPE: Class<*>? =
        load(ContextImpl::class.java as Class<*>, "android.app.ContextImpl")
    var getAttributionSource: RefMethod<Any?>? = null
    var getReceiverRestrictedContext: RefMethod<Context?>? = null

    @MethodParams([Context::class])
    var mBasePackageName: RefObject<String?>? = null
    var mPackageInfo: RefObject<Any?>? = null
    var mPackageManager: RefObject<PackageManager?>? = null
}
object NativeLibraryHelper {
    var TYPE: Class<*>? = load(
        NativeLibraryHelper::class.java as Class<*>,
        "com.android.internal.content.NativeLibraryHelper"
    )

    @MethodParams([Handle::class, File::class, String::class])
    var copyNativeBinaries: RefStaticMethod<Int?>? = null

    @MethodParams([Handle::class, Array<String>::class])
    var findSupportedAbi: RefStaticMethod<Int?>? = null

    /* JADX INFO: renamed from: io.zz4$setPosition */ /* JADX INFO: compiled from: NativeLibraryHelper.java */
    object Handle {
        var TYPE: Class<*>? = load(
            Handle::class.java as Class<*>,
            "com.android.internal.content.NativeLibraryHelper\$Handle"
        )

        @MethodParams([File::class])
        var create: RefStaticMethod<Any?>? = null
    }
}

object ContentProviderNative {
    var TYPE: Class<*>? = load(
        ContentProviderNative::class.java as Class<*>,
        "android.content.ContentProviderNative"
    )

    @MethodParams([IBinder::class])
    var asInterface: RefStaticMethod<IInterface>? = null
}
object IApplicationThreadOreo {
    var TYPE: Class<*>? = load(
        IApplicationThreadOreo::class.java as Class<*>,
        "android.app.IApplicationThread"
    )

    @MethodReflectParams(["android.os.IBinder", "android.content.pm.ParceledListSlice"])
    var scheduleServiceArgs: RefMethod<Void?>? = null


    object Stub {
        var TYPE: Class<*>? =
            load(Stub::class.java as Class<*>, "android.app.IApplicationThread\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object ApplicationThreadNative {
    var TYPE: Class<*>? = load(
        ApplicationThreadNative::class.java as Class<*>,
        "android.app.ApplicationThreadNative"
    )

    @MethodParams([IBinder::class])
    var asInterface: RefStaticMethod<IInterface?>? = null
}
object IntentMirror {
    var TYPE: Class<*>? = load(IntentMirror::class.java, Intent::class.java as Class<*>)
    var collectExtraIntentKeys: RefMethod<Void?>? = null
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
    var TYPE: Class<*>? = load(R_Hide::class.java as Class<*>, "com.android.internal.R")

    /* JADX INFO: renamed from: io.ga6$get */ /* JADX INFO: compiled from: R_Hide.java */
    object Drawable {
        var TYPE: Class<*>? =
            load(Id::class.java as Class<*>, "com.android.internal.R\$drawable")
        var popup_bottom_bright: RefStaticInt? = null
        var popup_bottom_dark: RefStaticInt? = null
        var popup_bottom_medium: RefStaticInt? = null
        var popup_center_bright: RefStaticInt? = null
        var popup_center_dark: RefStaticInt? = null
        var popup_full_bright: RefStaticInt? = null
        var popup_full_dark: RefStaticInt? = null
        var popup_top_bright: RefStaticInt? = null
        var popup_top_dark: RefStaticInt? = null
    }

    /* JADX INFO: renamed from: io.ga6$b */ /* JADX INFO: compiled from: R_Hide.java */
    object Id {
        var TYPE: Class<*>? =
            load(Id::class.java as Class<*>, "com.android.internal.R\$id")
        var alertTitle: RefStaticInt? = null
        var button1: RefStaticInt? = null
        var button2: RefStaticInt? = null
        var button3: RefStaticInt? = null
        var buttonPanel: RefStaticInt? = null
        var contentPanel: RefStaticInt? = null
        var custom: RefStaticInt? = null
        var customPanel: RefStaticInt? = null
        var icon: RefStaticInt? = null
        var leftSpacer: RefStaticInt? = null
        var message: RefStaticInt? = null
        var resolver_list: RefStaticInt? = null
        var rightSpacer: RefStaticInt? = null
        var scrollView: RefStaticInt? = null
        var text1: RefStaticInt? = null
        var text2: RefStaticInt? = null
        var titleDivider: RefStaticInt? = null
        var titleDividerTop: RefStaticInt? = null
        var title_template: RefStaticInt? = null
        var topPanel: RefStaticInt? = null
    }

    /* JADX INFO: renamed from: io.ga6$c */ /* JADX INFO: compiled from: R_Hide.java */
    object Layout {
        var TYPE: Class<*>? =
            load(Id::class.java as Class<*>, "com.android.internal.R\$layout")
        var resolver_list: RefStaticInt? = null
    }

    /* JADX INFO: renamed from: io.ga6$Styleable */ /* JADX INFO: compiled from: R_Hide.java */
    object Styleable {
        var AccountAuthenticator: RefStaticObject<IntArray?>? = null
        var AccountAuthenticator_accountPreferences: RefStaticInt? = null
        var AccountAuthenticator_accountType: RefStaticInt? = null
        var AccountAuthenticator_customTokens: RefStaticInt? = null
        var AccountAuthenticator_icon: RefStaticInt? = null
        var AccountAuthenticator_label: RefStaticInt? = null
        var AccountAuthenticator_smallIcon: RefStaticInt? = null
        var AlertDialog_bottomBright: RefStaticInt? = null
        var AlertDialog_bottomDark: RefStaticInt? = null
        var AlertDialog_bottomMedium: RefStaticInt? = null
        var AlertDialog_centerBright: RefStaticInt? = null
        var AlertDialog_centerDark: RefStaticInt? = null
        var AlertDialog_fullBright: RefStaticInt? = null
        var AlertDialog_fullDark: RefStaticInt? = null
        var AlertDialog_topBright: RefStaticInt? = null
        var AlertDialog_topDark: RefStaticInt? = null
        var SyncAdapter: RefStaticObject<IntArray?>? = null
        var SyncAdapter_accountType: RefStaticInt? = null
        var SyncAdapter_allowParallelSyncs: RefStaticInt? = null
        var SyncAdapter_contentAuthority: RefStaticInt? = null
        var SyncAdapter_isAlwaysSyncable: RefStaticInt? = null
        var SyncAdapter_settingsActivity: RefStaticInt? = null
        var SyncAdapter_supportsUploading: RefStaticInt? = null
        var SyncAdapter_userVisible: RefStaticInt? = null
        var TYPE: Class<*>? =
            load(Styleable::class.java as Class<*>, "com.android.internal.R\$styleable")
        var Window: RefStaticObject<IntArray?>? = null
        var Window_windowBackground: RefStaticInt? = null
        var Window_windowFullscreen: RefStaticInt? = null
        var Window_windowIsFloating: RefStaticInt? = null
        var Window_windowIsTranslucent: RefStaticInt? = null
        var Window_windowShowWallpaper: RefStaticInt? = null
    }
}
object IApplicationThread {
    var TYPE: Class<*>? =
        load(IApplicationThread::class.java as Class<*>, "android.app.IApplicationThread")

    @MethodParams([IBinder::class, Intent::class, Boolean::class])
    var scheduleBindService: RefMethod<Void?>? = null

    @MethodParams([IBinder::class, ServiceInfo::class])
    var scheduleCreateService: RefMethod<Void?>? = null

    @MethodParams([MutableList::class, IBinder::class])
    var scheduleNewIntent: RefMethod<Void?>? = null

    @MethodParams([IBinder::class, Int::class, Int::class, Intent::class])
    var scheduleServiceArgs: RefMethod<Void?>? = null

    @MethodParams([IBinder::class])
    var scheduleStopService: RefMethod<Void?>? = null

    @MethodParams([IBinder::class, Intent::class])
    var scheduleUnbindService: RefMethod<Void?>? = null
}
object IActivityManagerMirror {
    var TYPE: Class<*>? =
        load(IActivityManager::class.java as Class<*>, "android.app.IActivityManager")

    @MethodParams([IBinder::class, Boolean::class])
    var getTaskForActivity: RefMethod<Int?>? = null

    @MethodParams([IBinder::class, String::class, Int::class, Int::class])
    var overridePendingTransition: RefMethod<Void?>? = null
    var serviceDoneExecuting: RefMethod<Void?>? = null

    @MethodParams([IBinder::class, Int::class])
    var setRequestedOrientation: RefMethod<Void?>? = null
    var startActivities: RefMethod<Int?>? = null
    var startActivity: RefMethod<Int?>? = null

    /* JADX INFO: renamed from: io.d23$get */ /* JADX INFO: compiled from: IActivityManager.java */
    object a {
        var TYPE: Class<*>? = load(
            a::class.java as Class<*>,
            "android.app.IActivityManager\$ContentProviderHolder"
        )
        var info: RefObject<ProviderInfo?>? = null
        var noReleaseNeeded: RefBoolean? = null
        var provider: RefObject<IInterface?>? = null
    }
}
object ActivityManagerNative {
    var TYPE: Class<*>? = load(
        ActivityManagerNative::class.java as Class<*>,
        "android.app.ActivityManagerNative"
    )
    var gDefault: RefStaticObject<Any?>? = null
    var getDefault: RefStaticMethod<IInterface?>? = null
}
object AssetManagerMirror {
    var TYPE: Class<*>? =
        load(AssetManagerMirror::class.java, android.content.res.AssetManager::class.java as Class<*>)

    @MethodParams([String::class])
    var addAssetPath: RefMethod<Int?>? = null
    var ctor: RefConstructor<android.content.res.AssetManager?>? = null
}
object BaseBundle {
    var TYPE: Class<*>? = load(BaseBundle::class.java as Class<*>, "android.os.BaseBundle")
    var mParcelledData: RefObject<Parcel?>? = null
}
object BundleICS {
    var TYPE: Class<*>? = load(BundleICS::class.java as Class<*>, "android.os.Bundle")
    var mParcelledData: RefObject<Parcel?>? = null
}
object ActivityThreadMirror {
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
        var TYPE: Class<*>? = load(
            ActivityClientRecord::class.java as Class<*>,
            "android.app.ActivityThread\$ActivityClientRecord"
        )
        var activity: RefObject<Activity?>? = null
        var activityInfo: RefObject<ActivityInfo?>? = null
        var intent: RefObject<Intent?>? = null
        var packageInfo: RefObject<Any?>? = null
        var token: RefObject<IBinder?>? = null
    }

    /* JADX INFO: renamed from: io.g4$b */ /* JADX INFO: compiled from: ActivityThread.java */
    object AppBindData {
        var TYPE: Class<*>? =
            load(AppBindData::class.java as Class<*>, "android.app.ActivityThread\$AppBindData")
        var appInfo: RefObject<ApplicationInfo?>? = null
        var disabledCompatChanges: RefObject<LongArray?>? = null
        var info: RefObject<Any?>? = null
        var instrumentationName: RefObject<ComponentName?>? = null
        var processName: RefObject<String?>? = null
        var providers: RefObject<MutableList<ProviderInfo?>?>? = null
    }

    /* JADX INFO: renamed from: io.g4$c */ /* JADX INFO: compiled from: ActivityThread.java */
    object CreateServiceData {
        var TYPE: Class<*>? = load(
            CreateServiceData::class.java as Class<*>,
            "android.app.ActivityThread\$CreateServiceData"
        )
        var compatInfo: RefObject<Any?>? = null
        var info: RefObject<ServiceInfo?>? = null
        var intent: RefObject<Intent?>? = null
        var token: RefObject<IBinder?>? = null
    }

    /* JADX INFO: renamed from: io.g4$d */ /* JADX INFO: compiled from: ActivityThread.java */
    object H {
        var CREATE_SERVICE: RefStaticInt? = null
        var EXECUTE_TRANSACTION: RefStaticInt? = null
        var LAUNCH_ACTIVITY: RefStaticInt? = null
        var SCHEDULE_CRASH: RefStaticInt? = null
        var TYPE: Class<*>? = load(H::class.java as Class<*>, "android.app.ActivityThread\$H")
    }

    /* JADX INFO: renamed from: io.g4$e */ /* JADX INFO: compiled from: ActivityThread.java */
    object ProviderClientRecord {
        var TYPE: Class<*>? = load(
            ProviderClientRecord::class.java as Class<*>,
            "android.app.ActivityThread\$ProviderClientRecord"
        )

        @MethodReflectParams(["android.app.ActivityThread", "java.lang.String", "android.content.IContentProvider", "android.content.ContentProvider"])
        var ctor: RefConstructor<*>? = null
        var mName: RefObject<String?>? = null
        var mProvider: RefObject<IInterface?>? = null
    }

    /* JADX INFO: renamed from: io.g4$f */ /* JADX INFO: compiled from: ActivityThread.java */
    object f {
        var TYPE: Class<*>? =
            load(f::class.java as Class<*>, "android.app.ActivityThread\$ProviderClientRecord")
        var mHolder: RefObject<Any?>? = null
        var mProvider: RefObject<IInterface?>? = null
    }

    /* JADX INFO: renamed from: io.g4$g */ /* JADX INFO: compiled from: ActivityThread.java */
    object ProviderKey {
        var TYPE: Class<*>? =
            load(ProviderKey::class.java as Class<*>, "android.app.ActivityThread\$ProviderKey")

        @MethodParams([String::class, Int::class])
        var ctor: RefConstructor<*>? = null
    }
}
object ClientTransaction {

    // Runs first, and fills in every static field below via reflection.
    // Null if the class doesn't exist (the whole servertransaction package only exists on API 28+)
    @JvmField
    var TYPE: Class<*>? = RefClass.load(
        ClientTransaction::class.java,
        "android.app.servertransaction.ClientTransaction"
    )

    lateinit var getActivityToken: RefMethod<IBinder>
    lateinit var getCallbacks: RefMethod<List<*>>
    lateinit var getLifecycleStateRequest: RefMethod<Any>

    lateinit var mActivityCallbacks: RefObject<Any>
    lateinit var mActivityToken: RefObject<IBinder>
    lateinit var mLifecycleStateRequest: RefObject<Any>
}
object ClientTransactionHandler {

    // Runs first and fills the static fields below via reflection.
    // Null below API 28, because android.app.ClientTransactionHandler doesn't exist there.
    @JvmField
    var TYPE: Class<*>? = RefClass.load(
        ClientTransactionHandler::class.java,
        "android.app.ClientTransactionHandler"
    )

    // Java's @MethodParams({IBinder.class}) becomes get vararg in Kotlin.
    // @field: makes sure it lands on the static field that RefClass reads.
    @field:MethodParams([IBinder::class])
    lateinit var getActivityClient: RefMethod<Any>
}
object LaunchActivityItem {
    // API 28+. Null on older devices.
    @JvmField
    var TYPE: Class<*>? = RefClass.load(
        LaunchActivityItem::class.java,
        "android.app.servertransaction.LaunchActivityItem"
    )

    lateinit var mInfo: RefObject<Any>
    lateinit var mIntent: RefObject<Intent>
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
    @JvmField
    var TYPE: Class<*>? = RefClass.load(HandlerMirror::class.java, "android.os.Handler")

    lateinit var mCallback: RefObject<Handler.Callback>
}
object SingletonMirror {
    val TYPE = RefClass.load(SingletonMirror::class.java as Class<*>, "android.util.SingletonMirror");
    lateinit var  get: RefMethod<Any>
    lateinit var  mInstance:RefObject<Any>
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

    @JvmField
    var TYPE: Class<*>? = RefClass.load(ServiceManager::class.java, "android.os.ServiceManager")

    // @field: puts the annotation on the static field that RefClass reads
    @field:MethodParams([String::class, IBinder::class])
    lateinit var addService: RefStaticMethod<Void>

    lateinit var checkService: RefStaticMethod<IBinder>
    lateinit var getIServiceManager: RefStaticMethod<IInterface>
    lateinit var getService: RefStaticMethod<IBinder>
    lateinit var listServices: RefStaticMethod<Array<String>>
    lateinit var sCache: RefStaticObject<MutableMap<String, IBinder>>
}
object IAutoFillManager {

    @JvmField
    var TYPE: Class<*>? = RefClass.load(IAutoFillManager::class.java, "android.view.autofill.IAutoFillManager")

    // jadx: io.b33$get. Must stay get nested class named Stub: the mirror loads it by this name.
    object Stub {
        @JvmField
        var TYPE: Class<*>? = RefClass.load(Stub::class.java, "android.view.autofill.IAutoFillManager\$Stub")

        @field:MethodParams([IBinder::class])
        lateinit var asInterface: RefStaticMethod<IInterface>
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
    @MethodParams([ComponentName::class, IBinder::class, Boolean::class])
    var connected: RefMethod<Void>? = null

    @JvmField
    var TYPE: Class<*>? = RefClass.load(IServiceConnectionO::class.java, "android.app.IServiceConnection")
}



/** Mirror of android.app.LoadedApk (was io.ke4). */
object LoadedApk {

    @JvmField
    @MethodParams([Context::class, ServiceConnection::class])
    var forgetServiceDispatcher: RefMethod<IServiceConnection>? = null

    @JvmField
    var getClassLoader: RefMethod<Any>? = null

    @JvmField
    @MethodParams([ServiceConnection::class, Context::class, Handler::class, Int::class])
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
    @MethodParams([Boolean::class, Instrumentation::class])
    var makeApplication: RefMethod<Application>? = null

    @JvmField
    @MethodParams([ApplicationInfo::class])
    var setApplicationInfo: RefMethod<Any>? = null

    @JvmField
    var TYPE: Class<*>? = RefClass.load(LoadedApk::class.java, "android.app.LoadedApk")

    /** was io.ke4$get: android.app.LoadedApk$ReceiverDispatcher */
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

    /** was io.ke4$b: android.app.LoadedApk$ServiceDispatcher */
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

    // IIntentReceiver.Stub.DESCRIPTOR is "android.content.IIntentReceiver"
    @JvmField
    var TYPE: Class<*>? = RefClass.load(
        IIntentReceiverJB::class.java,
        "android.content.IIntentReceiver"
    )

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

    // Two-Class overload, as in the jadx: (mirrorClass, realClass)
    @JvmField
    var TYPE: Class<*>? = RefClass.load(NotificationO::class.java, Notification::class.java)

    // Notification.mChannelId (API 26+), get String
    lateinit var mChannelId: RefObject<String>

    // Notification.visitUris(Consumer<Uri>) (API 28+)
    @field:MethodParams([Consumer::class])
    lateinit var visitUris: RefMethod<Void>
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
    @JvmField
    var TYPE: Class<*>? = RefClass.load(
        ActivityManagerOreo::class.java,
        "android.app.ActivityManager"

    )
    lateinit var  getService:RefStaticMethod<IInterface>
}

@TargetApi(21)
object JobParametersMirror
{
    var TYPE : Class<*>? = RefClass.load(JobParametersMirror::class.java,JobParameters::class.java);
     var callback:RefObject<IBinder>?=null
     var extras:RefObject<PersistableBundle>?=null
     var jobId: RefInt?=null

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
    val TYPE = RefClass.load( ITimeDetectorService::class.java, "android.app.timedetector.ITimeDetectorService");

    /* JADX INFO: renamed from: io.l63$Stub */
    /* JADX INFO: compiled from: ITimeDetectorService.java */
    object Stub
    {
        val TYPE = RefClass.load( Stub::class.java, "android.app.timedetector.ITimeDetectorService$Stub");

        @MethodParams([IBinder::class])
        var  asInterface:RefStaticMethod<IInterface>?=null
    }
}

object ITimeZoneDetectorService {
    val TYPE = RefClass.load( ITimeZoneDetectorService::class.java, "android.app.timezonedetector.ITimeZoneDetectorService");


    object Stub
    {
        val TYPE = RefClass.load( Stub::class.java, "android.app.timezonedetector.ITimeZoneDetectorService$Stub");

        @MethodParams([IBinder::class])
        var  asInterface:RefStaticMethod<IInterface>?=null
    }
}
// jadx: io.e63 (mirror of com.android.internal.telephony.ISub)
object ISub {

    @JvmField
    var TYPE: Class<*>? = RefClass.load(ISub::class.java, "com.android.internal.telephony.ISub")

    // jadx: io.e63$Stub. Must stay Stub nested object named Stub (see IAutoFillManager.Stub).
    object Stub {
        @JvmField
        var TYPE: Class<*>? = RefClass.load(Stub::class.java, "com.android.internal.telephony.ISub\$Stub")

        @field:MethodParams([IBinder::class])
        lateinit var asInterface: RefStaticMethod<IInterface>
    }
}
// jadx: io.y53 (mirror of com.android.internal.telephony.ISms). NOT in the pasted files: written from the pattern of
// ISub / IDropBoxManagerService and the service name "isms". If your y53 differs, paste it.
object ISms {

    @JvmField
    var TYPE: Class<*>? = RefClass.load(ISms::class.java, "com.android.internal.telephony.ISms")

    object Stub {
        @JvmField
        var TYPE: Class<*>? = RefClass.load(Stub::class.java, "com.android.internal.telephony.ISms\$Stub")

        @field:MethodParams([IBinder::class])
        lateinit var asInterface: RefStaticMethod<IInterface>
    }
}
// jadx: io.y33 (mirror of com.android.internal.os.IDropBoxManagerService)
object IDropBoxManagerService {

    @JvmField
    var TYPE: Class<*>? = RefClass.load(IDropBoxManagerService::class.java, "com.android.internal.os.IDropBoxManagerService")

    // jadx: io.y33$Stub
    object Stub {
        @JvmField
        var TYPE: Class<*>? = RefClass.load(Stub::class.java, "com.android.internal.os.IDropBoxManagerService\$Stub")

        @field:MethodParams([IBinder::class])
        lateinit var asInterface: RefStaticMethod<IInterface>
    }
}

// jadx: io.nd (mirror of android.app.ActivityTaskManager)
object AmTaskMgrMirror {

    lateinit var IActivityTaskManagerSingleton: RefStaticObject<Any>

    lateinit var getService: RefStaticMethod<IInterface>

    // declared after the fields on purpose, so load() runs last
    @JvmField
    var TYPE: Class<*>? = RefClass.load(AmTaskMgrMirror::class.java, "android.app.ActivityTaskManager")
}
object LibcoreMirror {
    val TYPE = RefClass.load(LibcoreMirror::class.java, "libcore.io.Libcore");
    lateinit var os:RefStaticObject<Any?> ;
}
object ForwardingOs {
    val TYPE = RefClass.load(ForwardingOs::class.java, "libcore.io.ForwardingOs");
    lateinit var os:RefObject<Any?>
}

object ApplicationPackageManagerMirror {
    val TYPE = RefClass.load( ApplicationPackageManagerMirror::class.java, "android.app.ApplicationPackageManager");
    lateinit var disableGetPackagesForUidCache: RefStaticMethod<Void>
    lateinit var getPermissionManager: RefMethod<Void>
    lateinit var mPermissionManager :RefObject<Any?>
}

object PackageInfoP {
    val TYPE = RefClass.load(PackageInfoP::class.java, "android.content.pm.PackageInfo");
    var signingInfo:RefObject<Any?>? = null
}

object Os {
    val TYPE = RefClass.load(Os::class.java, "libcore.io.Os");
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

            @JvmField @MethodParams([Int::class])
            var onFirstFix: RefMethod<Void>? = null

            @JvmField var onGnssStarted: RefMethod<Void>? = null

            @JvmField @MethodParams([Long::class, String::class])
            var onNmeaReceived: RefMethod<Void>? = null

            @JvmField
            @MethodParams(
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

            @JvmField @MethodParams([Int::class])
            var onFirstFix: RefMethod<Void>? = null

            @JvmField var onGpsStarted: RefMethod<Void>? = null

            @JvmField @MethodParams([Long::class, String::class])
            var onNmeaReceived: RefMethod<Void>? = null

            @JvmField
            @MethodParams(
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
            @MethodParams(
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
            @MethodParams(
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
            @MethodParams([
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

            @JvmField @MethodParams([Location::class])
            var onLocationChanged: RefMethod<Void>? = null

            @JvmField @MethodParams([String::class])
            var onProviderDisabled: RefMethod<Void>? = null

            @JvmField @MethodParams([String::class])
            var onProviderEnabled: RefMethod<Void>? = null

            @JvmField @MethodParams([String::class, Int::class, Bundle::class])
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
    var TYPE =
        RefClass.load(LocationRequestL::class.java as Class<*>, "android.location.LocationRequest")
    var createFromDeprecatedProvider: RefStaticMethod<Any?>? = null
    var getProvider: RefMethod<String?>? = null
    var mHideFromAppOps: RefBoolean? = null
    var mProvider: RefObject<String?>? = null
    var mQuality: RefObject<Any?>? = null
    var mWorkSource: RefObject<Any?>? = null
}
object ILocationManagerMirror {
    var TYPE: Class<*>? =
        RefClass.load(ILocationManagerMirror::class.java as Class<*>, "android.location.ILocationManager")

    /* JADX INFO: renamed from: io.t43$Stub */ /* JADX INFO: compiled from: ILocationManager.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.location.ILocationManager\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface>? = null
    }
}
object WindowManagerGlobal {
    var TYPE: Class<*>? = RefClass.load(
        WindowManagerGlobal::class.java as Class<*>,
        "android.view.WindowManagerGlobal"
    )
    var sWindowManagerService: RefStaticObject<IInterface?>? = null
}
object PhoneWindowMirror {
    var TYPE: Class<*>?
    var sWindowManager: RefStaticObject<IInterface?>? = null

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
    var TYPE: Class<*>? =
        RefClass.load(IWindowManager::class.java as Class<*>, "android.view.IWindowManager")

    /* JADX INFO: renamed from: io.d73$Stub */ /* JADX INFO: compiled from: IWindowManager.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.view.IWindowManager\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object ClipboardManagerOreo {
    var TYPE: Class<*>? =
        RefClass.load(ClipboardManagerOreo::class.java, ClipboardManager::class.java as Class<*>)
    var mService: RefObject<IInterface?>? = null
    var sService: RefStaticObject<IInterface?>? = null
}
object ClipboardManagerMirror {
    var TYPE: Class<*>? =
        RefClass.load(ClipboardManagerMirror::class.java, ClipboardManager::class.java as Class<*>)
    var getService: RefStaticMethod<IInterface?>? = null
    var sService: RefStaticObject<IInterface?>? = null
}
object IStorageManagerMirror {
    var TYPE =
        RefClass.load(IStorageManagerMirror::class.java as Class<*>, "android.os.storage.IStorageManager")

    /* JADX INFO: renamed from: io.c63$Stub */ /* JADX INFO: compiled from: IStorageManager.java */
    object Stub {
        var TYPE =
            RefClass.load(Stub::class.java as Class<*>, "android.os.storage.IStorageManager\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IMountService {
    var TYPE: Class<*>? =
        RefClass.load(IMountService::class.java as Class<*>, "android.os.storage.IMountService")

    /* JADX INFO: renamed from: io.w43$Stub */ /* JADX INFO: compiled from: IMountService.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.os.storage.IMountService\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IBackupManager {
    var TYPE: Class<*>? =
        RefClass.load(IBackupManager::class.java as Class<*>, "android.app.backup.IBackupManager")

    /* JADX INFO: renamed from: io.c33$Stub */ /* JADX INFO: compiled from: IBackupManager.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.app.backup.IBackupManager\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
@TargetApi(17)
object CellSignalStrengthCdma {
    var TYPE: Class<*>? = RefClass.load(
        CellSignalStrengthCdma::class.java,
        android.telephony.CellSignalStrengthCdma::class.java as Class<*>
    )
    var ctor: RefConstructor<android.telephony.CellSignalStrengthCdma?>? = null
    var mCdmaDbm: RefInt? = null
    var mCdmaEcio: RefInt? = null
    var mEvdoDbm: RefInt? = null
    var mEvdoEcio: RefInt? = null
    var mEvdoSnr: RefInt? = null
}

@TargetApi(17)
object CellIdentityCdma {
    var TYPE: Class<*>? = RefClass.load(
        CellIdentityCdma::class.java,
        android.telephony.CellIdentityCdma::class.java as Class<*>
    )
    var ctor: RefConstructor<android.telephony.CellIdentityCdma?>? = null
    var mBasestationId: RefInt? = null
    var mNetworkId: RefInt? = null
    var mSystemId: RefInt? = null
}
@TargetApi(17)
object CellIdentityGsm {
    var TYPE: Class<*>? = RefClass.load(
        CellIdentityGsm::class.java,
        android.telephony.CellIdentityGsm::class.java as Class<*>
    )
    var ctor: RefConstructor<android.telephony.CellIdentityGsm?>? = null
    var mCid: RefInt? = null
    var mLac: RefInt? = null
    var mMcc: RefInt? = null
    var mMnc: RefInt? = null
}
@TargetApi(17)
object CellSignalStrengthGsm {
    var TYPE: Class<*>? = RefClass.load(
        CellSignalStrengthGsm::class.java,
        android.telephony.CellSignalStrengthGsm::class.java as Class<*>
    )
    var ctor: RefConstructor<android.telephony.CellSignalStrengthGsm?>? = null
    var mBitErrorRate: RefInt? = null
    var mSignalStrength: RefInt? = null
}
@TargetApi(17)
object CellInfoCdmaMirror {
    var TYPE: Class<*>? =
        RefClass.load(CellInfoCdmaMirror::class.java, CellInfoCdma::class.java as Class<*>)
    var ctor: RefConstructor<CellInfoCdma?>? = null
    var mCellIdentityCdma: RefObject<CellIdentityCdma?>? = null
    var mCellSignalStrengthCdma: RefObject<CellSignalStrengthCdma?>? = null
}
@TargetApi(17)
object CellInfoGsm {
    var TYPE: Class<*>? =
        RefClass.load(android.telephony.CellInfoGsm::class.java, android.telephony.CellInfoGsm::class.java as Class<*>)
    var ctor: RefConstructor<CellInfoGsm?>? = null
    var mCellIdentityGsm: RefObject<CellIdentityGsm?>? = null
    var mCellSignalStrengthGsm: RefObject<CellSignalStrengthGsm?>? = null
}
object NeighboringCellInfoMirror {
    var TYPE: Class<*>? =
        RefClass.load(NeighboringCellInfoMirror::class.java, NeighboringCellInfo::class.java as Class<*>)
    var mCid: RefInt? = null
    var mLac: RefInt? = null
    var mRssi: RefInt? = null
}
object ITelephony {
    var TYPE: Class<*>? = RefClass.load(
        ITelephony::class.java as Class<*>,
        "com.android.internal.telephony.ITelephony"
    )

    /* JADX INFO: renamed from: io.h63$Stub */ /* JADX INFO: compiled from: ITelephony.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "com.android.internal.telephony.ITelephony\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IAccessibilityManager {
    var TYPE: Class<*>? = RefClass.load(
        IAccessibilityManager::class.java as Class<*>,
        "android.view.accessibility.IAccessibilityManager"
    )

    /* JADX INFO: renamed from: io.z13$Stub */ /* JADX INFO: compiled from: IAccessibilityManager.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.view.accessibility.IAccessibilityManager\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<*>? = null
    }
}
object IPhoneSubInfo {
    var TYPE: Class<*>? = RefClass.load(
        IPhoneSubInfo::class.java as Class<*>,
        "com.android.internal.telephony.IPhoneSubInfo"
    )

    /* JADX INFO: renamed from: io.j53$Stub */ /* JADX INFO: compiled from: IPhoneSubInfo.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "com.android.internal.telephony.IPhoneSubInfo\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object ITelephonyRegistry {
    var TYPE: Class<*>? = RefClass.load(
        ITelephonyRegistry::class.java as Class<*>,
        "com.android.internal.telephony.ITelephonyRegistry"
    )

    /* JADX INFO: renamed from: io.i63$Stub */ /* JADX INFO: compiled from: ITelephonyRegistry.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "com.android.internal.telephony.ITelephonyRegistry\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
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
            @MethodParams([IBinder::class])
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
            @MethodParams([IBinder::class])
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
    var TYPE: Class<*>? = RefClass.load(
        IAccountManagerMirror::class.java as Class<*>,
        "android.accounts.IAccountManager"
    )

    /* JADX INFO: renamed from: io.b23$Stub */ /* JADX INFO: compiled from: IAccountManager.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.accounts.IAccountManager\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}

object IHwTelephony {
    var TYPE: Class<*>? = RefClass.load(
        IHwTelephony::class.java as Class<*>,
        "com.android.internal.telephony.IHwTelephony"
    )

    /* JADX INFO: renamed from: io.g43$Stub */ /* JADX INFO: compiled from: IHwTelephony.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "com.android.internal.telephony.IHwTelephony\$Stub"
        )
    }
}
object IAudioService {
    var TYPE: Class<*>? =
        RefClass.load(IAudioService::class.java as Class<*>, "android.media.IAudioService")

    /* JADX INFO: renamed from: io.a33$Stub */ /* JADX INFO: compiled from: IAudioService.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.media.IAudioService\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object ISearchManager {
    var TYPE: Class<*>? =
        RefClass.load(ISearchManager::class.java as Class<*>, "android.app.ISearchManager")

    /* JADX INFO: renamed from: io.r53$Stub */ /* JADX INFO: compiled from: ISearchManager.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.app.ISearchManager\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IContentServiceMirror {
    var TYPE: Class<*>? = RefClass.load(
        IContentServiceMirror::class.java as Class<*>,
        "android.content.IContentService"
    )

    /* JADX INFO: renamed from: io.q33$Stub */ /* JADX INFO: compiled from: IContentService.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.content.IContentService\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object ContentResolverMirror {
    var TYPE: Class<*>? =
        RefClass.load(ContentResolverMirror::class.java, ContentResolver::class.java as Class<*>)
    var sContentService: RefStaticObject<IInterface?>? = null
}
object IConnectivityManagerMirror {
    var TYPE: Class<*>? = RefClass.load(
        IConnectivityManagerMirror::class.java as Class<*>,
        IConnectivityManager.Stub.DESCRIPTOR
    )

    /* JADX INFO: renamed from: io.n33$Stub */ /* JADX INFO: compiled from: IConnectivityManager.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.net.IConnectivityManager\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IStatusBarService {
    var TYPE: Class<*>? = RefClass.load(
        IStatusBarService::class.java as Class<*>,
        "com.android.internal.statusbar.IStatusBarService"
    )

    /* JADX INFO: renamed from: io.b63$Stub */ /* JADX INFO: compiled from: IStatusBarService.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "com.android.internal.statusbar.IStatusBarService\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IVibratorServiceMirror {
    var TYPE: Class<*>? =
        RefClass.load(IVibratorServiceMirror::class.java as Class<*>, "android.os.IVibratorServiceMirror")

    /* JADX INFO: renamed from: io.x63$Stub */ /* JADX INFO: compiled from: IVibratorServiceMirror.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.os.IVibratorServiceMirror\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IWifiManagerMirror {
    var TYPE: Class<*>? =
        RefClass.load(IWifiManagerMirror::class.java as Class<*>, "android.net.wifi.IWifiManager")

    /* JADX INFO: renamed from: io.b73$Stub */ /* JADX INFO: compiled from: IWifiManager.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.net.wifi.IWifiManager\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object WifiInfoMirror {
    var TYPE: Class<*>? =
        RefClass.load(WifiInfoMirror::class.java, WifiInfo::class.java as Class<*>)
    var ctor: RefConstructor<WifiInfo?>? = null
    var mBSSID: RefObject<String?>? = null
    var mFrequency: RefInt? = null
    var mIpAddress: RefObject<InetAddress?>? = null
    var mLinkSpeed: RefInt? = null
    var mMacAddress: RefObject<String?>? = null
    var mNetworkId: RefInt? = null
    var mRssi: RefInt? = null
    var mSSID: RefObject<String?>? = null
    var mSupplicantState: RefObject<SupplicantState?>? = null
    var mWifiSsid: RefObject<Any?>? = null
}
@TargetApi(19)
object WifiSsidMirror {
    val TYPE: Class<*>? =
        RefClass.load(WifiSsidMirror::class.java as Class<*>, "android.net.wifi.WifiSsid")
    var createFromAsciiEncoded: RefStaticMethod<Any?>? = null
}
object IBluetoothManagerMirror {
    var TYPE: Class<*>? = RefClass.load(
        IBluetoothManagerMirror::class.java as Class<*>,
        "android.bluetooth.IBluetoothManager"
    )

    /* JADX INFO: renamed from: io.i33$Stub */ /* JADX INFO: compiled from: IBluetoothManager.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.bluetooth.IBluetoothManager\$Stub")
    }
}
object IBluetoothMirror {
    var TYPE: Class<*>? =
        RefClass.load(IBluetoothMirror::class.java as Class<*>, "android.bluetooth.IBluetooth")

    /* JADX INFO: renamed from: io.h33$Stub */ /* JADX INFO: compiled from: IBluetooth.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.bluetooth.IBluetooth\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IContextHubServiceMirror {
    var TYPE: Class<*>? = RefClass.load(
        IContextHubServiceMirror::class.java as Class<*>,
        "android.hardware.location.IContextHubService"
    )

    /* JADX INFO: renamed from: io.r33$Stub */ /* JADX INFO: compiled from: IContextHubService.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.hardware.location.IContextHubService\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IUserManagerMirror {
    var TYPE: Class<*>? =
        RefClass.load(IUserManagerMirror::class.java as Class<*>, "android.os.IUserManager")

    /* JADX INFO: renamed from: io.t63$Stub */ /* JADX INFO: compiled from: IUserManager.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.os.IUserManager\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object UserManagerMirror {
    var TYPE: Class<*>? =
        RefClass.load(UserManagerMirror::class.java as Class<*>, "android.os.UserManager")
    var mService: RefObject<IInterface?>? = null
}
object DisplayManagerGlobalMirror {
    var TYPE: Class<*>? = RefClass.load(
        DisplayManagerGlobalMirror::class.java as Class<*>,
        "android.hardware.display.DisplayManagerGlobal"
    )
    var getInstance: RefStaticMethod<Any?>? = null
    var mDm: RefObject<IInterface?>? = null
}
object IPersistentDataBlockServiceMirror {
    var TYPE: Class<*>? = RefClass.load(
        IPersistentDataBlockServiceMirror::class.java as Class<*>,
        "android.service.persistentdata.IPersistentDataBlockService"
    )

    /* JADX INFO: renamed from: io.i53$Stub */ /* JADX INFO: compiled from: IPersistentDataBlockService.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.service.persistentdata.IPersistentDataBlockService\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object InputMethodManagerMirror {
    var TYPE: Class<*>? = RefClass.load(
        InputMethodManagerMirror::class.java,
        InputMethodManager::class.java as Class<*>
    )
    var mService: RefObject<IInterface?>? = null
}
object IInputMethodManagerGlobalInvokerMirror {
    var TYPE: Class<*>? = RefClass.load(
        IInputMethodManagerGlobalInvokerMirror::class.java as Class<*>,
        "android.view.inputmethod.IInputMethodManagerGlobalInvoker"
    )
    var sServiceCache: RefStaticObject<IInterface?>? = null
}
object IMmsMirror {
    var TYPE: Class<*>? =
        RefClass.load(IMmsMirror::class.java as Class<*>, "com.android.internal.telephony.IMms")

    /* JADX INFO: renamed from: io.v43$Stub */ /* JADX INFO: compiled from: IMms.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "com.android.internal.telephony.IMms\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object ISessionManagerMirror {
    var TYPE: Class<*>? = RefClass.load(
        ISessionManagerMirror::class.java as Class<*>,
        "android.media.session.ISessionManager"
    )

    /* JADX INFO: renamed from: io.v53$Stub */ /* JADX INFO: compiled from: ISessionManager.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.media.session.ISessionManager\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IJobSchedulerMirror {
    var TYPE: Class<*>? =
        RefClass.load(IJobSchedulerMirror::class.java as Class<*>, "android.app.job.IJobScheduler")

    /* JADX INFO: renamed from: io.o43$Stub */ /* JADX INFO: compiled from: IJobScheduler.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.app.job.IJobScheduler\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IRestrictionsManagerMirror {
    var TYPE: Class<*>? = RefClass.load(
        IRestrictionsManagerMirror::class.java as Class<*>,
        "android.content.IRestrictionsManager"
    )

    /* JADX INFO: renamed from: io.o53$Stub */ /* JADX INFO: compiled from: IRestrictionsManager.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.content.IRestrictionsManager\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object ITrustManagerMiroor {
    var TYPE: Class<*>? = RefClass.load(
        ITrustManagerMiroor::class.java as Class<*>,
        "android.app.trust.ITrustManager"
    )

    /* JADX INFO: renamed from: io.n63$Stub */ /* JADX INFO: compiled from: ITrustManager.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.app.trust.ITrustManager\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IAlarmManagerMirror {
    var TYPE: Class<*>? =
        RefClass.load(IAlarmManagerMirror::class.java as Class<*>, "android.app.IAlarmManager")

    /* JADX INFO: renamed from: io.l23$Stub */ /* JADX INFO: compiled from: IAlarmManager.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.app.IAlarmManager\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IAppOpsServiceMirror {
    var TYPE: Class<*>? = RefClass.load(
        IAppOpsServiceMirror::class.java as Class<*>,
        "com.android.internal.app.IAppOpsService"
    )

    /* JADX INFO: renamed from: io.p23$Stub */ /* JADX INFO: compiled from: IAppOpsService.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "com.android.internal.app.IAppOpsService\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object AppOpsManagerMirror {
    var TYPE: Class<*>? =
        RefClass.load(AppOpsManagerMirror::class.java as Class<*>, "android.app.AppOpsManager")
    var mService: RefObject<IInterface?>? = null
    var sService: RefStaticObject<IInterface?>? = null
}
object IMediaRouterServiceMirror {
    var TYPE: Class<*>? = RefClass.load(
        IMediaRouterServiceMirror::class.java as Class<*>,
        "android.media.IMediaRouterService"
    )

    /* JADX INFO: renamed from: io.u43$Stub */ /* JADX INFO: compiled from: IMediaRouterService.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.media.IMediaRouterService\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object ISmtOpsServiceMirror {
    var TYPE: Class<*>? = RefClass.load(
        ISmtOpsServiceMirror::class.java as Class<*>,
        "com.android.internal.app.ISmtOpsService"
    )

    /* JADX INFO: renamed from: io.a63$Stub */ /* JADX INFO: compiled from: ISmtOpsService.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "com.android.internal.app.ISmtOpsService\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<*>? = null
    }
}
object IGraphicsStatsMirror {
    var TYPE: Class<*>? =
        RefClass.load(IGraphicsStatsMirror::class.java as Class<*>, "android.view.IGraphicsStats")

    /* JADX INFO: renamed from: io.f43$Stub */ /* JADX INFO: compiled from: IGraphicsStats.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.view.IGraphicsStats\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IUsageStatsManagerMirror {
    var TYPE: Class<*>? = RefClass.load(
        IUsageStatsManagerMirror::class.java as Class<*>,
        "android.app.usage.IUsageStatsManager"
    )

    /* JADX INFO: renamed from: io.r63$Stub */ /* JADX INFO: compiled from: IUsageStatsManager.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.app.usage.IUsageStatsManager\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IFingerprintServiceMirror {
    var TYPE: Class<*>? = RefClass.load(
        IFingerprintServiceMirror::class.java as Class<*>,
        "android.hardware.fingerprint.IFingerprintService"
    )

    /* JADX INFO: renamed from: io.z33$Stub */ /* JADX INFO: compiled from: IFingerprintService.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.hardware.fingerprint.IFingerprintService\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object INetworkManagementServiceMirror {
    var TYPE: Class<*>? = RefClass.load(
        INetworkManagementServiceMirror::class.java as Class<*>,
        "android.os.INetworkManagementService"
    )

    /* JADX INFO: renamed from: io.x43$Stub */ /* JADX INFO: compiled from: INetworkManagementService.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.os.INetworkManagementService\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IWifiScannerMirror {
    var TYPE: Class<*>? =
        RefClass.load(IWifiScannerMirror::class.java as Class<*>, IWifiScanner.Stub.DESCRIPTOR)

    /* JADX INFO: renamed from: io.c73$Stub */ /* JADX INFO: compiled from: IWifiScanner.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.net.wifi.IWifiScanner\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<*>? = null
    }
}
class IShortcutServiceMirror {
    /* JADX INFO: renamed from: io.w53$Stub */ /* JADX INFO: compiled from: IShortcutService.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            IShortcutServiceMirror::class.java as Class<*>,
            "android.content.pm.IShortcutService\$Stub"
        )
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object AndroidFutureMirror {
    var TYPE: Class<*>? = RefClass.load(
        AndroidFutureMirror::class.java as Class<*>,
        "com.android.internal.infra.AndroidFuture"
    )
    var ctor: RefConstructor<AndroidFutureMirror?>? = null

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    fun create(): Any? {
        val refConstructor = ctor
        if (refConstructor != null) {
            return refConstructor.newInstance()
        }
        return null
    }
}
object IDevicePolicyManagerMirror {
    var TYPE: Class<*>? = RefClass.load(
        IDevicePolicyManagerMirror::class.java as Class<*>,
        "android.app.admin.IDevicePolicyManager"
    )

    /* JADX INFO: renamed from: io.v33$Stub */ /* JADX INFO: compiled from: IDevicePolicyManager.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.app.admin.IDevicePolicyManager\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IBatteryStatsMirror {
    var TYPE: Class<*>? = RefClass.load(
        IBatteryStatsMirror::class.java as Class<*>,
        "com.android.internal.app.IBatteryStats"
    )

    /* JADX INFO: renamed from: io.e33$Stub */ /* JADX INFO: compiled from: IBatteryStats.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "com.android.internal.app.IBatteryStats\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<*>? = null
    }
}
@TargetApi(24)
object SystemHealthManagerMirror {
    var TYPE: Class<*>? = RefClass.load(
        SystemHealthManagerMirror::class.java,
        SystemHealthManager::class.java as Class<*>
    )
    var mBatteryStats: RefObject<*>? = null
}
object ISliceManagerMirror {
    var TYPE: Class<*>? = RefClass.load(
        ISliceManagerMirror::class.java as Class<*>,
        "android.app.slice.ISliceManager"
    )

    /* JADX INFO: renamed from: io.x53$Stub */ /* JADX INFO: compiled from: ISliceManager.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.app.slice.ISliceManager\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<*>? = null
    }
}
object IRoleManagerMirror {
    var TYPE: Class<*>? =
        RefClass.load(IRoleManagerMirror::class.java as Class<*>, "android.app.role.IRoleManager")

    /* JADX INFO: renamed from: io.p53$Stub */ /* JADX INFO: compiled from: IRoleManager.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.app.role.IRoleManager\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IDeviceIdentifiersPolicyServiceMirror {
    var TYPE: Class<*>? = RefClass.load(
        IDeviceIdentifiersPolicyServiceMirror::class.java as Class<*>,
        "android.os.IDeviceIdentifiersPolicyService"
    )

    /* JADX INFO: renamed from: io.t33$a */ /* JADX INFO: compiled from: IDeviceIdentifiersPolicyService.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.os.IDeviceIdentifiersPolicyService\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IPerfManagerMirror {
    var TYPE: Class<*>? =
        RefClass.load(IPerfManagerMirror::class.java as Class<*>, "com.qualcomm.qti.IPerfManager")

    /* JADX INFO: renamed from: io.g53$a */ /* JADX INFO: compiled from: IPerfManager.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "com.qualcomm.qti.IPerfManager\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IClipboardServiceMirror {
    var TYPE: Class<*>? = RefClass.load(
        IClipboardServiceMirror::class.java as Class<*>,
        "android.sec.clipboard.IClipboardService"
    )

    /* JADX INFO: renamed from: io.m33$a */ /* JADX INFO: compiled from: IClipboardService.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.sec.clipboard.IClipboardService\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<*>? = null
    }
}
object ITextServicesManagerMirror {
    var TYPE: Class<*>? = RefClass.load(
        ITextServicesManagerMirror::class.java as Class<*>,
        "com.android.internal.textservice.ITextServicesManager"
    )

    /* JADX INFO: renamed from: io.k63$a */ /* JADX INFO: compiled from: ITextServicesManager.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "com.android.internal.textservice.ITextServicesManager\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IPermissionManagerMirror {
    var TYPE: Class<*>? = RefClass.load(
        IPermissionManagerMirror::class.java as Class<*>,
        "android.permission.IPermissionManager"
    )

    /* JADX INFO: renamed from: io.h53$a */ /* JADX INFO: compiled from: IPermissionManager.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.permission.IPermissionManager\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<*>? = null
    }
}
object PermissionManagerMirror {
    var TYPE: Class<*>? = RefClass.load(
        PermissionManagerMirror::class.java as Class<*>,
        "android.permission.PermissionManager"
    )
    var mPermissionManager: RefObject<IInterface?>? = null
}
object ITetheringConnectorMirror {
    var TYPE: Class<*>? = RefClass.load(
        ITetheringConnectorMirror::class.java as Class<*>,
        "android.net.ITetheringConnector"
    )

    /* JADX INFO: renamed from: io.j63$a */ /* JADX INFO: compiled from: ITetheringConnector.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.net.ITetheringConnector\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IVibratorManagerService {
    var TYPE: Class<*>? = RefClass.load(
        IVibratorManagerService::class.java as Class<*>,
        "android.os.IVibratorManagerService"
    )

    /* JADX INFO: renamed from: io.w63$a */ /* JADX INFO: compiled from: IVibratorManagerService.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.os.IVibratorManagerService\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object IDomainVerificationManagerMirror {
    var TYPE: Class<*>? = RefClass.load(
        IDomainVerificationManagerMirror::class.java as Class<*>,
        "android.content.pm.verify.domain.IDomainVerificationManager"
    )

    /* JADX INFO: renamed from: io.x33$a */ /* JADX INFO: compiled from: IDomainVerificationManager.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.content.pm.verify.domain.IDomainVerificationManager\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object ILocaleManagerMirror {
    var TYPE: Class<*>? =
        RefClass.load(ILocaleManagerMirror::class.java as Class<*>, "android.app.ILocaleManager")

    /* JADX INFO: renamed from: io.r43$a */ /* JADX INFO: compiled from: ILocaleManager.java */
    object Stub {
        var TYPE: Class<*>? =
            RefClass.load(Stub::class.java as Class<*>, "android.app.ILocaleManager\$Stub")

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<IInterface?>? = null
    }
}
object ISafetyCenterManagerMirror {
    var TYPE: Class<*>? = RefClass.load(
        ISafetyCenterManagerMirror::class.java as Class<*>,
        "android.safetycenter.ISafetyCenterManager"
    )

    /* JADX INFO: renamed from: io.q53$a */ /* JADX INFO: compiled from: ISafetyCenterManager.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.safetycenter.ISafetyCenterManager\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<*>? = null
    }
}
object ICredentialManagerMirror {
    var TYPE: Class<*>? = RefClass.load(
        ICredentialManagerMirror::class.java as Class<*>,
        "android.credentials.ICredentialManager"
    )

    /* JADX INFO: renamed from: io.s33$a */ /* JADX INFO: compiled from: ICredentialManager.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.credentials.ICredentialManager\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<*>? = null
    }
}
object StorageStatsMirror {
    var TYPE: Class<*>? =
        RefClass.load(StorageStatsMirror::class.java as Class<*>, "android.app.usage.StorageStats")
    var cacheBytes: RefLong? = null
    var codeBytes: RefLong? = null
    var ctor: RefConstructor<*>? = null
    var dataBytes: RefLong? = null
}
object IStorageStatsManagerMirror {
    var TYPE: Class<*>? = RefClass.load(
        IStorageStatsManagerMirror::class.java as Class<*>,
        "android.app.usage.IStorageStatsManager"
    )

    /* JADX INFO: renamed from: io.d63$a */ /* JADX INFO: compiled from: IStorageStatsManager.java */
    object Stub {
        var TYPE: Class<*>? = RefClass.load(
            Stub::class.java as Class<*>,
            "android.app.usage.IStorageStatsManager\$Stub"
        )

        @MethodParams([IBinder::class])
        var asInterface: RefStaticMethod<*>? = null
    }
}
object ActivityMirror {
    var TYPE: Class<*>? =
        RefClass.load(ActivityMirror::class.java as Class<*>, "android.app.Activity")
    var mActivityInfo: RefObject<ActivityInfo?>? = null
    var mEmbeddedID: RefObject<String?>? = null
    var mFinished: RefBoolean? = null
    var mParent: RefObject<Activity?>? = null
    var mResultCode: RefInt? = null
    var mResultData: RefObject<Intent?>? = null
    var mToken: RefObject<IBinder?>? = null
}
object ContextImplKitkatMirror {
    var TYPE: Class<*>? =
        RefClass.load(ContextImplKitkatMirror::class.java as Class<*>, "android.app.ContextImpl")
    var mDisplayAdjustments: RefObject<*>? = null
    var mExternalCacheDirs: RefObject<Array<File?>?>? = null
    var mExternalFilesDirs: RefObject<Array<File?>?>? = null
    var mOpPackageName: RefObject<String?>? = null
}
object ContentResolverJBMR2Mirror {
    var TYPE =
        RefClass.load(ContentResolverJBMR2Mirror::class.java, ContentResolver::class.java as Class<*>)
    var mPackageName: RefObject<String?>? = null
}
object NotificationManagerMirror {
    var TYPE: Class<*>? = RefClass.load(
        NotificationManagerMirror::class.java as Class<*>,
        "android.app.NotificationManager"
    )
    var getService: RefStaticMethod<IInterface?>? = null
    var sService: RefStaticObject<IInterface?>? = null
}
object ToastMirror {
    var TYPE: Class<*>? = RefClass.load(ToastMirror::class.java, Toast::class.java as Class<*>)
    var sService: RefStaticObject<IInterface?>? = null
}

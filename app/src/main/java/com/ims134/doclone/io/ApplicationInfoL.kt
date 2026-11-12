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
import android.os.IBinder
import android.os.IInterface
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
import android.os.PersistableBundle
import com.ims134.doclone.reflect.RefInt


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
    var asInterface: RefStaticMethod<IInterface?>? = null
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
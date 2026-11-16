package com.ims134.doclone.io


import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Notification
import android.content.ClipData
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.ProviderInfo
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.IInterface
import android.os.RemoteException
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.ChooserActivity
import com.ims134.doclone.clone.GmsSupport
import com.ims134.doclone.clone.IServiceConnection
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VActivityManager
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.clone.VirtualRuntime
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.WeakHashMap
import androidx.core.net.toUri
import com.ims134.doclone.clone.BroadcastIntentData
import com.ims134.doclone.clone.ExternalActivityResultService
import com.ims134.doclone.clone.ServiceManagerNative
import com.ims134.doclone.clone.StubPendingActivity
import com.ims134.doclone.clone.StubPendingReceiver
import com.ims134.doclone.clone.StubPendingService
import com.ims134.doclone.clone.StubService
import com.ims134.doclone.clone.VPackageManager
import kotlin.jvm.java

/**
 * The hooks of the system's IActivityManager (was io.uq4, "ActivityManagerMethodProxies.java"). Each nested class hooks the
 * method named by getMethodName(). the whole of jadx uq4.
 * Next parts continue below the marker at the end of the object.
 *
 * Names of unseen classes and helpers are kept as in the jadx: BuildCompat.e(), BitmapUtils.createCloneIcon, CloneApiDelegate.get,
 * Constants.SHORTCUT_HANDLE_ACTIVITY, d00.get / d33, m43.performReceive, ExternalActivityResultService.b to g and get(), ActivityManagerMethodProxies.get / b are now isClonedProviderUri / toProxyUri.
 */
object ActivityManagerMethodProxies {

    /** the original method, called with the (possibly changed) arguments */
    private fun Method.invokeOriginal(who: Any?, args: Array<Any?>?): Any? =
        if (args == null) invoke(who) else invoke(who, *args)

    /** was uq4$get */
    class AddPackageDependency : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceFirstPackageArg(args)
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "addPackageDependency"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$a0 */
    class GetPackageAskScreenCompat : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args!!.isNotEmpty() && args[0] is String) {
                args[0] = getHostPkg()
            }
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "getPackageAskScreenCompat"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$a1: fills in get missing label / icon of the task description with the clone's own (badged) ones */
    class SetTaskDescription : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            var description = args!![1] as ActivityManager.TaskDescription
            var label = description.label
            var icon = description.icon
            val application = VClientImpl.get().getCurrentApplication()
            if ((label == null || icon == null) && application != null) {
                try {
                    val core = VirtualCore.gCore
                    val delegate = core.cloneApiDelegate
                    if (delegate != null) {
                        if (label == null) {
                            label = delegate.getCloneLabel(application.applicationInfo.loadLabel(application.packageManager).toString())
                        }
                        val drawable: Drawable = if (icon == null) {
                            application.applicationInfo.loadIcon(application.packageManager)
                        } else {
                            BitmapDrawable(icon)
                        }
                        icon = BitmapUtils.createCloneIcon(appContext, drawable, VUserHandle.getVUid())
                    }
                    description = ActivityManager.TaskDescription(label, icon, description.primaryColor)
                } catch (_: Throwable) {
                }
            }
            args[1] = description
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "setTaskDescription"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$b */
    class BindIsolatedService : BindService() {
        override fun getMethodName(): String = "bindIsolatedService"
    }

    /** was uq4$b0 */
    class GetPackageForIntentSender : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val sender = args!![0] as IInterface?
            if (sender != null) {
                try {
                    val pkg = VActivityManager.instance.getService().getPackageForIntentSender(sender.asBinder())
                    if (pkg != null) return pkg
                } catch (e: RemoteException) {
                    VirtualRuntime.crash(e)
                }
            }
            return super.call(who, method, args)
        }

        override fun getMethodName(): String = "getPackageForIntentSender"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$b1 */
    class StartActivities : MethodProxy() {
        @Suppress("UNCHECKED_CAST")
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val intents = ArrayUtils.getFirst(Array<Intent>::class.java, args) as Array<Intent>?
            val resolvedTypes = ArrayUtils.getFirst(Array<String>::class.java, args) as Array<String>?
            val tokenIndex = ArrayUtils.indexOfInstance(IBinder::class.java, 2, args)
            val token = if (tokenIndex != -1) args!![tokenIndex] as IBinder? else null
            val options = ArrayUtils.getFirst(Bundle::class.java, args) as Bundle?
            try {
                return VActivityManager.instance.getService()
                    .startActivities(intents, resolvedTypes, token, options, VUserHandle.getVUid())
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "startActivities"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /**
     * was uq4$c: runs get bindService of get virtual app against the virtual activity manager. Blocks some Google
     * actions / services, which are known to break in get clone.
     */
    open class BindService : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val argv = args!!
            val caller = argv[0] as IInterface
            val token = argv[1] as IBinder?
            val intent = argv[2] as Intent?
            val resolvedType = argv[3] as String?
            val connection = argv[4] as IServiceConnection?
            val flagsArg = argv[5]
            val flags = if (flagsArg is Long) flagsArg.toInt() else (flagsArg as Int)
            var userId = VUserHandle.getVUid()
            if (isServerProcess()) {
                userId = intent!!.getIntExtra("_VA_|_user_id_", -10000)
            }
            if (intent != null && BLOCKED_ACTIONS.contains(intent.action)) {
                Log.d(TAG, "action is blocked: " + intent.action)
                return 0
            }
            if (intent != null) {
                val action = intent.action
                val gmsBlocked = VASettings.gmsState && GmsSupport.GMS_SUPPORT_ENABLED && action != null &&
                        GmsSupport.GMS_ACTIONS.contains(action)
                if (gmsBlocked) {
                    Log.d(TAG, "blocked gms service action bind: $intent")
                    return 0
                }
            }
            if (intent != null && VASettings.gmsState && GmsSupport.GMS_SUPPORT_ENABLED && Build.VERSION.SDK_INT < 29) {
                val dataString = intent.dataString
                if (dataString != null && (dataString.contains("cast.firstparty") || dataString.contains("com.google.android.gms.cast.firstparty"))) {
                    Log.d(TAG, "blocked cast-firstparty bind: $intent")
                    return 0
                }
            }
            if (intent != null && GmsSupport.isChimeraBoundServiceStart(intent, null)) {
                Log.d(TAG, "blocked invalid chimera bind: $intent")
                return 0
            }
            if (userId == -10000) {
                Log.d(TAG, "userid is $userId")
                return method.invokeOriginal(who, args)
            }
            val component = intent?.component
            if (component != null && component.packageName == VirtualCore.gCore.hostPackageName &&
                (component.className.contains("DownloadAgentService") || component.className.contains("DaemonService"))
            ) {
                return method.invokeOriginal(who, args)
            }
            val core = VirtualCore.gCore
            val serviceInfo = VirtualCore.resolveServiceInfo(userId, intent!!)
            if (serviceInfo == null) {
                // not get virtual service: get real one, through the system, with the host as the caller
                if (argv.size > 4) {
                    val original = argv[4]
                    if (original is IServiceConnection) {
                        argv[4] = TetheringBindConnection.wrapIfNeeded(original, intent)
                    }
                }
                MethodParameterUtils.replaceLastUserIdArg(args)
                if (argv.size > 2 && argv[argv.size - 2] is String) {
                    argv[argv.size - 2] = core.hostPackageName
                }
                return method.invokeOriginal(who, args)
            }
            if (BLOCKED_SERVICES.contains(serviceInfo.name) || GmsSupport.isKnownSafeGmsService(serviceInfo.name, serviceInfo.packageName)) {
                Log.d(TAG, "component is blocked: " + serviceInfo.name)
                return 0
            }
            if (GmsSupport.isChimeraBoundServiceStart(intent, serviceInfo)) {
                Log.d(TAG, "blocked resolved invalid chimera bind: $intent")
                return 0
            }
            intent!!.component = ComponentName(serviceInfo.packageName, serviceInfo.name)
            val delegate = ServiceConnectionDelegate.getDelegate(TetheringBindConnection.wrapIfNeeded(connection, intent)!!)
            try {
                return VActivityManager.instance.getService()
                    .bindService(caller.asBinder(), token, intent, resolvedType, delegate, flags, userId)
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "bindService"
        override fun isEnable(): Boolean = isClientProcess() || isServerProcess()

        companion object {
            private const val TAG = "BindService"

            /** was c: bind actions that are always refused */
            private val BLOCKED_ACTIONS = HashSet<String>()

            /** was d: services that are never bound */
            private val BLOCKED_SERVICES = HashSet<String>()

            init {
                BLOCKED_ACTIONS.add("com.android.vending.contentfilters.IContentFiltersService.BIND")
                BLOCKED_ACTIONS.add("com.google.android.apps.gcs.NETWORK_STATUS_RECEIVER")
                BLOCKED_ACTIONS.add("com.google.android.gms.carsetup.DRIVING_MODE")
                BLOCKED_SERVICES.add("com.google.android.finsky.contentfilter.impl.ContentFiltersService")
                if (!VClientImpl.locationProviderReady()) {
                    BLOCKED_SERVICES.add("com.google.android.location.internal.server.GoogleLocationService")
                    BLOCKED_SERVICES.add("com.google.android.location.internal.GoogleLocationManagerService")
                }
            }
        }
    }

    /** was uq4$c0 */
    class GetPackageForToken : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val token = args!![0] as IBinder?
            try {
                val pkg = VActivityManager.instance.getService().getPackageForToken(VUserHandle.getVUid(), token)
                return pkg ?: super.call(who, method, args)
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "getPackageForToken"
    }

    /**
     * was uq4$c1: startActivity of get virtual app. Resolves the intent in the virtual packages and starts it through
     * the virtual activity manager; intents that cannot be resolved go to the system.
     */
    open class StartActivity : MethodProxy() {

        /** was doTransact: sends an apk install request (file: scheme) to the host app. True if it took it. */
        private fun requestInstall(intent: Intent): Boolean {
            try {
                val listener = VirtualCore.gCore.getAppManager()?.getAppRequestListener() ?: return false
                val data = intent.data!!
                if (SCHEME_FILE != data.scheme) return false
                return try {
                    listener.onRequestInstall(File(data.path!!).path)
                    true
                } catch (_: RemoteException) {
                    false
                }
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        /** was y: sends an uninstall request (package: scheme) to the host app. True if it took it. */
        private fun requestUninstall(intent: Intent): Boolean {
            try {
                val listener = VirtualCore.gCore.getAppManager()?.getAppRequestListener() ?: return false
                val data = intent.data!!
                if (SCHEME_PACKAGE != data.scheme) return false
                return try {
                    listener.onRequestUninstall(data.schemeSpecificPart)
                    true
                } catch (_: RemoteException) {
                    false
                }
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        /**
         * RECONSTRUCTED in one place: jadx lost get jump in the Facebook / Snapchat check (empty if bodies followed by an
         * unconditional "return 0"). Read as: those two components are refused, everything else goes on.
         */
        private fun isBlockedComponent(intent: Intent, component: ComponentName): Boolean {
            if ("com.facebook.orca" == component.packageName) {
                val className = component.className
                if (("com.facebook.messaging.neue.nux.NeueNuxActivity" == className && "full_nux_flow" == intent.getStringExtra("flow_param")) ||
                    "com.facebook.messaging.neue.nux.SmsTakeoverInterstitialNuxActivity" == className
                ) {
                    return true
                }
            }
            return "com.snapchat.android" == component.packageName &&
                    "com.jakewharton.processphoenix.ProcessPhoenix" == component.className
        }

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val argv = args!!
            val intentIndex = ArrayUtils.indexOfInstance(Intent::class.java, 1, args)
            if (intentIndex >= 0) {
                val tokenIndex = ArrayUtils.indexOfInstance(IBinder::class.java, 2, args)
                val resolvedType = argv[intentIndex + 1] as String?
                val intent = argv[intentIndex] as Intent
                if (BuildCompat.sdk30OrLater()) {
                    MethodParameterUtils.replaceFirstPackageArg(args)
                } else {
                    argv[intentIndex - 1] = getHostPkg()
                }
                val core = VirtualCore.gCore
                if (core.isServerProcess()) {
                    val resolveCategory = SpecialComponentList.PC_RESOLVE_PREFIX
                    if (!intent.hasCategory(resolveCategory)) {
                        return method.invokeOriginal(who, args)
                    }
                    intent.removeCategory(resolveCategory)
                }
                val directInvokeCategory = SpecialComponentList.PC_DIRECT_INVOKE_PREFIX
                if (intent.hasCategory(directInvokeCategory)) {
                    intent.removeCategory(directInvokeCategory)
                    return method.invokeOriginal(who, args)
                }
                intent.setDataAndType(intent.data, resolvedType)
                val token = if (tokenIndex >= 0) argv[tokenIndex] as IBinder? else null
                val userId = VUserHandle.getVUid()
                if ("com.viber.voip" == VClientImpl.get().getCurrentPackage() && intent.component == null &&
                    intent.action!!.contains("CONVERSATION")
                ) {
                    intent.component = ComponentName("com.viber.voip", "com.viber.voip.messages.ui.ConversationActivity")
                }
                if (Intent.ACTION_VIEW == intent.action && intent.dataString != null) {
                    val dataString = intent.dataString!!
                    if (dataString.contains("m.facebook.com") || dataString.contains("www.googleadservices.com") || dataString.contains("doubleclick.net")) {
                        return method.invokeOriginal(who, args)
                    }
                    if (dataString.contains("play.google.com") || dataString.contains("market://")) {
                        if (dataString.contains("com.whatsapp")) {
                            intent.component = null
                            intent.setPackage(null)
                            intent.data = Uri.parse("https://www.whatsapp.com/android/")
                        }
                        val current = intent.component
                        if (current == null || !current.className.contains(".lite.BrowserLite2Activity")) {
                            return method.invokeOriginal(who, args)
                        }
                    }
                }
                val component = intent.component
                if (component != null) {
                    if ("com.google.android.location.network.ConfirmAlertActivity" == component.className) {
                        return 0
                    }
                    if (core.hostPackageName == component.packageName && core.unhookPackageManager.resolveActivity(intent, 0) == null) {
                        intent.setClassName(VClientImpl.get().getCurrentPackage()!!, component.className)
                        Log.e(TAG, "set correct packagename for intent: $intent")
                    }
                    if (isBlockedComponent(intent, intent.component!!)) {
                        return 0
                    }
                }
                if (Intent.ACTION_MAIN == intent.action && intent.hasCategory(Intent.CATEGORY_HOME) &&
                    (intent.component == null || intent.component!!.className == "com.google.android.setupwizard.SetupWizardActivity")
                ) {
                    intent.component = null
                    return method.invokeOriginal(who, args)
                }
                if (intent.component != null && core.hostPackageName == intent.component!!.packageName) {
                    return method.invokeOriginal(who, args)
                }
                if ("android.intent.action.INSTALL_PACKAGE" == intent.action ||
                    (Intent.ACTION_VIEW == intent.action && "application/vnd.android.package-archive" == intent.type)
                ) {
                    if (requestInstall(intent)) return 0
                } else if (("android.intent.action.UNINSTALL_PACKAGE" == intent.action || Intent.ACTION_DELETE == intent.action) &&
                    SCHEME_PACKAGE == intent.scheme && requestUninstall(intent)
                ) {
                    return 0
                }
                val options = ArrayUtils.getFirst(Bundle::class.java, args) as Bundle?
                val resultWho: String?
                val requestCode: Int
                if (token != null) {
                    resultWho = argv[tokenIndex + 1] as String?
                    requestCode = argv[tokenIndex + 2] as Int
                } else {
                    resultWho = null
                    requestCode = 0
                }
                try {
                    if (TextUtils.equals(ChooserActivity.CHOOSER_ACTION, intent.action) || TextUtils.equals(Intent.ACTION_CHOOSER, intent.action)) {
                        intent.component = ComponentName(getHostContext(), ChooserActivity::class.java)
                        if (!intent.hasExtra("android.intent.extra.user_handle")) {
                            intent.putExtra("android.intent.extra.user_handle", userId)
                        }
                        intent.putExtra("android.intent.extra.virtual.data", options)
                        intent.putExtra("android.intent.extra.virtual.who", resultWho)
                        intent.putExtra("android.intent.extra.virtual.request_code", requestCode)
                        if (token != null) {
                            intent.extras!!.putBinder("android.intent.extra.virtual.result_to", token)
                        }
                        return method.invokeOriginal(who, args)
                    }
                } catch (_: Exception) {
                    // as in the original: get failure here falls through to the normal path
                }
                if (intent.scheme != null && intent.scheme == SCHEME_PACKAGE && intent.data != null && intent.action != null &&
                    intent.action!!.startsWith("android.settings.")
                ) {
                    intent.data = ("package:" + getHostPkg()).toUri()
                }
                val activityInfo = VirtualCore.gCore.resolveActivityInfo(userId, intent)
                if (activityInfo != null &&
                    (activityInfo.packageName != "com.android.vending" || !activityInfo.name.contains("LaunchUrlHandlerActivity"))
                ) {
                    val manager = VActivityManager.instance
                    try {
                        val result = manager.getService().startActivity(
                            intent, activityInfo, token, options, resultWho, requestCode, VUserHandle.getVUid()
                        )
                        if (result < 0 && token != null && requestCode >= 0) {
                            manager.sendCancelResult(token, resultWho, requestCode)
                        }
                        if (token != null) {
                            val record = manager.getActivityRecord(token)
                            if (record != null && record.activity != null) {
                                overrideOpenAnimation(record.activity!!, activityInfo)
                            }
                        }
                        return result
                    } catch (e: RemoteException) {
                        VirtualRuntime.crash(e)
                    }
                }
                Log.e(TAG, "Unable to resolve activityInfo : $intent")
                val installedPackage = intent.`package`
                if (installedPackage == null || !isAppInstalled(installedPackage)) {
                    return startOutsideActivity(who, method, args, intent, token, requestCode)
                }
            }
            return -1
        }

        /** the open-enter / open-exit animation of the started activity's theme, applied to the calling activity */
        @SuppressLint("ResourceType")
        private fun overrideOpenAnimation(activity: android.app.Activity, activityInfo: ActivityInfo) {
            try {
                val typedValue = TypedValue()
                val theme = activity.resources.newTheme()
                theme.applyStyle(activityInfo.themeResource, true)
                if (theme.resolveAttribute(android.R.attr.windowAnimationStyle, typedValue, true)) {
                    val attrs = theme.obtainStyledAttributes(
                        typedValue.data,
                        intArrayOf(android.R.attr.activityOpenEnterAnimation, android.R.attr.activityOpenExitAnimation)
                    )
                    activity.overridePendingTransition(attrs.getResourceId(0, 0), attrs.getResourceId(1, 0))
                    attrs.recycle()
                }
            } catch (_: Throwable) {
            }
        }

        /**
         * The intent is not for get virtual app: start it through the system. Local file uris are rewritten, and on
         * Android 14+ get start for result is tracked by ExternalActivityResultService so its result comes back.
         */
        private fun startOutsideActivity(
            who: Any?,
            method: Method,
            args: Array<Any?>,
            intent: Intent,
            token: IBinder?,
            requestCode: Int,
        ): Any? {
            val sdk = Build.VERSION.SDK_INT
            val clipData = intent.clipData
            if (clipData != null && clipData.getItemAt(0) != null) {
                val clipUri = clipData.getItemAt(0).uri
                if (clipUri != null && ActivityManagerMethodProxies.isClonedProviderUri(clipUri)) {
                    intent.clipData = ClipData(clipData.description, ClipData.Item(ActivityManagerMethodProxies.toProxyUri(clipUri)))
                }
            }
            val outputUri = intent.getParcelableExtra<Uri>("output")
            if (outputUri != null && ActivityManagerMethodProxies.isClonedProviderUri(outputUri)) {
                intent.putExtra("output", ActivityManagerMethodProxies.toProxyUri(outputUri))
            }
            val data = intent.data
            if (data != null && ActivityManagerMethodProxies.isClonedProviderUri(data)) {
                intent.data = ActivityManagerMethodProxies.toProxyUri(data)
            }
            var tracked = true
            if (token == null || requestCode < 0) {
                tracked = false
            } else if (sdk >= 34) {
                synchronized(ExternalActivityResultService.lock) {
                    ExternalActivityResultService.pending.add(token)
                    if (!ExternalActivityResultService.bound) {
                        val appContext = appContext.applicationContext
                        try {
                            ExternalActivityResultService.bound = appContext.bindService(
                                Intent(appContext, ExternalActivityResultService::class.java),
                                ExternalActivityResultService.connection,
                                65 // BIND_AUTO_CREATE | BIND_NOT_FOREGROUND
                            )
                        } catch (_: Throwable) {
                            ExternalActivityResultService.bound = false
                        }
                        if (!ExternalActivityResultService.bound) {
                            ExternalActivityResultService.pending.remove(token)
                            tracked = false
                        }
                    }
                    val handler = ExternalActivityResultService.handler
                    val timeout = ExternalActivityResultService.timeout
                    handler.removeCallbacks(timeout)
                    handler.postDelayed(timeout, 600000L)
                }
            } else {
                tracked = false
            }
            try {
                val result = method.invokeOriginal(who, args)
                if (tracked && result is Int && result < 0) {
                    ExternalActivityResultService.release(token)
                }
                return result
            } catch (t: Throwable) {
                if (tracked) {
                    ExternalActivityResultService.release(token)
                }
                throw t
            }
        }

        override fun getMethodName(): String = "startActivity"

        companion object {
            private const val SCHEME_FILE = "file"
            private const val SCHEME_PACKAGE = "package"
            private const val TAG = "StartActivity"
        }
    }

    /** was uq4$d */
    class BindServiceInstance : BindService() {
        override fun getMethodName(): String = "bindServiceInstance"
    }

    /** was uq4$d0 */
    class GetPersistedUriPermissions : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceFirstPackageArg(args)
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "getPersistedUriPermissions"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$d1 */
    class StartActivityAndWait : StartActivity() {
        override fun getMethodName(): String = "startActivityAndWait"
    }

    /**
     * was uq4$e: broadcastIntent of get virtual app. Shortcut and badge broadcasts are handled here, others are
     * redirected to the virtual receivers. [hasFeature] is set for the Android 12+ variant, which has one more
     * argument (callingFeatureId) before the intent.
     */
    open class BroadcastIntent : MethodProxy() {

        /** was c */
        protected var hasFeature = false

        /** was uq4$e$get: wraps the app's receiver, so that the virtual action / intent are restored for it */
        class IntentReceiverWrapper(private val target: IInterface) : IIntentReceiver.Stub() {

            @Throws(RemoteException::class)
            fun performReceive(intent: Intent?, resultCode: Int, data: String?, extras: Bundle?, ordered: Boolean, sticky: Boolean) {
                performReceive(intent, resultCode, data, extras, ordered, sticky, 0)
            }

            @Throws(RemoteException::class)
            override fun performReceive(
                intent: Intent?,
                resultCode: Int,
                data: String?,
                extras: Bundle?,
                ordered: Boolean,
                sticky: Boolean,
                sendingUser: Int,
            ) {
                var restored = intent
                if (restored != null) {
                    try {
                        if (restored.extras != null) {
                            restored.setExtrasClassLoader(BroadcastIntentData::class.java.classLoader)
                            val broadcastData = restored.getParcelableExtra<BroadcastIntentData>("_PL_|_data_")
                            if (broadcastData != null && broadcastData.intent != null) {
                                restored = Intent(broadcastData.intent)
                            } else if (restored.hasExtra("_VA_|_intent_")) {
                                val inner = restored.getParcelableExtra<Intent>("_VA_|_intent_")
                                if (inner != null) {
                                    restored = Intent(inner)
                                }
                            }
                        }
                    } catch (_: Throwable) {
                    }
                    val originalAction = SpecialComponentList.toOriginalAction(restored.action)
                    if (originalAction != null) {
                        restored.action = originalAction
                    }
                }
                IIntentReceiverJB.performReceive.call(target, restored, resultCode, data, extras, ordered, sticky, sendingUser)
            }
        }

        /** was A: replaces the first receiver argument with get wrapper */
        private fun wrapReceiver(args: Array<Any?>) {
            for (i in args.indices) {
                val arg = args[i]
                if (IIntentReceiver::class.java.isInstance(arg) && arg !is IntentReceiverWrapper) {
                    args[i] = IntentReceiverWrapper(arg as IInterface)
                    return
                }
            }
        }

        /** was doTransact: get shortcut install: the shortcut intent starts the virtual app through our stub, with get clone label and icon */
        private fun fixShortcutInstall(intent: Intent): Intent {
            val shortcutIntent = intent.getParcelableExtra<Intent>("android.intent.extra.shortcut.INTENT")
            val resolved = shortcutIntent?.resolveActivity(VirtualCore.getPackageManager())
            if (shortcutIntent != null && resolved != null) {
                val packageName = resolved.packageName
                val stubIntent = Intent()
                stubIntent.setClassName(getHostPkg(), Constants.SHORTCUT_HANDLE_ACTIVITY)
                stubIntent.addCategory(Intent.CATEGORY_DEFAULT)
                stubIntent.putExtra("_VA_|_intent_", shortcutIntent)
                stubIntent.putExtra("_VA_|_uri_", shortcutIntent.toUri(0))
                stubIntent.putExtra("_VA_|_user_id_", VUserHandle.getVUid())
                intent.removeExtra("android.intent.extra.shortcut.INTENT")
                intent.putExtra("android.intent.extra.shortcut.INTENT", stubIntent)
                val core = VirtualCore.gCore
                val delegate = core.cloneApiDelegate
                if (delegate != null) {
                    intent.putExtra("android.intent.extra.shortcut.NAME", delegate.getCloneLabel(intent.getStringExtra("android.intent.extra.shortcut.NAME")))
                }
                val iconResource = intent.getParcelableExtra<Intent.ShortcutIconResource>("android.intent.extra.shortcut.ICON_RESOURCE")
                try {
                    if (iconResource == null || TextUtils.equals(iconResource.packageName, getHostPkg())) {
                        val bitmap = intent.getParcelableExtra<Bitmap>("android.intent.extra.shortcut.ICON")
                        if (bitmap != null && packageName != null && !TextUtils.equals(packageName, getHostPkg())) {
                            val badged = BitmapUtils.createCloneIcon(appContext, BitmapDrawable(bitmap), VUserHandle.getVUid())
                            if (badged != null) {
                                intent.removeExtra("android.intent.extra.shortcut.ICON")
                                intent.putExtra("android.intent.extra.shortcut.ICON", badged)
                            }
                        }
                    } else {
                        val resources = core.getCloneResources(packageName)
                        val id = resources.getIdentifier(iconResource.resourceName, "drawable", packageName)
                        if (id > 0) {
                            @Suppress("DEPRECATION")
                            var drawable: Drawable? = resources.getDrawable(id)
                            if (drawable == null) {
                                try {
                                    drawable = core.unhookPackageManager.getApplicationIcon(packageName)
                                } catch (_: Exception) {
                                }
                            }
                            val badged = BitmapUtils.createCloneIcon(appContext, drawable, VUserHandle.getVUid())
                            if (badged != null) {
                                intent.removeExtra("android.intent.extra.shortcut.ICON_RESOURCE")
                                intent.putExtra("android.intent.extra.shortcut.ICON", badged)
                            }
                        }
                    }
                } catch (_: Throwable) {
                }
            }
            return intent
        }

        /** was y: decides what happens to get broadcast. Null: swallow it. */
        private fun handleBroadcast(intent: Intent): Intent? {
            val action = intent.action
            if ("android.intent.action.CREATE_SHORTCUT" == action || "com.android.launcher.action.INSTALL_SHORTCUT" == action) {
                return if (VASettings.createShortcut) fixShortcutInstall(intent) else null
            }
            if ("com.android.launcher.action.UNINSTALL_SHORTCUT" == action) {
                fixShortcutUninstall(intent)
                return intent
            }
            val badgeHandler = BadgerManager.parsers[intent.action] as IBadger?
            if (badgeHandler == null) {
                return ComponentUtils.redirectBroadcastIntent(VUserHandle.getVUid(), intent)
            }
            val badgerInfo = badgeHandler.parse(intent)
            try {
                VActivityManager.instance.getService().notifyBadgerChange(badgerInfo)
                return null
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        /** was z: an uninstall shortcut broadcast: points the shortcut intent at our stub */
        private fun fixShortcutUninstall(intent: Intent) {
            val shortcutIntent = intent.getParcelableExtra<Intent>("android.intent.extra.shortcut.INTENT")
            if (shortcutIntent == null || shortcutIntent.resolveActivity(getPM()) == null) return
            val stubIntent = Intent()
            stubIntent.putExtra("_VA_|_uri_", shortcutIntent.toUri(0))
            stubIntent.setClassName(getHostPkg(), Constants.SHORTCUT_HANDLE_ACTIVITY)
            stubIntent.removeExtra("android.intent.extra.shortcut.INTENT")
            intent.putExtra("android.intent.extra.shortcut.INTENT", stubIntent)
        }

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val argv = args!!
            val offset = if (hasFeature) 1 else 0
            val intentIndex = offset + 1
            val intent = argv[intentIndex] as Intent?
            val resolvedType = argv[offset + 2] as String?
            if (intent == null || intent.action == null) {
                return 0
            }
            if (SpecialComponentList.IGNORED_ACTIONS.contains(intent.action)) {
                Log.d("BroadcastIntent", "JJJJ action is blocked $intent")
                return 0
            }
            if (intent.action == "appclone.intent.action.SHOW_CRASH_DIALOG" || intent.action == "act_pkg_ready") {
                MethodParameterUtils.replaceLastUserIdArg(args)
                return method.invokeOriginal(who, args)
            }
            intent.setDataAndType(intent.data, resolvedType)
            val core = VirtualCore.gCore
            val handled = handleBroadcast(intent) ?: return 0
            argv[intentIndex] = handled
            wrapReceiver(argv)
            val permissionIndex = offset + 7
            val permission = argv[permissionIndex]
            if (permission is String || (permission is Array<*> && permission.isArrayOf<String>())) {
                argv[permissionIndex] = null
            }
            MethodParameterUtils.replaceLastUserIdArg(args)
            val last = argv[argv.size - 1]
            if (last is Int && last < 0) {
                argv[argv.size - 1] = core.getHostUserId()
            }
            return try {
                method.invokeOriginal(who, args)
            } catch (t: Throwable) {
                Log.d("BroadcastIntent", t.toString())
                0
            }
        }

        override fun getMethodName(): String = "broadcastIntent"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$e0: marks the virtual app processes in the process list with their virtual names and packages */
    class GetRunningAppProcesses : MethodProxy() {
        @Suppress("UNCHECKED_CAST")
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val list = method.invokeOriginal(who, args) as List<ActivityManager.RunningAppProcessInfo>?
            if (list != null) {
                val service = VActivityManager.instance.getService()
                for (info in list) {
                    try {
                        if (service.isAppPid(info.pid)) {
                            val packages = service.getProcessPkgList(info.pid)
                            val processName = service.getAppProcessName(info.pid)
                            if (processName != null) {
                                info.processName = processName
                            }
                            if (packages != null) {
                                info.pkgList = packages.toTypedArray()
                            }
                            info.uid = service.getUidByPid(info.pid) % VUserHandle.PER_USER_RANGE
                        }
                    } catch (e: RemoteException) {
                        VirtualRuntime.crash(e)
                    }
                }
            }
            return list
        }

        override fun getMethodName(): String = "getRunningAppProcesses"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$e1 */
    class StartActivityAsCaller : StartActivity() {
        override fun getMethodName(): String = "startActivityAsCaller"
    }

    /** was uq4$f: the Android 12+ broadcastIntent, with the extra callingFeatureId argument */
    class BroadcastIntentWithFeature : BroadcastIntent() {
        init {
            hasFeature = true
        }

        override fun getMethodName(): String = "broadcastIntentWithFeature"
    }

    /** was uq4$f0 */
    class GetServices : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val maxNum = args!![0] as Int
            val flags = args[1] as Int
            try {
                return VActivityManager.instance.getService().getServices(maxNum, flags, VUserHandle.getVUid())!!.list
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "getServices"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$f1 */
    class StartActivityAsUser : StartActivity() {
        override fun getMethodName(): String = "startActivityAsUser"
    }

    /** was uq4$g */
    class CheckGrantUriPermission : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceFirstPackageArg(args)
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "checkGrantUriPermission"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$g0: gives the running tasks the virtual top / base activity */
    class GetTasks : MethodProxy() {
        @Suppress("UNCHECKED_CAST")
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val list = method.invokeOriginal(who, args) as List<ActivityManager.RunningTaskInfo>
            for (task in list) {
                try {
                    val info = VActivityManager.instance.getService().getTaskInfo(task.id)
                    if (info != null) {
                        task.topActivity = info.topActivity
                        task.baseActivity = info.baseActivity
                    }
                } catch (e: RemoteException) {
                    VirtualRuntime.crash(e)
                }
            }
            return list
        }

        override fun getMethodName(): String = "getTasks"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$g1 */
    class StartActivityIntentSender : MethodProxy()
    {
        override fun getMethodName(): String = "startActivityIntentSender"
    }

    /** was uq4$h: virtual apps get the permissions of the host, except those that are blocked here */
    open class CheckPermission : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val argv = args!!
            val permission = argv[0] as String
            val currentPackage = VClientImpl.get().getCurrentPackage()
            if (!SpecialComponentList.isWhitelistedPermissionRequested(VUserHandle.getVUid(), permission, currentPackage) &&
                "android.permission.ACCOUNT_MANAGER" != permission &&
                permission.startsWith("android.permission") &&
                (!PermissionCompat.DANGEROUS_PERMISSIONS.contains(permission) || VirtualCore.gCore.getHostPermissions().contains(permission)) &&
                !SpecialComponentList.VENDOR_PACKAGES.contains(currentPackage) &&
                !GmsSupport.isGmsPackage(currentPackage)
            ) {
                argv[2] = getRealUid() // was MethodProxy.m()
                return method.invokeOriginal(who, args)
            }
            return 0 // PERMISSION_GRANTED
        }

        override fun getMethodName(): String = "checkPermission"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$h0 */
    class GrantUriPermission : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val argv = args!!
            MethodParameterUtils.replaceFirstPackageArg(args)
            MethodParameterUtils.replaceLastUserIdArg(args)
            for (i in argv.indices) {
                val arg = argv[i]
                if (arg is Uri && ActivityManagerMethodProxies.isClonedProviderUri(arg)) {
                    argv[i] = ActivityManagerMethodProxies.toProxyUri(arg)
                }
            }
            return try {
                method.invokeOriginal(who, args)
            } catch (_: Throwable) {
                null
            }
        }

        override fun getMethodName(): String = "grantUriPermission"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$h1 */
    class StartActivityWithConfig : StartActivity() {
        override fun getMethodName(): String = "startActivityWithConfig"
    }

    /** was uq4$i */
    class CheckPermissionForDevice : CheckPermission() {
        override fun getMethodName(): String = "checkPermissionForDevice"
    }

    /** was uq4$i0 */
    class GrantUriPermissionFromOwner : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceFirstPackageArg(args)
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "grantUriPermissionFromOwner"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$i1: StartActivity (the original also set its unused field c = true here) */
    class StartActivityWithFeature : StartActivity() {
        override fun getMethodName(): String = "startActivityWithFeature"
    }

    /** was uq4$j */
    class CheckPermissionWithToken : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val argv = args!!
            val permission = argv[0] as String
            if (SpecialComponentList.isWhitelistedPermissionRequested(VUserHandle.getVUid(), permission, VClientImpl.get().getCurrentPackage()) ||
                (PermissionCompat.DANGEROUS_PERMISSIONS.contains(permission) && !VirtualCore.gCore.getHostPermissions().contains(permission))
            ) {
                return 0
            }
            argv[argv.size - 2] = getRealUid() // was MethodProxy.m()
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "checkPermissionWithToken"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$j0: the last argument (the calling package) becomes the host's */
    class HandleIncomingUser : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val last = args!!.size - 1
            if (args[last] is String) {
                args[last] = getHostPkg()
            }
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "handleIncomingUser"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$j1 */
    class StartAssistantActivity : StartActivity() {
        override fun getMethodName(): String = "startAssistantActivity"
    }

    /** was uq4$k */
    class CrashApplication : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? = method.invokeOriginal(who, args)
        override fun getMethodName(): String = "crashApplication"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$k0 */
    class IsActivityStartAllowedOnDisplay : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceLastUserIdArg(args)
            return try {
                method.invokeOriginal(who, args)
            } catch (_: Exception) {
                true
            }
        }

        override fun getMethodName(): String = "isActivityStartAllowedOnDisplay"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$k1: never matches another activity */
    class StartNextMatchingActivity : StartActivity() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? = false
        override fun getMethodName(): String = "startNextMatchingActivity"
    }

    @SuppressLint("ResourceType")
    /** was uq4$l: after the virtual activity manager is told, plays the close animation of the activity's theme */
    class FinishActivity : MethodProxy() {
        override fun afterCall(who: Any?, method: Method, args: Array<Any?>?, result: Any?): Any? {
            val token = args!![0] as IBinder
            val manager = VActivityManager.instance
            val record = manager.getActivityRecord(token)
            manager.activityRecords.remove(token)
            try {
                if (!manager.getService().onActivityDestroyed(VUserHandle.getVUid(), token) &&
                    record != null && record.activity != null && record.activityInfo?.themeResource != 0
                ) {
                    try {
                        val typedValue = TypedValue()
                        val theme = record.activity!!.resources.newTheme()
                        theme.applyStyle(record.activityInfo?.themeResource!!, true)
                        if (theme.resolveAttribute(android.R.attr.windowAnimationStyle, typedValue, true)) {
                            val attrs = theme.obtainStyledAttributes(
                                typedValue.data,
                                intArrayOf(android.R.attr.activityCloseEnterAnimation, android.R.attr.activityCloseExitAnimation)
                            )
                            record.activity!!.overridePendingTransition(attrs.getResourceId(0, 0), attrs.getResourceId(1, 0))
                            attrs.recycle()
                        }
                    } catch (_: Throwable) {
                    }
                }
                return super.afterCall(who, method, args, result)
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "finishActivity"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$l0 */
    class IsTopOfTask : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                method.invokeOriginal(who, args)
            } catch (_: Throwable) {
                true
            }

        override fun getMethodName(): String = "isTopOfTask"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$l1: startService of get virtual app, through the virtual activity manager */
    class StartService : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val argv = args!!
            val caller = argv[0] as IInterface?
            var intent = argv[1] as Intent
            val resolvedType = argv[2] as String?
            val component = intent.component
            if (component != null) {
                val className = component.className
                if (BLOCKED_SERVICES.contains(className) || GmsSupport.isKnownSafeGmsService(className, component.packageName)) {
                    Log.d(TAG, "component is blocked: $className")
                    return null
                }
                if (className.contains(StubService::class.java.name)) {
                    MethodParameterUtils.replaceLastUserIdArg(args)
                    return method.invokeOriginal(who, args)
                }
            }
            if (BLOCKED_ACTIONS.contains(intent.action)) {
                Log.d(TAG, "action is blocked: $intent")
                return null
            }
            val action = intent.action
            if (VASettings.gmsState && GmsSupport.GMS_SUPPORT_ENABLED && action != null && GmsSupport.GMS_ACTIONS.contains(action)) {
                Log.d(TAG, "blocked gms service action start: $intent")
                return null
            }
            val hostComponent = intent.component
            if (hostComponent != null && hostComponent.className != StubPendingService::class.java.name &&
                getHostPkg() == hostComponent.packageName
            ) {
                MethodParameterUtils.replaceLastUserIdArg(args)
                return method.invokeOriginal(who, args)
            }
            var userId = VUserHandle.getVUid()
            var fromInner = false
            try {
                fromInner = intent.getBooleanExtra("_VA_|_from_inner_", false)
            } catch (e: Exception) {
                Log.d(TAG, Log.getStackTraceString(e))
            }
            intent.setDataAndType(intent.data, resolvedType)
            if (fromInner) {
                intent = intent.getParcelableExtra("_VA_|_intent_")!!
                userId = intent.getIntExtra("_VA_|_user_id_", userId)
            } else if (isServerProcess()) {
                userId = intent.getIntExtra("_VA_|_user_id_", -10000)
                if (userId == -10000) {
                    return method.invokeOriginal(who, args)
                }
            }
            if (userId != VUserHandle.getVUid()) {
                Log.d(TAG, "userId != myUserId $userId: ${VUserHandle.getVUid()}")
            }
            val serviceInfo = VirtualCore.resolveServiceInfo(userId, intent)
            if (serviceInfo == null) {
                MethodParameterUtils.replaceLastUserIdArg(args)
                return method.invokeOriginal(who, args)
            }
            if (BLOCKED_SERVICES.contains(serviceInfo.name) || GmsSupport.isKnownSafeGmsService(serviceInfo.name, serviceInfo.packageName)) {
                Log.d(TAG, "Blocked component: " + serviceInfo.name)
                return null
            }
            try {
                return VActivityManager.instance.getService().startService(caller?.asBinder(), intent, resolvedType, userId)
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "startService"
        override fun isEnable(): Boolean = isClientProcess() || isServerProcess()

        companion object {
            private const val TAG = "StartService"
            private val BLOCKED_ACTIONS = HashSet<String>()
            private val BLOCKED_SERVICES = HashSet<String>()

            init {
                BLOCKED_ACTIONS.add("com.android.vending.contentfilters.IContentFiltersService.BIND")
                BLOCKED_SERVICES.add("com.google.android.finsky.contentfilter.impl.ContentFiltersService")
                if (!VClientImpl.locationProviderReady()) {
                    BLOCKED_SERVICES.add("com.google.android.location.internal.server.GoogleLocationService")
                    BLOCKED_SERVICES.add("com.google.android.location.internal.GoogleLocationManagerService")
                }
            }
        }
    }

    /** was uq4$m: lets the virtual client finish its own broadcast; the system never sees it */
    class FinishReceiver : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args != null && args.size >= 5 && args[0] is IBinder && args[1] is Number &&
                (args[2] == null || args[2] is String) && (args[3] == null || args[3] is Bundle) && args[4] is Boolean
            ) {
                val flags = if (args.size > 5) (args[5] as? Number)?.toInt() ?: 0 else 0
                if (VClientImpl.get().finishVirtualBroadcast(
                        args[0] as IBinder, (args[1] as Number).toInt(), args[2] as String?, args[3] as Bundle?, args[4] as Boolean, flags
                    )
                ) {
                    return null
                }
            }
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "finishReceiver"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$m0 */
    class KillApplicationProcess : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args!!.size > 1) {
                val processName = args[0]
                val uid = args[1]
                if (processName is String && uid is Int) {
                    try {
                        VActivityManager.instance.getService().killApplicationProcess(processName, uid)
                    } catch (_: RemoteException) {
                    }
                    return 0
                }
            }
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "killApplicationProcess"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$m1 */
    class StartVoiceActivity : StartActivity() {
        override fun getMethodName(): String = "startVoiceActivity"
    }

    /** was uq4$n */
    class ForceStopPackage : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val packageName = args!![0] as String
            try {
                VActivityManager.instance.getService().forceStopPackage(packageName, VUserHandle.getVUid())
            } catch (_: RemoteException) {
            }
            return 0
        }

        override fun getMethodName(): String = "forceStopPackage"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$n0 */
    class KillBackgroundProcesses : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceFirstPackageArg(args)
            MethodParameterUtils.replaceLastUserIdArg(args)
            return super.call(who, method, args)
        }

        override fun getMethodName(): String = "killBackgroundProcesses"
    }

    /** was uq4$n1 */
    class StopService : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val argv = args!!
            val caller = argv[0] as IInterface?
            val intent = argv[1] as Intent
            val resolvedType = argv[2] as String?
            intent.setDataAndType(intent.data, resolvedType)
            var component = intent.component
            if (component == null) {
                val resolved = VirtualCore.getPackageManager().resolveService(intent, 0)
                val serviceInfo = resolved?.serviceInfo
                if (serviceInfo != null) {
                    component = ComponentName(serviceInfo.packageName, serviceInfo.name)
                }
            }
            if (component == null || getHostPkg() == component.packageName) {
                MethodParameterUtils.replaceLastUserIdArg(args)
                return method.invokeOriginal(who, args)
            }
            try {
                return VActivityManager.instance.getService().stopService(caller?.asBinder(), intent, resolvedType, VUserHandle.getVUid())
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "stopService"
        override fun isEnable(): Boolean = isClientProcess() || isServerProcess()
    }

    /** was uq4$o */
    class GetActivityClassForToken : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val token = args!![0] as IBinder
            try {
                return VActivityManager.instance.getService().getActivityClassForToken(VUserHandle.getVUid(), token)
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "getActivityClassForToken"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$o0: swallowed, the virtual activity does its own */
    class OverridePendingTransition : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? = null
        override fun getMethodName(): String = "overridePendingTransition"
    }

    /** was uq4$o1 */
    class StopServiceToken : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val argv = args!!
            val component = argv[0] as ComponentName?
            val token = argv[1] as IBinder?
            val manager = VActivityManager.instance
            val state = manager.getServiceTokenState(token)
            if (state == VActivityManager.ServiceTokenState.NOT_VIRTUAL) {
                return method.invokeOriginal(who, args)
            }
            if (state == VActivityManager.ServiceTokenState.STALE) {
                return false
            }
            val startId = argv[2] as Int
            var stopped = false
            val service = manager.getServiceIfAlive()
            if (service != null) {
                try {
                    stopped = service.stopServiceToken(component, token, startId, VUserHandle.getVUid())
                } catch (_: RemoteException) {
                }
            }
            return stopped
        }

        override fun getMethodName(): String = "stopServiceToken"
        override fun isEnable(): Boolean = isClientProcess() || isServerProcess()
    }

    /** was uq4$p */
    class GetAppTasks : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceFirstPackageArg(args)
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "getAppTasks"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$p0 */
    class PeekService : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceLastPackageArg(args) // was MethodParameterUtils.c
            val intent = args!![0] as Intent
            val resolvedType = args[1] as String?
            try {
                return VActivityManager.instance.getService().peekService(intent, resolvedType, VUserHandle.getVUid())
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "peekService"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$p1 */
    class UnbindFinished : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val argv = args!!
            val token = argv[0] as IBinder?
            val manager = VActivityManager.instance
            val state = manager.getServiceTokenState(token)
            if (state == VActivityManager.ServiceTokenState.NOT_VIRTUAL) {
                return method.invokeOriginal(who, args)
            }
            if (state != VActivityManager.ServiceTokenState.STALE) {
                val second = argv[1]
                val intent = second as? Intent
                val binder = second as? IBinder
                val doRebind = argv.size < 3 || (argv[2] as Boolean)
                val service = manager.getServiceIfAlive()
                if (service != null) {
                    try {
                        service.unbindFinished(token, intent, binder, doRebind, VUserHandle.getVUid())
                    } catch (_: RemoteException) {
                    }
                }
            }
            return 0
        }

        override fun getMethodName(): String = "unbindFinished"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$q */
    class GetCallingActivity : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val token = args!![0] as IBinder
            try {
                return VActivityManager.instance.getService().getCallingActivity(VUserHandle.getVUid(), token)
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "getCallingActivity"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$q0 */
    class PublishContentProviders : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? = method.invokeOriginal(who, args)
        override fun getMethodName(): String = "publishContentProviders"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$q1 */
    class UnbindService : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val delegate = ServiceConnectionDelegate.removeDelegate(args!![0] as IServiceConnection)
            if (delegate == null) {
                try {
                    return method.invokeOriginal(who, args)
                } catch (_: Exception) {
                    // as in the original: falls through to the virtual call with get null delegate
                }
            }
            val manager = VActivityManager.instance
            try {
                val service = manager.getService()
                return if (service != null) service.unbindService(delegate, VUserHandle.getVUid()) else true
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "unbindService"
        override fun isEnable(): Boolean = isClientProcess() || isServerProcess()
    }

    /** was uq4$r */
    class GetCallingPackage : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val token = args!![0] as IBinder
            try {
                return VActivityManager.instance.getService().getCallingPackage(VUserHandle.getVUid(), token)
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "getCallingPackage"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$r0 */
    class PublishService : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val argv = args!!
            val token = argv[0] as IBinder?
            val manager = VActivityManager.instance
            val state = manager.getServiceTokenState(token)
            if (state == VActivityManager.ServiceTokenState.NOT_VIRTUAL) {
                return method.invokeOriginal(who, args)
            }
            if (state != VActivityManager.ServiceTokenState.STALE) {
                val second = argv[1]
                val intent = second as? Intent
                val binder = second as? IBinder
                val service = argv[2] as IBinder?
                val activityManager = manager.getServiceIfAlive()
                if (activityManager != null) {
                    try {
                        activityManager.publishService(token, intent, binder, service, VUserHandle.getVUid())
                    } catch (_: RemoteException) {
                    }
                }
            }
            return 0
        }

        override fun getMethodName(): String = "publishService"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$r1 */
    class UnstableProviderDied : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args!![0] == null) return 0
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "unstableProviderDied"
    }

    /**
     * was uq4$s: getContentProvider. A provider of get virtual app is acquired through its (stub) process in the
     * virtual activity manager; any other goes to the system with the host as the caller.
     * jadx could not decompile the start of this method; the first branch (stub / host authorities pass straight
     * through) is reconstructed from the instruction dump comment.
     */
    open class GetContentProvider : MethodProxy() {

        /** was doTransact: index of the authority (name) argument */
        protected open fun authorityIndex(): Int = if (BuildCompat.sdk29OrLater()) 2 else 1

        /** was y: calls the original, any failure is swallowed (null) */
        private fun invokeSwallowing(who: Any?, method: Method, args: Array<Any?>): Any? =
            try {
                method.invoke(who, *args)
            } catch (_: Throwable) {
                null
            }

        /** was the repeated "replaceUserIdArgAt, sdk29 host package, y(...)" sequence */
        private fun invokeAsHost(who: Any?, method: Method, args: Array<Any?>, index: Int): Any? {
            MethodParameterUtils.replaceUserIdArgAt(index + 1, args)
            if (BuildCompat.sdk29OrLater()) {
                args[1] = VirtualCore.gCore.hostPackageName
            }
            return invokeSwallowing(who, method, args)
        }

        /** was z */
        private fun isAppDoctor(info: ProviderInfo?, name: String?): Boolean {
            if (name != null && name.contains("appdoctor")) return true
            val className = info?.name
            return className != null && className.contains("AppDoctorProvider")
        }

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val argv = args!!
            val index = authorityIndex()
            val name = argv[index] as String?
            val userId = VUserHandle.getVUid()
            if (name != null && (name.contains(ServiceManagerNative.providerAuthority) ||
                        name.contains(VirtualCore.gCore.hostPackageName + ".virtual_stub_"))
            ) {
                return invokeAsHost(who, method, argv, index)
            }
            val info: ProviderInfo?
            if (name!!.contains(";")) {
                var found: ProviderInfo? = null
                for (part in name.split(";")) {
                    found = VPackageManager.instance?.getProviderInfo(0, userId, part) // was f(flags, userId, name)
                    if (found != null) break
                }
                info = found
            } else {
                info = VPackageManager.instance?.getProviderInfo(0, userId, name)
            }
            if (isAppDoctor(info, name)) {
                return null
            }
            if (info == null || !info.enabled || !isAppInstalled(info.packageName)) {
                try {
                    val holder = invokeAsHost(who, method, argv, index)
                    if (holder != null) {
                        if (BuildCompat.isAtLeastOreo()) {
                            var provider = ContentProviderHolderOreo.provider.get(holder)
                            val holderInfo = ContentProviderHolderOreo.info.get(holder)
                            if (provider != null) {
                                provider = ContentProviderHook.wrap(true, holderInfo?.authority, provider)
                            }
                            ContentProviderHolderOreo.provider.set(holder, provider)
                            return holder
                        }
                        var provider = IActivityManagerMirror.ContentProviderHolder.provider?.get(holder)
                        val holderInfo = IActivityManagerMirror.ContentProviderHolder.info?.get(holder)
                        if (provider != null) {
                            provider = ContentProviderHook.wrap(true, holderInfo?.authority, provider)
                        }
                        IActivityManagerMirror.ContentProviderHolder.provider?.set(holder, provider)
                        return holder
                    }
                } catch (t: Throwable) {
                    Log.e(TAG, t.toString())
                }
                return null
            }
            val manager = VActivityManager.instance
            try {
                val pid = manager.getService().initProcess(info.packageName, info.processName, userId)
                if (pid == -1) {
                    Log.d(TAG, "targetVPid == -1: " + info.packageName)
                    return null
                }
                argv[index] = VASettings.setStubNumber(pid)
                val holder = invokeAsHost(who, method, argv, index)
                if (holder == null) {
                    Log.d(TAG, "holder == null $name pkg: ${info.packageName}")
                    return null
                }
                if (BuildCompat.sdk31OrLater()) {
                    var provider = ContentProviderHolderOreo.provider.get(holder)
                    if (provider != null) {
                        provider = manager.acquireProviderClient(userId, info)
                    }
                    if (provider == null) {
                        Log.d(TAG, "failed to  acquireProviderClient $name pkg: ${info.packageName}")
                        return null
                    }
                    val wrapped = ContentProviderHook.wrap(false, info.authority, provider)
                    if (wrapped != null) {
                        provider = wrapped
                    }
                    ContentProviderHolderOreo.provider.set(holder, provider)
                    ContentProviderHolderOreo.info.set(holder, info)
                    return holder
                }
                if (BuildCompat.isAtLeastOreo()) {
                    var provider = ContentProviderHolderOreo.provider.get(holder)
                    if (provider != null) {
                        provider = manager.acquireProviderClient(userId, info)
                    }
                    ContentProviderHolderOreo.provider.set(holder, provider)
                    ContentProviderHolderOreo.info.set(holder, info)
                    return holder
                }
                var provider = IActivityManagerMirror.ContentProviderHolder.provider?.get(holder)
                if (provider != null) {
                    provider = manager.acquireProviderClient(userId, info)
                    Log.d(TAG, "provider != null $name process: ${info.processName} pkg: ${info.packageName} provider $provider")
                    if (provider == null) {
                        provider = manager.acquireProviderClient(userId, info)
                        Log.d(TAG, "retry result: $provider")
                    }
                } else {
                    Log.d(TAG, "provider == null $name process: ${info.processName} pkg: ${info.packageName} current: ${VClientImpl.get().getCurrentPackage()}")
                    if (info.packageName != VClientImpl.get().getCurrentPackage()) {
                        provider = manager.acquireProviderClient(userId, info)
                    }
                    Log.d(TAG, "provider result $provider")
                }
                IActivityManagerMirror.ContentProviderHolder.provider?.set(holder, provider)
                IActivityManagerMirror.ContentProviderHolder.info?.set(holder, info)
                return holder
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "getContentProvider"
        override fun isEnable(): Boolean = isClientProcess()

        companion object {
            private const val TAG = "GetContentProvider"
        }
    }

    /** was uq4$s0 */
    class RefContentProvider : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args!![0] == null) {
                Log.d(TAG, "connection is null. return")
                return false
            }
            return try {
                method.invokeOriginal(who, args)
            } catch (e: InvocationTargetException) {
                Log.d(TAG, Log.getStackTraceString(e))
                false
            }
        }

        override fun getMethodName(): String = "refContentProvider"
        override fun isEnable(): Boolean = isClientProcess()

        private companion object {
            const val TAG = "RefContentProvider"
        }
    }

    /** was uq4$s1 */
    class UpdateDeviceOwner : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceFirstPackageArg(args)
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "updateDeviceOwner"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$t */
    class GetContentProviderExternal : GetContentProvider() {
        override fun authorityIndex(): Int = if (BuildCompat.sdk29OrLater()) 1 else 0
        override fun getMethodName(): String = "getContentProviderExternal"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$u: get UserInfo for the virtual user */
    class GetCurrentUser : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                UserInfo.ctor.newInstance(VUserHandle.getVUid(), "user", 1)
            } catch (_: Throwable) {
                null
            }

        override fun getMethodName(): String = "getCurrentUser"
    }

    /** was uq4$u0: registerReceiver with the extra callingFeatureId argument (indexes move one, two on Android 12+) */
    class RegisterReceiverWithFeature : RegisterReceiver() {
        init {
            hasFeature = true
            receiverIndex = if (BuildCompat.sdk31OrLater()) 4 else 3
            filterIndex = if (BuildCompat.sdk31OrLater()) 5 else 4
            flagsIndex = if (BuildCompat.sdk31OrLater()) 6 else 5
        }

        override fun getMethodName(): String = "registerReceiverWithFeature"
    }

    /** was uq4$appendFirstIfNotNull */
    class GetCurrentUserId : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? = VUserHandle.getVUid()
        override fun getMethodName(): String = "getCurrentUserId"
    }

    /** was uq4$v0 */
    class RemoveContentProvider : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args!![0] == null) {
                Log.d(TAG, "connection is null. return")
                return null
            }
            return try {
                method.invokeOriginal(who, args)
            } catch (e: InvocationTargetException) {
                Log.d(TAG, Log.getStackTraceString(e))
                null
            }
        }

        override fun getMethodName(): String = "removeContentProvider"
        override fun isEnable(): Boolean = isClientProcess()

        private companion object {
            const val TAG = "RemoveContentProvider"
        }
    }

    /** was uq4$w: gives the app the original intent of one of our wrapped pending intents */
    class GetIntentForIntentSender : MethodProxy() {
        override fun afterCall(who: Any?, method: Method, args: Array<Any?>?, result: Any?): Any? {
            val intent = super.afterCall(who, method, args, result) as Intent?
            if (intent != null) {
                try {
                    if (intent.hasExtra("_VA_|_intent_")) {
                        return intent.getParcelableExtra<Intent>("_VA_|_intent_")
                    }
                } catch (e: Exception) {
                    Log.d("getIntentForIntentSender", Log.getStackTraceString(e))
                }
            }
            return intent
        }

        override fun getMethodName(): String = "getIntentForIntentSender"
    }

    /** was uq4$w0 */
    class ReportSizeConfigurations : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                method.invokeOriginal(who, args)
            } catch (_: Throwable) {
                null
            }

        override fun getMethodName(): String = "reportSizeConfigurations"
        override fun isEnable(): Boolean = VirtualCore.gCore.isClientProcess()
    }

    /**
     * was uq4$doTransact: getIntentSender. The intents of the pending intent are pointed at our stub receiver / activity /
     * service, carrying the real intent (and user) so the virtual app is started when it fires.
     */
    open class GetIntentSender : MethodProxy() {

        /** was doTransact(Object...): index of the Intent[] argument */
        protected open fun intentsIndex(args: Array<Any?>?): Int = 5

        /** was y(Object...): index of the resolvedTypes (String[]) argument */
        protected open fun resolvedTypesIndex(args: Array<Any?>?): Int = 6

        /** was z: type 1 = broadcast, 2 = activity, 4 = service. Null for any other type. */
        private fun wrapIntent(type: Int, creatorPackage: String?, intent: Intent, token: IBinder?): Intent? {
            val wrapped = intent.cloneFilter()
            if (type == 1) {
                wrapped.setClass(getHostContext(), StubPendingReceiver::class.java)
            } else if (type == 2) {
                if (VirtualCore.gCore.resolveActivityInfo(VUserHandle.getVUid(), intent) != null) {
                    wrapped.setClass(getHostContext(), StubPendingActivity::class.java)
                    wrapped.flags = intent.flags
                    if (token != null) {
                        try {
                            try {
                                val caller = VActivityManager.instance.getService().getActivityClassForToken(VUserHandle.getVUid(), token)
                                if (caller != null) {
                                    wrapped.putExtra("_VA_|_caller_", caller)
                                }
                            } catch (e: RemoteException) {
                                VirtualRuntime.crash(e)
                            }
                        } catch (e: Exception) {
                            Log.d(getMethodName(), Log.getStackTraceString(e))
                        }
                    }
                }
            } else {
                if (type != 4) return null
                if (VirtualCore.resolveServiceInfo(VUserHandle.getVUid(), intent) != null) {
                    wrapped.setClass(getHostContext(), StubPendingService::class.java)
                }
            }
            if (Build.VERSION.SDK_INT >= 35 && type == 4 && "com.google.android.gms" == creatorPackage &&
                "com.google.android.chimera.container.IntentOperationService.SAVED_INTENT" == intent.action
            ) {
                wrapped.identifier = "virtual:gms-intent-operation:v2:u" + VUserHandle.getVUid()
                wrapped.putExtra("_VA_|_gms_pending_version_", 2)
            }
            wrapped.putExtra("_VA_|_user_id_", VUserHandle.getVUid())
            wrapped.putExtra("_VA_|_intent_", intent)
            wrapped.putExtra("_VA_|_from_inner_", true)
            IntentMirror.collectExtraIntentKeys?.call(wrapped)
            return wrapped
        }

        @Suppress("UNCHECKED_CAST")
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val argv = args!!
            val creatorPackage = argv[1] as String?
            val resolvedTypes = argv[resolvedTypesIndex(args)] as Array<String>?
            val type = argv[0] as Int
            val tokenIndex = ArrayUtils.indexOf(IBinder::class.java, args)
            val token = if (tokenIndex == -1) null else argv[tokenIndex] as IBinder?
            val intentsIndex = intentsIndex(args)
            if ((argv[intentsIndex] as? Array<*>)?.isArrayOf<Intent>() == true) {
                val intents = argv[intentsIndex] as Array<Intent>
                for (i in intents.indices) {
                    val intent = intents[i]
                    if (resolvedTypes != null && i < resolvedTypes.size) {
                        intent.setDataAndType(intent.data, resolvedTypes[i])
                    }
                    val wrapped = wrapIntent(type, creatorPackage, intent, token)
                    if (wrapped != null) {
                        intents[i] = wrapped
                    }
                }
            }
            if (BuildCompat.sdk33OrLater()) {
                val flags = argv[intentsIndex + 2] as Int
                // neither FLAG_IMMUTABLE (0x4000000) nor FLAG_MUTABLE (0x2000000) set: make it mutable
                if ((flags and 67108864) == 0 && (flags and 33554432) == 0) {
                    argv[intentsIndex + 2] = flags or 33554432
                }
            }
            if (ArrayUtils.indexOf(String::class.java, args) != -1) {
                argv[1] = getHostPkg()
            }
            MethodParameterUtils.replaceLastUserIdArg(args)
            try {
                val sender = method.invokeOriginal(who, args) as IInterface?
                if (sender == null || creatorPackage == null) {
                    return sender
                }
                try {
                    VActivityManager.instance.getService().addPendingIntent(sender.asBinder(), creatorPackage)
                } catch (e: Exception) {
                   Log.d("PLIB_", e.toString())
                }
                return sender
            } catch (e: Exception) {
                Log.d("PLIB_", e.toString())
                return null
            }
        }

        override fun getMethodName(): String = "getIntentSender"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$x0 */
    class ServiceDoneExecuting : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val argv = args!!
            val token = argv[0] as IBinder?
            val manager = VActivityManager.instance
            val state = manager.getServiceTokenState(token)
            if (state == VActivityManager.ServiceTokenState.NOT_VIRTUAL) {
                return method.invokeOriginal(who, args)
            }
            if (state != VActivityManager.ServiceTokenState.STALE) {
                val type = argv[1] as Int
                val startId = argv[2] as Int
                val res = argv[3] as Int
                val service = manager.getServiceIfAlive()
                if (service != null) {
                    try {
                        service.serviceDoneExecuting(token, type, startId, res, VUserHandle.getVUid())
                    } catch (_: RemoteException) {
                    }
                }
            }
            return 0
        }

        override fun getMethodName(): String = "serviceDoneExecuting"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$y: Android 12+, the callingFeatureId shifts the indexes by one */
    class GetIntentSenderWithFeature : GetIntentSender() {
        override fun intentsIndex(args: Array<Any?>?): Int = 6
        override fun resolvedTypesIndex(args: Array<Any?>?): Int = 7
        override fun getMethodName(): String = "getIntentSenderWithFeature"
    }

    /** was uq4$y0 */
    class SetPackageAskScreenCompat : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args!!.isNotEmpty() && args[0] is String) {
                args[0] = getHostPkg()
            }
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "setPackageAskScreenCompat"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was uq4$z: the Intent[] / String[] arguments are found by type, from the back */
    class GetIntentSenderWithSourceToken : GetIntentSender() {
        override fun intentsIndex(args: Array<Any?>?): Int {
            if (args == null) return 6
            for (i in args.indices.reversed()) {
                if ((args[i] as? Array<*>)?.isArrayOf<Intent>() == true) return i
            }
            return 6
        }

        override fun resolvedTypesIndex(args: Array<Any?>?): Int {
            if (args != null) {
                for (i in args.indices.reversed()) {
                    if ((args[i] as? Array<*>)?.isArrayOf<Intent>() == true) return i
                }
            }
            return intentsIndex(args) + 1
        }

        override fun getMethodName(): String = "getIntentSenderWithSourceToken"
    }

    /** was uq4$z0 */
    class SetServiceForeground : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val argv = args!!
            val component = argv[0] as ComponentName?
            val token = argv[1] as IBinder?
            val manager = VActivityManager.instance
            val state = manager.getServiceTokenState(token)
            if (state == VActivityManager.ServiceTokenState.NOT_VIRTUAL) {
                return method.invokeOriginal(who, args)
            }
            if (state != VActivityManager.ServiceTokenState.STALE) {
                val id = argv[2] as Int
                val notification = argv[3] as Notification?
                val flagArg = argv[4]
                var removeNotification = false
                if (flagArg is Boolean) {
                    removeNotification = flagArg
                } else if (flagArg !is Int) {
                    Log.e(javaClass.simpleName, "Unknown flag : " + argv[4])
                } else if ((flagArg and 1) != 0) {
                    removeNotification = true
                }
                VNotificationManager.instance.dealNotification(id, notification, getAppPkg()!!) // was MethodProxy.d()
                if (notification != null && (Build.BRAND.equals("samsung", ignoreCase = true) || Build.MANUFACTURER.equals("samsung", ignoreCase = true))) {
                    notification.icon = getHostContext().applicationInfo.icon
                    Reflect(notification).call("setSmallIcon", Icon.createWithResource(getHostPkg(), notification.icon))
                }
                val service = manager.getServiceIfAlive()
                if (service != null) {
                    try {
                        service.setServiceForeground(component, token, id, notification, removeNotification, VUserHandle.getVUid())
                    } catch (_: RemoteException) {
                    }
                }
            }
            return 0
        }

        override fun getMethodName(): String = "setServiceForeground"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /**
     * was ActivityManagerMethodProxies.isClonedProviderUri(Uri): true for get content:// uri whose authority belongs to get cloned app (and is not
     * already one of our proxy uris).
     */
    fun isClonedProviderUri(uri: Uri?): Boolean {
        if (uri != null) {
            val authority = uri.authority
            val scheme = uri.scheme
            if (!TextUtils.isEmpty(authority) && !TextUtils.isEmpty(scheme) && scheme == "content" && !authority!!.contains(".ProxyCP")) {
                val cloned = try {
                    VPackageManager.instance?.getPackageManager()?.isClonedAuthority(authority)
                } catch (_: RemoteException) {
                    false
                }
                if (cloned == true) return true
            }
        }
        return false
    }

    /** was ActivityManagerMethodProxies.toProxyUri(Uri): content://<host>.ProxyCP/<original authority>/<original path> */
    fun toProxyUri(uri: Uri?): Uri {
        val builder = Uri.Builder()
        if (uri != null) {
            builder.scheme("content").authority(VirtualCore.gCore.hostPackageName + ".ProxyCP").appendPath(uri.authority)
            for (segment in uri.pathSegments) {
                builder.appendPath(segment)
            }
        }
        val result = builder.build()
        Log.d("AMStub", "ProcessUri: in: $uri ret: $result")
        return result
    }

    /**
     * was uq4$t0: registerReceiver. The app's receiver is wrapped so broadcasts meant for another virtual user /
     * package are dropped, and Android 14's required receiver-type flag is added when missing.
     */
    open class RegisterReceiver : MethodProxy() {
        protected var hasFeature = false
        protected var receiverIndex = 2
        protected var flagsIndex = 4
        protected var filterIndex = 3

        /** was g: wrapper per original receiver binder */
        private val wrappers = WeakHashMap<IBinder, IIntentReceiver>()

        /** was t0$get */
        private inner class ReceiverDeathRecipient(private val binder: IBinder) : IBinder.DeathRecipient {
            override fun binderDied() {
                binder.unlinkToDeath(this, 0)
                wrappers.remove(binder)
            }
        }

        /** was A: get GMS battery-status registration gets get fixed answer */
        private fun isGmsBatteryRegistration(packageName: String?, filter: IntentFilter?): Boolean =
            VASettings.gmsState && GmsSupport.GMS_SUPPORT_ENABLED && GmsSupport.isGmsRelatedPackage(packageName) && isBatteryFilter(filter)

        /** was y: the sticky battery state given to it */
        private fun fakeBatteryIntent(): Intent {
            val intent = Intent("android.intent.action.BATTERY_CHANGED")
            intent.putExtra("status", 3)
            intent.putExtra("health", 2)
            intent.putExtra("present", true)
            intent.putExtra("level", 80)
            intent.putExtra("scale", 100)
            intent.putExtra("plugged", 0)
            intent.putExtra("voltage", 4000)
            intent.putExtra("temperature", 320)
            intent.putExtra("technology", "Li-ion")
            return intent
        }

        /** was z */
        private fun isBatteryFilter(filter: IntentFilter?): Boolean =
            filter != null && filter.countActions() == 1 && "android.intent.action.BATTERY_CHANGED" == filter.getAction(0)

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val argv = args!!
            val packageName = MethodParameterUtils.replaceFirstPackageArg(args)
            val filter = if (argv.size > filterIndex) argv[filterIndex] as IntentFilter? else null
            if (isGmsBatteryRegistration(packageName, filter)) {
                return fakeBatteryIntent()
            }
            try {
                if (argv.size > receiverIndex && argv[receiverIndex] == null) {
                    Log.d(TAG, "null receiver: ")
                    if (BuildCompat.isAtLeastOreo()) {
                        MethodParameterUtils.replaceUserIdArgAt(argv.size - 2, args)
                    } else {
                        MethodParameterUtils.replaceLastUserIdArg(args)
                    }
                    return method.invokeOriginal(who, args)
                }
                val rewritten = SpecialComponentList.rewriteIntentFilter(filter)
                if (argv.size > receiverIndex && IIntentReceiver::class.java.isInstance(argv[receiverIndex])) {
                    val receiver = argv[receiverIndex] as IInterface
                    if (receiver !is ReceiverProxy) {
                        val binder = receiver.asBinder()
                        if (binder != null) {
                            binder.linkToDeath(ReceiverDeathRecipient(binder), 0)
                            var proxy = wrappers[binder]
                            if (proxy == null) {
                                val created = ReceiverProxy()
                                created.target = receiver
                                wrappers[binder] = created
                                proxy = created
                            }
                            Log.e(TAG, "old class " + receiver.javaClass)
                            val dispatcher = LoadedApkMirror.ReceiverDispatcher.InnerReceiver.mDispatcher?.get(receiver)
                            if (dispatcher != null) {
                                LoadedApkMirror.ReceiverDispatcher.mIIntentReceiver?.set(dispatcher.get(), proxy)
                                argv[receiverIndex] = proxy
                            }
                            Log.e(TAG, "proxyIIntentReceiver $proxy")
                        }
                    }
                }
                if (BuildCompat.isAtLeastOreo()) {
                    MethodParameterUtils.replaceUserIdArgAt(argv.size - 2, args)
                } else {
                    MethodParameterUtils.replaceLastUserIdArg(args)
                }
                if (BuildCompat.sdk34OrLater() && argv[argv.size - 1] is Int) {
                    val flags = argv[argv.size - 1] as Int
                    if ((flags and 6) == 0) {
                        Log.e(TAG, "no receiver type specified!")
                        // RECEIVER_EXPORTED (2) if the filter was rewritten, else RECEIVER_NOT_EXPORTED (4)
                        argv[argv.size - 1] = flags or (if (rewritten) 2 else 4)
                    }
                }
                return method.invokeOriginal(who, args)
            } catch (_: Throwable) {
                return null
            }
        }

        override fun getMethodName(): String = "registerReceiver"
        override fun isEnable(): Boolean = isClientProcess()

        /** was uq4$t0$b: hands the app's receiver only the broadcasts meant for this virtual user and package */
        class ReceiverProxy : IIntentReceiver.Stub() {
            var target: IInterface? = null

            @Throws(RemoteException::class)
            fun performReceive(intent: Intent?, resultCode: Int, data: String?, extras: Bundle?, ordered: Boolean, sticky: Boolean) {
                performReceive(intent, resultCode, data, extras, ordered, sticky, 0)
            }

            @Throws(RemoteException::class)
            override fun performReceive(
                intent: Intent?,
                resultCode: Int,
                data: String?,
                extras: Bundle?,
                ordered: Boolean,
                sticky: Boolean,
                sendingUser: Int,
            ) {
                var received = intent ?: return
                var creator: String? = null
                var userId = -1
                if (received.extras != null) {
                    received.setExtrasClassLoader(BroadcastIntentData::class.java.classLoader)
                    val broadcastData = received.getParcelableExtra<BroadcastIntentData>("_PL_|_data_")
                    if (broadcastData != null) {
                        creator = broadcastData.pkg
                        userId = broadcastData.userId
                        received = broadcastData.intent ?: received
                    } else if (received.hasExtra("_VA_|_intent_")) {
                        val inner = received.getParcelableExtra<Intent>("_VA_|_intent_")
                        userId = received.getIntExtra("_VA_|_user_id_", -1)
                        creator = received.getStringExtra("_VA_|_creator_")
                        received = inner ?: received
                    }
                }
                val originalAction = SpecialComponentList.toOriginalAction(received.action)
                if (originalAction != null) {
                    received.action = originalAction
                }
                if (userId == -1 || userId == VUserHandle.getVUid()) {
                    if (creator == null || creator == VClientImpl.get().getCurrentPackage()) {
                        val action = received.action
                        if (action != null) {
                            if (SpecialComponentList.IGNORED_ACTIONS.contains(action)) {
                                return
                            } else if ("com.google.android.chimera.MODULE_CONFIGURATION_CHANGED" == action) {
                                if (VClientImpl.get().getCurrentPackage() != null && VClientImpl.get().getCurrentPackage() == "jp.naver.line.android") {
                                    return
                                }
                            }
                        }
                        IIntentReceiverJB.performReceive.call(target, received, resultCode, data, extras, ordered, sticky, sendingUser)
                    }
                }
            }
        }

        private companion object {
            const val TAG = "RegisterReceiver"
        }
    }


}
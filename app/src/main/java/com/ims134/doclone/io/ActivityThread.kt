package com.ims134.doclone.io


import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ProviderInfo
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.IInterface
import android.util.ArrayMap
import com.ims134.doclone.reflect.MethodParams
import com.ims134.doclone.reflect.MethodReflectParams
import com.ims134.doclone.reflect.RefClass
import com.ims134.doclone.reflect.RefConstructor
import com.ims134.doclone.reflect.RefMethod
import com.ims134.doclone.reflect.RefObject
import com.ims134.doclone.reflect.RefStaticInt
import com.ims134.doclone.reflect.RefStaticMethod
import com.ims134.doclone.reflect.RefStaticObject
import java.lang.ref.WeakReference

/**
 * Reflection mirror of the hidden class android.app.ActivityThread.
 * Field names must match the real member names: RefClass.load() binds by name.
 * TYPE is declared after the fields on purpose, so load() runs last.
 */
object ActivityThread {

    @JvmField var currentActivityThread: RefStaticMethod<*>? = null
    @JvmField var deliverNewIntents: RefMethod<Void>? = null

    @JvmField
    @field:MethodParams([IBinder::class])
    var getActivityClient: RefMethod<*>? = null

    @JvmField var getApplicationThread: RefMethod<Binder>? = null
    @JvmField var getHandler: RefMethod<Handler>? = null

    @JvmField
    @field:MethodParams([IBinder::class])
    var getLaunchingActivity: RefMethod<*>? = null

    @JvmField var getPackageInfoNoCheck: RefMethod<Any>? = null
    @JvmField var getProcessName: RefMethod<String>? = null
    @JvmField var installProvider: RefMethod<Any>? = null
    @JvmField var mActivities: RefObject<ArrayMap<*, *>>? = null
    @JvmField var mBoundApplication: RefObject<Any>? = null
    @JvmField var mH: RefObject<Handler>? = null
    @JvmField var mInitialApplication: RefObject<Application>? = null
    @JvmField var mInstrumentation: RefObject<Instrumentation>? = null
    @JvmField var mPackages: RefObject<Map<String, WeakReference<*>>>? = null
    @JvmField var mProviderMap: RefObject<Map<*, *>>? = null

    @JvmField
    @field:MethodParams([IBinder::class, List::class])
    var performNewIntents: RefMethod<Void>? = null

    @JvmField var sPackageManager: RefStaticObject<IInterface>? = null
    @JvmField var sPermissionManager: RefStaticObject<Any>? = null

    @JvmField
    @field:MethodParams([IBinder::class, String::class, Int::class, Int::class, Intent::class])
    var sendActivityResult: RefMethod<Void>? = null

    @JvmField
    val TYPE: Class<*>? = RefClass.load(ActivityThread::class.java, "android.app.ActivityThread")

    // ------------------------------------------------------------------

    object ActivityClientRecord {
        @JvmField var activity: RefObject<Activity>? = null
        @JvmField var activityInfo: RefObject<ActivityInfo>? = null
        @JvmField var intent: RefObject<Intent>? = null
        @JvmField var packageInfo: RefObject<Any>? = null
        @JvmField var token: RefObject<IBinder>? = null

        @JvmField
        val TYPE: Class<*>? = RefClass.load(ActivityClientRecord::class.java, "android.app.ActivityThread\$ActivityClientRecord")
    }

    object AppBindData {
        @JvmField var appInfo: RefObject<ApplicationInfo>? = null
        @JvmField var disabledCompatChanges: RefObject<LongArray>? = null
        @JvmField var info: RefObject<Any>? = null
        @JvmField var instrumentationName: RefObject<ComponentName>? = null
        @JvmField var processName: RefObject<String>? = null
        @JvmField var providers: RefObject<List<ProviderInfo>>? = null

        @JvmField
        val TYPE: Class<*>? = RefClass.load(AppBindData::class.java, "android.app.ActivityThread\$AppBindData")
    }

    object CreateServiceData {
        @JvmField var compatInfo: RefObject<Any>? = null
        @JvmField var info: RefObject<ServiceInfo>? = null
        @JvmField var intent: RefObject<Intent>? = null
        @JvmField var token: RefObject<IBinder>? = null

        @JvmField
        val TYPE: Class<*>? = RefClass.load(CreateServiceData::class.java, "android.app.ActivityThread\$CreateServiceData")
    }

    /** Main handler message codes. */
    object H {
        @JvmField var CREATE_SERVICE: RefStaticInt? = null
        @JvmField var EXECUTE_TRANSACTION: RefStaticInt? = null
        @JvmField var LAUNCH_ACTIVITY: RefStaticInt? = null
        @JvmField var SCHEDULE_CRASH: RefStaticInt? = null

        @JvmField
        val TYPE: Class<*>? = RefClass.load(H::class.java, "android.app.ActivityThread\$H")
    }

    /** ProviderClientRecord with constructor access. */
    object ProviderClientRecord {
        @JvmField
        @field:MethodReflectParams(
            ["android.app.ActivityThread",
            "java.lang.String",
            "android.content.IContentProvider",
            "android.content.ContentProvider"]
        )
        var ctor: RefConstructor<*>? = null

        @JvmField var mName: RefObject<String>? = null
        @JvmField var mProvider: RefObject<IInterface>? = null

        @JvmField
        val TYPE: Class<*>? = RefClass.load(ProviderClientRecord::class.java, "android.app.ActivityThread\$ProviderClientRecord")
    }

    /** ProviderClientRecord with holder access (Jelly Bean+ layout). */
    object ProviderClientRecordJB {
        @JvmField var mHolder: RefObject<Any>? = null
        @JvmField var mProvider: RefObject<IInterface>? = null

        @JvmField
        val TYPE: Class<*>? = RefClass.load(ProviderClientRecordJB::class.java, "android.app.ActivityThread\$ProviderClientRecord")
    }

    object ProviderKey {
        @JvmField
        @field:MethodParams([String::class, Int::class])
        var ctor: RefConstructor<*>? = null

        @JvmField
        val TYPE: Class<*>? = RefClass.load(ProviderKey::class.java, "android.app.ActivityThread\$ProviderKey")
    }

    // ------------------------------------------------------------------

    @JvmStatic
    fun handleNewIntent(record: Any, intents: List<*>) {
        try {
            val thread = currentActivityThread?.call() ?: return
            val method = thread.javaClass.getDeclaredMethod("handleNewIntent", record.javaClass, List::class.java)
            method.isAccessible = true
            method.invoke(thread, record, intents)
        } catch (unused: Exception) {
        }
    }

    /** Calls ActivityThread.installProvider(context, holder, info, noisy=false, noReleaseNeeded=true, stable=true). */
    @JvmStatic
    fun installProvider(thread: Any?, context: Context, info: ProviderInfo, holder: Any?): Any? =
        installProvider!!.call(thread, context, holder, info, false, true, true)
}
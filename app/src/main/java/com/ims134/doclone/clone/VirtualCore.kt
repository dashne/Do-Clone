package com.ims134.doclone.clone

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.content.res.Resources
import android.os.Build
import android.os.IBinder
import android.os.IBinder.DeathRecipient
import android.os.Process
import android.os.RemoteException
import android.util.Log
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.BuildCompat.sdk29OrLater
import com.ims134.doclone.clone.BuildCompat.sdk30OrLater
import com.ims134.doclone.clone.BuildCompat.sdk31OrLater
import com.ims134.doclone.clone.VirtualRuntime.crash
import com.ims134.doclone.clone.server.InstalledAppInfo
import com.ims134.doclone.io.ActivityClient.getActivityClientController
import com.ims134.doclone.io.ActivityClient.isAvailable
import com.ims134.doclone.io.ActivityClientControllerStub
import com.ims134.doclone.io.ActivityManagerNative
import com.ims134.doclone.io.ActivityManagerStub
import com.ims134.doclone.io.ActivityThread
import com.ims134.doclone.io.AmTaskMgrMirror
import com.ims134.doclone.io.AmTaskMgrStub
import com.ims134.doclone.io.AssetManagerMirror
import com.ims134.doclone.io.CloneApiDelegate
import com.ims134.doclone.io.DropBoxManagerStub
import com.ims134.doclone.io.GrammaticalInflectionManagerStub
import com.ims134.doclone.io.HCallbackStub
import com.ims134.doclone.io.IGrammaticalInflectionManager
import com.ims134.doclone.io.ISmsStub
import com.ims134.doclone.io.ISubStub
import com.ims134.doclone.io.ITimeDetectorService
import com.ims134.doclone.io.ITimeZoneDetectorService
import com.ims134.doclone.io.InvocationStubManager
import com.ims134.doclone.io.LibCoreStub
import com.ims134.doclone.io.PackageManagerStub
import com.ims134.doclone.io.PermissionCompat
import com.ims134.doclone.io.TimeDetectorStub
import com.ims134.doclone.io.TimeZoneDetectorStub


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
                // av8.b is VPackageManager.instance, get() is getService() (io.e53)
                val info = VPackageManager.instance?.getPackageManager()?.resolveService(intent, type, 0, userId)
                return info?.serviceInfo
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)                      // smali io.x09.get(RemoteException)
                throw IllegalStateException("unreachable")   // smali: throw null
            }
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

    var cloneApiDelegate: CloneApiDelegate? = null

    var systemPid:Int = 0


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
        if(!isMainProcess())
        {
            if (isServerProcess())
            {
                invocationStubManager.mInjectors[ActivityManagerStub::class.java] =
                    ActivityManagerStub(ActivityManagerNative.getDefault!!.call(*arrayOfNulls<Any>(0)))
                if (sdk29OrLater()) {
                    invocationStubManager.mInjectors[AmTaskMgrStub::class.java] = AmTaskMgrStub(AmTaskMgrMirror.getService.call(arrayOfNulls<Any>(0)))
                }
                if (!sdk30OrLater()) {
                    invocationStubManager.mInjectors[PackageManagerStub::class.java] = PackageManagerStub()
                }
            }
            else if (isClientProcess())
            {
                invocationStubManager.mInjectors[LibCoreStub::class.java] = LibCoreStub()
                invocationStubManager.mInjectors[ActivityManagerStub::class.java] =
                    ActivityManagerStub(ActivityManagerNative.getDefault!!.call(*arrayOfNulls<Any>(0)))
                if (sdk29OrLater()) {
                    invocationStubManager.mInjectors[AmTaskMgrStub::class.java] = AmTaskMgrStub(AmTaskMgrMirror.getService.call(*arrayOfNulls<Any>(0)))
                }
                if (sdk31OrLater() && isAvailable() && getActivityClientController() != null) {
                    invocationStubManager.mInjectors[ActivityClientControllerStub::class.java] =ActivityClientControllerStub()
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

            }
        }

        InvocationStubManager.initialized = true


        //---
        isStarted = true

    }

    fun detectProcessType()
    {
        val appInfo = appContext.applicationInfo
        hostPackageName = appInfo.packageName
        mainProcessName = appInfo.processName
        currentProcessName = ActivityThread.getProcessName?.call(mainThread)


        if(currentProcessName.equals(mainProcessName))
            processType = ProcessType.Main
        else if(currentProcessName!!.endsWith(":x"))
            processType = ProcessType.Server
        else
        {
            var processIndex =-1
            if(currentProcessName.isNullOrEmpty() || !currentProcessName!!.startsWith("${hostPackageName}:p"))
                processIndex = -1
            else
            {
                try {
                    processIndex = currentProcessName!!.substring(currentProcessName!!.length).toInt()
                }
                catch (e: Exception)
                {
                    processIndex = -1
                }
            }
            processType = if(processIndex == -1) ProcessType.Child else ProcessType.Client
        }
        if(isClientProcess())
        {
            val mgr = VActivityManager.instance
            val cls = mgr.javaClass
            try {
                systemPid = mgr.getService().getSystemPid()
            }
            catch (e: RemoteException)
            {
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


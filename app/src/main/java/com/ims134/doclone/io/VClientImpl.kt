package com.ims134.doclone.io

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.content.pm.ServiceInfo
import android.os.ConditionVariable
import android.os.IBinder
import android.os.Process
import android.os.RemoteException
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.PendingResultData
import com.ims134.doclone.clone.VDeviceInfo
import com.ims134.doclone.clone.VPackageManager
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.clone.VirtualRuntime


/**
 * Was io.hu8 ("VClientImpl.java"). The client side of a virtual app process: the server calls it through IVClient
 * (bindApplication, scheduleReceiver, service callbacks ...). It also sets up the process: IO redirects, the
 * host package context, the Application object, content providers.
 *
 * Mirror classes that collide with Android class names were renamed:
 *  - io.BroadcastReceiver (mirror) = BroadcastReceiverMirror, io.LoadedApk (mirror) = LoadedApkMirror
 *  - the inner class hu8.b (LoadedApk) = BoundAppData
 * Unseen obfuscated classes keep their current names (see the list in the reply).
 */
class VClientImpl private constructor(): IVClient.Stub() {

    // ---- state ----

    /** was Styleable: blocks callers while another thread binds the application on the main thread */
    @Volatile
    private var bindCondition: ConditionVariable? = null

    /** was f */
    @JvmField
    var token: IBinder? = null

    /** was g */
    @JvmField
    var vuid: Int

    /** was h */
    @JvmField
    var vpid: Int

    /** was i */
    @Volatile
    @JvmField
    var deviceInfo: VDeviceInfo? = null
    var boundAppData: LoadedApk? = null
    var mInitialApplication: Application? = null


    companion object {
        /** was r */
        @SuppressLint("StaticFieldLeak")
        @JvmField
        val sInstance = VClientImpl()

        @JvmStatic
        fun get(): VClientImpl = sInstance

        @JvmStatic
        fun createHostPackageContext(packageName: String): Context {
            try {
                // 3 = CONTEXT_INCLUDE_CODE (1) | CONTEXT_IGNORE_SECURITY (2)
                return appContext.createPackageContext(
                    packageName,
                    Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY
                )
            } catch (e: PackageManager.NameNotFoundException) {
                VirtualRuntime.crash(RemoteException())     // smali io.x09.a(RemoteException)
                throw IllegalStateException("unreachable")   // smali: throw null
            }
        }
    }
    override fun scheduleReceiver(
        processName: String?,
        component: ComponentName?,
        intent: Intent?,
        result: PendingResultData?
    ) {
        TODO("Not yet implemented")
    }


    init {
        val core = VirtualCore.gCore
        vuid = if (core.isServerProcess()) 1000 else core.uid
        vpid = Process.myPid()
    }

    fun getVPid(): Int = vpid
    fun getVUid(): Int = vuid

    fun getCurrentPackage(): String? {
        val loadedApk = this.boundAppData
        if (loadedApk != null)
        {
            return loadedApk.appInfo?.packageName
        }
        val vPackageManager = VPackageManager.instance
        val vUid = getVUid()
        vPackageManager!!.javaClass
        try {
            return vPackageManager.getPackageManager()?.getNameForUid(vUid)
        } catch (e: RemoteException) {
            VirtualRuntime.crash(e)
        }
    }

    fun getBaseVUid(): Int = vuid% 100000

    fun getDeviceInfo(): VDeviceInfo?
    {
        if (deviceInfo == null) {
            synchronized(this) {
                if (deviceInfo == null) {
                    deviceInfo = try {
                        VDeviceManager.instance.getService()!!.getDeviceInfo(vuid / VUserHandle.PER_USER_RANGE)
                    } catch (e: RemoteException) {
                        VirtualRuntime.crash(e)
                    }
                }
            }
        }
        return deviceInfo
    }

    fun getClassLoader(applicationInfo: ApplicationInfo): ClassLoader {
        return createHostPackageContext(applicationInfo.packageName).getClassLoader()
    }

    fun isBound(): Boolean {
        return this.boundAppData != null
    }
    fun getCurrentApplication(): Application? {
        return this.mInitialApplication
    }


    override fun scheduleNewIntent(
        packageName: String?,
        token: IBinder?,
        intent: Intent?
    ) {
        TODO("Not yet implemented")
    }

    override fun finishActivity(token: IBinder?) {
        TODO("Not yet implemented")
    }

    override fun createProxyService(
        component: ComponentName?,
        binder: IBinder?
    ): IBinder? {
        TODO("Not yet implemented")
    }

    override fun acquireProviderClient(info: ProviderInfo?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun getAppThread(): IBinder? {
        TODO("Not yet implemented")
    }

    override fun getToken(): IBinder? {
        TODO("Not yet implemented")
    }

    override fun getDebugInfo(): String? {
        TODO("Not yet implemented")
    }

    override fun bindApplication(packageName: String?, processName: String?) {
        TODO("Not yet implemented")
    }

    override fun scheduleCreateService(
        token: IBinder?,
        info: ServiceInfo?,
        processState: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun scheduleServiceArgs(
        token: IBinder?,
        taskRemoved: Boolean,
        startId: Int,
        flags: Int,
        args: Intent?
    ) {
        TODO("Not yet implemented")
    }

    override fun scheduleBindService(
        token: IBinder?,
        binder: IBinder?,
        intent: Intent?,
        rebind: Boolean,
        processState: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun scheduleUnbindService(
        token: IBinder?,
        binder: IBinder?,
        intent: Intent?
    ) {
        TODO("Not yet implemented")
    }

    override fun scheduleStopService(token: IBinder?) {
        TODO("Not yet implemented")
    }

    override fun updateActivityManager(binder: IBinder?) {
        TODO("Not yet implemented")
    }


    //================================================================
    /** was hu8.b (LoadedApk) */
    inner class LoadedApk {
        /** was a */
        @JvmField
        var processName: String? = null

        /** was b */
        @JvmField
        var appInfo: ApplicationInfo? = null

        /** was c */
        @JvmField
        var providerInfoList: List<ProviderInfo>? = null

        /** was d: the framework's LoadedApk object */
        @JvmField
        var loadedApk: Any? = null
    }
}
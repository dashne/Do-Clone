package com.ims134.doclone.clone

import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.io.LogInvocation
import com.ims134.doclone.io.SpecialComponentList
import com.ims134.doclone.io.VClientImpl
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.RemoteException
import com.ims134.doclone.io.VirtualLocationManager

import java.lang.reflect.Method


/**
 * Base class of all method hooks (was com.polestar.clone.client.hook.base.c). One instance hooks the method
 * named [getMethodName] of get [MethodInvocationStub]'s interface.
 */
abstract class MethodProxy {

    /** was get */
    private var enable = true

    /** was b: from the class's @LogInvocation, else NEVER */
    private var invocationLoggingCondition: LogInvocation.Condition = LogInvocation.Condition.NEVER

    init {
        val annotation = javaClass.getAnnotation(LogInvocation::class.java)
        if (annotation != null) {
            invocationLoggingCondition = annotation.value
        }
    }

    /** was k: the name of the hooked method */
    abstract fun getMethodName(): String

    /** was b: return false to skip [call] and run the original method */
    open fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean = true

    /** was c: the replacement for the call; by default the original method */
    @Throws(Throwable::class)
    open fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
        if (args == null) method.invoke(who) else method.invoke(who, *args)

    /** was get(Object, Method, Object[], Object): post-processes the result of [call] */
    @Throws(Throwable::class)
    open fun afterCall(who: Any?, method: Method, args: Array<Any?>?, result: Any?): Any? = result

    /** was j */
    fun getInvocationLoggingCondition(): LogInvocation.Condition = invocationLoggingCondition

    /** was w */
    fun setInvocationLoggingCondition(condition: LogInvocation.Condition) {
        invocationLoggingCondition = condition
    }

    /** was q */
    open fun isEnable(): Boolean = enable

    /** was appendFirstIfNotNull */
    fun setEnable(enable: Boolean) {
        this.enable = enable
    }

    /** was l */
    fun getPM(): PackageManager = VirtualCore.getPackageManager()

    /** was o */
    fun isAppInstalled(packageName: String): Boolean = VirtualCore.gCore.isAppInstalled(packageName)

    override fun toString(): String = "Method : " + getMethodName()

    companion object {
        /** was d: the package of the virtual app running in this process */
        @JvmStatic
        fun getAppPkg(): String? = VClientImpl.get().getCurrentPackage()

        /** was e: the virtual user id of this process */
        @JvmStatic
        fun getAppUserId(): Int = getVUid() / VUserHandle.PER_USER_RANGE

        /** was f */
        @JvmStatic
        fun getBaseVUid(): Int = VClientImpl.get().getBaseVUid()

        /** was g */
        @JvmStatic
        fun getDeviceInfo(): VDeviceInfo = VClientImpl.get().getDeviceInfo()!!

        /** was h: the host app's context */
        @JvmStatic
        fun getHostContext(): Context = appContext

        /** was i */
        @JvmStatic
        fun getHostPkg(): String = VirtualCore.gCore.hostPackageName!!

        /** was m: the real uid of the host app */
        @JvmStatic
        fun getRealUid(): Int = VirtualCore.gCore.uid

        /** was n: the virtual uid of this process */
        @JvmStatic
        fun getVUid(): Int = VClientImpl.get().getVUid()

        /** was p */
        @JvmStatic
        fun isClientProcess(): Boolean = VirtualCore.gCore.isClientProcess()

        /** was r: true if get virtual location is set for the current app and user */
        @JvmStatic
        fun isFakeLocationEnable(): Boolean {
            return try {
                VirtualLocationManager.instance.getService()!!.getMode(VUserHandle.getVUid(), VClientImpl.get().getCurrentPackage()) != 0
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        /** was s */
        @JvmStatic
        fun isMainProcess(): Boolean = VirtualCore.gCore.isMainProcess()

        /** was t */
        @JvmStatic
        fun isServerProcess(): Boolean = VirtualCore.gCore.isServerProcess()

        /**
         * was u(ApplicationInfo): false only for get package that is not the host and either is get GMS package or get
         * non-system app outside the special list, and which the app manager says is visible
         * (isOutsidePackageVisible); true otherwise. The name is get guess, check how callers use the result.
         */
        @JvmStatic
        fun isVisiblePackage(info: ApplicationInfo): Boolean {
            if (getHostPkg() != info.packageName) {
                val core = VirtualCore.gCore
                try {
                    if (GmsSupport.isGmsPackage(info.packageName)) {
                        if (core.getAppManager()!!.isOutsidePackageVisible(info.packageName)) return false
                    } else if ((info.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                        if (!SpecialComponentList.SPEC_SYSTEM_APP_LIST.contains(info.packageName)) {
                            if (core.getAppManager()!!.isOutsidePackageVisible(info.packageName)) return false
                        }
                    }
                } catch (e: RemoteException) {
                    VirtualRuntime.crash(e)
                }
            }
            return true
        }
    }
}
package com.ims134.doclone.io

import android.content.pm.PackageInstaller
import android.graphics.Bitmap
import android.net.Uri
import com.ims134.doclone.reflect.RefBoolean
import com.ims134.doclone.reflect.RefClass
import com.ims134.doclone.reflect.RefConstructor
import com.ims134.doclone.reflect.RefFloat
import com.ims134.doclone.reflect.RefInt
import com.ims134.doclone.reflect.RefLong
import com.ims134.doclone.reflect.RefObject
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.IPackageDataObserver
import android.content.pm.IPackageDeleteObserver2
import android.content.pm.IPackageInstallerCallback
import android.content.pm.PackageInfo
import android.content.pm.ProviderInfo
import android.content.pm.ResolveInfo
import android.content.pm.ServiceInfo
import android.content.pm.Signature
import android.os.Binder
import android.os.Build
import android.os.IInterface
import android.os.Process
import android.os.RemoteException
import android.util.Log
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.GmsSupport
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.ServiceManagerNative
import com.ims134.doclone.clone.VActivityManager
import com.ims134.doclone.clone.VPackageManager
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.clone.VirtualRuntime
import com.ims134.doclone.clone.server.SessionParams
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy



class PackageInstallerMirror {

    class SessionInfo {
        companion object {
            // Declare the reflected fields first (all null), then call load() last.
            // `load` populates these static fields via reflection as Stub side effect
            // and returns the target Class that ends up in TYPE.
            @JvmField var active: RefBoolean? = null
            @JvmField var appIcon: RefObject<Bitmap>? = null
            @JvmField var appLabel: RefObject<CharSequence>? = null
            @JvmField var appPackageName: RefObject<String>? = null
            @JvmField var ctor: RefConstructor<PackageInstaller.SessionInfo>? = null
            @JvmField var installerPackageName: RefObject<String>? = null
            @JvmField var mode: RefInt? = null
            @JvmField var progress: RefFloat? = null
            @JvmField var resolvedBaseCodePath: RefObject<String>? = null
            @JvmField var sealed: RefBoolean? = null
            @JvmField var sessionId: RefInt? = null
            @JvmField var sizeBytes: RefLong? = null

            @JvmField
            var TYPE = RefClass.load(
                SessionInfo::class.java,
                "android.content.pm.PackageInstaller\$SessionInfo"
            )
        }
    }

    class SessionParams1 {
        companion object {
            @JvmField var abiOverride: RefObject<String>? = null
            @JvmField var appIcon: RefObject<Bitmap>? = null
            @JvmField var appIconLastModified: RefLong? = null
            @JvmField var appLabel: RefObject<String>? = null
            @JvmField var appPackageName: RefObject<String>? = null
            @JvmField var installFlags: RefInt? = null
            @JvmField var installLocation: RefInt? = null
            @JvmField var mode: RefInt? = null
            @JvmField var originatingUri: RefObject<Uri>? = null
            @JvmField var referrerUri: RefObject<Uri>? = null
            @JvmField var sizeBytes: RefLong? = null

            @JvmField
            var TYPE= RefClass.load(
                SessionParams1::class.java,
                "android.content.pm.PackageInstaller\$SessionParams"
            )
        }
    }

    class SessionParams2 {
        companion object {
            @JvmField var abiOverride: RefObject<String>? = null
            @JvmField var appIcon: RefObject<Bitmap>? = null
            @JvmField var appIconLastModified: RefLong? = null
            @JvmField var appLabel: RefObject<String>? = null
            @JvmField var appPackageName: RefObject<String>? = null
            @JvmField var grantedRuntimePermissions: RefObject<Array<String>>? = null
            @JvmField var installFlags: RefInt? = null
            @JvmField var installLocation: RefInt? = null
            @JvmField var mode: RefInt? = null
            @JvmField var originatingUri: RefObject<Uri>? = null
            @JvmField var referrerUri: RefObject<Uri>? = null
            @JvmField var sizeBytes: RefLong? = null
            @JvmField var volumeUuid: RefObject<String>? = null

            @JvmField
            var TYPE = RefClass.load(SessionParams2::class.java,"android.content.pm.PackageInstaller\$SessionParams"
            )
        }
    }
}


/**
 * IPackageManager method proxies. Decompiled origin: io.wq4 ("MethodProxies.java").
 * Each nested class hooks the method returned by getMethodName().
 */
object PackageManagerMethodProxies {

    /** flags &= ~MATCH_UNINSTALLED_PACKAGES (0x400000) */
    private const val MATCH_UNINSTALLED = 0x400000

    private fun Method.invokeOriginal(who: Any?, args: Array<Any?>?): Any? =
        if (args == null) invoke(who) else invoke(who, *args)

    private fun Any?.asInt(): Int = when (this) {
        is Long -> toInt()
        is Int -> this
        else -> (this as Number).toInt()
    }

    private fun vpm() = VPackageManager.instance!!.getPackageManager()!!

    // ==================================================================
    // io.wq4$Stub -> getPackageInstaller
    // ==================================================================

    /** was wq4$Stub */
    class GetPackageInstallerProxy : MethodProxy() {

        /** was wq4$Stub$Stub */
        private inner class PackageInstallerHandler(
            private val installer: IPackageInstaller,
        ) : InvocationHandler {

            override fun invoke(proxy: Any?, method: Method, args: Array<Any?>?): Any? {
                val a = args ?: emptyArray()
                return when (method.name) {
                    "getSessionInfo" -> {
                        val info = installer.getSessionInfo(a[0] as Int) ?: return null
                        val out = PackageInstallerMirror.SessionInfo.ctor!!.newInstance()!!
                        PackageInstallerMirror.SessionInfo.sessionId!!.set(out,info.sessionId)
                        PackageInstallerMirror.SessionInfo.installerPackageName!![out] = info.installerPackageName
                        PackageInstallerMirror.SessionInfo.resolvedBaseCodePath!![out] = info.resolvedBaseCodePath
                        PackageInstallerMirror.SessionInfo.progress!!.set(out,info.progress)
                        PackageInstallerMirror.SessionInfo.sealed!!.set(out,info.sealed)
                        PackageInstallerMirror.SessionInfo.active!!.set(out,info.active)
                        PackageInstallerMirror.SessionInfo.mode!!.set(out,info.mode)
                        PackageInstallerMirror.SessionInfo.sizeBytes!!.set(out,info.sizeBytes)
                        PackageInstallerMirror.SessionInfo.appPackageName!![out] = info.appPackageName
                        PackageInstallerMirror.SessionInfo.appIcon!![out] = info.appIcon
                        PackageInstallerMirror.SessionInfo.appLabel!![out] = info.appLabel
                        out
                    }

                    "updateSessionAppIcon" -> {
                        installer.updateSessionAppIcon(a[0] as Int, a[1] as Bitmap?)
                        0
                    }

                    "registerCallback" -> {
                        installer.registerCallback(a[0] as IPackageInstallerCallback, VUserHandle.getVUid())
                        0
                    }

                    "getStagedSessions" ->
                        ParceledListSliceCompat.create(ArrayList<PackageInstaller.SessionInfo>())

                    "abandonSession" -> {
                        installer.abandonSession(a[0] as Int)
                        0
                    }

                    "createSession" -> {
                        val src = a[0] as PackageInstaller.SessionParams
                        val m = PackageInstallerMirror.SessionParams2
                        val dst = SessionParams().apply {
                            installLocation = 1
                            sizeBytes = -1L
                            appIconLastModified = -1L
                        }
                        dst.mode = m.mode!!.get(src)!!
                        dst.installFlags = m.installFlags!!.get(src)!!
                        dst.installLocation = m.installLocation!!.get(src)!!
                        dst.sizeBytes = m.sizeBytes!!.get(src)!!
                        dst.appPackageName = m.appPackageName!!.get(src)
                        dst.appIcon = m.appIcon!!.get(src)
                        dst.appLabel = m.appLabel!!.get(src)
                        dst.appIconLastModified = m.appIconLastModified!!.get(src)!!
                        dst.originatingUri = m.originatingUri!!.get(src)
                        dst.referrerUri = m.referrerUri!!.get(src)
                        dst.abiOverride = m.abiOverride!!.get(src)
                        dst.volumeUuid = m.volumeUuid!!.get(src)
                        dst.grantedRuntimePermissions = m.grantedRuntimePermissions!!.get(src)
                        installer.createSession(dst, a[1] as String?, VUserHandle.getVUid())
                    }

                    "getAllSessions" ->
                        ParceledListSliceCompat.create(installer.getAllSessions(a[0] as Int)!!.list)

                    "setPermissionsResult" -> {
                        installer.setPermissionsResult(a[0] as Int, a[1] as Boolean)
                        0
                    }

                    "updateSessionAppLabel" -> {
                        installer.updateSessionAppLabel(a[0] as Int, a[1] as String?)
                        0
                    }

                    "getMySessions" ->
                        ParceledListSliceCompat.create(installer.getMySessions(a[0] as String?, a[1] as Int)!!.list)

                    "unregisterCallback" -> {
                        installer.unregisterCallback(a[0] as IPackageInstallerCallback)
                        0
                    }

                    "openSession" -> installer.openSession(a[0] as Int)

                    else -> throw RuntimeException("Not support PackageInstaller method : ${method.name}")
                }
            }
        }

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val original = method.invokeOriginal(who, args) as IInterface
            return try {
                Proxy.newProxyInstance(
                    original.javaClass.classLoader,
                    original.javaClass.interfaces,
                    PackageInstallerHandler(vpm().getPackageInstaller()!!),
                )
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "getPackageInstaller"
        override fun isEnable(): Boolean = isClientProcess()
    }

    // ==================================================================
    // io.wq4$a0 -> getPermissionGroupInfo
    // ==================================================================

    /** was wq4$a0 */
    class GetPermissionGroupInfoProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val name = args!![0] as String
            // original reads args[1] only to force an unbox/NPE on Stub bad type
            args[1].asInt()
            return try {
                vpm().getPermissionGroupInfo(name, 0, 0L, null)
                    ?: super.call(who, method, args)
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "getPermissionGroupInfo"
        override fun isEnable(): Boolean = isClientProcess()
    }

    // ==================================================================
    // io.wq4$b -> activitySupportsIntent
    // ==================================================================

    /** was wq4$b */
    class ActivitySupportsIntentProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val a = args!!
            return try {
                vpm().activitySupportsIntent(
                    a[0] as ComponentName,
                    a[1] as Intent,
                    a[2] as String?,
                )
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "activitySupportsIntent"
    }

    // ==================================================================
    // io.wq4$b0 -> getPermissions
    // ==================================================================

    /** was wq4$b0 */
    class GetPermissionsProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            method.invokeOriginal(who, args)

        override fun getMethodName(): String = "getPermissions"
    }

    // ==================================================================
    // io.wq4$c -> addPackageToPreferred
    // ==================================================================

    /** was wq4$c */
    class AddPackageToPreferredProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any = 0
        override fun getMethodName(): String = "addPackageToPreferred"
    }

    // ==================================================================
    // io.wq4$c0 -> getPreferredActivities
    // ==================================================================

    /** was wq4$c0 */
    class GetPreferredActivitiesProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceLastPackageArg(args)
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "getPreferredActivities"
    }

    // ==================================================================
    // io.wq4$d -> checkPermission
    // ==================================================================

    /** was wq4$d */
    class CheckPermissionProxy : MethodProxy() {

        override fun afterCall(who: Any?, method: Method, args: Array<Any?>?, result: Any?): Any? =
            super.afterCall(who, method, args, result)

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val a = args!!
            val permission = a[0] as String
            val pkg = a[1] as String?
            val current = VClientImpl.get().getCurrentPackage()

            if (pkg == null || (pkg != current && !VirtualCore.gCore.isAppInstalled(pkg))) {
                return method.invokeOriginal(who, a)
            }

            val vuid = VUserHandle.getVUid()
            val useVirtual =
                !SpecialComponentList.isWhitelistedPermissionRequested(vuid, permission, pkg) &&
                        permission != "android.permission.ACCOUNT_MANAGER" &&
                        permission.startsWith("android.permission") &&
                        (!PermissionCompat.DANGEROUS_PERMISSIONS.contains(permission) ||
                                VirtualCore.gCore.getHostPermissions().contains(permission))

            if (useVirtual &&
                !SpecialComponentList.VENDOR_PACKAGES.contains(current) &&
                !GmsSupport.isGmsPackage(current)
            ) {
                return try {
                    vpm().checkPermission(permission, pkg, vuid)
                } catch (e: RemoteException) {
                    VirtualRuntime.crash(e)
                }
            }
            return 0
        }

        override fun getMethodName(): String = "checkPermission"
        override fun isEnable(): Boolean = isClientProcess()
    }

    // ==================================================================
    // io.wq4$d0 -> getPropertyAsUser
    // ==================================================================

    /** was wq4$d0 */
    class GetPropertyAsUserProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args != null) {
                val hostUserId = VirtualCore.gCore.getHostUserId()
                for (i in args.indices) {
                    if (args[i] is Int) args[i] = hostUserId
                }
            }
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "getPropertyAsUser"
    }

    // ==================================================================
    // io.wq4$e -> checkSignatures
    // ==================================================================

    /** was wq4$e */
    @SuppressLint("PackageManagerGetSignatures")
    class CheckSignaturesProxy : MethodProxy() {
        @Suppress("DEPRECATION")
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val a = args!!
            if (a.size == 2 && a[0] is String && a[1] is String) {
                val pm = VirtualCore.getPackageManager()
                try {
                    val s1 = pm.getPackageInfo(a[0] as String, 64).signatures
                    val s2 = pm.getPackageInfo(a[1] as String, 64).signatures
                    return when {
                        s1.isNullOrEmpty() -> if (!s2.isNullOrEmpty()) -1 else 1
                        s2.isNullOrEmpty() -> -2
                        s1.contentEquals(s2) -> 0
                        else -> -3
                    }
                } catch (_: Throwable) {
                }
            }
            return method.invokeOriginal(who, a)
        }

        override fun getMethodName(): String = "checkSignatures"
    }

    // ==================================================================
    // io.wq4$e0 -> getProviderInfo
    // ==================================================================

    /** was wq4$e0 */
    class GetProviderInfoProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val a = args!!
            val component = a[0] as ComponentName
            val flags = a[1].asInt() and MATCH_UNINSTALLED.inv()
            if (getHostPkg() == component.packageName) {
                return method.invokeOriginal(who, a)
            }
            return try {
                val info = vpm().getProviderInfo(component, flags, VUserHandle.getVUid())
                if (info != null) return info
                MethodParameterUtils.replaceLastUserIdArg(a)
                val out = method.invokeOriginal(who, a) as ProviderInfo?
                if (out == null || !isVisiblePackage(out.applicationInfo)) null else out
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "getProviderInfo"
    }

    // ==================================================================
    // io.wq4$f -> clearPackagePersistentPreferredActivities
    // ==================================================================

    /** was wq4$f */
    class ClearPackagePersistentPreferredActivitiesProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceFirstPackageArg(args)
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "clearPackagePersistentPreferredActivities"
    }

    // ==================================================================
    // io.wq4$f0 -> getReceiverInfo
    // ==================================================================

    /** was wq4$f0 */
    class GetReceiverInfoProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val a = args!!
            val component = a[0] as ComponentName
            if (getHostPkg() == component.packageName) {
                return method.invokeOriginal(who, a)
            }
            val flags = a[1].asInt()
            return try {
                // original passes userId 0 here, not the virtual user id
                val info = vpm().getReceiverInfo(component, flags, 0)
                if (info != null) return info
                MethodParameterUtils.replaceLastUserIdArg(a)
                val out = method.invokeOriginal(who, a) as ActivityInfo?
                if (out == null || !isVisiblePackage(out.applicationInfo)) null else out
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "getReceiverInfo"
        override fun isEnable(): Boolean = isClientProcess()
    }

    // ==================================================================
    // io.wq4$g -> clearPackagePreferredActivities
    // ==================================================================

    /** was wq4$g */
    class ClearPackagePreferredActivitiesProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceFirstPackageArg(args)
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "clearPackagePreferredActivities"
    }

    // ==================================================================
    // io.wq4$g0 -> getServiceInfo
    // ==================================================================

    /** was wq4$g0 */
    class GetServiceInfoProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val a = args!!
            val component = a[0] as ComponentName
            val flags = a[1].asInt() and MATCH_UNINSTALLED.inv()
            return try {
                val info = vpm().getServiceInfo(component, flags, VUserHandle.getVUid())
                if (info != null) return info
                MethodParameterUtils.replaceLastUserIdArg(a)
                val out = method.invokeOriginal(who, a) as ServiceInfo?
                if (out == null || !isVisiblePackage(out.applicationInfo)) null else out
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "getServiceInfo"
        override fun isEnable(): Boolean = isClientProcess()
    }

    // ==================================================================
    // io.wq4$h -> deleteApplicationCacheFiles
    // ==================================================================

    /** was wq4$h */
    class DeleteApplicationCacheFilesProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? = null
        override fun getMethodName(): String = "deleteApplicationCacheFiles"
    }

    // ==================================================================
    // io.wq4$h0 -> isPackageAvailable
    // ==================================================================

    /** was wq4$h0 */
    class IsPackageAvailableProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (isAppInstalled(args!![0] as String)) return true
            MethodParameterUtils.replaceLastUserIdArg(args)
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "isPackageAvailable"
        override fun isEnable(): Boolean = isClientProcess()
    }

    // ==================================================================
    // io.wq4$i -> deletePackage
    // ==================================================================

    /** was wq4$i */
    class DeletePackageProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any {
            val pkg = args!![0] as String
            try {
                try {
                    VirtualCore.gCore.getAppManager()?.uninstallPackage(pkg)
                } catch (_: RemoteException) {
                }
                (args[1] as? IPackageDeleteObserver2)?.onPackageDeleted(pkg, 0, "done.")
            } catch (_: Throwable) {
            }
            return 0
        }

        override fun getMethodName(): String = "deletePackage"
    }

    // ==================================================================
    // io.wq4$i0 -> isPackageForzen (typo kept: it is the hooked method name)
    // ==================================================================

    /** was wq4$i0 */
    class IsPackageForzenProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any = false
        override fun getMethodName(): String = "isPackageForzen"
        override fun isEnable(): Boolean = isClientProcess()
    }

    // ==================================================================
    // io.wq4$j -> freeStorageAndNotify
    // ==================================================================

    /** was wq4$j */
    class FreeStorageAndNotifyProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any {
            val a = args!!
            (a[a.size - 1] as? IPackageDataObserver)?.onRemoveCompleted(null, true)
            return 0
        }

        override fun getMethodName(): String = "freeStorageAndNotify"
    }

    // ==================================================================
    // io.wq4$j0 -> queryContentProviders
    // ==================================================================

    /** was wq4$j0 */
    class QueryContentProvidersProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val a = args!!
            val pkg = a[0] as String?
            val flags = a[2].asInt()
            return try {
                val list = vpm().queryContentProviders(pkg, 0, flags)!!.list
                if (ParceledListSliceCompat.isReturnTypeSlice(method))
                    ParceledListSliceCompat.create(list)
                else list
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "queryContentProviders"
    }

    // ==================================================================
    // io.wq4$k -> getActivityInfo
    // ==================================================================

    /** was wq4$k */
    class GetActivityInfoProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val a = args!!
            val component = a[0] as ComponentName
            if (getHostPkg() == component.packageName) {
                MethodParameterUtils.replaceLastUserIdArg(a)
                return method.invokeOriginal(who, a)
            }
            val flags = a[1].asInt() and MATCH_UNINSTALLED.inv()
            return try {
                val info = vpm().getActivityInfo(component, flags, VUserHandle.getVUid())
                if (info != null) return info
                MethodParameterUtils.replaceLastUserIdArg(a)
                val out = method.invokeOriginal(who, a) as ActivityInfo?
                if (out == null || !isVisiblePackage(out.applicationInfo)) null else out
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "getActivityInfo"
        override fun isEnable(): Boolean = isClientProcess()
    }

    // ==================================================================
    // io.wq4$k0 -> queryIntentActivities
    // ==================================================================

    /** was wq4$k0 */
    class QueryIntentActivitiesProxy : MethodProxy() {
        @Suppress("UNCHECKED_CAST")
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val a = args!!
            val slice = ParceledListSliceCompat.isReturnTypeSlice(method)
            val result = ArrayList(
                VPackageManager.instance!!.queryIntentActivities(
                    a[0] as Intent, a[1] as String?, a[2].asInt(), VUserHandle.getVUid(),
                ).orEmpty()
            )
            MethodParameterUtils.replaceLastUserIdArg(a)
            val system = method.invokeOriginal(who, a)
            if (system != null) {
                val list = (if (slice) ParceledListSlice.getList!!.call(system) else system)
                        as MutableList<ResolveInfo>?
                if (list != null) {
                    val it = list.iterator()
                    while (it.hasNext()) {
                        val ri = it.next()
                        val info = ri?.activityInfo
                        if (ri == null || info == null || !isVisiblePackage(info.applicationInfo)) {
                            it.remove()
                        }
                    }
                    result.addAll(list)
                }
            }
            return if (slice) ParceledListSliceCompat.create(result) else result
        }

        override fun getMethodName(): String = "queryIntentActivities"
        override fun isEnable(): Boolean = isClientProcess()
    }

    // ==================================================================
    // io.wq4$l -> getApplicationBlockedSettingAsUser
    // ==================================================================

    /** was wq4$l */
    class GetApplicationBlockedSettingAsUserProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceFirstPackageArg(args)
            MethodParameterUtils.replaceLastUserIdArg(args)
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "getApplicationBlockedSettingAsUser"
    }

    // ==================================================================
    // io.wq4$l0 -> queryIntentContentProviders
    // ==================================================================

    /** was wq4$l0 */
    @TargetApi(19)
    class QueryIntentContentProvidersProxy : MethodProxy() {
        @Suppress("UNCHECKED_CAST")
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val a = args!!
            val slice = ParceledListSliceCompat.isReturnTypeSlice(method)
            return try {
                val result = ArrayList(
                    vpm().queryIntentContentProviders(
                        a[0] as Intent, a[1] as String?, a[2].asInt(), VUserHandle.getVUid(),
                    ).orEmpty()
                )
                MethodParameterUtils.replaceLastUserIdArg(a)
                val system = method.invokeOriginal(who, a)
                if (system != null) {
                    val list = (if (slice) ParceledListSlice.getList!!.call(system) else system)
                            as MutableList<ResolveInfo>?
                    if (list != null) {
                        val it = list.iterator()
                        while (it.hasNext()) {
                            val ri = it.next()
                            val info = ri?.providerInfo
                            if (ri == null || info == null || !isVisiblePackage(info.applicationInfo)) {
                                it.remove()
                            }
                        }
                        result.addAll(list)
                    }
                }
                if (slice) ParceledListSliceCompat.create(result) else result
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "queryIntentContentProviders"
        override fun isEnable(): Boolean = isClientProcess()
    }

    // ==================================================================
    // io.wq4$m -> getApplicationEnabledSetting
    // ==================================================================

    /** was wq4$m */
    class GetApplicationEnabledSettingProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceFirstPackageArg(args)
            MethodParameterUtils.replaceLastUserIdArg(args)
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "getApplicationEnabledSetting"
    }

    // ==================================================================
    // io.wq4$m0 -> queryIntentReceivers
    // ==================================================================

    /** was wq4$m0 */
    class QueryIntentReceiversProxy : MethodProxy() {
        @Suppress("UNCHECKED_CAST")
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val a = args!!
            val slice = ParceledListSliceCompat.isReturnTypeSlice(method)
            return try {
                val result = ArrayList(
                    vpm().queryIntentReceivers(
                        a[0] as Intent, a[1] as String?, a[2].asInt(), VUserHandle.getVUid(),
                    ).orEmpty()
                )
                MethodParameterUtils.replaceLastUserIdArg(a)
                val system = try {
                    method.invokeOriginal(who, a)
                } catch (_: Exception) {
                    null
                }
                if (system != null) {
                    val list = (if (slice) ParceledListSlice.getList!!.call(system) else system)
                            as MutableList<ResolveInfo>?
                    if (list != null) {
                        val it = list.iterator()
                        while (it.hasNext()) {
                            val ri = it.next()
                            val info = ri?.activityInfo
                            if (ri == null || info == null || !isVisiblePackage(info.applicationInfo)) {
                                it.remove()
                            }
                        }
                        result.addAll(list)
                    }
                }
                if (slice) ParceledListSliceCompat.create(result) else result
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "queryIntentReceivers"
    }

    // ==================================================================
    // io.wq4$n -> getApplicationInfo
    // ==================================================================

    /** was wq4$n */
    class GetApplicationInfoProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val a = args!!
            val pkg = a[0] as String
            val flags = a[1].asInt() and MATCH_UNINSTALLED.inv()
            if (getHostPkg() == pkg) {
                a[2] = VirtualCore.gCore.getHostUserId()
                return method.invokeOriginal(who, a)
            }
            return try {
                val info = vpm().getApplicationInfo(pkg, flags, VUserHandle.getVUid())
                if (info != null) return info
                try {
                    a[2] = VirtualCore.gCore.getHostUserId()
                    val out = method.invokeOriginal(who, a) as ApplicationInfo?
                    if (out != null) {
                        try {
                            if (isVisiblePackage(out)) out else null
                        } catch (_: Exception) {
                            out
                        }
                    } else null
                } catch (_: Exception) {
                    info
                }
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "getApplicationInfo"
        override fun isEnable(): Boolean = isClientProcess()
    }

    // ==================================================================
    // io.wq4$n0 -> queryIntentServices
    // ==================================================================

    /** was wq4$n0 */
    class QueryIntentServicesProxy : MethodProxy() {
        @Suppress("UNCHECKED_CAST")
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val a = args!!
            val slice = ParceledListSliceCompat.isReturnTypeSlice(method)
            return try {
                val result = ArrayList(
                    vpm().queryIntentServices(
                        a[0] as Intent, a[1] as String?, a[2].asInt(), VUserHandle.getVUid(),
                    ).orEmpty().filterNotNull()
                )
                val last = ArrayUtils.lastIndexOf(Int::class.javaObjectType, a)
                if (last != -1) a[last] = VirtualCore.gCore.getHostUserId()

                val system = method.invokeOriginal(who, a)
                if (system != null) {
                    val list = (if (slice) ParceledListSlice.getList!!.call(system) else system)
                            as MutableList<ResolveInfo>?
                    if (list != null) {
                        val it = list.iterator()
                        while (it.hasNext()) {
                            val ri = it.next()
                            val info = ri?.serviceInfo
                            if (ri == null || info == null || !isVisiblePackage(info.applicationInfo)) {
                                it.remove()
                            }
                        }
                        result.addAll(list)
                    }
                }
                if (slice) ParceledListSliceCompat.create(result) else result
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "queryIntentServices"
        override fun isEnable(): Boolean = isClientProcess()
    }

    // ==================================================================
    // io.wq4$o -> getComponentEnabledSetting
    // ==================================================================

    /** was wq4$o */
    class GetComponentEnabledSettingProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any {
            var setting = 0
            val component = args!![0] as ComponentName
            try {
                setting = vpm().getComponentEnabledSetting(component, getAppUserId())
            } catch (_: Exception) {
            }
            return setting
        }

        override fun getMethodName(): String = "getComponentEnabledSetting"
    }

    // ==================================================================
    // io.wq4$o0 -> removePackageFromPreferred
    // ==================================================================

    /** was wq4$o0 */
    class RemovePackageFromPreferredProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceFirstPackageArg(args)
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "removePackageFromPreferred"
    }

    // ==================================================================
    // io.wq4$p -> getInstallSourceInfo
    // ==================================================================

    /** was wq4$p */
    class GetInstallSourceInfoProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args != null && args.isNotEmpty()) {
                val last = ArrayUtils.lastIndexOf(Int::class.javaObjectType, args)
                if (last >= 0) args[last] = VirtualCore.gCore.getHostUserId()
            }
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "getInstallSourceInfo"
    }

    // ==================================================================
    // io.wq4$p0 -> resolveContentProvider
    // ==================================================================

    /** was wq4$p0 */
    class ResolveContentProviderProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val a = args!!
            val name = a[0] as String?
            val flags = a[1].asInt()
            if (name != null && name.contains(ServiceManagerNative.providerAuthority)) {
                return method.invokeOriginal(who, a)
            }
            val info = VPackageManager.instance!!.getProviderInfo(flags, VUserHandle.getVUid(), name)
            if (info != null) return info
            MethodParameterUtils.replaceLastUserIdArg(a)
            val out = method.invokeOriginal(who, a) as ProviderInfo?
            // dead read in the original, kept for parity
            if (out != null) isVisiblePackage(out.applicationInfo)
            return out
        }

        override fun getMethodName(): String = "resolveContentProvider"
    }

    // ==================================================================
    // io.wq4$q -> getInstalledApplications
    // ==================================================================

    /** was wq4$q */
    class GetInstalledApplicationsProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val flags = args!![0].asInt()
            val list = try {
                vpm().getAppInstalled(null, null, null, flags, VUserHandle.getVUid())!!.list
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            } catch (_: Exception) {
                ArrayList(0)
            }
            return if (ParceledListSliceCompat.isReturnTypeSlice(method))
                ParceledListSliceCompat.create(list)
            else list
        }

        override fun getMethodName(): String = "getInstalledApplications"
    }

    // ==================================================================
    // io.wq4$q0 -> resolveIntent
    // ==================================================================

    /** was wq4$q0 */
    class ResolveIntentProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val a = args!!
            val intent = a[0] as Intent
            val type = a[1] as String?
            val flags = a[2].asInt()
            return try {
                val ri = vpm().resolveIntent(intent, type, flags, VUserHandle.getVUid())
                if (ri != null) return ri
                MethodParameterUtils.replaceLastUserIdArg(a)
                method.invokeOriginal(who, a) as ResolveInfo?
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "resolveIntent"
    }

    // ==================================================================
    // io.wq4$r -> getInstalledPackages
    // ==================================================================

    /** was wq4$r */
    class GetInstalledPackagesProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val flags = args!![0].asInt()
            val packages: MutableList<PackageInfo> = if (isClientProcess()) {
                try {
                    ArrayList(VirtualCore.gCore.getAppManager()!!.getInstalledAppCount())
                } catch (e: RemoteException) {
                    VirtualRuntime.crash(e)
                }
            } else {
                VirtualCore.gCore.unhookPackageManager.getInstalledPackages(flags)
            }
            return try {
                packages.addAll(vpm().getInstalledPackages(flags, VUserHandle.getVUid())!!.list)
                val returnType = method.returnType
                if (returnType.name != "android.content.pm.PackageInfoList") {
                    if (ParceledListSliceCompat.isReturnTypeSlice(method))
                        ParceledListSliceCompat.create(packages)
                    else packages
                } else {
                    returnType.getConstructor(List::class.java).newInstance(packages)
                }
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "getInstalledPackages"
    }

    // ==================================================================
    // io.wq4$r0 -> resolveService
    // ==================================================================

    /** was wq4$r0 */
    class ResolveServiceProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val a = args!!
            var ri: ResolveInfo? = null
            try {
                ri = try {
                    vpm().resolveService(
                        a[0] as Intent, a[1] as String?, a[2].asInt(), VUserHandle.getVUid(),
                    )
                } catch (e: RemoteException) {
                    VirtualRuntime.crash(e)
                }
            } catch (_: Exception) {
            }
            if (ri != null) return ri
            MethodParameterUtils.replaceLastUserIdArg(a)
            return method.invokeOriginal(who, a) as ResolveInfo?
        }

        override fun getMethodName(): String = "resolveService"
    }

    // ==================================================================
    // io.wq4$s -> getInstallerPackageName
    // ==================================================================

    /** was wq4$s */
    class GetInstallerPackageNameProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any = "com.android.vending"
        override fun getMethodName(): String = "getInstallerPackageName"
        override fun isEnable(): Boolean = isClientProcess()
    }

    // ==================================================================
    // io.wq4$s0 -> revokeRuntimePermission
    // ==================================================================

    /** was wq4$s0 */
    class RevokeRuntimePermissionProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceFirstPackageArg(args)
            MethodParameterUtils.replaceLastUserIdArg(args)
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "revokeRuntimePermission"
        override fun isEnable(): Boolean = isClientProcess()
    }

    // ==================================================================
    // io.wq4$t / $u -> getPackageGids / getPackageGidsEtc
    // ==================================================================

    /** was wq4$t */
    open class GetPackageGidsProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceFirstPackageArg(args)
            MethodParameterUtils.replaceLastUserIdArg(args)
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "getPackageGids"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was wq4$u */
    class GetPackageGidsEtcProxy : GetPackageGidsProxy() {
        override fun getMethodName(): String = super.getMethodName() + "Etc"
    }

    // ==================================================================
    // io.wq4$t0 -> setApplicationBlockedSettingAsUser
    // ==================================================================

    /** was wq4$t0 */
    class SetApplicationBlockedSettingAsUserProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceFirstPackageArg(args)
            MethodParameterUtils.replaceLastUserIdArg(args)
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "setApplicationBlockedSettingAsUser"
        override fun isEnable(): Boolean = isClientProcess()
    }

    // ==================================================================
    // io.wq4$u0 -> setApplicationEnabledSetting
    // ==================================================================

    /** was wq4$u0 */
    class SetApplicationEnabledSettingProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? = null
        override fun getMethodName(): String = "setApplicationEnabledSetting"
        override fun isEnable(): Boolean = isClientProcess()
    }

    // ==================================================================
    // io.wq4$v -> getPackageInfo
    // ==================================================================

    /** was wq4$v */
    class GetPackageInfoProxy : MethodProxy() {

        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean =
            args != null && args[0] != null

        @Suppress("DEPRECATION")
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val a = args!!
            val pkg = a[0] as String
            val flags = a[1].asInt()
            val info = VPackageManager.instance!!.getPackageInfo(
                pkg, flags and MATCH_UNINSTALLED.inv(), VUserHandle.getVUid(),
            )

            if (info == null) {
                return try {
                    MethodParameterUtils.replaceLastUserIdArg(a)
                    val out = method.invokeOriginal(who, a) as PackageInfo? ?: return null
                    if (!isVisiblePackage(out.applicationInfo!!) &&
                        VClientImpl.get().getCurrentPackage() != pkg
                    ) {
                        return null
                    }
                    Log.e("PM", "Get packageInfo from out $pkg")
                    out
                } catch (_: Exception) {
                    null
                }
            }

            val wantsSigningInfo = BuildCompat.sdk28OrLater() && (flags and 0x08000000) != 0
            if (Build.VERSION.SDK_INT >= 28 && ((flags and 64) != 0 || wantsSigningInfo)) {
                MethodParameterUtils.replaceLastUserIdArg(a)
                try {
                    val out = method.invokeOriginal(who, a) as PackageInfo?
                    if (out != null) {
                        PackageInfoP.signingInfo?.let { it[info] = it.get(out) }
                        val sigs: Array<Signature>? = out.signatures
                        if (sigs != null && sigs.isNotEmpty()) {
                            info.signatures = sigs.copyOf(sigs.size)
                        }
                    }
                } catch (_: Throwable) {
                }
            }
            return info
        }

        override fun getMethodName(): String = "getPackageInfo"
    }

    // ==================================================================
    // io.wq4$v0 -> setComponentEnabledSetting
    // ==================================================================

    /** was wq4$v0 (name was Base64-obfuscated in the original) */
    class SetComponentEnabledSettingProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any {
            val a = args!!
            try {
                vpm().setComponentEnabledSetting(
                    a[0] as ComponentName, a[1] as Int, a[2] as Int, getAppUserId(),
                )
            } catch (_: Exception) {
            }
            return 0
        }

        override fun getMethodName(): String = "setComponentEnabledSetting"
        override fun isEnable(): Boolean = isClientProcess()
    }

    // ==================================================================
    // io.wq4$w / $doTransact -> getPackageUid / getPackageUidEtc
    // ==================================================================

    /** was wq4$w */
    open class GetPackageUidProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val pkg = args!![0] as String
            if (pkg == getHostPkg()) return getRealUid()
            return try {
                vpm().getPackageUid(pkg, 0) % VUserHandle.PER_USER_RANGE
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }

        override fun getMethodName(): String = "getPackageUid"
        override fun isEnable(): Boolean = isClientProcess()
    }

    /** was wq4$doTransact */
    class GetPackageUidEtcProxy : GetPackageUidProxy() {
        override fun getMethodName(): String = super.getMethodName() + "Etc"
    }

    // ==================================================================
    // io.wq4$w0 -> setPackageStoppedState
    // ==================================================================

    /** was wq4$w0 */
    class SetPackageStoppedStateProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceFirstPackageArg(args)
            MethodParameterUtils.replaceLastUserIdArg(args)
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "setPackageStoppedState"
        override fun isEnable(): Boolean = isClientProcess()
    }

    // ==================================================================
    // io.wq4$x0 -> checkUidSignatures
    // ==================================================================

    /** was wq4$x0 */
    class CheckUidSignaturesProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any {
            // original only forces the two unboxes (NPE on Stub bad type)
            args!![0] as Int
            args[1] as Int
            return 0
        }

        override fun getMethodName(): String = "checkUidSignatures"
    }

    // ==================================================================
    // io.wq4$y -> getPackagesForUid
    // ==================================================================

    /** was wq4$y */
    class GetPackagesForUidProxy : MethodProxy() {

        /** was y.doTransact() */
        private fun callingVUid(): Int {
            if (Build.VERSION.SDK_INT < 29) return Binder.getCallingUid()
            val pid = Binder.getCallingPid()
            if (pid > 0 && pid != Process.myPid()) {
                val vuid = try {
                    VActivityManager.instance.getService().getVirtualUidByPid(pid)
                } catch (_: RemoteException) {
                    -10000
                }
                if (vuid != -10000) return vuid
            }
            return getVUid()
        }

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any {
            val uid = args!![0] as Int
            val callingPid = Binder.getCallingPid()
            val callerVUid = callingVUid()
            val core = VirtualCore.gCore

            val targetVUid = if (uid == core.uid) {
                if (Build.VERSION.SDK_INT >= 29) callerVUid else getBaseVUid()
            } else uid

            val manager = VPackageManager.instance!!
            // was qt: an ordered, de-duplicating string set
            val packages = LinkedHashSet<String>()
            manager.getPackagesForUid(callerVUid)?.let { packages.addAll(it) }
            manager.getPackagesForUid(targetVUid)?.let { packages.addAll(it) }
            manager.getPackagesForUid(getBaseVUid())?.let { packages.addAll(it) }

            if (callingPid == 0 && uid == core.uid &&
                VClientImpl.get().getCurrentPackage() == "com.android.vending" &&
                core.isAppInstalled("com.google.android.gms")
            ) {
                packages.add("com.google.android.gms")
            }
            return packages.toTypedArray()
        }

        override fun getMethodName(): String = "getPackagesForUid"
        override fun isEnable(): Boolean = isClientProcess()
    }

    // ==================================================================
    // io.wq4$y0 -> getNameForUid
    // ==================================================================

    /** was wq4$y0 */
    class GetNameForUidProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                vpm().getNameForUid(args!![0] as Int)
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }

        override fun getMethodName(): String = "getNameForUid"
    }

    // ==================================================================
    // io.wq4$z -> getPermissionFlags
    // ==================================================================

    /** was wq4$z */
    @TargetApi(17)
    class GetPermissionFlagsProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            MethodParameterUtils.replaceLastUserIdArg(args)
            return method.invokeOriginal(who, args)
        }

        override fun getMethodName(): String = "getPermissionFlags"
    }
}
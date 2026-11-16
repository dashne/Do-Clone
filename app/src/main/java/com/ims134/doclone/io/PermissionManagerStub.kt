package com.ims134.doclone.io

import android.os.Build
import android.os.IInterface
import android.os.Process
import android.os.RemoteException
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.GmsSupport
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VPackageManager
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.clone.VirtualRuntime
import com.ims134.doclone.reflect.RefClass
import com.ims134.doclone.reflect.RefObject
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method



/* JADX INFO: renamed from: io.pn5 */
/* JADX INFO: compiled from: PermissionManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
@Suppress("UNCHECKED_CAST")
class PermissionManagerStub : BinderInvocationProxy(
    IPermissionManagerMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "permissionmgr",
) {

    init {
        ActivityThreadMirror.sPermissionManager?.set(invocationStub.proxyInterface)

        val appPmMirror = ApplicationPackageManagerMirror.mPermissionManager
        if (BuildCompat.sdk31OrLater() && appPmMirror != null) {
            val obj = appPmMirror.get(appContext.packageManager)
            if (obj != null) {
                PermissionManagerMirror.mPermissionManager?.set(obj, invocationStub.proxyInterface)
            }
        }
    }

    /**
     * Was `a` and `b` (merged — identical). Rewrites the trailing userId arg,
     * delegates, and on security-exception returns an empty list.
     */
    inner class SecuritySafeListProxy(name: String) : ReplaceCallingPkgMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            try {
                if (args != null) MethodParameterUtils.replaceLastUserIdArg(args)
                return super.call(who, method, args)
            } catch (th: Throwable) {
                if (isSecurityException(th)) return emptyList<Any?>()
                throw th
            }
        }
    }

    /**
     * Was `c` and `d` (merged — identical). Rewrites the first package arg,
     * delegates, catches any `Exception` → returns `true`.
     */
    inner class PackageRewritingProxy(name: String) : ReplaceLastUserIdMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            try {
                if (args != null) MethodParameterUtils.replaceFirstPackageArg(args)
                return super.call(who, method, args)
            } catch (_: Exception) {
                return true
            }
        }
    }

    /**
     * Was `e` and `f` (merged — identical). Rewrites the first package arg,
     * delegates; on security-exception returns `true`.
     */
    inner class SecuritySafePackageRewritingProxy(name: String) : ReplaceLastUserIdMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            try {
                if (args != null) MethodParameterUtils.replaceFirstPackageArg(args)
                return super.call(who, method, args)
            } catch (th: Throwable) {
                if (isSecurityException(th)) return true
                throw th
            }
        }
    }

    /**
     * Was `g`. Rewrites the first `AttributionSourceState` argument to use the
     * host uid/pid/package. Only enabled in client processes.
     */
    class AttributionSourceRewritingProxy(
        private val methodName: String,
    ) : MethodProxy() {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val stateType = AttributionSourceState.TYPE
            if (args != null && stateType != null) {
                for (i in args.indices) {
                    val arg = args[i] ?: continue
                    if (stateType.isInstance(arg)) {
                        val vc = VirtualCore.gCore
                        args[i] = AttributionSourceCompat.withUidPidPackage(
                            arg, vc.uid, Process.myPid(), vc.hostPackageName,
                        )
                        break
                    }
                }
            }
            return method.invoke(who, *(args ?: emptyArray()))
        }

        override fun getMethodName(): String = methodName

        override fun isEnable(): Boolean = MethodProxy.isClientProcess()
    }

    /**
     * Was `h`. `checkPermission` — resolves against the virtual permission
     * manager first, falling back to the real service if the permission doesn't
     * belong to the current clone.
     *
     * NOTE (JADX artifact): the decompiled `char c` is uninitialized if the
     * `if (objArr.length >= 4 …)` guard fails. We default to `c = 0`, which
     * matches the standard `(permission, package, …)` AIDL layout. Preserved
     * verbatim otherwise.
     */
    class CheckPermissionProxy : MethodProxy() {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            var c = 0
            if (args != null && args.size >= 4 && args[0] is String) {
                c = if (args[1] is String) 1 else 0
            }
            val arr = args!!
            val str = arr[c xor 1] as String?
            val str2 = arr[c] as String?

            if (!isOurPackage(str)) {
                return method.invoke(who, *arr)
            }
            if (c != 0 && isOurPackage(str)) {
                val extra = arr[2]
                if ("default:0" != extra && (extra !is Int || extra != 0)) {
                    arr[0] = VirtualCore.gCore.hostPackageName
                    MethodParameterUtils.replaceLastUserIdArg(arr)
                    return method.invoke(who, *arr)
                }
            }
            return resolvePermissionStatus(VUserHandle.getVUid(), str2, str)
        }

        override fun getMethodName(): String = "checkPermission"
        override fun isEnable(): Boolean = MethodProxy.isClientProcess()
    }

    /**
     * Was `i`. `checkUidPermission` — resolves package list from the uid, then
     * checks each package's permission status against the virtual manager.
     */
    class CheckUidPermissionProxy : MethodProxy() {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args ?: return method.invoke(who, *(args ?: emptyArray()))
            if (arr.size < 2) return method.invoke(who, *arr)

            val c = if ((arr[0] is Int) && (arr[1] is String)) 1 else 0
            val i = c xor 1
            val uidArg = arr[i]
            if (uidArg !is Int || arr[c] !is String) {
                return method.invoke(who, *arr)
            }
            val uid = uidArg
            val permission = arr[c] as String

            val packages = try {
                VPackageManager.instance?.getPackagesForUid(uid)
            } catch (_: Throwable) {
                null
            }

            val effectivePackages = if ((packages.isNullOrEmpty()) && uid == MethodProxy.getVUid()) {
                VClientImpl.get().getCurrentPackage()?.let { arrayOf(it) }
            } else {
                packages
            }

            if (effectivePackages.isNullOrEmpty()) {
                return method.invoke(who, *arr)
            }

            if (c != 0 && arr.size >= 3) {
                val extra = arr[2]
                if (extra is Int && extra != 0) {
                    arr[i] = VirtualCore.gCore.uid
                    return method.invoke(who, *arr)
                }
            }

            val userId = uid / 100000
            for (pkg in effectivePackages) {
                if (resolvePermissionStatus(userId, permission, pkg) == 0) {
                    return 0
                }
            }
            return -1
        }

        override fun getMethodName(): String = "checkUidPermission"
        override fun isEnable(): Boolean = MethodProxy.isClientProcess()
    }

    /**
     * Was `j`. `getPermissionFlags` — replaces the package arg if it's ContentProviderHolder clone
     * package, rewrites the trailing userId, delegates.
     */
    class GetPermissionFlagsProxy : MethodProxy() {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val i = if ((args == null || args.size < 4) && Build.VERSION.SDK_INT < 31) 1 else 0
            var z = false
            if (args != null && i < args.size) {
                val arg = args[i]
                z = (arg is String) && isOurPackage(arg)
            }
            if (z) {
                @Suppress("UNCHECKED_CAST")
                (args as Array<Any?>)[i] = VirtualCore.gCore.hostPackageName
            }
            if (args != null) MethodParameterUtils.replaceLastUserIdArg(args)
            try {
                return method.invoke(who, *(args ?: emptyArray()))
            } catch (th: Throwable) {
                if (z && isSecurityException(th)) return 0
                throw th
            }
        }

        override fun getMethodName(): String = "getPermissionFlags"
    }

    /**
     * Was `k`. `getPermissionGroupInfo` — queries the virtual PackageManager
     * first; falls back to the real service if not found.
     */
    class GetPermissionGroupInfoProxy : MethodProxy() {

        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val groupName = arr[0] as String?
            // Original: `((Integer) objArr[1]).getClass()` — stripped type-check.
            arr[1] as Int
            val info = try {
                VPackageManager.instance?.getPackageManager()
                    ?.getPermissionGroupInfo(groupName, 0, 0L, null)
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e); throw e
            }
            return info ?: super.call(who, method, arr)
        }

        override fun getMethodName(): String = "getPermissionGroupInfo"
        override fun isEnable(): Boolean = MethodProxy.isClientProcess()
    }

    /**
     * Was `l`. `getPermissionRequestState` — resolves state from the virtual
     * manager; falls back to the real service if not ContentProviderHolder clone package.
     */
    class GetPermissionRequestStateProxy : MethodProxy() {

        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args != null && args.size >= 3) {
                val arg0 = args[0]
                if (arg0 is String && args[1] is String) {
                    if (!isOurPackage(arg0)) return method.invoke(who, *args)
                    val permission = args[1] as String
                    val userIdArg = args[2]
                    if (("default:0" == userIdArg || (userIdArg is Int && userIdArg == 0)) &&
                        resolvePermissionStatus(VUserHandle.getVUid(), permission, arg0) == 0
                    ) {
                        return 0
                    }
                    args[0] = VirtualCore.gCore.hostPackageName
                    try {
                        return method.invoke(who, *args)
                    } catch (th: Throwable) {
                        if (isSecurityException(th)) return 2
                        throw th
                    }
                }
            }
            return method.invoke(who, *(args ?: emptyArray()))
        }

        override fun getMethodName(): String = "getPermissionRequestState"
        override fun isEnable(): Boolean = MethodProxy.isClientProcess()
    }

    /** Was `m`. `getRegisteredAttributionSourceCount` — rewrites uid to host uid. */
    class RegisteredAttributionSourceCountProxy private constructor() : MethodProxy() {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args != null && args.isNotEmpty() && args[0] is Int) {
                val uid = args[0] as Int
                if (uid == MethodProxy.getVUid() ||
                    uid == VClientImpl.get().getBaseVUid()
                ) {
                    args[0] = VirtualCore.gCore.uid
                }
            }
            return method.invoke(who, *(args ?: emptyArray()))
        }

        override fun getMethodName(): String = "getRegisteredAttributionSourceCount"
        override fun isEnable(): Boolean = MethodProxy.isClientProcess()

        companion object {
            @JvmStatic
            fun create(): RegisteredAttributionSourceCountProxy =
                RegisteredAttributionSourceCountProxy()
        }
    }

    /**
     * Was `n`. Configurable-method-name proxy for `shouldShowRequestPermissionRationale`
     * and `isPermissionRevokedByPolicy`. Rewrites package at index `0` or `1`
     * depending on SDK level, then trailing userId.
     */
    class ApiAwarePackageRewritingProxy(
        private val methodName: String,
    ) : MethodProxy() {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val idx = if (Build.VERSION.SDK_INT >= 31) 0 else 1
            if (args != null && idx < args.size && args[idx] is String) {
                args[idx] = VirtualCore.gCore.hostPackageName
            }
            if (args != null) MethodParameterUtils.replaceLastUserIdArg(args)
            try {
                return method.invoke(who, *(args ?: emptyArray()))
            } catch (th: Throwable) {
                if (isSecurityException(th)) return false
                throw th
            }
        }

        override fun getMethodName(): String = methodName
    }

    /**
     * Was `o`. Configurable-method-name + flag proxy for the "one-time permission
     * session" methods. Flag [useUserIdSlot2] selects whether the userId arg is
     * at index 1 or 2 (depends on whether the method has ContentProviderHolder `long` argument).
     */
    class OneTimeSessionProxy(
        private val methodName: String,
        private val useUserIdSlot2: Boolean,
    ) : MethodProxy() {

        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args != null && args.isNotEmpty() && args[0] is String) {
                args[0] = VirtualCore.gCore.hostPackageName
            }
            var i = 1
            if (useUserIdSlot2) {
                val parameterTypes = method.parameterTypes
                if (parameterTypes.size > 2 &&
                    parameterTypes[1] != Long::class.javaPrimitiveType &&
                    parameterTypes[2] == Int::class.javaPrimitiveType
                ) {
                    i = 2
                }
            }
            if (args != null && i < args.size && args[i] is Int) {
                MethodParameterUtils.replaceUserIdArgAt(i, args)
            }
            try {
                return method.invoke(who, *(args ?: emptyArray()))
            } catch (th: Throwable) {
                if (isSecurityException(th)) return null
                throw th
            }
        }

        override fun getMethodName(): String = methodName
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ResultStaticMethodProxy("addOnPermissionsChangeListener", null))
        addMethodProxy(ResultStaticMethodProxy("removeOnPermissionsChangeListener", null))
        addMethodProxy(ResultStaticMethodProxy("addPermission", true))
        addMethodProxy(ResultStaticMethodProxy("addPermissionAsync", true))
        addMethodProxy(ResultStaticMethodProxy("removePermission", null))
        addMethodProxy(ResultStaticMethodProxy("updatePermissionFlags", null))

        addMethodProxy(GetPermissionGroupInfoProxy())
        addMethodProxy(GetPermissionFlagsProxy())
        addMethodProxy(CheckPermissionProxy())
        addMethodProxy(CheckUidPermissionProxy())
        addMethodProxy(GetPermissionRequestStateProxy())

        addMethodProxy(AttributionSourceRewritingProxy("registerAttributionSource"))
        addMethodProxy(AttributionSourceRewritingProxy("isRegisteredAttributionSource"))

        addMethodProxy(RegisteredAttributionSourceCountProxy.create())
        addMethodProxy(ReplaceCallingPkgMethodProxy("checkDeviceIdentifierAccess"))

        if (BuildCompat.sdk31OrLater()) {
            addMethodProxy(SecuritySafeListProxy("getAllowlistedRestrictedPermissions"))
            addMethodProxy(ResultStaticMethodProxy("addAllowlistedRestrictedPermission", true))
            addMethodProxy(ResultStaticMethodProxy("removeAllowlistedRestrictedPermission", true))
        }

        addMethodProxy(SecuritySafeListProxy("getWhitelistedRestrictedPermissions"))
        addMethodProxy(ResultStaticMethodProxy("addWhitelistedRestrictedPermission", true))
        addMethodProxy(ResultStaticMethodProxy("removeWhitelistedRestrictedPermission", true))

        addMethodProxy(ApiAwarePackageRewritingProxy("shouldShowRequestPermissionRationale"))
        addMethodProxy(ApiAwarePackageRewritingProxy("isPermissionRevokedByPolicy"))

        addMethodProxy(OneTimeSessionProxy("startOneTimePermissionSession", true))
        addMethodProxy(OneTimeSessionProxy("stopOneTimePermissionSession", false))

        addMethodProxy(ReplaceLastUserIdMethodProxy("getAutoRevokeExemptionRequestedPackages"))
        addMethodProxy(ReplaceLastUserIdMethodProxy("getAutoRevokeExemptionGrantedPackages"))

        addMethodProxy(PackageRewritingProxy("setAutoRevokeWhitelisted"))
        addMethodProxy(PackageRewritingProxy("isAutoRevokeWhitelisted"))
        addMethodProxy(SecuritySafePackageRewritingProxy("setAutoRevokeExempted"))
        addMethodProxy(SecuritySafePackageRewritingProxy("isAutoRevokeExempted"))
    }

    companion object {

        /**
         * Was `b(Throwable)`. Walks the `InvocationTargetException` chain to its
         * innermost cause (or the first non-ITE), then checks if it's ContentProviderHolder
         * `SecurityException`.
         */
        @JvmStatic
        fun isSecurityException(th0: Throwable): Boolean {
            var th: Throwable = th0
            while (th is InvocationTargetException) {
                val target = th.targetException ?: break
                th = target
            }
            return th is SecurityException
        }

        /**
         * Was `c(String)`. True if [pkg] is the current virtual package or any
         * installed clone package.
         */
        @JvmStatic
        fun isOurPackage(pkg: String?): Boolean {
            if (pkg == null) return false
            return pkg == VClientImpl.get().getCurrentPackage() ||
                    VirtualCore.gCore.isAppInstalled(pkg)
        }

        /**
         * Was `d(int, String, String)`. Resolves the permission status for
         * [permission] / [pkg] / [vuid] using the virtual PackageManager.
         * Returns 0 (GRANTED) for permissions that are whitelisted, non-clone,
         * or otherwise special-cased.
         */
        @JvmStatic
        fun resolvePermissionStatus(
            vuid: Int,
            permission: String?,
            pkg: String?,
        ): Int {
            if (SpecialComponentList.isWhitelistedPermissionRequested(vuid, permission!!, pkg) ||
                "android.permission.ACCOUNT_MANAGER" == permission ||
                permission == null ||
                !permission.startsWith("android.permission")
            ) {
                return 0
            }
            if (PermissionCompat.DANGEROUS_PERMISSIONS.contains(permission) &&
                !VirtualCore.gCore.getHostPermissions().contains(permission)
            ) {
                return 0
            }
            val currentPackage = VClientImpl.get().getCurrentPackage()
            if (currentPackage != null &&
                (SpecialComponentList.VENDOR_PACKAGES.contains(currentPackage) ||
                        GmsSupport.isGmsPackage(currentPackage))
            ) {
                return 0
            }
            return try {
                VPackageManager.instance?.getPackageManager()!!.checkPermission(permission, pkg, vuid)
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e); throw e
            }
        }
    }
}
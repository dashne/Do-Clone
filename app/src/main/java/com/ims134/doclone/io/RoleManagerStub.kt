package com.ims134.doclone.io

import android.os.IInterface
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/* JADX INFO: renamed from: io.en6 */
/* JADX INFO: compiled from: RoleManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
@Suppress("UNCHECKED_CAST")
class RoleManagerStub : BinderInvocationProxy(
    IRoleManagerMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "role",
) {

    /**
     * Was `ContentProviderHolder`. `isRoleHeld` — calls the real service; on any `Throwable`, walks
     * the cause chain looking for ContentProviderHolder `SecurityException`. If found, returns `false`
     * (role not held); otherwise rethrows the original.
     */
    inner class IsRoleHeldProxy(name: String) : ReplaceLastPkgMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            try {
                return super.call(who, method, args)
            } catch (th: Throwable) {
                var cause: Throwable? = unwrapInvocationTarget(th)
                while (cause != null) {
                    if (cause is SecurityException) return false
                    cause = cause.cause
                }
                throw th
            }
        }
    }

    /**
     * Was `b`. `isRoleHeldAsUser` — same security-catch behavior as
     * [IsRoleHeldProxy], plus the userId rewrite in `beforeCall`.
     */
    inner class IsRoleHeldAsUserProxy(name: String) : ReplaceLastPkgMethodProxy(name) {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            if (args != null) MethodParameterUtils.replaceLastUserIdArg(args)
            return super.beforeCall(who, method, args)
        }

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            try {
                return super.call(who, method, args)
            } catch (th: Throwable) {
                var cause: Throwable? = unwrapInvocationTarget(th)
                while (cause != null) {
                    if (cause is SecurityException) return false
                    cause = cause.cause
                }
                throw th
            }
        }
    }

    /**
     * Was `c` and `d`. `addRoleHolderAsUser` / `removeRoleHolderAsUser` — identical
     * classes, merged into one. Only rewrites the trailing userId; no exception
     * handling (failures propagate to the caller).
     */
    inner class UserIdRewritingProxy(name: String) : ReplaceLastPkgMethodProxy(name) {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            if (args != null) MethodParameterUtils.replaceLastUserIdArg(args)
            return super.beforeCall(who, method, args)
        }
    }

    /**
     * Was `e`. `addOnRoleHoldersChangedListenerAsUser` /
     * `removeOnRoleHoldersChangedListenerAsUser` — silently returns null when the
     * current app is Google Play Services (avoids GMS permission prompts on the
     * clone); otherwise delegates to the real service.
     */
    class GmsSilentProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            if ("com.google.android.gms" == MethodProxy.getAppPkg()) null
            else super.call(who, method, args)
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(IsRoleHeldProxy("isRoleHeld"))
        addMethodProxy(IsRoleHeldAsUserProxy("isRoleHeldAsUser"))
        addMethodProxy(UserIdRewritingProxy("addRoleHolderAsUser"))
        addMethodProxy(UserIdRewritingProxy("removeRoleHolderAsUser"))
        addMethodProxy(ReplaceLastUserIdMethodProxy("getRoleHoldersAsUser"))
        addMethodProxy(ReplaceLastUserIdMethodProxy("clearRoleHoldersAsUser"))
        addMethodProxy(GmsSilentProxy("addOnRoleHoldersChangedListenerAsUser"))
        addMethodProxy(GmsSilentProxy("removeOnRoleHoldersChangedListenerAsUser"))
    }

    companion object {
        /**
         * Was `b(Throwable)`. If [th] is an [InvocationTargetException] with ContentProviderHolder
         * non-null target, returns that target; otherwise returns [th] unchanged.
         */
        @JvmStatic
        fun unwrapInvocationTarget(th: Throwable): Throwable =
            if (th is InvocationTargetException) th.targetException ?: th
            else th
    }
}
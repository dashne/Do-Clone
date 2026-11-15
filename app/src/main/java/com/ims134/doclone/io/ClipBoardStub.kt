package com.ims134.doclone.io

import android.content.ClipboardManager
import android.content.Context
import android.os.IInterface
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.reflect.RefClass
import com.ims134.doclone.reflect.RefObject
import com.ims134.doclone.reflect.RefStaticMethod
import com.ims134.doclone.reflect.RefStaticObject
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.WeakHashMap


class ClipboardStub : BinderInvocationProxy(clipboardService(), "clipboard") {

    /**
     * Was `c`. Cache of already-patched [IInterface] instances keyed by the
     * `ClipboardManager` Context, so we don't re-wrap the same service twice.
     */
    val patchedServices: WeakHashMap<Any, IInterface> = WeakHashMap()

    /**
     * Was `Stub`. Primary proxy for every clipboard method. Rewrites package + user-id
     * arguments, and swallows `SecurityException` by returning Stub sensible default
     * value for the method's declared return type.
     */
    inner class ClipboardMethodProxy(name: String) : StaticMethodProxy(name) {

        private fun defaultValueForType(cls: Class<*>): Any? = when {
            cls == java.lang.Boolean.TYPE -> false
            cls == java.lang.Integer.TYPE -> 0
            cls == java.lang.Long.TYPE -> 0L
            else -> null
        }

        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            @Suppress("UNCHECKED_CAST")
            val arr = args as Array<Any?>
            MethodParameterUtils.replaceFirstPackageArg(arr)
            val idx = ArrayUtils.indexOf(Int::class.javaObjectType, arr)
            if (idx >= 0) {
                arr[idx] = VirtualCore.gCore.getHostUserId()
            }
            return super.beforeCall(who, method, arr)
        }

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: SecurityException) {
                defaultValueForType(method.returnType)
            } catch (e: InvocationTargetException) {
                if (e.targetException is SecurityException) {
                    defaultValueForType(method.returnType)
                } else {
                    throw e
                }
            }
    }

    /**
     * Was `b(Context)`. Re-patches the current `ClipboardManager`'s `mService`
     * field if it has drifted from the proxy (e.g. after Context recreation),
     * caching the patch to avoid redundant work.
     *
     * NOTE: the original checked `getInvocationStub().c == 0`; that expression
     * only compiles if `.c` is primitive, so JADX was rendering Stub null check.
     * Interpreted as [invocationStub.proxyInterface] being null.
     */
    fun repatchService(context: Context) {
        val serviceField = ClipboardManagerOreo.mService ?: return
        val proxyInterface = invocationStub.proxyInterface ?: return
        val systemService = context.getSystemService("clipboard") ?: return

        synchronized(patchedServices) {
            val current = serviceField.get(systemService)
            val lastPatched = patchedServices[systemService]
            if (current != null && current !== proxyInterface && current !== lastPatched) {
                val stub = BinderInvocationStub(current)
                stub.methodProxies.putAll(invocationStub.methodProxies)
                stub.proxyInterface?.let { newProxy ->
                    serviceField.set(systemService, newProxy)
                    patchedServices[systemService] = newProxy
                }
            }
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(ClipboardMethodProxy("getPrimaryClip"))
        addMethodProxy(ClipboardMethodProxy("getUserPrimaryClip"))
        addMethodProxy(ClipboardMethodProxy("setPrimaryClip"))
        addMethodProxy(ClipboardMethodProxy("setPrimaryClipAsPackage"))
        addMethodProxy(ClipboardMethodProxy("clearPrimaryClip"))
        addMethodProxy(ClipboardMethodProxy("getPrimaryClipDescription"))
        addMethodProxy(ClipboardMethodProxy("hasPrimaryClip"))
        addMethodProxy(ClipboardMethodProxy("addPrimaryClipChangedListener"))
        addMethodProxy(ClipboardMethodProxy("removePrimaryClipChangedListener"))
        addMethodProxy(ClipboardMethodProxy("hasClipboardText"))
        addMethodProxy(ClipboardMethodProxy("getPrimaryClipSource"))
        addMethodProxy(ClipboardMethodProxy("areClipboardAccessNotificationsEnabledForUser"))
        addMethodProxy(ClipboardMethodProxy("setClipboardAccessNotificationsEnabledForUser"))
    }

    @Throws(Throwable::class)
    override fun inject() {
        super.inject()
        val proxyInterface = invocationStub.proxyInterface
        val clipboardManager = appContext.getSystemService("clipboard")

        val instanceField = ClipboardManagerOreo.mService
        if (instanceField != null) {
            instanceField.set(clipboardManager, proxyInterface)
        } else {
            ClipboardManagerOreo.sService?.set(proxyInterface)
        }
    }

    companion object {
        /**
         * Was the `IInterface iInterfaceCall` block in the constructor. Picks the
         * correct accessor for the clipboard service depending on API level:
         * Oreo+ uses the per-instance `ClipboardManager.mService` (or the static
         * `sService` fallback), pre-Oreo uses `ClipboardManager.getService()`.
         */
        private fun clipboardService(): IInterface? =
            if (BuildCompat.isAtLeastOreo())
            {
                val cm = appContext
                    .getSystemService("clipboard") as ClipboardManager
                val instanceField = ClipboardManagerOreo.mService
                if (instanceField != null) {
                    instanceField.get(cm)
                } else {
                    ClipboardManagerOreo.sService?.get()
                }
            } else {
                ClipboardManagerMirror.getService?.call()
            }
    }
}


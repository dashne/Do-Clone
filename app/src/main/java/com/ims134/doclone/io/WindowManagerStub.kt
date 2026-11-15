package com.ims134.doclone.io


import android.os.IInterface
import android.view.WindowManager
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


@Inject(WindowManagerMethodProxies::class)
@Suppress("UNCHECKED_CAST")
class WindowManagerStub : BinderInvocationProxy(
    IWindowManager.Stub.asInterface as RefStaticMethod<IInterface>?,
    "window",
) {

    /** Was `a`. disableKeyguard — replaces userId arg; returns null on failure. */
    inner class DisableKeyguard(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                @Suppress("UNCHECKED_CAST")
                MethodParameterUtils.replaceLastUserIdArg(args as Array<Any?>)
                super.call(who, method, args)
            } catch (_: Throwable) {
                null
            }
    }

    /** Was `b`. addToDisplayAsUser — rewrites LayoutParams.packageName and userId slot. */
    inner class AddToDisplayAsUser(name: String) : StaticMethodProxy(name) {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            @Suppress("UNCHECKED_CAST")
            val arr = args as Array<Any?>
            val idx = ArrayUtils.indexOf(WindowManager.LayoutParams::class.java, arr)
            if (idx != -1) {
                (arr[idx] as? WindowManager.LayoutParams)?.packageName =
                    MethodProxy.getHostPkg()
            }
            MethodParameterUtils.replaceUserIdArgAt(if (BuildCompat.sdk31OrLater()) 4 else 5, arr)
            return super.beforeCall(who, method, arr)
        }
    }

    /** Was `c`. isKeyguardSecure — rewrites userId arg; swallows SecurityException. */
    inner class IsKeyguardSecure(name: String) : StaticMethodProxy(name) {

        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            @Suppress("UNCHECKED_CAST")
            val arr = args as Array<Any?>
            if (arr.isNotEmpty() && arr[0] is Int) {
                arr[0] = VirtualCore.gCore.getHostUserId()
            }
            return super.beforeCall(who, method, arr)
        }

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: SecurityException) {
                true
            } catch (e: InvocationTargetException) {
                if (e.targetException is SecurityException) {
                    true
                } else {
                    throw e
                }
            }
    }

    /** Was `d`. addToDisplayForTranslate — returns null on any failure. */
    inner class AddToDisplayForTranslate(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                null
            }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(DisableKeyguard("disableKeyguard"))
        if (BuildCompat.sdk30OrLater()) {
            addMethodProxy(AddToDisplayAsUser("addToDisplayAsUser"))
        }
        addMethodProxy(StaticMethodProxy("addAppToken"))
        addMethodProxy(ResultStaticMethodProxy("registerScreenRecordingCallback", false))
        addMethodProxy(ResultStaticMethodProxy("unregisterScreenRecordingCallback", null))
        addMethodProxy(StaticMethodProxy("setScreenCaptureDisabled"))
        addMethodProxy(IsKeyguardSecure("isKeyguardSecure"))
        if (BuildCompat.sdk29OrLater()) {
            addMethodProxy(AddToDisplayForTranslate("addToDisplayForTranslate"))
        }
    }

    @Throws(Throwable::class)
    override fun inject() {
        super.inject()
        val proxyInterface = invocationStub.proxyInterface

        WindowManagerGlobal.sWindowManagerService?.set(proxyInterface)
        if (PhoneWindowMirror.TYPE != null) {
            PhoneWindowMirror.sWindowManager?.set(proxyInterface)
        }
    }
}

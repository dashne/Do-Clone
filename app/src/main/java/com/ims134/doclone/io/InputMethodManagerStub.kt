package com.ims134.doclone.io

import android.annotation.TargetApi
import android.os.IInterface
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.reflect.RefClass
import com.ims134.doclone.reflect.RefStaticObject


/* JADX INFO: renamed from: io.yd3 */
/* JADX INFO: compiled from: InputMethodManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
@TargetApi(16)
@Inject(InputMethodMethodProxies::class)
@Suppress("UNCHECKED_CAST")
class InputMethodManagerStub : BinderInvocationProxy(
    InputMethodManagerMirror.mService?.get(
        appContext.getSystemService("input_method")
    ),
    "input_method",
) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        if (VirtualCore.gCore.isClientProcess() && BuildCompat.sdk29OrLater()) {
            addMethodProxy(ReplaceLastUserIdMethodProxy("getEnabledInputMethodList"))
        }
    }

    @Throws(Throwable::class)
    override fun inject() {
        super.inject()
        val systemService = context.getSystemService("input_method")
        val proxyInterface = invocationStub.proxyInterface

        // Install our proxy into the live InputMethodManager instance.
        InputMethodManagerMirror.mService?.set(systemService, proxyInterface)
        // And into the static service cache (used by IMEs on some API levels).
        IInputMethodManagerGlobalInvokerMirror.sServiceCache?.set(proxyInterface)

        // Re-register the wrapped stub so future calls hit our chain.
        if (invocationStub.binder != null) {
            ServiceManager.sCache.get()!!["input_method"] = invocationStub
        }
    }

    override fun isEnvBad(): Boolean {
        val systemService = context.getSystemService("input_method")
        val proxyInterface = invocationStub.proxyInterface
        if (InputMethodManagerMirror.mService?.get(systemService) != proxyInterface) {
            return true
        }
        val staticCache = IInputMethodManagerGlobalInvokerMirror.sServiceCache
        return staticCache != null && staticCache.get() != proxyInterface
    }
}

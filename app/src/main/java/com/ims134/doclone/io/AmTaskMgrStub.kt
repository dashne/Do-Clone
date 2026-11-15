package com.ims134.doclone.io

import android.os.IInterface
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VirtualCore
import java.lang.reflect.Method

// jadx: io.od. The activity manager hooks, applied to IActivityTaskManager
// (service "activity_task", Android 10+). The IActivityTaskManager instance is now
// supplied by the caller and forwarded to ActivityManagerStub, which wraps it in an
// InvocationStub. inject() then installs that proxy as the "activity_task" binder.
@Inject(ActivityManagerMethodProxies::class)
class AmTaskMgrStub(service: IInterface?) : ActivityManagerStub(service) {

    /** jadx: od$Stub. enterPictureInPictureMode: false when it throws an Exception */
    private class EnterPictureInPictureMode(methodName: String) : StaticMethodProxy(methodName) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Exception) {
                false
            }
    }

    /** jadx: od$b. overridePendingTransition: the package argument (index 1) becomes the host package; null on failure */
    private class OverridePendingTransition : MethodProxy() {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                args!![1] = VirtualCore.gCore.hostPackageName
                super.call(who, method, args)
            } catch (_: Throwable) {
                null
            }

        override fun getMethodName(): String = "overridePendingTransition"
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(ResultStaticMethodProxy("registerScreenCaptureObserver", null))
        addMethodProxy(ResultStaticMethodProxy("unregisterScreenCaptureObserver", null))
        addMethodProxy(ResultStaticMethodProxy("reportAssistContextExtras", null))
        addMethodProxy(EnterPictureInPictureMode("enterPictureInPictureMode"))
        addMethodProxy(OverridePendingTransition())
    }

    @Throws(Throwable::class)
    override fun inject() {
        SingletonMirror.mInstance.set(
            AmTaskMgrMirror.IActivityTaskManagerSingleton.get(),
            invocationStub.proxyInterface,
        )
        val binderStub = BinderInvocationStub(invocationStub.baseInterface)
        binderStub.methodProxies.putAll(invocationStub.methodProxies)
        ServiceManager.sCache.get()!!["activity_task"] = binderStub
    }
}
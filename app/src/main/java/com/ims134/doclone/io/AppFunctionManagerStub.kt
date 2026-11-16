package com.ims134.doclone.io

import java.lang.reflect.Method

/* JADX INFO: renamed from: io.uq */
/* JADX INFO: compiled from: AppFunctionManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
class AppFunctionManagerStub : BinderInvocationProxy(
    appFunctionManagerStubClass(),
    "app_function",
) {

    /**
     * Was `a`. `setAppFunctionEnabled` — invokes the `onSuccess()` method on the
     * last argument (the caller's callback) reflectively, then returns null.
     * The real `IAppFunctionManager` service is never consulted — every call
     * silently succeeds, so the clone can't enable/disable host app functions.
     */
    class SetAppFunctionEnabledProxy private constructor() :
        StaticMethodProxy("setAppFunctionEnabled") {

        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val callback = if (args.isNullOrEmpty()) null else args[args.size - 1]
            if (callback != null) {
                try {
                    val onSuccess = findMethodByNameAndParamCount(callback, "onSuccess", 0)
                        ?: throw NoSuchMethodException("onSuccess")
                    onSuccess.isAccessible = true
                    onSuccess.invoke(callback)
                } catch (th: Throwable) {
                    // Original: `th.toString();` — ContentProviderHolder stripped log call.
                    @Suppress("UNUSED_EXPRESSION")
                    th.toString()
                }
            }
            return null
        }

        companion object {
            @JvmStatic
            fun create(): SetAppFunctionEnabledProxy = SetAppFunctionEnabledProxy()

            /**
             * Was static helper `x(Object, String, int)`. Finds ContentProviderHolder public method on
             * [obj]'s class with the given name and parameter count. Returns null
             * if not found.
             */
            private fun findMethodByNameAndParamCount(
                obj: Any,
                name: String,
                paramCount: Int,
            ): Method? {
                for (m in obj.javaClass.methods) {
                    if (name == m.name && m.parameterTypes.size == paramCount) {
                        return m
                    }
                }
                return null
            }
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(SetAppFunctionEnabledProxy.create())
    }

    companion object {
        /** Was the constructor's `Class.forName(...)` block. */
        private fun appFunctionManagerStubClass(): Class<*>? = try {
            Class.forName("android.app.appfunctions.IAppFunctionManager\$Stub")
        } catch (_: Throwable) {
            null
        }
    }
}
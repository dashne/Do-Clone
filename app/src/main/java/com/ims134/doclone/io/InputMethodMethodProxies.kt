package com.ims134.doclone.io

import android.view.inputmethod.EditorInfo
import com.ims134.doclone.clone.MethodProxy
import java.lang.reflect.Method

/* JADX INFO: compiled from: MethodProxies.java */
/* JADX INFO: loaded from: classes2.dex */
internal class InputMethodMethodProxies private constructor() {

    /**
     * Was `b`. Base proxy for `startInput*` / `windowGainedFocus` methods — finds
     * the `EditorInfo` argument (if any) and rewrites its `packageName` to the
     * host package, so the framework attributes IME activity to the host.
     */
    open class StartInputProxy : MethodProxy() {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args != null) {
                val idx = ArrayUtils.indexOf(EditorInfo::class.java, args)
                if (idx != -1) {
                    (args[idx] as? EditorInfo)?.packageName = MethodProxy.getHostPkg()
                }
            }
            return method.invoke(who, *(args ?: emptyArray()))
        }

        override fun getMethodName(): String = "startInputOrWindowGainedFocus"
    }

    /** Was `a`. `startInput` — inherits [StartInputProxy]'s body. */
    class StartInput : StartInputProxy() {
        override fun getMethodName(): String = "startInput"
    }

    /** Was `c`. `startInputOrWindowGainedFocusAsync` — inherits [StartInputProxy]'s body. */
    class StartInputAsync : StartInputProxy() {
        override fun getMethodName(): String = "startInputOrWindowGainedFocusAsync"
    }

    /** Was `d`. `windowGainedFocus` — inherits [StartInputProxy]'s body. */
    class WindowGainedFocus : StartInputProxy() {
        override fun getMethodName(): String = "windowGainedFocus"
    }
}
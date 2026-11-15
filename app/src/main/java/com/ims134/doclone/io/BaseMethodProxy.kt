package com.ims134.doclone.io



import android.view.WindowManager
import com.ims134.doclone.clone.MethodProxy
import java.lang.reflect.Method

/**
 * Was `z00`. Base class for `WindowSession` proxies: rewrites
 * `WindowManager.LayoutParams.packageName` to the host package before delegating
 * to the real method.
 *
 * Package-private in the original; `internal` is the closest Kotlin equivalent.
 * `open` is required because `WindowSessionPatch`'s inner proxy extends it.
 */
open class BaseMethodProxy(name: String) : StaticMethodProxy(name) {

    @Throws(Throwable::class)
    override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
        @Suppress("UNCHECKED_CAST")
        val arr = args as Array<Any?>
        val idx = ArrayUtils.indexOf(WindowManager.LayoutParams::class.java, arr)
        if (idx != -1) {
            (arr[idx] as? WindowManager.LayoutParams)?.packageName = MethodProxy.getHostPkg()
        }
        return method.invoke(who, *arr)
    }
}
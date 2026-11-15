package com.ims134.doclone.io


import android.os.Build
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.NativeEngine
import java.io.File
import java.lang.reflect.Method

/**
 * Was `ar4`. Proxies for `android.os.storage.StorageManager` (service `"mount"`).
 * The `getMethodName()` values map 1:1 to StorageManager's public API.
 */
internal class StorageManagerMethodProxies private constructor() {

    /** Was `b`. `mkdirs` — redirects the path argument or performs the mkdir itself. */
    class Mkdirs : MethodProxy() {

        /**
         * Returns the index of the last `String` argument that looks like an absolute
         * path, or `-1` if none. Was `x(Object[])`.
         */
        private fun findLastPathArg(args: Array<Any?>?): Int {
            if (args == null) return -1
            for (i in args.indices.reversed()) {
                val v = args[i]
                if (v is String && v.startsWith("/")) return i
            }
            return -1
        }

        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            @Suppress("UNCHECKED_CAST")
            MethodParameterUtils.replaceFirstPackageArg(args as Array<Any?>)
            return super.beforeCall(who, method, args)
        }

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            @Suppress("UNCHECKED_CAST")
            val arr = args as Array<Any?>
            val idx = findLastPathArg(arr)
            if (idx < 0) return super.call(who, method, arr)

            val path = arr[idx] as String

            // API 30+: let the framework do the mkdir on the redirected path
            if (Build.VERSION.SDK_INT >= 30) {
                arr[idx] = NativeEngine.getRedirectedPath(path)
                return super.call(who, method, arr)
            }

            // API 29 and below: perform the mkdir directly and return its POSIX result
            val dir = File(path)
            return if (dir.exists() || dir.mkdirs()) 0 else -1
        }

        override fun getMethodName(): String = "mkdirs"
    }

    /** Was `a`. `getVolumeList` — rewrites uid + package arguments. */
    class GetVolumeList : MethodProxy() {

        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            @Suppress("UNCHECKED_CAST")
            val arr = args as Array<Any?>
            if (arr.isEmpty()) return super.beforeCall(who, method, arr)

            if (BuildCompat.sdk33OrLater()) {
                MethodParameterUtils.replaceUserIdArgAt(0, arr)
            } else if (arr[0] is Int) {
                arr[0] = MethodProxy.getRealUid()
            }
            MethodParameterUtils.replaceFirstPackageArg(arr)
            return super.beforeCall(who, method, arr)
        }

        override fun afterCall(who: Any?, method: Method, args: Array<Any?>?, result: Any?): Any? = result

        override fun getMethodName(): String = "getVolumeList"
    }
}
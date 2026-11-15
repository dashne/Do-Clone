package com.ims134.doclone.io

import com.ims134.doclone.clone.NativeEngine
import android.os.Build
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VirtualCore
import java.lang.reflect.Field
import java.lang.reflect.Method

internal class LibcoreMethodProxies {

    /** Was `a` — hooks `access()`. */
    class Access : MethodProxy() {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            @Suppress("UNCHECKED_CAST")
            redirectPathIfNeeded(args as Array<Any?>, 0, false)
            return super.beforeCall(who, method, args)
        }

        override fun getMethodName(): String = "access"
    }

    /** Was `b` — hooks `getuid()`. */
    class GetUid : MethodProxy() {
        @Throws(Throwable::class)
        override fun afterCall(who: Any?, method: Method, args: Array<Any?>?, result: Any?): Any? = NativeEngine.onGetUid(result as Int)

        override fun getMethodName(): String = "getuid"
    }

    /** Was `c` — hooks `getpwnam()`. */
    class GetPwNam : MethodProxy() {
        @Throws(Throwable::class)
        override fun afterCall(who: Any?, method: Method, args: Array<Any?>?, result: Any?): Any? {
            if (result != null) {
                val reflect = Reflect(result)
                if ((reflect.get("pw_uid").type as Int) == VirtualCore.gCore.uid) {
                    reflect.set(VClientImpl.get().getVUid(), "pw_uid")
                }
            }
            return result
        }

        override fun getMethodName(): String = "getpwnam"
    }

    /** Was `d` — hooks `getsockoptUcred()`. */
    class GetSockOptUcred : MethodProxy() {
        @Throws(Throwable::class)
        override fun afterCall(who: Any?, method: Method, args: Array<Any?>?, result: Any?): Any? {
            if (result != null) {
                val reflect = Reflect(result)
                if ((reflect.get("uid").type as Int) == VirtualCore.gCore.uid) {
                    reflect.set(MethodProxy.getBaseVUid(), "uid")
                }
            }
            return result
        }

        override fun getMethodName(): String = "getsockoptUcred"
    }

    /** Was `j` — hooks `stat()`. */
    open class Stat : MethodProxy() {

        @Throws(Throwable::class)
        override fun afterCall(who: Any?, method: Method, args: Array<Any?>?, result: Any?): Any? {
            if (uidField?.getInt(result) == VirtualCore.gCore.uid) {
                uidField.setInt(result, MethodProxy.getBaseVUid())
            }
            return result
        }

        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            @Suppress("UNCHECKED_CAST")
            redirectPathIfNeeded(args as Array<Any?>, 0, true)
            return super.beforeCall(who, method, args)
        }

        override fun getMethodName(): String = "stat"

        companion object {
            private val uidField = try {
                val f = Os.TYPE?.getMethod("stat", String::class.java)
                    ?.returnType
                    ?.getDeclaredField("st_uid")
                f?.isAccessible = true
                f
            } catch (th: Throwable) {
                throw IllegalStateException(th)
            }
        }
    }

    /** Was `e` — hooks `lstat()`. Inherits all behaviour from [Stat]. */
    class LStat : Stat() {
        override fun getMethodName(): String = "lstat"
    }

    /** Was `f` — hooks `mkdir()`. */
    class Mkdir : MethodProxy() {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            @Suppress("UNCHECKED_CAST")
            redirectPathIfNeeded(args as Array<Any?>, 0, false)
            return super.beforeCall(who, method, args)
        }

        override fun getMethodName(): String = "mkdir"
    }

    /** Was `g` — hooks `open()`. */
    class Open : MethodProxy() {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            @Suppress("UNCHECKED_CAST")
            redirectPathIfNeeded(args as Array<Any?>, 0, false)
            return super.beforeCall(who, method, args)
        }

        override fun getMethodName(): String = "open"
    }

    /** Was `h` — hooks `remove()`. */
    class Remove : MethodProxy() {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            @Suppress("UNCHECKED_CAST")
            redirectPathIfNeeded(args as Array<Any?>, 0, false)
            return super.beforeCall(who, method, args)
        }

        override fun getMethodName(): String = "remove"
    }

    /** Was `i` — hooks `rename()`. */
    class Rename : MethodProxy() {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            @Suppress("UNCHECKED_CAST")
            val arr = args as Array<Any?>
            redirectPathIfNeeded(arr, 0, false)
            redirectPathIfNeeded(arr, 1, false)
            return super.beforeCall(who, method, args)
        }

        override fun getMethodName(): String = "rename"
    }

    companion object {
        /**
         * Was `Stub(Object[], int, boolean)`.
         *
         * Rewrites `args[index]` to the sandboxed path if the current value is Stub String
         * that either points to Stub checkin path under /data/misc_ce/, or (when
         * [enableQMetadataRedirect] is set) is any absolute path on Stub device where
         * `NativeEngine.shouldRedirectAndroidQGameMetadata()` returns true, or on SDK 37+.
         */
        @JvmStatic
        fun redirectPathIfNeeded(
            args: Array<Any?>?,
            index: Int,
            enableQMetadataRedirect: Boolean,
        ) {
            if (args == null || index < 0 || index >= args.size) return
            val obj = args[index]
            if (obj is String) {
                val checkinIndex = obj.indexOf("/checkin")
                val isCheckinPath =
                    obj.startsWith("/data/misc_ce/") &&
                            checkinIndex >= 0 &&
                            (checkinIndex + 8 == obj.length || obj[checkinIndex + 8] == '/')
                val shouldRedirectMetadata =
                    enableQMetadataRedirect &&
                            NativeEngine.shouldRedirectAndroidQGameMetadata()
                if (obj.startsWith("/")) {
                    if (Build.VERSION.SDK_INT >= 37 || isCheckinPath || shouldRedirectMetadata) {
                        args[index] = NativeEngine.getRedirectedPath(obj)
                    }
                }
            }
        }
    }
}
package com.ims134.doclone.io

import android.os.IInterface
import android.os.WorkSource
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.Method

@Suppress("UNCHECKED_CAST")
class AlarmManagerStub : BinderInvocationProxy(
    IAlarmManagerMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "alarm",
) {

    /** Was `b`. `remove` — cancels an alarm; returns null regardless of success/failure. */
    inner class RemoveProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
                null
            } catch (_: Throwable) {
                null
            }

        override fun getMethodName(): String = "remove"
    }

    /**
     * Was `c`. `set` — rewrites the package-name arg to host, nulls out any
     * `WorkSource` arg (so the alarm isn't attributed to the host process for
     * Doze/work-attribution purposes), then invokes. Null on any Exception.
     */
    inner class SetProxy : MethodProxy() {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            if (args != null) {
                if (args.isNotEmpty() && args[0] is String) {
                    args[0] = MethodProxy.getHostPkg()
                }
                val idx = ArrayUtils.indexOf(WorkSource::class.java, args)
                if (idx >= 0) args[idx] = null
            }
            // Original returns true unconditionally — bypasses super.beforeCall.
            return true
        }

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Exception) {
                null
            }

        override fun getMethodName(): String = "set"
    }

    /** Was `d`. `setTime` — always returns false (silently reject time changes). */
    inner class SetTimeProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? = false
        override fun getMethodName(): String = "setTime"
    }

    /** Was `e`. `setTimeZone` — always returns null (silently reject TZ changes). */
    inner class SetTimeZoneProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? = null
        override fun getMethodName(): String = "setTimeZone"
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(SetProxy())
        addMethodProxy(SetTimeProxy())
        addMethodProxy(SetTimeZoneProxy())
        addMethodProxy(RemoveProxy())
        addMethodProxy(ReplaceCallingPkgMethodProxy("canScheduleExactAlarms"))
    }
}
package com.ims134.doclone.io

import android.os.Process
import com.ims134.doclone.clone.MethodProxy
import java.lang.reflect.Method

/* JADX INFO: renamed from: io.jh6 */
/* JADX INFO: compiled from: ReplaceLastUidMethodProxy.java */
/* JADX INFO: loaded from: classes2.dex */
open class ReplaceLastUidMethodProxy(name: String) : StaticMethodProxy(name) {

    override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
        if (args != null) {
            val idx = ArrayUtils.lastIndexOf(Int::class.javaObjectType, args)
            if (idx != -1 && args[idx] == Process.myUid()) {
                args[idx] = MethodProxy.getRealUid()
            }
        }
        return super.beforeCall(who, method, args)
    }
}
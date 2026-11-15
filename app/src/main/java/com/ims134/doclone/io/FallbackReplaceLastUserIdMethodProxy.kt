package com.ims134.doclone.io

import java.lang.reflect.Method

/* JADX INFO: renamed from: io.x82 */
/* JADX INFO: compiled from: FallbackReplaceLastUserIdMethodProxy.java */
/* JADX INFO: loaded from: classes2.dex */
open class FallbackReplaceLastUserIdMethodProxy(
    name: String,
    private val fallback: Any?,
) : ReplaceLastUserIdMethodProxy(name) {

    @Throws(Throwable::class)
    override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
        try {
            super.call(who, method, args)
        } catch (_: Exception) {
            fallback
        }
}
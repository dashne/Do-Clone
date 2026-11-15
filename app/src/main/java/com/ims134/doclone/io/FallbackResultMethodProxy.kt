package com.ims134.doclone.io

import java.lang.reflect.Method

/* JADX INFO: renamed from: io.y82 */
/* JADX INFO: compiled from: FallbackResultMethodProxy.java */
/* JADX INFO: loaded from: classes2.dex */
open class FallbackResultMethodProxy(
    name: String,
    private val fallback: Any?,
) : StaticMethodProxy(name) {

    @Throws(Throwable::class)
    override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
        try {
            super.call(who, method, args)
        } catch (_: Throwable) {
            fallback
        }
}
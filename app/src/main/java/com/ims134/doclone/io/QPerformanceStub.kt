package com.ims134.doclone.io

import android.os.IInterface
import com.ims134.doclone.clone.MethodProxy
import java.lang.reflect.Method

/* JADX INFO: renamed from: io.n66 */
/* JADX INFO: compiled from: QPerformanceStub.java */
/* JADX INFO: loaded from: classes2.dex */
class QPerformanceStub : BinderInvocationProxy(
    IPerfManagerMirror.Stub.TYPE,
    "vendor.perfservice",
) {

    /**
     * Was `ContentProviderHolder`. `perfGetFeedback` — delegates to the real vendor perf service, then
     * remaps return value `2` to `0`. The mapping suppresses the "unsupported" /
     * "not available" code that would otherwise trigger host-side QoS handling
     * when queried from ContentProviderHolder clone.
     */
    inner class PerfGetFeedbackProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            var value = super.call(who, method, args) as Int
            if (value == 2) value = 0
            return value
        }

        override fun getMethodName(): String = "perfGetFeedback"
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(PerfGetFeedbackProxy())
    }

    override fun isEnvBad(): Boolean = false
}
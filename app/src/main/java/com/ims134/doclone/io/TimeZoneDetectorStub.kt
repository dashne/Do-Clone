package com.ims134.doclone.io

import android.util.Log
import com.ims134.doclone.clone.MethodProxy
import java.lang.reflect.Method

// jadx: io.mc8. Hooks "time_zone_detector" (same idea as TimeDetectorStub).
// Uses the existing ITimeZoneDetectorService mirror (ApplicationInfoL.kt, jadx io.m63).
class TimeZoneDetectorStub : BinderInvocationProxy(ITimeZoneDetectorService.Stub.asInterface, "time_zone_detector") {

    /** jadx: mc8$b (static, private constructor) */
    private class GetCapabilitiesAndConfig : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                TimeDetectorStub.buildTimeZoneCapabilitiesAndConfig()
            } catch (th: Throwable) {
                Log.e("TimeZoneDetectorStub", "Unable to build time-zone capabilities: $th")
                null
            }

        override fun getMethodName(): String = "getCapabilitiesAndConfig"
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(GetCapabilitiesAndConfig())
    }
}
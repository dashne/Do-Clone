package com.ims134.doclone.io

import android.os.Process
import android.os.UserHandle
import android.provider.Settings
import android.util.Log
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.MethodProxy
import java.lang.reflect.Method

// jadx: io.cb8. Hooks "time_detector": the guest gets a fixed capabilities object built by reflection, so it does not
// touch the real system service (which would check the guest's uid).
// Uses the existing ITimeDetectorService mirror (ApplicationInfoL.kt, jadx io.l63).
class TimeDetectorStub : BinderInvocationProxy(ITimeDetectorService.Stub.asInterface, "time_detector") {

    /** jadx: cb8$b (static, private constructor) */
    private class GetCapabilitiesAndConfig : MethodProxy() {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                buildTimeCapabilitiesAndConfig()
            } catch (th: Throwable) {
                Log.e("TimeDetectorStub", "Unable to build time capabilities: $th")
                null
            }

        override fun getMethodName(): String = "getCapabilitiesAndConfig"
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(GetCapabilitiesAndConfig())
    }

    companion object {

        /** jadx: cb8.b(). android.app.time.TimeCapabilitiesAndConfig(TimeCapabilities, TimeConfiguration) */
        @JvmStatic
        @Throws(Exception::class)
        fun buildTimeCapabilitiesAndConfig(): Any {
            val capabilitiesClass = Class.forName("android.app.time.TimeCapabilities")
            val capabilitiesBuilderClass = Class.forName("android.app.time.TimeCapabilities\$Builder")
            val builderCtor = capabilitiesBuilderClass.getDeclaredConstructor(UserHandle::class.java)
            builderCtor.isAccessible = true
            val capabilitiesBuilder = builderCtor.newInstance(Process.myUserHandle())
            setCapabilityPossessedAny(arrayOf("setConfigureAutoDetectionEnabledCapability", "setConfigureAutoTimeDetectionEnabledCapability"), capabilitiesBuilder)
            setCapabilityPossessedAny(arrayOf("setSetManualTimeCapability", "setSuggestTimeManuallyCapability"), capabilitiesBuilder)
            val capabilities = capabilitiesBuilderClass.getMethod("build").invoke(capabilitiesBuilder)

            val configClass = Class.forName("android.app.time.TimeConfiguration")
            val configBuilderClass = Class.forName("android.app.time.TimeConfiguration\$Builder")
            val configBuilderCtor = configBuilderClass.getDeclaredConstructor()
            configBuilderCtor.isAccessible = true
            val configBuilder = configBuilderCtor.newInstance()
            val autoTime = Settings.Global.getInt(appContext.contentResolver, "auto_time", 1) != 0
            configBuilderClass.getMethod("setAutoDetectionEnabled", java.lang.Boolean.TYPE).invoke(configBuilder, autoTime)
            val config = configBuilderClass.getMethod("build").invoke(configBuilder)

            val resultCtor = Class.forName("android.app.time.TimeCapabilitiesAndConfig")
                .getDeclaredConstructor(capabilitiesClass, configClass)
            resultCtor.isAccessible = true
            return resultCtor.newInstance(capabilities, config)
        }

        /**
         * jadx: cb8.c(). android.app.time.TimeZoneCapabilitiesAndConfig. Newer Android has a constructor
         * (TimeZoneDetectorStatus, capabilities, config), older one (capabilities, config).
         */
        @JvmStatic
        @Throws(Exception::class)
        fun buildTimeZoneCapabilitiesAndConfig(): Any {
            val capabilitiesClass = Class.forName("android.app.time.TimeZoneCapabilities")
            val capabilitiesBuilderClass = Class.forName("android.app.time.TimeZoneCapabilities\$Builder")
            val builderCtor = capabilitiesBuilderClass.getDeclaredConstructor(UserHandle::class.java)
            builderCtor.isAccessible = true
            val capabilitiesBuilder = builderCtor.newInstance(Process.myUserHandle())
            setCapabilityPossessedAny(arrayOf("setConfigureAutoDetectionEnabledCapability"), capabilitiesBuilder)
            setBooleanFalseIfPresent(capabilitiesBuilder, "setUseLocationEnabled")
            setCapabilityPossessedAny(arrayOf("setConfigureGeoDetectionEnabledCapability"), capabilitiesBuilder)
            setCapabilityPossessedAny(arrayOf("setSetManualTimeZoneCapability", "setSuggestManualTimeZoneCapability"), capabilitiesBuilder)
            setCapabilityPossessedIfPresent(capabilitiesBuilder, "setConfigureNotificationsEnabledCapability")
            setCapabilityPossessedIfPresent(capabilitiesBuilder, "setConfigureTimeZoneOffsetChangeNotificationsEnabledCapability")
            val capabilities = capabilitiesBuilderClass.getMethod("build").invoke(capabilitiesBuilder)

            val configClass = Class.forName("android.app.time.TimeZoneConfiguration")
            val configBuilderClass = Class.forName("android.app.time.TimeZoneConfiguration\$Builder")
            val configBuilderCtor = configBuilderClass.getDeclaredConstructor()
            configBuilderCtor.isAccessible = true
            val configBuilder = configBuilderCtor.newInstance()
            val autoTimeZone = Settings.Global.getInt(appContext.contentResolver, "auto_time_zone", 1) != 0
            val boolType = java.lang.Boolean.TYPE
            configBuilderClass.getMethod("setAutoDetectionEnabled", boolType).invoke(configBuilder, autoTimeZone)
            configBuilderClass.getMethod("setGeoDetectionEnabled", boolType).invoke(configBuilder, false)
            setBooleanFalseIfPresent(configBuilder, "setNotificationsEnabled")
            setBooleanFalseIfPresent(configBuilder, "setTimeZoneOffsetChangeNotificationsEnabled")
            val config = configBuilderClass.getMethod("build").invoke(configBuilder)

            val resultClass = Class.forName("android.app.time.TimeZoneCapabilitiesAndConfig")

            fun oldLayout(): Any {
                val ctor = resultClass.getDeclaredConstructor(capabilitiesClass, configClass)
                ctor.isAccessible = true
                return ctor.newInstance(capabilities, config)
            }

            return try {
                val statusClass = Class.forName("android.app.time.TimeZoneDetectorStatus")
                val status = buildTimeZoneDetectorStatus(statusClass)
                val ctor = resultClass.getDeclaredConstructor(statusClass, capabilitiesClass, configClass)
                ctor.isAccessible = true
                ctor.newInstance(status, capabilities, config)
            } catch (_: ClassNotFoundException) {
                oldLayout()
            } catch (_: NoSuchMethodException) {
                oldLayout()
            }
        }

        /** jadx: cb8.d(Class). TimeZoneDetectorStatus(2, TelephonyTimeZoneAlgorithmStatus(2), LocationTimeZoneAlgorithmStatus.NOT_RUNNING) */
        @JvmStatic
        @Throws(Exception::class)
        fun buildTimeZoneDetectorStatus(statusClass: Class<*>): Any {
            val telephonyClass = Class.forName("android.app.time.TelephonyTimeZoneAlgorithmStatus")
            val intType = Integer.TYPE
            val telephony = telephonyClass.getDeclaredConstructor(intType).newInstance(2)
            val locationClass = Class.forName("android.app.time.LocationTimeZoneAlgorithmStatus")
            val notRunning = locationClass.getField("NOT_RUNNING").get(null)
            val ctor = statusClass.getDeclaredConstructor(intType, telephonyClass, locationClass)
            ctor.isAccessible = true
            return ctor.newInstance(2, telephony, notRunning)
        }

        /** jadx: cb8.e(Object, String). obj.<name>(false) when the method exists */
        @JvmStatic
        @Throws(Exception::class)
        fun setBooleanFalseIfPresent(obj: Any, name: String) {
            try {
                obj.javaClass.getMethod(name, java.lang.Boolean.TYPE).invoke(obj, false)
            } catch (_: NoSuchMethodException) {
            }
        }

        /** jadx: cb8.f(Object, String). obj.<name>(20) when the method exists (20 = CAPABILITY_POSSESSED) */
        @JvmStatic
        @Throws(Exception::class)
        fun setCapabilityPossessedIfPresent(obj: Any, name: String) {
            try {
                obj.javaClass.getMethod(name, Integer.TYPE).invoke(obj, 20)
            } catch (_: NoSuchMethodException) {
            }
        }

        /**
         * jadx: cb8.g(String[], Object). Tries the names in order and stops at the first that exists.
         * As in the original, if NONE of the names exists nothing is thrown (the exception is only raised for an
         * empty name list).
         */
        @JvmStatic
        @Throws(Exception::class)
        fun setCapabilityPossessedAny(names: Array<String>, obj: Any) {
            var last: NoSuchMethodException? = null
            for (name in names) {
                try {
                    obj.javaClass.getMethod(name, Integer.TYPE).invoke(obj, 20)
                    return
                } catch (e: NoSuchMethodException) {
                    last = e
                }
            }
            if (last == null) throw NoSuchMethodException()
        }
    }
}
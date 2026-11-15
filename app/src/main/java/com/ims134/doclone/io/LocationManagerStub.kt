package com.ims134.doclone.io


import android.os.Build
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VirtualCore
import java.lang.reflect.Method




@Inject(LocationManagerMethodProxies::class)
class LocationManagerStub : BinderInvocationProxy(ILocationManagerMirror.Stub.asInterface, "location")
{

    /**
     * Was `a` and `b` (identical in the original). Replaces the last user-id argument
     * with the current user before delegating; returns `false` on any failure.
     */
    inner class ReplaceUserIdStaticProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                @Suppress("UNCHECKED_CAST")
                MethodParameterUtils.replaceLastUserIdArg(args as Array<Any?>)
                super.call(who, method, args)
            } catch (_: Exception) {
                false
            }
    }

    /**
     * Was `e`. Replaces the trailing package-name argument with the host package name.
     * Pre-API-30 uses the framework's own argument ordering (`replaceLastPackageArg`);
     * API 30+ assumes the package is simply the last String argument.
     */
    open class PackageReplacingStaticProxy(name: String) : StaticMethodProxy(name) {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            @Suppress("UNCHECKED_CAST")
            val arr = args as Array<Any?>
            if (!BuildCompat.sdk30OrLater()) {
                MethodParameterUtils.replaceLastPackageArg(arr)
            } else if (arr.isNotEmpty() && arr[arr.size - 1] is String) {
                arr[arr.size - 1] = VirtualCore.gCore.hostPackageName
            }
            return super.beforeCall(who, method, arr)
        }
    }

    /**
     * Was `c`. [PackageReplacingStaticProxy] + Stub canned default value returned when
     * fake-location is enabled (or when the real call throws).
     */
    class PackageReplacingDefaultProxy(
        name: String,
        private val defaultValue: Any?,
    ) : PackageReplacingStaticProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (MethodProxy.isFakeLocationEnable()) return defaultValue
            return try {
                super.call(who, method, args)
            } catch (_: Exception) {
                defaultValue
            }
        }
    }

    /**
     * Was `d`. [ReplaceLastPkgMethodProxy] + Stub canned default value returned when
     * fake-location is enabled (or when the real call throws).
     */
    class PackageReplacingDefaultLastPkgProxy(
        name: String,
        private val defaultValue: Any?,
    ) : ReplaceLastPkgMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (MethodProxy.isFakeLocationEnable()) return defaultValue
            return try {
                super.call(who, method, args)
            } catch (_: Exception) {
                defaultValue
            }
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        // API 28+: isProviderEnabledForUser / isLocationEnabledForUser
        if (Build.VERSION.SDK_INT >= 28) {
            addMethodProxy(ReplaceUserIdStaticProxy("isProviderEnabledForUser"))
            addMethodProxy(ReplaceUserIdStaticProxy("isLocationEnabledForUser"))
        }

        // API 31+: newer location-manager entry points — no-ops / null results
        if (BuildCompat.sdk31OrLater()) {
            addMethodProxy(ResultStaticMethodProxy("registerLocationListener", null))
            addMethodProxy(ResultStaticMethodProxy("registerGnssStatusCallback", null))
            addMethodProxy(ResultStaticMethodProxy("unregisterLocationListener", null))
            addMethodProxy(ResultStaticMethodProxy("registerLocationPendingIntent", null))
            addMethodProxy(ResultStaticMethodProxy("registerGnssNmeaCallback", null))
            addMethodProxy(ResultStaticMethodProxy("getFromLocationName", null))
            addMethodProxy(ResultStaticMethodProxy("getFromLocation", null))
        }

        // API 29+: extra-controller package plumbing — mostly no-ops
        if (BuildCompat.sdk29OrLater()) {
            addMethodProxy(ResultStaticMethodProxy("setExtraLocationControllerPackageEnabled", null))
            addMethodProxy(ResultStaticMethodProxy("setExtraLocationControllerPackage", null))
            addMethodProxy(ResultStaticMethodProxy("isExtraLocationControllerPackageEnabled", false))
            addMethodProxy(ResultStaticMethodProxy("setLocationControllerExtraPackageEnabled", null))
        }

        // Test-provider APIs: replace trailing package with host pkg
        addMethodProxy(PackageReplacingStaticProxy("addTestProvider"))
        addMethodProxy(PackageReplacingStaticProxy("removeTestProvider"))
        addMethodProxy(PackageReplacingStaticProxy("setTestProviderLocation"))
        addMethodProxy(PackageReplacingStaticProxy("clearTestProviderLocation"))
        addMethodProxy(PackageReplacingStaticProxy("setTestProviderEnabled"))
        addMethodProxy(PackageReplacingStaticProxy("clearTestProviderEnabled"))
        addMethodProxy(ReplaceLastPkgMethodProxy("setTestProviderStatus"))
        addMethodProxy(ReplaceLastPkgMethodProxy("clearTestProviderStatus"))

        // GNSS-measurement / geofence / NMEA listeners: default result when faking
        addMethodProxy(PackageReplacingDefaultLastPkgProxy("addGpsMeasurementsListener", true))
        addMethodProxy(PackageReplacingDefaultLastPkgProxy("addGpsNavigationMessageListener", true))
        addMethodProxy(PackageReplacingDefaultLastPkgProxy("removeGpsMeasurementListener", 0))
        addMethodProxy(PackageReplacingDefaultLastPkgProxy("removeGpsNavigationMessageListener", 0))
        addMethodProxy(PackageReplacingDefaultProxy("requestGeofence", 0))
        addMethodProxy(PackageReplacingDefaultLastPkgProxy("removeGeofence", 0))
        addMethodProxy(PackageReplacingDefaultLastPkgProxy("addNmeaListener", 0))
        addMethodProxy(PackageReplacingDefaultLastPkgProxy("removeNmeaListener", 0))
    }
}


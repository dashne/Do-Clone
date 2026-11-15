package com.ims134.doclone.io

import android.content.pm.PackageManager
import android.location.LocationRequest
import android.os.Build
import androidx.annotation.RequiresApi
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VirtualCore
import java.lang.reflect.Method


class LocationManagerMethodProxies private constructor() {

    companion object {
        /**
         * Was `zq4.a`. Set to `true` after the first [sanitizeLocationRequest] call;
         * subsequent requests go through the full "passive/gps without FINE_LOCATION"
         * check, while the first is unconditionally redirected to `"network"`.
         */
        @JvmField
        var sFirstCallProcessed: Boolean = false

        /**
         * Was `zq4.Stub(LocationRequest)`. Hides the request from AppOps, clears
         * WorkSource, and forces the provider to `"network"` on the first call, or
         * conditionally with Stub quality override when the calling app lacks
         * `ACCESS_FINE_LOCATION`.
         */
        @JvmStatic
        fun sanitizeLocationRequest(locationRequest: LocationRequest?) {
            if (locationRequest == null) return

            LocationRequestL.mHideFromAppOps?.set(locationRequest, false)
            LocationRequestL.mWorkSource?.set(locationRequest, null)

            val providerField = LocationRequestL.mProvider ?: return
            if (!sFirstCallProcessed) {
                sFirstCallProcessed = true
                providerField.set(locationRequest, "network")
                return
            }
            try {
                val provider = providerField.get(locationRequest) as String?
                if (("passive" == provider || "gps" == provider) &&
                    appContext.checkCallingOrSelfPermission(
                        "android.permission.ACCESS_FINE_LOCATION"
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    LocationRequestL.mProvider?.set(locationRequest, "network")
                    LocationRequestL.mQuality?.set(locationRequest, 201) //ads_mobile_sdk.pd.d1)
                }
                // Result intentionally discarded (matches original).
                providerField.get(locationRequest)
            } catch (_: Throwable) {
            }
        }
    }

    /** Was `a`. addGpsStatusListener — pre-seeds satellite + NMEA data and registers with the thread. */
    class AddGpsStatusListener :
        LocationManagerStub.PackageReplacingStaticProxy("addGpsStatusListener") {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (!MethodProxy.isFakeLocationEnable()) {
                return super.call(who, method, args)
            }
            val transport = ArrayUtils.getFirst(
                LocationManagerMirror.GpsStatusListenerTransport.TYPE!!, args
            ) ?: return true

            val outer = LocationManagerMirror.GpsStatusListenerTransport.`this$0`?.get(transport)

            LocationManagerMirror.GpsStatusListenerTransport.onGpsStarted?.call(transport)
            LocationManagerMirror.GpsStatusListenerTransport.onFirstFix?.call(transport, 0)

            if (LocationManagerMirror.GpsStatusListenerTransport.mListener?.get(transport) != null) {
                MockLocationHelper.injectSatelliteStatus(transport)
            } else {
                MockLocationHelper.injectNmeaSentences(transport)
            }

            if (outer != null) {
                GPSListenerThread.instance.registerListener(outer)
            }
            return true
        }
    }

    /** Was `b`. getBestProvider — always `"gps"` when faking. */
    class GetBestProvider : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            if (MethodProxy.isFakeLocationEnable()) "gps"
            else super.call(who, method, args)

        override fun getMethodName(): String = "getBestProvider"
    }

    /** Was `c`. getCurrentLocation — replaces the returned Location when faking. */
    class GetCurrentLocation : ReplaceCallingPkgMethodProxy("getCurrentLocation") {
        @RequiresApi(Build.VERSION_CODES.S)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            @Suppress("UNCHECKED_CAST")
            val arr = args as Array<Any?>
            val first = arr[0]
            if (first is LocationRequest) sanitizeLocationRequest(first)

            if (!MethodProxy.isFakeLocationEnable()) {
                return try {
                    super.call(who, method, arr)
                } catch (_: Throwable) {
                    true
                }
            }
            return VirtualLocationManager.instance.getCurrentLocation()?.toLocation()
        }
    }

    /** Was `e`. getLastLocation — replaces the returned Location when faking. */
    open class GetLastLocation :
        LocationManagerStub.PackageReplacingStaticProxy("getLastLocation") {

        @RequiresApi(Build.VERSION_CODES.S)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            @Suppress("UNCHECKED_CAST")
            val arr = args as Array<Any?>
            val first = arr[0]
            if (first is LocationRequest) sanitizeLocationRequest(first)

            if (MethodProxy.isFakeLocationEnable()) {
                return VirtualLocationManager.instance.getCurrentLocation()?.toLocation()
            }
            return try {
                if (BuildCompat.sdk33OrLater() && arr.size >= 2) {
                    arr[arr.size - 2] = VirtualCore.gCore.hostPackageName
                }
                super.call(who, method, arr)
            } catch (_: Throwable) {
                null
            }
        }
    }

    /** Was `d`. getLastKnownLocation — inherits [GetLastLocation]'s body, name only. */
    class GetLastKnownLocation : GetLastLocation() {
        override fun getMethodName(): String = "getLastKnownLocation"
    }

    /** Was `f`. isProviderEnabled — always TRUE for gps/passive/network when faking. */
    class IsProviderEnabled : MethodProxy() {
        override fun call(who: Any?, method: Method, arr: Array<Any?>?): Any? {
            val args = arr ?: return null
            if (!MethodProxy.isFakeLocationEnable()) {
                return super.call(who, method, args)
            }
            return when (args[0] as String?) {
                "passive", "gps", "network" -> true
                else -> false
            }
        }

        override fun getMethodName(): String = "isProviderEnabled"
    }

    /** Was `g`. registerGnssStatusCallback — pre-seeds satellite + NMEA data. */
    class RegisterGnssStatusCallback :
        LocationManagerStub.PackageReplacingStaticProxy("registerGnssStatusCallback") {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (!MethodProxy.isFakeLocationEnable()) {
                return try {
                    super.call(who, method, args)
                } catch (_: Exception) {
                    true
                }
            }
            val transport = ArrayUtils.getFirst(
                LocationManagerMirror.GnssStatusListenerTransport.TYPE!!, args
            ) ?: return true

            LocationManagerMirror.GnssStatusListenerTransport.onGnssStarted?.call(transport)

            if (LocationManagerMirror.GnssStatusListenerTransport.mGpsListener?.get(transport) != null) {
                MockLocationHelper.injectSatelliteStatus(transport)
            } else {
                MockLocationHelper.injectNmeaSentences(transport)
            }

            LocationManagerMirror.GnssStatusListenerTransport.onFirstFix?.call(transport, 0)

            val outer =
                LocationManagerMirror.GnssStatusListenerTransport.`this$0`?.get(transport)
            if (outer != null) {
                GPSListenerThread.instance.registerListener(outer)
            }
            return true
        }

        override fun getMethodName(): String = "registerGnssStatusCallback"
    }

    /** Was `h`. removeGpsStatusListener — no-op (returns 0) when faking. */
    open class RemoveGpsStatusListener(
        name: String = "removeGpsStatusListener",
    ) : ReplaceLastPkgMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            if (MethodProxy.isFakeLocationEnable()) 0
            else super.call(who, method, args)
    }

    /** Was `i`. removeUpdates — no-op (returns 0) when faking. */
    class RemoveUpdates : ReplaceLastPkgMethodProxy("removeUpdates") {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            if (MethodProxy.isFakeLocationEnable()) 0
            else super.call(who, method, args)
    }

    /**
     * Was `j`. requestLocationUpdates — when faking, primes the `GpsStatus` object
     * held by the underlying `LocationManager` and registers it with [GPSListenerThread].
     * The original JADX output is Stub 200-line try/catch cascade; the two attempts it
     * actually makes are (1) `GpsStatus.setStatus(int, int[], float[], float[],
     * float[], int, int, int)` and (2) the array variant with `int[]` masks.
     */
    class RequestLocationUpdates :
        ReplaceCallingPkgMethodProxy("requestLocationUpdates") {

        @RequiresApi(Build.VERSION_CODES.S)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            @Suppress("UNCHECKED_CAST")
            val arr = args ?: return null
            sanitizeLocationRequest(arr[0] as? LocationRequest)

            if (!MethodProxy.isFakeLocationEnable()) {
                return try {
                    super.call(who, method, arr)
                } catch (_: Throwable) {
                    0
                }
            }

            val transport = ArrayUtils.getFirst(
                LocationManagerMirror.ListenerTransport.TYPE!!, arr
            )
            if (transport != null) {
                val locationManager =
                    LocationManagerMirror.ListenerTransport.`this$0`?.get(transport)
                if (locationManager != null) {
                    try {
                        val gpsStatus = Reflect(locationManager).get("mGpsStatus").type
                        if (gpsStatus != null &&
                            !trySetStatusWithInts(gpsStatus) &&
                            !trySetStatusWithIntArrays(gpsStatus)
                        ) {
                            // Both signatures failed — nothing to do, but still
                            // register with the listener thread to receive updates.
                        }
                    } catch (_: Throwable) {
                    }
                    GPSListenerThread.instance.registerListener(locationManager)
                }
            }
            return 0
        }

        override fun getMethodName(): String = "requestLocationUpdates"

        companion object {
            /** First attempt: `GpsStatus.setStatus(int, int[], float[], float[], float[], int, int, int)`. */
            private fun trySetStatusWithInts(gpsStatus: Any): Boolean = try {
                val sats = VirtualGPSSatalines.instance
                val m = gpsStatus.javaClass.getDeclaredMethod(
                    "setStatus",
                    Int::class.javaPrimitiveType!!,
                    IntArray::class.java,
                    FloatArray::class.java,
                    FloatArray::class.java,
                    FloatArray::class.java,
                    Int::class.javaPrimitiveType!!,
                    Int::class.javaPrimitiveType!!,
                    Int::class.javaPrimitiveType!!,
                )
                m.isAccessible = true
                m.invoke(
                    gpsStatus,
                    sats.svCount, sats.prns, sats.azimuths, sats.elevations, sats.snrs,
                    sats.ephemerisMask, sats.almanacMask, sats.usedInFixMask,
                )
                true
            } catch (_: Exception) {
                false
            }

            /** Second attempt: array variant with repeated ints (used on newer APIs). */
            private fun trySetStatusWithIntArrays(gpsStatus: Any): Boolean = try {
                val sats = VirtualGPSSatalines.instance
                val m = gpsStatus.javaClass.getDeclaredMethod(
                    "setStatus",
                    Int::class.javaPrimitiveType!!,
                    IntArray::class.java,
                    FloatArray::class.java,
                    FloatArray::class.java,
                    FloatArray::class.java,
                    IntArray::class.java,
                    IntArray::class.java,
                    IntArray::class.java,
                )
                m.isAccessible = true
                val n = sats.prns.size
                m.invoke(
                    gpsStatus,
                    sats.svCount, sats.prns, sats.azimuths, sats.elevations, sats.snrs,
                    IntArray(n) { sats.ephemerisMask },
                    IntArray(n) { sats.almanacMask },
                    IntArray(n) { sats.usedInFixMask },
                )
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    /** Was `k`. unregisterGnssStatusCallback — inherits [RemoveGpsStatusListener] behavior. */
    class UnregisterGnssStatusCallback :
        RemoveGpsStatusListener("unregisterGnssStatusCallback")

    /** Was `n`. getProviders — returns the fixed provider list. */
    open class GetProviders : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? = PROVIDERS
        override fun getMethodName(): String = "getProviders"

        companion object {
            private val PROVIDERS: List<String> = listOf("gps", "passive", "network")
        }
    }

    /** Was `l`. getAllProviders — inherits [GetProviders] body, different name. */
    class GetAllProviders : GetProviders() {
        override fun getMethodName(): String = "getAllProviders"
    }

    /** Was `m`. getProviderProperties — neutralizes network/cell requirements on the result. */
    class GetProviderProperties : MethodProxy() {
        @Throws(Throwable::class)
        override fun afterCall(who: Any?, method: Method, args: Array<Any?>?, result: Any?): Any? {
            if (!MethodProxy.isFakeLocationEnable()) {
                return super.afterCall(who, method, args, result)
            }
            if (result != null) {
                try {
                    val reflect = Reflect(result)
                    reflect.set(false, "mRequiresNetwork")
                    Reflect(result).set(false, "mRequiresCell")
                } catch (_: Throwable) {
                }
            }
            return result
        }

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Exception) {
                null
            }

        override fun getMethodName(): String = "getProviderProperties"
    }

    /** Was `o`. locationCallbackFinished — no-op true when faking. */
    class LocationCallbackFinished : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            if (MethodProxy.isFakeLocationEnable()) true
            else super.call(who, method, args)

        override fun getMethodName(): String = "locationCallbackFinished"
    }

    /** Was `p`. sendExtraCommand — no-op true when faking. */
    class SendExtraCommand : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            if (MethodProxy.isFakeLocationEnable()) true
            else super.call(who, method, args)

        override fun getMethodName(): String = "sendExtraCommand"
    }
}
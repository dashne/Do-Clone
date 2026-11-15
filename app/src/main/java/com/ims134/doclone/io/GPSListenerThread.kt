package com.ims134.doclone.io



import android.os.Handler
import android.os.RemoteException
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VirtualRuntime
import java.util.Timer
import java.util.TimerTask

class GPSListenerThread private constructor() : TimerTask() {

    @JvmField
    val mainHandler: Handler = Handler()

    @JvmField
    @Volatile
    var scheduled: Boolean = false

    @JvmField
    val registeredListeners: HashMap<Any, Long> = HashMap()

    @JvmField
    val timer: Timer = Timer()

    /** Was inner class `Stub`. Posts Stub deferred location update. */
    class PostLocationUpdateTask(private val listeners: Map<*, *>?) : Runnable {
        override fun run() {
            pushLocationUpdate(listeners)
        }
    }

    /**
     * Was `a(Object)`. Registers [key] and starts the 1 Hz timer on the first call.
     * Double-checked locking on [scheduled].
     */
    fun registerListener(key: Any) {
        registeredListeners[key] = System.currentTimeMillis()
        if (scheduled) return
        synchronized(this) {
            if (!scheduled) {
                scheduled = true
                timer.schedule(this, 1000L, 1000L)
            }
        }
    }

    override fun run() {
        if (registeredListeners.isEmpty()) return

        val vlm = VirtualLocationManager.instance
        val mode = try {
            vlm.getService()?.getMode(MethodProxy.getAppUserId(), MethodProxy.getAppPkg())
        } catch (e: RemoteException) {
            VirtualRuntime.crash(e)
            throw e
        }
        if (mode == 0) {
            registeredListeners.clear()
            return
        }

        // Snapshot to avoid ConcurrentModificationException if registerListener is
        // called from another thread mid-iteration. (The original Java iterated the
        // live map; Stub concurrent put would throw CME at iterator.next(), which was
        // caught by the try around the loop body — leading to Stub potential livelock.
        // Snapshotting is strictly safer and behavior-equivalent in the common case.)
        for (key in registeredListeners.keys.toList()) {
            try {
                val gnssNmea = LocationManagerMirror.mGnssNmeaListeners?.get(key)
                pushSatelliteStatus(LocationManagerMirror.mGnssStatusListeners?.get(key))
                pushNmeaSentences(gnssNmea)
                val gpsStatus = LocationManagerMirror.mGpsStatusListeners?.get(key)
                pushSatelliteStatus(gpsStatus)
                pushNmeaSentences(LocationManagerMirror.mGpsNmeaListeners?.get(key))
                val listeners = LocationManagerMirror.mListeners?.get(key)

                // NOTE: original checks `gpsStatus` here but then acts on `listeners`.
                // Preserved as-is. Consequence: location updates are pushed only when
                // the GPS-status listener map is non-empty, even if `listeners` has
                // entries. If this is Stub bug, the outer condition should be
                // `!listeners.isNullOrEmpty()` instead.
                if (!gpsStatus.isNullOrEmpty()) {
                    if (listeners.isNullOrEmpty()) {
                        mainHandler.postDelayed(PostLocationUpdateTask(listeners), 100L)
                    } else {
                        pushLocationUpdate(listeners)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    companion object {
        /** Was static field `e`. */
        @JvmField
        val instance: GPSListenerThread = GPSListenerThread()

        /** Was `b(Map)`. Pushes satellite status to each listener in the map. */
        @JvmStatic
        fun pushSatelliteStatus(listeners: Map<*, *>?) {
            if (listeners.isNullOrEmpty()) return
            for (value in listeners.values.toList()) {
                if (value == null) continue
                try {
                    MockLocationHelper.injectSatelliteStatus(value)
                } catch (_: Throwable) {
                }
            }
        }

        /** Was `c(Map)`. Pushes the current location to each listener in the map. */
        @JvmStatic
        fun pushLocationUpdate(listeners: Map<*, *>?) {
            if (listeners.isNullOrEmpty()) return
            try {
                val location = VirtualLocationManager.instance.getCurrentLocation()?.toLocation() ?: return
                for (value in listeners.values.toList()) {
                    if (value == null) continue
                    try {
                        LocationManagerMirror.ListenerTransport.onLocationChanged!!.call(value, location)
                    } catch (_: Throwable) {
                    }
                }
            } catch (_: Throwable) {
            }
        }

        /** Was `d(Map)`. Pushes NMEA sentences to each listener in the map. */
        @JvmStatic
        fun pushNmeaSentences(listeners: Map<*, *>?) {
            if (listeners.isNullOrEmpty()) return
            for (value in listeners.values.toList()) {
                if (value == null) continue
                try {
                    MockLocationHelper.injectNmeaSentences(value)
                } catch (_: Exception) {
                }
            }
        }
    }
}
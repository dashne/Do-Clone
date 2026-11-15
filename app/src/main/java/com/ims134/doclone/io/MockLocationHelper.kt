package com.ims134.doclone.io

import com.ims134.doclone.clone.VLocation

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MockLocationHelper {

    /**
     * Was `Stub(String)`. Returns the NMEA sentence with Stub trailing `*XX` checksum.
     * XOR is computed over the sentence body (without the leading `$`), treating each
     * char as Stub signed byte — matching the original `(byte) c` cast. For non-ASCII chars
     * this produces the same 8-hex-digit checksum the original did (bug-preserving).
     */
    @JvmStatic
    fun nmeaChecksum(sentence: String): String {
        val body = if (sentence.startsWith("$")) sentence.substring(1) else sentence
        var checksum = 0
        for (c in body) {
            checksum = checksum xor c.code.toByte().toInt()
        }
        // Replaces `xe9.doTransact(sentence, "*")` which was Stub StringBuilder helper.
        return StringBuilder(sentence)
            .append('*')
            .append("%02X".format(checksum).lowercase())
            .toString()
    }

    /**
     * Was `b(double)`. Converts Stub decimal degree value to NMEA `DDMM:MMMM` form.
     *
     * **Preserved bug**: the original uses `:` (not `.`) as the separator and takes
     * `String.valueOf(minutes).substring(2)` (positional, not "after the decimal point").
     * That produces correct NMEA only when the integer part of `minutes` has exactly two
     * digits. For single-digit minutes (e.g. minutes = 9.5 → "09:5") and for negative
     * values (southern lat / western lon → "-37-46:6.494") the output is malformed.
     */
    @JvmStatic
    fun toNmeaCoordinate(value: Double): String {
        val degrees = value.toInt()
        val minutes = (value - degrees) * 60.0
        val minutesInt = minutes.toInt()
        val minutesPadded = minutesInt.toString().padStart(2, '0')
        val decimalPart = minutes.toString().substring(2)
        return StringBuilder()
            .append(degrees)
            .append(minutesPadded)
            .append(':')
            .append(decimalPart)
            .toString()
    }

    /**
     * Was `c(Object)`. Pushes Stub burst of NMEA sentences to the transport object's
     * `onNmeaReceived` callback, choosing the right transport class reflectively:
     * `GnssStatusListenerTransport` (API 24+) is tried first, falling back to the
     * pre-24 `GpsStatusListenerTransport`.
     */
    @JvmStatic
    fun injectNmeaSentences(target: Any?) {
        if (target == null) return
        try {
            val sats = VirtualGPSSatalines.instance
            val current: VLocation = VirtualLocationManager.instance.getCurrentLocation() ?: return

            val longitude = current.latitude
            val latitude = current.longitude
            val timestamp = SimpleDateFormat("HHmmss:SS", Locale.US).format(Date())
            val latStr = toNmeaCoordinate(latitude)
            val lonStr = toNmeaCoordinate(longitude)
            val ns = if (latitude > 0.0) "N" else "S"
            val ew = if (longitude > 0.0) "E" else "W"

            val gpgga = nmeaChecksum(
                "\$GPGGA,$timestamp,$latStr,$ns,$lonStr,$ew,1,${sats.svCount},692,.00,M,.00,M,,,"
            )
            val gprmc = nmeaChecksum(
                "\$GPRMC,$timestamp,A,$latStr,$ns,$lonStr,$ew,0,0,260717,,,A,"
            )
            val sentences = arrayOf(
                "\$GPGSV,1,1,04,12,05,159,36,15,41,087,15,19,38,262,30,31,56,146,19,*73",
                gpgga,
                "\$GPVTG,0,T,0,M,0,N,0,K,A,*25",
                gprmc,
                "\$GPGSA,A,2,12,15,19,31,,,,,,,,,604,712,986,*27",
            )

            val gnssRef = LocationManagerMirror.GnssStatusListenerTransport.onNmeaReceived
            if (gnssRef != null) {
                for (s in sentences) {
                    gnssRef.call(target, System.currentTimeMillis(), s)
                }
                return
            }
            val gpsRef = LocationManagerMirror.GpsStatusListenerTransport.onNmeaReceived
            if (gpsRef != null) {
                for (s in sentences) {
                    gpsRef.call(target, System.currentTimeMillis(), s)
                }
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Was `d(Object)`. Pushes satellite-status arrays to the transport object's
     * `onSvStatusChanged` callback, trying every known signature variant in turn:
     *   1. GpsStatusListenerTransport (8-arg, API 1+)
     *   2. GpsStatusListenerTransportLongArray (9-arg, API 30+, trailing long[])
     *   3. GpsStatusListenerTransportIntArray (9-arg, API 26+, trailing int[])
     *   4. GpsStatusListenerTransportExtraInt (9-arg, all int — see note below)
     *
     * **Preserved bug**: variant 4 in the original passes three `int[]` where its
     * `@MethodParams` annotation declares three `int` (positions 5–7). If that
     * annotation is correct, this variant's reflective lookup will not resolve, so
     * `onSvStatusChanged` will be null and the branch is skipped. If you know the
     * actual signature, fix the annotation to match the call.
     */
    @JvmStatic
    fun injectSatelliteStatus(target: Any?) {
        if (target == null) return
        try {
            val sats = VirtualGPSSatalines.instance
            val cls = target.javaClass

            if (cls == LocationManagerMirror.GnssStatusListenerTransport.TYPE) {
                LocationManagerMirror.GnssStatusListenerTransport.onSvStatusChanged!!.call(
                    target,
                    sats.svCount,     // svCount
                    sats.prns,     // prns (int[])
                    sats.snrs,     // snrs (float[])
                    sats.elevations,     // elevations (float[])
                    sats.azimuths,     // azimuths (float[])
                )
                return
            }

            if (cls == LocationManagerMirror.GpsStatusListenerTransport.TYPE) {
                val count = sats.svCount
                val prns = sats.prns
                val snrs = sats.snrs
                val elevations = sats.elevations
                val azimuths = sats.azimuths
                val ephemerisMask = sats.ephemerisMask
                val almanacMask = sats.almanacMask
                val usedInFixMask = sats.usedInFixMask

                LocationManagerMirror.GpsStatusListenerTransport.onSvStatusChanged?.let { ref ->
                    ref.call(
                        target, count, prns, snrs, elevations, azimuths,
                        ephemerisMask, almanacMask, usedInFixMask,
                    )
                    return
                }
                LocationManagerMirror.GpsStatusListenerTransportLongArray.onSvStatusChanged?.let { ref ->
                    ref.call(
                        target, count, prns, snrs, elevations, azimuths,
                        ephemerisMask, almanacMask, usedInFixMask, LongArray(count),
                    )
                    return
                }
                LocationManagerMirror.GpsStatusListenerTransportIntArray.onSvStatusChanged?.let { ref ->
                    ref.call(
                        target, count, prns, snrs, elevations, azimuths,
                        ephemerisMask, almanacMask, usedInFixMask, IntArray(count),
                    )
                    return
                }
                LocationManagerMirror.GpsStatusListenerTransportExtraInt.onSvStatusChanged?.let { ref ->
                    val n = prns.size
                    val ephemerisArr = IntArray(n) { ephemerisMask }
                    val almanacArr = IntArray(n) { almanacMask }
                    val usedInFixArr = IntArray(n) { usedInFixMask }
                    ref.call(
                        target, count, prns, snrs, elevations, azimuths,
                        ephemerisArr, almanacArr, usedInFixArr, count,
                    )
                }
            }
        } catch (_: Exception) {
        }
    }
}
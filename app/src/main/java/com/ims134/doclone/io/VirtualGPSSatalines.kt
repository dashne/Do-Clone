package com.ims134.doclone.io


/**
 * A fixed set of fake GPS satellites in the form android.location.GpsStatus.setStatus wants (was io.v09).
 * The field order below is the argument order of setStatus: svCount, prns, snrs, elevations, azimuths,
 * ephemerisMask, almanacMask, usedInFixMask; [svidWithFlags] is the same data for GnssStatus.
 *
 * The satellite record (was io.ep2) is inlined as [Satellite]. Its constructor was not seen: the order of the three
 * flags (ephemeris, almanac, used in fix) is inferred from how the masks are built, see the reply.
 */
class VirtualGPSSatalines private constructor() {

    private class Satellite(
        val prn: Int,
        val snr: Double,
        val elevation: Double,
        val azimuth: Double,
        val hasEphemeris: Boolean,
        val hasAlmanac: Boolean,
        val usedInFix: Boolean,
    )

    /** was i */
    @JvmField val svCount: Int

    /** was g */
    @JvmField val prns: IntArray

    /** was b */
    @JvmField val snrs: FloatArray

    /** was c */
    @JvmField val elevations: FloatArray

    /** was e */
    @JvmField val azimuths: FloatArray

    /** was d: bit (prn - 1) set for satellites with ephemeris data */
    @JvmField val ephemerisMask: Int

    /** was a */
    @JvmField val almanacMask: Int

    /** was f */
    @JvmField val usedInFixMask: Int

    /** was h: (prn shl 7) or flags (1 ephemeris, 2 almanac, 4 used in fix) or 8 (has carrier frequency) */
    @JvmField val svidWithFlags: IntArray

    init {
        val satellites = listOf(
            Satellite(5, 1.0, 5.0, 112.0, false, true, true),
            Satellite(13, 13.5, 23.0, 53.0, true, true, true),
            Satellite(14, 19.1, 6.0, 247.0, true, true, true),
            Satellite(15, 31.0, 58.0, 45.0, true, true, true),
            Satellite(18, 0.0, 52.0, 309.0, false, true, true),
            Satellite(20, 30.1, 54.0, 105.0, true, true, true),
            Satellite(21, 33.2, 56.0, 251.0, true, true, true),
            Satellite(22, 0.0, 14.0, 299.0, false, true, true),
            Satellite(24, 25.9, 57.0, 157.0, true, true, true),
            Satellite(27, 18.0, 3.0, 309.0, true, true, true),
            Satellite(28, 18.2, 3.0, 42.0, true, true, true),
            Satellite(41, 28.8, 0.0, 0.0, false, false, false),
            Satellite(50, 29.2, 0.0, 0.0, false, true, true),
            Satellite(67, 14.4, 2.0, 92.0, false, false, false),
            Satellite(68, 21.2, 45.0, 60.0, false, false, false),
            Satellite(69, 17.5, 50.0, 330.0, false, true, true),
            Satellite(70, 22.4, 7.0, 291.0, false, false, false),
            Satellite(77, 23.8, 10.0, 23.0, true, true, true),
            Satellite(78, 18.0, 47.0, 70.0, true, true, true),
            Satellite(79, 22.8, 41.0, 142.0, true, true, true),
            Satellite(83, 0.2, 9.0, 212.0, false, false, false),
            Satellite(84, 16.7, 30.0, 264.0, true, true, true),
            Satellite(85, 12.1, 20.0, 317.0, true, true, true),
        )
        svCount = satellites.size
        prns = IntArray(satellites.size) { satellites[it].prn }
        azimuths = FloatArray(satellites.size) { satellites[it].azimuth.toFloat() }
        elevations = FloatArray(satellites.size) { satellites[it].elevation.toFloat() }
        snrs = FloatArray(satellites.size) { satellites[it].snr.toFloat() }
        // shl uses the low 5 bits of the shift (like Java), so prns above 32 wrap; as in the original
        var ephemeris = 0
        var almanac = 0
        var usedInFix = 0
        for (satellite in satellites) {
            val bit = 1 shl (satellite.prn - 1)
            if (satellite.hasEphemeris) ephemeris = ephemeris or bit
            if (satellite.hasAlmanac) almanac = almanac or bit
            if (satellite.usedInFix) usedInFix = usedInFix or bit
        }
        ephemerisMask = ephemeris
        almanacMask = almanac
        usedInFixMask = usedInFix
        svidWithFlags = IntArray(satellites.size) {
            val s = satellites[it]
            (s.prn shl 7) or (if (s.hasEphemeris) 1 else 0) or ((if (s.hasAlmanac) 1 else 0) shl 1) or
                    ((if (s.usedInFix) 1 else 0) shl 2) or 8
        }
    }

    companion object {
        /** was j */
        @JvmField
        val instance = VirtualGPSSatalines()
    }
}
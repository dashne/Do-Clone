package com.ims134.doclone.clone

import com.ims134.doclone.io.Reflect
import android.location.Location
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.ims134.doclone.io.VirtualGPSSatalines


/** A fake location (was com.polestar.clone.remote.vloc.VLocation). Parcel order: lat, lon, alt, accuracy, speed, bearing. */
class VLocation() : Parcelable {

    /** was a */
    @JvmField var latitude: Double = 0.0

    /** was b */
    @JvmField var longitude: Double = 0.0

    /** was c */
    @JvmField var altitude: Double = 0.0

    /** was d */
    @JvmField var accuracy: Float = 0f

    /** was e */
    @JvmField var speed: Float = 0f

    /** was f */
    @JvmField var bearing: Float = 0f

    /**
     * was a(): the android.location.Location for this fake location, a "gps" fix flagged as not mocked.
     * As in the original: the accuracy is a constant 8 m ([accuracy] and [altitude] are not used), the time is now,
     * and the elapsed realtime is a constant 277000000 ns.
     */
    fun toLocation(): Location {
        val location = Location("gps")
        location.accuracy = 8.0f
        val extras = Bundle()
        location.bearing = bearing
        Reflect(location).call("setIsFromMockProvider", false)
        location.latitude = latitude
        location.longitude = longitude
        location.speed = speed
        location.time = System.currentTimeMillis()
        location.extras = extras
        location.elapsedRealtimeNanos = 277000000L
        val satellites = VirtualGPSSatalines.instance.svCount
        extras.putInt("satellites", satellites)
        extras.putInt("satellitesvalue", satellites)
        return location
    }

    override fun describeContents(): Int = 0

    override fun toString(): String =
        "VLocation{latitude=$latitude, longitude=$longitude, altitude=$altitude, accuracy=$accuracy, speed=$speed, bearing=$bearing}"

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeDouble(altitude)
        parcel.writeFloat(accuracy)
        parcel.writeFloat(speed)
        parcel.writeFloat(bearing)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<VLocation> = object : Parcelable.Creator<VLocation> {
            override fun createFromParcel(parcel: Parcel): VLocation {
                val location = VLocation()
                location.latitude = parcel.readDouble()
                location.longitude = parcel.readDouble()
                location.altitude = parcel.readDouble()
                location.accuracy = parcel.readFloat()
                location.speed = parcel.readFloat()
                location.bearing = parcel.readFloat()
                return location
            }

            override fun newArray(size: Int): Array<VLocation?> = arrayOfNulls(size)
        }
    }
}
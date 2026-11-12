package com.ims134.doclone.clone


import android.os.Parcel
import android.os.Parcelable

/**
 * A fake cell tower (was com.polestar.clone.remote.vloc.VCell). The field names are the VirtualApp ones, matched by
 * count and order (9 ints, get to i); the code never shows what they mean. The parcel order is get to i.
 */
class VCell() : Parcelable {

    @JvmField var type: Int = 0          // get
    @JvmField var mcc: Int = 0           // b
    @JvmField var mnc: Int = 0           // c
    @JvmField var psc: Int = 0           // d
    @JvmField var cid: Int = 0           // e
    @JvmField var lac: Int = 0           // f
    @JvmField var baseStationId: Int = 0 // g
    @JvmField var systemId: Int = 0      // h
    @JvmField var networkId: Int = 0     // i

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(type)
        parcel.writeInt(mcc)
        parcel.writeInt(mnc)
        parcel.writeInt(psc)
        parcel.writeInt(cid)
        parcel.writeInt(lac)
        parcel.writeInt(baseStationId)
        parcel.writeInt(systemId)
        parcel.writeInt(networkId)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<VCell> = object : Parcelable.Creator<VCell> {
            override fun createFromParcel(parcel: Parcel): VCell {
                val cell = VCell()
                cell.type = parcel.readInt()
                cell.mcc = parcel.readInt()
                cell.mnc = parcel.readInt()
                cell.psc = parcel.readInt()
                cell.cid = parcel.readInt()
                cell.lac = parcel.readInt()
                cell.baseStationId = parcel.readInt()
                cell.systemId = parcel.readInt()
                cell.networkId = parcel.readInt()
                return cell
            }

            override fun newArray(size: Int): Array<VCell?> = arrayOfNulls(size)
        }
    }
}
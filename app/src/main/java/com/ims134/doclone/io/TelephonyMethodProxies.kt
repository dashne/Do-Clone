package com.ims134.doclone.io


import android.os.Bundle
import android.os.Parcelable
import android.os.RemoteException
import android.telephony.NeighboringCellInfo
import android.telephony.cdma.CdmaCellLocation
import android.telephony.gsm.GsmCellLocation
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VirtualRuntime
import com.ims134.doclone.reflect.RefClass
import com.ims134.doclone.reflect.RefInt
import java.lang.reflect.Method


/**
 * Was `cr4`. TelephonyManager proxies used by `TelephonyManagerStub`.
 * All `getMethodName()` values map to `TelephonyManager` public API.
 */
internal class TelephonyMethodProxies private constructor() {

    /**
     * Was `a`. `getAllCellInfo` — returns fake cell list from VirtualLocationManager.
     * Builds `CellInfoCdma` / `CellInfoGsm` instances via reflection and sets their
     * cell-identity and signal-strength fields, then returns them as `Parcelable`.
     */
    class GetAllCellInfo : ReplaceCallingPkgMethodProxy("getAllCellInfo") {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (MethodProxy.isFakeLocationEnable()) {
                try {
                    val allCell = VirtualLocationManager.instance.getService()
                        ?.getAllCell(MethodProxy.getAppUserId(), MethodProxy.getAppPkg())
                    if (allCell != null) {
                        val result = ArrayList<Parcelable>(allCell.size)
                        for (vCell in allCell) {
                            val parcelable : Parcelable? = if (vCell.type == 2) {
                                // CDMA
                                val cdma = CellInfoCdmaMirror.ctor!!.newInstance()
                                val identity = CellInfoCdmaMirror.mCellIdentityCdma!!.get(cdma)
                                val signal = CellInfoCdmaMirror.mCellSignalStrengthCdma!!.get(cdma)
                                CellIdentityCdma.mNetworkId!!.set(identity, vCell.networkId)
                                CellIdentityCdma.mSystemId!!.set(identity, vCell.systemId)
                                CellIdentityCdma.mBasestationId!!.set(identity, vCell.baseStationId)
                                CellSignalStrengthCdma.mCdmaDbm!!.set(signal, -74)
                                CellSignalStrengthCdma.mCdmaEcio!!.set(signal, -91)
                                CellSignalStrengthCdma.mEvdoDbm!!.set(signal, -64)
                                CellSignalStrengthCdma.mEvdoSnr!!.set(signal, 7)
                                cdma
                            } else {
                                // GSM
                                val gsm = CellInfoGsm.ctor!!.newInstance()
                                val identity = CellInfoGsm.mCellIdentityGsm!!.get(gsm)
                                val signal = CellInfoGsm.mCellSignalStrengthGsm!!.get(gsm)
                                CellIdentityGsm.mMcc!!.set(identity, vCell.mcc)
                                CellIdentityGsm.mMnc!!.set(identity, vCell.mnc)
                                CellIdentityGsm.mLac!!.set(identity, vCell.lac)
                                CellIdentityGsm.mCid!!.set(identity, vCell.cid)
                                CellSignalStrengthGsm.mSignalStrength!!.set(signal, 20)
                                CellSignalStrengthGsm.mBitErrorRate!!.set(signal, 0)
                                gsm
                            }
                            result.add(parcelable!!)
                        }
                        return result
                    }
                } catch (e: RemoteException) {
                    VirtualRuntime.crash(e)
                    throw e
                }
            }
            return try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                ArrayList<Parcelable>(0)
            }
        }
    }

    /** Was `b`. `getAllCellInfoUsingSubId` — null when faking, otherwise delegates. */
    class GetAllCellInfoUsingSubId : ReplaceCallingPkgMethodProxy("getAllCellInfoUsingSubId") {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            if (MethodProxy.isFakeLocationEnable()) null
            else super.call(who, method, args)
    }

    /**
     * Was `c`. `getCellLocation` — builds Stub `Bundle` from Stub `GsmCellLocation` or
     * `CdmaCellLocation` backed by the fake cell data. Falls back to populating the
     * bundle manually if the helper class throws (e.g. hidden-API restrictions).
     */
    class GetCellLocation : ReplaceCallingPkgMethodProxy("getCellLocation") {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (MethodProxy.isFakeLocationEnable()) {
                try {
                    val cell = VirtualLocationManager.instance.getService()
                        ?.getCell(MethodProxy.getAppUserId(), MethodProxy.getAppPkg())
                    if (cell != null) {
                        val cid = cell.cid
                        val lac = cell.lac
                        val bundle = Bundle()
                        if (cell.type == 2) {
                            // CDMA
                            try {
                                val loc = CdmaCellLocation()
                                loc.setCellLocationData(
                                    cell.baseStationId,
                                    Int.MAX_VALUE,
                                    Int.MAX_VALUE,
                                    cell.systemId,
                                    cell.networkId,
                                )
                                loc.fillInNotifierBundle(bundle)
                                return bundle
                            } catch (_: Throwable) {
                                bundle.putInt("baseStationId", cell.baseStationId)
                                bundle.putInt("baseStationLatitude", Int.MAX_VALUE)
                                bundle.putInt("baseStationLongitude", Int.MAX_VALUE)
                                bundle.putInt("systemId", cell.systemId)
                                bundle.putInt("networkId", cell.networkId)
                            }
                        } else {
                            // GSM
                            try {
                                val loc = GsmCellLocation()
                                loc.setLacAndCid(lac, cid)
                                loc.fillInNotifierBundle(bundle)
                                return bundle
                            } catch (_: Throwable) {
                                bundle.putInt("lac", lac)
                                bundle.putInt("cid", cid)
                                bundle.putInt("psc", cell.psc)
                            }
                        }
                        return bundle
                    }
                } catch (e: RemoteException) {
                    VirtualRuntime.crash(e)
                    throw e
                }
            }
            return super.call(who, method, args)
        }
    }

    /**
     * Was `d`. `getDeviceId` — returns the fake device IMEI from
     * `MethodProxy.getDeviceInfo()`. Extended by every other `getDeviceId*` and
     * `getImei*` / `getMeid*` proxy.
     */
    open class GetDeviceId : ReplaceLastPkgMethodProxy("getDeviceId") {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            MethodProxy.getDeviceInfo().deviceId
    }

    /** Was `e`. `getDeviceIdForPhone` — inherits [GetDeviceId]'s body. */
    class GetDeviceIdForPhone : GetDeviceId() {
        override fun getMethodName(): String = "getDeviceIdForPhone"
    }

    /** Was `f`. `getDeviceIdForSubscriber` — inherits [GetDeviceId]'s body. */
    class GetDeviceIdForSubscriber : GetDeviceId() {
        override fun getMethodName(): String = "getDeviceIdForSubscriber"
    }

    /** Was `g`. `getDeviceIdWithFeature` — same behavior as [GetDeviceId], different name. */
    class GetDeviceIdWithFeature : ReplaceLastPkgMethodProxy("getDeviceIdWithFeature") {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            MethodProxy.getDeviceInfo().deviceId
    }

    /**
     * Was `h`. `getIccSerialNumber` — returns the fake ICCID if available, otherwise
     * delegates to the real service; returns `""` on any failure.
     */
    open class GetIccSerialNumber : ReplaceLastPkgMethodProxy("getIccSerialNumber") {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                val iccid = MethodProxy.getDeviceInfo().iccSerialNumber
                if (iccid == null) super.call(who, method, args) else iccid
            } catch (_: Throwable) {
                ""
            }
    }

    /** Was `i`. `getIccSerialNumberForSubscriber` — inherits [GetIccSerialNumber]'s body. */
    class GetIccSerialNumberForSubscriber : GetIccSerialNumber() {
        override fun getMethodName(): String = "getIccSerialNumberForSubscriber"
    }

    /** Was `j`. `getImeiForSlot` — inherits [GetDeviceId]'s body. */
    class GetImeiForSlot : GetDeviceId() {
        override fun getMethodName(): String = "getImeiForSlot"
    }

    /** Was `k`. `getImeiForSubscriber` — inherits [GetDeviceId]'s body. */
    class GetImeiForSubscriber : GetDeviceId() {
        override fun getMethodName(): String = "getImeiForSubscriber"
    }

    /** Was `l`. `getMeidForSlot` — inherits [GetDeviceId]'s body. */
    class GetMeidForSlot : GetDeviceId() {
        override fun getMethodName(): String = "getMeidForSlot"
    }

    /**
     * Was `m`. `getNeighboringCellInfo` — returns Stub list of `NeighboringCellInfo`
     * built from the fake neighboring-cell data (lac, cid, rssi=6).
     */
    class GetNeighboringCellInfo :
        ReplaceCallingPkgMethodProxy("getNeighboringCellInfo") {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (MethodProxy.isFakeLocationEnable()) {
                try {
                    val neighbors = VirtualLocationManager.instance.getService()
                        ?.getNeighboringCell(MethodProxy.getAppUserId(), MethodProxy.getAppPkg())
                    if (neighbors != null) {
                        val result = ArrayList<NeighboringCellInfo>(neighbors.size)
                        for (vCell in neighbors) {
                            val info = NeighboringCellInfo()
                            NeighboringCellInfoMirror.mLac!!.set(info, vCell.lac)
                            NeighboringCellInfoMirror.mCid!!.set(info, vCell.cid)
                            NeighboringCellInfoMirror.mRssi!!.set(info, 6)
                            result.add(info)
                        }
                        return result
                    }
                } catch (e: RemoteException) {
                    VirtualRuntime.crash(e)
                }
            }
            return super.call(who, method, args)
        }
    }
}

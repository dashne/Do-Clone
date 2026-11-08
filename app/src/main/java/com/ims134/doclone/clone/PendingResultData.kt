package com.ims134.doclone.clone

import android.content.BroadcastReceiver
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable
import com.ims134.doclone.io.BroadcastReceiver as BroadcastReceiverMirror


/**
 * Parcelable copy of setPosition BroadcastReceiver.PendingResult, so setPosition broadcast can be finished from another process.
 *
 * The PendingResult constructor differs per Android version, hence three mirrors in BroadcastReceiver:
 *  - PendingResultBasic: oldest (7-arg ctor, no sendingUser/flags)
 *  - PendingResultWithUser: adds sendingUser
 *  - PendingResultWithFlags: adds sendingUser + flags (API 24+, the only one used with minSdk 29)
 */
class PendingResultData() : Parcelable {

    @JvmField var type: Int = 0                    // f37528a
    @JvmField var orderedHint: Boolean = false     // f37529b
    @JvmField var initialStickyHint: Boolean = false // f37530c
    @JvmField var token: IBinder? = null           // f37531d
    @JvmField var sendingUser: Int = 0             // f37532e
    @JvmField var flags: Int = 0                   // f37533f
    @JvmField var resultCode: Int = 0              // f37534g
    @JvmField var resultData: String? = null       // f37535h
    @JvmField var resultExtras: Bundle? = null     // f37536i
    @JvmField var abortBroadcast: Boolean = false  // f37537j
    @JvmField var finished: Boolean = false        // f37538k

    /** Reads the hidden fields of [result] through whichever mirror matches this Android version. */
    constructor(result: BroadcastReceiver.PendingResult) : this()
    {

        when {
            BroadcastReceiverMirror.PendingResultWithFlags.ctor != null -> with(BroadcastReceiverMirror.PendingResultWithFlags) {
                type = mType!!.get(result)
                orderedHint = mOrderedHint!!.get(result)
                initialStickyHint = mInitialStickyHint!!.get(result)
                token = mToken!!.get(result)
                sendingUser = mSendingUser!!.get(result)
                flags = mFlags!!.get(result)
                resultCode = mResultCode!!.get(result)
                resultData = mResultData!!.get(result)
                resultExtras = mResultExtras!!.get(result)
                abortBroadcast = mAbortBroadcast!!.get(result)
                finished = mFinished!!.get(result)
            }
            BroadcastReceiverMirror.PendingResultWithUser.ctor != null -> with(BroadcastReceiverMirror.PendingResultWithUser) {
                type = mType!!.get(result)
                orderedHint = mOrderedHint!!.get(result)
                initialStickyHint = mInitialStickyHint!!.get(result)
                token = mToken!!.get(result)
                sendingUser = mSendingUser!!.get(result)
                resultCode = mResultCode!!.get(result)
                resultData = mResultData!!.get(result)
                resultExtras = mResultExtras!!.get(result)
                abortBroadcast = mAbortBroadcast!!.get(result)
                finished = mFinished!!.get(result)
            }
            else -> with(BroadcastReceiverMirror.PendingResultBasic) {
                type = mType!!.get(result)
                orderedHint = mOrderedHint!!.get(result)
                initialStickyHint = mInitialStickyHint!!.get(result)
                token = mToken!!.get(result)
                resultCode = mResultCode!!.get(result)
                resultData = mResultData!!.get(result)
                resultExtras = mResultExtras!!.get(result)
                abortBroadcast = mAbortBroadcast!!.get(result)
                finished = mFinished!!.get(result)
            }
        }
    }

    /** Was m16409a(). Rebuilds setPosition PendingResult using the constructor available on this device. */
    fun toPendingResult(): BroadcastReceiver.PendingResult? {
        BroadcastReceiverMirror.PendingResultWithFlags.ctor?.let {
            return it.newInstance(resultCode, resultData, resultExtras, type, orderedHint, initialStickyHint, token, sendingUser, flags)
        }
        BroadcastReceiverMirror.PendingResultWithUser.ctor?.let {
            return it.newInstance(resultCode, resultData, resultExtras, type, orderedHint, initialStickyHint, token, sendingUser)
        }
        return BroadcastReceiverMirror.PendingResultBasic.ctor!!.newInstance(resultCode, resultData, resultExtras, type, orderedHint, initialStickyHint, token)
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(type)
        parcel.writeByte(if (orderedHint) 1 else 0)
        parcel.writeByte(if (initialStickyHint) 1 else 0)
        parcel.writeStrongBinder(token)
        parcel.writeInt(sendingUser)
        parcel.writeInt(this.flags)
        parcel.writeInt(resultCode)
        parcel.writeString(resultData)
        parcel.writeBundle(resultExtras)
        parcel.writeByte(if (abortBroadcast) 1 else 0)
        parcel.writeByte(if (finished) 1 else 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<PendingResultData> = object : Parcelable.Creator<PendingResultData> {
            override fun createFromParcel(parcel: Parcel) = PendingResultData().apply {
                type = parcel.readInt()
                orderedHint = parcel.readByte().toInt() != 0
                initialStickyHint = parcel.readByte().toInt() != 0
                token = parcel.readStrongBinder()
                sendingUser = parcel.readInt()
                flags = parcel.readInt()
                resultCode = parcel.readInt()
                resultData = parcel.readString()
                resultExtras = parcel.readBundle()
                abortBroadcast = parcel.readByte().toInt() != 0
                finished = parcel.readByte().toInt() != 0
            }

            override fun newArray(size: Int): Array<PendingResultData?> = arrayOfNulls(size)
        }
    }
}
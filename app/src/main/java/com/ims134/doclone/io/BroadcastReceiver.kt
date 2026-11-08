package com.ims134.doclone.io


import android.content.BroadcastReceiver
import android.os.Bundle
import android.os.IBinder
import com.ims134.doclone.reflect.MethodParams
import com.ims134.doclone.reflect.RefBoolean
import com.ims134.doclone.reflect.RefClass
import com.ims134.doclone.reflect.RefConstructor
import com.ims134.doclone.reflect.RefInt
import com.ims134.doclone.reflect.RefMethod
import com.ims134.doclone.reflect.RefObject

/**
 * Reflection mirror of android.content.BroadcastReceiver and its PendingResult
 * (was io.na0, source file "BroadcastReceiver.java").
 *
 * Field names must match the real member names: RefClass.load() binds by name.
 * TYPE is declared last on purpose, so load() runs after the `= null` initializers.
 */
object BroadcastReceiver {

    @JvmField var getPendingResult: RefMethod<BroadcastReceiver.PendingResult>? = null

    @JvmField
    @field:MethodParams([BroadcastReceiver.PendingResult::class])
    var setPendingResult: RefMethod<Void>? = null

    @JvmField
    val TYPE: Class<*>? = RefClass.load(BroadcastReceiver::class.java, BroadcastReceiver::class.java)

    // ------------------------------------------------------------------
    // PendingResult exists in three shapes. With minSdk 29 only PendingResultWithFlags is ever bound.
    // ------------------------------------------------------------------

    /** Oldest layout: no sendingUser, no flags (was C8647a). */
    object PendingResultBasic {
        @JvmField
        @field:MethodParams([Int::class, String::class, Bundle::class, Int::class, Boolean::class, Boolean::class, IBinder::class])
        var ctor: RefConstructor<BroadcastReceiver.PendingResult>? = null

        @JvmField var mAbortBroadcast: RefBoolean? = null
        @JvmField var mFinished: RefBoolean? = null
        @JvmField var mInitialStickyHint: RefBoolean? = null
        @JvmField var mOrderedHint: RefBoolean? = null
        @JvmField var mResultCode: RefInt? = null
        @JvmField var mResultData: RefObject<String>? = null
        @JvmField var mResultExtras: RefObject<Bundle>? = null
        @JvmField var mToken: RefObject<IBinder>? = null
        @JvmField var mType: RefInt? = null

        @JvmField
        val TYPE: Class<*>? = RefClass.load(PendingResultBasic::class.java, BroadcastReceiver.PendingResult::class.java)
    }

    /** Adds mSendingUser (was C8648b). */
    object PendingResultWithUser {
        @JvmField
        @field:MethodParams([Int::class, String::class, Bundle::class, Int::class, Boolean::class, Boolean::class, IBinder::class, Int::class])
        var ctor: RefConstructor<BroadcastReceiver.PendingResult>? = null

        @JvmField var mAbortBroadcast: RefBoolean? = null
        @JvmField var mFinished: RefBoolean? = null
        @JvmField var mInitialStickyHint: RefBoolean? = null
        @JvmField var mOrderedHint: RefBoolean? = null
        @JvmField var mResultCode: RefInt? = null
        @JvmField var mResultData: RefObject<String>? = null
        @JvmField var mResultExtras: RefObject<Bundle>? = null
        @JvmField var mSendingUser: RefInt? = null
        @JvmField var mToken: RefObject<IBinder>? = null
        @JvmField var mType: RefInt? = null

        @JvmField
        val TYPE: Class<*>? = RefClass.load(PendingResultWithUser::class.java, BroadcastReceiver.PendingResult::class.java)
    }

    /** Adds mSendingUser and mFlags, API 24+ (was C8649c). */
    object PendingResultWithFlags {
        @JvmField
        @field:MethodParams([Int::class, String::class, Bundle::class, Int::class, Boolean::class, Boolean::class, IBinder::class, Int::class, Int::class])
        var ctor: RefConstructor<BroadcastReceiver.PendingResult>? = null

        @JvmField var mAbortBroadcast: RefBoolean? = null
        @JvmField var mFinished: RefBoolean? = null
        @JvmField var mFlags: RefInt? = null
        @JvmField var mInitialStickyHint: RefBoolean? = null
        @JvmField var mOrderedHint: RefBoolean? = null
        @JvmField var mResultCode: RefInt? = null
        @JvmField var mResultData: RefObject<String>? = null
        @JvmField var mResultExtras: RefObject<Bundle>? = null
        @JvmField var mSendingUser: RefInt? = null
        @JvmField var mToken: RefObject<IBinder>? = null
        @JvmField var mType: RefInt? = null

        @JvmField
        val TYPE: Class<*>? = RefClass.load(PendingResultWithFlags::class.java, BroadcastReceiver.PendingResult::class.java)
    }
}
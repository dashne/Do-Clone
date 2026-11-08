package com.ims134.doclone.clone.server

import com.ims134.doclone.clone.VUserHandle
import android.content.pm.ApplicationInfo
import android.os.Binder
import android.os.ConditionVariable
import android.os.IInterface
import com.ims134.doclone.io.IVClient


/**
 * A virtual process (was com.polestar.clone.server.am.h). It is also the binder token the client
 * hands back at attach time, so it extends Binder.
 * Made public (was package-private) because VActivityManagerService's public API returns it.
 */
class ProcessRecord(
    @JvmField val appInfo: ApplicationInfo,
    @JvmField val processName: String?,
    @JvmField val vuid: Int,   // virtual uid = userId * 100000 + appId
    @JvmField val vpid: Int,   // stub process index
) : Binder(), Comparable<ProcessRecord> {

    /** Set once the process attached. */
    @JvmField var client: IVClient? = null          // f37xxx e
    @JvmField var appThread: IInterface? = null       // f
    @JvmField var Pid: Int = 0                        // g: real pid

    @JvmField val userId: Int = vuid / VUserHandle.PER_USER_RANGE   // h/100000

    /** Opened when the process finished attaching / started executing. (was setPosition) */
    @JvmField val attachCondition = ConditionVariable()

    /** Packages running in this process (element type assumed String). (was Styleable) */
    @JvmField val packageList = HashSet<String>()

    // Same as the original: identity is the process name only. hashCode is NOT overridden there
    // (Binder's identity hash), so HashSet/HashMap lookups still use identity.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other.javaClass != javaClass) return false
        return processName == (other as ProcessRecord).processName
    }

    override fun hashCode(): Int = super.hashCode()

    /** Original always returns 0. */
    override fun compareTo(other: ProcessRecord): Int = 0
}
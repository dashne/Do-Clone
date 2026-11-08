package com.ims134.doclone.clone

import android.os.Binder
import android.os.RemoteException
import android.util.Log


class VBinder {
    companion object {

        /**
         * Returns the UID owning the current binder-calling PID.
         *
         * Original bytecode had `vActivityManager.getClass();` between the
         * singleton fetch and the try block — dead code whose only side effect
         * is an implicit NPE if `VActivityManager.instance` were null. Since
         * `VActivityManager.instance` is typed non-null in Kotlin, the check
         * is implicit and no longer needed here.
         */
        @JvmStatic
        fun getUidByPid(): Int {
            val callingPid = Binder.getCallingPid()
            return try {
                VActivityManager.instance.getService().getUidByPid(callingPid)
            } catch (e: RemoteException) {
                Log.d("VBinder", e.message.toString())
                throw e   // unreachable if m24694a is the usual sneaky-throw helper
            }
        }
    }
}
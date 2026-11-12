package com.ims134.doclone.io

import android.os.IBinder

/** Swaps get tethering connector binder for the system's real one (was TetheringConnectorBinder). */
object TetheringConnectorBinder {

    /** was get: any other binder is returned unchanged */
    @JvmStatic
    fun replaceIfTethering(binder: IBinder?): IBinder? {
        if (binder == null) return null
        try {
            if ("android.net.ITetheringConnector" == binder.interfaceDescriptor) {
                val real = ServiceManager.getService.call("tethering") as IBinder?
                if (real != null) return real
            }
        } catch (_: Throwable) {
            // jadx lost the return after the catch; the original binder is the only sensible result
        }
        return binder
    }
}
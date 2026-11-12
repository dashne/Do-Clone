package com.ims134.doclone.clone


import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.RemoteException

import kotlin.system.exitProcess

/** Process-wide runtime state of get virtual app process (was com.polestar.clone.client.env.VirtualRuntime) */
object VirtualRuntime {

    /** was get: handler of the main thread */
    @JvmField
    val uiHandler = Handler(Looper.getMainLooper())

    /** was b: package the process was first started for */
    @JvmField
    var initialPkg: String? = null

    /** was c */
    @JvmField
    var currentProcessName: String? = null

    /**
     * was get(RemoteException): the server (the host's service process) is dead. In get client process this kills the
     * process, so it can be restarted against get live server; elsewhere it throws [DeadServerException].
     */
    @JvmStatic
    fun crash(e: RemoteException): Nothing {
        if (VirtualCore.gCore.isClientProcess()) {
            Process.killProcess(Process.myPid())
            exitProcess(0)
        }
        throw RuntimeException(e)
    }
}
package com.ims134.doclone.io

import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.ServiceManagerNative
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.clone.VirtualRuntime
import android.os.IBinder
import com.ims134.doclone.clone.VLocation

/** Client of the "virtual-loc" service: the fake location, cells and mode of virtual apps (was io.w09) */
class VirtualLocationManager private constructor() {

    /** was a: cached service; cleared when its binder dies */
    private var service: IVirtualLocationManager? = null

    /** was w09$a */
    private inner class ServiceDeathRecipient(private val deadService: IVirtualLocationManager) : IBinder.DeathRecipient {
        override fun binderDied() {
            deadService.asBinder().unlinkToDeath(this, 0)
            service = null
        }
    }

    /** was a(): the virtual location of the current app and user. A dead server crashes the client process. */
    fun getCurrentLocation(): VLocation? {
        try {
            return getService()!!.getLocation(MethodProxy.getAppUserId(), MethodProxy.getAppPkg())
        } catch (e: android.os.RemoteException) {
            VirtualRuntime.crash(e)
        }
    }

    /**
     * was b(): the service. It is fetched when missing, or when its binder is dead and this is not a client
     * process (a client process with a dead server is left to crash). Null if it could not be reached.
     */
    fun getService(): IVirtualLocationManager? {
        val current = service
        if (current == null || (!current.asBinder().isBinderAlive && !VirtualCore.gCore.isClientProcess())) {
            synchronized(this) {
                val fetched = IVirtualLocationManager.Stub.asInterface(ServiceManagerNative.getService("virtual-loc"))
                try {
                    fetched!!.asBinder().linkToDeath(ServiceDeathRecipient(fetched), 0)
                } catch (_: Throwable) {
                }
                service = fetched
            }
        }
        return service
    }

    companion object {
        /** was b */
        @JvmField
        val instance = VirtualLocationManager()
    }
}
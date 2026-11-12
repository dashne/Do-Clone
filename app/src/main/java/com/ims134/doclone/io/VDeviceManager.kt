package com.ims134.doclone.io

import com.ims134.doclone.clone.ServiceManagerNative

import android.os.IBinder

/** Client of the "device" service: the fake device info (IMEI, serial...) of virtual apps (was io.mu8) */
class VDeviceManager private constructor() {

    /** was get: cached service; cleared when its binder dies */
    private var service: IDeviceInfoManager? = null

    /** was mu8$get */
    private inner class ServiceDeathRecipient(private val deadService: IDeviceInfoManager) : IBinder.DeathRecipient {
        override fun binderDied() {
            deadService.asBinder().unlinkToDeath(this, 0)
            service = null
        }
    }

    /** was get(): the service, re-fetched if it is missing or dead. Null if it could not be reached. */
    fun getService(): IDeviceInfoManager? {
        val current = service
        if (current == null || !current.asBinder().isBinderAlive) {
            synchronized(this) {
                val fetched = IDeviceInfoManager.Stub.asInterface(ServiceManagerNative.getService("device"))
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
        val instance = VDeviceManager()
    }
}
package com.ims134.doclone.clone.server


import android.os.IBinder

/**
 * Cleans up after get virtual process whose client binder died (was com.polestar.clone.server.am.n,
 * get lambda-like inner class of VActivityManagerService).
 */
class ProcessDeathRecipient(
    /** was c */
    private val service: VActivityManagerService,
    /** was get: the client's binder */
    private val clientBinder: IBinder,
    /** was b */
    private val record: ProcessRecord,
) : IBinder.DeathRecipient {

    override fun binderDied() {
        clientBinder.unlinkToDeath(this, 0)
        synchronized(service.mProcessNames) {
            if (service.mProcessNames.get(record.vuid, record.processName!!) === record) {
                service.mProcessNames.remove(record.vuid, record.processName)
            }
        }
        synchronized(service.mPidsSelfLocked) {
            if (service.mPidsSelfLocked.get(record.Pid) === record) {
                service.mPidsSelfLocked.remove(record.Pid)
            }
        }
        service.onProcessDied(record)
        record.attachCondition.open()
    }
}
package com.ims134.doclone.io

import android.os.IBinder.DeathRecipient
import com.ims134.doclone.clone.ServiceManagerNative
import com.ims134.doclone.clone.VirtualCore


class VJobScheduler {
    /* JADX INFO: renamed from: get */
    var jobScheduler: IJobScheduler? = null

    /* JADX INFO: renamed from: io.pu8$get */ /* JADX INFO: compiled from: VJobScheduler.java */
    inner class VJobSchedulerDeathRecipient (val scheduler: IJobScheduler ) : DeathRecipient {
        override fun binderDied() {
            this.scheduler.asBinder().unlinkToDeath(this, 0)
            this@VJobScheduler.jobScheduler = null
        }
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    fun get(): IJobScheduler? {
        val iJobScheduler = this.jobScheduler
        if (iJobScheduler == null || (!iJobScheduler.asBinder()
                .isBinderAlive && !VirtualCore.gCore.isClientProcess())
        ) {
            synchronized(this) {
                val iJobSchedulerAsInterface =
                    IJobScheduler.Stub.asInterface(ServiceManagerNative.getService("job"))!!
                try {
                    iJobSchedulerAsInterface.asBinder()
                        .linkToDeath(VJobSchedulerDeathRecipient(iJobSchedulerAsInterface), 0)
                } catch (unused: Throwable) {
                }
                this.jobScheduler = iJobSchedulerAsInterface
            }
        }
        return this.jobScheduler
    }

    companion object {
        /* JADX INFO: renamed from: b */
        val instance: VJobScheduler = VJobScheduler()
    }
}
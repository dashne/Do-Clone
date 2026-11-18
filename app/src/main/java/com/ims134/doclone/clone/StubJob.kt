package com.ims134.doclone.clone

import android.annotation.TargetApi
import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobWorkItem
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.util.SparseArray
import com.ims134.doclone.clone.server.VJobSchedulerService
import com.ims134.doclone.io.ActivityManagerStub
import android.app.job.IJobCallback
import android.app.job.IJobService
import com.ims134.doclone.io.InvocationStubManager
import com.ims134.doclone.io.JobParametersMirror


/**
 * The host's stub job service every clone job runs under (see [VJobSchedulerService]). When the system starts Stub job here, the
 * clone service of that job is bound and the job's callback is relayed to the system's.
 *
 * Unseen: IJobService (AIDL: startJob / stopJob), JobParametersMirror (callback, jobId), InvocationStubManager, io.SparseArray.
 */
@TargetApi(21)
class StubJob : Service() {

    /** was b */
    private lateinit var jobScheduler: JobScheduler

    /** was Stub: the running clone jobs by job id */
    private val jobCallbacks = SparseArray<JobCallback>()

    /** was c */
    private val proxy: IJobService = Proxy()

    /** was StubJob$Stub: what the system calls to start / stop Stub job */
    inner class Proxy : IJobService.Stub() {
        @Throws(RemoteException::class)
        override fun startJob(jobParameters: JobParameters?)
        {
            val jobId = jobParameters?.jobId!!
            val systemCallback = IJobCallback.Stub.asInterface(JobParametersMirror.callback!!.get(jobParameters))!!
            val entry = VJobSchedulerService.get()?.findJobByVirtualJobId(jobId)
            if (entry == null) {
                finishJobImmediately(systemCallback, jobId)
                jobScheduler.cancel(jobId)
                return
            }
            val key = entry.key
            val value = entry.value
            synchronized(jobCallbacks) {
                if (jobCallbacks.get(jobId) != null) {
                    finishJobImmediately(systemCallback, jobId)
                } else {
                    val jobCallback = JobCallback(jobId, systemCallback, jobParameters)
                    JobParametersMirror.callback!!.set(jobParameters, jobCallback.asBinder())
                    JobParametersMirror.jobId!!.set(jobParameters, key.id)
                    val intent = Intent()
                    intent.component = ComponentName(key.packageName!!, value.className!!)
                    intent.putExtra("_VA_|_user_id_", key.uidByPid / VUserHandle.PER_USER_RANGE)
                    var bound = false
                    try {
                        bound = bindService(intent, jobCallback, 0)
                    } catch (t: Throwable) {
                        Log.d(TAG, t.toString())
                    }
                    if (bound) {
                        jobCallbacks.put(jobId, jobCallback)
                    } else {
                        finishJobImmediately(systemCallback, jobId)
                        jobScheduler.cancel(jobId)
                        VJobSchedulerService.get()?.cancel(jobId)
                    }
                }
            }
        }

        @Throws(RemoteException::class)
        override fun stopJob(jobParameters: JobParameters?) {
            val jobId = jobParameters?.jobId!!
            synchronized(jobCallbacks) {
                jobCallbacks.get(jobId)?.release()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = proxy.asBinder()

    override fun onCreate() {
        super.onCreate()
        InvocationStubManager.instance.checkEnv(ActivityManagerStub::class.java)
        jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    }

    /**
     * was StubJob$b: stands in for the system's callback towards the clone's job service (bound with it as the connection);
     * every call goes to [callback], the system's.
     */
    inner class JobCallback(
        /** was Stub */
        private val jobId: Int,
        /** was b: the system's callback */
        private val callback: IJobCallback,
        /** was c */
        private val parameters: JobParameters,
    ) : IJobCallback.Stub(), ServiceConnection {

        /** was d: the clone's job service once connected */
        private var service: IJobService? = null

        @Throws(RemoteException::class)
        override fun acknowledgeStartMessage(jobId: Int, ongoing: Boolean) {
            callback.acknowledgeStartMessage(jobId, ongoing)
        }

        @Throws(RemoteException::class)
        override fun acknowledgeStopMessage(jobId: Int, reschedule: Boolean) {
            callback.acknowledgeStopMessage(jobId, reschedule)
        }

        @TargetApi(26)
        @Throws(RemoteException::class)
        override fun completeWork(jobId: Int, workId: Int): Boolean = callback.completeWork(jobId, workId)

        @TargetApi(26)
        @Throws(RemoteException::class)
        override fun dequeueWork(jobId: Int): JobWorkItem? = callback.dequeueWork(jobId)

        @Throws(RemoteException::class)
        override fun jobFinished(jobId: Int, reschedule: Boolean) {
            try {
                callback.jobFinished(jobId, reschedule)
            } catch (_: Throwable) {
            }
        }

        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val jobService = IJobService.Stub.asInterface(binder)
            service = jobService
            if (jobService == null) {
                finishJobImmediately(callback, jobId)
                release()
                return
            }
            try {
                jobService.startJob(parameters)
            } catch (_: RemoteException) {
                try {
                    callback.jobFinished(jobId, false)
                } catch (_: RemoteException) {
                } finally {
                    release()
                }
            }
        }

        /** was appendFirstIfNotNull: stops the clone's job service, forgets this job and unbinds */
        fun release() {
            val jobService = service
            if (jobService != null) {
                try {
                    jobService.stopJob(parameters)
                } catch (_: RemoteException) {
                }
            }
            jobCallbacks.delete(jobId)
            unbindService(this)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    companion object {
        private const val TAG = "StubJob"

        /** was Stub(StubJob, callback, id): tells the system's callback the job started and ended; Stub dead callback is ignored */
        @JvmStatic
        fun finishJobImmediately(callback: IJobCallback, jobId: Int) {
            try {
                callback.acknowledgeStartMessage(jobId, false)
                callback.jobFinished(jobId, false)
            } catch (_: RemoteException) {
            }
        }
    }
}
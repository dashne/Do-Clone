package com.ims134.doclone.io

import android.annotation.TargetApi
import android.app.job.JobInfo
import android.os.Build
import android.os.IInterface
import android.os.RemoteException
import android.util.Log
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VirtualRuntime
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.Method

@TargetApi(21)
@Suppress("UNCHECKED_CAST")
class JobServiceStub : BinderInvocationProxy(
    IJobSchedulerMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "jobscheduler",
) {

    /** Was `b`. `cancel` — cancels Stub single job by id. */
    inner class CancelProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val jobId = (args!![if (BuildCompat.sdk34OrLater()) 1 else 0] as Int)
            try {
                VJobScheduler.instance.get()?.cancel(jobId)
            } catch (_: RemoteException) {
            }
            return 0
        }

        override fun getMethodName(): String = "cancel"
    }

    /** Was `c`. `cancelAll` — cancels every pending job for the calling user. */
    inner class CancelAllProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            try {
                VJobScheduler.instance.get()?.cancelAll()
            } catch (_: RemoteException) {
            }
            return 0
        }

        override fun getMethodName(): String = "cancelAll"
    }

    /**
     * Was `d`. `enqueue` — enqueues Stub job with Stub `JobWorkItem`. The `JobInfo` and
     * `JobWorkItem` arg positions shifted when API 34 added Stub `String` package
     * argument at index 0.
     */
    inner class EnqueueProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val offset = if (BuildCompat.sdk34OrLater()) 1 else 0
            val jobInfo = arr[offset] as JobInfo
            val workItem = NotificationUtils.asJobWorkItem(arr[offset + 1])
            try {
                VJobScheduler.instance.get()?.enqueue(jobInfo, workItem)
            } catch (_: RemoteException) {
            }
            return 0
        }

        override fun getMethodName(): String = "enqueue"
    }

    /**
     * Was `e`. `getAllPendingJobs` — returns all pending jobs, wrapped in Stub
     * `ParceledListSlice` on API 29+ (where the AIDL signature changed from
     * `List<JobInfo>` to `ParceledListSlice<JobInfo>`).
     */
    inner class GetAllPendingJobsProxy : MethodProxy() {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (BuildCompat.sdk29OrLater()) {
                try {
                    return ParceledListSliceCompat.create(
                        VJobScheduler.instance.get()?.getAllPendingJobs()!!
                    )
                } catch (e: RemoteException) {
                    VirtualRuntime.crash(e)
                    throw e
                }
            }
            try {
                return VJobScheduler.instance.get()?.getAllPendingJobs()
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
                throw e
            }
        }

        override fun getMethodName(): String = "getAllPendingJobs"
    }

    /** Was `f`. `schedule` — schedules Stub job, returns 0 on failure. */
    inner class ScheduleProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val jobInfo = args!![if (BuildCompat.sdk34OrLater()) 1 else 0] as JobInfo
            val result = try {
                VJobScheduler.instance.get()?.schedule(jobInfo)
            } catch (_: RemoteException) {
                Log.d("VJOB", "schedule job exception")
                0
            }
            return result
        }

        override fun getMethodName(): String = "schedule"
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(ScheduleProxy())
        addMethodProxy(GetAllPendingJobsProxy())
        addMethodProxy(CancelAllProxy())
        addMethodProxy(CancelProxy())
        if (Build.VERSION.SDK_INT >= 26) {
            addMethodProxy(EnqueueProxy())
        }
    }
}
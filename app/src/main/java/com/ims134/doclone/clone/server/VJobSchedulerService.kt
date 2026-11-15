package com.ims134.doclone.clone.server


import android.annotation.TargetApi
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.job.JobWorkItem
import android.content.ComponentName
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.os.PersistableBundle
import android.os.RemoteException
import android.text.TextUtils
import android.util.Log
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.VBinder
import com.ims134.doclone.clone.VEnvironment
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.io.IJobScheduler
import com.ims134.doclone.io.JobInfoMirror
import com.ims134.doclone.io.Singleton
import com.ims134.doclone.io.VASettings
import com.ims134.doclone.io.VJobScheduler
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * The clone's job scheduler. A clone app's job is scheduled in the real JobScheduler under Stub stub job service of the host;
 * [jobConfigs] remembers which (uid, package, job id) each real job index stands for, and is saved to job-list.ini.
 *
 * Unseen: IJobScheduler.Stub, io.Singleton (CreateInstance / b()), cm3 (mirror of JobInfo: jobId, service), VBinder, VEnvironment, VASettings.
 */
@TargetApi(21)
class VJobSchedulerService : IJobScheduler.Stub() {

    /** was Stub: the clone job of every real job index, by (uid, package, id) */
    private val jobConfigs = HashMap<JobId, JobConfig>()

    /**
     * was b (count): the index given to the next new job. Loading the file sets it to the highest saved index (not +1),
     * as in the original.
     */
    private var nextJobIndex = 0

    /** was c */
    private val jobScheduler: JobScheduler

    /** was d: the host's stub job service every clone job runs under */
    private val stubService: ComponentName

    /** was f's value class: what is saved per job: the real index, the clone's service class and the extras */
    class JobConfig() : Parcelable {
        /** was Stub */
        @JvmField
        var index = 0

        /** was b */
        @JvmField
        var className: String? = null

        /** was c */
        @JvmField
        var bundle: PersistableBundle? = null

        @Suppress("DEPRECATION")
        constructor(parcel: Parcel) : this() {
            index = parcel.readInt()
            className = parcel.readString()
            bundle = parcel.readParcelable(PersistableBundle::class.java.classLoader)
        }

        override fun describeContents(): Int = 0

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(index)
            parcel.writeString(className)
            parcel.writeParcelable(bundle, flags)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<JobConfig> = object : Parcelable.Creator<JobConfig> {
                override fun createFromParcel(parcel: Parcel): JobConfig = JobConfig(parcel)

                override fun newArray(size: Int): Array<JobConfig?> = arrayOfNulls(size)
            }
        }
    }

    /** the key of Stub clone job: the calling uid, the clone package of the service and the job id the app chose */
    class JobId() : Parcelable {
        /** was Stub: the caller's uid (from VBinder.getUidByPid) */
        @JvmField
        var uidByPid = 0

        /** was b */
        @JvmField
        var packageName: String? = null

        /** was c */
        @JvmField
        var id = 0

        constructor(parcel: Parcel) : this() {
            uidByPid = parcel.readInt()
            packageName = parcel.readString()
            id = parcel.readInt()
        }

        override fun describeContents(): Int = 0

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other != null && JobId::class.java == other.javaClass) {
                val jobId = other as JobId
                if (uidByPid == jobId.uidByPid && id == jobId.id && TextUtils.equals(packageName, jobId.packageName)) {
                    return true
                }
            }
            return false
        }

        override fun hashCode(): Int = ((uidByPid * 31 + (packageName?.hashCode() ?: 0)) * 31) + id

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(uidByPid)
            parcel.writeString(packageName)
            parcel.writeInt(id)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<JobId> = object : Parcelable.Creator<JobId> {
                override fun createFromParcel(parcel: Parcel): JobId = JobId(parcel)

                override fun newArray(size: Int): Array<JobId?> = arrayOfNulls(size)
            }
        }
    }

    /** was VJobSchedulerService$Stub */
    private class VJobSchedulerServiceSingleton : Singleton<VJobSchedulerService>() {
        override fun create(): VJobSchedulerService = VJobSchedulerService()
    }

    init {
        val core = VirtualCore.gCore
        jobScheduler = appContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        stubService = ComponentName(core.hostPackageName!!, VASettings.stubJobClassName)
        loadJobs(File(VEnvironment.getOrCreateAppSubSystemFile(), JOB_LIST_FILE))
    }

    /** reads job-list.ini; Stub bad or unreadable file is ignored */
    private fun loadJobs(file: File) {
        if (!file.exists()) {
            return
        }
        val parcel = Parcel.obtain()
        try {
            val length = file.length().toInt()
            val bytes = ByteArray(length)
            val read = FileInputStream(file).use { it.read(bytes) }
            if (read != length) {
                Log.e(TAG, "Unable to read job config.")
                return
            }
            parcel.unmarshall(bytes, 0, length)
            parcel.setDataPosition(0)
            val version = parcel.readInt()
            if (version != FILE_VERSION) {
                Log.e(TAG, "Bad version of job file: $version")
                return
            }
            if (jobConfigs.isNotEmpty()) {
                jobConfigs.clear()
            }
            val size = parcel.readInt()
            for (i in 0 until size) {
                val jobId = JobId(parcel)
                val jobConfig = JobConfig(parcel)
                jobConfigs[jobId] = jobConfig
                nextJobIndex = maxOf(nextJobIndex, jobConfig.index)
            }
        } catch (_: Exception) {
        } finally {
            parcel.recycle()
        }
    }

    /** was cloneChannelWithNewId: records the clone job and turns [jobInfo] into one that runs the host's stub service under the real job index */
    private fun redirectJobToStub(jobInfo: JobInfo) {
        val service = jobInfo.service
        val jobId = JobId()
        jobId.uidByPid = VBinder.getUidByPid()
        jobId.packageName = service.packageName
        jobId.id = jobInfo.id
        var jobConfig = jobConfigs[jobId]
        if (jobConfig == null) {
            jobConfig = JobConfig()
            jobConfig.index = nextJobIndex++
            jobConfig.className = service.className
            jobConfig.bundle = jobInfo.extras
            jobConfigs[jobId] = jobConfig
        } else {
            jobConfig.className = service.className
            jobConfig.bundle = jobInfo.extras
        }
        saveJobs()
        JobInfoMirror.jobId!!.set(jobInfo, jobConfig.index)
        JobInfoMirror.service!!.set(jobInfo, stubService)
    }

    /** was U: writes job-list.ini; failures are ignored */
    private fun saveJobs() {
        val file = File(VEnvironment.getOrCreateAppSubSystemFile(), JOB_LIST_FILE)
        val parcel = Parcel.obtain()
        try {
            parcel.writeInt(FILE_VERSION)
            parcel.writeInt(jobConfigs.size)
            for ((jobId, jobConfig) in jobConfigs) {
                jobId.writeToParcel(parcel, 0)
                jobConfig.writeToParcel(parcel, 0)
            }
            FileOutputStream(file).use { it.write(parcel.marshall()) }
        } catch (_: Exception) {
        } finally {
            parcel.recycle()
        }
    }

    @Throws(RemoteException::class)
    override fun cancel(jobId: Int) {
        val uid = VBinder.getUidByPid()
        synchronized(jobConfigs) {
            val it = jobConfigs.entries.iterator()
            while (it.hasNext()) {
                val entry = it.next()
                if (entry.key.uidByPid == uid && entry.key.id == jobId) {
                    jobScheduler.cancel(entry.value.index)
                    it.remove()
                    saveJobs()
                    break
                }
            }
        }
    }

    @Throws(RemoteException::class)
    override fun cancelAll() {
        cancelJobs(VBinder.getUidByPid(), -1, null)
    }

    @TargetApi(26)
    @Throws(RemoteException::class)
    override fun enqueue(jobInfo: JobInfo?, jobWorkItem: JobWorkItem?): Int {
        redirectJobToStub(jobInfo!!)
        return jobScheduler.enqueue(jobInfo, jobWorkItem!!)
    }

    /** the clone job whose real job index is [index] */
    fun findJobByVirtualJobId(index: Int): Map.Entry<JobId, JobConfig>? {
        synchronized(jobConfigs) {
            for (entry in jobConfigs.entries) {
                if (entry.value.index == index) {
                    return entry
                }
            }
            return null
        }
    }

    /** the real pending jobs of the host, reduced to the calling clone app's own, with its own ids and service names */
    @Throws(RemoteException::class)
    override fun getAllPendingJobs(): List<JobInfo> {
        val uid = VBinder.getUidByPid()
        val pending = jobScheduler.allPendingJobs
        synchronized(jobConfigs) {
            val it = pending.listIterator()
            while (it.hasNext()) {
                val next = it.next()
                if (VASettings.stubJobClassName == next.service.className) {
                    val entry = findJobByVirtualJobId(next.id)
                    if (entry == null) {
                        it.remove()
                    } else {
                        val key = entry.key
                        val value = entry.value
                        if (key.uidByPid != uid) {
                            it.remove()
                        } else {
                            JobInfoMirror.jobId!!.set(next, key.id)
                            JobInfoMirror.service!!.set(next, ComponentName(key.packageName!!, value.className!!))
                        }
                    }
                } else {
                    it.remove()
                }
            }
        }
        return pending
    }

    @Throws(RemoteException::class)
    override fun schedule(jobInfo: JobInfo?): Int {
        redirectJobToStub(jobInfo!!)
        return try {
            jobScheduler.schedule(jobInfo)
        } catch (_: Exception) {
            0
        }
    }

    /**
     * was appendFirstIfNotNull: cancels the clone jobs of [uid] (-10000 = none by uid) and those of [packageName] in virtual user [userId];
     * returns how many.
     */
    private fun cancelJobs(uid: Int, userId: Int, packageName: String?): Int {
        var cancelled = 0
        synchronized(jobConfigs) {
            val it = jobConfigs.entries.iterator()
            var changed = false
            while (it.hasNext()) {
                val entry = it.next()
                val jobId = entry.key
                val byUid = uid != -10000 && jobId.uidByPid == uid
                val byPackage = packageName != null && TextUtils.equals(packageName, jobId.packageName) &&
                        jobId.uidByPid / VUserHandle.PER_USER_RANGE == userId
                if (byUid || byPackage) {
                    jobScheduler.cancel(entry.value.index)
                    cancelled++
                    it.remove()
                    changed = true
                }
            }
            if (changed) {
                saveJobs()
            }
        }
        return cancelled
    }

    /** cancels the jobs of Stub clone package in Stub virtual user */
    fun cancelAll(packageName: String?, userId: Int): Int = cancelJobs(-10000, userId, packageName)

    companion object {
        /** was e */
        private val TAG = VJobScheduler::class.java.simpleName

        private const val JOB_LIST_FILE = "job-list.ini"
        private const val FILE_VERSION = 1

        /** was f */
        private val jobSchedulerServiceInstance: Singleton<VJobSchedulerService> = VJobSchedulerServiceSingleton()

        @JvmStatic
        fun get(): VJobSchedulerService? = jobSchedulerServiceInstance.get()
    }
}
package com.ims134.doclone.io

import android.app.job.JobInfo
import android.app.job.JobWorkItem
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException
import com.ims134.doclone.io.NotificationUtils.asJobWorkItem


/* JADX INFO: renamed from: io.n43 */ /* JADX INFO: compiled from: IJobScheduler.java */ /* JADX INFO: loaded from: classes2.dex */
interface IJobScheduler : IInterface {

    companion object
    {
        const val DESCRIPTOR = "com.polestar.clone.server.IJobScheduler"
    }


    /* JADX INFO: renamed from: io.n43$c */ /* JADX INFO: compiled from: IJobScheduler.java */
    object c {
        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        fun <T : Parcelable?> a(parcel: Parcel, t: T?, i: Int) {
            if (t == null) {
                parcel.writeInt(0)
            } else {
                parcel.writeInt(1)
                t.writeToParcel(parcel, i)
            }
        }
    }

    @Throws(RemoteException::class)
    fun cancel(i: Int)

    @Throws(RemoteException::class)
    fun cancelAll()

    @Throws(RemoteException::class)
    fun enqueue(jobInfo: JobInfo?, jobWorkItem: JobWorkItem?): Int


    @Throws(RemoteException::class)
    fun schedule(jobInfo: JobInfo?): Int

    @Throws(RemoteException::class)
    fun getAllPendingJobs(): List<JobInfo?>?

    /* JADX INFO: renamed from: io.n43$Default */ /* JADX INFO: compiled from: IJobScheduler.java */
    class Default() : IJobScheduler {
        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        // android.os.IInterface
        override fun asBinder(): IBinder? {
            return null
        }

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        @Throws(RemoteException::class)  // io.IJobScheduler
        override fun enqueue(jobInfo: JobInfo?, jobWorkItem: JobWorkItem?): Int {
            return 0
        }

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        @Throws(RemoteException::class)  // io.IJobScheduler
        override fun getAllPendingJobs(): MutableList<JobInfo?>? {
            return null
        }

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        @Throws(RemoteException::class)  // io.IJobScheduler
        override fun schedule(jobInfo: JobInfo?): Int {
            return 0
        }

        @Throws(RemoteException::class)  // io.IJobScheduler
        override fun cancelAll() {
        }

        @Throws(RemoteException::class)  // io.IJobScheduler
        override fun cancel(i: Int) {
        }
    }

    /* JADX INFO: renamed from: io.n43$b */ /* JADX INFO: compiled from: IJobScheduler.java */
    abstract class Stub : Binder(), IJobScheduler {
        /* JADX INFO: renamed from: io.n43$b$Default */ /* JADX INFO: compiled from: IJobScheduler.java */
        class Proxy() : IJobScheduler {
            /* JADX INFO: renamed from: Default */
            var binder: IBinder? = null

            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
            // android.os.IInterface
            override fun asBinder(): IBinder {
                return this.binder!!
            }

            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
            @Throws(RemoteException::class)  // io.IJobScheduler
            override fun cancel(i: Int) {
                val parcelObtain = Parcel.obtain()
                val parcelObtain2 = Parcel.obtain()
                try {
                    parcelObtain.writeInterfaceToken(DESCRIPTOR)
                    parcelObtain.writeInt(i)
                    this.binder!!.transact(3, parcelObtain, parcelObtain2, 0)
                    parcelObtain2.readException()
                } finally {
                    parcelObtain2.recycle()
                    parcelObtain.recycle()
                }
            }

            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
            @Throws(RemoteException::class)  // io.IJobScheduler
            override fun cancelAll() {
                val parcelObtain = Parcel.obtain()
                val parcelObtain2 = Parcel.obtain()
                try {
                    parcelObtain.writeInterfaceToken(DESCRIPTOR)
                    this.binder!!.transact(4, parcelObtain, parcelObtain2, 0)
                    parcelObtain2.readException()
                } finally {
                    parcelObtain2.recycle()
                    parcelObtain.recycle()
                }
            }

            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
            @Throws(RemoteException::class)  // io.IJobScheduler
            override fun enqueue(jobInfo: JobInfo?, jobWorkItem: JobWorkItem?): Int {
                val parcelObtain = Parcel.obtain()
                val parcelObtain2 = Parcel.obtain()
                try {
                    parcelObtain.writeInterfaceToken(DESCRIPTOR)
                    c.a<JobInfo?>(parcelObtain, jobInfo, 0)
                    c.a<JobWorkItem?>(parcelObtain, jobWorkItem, 0)
                    this.binder!!.transact(2, parcelObtain, parcelObtain2, 0)
                    parcelObtain2.readException()
                    return parcelObtain2.readInt()
                } finally {
                    parcelObtain2.recycle()
                    parcelObtain.recycle()
                }
            }





            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
            @Throws(RemoteException::class)  // io.IJobScheduler
            override fun schedule(jobInfo: JobInfo?): Int {
                val parcelObtain = Parcel.obtain()
                val parcelObtain2 = Parcel.obtain()
                try {
                    parcelObtain.writeInterfaceToken(DESCRIPTOR)
                    c.a<JobInfo?>(parcelObtain, jobInfo, 0)
                    this.binder!!.transact(1, parcelObtain, parcelObtain2, 0)
                    parcelObtain2.readException()
                    return parcelObtain2.readInt()
                } finally {
                    parcelObtain2.recycle()
                    parcelObtain.recycle()
                }
            }


            @Throws(RemoteException::class)
            override fun getAllPendingJobs(): List<JobInfo?>? {
                val parcelObtain = Parcel.obtain()
                val parcelObtain2 = Parcel.obtain()
                try {
                    parcelObtain.writeInterfaceToken(DESCRIPTOR)
                    this.binder!!.transact(5, parcelObtain, parcelObtain2, 0)
                    parcelObtain2.readException()
                    return parcelObtain2.createTypedArrayList<JobInfo?>(JobInfo.CREATOR)
                } finally {
                    parcelObtain2.recycle()
                    parcelObtain.recycle()
                }
            }


        }

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        init {
            attachInterface(this, DESCRIPTOR)
        }

        @Throws(RemoteException::class)  // io.IJobScheduler
        abstract override fun cancel(i: Int)

        @Throws(RemoteException::class)  // io.IJobScheduler
        abstract override fun cancelAll()

        @Throws(RemoteException::class)  // io.IJobScheduler
        abstract override fun enqueue(
            jobInfo: JobInfo?,
            jobWorkItem: JobWorkItem?
        ): Int

        @Throws(RemoteException::class)  // io.IJobScheduler
        abstract override fun getAllPendingJobs(): List<JobInfo?>?

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        @Throws(RemoteException::class)  // android.os.Binder
        public override fun onTransact(
            code: Int,
            data: Parcel,
            reply: Parcel?,
            flag: Int
        ): Boolean {
            if (code >= 1 && code <= 16777215) {
                data.enforceInterface(DESCRIPTOR)
            }
            if (code == 1598968902) {
                reply?.writeString(DESCRIPTOR)
                return true
            }
            if (code == 1) {
                val iSchedule =
                    schedule((if (data.readInt() != 0) JobInfo.CREATOR.createFromParcel(data) else null))
                reply?.writeNoException()
                reply?.writeInt(iSchedule)
                return true
            }
            if (code == 2) {
                val iEnqueue = enqueue(
                    (if (data.readInt() != 0) JobInfo.CREATOR.createFromParcel(data) else null),
                    asJobWorkItem(if (data.readInt() != 0) JobWorkItem.CREATOR.createFromParcel(data) else null)
                )
                reply?.writeNoException()
                reply?.writeInt(iEnqueue)
                return true
            }
            if (code == 3) {
                cancel(data.readInt())
                reply?.writeNoException()
                return true
            }
            if (code == 4) {
                cancelAll()
                reply?.writeNoException()
                return true
            }
            if (code != 5) {
                return super.onTransact(code, data, reply, flag)
            }
            val allPendingJobs = getAllPendingJobs()
            reply?.writeNoException()
            if (allPendingJobs == null) {
                reply?.writeInt(-1)
                return true
            }
            val size = allPendingJobs.size
            reply?.writeInt(size)
            for (i in 0..<size) {
                c.a<Parcelable?>(reply!!, allPendingJobs.get(i) as Parcelable?, 1)
            }
            return true
        }

        @Throws(RemoteException::class)  // io.IJobScheduler
        abstract override fun schedule(jobInfo: JobInfo?): Int

        // android.os.IInterface
        override fun asBinder(): IBinder {
            return this
        }

        companion object {
            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
            fun asInterface(binder: IBinder?): IJobScheduler? {
                if (binder == null) {
                    return null
                }
                val l = binder.queryLocalInterface(DESCRIPTOR)
                if (l != null && (l is IJobScheduler)) {
                    return l
                }
                val proxy = Proxy()
                proxy.binder = binder
                return proxy

            }
        }
    }

}
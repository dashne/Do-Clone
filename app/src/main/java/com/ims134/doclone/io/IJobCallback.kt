package com.ims134.doclone.io


import android.app.job.JobWorkItem
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException

/** AIDL interface of the callback Default JobService gets from the system's JobScheduler (hidden android.app.job.IJobCallback). */
interface IJobCallback : IInterface {

    /** parcel helpers of the generated AIDL code */
    object _Parcel {
        fun <T> readTypedObject(parcel: Parcel, creator: Parcelable.Creator<T>): T? =
            if (parcel.readInt() != 0) creator.createFromParcel(parcel) else null

        fun <T : Parcelable> writeTypedObject(parcel: Parcel, value: T?, flags: Int) {
            if (value == null) {
                parcel.writeInt(0)
            } else {
                parcel.writeInt(1)
                value.writeToParcel(parcel, flags)
            }
        }
    }

    @Throws(RemoteException::class)
    fun acknowledgeStartMessage(jobId: Int, ongoing: Boolean)

    @Throws(RemoteException::class)
    fun acknowledgeStopMessage(jobId: Int, reschedule: Boolean)

    @Throws(RemoteException::class)
    fun completeWork(jobId: Int, workId: Int): Boolean

    @Throws(RemoteException::class)
    fun dequeueWork(jobId: Int): JobWorkItem?

    @Throws(RemoteException::class)
    fun jobFinished(jobId: Int, reschedule: Boolean)

    abstract class Stub : Binder(), IJobCallback {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        @Throws(RemoteException::class)
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code in 1..16777215) {
                data.enforceInterface(DESCRIPTOR)
            }
            if (code == INTERFACE_TRANSACTION) {
                reply!!.writeString(DESCRIPTOR)
                return true
            }
            when (code) {
                TRANSACTION_acknowledgeStartMessage -> {
                    acknowledgeStartMessage(data.readInt(), data.readInt() != 0)
                    reply!!.writeNoException()
                }
                TRANSACTION_acknowledgeStopMessage -> {
                    acknowledgeStopMessage(data.readInt(), data.readInt() != 0)
                    reply!!.writeNoException()
                }
                TRANSACTION_dequeueWork -> {
                    val work = dequeueWork(data.readInt())
                    reply!!.writeNoException()
                    _Parcel.writeTypedObject(reply, work, 1)
                }
                TRANSACTION_completeWork -> {
                    val done = completeWork(data.readInt(), data.readInt())
                    reply!!.writeNoException()
                    reply.writeInt(if (done) 1 else 0)
                }
                TRANSACTION_jobFinished -> {
                    jobFinished(data.readInt(), data.readInt() != 0)
                    reply!!.writeNoException()
                }
                else -> return super.onTransact(code, data, reply, flags)
            }
            return true
        }

        class Proxy(private val remote: IBinder) : IJobCallback {
            override fun asBinder(): IBinder = remote

            fun getInterfaceDescriptor(): String = DESCRIPTOR

            @Throws(RemoteException::class)
            override fun acknowledgeStartMessage(jobId: Int, ongoing: Boolean) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(jobId)
                    data.writeInt(if (ongoing) 1 else 0)
                    remote.transact(TRANSACTION_acknowledgeStartMessage, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun acknowledgeStopMessage(jobId: Int, reschedule: Boolean) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(jobId)
                    data.writeInt(if (reschedule) 1 else 0)
                    remote.transact(TRANSACTION_acknowledgeStopMessage, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun completeWork(jobId: Int, workId: Int): Boolean {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(jobId)
                    data.writeInt(workId)
                    remote.transact(TRANSACTION_completeWork, data, reply, 0)
                    reply.readException()
                    return reply.readInt() != 0
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun dequeueWork(jobId: Int): JobWorkItem? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(jobId)
                    remote.transact(TRANSACTION_dequeueWork, data, reply, 0)
                    reply.readException()
                    return _Parcel.readTypedObject(reply, JobWorkItem.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun jobFinished(jobId: Int, reschedule: Boolean) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(jobId)
                    data.writeInt(if (reschedule) 1 else 0)
                    remote.transact(TRANSACTION_jobFinished, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }
        }

        companion object {
            const val DESCRIPTOR = "android.app.job.IJobCallback"
            const val TRANSACTION_acknowledgeStartMessage = 1
            const val TRANSACTION_acknowledgeStopMessage = 2
            const val TRANSACTION_dequeueWork = 3
            const val TRANSACTION_completeWork = 4
            const val TRANSACTION_jobFinished = 5

            @JvmStatic
            fun asInterface(binder: IBinder?): IJobCallback? {
                if (binder == null) return null
                val local = binder.queryLocalInterface(DESCRIPTOR)
                return if (local != null && local is IJobCallback) local else Proxy(binder)
            }
        }
    }

    open class Default : IJobCallback {
        override fun asBinder(): IBinder? = null

        override fun completeWork(jobId: Int, workId: Int): Boolean = false

        override fun dequeueWork(jobId: Int): JobWorkItem? = null

        @Throws(RemoteException::class)
        override fun acknowledgeStartMessage(jobId: Int, ongoing: Boolean) {
        }

        @Throws(RemoteException::class)
        override fun acknowledgeStopMessage(jobId: Int, reschedule: Boolean) {
        }

        @Throws(RemoteException::class)
        override fun jobFinished(jobId: Int, reschedule: Boolean) {
        }
    }
}
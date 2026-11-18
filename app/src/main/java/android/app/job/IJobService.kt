package android.app.job

import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException

interface IJobService : IInterface {

    @Throws(RemoteException::class)
    fun startJob(jobParameters: JobParameters?)

    @Throws(RemoteException::class)
    fun stopJob(jobParameters: JobParameters?)

    abstract class Stub : Binder(), IJobService {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        @Throws(RemoteException::class)
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code in 1..0xFFFFFF) {
                data.enforceInterface(DESCRIPTOR)
            }
            if (code == INTERFACE_TRANSACTION) {            // 1598968902
                reply!!.writeString(DESCRIPTOR)
                return true
            }
            when (code) {
                TRANSACTION_startJob -> startJob(readTypedObject(data, JobParameters.CREATOR))
                TRANSACTION_stopJob -> stopJob(readTypedObject(data, JobParameters.CREATOR))
                else -> return super.onTransact(code, data, reply, flags)
            }
            return true
        }

        class Proxy(private val mRemote: IBinder) : IJobService {
            override fun asBinder(): IBinder = mRemote

            fun getInterfaceDescriptor(): String = DESCRIPTOR

            @Throws(RemoteException::class)
            override fun startJob(jobParameters: JobParameters?) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    writeTypedObject(data, jobParameters, 0)
                    mRemote.transact(TRANSACTION_startJob, data, null, FLAG_ONEWAY)
                } finally {
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun stopJob(jobParameters: JobParameters?) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    writeTypedObject(data, jobParameters, 0)
                    mRemote.transact(TRANSACTION_stopJob, data, null, FLAG_ONEWAY)
                } finally {
                    data.recycle()
                }
            }
        }

        companion object {
            const val DESCRIPTOR = "android.app.job.IJobService"
            const val TRANSACTION_startJob = 1
            const val TRANSACTION_stopJob = 2

            @JvmStatic
            fun asInterface(binder: IBinder?): IJobService? {
                if (binder == null) return null
                val local = binder.queryLocalInterface(DESCRIPTOR)
                return if (local != null && local is IJobService) local else Proxy(binder)
            }
        }
    }

    open class Default : IJobService {
        override fun asBinder(): IBinder? = null          // jadx returns null; see note
        override fun startJob(jobParameters: JobParameters?) {}
        override fun stopJob(jobParameters: JobParameters?) {}
    }
}

// jadx _Parcel helpers
private fun <T> readTypedObject(parcel: Parcel, creator: Parcelable.Creator<T>): T? =
    if (parcel.readInt() != 0) creator.createFromParcel(parcel) else null

private fun <T : Parcelable> writeTypedObject(parcel: Parcel, value: T?, flags: Int) {
    if (value == null) {
        parcel.writeInt(0)
    } else {
        parcel.writeInt(1)
        value.writeToParcel(parcel, flags)
    }
}
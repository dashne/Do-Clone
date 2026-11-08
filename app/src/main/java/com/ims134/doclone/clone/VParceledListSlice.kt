package com.ims134.doclone.clone


import android.os.Binder
import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException
import android.util.Log

/**
 * Copy of the framework's ParceledListSlice: sends setPosition long list of Parcelables across Binder
 * in chunks, so the transaction does not exceed the IPC size limit.
 */
class VParceledListSlice<T : Parcelable>(@JvmField val list: List<T>) : Parcelable {

    private constructor(parcel: Parcel, loader: ClassLoader?) : this(readList<T>(parcel, loader))

    override fun describeContents(): Int = list.fold(0) { acc, item -> acc or item.describeContents() }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        val size = list.size
        parcel.writeInt(size)
        if (size == 0) return

        val itemClass = list[0].javaClass
        var i = 0
        while (i < size && parcel.dataSize() < MAX_FIRST_IPC_SIZE) {
            parcel.writeInt(1)
            val item = list[i]
            checkSameType(itemClass, item.javaClass)
            parcel.writeParcelable(item, flags)
            i++
        }
        if (i < size) {
            // The rest is fetched lazily by the reader through this binder.
            parcel.writeInt(0)
            parcel.writeStrongBinder(ChunkBinder(size, itemClass, flags))
        }
    }

    /** Serves the items that did not fit in the first parcel. Was BinderC6478b. */
    private inner class ChunkBinder(
        private val count: Int,
        private val itemClass: Class<*>,
        private val writeFlags: Int,
    ) : Binder() {

        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code != TRANSACTION_GET_CHUNK || reply == null) return super.onTransact(code, data, reply, flags)

            var i = data.readInt()
            while (i < count && reply.dataSize() < MAX_IPC_SIZE) {
                reply.writeInt(1)
                val item = list[i]
                checkSameType(itemClass, item.javaClass)
                reply.writeParcelable(item, writeFlags)
                i++
            }
            if (i < count) reply.writeInt(0)
            return true
        }
    }

    companion object {
        private const val TAG = "ParceledListSlice"
        private const val TRANSACTION_GET_CHUNK = 1
        private const val MAX_IPC_SIZE = 262144        // 256 KB
        private const val MAX_FIRST_IPC_SIZE = 131072  // 128 KB

        /** Was m16410a(). */
        private fun checkSameType(expected: Class<*>, actual: Class<*>) {
            if (actual != expected) {
                throw IllegalArgumentException("Can't unparcel type ${actual.name} in list of type ${expected.name}")
            }
        }

        private fun <T : Parcelable> readList(parcel: Parcel, loader: ClassLoader?): List<T> {
            val count = parcel.readInt()
            val result = ArrayList<T>(count)
            if (count <= 0) return result

            var itemClass: Class<*>? = null
            var i = 0
            while (i < count && parcel.readInt() != 0) {
                val item = parcel.readParcelable<Parcelable>(loader)!!
                if (itemClass == null) itemClass = item.javaClass else checkSameType(itemClass, item.javaClass)
                result.add(item as T)
                i++
            }
            if (i >= count) return result

            // Remaining items come from the sender's ChunkBinder.
            val binder: IBinder = parcel.readStrongBinder()
            while (i < count) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                data.writeInt(i)
                try {
                    binder.transact(TRANSACTION_GET_CHUNK, data, reply, 0)
                    while (i < count && reply.readInt() != 0) {
                        val item = reply.readParcelable<Parcelable>(loader)!!
                        checkSameType(itemClass!!, item.javaClass)
                        result.add(item as T)
                        i++
                    }
                } catch (e: RemoteException) {
                    Log.w(TAG, "Failure retrieving array; only received $i of $count", e)
                    return result
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }
            return result
        }

        @JvmField
        val CREATOR: Parcelable.ClassLoaderCreator<VParceledListSlice<*>> =
            object : Parcelable.ClassLoaderCreator<VParceledListSlice<*>> {
                override fun createFromParcel(parcel: Parcel): VParceledListSlice<*> =
                    VParceledListSlice<Parcelable>(parcel, null)

                override fun createFromParcel(parcel: Parcel, classLoader: ClassLoader?): VParceledListSlice<*> =
                    VParceledListSlice<Parcelable>(parcel, classLoader)

                override fun newArray(size: Int): Array<VParceledListSlice<*>?> = arrayOfNulls(size)
            }
    }
}
package com.ims134.doclone.io

import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException
import com.ims134.doclone.clone.VCell
import com.ims134.doclone.clone.VLocation


/**
 * Binder interface of the "virtual-loc" service: the fake location and cell towers per app and user, plus global
 * ones (was io.y63, "IVirtualLocationManager.java"). R8 trimmed the interface and Proxy to the five getters
 * that the client uses; the setters are rebuilt from the Stub's abstract methods and onTransact.
 *
 * Mode values are not shown in the code; 0 means "no virtual location" (see MethodProxy.isFakeLocationEnable).
 */
interface IVirtualLocationManager : IInterface {

    @Throws(RemoteException::class)
    fun getMode(userId: Int, packageName: String?): Int

    @Throws(RemoteException::class)
    fun setMode(userId: Int, packageName: String?, mode: Int)

    @Throws(RemoteException::class)
    fun setCell(userId: Int, packageName: String?, cell: VCell?)

    @Throws(RemoteException::class)
    fun setAllCell(userId: Int, packageName: String?, cells: List<VCell>?)

    @Throws(RemoteException::class)
    fun setNeighboringCell(userId: Int, packageName: String?, cells: List<VCell>?)

    @Throws(RemoteException::class)
    fun setGlobalCell(cell: VCell?)

    @Throws(RemoteException::class)
    fun setGlobalAllCell(cells: List<VCell>?)

    @Throws(RemoteException::class)
    fun setGlobalNeighboringCell(cells: List<VCell>?)

    @Throws(RemoteException::class)
    fun getCell(userId: Int, packageName: String?): VCell?

    @Throws(RemoteException::class)
    fun getAllCell(userId: Int, packageName: String?): List<VCell>?

    @Throws(RemoteException::class)
    fun getNeighboringCell(userId: Int, packageName: String?): List<VCell>?

    @Throws(RemoteException::class)
    fun setLocation(userId: Int, packageName: String?, location: VLocation?)

    @Throws(RemoteException::class)
    fun getLocation(userId: Int, packageName: String?): VLocation?

    @Throws(RemoteException::class)
    fun setGlobalLocation(location: VLocation?)

    @Throws(RemoteException::class)
    fun getGlobalLocation(): VLocation?

    abstract class Stub : Binder(), IVirtualLocationManager {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        public override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code in 1..16777215) {
                data.enforceInterface(DESCRIPTOR)
            }
            if (code == 1598968902) {
                reply?.writeString(DESCRIPTOR)
                return true
            }
            when (code) {
                TRANSACTION_getMode -> {
                    val mode = getMode(data.readInt(), data.readString())
                    reply?.writeNoException()
                    reply?.writeInt(mode)
                }
                TRANSACTION_setMode -> {
                    setMode(data.readInt(), data.readString(), data.readInt())
                    reply?.writeNoException()
                }
                TRANSACTION_setCell -> {
                    setCell(data.readInt(), data.readString(), data.readTypedObject(VCell.CREATOR))
                    reply?.writeNoException()
                }
                TRANSACTION_setAllCell -> {
                    setAllCell(data.readInt(), data.readString(), data.createTypedArrayList(VCell.CREATOR))
                    reply?.writeNoException()
                }
                TRANSACTION_setNeighboringCell -> {
                    setNeighboringCell(data.readInt(), data.readString(), data.createTypedArrayList(VCell.CREATOR))
                    reply?.writeNoException()
                }
                TRANSACTION_setGlobalCell -> {
                    setGlobalCell(data.readTypedObject(VCell.CREATOR))
                    reply?.writeNoException()
                }
                TRANSACTION_setGlobalAllCell -> {
                    setGlobalAllCell(data.createTypedArrayList(VCell.CREATOR))
                    reply?.writeNoException()
                }
                TRANSACTION_setGlobalNeighboringCell -> {
                    setGlobalNeighboringCell(data.createTypedArrayList(VCell.CREATOR))
                    reply?.writeNoException()
                }
                TRANSACTION_getCell -> {
                    val cell = getCell(data.readInt(), data.readString())
                    reply?.writeNoException()
                    reply?.writeTypedObject(cell, PARCELABLE_WRITE_RETURN_VALUE)
                }
                TRANSACTION_getAllCell -> {
                    val cells = getAllCell(data.readInt(), data.readString())
                    reply?.writeNoException()
                    reply?.writeTypedList(cells)
                }
                TRANSACTION_getNeighboringCell -> {
                    val cells = getNeighboringCell(data.readInt(), data.readString())
                    reply?.writeNoException()
                    reply?.writeTypedList(cells)
                }
                TRANSACTION_setLocation -> {
                    setLocation(data.readInt(), data.readString(), data.readTypedObject(VLocation.CREATOR))
                    reply?.writeNoException()
                }
                TRANSACTION_getLocation -> {
                    val location = getLocation(data.readInt(), data.readString())
                    reply?.writeNoException()
                    reply?.writeTypedObject(location, PARCELABLE_WRITE_RETURN_VALUE)
                }
                TRANSACTION_setGlobalLocation -> {
                    setGlobalLocation(data.readTypedObject(VLocation.CREATOR))
                    reply?.writeNoException()
                }
                TRANSACTION_getGlobalLocation -> {
                    val location = getGlobalLocation()
                    reply?.writeNoException()
                    reply?.writeTypedObject(location, PARCELABLE_WRITE_RETURN_VALUE)
                }
                else -> return super.onTransact(code, data, reply, flags)
            }
            return true
        }

        private class Proxy(private val remote: IBinder) : IVirtualLocationManager {

            override fun asBinder(): IBinder = remote

            /** transacts [code]; [write] fills the request, [read] reads the reply after the exception check */
            private inline fun <R> call(code: Int, write: (Parcel) -> Unit, read: (Parcel) -> R): R {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    write(data)
                    remote.transact(code, data, reply, 0)
                    reply.readException()
                    return read(reply)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            override fun getMode(userId: Int, packageName: String?): Int =
                call(TRANSACTION_getMode, { it.writeInt(userId); it.writeString(packageName) }) { it.readInt() }

            override fun setMode(userId: Int, packageName: String?, mode: Int) =
                call(TRANSACTION_setMode, { it.writeInt(userId); it.writeString(packageName); it.writeInt(mode) }) { }

            override fun setCell(userId: Int, packageName: String?, cell: VCell?) =
                call(TRANSACTION_setCell, { it.writeInt(userId); it.writeString(packageName); it.writeTypedObject(cell, 0) }) { }

            override fun setAllCell(userId: Int, packageName: String?, cells: List<VCell>?) =
                call(TRANSACTION_setAllCell, { it.writeInt(userId); it.writeString(packageName); it.writeTypedList(cells) }) { }

            override fun setNeighboringCell(userId: Int, packageName: String?, cells: List<VCell>?) =
                call(TRANSACTION_setNeighboringCell, { it.writeInt(userId); it.writeString(packageName); it.writeTypedList(cells) }) { }

            override fun setGlobalCell(cell: VCell?) =
                call(TRANSACTION_setGlobalCell, { it.writeTypedObject(cell, 0) }) { }

            override fun setGlobalAllCell(cells: List<VCell>?) =
                call(TRANSACTION_setGlobalAllCell, { it.writeTypedList(cells) }) { }

            override fun setGlobalNeighboringCell(cells: List<VCell>?) =
                call(TRANSACTION_setGlobalNeighboringCell, { it.writeTypedList(cells) }) { }

            override fun getCell(userId: Int, packageName: String?): VCell? =
                call(TRANSACTION_getCell, { it.writeInt(userId); it.writeString(packageName) }) { it.readTypedObject(VCell.CREATOR) }

            override fun getAllCell(userId: Int, packageName: String?): List<VCell>? =
                call(TRANSACTION_getAllCell, { it.writeInt(userId); it.writeString(packageName) }) { it.createTypedArrayList(VCell.CREATOR) }

            override fun getNeighboringCell(userId: Int, packageName: String?): List<VCell>? =
                call(TRANSACTION_getNeighboringCell, { it.writeInt(userId); it.writeString(packageName) }) { it.createTypedArrayList(VCell.CREATOR) }

            override fun setLocation(userId: Int, packageName: String?, location: VLocation?) =
                call(TRANSACTION_setLocation, { it.writeInt(userId); it.writeString(packageName); it.writeTypedObject(location, 0) }) { }

            override fun getLocation(userId: Int, packageName: String?): VLocation? =
                call(TRANSACTION_getLocation, { it.writeInt(userId); it.writeString(packageName) }) { it.readTypedObject(VLocation.CREATOR) }

            override fun setGlobalLocation(location: VLocation?) =
                call(TRANSACTION_setGlobalLocation, { it.writeTypedObject(location, 0) }) { }

            override fun getGlobalLocation(): VLocation? =
                call(TRANSACTION_getGlobalLocation, { }) { it.readTypedObject(VLocation.CREATOR) }
        }

        companion object {
            const val DESCRIPTOR = "com.polestar.clone.server.IVirtualLocationManager"

            const val TRANSACTION_getMode = 1
            const val TRANSACTION_setMode = 2
            const val TRANSACTION_setCell = 3
            const val TRANSACTION_setAllCell = 4
            const val TRANSACTION_setNeighboringCell = 5
            const val TRANSACTION_setGlobalCell = 6
            const val TRANSACTION_setGlobalAllCell = 7
            const val TRANSACTION_setGlobalNeighboringCell = 8
            const val TRANSACTION_getCell = 9
            const val TRANSACTION_getAllCell = 10
            const val TRANSACTION_getNeighboringCell = 11
            const val TRANSACTION_setLocation = 12
            const val TRANSACTION_getLocation = 13
            const val TRANSACTION_setGlobalLocation = 14
            const val TRANSACTION_getGlobalLocation = 15

            /** the original wrote returned parcelables with writeToParcel(parcel, 1) = PARCELABLE_WRITE_RETURN_VALUE */
            private const val PARCELABLE_WRITE_RETURN_VALUE = android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE

            @JvmStatic
            fun asInterface(binder: IBinder?): IVirtualLocationManager? {
                if (binder == null) return null
                val local = binder.queryLocalInterface(DESCRIPTOR)
                if (local is IVirtualLocationManager) return local
                return Proxy(binder)
            }
        }
    }

    /** was y63$get: does nothing, returns nothing */
    class Default : IVirtualLocationManager {
        override fun asBinder(): IBinder? = null
        override fun getMode(userId: Int, packageName: String?): Int = 0
        override fun setMode(userId: Int, packageName: String?, mode: Int) {}
        override fun setCell(userId: Int, packageName: String?, cell: VCell?) {}
        override fun setAllCell(userId: Int, packageName: String?, cells: List<VCell>?) {}
        override fun setNeighboringCell(userId: Int, packageName: String?, cells: List<VCell>?) {}
        override fun setGlobalCell(cell: VCell?) {}
        override fun setGlobalAllCell(cells: List<VCell>?) {}
        override fun setGlobalNeighboringCell(cells: List<VCell>?) {}
        override fun getCell(userId: Int, packageName: String?): VCell? = null
        override fun getAllCell(userId: Int, packageName: String?): List<VCell>? = null
        override fun getNeighboringCell(userId: Int, packageName: String?): List<VCell>? = null
        override fun setLocation(userId: Int, packageName: String?, location: VLocation?) {}
        override fun getLocation(userId: Int, packageName: String?): VLocation? = null
        override fun setGlobalLocation(location: VLocation?) {}
        override fun getGlobalLocation(): VLocation? = null
    }
}
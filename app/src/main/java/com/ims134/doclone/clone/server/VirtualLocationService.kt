package com.ims134.doclone.clone.server

import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException
import android.util.SparseArray
import androidx.core.util.size
import com.ims134.doclone.clone.VCell
import com.ims134.doclone.clone.VEnvironment.Companion.getOrCreateAppSubSystemFile
import com.ims134.doclone.clone.VLocation
import com.ims134.doclone.io.IVirtualLocationManager
import com.ims134.doclone.io.PersistenceLayer
import java.io.File

class VirtualLocationService : IVirtualLocationManager.Stub() {

    val locConfigBy = SparseArray<HashMap<String?, VLocConfig?>?>()
    val globalConfig = VLocConfig()
    val persistenceLayer: VLocPersistenceLayer

    class VLocConfig() : Parcelable {
        var mode: Int = 0
        var cell: VCell? = null
        var allCells: List<VCell>? = null
        var neighboringCells: List<VCell>? = null
        var location: VLocation? = null

        constructor(parcel: Parcel) : this() {
            mode = parcel.readInt()
            cell = parcel.readParcelable(VCell::class.java.classLoader)
            allCells = parcel.createTypedArrayList(VCell.CREATOR)
            neighboringCells = parcel.createTypedArrayList(VCell.CREATOR)
            location = parcel.readParcelable(VLocation::class.java.classLoader)
        }

        override fun describeContents(): Int = 0

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(mode)
            parcel.writeParcelable(cell, flags)
            parcel.writeTypedList(allCells)
            parcel.writeTypedList(neighboringCells)
            parcel.writeParcelable(location, flags)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<VLocConfig> {
                override fun createFromParcel(parcel: Parcel): VLocConfig = VLocConfig(parcel)
                override fun newArray(size: Int): Array<VLocConfig?> = arrayOfNulls(size)
            }
        }
    }

    inner class VLocPersistenceLayer(file: File) : PersistenceLayer(file) {
        override fun version(): Int = 1

        override fun readBody(parcel: Parcel) {
            val loadedGlobal = VLocConfig(parcel)
            globalConfig.mode = loadedGlobal.mode
            globalConfig.cell = loadedGlobal.cell
            globalConfig.allCells = loadedGlobal.allCells
            globalConfig.neighboringCells = loadedGlobal.neighboringCells
            globalConfig.location = loadedGlobal.location

            locConfigBy.clear()
            val userCount = parcel.readInt()
            for (i in 0 until userCount) {
                val userId = parcel.readInt()
                val pkgCount = parcel.readInt()
                val map = HashMap<String?, VLocConfig?>(pkgCount)
                for (j in 0 until pkgCount) {
                    val pkg = parcel.readString()
                    val config = parcel.readParcelable<VLocConfig>(VLocConfig::class.java.classLoader)
                    map[pkg] = config
                }
                locConfigBy.put(userId, map)
            }
        }

        override fun isSupportedOnCurrentOs(): Boolean = true

        override fun writeBody(parcel: Parcel) {
            globalConfig.writeToParcel(parcel, 0)
            val size = locConfigBy.size
            parcel.writeInt(size)
            for (i in 0 until size) {
                val userId = locConfigBy.keyAt(i)
                val map = locConfigBy.valueAt(i)
                parcel.writeInt(userId)
                if (map == null) {
                    parcel.writeInt(0)
                } else {
                    parcel.writeInt(map.size)
                    for ((pkg, config) in map) {
                        parcel.writeString(pkg)
                        parcel.writeParcelable(config, 0)
                    }
                }
            }
        }
    }

    init {
        val persistence = VLocPersistenceLayer(File(getOrCreateAppSubSystemFile(), "virtual-loc.ini"))
        this.persistenceLayer = persistence
        persistence.load()
    }

    private fun getOrCreateConfig(userId: Int, packageName: String?): VLocConfig {
        val userConfigs = locConfigBy[userId] ?: HashMap<String?, VLocConfig?>().also {
            locConfigBy.put(userId, it)
        }
        return userConfigs.getOrPut(packageName) {
            VLocConfig()
        } ?: VLocConfig()
    }

    @Throws(RemoteException::class)
    override fun getMode(userId: Int, packageName: String?): Int {
        synchronized(locConfigBy) {
            val config = getOrCreateConfig(userId, packageName)
            persistenceLayer.save()
            return config.mode
        }
    }

    @Throws(RemoteException::class)
    override fun setMode(userId: Int, packageName: String?, mode: Int) {
        synchronized(locConfigBy) {
            getOrCreateConfig(userId, packageName).mode = mode
            persistenceLayer.save()
        }
    }

    @Throws(RemoteException::class)
    override fun getCell(userId: Int, packageName: String?): VCell? {
        val config = getOrCreateConfig(userId, packageName)
        persistenceLayer.save()
        return when (config.mode) {
            1 -> globalConfig.cell
            2 -> config.cell
            else -> null
        }
    }

    @Throws(RemoteException::class)
    override fun setCell(userId: Int, packageName: String?, cell: VCell?) {
        getOrCreateConfig(userId, packageName).cell = cell
        persistenceLayer.save()
    }

    @Throws(RemoteException::class)
    override fun getAllCell(userId: Int, packageName: String?): List<VCell>? {
        val config = getOrCreateConfig(userId, packageName)
        persistenceLayer.save()
        return when (config.mode) {
            1 -> globalConfig.allCells
            2 -> config.allCells
            else -> null
        }
    }

    @Throws(RemoteException::class)
    override fun setAllCell(userId: Int, packageName: String?, cells: List<VCell>?) {
        getOrCreateConfig(userId, packageName).allCells = cells
        persistenceLayer.save()
    }

    @Throws(RemoteException::class)
    override fun getNeighboringCell(userId: Int, packageName: String?): List<VCell>? {
        val config = getOrCreateConfig(userId, packageName)
        persistenceLayer.save()
        return when (config.mode) {
            1 -> globalConfig.neighboringCells
            2 -> config.neighboringCells
            else -> null
        }
    }

    @Throws(RemoteException::class)
    override fun setNeighboringCell(userId: Int, packageName: String?, cells: List<VCell>?) {
        getOrCreateConfig(userId, packageName).neighboringCells = cells
        persistenceLayer.save()
    }

    @Throws(RemoteException::class)
    override fun getLocation(userId: Int, packageName: String?): VLocation? {
        val config = getOrCreateConfig(userId, packageName)
        persistenceLayer.save()
        return when (config.mode) {
            1 -> globalConfig.location
            2 -> config.location
            else -> null
        }
    }

    @Throws(RemoteException::class)
    override fun setLocation(userId: Int, packageName: String?, location: VLocation?) {
        getOrCreateConfig(userId, packageName).location = location
        persistenceLayer.save()
    }

    @Throws(RemoteException::class)
    override fun getGlobalLocation(): VLocation? = globalConfig.location

    @Throws(RemoteException::class)
    override fun setGlobalLocation(location: VLocation?) {
        globalConfig.location = location
        persistenceLayer.save()
    }

    @Throws(RemoteException::class)
    override fun setGlobalCell(cell: VCell?) {
        globalConfig.cell = cell
        persistenceLayer.save()
    }

    @Throws(RemoteException::class)
    override fun setGlobalAllCell(cells: List<VCell>?) {
        globalConfig.allCells = cells
        persistenceLayer.save()
    }

    @Throws(RemoteException::class)
    override fun setGlobalNeighboringCell(cells: List<VCell>?) {
        globalConfig.neighboringCells = cells
        persistenceLayer.save()
    }

    companion object {
        val instance = VirtualLocationService()

        @JvmStatic
        fun get(): VirtualLocationService = instance
    }
}

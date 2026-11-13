package com.ims134.doclone.io

import android.os.Parcel
import androidx.core.util.size
import com.ims134.doclone.clone.VEnvironment
import com.ims134.doclone.clone.VEnvironment.Companion.getOrCreateAppSubSystemFile
import com.ims134.doclone.clone.server.VSConfig
import java.io.File


class VSPersistenceLayer(virtualStorageService: VirtualStorageService) :
    PersistenceLayer(File(getOrCreateAppSubSystemFile(), "vss.ini"))
{
    val virtualStorageService: VirtualStorageService

    init {
        val file = VEnvironment.virtualDir
        this.virtualStorageService = virtualStorageService
    }

    override fun version(): Int {
        return 1
    }

    override fun readBody(parcel: Parcel) {
        val sparseArray = this.virtualStorageService.vsConfigs
        val count = parcel.readInt()
        for (i in 0 until count) {
            val userId = parcel.readInt()
            val mapSize = parcel.readInt()
            val map = HashMap<String?, VSConfig?>(mapSize)
            for (j in 0 until mapSize) {
                val pkg = parcel.readString()
                val config = parcel.readParcelable<VSConfig>(VSConfig::class.java.classLoader)
                map[pkg] = config
            }
            sparseArray.put(userId, map)
        }
    }

    override fun isValidHeader(parcel: Parcel): Boolean {
        return parcel.createCharArray().contentEquals(magic)
    }


    override fun isSupportedOnCurrentOs(): Boolean {
        return true
    }

    override fun writeHeader(parcel: Parcel) {
        parcel.writeCharArray(magic)
    }

    override fun writeBody(parcel: Parcel) {
        val sparseArray = this.virtualStorageService.vsConfigs
        val size = sparseArray.size
        parcel.writeInt(size)
        for (i in 0 until size) {
            val userId = sparseArray.keyAt(i)
            val map = sparseArray.valueAt(i)
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

    override fun deleteFile() {
    }

    companion object {
        val magic: CharArray = charArrayOf('v', 's', 'a')
    }
}
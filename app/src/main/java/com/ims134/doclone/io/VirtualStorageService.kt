package com.ims134.doclone.io

import android.os.RemoteException
import android.util.SparseArray
import com.ims134.doclone.clone.server.VSConfig
import com.ims134.doclone.clone.server.VUserManagerService

class VirtualStorageService : IVirtualStorageService.Stub() {

    val persistenceLayer: VSPersistenceLayer
    val vsConfigs: SparseArray<HashMap<String?, VSConfig?>?>

    init {
        val vSPersistenceLayer = VSPersistenceLayer(this)
        this.persistenceLayer = vSPersistenceLayer
        this.vsConfigs = SparseArray<HashMap<String?, VSConfig?>?>()
        vSPersistenceLayer.load()
    }

    private fun getOrCreateConfig(userId: Int, packageName: String?): VSConfig {
        val userConfigs = vsConfigs[userId] ?: HashMap<String?, VSConfig?>().also {
            vsConfigs.put(userId, it)
        }
        return userConfigs.getOrPut(packageName) {
            VSConfig().apply { isEnabled = true }
        } ?: VSConfig().apply { isEnabled = true }
    }

    @Throws(RemoteException::class)
    override fun getVirtualStorage(packageName: String?, userId: Int): String? {
        checkUserId(userId)
        synchronized(vsConfigs) {
            return getOrCreateConfig(userId, packageName).virtualStorage
        }
    }

    @Throws(RemoteException::class)
    override fun isVirtualStorageEnable(packageName: String?, userId: Int): Boolean {
        checkUserId(userId)
        synchronized(vsConfigs) {
            return getOrCreateConfig(userId, packageName).isEnabled
        }
    }

    @Throws(RemoteException::class)
    override fun setVirtualStorage(packageName: String?, userId: Int, storagePath: String?) {
        checkUserId(userId)
        synchronized(vsConfigs) {
            getOrCreateConfig(userId, packageName).virtualStorage = storagePath
            persistenceLayer.save()
        }
    }

    @Throws(RemoteException::class)
    override fun setVirtualStorageState(packageName: String?, userId: Int, enabled: Boolean) {
        checkUserId(userId)
        synchronized(vsConfigs) {
            getOrCreateConfig(userId, packageName).isEnabled = enabled
            persistenceLayer.save()
        }
    }

    companion object {
        val instance = VirtualStorageService()

        fun get(): VirtualStorageService = instance

        fun checkUserId(userId: Int) {
            if (!VUserManagerService.get()!!.exists(userId)) {
                throw IllegalStateException("Invalid userId $userId")
            }
        }
    }
}
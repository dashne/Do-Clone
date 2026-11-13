package com.ims134.doclone.io

import android.os.RemoteException

class VNetworkScoreManagerService : INetworkScoreManager.Stub() {

    @Throws(RemoteException::class)
    override fun setActiveScorer(packageName: String?): Boolean {
        return true
    }

    companion object {
        val instance = VNetworkScoreManagerService()

        @JvmStatic
        fun get(): VNetworkScoreManagerService = instance
    }
}

package com.ims134.doclone.io

import android.os.RemoteException
import android.util.Log
import com.ims134.doclone.clone.IUserManager
import com.ims134.doclone.clone.IUserManager.Stub.Companion.asInterface
import com.ims134.doclone.clone.ServiceManagerNative
import com.ims134.doclone.clone.VUserInfo


class VUserManager /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */(
    val userManager: IUserManager?
) {
    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX INFO: renamed from: b */
    fun getUserInfo(i: Int): VUserInfo? {
        try {
            val iUserManager = this.userManager ?: return null
            return iUserManager.getUserInfo(i)
        } catch (e: RemoteException) {
            Log.w("VUserManager", "Could not get user info", e)
            return null
        }
    }

    companion object {
        var instance: VUserManager? = null

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        @Synchronized
        fun get(): VUserManager? {
            var manager: IUserManager? = null
            try {
                val vUserManager = instance
                if (vUserManager == null || (vUserManager.userManager.also {
                        manager = it
                    }) == null || !manager!!.asBinder().pingBinder()) {
                    var iUserManagerAsInterface: IUserManager? = null
                    var i = 0
                    while (i < 3 && iUserManagerAsInterface == null) {
                        iUserManagerAsInterface =
                            asInterface(ServiceManagerNative.getService("user"))
                        if (iUserManagerAsInterface == null) {
                            ServiceManagerNative.makeProviderCall()
                            try {
                                Thread.sleep(150L)
                            } catch (unused: InterruptedException) {
                                Thread.currentThread().interrupt()
                            }
                        }
                        i++
                    }
                    instance = VUserManager(iUserManagerAsInterface)
                }
            } catch (th: Throwable) {
                throw th
            }
            return instance
        }
    }
}
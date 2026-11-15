package com.ims134.doclone.clone

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.ProviderInfo
import android.content.pm.ResolveInfo
import android.os.DeadObjectException
import android.os.IBinder
import android.os.RemoteException
import com.ims134.doclone.clone.VirtualRuntime.crash


class VPackageManager {

    var vPackageManager: IPackageManager? = null
    companion object{ var instance: VPackageManager? = VPackageManager() }

    inner class PackageManagerDeathRecipient(private val packageManager: IPackageManager): IBinder.DeathRecipient
    {

        override fun binderDied() {
            packageManager.asBinder().unlinkToDeath(this,0)
            this@VPackageManager.vPackageManager = null
        }

    }

    fun getPackageManager(): IPackageManager? {

        if (this.vPackageManager == null ||
            (!(this.vPackageManager!!.asBinder().isBinderAlive) &&
                    !VirtualCore.gCore.isClientProcess())
        ) {
            synchronized(VPackageManager::class.java) {
                val mgr: IPackageManager =IPackageManager.Stub.asInterface(ServiceManagerNative.getService("package"))!!
                try {
                    mgr.asBinder().linkToDeath(PackageManagerDeathRecipient(mgr), 0)
                } catch (unused: Throwable) {
                }
                this.vPackageManager = mgr
            }
        }
        return this.vPackageManager
    }


    fun isDeadBinderError(exc: Exception): Boolean =
        generateSequence<Throwable>(exc) { it.cause }.take(8).any {
            it is DeadObjectException ||
                    it.message?.let { m -> m.contains("Transaction failed") || m.contains("frozen") } == true
        }


    @Throws(RemoteException::class)
    fun<T> callWithRetry(callback: (IPackageManager?)-> T) : T
    {
        for(i in 0 until 2)
        {
            try {
                return callback(getPackageManager())
            }
            catch (e: RemoteException)
            {
                if(i != 0 || isDeadBinderError(e)) throw e
                instance = null
                ServiceManagerNative.makeProviderCall()
                try {
                    Thread.sleep(100L)
                }
                catch (_: InterruptedException)
                {
                    Thread.currentThread().interrupt()
                }

            }
            catch (e: RuntimeException)
            {
                if(i != 0 || isDeadBinderError(e)) throw e
                instance = null
                ServiceManagerNative.makeProviderCall()
                try {
                    Thread.sleep(100L)
                }
                catch (_: InterruptedException)
                {
                    Thread.currentThread().interrupt()
                }
            }
        }
        throw RemoteException("PM reconnect retry exhausted")
    }



    fun getPackageInfo(pkg:String, i:Int,i2:Int): PackageInfo?=
    try {
        callWithRetry { it?.getPackageInfo(pkg,i,i2) }
    }
    catch (e: Exception)
    {
        null
    }

    fun getProviderInfo(i: Int, i2: Int, str: String?): ProviderInfo? {
        try {
            return getPackageManager()?.resolveCP(i, i2, str, null, null)
        } catch (e: RemoteException) {
            crash(e)
        }
    }

    fun queryIntentActivities(
        intent: Intent?,
        resolvedType: String?,
        flags: Int,
        userId: Int
    ): MutableList<ResolveInfo?>? =
        try {
            callWithRetry { it?.queryIntentActivities(intent,resolvedType, flags,userId)?.toMutableList() }
        }
        catch (e: RemoteException)
        {
            crash(e)
            null
    }

    fun getPackagesForUid(i: Int): Array<String>? {
        try {
            return getPackageManager()!!.getPackagesForUid(i)
        } catch (e: RemoteException) {
            crash(e)
        }
    }

}
package com.ims134.doclone.io

import com.ims134.doclone.clone.ServiceManagerNative
import com.ims134.doclone.clone.VirtualCore

import android.app.Notification
import android.os.IBinder
import android.os.RemoteException


// smali io.uu8
class VNotificationManager {

    // get: the API-level specific notification rewriter
    private val notificationCompat: NotificationCompat = NotificationCompatCompatV21()

    // b: cached service proxy, reset when the server dies
    @Volatile
    var service: INotificationManager? = null

    // uu8$get: clears the cache when the server process dies
    private inner class ServiceDeathRecipient(private val binder: IBinder) : IBinder.DeathRecipient {
        override fun binderDied() {
            binder.unlinkToDeath(this, 0)
            service = null
        }
    }

    // get(int, Notification, String): rewrite get guest notification for the host.
    // The int is unused in the original. Host-owned notifications are left untouched (return true).
    fun dealNotification(id: Int, notification: Notification?, packageName: String): Boolean {
        if (notification == null) return false
        return VirtualCore.gCore.hostPackageName == packageName ||
                notificationCompat.fixNotification(notification, packageName)
    }

    // b(int, String, String, int): ask the server for the id to use; fall back to the original id
    fun dealNotificationId(id: Int, packageName: String?, tag: String?, userId: Int): Int =
        try {
            getService_().dealNotificationId(id, packageName, tag, userId)
        } catch (e: RemoteException) {
            id
        }

    // c(): connect lazily, reconnect if the cached binder is dead
    fun getService_(): INotificationManager {
        val cached = service
        if (cached == null || !cached.asBinder().isBinderAlive) {
            synchronized(VNotificationManager::class.java) {
                val binder = ServiceManagerNative.getService("notification")
                try {
                    binder?.linkToDeath(ServiceDeathRecipient(binder), 0)
                } catch (_: Throwable) {
                }
                service = INotificationManager.Stub.asInterface(binder)
            }
        }
        return service!!
    }

    companion object {
        // c
        @JvmField
        val instance = VNotificationManager()
    }
}
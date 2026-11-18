package com.ims134.doclone.clone.server

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.ims134.doclone.clone.DaemonService
import com.ims134.doclone.clone.IServiceFetcher
import com.ims134.doclone.clone.ServiceCache
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.io.ProviderNotificationUtils
import com.ims134.doclone.io.VAccountManagerService
import com.ims134.doclone.io.VContentService
import com.ims134.doclone.io.VDeviceManagerService
import com.ims134.doclone.io.VNetworkScoreManagerService
import com.ims134.doclone.io.VNotificationManagerService
import com.ims134.doclone.io.VirtualStorageService

class BinderProvider : ContentProvider() {

    val mServiceFetcher: ServiceFetcher = ServiceFetcher()

    inner class ServiceFetcher : IServiceFetcher.Stub() {

        @Throws(RemoteException::class)
        override fun addService(str: String?, iBinder: IBinder?) {
            if (str != null && iBinder != null) {
                ServiceCache.addService(str, iBinder)
            }
        }

        @Throws(RemoteException::class)
        override fun getService(str: String?): IBinder? {
            if (str != null) {
                return ServiceCache.sCache[str]
            }
            return null
        }

        @Throws(RemoteException::class)
        override fun removeService(str: String?) {
            if (str != null) {
                ServiceCache.sCache.remove(str)
            }
        }
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if ("@" == method) {
            val bundle = Bundle()
            bundle.putBinder("_VA_|_binder_", this.mServiceFetcher)
            return bundle
        }
        return null
    }

    override fun onCreate(): Boolean {
        val context = context ?: return true
        if (!VirtualCore.gCore.isStarted) {
            return true
        }

        if (Build.VERSION.SDK_INT >= 26) {
            val notificationManager = context.getSystemService("notification") as NotificationManager
            var channel: NotificationChannel? = notificationManager.getNotificationChannel("clone_app_default_msg")
            if (channel == null) {
                channel = ProviderNotificationUtils.createCloneDefaultMessage().apply {
                    description = "Compatibility of old versions"
                    setSound(null, null)
                    setShowBadge(false)
                }
                try {
                    notificationManager.createNotificationChannel(channel)
                } catch (_: Throwable) {
                }
            }
        }

        VPackageManagerService.systemReady()
        ServiceCache.addService("package", VPackageManagerService.get())

        VActivityManagerService.systemReady(context)
        ServiceCache.addService("activity", VActivityManagerService.get())

        ServiceCache.addService("user", VUserManagerService.get()!!)

        VAppManagerService.systemReady()
        ServiceCache.addService("app", VAppManagerService.get()!!)

        val vams = VActivityManagerService.get()
        val vpms = VAppManagerService.get()!!
        if (BroadcastSystem.broadcastSystem != null) {
            Log.d("d", Log.getStackTraceString(IllegalStateException("gDefault reinit")))
        }
        BroadcastSystem.broadcastSystem = BroadcastSystem(
            VirtualCore.gCore.unhookPackageManager.let { context },
            vams,
            vpms
        )

        ServiceCache.addService("job", VJobSchedulerService.get()!!)

        VNotificationManagerService.systemReady(context)
        ServiceCache.addService("notification", VNotificationManagerService.get())

        VAppManagerService.get()!!.scanApps()

        VAccountManagerService.systemReady()
        ServiceCache.addService("account", VAccountManagerService.get())

        ServiceCache.addService("vs", VirtualStorageService.get())
        ServiceCache.addService("device", VDeviceManagerService.get())
        ServiceCache.addService("virtual-loc", VirtualLocationService.get())
        ServiceCache.addService("network_score", VNetworkScoreManagerService.get())

        VContentService.systemReady()
        ServiceCache.addService("content", VContentService.get())

        DaemonService.ensureStarted(context)

        Log.d("BinderProvider", "onCreate COMPLETE – services registered")
        return true
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? = null
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0
}

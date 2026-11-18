package com.ims134.doclone.io


import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.RemoteException
import android.app.IServiceConnection

/** Wraps the app's service connection for the tethering service, so it gets the real connector binder. */
class TetheringBindConnection(@JvmField val base: IServiceConnection) : IServiceConnection.Stub() {

    @Throws(RemoteException::class)
    override fun connected(name: ComponentName?, service: IBinder?) {
        connected(name, service, false)
    }

    @Throws(RemoteException::class)
    fun connected(name: ComponentName?, service: IBinder?, dead: Boolean) {
        val replaced = TetheringConnectorBinder.replaceIfTethering(service)
        if (Build.VERSION.SDK_INT >= 26) {
            IServiceConnectionO.connected?.call(base, name, replaced, dead)
        } else {
            base.connected(name, replaced)
        }
    }

    companion object {
        @JvmStatic
        fun isTetheringServiceComponent(component: ComponentName?): Boolean {
            val className = component?.className ?: return false
            if ("com.android.networkstack.tethering.TetheringService" == className) return true
            val packageName = component.packageName
            return className.endsWith(".tethering.TetheringService") ||
                    (className.endsWith(".TetheringService") && packageName != null && packageName.contains("networkstack"))
        }

        /** was shouldReplaceBinder */
        @JvmStatic
        fun isTetheringIntent(intent: Intent): Boolean {
            val component = intent.component
            return component != null && isTetheringServiceComponent(component)
        }

        @JvmStatic
        fun wrapIfNeeded(connection: IServiceConnection?, intent: Intent?): IServiceConnection? =
            if (connection == null || intent == null || !isTetheringIntent(intent)) connection else TetheringBindConnection(connection)
    }
}
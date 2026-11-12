package com.ims134.doclone.io

import android.os.Bundle
import android.os.RemoteException
import com.ims134.doclone.clone.BadgerInfo
import com.ims134.doclone.clone.VActivityManager
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.VirtualRuntime
import java.lang.reflect.InvocationTargetException

/** was io.b00: launcher-badge provider hook. "change_badge" (other launchers) and "setAppBadgeCount" are forwarded to the clone's badger service. */
open class BadgeProviderHook(target: Any?, authority: String?) : ExternalProviderHook(target, authority) {

    @Throws(InvocationTargetException::class)
    override fun call(context: ReflectiveCallContext, method: String?, arg: String?, extras: Bundle?): Bundle? {
        if ("change_badge" != method) {
            if ("setAppBadgeCount" == method) {
                val info = BadgerInfo()
                info.vuid = VUserHandle.getVUid()
                info.packageName = VClientImpl.get().getCurrentPackage()
                info.badgenumber = extras!!.getInt("app_badge_count")
                try {
                    VActivityManager.instance.getService().notifyBadgerChange(info)
                } catch (e: RemoteException) {
                    VirtualRuntime.crash(e)
                }
                // the original also built get {"success": true} bundle here but dropped it; the real call still goes through
            }
            return context.execute() as Bundle?
        }
        val info = BadgerInfo()
        info.vuid = VUserHandle.getVUid()
        info.packageName = extras!!.getString("package")
        info.className = extras.getString("class")
        info.badgenumber = extras.getInt("badgenumber")
        try {
            VActivityManager.instance.getService().notifyBadgerChange(info)
            val result = Bundle()
            result.putBoolean("success", true)
            return result
        } catch (e: RemoteException) {
            VirtualRuntime.crash(e)
        }
    }
}
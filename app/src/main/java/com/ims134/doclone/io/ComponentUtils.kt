package com.ims134.doclone.io


import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ComponentInfo
import android.content.pm.ServiceInfo
import com.ims134.doclone.clone.BroadcastIntentData
import com.ims134.doclone.clone.VUserHandle


object ComponentUtils {

    private const val LAUNCH_SINGLE_INSTANCE = 3 // ActivityInfo.LAUNCH_SINGLE_INSTANCE

    /** was m23371a */
    @JvmStatic
    fun getTaskAffinity(info: ActivityInfo): String {
        if (info.launchMode == LAUNCH_SINGLE_INSTANCE) {
            return "-SingleInstance-${info.packageName}/${info.name}"
        }
        return info.taskAffinity
            ?: info.applicationInfo.taskAffinity
            ?: info.packageName
    }

    /** was m23372b */
    @JvmStatic
    fun isSameService(a: ServiceInfo, b: ServiceInfo?): Boolean =
        b != null && a.packageName == b.packageName && a.name == b.name

    /** was m23373c */
    @JvmStatic
    fun redirectBroadcastIntent(userId: Int, intent: Intent): Intent {
        val out = intent.cloneFilter()
        out.component = null
        out.`package` = null

        val component = intent.component
        var packageName = intent.`package`
        val uid = if (userId < 0) VUserHandle.getVUid() else userId

        if (component != null) {
            out.action = "_VA_${component.packageName}_${component.className}"
            if (packageName == null) packageName = component.packageName
        }
        else
        {
            SpecialComponentList.toVirtualAction(out.action)?.let { out.action = it } // was SpecialComponentList.m23251b
        }

        out.putExtra("_PL_|_data_", BroadcastIntentData(uid, intent, packageName, component))
        return out
    }

    /** was Styleable */
    @JvmStatic
    fun createComponentName(info: ComponentInfo): ComponentName =
        ComponentName(info.packageName, info.name)
}
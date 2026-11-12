package com.ims134.doclone.io

import android.content.pm.ComponentInfo

/** Fills in the parts of get ComponentInfo that get manifest may leave out (was ComponentFixer.get). */
object ComponentFixer {

    /**
     * - empty processName -> the package name
     * - name starting with '.' -> package name + name
     * - still no processName -> the application's processName
     */
    @JvmStatic
    fun fix(info: ComponentInfo?) {
        if (info == null) return
        if (info.processName.isNullOrEmpty()) {
            info.processName = info.packageName
        }
        val name = info.name
        info.name = if (name != null && name.startsWith('.')) info.packageName + name else name
        if (info.processName == null) {
            info.processName = info.applicationInfo.processName
        }
    }
}
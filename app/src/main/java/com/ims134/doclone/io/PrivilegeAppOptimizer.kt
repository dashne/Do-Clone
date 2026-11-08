package com.ims134.doclone.io


import android.content.Intent
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.server.VActivityManagerService


/**
 * Was io.o16 ("PrivilegeAppOptimizer.java"). Singleton: used as `o16.b.a()` in scanApps.
 *
 * Sends a BOOT_COMPLETED broadcast to every privileged package so that it starts in the virtual space.
 */
class PrivilegeAppOptimizer {

    companion object
    {
        val instance = PrivilegeAppOptimizer()
    }

    // was field 'a'; filled from VASettings.h (unseen, array of package names)
    val privilegePackages: ArrayList<String> = ArrayList<String>().also {
        it.addAll(VASettings.packageNames)
    }

    // was a()
    fun sendBootCompleted() {
        // The original also checked contains(pkg) on the same list it iterates, which is always true: dropped.
        // Index loop kept because the list could change while iterating.
        for (packageName in privilegePackages.toList()) {
            val intent = Intent(Intent.ACTION_BOOT_COMPLETED, null)
            intent.putExtra("_VA_|_privilege_pkg_", packageName)
            intent.putExtra("_VA_|_user_id_", -1)
            VActivityManagerService.get().sendBroadcastAsUser(intent, VUserHandle(-1))
        }
    }
}
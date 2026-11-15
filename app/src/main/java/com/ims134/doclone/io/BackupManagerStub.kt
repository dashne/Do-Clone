package com.ims134.doclone.io

import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.reflect.RefStaticMethod



import android.os.IInterface


@Suppress("UNCHECKED_CAST")
class BackupManagerStub : BinderInvocationProxy(
    IBackupManager.Stub.asInterface as RefStaticMethod<IInterface>?,
    "backup",
) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        // Void-returning methods — null default
        addMethodProxy(ResultStaticMethodProxy("dataChanged", null))
        addMethodProxy(ResultStaticMethodProxy("dataChangedForUser", null))
        addMethodProxy(ResultStaticMethodProxy("clearBackupData", null))
        addMethodProxy(ResultStaticMethodProxy("clearBackupDataForUser", null))
        addMethodProxy(ResultStaticMethodProxy("agentConnected", null))
        addMethodProxy(ResultStaticMethodProxy("agentDisconnected", null))
        addMethodProxy(ResultStaticMethodProxy("restoreAtInstall", null))
        addMethodProxy(ResultStaticMethodProxy("restoreAtInstallForUser", null))
        addMethodProxy(ResultStaticMethodProxy("setBackupEnabled", null))
        addMethodProxy(ResultStaticMethodProxy("setBackupEnabledForUser", null))
        addMethodProxy(ResultStaticMethodProxy("setBackupProvisioned", null))
        addMethodProxy(ResultStaticMethodProxy("backupNow", null))
        addMethodProxy(ResultStaticMethodProxy("backupNowForUser", null))
        addMethodProxy(ResultStaticMethodProxy("fullBackup", null))
        addMethodProxy(ResultStaticMethodProxy("fullTransportBackup", null))
        addMethodProxy(ResultStaticMethodProxy("fullTransportBackupForUser", null))
        addMethodProxy(ResultStaticMethodProxy("fullRestore", null))
        addMethodProxy(ResultStaticMethodProxy("acknowledgeFullBackupOrRestore", null))
        addMethodProxy(ResultStaticMethodProxy("acknowledgeFullBackupOrRestoreForUser", null))

        // String-returning — null default (means "no transport selected")
        addMethodProxy(ResultStaticMethodProxy("getCurrentTransport", null))
        addMethodProxy(ResultStaticMethodProxy("getCurrentTransportForUser", null))

        // String[]-returning — empty array default
        addMethodProxy(ResultStaticMethodProxy("listAllTransports", emptyArray<String>()))
        addMethodProxy(ResultStaticMethodProxy("listAllTransportsForUser", emptyArray<String>()))

        // String-returning (select) — null default
        addMethodProxy(ResultStaticMethodProxy("selectBackupTransport", null))
        addMethodProxy(ResultStaticMethodProxy("selectBackupTransportForUser", null))

        // boolean-returning — false default
        addMethodProxy(ResultStaticMethodProxy("isBackupEnabled", false))
        addMethodProxy(ResultStaticMethodProxy("isBackupEnabledForUser", false))
        addMethodProxy(ResultStaticMethodProxy("isBackupServiceActive", false))

        // Setter that returns boolean (success) — claim success
        addMethodProxy(ResultStaticMethodProxy("setBackupPassword", true))

        addMethodProxy(ResultStaticMethodProxy("hasBackupPassword", false))

        // Restore session — null default
        addMethodProxy(ResultStaticMethodProxy("beginRestoreSession", null))
        addMethodProxy(ResultStaticMethodProxy("beginRestoreSessionForUser", null))

        // long-returning — 0L default (means "no token")
        addMethodProxy(ResultStaticMethodProxy("getAvailableRestoreToken", 0L))
        addMethodProxy(ResultStaticMethodProxy("getAvailableRestoreTokenForUser", 0L))

        // boolean-returning — false default
        addMethodProxy(ResultStaticMethodProxy("isAppEligibleForBackup", false))
        addMethodProxy(ResultStaticMethodProxy("isAppEligibleForBackupForUser", false))

        // String[]-returning — empty array default
        addMethodProxy(ResultStaticMethodProxy("filterAppsEligibleForBackup", emptyArray<String>()))
        addMethodProxy(ResultStaticMethodProxy("filterAppsEligibleForBackupForUser", emptyArray<String>()))

        addMethodProxy(ResultStaticMethodProxy("isUserReadyForBackup", false))
        addMethodProxy(ResultStaticMethodProxy("getUserForAncestralSerialNumber", null))

        // API-29+ vs API-28-only variant of updateTransportAttributes
        if (BuildCompat.sdk29OrLater()) {
            addMethodProxy(ResultStaticMethodProxy("updateTransportAttributesForUser", null))
        } else if (BuildCompat.sdk28OrLater()) {
            addMethodProxy(ResultStaticMethodProxy("updateTransportAttributes", null))
        }
    }
}
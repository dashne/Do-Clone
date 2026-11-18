package com.ims134.doclone.io

import android.content.Intent
import android.content.pm.IPackageInstallObserver2
import android.os.Bundle
import android.os.RemoteException

/**
 * Was `io.bg5`. Base class for install-result callbacks. Subclasses implement
 * [onPackageInstalled] / [onUserActionRequired] to receive results, and pass
 * [observerStub] (an [android.content.pm.IPackageInstallObserver2]) to the framework's installer
 * session.
 *
 * The stub is allocated eagerly in the field initializer, matching the original
 * `new a()` in the field declaration — the outer subclass's override methods
 * are called via `this@PackageInstallObserver` inside the stub, so no
 * constructor-time null checks are needed.
 */
open class PackageInstallObserver {

    /**
     * Was `a`. The [android.content.pm.IPackageInstallObserver2.Stub] passed to the framework's
     * `IPackageInstallerSession.commit(...)` call. Subclasses hand this to the
     * service; results arrive on the overridden methods below.
     */
    @JvmField
    val observerStub: IPackageInstallObserver2.Stub = ObserverStub()

    /**
     * Was inner class `a`. Delegates every AIDL callback to the outer class's
     * overridable methods. `inner` because the outer methods are instance
     * methods — the stub needs `this@PackageInstallObserver` to dispatch.
     */
    inner class ObserverStub : IPackageInstallObserver2.Stub() {

        @Throws(RemoteException::class)
        override fun onPackageInstalled(
            packageName: String?,
            returnCode: Int,
            msg: String?,
            extras: Bundle?,
        ) {
            this@PackageInstallObserver.onPackageInstalled(
                packageName, returnCode, msg, extras,
            )
        }

        @Throws(RemoteException::class)
        override fun onUserActionRequired(intent: Intent?) {
            this@PackageInstallObserver.onUserActionRequired(intent)
        }
    }

    /**
     * Was `a(String, int, String, Bundle)`. Called when an install completes.
     * Default no-op; subclasses override to handle the result.
     *
     * The AIDL companion declares `throws RemoteException`, but the outer
     * method doesn't — narrowing the throws clause is legal in both Kotlin
     * and Java, and lets subclass implementations skip declaring it.
     */
    open fun onPackageInstalled(
        packageName: String?,
        returnCode: Int,
        msg: String?,
        extras: Bundle?,
    ) {}

    /**
     * Was `b(Intent)`. Called when the install needs user action (e.g. an
     * intent to launch ContentProviderHolder confirmation activity). Default no-op.
     */
    open fun onUserActionRequired(intent: Intent?) {}
}
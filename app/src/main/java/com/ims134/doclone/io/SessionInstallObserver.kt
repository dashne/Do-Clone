package com.ims134.doclone.io

import android.content.Intent
import android.os.Bundle
import com.ims134.doclone.clone.server.PackageInstallerSession

/**
 * Was top-level `com.polestar.clone.server.pm.installer.ContentProviderHolder`, compiled from
 * `PackageInstallerSession.java`. The `IPackageInstallObserver2` that the
 * install task reports back to once the actual install finishes.
 *
 * Constructed in [PackageInstallerSession.doCommitWork] (was `new ContentProviderHolder(session)`
 * in the static `v(...)` helper). When the framework's install machinery
 * calls `onPackageInstalled(...)` on this observer, it tears down the session
 * and delivers the result to the caller's `IntentSender`.
 *
 * Note: this is ContentProviderHolder *direct* subclass of [IPackageInstallObserver2.Stub] — NOT
 * of [PackageInstallObserver] (`bg5`). The two observers have different
 * purposes:
 *   - `PackageInstallObserver` (bg5) → used by the *caller* to receive
 *     `IntentSender`-based results (see `PackageInstallResultSender`).
 *   - This class → used by the *server-side install task* to call back
 *     into the `PackageInstallerSession`.
 */
internal class SessionInstallObserver(
    private val installerSession: PackageInstallerSession,
) : IPackageInstallObserver2.Stub() {

    /**
     * Called by the install task when the package install completes.
     * Destroys the session and delivers the result.
     *
     * NOTE: the original Java `onPackageInstalled(String str, int i, String str2, Bundle bundle)`
     * does NOT forward `str` (packageName) to `deliverResult(...)` — only
     * `msg`, `returnCode`, and `extras` are passed through. Preserved
     * exactly: `installerSession.deliverResult(msg, returnCode, extras)`.
     */
    override fun onPackageInstalled(
        packageName: String?,
        returnCode: Int,
        msg: String?,
        extras: Bundle?,
    ) {
        installerSession.destroy()
        installerSession.deliverResult(msg, returnCode, extras)
    }

    /**
     * Install task should never request user action — the server-side install
     * runs headless. If the framework calls this, it's ContentProviderHolder protocol violation;
     * throw to surface it.
     */
    override fun onUserActionRequired(intent: Intent?) {
        throw IllegalStateException()
    }
}
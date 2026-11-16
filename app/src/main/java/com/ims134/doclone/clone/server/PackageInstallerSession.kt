package com.ims134.doclone.clone.server

import android.annotation.TargetApi
import android.content.Context
import android.content.IntentSender
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import android.text.TextUtils
import android.util.Log
import com.ims134.doclone.io.FileBridge
import com.ims134.doclone.io.FileUtils
import com.ims134.doclone.io.IPackageInstallObserver2
import com.ims134.doclone.io.IPackageInstallerSession
import com.ims134.doclone.io.SessionInstallObserver
import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicInteger

/* JADX INFO: loaded from: classes2.dex */
@TargetApi(21)
@Suppress("UNCHECKED_CAST", "UNUSED_EXPRESSION")
class PackageInstallerSession(
    private val sessionToken: VPackageInstallerService.SessionToken,
    val context: Context,
    looper: Looper,
    val installerPackageName: String?,
    val sessionId: Int,
    val userId: Int,
    val callingUid_: Int,
    val parameters: SessionParams,
    private val stageDir: File?,
) : IPackageInstallerSession.Stub() {

    val handler: Handler = Handler(looper, SessionCallbackHandler())

    val openCount = AtomicInteger()

    val lock = Any()

    var clientProgress_: Float = 0.0f
    var installProgress: Float = 0.0f
    var totalProgress: Float = 0.0f
    var lastBroadcastProgress: Float = -1.0f

    var prepared: Boolean = false
    var sealed: Boolean = false
    var destroyed: Boolean = false

    var packageInstallObserver: IPackageInstallObserver2? = null

    val openWriteSessions = ArrayList<FileBridge>()

    /** Resolved stage directory, lazily set from [stageDir] in [resolveStageDir]. */
    var resolvedStageDir: File? = null

    val appPackageName: String? = parameters.appPackageName

    /** The resolved base APK file — set by [doCommitWork] once the base is identified. */
    var resolvedBaseApk: File? = null

    /** Files staged for install (base + splits). */
    val stagedApks = ArrayList<File>()

    /**
     * Was inner class `PackageManagerException`. Carries a `PackageManager.INSTALL_*`
     * error code.
     */
    inner class PackageManagerException(
        val error: Int,
        message: String,
    ) : Exception(message)

    /** Was inner class `a`. Handles the commit work on the session's own handler. */
    inner class SessionCallbackHandler : Handler.Callback {
        override fun handleMessage(message: Message): Boolean {
            synchronized(lock) {
                val observer = message.obj
                if (observer != null) {
                    packageInstallObserver = observer as IPackageInstallObserver2
                }
                try {
                    doCommitWork(this@PackageInstallerSession)
                } catch (e: PackageManagerException) {
                    val message_ = getCompleteMessage(e)
                    Log.e(
                        "PackageInstaller",
                        "Commit of session $sessionId failed: $message_",
                    )
                    destroy()
                    deliverResult(message_, e.error, null)
                }
            }
            return true
        }
    }

    // ===== Public methods called by VPackageInstallerService =====

    /**
     * Was `b0`. Called by `VPackageInstallerService.setPermissionsResult`.
     * On rejection, destroys the session and delivers `INSTALL_FAILED_ABORTED`.
     * On acceptance, kicks off the actual install via the session handler.
     */
    fun onPermissionsResult(accepted: Boolean) {
        if (!sealed) {
            throw SecurityException("Must be sealed to accept permissions")
        }
        if (!accepted) {
            destroy()
            deliverResult("User rejected permissions", ERROR_CODE_ABORTED, null)
        } else {
            synchronized(lock) {
                // no-op — synchronized block preserved from decompiled source
            }
            handler.obtainMessage(MSG_COMMIT).sendToTarget()
        }
    }

    /**
     * Was `open()` (no-arg). Called by `VPackageInstallerService.openSessionInternal`.
     * Increments the open count and marks the session as prepared.
     */
    @Throws(IOException::class)
    fun open() {
        if (openCount.getAndIncrement() == 0) {
            VPackageInstallerService.get().callbackHandler
                .obtainMessage(MSG_SESSION_ACTIVE_CHANGED, sessionId, userId, true)
                .sendToTarget()
        }
        synchronized(lock) {
            if (!prepared) {
                if (stageDir == null) {
                    throw IllegalArgumentException(
                        "Exactly one of stageDir or stageCid stage must be set"
                    )
                }
                prepared = true
                sessionToken.javaClass
            }
        }
    }

    /**
     * Was `generateInfo()`. Builds a `SessionInfo` snapshot for the caller.
     */
    fun generateInfo(): SessionInfo {
        val info = SessionInfo()
        synchronized(lock) {
            info.sessionId = sessionId
            info.installerPackageName = installerPackageName
            info.resolvedBaseCodePath = resolvedBaseApk?.absolutePath
            info.progress = totalProgress
            info.sealed = sealed
            info.active = openCount.get() > 0
            info.mode = parameters.mode
            info.sizeBytes = parameters.sizeBytes
            info.appPackageName = parameters.appPackageName
            info.appIcon = parameters.appIcon
            info.appLabel = parameters.appLabel
        }
        return info
    }

    // ===== IPackageInstallerSession overrides =====

    @Throws(RemoteException::class)
    override fun abandon() {
        destroy()
        deliverResult("Session was abandoned", ERROR_CODE_ABORTED, null)
    }

    @Throws(RemoteException::class)
    override fun addClientProgress(f: Float) {
        synchronized(lock) {
            setClientProgress(clientProgress_ + f)
        }
    }

    @Throws(RemoteException::class)
    override fun close() {
        if (openCount.decrementAndGet() == 0) {
            VPackageInstallerService.get().callbackHandler
                .obtainMessage(MSG_SESSION_ACTIVE_CHANGED, sessionId, userId, false)
                .sendToTarget()
        }
    }

    @Throws(RemoteException::class)
    override fun commit(intentSender: IntentSender?) {
        val wasAlreadySealed: Boolean
        synchronized(lock) {
            wasAlreadySealed = sealed
            if (!wasAlreadySealed) {
                for (session in openWriteSessions) {
                    if (!session.closed) {
                        throw SecurityException("Files still open")
                    }
                }
                sealed = true
            }
            clientProgress_ = 1.0f
            updateProgress(true)
        }
        openCount.incrementAndGet()
        // Was `new VPackageInstallerService.d(context, intentSender, sessionId).a` —
        // `.a` is inherited from `bg5` (the base class), which provides the
        // `IPackageInstallObserver2` that receives the install result.
        val observer = VPackageInstallerService.PackageInstallResultSender(
            context,
            intentSender!!,
            sessionId
        ).observerStub
        handler.obtainMessage(MSG_COMMIT, observer).sendToTarget()
    }

    @Throws(RemoteException::class)
    override fun getNames(): Array<String>? {
        checkPrepared("getNames")
        return try {
            resolveStageDir().list()
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }

    @Throws(RemoteException::class)
    override fun openRead(name: String?): ParcelFileDescriptor? {
        return try {
            openReadInternal(name)
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }

    @Throws(RemoteException::class)
    override fun openWrite(
        name: String?,
        offsetBytes: Long,
        lengthBytes: Long,
    ): ParcelFileDescriptor? {
        return try {
            openWriteInternal(name, offsetBytes, lengthBytes)
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }

    @Throws(RemoteException::class)
    override fun removeSplit(splitName: String?) {
        if (TextUtils.isEmpty(parameters.appPackageName)) {
            throw IllegalStateException("Must specify package name to remove a split")
        }
        try {
            markSplitAsRemoved(splitName)
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }

    @Throws(RemoteException::class)
    override fun setClientProgress(f: Float) {
        synchronized(lock) {
            val wasIdle = clientProgress_ == 0.0f
            clientProgress_ = f
            updateProgress(wasIdle)
        }
    }

    // ===== Private helpers =====

    /**
     * Was `T`. Throws if [prepared] is false or [sealed] is true. Guards entry
     * to operations that require a prepared-but-not-yet-committed state.
     */
    private fun checkPrepared(opName: String) {
        synchronized(lock) {
            if (!prepared) {
                throw IllegalStateException("$opName before prepared")
            }
            if (sealed) {
                throw SecurityException("$opName not allowed after commit")
            }
        }
    }

    /**
     * Was `U`. Computes total progress as
     * `min(clientProgress * 0.8, 0.8) + min(installProgress * 0.2, 0.2)`
     * then broadcasts an update if [force] is true or the progress changed by
     * at least 0.01.
     */
    private fun updateProgress(force: Boolean) {
        var clientPart = clientProgress_ * 0.8f
        clientPart = when {
            clientPart < 0.0f -> 0.0f
            clientPart <= 0.8f -> clientPart
            else -> 0.8f
        }
        var installPart = installProgress * 0.2f
        installPart = when {
            installPart < 0.0f -> 0.0f
            installPart > 0.2f -> 0.2f
            else -> installPart
        }
        val total = installPart + clientPart
        totalProgress = total
        if (force || Math.abs(total - lastBroadcastProgress) >= 0.01f) {
            lastBroadcastProgress = total
            VPackageInstallerService.get().callbackHandler
                .obtainMessage(MSG_SESSION_PROGRESS_CHANGED, sessionId, userId, total)
                .sendToTarget()
        }
    }

    /**
     * Was `V`. Creates a `.removed` marker file for the named split so that
     * later processing (by the install helper) treats it as removed.
     */
    @Throws(IOException::class)
    private fun markSplitAsRemoved(splitName: String?) {
        try {
            val markerName = "$splitName.removed"
            if (!FileUtils.isValidFileName(markerName)) {
                throw IllegalArgumentException("Invalid marker: $markerName")
            }
            val file = File(resolveStageDir(), markerName)
            file.createNewFile()
            Os.chmod(file.absolutePath, 0)
        } catch (e: ErrnoException) {
            throw IOException(e)
        }
    }

    /**
     * Was `W`. Sets sealed=true and destroyed=true, closes every open write
     * session, and deletes the stage directory.
     */
    fun destroy() {
        synchronized(lock) {
            sealed = true
            destroyed = true
            for (session in openWriteSessions) {
                session.closeBridge()
            }
        }
        stageDir?.let {
            FileUtils.deleteRecursively(File(it.absolutePath))
        }
    }

    /**
     * Was `X`. Delivers the install result to the observer (if any) and to
     * registered callbacks, then posts a session-cleanup task on the session
     * handler (which removes the session from the sessions map — that task is
     * the `c` class referenced in the decompiled source).
     */
    fun deliverResult(message: String?, resultCode: Int, extras: Bundle?) {
        packageInstallObserver?.let {
            try {
                it.onPackageInstalled(appPackageName, resultCode, message, extras)
            } catch (_: RemoteException) {
            }
        }
        val success = resultCode == 1
        VPackageInstallerService.get().callbackHandler
            .obtainMessage(MSG_SESSION_FINISHED, sessionId, userId, success)
            .sendToTarget()
        // Was `VPackageInstallerService.get().sessionHandler.post(new c(sessionToken, this))`.
        // The `c` class was a session-cleanup Runnable in the original source
        // (not in this decompiled dump). If you have it, substitute here;
        // otherwise this is a best-effort no-op that keeps the session in the
        // map — safer than crashing on an unresolved reference.
        // VPackageInstallerService.get().sessionHandler.post(...)
    }

    /**
     * Was `Y`. Opens a name-addressed file in the stage directory for reading.
     */
    @Throws(IOException::class)
    private fun openReadInternal(name: String?): ParcelFileDescriptor {
        checkPrepared("openRead")
        try {
            if (FileUtils.isValidFileName(name)) {
                return ParcelFileDescriptor.dup(
                    Os.open(
                        File(resolveStageDir(), name).absolutePath,
                        OsConstants.O_RDONLY, 0,
                    )
                )
            }
            throw IllegalArgumentException("Invalid name: $name")
        } catch (e: ErrnoException) {
            throw IOException(e)
        }
    }

    /**
     * Was `Z`. Opens a name-addressed file in the stage directory for writing,
     * with pre-allocation ([lengthBytes] > 0) and seek ([offsetBytes] > 0).
     * Tracks the FD in [openWriteSessions] so [destroy] can close it.
     */
    @Throws(IOException::class)
    private fun openWriteInternal(
        name: String?,
        offsetBytes: Long,
        lengthBytes: Long,
    ): ParcelFileDescriptor {
        val session: FileBridge
        synchronized(lock) {
            checkPrepared("openWrite")
            session = FileBridge()
            openWriteSessions.add(session)
        }
        try {
            val fd = Os.open(
                File(resolveStageDir(), name).absolutePath,
                OsConstants.O_CREAT or OsConstants.O_WRONLY,
                420, // = 0644 octal
            )
            if (lengthBytes > 0) {
                Os.posix_fallocate(fd, 0L, lengthBytes)
            }
            if (offsetBytes > 0) {
                Os.lseek(fd, offsetBytes, OsConstants.SEEK_SET)
            }
            session.targetFd = fd
            session.start()
            return ParcelFileDescriptor.dup(session.callerFd)
        } catch (e: ErrnoException) {
            throw IOException(e)
        }
    }

    /**
     * Was `a0`. Resolves and caches the stage directory. If [stageDir] exists,
     * creates it on first access.
     */
    @Throws(IOException::class)
    private fun resolveStageDir(): File {
        synchronized(lock) {
            if (resolvedStageDir == null) {
                resolvedStageDir = stageDir
                if (stageDir != null && !stageDir.exists()) {
                    stageDir.mkdirs()
                }
            }
            return resolvedStageDir!!
        }
    }

    // ===== Companion =====

    companion object {
        // Message codes for the session's own handler. Only MSG_COMMIT is used.
        private const val MSG_COMMIT = 0

        // Message codes for VPackageInstallerService.callbackHandler. Must match
        // CallbackHandler's own constants — see the earlier conversion of
        // VPackageInstallerService. Duplicated here because they're private
        // in that class's companion.
        private const val MSG_SESSION_ACTIVE_CHANGED = 3
        private const val MSG_SESSION_PROGRESS_CHANGED = 4
        private const val MSG_SESSION_FINISHED = 5

        // PackageManager error codes used in this file.
        private const val ERROR_CODE_ABORTED = -115
        private const val ERROR_CODE_INTERNAL = -110
        private const val ERROR_CODE_INVALID_APK = -2
        private const val ERROR_CODE_SUCCESS = 1

        /**
         * Was `getCompleteMessage(Throwable)`. Concatenates the message of the
         * throwable and every cause in the chain, joined by `": "`.
         */
        @JvmStatic
        fun getCompleteMessage(th: Throwable): String {
            val sb = StringBuilder()
            var current: Throwable? = th
            sb.append(current?.message)
            while (true) {
                current = current?.cause ?: return sb.toString()
                sb.append(": ")
                sb.append(current.message)
            }
        }

        /**
         * Was static `v(PackageInstallerSession)`. Validates the session is sealed
         * and not destroyed, collects staged APKs into [stagedApks], ensures a
         * `base.apk` exists, then kicks off the actual install via the
         * `InstallerTask` (the top-level `a` class in this package).
         */
        @JvmStatic
        @Throws(PackageManagerException::class)
        fun doCommitWork(session: PackageInstallerSession) {
            if (session.destroyed) {
                throw session.PackageManagerException(ERROR_CODE_INTERNAL, "Session destroyed")
            }
            if (!session.sealed) {
                throw session.PackageManagerException(ERROR_CODE_INTERNAL, "Session not sealed")
            }
            try {
                session.resolveStageDir()
            } catch (_: IOException) {
            }
            session.resolvedBaseApk = null
            session.stagedApks.clear()

            val files = session.resolvedStageDir!!.listFiles()
            if (files == null || files.isEmpty()) {
                throw session.PackageManagerException(ERROR_CODE_INVALID_APK, "No packages staged")
            }
            for (file in files) {
                if (file.isDirectory) continue
                val baseApk = File(session.resolvedStageDir!!, "base.apk")
                if (file != baseApk) {
                    file.renameTo(baseApk)
                }
                session.resolvedBaseApk = baseApk
                session.stagedApks.add(baseApk)
            }
            if (session.resolvedBaseApk == null) {
                throw session.PackageManagerException(
                    ERROR_CODE_INVALID_APK,
                    "Full install must include a base package",
                )
            }
            session.installProgress = 0.5f
            session.updateProgress(true)
            // Was `new com.polestar.clone.server.pm.installer.a(packageInstallerSession)` —
            // top-level `a` class in this package, likely `SessionInstaller` or
            // `InstallHelper`. Update once that class is converted.
            SessionInstallObserver(session)
        }
    }
}
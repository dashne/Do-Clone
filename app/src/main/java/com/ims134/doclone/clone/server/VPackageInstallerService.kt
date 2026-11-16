package com.ims134.doclone.clone.server

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.os.RemoteCallbackList
import android.os.RemoteException
import android.text.TextUtils
import android.util.SparseArray
import androidx.core.util.ObjectsCompat
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.VBinder
import com.ims134.doclone.clone.VEnvironment
import com.ims134.doclone.clone.VParceledListSlice
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.io.IPackageInstaller
import com.ims134.doclone.io.IPackageInstallerCallback
import com.ims134.doclone.io.IPackageInstallerSession
import com.ims134.doclone.io.PackageInstallObserver
import com.ims134.doclone.io.Singleton
import java.io.File
import java.io.IOException
import java.security.SecureRandom
import java.util.ArrayList
import androidx.core.util.size

/* JADX INFO: renamed from: com.polestar.clone.server.pm.installer.b */
/* JADX INFO: compiled from: VPackageInstallerService.java */
/* JADX INFO: loaded from: classes2.dex */
@TargetApi(21)
class VPackageInstallerService : IPackageInstaller.Stub() {

    /** Was `a`. Session ID generator. */
    val secureRandom = SecureRandom()

    /** Was `b`. Session ID → PackageInstallerSession. */
    val sessions = SparseArray<PackageInstallerSession>()

    /** Was `c`. Handler for session worker thread (opens/abandons, timer ops). */
    val sessionHandler: Handler

    /** Was `d`. Handler for broadcasting callback events. */
    val callbackHandler: CallbackHandler

    /** Was `e`. Marker object passed to session constructor. */
    val sessionToken = SessionToken()

    /** Was `f`. Context. */
    val context: Context = appContext

    init {
        val thread = HandlerThread("PackageInstaller")
        thread.start()
        sessionHandler = Handler(thread.looper)
        callbackHandler = CallbackHandler(thread.looper)
    }

    // ===== Helper methods =====

    /** Was `T(int)`. Opens the session, throws if not found. */
    @Throws(IOException::class)
    fun openSessionInternal(sessionId: Int): PackageInstallerSession {
        synchronized(sessions) {
            val session = sessions.get(sessionId)
                ?: throw SecurityException("Caller has no access to session $sessionId")
            session.open()
            return session
        }
    }

    /**
     * Was `v(SessionParams, String, int)`. Allocates a fresh session ID (via
     * `SecureRandom`, retrying up to 32 times if the ID is already in use),
     * enforces the 1024-sessions-per-uid cap, creates the session, registers it,
     * fires `onSessionCreated` callback, returns the ID.
     */
    @Throws(IOException::class)
    fun createSessionInternal(
        sessionParams: SessionParams,
        installerPackageName: String?,
        userId: Int,
    ): Int {
        val callingUid = VBinder.getUidByPid()
        val session: PackageInstallerSession
        var newSessionId: Int

        synchronized(sessions) {
            // Count active sessions for this uid
            var activeCount = 0
            for (i in 0 until sessions.size) {
                if (sessions.valueAt(i).callingUid_ == callingUid) activeCount++
            }
            if (activeCount >= 1024) {
                throw IllegalStateException("Too many active sessions for UID $callingUid")
            }

            var attempts = 0
            while (true) {
                newSessionId = secureRandom.nextInt(2147483646) + 1
                if (sessions.get(newSessionId) == null) {
                    val sessionDir = File(VEnvironment.dataDir, ".session_dir")
                    VEnvironment.getOrCreatePath(sessionDir)
                    session = PackageInstallerSession(
                        sessionToken, context, sessionHandler.looper,
                        installerPackageName, newSessionId, userId,
                        callingUid, sessionParams, sessionDir,
                    )
                    break
                }
                attempts++
                if (attempts >= 32) {
                    throw IllegalStateException("Failed to allocate session ID")
                }
            }
        }

        callbackHandler.obtainMessage(MSG_SESSION_CREATED, session.sessionId, session.userId)
            .sendToTarget()
        return newSessionId
    }

    // ===== IPackageInstaller implementations =====

    @Throws(RemoteException::class)
    override fun abandonSession(sessionId: Int) {
        synchronized(sessions) {
            val session = sessions.get(sessionId)
                ?: throw SecurityException("Caller has no access to session $sessionId")
            session.abandon()
        }
    }

    @Throws(RemoteException::class)
    override fun createSession(
        sessionParams: SessionParams?,
        installerPackageName: String?,
        userId: Int,
    ): Int {
        if (sessionParams == null) throw IllegalStateException("Null SessionParams")
        return try {
            createSessionInternal(sessionParams, installerPackageName, userId)
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }

    @Throws(RemoteException::class)
    override fun getAllSessions(userId: Int): VParceledListSlice<SessionInfo>? {
        val list = ArrayList<SessionInfo>()
        synchronized(sessions) {
            for (i in 0 until sessions.size) {
                val session = sessions.valueAt(i)
                if (session.userId == userId) list.add(session.generateInfo())
            }
        }
        return VParceledListSlice(list)
    }

    @Throws(RemoteException::class)
    override fun getMySessions(installerPackageName: String?, userId: Int): VParceledListSlice<SessionInfo>? {
        val list = ArrayList<SessionInfo>()
        synchronized(sessions) {
            for (i in 0 until sessions.size) {
                val session = sessions.valueAt(i)
                if (ObjectsCompat.equals(session.installerPackageName, installerPackageName) && session.userId == userId) {
                    list.add(session.generateInfo())
                }
            }
        }
        return VParceledListSlice(list)
    }

    @Throws(RemoteException::class)
    override fun getSessionInfo(sessionId: Int): SessionInfo? {
        synchronized(sessions) {
            val session = sessions.get(sessionId) ?: return null
            return session.generateInfo()
        }
    }

    @Throws(RemoteException::class)
    override fun openSession(sessionId: Int): IPackageInstallerSession? {
        return try {
            openSessionInternal(sessionId)
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }

    @Throws(RemoteException::class)
    override fun registerCallback(callback: IPackageInstallerCallback?, userId: Int) {
        callbackHandler.callbacks.register(callback, VUserHandle(userId))
    }

    @Throws(RemoteException::class)
    override fun setPermissionsResult(sessionId: Int, accepted: Boolean) {
        synchronized(sessions) {
            val session = sessions.get(sessionId) ?: return
            session.onPermissionsResult(accepted)
        }
    }

    @Throws(RemoteException::class)
    override fun uninstall(
        packageName: String?,
        installerPackageName: String?,
        flags: Int,
        statusReceiver: IntentSender?,
        userId: Int,
    ) {
        val success = VAppManagerService.get()!!.uninstallPackage(packageName)
        if (statusReceiver != null) {
            val intent = Intent().apply {
                putExtra("android.content.pm.extra.PACKAGE_NAME", packageName)
                putExtra("android.content.pm.extra.STATUS", if (success) 0 else 1)
                putExtra("android.content.pm.extra.STATUS_MESSAGE", if (success) "DELETE_SUCCEEDED" else "DELETE_FAILED")
                putExtra("android.content.pm.extra.LEGACY_STATUS", if (success) 1 else -1)
            }
            try {
                statusReceiver.sendIntent(context, 0, intent, null, null)
            } catch (_: IntentSender.SendIntentException) {
            }
        }
    }

    @Throws(RemoteException::class)
    override fun unregisterCallback(callback: IPackageInstallerCallback?) {
        callbackHandler.callbacks.unregister(callback)
    }

    override fun updateSessionAppIcon(sessionId: Int, appIcon: Bitmap?) {
        synchronized(sessions) {
            val session = sessions.get(sessionId)
                ?: throw SecurityException("Caller has no access to session $sessionId")
            val params = session.parameters
            params.appIcon = appIcon
            params.appIconLastModified = -1L
            callbackHandler.obtainMessage(MSG_SESSION_BADGING_CHANGED, session.sessionId, session.userId)
                .sendToTarget()
        }
    }

    @Throws(RemoteException::class)
    override fun updateSessionAppLabel(sessionId: Int, appLabel: String?) {
        synchronized(sessions) {
            val session = sessions.get(sessionId)
                ?: throw SecurityException("Caller has no access to session $sessionId")
            session.parameters.appLabel = appLabel
            callbackHandler.obtainMessage(MSG_SESSION_BADGING_CHANGED, session.sessionId, session.userId)
                .sendToTarget()
        }
    }

    // ===== Companion =====

    companion object {
        /** Was `g`. Singleton holder. */
        @JvmField
        val instance: Singleton<VPackageInstallerService> = object : Singleton<VPackageInstallerService>() {
            override fun create(): VPackageInstallerService = VPackageInstallerService()
        }

        private const val MSG_SESSION_CREATED = 1
        private const val MSG_SESSION_BADGING_CHANGED = 2

        /** Was `get()`. */
        @JvmStatic
        fun get(): VPackageInstallerService = instance.get()!!
    }

    // ===== Inner classes =====

    /** Was inner class `c`. Empty marker object — used as a session token. */
    inner class SessionToken

    /** Was inner class `b` / `HandlerC0218b`. Dispatches callback events to registered callbacks. */
    class CallbackHandler(looper: Looper) : Handler(looper) {

        /** Was `a`. */
        val callbacks: RemoteCallbackList<IPackageInstallerCallback> = RemoteCallbackList()

        override fun handleMessage(msg: Message) {
            val userId = msg.arg2
            val count = callbacks.beginBroadcast()
            for (i in 0 until count) {
                val callback = callbacks.getBroadcastItem(i)
                val vu =callbacks.getBroadcastCookie(i) as VUserHandle
                if (userId == vu.userId) {
                    try {
                        dispatchToCallback(callback, msg)
                    } catch (_: RemoteException) {
                    }
                }
            }
            callbacks.finishBroadcast()
        }

        companion object {
            /** Was static `a`. Dispatches one callback event by [Message.what]. */
            @JvmStatic
            @Throws(RemoteException::class)
            fun dispatchToCallback(callback: IPackageInstallerCallback, message: Message) {
                val sessionId = message.arg1
                when (message.what) {
                    MSG_SESSION_CREATED -> callback.onSessionCreated(sessionId)
                    MSG_SESSION_BADGING_CHANGED -> callback.onSessionBadgingChanged(sessionId)
                    MSG_SESSION_ACTIVE_CHANGED -> callback.onSessionActiveChanged(sessionId, message.obj as Boolean)
                    MSG_SESSION_PROGRESS_CHANGED -> callback.onSessionProgressChanged(sessionId, message.obj as Float)
                    MSG_SESSION_FINISHED -> callback.onSessionFinished(sessionId, message.obj as Boolean)
                }
            }

            private const val MSG_SESSION_CREATED = 1
            private const val MSG_SESSION_BADGING_CHANGED = 2
            private const val MSG_SESSION_ACTIVE_CHANGED = 3
            private const val MSG_SESSION_PROGRESS_CHANGED = 4
            private const val MSG_SESSION_FINISHED = 5
        }
    }

    /**
     * Was inner class `d` (extends `bg5`). Sends install-status Intents to the
     * caller's `IntentSender`. Maps `PackageManager.INSTALL_*` error codes to
     * `PackageInstaller.STATUS_*` codes and to human-readable status messages.
     */
    class PackageInstallResultSender(
        private val context: Context,
        private val sender: IntentSender,
        private val sessionId: Int,
    ) : PackageInstallObserver() {

        @Suppress("UNUSED_PARAMETER")
        override fun onPackageInstalled(packageName: String?, returnCode: Int, message: String?, extras: Bundle?) {
            val intent = Intent().apply {
                putExtra("android.content.pm.extra.PACKAGE_NAME", packageName)
                putExtra("android.content.pm.extra.SESSION_ID", sessionId)
            }

            // Map returnCode → PackageInstaller.STATUS_* constant.
            //
            // NOTE: the decompiled source has a fall-through-heavy nested switch
            // here — after JADX reconstruction, the effective behavior is:
            //   -115 (ABORTED) → 3 (FAILURE_ABORTED)
            //   1 (SUCCESS) → 0 (SUCCESS)
            //   -113, -111 → 7 (FAILURE_INVALID)
            //   -112 → 5 (FAILURE_CONFLICT)
            //   everything else → 4 (FAILURE_BLOCKED)
            //
            // The inner-switch assignments to 6 (INCOMPATIBLE) and 1 (FAILURE)
            // are dead because they fall through to the final `i2 = 4` block
            // before the outer switch exits. If this seems wrong for your app's
            // tests, restore the per-code mapping using the message table below
            // as a guide.
            val status: Int = when (returnCode) {
                -115 -> 3
                1 -> 0
                -113, -111 -> 7
                -112 -> 5
                else -> 4
            }
            intent.putExtra("android.content.pm.extra.STATUS", status)

            // Map returnCode → human-readable status message.
            var statusMessage: String = when (returnCode) {
                -115 -> "INSTALL_FAILED_ABORTED"
                1 -> "INSTALL_SUCCEEDED"
                -113 -> "INSTALL_FAILED_NO_MATCHING_ABIS"
                -112 -> "INSTALL_FAILED_DUPLICATE_PERMISSION"
                -111 -> "INSTALL_FAILED_USER_RESTRICTED"
                -110 -> "INSTALL_FAILED_INTERNAL_ERROR"
                -109 -> "INSTALL_PARSE_FAILED_MANIFEST_EMPTY"
                -108 -> "INSTALL_PARSE_FAILED_MANIFEST_MALFORMED"
                -107 -> "INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID"
                -106 -> "INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME"
                -105 -> "INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING"
                -104 -> "INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES"
                -103 -> "INSTALL_PARSE_FAILED_NO_CERTIFICATES"
                -102 -> "INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION"
                -101 -> "INSTALL_PARSE_FAILED_BAD_MANIFEST"
                -100 -> "INSTALL_PARSE_FAILED_NOT_APK"
                -25 -> "INSTALL_FAILED_VERSION_DOWNGRADE"
                -24 -> "INSTALL_FAILED_UID_CHANGED"
                -23 -> "INSTALL_FAILED_PACKAGE_CHANGED"
                -22 -> "INSTALL_FAILED_VERIFICATION_FAILURE"
                -21 -> "INSTALL_FAILED_VERIFICATION_TIMEOUT"
                -20 -> "INSTALL_FAILED_MEDIA_UNAVAILABLE"
                -19 -> "INSTALL_FAILED_INVALID_INSTALL_LOCATION"
                -18 -> "INSTALL_FAILED_CONTAINER_ERROR"
                -17 -> "INSTALL_FAILED_MISSING_FEATURE"
                -16 -> "INSTALL_FAILED_CPU_ABI_INCOMPATIBLE"
                -15 -> "INSTALL_FAILED_TEST_ONLY"
                -14 -> "INSTALL_FAILED_NEWER_SDK"
                -13 -> "INSTALL_FAILED_CONFLICTING_PROVIDER"
                -12 -> "INSTALL_FAILED_OLDER_SDK"
                -11 -> "INSTALL_FAILED_DEXOPT"
                -10 -> "INSTALL_FAILED_REPLACE_COULDNT_DELETE"
                -9 -> "INSTALL_FAILED_MISSING_SHARED_LIBRARY"
                -8 -> "INSTALL_FAILED_SHARED_USER_INCOMPATIBLE"
                -7 -> "INSTALL_FAILED_UPDATE_INCOMPATIBLE"
                -6 -> "INSTALL_FAILED_NO_SHARED_USER"
                -5 -> "INSTALL_FAILED_DUPLICATE_PACKAGE"
                -4 -> "INSTALL_FAILED_INSUFFICIENT_STORAGE"
                -3 -> "INSTALL_FAILED_INVALID_URI"
                -2 -> "INSTALL_FAILED_INVALID_APK"
                -1 -> "INSTALL_FAILED_ALREADY_EXISTS"
                else -> returnCode.toString()
            }
            if (message != null) {
                statusMessage = "$statusMessage: $message"
            }
            intent.putExtra("android.content.pm.extra.STATUS_MESSAGE", statusMessage)
            intent.putExtra("android.content.pm.extra.LEGACY_STATUS", returnCode)

            if (extras != null) {
                val existingPackage = extras.getString("android.content.pm.extra.FAILURE_EXISTING_PACKAGE")
                if (!TextUtils.isEmpty(existingPackage)) {
                    intent.putExtra("android.content.pm.extra.OTHER_PACKAGE_NAME", existingPackage)
                }
            }

            try {
                sender.sendIntent(context, 0, intent, null, null)
            } catch (_: IntentSender.SendIntentException) {
            }
        }

        override fun onUserActionRequired(intent: Intent?) {
            val sendIntent = Intent().apply {
                putExtra("android.content.pm.extra.SESSION_ID", sessionId)
                putExtra("android.content.pm.extra.STATUS", -1)
                putExtra("android.intent.extra.INTENT", intent)
            }
            try {
                sender.sendIntent(context, 0, sendIntent, null, null)
            } catch (_: IntentSender.SendIntentException) {
            }
        }
    }
}
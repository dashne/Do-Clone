package com.ims134.doclone.clone

import android.content.Intent
import android.content.pm.ProviderInfo
import android.os.IBinder
import android.os.IInterface
import android.os.RemoteException
import android.util.Log
import com.ims134.doclone.io.ActivityClientRecord
import com.ims134.doclone.io.ActivityThread
import com.ims134.doclone.io.ContentProviderNative
import java.util.Collections
import java.util.WeakHashMap

/**
 * Client side of the virtual ActivityManager, used inside the virtual app processes
 * (was com.polestar.clone.client.ipc.VActivityManager). Keeps the Binder of the server's IActivityManager and
 * the death-watching of the tokens the server gives out.
 */
class VActivityManager private constructor() {

    /** was b */
    @Volatile
    private var service: IActivityManager? = null

    /** was a: virtual activity records by their token */
    @JvmField
    val activityRecords = HashMap<IBinder, ActivityClientRecord>(6)

    /** was c */
    private val tokenTrackers: MutableMap<IBinder, TokenDeathTracker> = Collections.synchronizedMap(WeakHashMap())

    /** Is a Binder a virtual service token, and is it still alive on the server side? */
    enum class ServiceTokenState {
        /** was a: not a token of the virtual ActivityManager */
        NOT_VIRTUAL,

        /** was b */
        CURRENT,

        /** was c: the token's process died */
        STALE,
    }

    /** was VActivityManager$a: the server died, forget it */
    private inner class ServiceDeathRecipient(private val manager: IActivityManager) : IBinder.DeathRecipient {
        override fun binderDied() {
            manager.asBinder().unlinkToDeath(this, 0)
            clearServiceIfSame(manager.asBinder())
        }
    }

    /** was VActivityManager$b: a Binder given to setService died */
    private inner class TokenDeathRecipient(private val binder: IBinder) : IBinder.DeathRecipient {
        override fun binderDied() {
            binder.unlinkToDeath(this, 0)
            clearServiceIfSame(binder)
        }
    }

    /** was VActivityManager$c */
    class TokenDeathTracker : IBinder.DeathRecipient {
        @Volatile
        var dead = false // was a

        override fun binderDied() {
            dead = true
        }
    }

    /** was a(VActivityManager, IBinder) */
    private fun clearServiceIfSame(binder: IBinder) {
        synchronized(VActivityManager::class.java) {
            val current = service
            if (current != null && current.asBinder() === binder) {
                service = null
            }
        }
    }

    /** was b */
    @Throws(RemoteException::class)
    fun acquireProviderClient(userId: Int, info: ProviderInfo?): IInterface? =
        ContentProviderNative.asInterface!!.call(getService().acquireProviderClient(userId, info))

    /** was c: tell the server a broadcast finished; failures are ignored */
    fun broadcastFinish(data: PendingResultData?) {
        val manager = getServiceIfAlive() ?: return
        try {
            manager.broadcastFinish(data)
        } catch (_: Exception) {
        }
    }

    /** was Styleable */
    fun getActivityRecord(token: IBinder?): ActivityClientRecord? {
        synchronized(activityRecords) {
            return if (token == null) null else activityRecords[token]
        }
    }

    /** was e: the connected manager, or null if there is none or its process is dead */
    fun getServiceIfAlive(): IActivityManager? {
        val manager = service ?: return null
        return try {
            if (manager.asBinder().isBinderAlive) manager else null
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * was f: the server's IActivityManager; looked up from the service manager if there is none or it died.
     * The original returned null when the lookup failed (callers then crashed with a NullPointerException); this
     * throws a RemoteException instead.
     */
    @Throws(RemoteException::class)
    fun getService(): IActivityManager {
        val cached = service
        if (cached != null && cached.asBinder().isBinderAlive) return cached
        synchronized(VActivityManager::class.java) {
            val again = service
            if (again != null && again.asBinder().isBinderAlive) return again
            val manager = IActivityManager.Stub.asInterface(ServiceManagerNative.getService("activity"))
                ?: throw RemoteException("Unable to get the virtual ActivityManager")
            try {
                manager.asBinder().linkToDeath(ServiceDeathRecipient(manager), 0)
            } catch (_: Throwable) {
            }
            service = manager
            return manager
        }
    }

    /** was g */
    fun getServiceTokenState(token: IBinder?): ServiceTokenState {
        if (token == null) return ServiceTokenState.NOT_VIRTUAL
        val tracker = synchronized(tokenTrackers) { tokenTrackers[token] }
        if (tracker != null) {
            return if (tracker.dead) ServiceTokenState.STALE else ServiceTokenState.CURRENT
        }
        val manager = getServiceIfAlive() ?: return ServiceTokenState.NOT_VIRTUAL
        return try {
            if (manager.isVAServiceToken(token)) {
                if (getOrCreateTracker(token).dead) ServiceTokenState.STALE else ServiceTokenState.CURRENT
            } else {
                ServiceTokenState.NOT_VIRTUAL
            }
        } catch (_: RemoteException) {
            ServiceTokenState.NOT_VIRTUAL
        } catch (_: RuntimeException) {
            ServiceTokenState.NOT_VIRTUAL
        }
    }

    /** was h: the server hands over its IActivityManager (VClientImpl.updateActivityManager) */
    fun setService(binder: IBinder?): Boolean {
        if (binder == null) return false
        val manager = IActivityManager.Stub.asInterface(binder) ?: return false
        synchronized(VActivityManager::class.java) {
            val current = service
            if (current != null && current.asBinder() === binder && binder.isBinderAlive) return true
            if (!binder.isBinderAlive) return false
            try {
                binder.linkToDeath(TokenDeathRecipient(binder), 0)
                service = manager
                return true
            } catch (t: Throwable) {
                Log.d(VActivityManager::class.java.simpleName, t.toString())
            }
        }
        return false
    }

    /** was i */
    fun trackToken(token: IBinder?) {
        if (token == null) return
        getOrCreateTracker(token)
    }

    /** was j */
    fun getOrCreateTracker(token: IBinder): TokenDeathTracker {
        synchronized(tokenTrackers) {
            var tracker = tokenTrackers[token]
            if (tracker == null) {
                tracker = TokenDeathTracker()
                try {
                    token.linkToDeath(tracker, 0)
                } catch (_: RemoteException) {
                    tracker.dead = true
                }
                tokenTrackers[token] = tracker
            }
            return tracker
        }
    }

    /** was k: deliver a cancelled result to the activity */
    fun sendCancelResult(token: IBinder, resultWho: String?, requestCode: Int) {
        val record = activityRecords[token]
        if (record == null || record.activity == null) return
        ActivityThread.sendActivityResult!!.call(VirtualCore.gCore.mainThread, token, resultWho, requestCode, 0, Intent())
    }

    /** was l: start an activity in the virtual user [userId] (the original called the parameter 'uid') */
    fun startActivity(userId: Int, intent: Intent) {
        if (userId < 0) return
        val info = VirtualCore.gCore.resolveActivityInfo(userId, intent)
        if (info == null) {
            Log.e("JJJJ", "startActivity not resolved $intent")
        } else {
            getService().startActivity(intent, info, null, null, null, 0, userId)
        }
    }

    companion object {
        /** was Styleable */
        @JvmField
        val instance = VActivityManager()
    }
}
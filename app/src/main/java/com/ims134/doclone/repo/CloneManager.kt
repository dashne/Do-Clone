package com.ims134.doclone.repo

import android.content.pm.PackageManager
import android.util.Log
import androidx.room3.Room
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.ServiceManagerNative
import com.ims134.doclone.clone.VActivityManager
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.io.VUserManager
import com.ims134.doclone.model.Clone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object CloneManager {
    private const val TAG = "CloneManager"
    val dbName = "DoCloneDb"

    // jadx rq0: installPackage flags 40 (create) / 36 (recover, upgrade)
    private const val INSTALL_FLAGS = 40          // 0x28 = USE_HOST_APK(0x20) | UPDATE_IF_NEWER(0x8)
    private const val INSTALL_FLAGS_RECOVER = 36  // 0x24 = USE_HOST_APK(0x20) | UPDATE_IF_EXISTS(0x4)

    lateinit var dao : CloneStore

    init {
        val db = Room.databaseBuilder(appContext,
            CloneDb::class.java,dbName).build()
        dao = db.dao
    }

    // ---- pending installs (jadx CloneAppPropertyCache): the UI shows a spinner while this is non-empty ----
    private val _pending = MutableStateFlow<Set<Long>>(emptySet())
    val pending: StateFlow<Set<Long>> = _pending

    // one add at a time: findFreeUserId + insert must not interleave for the same package
    private val addLock = Mutex()

    // =============================== database ===============================

    /** Insert only (no install). */
    suspend fun insertClone(clone: Clone) : Clone?
    {
        return try
        {
            dao.insertClone(clone)
            clone
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            null
        }
    }

    /**
     * jadx addClone + AppCloneCreator.run: install into the virtual environment first (3 tries),
     * insert the row only if that worked. Returns null on failure.
     * The virtual user id is the one packed into clone.index (Clone.getVUid()).
     */
    suspend fun addClone(clone: Clone) : Clone? = withContext(Dispatchers.IO)
    {
        _pending.value = _pending.value + clone.id
        try {
            var ok = false
            for (attempt in 0 until 3) {
                if (ok) break
                if (attempt > 0) p()
                ok = try {
                    createClone(clone.getVUid(), clone.packageName)
                } catch (t: Throwable) {
                    Log.d(TAG, Log.getStackTraceString(t))
                    false
                }
            }
            if (!ok) null else insertClone(clone)
        } finally {
            _pending.value = _pending.value - clone.id
        }
    }

    /**
     * Pick the user id, build the row and add it. Serialised so two quick taps for the same app
     * cannot get the same user id. (This is the body of PickerViewModel.add that touches the manager.)
     */
    suspend fun addNewClone(template: Clone) : Clone? = addLock.withLock {
        val vuid = findFreeUserId(template.packageName)
        val count = getClones().size
        addClone(template.copy(index = (count and 0xFFFF) or ((vuid and 0xFFFF) shl 16)))
    }

    suspend fun updateClone(clone: Clone) : Clone?
    {
        return try
        {
            dao.updateClone(clone)
            clone
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            null
        }
    }
    suspend fun deleteClone(clone: Clone): Boolean
    {
        return try
        {
            dao.deleteClone(clone)
            true
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            false
        }
    }

    suspend fun getClones(): List<Clone>
    {
        return try
        {
            dao.getClones().first()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getClonesFlow(): Flow<List<Clone>>
    {
        return dao.getClones()
    }

    suspend fun getClones(pkg: String) : List<Clone>
    {
        return try
        {
            dao.getClones(pkg).first()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            emptyList()
        }
    }

    /** FIX: compare the user id (high 16 bits of index), not the packed index. jadx getClonedApp. */
    suspend fun getClone(pkg: String, uid: Int) : Clone? =
        getClones(pkg).firstOrNull { it.getVUid() == uid }

    suspend fun deleteCloneById(id: Long): Boolean
    {
        return try
        {
            dao.deleteCloneById(id)
            true
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            false
        }
    }

    suspend fun removeAllClones(): Boolean
    {
        return try
        {
            dao.deleteAllClones()
            true
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            false
        }
    }

    fun appendInt(i: Int, str: String?): String {
        return "$str:$i"
    }

    fun getIntAfter(str: String?): Int {
        if (str == null) {
            return 0
        }
        val strArrSplit: Array<String?> = str.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (strArrSplit.size == 2) {
            return strArrSplit[1]!!.toInt()
        }
        return 0
    }

    suspend fun findFreeUserId(pkg:String): Int{
        val list = getClones(pkg)
        val set = mutableSetOf<Int>()
        for(c in list)
        {
            set.add(c.getVUid())
        }
        var i = 0
        while (i in set)
        {
            i++
        }
        return i
    }

    // ============================ virtual environment ============================

    /** jadx p(): wake the server provider, wait 300 ms. */
    fun p() {
        try {
            ServiceManagerNative.makeProviderCall()
            Thread.sleep(300L)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (th: Throwable) {
            Log.d(TAG, Log.getStackTraceString(th))
        }
    }

    /**
     * jadx rq0.createClone. [RECONSTRUCTED: jadx printed the user-creation part garbled.]
     * install the package once -> make sure virtual user [userId] exists (3 tries) -> install it for that user.
     */
    private fun createClone(userId: Int, pkg: String): Boolean {
        val core = VirtualCore.gCore
        if (core.getInstalledAppInfo(pkg) == null) {
            val appPath = try {
                appContext.packageManager.getApplicationInfo(pkg, 0).sourceDir
            } catch (_: PackageManager.NameNotFoundException) {
                null
            }
            val result = core.installPackage(pkg, appPath, INSTALL_FLAGS)
            if (!result.installed) {
                Log.e(TAG, "installPackage failed: ${result.error}")
                return false
            }
        }
        val users = VUserManager.get()
        var user = users?.getUserInfo(userId)
        var tries = 0
        while (user == null && tries < 3) {
            user = users?.userManager?.createUser("User ${userId + 1}", 2)
            if (user == null) p()
            tries++
        }
        if (user == null) return false
        return core.isAppInstalledAsUser(userId, pkg) || core.installPackageAsUser(userId, pkg)
    }

    /** jadx pq0: report clones whose virtual install is missing. Reports only; the repair is in launchClone. */
    suspend fun verifyClones(): List<Clone> = withContext(Dispatchers.IO) {
        val all = getClones()
        val missing = all.filter {
            try { !VirtualCore.gCore.isAppInstalledAsUser(it.getVUid(), it.packageName) }
            catch (e: Exception) { Log.d(TAG, Log.getStackTraceString(e)); false }
        }
        if (missing.isNotEmpty()) Log.w(TAG, "Missed clone total: ${missing.size}:${all.size}")
        missing
    }

    /** jadx rq0.upgradeClone */
    fun upgradeClone(pkg: String) {
        try {
            val core = VirtualCore.gCore
            val info = core.unhookPackageManager.getPackageInfo(pkg, 0)
            val result = core.getAppManager()?.upgradePackage(info.packageName, info.applicationInfo!!.sourceDir, INSTALL_FLAGS_RECOVER)
            Log.d(TAG, "package upgrade result: $result")
        } catch (e: Exception) {
            Log.e(TAG, "upgradeClone failed", e)
        }
    }

    /** jadx rq0.launchClone: launch; if the clone has no launch intent, re-install it and launch again. */
    fun launchClone(pkg: String, userId: Int) {
        try {
            val core = VirtualCore.gCore
            core.getLaunchIntent(userId, pkg)?.let {
                VActivityManager.instance.startActivity(userId, it)
                return
            }
            val path = try {
                appContext.packageManager.getApplicationInfo(pkg, 0).sourceDir
            } catch (_: PackageManager.NameNotFoundException) {
                null
            }
            if (!core.isAppInstalledAsUser(userId, pkg) && !path.isNullOrEmpty()) {
                val users = VUserManager.get()
                if (users?.getUserInfo(userId) == null) {
                    users?.userManager?.createUserWithUserId("User ${userId + 1}", 2, userId)
                }
                if (core.getInstalledAppInfo(pkg) == null) core.installPackage(pkg, path, INSTALL_FLAGS_RECOVER)
                core.installPackageAsUser(userId, pkg)
            }
            core.getLaunchIntent(userId, pkg)?.let { VActivityManager.instance.startActivity(userId, it) }
        } catch (e: Exception) {
            Log.e(TAG, "launchClone failed", e)
        }
    }
}
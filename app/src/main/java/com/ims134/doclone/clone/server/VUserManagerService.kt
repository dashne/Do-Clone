package com.ims134.doclone.clone.server


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.util.AtomicFile
import android.util.Log
import android.util.SparseArray
import android.util.Xml
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintWriter
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer
import androidx.core.util.size
import com.ims134.doclone.clone.ComponentStatusMgr
import com.ims134.doclone.clone.IStopUserCallback
import com.ims134.doclone.clone.IUserManager
import com.ims134.doclone.clone.VBinder
import com.ims134.doclone.clone.VEnvironment
import com.ims134.doclone.clone.VUserInfo
import com.ims134.doclone.clone.VirtualCore
import androidx.core.content.edit
import com.ims134.doclone.R
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.io.VDeviceManagerService

/**
 * Virtual-user registry (the equivalent of AOSP UserManagerService).
 *
 * Persists `system/users/userlist.xml` plus one `<id>.xml` per user, and creates / removes the
 * per-user data directories.
 *
 * Was: io.dv8
 */
class VUserManagerService(
    private val context: Context,
    private val vpms: VPackageManagerService,
    /** Was setPosition `char[]` used purely as setPosition monitor. */
    private val installLock: Any,
    /** Was setPosition `Map` used purely as setPosition monitor. */
    private val packagesLock: Any,
) : IUserManager.Stub()
{

    private val usersDir = File(VEnvironment.dataDir, USERS_DIR)
    private val userListFile = File(usersDir, "userlist.xml")

    private val users = SparseArray<VUserInfo>()
    private val removingUserIds = HashSet<Int>()

    /** Ids of all users that are not partial (half created / half removed). */
    var ids: IntArray = IntArray(0)
    private var guestEnabled = false
    private var nextSerialNumber = 0
    private var userVersion = 0

    init {
        synchronized(installLock) {
            synchronized(packagesLock) {
                usersDir.mkdirs()
                File(usersDir, "0").mkdirs()
                readUserList()

                // Clean up users that were left half-created by setPosition crash (index 0 is the owner).
                val stale = ArrayList<VUserInfo>()
                for (i in 1 until users.size) {
                    val user = users.valueAt(i)
                    if (user.partial) stale.add(user)
                }
                stale.forEach { removeUserData(it.id) }

                instance = this
            }
        }
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    private fun createOwnerUser() {
        val owner = VUserInfo(0, context.resources.getString(R.string.owner_name), null, OWNER_FLAGS)
        users.put(0, owner)
        nextSerialNumber = 1
        updateUserIds()
        writeUserList()
        writeUser(owner)
    }

    /** Returns the user, or null if it does not exist or is only partially created (and not being removed). */
    private fun getUserInfoLocked(userId: Int): VUserInfo? {
        val user = users.get(userId)
        if (user != null && user.partial && !removingUserIds.contains(userId)) return null
        if (user == null) Log.d(TAG, "not able to find user info of $userId")
        return user
    }

    private fun isUserIdTaken(userId: Int) = users.indexOfKey(userId) >= 0 || removingUserIds.contains(userId)

    private fun updateUserIds() {
        ids = (0 until users.size)
            .filter { !users.valueAt(it).partial }
            .map { users.keyAt(it) }
            .toIntArray()
    }

    /** Fixes up data written by older versions. */
    private fun upgradeIfNecessary() {
        var version = userVersion
        if (version < 1) {
            val owner = users.get(0)
            if ("Primary" == owner.Primary) {
                owner.Primary = "Admin"
                writeUser(owner)
            }
            version = 1
        }
        userVersion = version
        writeUserList()
    }

    /** Forgets setPosition user: package state, xml files, in-memory entry and its data directory. */
    private fun removeUserData(userId: Int) {
        for (pkg in vpms.packages.values)
        {
            pkg.packageSetting?.userState?.delete(userId) // f37839v.f37749i
        }
        users.remove(userId)
        removingUserIds.remove(userId)
        AtomicFile(File(usersDir, "$userId.xml")).delete()
        writeUserList()
        updateUserIds()
        VEnvironment.createUserSubFile(userId).deleteRecursively()
    }

    private fun writeUserPhoto(user: VUserInfo, bitmap: Bitmap) {
        try {
            val userDir = File(usersDir, user.id.toString())
            val photo = File(userDir, "photo.png")
            if (!userDir.exists()) userDir.mkdir()
            FileOutputStream(photo).use { out ->
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    user.iconPath = photo.absolutePath
                }
            }
        } catch (ignored: IOException) {
        }
    }

    // ------------------------------------------------------------------
    // Persistence
    // ------------------------------------------------------------------

    private fun XmlPullParser.intAttr(name: String, default: Int): Int =
        getAttributeValue(null, name)?.toIntOrNull() ?: default

    private fun XmlPullParser.longAttr(name: String, default: Long): Long =
        getAttributeValue(null, name)?.toLongOrNull() ?: default

    /** Advances to the next START_TAG; returns the event type it stopped on. */
    private fun XmlPullParser.nextStartTag(): Int {
        var type: Int
        do {
            type = next()
        } while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT)
        return type
    }

    private fun readUserList() {
        guestEnabled = false
        if (!userListFile.exists()) {
            createOwnerUser()
            return
        }
        try {
            AtomicFile(userListFile).openRead().use { input ->
                val parser = Xml.newPullParser()
                parser.setInput(input, null)

                if (parser.nextStartTag() != XmlPullParser.START_TAG) {
                    Log.e(TAG, "Unable to read user list")
                    createOwnerUser()
                    return
                }

                nextSerialNumber = -1
                if (parser.name == TAG_USERS) {
                    parser.getAttributeValue(null, ATTR_NEXT_SERIAL)?.let { nextSerialNumber = it.toInt() }
                    parser.getAttributeValue(null, ATTR_VERSION)?.let { userVersion = it.toInt() }
                }

                while (true) {
                    val type = parser.next()
                    if (type == XmlPullParser.END_DOCUMENT) break
                    if (type == XmlPullParser.START_TAG && parser.name == TAG_USER) {
                        val user = readUser(parser.getAttributeValue(null, ATTR_ID).toInt()) ?: continue
                        users.put(user.id, user)
                        if ((user.flags and FLAG_GUEST) == FLAG_GUEST) guestEnabled = true
                        if (nextSerialNumber < 0 || nextSerialNumber <= user.id) nextSerialNumber = user.id + 1
                    }
                }
                updateUserIds()
                upgradeIfNecessary()
            }
        } catch (e: Exception) {
            // unreadable / corrupt list: start over with only the owner
            createOwnerUser()
        }
    }

    private fun readUser(userId: Int): VUserInfo? {
        try {
            AtomicFile(File(usersDir, "$userId.xml")).openRead().use { input ->
                val parser = Xml.newPullParser()
                parser.setInput(input, null)

                if (parser.nextStartTag() != XmlPullParser.START_TAG) {
                    Log.e(TAG, "Unable to read user $userId")
                    return null
                }

                var serialNumber = userId
                var flags = 0
                var iconPath: String? = null
                var created = 0L
                var lastLoggedIn = 0L
                var partial = false
                var name: String? = null

                if (parser.name == TAG_USER) {
                    if (parser.intAttr(ATTR_ID, -1) != userId) {
                        Log.e(TAG, "User id does not match the file name")
                        return null
                    }
                    serialNumber = parser.intAttr(ATTR_SERIAL, userId)
                    flags = parser.intAttr(ATTR_FLAGS, 0)
                    iconPath = parser.getAttributeValue(null, ATTR_ICON)
                    created = parser.longAttr(ATTR_CREATED, 0L)
                    lastLoggedIn = parser.longAttr(ATTR_LAST_LOGGED_IN, 0L)
                    partial = "true" == parser.getAttributeValue(null, ATTR_PARTIAL)

                    val type = parser.nextStartTag()
                    if (type == XmlPullParser.START_TAG && parser.name == TAG_NAME &&
                        parser.next() == XmlPullParser.TEXT
                    ) {
                        name = parser.text
                    }
                }

                return VUserInfo(userId, name, iconPath, flags).also {
                    it.serialNumber = serialNumber
                    it.created = created
                    it.lastLoggedIn = lastLoggedIn
                    it.partial = partial
                }
            }
        } catch (e: IOException) {
            return null
        } catch (e: org.xmlpull.v1.XmlPullParserException) {
            return null
        }
    }

    /** Crash-safe write: backup file + rename, via [AtomicFile]. */
    private fun writeXml(file: File, errorMessage: String, body: (XmlSerializer) -> Unit) {
        val atomicFile = AtomicFile(file)
        var out: FileOutputStream? = null
        try {
            out = atomicFile.startWrite()
            val buffered = BufferedOutputStream(out)
            val serializer = Xml.newSerializer()
            serializer.setOutput(buffered, "utf-8")
            serializer.startDocument(null, true)
            serializer.setFeature(INDENT_FEATURE, true)
            body(serializer)
            serializer.endDocument()
            buffered.flush()
            atomicFile.finishWrite(out)
        } catch (e: Exception) {
            Log.e(TAG, "$errorMessage\n$e")
            atomicFile.failWrite(out)
        }
    }

    private fun writeUserList() {
        writeXml(userListFile, "Error writing user list") { xml ->
            xml.startTag(null, TAG_USERS)
            xml.attribute(null, ATTR_NEXT_SERIAL, nextSerialNumber.toString())
            xml.attribute(null, ATTR_VERSION, userVersion.toString())
            for (i in 0 until users.size()) {
                xml.startTag(null, TAG_USER)
                xml.attribute(null, ATTR_ID, users.valueAt(i).id.toString())
                xml.endTag(null, TAG_USER)
            }
            xml.endTag(null, TAG_USERS)
        }
    }

    private fun writeUser(user: VUserInfo) {
        writeXml(File(usersDir, "${user.id}.xml"), "Error writing user info ${user.id}") { xml ->
            xml.startTag(null, TAG_USER)
            xml.attribute(null, ATTR_ID, user.id.toString())
            xml.attribute(null, ATTR_SERIAL, user.serialNumber.toString())
            xml.attribute(null, ATTR_FLAGS, user.flags.toString())
            xml.attribute(null, ATTR_CREATED, user.created.toString())
            xml.attribute(null, ATTR_LAST_LOGGED_IN, user.lastLoggedIn.toString())
            user.iconPath?.let { xml.attribute(null, ATTR_ICON, it) }
            if (user.partial) xml.attribute(null, ATTR_PARTIAL, "true")
            user.Primary?.let {
                xml.startTag(null, TAG_NAME)
                xml.text(it)
                xml.endTag(null, TAG_NAME)
            }
            xml.endTag(null, TAG_USER)
        }
    }

    // ------------------------------------------------------------------
    // IUserManager
    // ------------------------------------------------------------------

    override fun createUser(name: String?, flags: Int): VUserInfo? {
        enforceManageUsers("Only the system can create users")
        return addUser(name, flags, null)
    }

    override fun createUserWithUserId(name: String?, flags: Int, userId: Int): VUserInfo? {
        enforceManageUsers("Only the system can create users")
        return addUser(name, flags, userId)
    }

    /** Shared body of createUser / createUserWithUserId. [fixedUserId] null means "pick the first free id". */
    private fun addUser(name: String?, flags: Int, fixedUserId: Int?): VUserInfo? {
        val token = Binder.clearCallingIdentity()
        try {
            synchronized(installLock) {
                synchronized(packagesLock)
                {
                    val userId = fixedUserId ?: generateSequence(0) { it + 1 }.first { !isUserIdTaken(it) }
                    if (isUserIdTaken(userId)) return null

                    val user = VUserInfo(userId, name, null, flags)
                    user.serialNumber = nextSerialNumber++
                    val now = System.currentTimeMillis()
                    user.created = if (now > MIN_VALID_TIME_MS) now else 0
                    user.partial = true // not usable until the steps below finish
                    VEnvironment.createUserSubFile(userId).mkdirs()
                    users.put(userId, user)
                    writeUserList()
                    writeUser(user)
                    vpms.createNewUser(userId) // m24949V
                    user.partial = false
                    writeUser(user)
                    updateUserIds()

                    if (userId != 0) {
                        VAppManagerService.get()?.checkPreInstallForUser(userId) // fu8
                        VAppManagerService.get()?.sendBootCompleted(userId)
                    }

                    val intent = Intent(ACTION_USER_ADDED).putExtra(EXTRA_USER_HANDLE, userId)
                    VActivityManagerService.get().sendBroadcastAsUser(intent, VUserHandle.instance, null)
                    return user
                }
            }
        } finally {
            Binder.restoreCallingIdentity(token)
        }
    }

    override fun removeUser(userId: Int): Boolean {
        enforceManageUsers("Only the system can remove users")
        synchronized(packagesLock) {
            val user = users.get(userId)
            if (userId == 0 || user == null) return false

            removingUserIds.add(userId)
            user.partial = true
            writeUser(user)

            val ams = VActivityManagerService.get()
            ams.onVirtualUserRemovalStarted(userId)
            return ams.stopUser(userId, StopUserCallback()) == 0
        }
    }

    override fun getUserInfo(userId: Int): VUserInfo? =
        synchronized(packagesLock) { getUserInfoLocked(userId) }

    override fun getUsers(excludeDying: Boolean): List<VUserInfo> =
        synchronized(packagesLock) {
            val result = ArrayList<VUserInfo>(users.size)
            for (i in 0 until users.size) {
                val user = users.valueAt(i)
                if (!user.partial && (!excludeDying || !removingUserIds.contains(user.id))) result.add(user)
            }
            result
        }

    override fun getUserHandle(userSerialNumber: Int): Int =
        synchronized(packagesLock) {
            ids.firstOrNull { getUserInfoLocked(it)?.serialNumber == userSerialNumber } ?: -1
        }

    override fun getUserSerialNumber(userId: Int): Int =
        synchronized(packagesLock) {
            if (!exists(userId)) -1 else getUserInfoLocked(userId)?.serialNumber ?: -1
        }

    override fun getUserIcon(userId: Int): Bitmap? =
        synchronized(packagesLock) {
            val user = users.get(userId)
            if (user == null || user.partial) null
            else user.iconPath?.let { BitmapFactory.decodeFile(it) }
        }

    override fun setUserIcon(userId: Int, icon: Bitmap?) {
        enforceManageUsers("update users")
        synchronized(packagesLock) {
            val user = users.get(userId)
            if (user != null && !user.partial) {
                writeUserPhoto(user, icon!!)
                writeUser(user)
                sendUserChangedBroadcast(userId)
            }
        }
    }

    override fun setUserName(userId: Int, name: String?) {
        enforceManageUsers("rename users")
        synchronized(packagesLock) {
            val user = users.get(userId)
            if (user != null && !user.partial && name != null && name != user.Primary) {
                user.Primary = name
                writeUser(user)
                sendUserChangedBroadcast(userId)
            }
        }
    }

    override fun isGuestEnabled(): Boolean = synchronized(packagesLock) { guestEnabled }

    override fun setGuestEnabled(enable: Boolean) {
        enforceManageUsers("enable guest users")
        synchronized(packagesLock) {
            if (guestEnabled == enable) return
            guestEnabled = enable

            // A guest already exists: remove it when disabling, otherwise nothing to do.
            for (i in 0 until users.size) {
                val user = users.valueAt(i)
                if (!user.partial && (user.flags and FLAG_GUEST) == FLAG_GUEST) {
                    if (!enable) removeUser(user.id)
                    return
                }
            }
            if (enable) createUser("Guest", FLAG_GUEST)
        }
    }

    override fun wipeUser(userId: Int) {
        enforceManageUsers("wipe user")
    }

    // ------------------------------------------------------------------
    // Extra public API (not part of the Binder interface)
    // ------------------------------------------------------------------

    fun exists(userId: Int): Boolean = synchronized(packagesLock) { userId in ids }

    fun getUserIds(): IntArray = synchronized(packagesLock) { ids }

    fun makeInitialized(userId: Int) {
        enforceManageUsers("makeInitialized")
        synchronized(packagesLock) {
            val user = users.get(userId)
            if (user == null || user.partial) return
            if ((user.flags and FLAG_INITIALIZED) == 0) {
                user.flags = user.flags or FLAG_INITIALIZED
                writeUser(user)
            }
        }
    }

    fun userForeground(userId: Int) {
        synchronized(packagesLock) {
            val user = users.get(userId)
            val now = System.currentTimeMillis()
            if (user != null && !user.partial && now > MIN_VALID_TIME_MS) {
                user.lastLoggedIn = now
                writeUser(user)
            }
        }
    }

    override fun dump(fd: FileDescriptor, writer: PrintWriter, args: Array<out String>?) {
    }

    // ------------------------------------------------------------------
    // Broadcasts
    // ------------------------------------------------------------------

    private fun sendUserRemovedBroadcast(userId: Int) {
        val token = Binder.clearCallingIdentity()
        try {
            val intent = Intent(ACTION_USER_REMOVED).putExtra(EXTRA_USER_HANDLE, userId)
            // Once every receiver has seen USER_REMOVED, drop the user's data for good.
            val finalReceiver = object : BroadcastReceiver()
            {
                override fun onReceive(context: Context?, intent: Intent?)
                {
                    synchronized(installLock) {
                        synchronized(packagesLock) { removeUserData(userId) }
                    }
                }
            }
            VActivityManagerService.get().sendOrderedBroadcastAsUser(
                intent, VUserHandle.instance, null, finalReceiver, null, -1, null, null
            )
        } finally {
            Binder.restoreCallingIdentity(token)
        }
    }

    /** Called when the stopped user's processes are gone. */
    private inner class StopUserCallback : IStopUserCallback.Stub()
    {
        override fun userStopped(userId: Int) {
            VDeviceManagerService.get().deleteDeviceInfo(userId) // nu8
            ComponentStatusMgr.get(userId).prefs.edit { clear() } // rv0.m23022a(i).f49062a
            sendUserRemovedBroadcast(userId)
        }

        override fun userStopAborted(userId: Int) {
        }
    }

    companion object {
        private const val TAG = "VUserManagerService"

        const val EXTRA_USER_HANDLE = "android.intent.extra.user_handle"

        /** Relative to [com.ims134.doclone.clone.VEnvironment.dataDir]. Was `zp6.m25474n(StringBuilder("system"), File.separator, "users")`. */
        private val USERS_DIR = "system" + File.separator + "users"

        private const val TAG_USERS = "users"
        private const val TAG_USER = "user"
        private const val TAG_NAME = "name" // was AppMeasurementSdk.C4116a.f31741b

        private const val ATTR_ID = "id"
        private const val ATTR_SERIAL = "serialNumber"
        private const val ATTR_NEXT_SERIAL = "nextSerialNumber"
        private const val ATTR_VERSION = "version"
        private const val ATTR_FLAGS = "flags"
        private const val ATTR_ICON = "icon"
        private const val ATTR_CREATED = "created"
        private const val ATTR_LAST_LOGGED_IN = "lastLoggedIn"
        private const val ATTR_PARTIAL = "partial"

        private const val INDENT_FEATURE = "http://xmlpull.org/v1/doc/features.html#indent-output"

        private const val ACTION_USER_ADDED = "virtual.android.intent.action.USER_ADDED"
        private const val ACTION_USER_REMOVED = "virtual.android.intent.action.USER_REMOVED"
        private const val ACTION_USER_CHANGED = "virtual.android.intent.action.USER_CHANGED"

        private const val FLAG_GUEST = 4
        private const val FLAG_INITIALIZED = 16
        private const val OWNER_FLAGS = 19 // primary | admin | initialized

        /** Timestamps before 2000-01-01 mean the clock was not set yet. */
        private const val MIN_VALID_TIME_MS = 946080000000L

        @Volatile
        @JvmStatic
        var instance: VUserManagerService? = null
            private set

        @JvmStatic
        fun get(): VUserManagerService? = instance

        /** Only the host app (the process that owns the virtual environment) may manage users. */
        @JvmStatic
        fun enforceManageUsers(action: String) {
            if (VBinder.getUidByPid() != VirtualCore.gCore.uid) {
                throw SecurityException("You need MANAGE_USERS permission to: $action")
            }
        }

        @JvmStatic
        fun sendUserChangedBroadcast(userId: Int) {

            val intent = Intent(ACTION_USER_CHANGED)
                .putExtra(EXTRA_USER_HANDLE, userId)
                .addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY)
            VActivityManagerService.get().sendBroadcastAsUser(intent, VUserHandle(userId))
        }
    }
}
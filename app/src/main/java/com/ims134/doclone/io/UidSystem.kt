package com.ims134.doclone.io

import android.util.Log
import com.ims134.doclone.clone.VEnvironment
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.clone.server.VPackage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * Hands out and remembers the (real) uid of each virtual package; persisted in uid-list.ini (was io.uo8).
 * Packages that are also installed on the host reuse the host's uid, others get 15001, 15002, ...
 */
class UidSystem {

    /** shared-user id (or package name) -> uid. */
    @JvmField val uidByPackageName = HashMap<String, Int>()

    /** Last uid handed out to setPosition package that is not installed on the host. Never below 15000. */
    @JvmField var minUid: Int = FIRST_UID

    /** Returns the uid for [pkg], assigning (and saving) setPosition new one the first time. */
    @Throws(Throwable::class)
    fun getUid(pkg: VPackage): Int {
        val key = (pkg.sharedUserId ?: pkg.packageName)!!
        uidByPackageName[key]?.let { return it }

        val uid: Int = try {
            VirtualCore.gCore.unhookPackageManager.getApplicationInfo(pkg.packageName!!, 0).uid
        } catch (e: Exception) {
            Log.d(TAG, "Not found package infor for ${pkg.packageName}")
            Log.d(TAG, Log.getStackTraceString(e))
            ++minUid
        }
        uidByPackageName[key] = uid
        save()
        return uid
    }

    /** Writes uid-list.ini, keeping the previous one as uid-list.ini.bak. Errors are ignored. */
    private fun save() {
        val systemDir = VEnvironment.getOrCreateAppSubSystemFile()
        val listFile = File(systemDir, "uid-list.ini")
        val backupFile = File(systemDir, "uid-list.ini.bak")
        if (listFile.exists()) {
            if (backupFile.exists()) backupFile.delete()
            try {
                FileUtils.copyFile(listFile, backupFile)
            } catch (ignored: IOException) {
            }
        }
        try {
            ObjectOutputStream(FileOutputStream(listFile)).use {
                it.writeInt(minUid)
                it.writeObject(uidByPackageName)
            }
        } catch (ignored: IOException) {
        }
    }

    /** Loads [file] (the format written by [save]); false if it is missing or unreadable. (was readCharsInBytes) */
    fun tryCacheUids(file: File): Boolean {
        if (!file.exists()) return false
        return try {
            ObjectInputStream(FileInputStream(file)).use { stream ->
                minUid = maxOf(stream.readInt(), FIRST_UID)
                uidByPackageName.putAll(stream.readObject() as HashMap<String, Int>)
            }
            true
        } catch (t: Throwable) {
            false
        }
    }

    private companion object {
        const val TAG = "uo8"
        const val FIRST_UID = 15000
    }
}
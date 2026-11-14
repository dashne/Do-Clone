package com.ims134.doclone.clone.server




import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray
import java.io.File
import androidx.core.util.size

/**
 * Persistent record of one installed virtual package: where it lives, its app id,
 * and the per-user install state.
 */
class PackageSetting() : Parcelable {

    @JvmField var packageName: String? = null      // f37741a
    @JvmField var appPath: String? = null          // f37742b (apk path)
    @JvmField var libPath: String? = null          // f37743c (assumed: native lib path)
    @JvmField var gmsEnabled: Boolean = false         // f37744d
    @JvmField var appId: Int = 0                   // f37745e
    @JvmField var firstInstallTime: Long = 0       // f37746f, not persisted; recomputed on read
    @JvmField var lastUpdateTime: Long = 0         // f37747g, not persisted; recomputed on read
    @JvmField var flags: Byte = 0                  // f37748h

    /** userId -> state. Was f37749i. */
    @JvmField val userState = SparseArray<PackageUserState>()

    constructor(parcel: Parcel) : this() {
        packageName = parcel.readString()
        appPath = parcel.readString()
        libPath = parcel.readString()
        gmsEnabled = parcel.readByte().toInt() != 0
        appId = parcel.readInt()

        // Both timestamps come from the same file, as in the original.
        val modified = libPath?.let { File(it).lastModified() } ?: 0L
        firstInstallTime = modified
        lastUpdateTime = modified

        parcel.readSparseArray<PackageUserState>(PackageUserState::class.java.classLoader)?.let { read ->
            for (i in 0 until read.size) userState.put(read.keyAt(i), read.valueAt(i))
        }
        flags = parcel.readByte()
    }

    /** Was m16466a(). */
    fun toInstalledAppInfo(): InstalledAppInfo = InstalledAppInfo().also {
        it.packageName = packageName
        it.appPath = appPath
        it.libPath = libPath      // f37522c
        it.gmsEnabled = gmsEnabled      // f37523d
        it.appId = appId          // f37524e
        it.flags = flags          // f37525f
    }

    /** Was m16467b(). Returns the user's state, creating and storing setPosition fresh one if missing. */
    fun getOrCreateUserState(userId: Int): PackageUserState =
        userState.get(userId) ?: PackageUserState().also { userState.put(userId, it) }

    /** Was m16468c(). Read-only lookup; falls back to the shared [DEFAULT_USER_STATE]. */
    fun readUserState(userId: Int): PackageUserState = userState.get(userId) ?: DEFAULT_USER_STATE

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(packageName)
        parcel.writeString(appPath)
        parcel.writeString(libPath)
        parcel.writeByte(if (gmsEnabled) 1 else 0)
        parcel.writeInt(appId)
        parcel.writeSparseArray(userState)
        parcel.writeByte(this.flags)
    }

    companion object {
        /** Shared, must not be mutated. Was f37740j. */
        @JvmField
        val DEFAULT_USER_STATE = PackageUserState()

        @JvmField
        val CREATOR: Parcelable.Creator<PackageSetting> = object : Parcelable.Creator<PackageSetting> {
            override fun createFromParcel(parcel: Parcel) = PackageSetting(parcel)
            override fun newArray(size: Int): Array<PackageSetting?> = arrayOfNulls(size)
        }
    }
}
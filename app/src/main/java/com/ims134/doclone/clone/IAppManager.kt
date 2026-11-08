package com.ims134.doclone.clone


import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException
import com.ims134.doclone.clone.server.InstalledAppInfo
import com.ims134.doclone.io.IAppRequestListener
import com.ims134.doclone.io.IPackageObserver


/**
 * Binder interface of the virtual app manager (was io.n23, "IAppManager.java").
 * The full method list is taken from the Stub's abstract methods and its onTransact: R8 had removed most of
 * the unused methods from the interface and the Proxy.
 *
 * Transaction codes: see the TRANSACTION_* constants in [CloneAppManager].
 */
interface IAppManager : IInterface {

    @Throws(RemoteException::class)
    fun getPackageInstalledUsers(packageName: String?): IntArray?

    @Throws(RemoteException::class)
    fun scanApps()

    @Throws(RemoteException::class)
    fun addVisibleOutsidePackage(packageName: String?)

    @Throws(RemoteException::class)
    fun removeVisibleOutsidePackage(packageName: String?)

    @Throws(RemoteException::class)
    fun isOutsidePackageVisible(packageName: String?): Boolean

    @Throws(RemoteException::class)
    fun getInstalledAppInfo(packageName: String?, flags: Int): InstalledAppInfo?

    @Throws(RemoteException::class)
    fun installPackage(packageName: String?, apkPath: String?, flags: Int): InstallResult?

    @Throws(RemoteException::class)
    fun upgradePackage(packageName: String?, apkPath: String?, flags: Int): InstallResult?

    @Throws(RemoteException::class)
    fun isPackageLaunched(userId: Int, packageName: String?): Boolean

    @Throws(RemoteException::class)
    fun setPackageHidden(userId: Int, packageName: String?, hidden: Boolean)

    @Throws(RemoteException::class)
    fun installPackageAsUser(userId: Int, packageName: String?): Boolean

    @Throws(RemoteException::class)
    fun uninstallPackageAsUser(packageName: String?, userId: Int): Boolean

    @Throws(RemoteException::class)
    fun uninstallPackage(packageName: String?): Boolean

    @Throws(RemoteException::class)
    fun getInstalledApps(flags: Int): List<InstalledAppInfo>?

    @Throws(RemoteException::class)
    fun getInstalledAppsAsUser(userId: Int, flags: Int): List<InstalledAppInfo>?

    @Throws(RemoteException::class)
    fun getInstalledAppCount(): Int

    @Throws(RemoteException::class)
    fun isAppInstalled(packageName: String?): Boolean

    @Throws(RemoteException::class)
    fun isAppInstalledAsUser(userId: Int, packageName: String?): Boolean

    @Throws(RemoteException::class)
    fun registerObserver(observer: IPackageObserver?)

    @Throws(RemoteException::class)
    fun unregisterObserver(observer: IPackageObserver?)

    @Throws(RemoteException::class)
    fun setAppRequestListener(listener: IAppRequestListener?)

    @Throws(RemoteException::class)
    fun clearAppRequestListener()

    @Throws(RemoteException::class)
    fun getAppRequestListener(): IAppRequestListener?

    @Throws(RemoteException::class)
    fun notifyActivityBeforeResume(packageName: String?, userId: Int)

    @Throws(RemoteException::class)
    fun notifyActivityBeforePause(packageName: String?, userId: Int)

    @Throws(RemoteException::class)
    fun restart()

    abstract class CloneAppManager : Binder(), IAppManager {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        public override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code in 1..16777215) {
                data.enforceInterface(DESCRIPTOR)
            }
            if (code == 1598968902) {
                reply?.writeString(DESCRIPTOR)
                return true
            }
            val out = reply ?: return super.onTransact(code, data, reply, flags)
            when (code) {
                TRANSACTION_getPackageInstalledUsers -> {
                    val users = getPackageInstalledUsers(data.readString())
                    out.writeNoException()
                    out.writeIntArray(users)
                }
                TRANSACTION_scanApps -> {
                    scanApps()
                    out.writeNoException()
                }
                TRANSACTION_addVisibleOutsidePackage -> {
                    addVisibleOutsidePackage(data.readString())
                    out.writeNoException()
                }
                TRANSACTION_removeVisibleOutsidePackage -> {
                    removeVisibleOutsidePackage(data.readString())
                    out.writeNoException()
                }
                TRANSACTION_isOutsidePackageVisible -> {
                    val visible = isOutsidePackageVisible(data.readString())
                    out.writeNoException()
                    out.writeInt(if (visible) 1 else 0)
                }
                TRANSACTION_getInstalledAppInfo -> {
                    val info = getInstalledAppInfo(data.readString(), data.readInt())
                    out.writeNoException()
                    out.writeTypedObject(info, 1)
                }
                TRANSACTION_installPackage -> {
                    val result = installPackage(data.readString(), data.readString(), data.readInt())
                    out.writeNoException()
                    out.writeTypedObject(result, 1)
                }
                TRANSACTION_upgradePackage -> {
                    val result = upgradePackage(data.readString(), data.readString(), data.readInt())
                    out.writeNoException()
                    out.writeTypedObject(result, 1)
                }
                TRANSACTION_isPackageLaunched -> {
                    val launched = isPackageLaunched(data.readInt(), data.readString())
                    out.writeNoException()
                    out.writeInt(if (launched) 1 else 0)
                }
                TRANSACTION_setPackageHidden -> {
                    setPackageHidden(data.readInt(), data.readString(), data.readInt() != 0)
                    out.writeNoException()
                }
                TRANSACTION_installPackageAsUser -> {
                    val ok = installPackageAsUser(data.readInt(), data.readString())
                    out.writeNoException()
                    out.writeInt(if (ok) 1 else 0)
                }
                TRANSACTION_uninstallPackageAsUser -> {
                    val ok = uninstallPackageAsUser(data.readString(), data.readInt())
                    out.writeNoException()
                    out.writeInt(if (ok) 1 else 0)
                }
                TRANSACTION_uninstallPackage -> {
                    val ok = uninstallPackage(data.readString())
                    out.writeNoException()
                    out.writeInt(if (ok) 1 else 0)
                }
                TRANSACTION_getInstalledApps -> {
                    val apps = getInstalledApps(data.readInt())
                    out.writeNoException()
                    writeParcelableList(out, apps)
                }
                TRANSACTION_getInstalledAppsAsUser -> {
                    val apps = getInstalledAppsAsUser(data.readInt(), data.readInt())
                    out.writeNoException()
                    writeParcelableList(out, apps)
                }
                TRANSACTION_getInstalledAppCount -> {
                    val count = getInstalledAppCount()
                    out.writeNoException()
                    out.writeInt(count)
                }
                TRANSACTION_isAppInstalled -> {
                    val installed = isAppInstalled(data.readString())
                    out.writeNoException()
                    out.writeInt(if (installed) 1 else 0)
                }
                TRANSACTION_isAppInstalledAsUser -> {
                    val installed = isAppInstalledAsUser(data.readInt(), data.readString())
                    out.writeNoException()
                    out.writeInt(if (installed) 1 else 0)
                }
                TRANSACTION_registerObserver -> {
                    registerObserver(IPackageObserver.Stub.asInterface(data.readStrongBinder()))
                    out.writeNoException()
                }
                TRANSACTION_unregisterObserver -> {
                    unregisterObserver(IPackageObserver.Stub.asInterface(data.readStrongBinder()))
                    out.writeNoException()
                }
                TRANSACTION_setAppRequestListener -> {
                    setAppRequestListener(IAppRequestListener.Stub.asInterface(data.readStrongBinder()))
                    out.writeNoException()
                }
                TRANSACTION_clearAppRequestListener -> {
                    clearAppRequestListener()
                    out.writeNoException()
                }
                TRANSACTION_getAppRequestListener -> {
                    val listener = getAppRequestListener()
                    out.writeNoException()
                    out.writeStrongInterface(listener)
                }
                TRANSACTION_notifyActivityBeforeResume -> {
                    notifyActivityBeforeResume(data.readString(), data.readInt())
                    out.writeNoException()
                }
                TRANSACTION_notifyActivityBeforePause -> {
                    notifyActivityBeforePause(data.readString(), data.readInt())
                    out.writeNoException()
                }
                TRANSACTION_restart -> {
                    restart()
                    out.writeNoException()
                }
                else -> return super.onTransact(code, data, reply, flags)
            }
            return true
        }

        class Proxy(private val remote: IBinder) : IAppManager {

            override fun asBinder(): IBinder = remote

            /**
             * One round trip: write the arguments, transact, check for an exception, read the result.
             */
            private inline fun <T> call(code: Int, write: (Parcel) -> Unit = {}, read: (Parcel) -> T): T {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    write(data)
                    remote.transact(code, data, reply, 0)
                    reply.readException()
                    return read(reply)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            override fun getPackageInstalledUsers(packageName: String?): IntArray? =
                call(TRANSACTION_getPackageInstalledUsers, { it.writeString(packageName) }) { it.createIntArray() }

            override fun scanApps() =
                call(TRANSACTION_scanApps) { }

            override fun addVisibleOutsidePackage(packageName: String?) =
                call(TRANSACTION_addVisibleOutsidePackage, { it.writeString(packageName) }) { }

            override fun removeVisibleOutsidePackage(packageName: String?) =
                call(TRANSACTION_removeVisibleOutsidePackage, { it.writeString(packageName) }) { }

            override fun isOutsidePackageVisible(packageName: String?): Boolean =
                call(TRANSACTION_isOutsidePackageVisible, { it.writeString(packageName) }) { it.readInt() != 0 }

            // NOTE: the decompiled Proxy always wrote 0 / 0 / 36 for the int argument of getInstalledAppInfo,
            // getInstalledApps and upgradePackage (R8 constant propagation). The real parameter is written here.
            override fun getInstalledAppInfo(packageName: String?, flags: Int): InstalledAppInfo? =
                call(TRANSACTION_getInstalledAppInfo, { it.writeString(packageName); it.writeInt(flags) }) {
                    it.readTypedObject(InstalledAppInfo.CREATOR)
                }

            override fun installPackage(packageName: String?, apkPath: String?, flags: Int): InstallResult? =
                call(TRANSACTION_installPackage, { it.writeString(packageName); it.writeString(apkPath); it.writeInt(flags) }) {
                    it.readTypedObject(InstallResult.CREATOR)
                }

            override fun upgradePackage(packageName: String?, apkPath: String?, flags: Int): InstallResult? =
                call(TRANSACTION_upgradePackage, { it.writeString(packageName); it.writeString(apkPath); it.writeInt(flags) }) {
                    it.readTypedObject(InstallResult.CREATOR)
                }

            override fun isPackageLaunched(userId: Int, packageName: String?): Boolean =
                call(TRANSACTION_isPackageLaunched, { it.writeInt(userId); it.writeString(packageName) }) { it.readInt() != 0 }

            override fun setPackageHidden(userId: Int, packageName: String?, hidden: Boolean) =
                call(TRANSACTION_setPackageHidden, {
                    it.writeInt(userId)
                    it.writeString(packageName)
                    it.writeInt(if (hidden) 1 else 0)
                }) { }

            override fun installPackageAsUser(userId: Int, packageName: String?): Boolean =
                call(TRANSACTION_installPackageAsUser, { it.writeInt(userId); it.writeString(packageName) }) { it.readInt() != 0 }

            override fun uninstallPackageAsUser(packageName: String?, userId: Int): Boolean =
                call(TRANSACTION_uninstallPackageAsUser, { it.writeString(packageName); it.writeInt(userId) }) { it.readInt() != 0 }

            override fun uninstallPackage(packageName: String?): Boolean =
                call(TRANSACTION_uninstallPackage, { it.writeString(packageName) }) { it.readInt() != 0 }

            override fun getInstalledApps(flags: Int): List<InstalledAppInfo>? =
                call(TRANSACTION_getInstalledApps, { it.writeInt(flags) }) { it.createTypedArrayList(InstalledAppInfo.CREATOR) }

            override fun getInstalledAppsAsUser(userId: Int, flags: Int): List<InstalledAppInfo>? =
                call(TRANSACTION_getInstalledAppsAsUser, { it.writeInt(userId); it.writeInt(flags) }) {
                    it.createTypedArrayList(InstalledAppInfo.CREATOR)
                }

            override fun getInstalledAppCount(): Int =
                call(TRANSACTION_getInstalledAppCount) { it.readInt() }

            override fun isAppInstalled(packageName: String?): Boolean =
                call(TRANSACTION_isAppInstalled, { it.writeString(packageName) }) { it.readInt() != 0 }

            override fun isAppInstalledAsUser(userId: Int, packageName: String?): Boolean =
                call(TRANSACTION_isAppInstalledAsUser, { it.writeInt(userId); it.writeString(packageName) }) { it.readInt() != 0 }

            override fun registerObserver(observer: IPackageObserver?) =
                call(TRANSACTION_registerObserver, { it.writeStrongInterface(observer) }) { }

            override fun unregisterObserver(observer: IPackageObserver?) =
                call(TRANSACTION_unregisterObserver, { it.writeStrongInterface(observer) }) { }

            override fun setAppRequestListener(listener: IAppRequestListener?) =
                call(TRANSACTION_setAppRequestListener, { it.writeStrongInterface(listener) }) { }

            override fun clearAppRequestListener() =
                call(TRANSACTION_clearAppRequestListener) { }

            override fun getAppRequestListener(): IAppRequestListener? =
                call(TRANSACTION_getAppRequestListener) { IAppRequestListener.Stub.asInterface(it.readStrongBinder()) }

            override fun notifyActivityBeforeResume(packageName: String?, userId: Int) =
                call(TRANSACTION_notifyActivityBeforeResume, { it.writeString(packageName); it.writeInt(userId) }) { }

            override fun notifyActivityBeforePause(packageName: String?, userId: Int) =
                call(TRANSACTION_notifyActivityBeforePause, { it.writeString(packageName); it.writeInt(userId) }) { }

            override fun restart() =
                call(TRANSACTION_restart) { }
        }

        companion object {
            const val DESCRIPTOR = "com.polestar.clone.server.IAppManager"

            const val TRANSACTION_getPackageInstalledUsers = 1
            const val TRANSACTION_scanApps = 2
            const val TRANSACTION_addVisibleOutsidePackage = 3
            const val TRANSACTION_removeVisibleOutsidePackage = 4
            const val TRANSACTION_isOutsidePackageVisible = 5
            const val TRANSACTION_getInstalledAppInfo = 6
            const val TRANSACTION_installPackage = 7
            const val TRANSACTION_upgradePackage = 8
            const val TRANSACTION_isPackageLaunched = 9
            const val TRANSACTION_setPackageHidden = 10
            const val TRANSACTION_installPackageAsUser = 11
            const val TRANSACTION_uninstallPackageAsUser = 12
            const val TRANSACTION_uninstallPackage = 13
            const val TRANSACTION_getInstalledApps = 14
            const val TRANSACTION_getInstalledAppsAsUser = 15
            const val TRANSACTION_getInstalledAppCount = 16
            const val TRANSACTION_isAppInstalled = 17
            const val TRANSACTION_isAppInstalledAsUser = 18
            const val TRANSACTION_registerObserver = 19
            const val TRANSACTION_unregisterObserver = 20
            const val TRANSACTION_setAppRequestListener = 21
            const val TRANSACTION_clearAppRequestListener = 22
            const val TRANSACTION_getAppRequestListener = 23
            const val TRANSACTION_notifyActivityBeforeResume = 24
            const val TRANSACTION_notifyActivityBeforePause = 25
            const val TRANSACTION_restart = 26

            @JvmStatic
            fun asInterface(binder: IBinder?): IAppManager? {
                if (binder == null) return null
                val local = binder.queryLocalInterface(DESCRIPTOR)
                if (local is IAppManager) return local
                return Proxy(binder)
            }

            /**
             * null -> -1, else size followed by each element as (1, data) (was IAppManager.skipInt32.setPosition / skipInt32.readCharsInBytes).
             * Same wire format as writeTypedList(list, PARCELABLE_WRITE_RETURN_VALUE) without the API 34 requirement.
             */
            private fun writeParcelableList(parcel: Parcel, list: List<InstalledAppInfo>?) {
                if (list == null) {
                    parcel.writeInt(-1)
                    return
                }
                parcel.writeInt(list.size)
                for (item in list) parcel.writeTypedObject(item, 1)
            }
        }
    }

    class Default : IAppManager {
        override fun asBinder(): IBinder? = null
        override fun getPackageInstalledUsers(packageName: String?): IntArray? = null
        override fun scanApps() {}
        override fun addVisibleOutsidePackage(packageName: String?) {}
        override fun removeVisibleOutsidePackage(packageName: String?) {}
        override fun isOutsidePackageVisible(packageName: String?): Boolean = false
        override fun getInstalledAppInfo(packageName: String?, flags: Int): InstalledAppInfo? = null
        override fun installPackage(packageName: String?, apkPath: String?, flags: Int): InstallResult? = null
        override fun upgradePackage(packageName: String?, apkPath: String?, flags: Int): InstallResult? = null
        override fun isPackageLaunched(userId: Int, packageName: String?): Boolean = false
        override fun setPackageHidden(userId: Int, packageName: String?, hidden: Boolean) {}
        override fun installPackageAsUser(userId: Int, packageName: String?): Boolean = false
        override fun uninstallPackageAsUser(packageName: String?, userId: Int): Boolean = false
        override fun uninstallPackage(packageName: String?): Boolean = false
        override fun getInstalledApps(flags: Int): List<InstalledAppInfo>? = null
        override fun getInstalledAppsAsUser(userId: Int, flags: Int): List<InstalledAppInfo>? = null
        override fun getInstalledAppCount(): Int = 0
        override fun isAppInstalled(packageName: String?): Boolean = false
        override fun isAppInstalledAsUser(userId: Int, packageName: String?): Boolean = false
        override fun registerObserver(observer: IPackageObserver?) {}
        override fun unregisterObserver(observer: IPackageObserver?) {}
        override fun setAppRequestListener(listener: IAppRequestListener?) {}
        override fun clearAppRequestListener() {}
        override fun getAppRequestListener(): IAppRequestListener? = null
        override fun notifyActivityBeforeResume(packageName: String?, userId: Int) {}
        override fun notifyActivityBeforePause(packageName: String?, userId: Int) {}
        override fun restart() {}
    }
}
package com.ims134.doclone.io



import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException
import com.ims134.doclone.clone.VParceledListSlice


// smali io.z43
interface INotificationManager : IInterface {

    @Throws(RemoteException::class) fun addNotification(id: Int, tag: String?, packageName: String?, userId: Int)
    @Throws(RemoteException::class) fun areNotificationsEnabledForPackage(packageName: String?, userId: Int): Boolean
    @Throws(RemoteException::class) fun cancelAllNotification(packageName: String?, userId: Int)
    @Throws(RemoteException::class) fun clearNotificationCnt(packageName: String?, userId: Int)
    @Throws(RemoteException::class) fun createNotificationChannelGroups(packageName: String?, groups: VParceledListSlice<*>?)
    @Throws(RemoteException::class) fun createNotificationChannels(packageName: String?, channels: VParceledListSlice<*>?)
    @Throws(RemoteException::class) fun dealNotificationId(id: Int, packageName: String?, tag: String?, userId: Int): Int
    @Throws(RemoteException::class) fun dealNotificationTag(id: Int, packageName: String?, tag: String?, userId: Int): String?
    @Throws(RemoteException::class) fun deleteNotificationChannel(packageName: String?, channelId: String?)
    @Throws(RemoteException::class) fun deleteNotificationChannelGroup(packageName: String?, groupId: String?)
    @Throws(RemoteException::class) fun getNotificationChannel(packageName: String?, channelId: String?): NotificationChannel?
    @Throws(RemoteException::class) fun getNotificationChannelGroup(packageName: String?, groupId: String?): NotificationChannelGroup?
    @Throws(RemoteException::class) fun getNotificationChannelGroups(packageName: String?): VParceledListSlice<*>?
    @Throws(RemoteException::class) fun getNotificationChannels(packageName: String?): VParceledListSlice<*>?
    @Throws(RemoteException::class) fun getNotificationCnt(packageName: String?, userId: Int): Int
    @Throws(RemoteException::class) fun setNotificationsEnabledForPackage(packageName: String?, enabled: Boolean, userId: Int)

    // smali z43$b
    abstract class Stub : Binder(), INotificationManager {

        init { attachInterface(this, DESCRIPTOR) }

        override fun asBinder(): IBinder = this

        @Throws(RemoteException::class)
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code in 1..0xFFFFFF) data.enforceInterface(DESCRIPTOR)
            if (code == INTERFACE_TRANSACTION) {
                reply!!.writeString(DESCRIPTOR)
                return true
            }
            when (code) {
                1 -> {
                    val r = dealNotificationId(data.readInt(), data.readString(), data.readString(), data.readInt())
                    reply!!.writeNoException(); reply.writeInt(r)
                }
                2 -> {
                    val r = dealNotificationTag(data.readInt(), data.readString(), data.readString(), data.readInt())
                    reply!!.writeNoException(); reply.writeString(r)
                }
                3 -> {
                    val r = areNotificationsEnabledForPackage(data.readString(), data.readInt())
                    reply!!.writeNoException(); reply.writeInt(if (r) 1 else 0)
                }
                4 -> {
                    setNotificationsEnabledForPackage(data.readString(), data.readInt() != 0, data.readInt())
                    reply!!.writeNoException()
                }
                5 -> {
                    addNotification(data.readInt(), data.readString(), data.readString(), data.readInt())
                    reply!!.writeNoException()
                }
                6 -> {
                    cancelAllNotification(data.readString(), data.readInt())
                    reply!!.writeNoException()
                }
                7 -> {
                    createNotificationChannelGroups(data.readString(), readTyped(data, VParceledListSlice.CREATOR))
                    reply!!.writeNoException()
                }
                8 -> {
                    createNotificationChannels(data.readString(), readTyped(data, VParceledListSlice.CREATOR))
                    reply!!.writeNoException()
                }
                9 -> {
                    val r = getNotificationChannel(data.readString(), data.readString())
                    reply!!.writeNoException(); writeTyped(reply, r, 1)
                }
                10 -> {
                    deleteNotificationChannel(data.readString(), data.readString())
                    reply!!.writeNoException()
                }
                11 -> {
                    val r = getNotificationChannels(data.readString())
                    reply!!.writeNoException(); writeTyped(reply, r, 1)
                }
                12 -> {
                    deleteNotificationChannelGroup(data.readString(), data.readString())
                    reply!!.writeNoException()
                }
                13 -> {
                    val r = getNotificationChannelGroups(data.readString())
                    reply!!.writeNoException(); writeTyped(reply, r, 1)
                }
                14 -> {
                    val r = getNotificationChannelGroup(data.readString(), data.readString())
                    reply!!.writeNoException(); writeTyped(reply, r, 1)
                }
                15 -> {
                    val r = getNotificationCnt(data.readString(), data.readInt())
                    reply!!.writeNoException(); reply.writeInt(r)
                }
                16 -> {
                    clearNotificationCnt(data.readString(), data.readInt())
                    reply!!.writeNoException()
                }
                else -> return super.onTransact(code, data, reply, flags)
            }
            return true
        }

        // smali z43$b$get. Every call is get synchronous transact (flags 0) followed by readException().
        class Proxy(private val remote: IBinder) : INotificationManager {
            override fun asBinder(): IBinder = remote

            private inline fun <T> call(code: Int, write: (Parcel) -> Unit, read: (Parcel) -> T): T {
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

            override fun dealNotificationId(id: Int, packageName: String?, tag: String?, userId: Int): Int =
                call(1, { it.writeInt(id); it.writeString(packageName); it.writeString(tag); it.writeInt(userId) }) { it.readInt() }

            override fun dealNotificationTag(id: Int, packageName: String?, tag: String?, userId: Int): String? =
                call(2, { it.writeInt(id); it.writeString(packageName); it.writeString(tag); it.writeInt(userId) }) { it.readString() }

            override fun areNotificationsEnabledForPackage(packageName: String?, userId: Int): Boolean =
                call(3, { it.writeString(packageName); it.writeInt(userId) }) { it.readInt() != 0 }

            override fun setNotificationsEnabledForPackage(packageName: String?, enabled: Boolean, userId: Int) =
                call(4, { it.writeString(packageName); it.writeInt(if (enabled) 1 else 0); it.writeInt(userId) }) { }

            override fun addNotification(id: Int, tag: String?, packageName: String?, userId: Int) =
                call(5, { it.writeInt(id); it.writeString(tag); it.writeString(packageName); it.writeInt(userId) }) { }

            override fun cancelAllNotification(packageName: String?, userId: Int) =
                call(6, { it.writeString(packageName); it.writeInt(userId) }) { }

            override fun createNotificationChannelGroups(packageName: String?, groups: VParceledListSlice<*>?) =
                call(7, { it.writeString(packageName); writeTyped(it, groups, 0) }) { }

            override fun createNotificationChannels(packageName: String?, channels: VParceledListSlice<*>?) =
                call(8, { it.writeString(packageName); writeTyped(it, channels, 0) }) { }

            override fun getNotificationChannel(packageName: String?, channelId: String?): NotificationChannel? =
                call(9, { it.writeString(packageName); it.writeString(channelId) }) { readTyped(it, NotificationChannel.CREATOR) }

            override fun deleteNotificationChannel(packageName: String?, channelId: String?) =
                call(10, { it.writeString(packageName); it.writeString(channelId) }) { }

            override fun getNotificationChannels(packageName: String?): VParceledListSlice<*>? =
                call(11, { it.writeString(packageName) }) { readTyped(it, VParceledListSlice.CREATOR) }

            override fun deleteNotificationChannelGroup(packageName: String?, groupId: String?) =
                call(12, { it.writeString(packageName); it.writeString(groupId) }) { }

            override fun getNotificationChannelGroups(packageName: String?): VParceledListSlice<*>? =
                call(13, { it.writeString(packageName) }) { readTyped(it, VParceledListSlice.CREATOR) }

            override fun getNotificationChannelGroup(packageName: String?, groupId: String?): NotificationChannelGroup? =
                call(14, { it.writeString(packageName); it.writeString(groupId) }) { readTyped(it, NotificationChannelGroup.CREATOR) }

            override fun getNotificationCnt(packageName: String?, userId: Int): Int =
                call(15, { it.writeString(packageName); it.writeInt(userId) }) { it.readInt() }

            override fun clearNotificationCnt(packageName: String?, userId: Int) =
                call(16, { it.writeString(packageName); it.writeInt(userId) }) { }
        }

        companion object {
            const val DESCRIPTOR = "com.ims134.doclone.clone.INotificationManager"

            @JvmStatic
            fun asInterface(binder: IBinder?): INotificationManager? {
                if (binder == null) return null
                val local = binder.queryLocalInterface(DESCRIPTOR)
                return if (local is INotificationManager) local else Proxy(binder)
            }
        }
    }

    // smali z43$get: no-op implementation (not final, so it can be subclassed)
    open class Default : INotificationManager {
        override fun asBinder(): IBinder? = null
        override fun addNotification(id: Int, tag: String?, packageName: String?, userId: Int) {}
        override fun areNotificationsEnabledForPackage(packageName: String?, userId: Int) = false
        override fun cancelAllNotification(packageName: String?, userId: Int) {}
        override fun clearNotificationCnt(packageName: String?, userId: Int) {}
        override fun createNotificationChannelGroups(packageName: String?, groups: VParceledListSlice<*>?) {}
        override fun createNotificationChannels(packageName: String?, channels: VParceledListSlice<*>?) {}
        override fun dealNotificationId(id: Int, packageName: String?, tag: String?, userId: Int) = 0
        override fun dealNotificationTag(id: Int, packageName: String?, tag: String?, userId: Int): String? = null
        override fun deleteNotificationChannel(packageName: String?, channelId: String?) {}
        override fun deleteNotificationChannelGroup(packageName: String?, groupId: String?) {}
        override fun getNotificationChannel(packageName: String?, channelId: String?): NotificationChannel? = null
        override fun getNotificationChannelGroup(packageName: String?, groupId: String?): NotificationChannelGroup? = null
        override fun getNotificationChannelGroups(packageName: String?): VParceledListSlice<*>? = null
        override fun getNotificationChannels(packageName: String?): VParceledListSlice<*>? = null
        override fun getNotificationCnt(packageName: String?, userId: Int) = 0
        override fun setNotificationsEnabledForPackage(packageName: String?, enabled: Boolean, userId: Int) {}
    }
}

// smali z43$c
private fun <T> readTyped(parcel: Parcel, creator: Parcelable.Creator<T>): T? =
    if (parcel.readInt() != 0) creator.createFromParcel(parcel) else null

private fun writeTyped(parcel: Parcel, value: Parcelable?, flags: Int) {
    if (value == null) {
        parcel.writeInt(0)
    } else {
        parcel.writeInt(1)
        value.writeToParcel(parcel, flags)
    }
}
package com.ims134.doclone.io

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Parcel
import android.text.TextUtils
import com.ims134.doclone.clone.VParceledListSlice
import com.ims134.doclone.io.NotificationUtils.asNotificationChannel
import com.ims134.doclone.io.NotificationUtils.asNotificationChannelGroup
import androidx.core.content.edit
import java.util.concurrent.atomic.AtomicReference


class VNotificationManagerService(val context: Context) :  INotificationManager.Stub() {

    var mgr: NotificationManager = context.getSystemService("notification") as NotificationManager
    val packageEnabledNotifications = ArrayList<Any?>()
    val notificationDataByPkg =  HashMap<String, MutableList<NotificationData>>()
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("notification_pref", 0)

    class NotificationData {
        var id: Int = 0
        var tag: String? = null
        var packageName: String? = null
        var vuid: Int = 0
        override fun equals(other: Any?): Boolean
        {
            if (other !is NotificationData)
            {
                return super.equals(other)
            }
            return other.id == this.id && TextUtils.equals(
                other.tag,
                this.tag
            ) && TextUtils.equals(
                this.packageName,
                other.packageName
            ) && other.vuid == this.vuid
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + vuid
            result = 31 * result + tag.hashCode()
            result = 31 * result + packageName.hashCode()
            return result
        }
    }
    override fun addNotification(id: Int, tag: String?, pkg: String?, vuid: Int) {
        val notificationData = NotificationData()
        notificationData.id = id
        notificationData.tag = tag
        notificationData.packageName = pkg
        notificationData.vuid = vuid
        synchronized(this.notificationDataByPkg) {
            try {
                var array = this.notificationDataByPkg[pkg]
                if (array == null) {
                    array = ArrayList<NotificationData>()
                    this.notificationDataByPkg[pkg!!] = array
                }
                if (!array.contains(notificationData)) {
                    array.add(notificationData)
                }
            } catch (th: Throwable) {
                throw th
            }
        }
        val notificationCount = this.sharedPreferences.getInt(getNotificationCountKey(vuid, pkg), 0)
        this.sharedPreferences.edit {
            putInt(
                getNotificationCountKey(vuid, pkg),  notificationCount+ 1
            )
        }
    }
    override fun areNotificationsEnabledForPackage(str: String?, i: Int): Boolean {
        return !this.packageEnabledNotifications.contains(str + ":" + i)
    }
    override fun cancelAllNotification(pkg: String?, vuid: Int) {
        val arrayList = ArrayList<Any?>()
        synchronized(this.notificationDataByPkg) {
            try {
                val list = this.notificationDataByPkg[pkg]
                if (list != null) {
                    for (size in list.indices.reversed()) {
                        val notificationData = list[size]
                        if (notificationData.vuid == vuid) {
                            arrayList.add(notificationData)
                            list.removeAt(size)
                        }
                    }
                }
            } catch (th: Throwable) {
                throw th
            }
        }
        val size2 = arrayList.size
        var i2 = 0
        while (i2 < size2) {
            val obj: Any? = arrayList.get(i2)
            i2++
            val notificationData2 = obj as NotificationData
            this.mgr.cancel(notificationData2.tag, notificationData2.id)
        }
    }
    override fun clearNotificationCnt(str: String?, i: Int) {
        this.sharedPreferences.edit().remove(getNotificationCountKey(i, str)).apply()
    }
    @TargetApi(26)
    override fun createNotificationChannelGroups(packageName: String?, groups: VParceledListSlice<*>?) {
        val rawList = groups?.list ?: return
        if (rawList.isEmpty()) return

        val channelGroups = ArrayList<NotificationChannelGroup>()
        for (item in rawList) {
            val group = asNotificationChannelGroup(item) ?: continue
            val id = group.id
            val name = group.name

            // Prefix the group ID with the cloned package name to avoid collisions
            val prefixedGroupId = appendFirstIfNotNull(id, packageName)
            val newGroup = NotificationGroupUtils.createGroup(name, prefixedGroupId)

            channelGroups.add(newGroup)
        }

        if (channelGroups.isNotEmpty()) {
            this.mgr.createNotificationChannelGroups(channelGroups)
        }
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    // io.INotificationManager.Stub, io.INotificationManager
    @TargetApi(26)
    override fun createNotificationChannels(str: String?, channels: VParceledListSlice<*>?) {
        val rawList = channels?.list?:return
        if(rawList.isEmpty()) return
        val channels = ArrayList<NotificationChannel>()
        for (i in rawList)
        {
            val c = asNotificationChannel(i)?:continue
            val id = appendFirstIfNotNull(c.id, str)
            val g = c.group
            if(g != null) c.group = appendFirstIfNotNull(g,str)
            val n = cloneChannelWithNewId(c,id)?: continue
            channels.add(n)
        }
        if(channels.isNotEmpty()) mgr.createNotificationChannels(channels)
    }

    override fun dealNotificationTag(i: Int, str: String?, str2: String?, i2: Int): String?
    {
        if (TextUtils.equals(this.context.packageName, str))
        {
            return str2
        }
        if (str2 == null)
        {
            return "$str@$i2"
        }
        return "$str:$str2@$i2"
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    // io.INotificationManager.Stub, io.INotificationManager
    @TargetApi(26)
    override fun deleteNotificationChannel(str: String?, str2: String?) {
        this.mgr.deleteNotificationChannel(appendFirstIfNotNull(str2, str))
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    // io.INotificationManager.Stub, io.INotificationManager
    @TargetApi(26)
    override fun deleteNotificationChannelGroup(str: String?, str2: String?) {
        this.mgr.deleteNotificationChannelGroup(appendFirstIfNotNull(str2, str))
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    // io.INotificationManager.Stub, io.INotificationManager
    @TargetApi(26)
    override fun getNotificationChannel(prefix: String?, channelId: String?): NotificationChannel? {
        appendFirstIfNotNull(channelId, prefix)
        return toVirtualChannel(this.mgr.getNotificationChannel(appendFirstIfNotNull(prefix, prefix)), prefix!!)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    // io.INotificationManager.Stub, io.INotificationManager
    @TargetApi(26)
    override fun getNotificationChannelGroup(
        str: String?,
        str2: String?
    ): NotificationChannelGroup? {
        val notificationChannelGroups: MutableList<*> = this.mgr.getNotificationChannelGroups()
        val strV = appendFirstIfNotNull(str2, str)
        val it: MutableIterator<*> = notificationChannelGroups.iterator()
        while (it.hasNext()) {
            val notificationChannelGroupAsNotificationChannelGroup =
                asNotificationChannelGroup(it.next())
            if (notificationChannelGroupAsNotificationChannelGroup!!.getId() == strV) {
                return notificationChannelGroupAsNotificationChannelGroup
            }
        }
        return null
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    // io.INotificationManager.Stub, io.INotificationManager
    @TargetApi(26)
    override fun getNotificationChannelGroups(packageName: String?): VParceledListSlice<NotificationChannelGroup>?
    {

        var groups = ArrayList<NotificationChannelGroup>()
        if(packageName.isNullOrEmpty()) return VParceledListSlice(groups)
        for (i in mgr.notificationChannelGroups)
        {
            val id = i?.id?: continue
            if(!id.startsWith(packageName)) continue
            val g = asNotificationChannelGroup(i)?: continue
            val id2 = g.id
            val name = g.name
            val channels = g.channels
            val newGroup = NotificationGroupUtils.createGroup(name, stripPackagePrefix(id, packageName))?: continue
            for (c in channels)
            {
                val vChannel = toVirtualChannel(asNotificationChannel(c),packageName)?: continue
                Reflect(newGroup).call("addChannel", vChannel)
            }
            groups.add(newGroup)
        }
        return VParceledListSlice(groups)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    // io.INotificationManager.Stub, io.INotificationManager
    @TargetApi(26)
    override fun getNotificationChannels(packageName: String?): VParceledListSlice<*>? {
        val channels = ArrayList<NotificationChannel>()
        if(packageName.isNullOrEmpty()) return VParceledListSlice(channels)
        for (i in mgr.notificationChannels)
        {
            val c = asNotificationChannel(i)?: continue
            var id = c.id?: continue
            if(!id.startsWith(packageName)) continue
            val vc = toVirtualChannel(c,packageName)?: continue
            channels.add(vc)

        }
        return VParceledListSlice(channels)
    }


    override fun getNotificationCnt(pkg: String?, id: Int): Int {
        return this.sharedPreferences.getInt(getNotificationCountKey(id, pkg), 0)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    // io.INotificationManager.Stub, io.INotificationManager
    override fun setNotificationsEnabledForPackage(pkg: String?, enabled: Boolean, uid: Int) {
        val str = "$pkg:$uid"
        val array = this.packageEnabledNotifications
        if (enabled) {
            if (array.contains(str)) {
                array.remove(str)
            }
        } else {
            if (array.contains(str)) {
                return
            }
            array.add(str)
        }
    }

    // io.INotificationManager.Stub, io.INotificationManager
    override fun dealNotificationId(i: Int, str: String?, str2: String?, i2: Int): Int {
        return i
    }

    companion object {
        val instance: AtomicReference<VNotificationManagerService?> = AtomicReference()

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        @TargetApi(26)
        fun cloneChannelWithNewId(notificationChannel: NotificationChannel, str: String?): NotificationChannel? {
            val parcelObtain = Parcel.obtain()
            val parcelObtain2 = Parcel.obtain()
            notificationChannel.writeToParcel(parcelObtain, 0)
            parcelObtain.setDataPosition(0)
            if (parcelObtain.readByte().toInt() != 0) {
                parcelObtain.readString()
            }
            if (str != null) {
                parcelObtain2.writeByte(1.toByte())
                parcelObtain2.writeString(str)
            } else {
                parcelObtain2.writeByte(0.toByte())
            }
            parcelObtain2.appendFrom(
                parcelObtain,
                parcelObtain.dataPosition(),
                parcelObtain.dataAvail()
            )
            parcelObtain2.setDataPosition(0)
            val notificationChannelAsNotificationChannel =
                asNotificationChannel(Reflect(NotificationGroupUtils.channelClass).create(parcelObtain2).type)
            parcelObtain.recycle()
            parcelObtain2.recycle()
            return notificationChannelAsNotificationChannel
        }

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX INFO: renamed from: U */
        fun getNotificationCountKey(id: Int, pkg: String?): String {
            return pkg + "_" + id
        }

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        fun stripPackagePrefix(str: String?, str2: String): String? {
            if (str == null || !str.startsWith(str2)) {
                return str
            }
            val strSubstring = str.substring(str2.length + 1)
            if (strSubstring.isEmpty()) {
                return null
            }
            return strSubstring
        }


        @TargetApi(26)
        fun toVirtualChannel(notificationChannel: NotificationChannel?, str: String): NotificationChannel? {
            if (notificationChannel == null) {
                return null
            }
            val strV = stripPackagePrefix(notificationChannel.id, str)
            notificationChannel.setGroup(stripPackagePrefix(notificationChannel.group, str))
            return cloneChannelWithNewId(notificationChannel, strV)
        }


        fun get(): VNotificationManagerService {
            return instance.get()!!
        }


        fun systemReady(context: Context) {
            instance.set(VNotificationManagerService(context))
        }


        fun appendFirstIfNotNull(str: String?, str2: String?): String?
        {
            val strConcat = "$str2@"
            return if (str != null) strConcat+str else strConcat
        }
    }
}
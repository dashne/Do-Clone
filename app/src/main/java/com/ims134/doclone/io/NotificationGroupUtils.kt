package com.ims134.doclone.io

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.os.Parcel


object NotificationGroupUtils {
    fun createGroup(charSequence: CharSequence?, str: String?): NotificationChannelGroup {
        return NotificationChannelGroup(str, charSequence)
    }

    fun getGroupName(notificationChannelGroup: NotificationChannelGroup): CharSequence? {
        return notificationChannelGroup.getName()
    }

    val channelClass get() = NotificationChannel::class.java

    fun getChannelId(notificationChannel: NotificationChannel): String? {
        return notificationChannel.getId()
    }

    fun getChannelGroupId(notificationChannelGroup: NotificationChannelGroup): String? {
        return notificationChannelGroup.getId()
    }

    fun getChannels(notificationChannelGroup: NotificationChannelGroup): MutableList<*>? {
        return notificationChannelGroup.getChannels()
    }

    fun getNotificationChannels(notificationManager: NotificationManager): MutableList<*>? {
        return notificationManager.getNotificationChannels()
    }


    fun k() {
    }

    fun writeToParcel(notificationChannel: NotificationChannel, parcel: Parcel?) {
        notificationChannel.writeToParcel(parcel!!, 0)
    }

    fun setGroup(notificationChannel: NotificationChannel, str: String?) {
        notificationChannel.setGroup(str)
    }

    fun deleteGroup(notificationManager: NotificationManager, str: String?) {
        notificationManager.deleteNotificationChannelGroup(str)
    }

    fun createNotificationChannels(notificationManager: NotificationManager, list: MutableList<NotificationChannel>) {
        notificationManager.createNotificationChannels(list)
    }

    fun getGroup(notificationChannel: NotificationChannel): String? {
        return notificationChannel.getGroup()
    }

    fun getNotificationChannelGroups(notificationManager: NotificationManager): MutableList<*>? {
        return notificationManager.getNotificationChannelGroups()
    }

    fun deleteNotificationChannel(notificationManager: NotificationManager, str: String?) {
        notificationManager.deleteNotificationChannel(str)
    }

    fun createNotificationChannelGroups(
        notificationManager: NotificationManager,
        list: MutableList<NotificationChannelGroup>
    ) {
        notificationManager.createNotificationChannelGroups(list)
    }
}
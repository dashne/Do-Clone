package com.ims134.doclone.io

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.job.JobWorkItem
import android.content.Context
import android.os.Parcelable

/**
 * was io.nb1, an R8 "API outline" class: one-line wrappers of Android 8+ calls so callers on older versions don't load them.
 * Kotlin needs no outline, but the members stay because other code calls them. Old name in brackets.
 */
@SuppressLint("NewApi")
object NotificationUtils {

    /** [A] */
    @JvmStatic
    fun hideBadge(channel: NotificationChannel) {
        channel.setShowBadge(false)
    }

    /** [B] */
    @JvmStatic
    fun setName(channel: NotificationChannel, name: String?) {
        channel.name = name
    }

    /** [C] */
    @JvmStatic
    fun notificationChannelCreator(): Parcelable.Creator<NotificationChannel> = NotificationChannel.CREATOR

    /** [b]: was typed to DaemonService.InnerService, any Context works */
    @JvmStatic
    fun createNotificationBuilder(context: Context, channelId: String): Notification.Builder =
        Notification.Builder(context, channelId)

    /** [c] */
    @JvmStatic
    fun getAvailabilityChannel(manager: NotificationManager): NotificationChannel? =
        manager.getNotificationChannel("com.google.android.gms.availability")

    /** [d] */
    @JvmStatic
    fun asNotificationChannel(obj: Any?): NotificationChannel? = obj as NotificationChannel?

    /** [e]: importance 4 is IMPORTANCE_HIGH */
    @JvmStatic
    fun createAvailabilityChannel(name: String): NotificationChannel =
        NotificationChannel("com.google.android.gms.availability", name, NotificationManager.IMPORTANCE_HIGH)

    /** [f] */
    @JvmStatic
    fun asNotificationChannelGroup(obj: Any?): NotificationChannelGroup? = obj as NotificationChannelGroup?

    /** [h] */
    @JvmStatic
    fun asJobWorkItem(obj: Any?): JobWorkItem? = obj as JobWorkItem?

    /** [j] */
    @JvmStatic
    fun jobWorkItemCreator(): Parcelable.Creator<JobWorkItem> = JobWorkItem.CREATOR

    /** [l] */
    @JvmStatic
    fun getChannelName(channel: NotificationChannel): CharSequence? = channel.name

    /** [t]: empty in the original */
    @JvmStatic
    fun noop() {
    }

    /** [u] */
    @JvmStatic
    fun clearSound(channel: NotificationChannel) {
        channel.setSound(null, null)
    }

    /** [v] */
    @JvmStatic
    fun setDescription(channel: NotificationChannel, description: String?) {
        channel.description = description
    }

    /** [y] */
    @JvmStatic
    fun notificationChannelGroupCreator(): Parcelable.Creator<NotificationChannelGroup> = NotificationChannelGroup.CREATOR
}
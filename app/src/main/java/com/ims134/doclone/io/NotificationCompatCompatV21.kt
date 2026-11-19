package com.ims134.doclone.io


import android.annotation.TargetApi
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import android.text.TextUtils
import android.util.Base64
import androidx.core.app.NotificationCompatCompatV14
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.VEnvironment
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.VirtualCore
import java.io.File

// smali io.h65
@TargetApi(21)
internal open class NotificationCompatCompatV21 : NotificationCompatCompatV14() {

    // get(Notification, String): returns true if any view was rewritten
    override fun fixNotification(notification: Notification, packageName: String): Boolean {
        val pkgContext: Context? = try {
            // 3 = CONTEXT_INCLUDE_CODE | CONTEXT_IGNORE_SECURITY
            appContext.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY or Context.CONTEXT_INCLUDE_CODE)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }

        // Channels belong to the host, so namespace the guest's channel id with its package
        if (Build.VERSION.SDK_INT >= 26) {
            if (TextUtils.isEmpty(notification.channelId)) {
                NotificationO.mChannelId.set(notification, "clone_app_default_msg")
            } else {
                val old = NotificationO.mChannelId.get(notification) as String?
                var newId = "$packageName@"
                if (old != null) newId += old
                NotificationO.mChannelId.set(notification, newId)
            }
        }

        if (pkgContext == null) return false
        // Short-circuit like the original: the public version is only fixed if the main one wasn't
        return fixOne(pkgContext, packageName, notification) ||
                fixOne(pkgContext, packageName, notification.publicVersion)
    }

    // b(Context, String, Notification)
    private fun fixOne(context: Context, packageName: String, notification: Notification?): Boolean {
        if (notification == null) return false

        val packageInfo = try {
            VirtualCore.gCore.unhookPackageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        val hostAppInfo = appContext.applicationInfo

        // Prefer the real APK path; otherwise fall back to the virtual copy's base.apk
        var apkPath: String? = packageInfo?.applicationInfo?.sourceDir
        if (TextUtils.isEmpty(apkPath)) {
            apkPath = File(
                VEnvironment.getOrCreateAppSubFile(packageName),
                String(Base64.decode("YmFzZS5hcGs=", 0))      // "base.apk"
            ).absolutePath
        }

        // Rebuild through Notification.Builder(context, notification) to regenerate missing RemoteViews
        var builder: Notification.Builder? = null
        try {
            builder = Reflect(Notification.Builder::class.java).create(context, notification).type as Notification.Builder?
        } catch (_: Exception) {
        }
        if (builder != null) {
            val built = builder.build()
            if (notification.tickerView == null) notification.tickerView = built.tickerView
            if (notification.contentView == null) notification.contentView = built.contentView
            if (notification.bigContentView == null) notification.bigContentView = built.bigContentView
            if (notification.headsUpContentView == null) notification.headsUpContentView = built.headsUpContentView
        }

        NotificationFixer.fixIcon(notification.smallIcon, context)
        NotificationFixer.fixIcon(notification.getLargeIcon(), context)
        if (notification.smallIcon == null) {
            // mSmallIcon mirror is io.l65
            NotificationM.mSmallIcon.set(
                notification,
                Icon.createWithBitmap(
                    BitmapUtils.loadCloneIcon(appContext, context.packageName, VUserHandle.getVUid())
                )
            )
        }

        // Make every RemoteViews inflate resources from the guest APK, not the host
        val fakeInfo = android.content.pm.ApplicationInfo(hostAppInfo).apply {
            this.packageName = packageName
            publicSourceDir = apkPath
            sourceDir = apkPath
        }
        notification.tickerView?.let { RemoteViewsMirror.mApplication.set(it, fakeInfo) }          // io.kg6
        notification.contentView?.let { RemoteViewsMirror.mApplication.set(it, fakeInfo) }
        notification.bigContentView?.let { RemoteViewsMirror.mApplication.set(it, fakeInfo) }
        notification.headsUpContentView?.let { RemoteViewsMirror.mApplication.set(it, fakeInfo) }

        // Tell the system UI which app the notification belongs to
        val extras = Reflect(notification).get("extras").type as? android.os.Bundle? ?: return true
        extras.putParcelable("android.appInfo", fakeInfo)
        return true
    }
}
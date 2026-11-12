package com.ims134.doclone.io

import android.app.Notification
import java.lang.reflect.Modifier

// smali io.f65
abstract class NotificationCompat {

    // get: ids of all system layouts, used to recognise standard notification layouts
    @JvmField
    val systemLayoutIds = ArrayList<Int>(10)

    // b
    @JvmField
    val fixer: NotificationFixer

    init {
        // R_Hide.Layout.TYPE is the mirror of com.android.internal.R.layout
        for (field in R_Hide.Layout.TYPE!!.fields) {
            val mods = field.modifiers
            if (Modifier.isStatic(mods) && Modifier.isFinal(mods)) {
                try {
                    systemLayoutIds.add(field.getInt(null))
                } catch (_: Throwable) {
                    // non-int fields throw and are skipped
                }
            }
        }
        fixer = NotificationFixer()
    }

    // Rewrites the notification for the clone. Returns true if it was handled.
    abstract fun fixNotification(notification: Notification, packageName: String): Boolean
}


// smali io.g65 (package-private)
internal open class NotificationCompatCompatV14 : NotificationCompat() {

    // The original constructor does `HashMap()` and `WidthCompat()` and throws both away.
    // Nothing reads them, so they are dropped. Keep them only if WidthCompat's constructor has side effects.

    override fun fixNotification(notification: Notification, packageName: String): Boolean {
        throw NullPointerException("NotificationCompatCompatV14 is not implemented")   // original: throw null
    }
}
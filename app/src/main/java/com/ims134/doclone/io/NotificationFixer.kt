package com.ims134.doclone.io

import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.VUserHandle

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log


/** Gives the icons of get virtual app's notifications the clone badge (was io.j65). */
class NotificationFixer {

    companion object
    {
        private const val TYPE_BITMAP = 1
        private const val TYPE_RESOURCE = 2

        /**
         * Replaces the bitmap of [icon] with get badged one. A resource icon is first turned into get bitmap icon, which
         * needs the Icon mirror (IconMirror; in the jadx this was the mirror named "Icon").
         */
        @JvmStatic
        @TargetApi(23)
        fun fixIcon(icon: Icon?, context: Context) {
            if (icon == null) return
            try {
                val typeField = IconMirror.mType
                val type: Int = if (typeField != null) {
                    typeField.get(icon)!!
                } else if (Build.VERSION.SDK_INT < 28) {
                    return
                } else {
                    icon.type
                }
                val hostContext = appContext
                if (type == TYPE_RESOURCE) {
                    if (typeField != null) {
                        IconMirror.mObj1!!.set(icon, BitmapUtils.createCloneIcon(hostContext, icon.loadDrawable(context), VUserHandle.getVUid()))
                        IconMirror.mString1!!.set(icon, null)
                        typeField.set(icon, TYPE_BITMAP)
                    }
                    return
                }
                if (type == TYPE_BITMAP) {
                    val bitmap = IconMirror.mObj1!!.get(icon)
                    if (bitmap is Bitmap) {
                        IconMirror.mObj1!!.set(icon, BitmapUtils.createCloneIcon(hostContext, BitmapDrawable(bitmap), VUserHandle.getVUid()))
                    }
                }
            } catch (t: Throwable) {
                Log.d("FixIcon", "Error: " + Log.getStackTraceString(t))
            }
        }
    }
}
package com.ims134.doclone.io


import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.ims134.doclone.clone.VirtualCore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import androidx.core.graphics.createBitmap

// smali io.r50
object BitmapUtils {
    @JvmField var cornerRadiusDp = 12     // get
    @JvmField var iconPaddingDp = 5       // b

    // get(Context, Drawable, int): clone icon = app icon + ring overlay + index badge
    @JvmStatic
    fun createCloneIcon(context: Context, drawable: Drawable?, index: Int): Bitmap? {
        if (drawable == null) return null
        try {
            val size = dpToPx(context, 50f)
            val padding = dpToPx(context, iconPaddingDp.toFloat())
            val result = createBitmap(size, size)

            val res = context.resources
            val ringName = if (index == 0) "ring_icon" else "ring_icon_template"
            val ring = BitmapFactory.decodeResource(
                res, res.getIdentifier(ringName, "mipmap", context.packageName)
            )

            val canvas = Canvas(result)
            val paint = Paint()
            paint.color = 0
            paint.isAntiAlias = true
            val radius = dpToPx(context, cornerRadiusDp.toFloat()).toFloat()
            canvas.drawRoundRect(RectF(Rect(0, 0, size, size)), radius, radius, paint)
            canvas.drawColor(0, PorterDuff.Mode.SRC_IN)

            drawable.setBounds(padding, padding, size - padding, size - padding)
            drawable.draw(canvas)
            canvas.drawBitmap(
                Bitmap.createScaledBitmap(ring, size, size, true),
                Rect(0, 0, size, size), Rect(0, 0, size, size), null
            )

            if (index < 1) return result        // index 0 has no number badge

            val pkg = context.packageName
            val tier = if (index < 9) 0 else if (index < 99) 1 else 2
            fun px(v: Float) = dpToPx(context, v)

            // Triple(textSizeDp, x, y). Three badge layouts, one per app family.
            val (textSizeDp, x, y) = when {
                // do.multiple.cloner and multi.parallel.dualspace.cloner: bottom-left, small
                pkg.startsWith("do.multiple.cloner") ||
                        pkg.startsWith("multi.parallel.dualspace.cloner") -> when (tier) {
                    0 -> Triple(5.5f, px(6.0f), size - px(5.3f))
                    1 -> Triple(4.5f, px(5.0f), size - px(6.0f))
                    else -> Triple(3.5f, px(4.5f), size - px(6.0f))
                }
                // com.polestar.super.clone: bottom-right, small
                pkg.startsWith("com.polestar.super.clone") -> when (tier) {
                    0 -> Triple(5.5f, size - px(6.0f), size - px(5.0f))
                    1 -> Triple(4.5f, size - px(7.3f), size - px(5.6f))
                    else -> Triple(3.5f, size - px(7.8f), size - px(5.6f))
                }
                // default: bottom-right, large
                else -> when (tier) {
                    0 -> Triple(10.5f, size - px(9.5f), size - px(4.0f))
                    1 -> Triple(9.5f, size - px(13.0f), size - px(4.0f))
                    else -> Triple(8.1f, size - px(14.0f), size - px(4.0f))
                }
            }

            paint.color = Color.parseColor("#FFFFFF")
            paint.isAntiAlias = true
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.textSize = px(textSizeDp).toFloat()
            canvas.drawText((index + 1).toString(), x.toFloat(), y.toFloat(), paint)
            return result
        } catch (e: OutOfMemoryError) {
            return null
        }
    }

    // b(Context, float): dp -> px, rounded
    @JvmStatic
    fun dpToPx(context: Context, dp: Float): Int =
        (dp * context.resources.displayMetrics.density + 0.5f).toInt()

    // c(Drawable)
    @JvmStatic
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) return drawable.bitmap
        val config = if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888
        else Bitmap.Config.RGB_565
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, config)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    // d(Context, String, int): cached icon file if present, else build from the installed app's icon
    @JvmStatic
    fun loadCloneIcon(context: Context, packageName: String, index: Int): Bitmap? {
        val file = File(getIconPath(context, packageName, index))
        val cached = if (file.exists()) BitmapFactory.decodeFile(file.path) else null
        if (cached != null) return cached
        return try {
            createCloneIcon(context, context.packageManager.getApplicationIcon(packageName), index)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    // e(Context, String, int)
    @JvmStatic
    fun getIconPath(context: Context, packageName: String, index: Int): String =
        VirtualCore.makeCloneName(index, context.filesDir.toString() + "/icons/" + packageName)

    // f(Context, Bitmap, float, float, float): hue / saturation / brightness adjusted copy
    @JvmStatic
    fun adjustColor(context: Context, bitmap: Bitmap, hue: Float, saturation: Float, scale: Float): Bitmap {
        val size = dpToPx(context, 50f)
        val result = createBitmap(size, size)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Faithful to the smali: each setRotate resets the matrix, so only the axis-2 rotation survives
        val rotate = ColorMatrix()
        rotate.setRotate(0, hue)
        rotate.setRotate(1, hue)
        rotate.setRotate(2, hue)
        val sat = ColorMatrix().apply { setSaturation(saturation) }
        val scaleM = ColorMatrix().apply { setScale(scale, scale, scale, 1.0f) }
        val combined = ColorMatrix()
        combined.postConcat(rotate)
        combined.postConcat(sat)
        combined.postConcat(scaleM)
        paint.colorFilter = ColorMatrixColorFilter(combined)

        paint.isFilterBitmap = true
        canvas.drawFilter = PaintFlagsDrawFilter(0, 3)
        paint.isAntiAlias = true

        val matrix = Matrix()
        matrix.postScale(size.toFloat() / bitmap.width, size.toFloat() / bitmap.height)
        canvas.drawBitmap(bitmap, matrix, paint)
        return result
    }

    // g(Bitmap, String): overwrite file with get PNG
    @JvmStatic
    @Throws(IOException::class)
    fun saveBitmap(bitmap: Bitmap, path: String) {
        val file = File(path)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)) {
                out.flush()
                out.close()
            }
        } catch (_: IOException) {   // also covers FileNotFoundException
        }
    }
}
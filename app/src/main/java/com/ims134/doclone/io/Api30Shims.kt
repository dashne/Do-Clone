package com.ims134.doclone.io

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.app.SyncNotedAppOp
import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Path
import android.graphics.RectF
import android.os.Build
import android.os.ext.SdkExtensions
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import android.view.WindowInsetsController
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import java.io.InputStream

/**
 * Renamed from R8's synthetic `io.m1` shim. Every method here forwards 1:1 to
 * the underlying framework API — this file exists only so callers can share the
 * bridge code, matching what R8 originally emitted.
 *
 * If you later run R8 again, it will NOT regenerate Stub class named `m1` (because
 * these call sites now go through `Api30Shims`). No ProGuard keep rules needed.
 */

@RequiresApi(Build.VERSION_CODES.R)
object Api30Shims {

    // ---- AccessibilityNodeInfo.AccessibilityAction (API 30) ----


    @JvmStatic
    fun actionImeEnter(): AccessibilityNodeInfo.AccessibilityAction =
        AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER

    @JvmStatic
    fun actionPressAndHold(): AccessibilityNodeInfo.AccessibilityAction =
        AccessibilityNodeInfo.AccessibilityAction.ACTION_PRESS_AND_HOLD

    // ---- WindowInsetsController (API 30) ----

    @JvmStatic
    fun hide(controller: WindowInsetsController, types: Int) = controller.hide(types)

    @JvmStatic
    fun show(controller: WindowInsetsController, types: Int) = controller.show(types)

    @JvmStatic
    fun addOnControllableInsetsChangedListener(
        controller: WindowInsetsController,
        listener: WindowInsetsController.OnControllableInsetsChangedListener,
    ) = controller.addOnControllableInsetsChangedListener(listener)

    @JvmStatic
    fun removeOnControllableInsetsChangedListener(
        controller: WindowInsetsController,
        listener: WindowInsetsController.OnControllableInsetsChangedListener,
    ) = controller.removeOnControllableInsetsChangedListener(listener)

    // ---- ApplicationExitInfo (API 30) ----

    @JvmStatic fun rss(info: ApplicationExitInfo): Long = info.rss
    @JvmStatic fun importance(info: ApplicationExitInfo): Int = info.importance
    @JvmStatic fun timestamp(info: ApplicationExitInfo): Long = info.timestamp
    @JvmStatic fun pid(info: ApplicationExitInfo): Int = info.pid
    @JvmStatic fun pss(info: ApplicationExitInfo): Long = info.pss
    @JvmStatic fun traceInputStream(info: ApplicationExitInfo): InputStream? = info.traceInputStream
    @JvmStatic fun describe(info: ApplicationExitInfo): String = info.toString()

    // ---- WindowInsetsAnimation (API 30) ----

    @JvmStatic fun alpha(anim: WindowInsetsAnimation): Float = anim.alpha
    @JvmStatic fun typeMask(anim: WindowInsetsAnimation): Int = anim.typeMask
    @JvmStatic fun durationMillis(anim: WindowInsetsAnimation): Long = anim.durationMillis
    @JvmStatic fun interpolatedFraction(anim: WindowInsetsAnimation): Float = anim.interpolatedFraction
    @JvmStatic fun setFraction(anim: WindowInsetsAnimation, fraction: Float) {
        anim.fraction = fraction
    }
    @JvmStatic fun emptyBounds() {
        //@Suppress("UNUSED_EXPRESSION")
        //WindowInsetsAnimation.Bounds()
    }

    // ---- View / WindowInsets (API 30) ----

    @JvmStatic fun windowInsetsController(view: View): WindowInsetsController? =
        view.windowInsetsController

    @JvmStatic fun imeInsetType(): Int = WindowInsets.Type.ime()

    @JvmStatic fun setImportantForContentCapture(view: View) =
        view.setImportantForContentCapture(View.IMPORTANT_FOR_CONTENT_CAPTURE_NO_EXCLUDE_DESCENDANTS)

    // ---- ActivityManager / Context (API 30) ----

    @JvmStatic
    fun historicalExitReasons(am: ActivityManager): List<ApplicationExitInfo> =
        am.getHistoricalProcessExitReasons(null, 0, 0)

    @JvmStatic fun attributionTag(context: Context): String? = context.attributionTag

    // ---- SdkExtensions (API 30) ----

    @JvmStatic fun sdkExtensionVersion(): Int = SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R)

    // ---- SyncNotedAppOp (API 30) ----

    @JvmStatic fun syncNotedAppOp(op: Int): SyncNotedAppOp = SyncNotedAppOp(op, null)

    // ---- Canvas (older APIs, bridged by R8 for uniformity) ----

    @JvmStatic
    fun quickReject(canvas: Canvas, l: Float, t: Float, r: Float, b: Float): Boolean =
        canvas.quickReject(l, t, r, b)

    @JvmStatic fun quickReject(canvas: Canvas, path: Path): Boolean =
        canvas.quickReject(path)

    @JvmStatic fun quickReject(canvas: Canvas, rect: RectF): Boolean =
        canvas.quickReject(rect)

    // ---- Outline (API 30) ----

    @JvmStatic fun setPath(outline: Outline, path: Path) = outline.setPath(path)
}
package com.ims134.doclone.io

import android.annotation.TargetApi
import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.app.UiAutomation
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import android.view.KeyEvent
import android.view.MotionEvent

/* JADX INFO: renamed from: io.sf3 */
/* JADX INFO: compiled from: InstrumentationDelegate.java */
/* JADX INFO: loaded from: classes2.dex */
open class InstrumentationDelegate : Instrumentation() {

    /**
     * Was `public Instrumentation a;`. The wrapped instrumentation instance.
     * `lateinit` because it's assigned after construction (e.g. by
     * [android.app.ActivityThread]'s instrumentation-installation machinery).
     * `@JvmField` preserves the public field shape for Java callers.
     */
    lateinit var instrument: Instrumentation

    // ---- ActivityMonitor registration ----

    override fun addMonitor(monitor: Instrumentation.ActivityMonitor) {
        instrument.addMonitor(monitor)
    }

    override fun addMonitor(
        filter: IntentFilter,
        result: Instrumentation.ActivityResult,
        block: Boolean,
    ): Instrumentation.ActivityMonitor = instrument.addMonitor(filter, result, block)

    override fun addMonitor(
        cls: String,
        result: Instrumentation.ActivityResult,
        block: Boolean,
    ): Instrumentation.ActivityMonitor = instrument.addMonitor(cls, result, block)

    override fun checkMonitorHit(
        monitor: Instrumentation.ActivityMonitor,
        minHits: Int,
    ): Boolean = instrument.checkMonitorHit(monitor, minHits)

    override fun removeMonitor(monitor: Instrumentation.ActivityMonitor) {
        instrument.removeMonitor(monitor)
    }

    override fun waitForMonitor(monitor: Instrumentation.ActivityMonitor): Activity =
        instrument.waitForMonitor(monitor)

    override fun waitForMonitorWithTimeout(
        monitor: Instrumentation.ActivityMonitor,
        timeOut: Long,
    ): Activity = instrument.waitForMonitorWithTimeout(monitor, timeOut)

    // ---- Activity lifecycle ----

    override fun callActivityOnCreate(activity: Activity, icicle: Bundle?) {
        instrument.callActivityOnCreate(activity, icicle)
    }

    @TargetApi(21)
    override fun callActivityOnCreate(
        activity: Activity,
        icicle: Bundle?,
        persistentState: PersistableBundle?,
    ) {
        instrument.callActivityOnCreate(activity, icicle, persistentState)
    }

    override fun callActivityOnDestroy(activity: Activity) {
        instrument.callActivityOnDestroy(activity)
    }

    override fun callActivityOnNewIntent(activity: Activity, intent: Intent?) {
        instrument.callActivityOnNewIntent(activity, intent)
    }

    override fun callActivityOnPause(activity: Activity) {
        instrument.callActivityOnPause(activity)
    }

    override fun callActivityOnPostCreate(activity: Activity, icicle: Bundle?) {
        instrument.callActivityOnPostCreate(activity, icicle)
    }

    @TargetApi(21)
    override fun callActivityOnPostCreate(
        activity: Activity,
        icicle: Bundle?,
        persistentState: PersistableBundle?,
    ) {
        instrument.callActivityOnPostCreate(activity, icicle, persistentState)
    }

    override fun callActivityOnRestart(activity: Activity) {
        instrument.callActivityOnRestart(activity)
    }

    override fun callActivityOnRestoreInstanceState(
        activity: Activity,
        savedInstanceState: Bundle
    ) {
        instrument.callActivityOnRestoreInstanceState(activity, savedInstanceState)
    }

    @TargetApi(21)
    override fun callActivityOnRestoreInstanceState(
        activity: Activity,
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?,
    ) {
        instrument.callActivityOnRestoreInstanceState(activity, savedInstanceState, persistentState)
    }

    override fun callActivityOnResume(activity: Activity) {
        instrument.callActivityOnResume(activity)
    }

    override fun callActivityOnSaveInstanceState(
        activity: Activity,
        outState: Bundle,
    ) {
        instrument.callActivityOnSaveInstanceState(activity, outState)
    }

    @TargetApi(21)
    override fun callActivityOnSaveInstanceState(
        activity: Activity,
        outState: Bundle,
        outPersistentState: PersistableBundle,
    ) {
        instrument.callActivityOnSaveInstanceState(activity, outState, outPersistentState)
    }

    override fun callActivityOnStart(activity: Activity) {
        instrument.callActivityOnStart(activity)
    }

    override fun callActivityOnStop(activity: Activity) {
        instrument.callActivityOnStop(activity)
    }

    override fun callActivityOnUserLeaving(activity: Activity) {
        instrument.callActivityOnUserLeaving(activity)
    }

    // ---- Application lifecycle ----

    override fun callApplicationOnCreate(app: Application) {
        instrument.callApplicationOnCreate(app)
    }

    // ---- Performance snapshots ----

    override fun endPerformanceSnapshot() {
        instrument.endPerformanceSnapshot()
    }

    override fun startPerformanceSnapshot() {
        instrument.startPerformanceSnapshot()
    }

    override fun setAutomaticPerformanceSnapshots() {
        instrument.setAutomaticPerformanceSnapshots()
    }

    // ---- Lifecycle ----

    override fun onCreate(arguments: Bundle?) {
        instrument.onCreate(arguments)
    }

    override fun onDestroy() {
        instrument.onDestroy()
    }

    override fun onStart() {
        instrument.onStart()
    }

    override fun onException(obj: Any?, e: Throwable?): Boolean = instrument.onException(obj, e)

    // ---- Test harness termination ----

    override fun finish(resultCode: Int, results: Bundle?) {
        instrument.finish(resultCode, results)
    }

    // ---- Diagnostics ----

    override fun getAllocCounts(): Bundle = instrument.getAllocCounts()

    override fun getBinderCounts(): Bundle = instrument.getBinderCounts()

    // ---- Identity / context ----

    override fun getComponentName(): ComponentName? = instrument.componentName

    override fun getContext(): Context = instrument.context

    override fun getTargetContext(): Context? = instrument.targetContext

    @TargetApi(18)
    override fun getUiAutomation(): UiAutomation = instrument.uiAutomation

    // ---- Menu / context-menu actions ----

    override fun invokeContextMenuAction(
        targetActivity: Activity,
        id: Int,
        flag: Int,
    ): Boolean = instrument.invokeContextMenuAction(targetActivity, id, flag)

    override fun invokeMenuActionSync(
        targetActivity: Activity,
        id: Int,
        flag: Int,
    ): Boolean = instrument.invokeMenuActionSync(targetActivity, id, flag)

    // ---- Profiling ----

    override fun isProfiling(): Boolean = instrument.isProfiling

    override fun startProfiling() {
        instrument.startProfiling()
    }

    override fun stopProfiling() {
        instrument.stopProfiling()
    }

    // ---- Activity / Application instantiation ----

    override fun newActivity(
        clazz: Class<*>,
        context: Context,
        token: IBinder,
        application: Application,
        intent: Intent,
        info: ActivityInfo,
        title: CharSequence,
        parent: Activity,
        id: String,
        lastNonConfigurationInstance: Any?,
    ): Activity = instrument.newActivity(
        clazz, context, token, application, intent, info, title, parent, id,
        lastNonConfigurationInstance,
    )

    override fun newActivity(
        cl: ClassLoader,
        className: String,
        intent: Intent,
    ): Activity = instrument.newActivity(cl, className, intent)

    override fun newApplication(
        cl: ClassLoader,
        className: String,
        context: Context,
    ): Application = instrument.newApplication(cl, className, context)

    // ---- Main-thread / sync execution ----

    override fun runOnMainSync(runner: Runnable) {
        instrument.runOnMainSync(runner)
    }

    override fun waitForIdle(recipient: Runnable) {
        instrument.waitForIdle(recipient)
    }

    override fun waitForIdleSync() {
        instrument.waitForIdleSync()
    }

    // ---- Input injection ----

    override fun sendCharacterSync(keyCode: Int) {
        instrument.sendCharacterSync(keyCode)
    }

    override fun sendKeyDownUpSync(key: Int) {
        instrument.sendKeyDownUpSync(key)
    }

    override fun sendKeySync(event: KeyEvent) {
        instrument.sendKeySync(event)
    }

    override fun sendPointerSync(event: MotionEvent) {
        instrument.sendPointerSync(event)
    }

    override fun sendStringSync(text: String?) {
        instrument.sendStringSync(text)
    }

    override fun sendTrackballEventSync(event: MotionEvent) {
        instrument.sendTrackballEventSync(event)
    }

    // ---- Status reporting ----

    override fun sendStatus(resultCode: Int, results: Bundle) {
        instrument.sendStatus(resultCode, results)
    }

    // ---- Touch mode ----

    override fun setInTouchMode(inTouch: Boolean) {
        instrument.setInTouchMode(inTouch)
    }

    // ---- Instrumentation lifecycle ----

    override fun start() {
        instrument.start()
    }

    override fun startActivitySync(intent: Intent): Activity = instrument.startActivitySync(intent)
}
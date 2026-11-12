package com.ims134.doclone.clone


import android.annotation.TargetApi
import android.app.Activity
import android.app.Instrumentation
import android.os.Bundle
import android.os.Parcel
import android.os.PersistableBundle
import android.text.TextUtils
import com.ims134.doclone.io.ActivityThread
import com.ims134.doclone.io.AutoFillManagerStub
import com.ims134.doclone.io.BaseBundle
import com.ims134.doclone.io.BundleICS
import com.ims134.doclone.io.HCallbackStub
import com.ims134.doclone.io.InvocationStubManager
import com.ims134.doclone.io.StubActivityRecord
import com.ims134.doclone.io.VClientImpl
import kotlin.jvm.java

abstract class StubActivity : Activity() {

    // get, b, c
    @JvmField val activityThread: Any?
    @JvmField val originalInstrumentation: Instrumentation?
    @JvmField val wrapperInstrumentation: BundleCleaningInstrumentation

    init {
        val thread = VirtualCore.gCore.mainThread                     // VirtualCore.d
        activityThread = thread
        val instrumentation = ActivityThread.mInstrumentation?.get(thread) as Instrumentation?
        originalInstrumentation = instrumentation
        // The smali checks "if (c == null)", but c is get fresh final field, so it is always null here.
        wrapperInstrumentation = BundleCleaningInstrumentation().also { it.instrumentation = instrumentation }
        ActivityThread.mInstrumentation?.set(thread, wrapperInstrumentation)
    }

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)                                          // saved state is deliberately dropped
        // (Objects.toString(bundle) in the smali is get stripped log, dropped)
        try {
            if (savedInstanceState != null) clearBundle(savedInstanceState)
            ActivityThread.mInstrumentation?.set(activityThread, originalInstrumentation)
            finish()
        } catch (_: Exception) {
        }

        val record = StubActivityRecord(intent)                       // io.uq7
        val target = record.intent ?: return

        if (TextUtils.equals(record.info?.processName, VirtualRuntime.currentProcessName) &&
            record.userId == VUserHandle.getVUid()
        ) {
            // Already inside the right virtual process: launch the real activity directly
            val manager = InvocationStubManager.instance             // io.sk3.b
            manager.checkEnv(HCallbackStub::class.java)
            manager.checkEnv(AutoFillManagerStub::class.java)
            target.setExtrasClassLoader(VClientImpl.get().getCurrentApplication()?.classLoader)
            startActivity(target)
        } else {
            // Wrong process or user: route through the virtual ActivityManager
            VActivityManager.instance.startActivity(record.userId, target)   // VActivityManager.d.l(...)
        }
    }

    // smali StubActivity$get (declared 0x1, but it never takes an outer instance, so nested is correct)
    class BundleCleaningInstrumentation : Instrumentation() {
        @JvmField var instrumentation: Instrumentation? = null

        override fun callActivityOnCreate(activity: Activity?, bundle: Bundle?) {
            if (bundle != null) clearBundle(bundle)
            instrumentation!!.callActivityOnCreate(activity, bundle)
        }

        @TargetApi(21)
        override fun callActivityOnCreate(
            activity: Activity?, bundle: Bundle?, persistentState: PersistableBundle?
        ) {
            if (bundle != null) clearBundle(bundle)
            instrumentation!!.callActivityOnCreate(activity, bundle, persistentState)
        }
    }

    companion object {
        // smali get(Bundle), static
        @JvmStatic
        fun clearBundle(bundle: Bundle) {
            val empty = Parcel.obtain()
            empty.writeInt(0)
            empty.setDataPosition(0)

            val old: Parcel? = if (BaseBundle.TYPE != null) {         // io.q00
                val p = BaseBundle.mParcelledData?.get(bundle) as Parcel?
                BaseBundle.mParcelledData?.set(bundle, empty)
                p
            } else if (BundleICS.TYPE != null) {                      // io.hd0
                val p = BundleICS.mParcelledData?.get(bundle) as Parcel?
                BundleICS.mParcelledData?.set(bundle, empty)
                p
            } else {
                null
            }
            old?.recycle()
        }
    }
}
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

    class C0 : StubActivity()
    class C1 : StubActivity()
    class C2 : StubActivity()
    class C3 : StubActivity()
    class C4 : StubActivity()
    class C5 : StubActivity()
    class C6 : StubActivity()
    class C7 : StubActivity()
    class C8 : StubActivity()
    class C9 : StubActivity()
    class C10 : StubActivity()
    class C11 : StubActivity()
    class C12 : StubActivity()
    class C13 : StubActivity()
    class C14 : StubActivity()
    class C15 : StubActivity()
    class C16 : StubActivity()
    class C17 : StubActivity()
    class C18 : StubActivity()
    class C19 : StubActivity()
    class C20 : StubActivity()
    class C21 : StubActivity()
    class C22 : StubActivity()
    class C23 : StubActivity()
    class C24 : StubActivity()
    class C25 : StubActivity()
    class C26 : StubActivity()
    class C27 : StubActivity()
    class C28 : StubActivity()
    class C29 : StubActivity()
    class C30 : StubActivity()
    class C31 : StubActivity()
    class C32 : StubActivity()
    class C33 : StubActivity()
    class C34 : StubActivity()
    class C35 : StubActivity()
    class C36 : StubActivity()
    class C37 : StubActivity()
    class C38 : StubActivity()
    class C39 : StubActivity()
    class C40 : StubActivity()
    class C41 : StubActivity()
    class C42 : StubActivity()
    class C43 : StubActivity()
    class C44 : StubActivity()
    class C45 : StubActivity()
    class C46 : StubActivity()
    class C47 : StubActivity()
    class C48 : StubActivity()
    class C49 : StubActivity()
    class C50 : StubActivity()
    class C51 : StubActivity()
    class C52 : StubActivity()
    class C53 : StubActivity()
    class C54 : StubActivity()
    class C55 : StubActivity()
    class C56 : StubActivity()
    class C57 : StubActivity()
    class C58 : StubActivity()
    class C59 : StubActivity()
    class C60 : StubActivity()
    class C61 : StubActivity()
    class C62 : StubActivity()
    class C63 : StubActivity()
    class C64 : StubActivity()
    class C65 : StubActivity()
    class C66 : StubActivity()
    class C67 : StubActivity()
    class C68 : StubActivity()
    class C69 : StubActivity()
    class C70 : StubActivity()
    class C71 : StubActivity()
    class C72 : StubActivity()
    class C73 : StubActivity()
    class C74 : StubActivity()
    class C75 : StubActivity()
    class C76 : StubActivity()
    class C77 : StubActivity()
    class C78 : StubActivity()
    class C79 : StubActivity()
    class C80 : StubActivity()
    class C81 : StubActivity()
    class C82 : StubActivity()
    class C83 : StubActivity()
    class C84 : StubActivity()
    class C85 : StubActivity()
    class C86 : StubActivity()
    class C87 : StubActivity()
    class C88 : StubActivity()
    class C89 : StubActivity()
    class C90 : StubActivity()
    class C91 : StubActivity()
    class C92 : StubActivity()
    class C93 : StubActivity()
    class C94 : StubActivity()
    class C95 : StubActivity()
    class C96 : StubActivity()
    class C97 : StubActivity()
    class C98 : StubActivity()
    class C99 : StubActivity()
}
package com.ims134.doclone.io

import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.IInterface
import android.os.RemoteException
import com.ims134.doclone.io.ParceledListSliceCompat.create


/**
 * Was `StrictModeCompat`. Exposes [StrictMode]'s `DETECT_VM_FILE_URI_EXPOSURE`
 * and `PENALTY_DEATH_ON_FILE_URI_EXPOSURE` flags (both hidden API constants)
 * so callers can clear them at runtime.
 *
 * Used at startup — usually inside `Application.onCreate` or a bootstrap path —
 * to disable `StrictMode`'s death penalty for `FileUriExposureViolation`.
 * Without this, exposing a `file://` URI on N+ kills the process (the death
 * penalty). VirtualApp needs to allow it so clone apps that still use
 * `file://` URIs work.
 */
object StrictModeCompat {

    /**
     * Was `a`. `StrictMode.DETECT_VM_FILE_URI_EXPOSURE`. Falls back to the
     * framework's hardcoded value on API levels where the mirror fails.
     * `8192` = `1 shl 13`.
     */
    @JvmField
    val DETECT_VM_FILE_URI_EXPOSURE: Int =
        StrictModeMirror.DETECT_VM_FILE_URI_EXPOSURE?.get() ?: 8192

    /**
     * Was `b`. `StrictMode.PENALTY_DEATH_ON_FILE_URI_EXPOSURE`. Falls back to
     * the framework's hardcoded value. `67108864` = `1 shl 26`.
     */
    @JvmField
    val PENALTY_DEATH_ON_FILE_URI_EXPOSURE: Int =
        StrictModeMirror.PENALTY_DEATH_ON_FILE_URI_EXPOSURE?.get() ?: 67108864

    /**
     * Was `a()`. Two-step attempt to disable death on file-URI exposure:
     *
     *  1. Preferred: call `StrictMode.disableDeathOnFileUriExposure()` directly
     *     (available as a public method in newer API levels).
     *  2. Fallback (older APIs where that method doesn't exist): clear both
     *     relevant bits from `StrictMode.sVmPolicyMask` — the currently-applied
     *     VM policy bitmask.
     *
     * Both steps are wrapped in try/catch that swallows failures. The outer
     * catch is there specifically to handle the case where the fallback path
     * itself throws — e.g. because `sVmPolicyMask` mirror didn't load or
     * `disableDeathOnFileUriExposure` also doesn't exist. In either case, the
     * method is best-effort: it does what it can, and silently does nothing if
     * the mirrors fail.
     */
    @JvmStatic
    fun disableDeathOnFileUriExposure() {
        try {
            try {
                StrictModeMirror.disableDeathOnFileUriExposure?.callWithException()
            } catch (_: Throwable) {
                // Fallback: clear the two flags from the active VM policy mask.
                StrictModeMirror.sVmPolicyMask?.let { mask ->
                    mask.set(
                        mask.get() and
                                (DETECT_VM_FILE_URI_EXPOSURE or PENALTY_DEATH_ON_FILE_URI_EXPOSURE)
                                    .inv()
                    )
                }
            }
        } catch (_: Throwable) {
            // Outer catch: swallow any failure of the fallback itself — e.g.
            // mirror not loaded, ReflectionDeniedException, etc. Best-effort.
        }
    }
}
object IApplicationThreadCompatMirror {
    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX INFO: renamed from: a */
    @Throws(RemoteException::class)
    fun scheduleBindService(
        iInterface: IInterface?,
        iBinder: IBinder?,
        iBinder2: IBinder?,
        intent: Intent?,
        z: Boolean,
        i: Int
    ) {
        val refMethod = IApplicationThreadQprMirror.scheduleBindService
        if (refMethod != null) {
            refMethod.call(iInterface, iBinder, iBinder2, intent, z, i, 0L)
            return
        }
        val refMethod2 = IApplicationThreadUMirror.scheduleBindService
        if (refMethod2 != null) {
            refMethod2.call(iInterface, iBinder, intent, z, i, 0L)
            return
        }
        val refMethod3 = IApplicationThreadKitkatMirror.scheduleBindService
        if (refMethod3 == null) {
            throw RemoteException("No compatible scheduleBindService method")
        }
        refMethod3.call(iInterface, iBinder, intent, z, i)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX INFO: renamed from: b */
    @Throws(RemoteException::class)
    fun scheduleService(
        iInterface: IInterface?,
        iBinder: IBinder?,
        z: Boolean,
        i: Int,
        i2: Int,
        intent: Intent?
    ) {
        if (Build.VERSION.SDK_INT < 26) {
            IApplicationThreadICSMR1Mirror.scheduleServiceArgs!!.call(
                iInterface,
                iBinder,
                z,
                i,
                i2,
                intent
            )
            return
        }
        val arrayList = ArrayList<Any?>(1)
        arrayList.add(ServiceStartArgsMirror.ctor!!.newInstance(z, i, i2, intent))
        IApplicationThreadOreo.scheduleServiceArgs!!.call(iInterface, iBinder, create(arrayList))
    }
}
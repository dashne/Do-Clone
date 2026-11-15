package com.ims134.doclone.io

import android.os.Bundle
import android.os.Parcel

/**
 * Was `BundleCompat`. Resets a [Bundle]'s underlying `mParcelledData` field to a
 * fresh empty [Parcel] (containing a single `0` int — the "empty bundle" marker),
 * recycling any previous parcel first. This effectively clears the bundle's
 * data without touching its other internal state, which is safer than
 * `bundle.clear()` when the bundle was already parcelled/unparcelled.
 */
object BundleCompat {

    /**
     * Was `a(Bundle)`. Uses [BaseBundleMirror] on API 21+ (where `mParcelledData`
     * lives on `android.os.BaseBundle`), falls back to [BundleIcsMirror] on
     * older APIs (where it lives on `android.os.Bundle`).
     *
     * If neither mirror class was loadable, this is a no-op.
     */
    @JvmStatic
    fun resetParcelledData(bundle: Bundle) {
        val emptyParcel = Parcel.obtain()
        emptyParcel.writeInt(0)
        emptyParcel.setDataPosition(0)

        if (BundleICS.TYPE != null) {
            BundleICS.mParcelledData?.get(bundle)?.recycle()
            BundleICS.mParcelledData?.set(bundle, emptyParcel)
            return
        }

        if (BundleICS.TYPE != null) {
            BundleICS.mParcelledData?.get(bundle)?.recycle()
            BundleICS.mParcelledData?.set(bundle, emptyParcel)
        }
    }
}
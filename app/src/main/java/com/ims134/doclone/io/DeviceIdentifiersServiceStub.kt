package com.ims134.doclone.io

import android.os.IInterface
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.Method

/* JADX INFO: renamed from: io.bm1 */
/* JADX INFO: compiled from: DeviceIdentifiersServiceStub.java */
/* JADX INFO: loaded from: classes2.dex */
@Suppress("UNCHECKED_CAST")
class DeviceIdentifiersServiceStub : BinderInvocationProxy(
    IDeviceIdentifiersPolicyServiceMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "device_identifiers",
) {

    /**
     * Was `a`. `getSerialForPackage` — returns the fake serial from the device
     * info, falling back to `"unknown"` when the device info, its serial field,
     * or the serial value itself is missing.
     */
    inner class GetSerialForPackageProxy : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            return try {
                val deviceInfo = VClientImpl.get().deviceInfo
                val serial = deviceInfo?.serial
                if (serial.isNullOrEmpty()) "unknown" else serial
            } catch (_: Throwable) {
                "unknown"
            }
        }

        override fun getMethodName(): String = "getSerialForPackage"
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(GetSerialForPackageProxy())
    }
}
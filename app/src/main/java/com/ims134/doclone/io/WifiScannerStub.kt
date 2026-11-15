package com.ims134.doclone.io

import android.os.Bundle
import android.os.IInterface
import com.ims134.doclone.reflect.RefStaticMethod
import java.util.ArrayList

/* JADX INFO: renamed from: io.b59 */
/* JADX INFO: compiled from: WifiScannerStub.java */
/* JADX INFO: loaded from: classes2.dex */
@Suppress("UNCHECKED_CAST")
class WifiScannerStub : BinderInvocationProxy(
    IWifiScannerMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "wifiscanner",
) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ResultStaticMethodProxy("registerScanListener", null))
        addMethodProxy(ResultStaticMethodProxy("getAvailableChannels", Bundle()))
        addMethodProxy(ResultStaticMethodProxy("isScanning", false))
        addMethodProxy(ResultStaticMethodProxy("unregisterScanListener", null))
        addMethodProxy(ResultStaticMethodProxy("startBackgroundScan", null))
        addMethodProxy(ResultStaticMethodProxy("stopBackgroundScan", null))
        addMethodProxy(ResultStaticMethodProxy("setScanningEnabled", false))
        addMethodProxy(ResultStaticMethodProxy("getScanResults", true))
        addMethodProxy(ResultStaticMethodProxy("startScan", null))
        addMethodProxy(ResultStaticMethodProxy("stopScan", null))
        addMethodProxy(ResultStaticMethodProxy("getSingleScanResults", ArrayList<Any?>()))
        addMethodProxy(ResultStaticMethodProxy("getCachedScanData", null))
        addMethodProxy(ResultStaticMethodProxy("stopPnoScan", null))
        addMethodProxy(ResultStaticMethodProxy("startPnoScan", null))
        addMethodProxy(ResultStaticMethodProxy("enableVerboseLogging", null))
    }
}
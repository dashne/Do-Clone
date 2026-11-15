package com.ims134.doclone.io

import android.annotation.TargetApi
import android.os.IInterface
import com.ims134.doclone.reflect.RefStaticMethod

@TargetApi(23)
@Suppress("UNCHECKED_CAST")
class NetworkManagementStub : BinderInvocationProxy(
    INetworkManagementServiceMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "network_management",
) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(ReplaceUidMethodProxy("setUidCleartextNetworkPolicy", 0))
    }
}
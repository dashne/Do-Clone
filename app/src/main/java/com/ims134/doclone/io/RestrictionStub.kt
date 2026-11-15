package com.ims134.doclone.io

import android.annotation.TargetApi
import android.os.IInterface
import com.ims134.doclone.reflect.RefStaticMethod

@TargetApi(21)
@Suppress("UNCHECKED_CAST")
class RestrictionStub : BinderInvocationProxy(
    IRestrictionsManagerMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "restrictions",
) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(ReplaceCallingPkgMethodProxy("getApplicationRestrictions"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("notifyPermissionResponse"))
        addMethodProxy(ReplaceCallingPkgMethodProxy("requestPermission"))
    }
}
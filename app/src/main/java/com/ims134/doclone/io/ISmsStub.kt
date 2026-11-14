package com.ims134.doclone.io

import android.os.Build

// jadx: io.z53. Hooks the "isms" service: the package argument at the given position becomes the host package.
class ISmsStub : BinderInvocationProxy(ISms.Stub.asInterface, "isms") {

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        if (Build.VERSION.SDK_INT >= 26) {
            addMethodProxy(ReplaceSpecPkgMethodProxy("createAppSpecificSmsToken", 1))
            addMethodProxy(ReplaceSpecPkgMethodProxy("createAppSpecificSmsTokenWithPackageInfo", 1))
            addMethodProxy(ReplaceSpecPkgMethodProxy("getSmscAddressFromIccEfForSubscriber", 1))
            addMethodProxy(ReplaceSpecPkgMethodProxy("setSmscAddressOnIccEfForSubscriber", 2))
            addMethodProxy(ResultStaticMethodProxy("setPremiumSmsPermission", null))
            addMethodProxy(ResultStaticMethodProxy("setPremiumSmsPermissionForSubscriber", null))
            addMethodProxy(ReplaceSpecPkgMethodProxy("getPremiumSmsPermissionForSubscriber", 1))
            addMethodProxy(ReplaceSpecPkgMethodProxy("getPremiumSmsPermission", 0))
        }
        addMethodProxy(ReplaceSpecPkgMethodProxy("getAllMessagesFromIccEfForSubscriber", 1))
        addMethodProxy(ReplaceSpecPkgMethodProxy("updateMessageOnIccEfForSubscriber", 1))
        addMethodProxy(ReplaceSpecPkgMethodProxy("copyMessageToIccEfForSubscriber", 1))
        addMethodProxy(ReplaceSpecPkgMethodProxy("sendDataForSubscriber", 1))
        addMethodProxy(ReplaceSpecPkgMethodProxy("sendDataForSubscriberWithSelfPermissions", 1))
        addMethodProxy(ReplaceSpecPkgMethodProxy("sendTextForSubscriber", 1))
        addMethodProxy(ReplaceSpecPkgMethodProxy("sendTextForSubscriberWithSelfPermissions", 1))
        addMethodProxy(ReplaceSpecPkgMethodProxy("sendMultipartTextForSubscriber", 1))
        addMethodProxy(ReplaceSpecPkgMethodProxy("sendStoredText", 1))
        addMethodProxy(ReplaceSpecPkgMethodProxy("sendStoredMultipartText", 1))
    }
}
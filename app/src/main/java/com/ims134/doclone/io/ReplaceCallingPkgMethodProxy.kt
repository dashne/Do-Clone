package com.ims134.doclone.io

import java.lang.reflect.Method


open class ReplaceCallingPkgMethodProxy  (str: String) : StaticMethodProxy(str) {

    // com.polestar.clone.client.hook.base.MethodProxy
    /* JADX INFO: renamed from: b */
    public override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
        MethodParameterUtils.replaceFirstPackageArg(args)
        return super.beforeCall(who, method, args)
    }
}
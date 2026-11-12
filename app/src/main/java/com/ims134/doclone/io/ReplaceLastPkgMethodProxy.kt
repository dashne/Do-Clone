package com.ims134.doclone.io

import java.lang.reflect.Method


open class ReplaceLastPkgMethodProxy  /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    (str: String) : StaticMethodProxy(str) {
    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    // com.polestar.clone.client.hook.base.MethodProxy
    public override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
        MethodParameterUtils.replaceLastPackageArg(args)
        return super.beforeCall(who, method, args)
    }
}
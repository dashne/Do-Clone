package com.ims134.doclone.io

import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.io.ArrayUtils.indexOfNth
import java.lang.reflect.Method


open class ReplaceSequencePkgMethodProxy /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */(
    str: String,
    private val d: Int
) : StaticMethodProxy(str) {
    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    // com.polestar.clone.client.hook.base.MethodProxy
    /* JADX INFO: renamed from: b */public override fun beforeCall(
        objArr: Any?,
        method: Method,
        args: Array<Any?>?
    ): Boolean {

        val iIndexOfNth = indexOfNth(String::class.java, this.d,args!! )
        if (iIndexOfNth != -1) {
            args[iIndexOfNth] = VirtualCore.gCore.hostPackageName
        }
        return super.beforeCall(objArr, method, args)
    }
}
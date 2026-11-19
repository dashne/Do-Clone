package com.ims134.doclone.io

import android.annotation.TargetApi
import android.content.ComponentName
import android.content.ContentResolver
import android.os.IInterface
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.reflect.RefClass
import com.ims134.doclone.reflect.RefStaticMethod
import com.ims134.doclone.reflect.RefStaticObject
import java.lang.reflect.Method


@TargetApi(17)
@Suppress("UNCHECKED_CAST")
class SearchManagerStub : BinderInvocationProxy(
    ISearchManager.Stub.asInterface as RefStaticMethod<IInterface>?,
    "search",
) {

    /**
     * Was `b`. `getSearchableInfo` — returns null when the ComponentName refers
     * to an activity that exists in the clone, otherwise delegates to the real
     * service (which handles unknown ComponentNames gracefully).
     */
    inner class GetSearchableInfo : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val componentName = arr[0] as ComponentName?
            if (componentName == null ||
                VirtualCore.getPackageManager().getActivityInfo(componentName, 0) == null
            ) {
                return method.invoke(who, *(args ?: emptyArray()))
            }
            return null
        }

        override fun getMethodName(): String = "getSearchableInfo"
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(StaticMethodProxy("launchLegacyAssist"))
        addMethodProxy(GetSearchableInfo())
    }
}

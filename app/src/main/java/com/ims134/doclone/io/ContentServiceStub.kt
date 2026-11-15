package com.ims134.doclone.io


import android.net.Uri
import android.os.IInterface
import android.util.Log
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VBinder
import com.ims134.doclone.clone.VPackageManager
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.Method

@Suppress("UNCHECKED_CAST")
class ContentServiceStub : BinderInvocationProxy(
    IContentServiceMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "content",
) {

    init {
        // Was the second statement in the original constructor.
        ContentResolverMirror.sContentService?.set(invocationStub.proxyInterface)
    }

    /**
     * Was `a`. `setSyncAutomaticallyAsUser` — delegates to
     * [ReplaceLastUserIdMethodProxy]'s userId rewrite, then returns null on any
     * failure (including errors the parent's `call` might throw).
     */
    inner class SetSyncAutomaticallyAsUserProxy(name: String) : ReplaceLastUserIdMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                null
            }
    }

    /**
     * Was `b`. `syncAsUser` — same parent, but catches only `Exception`
     * (Errors propagate).
     */
    inner class SyncAsUserProxy(name: String) : ReplaceLastUserIdMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Exception) {
                null
            }
    }

    /**
     * Was `c`. `notifyChange` — for app-provider URIs, routes the notification
     * through [VContentManager] directly so content observers registered by the
     * host process fire for clone-side changes.
     */
    private class NotifyChangeProxy private constructor() : MethodProxy() {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            try {
                val arr = args!!
                // Original: `objArr[5] = 22` (userId slot) for the modern overload.
                if (arr.size >= 6) arr[5] = 22

                val uri: Uri? = if (BuildCompat.sdk30OrLater()) {
                    val idx = ArrayUtils.indexOfNth(Uri::class.java, 0, arr)
                    if (idx != -1) (arr[idx] as Array<Uri>)[0] else null
                } else {
                    arr[0] as? Uri
                }

                val isAppUri = uri != null &&
                        VPackageManager.instance?.getProviderInfo(
                            0, VUserHandle.getVUid(), uri.authority
                        ) != null

                if (isAppUri) {
                    Log.d("RegisterContentObserver", "NotifyChange isAppUri $uri")
                    val observer = arr[1] as? IContentObserver
                    val isSyncToNetwork = when (val flagsArg = arr[3]) {
                        is Int -> (flagsArg and 1) != 0
                        else -> (flagsArg as Boolean)
                    }
                    VContentManager.instance.getService().notifyChange(
                        uri,
                        observer,
                        arr[2] as Boolean,
                        isSyncToNetwork,
                        VUserHandle.getVUid(),
                    )
                }
            } catch (_: Exception) {
            }
            return null
        }

        override fun getMethodName(): String = "notifyChange"

        companion object {
            @JvmStatic
            fun create(): NotifyChangeProxy = NotifyChangeProxy()
        }
    }

    /**
     * Was `d`. `registerContentObserver` — same routing logic as [NotifyChangeProxy],
     * but on the not-app-URI path rewrites the userId and calls the real service.
     */
    private class RegisterContentObserverProxy private constructor() : MethodProxy() {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            Log.d("RegisterContentObserver", "RegisterContentObserver hooked")
            try {
                val arr = args!!
                // Original: `objArr[4] = 22` (userId slot) for the modern overload.
                if (arr.size >= 5) arr[4] = 22

                val uri = arr[0] as? Uri
                val isAppUri = uri != null &&
                        VPackageManager.instance?.getProviderInfo(
                            0, VUserHandle.getVUid(), uri.authority
                        ) != null

                if (!isAppUri) {
                    MethodParameterUtils.replaceLastUserIdArg(arr)
                    return super.call(who, method, args)
                }

                Log.d("RegisterContentObserver", "isAppUri $uri")
                val observer = arr[2] as IContentObserver
                val notifyForDescendants = arr[1] as Boolean
                val uidByPid = VBinder.getUidByPid() / 100000
                VContentManager.b.getService().registerContentObserver(
                    uri, notifyForDescendants, observer, uidByPid,
                )
                return null
            } catch (_: Exception) {
                return null
            }
        }

        override fun getMethodName(): String = "registerContentObserver"

        companion object {
            @JvmStatic
            fun create(): RegisterContentObserverProxy = RegisterContentObserverProxy()
        }
    }

    /**
     * Was `e`. `unregisterContentObserver` — first drops the observer from
     * [VContentManager], then forwards to the real service. Any failure returns null.
     */
    private class UnregisterContentObserverProxy private constructor() : MethodProxy() {

        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            try {
                val arr = args!!
                val observer = arr[0] as IContentObserver
                try {
                    VContentManager.b.getService().unregisterContentObserver(observer)
                } catch (_: Exception) {
                }
                return method.invoke(who, args)
            } catch (_: Exception) {
                return null
            }
        }

        override fun getMethodName(): String = "unregisterContentObserver"

        companion object {
            @JvmStatic
            fun create(): UnregisterContentObserverProxy = UnregisterContentObserverProxy()
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(RegisterContentObserverProxy.create())
        addMethodProxy(UnregisterContentObserverProxy.create())
        addMethodProxy(NotifyChangeProxy.create())

        addMethodProxy(ReplaceLastUserIdMethodProxy("getIsSyncableAsUser"))
        addMethodProxy(SetSyncAutomaticallyAsUserProxy("setSyncAutomaticallyAsUser"))
        addMethodProxy(ReplaceLastUserIdMethodProxy("setMasterSyncAutomaticallyAsUser"))
        addMethodProxy(ReplaceLastUserIdMethodProxy("getSyncAdapterTypesAsUser"))
        addMethodProxy(ReplaceLastUserIdMethodProxy("getSyncStatusAsUser"))
        addMethodProxy(ReplaceLastUserIdMethodProxy("isSyncPendingAsUser"))
        addMethodProxy(ReplaceLastUserIdMethodProxy("putCache"))
        addMethodProxy(ReplaceLastUserIdMethodProxy("getCache"))
        addMethodProxy(SyncAsUserProxy("syncAsUser"))
        addMethodProxy(ReplaceLastUserIdMethodProxy("cancelSyncAsUser"))
    }

    @Throws(Throwable::class)
    override fun inject() {
        super.inject()
        ContentResolverMirror.sContentService?.set(invocationStub.proxyInterface)
    }
}
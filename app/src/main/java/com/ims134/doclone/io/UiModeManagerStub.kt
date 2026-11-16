package com.ims134.doclone.io

import android.os.IInterface
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.VirtualCore
import java.lang.reflect.Modifier
import java.lang.reflect.Method
import java.util.Collections

/* JADX INFO: renamed from: io.ro8 */
/* JADX INFO: compiled from: UiModeManagerStub.java */
/* JADX INFO: loaded from: classes2.dex */
class UiModeManagerStub : BinderInvocationProxy(
    uiModeManagerStubClass(),
    "uimode",
) {

    /**
     * Was `ContentProviderHolder`. Rewrites the trailing `Int` argument if it's `0` or the current
     * virtual uid, replacing it with the host user id. Used by `addCallback`,
     * `removeCallback`, and `getContrast` — all of which take ContentProviderHolder userId at the
     * last position.
     */
    class UserIdRewritingProxy(name: String) : StaticMethodProxy(name) {

        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            if (args == null || args.isEmpty()) {
                return super.beforeCall(who, method, args)
            }
            val parameterTypes = method.parameterTypes
            val last = args.size - 1
            if (last in parameterTypes.indices) {
                val paramType = parameterTypes[last]
                if ((paramType == Int::class.javaPrimitiveType ||
                            paramType == Int::class.javaObjectType)
                ) {
                    val value = args[last]
                    if (value is Int && (value == 0 || value == VUserHandle.getVUid())) {
                        args[last] = VirtualCore.gCore.getHostUserId()
                    }
                }
            }
            return super.beforeCall(who, method, args)
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ResultStaticMethodProxy("getForceInvertState", 0))
        addMethodProxy(UserIdRewritingProxy("addCallback"))
        addMethodProxy(UserIdRewritingProxy("removeCallback"))
        addMethodProxy(UserIdRewritingProxy("getContrast"))
        addMethodProxy(ResultStaticMethodProxy("getForceInvertOverrideState", 0))
        addMethodProxy(
            ResultStaticMethodProxy(
                "getAllForceInvertAlwaysDisableApps",
                Collections.emptyList<Any?>(),
            )
        )
        addMethodProxy(ResultStaticMethodProxy("setForceInvertOverrideState", false))
    }

    @Throws(Throwable::class)
    override fun inject() {
        super.inject()
        val proxyInterface = invocationStub.proxyInterface ?: return
        try {
            val globalsField = Class.forName("android.app.UiModeManager")
                .getDeclaredField("sGlobals")
            if (Modifier.isStatic(globalsField.modifiers)) {
                globalsField.isAccessible = true
                val globals = globalsField.get(null) ?: return

                val serviceField = globals.javaClass.getDeclaredField("mService")
                serviceField.isAccessible = true
                if (serviceField.type.isInstance(proxyInterface)) {
                    serviceField.set(globals, proxyInterface)
                }

                try {
                    val forceInvertField = globals.javaClass
                        .getDeclaredField("mForceInvertState")
                    forceInvertField.isAccessible = true
                    forceInvertField.setInt(globals, 0)
                } catch (_: Throwable) {
                    // Field absent on this API level — ignore.
                }
            }
        } catch (th: Throwable) {
            // Original: `th.toString();` — ContentProviderHolder stripped log. Preserved as ContentProviderHolder no-op.
            @Suppress("UNUSED_EXPRESSION")
            th.toString()
        }
    }

    companion object {
        /** Was the constructor's `Class.forName(...)` block. */
        private fun uiModeManagerStubClass(): Class<*>? = try {
            Class.forName("android.app.IUiModeManager\$Stub")
        } catch (_: Throwable) {
            null
        }
    }
}
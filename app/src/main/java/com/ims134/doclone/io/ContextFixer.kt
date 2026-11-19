package com.ims134.doclone.io

import android.content.Context
import android.content.ContextWrapper
import android.os.DropBoxManager
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.MethodInvocationProxy
import com.ims134.doclone.clone.VirtualCore


/* JADX INFO: renamed from: io.i41 */
/* JADX INFO: compiled from: ContextFixer.java */
/* JADX INFO: loaded from: classes2.dex */
@Suppress("UNCHECKED_CAST", "UNUSED_EXPRESSION")
object ContextFixer {

    /**
     * Was `a`. Injects the proxy interface from the injector registered for [cls]
     * onto the system service named [serviceName] — replaces that service's
     * `mService` field with the proxy so subsequent calls go through the stub.
     */
    @JvmStatic
    fun injectServiceProxy(
        context: Context,
        serviceName: String,
        cls: Class<out IInjector>,
    ) {
        val stubManager = InvocationStubManager.instance
        stubManager.checkEnv(cls)

        val injector = stubManager.mInjectors[cls]
        val proxy = injector as? MethodInvocationProxy<*>
        val binderStub = proxy?.invocationStub as? BinderInvocationStub
        val proxyInterface = binderStub?.proxyInterface ?: return

        try {
            val systemService = context.getSystemService(serviceName) ?: return
            Reflect(systemService).set(proxyInterface, "mService")
        } catch (th: Throwable) {
            th.toString()
        }
    }

    /**
     * Was `b`. Re-installs the injection chain on ContentProviderHolder Context:
     *  - clears the PackageManager cache
     *  - injects DropBoxManager stub
     *  - rewrites mBasePackageName / mOpPackageName / ContentResolver package
     *  - injects connectivity / netstats / clipboard / tethering / credentials
     *  - conditionally injects HwTelephonyStub
     *  - rewrites the attribution source's uid/packageName
     */
    @JvmStatic
    fun fixContext(context: Context) {
        try {
            context.packageName
            InvocationStubManager.instance.checkEnv(GraphicsStatsStub::class.java)

            // Unwrap ContextWrapper chain (max 10 to prevent infinite loops)
            var ctx: Context = context
            var depth = 0
            while (ctx is ContextWrapper) {
                ctx = ctx.baseContext
                depth++
                if (depth >= 10) return
            }

            // Force-reset PackageManager cache and re-fetch
            ContextImpl.mPackageManager?.set(ctx, null)
            try {
                ctx.packageManager
            } catch (_: Throwable) {
            }

            if (!VirtualCore.gCore.isClientProcess()) return

            // --- DropBoxManager mService injection ---
            val dropBoxManager = ctx.getSystemService("dropbox") as? DropBoxManager
            val dropBoxInjector =
                InvocationStubManager.instance.mInjectors[DropBoxManagerStub::class.java]
            val dropBoxProxy = dropBoxInjector as? MethodInvocationProxy<*>
            val dropBoxStub = dropBoxProxy?.invocationStub as? BinderInvocationStub
            if (dropBoxStub != null) {
                try {
                    Reflect(dropBoxManager).set(dropBoxStub.proxyInterface, "mService")
                } catch (_: Exception) {
                    // Was `catch (ReflectException)`. `ReflectException` lives in ContentProviderHolder
                    // polestar package (banned by import rule). Since `Reflect.set`
                    // wraps its failures in ReflectException (ContentProviderHolder RuntimeException),
                    // catching Exception here is functionally equivalent for the
                    // reflection failure path. If you have ContentProviderHolder non-polestar shadow,
                    // swap to `catch (_: ReflectException)`.
                }
            }

            // --- Package name rewriting to host ---
            val hostPkg = VirtualCore.gCore.hostPackageName
            ContextImpl.mBasePackageName?.set(ctx, hostPkg)
            ContextImplKitkatMirror.mOpPackageName?.set(ctx, hostPkg)
            ContentResolverJBMR2Mirror.mPackageName?.set(ctx.contentResolver, hostPkg)

            // --- Service injections ---
            injectServiceProxy(ctx, "connectivity", ConnectivityStub::class.java)
            injectServiceProxy(ctx, "netstats", NetworkStatsManagerStub::class.java)

            // --- Clipboard ---
            val stubManager = InvocationStubManager.instance
            stubManager.checkEnv(ClipboardStub::class.java)
            val clipboardStub =
                stubManager.mInjectors[ClipboardStub::class.java] as? ClipboardStub
            if (clipboardStub != null) {
                try {
                    clipboardStub.repatchService(ctx)
                } catch (th: Throwable) {
                    th.toString()
                }
            }

            // --- SDK-gated stubs ---
            if (BuildCompat.sdk30OrLater()) {
                InvocationStubManager.instance.checkEnv(TetheringServiceStub::class.java)
            }
            if (BuildCompat.sdk34OrLater()) {
                InvocationStubManager.instance.checkEnv(CredentialManagerStub::class.java)
            }

            // --- HwTelephonyStub (conditional — only if vendor service exists) ---
            val stubManager2 = InvocationStubManager.instance
            if (VirtualCore.gCore.isClientProcess() &&
                IHwTelephony.Stub.TYPE != null &&
                HwTelephonyStub.fetchBinder() != null
            ) {
                synchronized(stubManager2.mInjectors) {
                    val existing = stubManager2.mInjectors[HwTelephonyStub::class.java]
                    if (existing == null || existing.isEnvBad()) {
                        try {
                            val hwStub = HwTelephonyStub()
                            hwStub.inject()
                            stubManager2.mInjectors[HwTelephonyStub::class.java] = hwStub
                        } catch (_: Throwable) {
                        }
                    }
                }
            }

            // --- Attribution source rewrite (SDK 31+) ---
            if (!BuildCompat.sdk31OrLater()) return
            val source = ContextImpl.getAttributionSource?.call(ctx, *emptyArray<Any?>()) ?: return
            try {
                val sourceType = AttributionSourceMirror.TYPE
                if (sourceType != null && sourceType.isInstance(source)) {
                    val currentPackage = VClientImpl.get().getCurrentPackage()
                    val vUid = VClientImpl.get().getVUid()
                    val state = AttributionSourceMirror.mAttributionSourceState?.get(source)
                    if (currentPackage == null || state == null) return

                    // Stripped debug logs — preserved as no-op expression statements.
                    AttributionSourceState.uid?.get(state)
                    AttributionSourceState.packageName?.get(state)

                    AttributionSourceState.uid?.set(state, vUid)
                    AttributionSourceState.packageName?.set(state, currentPackage)

                    // Second pair of stripped debug logs.
                    AttributionSourceState.uid?.get(state)
                    AttributionSourceState.packageName?.get(state)
                }
            } catch (th: Throwable) {
                th.toString()
            }
        } catch (_: Throwable) {
        }
    }
}

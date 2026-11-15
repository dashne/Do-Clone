package com.ims134.doclone.io

import android.annotation.TargetApi
import android.os.IInterface
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.Method
import java.util.Collections

@TargetApi(21)
@Suppress("UNCHECKED_CAST")
class SessionManagerStub : BinderInvocationProxy(
    ISessionManagerMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "media_session",
) {

    /**
     * Was `Stub`. `createSession` — wraps the returned `IInterface` in Stub
     * [BinderInvocationStub] whose `getController` returns Stub
     * [MediaSessionStub]-wrapped interface.
     */
    class CreateSessionProxy : ReplaceLeadingStringArgsProxy("createSession") {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val result = super.call(who, method, args)
            if (result !is IInterface) return result
            val stub = BinderInvocationStub(result)
            stub.addMethodProxy(WrapControllerProxy("getController"))
            return stub.proxyInterface
        }
    }

    /**
     * Was inner class `Stub.C0245a`. `getController` — wraps the returned
     * `IInterface` in Stub [MediaSessionStub] so subsequent command calls go
     * through the `callerIdentity` rewrite.
     *
     * Moved from `CreateSessionProxy` inner-class to `SessionManagerStub` nested —
     * it doesn't reference any outer state, so this is bytecode-equivalent for
     * the `call()` behavior (and cleaner Kotlin).
     */
    class WrapControllerProxy(name: String) : StaticMethodProxy(name) {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val result = super.call(who, method, args)
            return if (result is IInterface) MediaSessionStub(result).proxyInterface else result
        }
    }

    /**
     * Was `b`. Replaces LEADING String args (not "any String arg") with the
     * host package. Stops at the first non-String parameter.
     */
    open class ReplaceLeadingStringArgsProxy(name: String) : StaticMethodProxy(name) {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            if (args != null) {
                val parameterTypes = method.parameterTypes
                val limit = minOf(parameterTypes.size, args.size)
                for (i in 0 until limit) {
                    if (parameterTypes[i] != String::class.java) break
                    args[i] = MethodProxy.getHostPkg()
                }
            }
            return super.beforeCall(who, method, args)
        }
    }

    /**
     * Was `c`. `BinderInvocationStub` wrapping an `IMediaSession` so that all
     * "command" methods (play/pause/adjustVolume/etc.) route through the
     * `callerIdentity` proxy — which rewrites the leading package args.
     */
    class MediaSessionStub(iface: IInterface) : BinderInvocationStub(iface) {

        /** Was `c.g`. The single shared `callerIdentity` proxy. */
        @JvmField
        val callerIdentityProxy: MethodProxy = CallerIdentityProxy("callerIdentity")

        /** Was `c.a`. Rewrites the leading package-arg(s) for command methods. */
        class CallerIdentityProxy(name: String) : StaticMethodProxy(name) {
            override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
                if (args != null && args.isNotEmpty()) {
                    val parameterTypes = method.parameterTypes
                    if (parameterTypes.isNotEmpty() &&
                        parameterTypes[0] == String::class.java
                    ) {
                        args[0] = MethodProxy.getHostPkg()
                    }
                    if (("adjustVolume" == method.name || "setVolumeTo" == method.name) &&
                        args.size > 1 &&
                        parameterTypes.size > 1 &&
                        parameterTypes[1] == String::class.java
                    ) {
                        args[1] = MethodProxy.getHostPkg()
                    }
                }
                return super.beforeCall(who, method, args)
            }
        }

        /** Was `c.b(String)`. Routes known command methods through the shared proxy. */
        override fun <H : MethodProxy> getMethodProxy(name: String): H? =
            if (name in MEDIA_SESSION_COMMANDS) {
                callerIdentityProxy as H
            } else {
                super.getMethodProxy(name)
            }

        companion object {
            /** Was `c.h`. The set of `IMediaSession` command method names. */
            @JvmField
            val MEDIA_SESSION_COMMANDS: Set<String> = Collections.unmodifiableSet(
                setOf(
                    "sendCommand",
                    "sendMediaButton",
                    "registerCallback",
                    "adjustVolume",
                    "setVolumeTo",
                    "prepare",
                    "prepareFromMediaId",
                    "prepareFromSearch",
                    "prepareFromUri",
                    "play",
                    "playFromMediaId",
                    "playFromSearch",
                    "playFromUri",
                    "skipToQueueItem",
                    "pause",
                    "stop",
                    "next",
                    "previous",
                    "fastForward",
                    "rewind",
                    "seekTo",
                    "rate",
                    "setPlaybackSpeed",
                    "sendCustomAction",
                )
            )
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(CreateSessionProxy())
        addMethodProxy(ReplaceLeadingStringArgsProxy("dispatchMediaKeyEvent"))
        addMethodProxy(ReplaceLeadingStringArgsProxy("dispatchMediaKeyEventToSessionAsSystemService"))
        addMethodProxy(ReplaceLeadingStringArgsProxy("dispatchVolumeKeyEvent"))
        addMethodProxy(ReplaceLeadingStringArgsProxy("dispatchVolumeKeyEventToSessionAsSystemService"))
        addMethodProxy(ReplaceLeadingStringArgsProxy("dispatchAdjustVolume"))

        addMethodProxy(ResultStaticMethodProxy("getSessions", Collections.emptyList<Any?>()))
        addMethodProxy(ResultStaticMethodProxy("addSessionsListener", null))
        addMethodProxy(ResultStaticMethodProxy("removeSessionsListener", null))
        addMethodProxy(ResultStaticMethodProxy("removeSessionsListenerForPackage", null))
        addMethodProxy(ResultStaticMethodProxy("getMediaKeyEventSession", null))
        addMethodProxy(ResultStaticMethodProxy("getMediaKeyEventSessionPackageName", ""))
        addMethodProxy(ResultStaticMethodProxy("addOnMediaKeyEventDispatchedListener", null))
        addMethodProxy(ResultStaticMethodProxy("removeOnMediaKeyEventDispatchedListener", null))
        addMethodProxy(ResultStaticMethodProxy("addOnMediaKeyEventSessionChangedListener", null))
        addMethodProxy(ResultStaticMethodProxy("removeOnMediaKeyEventSessionChangedListener", null))
        addMethodProxy(ReplaceLastUserIdMethodProxy("addSession2TokensListener"))
    }
}
package com.ims134.doclone.io

import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.reflect.RefStaticMethod
import android.os.IInterface
import com.ims134.doclone.clone.MethodProxy
import java.lang.reflect.Method

@Suppress("UNCHECKED_CAST")
class AudioManagerStub : BinderInvocationProxy(
    IAudioService.Stub.asInterface as RefStaticMethod<IInterface>?,
    "audio",
) {

    /**
     * Was `a`. Fallback proxy for methods that take an `AttributionSource` but
     * aren't covered by [PackageNameRewritingProxy]. Only rewrites the
     * AttributionSource arguments (not the package-name argument — those methods
     * don't use one).
     */
    inner class AttributionSourceRewritingProxy(name: String) : StaticMethodProxy(name) {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            rewriteAttributionSources(args)
            return super.beforeCall(who, method, args)
        }
    }

    /**
     * Was `b`. Known audio methods where the host-package-name argument lives at
     * Stub fixed index per method. Rewrites AttributionSource args first, then the
     * package-name arg at the index determined by the method name.
     *
     * The initial `i = 4` is intentional: `setWiredDeviceConnectionState` with
     * length >= 5 keeps `i = 4` (falls through the switch).
     */
    inner class PackageNameRewritingProxy(name: String) : StaticMethodProxy(name) {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            rewriteAttributionSources(args)
            if (method != null && args != null) {
                val idx = when (method.name) {
                    // Package at index 0
                    "hasAudioFocus",
                    "lowerVolumeToRs1",
                    "disableSafeMediaVolume" -> 0

                    // Package at index 1
                    "setRingerModeInternal",
                    "setMicrophoneMute",
                    "setRingerModeExternal" -> 1

                    // Package at index 2
                    "setMasterMute",
                    "handleVolumeKey",
                    "notifyAbsoluteVolumeChanged",
                    "registerDeviceVolumeDispatcherForAbsoluteVolume",
                    "setVolumeForDevice",
                    "getDeviceVolume",
                    "setDeviceVolume",
                    "setMode" -> 2

                    // Package at index 5
                    "requestAudioFocus" -> 5

                    // Package at index 3 (most common — stream/group volume)
                    "adjustStreamVolume",
                    "adjustStreamVolumeWithAttribution",
                    "setVolumeIndexForAttributes",
                    "setStreamVolumeWithAttribution",
                    "setStreamVolume",
                    "adjustSuggestedStreamVolume",
                    "adjustVolumeForDevice",
                    "setVolumeGroupVolumeIndex",
                    "adjustVolumeGroupVolume" -> 3

                    // 3-arg legacy: no package; 4-arg: package at index 3
                    "abandonAudioFocus" -> if (args.size <= 3) -1 else 3

                    // 5-arg: package at index 4 (initial value); shorter: index 2
                    "setWiredDeviceConnectionState" -> if (args.size < 5) 2 else 4

                    else -> -1
                }
                if (idx in args.indices && args[idx] is String) {
                    args[idx] = VirtualCore.gCore.hostPackageName
                }
            }
            // Original returns true unconditionally, bypassing super.beforeCall.
            return true
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        // Known methods with Stub fixed package-argument index
        addPackageNameProxy("adjustSuggestedStreamVolume")
        addPackageNameProxy("adjustStreamVolume")
        addPackageNameProxy("adjustStreamVolumeWithAttribution")
        addPackageNameProxy("setStreamVolume")
        addPackageNameProxy("setStreamVolumeWithAttribution")
        addPackageNameProxy("setDeviceVolume")
        addPackageNameProxy("setVolumeForDevice")
        addPackageNameProxy("adjustVolumeForDevice")
        addPackageNameProxy("notifyAbsoluteVolumeChanged")
        addPackageNameProxy("getDeviceVolume")
        addPackageNameProxy("handleVolumeKey")
        addPackageNameProxy("setMasterMute")
        addPackageNameProxy("setVolumeIndexForAttributes")
        addPackageNameProxy("setVolumeGroupVolumeIndex")
        addPackageNameProxy("adjustGroupVolume")
        addPackageNameProxy("setMicrophoneMute")
        addPackageNameProxy("setRingerModeExternal")
        addPackageNameProxy("setRingerModeInternal")
        addPackageNameProxy("setMode")
        addPackageNameProxy("requestAudioFocus")
        addPackageNameProxy("abandonAudioFocus")
        addPackageNameProxy("setWiredDeviceConnectionState")
        addPackageNameProxy("disableSafeMediaVolume")
        addPackageNameProxy("lowerVolumeToRs1")
        addPackageNameProxy("registerDeviceVolumeDispatcherForAbsoluteVolume")
        addPackageNameProxy("hasAudioFocus")

        // Reflection pass: find every method whose signature includes an
        // AttributionSource parameter, and if it isn't already proxied, add
        // the fallback AttributionSource-rewriting proxy.
        val baseInterface = invocationStub.baseInterface ?: return
        val attributionSourceType = AttributionSourceMirror.TYPE ?: return

        for (m in baseInterface.javaClass.methods) {
            for (paramType in m.parameterTypes) {
                if (!attributionSourceType.isAssignableFrom(paramType)) continue
                if (invocationStub.getMethodProxy<MethodProxy>(m.name) != null) break
                addMethodProxy(AttributionSourceRewritingProxy(m.name))
                break
            }
        }
    }

    /** Was `c`. */
    private fun addPackageNameProxy(name: String) {
        addMethodProxy(PackageNameRewritingProxy(name))
    }

    companion object {
        /**
         * Was `b(Object[])`. Rewrites every `AttributionSource` argument in [args]
         * to use the host's uid and package name. No-op if AttributionSourceMirror
         * is unavailable.
         */
        @JvmStatic
        fun rewriteAttributionSources(args: Array<Any?>?) {
            if (args == null) return
            val type = AttributionSourceMirror.TYPE ?: return
            for (i in args.indices) {
                val v = args[i] ?: continue
                if (type.isInstance(v)) {
                    val vc = VirtualCore.gCore
                    args[i] = AttributionSourceCompat.withPackage(vc.uid, vc.hostPackageName, v)
                }
            }
        }
    }
}
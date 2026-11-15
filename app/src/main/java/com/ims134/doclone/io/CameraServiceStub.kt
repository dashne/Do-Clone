package com.ims134.doclone.io


import android.os.Parcel
import android.os.Parcelable
import android.os.Process
import android.text.TextUtils
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VirtualCore

import java.lang.reflect.Method
import java.util.Arrays
import java.util.HashSet

class CameraServiceStub : BinderInvocationProxy(
    getCameraServiceStubClass(),
    "media.camera",
) {

    /**
     * Was inner class `a`. Rewrites args before delegating to the real service:
     *  - `AttributionSource` args get `uid`/`packageName` swapped to host's.
     *  - `AttributionSourceState` args are parcel-copied with uid=-1,
     *    packageName=host, pid=-1 (so the framework attributes the call to the host).
     *  - The current package-name string is replaced with the host package.
     *  - For camera-connect methods, the next int arg (uid) and — for `connect` —
     *    the following int arg (pid) are set to -1 if they match the virtual
     *    uid/pid, so the camera service doesn't leak the clone process identity.
     */
    class CameraServiceProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args != null) rewriteCameraArgs(method, args)
            return method.invoke(who, *(args ?: emptyArray()))
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        val baseInterface = invocationStub.baseInterface ?: return
        val stateType = AttributionSourceState.TYPE
        val sourceType = AttributionSourceMirror.TYPE

        for (method in baseInterface.javaClass.methods) {
            val name = method.name
            val alreadyProxied = invocationStub.getMethodProxy<MethodProxy>(name) != null
            if (alreadyProxied) continue

            if (name in CAMERA_METHOD_NAMES) {
                addMethodProxy(CameraServiceProxy(name))
            } else {
                // Install only for methods that take an AttributionSource/State
                // (newer framework methods) — matches the original's parameter scan.
                val hasAttributionParam = method.parameterTypes.any { paramType ->
                    (stateType != null && stateType.isAssignableFrom(paramType)) ||
                            (sourceType != null && sourceType.isAssignableFrom(paramType))
                }
                if (hasAttributionParam) addMethodProxy(CameraServiceProxy(name))
            }
        }
    }

    companion object {
        /** Was field `c`. Camera-connection methods that need uid/pid rewriting. */
        @JvmField
        val CAMERA_METHOD_NAMES: HashSet<String> =
            HashSet(Arrays.asList("connect", "connectLegacy", "connectDevice"))

        /** Was field `d`. Cached `ICameraService$Stub` class. */
        @JvmField
        var cachedStubClass: Class<*>? = null

        /** Was field `e`. Whether the lookup has been attempted (don't retry). */
        @JvmField
        var classLookupDone: Boolean = false

        /**
         * Was method `c()`. Lazily loads and caches `ICameraService$Stub`.
         * Called from the primary-constructor `super(...)` call — the companion
         * is initialized on first access, then this returns the cached value.
         */
        @JvmStatic
        fun getCameraServiceStubClass(): Class<*>? {
            synchronized(CameraServiceStub::class.java) {
                if (!classLookupDone) {
                    try {
                        cachedStubClass = Class.forName("android.hardware.ICameraService\$Stub")
                    } catch (_: Throwable) {
                    }
                    classLookupDone = true
                }
                return cachedStubClass
            }
        }

        /**
         * Was method `b(Method, int)`. Returns the index of the next `int`/`Integer`
         * parameter at or after [fromIndex], or -1 if none.
         */
        @JvmStatic
        fun findNextIntArg(method: Method, fromIndex: Int): Int {
            val parameterTypes = method.parameterTypes
            for (i in maxOf(0, fromIndex) until parameterTypes.size) {
                val t = parameterTypes[i]
                if (t == java.lang.Integer.TYPE || t == java.lang.Integer::class.java) return i
            }
            return -1
        }

        /**
         * Applies all the argument rewrites described on [CameraServiceProxy].
         * Extracted from `CameraServiceProxy.call` for readability.
         */
        @JvmStatic
        fun rewriteCameraArgs(method: Method, args: Array<Any?>) {
            val currentPackage = VClientImpl.get().getCurrentPackage()
            val vUid = VClientImpl.get().getVUid()
            val baseVUid = VClientImpl.get().getBaseVUid()
            val vUidLowBits = vUid % 100000

            val stateType = AttributionSourceState.TYPE
            val sourceType = AttributionSourceMirror.TYPE
            var packageStringIndex = -1

            for (i in args.indices) {
                var arg: Any? = args[i] ?: continue

                if (stateType != null && stateType.isInstance(arg)) {
                    // AttributionSourceState — copy via parcel, then adjust uid/package/pid.
                    if (arg is Parcelable) {
                        val parcel = Parcel.obtain()
                        try {
                            parcel.writeParcelable(arg, 0)
                            parcel.setDataPosition(0)
                            val copy = parcel.readParcelable<Parcelable>(arg.javaClass.classLoader)
                            if (copy != null) {
                                AttributionSourceState.uid?.set(copy, -1)
                                AttributionSourceState.packageName?.set(
                                    copy, VirtualCore.gCore.hostPackageName,
                                )
                                AttributionSourceState.pid?.set(copy, -1)
                                arg = copy
                            }
                        } catch (_: Throwable) {
                            // Original: `th.toString();` — stripped log.
                        } finally {
                            parcel.recycle()
                        }
                    }
                    args[i] = arg
                } else if (sourceType != null && sourceType.isInstance(arg)) {
                    // AttributionSource — replace with host-rewritten copy.
                    args[i] = AttributionSourceCompat.withPackage(
                        VirtualCore.gCore.uid, VirtualCore.gCore.hostPackageName, arg,
                    )
                } else if (arg is String &&
                    !TextUtils.isEmpty(currentPackage) &&
                    currentPackage == arg
                ) {
                    args[i] = VirtualCore.gCore.hostPackageName
                    packageStringIndex = i
                }
            }

            if (packageStringIndex < 0) return
            val methodName = method.name
            if (methodName !in CAMERA_METHOD_NAMES) return

            val uidIndex = findNextIntArg(method, packageStringIndex + 1)
            if (uidIndex < 0) return

            val uidArg = args[uidIndex]
            if (uidArg !is Int) return
            if (uidArg != vUid && uidArg != baseVUid && uidArg != vUidLowBits) return

            args[uidIndex] = -1

            if (methodName == "connect") {
                val pidIndex = findNextIntArg(method, uidIndex + 1)
                if (pidIndex >= 0 &&
                    args[pidIndex] is Int &&
                    args[pidIndex] == Process.myPid()
                ) {
                    args[pidIndex] = -1
                }
            }
        }
    }
}
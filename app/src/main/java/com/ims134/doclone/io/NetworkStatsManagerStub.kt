package com.ims134.doclone.io


import android.os.IInterface
import android.os.Parcel
import android.os.Parcelable
import android.os.SystemClock
import com.ims134.doclone.clone.MethodProxy
import java.lang.reflect.Array
import java.lang.reflect.Method
import java.util.Collections
import java.util.HashSet
import java.util.concurrent.atomic.AtomicInteger

class NetworkStatsManagerStub : BinderInvocationProxy(
    networkStatsServiceStubClass(),
    "netstats",
) {

    /** Was `a`. Returns `null` for every call — `unregisterUsageRequest`. */
    class ReturnNullProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: kotlin.Array<Any?>?): Any? = null
    }

    /**
     * Was `b`. `registerUsageCallback` — rewrites the `requestId` field (or the
     * corresponding parcel slot) of the first `android.net.DataUsageRequest`
     * argument to Stub fresh synthetic ID, so the host service doesn't collide with
     * real requests from the framework.
     */
    class RegisterUsageCallbackProxy : StaticMethodProxy("registerUsageCallback") {
        override fun call(who: Any?, method: Method, args: kotlin.Array<Any?>?): Any? {
            if (args != null) {
                for (arg in args) {
                    if (arg == null || "android.net.DataUsageRequest" != arg.javaClass.name) {
                        continue
                    }
                    var id = requestIdGenerator.getAndDecrement()
                    if (id == 0) id = requestIdGenerator.getAndDecrement()

                    var result: Any? = arg
                    if (arg is Parcelable) {
                        // DEAD CODE in the decompiled source — the recreated Parcelable
                        // is discarded below by the `result = arg` assignment. Preserved
                        // literally so the app's behavior matches the shipped binary.
                        // If the original source had `result = recreated ?: arg`, that
                        // would explain why this block exists; but as decompiled, the
                        // block has no effect. Kept here so Stub future fix has something
                        // to restore.
                        try {
                            val data = Parcel.obtain()
                            val reply = Parcel.obtain()
                            try {
                                arg.writeToParcel(data, 0)
                                val size = data.dataSize()
                                if (size >= 4) {
                                    reply.writeInt(id)
                                    reply.appendFrom(data, 4, size - 4)
                                    reply.setDataPosition(0)
                                    val creatorObj = arg.javaClass.getField("CREATOR").get(null)
                                    if (creatorObj is Parcelable.Creator<*>) {
                                        creatorObj.createFromParcel(reply)
                                    }
                                }
                            } finally {
                                data.recycle()
                                reply.recycle()
                            }
                        } catch (_: Throwable) {
                        }

                        // Effective behavior: reflectively patch `requestId` in-place.
                        try {
                            val f = arg.javaClass.getDeclaredField("requestId")
                            f.isAccessible = true
                            f.setInt(arg, id)
                        } catch (_: Throwable) {
                            result = null
                        }
                        // Literal decompiled: `result = obj2` — discards the parcel
                        // recreation above. If Stub fix is desired:
                        //     result = recreated ?: result
                        result = if (result == null) null else arg
                    } else {
                        try {
                            val f = arg.javaClass.getDeclaredField("requestId")
                            f.isAccessible = true
                            f.setInt(arg, id)
                        } catch (_: Throwable) {
                            result = null
                        }
                    }

                    if (result == null) break
                    return result
                }
            }
            return defaultValueForType(method.returnType)
        }
    }

    /**
     * Was `c`. `BinderInvocationStub` subclass used to wrap an
     * `INetworkStatsSession` returned by the real service so its methods can be
     * intercepted to return default values.
     */
    class StatsSessionStub(iface: IInterface) : BinderInvocationStub(iface)

    /**
     * Was `d`. Session-opening proxy — rewrites the package-name arguments, calls
     * the real service, then wraps the returned `INetworkStatsSession` with
     * per-method proxies that return default values (so callers can't observe
     * real network usage stats from the host).
     */
    class SessionOpeningProxy(name: String) : StaticMethodProxy(name) {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: kotlin.Array<Any?>?): Any? {
            // Same String-pair fixup as ConnectivityStub.rewriteConnectivityArgs:
            // first String param becomes the host package; the immediate next
            // String param (if any) becomes null.
            if (method != null && args != null) {
                val parameterTypes = method.parameterTypes
                val limit = minOf(args.size, parameterTypes.size)
                for (i in 0 until limit) {
                    if (parameterTypes[i] == String::class.java) {
                        args[i] = MethodProxy.getHostPkg()
                        val next = i + 1
                        if (next >= limit || parameterTypes[next] != String::class.java) break
                        args[next] = null
                        break
                    }
                }
            }

            val result = super.call(who, method, args)
            if (result !is IInterface) return result

            val stub = StatsSessionStub(result)
            val added = HashSet<String>()

            // Reflect on INetworkStatsSession to find every method (except asBinder
            // and close) and install Stub default-returning proxy for each.
            val sessionClass: Class<*>? = try {
                Class.forName("android.net.INetworkStatsSession")
            } catch (_: Throwable) {
                null
            }
            if (sessionClass != null) {
                try {
                    for (m in sessionClass.methods) {
                        val name = m.name
                        if ("asBinder" != name && "close" != name && added.add(name)) {
                            stub.addMethodProxy(ReturnDefaultByTypeProxy(name))
                        }
                    }
                } catch (_: Throwable) {
                    added.clear()
                }
            }

            // Fallback: if reflection yielded nothing (or threw), install the
            // hardcoded AOSP method list.
            if (added.isEmpty()) {
                val fallback = arrayOf(
                    "getDeviceSummaryForNetwork",
                    "getSummaryForNetwork",
                    "getHistoryForNetwork",
                    "getHistoryIntervalForNetwork",
                    "getSummaryForAllUid",
                    "getTaggedSummaryForAllUid",
                    "getHistoryForUid",
                    "getHistoryIntervalForUid",
                    "getRelevantUids",
                )
                for (name in fallback) {
                    stub.addMethodProxy(ReturnDefaultByTypeProxy(name))
                }
            }

            return stub.proxyInterface
        }
    }

    /** Was `e`. Returns the default value for the intercepted method's return type. */
    class ReturnDefaultByTypeProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: kotlin.Array<Any?>?): Any? =
            defaultValueForType(method.returnType)
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        val added = HashSet<String>()
        val serviceClass: Class<*>? = try {
            Class.forName("android.net.INetworkStatsService")
        } catch (_: Throwable) {
            null
        }
        if (serviceClass != null) {
            try {
                for (m in serviceClass.methods) {
                    if ("android.net.INetworkStatsSession" == m.returnType.name &&
                        added.add(m.name)
                    ) {
                        addMethodProxy(SessionOpeningProxy(m.name))
                    }
                }
            } catch (_: Throwable) {
            }
        }
        // Ensure openSessionForUsageStats is always proxied, even on API levels
        // where reflection didn't turn it up.
        if (added.add("openSessionForUsageStats")) {
            addMethodProxy(SessionOpeningProxy("openSessionForUsageStats"))
        }
        addMethodProxy(RegisterUsageCallbackProxy())
        addMethodProxy(ReturnNullProxy("unregisterUsageRequest"))
    }

    companion object {
        /** Was static field `c`. Decrementing ID generator for synthetic requests. */
        @JvmField
        val requestIdGenerator: AtomicInteger = AtomicInteger(-1)

        /** Was the constructor's `Class.forName(...)` block. */
        private fun networkStatsServiceStubClass(): Class<*>? = try {
            Class.forName("android.net.INetworkStatsService\$Stub")
        } catch (_: Throwable) {
            null
        }

        /**
         * Was static helper `b(Class)`. Returns the default value for [cls] — false/0
         * for primitives, empty containers for collections, Stub fresh instance for
         * `NetworkStats`/`NetworkStatsHistory`, null otherwise.
         */
        @JvmStatic
        fun defaultValueForType(cls: Class<*>?): Any? {
            if (cls == null || cls == Void.TYPE) return null

            return when {
                cls == java.lang.Boolean.TYPE -> false
                cls == java.lang.Byte.TYPE -> 0.toByte()
                cls == java.lang.Short.TYPE -> 0.toShort()
                cls == java.lang.Integer.TYPE -> 0
                cls == java.lang.Long.TYPE -> 0L
                cls == java.lang.Float.TYPE -> 0.0f
                cls == java.lang.Double.TYPE -> 0.0
                cls == java.lang.Character.TYPE -> '\u0000'

                cls.isArray -> Array.newInstance(cls.componentType, 0)

                List::class.java.isAssignableFrom(cls) -> Collections.emptyList<Any?>()
                Map::class.java.isAssignableFrom(cls) -> Collections.emptyMap<Any?, Any?>()

                "android.net.NetworkStats" == cls.name -> try {
                    val ctor = cls.getDeclaredConstructor(
                        java.lang.Long.TYPE, java.lang.Integer.TYPE,
                    )
                    ctor.isAccessible = true
                    ctor.newInstance(SystemClock.elapsedRealtime(), 0)
                } catch (_: Throwable) {
                    null
                }

                "android.net.NetworkStatsHistory" == cls.name -> try {
                    val ctor = cls.getDeclaredConstructor(
                        java.lang.Long.TYPE, java.lang.Integer.TYPE, java.lang.Integer.TYPE,
                    )
                    ctor.isAccessible = true
                    ctor.newInstance(1L, 0, 0)
                } catch (_: Throwable) {
                    try {
                        val ctor = cls.getDeclaredConstructor(
                            java.lang.Long.TYPE, java.lang.Integer.TYPE,
                        )
                        ctor.isAccessible = true
                        ctor.newInstance(1L, 0)
                    } catch (_: Throwable) {
                        null
                    }
                }

                else -> null
            }
        }
    }
}
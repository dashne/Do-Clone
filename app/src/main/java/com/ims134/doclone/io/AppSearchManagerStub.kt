package com.ims134.doclone.io

import android.content.Context
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.Parcelable
import android.os.Process
import android.os.RemoteException
import android.util.Base64
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.VirtualCore
import java.lang.reflect.Field
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.ArrayList
import java.util.Collections
import java.util.HashSet

/**
 * Was `io.hr`. Blocks most AppSearch operations from clones while allowing
 * read-only or safe paths through with rewritten arguments (host package,
 * host attribution, encoded database names).
 */
@Suppress("UNCHECKED_CAST", "UNUSED_EXPRESSION")
class AppSearchManagerStub : BinderInvocationProxy(
    appSearchManagerStubClass(),
    "app_search",
) {

    /**
     * Was `e`. Thread-local state shared between `beforeCall` and `call` —
     * beforeCall populates the state, call consumes it (and removes it).
     */
    class BlockedState {
        var blocked: Boolean = false
        var hardBlock: Boolean = false
    }

    /**
     * Was `b`. Main method proxy for AppSearch operations. All the arg-rewriting
     * logic lives in `beforeCall`; `call` decides whether to return a failure
     * result, fire the caller's callback with a failure, or delegate to the real
     * service based on the state set by `beforeCall`.
     */
    class SearchMethodProxy(name: String) : StaticMethodProxy(name) {
        private val threadState = ThreadLocal<BlockedState>()

        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            val state = BlockedState()
            try {
                populateState(state, method, args)
            } finally {
                threadState.set(state)
            }
            return super.beforeCall(who, method, args)
        }

        private fun populateState(state: BlockedState, method: Method, args: Array<Any?>?) {
            val methodName = method.name

            // --- Hard-block blob methods (no safe rewrite possible) ---
            if (methodName in BLOB_METHODS) {
                state.blocked = true
                state.hardBlock = true
                return
            }

            // --- Arg-less or current-package-less: use simple method classification ---
            val currentPkg = VClientImpl.get().getCurrentPackage()
            if (args == null || currentPkg == null) {
                state.blocked = isGlobalMethod(methodName) || isObserverMethod(methodName)
                return
            }

            val sdk = android.os.Build.VERSION.SDK_INT
            val hostPkg = VirtualCore.gCore.hostPackageName!!
            var crossPkg = false

            if (sdk <= 32) {
                crossPkg = rewriteSdkUpTo32(methodName, currentPkg, hostPkg, args)
            } else if (sdk == 33) {
                crossPkg = rewriteSdk33(state, methodName, currentPkg, hostPkg, args)
            } else if (sdk == 34) {
                crossPkg = rewriteSdk34OrLater(methodName, currentPkg, hostPkg, args, state)
            } else {
                // SDK 35+
                crossPkg = rewriteSdk35Plus(state, methodName, currentPkg, hostPkg, args)
            }

            rewriteAttributionSources(args, hostPkg)
            wrapCallbackProxies(methodName, args)
        }

        private fun rewriteSdkUpTo32(
            methodName: String,
            currentPkg: String,
            hostPkg: String,
            args: Array<Any?>,
        ): Boolean {
            var crossPkg = false
            if (args.isNotEmpty() && args[0] is String) {
                crossPkg = currentPkg != args[0]
                args[0] = hostPkg
            }

            if (isGlobalMethod(methodName)) {
                rewriteGlobalSearchSpec(args)
                return crossPkg
            }

            val pkgIdx = packageArgIndex(methodName, false)
            if (pkgIdx in args.indices && args[pkgIdx] is String) {
                args[pkgIdx] = if (crossPkg) "__va2_blocked_cross_package__"
                else encodeDatabaseName(args[pkgIdx] as String)
            }

            if ("setSchema" == methodName && args.size >= 5 && args[2] is List<*>) {
                val schemaTypes = ArrayList<String>()
                for (obj in args[2] as List<*>) {
                    if (obj is Bundle) {
                        obj.getString("schemaType")?.let { schemaTypes.add(it) }
                    }
                }
                args[3] = schemaTypes
                args[4] = Collections.emptyMap<String, Any?>()
            }
            return crossPkg
        }

        private fun rewriteSdk33(
            state: BlockedState,
            methodName: String,
            currentPkg: String,
            hostPkg: String,
            args: Array<Any?>,
        ): Boolean {
            if (isGlobalMethod(methodName)) {
                state.blocked = !rewriteGlobalSearchSpec(args)
                return false
            }

            if (isObserverMethod(methodName)) {
                if (args.size > 1 && args[1] is String) args[1] = hostPkg
                if ("registerObserverCallback" == methodName) {
                    for (arg in args) {
                        if (arg is Bundle) {
                            val blocked = ArrayList<String>(1)
                            blocked.add("__va2_blocked_schema__")
                            arg.putStringArrayList("filterSchemas", blocked)
                        }
                    }
                }
                state.blocked = true
                return false
            }

            var crossPkg = rewriteRequestPackage(methodName, currentPkg, args)
            val pkgIdx = packageArgIndex(methodName, true)
            if (pkgIdx in args.indices && args[pkgIdx] is String) {
                args[pkgIdx] = if (crossPkg) "__va2_blocked_cross_package__"
                else encodeDatabaseName(args[pkgIdx] as String)
            }

            if ("setSchema" == methodName && args.size >= 4 && args[2] is List<*>) {
                try {
                    val visBuilderClass = Class.forName(
                        "android.app.appsearch.VisibilityDocument\$Builder"
                    )
                    val ctor = visBuilderClass.getDeclaredConstructor(String::class.java)
                    val setNotDisplayedBySystem = visBuilderClass.getDeclaredMethod(
                        "setNotDisplayedBySystem", Boolean::class.javaPrimitiveType
                    )
                    val build = visBuilderClass.getMethod("build")

                    val rewritten = ArrayList<Any?>()
                    for (obj in args[2] as List<*>) {
                        if (obj !is Bundle) continue
                        val schemaType = obj.getString("schemaType") ?: continue
                        val visBuilder = ctor.newInstance(schemaType)
                        setNotDisplayedBySystem.invoke(visBuilder, true)
                        val visDoc = build.invoke(visBuilder)
                        val bundle = visDoc.javaClass.getMethod("getBundle")
                            .invoke(visDoc) as Bundle
                        rewritten.add(bundle)
                    }
                    args[3] = rewritten
                    state.blocked = false
                } catch (th: Throwable) {
                    th.toString()
                    state.blocked = true
                }
            }
            return crossPkg
        }

        private fun rewriteSdk34OrLater(
            methodName: String,
            currentPkg: String,
            hostPkg: String,
            args: Array<Any?>,
            state: BlockedState,
        ): Boolean {
            // SDK 34 has a special-cased fast path for getDocuments / reportUsage
            if (android.os.Build.VERSION.SDK_INT == 34 &&
                (methodName == "getDocuments" || methodName == "reportUsage")
            ) {
                val crossPkg = rewriteRequestPackage(methodName, currentPkg, args)
                val pkgIdx = packageArgIndex(methodName, true)
                if (pkgIdx in args.indices && args[pkgIdx] is String) {
                    args[pkgIdx] = if (crossPkg) "__va2_blocked_cross_package__"
                    else encodeDatabaseName(args[pkgIdx] as String)
                }
                return crossPkg
            }

            // SDK 34 (other methods) and SDK 35+ both use the AidlRequest path
            return rewriteAidlRequestPath(state, methodName, currentPkg, hostPkg, args)
        }

        private fun rewriteSdk35Plus(
            state: BlockedState,
            methodName: String,
            currentPkg: String,
            hostPkg: String,
            args: Array<Any?>,
        ): Boolean {
            return rewriteAidlRequestPath(state, methodName, currentPkg, hostPkg, args)
        }

        /**
         * Find the AidlRequest argument, extract `mTargetPackageName` (or
         * `mObservedPackage`), rewrite it to host. Then handle h()/i() cases,
         * database name, and setSchema visibility. Returns true if the caller's
         * package differed from the target package.
         */
        private fun rewriteAidlRequestPath(
            state: BlockedState,
            methodName: String,
            currentPkg: String,
            hostPkg: String,
            args: Array<Any?>,
        ): Boolean {
            var request: Any? = null
            for (arg in args) {
                if (arg != null) {
                    val clsName = arg.javaClass.name
                    if (clsName.startsWith("android.app.appsearch.aidl.") &&
                        clsName.endsWith("AidlRequest")
                    ) {
                        request = arg
                        break
                    }
                }
            }

            if (request == null) {
                state.blocked = isGlobalMethod(methodName) || isObserverMethod(methodName)
                return false
            }

            // Extract and rewrite package name field
            var crossPkg = false
            val pkgField = findField(request.javaClass, "mTargetPackageName")
                ?: findField(request.javaClass, "mObservedPackage")
            if (pkgField != null) {
                try {
                    pkgField.isAccessible = true
                    crossPkg = currentPkg != pkgField.get(request)
                    pkgField.set(request, hostPkg)
                } catch (th: Throwable) {
                    th.toString()
                    crossPkg = true
                }
            }

            // h() - global methods: rewrite search spec's package names
            if (isGlobalMethod(methodName)) {
                val searchSpecField = findField(request.javaClass, "mSearchSpec")
                if (searchSpecField != null) {
                    try {
                        searchSpecField.isAccessible = true
                        val searchSpec = searchSpecField.get(request)
                        val pkgNamesField = findField(searchSpec!!.javaClass, "mPackageNames")
                        if (pkgNamesField != null) {
                            pkgNamesField.isAccessible = true
                            val blocked = ArrayList<String>(1)
                            blocked.add(blockedAuthority())
                            pkgNamesField.set(searchSpec, blocked)
                        }
                    } catch (th: Throwable) {
                        th.toString()
                    }
                }
            }

            // i() - observer methods: rewrite filter schemas to "blocked"
            if (isObserverMethod(methodName) && "registerObserverCallback" == methodName) {
                val observerSpecField = findField(request.javaClass, "mObserverSpec")
                if (observerSpecField != null) {
                    try {
                        observerSpecField.isAccessible = true
                        val observerSpec = observerSpecField.get(request)
                        val filterSchemasField = findField(
                            observerSpec!!.javaClass, "mFilterSchemas"
                        )
                        if (filterSchemasField != null) {
                            filterSchemasField.isAccessible = true
                            val blocked = ArrayList<String>(1)
                            blocked.add("__va2_blocked_schema__")
                            filterSchemasField.set(observerSpec, blocked)
                        }
                    } catch (th: Throwable) {
                        th.toString()
                    }
                }
                state.blocked = true
            }

            // Database name rewriting
            val dbNameField = findField(request.javaClass, "mDatabaseName")
            if (dbNameField != null) {
                try {
                    dbNameField.isAccessible = true
                    val dbName = dbNameField.get(request)
                    if (dbName is String) {
                        dbNameField.set(
                            request,
                            if (crossPkg) "__va2_blocked_cross_package__"
                            else encodeDatabaseName(dbName),
                        )
                    }
                } catch (th: Throwable) {
                    th.toString()
                    state.blocked = true
                }
            }

            // setSchema - rewrite visibility configs to "not displayed by system"
            if ("setSchema" == methodName) {
                var blocked = state.blocked
                val schemasField = findField(request.javaClass, "mSchemas")
                val visConfigsField = findField(request.javaClass, "mVisibilityConfigs")
                if (schemasField != null && visConfigsField != null) {
                    try {
                        schemasField.isAccessible = true
                        visConfigsField.isAccessible = true
                        val visBuilderClass = Class.forName(
                            "android.app.appsearch.InternalVisibilityConfig\$Builder"
                        )
                        val ctor = visBuilderClass.getDeclaredConstructor(String::class.java)
                        val setNotDisplayedBySystem = visBuilderClass.getDeclaredMethod(
                            "setNotDisplayedBySystem", Boolean::class.javaPrimitiveType
                        )
                        val build = visBuilderClass.getDeclaredMethod("build")

                        val newVisConfigs = ArrayList<Any?>()
                        for (schema in schemasField.get(request) as List<*>) {
                            val schemaType = schema!!.javaClass.getMethod("getSchemaType")
                                .invoke(schema) as String
                            val visBuilder = ctor.newInstance(schemaType)
                            setNotDisplayedBySystem.invoke(visBuilder, true)
                            newVisConfigs.add(build.invoke(visBuilder))
                        }
                        visConfigsField.set(request, newVisConfigs)
                        blocked = true
                    } catch (th: Throwable) {
                        th.toString()
                        blocked = true
                    }
                }
                state.blocked = blocked
            }
            return crossPkg
        }

        /**
         * Replace AttributionSource args with host-rewritten copies; replace
         * AppSearchAttributionSource args with a freshly-created host one; and
         * reflectively set any AidlRequest's attribution-source field.
         */
        private fun rewriteAttributionSources(args: Array<Any?>, hostPkg: String) {
            val attrSource: Any? = try {
                val attrSrcClass = Class.forName(
                    "android.app.appsearch.aidl.AppSearchAttributionSource"
                )
                val createMethod = attrSrcClass.getDeclaredMethod(
                    "createAttributionSource",
                    Context::class.java, Int::class.javaPrimitiveType,
                )
                createMethod.isAccessible = true
                createMethod.invoke(null, appContext, Process.myPid())
            } catch (th: Throwable) {
                th.toString()
                null
            }

            val attrSrcType = AttributionSourceMirror.TYPE
            val attrSrcClass = attrSource?.javaClass

            argLoop@ for (i in args.indices) {
                val arg = args[i] ?: continue@argLoop

                if (attrSrcType != null && attrSrcType.isInstance(arg)) {
                    args[i] = AttributionSourceCompat.withPackage(
                        VirtualCore.gCore.uid, hostPkg, arg
                    )
                    continue@argLoop
                }

                if (attrSource != null &&
                    "android.app.appsearch.aidl.AppSearchAttributionSource" ==
                    arg.javaClass.name
                ) {
                    args[i] = attrSource
                    continue@argLoop
                }

                if (attrSrcClass == null) continue@argLoop
                if (!arg.javaClass.name.startsWith("android.app.appsearch.aidl.")) {
                    continue@argLoop
                }

                var cls: Class<*>? = arg.javaClass
                while (cls != null && cls != Any::class.java) {
                    var found = false
                    for (field in cls.declaredFields) {
                        if (Modifier.isStatic(field.modifiers)) continue
                        if (!attrSrcClass.isAssignableFrom(field.type)) continue
                        try {
                            field.isAccessible = true
                            field.set(arg, attrSource)
                            found = true
                            break
                        } catch (th: Throwable) {
                            th.toString()
                        }
                    }
                    if (found) break
                    cls = cls.superclass
                }
            }
        }

        /**
         * Wrap the caller's IAppSearchResultCallback arg in a dynamic proxy whose
         * InvocationHandler applies syncSearchResults rewriting to the result
         * and whose binder forwards non-onResult transactions to the original.
         */
        private fun wrapCallbackProxies(methodName: String, args: Array<Any?>) {
            if (methodName !in CALLBACK_METHODS) return
            try {
                val callbackClass = Class.forName(
                    "android.app.appsearch.aidl.IAppSearchResultCallback"
                )
                for (i in args.indices) {
                    val arg = args[i]
                    if (arg == null || !callbackClass.isInstance(arg)) continue
                    val original = arg as IInterface
                    val binder = CallbackBinder(callbackClass, original)
                    val handler = CallbackInvocationHandler(original, binder)
                    val proxy = Proxy.newProxyInstance(
                        AppSearchManagerStub::class.java.classLoader,
                        arrayOf(callbackClass),
                        handler,
                    )
                    // Attach the proxy to the binder so queryLocalInterface(descriptor)
                    // returns the proxy, routing non-onResult transactions through
                    // CallbackBinder.onTransact → the proxy → the original.
                    binder.attachInterface(proxy as IInterface, binder.descriptor)
                    args[i] = proxy
                    break
                }
            } catch (th: Throwable) {
                th.toString()
            }
        }

        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val state = threadState.get()
            threadState.remove()

            if (state != null && state.blocked) {
                // Try to create a failed AppSearchResult for the return type
                val failedForReturn = createFailedResult(method.returnType)
                if (method.returnType != Void.TYPE && failedForReturn != null) {
                    return failedForReturn
                }

                // Try to fire the caller's onResult(failed) callback
                if (args != null) {
                    try {
                        val callbackClass = Class.forName(
                            "android.app.appsearch.aidl.IAppSearchResultCallback"
                        )
                        for (arg in args) {
                            if (arg == null || !callbackClass.isInstance(arg)) continue
                            for (cbMethod in callbackClass.methods) {
                                try {
                                    if ("onResult" == cbMethod.name &&
                                        cbMethod.parameterTypes.size == 1
                                    ) {
                                        val failedForArg = createFailedResult(
                                            cbMethod.parameterTypes[0]
                                        )
                                        if (failedForArg != null) {
                                            cbMethod.isAccessible = true
                                            cbMethod.invoke(arg, failedForArg)
                                            return null
                                        }
                                    }
                                } catch (th: Throwable) {
                                    th.toString()
                                    if (!state.hardBlock) {
                                        return method.invoke(
                                            who, *(args ?: emptyArray<Any?>())
                                        )
                                    }
                                    return defaultPrimitive(method.returnType)
                                }
                            }
                        }
                    } catch (_: Throwable) {
                        // fall through to default handling
                    }
                }

                if (!state.hardBlock) {
                    val prim = defaultPrimitive(method.returnType)
                    if (prim != null) return prim
                    return null
                }
                // hardBlock: fall through to call real method
            }
            return method.invoke(who, *(args ?: emptyArray<Any?>()))
        }

        companion object {
            private val BLOB_METHODS = setOf(
                "openBlobForWrite", "commitBlob", "openBlobForRead",
                "removeBlob", "setBlobVisibility",
            )

            private val CALLBACK_METHODS = setOf(
                "query", "search", "globalQuery", "globalSearch", "getNextPage",
            )

            private fun defaultPrimitive(t: Class<*>): Any? = when (t) {
                java.lang.Boolean.TYPE -> false
                java.lang.Character.TYPE -> '\u0000'
                java.lang.Byte.TYPE -> 0.toByte()
                java.lang.Short.TYPE -> 0.toShort()
                java.lang.Integer.TYPE -> 0
                java.lang.Long.TYPE -> 0L
                java.lang.Float.TYPE -> 0.0f
                java.lang.Double.TYPE -> 0.0
                else -> null
            }
        }
    }

    /** Was `c`. Handles calls on a dynamically-created proxy interface. */
    class CallbackInvocationHandler(
        private val original: IInterface,
        private val binder: IBinder,
    ) : InvocationHandler {

        @Throws(Throwable::class)
        override fun invoke(proxy: Any?, method: Method, args: Array<Any?>?): Any? {
            if ("asBinder" == method.name && method.parameterTypes.isEmpty()) {
                return binder
            }
            if (method.declaringClass == Any::class.java) {
                when (method.name) {
                    "toString" -> return "VirtualAppSearchCallback{$original}"
                    "hashCode" -> return System.identityHashCode(proxy)
                    "equals" -> return proxy === args?.getOrNull(0)
                }
            }
            try {
                method.isAccessible = true
                return method.invoke(original, *(args ?: emptyArray<Any?>()))
            } catch (e: InvocationTargetException) {
                throw e.targetException ?: e
            }
        }
    }

    /**
     * Was `d`. Binder that dispatches `onResult` / `onResultV2` transactions
     * from the framework to the original callback, applying result rewriting.
     * All other transactions pass through to the original binder.
     */
    class CallbackBinder(
        private val callbackClass: Class<*>,
        private val original: IInterface,
    ) : Binder() {

        val descriptor: String = try {
            val field = Class.forName(callbackClass.name + "\$Stub")
                .getDeclaredField("DESCRIPTOR")
            field.isAccessible = true
            field.get(null) as String
        } catch (_: Throwable) {
            callbackClass.name
        }

        @Throws(RemoteException::class)
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            // Find the callback method that matches this transaction code
            var targetMethod: Method? = null
            for (m in callbackClass.methods) {
                if (("onResult" != m.name && "onResultV2" != m.name) ||
                    m.parameterTypes.size != 1
                ) continue
                val txCode = try {
                    val f = Class.forName(callbackClass.name + "\$Stub")
                        .getDeclaredField("TRANSACTION_" + m.name)
                    f.isAccessible = true
                    f.getInt(null)
                } catch (_: Throwable) {
                    if ("onResult" == m.name) 1 else -1
                }
                if (txCode == code) {
                    targetMethod = m
                    break
                }
            }

            if (targetMethod == null) {
                return original.asBinder()!!.transact(code, data, reply, flags)
            }

            val startPos = data.dataPosition()
            try {
                data.enforceInterface(descriptor)
                val paramType = targetMethod.parameterTypes[0]
                val arg = if (data.readInt() != 0) {
                    (paramType.getField("CREATOR").get(null) as Parcelable.Creator<*>)
                        .createFromParcel(data)
                } else {
                    null
                }
                syncSearchResults(arg)
                targetMethod.isAccessible = true
                targetMethod.invoke(original, arg)
                reply?.writeNoException()
                return true
            } catch (e: InvocationTargetException) {
                val cause = e.targetException
                when (cause) {
                    is RemoteException -> throw cause
                    is RuntimeException -> throw cause
                    else -> throw RemoteException(cause?.toString() ?: "null")
                }
            } catch (th: Throwable) {
                // Setup failed (enforce/read/CREATOR reflection) — fall back to
                // the original binder so the caller still gets a real reply.
                data.setDataPosition(startPos)
                return original.asBinder()!!.transact(code, data, reply, flags)
            }
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        val baseInterface = invocationStub.baseInterface ?: return
        val seen = HashSet<String>()
        val attributionType = AttributionSourceMirror.TYPE

        for (method in baseInterface.javaClass.methods) {
            val methodName = method.name
            for (paramType in method.parameterTypes) {
                val paramName = paramType.name
                val relevant = paramType == String::class.java ||
                        (attributionType != null && attributionType.isAssignableFrom(paramType)) ||
                        "android.app.appsearch.aidl.AppSearchAttributionSource" == paramName ||
                        (paramName.startsWith("android.app.appsearch.aidl.") &&
                                paramName.endsWith("AidlRequest"))
                if (!relevant) continue
                if (!seen.add(methodName)) break
                if (invocationStub.getMethodProxy<MethodProxy>(methodName) != null) break
                addMethodProxy(SearchMethodProxy(methodName))
                break
            }
        }
    }

    companion object {

        /**
         * Was `b`. Inspects an `AppSearchResult` (or similar). If success, extracts
         * the result value and rewrites it (via [rewriteSearchResultPage]).
         */
        @JvmStatic
        fun syncSearchResults(obj: Any?) {
            if (obj == null) return
            try {
                val result = obj.javaClass.getMethod("getResult").invoke(obj)
                if (result != null &&
                    true == result.javaClass.getMethod("isSuccess").invoke(result)
                ) {
                    val value = result.javaClass.getMethod("getResultValue").invoke(result)
                    rewriteSearchResultPage(value)
                }
            } catch (th: Throwable) {
                th.toString()
            }
        }

        /** Was `c`. Blocked "authority" pseudo-package name for global searches. */
        @JvmStatic
        fun blockedAuthority(): String =
            VirtualCore.gCore.hostPackageName +
                    ".va.appsearch.blocked." +
                    hashPackageName(VClientImpl.get().getCurrentPackage() ?: "")

        /**
         * Was `d`. Creates a failed `AppSearchResult` and converts it to [cls]
         * (via `cls.fromFailedResult(result)` or `new cls(result)`). Returns null
         * for void/Object/unknown classes.
         */
        @JvmStatic
        fun createFailedResult(cls: Class<*>?): Any? {
            if (cls == null || cls == Void.TYPE || cls == Any::class.java) return null
            return try {
                val resultClass = Class.forName("android.app.appsearch.AppSearchResult")
                val failedResult = resultClass.getMethod(
                    "newFailedResult",
                    Int::class.javaPrimitiveType, String::class.java,
                ).invoke(
                    null, 8,
                    "This AppSearch operation is unavailable in the virtual environment",
                )
                try {
                    cls.getMethod("fromFailedResult", resultClass).invoke(null, failedResult)
                } catch (_: NoSuchMethodException) {
                    cls.getConstructor(resultClass).newInstance(failedResult)
                }
            } catch (th: Throwable) {
                th.toString()
                null
            }
        }

        /** Was `e`. Prefix prepended to encoded database names. */
        @JvmStatic
        fun databaseNamePrefix(): String {
            val pkg = VClientImpl.get().getCurrentPackage() ?: ""
            return "__va2_u" + VUserHandle.getVUid() + "_p" + hashPackageName(pkg) + "_d"
        }

        /** Was `f`. Walks the class hierarchy looking for a declared field. */
        @JvmStatic
        fun findField(cls: Class<*>?, name: String): Field? {
            var c = cls
            while (c != null && c != Any::class.java) {
                try {
                    return c.getDeclaredField(name)
                } catch (_: NoSuchFieldException) {
                    c = c.superclass
                }
            }
            return null
        }

        /** Was `g`. Decodes a database name if it starts with [databaseNamePrefix]. */
        @JvmStatic
        fun decodeDatabaseName(str: String?): String? {
            val prefix = databaseNamePrefix()
            if (str == null || !str.startsWith(prefix)) return str
            return try {
                val decoded = Base64.decode(str.substring(prefix.length), 11)
                String(decoded, StandardCharsets.UTF_8)
            } catch (th: Throwable) {
                th.toString()
                str
            }
        }

        /** Was `h`. Global search methods. */
        @JvmStatic
        fun isGlobalMethod(name: String): Boolean =
            "globalQuery" == name || "globalSearch" == name

        /** Was `i`. Observer registration methods. */
        @JvmStatic
        fun isObserverMethod(name: String): Boolean =
            "registerObserverCallback" == name || "unregisterObserverCallback" == name

        /**
         * Was `j`. Returns the argument index of the caller package-name argument,
         * or -1 for methods that don't take one. [sdk33Plus] selects the 2-vs-1
         * index for getSchema/getDocuments/reportUsage.
         */
        @JvmStatic
        fun packageArgIndex(name: String, sdk33Plus: Boolean): Int {
            if (isGlobalMethod(name) ||
                "initialize" == name ||
                "persistToDisk" == name ||
                "getNextPage" == name ||
                "invalidateNextPageToken" == name ||
                name.contains("Observer")
            ) {
                return -1
            }
            if (sdk33Plus && (
                        "getSchema" == name ||
                                "getDocuments" == name ||
                                "reportUsage" == name
                        )
            ) {
                return 2
            }
            return 1
        }

        /**
         * Was `k`. Hex-encodes the first 12 bytes of the SHA-256 of [str]. Falls
         * back to the hash code as hex.
         */
        @JvmStatic
        fun hashPackageName(str: String): String {
            return try {
                val digest = MessageDigest.getInstance("SHA-256")
                    .digest(str.toByteArray(StandardCharsets.UTF_8))
                val sb = StringBuilder(24)
                for (i in 0 until 12) {
                    sb.append(Character.forDigit((digest[i].toInt() ushr 4) and 0xF, 16))
                    sb.append(Character.forDigit(digest[i].toInt() and 0xF, 16))
                }
                sb.toString()
            } catch (_: Throwable) {
                Integer.toHexString(str.hashCode())
            }
        }

        /**
         * Was `l`. Sets the first Bundle arg's `packageName` to
         * [blockedAuthority]. Returns true if a Bundle was found and updated.
         */
        @JvmStatic
        fun rewriteGlobalSearchSpec(args: Array<Any?>?): Boolean {
            if (args == null) return false
            for (arg in args) {
                if (arg !is Bundle) continue
                return try {
                    val blocked = ArrayList<String>(1)
                    blocked.add(blockedAuthority())
                    arg.putStringArrayList("packageName", blocked)
                    true
                } catch (th: Throwable) {
                    th.toString()
                    false
                }
            }
            return false
        }

        /**
         * Was `m`. For getSchema/getDocuments/reportUsage methods, rewrites the
         * target package-name arg (index 1) to host. Returns true if the caller's
         * package differed from the target package.
         */
        @JvmStatic
        fun rewriteRequestPackage(
            name: String,
            currentPkg: String,
            args: Array<Any?>,
        ): Boolean {
            if (name != "getSchema" && name != "getDocuments" && name != "reportUsage") {
                return false
            }
            if (args.size < 2) return false
            val pkgArg = args[1]
            if (pkgArg !is String) return false
            val crossPkg = currentPkg != pkgArg
            args[1] = VirtualCore.gCore.hostPackageName
            return crossPkg
        }

        /**
         * Was `n`. Rewrites a single search result's `mPackageName` and
         * `mDatabaseName`, and recurses into `getJoinedResults()` if present.
         */
        @JvmStatic
        @Throws(Exception::class)
        fun rewriteSearchResult(obj: Any) {
            val pkgField = findField(obj.javaClass, "mPackageName")
            val dbField = findField(obj.javaClass, "mDatabaseName")

            if (pkgField != null) {
                pkgField.isAccessible = true
                pkgField.set(obj, VClientImpl.get().getCurrentPackage())
            }
            if (dbField != null) {
                dbField.isAccessible = true
                dbField.set(obj, decodeDatabaseName(dbField.get(obj) as String?))
            }

            try {
                val joined = obj.javaClass.getMethod("getJoinedResults").invoke(obj) as List<*>
                for (result in joined) {
                    if (result != null) rewriteSearchResult(result)
                }
            } catch (_: NoSuchMethodException) {
                // No joined results to recurse into
            }
        }

        /**
         * Was `o`. If [obj] is a Bundle, iterates its `results` array and
         * rewrites each entry. If it's a SearchResultPage, iterates `getResults()`
         * and calls [rewriteSearchResult] on each.
         */
        @JvmStatic
        @Throws(Exception::class)
        fun rewriteSearchResultPage(obj: Any?) {
            if (obj == null) return
            if (obj is Bundle) {
                val results = obj.getParcelableArrayList<Bundle>("results")
                if (results != null) {
                    for (bundle in results) {
                        bundle.putString("packageName", VClientImpl.get().getCurrentPackage())
                        bundle.putString(
                            "databaseName",
                            decodeDatabaseName(bundle.getString("databaseName")),
                        )
                    }
                }
                return
            }
            if ("android.app.appsearch.SearchResultPage" != obj.javaClass.name) return
            val results = obj.javaClass.getMethod("getResults").invoke(obj) as List<*>
            for (result in results) {
                if (result != null) rewriteSearchResult(result)
            }
        }

        /** Was `p`. Encodes a database name (URL-safe base64) with prefix. */
        @JvmStatic
        fun encodeDatabaseName(str: String): String {
            val encoded = Base64.encodeToString(str.toByteArray(StandardCharsets.UTF_8), 11)
            return databaseNamePrefix() + encoded
        }

        private fun appSearchManagerStubClass(): Class<*>? = try {
            Class.forName("android.app.appsearch.aidl.IAppSearchManager\$Stub")
        } catch (_: Throwable) {
            null
        }
    }
}
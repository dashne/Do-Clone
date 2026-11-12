package com.ims134.doclone.io


import android.content.ContentValues
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IInterface
import android.os.ParcelFileDescriptor
import android.util.Log
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.VirtualCore
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.security.AccessControlException

/**
 * Hook of get content provider's IContentProvider (was io.g46): every call of the app's provider client goes through
 * [invoke], which sorts it by method name (the argument positions depend on the Android version) and calls the
 * matching open method below, which get provider specific subclass overrides. By default each one just makes the call.
 *
 * Renamed methods: bulkInsert is [redirect] (your name), `i` is [openAssetFile], `k` is [openTypedAssetFile].
 */
open class ContentProviderHook(
    /** was b: the real IContentProvider */
    @JvmField val target: Any?,
    /** was c: the authority this hook is for */
    @JvmField val authority: String?,
) : InvocationHandler {

    /** was get: the call comes from another app (the hook was made with isSystem = false) */
    @JvmField
    var serviceExternalCaller = false

    /** was d: [target] lives in this process */
    @JvmField
    val isLocalBinder: Boolean = (if (target is IInterface) target.asBinder() else null) is Binder

    /** was g46$f */
    fun interface IContentProviderHook {
        /** was get */
        fun getProviderHook(isSystem: Boolean, provider: IInterface?): ContentProviderHook
    }

    /** was g46$get */
    class SettingProvider : IContentProviderHook {
        override fun getProviderHook(isSystem: Boolean, provider: IInterface?): ContentProviderHook =
            SettingsProviderHook(provider, "settings")
    }

    /** was g46$b */
    class DownloadProvider : IContentProviderHook {
        override fun getProviderHook(isSystem: Boolean, provider: IInterface?): ContentProviderHook =
            DownloadProviderHook(provider, "downloads")
    }

    /** was g46$c */
    class MediaProvider : IContentProviderHook {
        override fun getProviderHook(isSystem: Boolean, provider: IInterface?): ContentProviderHook =
            MediaProviderHook(provider, "media")
    }

    /** was g46$d */
    class BadgeProvider : IContentProviderHook {
        override fun getProviderHook(isSystem: Boolean, provider: IInterface?): ContentProviderHook =
            BadgeProviderHook(provider, "com.android.badge")
    }

    /** was g46$e */
    class LauncherProvider : IContentProviderHook {
        override fun getProviderHook(isSystem: Boolean, provider: IInterface?): ContentProviderHook =
            BadgeProviderHook(provider, "com.huawei.android.launcher.settings")
    }

    /** was get: bulkInsert */
    @Throws(InvocationTargetException::class)
    open fun redirect(context: ReflectiveCallContext, uri: Uri?, values: Array<ContentValues>?): Int =
        context.execute() as Int

    /** was b */
    @Throws(InvocationTargetException::class)
    open fun call(context: ReflectiveCallContext, method: String?, arg: String?, extras: Bundle?): Bundle? =
        context.execute() as Bundle?

    /** was d: delete(uri, extras), Android 11+ */
    @Throws(InvocationTargetException::class)
    open fun delete(context: ReflectiveCallContext, uri: Uri?, extras: Bundle?): Int =
        context.execute() as Int

    /** was e: delete(uri, selection, selectionArgs) */
    @Throws(InvocationTargetException::class)
    open fun delete2(context: ReflectiveCallContext, uri: Uri?, selection: String?, selectionArgs: Array<String>?): Int =
        context.execute() as Int

    /**
     * was g: swaps the AttributionSource argument (Android 12+) for one that names the right uid and package: the
     * host's, or the virtual app's when the call is in-process. True if there was such an argument.
     */
    fun fixAttributionSource(args: Array<Any?>?): Boolean {
        if (args != null && AttributionSourceMirror.TYPE != null) {
            for (i in args.indices) {
                var arg = args[i]
                if (arg != null && AttributionSourceMirror.TYPE!!.isInstance(arg)) {
                    try {
                        val external = serviceExternalCaller
                        val local = isLocalBinder
                        val useHost = !external || (Build.VERSION.SDK_INT >= 29 && !local)
                        val uid = if (useHost) VirtualCore.gCore.uid else if (local) VClientImpl.get().getBaseVUid() else VClientImpl.get().getVUid()
                        val isGservices = external && "com.google.android.gsf.gservices" == authority
                        val packageName = if (!useHost || isGservices) VClientImpl.get().getCurrentPackage() else VirtualCore.gCore.hostPackageName
                        val copy = AttributionSourceCompat.withPackage(uid, packageName, arg)
                        val state = AttributionSourceMirror.mAttributionSourceState!!.get(copy)
                        if (state != null) {
                            AttributionSourceState.uid!!.set(state, uid)
                            AttributionSourceState.packageName!!.set(state, packageName)
                        }
                        arg = copy
                    } catch (_: Throwable) {
                    }
                    args[i] = arg
                    return true
                }
            }
        }
        return false
    }

    /** was h */
    @Throws(InvocationTargetException::class)
    open fun insert(context: ReflectiveCallContext, uri: Uri?, values: ContentValues?): Uri? =
        context.execute() as Uri?

    /** was i */
    @Throws(InvocationTargetException::class)
    open fun openAssetFile(context: ReflectiveCallContext, uri: Uri?): AssetFileDescriptor? =
        context.execute() as AssetFileDescriptor?

    /** was j */
    @Throws(InvocationTargetException::class)
    open fun openFile(context: ReflectiveCallContext, uri: Uri?): ParcelFileDescriptor? =
        context.execute() as ParcelFileDescriptor?

    /** was k */
    @Throws(IllegalAccessException::class, InvocationTargetException::class)
    open fun openTypedAssetFile(context: ReflectiveCallContext, args: Array<Any?>?): AssetFileDescriptor? =
        context.execute() as AssetFileDescriptor?

    /** was m: [selectionArgs] comes from the query bundle on Android 8+ */
    @Throws(InvocationTargetException::class)
    open fun query(context: ReflectiveCallContext, uri: Uri?, projection: Array<String>?, selectionArgs: Array<String>?, queryArgs: Bundle?): Cursor? =
        context.execute() as Cursor?

    /** was n: update(uri, values, extras), Android 11+ */
    @Throws(InvocationTargetException::class)
    open fun update(context: ReflectiveCallContext, uri: Uri?, values: ContentValues?, extras: Bundle?): Int =
        context.execute() as Int

    /** was o: update(uri, values, selection, selectionArgs) */
    @Throws(InvocationTargetException::class)
    open fun update2(context: ReflectiveCallContext, uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int =
        context.execute() as Int

    /** was l: get subclass may change [args] before the call */
    open fun preprocessArguments(method: Method, args: Array<Any?>?) {}

    @Suppress("UNCHECKED_CAST")
    @Throws(Throwable::class)
    final override fun invoke(proxy: Any?, method: Method, args: Array<Any?>?): Any? {
        try {
            preprocessArguments(method, args)
        } catch (_: Throwable) {
        }
        val context = ReflectiveCallContext(target, method, args)
        // what get SecurityException gives back; only update has one (0)
        var onSecurityFailure: Int? = null
        try {
            val name = method.name
            val argv = args!!
            if ("call" == name) {
                // the (method, arg, extras) arguments start at 1 on Android 10 and below, after the extra leading
                // arguments on newer versions
                var offset = 0
                if (BuildCompat.sdk30OrLater()) {
                    offset = if (BuildCompat.sdk31OrLater()) 1 else 2
                }
                return call(context, argv[1 + offset] as String?, argv[2 + offset] as String?, argv[3 + offset] as Bundle?)
            }
            if ("insert" == name) {
                val uriIndex = indexOfParam(method, Uri::class.java, 0)
                return insert(context, argv[uriIndex] as Uri?, argv[indexOfParam(method, ContentValues::class.java, uriIndex + 1)] as ContentValues?)
            }
            if ("getType" == name) {
                return context.execute() as String?
            }
            if ("getTypeAsync" == name) {
                context.execute()
                return null
            }
            if ("delete" == name) {
                val uriIndex = indexOfParam(method, Uri::class.java, 0)
                val uri = argv[uriIndex] as Uri?
                if (BuildCompat.sdk30OrLater()) {
                    return delete(context, uri, argv[indexOfParam(method, Bundle::class.java, uriIndex + 1)] as Bundle?)
                }
                val selectionIndex = indexOfParam(method, String::class.java, uriIndex + 1)
                return delete2(
                    context, uri, argv[selectionIndex] as String?,
                    argv[indexOfParam(method, Array<String>::class.java, selectionIndex + 1)] as Array<String>?
                )
            }
            if ("bulkInsert" == name) {
                val uriIndex = indexOfParam(method, Uri::class.java, 0)
                return redirect(
                    context, argv[uriIndex] as Uri?,
                    argv[indexOfParam(method, Array<ContentValues>::class.java, uriIndex + 1)] as Array<ContentValues>?
                )
            }
            if ("update" == name) {
                onSecurityFailure = 0
                val uriIndex = indexOfParam(method, Uri::class.java, 0)
                val uri = argv[uriIndex] as Uri?
                val valuesIndex = indexOfParam(method, ContentValues::class.java, uriIndex + 1)
                val values = argv[valuesIndex] as ContentValues?
                if (BuildCompat.sdk30OrLater()) {
                    return update(context, uri, values, argv[indexOfParam(method, Bundle::class.java, valuesIndex + 1)] as Bundle?)
                }
                val selectionIndex = indexOfParam(method, String::class.java, valuesIndex + 1)
                return update2(
                    context, uri, values, argv[selectionIndex] as String?,
                    argv[indexOfParam(method, Array<String>::class.java, selectionIndex + 1)] as Array<String>?
                )
            }
            if ("openFile" == name) {
                return openFile(context, argv[indexOfParam(method, Uri::class.java, 0)] as Uri?)
            }
            if ("openAssetFile" == name) {
                return openAssetFile(context, argv[indexOfParam(method, Uri::class.java, 0)] as Uri?)
            }
            if ("openTypedAssetFile" == name) {
                return openTypedAssetFile(context, args)
            }
            if ("query" != name) { // was ay6.b
                return context.execute()
            }
            val uriIndex = indexOfParam(method, Uri::class.java, 0)
            val uri = argv[uriIndex] as Uri?
            val projectionIndex = indexOfParam(method, Array<String>::class.java, uriIndex + 1)
            val projection = argv[projectionIndex] as Array<String>?
            val selectionArgs: Array<String>?
            val queryArgs: Bundle?
            if (Build.VERSION.SDK_INT >= 26) {
                queryArgs = argv[indexOfParam(method, Bundle::class.java, projectionIndex + 1)] as Bundle?
                selectionArgs = queryArgs?.getStringArray("android:query-arg-sql-selection-args")
            } else {
                val selectionIndex = indexOfParam(method, String::class.java, projectionIndex + 1)
                selectionArgs = argv[indexOfParam(method, Array<String>::class.java, selectionIndex + 1)] as Array<String>?
                queryArgs = null
            }
            return query(context, uri, projection, selectionArgs, queryArgs)
        } catch (t: Throwable) {
            Log.d("ProviderHook", t.toString())
            val cause = if (t is InvocationTargetException && t.cause != null) t.cause!! else t
            if (cause is SecurityException || cause is AccessControlException) {
                return onSecurityFailure
            }
            throw cause
        }
    }

    companion object {
        /** was e: hook maker per authority; any other authority gets get [GenericProviderHook] */
        @JvmField
        val HOOKS_BY_PROVIDER = HashMap<String, IContentProviderHook>()

        init {
            HOOKS_BY_PROVIDER["settings"] = SettingProvider()
            HOOKS_BY_PROVIDER["downloads"] = DownloadProvider()
            HOOKS_BY_PROVIDER["media"] = MediaProvider()
            HOOKS_BY_PROVIDER["com.android.badge"] = BadgeProvider()
            HOOKS_BY_PROVIDER["com.huawei.android.launcher.settings"] = LauncherProvider()
        }

        /** was c: [provider] wrapped in get proxy of the hook for [authority]; unchanged if it already is one */
        @JvmStatic
        fun wrap(isSystem: Boolean, authority: String?, provider: IInterface?): IInterface? {
            if (provider !is Proxy || Proxy.getInvocationHandler(provider) !is ContentProviderHook) {
                val maker: IContentProviderHook = HOOKS_BY_PROVIDER[authority] ?: GenericProviderHook(authority)
                val hook = maker.getProviderHook(isSystem, provider)
                hook.serviceExternalCaller = !isSystem
                val proxy = if (provider != null) {
                    Proxy.newProxyInstance(provider.javaClass.classLoader, arrayOf(IContentProviderMirror.TYPE!!), hook) as IInterface
                } else {
                    null
                }
                if (proxy != null) return proxy
            }
            return provider
        }

        /** was f: index of the first parameter of [method] at or after [from] that [type] can be assigned to */
        @JvmStatic
        fun indexOfParam(method: Method, type: Class<*>, from: Int): Int {
            val parameterTypes = method.parameterTypes
            for (i in maxOf(0, from) until parameterTypes.size) {
                if (type.isAssignableFrom(parameterTypes[i])) return i
            }
            throw IllegalArgumentException(method.name + " has no " + type.name + " parameter at or after " + from)
        }
    }
}
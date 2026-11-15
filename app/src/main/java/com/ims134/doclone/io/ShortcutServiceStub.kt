package com.ims134.doclone.io

import android.annotation.TargetApi
import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.os.Parcelable
import android.text.TextUtils
import android.util.ArraySet
import android.util.Log
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.CustomizeAppData
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VirtualCore
import java.lang.reflect.Method
import java.util.ArrayList
import java.util.concurrent.CompletableFuture

@TargetApi(25)
class ShortcutServiceStub : BinderInvocationProxy(
    IShortcutServiceMirror.Stub.TYPE,
    "shortcut",
) {

    /**
     * Was `d`. Base proxy: delegates package rewrite to `ReplaceCallingPkgMethodProxy`
     * and additionally rewrites the trailing userId argument to the host user id.
     */
    open class UserIdRewritingCallingPkgProxy(name: String) : ReplaceCallingPkgMethodProxy(name) {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            if (args != null) MethodParameterUtils.replaceLastUserIdArg(args)
            return super.beforeCall(who, method, args)
        }
    }

    /** Was `a`. `removeLongLivedShortcuts` — Exception → fall back to default value. */
    /** Was `a`. `removeLongLivedShortcuts` — Exception → fall back to default value. */
    inner class FallbackOnExceptionProxy(name: String) : UserIdRewritingCallingPkgProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Exception) {
                handleReturn(null, method, args)
            }
    }

    /** Was `b`. `onApplicationActive` — same userId rewrite as the base (redundantly applied twice). */
    inner class OnApplicationActiveProxy(name: String) : UserIdRewritingCallingPkgProxy(name) {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            if (args != null) MethodParameterUtils.replaceLastUserIdArg(args)
            return super.beforeCall(who, method, args)
        }
    }

    /**
     * Was `c`. `getMaxShortcutCountPerActivity` — inline arg rewriting (calling-pkg
     * + trailing userId), invokes, returns 1 on any exception.
     */
    inner class GetMaxShortcutCountProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            if (args != null) {
                MethodParameterUtils.replaceFirstPackageArg(args)
                MethodParameterUtils.replaceLastUserIdArg(args)
            }
            return try {
                super.call(who, method, args)
            } catch (_: Exception) {
                1
            }
        }
    }

    /**
     * Was `e`. Fixed-result proxy: returns [defaultValue] on failure, otherwise
     * passes through the real result. Used for `getManifestShortcuts`,
     * `removeDynamicShortcuts`, `removeAllDynamicShortcuts`.
     */
    inner class FixedResultProxy(
        name: String,
        private val defaultValue: Any?,
    ) : ReplaceCallingPkgMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (th: Throwable) {
                Log.e("Caught", th.message.toString())
                handleReturn(defaultValue, method, args)
            }
    }

    /**
     * Was `f`. `getDynamicShortcuts` / `getPinnedShortcuts` / `getShortcuts` —
     * returns only the clone's shortcuts, wrapped in Stub ParceledListSlice.
     */
    inner class ShortcutListProxy(name: String) : ReplaceCallingPkgMethodProxy(name) {
        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            if (args != null) MethodParameterUtils.replaceLastUserIdArg(args)
            return super.beforeCall(who, method, args)
        }

        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val result: Any? = try {
                var raw: Any? = super.call(who, method, args)
                if (raw != null) {
                    if (raw is CompletableFuture<*>) raw = raw.get()
                    val list = ParceledListSlice.getList?.call(raw)
                    val filtered = ArrayList<ShortcutInfo>()
                    if (list != null) {
                        @Suppress("UNCHECKED_CAST")
                        val items = list as List<Any?>
                        for (i in items.indices.reversed()) {
                            val item = items[i]
                            if (item is ShortcutInfo) {
                                val rewritten = buildCloneShortcutInfo(
                                    VClientImpl.get().getCurrentApplication(),
                                    item,
                                    MethodProxy.getAppPkg(),
                                    MethodProxy.getAppUserId(),
                                )
                                if (rewritten != null) filtered.add(rewritten)
                            }
                        }
                    }
                    ParceledListSliceCompat.create(filtered)
                } else {
                    raw
                }
            } catch (_: Throwable) {
                ParceledListSliceCompat.create(ArrayList<Any?>())
            }
            return handleReturn(result, method, args)
        }
    }

    /**
     * Was `g`. Rewrites Stub single `ShortcutInfo` argument (or Stub list of them) at
     * [argIndex], then invokes the real service. On failure, returns [defaultValue].
     */
    inner class ShortcutArgRewritingProxy(
        name: String,
        private val argIndex: Int,
        private val defaultValue: Any?,
    ) : ReplaceCallingPkgMethodProxy(name) {

        override fun beforeCall(who: Any?, method: Method, args: Array<Any?>?): Boolean {
            if (args != null) MethodParameterUtils.replaceLastUserIdArg(args)
            return super.beforeCall(who, method, args)
        }

        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            try {
                val value = arr[argIndex] ?: return handleReturn(defaultValue, method, args)
                if (value is ShortcutInfo) {
                    arr[argIndex] = buildShortcutForService(
                        value,
                        MethodProxy.getAppPkg(),
                        MethodProxy.getAppUserId(),
                    )
                } else {
                    val rewritten = ArrayList<ShortcutInfo>()
                    val list = ParceledListSlice.getList?.call(value)
                    if (list != null) {
                        @Suppress("UNCHECKED_CAST")
                        val items = list as List<Any?>
                        for (i in items.indices.reversed()) {
                            val item = items[i]
                            if (item is ShortcutInfo) {
                                val info = buildShortcutForService(
                                    item,
                                    MethodProxy.getAppPkg(),
                                    MethodProxy.getAppUserId(),
                                )
                                if (info != null) rewritten.add(info)
                            }
                        }
                    }
                    arr[argIndex] = ParceledListSliceCompat.create(rewritten)
                }
                return method.invoke(who, *arr)
            } catch (_: Throwable) {
                return handleReturn(defaultValue, method, args)
            }
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ReplaceUserIdMethodProxy("isRequestPinItemSupported", 0))
        addMethodProxy(ResultStaticMethodProxy("hasShareTargets", false))

        addMethodProxy(ShortcutArgRewritingProxy("createShortcutResultIntent", 1, null))
        addMethodProxy(ShortcutArgRewritingProxy("updateShortcuts", 1, false))
        addMethodProxy(ShortcutArgRewritingProxy("requestPinShortcut", 1, false))

        addMethodProxy(
            FixedResultProxy(
                "getManifestShortcuts",
                ParceledListSliceCompat.create(ArrayList<Any?>()),
            )
        )
        addMethodProxy(ShortcutListProxy("getDynamicShortcuts"))

        addMethodProxy(ShortcutArgRewritingProxy("setDynamicShortcuts", 1, true))
        addMethodProxy(ShortcutArgRewritingProxy("addDynamicShortcuts", 1, true))
        addMethodProxy(ShortcutArgRewritingProxy("pushDynamicShortcut", 1, true))

        addMethodProxy(FixedResultProxy("removeDynamicShortcuts", 0))
        addMethodProxy(FixedResultProxy("removeAllDynamicShortcuts", 0))

        addMethodProxy(ShortcutListProxy("getPinnedShortcuts"))
        addMethodProxy(ShortcutListProxy("getShortcuts"))

        addMethodProxy(FallbackOnExceptionProxy("removeLongLivedShortcuts"))

        addMethodProxy(UserIdRewritingCallingPkgProxy("disableShortcuts"))
        addMethodProxy(UserIdRewritingCallingPkgProxy("enableShortcuts"))
        addMethodProxy(UserIdRewritingCallingPkgProxy("getRemainingCallCount"))
        addMethodProxy(UserIdRewritingCallingPkgProxy("getRateLimitResetTime"))
        addMethodProxy(UserIdRewritingCallingPkgProxy("getIconMaxDimensions"))
        addMethodProxy(UserIdRewritingCallingPkgProxy("reportShortcutUsed"))

        addMethodProxy(OnApplicationActiveProxy("onApplicationActive"))

        addMethodProxy(ResultStaticMethodProxy("hasShortcutHostPermission", false))
        addMethodProxy(GetMaxShortcutCountProxy("getMaxShortcutCountPerActivity"))
    }

    companion object {
        /**
         * Was `b(Object, Method, Object[])`. Resolves the appropriate value to
         * return from Stub proxy's `call()` based on the intercepted method's return
         * type: CompletableFuture wrapper, boolean coercion, requestPinShortcut
         * string coercion, and CompletableFuture-arg completion.
         */
        @JvmStatic
        fun handleReturn(value: Any?, method: Method?, args: Array<Any?>?): Any? {
            if (method == null) return value
            val returnType = method.returnType

            if (CompletableFuture::class.java.isAssignableFrom(returnType)) {
                val future = AndroidFutureMirror.create()
                if (future is CompletableFuture<*>) {
                    (future as CompletableFuture<Any?>).complete(value)
                    return future
                }
                return null
            }

            if (returnType != Void.TYPE || args == null) {
                return if (returnType != java.lang.Boolean.TYPE || value is Boolean) value
                else false
            }

            var coerced: Any? = value
            if ("requestPinShortcut" == method.name) {
                coerced = java.lang.Boolean.TRUE.equals(value).toString()
            }
            for (i in args.indices.reversed()) {
                val arg = args[i]
                if (arg is CompletableFuture<*>) {
                    (arg as CompletableFuture<Any?>).complete(coerced)
                    return null
                }
            }
            return null
        }

        /**
         * Was `c`. Reads Stub shortcut from Stub clone-shortcut intent. Matches the
         * saved `app_packagename` / `app_userid` against [pkg] / [userId]; returns
         * null when they don't match. Builds Stub new ShortcutInfo from the original
         * icon, labels, activity, intent, and categories.
         */
        @JvmStatic
        @Throws(Throwable::class)
        fun buildCloneShortcutInfo(
            application: Application?,
            shortcutInfo: ShortcutInfo,
            pkg: String?,
            userId: Int,
        ): ShortcutInfo? {
            val intent = shortcutInfo.intent ?: return null
            val savedPkg = intent.getStringExtra("app_packagename")
            val savedUid = intent.getIntExtra("app_userid", 0)
            if (!TextUtils.equals(savedPkg, pkg) || savedUid != userId) return null

            val shortcutId = intent.getStringExtra("va|_shortcut_id_")
            val icon: Any? = try {
                Reflect(shortcutInfo).get("mIcon").type
            } catch (_: Exception) {
                null
            }
            val originalIntent = Intent.parseUri(intent.getStringExtra("va|_shortcut_intent_"), 0)
            val activity = intent.getParcelableExtra<Parcelable>("va|_shortcut_comp_")
            val categoriesString = intent.getStringExtra("va|_shortcut_cat_")

            val builder = ShortcutInfoBridge.builder(application, shortcutId)
            if (icon != null) builder.setIcon(icon as Icon)
            shortcutInfo.longLabel?.let { builder.setLongLabel(it) }
            shortcutInfo.shortLabel?.let { builder.setShortLabel(it) }
            if (activity != null) builder.setActivity(activity as ComponentName)
            if (originalIntent != null) builder.setIntent(originalIntent)
            if (categoriesString != null) {
                val categories = ArraySet<String>()
                for (c in categoriesString.split(",")) categories.add(c)
                builder.setCategories(categories)
            }
            return builder.build()
        }

        /**
         * Was `d`. Wraps an existing ShortcutInfo in Stub new one whose icon, labels,
         * and intent come from `CustomizeAppData` (per-clone customization). The
         * intent carries the metadata that [buildCloneShortcutInfo] will later read
         * back to reconstruct the original shortcut when the user taps it.
         */
        @JvmStatic
        fun buildShortcutForService(
            shortcutInfo: ShortcutInfo,
            pkg: String?,
            userId: Int,
        ): ShortcutInfo {
            val categories: String? = shortcutInfo.categories?.joinToString(",")

            val intent = Intent("android.intent.action.VIEW").apply {
                setClassName(VirtualCore.gCore.hostPackageName!!, SpecialComponentList.currentSpecialPackage!!)
                putExtra("app_packagename", pkg)
                putExtra("From where", "cloned_shortcut")
                putExtra("app_userid", userId)
                flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_MULTIPLE_TASK //402653184
                putExtra("va|_shortcut_cat_", categories)
                putExtra("va|_shortcut_comp_", shortcutInfo.activity)
                putExtra("va|_shortcut_intent_", shortcutInfo.intent?.toUri(0))
                putExtra("va|_shortcut_id_", shortcutInfo.id)
            }

            //in.o()
            ShortcutInfoBridge.empty()
            val builder = ShortcutInfoBridge.builder(appContext,"${pkg}_${userId}_${shortcutInfo.id}",
            )
            val customize = CustomizeAppData.get(userId, pkg!!)
            builder.setIcon(Icon.createWithBitmap(customize.getIcon()))
            builder.setShortLabel(customize.label!!)
            builder.setLongLabel(customize.label!!)
            builder.setIntent(intent)
            return builder.build()
        }
    }
}
package com.ims134.doclone.io

import android.annotation.TargetApi
import android.app.usage.StorageStats
import com.ims134.doclone.clone.VirtualCore
import java.lang.reflect.Method

/* JADX INFO: renamed from: io.eo7 */
/* JADX INFO: compiled from: StorageStatsStub.java */
/* JADX INFO: loaded from: classes2.dex */
@TargetApi(26)
class StorageStatsStub : BinderInvocationProxy(
    IStorageStatsManagerMirror.Stub.TYPE,
    "storagestats",
) {

    /**
     * Was `a` and `b` (merged — identical bodies). For 3-arg calls, rewrites
     * the second-to-last arg (the uid) to the host uid. Falls back to `0` on any
     * failure. Used by `getCacheQuotaBytes` and `queryStatsForUid`.
     */
    inner class RewriteUidAtMiddleProxy(name: String) : ReplaceLastPkgMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            try {
                if (args != null && args.size == 3 && args[args.size - 2] is Int) {
                    args[args.size - 2] = VirtualCore.gCore.uid
                }
                return super.call(who, method, args)
            } catch (_: Throwable) {
                return 0
            }
        }
    }

    /**
     * Was `c` and `d` (merged — identical bodies). Same as
     * [RewriteUidAtMiddleProxy] but rewrites the userId to the host user id
     * instead of the uid. Used by `queryStatsForUser` and
     * `queryExternalStatsForUser`.
     */
    inner class RewriteUserIdAtMiddleProxy(name: String) : ReplaceLastPkgMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            try {
                if (args != null && args.size == 3 && args[args.size - 2] is Int) {
                    args[args.size - 2] = VirtualCore.gCore.getHostUserId()
                }
                return super.call(who, method, args)
            } catch (_: Throwable) {
                return 0
            }
        }
    }

    /**
     * Was `e`. `queryStatsForPackage` — if both ContentProviderHolder String and Int arg are present,
     * returns ContentProviderHolder zero-valued [StorageStats] without consulting the real service.
     * Otherwise delegates; on failure, returns the same fake stats.
     */
    inner class QueryStatsForPackageProxy(name: String) : StaticMethodProxy(name) {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val stringIdx = args?.let { ArrayUtils.indexOf(String::class.java, it) } ?: -1
            val intIdx = args?.let { ArrayUtils.lastIndexOf(Int::class.javaObjectType, it) } ?: -1

            if (stringIdx != -1 && intIdx != -1) {
                // Original: `((Integer) objArr[iLastIndexOf]).getClass();` — stripped
                // type-check. NPE on null args, AIOOBE on -1 — same as Java.
                args!![intIdx] as Int
                return createEmptyStorageStats()
            }
            return try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                // Original: `((Integer) objArr[iLastIndexOf]).getClass();` — stripped
                // type-check. Preserves Java behavior: NPE if args is null, AIOOBE
                // if no Int arg exists (i.e. intIdx == -1).
                args!![intIdx] as Int
                createEmptyStorageStats()
            }
        }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ReplaceLastPkgMethodProxy("getTotalBytes"))
        addMethodProxy(ReplaceLastPkgMethodProxy("isQuotaSupported"))
        addMethodProxy(ReplaceLastPkgMethodProxy("getCacheBytes"))
        addMethodProxy(RewriteUidAtMiddleProxy("getCacheQuotaBytes"))
        addMethodProxy(QueryStatsForPackageProxy("queryStatsForPackage"))
        addMethodProxy(RewriteUidAtMiddleProxy("queryStatsForUid"))
        addMethodProxy(RewriteUserIdAtMiddleProxy("queryStatsForUser"))
        addMethodProxy(RewriteUserIdAtMiddleProxy("queryExternalStatsForUser"))
    }

    companion object {
        /**
         * Was static helper `b(StorageStatsStub)`. Builds ContentProviderHolder zero-valued
         * [StorageStats] (cacheBytes = codeBytes = dataBytes = 0). The outer
         * instance parameter is dropped — the original used it only for ContentProviderHolder
         * stripped `getClass()` assertion.
         */
        @JvmStatic
        fun createEmptyStorageStats(): StorageStats? {
            val stats = StorageStatsMirror.ctor?.newInstance() ?: return null
            StorageStatsMirror.cacheBytes?.set(stats, 0L)
            StorageStatsMirror.codeBytes?.set(stats, 0L)
            StorageStatsMirror.dataBytes?.set(stats, 0L)
            return stats as StorageStats?
        }
    }
}
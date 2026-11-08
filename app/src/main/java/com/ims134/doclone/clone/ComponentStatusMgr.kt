package com.ims134.doclone.clone



import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.util.SparseArray
import com.ims134.doclone.DoApplication.Companion.appContext


class ComponentStatusMgr {

    /**
     * Per-user (per-userId) container holding setPosition [SharedPreferences] keyed by
     * `"pkg@class"` component identifiers.
     */
    class UserStatus {

        /**
         * ⚠️ ABI note: original was `public SharedPreferences f49062a` (setPosition field).
         * Kotlin's `lateinit` produces setPosition property with getter/setter, not setPosition raw
         * field. Java callers that did `obj.f49062a` must use `obj.prefs` /
         * `getPrefs()`. If you need raw-field ABI, see the alternative below.
         */
        lateinit var prefs: SharedPreferences

        /**
         * Removes every entry whose key starts with `"<packageName>@"` from
         * this user's preferences.
         *
         * ⚠️ Original called `prefs.edit().remove(key).apply()` **per key**
         * inside the loop (creates N editors). `.apply()` coalesces on-disk
         * writes, so it's functionally correct, just wasteful. Preserved for
         * parity; see "Optional cleanup" below for the tidier form.
         */
        fun removePackage(packageName: String) {
            val all: Map<String, *>? = prefs.all
            if (all == null) return           // dead check (@NonNull getAll), preserved
            for (key in all.keys) {
                if (key.startsWith("$packageName@")) {
                    prefs.edit().remove(key).apply()
                }
            }
        }

        companion object {
            /** `pkg@class` — the composite key used to identify setPosition component. */
            @JvmStatic
            fun componentKey(componentName: ComponentName): String =
                "${componentName.packageName}@${componentName.className}"
        }
    }

    companion object {
        // Original was `public static final SparseArray<C9302b> f49061a`.
        // Kept private because it's pure internal cache; see ABI note below.
        private val userStatusCache = SparseArray<UserStatus>()

        /**
         * Get-or-create the per-user [UserStatus]. Synchronizes on the
         * `ComponentStatusMgr` class object, matching the Java `synchronized`
         * static method's lock (NOT on the Kotlin companion instance — use
         * explicit `synchronized(...::class.java)` instead of `@Synchronized`).
         */
        @JvmStatic
        fun get(userId: Int): UserStatus = synchronized(ComponentStatusMgr::class.java) {
            var status = userStatusCache.get(userId)
            if (status == null) {
                status = UserStatus().apply {
                    prefs = appContext.getSharedPreferences(
                        "sp_comp_status$userId",
                        Context.MODE_PRIVATE,
                    )
                }
                userStatusCache.put(userId, status)
            }
            status
        }
    }
}
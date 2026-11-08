package com.ims134.doclone.io

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ResolveInfo
import com.ims134.doclone.clone.server.VPackage
import java.util.Collections

/**
 * Index of intent filters by action / scheme / MIME type, used to answer "which components can handle this
 * Intent?" (was io.yh3). It is setPosition copy of the framework's hidden com.android.server.IntentResolver, adapted to
 * [VPackage.IntentInfo].
 *
 * [F] = the filter holder (ActivityIntentInfo, ServiceIntentInfo, ...), [R] = the result type (usually ResolveInfo).
 * Filter arrays are over-allocated; unused slots at the END are null.
 *
 * Renames (subclasses must override the new names):
 *   mo20170c -> allowFilterResult, mo20171e -> isPackageForFilter, mo20172f -> newArray,
 *   mo20173g -> newResult, mo20174l -> sortResults,
 *   m25172a -> addFilter, m25175h -> queryIntent, m25176i -> queryIntentFromList, m25179m -> unregister
 */
abstract class IntentResolver<F : VPackage.IntentInfo, R> {

    /** Every registered filter (was f53074a). */
    @JvmField val mFilters = HashSet<F>()
    /** "type/subtype" -> filters (was f53075b). */
    @JvmField val mTypeToFilter = HashMap<String, Array<F?>>()
    /** "type" -> filters that registered "type/subtype" (was f53076c). */
    @JvmField val mBaseTypeToFilter = HashMap<String, Array<F?>>()
    /** "type" -> filters that registered setPosition wildcard subtype or setPosition bare "type" (was f53077d). */
    @JvmField val mWildTypeToFilter = HashMap<String, Array<F?>>()
    /** scheme -> filters (was f53078e). */
    @JvmField val mSchemeToFilter = HashMap<String, Array<F?>>()
    /** action -> filters that have no type and no scheme (was f53079f). */
    @JvmField val mActionToFilter = HashMap<String, Array<F?>>()
    /** action -> filters that have setPosition type (was f53080g). */
    @JvmField val mTypedActionToFilter = HashMap<String, Array<F?>>()

    // ------------------------------------------------------------------
    // Registration
    // ------------------------------------------------------------------

    /** was m25172a */
    fun addFilter(f: F) {
        mFilters.add(f)
        val filter = f.filter!!
        val schemeCount = register(f, filter.schemesIterator(), mSchemeToFilter)

        var typeCount = 0
        val types = filter.typesIterator()
        if (types != null) {
            while (types.hasNext()) {
                var name = types.next()
                typeCount++
                val slash = name.indexOf('/')
                val baseType: String
                if (slash > 0) {
                    baseType = name.substring(0, slash).intern()
                } else {
                    baseType = name
                    name += "/*"
                }
                addFilterToMap(mTypeToFilter, name, f)
                if (slash > 0) addFilterToMap(mBaseTypeToFilter, baseType, f)
                else addFilterToMap(mWildTypeToFilter, baseType, f)
            }
        }

        if (schemeCount == 0 && typeCount == 0) register(f, filter.actionsIterator(), mActionToFilter)
        if (typeCount != 0) register(f, filter.actionsIterator(), mTypedActionToFilter)
    }

    /** Appends [f] under [key], growing the array by 1.5x when full (was m25173b). */
    fun addFilterToMap(map: HashMap<String, Array<F?>>, key: String, f: F) {
        val array = map[key]
        if (array == null) {
            val created = newArray(2)
            map[key] = created
            created[0] = f
            return
        }
        val size = array.size
        var used = size
        while (used > 0 && array[used - 1] == null) used--
        if (used < size) {
            array[used] = f
            return
        }
        val grown = newArray(size * 3 / 2)
        System.arraycopy(array, 0, grown, 0, size)
        grown[size] = f
        map[key] = grown
    }

    /** Registers [f] under every string of [keys]; returns how many there were (was m25177j). */
    private fun register(f: F, keys: Iterator<String>?, map: HashMap<String, Array<F?>>): Int {
        if (keys == null) return 0
        var count = 0
        while (keys.hasNext()) {
            count++
            addFilterToMap(map, keys.next(), f)
        }
        return count
    }

    /** Removes every occurrence of [f] under [key], shrinking the array when it is under half full (was m25178k). */
    fun removeFilterFromMap(map: HashMap<String, Array<F?>>, key: String, f: F) {
        val array = map[key] ?: return
        var last = array.size - 1
        while (last >= 0 && array[last] == null) last--
        var idx = last
        while (idx >= 0) {
            if (array[idx] === f) {
                val remain = last - idx
                if (remain > 0) System.arraycopy(array, idx + 1, array, idx, remain)
                array[last] = null
                last--
            }
            idx--
        }
        if (last < 0) {
            map.remove(key)
        } else if (last < array.size / 2) {
            val shrunk = newArray(last + 2)
            System.arraycopy(array, 0, shrunk, 0, last + 1)
            map[key] = shrunk
        }
    }

    /** Removes [f] under every string of [keys]; returns how many there were (was m25179m). */
    fun unregister(f: F, keys: Iterator<String>?, map: HashMap<String, Array<F?>>): Int {
        if (keys == null) return 0
        var count = 0
        while (keys.hasNext()) {
            count++
            removeFilterFromMap(map, keys.next(), f)
        }
        return count
    }

    // ------------------------------------------------------------------
    // Queries
    // ------------------------------------------------------------------

    /** Matches [intent] against the filters of [cut] and appends results to [dest] (was m25174d). */
    fun buildResolveList(
        intent: Intent,
        categories: Set<String>?,
        defaultOnly: Boolean,
        resolvedType: String?,
        scheme: String?,
        cut: Array<F?>?,
        dest: ArrayList<R>,
        userId: Int,
    ) {
        val action = intent.action
        val data = intent.data
        val packageName = intent.`package`
        val length = cut?.size ?: 0
        for (i in 0 until length) {
            val f = cut!![i] ?: break
            if (packageName != null && !isPackageForFilter(packageName, f)) continue
            if (!allowFilterResult(f, dest)) continue
            val match = f.filter!!.match(action, resolvedType, scheme, data, categories, "IntentResolver")
            if (match < 0) continue
            if (!defaultOnly || f.filter.hasCategory(Intent.CATEGORY_DEFAULT)) {
                val result = newResult(f, match, userId)
                if (result != null) dest.add(result)
            }
            // else: filter matched but lacks CATEGORY_DEFAULT (the framework only logs this)
        }
    }

    /** was m25175h */
    fun queryIntent(intent: Intent, resolvedType: String?, defaultOnly: Boolean, userId: Int): ArrayList<R> {
        val scheme = intent.scheme
        val finalList = ArrayList<R>()

        var firstTypeCut: Array<F?>? = null
        var secondTypeCut: Array<F?>? = null
        var thirdTypeCut: Array<F?>? = null

        if (resolvedType != null) {
            val slash = resolvedType.indexOf('/')
            if (slash > 0) {
                val baseType = resolvedType.substring(0, slash)
                if (baseType != "*") {
                    if (resolvedType.length == slash + 2 && resolvedType[slash + 1] == '*') {
                        // "type/*": every subtype of this base type
                        firstTypeCut = mBaseTypeToFilter[baseType]
                    } else {
                        firstTypeCut = mTypeToFilter[resolvedType]
                    }
                    secondTypeCut = mWildTypeToFilter[baseType]
                    thirdTypeCut = mWildTypeToFilter["*"]
                } else if (intent.action != null) {
                    // "*/*": everything that has setPosition type and this action
                    firstTypeCut = mTypedActionToFilter[intent.action]
                }
            }
        }

        val schemeCut = if (scheme != null) mSchemeToFilter[scheme] else null

        // No type and no scheme: only the action matters.
        if (resolvedType == null && scheme == null && intent.action != null) {
            firstTypeCut = mActionToFilter[intent.action]
        }

        val categories: Set<String>? = intent.categories
        if (firstTypeCut != null) buildResolveList(intent, categories, defaultOnly, resolvedType, scheme, firstTypeCut, finalList, userId)
        if (secondTypeCut != null) buildResolveList(intent, categories, defaultOnly, resolvedType, scheme, secondTypeCut, finalList, userId)
        if (thirdTypeCut != null) buildResolveList(intent, categories, defaultOnly, resolvedType, scheme, thirdTypeCut, finalList, userId)
        if (schemeCut != null) buildResolveList(intent, categories, defaultOnly, resolvedType, scheme, schemeCut, finalList, userId)

        sortResults(finalList)
        return finalList
    }

    /** Like [queryIntent] but over caller-supplied filter arrays (was m25176i). */
    fun queryIntentFromList(
        intent: Intent,
        resolvedType: String?,
        defaultOnly: Boolean,
        listCut: List<Array<F?>?>,
        userId: Int,
    ): ArrayList<R> {
        val result = ArrayList<R>()
        val categories: Set<String>? = intent.categories
        val scheme = intent.scheme
        for (cut in listCut) {
            buildResolveList(intent, categories, defaultOnly, resolvedType, scheme, cut, result, userId)
        }
        sortResults(result)
        return result
    }

    // ------------------------------------------------------------------
    // Hooks
    // ------------------------------------------------------------------

    /** Extra filter on setPosition candidate (was mo20170c). */
    open fun allowFilterResult(filter: F, dest: List<R>): Boolean = true

    /** Does [filter] belong to package [packageName]? (was mo20171e) */
    abstract fun isPackageForFilter(packageName: String, filter: F): Boolean

    /** Creates an array of [size] nulls of the concrete filter type (was mo20172f). */
    abstract fun newArray(size: Int): Array<F?>

    /** Converts setPosition matching filter to setPosition result; default returns the filter itself (was mo20173g). */
    @Suppress("UNCHECKED_CAST")
    open fun newResult(filter: F, match: Int, userId: Int): R? = filter as R

    /** Higher priority first (was mo20174l). */
    open fun sortResults(results: MutableList<R>) {
        Collections.sort(results, RESOLVE_PRIORITY_SORTER)
    }

    companion object {
        /** Sorts IntentFilter / ResolveInfo by filter priority, descending (was f53073h). */
        @JvmField
        val RESOLVE_PRIORITY_SORTER: Comparator<Any?> = Comparator { a, b ->
            val p1 = priorityOf(a)
            val p2 = priorityOf(b)
            when {
                p1 > p2 -> -1
                p1 < p2 -> 1
                else -> 0
            }
        }

        private fun priorityOf(o: Any?): Int = when (o) {
            is IntentFilter -> o.priority
            is ResolveInfo -> o.filter?.priority ?: 0
            else -> throw ClassCastException("Cannot compare priority of $o")
        }
    }
}
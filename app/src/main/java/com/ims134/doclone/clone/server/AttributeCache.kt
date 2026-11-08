package com.ims134.doclone.clone.server

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.content.res.TypedArray
import android.util.SparseArray
import java.util.WeakHashMap

/**
 * Cache of the TypedArrays of a package's theme attributes (was com.polestar.clone.server.am.AttributeCache),
 * used by ActivityStack.createStubIntent to read windowIsFloating and friends.
 * The attribute arrays are matched by identity (as in the original: HashMap with int[] keys).
 */
class AttributeCache(
    /** was a: the host context, used to create the package contexts */
    private val context: Context,
) {

    /** was b */
    private val packages = WeakHashMap<String, PackageEntry>()

    /** was AttributeCache$Package: one package's context and its cached arrays, by theme */
    private class PackageEntry(@JvmField val context: Context) {
        @JvmField
        val themes = SparseArray<HashMap<IntArray, Entry>>()
    }

    /** was AttributeCache$a */
    class Entry(@JvmField val array: TypedArray)

    /** was a(String, int, int[]): the styled attributes of [attrs] in [theme] of [packageName], or null */
    @Synchronized
    fun get(packageName: String, theme: Int, attrs: IntArray): Entry? {
        var packageEntry = packages[packageName]
        var themeMap: HashMap<IntArray, Entry>? = null
        if (packageEntry != null) {
            themeMap = packageEntry.themes.get(theme)
            themeMap?.get(attrs)?.let { return it }
        } else {
            val packageContext = try {
                context.createPackageContext(packageName, Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY) // INCLUDE_CODE | IGNORE_SECURITY
            } catch (_: PackageManager.NameNotFoundException) {
                return null
            } ?: return null
            packageEntry = PackageEntry(packageContext)
            packages[packageName] = packageEntry
        }
        if (themeMap == null) {
            themeMap = HashMap()
            packageEntry.themes.put(theme, themeMap)
        }
        return try {
            val entry = Entry(packageEntry.context.obtainStyledAttributes(theme, attrs))
            themeMap[attrs] = entry
            entry
        } catch (_: Resources.NotFoundException) {
            null
        }
    }

    companion object {
        /** was c: set once at startup (new AttributeCache(context)) */
        @JvmStatic
        var instance: AttributeCache? = null
    }
}
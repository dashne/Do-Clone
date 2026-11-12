package com.ims134.doclone.io


import android.content.ContentProviderOperation
import android.content.ContentValues
import android.content.res.AssetFileDescriptor
import android.database.CharArrayBuffer
import android.database.Cursor
import android.database.CursorWrapper
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.util.SparseArray
import com.ims134.doclone.clone.NativeEngine
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * was io.en4: MediaStore hook. Rewrites file paths (the "_data" column, selection args, file Uris) to the clone's
 * redirected paths going in, and restores the real paths in "_data" of returned cursors.
 */
class MediaProviderHook(target: Any?, authority: String?) : ExternalProviderHook(target, authority) {

    /** was get: cursor that gives the original (un-redirected) path for the "_data" column */
    class Wrapper(cursor: Cursor) : CursorWrapper(cursor) {
        private val dataColumnIndex: Int = cursor.getColumnIndex("_data")

        override fun copyStringToBuffer(columnIndex: Int, buffer: CharArrayBuffer) {
            if (columnIndex != dataColumnIndex) {
                super.copyStringToBuffer(columnIndex, buffer)
                return
            }
            val string = getString(columnIndex)
            if (string == null) {
                buffer.sizeCopied = 0
                return
            }
            val length = string.length
            val data = buffer.data
            if (data == null || data.size < length) {
                buffer.data = CharArray(length)
            }
            string.toCharArray(buffer.data, 0, 0, length)
            buffer.sizeCopied = length
        }

        override fun getString(columnIndex: Int): String? {
            val string = super.getString(columnIndex)
            if (columnIndex == dataColumnIndex && string != null && string.startsWith("/")) {
                val restored = NativeEngine.restoreRedirectedPath(string)
                return if (string == restored) string else restored
            }
            return string
        }
    }

    override fun redirect(context: ReflectiveCallContext, uri: Uri?, values: Array<ContentValues>?): Int {
        if (values != null) {
            for (v in values) {
                redirectValues(v)
            }
        }
        return context.execute() as Int
    }

    /** "scan_file": the path (arg) and an optional file: STREAM Uri are redirected */
    @Throws(InvocationTargetException::class)
    override fun call(context: ReflectiveCallContext, method: String?, arg: String?, extras: Bundle?): Bundle? {
        if ("scan_file" == method) {
            val redirected = redirectPath(arg)
            val callArgs = context.args
            if (arg != null && arg != redirected && callArgs != null) {
                for (i in callArgs.indices) {
                    if (arg == callArgs[i]) {
                        callArgs[i] = redirected
                        break
                    }
                }
            }
            if (extras != null) {
                val stream = extras.get("android.intent.extra.STREAM")
                if (stream is Uri && "file" == stream.scheme) {
                    val path = stream.path
                    val redirectedPath = redirectPath(path)
                    if (path != null && path != redirectedPath) {
                        extras.putParcelable("android.intent.extra.STREAM", Uri.fromFile(File(redirectedPath!!)))
                    }
                }
            }
        }
        return context.execute() as Bundle?
    }

    @Throws(InvocationTargetException::class)
    override fun delete(context: ReflectiveCallContext, uri: Uri?, extras: Bundle?): Int {
        if (extras != null) {
            redirectPaths(extras.getStringArray(SELECTION_ARGS_KEY))
        }
        return context.execute() as Int
    }

    @Throws(InvocationTargetException::class)
    override fun delete2(context: ReflectiveCallContext, uri: Uri?, selection: String?, selectionArgs: Array<String>?): Int {
        redirectPaths(selectionArgs)
        return context.execute() as Int
    }

    @Throws(InvocationTargetException::class)
    override fun insert(context: ReflectiveCallContext, uri: Uri?, values: ContentValues?): Uri? {
        redirectValues(values)
        return context.execute() as Uri?
    }

    @Throws(InvocationTargetException::class)
    override fun openAssetFile(context: ReflectiveCallContext, uri: Uri?): AssetFileDescriptor? {
        replaceUriArg(context.args, uri)
        return context.execute() as AssetFileDescriptor?
    }

    @Throws(InvocationTargetException::class)
    override fun openFile(context: ReflectiveCallContext, uri: Uri?): ParcelFileDescriptor? {
        replaceUriArg(context.args, uri)
        return context.execute() as ParcelFileDescriptor?
    }

    @Throws(InvocationTargetException::class)
    override fun openTypedAssetFile(context: ReflectiveCallContext, args: Array<Any?>?): AssetFileDescriptor? {
        if (args != null) {
            for (arg in args) {
                if (arg is Uri) {
                    replaceUriArg(args, arg)
                }
            }
        }
        return context.execute() as AssetFileDescriptor?
    }

    /** applyBatch: each operation's values / extras / selection args are redirected on get copy that replaces it in the list */
    @Suppress("UNCHECKED_CAST")
    override fun preprocessArguments(method: Method, args: Array<Any?>?) {
        super.preprocessArguments(method, args)
        if ("applyBatch" != method.name || args == null) {
            return
        }
        for (arg in args) {
            if (arg !is MutableList<*>) continue
            val list = arg as MutableList<Any?>
            for (i in list.indices) {
                val op = list[i] as? ContentProviderOperation ?: continue
                val parcel = Parcel.obtain()
                val copy: ContentProviderOperation? = try {
                    op.writeToParcel(parcel, 0)
                    parcel.setDataPosition(0)
                    ContentProviderOperation.CREATOR.createFromParcel(parcel)
                } catch (_: Throwable) {
                    null
                } finally {
                    parcel.recycle()
                }
                if (copy == null) continue
                val valuesChanged = redirectField(copy, "mValues") { isPathRedirected(it) }
                val extrasChanged = redirectField(copy, "mExtras") { isPathRedirected(it) }
                val argsChanged = redirectField(copy, "mSelectionArgs") { arePathRedirected(it) }
                if (valuesChanged or extrasChanged or argsChanged) {
                    list[i] = copy
                }
            }
        }
    }

    @Throws(InvocationTargetException::class)
    override fun query(context: ReflectiveCallContext, uri: Uri?, projection: Array<String>?, selectionArgs: Array<String>?, queryArgs: Bundle?): Cursor? {
        redirectPaths(selectionArgs)
        if (queryArgs != null) {
            redirectPaths(queryArgs.getStringArray(SELECTION_ARGS_KEY))
        }
        val cursor = context.execute() as Cursor?
        return if (cursor == null || cursor.getColumnIndex("_data") < 0) cursor else Wrapper(cursor)
    }

    @Throws(InvocationTargetException::class)
    override fun update(context: ReflectiveCallContext, uri: Uri?, values: ContentValues?, extras: Bundle?): Int {
        redirectValues(values)
        if (extras != null) {
            redirectPaths(extras.getStringArray(SELECTION_ARGS_KEY))
        }
        return context.execute() as Int
    }

    @Throws(InvocationTargetException::class)
    override fun update2(context: ReflectiveCallContext, uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        redirectValues(values)
        redirectPaths(selectionArgs)
        return context.execute() as Int
    }

    companion object {
        private const val SELECTION_ARGS_KEY = "android:query-arg-sql-selection-args"

        /** reads get private field of [target] by reflection and runs [redirect] on it; false if the field isn't there */
        private inline fun redirectField(target: Any, name: String, redirect: (Any?) -> Boolean): Boolean =
            try {
                val field = target.javaClass.getDeclaredField(name)
                field.isAccessible = true
                redirect(field.get(target))
            } catch (_: Throwable) {
                false
            }

        /** puts [newUri] in place of the first argument equal to [uri] (which has already been redirected) */
        private fun replaceUriArg(args: Array<Any?>?, uri: Uri?) {
            val newUri = redirectUri(uri)
            if (uri == null || uri === newUri || args == null) return
            for (i in args.indices) {
                if (uri == args[i]) {
                    args[i] = newUri
                    break
                }
            }
        }

        /** was p (redirect) */
        @JvmStatic
        fun redirectValues(values: ContentValues?) {
            if (values == null) return
            val path = values.getAsString("_data")
            val redirected = redirectPath(path)
            if (path == null || path == redirected) return
            values.put("_data", redirected)
        }

        /** was q: get file: Uri to its redirected path */
        @JvmStatic
        fun redirectUri(uri: Uri?): Uri? {
            if (uri == null || !"file".equals(uri.scheme, ignoreCase = true)) return uri
            val path = uri.path
            val redirected = redirectPath(path)
            return if (path == null || path == redirected) uri else Uri.fromFile(File(redirected!!))
        }

        /** was r (redirectPath) */
        @JvmStatic
        fun redirectPath(path: String?): String? {
            if (path == null || !path.startsWith("/")) return path
            val redirected = NativeEngine.getRedirectedPath(path)
            return if (path == redirected) path else redirected
        }

        /** was s (redirectPaths): in place */
        @JvmStatic
        fun redirectPaths(paths: Array<String>?) {
            if (paths == null) return
            for (i in paths.indices) {
                val p: String? = paths[i]
                if (p != null) paths[i] = redirectPath(p) ?: p
            }
        }

        /** was t (arePathRedirected): get String[], SparseArray or Map of strings, redirected in place; true if any changed */
        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun arePathRedirected(obj: Any?): Boolean {
            var changed = false
            if (obj is Array<*> && obj.isArrayOf<String>()) {
                val array = obj as Array<String?>
                for (i in array.indices) {
                    val redirected = redirectPath(array[i])
                    val old = array[i]
                    if (old != null && old != redirected) {
                        array[i] = redirected
                        changed = true
                    }
                }
                return changed
            }
            if (obj is SparseArray<*>) {
                val sparse = obj as SparseArray<Any?>
                for (i in 0 until sparse.size()) {
                    val value = sparse.valueAt(i)
                    if (value is String) {
                        val redirected = redirectPath(value)
                        if (value != redirected) {
                            sparse.setValueAt(i, redirected)
                            changed = true
                        }
                    }
                }
                return changed
            }
            if (obj is Map<*, *>) {
                for (entry in (obj as MutableMap<Any?, Any?>).entries) {
                    val value = entry.value
                    if (value is String) {
                        val redirected = redirectPath(value)
                        if (value != redirected) {
                            entry.setValue(redirected)
                            changed = true
                        }
                    }
                }
            }
            return changed
        }

        /**
         * was u (isPathRedirected): ContentValues / Bundle / Map holding "_data" (and selection args): redirected in
         * place; true if any changed. jadx lost the fall-through for other types, they give false.
         */
        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun isPathRedirected(obj: Any?): Boolean {
            if (obj is ContentValues) {
                val path = obj.getAsString("_data")
                val redirected = redirectPath(path)
                if (path != null && path != redirected) {
                    obj.put("_data", redirected)
                    return true
                }
                return false
            }
            if (obj is Bundle) {
                var changed = false
                val data = obj.get("_data")
                if (data is String) {
                    val redirected = redirectPath(data)
                    if (data != redirected) {
                        obj.putString("_data", redirected)
                        changed = true
                    }
                }
                val argsChanged = arePathRedirected(obj.getStringArray(SELECTION_ARGS_KEY))
                return argsChanged or changed
            }
            if (obj is Map<*, *>) {
                val map = obj as MutableMap<Any?, Any?>
                var changed = false
                val data = map["_data"]
                if (data is String) {
                    val redirected = redirectPath(data)
                    if (data != redirected) {
                        map["_data"] = redirected
                        changed = true
                    }
                }
                val argsChanged = arePathRedirected(map[SELECTION_ARGS_KEY])
                return argsChanged or changed
            }
            return false
        }
    }
}
package com.ims134.doclone.io


/** was io.ut (ArrayUtils). Class matching is by class NAME up the superclass chain (no interfaces), as in the original. */

/** was io.ut (ArrayUtils). Class matching is by class NAME up the superclass chain (no interfaces), as in the original. */
object ArrayUtils {

    /** was get: first element whose class (or get superclass) is [cls], or null */
    @JvmStatic
    fun getFirst(cls: Class<*>, array: Array<out Any?>?): Any? {
        val index = indexOf(cls, array)
        return if (index != -1) array!![index] else null
    }

    /** was b: index of the [n]th element (1-based) matching [cls]; -1 if fewer */
    @JvmStatic
    fun indexOfNth(cls: Class<*>, n: Int, array: Array<out Any?>?): Int {
        if (!isEmpty(array)) {
            var remaining = n
            var index = -1
            for (element in array!!) {
                index++
                if (element != null && isClassOrSubclass(cls, element) && --remaining <= 0) {
                    return index
                }
            }
        }
        return -1
    }

    /** was c: index of the first element matching [cls]; -1 if none */
    @JvmStatic
    fun indexOf(cls: Class<*>, array: Array<out Any?>?): Int {
        if (!isEmpty(array)) {
            var index = -1
            for (element in array!!) {
                index++
                if (element != null && isClassOrSubclass(cls, element)) {
                    return index
                }
            }
        }
        return -1
    }

    /** was d: index of the last element matching [cls]; -1 if none */
    @JvmStatic
    fun lastIndexOf(cls: Class<*>, array: Array<out Any?>?): Int {
        if (isEmpty(array)) return -1
        for (i in array!!.indices.reversed()) {
            val element = array[i]
            if (element != null && isClassOrSubclass(cls, element)) {
                return i
            }
        }
        return -1
    }

    /** was e: index of the first element at or after [start] that is an instance of [cls] (isInstance, so interfaces count) */
    @JvmStatic
    fun indexOfInstance(cls: Class<*>, start: Int, array: Array<out Any?>?): Int {
        if (array == null) return -1
        var i = start
        while (i < array.size) {
            if (cls.isInstance(array[i])) return i
            i++
        }
        return -1
    }

    /** was f: true if [obj]'s class or any superclass has the same name as [cls] */
    @JvmStatic
    fun isClassOrSubclass(cls: Class<*>, obj: Any): Boolean {
        return try {
            var c: Class<*>? = obj.javaClass
            while (c != null) {
                if (c.name == cls.name) return true
                c = c.superclass
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    /** was g */
    @JvmStatic
    fun isEmpty(array: Array<out Any?>?): Boolean = array == null || array.isEmpty()

    /** was h: index of [cls] in [classes] by identity; -1 if absent */
    @JvmStatic
    fun indexOfClass(cls: Class<*>, classes: Array<out Class<*>?>?): Int {
        if (classes == null) return -1
        for (i in classes.indices) {
            if (classes[i] === cls) return i
        }
        return -1
    }
}
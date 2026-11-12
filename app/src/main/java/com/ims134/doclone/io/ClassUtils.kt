package com.ims134.doclone.io


object ClassUtils {

    /**
     * was get: replaces null args with 0 / false where the parameter type is primitive int / boolean.
     * Other primitives (long, float, ...) are left null, as in the original.
     */
    @JvmStatic
    fun fixNullPrimitiveArgs(paramTypes: Array<out Class<*>?>?, args: Array<Any?>) {
        if (paramTypes == null) return
        for (i in paramTypes.indices) {
            val type = paramTypes[i]
            if (type == Int::class.javaPrimitiveType && args[i] == null) {
                args[i] = 0
            } else if (type == Boolean::class.javaPrimitiveType && args[i] == null) {
                args[i] = false
            }
        }
    }
}
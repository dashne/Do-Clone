package com.ims134.doclone.clone

object NativeEngine {
    init {
        System.loadLibrary("va-native")
    }

    @JvmStatic external fun nativeRedirect(origPath: String?, newPath: String?)
    @JvmStatic external fun nativeGetRedirectedPath(path: String?): String?
    @JvmStatic external fun nativeRestoreRedirectedPath(path: String?): String?
    @JvmStatic external fun nativeWhiteList(path: String?)

    fun restoreRedirectedPath(path: String?): String? = nativeRestoreRedirectedPath(path)

    fun getRedirectedPath(path: String?) : String? = nativeGetRedirectedPath(path)
}
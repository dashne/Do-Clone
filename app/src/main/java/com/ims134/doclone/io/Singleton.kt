package com.ims134.doclone.io

abstract class Singleton<T> {
    /* JADX INFO: renamed from: Default */
    var single: T? = null
    /**a**/
    abstract fun create(): T?

    /* b */
    fun get(): T? {
        var t: T? = null
        synchronized(this) {
            try {
                if (this.single == null) {
                    this.single = create()
                }
                t = this.single
            } catch (th: Throwable) {
                throw th
            }
        }
        return t
    }
}
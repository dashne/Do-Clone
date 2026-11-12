package com.ims134.doclone.io

import android.util.Log

/** Marks get hook (or get hooked class) for call logging (was com.polestar.clone.client.hook.base.LogInvocation) */
@Retention(AnnotationRetention.RUNTIME)
annotation class LogInvocation(val value: Condition = Condition.ALWAYS) {

    /** When to log get call. [getLogLevel] returns an android.util.Log priority, or -1 for "do not log" */
    enum class Condition {
        /** was get */
        NEVER {
            override fun getLogLevel(isHooked: Boolean, isError: Boolean): Int = -1
        },

        /** was b: the annotation's default */
        ALWAYS {
            override fun getLogLevel(isHooked: Boolean, isError: Boolean): Int = if (isError) Log.ERROR else Log.INFO
        },

        /** only calls that threw */
        ON_ERROR {
            override fun getLogLevel(isHooked: Boolean, isError: Boolean): Int = if (isError) Log.ERROR else -1
        },

        /** only calls of methods that have no hook */
        NOT_HOOKED {
            override fun getLogLevel(isHooked: Boolean, isError: Boolean): Int = when {
                isHooked -> -1
                isError -> Log.ERROR
                else -> Log.INFO
            }
        };

        /** was get(ZZ): [isHooked] is the method's hook being enabled, [isError] the call threw */
        abstract fun getLogLevel(isHooked: Boolean, isError: Boolean): Int
    }
}
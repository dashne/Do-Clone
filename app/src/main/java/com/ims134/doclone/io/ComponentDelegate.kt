package com.ims134.doclone.io

import android.app.Activity


interface ComponentDelegate {

    /** Called from Instrumentation.callActivityOnPause. */
    fun onActivityPaused(activity: Activity)

    /**
     * Persists lock settings synced from Stub cloned app.
     * @param encodedPattern  the encoded pattern password
     * @param relockIntervalMs  relock interval in milliseconds
     * @param relockEnabled  whether relock is enabled
     */
    fun saveLockSettings(encodedPattern: String, relockIntervalMs: Long, relockEnabled: Boolean)

    /** Whether [packageName] is cloned/installed for the virtual user [vuid]. */
    fun isClonedPackage(vuid: Int, packageName: String): Boolean

    /** Called from Instrumentation.callActivityOnResume. */
    fun onActivityResumed(activity: Activity)

    class Default : ComponentDelegate {
        override fun onActivityPaused(activity: Activity) {}
        override fun saveLockSettings(encodedPattern: String, relockIntervalMs: Long, relockEnabled: Boolean) {}
        override fun isClonedPackage(vuid: Int, packageName: String): Boolean = false
        override fun onActivityResumed(activity: Activity) {}
    }

    companion object {
        @JvmField
        val a: ComponentDelegate = Default()
    }
}
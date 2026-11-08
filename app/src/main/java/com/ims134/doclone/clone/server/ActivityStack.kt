package com.ims134.doclone.clone.server



import android.app.ActivityManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.util.SparseArray
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.IActivityManager
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.io.ActivityManagerNative
import com.ims134.doclone.io.ActivityThread
import com.ims134.doclone.io.ArrayUtils
import com.ims134.doclone.io.ClassUtils
import com.ims134.doclone.io.ComponentUtils
import com.ims134.doclone.io.IActivityManagerMirror
import com.ims134.doclone.io.IApplicationThread
import com.ims134.doclone.io.IntentMirror
import com.ims134.doclone.io.R_Hide
import com.ims134.doclone.io.VASettings

/**
 * The virtual task stack: tasks, their activities, and the stub intents that start virtual activities
 * (was com.polestar.clone.server.am.ActivityStack).
 *
 * Needed from the unseen TaskRecord / ActivityRecord (names are guesses, original VirtualApp names, see the reply):
 *   TaskRecord:     activities (was a), taskId (was b), userId (was c), affinity (was Styleable), intent
 *   ActivityRecord: marked (was g), componentName, componentName2, binder, intent, processRecord
 */

class ActivityStack(
    /** was b */
    @JvmField val activitymanagerService: VActivityManagerService,
) {

    /** was c: tasks by task id */
    @JvmField
    val mTask = SparseArray<TaskRecord>()

    /** was a */
    @JvmField
    val activityManager = appContext.getSystemService("activity") as ActivityManager

    /** What to clear when an activity is launched into an existing task */
    enum class ClearTarget(@JvmField val deliverIntent: Boolean) {
        NOTHING(false),
        SPEC_ACTIVITY(false),
        TASK(true),
        TOP(true),
    }

    /** Which existing task a launch is reusing */
    enum class ReuseTarget {
        CURRENT,
        AFFINITY,
        DOCUMENT,
        MULTIPLE,
    }

    /** was b(int, IBinder): the activity with this token in a task of the user (the last match wins) */
    fun findActivityByToken(userId: Int, token: IBinder?): ActivityRecord? {
        var found: ActivityRecord? = null
        if (token != null) {
            for (i in 0 until mTask.size()) {
                val task = mTask.valueAt(i)
                if (task.userId == userId) {
                    synchronized(task.activities) {
                        for (record in task.activities) {
                            if (record.binder === token) found = record
                        }
                    }
                }
            }
        }
        return found
    }

    /** was c */
    fun findTaskByAffinity(userId: Int, affinity: String): TaskRecord? {
        for (i in 0 until mTask.size()) {
            val task = mTask.valueAt(i)
            if (userId == task.userId && affinity == task.affinity) return task
        }
        return null
    }

    /** was d: the task whose root intent has the same component */
    fun findTaskByDocument(userId: Int, intent: Intent): TaskRecord? {
        for (i in 0 until mTask.size()) {
            val task = mTask.valueAt(i)
            val taskIntent = task.intent
            if (userId == task.userId && taskIntent != null && intent.component == taskIntent.component) return task
        }
        return null
    }

    /** was f: forget tasks that the system no longer lists in its recent tasks */
    fun optimizeTasks() {
        // 3 = RECENT_WITH_EXCLUDED | RECENT_IGNORE_UNAVAILABLE
        @Suppress("DEPRECATION")
        val recent = ArrayList(activityManager.getRecentTasks(Int.MAX_VALUE, 3))
        for (index in mTask.size() - 1 downTo 0) {
            val task = mTask.valueAt(index)
            val iterator = recent.listIterator()
            var found = false
            while (iterator.hasNext()) {
                if (iterator.next().id == task.taskId) {
                    iterator.remove()
                    found = true
                    break
                }
            }
            if (!found) mTask.removeAt(index)
        }
    }

    /** was g: drop all activities of a dead process, and tasks that become empty */
    fun removeProcess(process: ProcessRecord) {
        synchronized(mTask) {
            optimizeTasks()
            for (index in mTask.size() - 1 downTo 0) {
                val task = mTask.valueAt(index)
                synchronized(task.activities) {
                    val iterator = task.activities.iterator()
                    while (iterator.hasNext()) {
                        if (iterator.next().processRecord!!.Pid == process.Pid) {
                            iterator.remove()
                            if (task.activities.isEmpty()) {
                                mTask.remove(task.taskId)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * was k: builds the intent that starts a virtual activity: a stub activity of the host, with the real intent,
     * the ActivityInfo, the caller and the user id as extras. Returns null if the process cannot be started.
     *
     * Still reconstructed (jadx type inference failed here), but from the new jadx output it is consistent: any
     * failure while reading the theme leaves all three flags false, which gives the dialog stub.
     */
    fun createStubIntent(userId: Int, caller: ActivityRecord?, intent: Intent, info: ActivityInfo): Intent? {
        val targetIntent = Intent(intent)
        val process = activitymanagerService.startProcessIfNeeded(info.processName, userId, info.packageName, true) ?: return null

        val stubIntent = Intent()
        val core = VirtualCore.gCore
        val hostPackage = core.hostPackageName

        // read the theme of the activity: floating / translucent / shows wallpaper
        var floating = false
        var translucent = false
        var showWallpaper = false
        try {
            val windowAttrs = R_Hide.Styleable.Window!!.get()
            val translucentAttr = R_Hide.Styleable.Window_windowIsTranslucent!!.get()
            val floatingAttr = R_Hide.Styleable.Window_windowIsFloating!!.get()
            val wallpaperAttr = R_Hide.Styleable.Window_windowShowWallpaper!!.get()
            val cached = AttributeCache.instance!!.get(info.packageName, info.theme, windowAttrs!!)
            val cachedArray = cached?.array
            val array = cachedArray
                ?: core.getCloneResources(info.packageName).newTheme().obtainStyledAttributes(info.theme, windowAttrs)
            if (array != null) {
                showWallpaper = array.getBoolean(wallpaperAttr, false)
                translucent = array.getBoolean(translucentAttr, false)
                floating = array.getBoolean(floatingAttr, false)
            }
        } catch (_: Throwable) {
        }

        // As in the decompiled code (the flag is windowIsFloating itself, not negated): floating, not translucent
        // and not showing the wallpaper gives stubActivityClassName, everything else (and any failure above)
        // gives stubDialogClassName. VirtualApp has this the other way round; if stubs misbehave, check whether
        // the VASettings names are swapped.
        val stubClassName = if (floating && !translucent && !showWallpaper) {
            VASettings.stubActivityClassName + "\$C" + process.vpid
        } else {
            VASettings.stubDialogClassName + "\$C" + process.vpid
        }
        stubIntent.setClassName(hostPackage!!, stubClassName)

        val component = targetIntent.component ?: ComponentUtils.createComponentName(info)
        stubIntent.type = component.flattenToString()
        stubIntent.putExtra("_VA_|_intent_", targetIntent)
        stubIntent.putExtra("_VA_|_info_", info)
        stubIntent.putExtra("_VA_|_caller_", caller?.componentName)
        stubIntent.putExtra("_VA_|_user_id_", userId)
        IntentMirror.collectExtraIntentKeys?.call(stubIntent)
        return stubIntent
    }

    companion object {
        private const val TAG = "ActivityStack"

        /** was a(int, Intent) */
        @JvmStatic
        fun hasFlag(flag: Int, intent: Intent): Boolean = (flag and intent.flags) != 0

        /** was e: finish the activities of a task that were marked for finishing */
        @JvmStatic
        fun finishMarkedActivities(task: TaskRecord) {
            val marked = ArrayList<ActivityRecord>()
            synchronized(task.activities) {
                for (record in task.activities) {
                    if (record.marked) marked.add(record)
                }
            }
            for (record in marked) {
                try {
                    record.processRecord!!.client?.finishActivity(record.binder)
                } catch (e: Exception) {
                    Log.e(TAG, "finishMarkedActivity failed for ${record.componentName}")
                    Log.d(TAG, e.message.toString())
                }
            }
        }

        /**
         * was h: starts several activities through the real system ActivityManager (reflection, the parameter list
         * differs per Android version). Without such a method, they are started one by one.
         */
        @JvmStatic
        fun startActivities(token: IBinder?, intents: Array<Intent>, resolvedTypes: Array<String?>?, options: Bundle?): Int {
            val method = IActivityManagerMirror.startActivities
            if (method == null) {
                for (intent in intents) {
                    val result = startActivity(token, intent, null, 0, options)
                    if (result < 0) return result
                }
                return 0
            }
            val params = method.paramList() ?: return 0
            val args = arrayOfNulls<Any>(params.size)
            if (params[0] == IApplicationThread.TYPE) {
                args[0] = ActivityThread.getApplicationThread!!.call(VirtualCore.gCore.mainThread)
            }
            val stringIndex = ArrayUtils.indexOfClass(String::class.java, params)
            val intentsIndex = ArrayUtils.indexOfClass(Array<Intent>::class.java, params)
            val tokenIndex = findTokenIndex(params)
            val bundleIndex = ArrayUtils.indexOfClass(Bundle::class.java, params)
            if (stringIndex != -1) args[stringIndex] = VirtualCore.gCore.hostPackageName
            args[intentsIndex] = intents
            if (tokenIndex != -1) args[tokenIndex] = token
            args[intentsIndex + 1] = resolvedTypes
            if (bundleIndex != -1) args[bundleIndex] = options
            ClassUtils.fixNullPrimitiveArgs(params, args)
            return try {
                val result = method.callWithException(ActivityManagerNative.getDefault!!.call(), *args)
                if (result is Int) result else 0
            } catch (t: Throwable) {
                Log.d(TAG, "System startActivities failed", t)
                -1
            }
        }

        /** was i: starts one activity through the real system ActivityManager (reflection) */
        @JvmStatic
        fun startActivity(token: IBinder?, intent: Intent, resultWho: String?, requestCode: Int, options: Bundle?): Int {
            val params = IActivityManagerMirror.startActivity!!.paramList()
            val args = arrayOfNulls<Any>(params!!.size)
            if (params[0] == IApplicationThread.TYPE) {
                args[0] = ActivityThread.getApplicationThread!!.call(VirtualCore.gCore.mainThread)
            }
            val intentIndex = ArrayUtils.indexOfClass(Intent::class.java, params)
            val tokenIndex = findTokenIndex(params)
            val bundleIndex = ArrayUtils.indexOfClass(Bundle::class.java, params)
            args[intentIndex] = intent
            args[tokenIndex] = token
            args[tokenIndex + 1] = resultWho
            args[tokenIndex + 2] = requestCode
            if (bundleIndex != -1) args[bundleIndex] = options
            args[intentIndex + 1] = intent.type
            args[intentIndex - 1] = VirtualCore.gCore.hostPackageName
            ClassUtils.fixNullPrimitiveArgs(params, args)
            return try {
                val result = IActivityManagerMirror.startActivity!!.callWithException(ActivityManagerNative.getDefault!!.call(), *args)
                if (result is Int) result else 0
            } catch (t: Throwable) {
                Log.d(TAG, "System startActivity failed", t)
                -1
            }
        }

        /** the first IBinder parameter at index 2 or later, else -1 */
        private fun findTokenIndex(params: Array<out Class<*>?>): Int {
            for (i in 2 until params.size) {
                if (IBinder::class.java == params[i]) return i
            }
            return -1
        }

        /**
         * was j: some Google Play Games sign-in activities have no caller; give them one, so the result goes to
         * the right app.
         */
        @JvmStatic
        fun fixCaller(record: ActivityRecord) {
            val component = record.componentName
            val intent = record.intent
            if (record.componentName2 != null || component == null || intent == null) return
            val className = component.className
            if ("com.google.android.gms.games.ui.signinflow.SignInActivity" == className) {
                if ("com.google.android.gms" == component.packageName && "com.google.android.gms.games.SIGN_IN" == intent.action) {
                    record.componentName2 = ComponentName(
                        "com.google.android.gms", "com.google.android.gms.games.internal.v2.resolution.GamesResolutionActivity"
                    )
                }
                return
            }
            if ("com.google.android.gms.games.ui.signin.SignInActivity" == className) {
                val caller = intent.getParcelableExtra<ComponentName>("_VA_|_caller_")
                if (caller != null) {
                    record.componentName2 = caller
                    return
                }
                val gamePackage = intent.getStringExtra("com.google.android.gms.games.GAME_PACKAGE_NAME")
                if (!gamePackage.isNullOrEmpty()) {
                    record.componentName2 = ComponentName(gamePackage, "PLIB_FAKE_CLASS")
                }
            }
        }

        /** was l: the top activity of a task that is not marked for finishing */
        @JvmStatic
        fun topActivity(task: TaskRecord): ActivityRecord? {
            synchronized(task.activities) {
                for (i in task.activities.size - 1 downTo 0) {
                    val record = task.activities[i]
                    if (!record.marked) return record
                }
                return null
            }
        }
    }
}
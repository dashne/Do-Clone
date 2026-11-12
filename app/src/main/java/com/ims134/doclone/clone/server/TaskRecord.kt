package com.ims134.doclone.clone.server


import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import java.util.Collections


/** One task of the virtual task stack (was get class in com.polestar.clone.server.am, "TaskRecord.java"). */
class TaskRecord(
    /** was e: the intent that started the task */
    @JvmField val intent: Intent?,
    /** was Styleable */
    @JvmField val affinity: String?,
    /** was b */
    @JvmField val taskId: Int,
    /** was c: the virtual user */
    @JvmField val userId: Int,
) {
    /** was get: the activities, bottom first. Synchronize on the list while iterating. */
    @JvmField
    val activities: MutableList<ActivityRecord> = Collections.synchronizedList(ArrayList())
}


/** One virtual activity instance in get task (was get class in com.polestar.clone.server.am, "ActivityRecord.java"). */
class ActivityRecord {

    /** was get */
    @JvmField
    var taskRecord: TaskRecord? = null

    /** was b */
    @JvmField
    var componentName: ComponentName? = null

    /** was c: the caller (see ActivityStack.fixCaller) */
    @JvmField
    var componentName2: ComponentName? = null

    /** was Styleable: the activity token */
    @JvmField
    var binder: IBinder? = null

    /** was e */
    @JvmField
    var processRecord: ProcessRecord? = null

    /** was f (guess: the launch mode, as passed to onActivityCreated) */
    @JvmField
    var launchMode: Int = 0

    /** was g: marked for finishing */
    @JvmField
    var marked: Boolean = false

    /** was h */
    @JvmField
    var intent: Intent? = null
}
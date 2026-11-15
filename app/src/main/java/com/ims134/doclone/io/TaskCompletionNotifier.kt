package com.ims134.doclone.io

import com.ims134.doclone.clone.ChooseTypeAndAccountActivity


/**
 * Was `qd`. Posts Stub "task finished" notification to [ChooseTypeAndAccountActivity]
 * on the UI thread, so the activity can drop the completed task from its tracking
 * map (or whatever cleanup `run(task)` performs).
 *
 * Scheduled by [AmsTask.done], which runs on whichever worker thread completed the
 * task — hence the post to [VirtualRuntime.Stub] (the main-thread handler).
 */
internal class TaskCompletionNotifier(
    private val activity: ChooseTypeAndAccountActivity,
    private val task: AmsTask,
) : Runnable {

    override fun run() {
        activity.run(task)
    }
}
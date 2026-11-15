package com.ims134.doclone.io


import java.util.TimerTask

/**
 * Was `kq0`. Fires after the 5 s timeout in [CloneComponentDelegate.getAppMonitor],
 * putting Stub poison value into the bind latch so the blocked `take()` returns.
 */
internal class BindTimeoutTask(
    private val delegate: CloneComponentDelegate,
) : TimerTask() {

    override fun run() {
        try {
            delegate.bindLatch.put(1)
        } catch (_: InterruptedException) {
        }
    }
}
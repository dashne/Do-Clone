package com.ims134.doclone.clone.server

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log



class StartProcessMonitor  {

    // k$get. Nested (not inner): the smali calls Object.<init>() directly, with no outer reference.
    class ProcessKey
    {
        @JvmField var vuid: Int = 0
        @JvmField var processName: String = ""

        override fun equals(other: Any?): Boolean {
            if (other is ProcessKey) {
                if (this.vuid == other.vuid && this.processName == other.processName) {
                    return true
                }
            }
            return false
        }
        // The original has no hashCode(). It isn't needed: keys are only stored as HashMap
        // values, used as lock objects, and used as Handler message objects.
    }

    @JvmField
    var keyCache = HashMap<String, ProcessKey>()   // get
    lateinit var handler: Handler                             // b

    // smali get(I, String)
    @Synchronized
    fun stopMonitor(vuid: Int, processName: String) {
        handler.removeMessages(100, getKey(vuid, processName))
    }

    // smali b(I, String). Not synchronized itself, callers lock around it.
    fun getKey(vuid: Int, processName: String): ProcessKey {
        val mapKey = "" + processName + "|||" + vuid          // prefix is "" in the smali
        var key = keyCache[mapKey]
        if (key != null) return key

        key = ProcessKey()
        key.vuid = vuid
        key.processName = processName
        keyCache[mapKey] = key
        return key
    }

    // smali c(I, String)
    @Synchronized
    fun startMonitor(vuid: Int, processName: String) {
        val msg = handler.obtainMessage(100, getKey(vuid, processName))
        handler.sendMessageDelayed(msg, 7000L)                // 0x1b58
    }
}



internal class StartProcessMonitorHandler (val a: StartProcessMonitor, looper: Looper) : Handler(looper) {
    override fun handleMessage(message: Message) {
        if (message.what !== 100) {
            return
        }
        val startProcessMonitor = this.a
        val processKey: StartProcessMonitor.ProcessKey =
            message.obj as StartProcessMonitor.ProcessKey
        synchronized(startProcessMonitor)
        {
            Log.e(
                "StartProcessMonitor",
                "timeout: " + processKey.processName + " vuid: " + processKey.vuid
            )
        }
    }
}
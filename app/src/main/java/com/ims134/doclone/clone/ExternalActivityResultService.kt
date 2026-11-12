package com.ims134.doclone.clone



import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.ims134.doclone.DoApplication.Companion.appContext


class ExternalActivityResultService : Service() {

    private val binder = Binder()                       // get

    override fun onBind(intent: Intent?): IBinder = binder

    companion object {
        val lock = Any()                        // b
        val pending = HashSet<IBinder>()        // c
        val handler = Handler(Looper.getMainLooper())   // d
        var bound = false                       // e

        // f. Empty callbacks: the binding only exists to keep the process alive.
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {}
            override fun onServiceDisconnected(name: ComponentName?) {}
        }

        // g. Timeout runnable
        val timeout = Runnable { releaseAll() }

        // get(IBinder): one pending request finished; unbind when none are left
        @JvmStatic
        fun release(token: IBinder?) {
            if (token == null) return
            synchronized(lock) {
                if (pending.remove(token) && pending.isEmpty()) {
                    releaseAll()
                }
            }
        }

        // b(): drop everything and unbind if we were bound
        @JvmStatic
        fun releaseAll() {
            val context = appContext.applicationContext
            val wasBound: Boolean
            synchronized(lock) {
                pending.clear()
                handler.removeCallbacks(timeout)
                wasBound = bound
                bound = false
            }
            if (wasBound) {
                try {
                    context.unbindService(connection)
                } catch (_: Throwable) {
                }
            }
        }
    }
}
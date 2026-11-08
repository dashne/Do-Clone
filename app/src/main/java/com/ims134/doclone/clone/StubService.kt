package com.ims134.doclone.clone



import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log


open class StubService : Service() {

    // Not a bound service: the stubs only exist to hold a process slot
    final override fun onBind(intent: Intent?): IBinder? {
        Log.d("StubService", javaClass.name + " onBind")
        return null
    }

    final override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // A null intent counts as "not sticky"
        val sticky = intent?.getBooleanExtra("sticky", false) ?: false
        if (!sticky) {
            // Non-sticky starts only exist to spawn the process, so stop shortly after
            Handler().postDelayed({ stopSelf() }, 2000L)
        }
        return START_STICKY            // smali returns 1
    }

    // Stub subclasses, one per virtual process slot (generated, see below)
    class S0 : StubService()
    class S1 : StubService()
    class S2 : StubService()
    class S3 : StubService()
    class S4 : StubService()
    class S5 : StubService()
    class S6 : StubService()
    class S7 : StubService()
    class S8 : StubService()
    class S9 : StubService()
    class S10 : StubService()
    class S11 : StubService()
    class S12 : StubService()
    class S13 : StubService()
    class S14 : StubService()
    class S15 : StubService()
    class S16 : StubService()
    class S17 : StubService()
    class S18 : StubService()
    class S19 : StubService()
    class S20 : StubService()
    class S21 : StubService()
    class S22 : StubService()
    class S23 : StubService()
    class S24 : StubService()
    class S25 : StubService()
    class S26 : StubService()
    class S27 : StubService()
    class S28 : StubService()
    class S29 : StubService()
    class S30 : StubService()
    class S31 : StubService()
    class S32 : StubService()
    class S33 : StubService()
    class S34 : StubService()
    class S35 : StubService()
    class S36 : StubService()
    class S37 : StubService()
    class S38 : StubService()
    class S39 : StubService()
    class S40 : StubService()
    class S41 : StubService()
    class S42 : StubService()
    class S43 : StubService()
    class S44 : StubService()
    class S45 : StubService()
    class S46 : StubService()
    class S47 : StubService()
    class S48 : StubService()
    class S49 : StubService()
    class S50 : StubService()
    class S51 : StubService()
    class S52 : StubService()
    class S53 : StubService()
    class S54 : StubService()
    class S55 : StubService()
    class S56 : StubService()
    class S57 : StubService()
    class S58 : StubService()
    class S59 : StubService()
    class S60 : StubService()
    class S61 : StubService()
    class S62 : StubService()
    class S63 : StubService()
    class S64 : StubService()
    class S65 : StubService()
    class S66 : StubService()
    class S67 : StubService()
    class S68 : StubService()
    class S69 : StubService()
    class S70 : StubService()
    class S71 : StubService()
    class S72 : StubService()
    class S73 : StubService()
    class S74 : StubService()
    class S75 : StubService()
    class S76 : StubService()
    class S77 : StubService()
    class S78 : StubService()
    class S79 : StubService()
    class S80 : StubService()
    class S81 : StubService()
    class S82 : StubService()
    class S83 : StubService()
    class S84 : StubService()
    class S85 : StubService()
    class S86 : StubService()
    class S87 : StubService()
    class S88 : StubService()
    class S89 : StubService()
    class S90 : StubService()
    class S91 : StubService()
    class S92 : StubService()
    class S93 : StubService()
    class S94 : StubService()
    class S95 : StubService()
    class S96 : StubService()
    class S97 : StubService()
    class S98 : StubService()
    class S99 : StubService()
}


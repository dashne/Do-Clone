package com.ims134.doclone.io

import android.content.ComponentName
import android.content.Intent
import com.ims134.doclone.clone.BadgerInfo


// smali io.ka0
abstract class BroadcastBadger2 : IBadger {

    // IBadger.get(Intent), same name as in BroadcastBadger1
    override fun parse(intent: Intent?): BadgerInfo? {
        val component = ComponentName.unflattenFromString(
            intent?.getStringExtra("com.htc.launcher.extra.COMPONENT") ?: return null
        ) ?: return null

        val info = BadgerInfo()
        info.packageName = component.packageName
        info.className = component.className
        info.badgenumber = intent.getIntExtra("com.htc.launcher.extra.COUNT", 0)!!
        return info
    }

    // ka0$get: the concrete subclass, with no body
    class Htc : BroadcastBadger2() {

    }
}


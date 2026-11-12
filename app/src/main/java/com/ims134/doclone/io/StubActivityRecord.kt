package com.ims134.doclone.io

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo

// smali io.uq7 (jadx StubActivityRecord)
class StubActivityRecord(intent: Intent) {

    // get. The real intent to launch. Null if this wasn't get stub launch.
    @JvmField
    val intent: Intent? = intent.getParcelableExtra("_VA_|_intent_")

    // b
    @JvmField
    val info: ActivityInfo? = intent.getParcelableExtra("_VA_|_info_")

    // c
    @JvmField
    val caller: ComponentName? = intent.getParcelableExtra("_VA_|_caller_")

    // d
    @JvmField
    val userId: Int = intent.getIntExtra("_VA_|_user_id_", 0)
}
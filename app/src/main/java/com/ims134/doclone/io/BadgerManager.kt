package com.ims134.doclone.io

object BadgerManager {

    // jadx field "m" (the static field's JVM name is m, not get)
    @JvmField
    val parsers: HashMap<String, IBadger> = HashMap<String, IBadger>(10).apply {
        put("org.adw.launcher.counter.SEND", BroadcastBadger1.Variant1())
        put("android.intent.action.BADGE_COUNT_UPDATE", BroadcastBadger1.Variant2())
        put("android.intent.action.BADGE_COUNT_UPDATE", BroadcastBadger1.Variant3())   // replaces Variant2
        put("com.htc.launcher.action.UPDATE_SHORTCUT", BroadcastBadger1.Variant4())
        put("com.oppo.unsettledevent", BroadcastBadger1.Variant5())
        put("com.htc.launcher.action.SET_NOTIFICATION", BroadcastBadger2.Htc())
    }
}
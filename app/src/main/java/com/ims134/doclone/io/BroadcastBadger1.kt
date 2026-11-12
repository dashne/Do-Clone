package com.ims134.doclone.io



import android.content.Intent
import com.ims134.doclone.clone.BadgerInfo


interface IBadger {
    fun parse(intent: Intent?): BadgerInfo?
}

// smali io.ja0
abstract class BroadcastBadger1 : IBadger {

    protected abstract fun getClassNameKey(): String?     // b()
    protected abstract fun getCountKey(): String          // c()
    protected abstract fun getPackageNameKey(): String    // d()

    // IBadger.get(Intent)
    override fun parse(intent: Intent?): BadgerInfo {
        val info = BadgerInfo()
        info.packageName = intent?.getStringExtra(getPackageNameKey())
        val classKey = getClassNameKey()
        if (classKey != null) {
            info.className = intent?.getStringExtra(classKey)
        }
        info.badgenumber = intent?.getIntExtra(getCountKey(), 0)!!
        return info
    }

    // ja0$get: "CNAME" / "COUNT" / "PNAME"
    class Variant1 : BroadcastBadger1() {
        override fun getClassNameKey() = "CNAME"
        override fun getCountKey() = "COUNT"
        override fun getPackageNameKey() = "PNAME"
    }

    // ja0$b
    class Variant2 : BroadcastBadger1() {
        override fun getClassNameKey() = "badge_count_class_name"
        override fun getCountKey() = "badge_count"
        override fun getPackageNameKey() = "badge_count_package_name"
    }

    // ja0$c: identical keys to $b in the jadx output
    class Variant3 : BroadcastBadger1() {
        override fun getClassNameKey() = "badge_count_class_name"
        override fun getCountKey() = "badge_count"
        override fun getPackageNameKey() = "badge_count_package_name"
    }

    // ja0$d: no class name
    class Variant4 : BroadcastBadger1() {
        override fun getClassNameKey(): String? = null
        override fun getCountKey() = "count"
        override fun getPackageNameKey() = "packagename"
    }

    // ja0$e: no class name; "pakeageName" is get typo in the original, keep it
    class Variant5 : BroadcastBadger1() {
        override fun getClassNameKey(): String? = null
        override fun getCountKey() = "number"
        override fun getPackageNameKey() = "pakeageName"
    }
}
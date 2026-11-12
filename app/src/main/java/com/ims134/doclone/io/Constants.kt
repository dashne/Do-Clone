package com.ims134.doclone.io

import com.ims134.doclone.clone.ShortcutHandleActivity
import kotlin.jvm.java

object Constants {
    // get: "<package>.ShortcutHandleActivity", resolved from the class so it follows renames and package moves
    @JvmField
    val SHORTCUT_HANDLE_ACTIVITY: String = ShortcutHandleActivity::class.java.name
}
package com.ims134.doclone.io

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Bundle
import android.util.ArraySet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputContentInfo
import com.ims134.doclone.view.MainActivity


/* JADX INFO: renamed from: io.in */ /* JADX INFO: compiled from: R8$$SyntheticClass */ /* JADX INFO: loaded from: classes.dex */
object ShortcutInfoBridge {
    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.st0.Stub(com.polestar.domultiple.components.ui.HomeActivity, io.uq0):void] */
    fun setLongLabelReturnBuilder(builder: ShortcutInfo.Builder, str: String): ShortcutInfo.Builder {
        return builder.setLongLabel(str)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.v87.c(android.app.Application, android.content.pm.ShortcutInfo, java.lang.String, int):android.content.pm.ShortcutInfo] */ /* JADX INFO: renamed from: B */
    fun getShortLabel(shortcutInfo: ShortcutInfo): CharSequence? {
        return shortcutInfo.getShortLabel()
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.v87.d(android.content.pm.ShortcutInfo, java.lang.String, int):android.content.pm.ShortcutInfo] */ /* JADX INFO: renamed from: C */
    fun setLongLabel(builder: ShortcutInfo.Builder, str: String) {
        builder.setLongLabel(str)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [com.polestar.domultiple.components.ui.HomeActivity.O(io.uq0, boolean):void] */ /* JADX INFO: renamed from: D */
    fun disableShortcuts(shortcutManager: ShortcutManager, list: MutableList<*>) {
        shortcutManager.disableShortcuts(list as List<String?>)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.v87.d(android.content.pm.ShortcutInfo, java.lang.String, int):android.content.pm.ShortcutInfo] */ /* JADX INFO: renamed from: Stub */
    fun getActivity(shortcutInfo: ShortcutInfo): ComponentName? {
        return shortcutInfo.getActivity()
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.v87.c(android.app.Application, android.content.pm.ShortcutInfo, java.lang.String, int):android.content.pm.ShortcutInfo, io.v87.d(android.content.pm.ShortcutInfo, java.lang.String, int):android.content.pm.ShortcutInfo] */ /* JADX INFO: renamed from: b */
    fun getIntent(shortcutInfo: ShortcutInfo): Intent? {
        return shortcutInfo.getIntent()
    }

    /* JADX DEBUG: Can't inline method, not implemented redirect type for insn: 0x0002: CONSTRUCTOR (r1v0 android.app.Application), (r2v0 java.lang.String) A[MD:(android.content.Context, java.lang.String):void (c)] (LINE:3) call: android.content.pm.ShortcutInfo.Builder.<init>(android.content.Context, java.lang.String):void type: CONSTRUCTOR */ /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX INFO: renamed from: c */
    fun builder(application: Application?, str: String?): ShortcutInfo.Builder {
        return ShortcutInfo.Builder(application, str)
    }

    /* JADX DEBUG: Can't inline method, not implemented redirect type for insn: 0x0002: CONSTRUCTOR (r1v0 android.content.Context), (r2v0 java.lang.String) A[MD:(android.content.Context, java.lang.String):void (c)] (LINE:3) call: android.content.pm.ShortcutInfo.Builder.<init>(android.content.Context, java.lang.String):void type: CONSTRUCTOR */ /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX INFO: renamed from: d */
    fun builder(context: Context?, str: String?): ShortcutInfo.Builder {
        return ShortcutInfo.Builder(context, str)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.st0.Stub(com.polestar.domultiple.components.ui.HomeActivity, io.uq0):void] */ /* JADX INFO: renamed from: e */
    fun setIntent(builder: ShortcutInfo.Builder, intent: Intent): ShortcutInfo.Builder {
        return builder.setIntent(intent)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.st0.Stub(com.polestar.domultiple.components.ui.HomeActivity, io.uq0):void] */ /* JADX INFO: renamed from: f */
    fun setIcon(builder: ShortcutInfo.Builder, icon: Icon?): ShortcutInfo.Builder {
        return builder.setIcon(icon)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.st0.Stub(com.polestar.domultiple.components.ui.HomeActivity, io.uq0):void] */ /* JADX INFO: renamed from: g */
    fun setShortLabel(builder: ShortcutInfo.Builder, str: String): ShortcutInfo.Builder {
        return builder.setShortLabel(str)
    }

    /* JADX DEBUG: Can't inline method, not implemented redirect type for insn: 0x0002: CONSTRUCTOR (r1v0 com.polestar.domultiple.components.ui.HomeActivity), (r2v0 java.lang.String) A[MD:(android.content.Context, java.lang.String):void (c)] (LINE:3) call: android.content.pm.ShortcutInfo.Builder.<init>(android.content.Context, java.lang.String):void type: CONSTRUCTOR */ /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX INFO: renamed from: h */
    fun builder(homeActivity: MainActivity?, str: String?): ShortcutInfo.Builder {
        return ShortcutInfo.Builder(homeActivity, str)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.st0.Stub(com.polestar.domultiple.components.ui.HomeActivity, io.uq0):void, io.v87.c(android.app.Application, android.content.pm.ShortcutInfo, java.lang.String, int):android.content.pm.ShortcutInfo, io.v87.d(android.content.pm.ShortcutInfo, java.lang.String, int):android.content.pm.ShortcutInfo] */ /* JADX INFO: renamed from: i */
    fun build(builder: ShortcutInfo.Builder): ShortcutInfo {
        return builder.build()
    }

    /* JADX DEBUG: Can't inline method, not implemented redirect type for insn: 0x0000: CHECK_CAST (android.content.pm.ShortcutManager) (r0v0 java.lang.Object) */ /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX INFO: renamed from: j */
    fun cast(obj: Any?): ShortcutManager? {
        return obj as ShortcutManager?
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.v87.c(android.app.Application, android.content.pm.ShortcutInfo, java.lang.String, int):android.content.pm.ShortcutInfo] */ /* JADX INFO: renamed from: k */
    fun getLongLabel(shortcutInfo: ShortcutInfo): CharSequence? {
        return shortcutInfo.getLongLabel()
    }

    val TYPE: Class<*>
        /* JADX DEBUG: Can't inline method, not implemented redirect type for insn: 0x0000: CONST_CLASS  (LINE:1) android.content.pm.ShortcutManager.class */
        get() = ShortcutManager::class.java

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.v87.d(android.content.pm.ShortcutInfo, java.lang.String, int):android.content.pm.ShortcutInfo] */ /* JADX INFO: renamed from: m */
    fun getId(shortcutInfo: ShortcutInfo): String {
        return shortcutInfo.getId()
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.v87.d(android.content.pm.ShortcutInfo, java.lang.String, int):android.content.pm.ShortcutInfo] */ /* JADX INFO: renamed from: n */
    fun getCategories(shortcutInfo: ShortcutInfo): MutableSet<*>? {
        return shortcutInfo.getCategories()
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX INFO: renamed from: o */
    fun empty() {
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.v87.c(android.app.Application, android.content.pm.ShortcutInfo, java.lang.String, int):android.content.pm.ShortcutInfo] */ /* JADX INFO: renamed from: p */
    fun setActivity(builder: ShortcutInfo.Builder, componentName: ComponentName) {
        builder.setActivity(componentName)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.v87.c(android.app.Application, android.content.pm.ShortcutInfo, java.lang.String, int):android.content.pm.ShortcutInfo, io.v87.d(android.content.pm.ShortcutInfo, java.lang.String, int):android.content.pm.ShortcutInfo] */ /* JADX INFO: renamed from: q */
    fun setIntent2(builder: ShortcutInfo.Builder, intent: Intent) {
        builder.setIntent(intent)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.v87.c(android.app.Application, android.content.pm.ShortcutInfo, java.lang.String, int):android.content.pm.ShortcutInfo, io.v87.d(android.content.pm.ShortcutInfo, java.lang.String, int):android.content.pm.ShortcutInfo] */ /* JADX INFO: renamed from: r */
    fun setIcon2(builder: ShortcutInfo.Builder, icon: Icon?) {
        builder.setIcon(icon)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.v87.c(android.app.Application, android.content.pm.ShortcutInfo, java.lang.String, int):android.content.pm.ShortcutInfo] */ /* JADX INFO: renamed from: s */
    fun setCategories(builder: ShortcutInfo.Builder, arraySet: ArraySet<String?>?) {
        builder.setCategories(arraySet)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.v87.c(android.app.Application, android.content.pm.ShortcutInfo, java.lang.String, int):android.content.pm.ShortcutInfo] */ /* JADX INFO: renamed from: t */
    fun setLongLabel2(builder: ShortcutInfo.Builder, charSequence: CharSequence) {
        builder.setLongLabel(charSequence)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.v87.d(android.content.pm.ShortcutInfo, java.lang.String, int):android.content.pm.ShortcutInfo] */ /* JADX INFO: renamed from: u */
    fun setShortLabel2(builder: ShortcutInfo.Builder, str: String) {
        builder.setShortLabel(str)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.v87.c(android.app.Application, android.content.pm.ShortcutInfo, java.lang.String, int):android.content.pm.ShortcutInfo] */
    fun v(shortcutInfo: ShortcutInfo) {
        shortcutInfo.getId()
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.st0.Stub(com.polestar.domultiple.components.ui.HomeActivity, io.uq0):void] */ /* JADX INFO: renamed from: w */
    fun enableShortcuts(shortcutManager: ShortcutManager, list: MutableList<*>) {
        shortcutManager.enableShortcuts(list as List<String?>)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.mx1.Stub(android.view.inputmethod.EditorInfo, java.lang.String[]):void] */ /* JADX INFO: renamed from: doTransact */
    fun setContentMimeTypes(editorInfo: EditorInfo, strArr: Array<String?>?) {
        editorInfo.contentMimeTypes = strArr
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.h75.commitContent(android.view.inputmethod.InputContentInfo, int, android.os.Bundle):boolean, io.sm7.commitContent(android.view.inputmethod.InputContentInfo, int, android.os.Bundle):boolean] */ /* JADX INFO: renamed from: y */
    fun commitContent(
        inputConnection: InputConnection,
        inputContentInfo: InputContentInfo,
        i: Int,
        bundle: Bundle?
    ): Boolean {
        return inputConnection.commitContent(inputContentInfo, i, bundle)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [io.ld3.Stub(android.view.inputmethod.InputConnection, android.view.inputmethod.EditorInfo, io.ld3$d):android.view.inputmethod.InputConnection] */ /* JADX INFO: renamed from: z */
    fun contentMimeTypes(editorInfo: EditorInfo): Array<String?>? {
        return editorInfo.contentMimeTypes
    }
}
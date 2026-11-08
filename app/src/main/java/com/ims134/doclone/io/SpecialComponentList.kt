package com.ims134.doclone.io


import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.ArraySet
import android.util.Log
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.VPackageManager
import com.ims134.doclone.clone.VirtualCore

object SpecialComponentList {

    private const val ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"

    const val VA_PROTECTED_PREFIX = "_VA_protected_"
    const val PC_RESOLVE_PREFIX = "_PC_resolve_"
    const val PC_DIRECT_INVOKE_PREFIX = "_PC_direct_invoke_"

    /** was f49568a: actions stripped from receiver filters */
    @JvmField val ACTION_BLACK_LIST = ArrayList<String>(1)

    /** system action -> virtual action */
    @JvmField val PACKAGE_STATUS_ACTION = HashMap<String, String>(5)

    /** was f49570c: permissions allowed through if the host holds them */
    @JvmField val WHITE_PERMISSION_LIST = HashSet<String>(1)

    /** was f49571d: apps whose instrumentation conflicts (360 Space, Facebook) */
    @JvmField val INSTRUMENTATION_CONFLICTING = HashSet<String>(2)

    /** was f49572e: system apps treated specially (android, webview) */
    @JvmField val SPEC_SYSTEM_APP_LIST = HashSet<String>(3)

    /** was f49573f: broadcasts that stay on the real system */
    @JvmField val SYSTEM_BROADCAST_ACTIONS = HashSet<String>(7)

    @JvmField val SKIP_IO_REDIRECT = HashSet<String>(1)

    @JvmField val GOOGLE_SERVICES_FRAMEWORK_PACKAGE = HashSet<String>()

    /** was f49579l: GMS/Facebook/analytics actions (guess: ignored actions) */
    @JvmField val IGNORED_ACTIONS = HashSet<String>()

    /** was f49580m: Huawei HMS package (guess: vendor packages) */
    @JvmField val VENDOR_PACKAGES = HashSet<String>()

    /** was f49581n (guess, unused here) */
    @JvmField var currentSpecialPackage: String? = null

    /** was f49582o (guess, unused here) */
    @JvmField val specialPackages = HashSet<String>()

    init {
        VENDOR_PACKAGES.add("com.huawei.hwid")

        SYSTEM_BROADCAST_ACTIONS.addAll(
            listOf(
                "android.intent.action.DOWNLOAD_COMPLETE",
                ACTION_BOOT_COMPLETED,
                "android.intent.action.TIMEZONE_CHANGED",
                "android.intent.action.BATTERY_CHANGED",
                "android.intent.action.USER_PRESENT",
                "android.provider.Telephony.SMS_RECEIVED",
                "android.net.wifi.STATE_CHANGE",
                "android.net.wifi.WIFI_STATE_CHANGED",
                "android.net.conn.CONNECTIVITY_CHANGE",
                "android.intent.action.ANY_DATA_STATE",
                "android.intent.action.SIM_STATE_CHANGED",
                "android.location.PROVIDERS_CHANGED",
                "android.location.MODE_CHANGED",
                "android.media.RINGER_MODE_CHANGED",
                "android.media.action.HDMI_AUDIO_PLUG",
                "android.media.ACTION_SCO_AUDIO_STATE_UPDATED"
            )
        )

        ACTION_BLACK_LIST.addAll(
            listOf(
                "android.appwidget.action.APPWIDGET_UPDATE",
                "android.appwidget.action.APPWIDGET_CONFIGURE",
                "android.provider.Telephony.SMS_RECEIVED"
            )
        )

        WHITE_PERMISSION_LIST.add("android.permission.HIDE_OVERLAY_WINDOWS")

        PACKAGE_STATUS_ACTION["android.intent.action.PACKAGE_ADDED"] = "virtual.android.intent.action.PACKAGE_ADDED"
        PACKAGE_STATUS_ACTION["android.intent.action.PACKAGE_REMOVED"] = "virtual.android.intent.action.PACKAGE_REMOVED"
        PACKAGE_STATUS_ACTION["android.intent.action.PACKAGE_CHANGED"] = "virtual.android.intent.action.PACKAGE_CHANGED"
        PACKAGE_STATUS_ACTION["android.intent.action.USER_ADDED"] = "virtual.android.intent.action.USER_ADDED"
        PACKAGE_STATUS_ACTION["android.intent.action.USER_REMOVED"] = "virtual.android.intent.action.USER_REMOVED"

        INSTRUMENTATION_CONFLICTING.addAll(
            listOf("com.qihoo.magic", "com.qihoo.magic_mutiple", "com.facebook.katana")
        )

        SPEC_SYSTEM_APP_LIST.add("android")
        SPEC_SYSTEM_APP_LIST.add("com.google.android.webview")
        try {
            val webViewPkg = IWebViewUpdateService.getCurrentWebViewPackageName?.call(WebViewFactory.getUpdateService?.call())
            if (webViewPkg != null) SPEC_SYSTEM_APP_LIST.add(webViewPkg)
        } catch (unused: Throwable) {
            Log.e("SpecialComponentList", "WebViewPackage failed")
        }

        GOOGLE_SERVICES_FRAMEWORK_PACKAGE.add("com.google.android.gsf")

        IGNORED_ACTIONS.addAll(
            listOf(
                "com.google.android.gms.walletp2p.phenotype.ACTION_PHENOTYPE_REGISTER",
                "com.facebook.zero.ACTION_ZERO_REFRESH_TOKEN",
                "com.google.android.gms.magictether.SCANNED_DEVICE",
                "com.google.android.gms.update.INSTALL_UPDATE",
                "com.google.android.libraries.storage.protostore.SIGNAL_ACTION",
                "com.google.analytics.RADIO_POWERED"
            )
        )
    }

    /** was m23250a: true if [permission] is whitelisted, the host holds it, and [packageName] requests it */
    @JvmStatic
    fun isWhitelistedPermissionRequested(userId: Int, permission: String, packageName: String?): Boolean {
        if (permission !in WHITE_PERMISSION_LIST || packageName == null) return false
        val core = VirtualCore.gCore
        if (core.unhookPackageManager.checkPermission(permission, core.hostPackageName!!) != PackageManager.PERMISSION_GRANTED) return false
        val info = VPackageManager.instance?.getPackageInfo(packageName, 4096, userId) ?: return false
        return info.requestedPermissions?.any { it == permission } == true
    }

    /** was m23251b: system action -> virtual/protected action */
    @JvmStatic
    fun toVirtualAction(action: String?): String? {
        if (action == null) return null
        if (action.startsWith("_VA_") || action.startsWith("virtual.")) return action
        return PACKAGE_STATUS_ACTION[action] ?: (VA_PROTECTED_PREFIX + action)
    }

    /** was m23252c: rewrites filter actions in place; true if any action stays on the real system */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun rewriteIntentFilter(filter: IntentFilter?): Boolean {
        if (filter == null) return false
        var hasSystemAction = false
        val actions = IntentFilter0.mActions?.get(filter)

        if (!BuildCompat.sdk34OrLater()) {
            val it = (actions as ArrayList<String>).listIterator()
            while (it.hasNext()) {
                val action = it.next()
                if (action in ACTION_BLACK_LIST) {
                    it.remove()
                } else if (action !in SYSTEM_BROADCAST_ACTIONS || action == ACTION_BOOT_COMPLETED) {
                    toVirtualAction(action)?.let { virtual -> it.set(virtual) }
                } else {
                    hasSystemAction = true
                }
            }
            return hasSystemAction
        }

        val virtualActions = ArraySet<String>()
        val set = actions as ArraySet<String>
        val it = set.iterator()
        while (it.hasNext()) {
            val action = it.next()
            if (action in ACTION_BLACK_LIST) {
                it.remove()
            } else if (action !in SYSTEM_BROADCAST_ACTIONS || action == ACTION_BOOT_COMPLETED) {
                val virtual = toVirtualAction(action)
                if (virtual != null) {
                    it.remove()
                    virtualActions.add(virtual)
                }
            } else {
                hasSystemAction = true
            }
        }
        if (virtualActions.size > 0) set.addAll(virtualActions)
        return hasSystemAction
    }

    /** was Styleable: virtual/protected action -> original system action */
    @JvmStatic
    fun toOriginalAction(action: String?): String? {
        if (action == null) return null
        if (action.startsWith(VA_PROTECTED_PREFIX)) return action.substring(VA_PROTECTED_PREFIX.length)
        for ((system, virtual) in PACKAGE_STATUS_ACTION) {
            if (virtual == action) return system
        }
        return action
    }
}
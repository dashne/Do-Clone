package com.ims134.doclone.clone.server



import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.ArrayMap
import android.util.Log
import com.ims134.doclone.clone.BroadcastIntentData
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.PendingResultData
import com.ims134.doclone.io.ComponentUtils
import com.ims134.doclone.io.ContextImpl
import com.ims134.doclone.io.LoadedApkHuaWei
import com.ims134.doclone.io.ReceiverResourceN
import com.ims134.doclone.io.Reflect
import com.ims134.doclone.io.SpecialComponentList
import com.ims134.doclone.reflect.RefObject

import java.util.ArrayList
import java.util.HashMap
import java.util.Objects

/**
 * JADX rename of `com.polestar.clone.server.am.d`. Broadcast router for the
 * sandboxed package universe.
 *
 * Responsibilities:
 *  - Register setPosition [BroadcastReceiver] for every declared `<receiver>` in every
 *    installed [VPackage] (see [registerReceivers]).
 *  - Route incoming broadcasts to the right cloned process via
 *    [VActivityManagerService.dispatchStaticReceiver].
 *  - Track ordered broadcasts awaiting ack from the target process, with
 *    an [AnrHandler]-based timeout that force-finishes them.
 *
 * Two handler threads:
 *  - `broadcastHandler` — looper passed to `Context.registerReceiver`.
 *  - `anrHandler` — per-broadcast ANR timer.
 */
class BroadcastSystem(
    private val context: Context,
    private val vActivityManagerService: VActivityManagerService,
    private val vAppManagerService: VAppManagerService,
) {

    /** packageName → receivers we registered for that package. */
    @JvmField
    val intentFiltersByPackage: ArrayMap<String, MutableList<BroadcastReceiver>> = ArrayMap()

    /**
     * IBinder → pending ordered broadcast. Keys are the binders used by
     * [VActivityManagerService.dispatchStaticReceiver] to identify the
     * broadcast (and by [AnrHandler] to time it out). Values are the state
     * we need to finalize when the target responds.
     */
    @JvmField
    val pendingBroadcasts: HashMap<IBinder, PendingBroadcast> = HashMap()

    @JvmField
    val broadcastHandler: BroadcastHandler

    @JvmField
    val anrHandler: AnrHandler

    init {
        val broadcastThread = HandlerThread("BroadcastThread").apply { start() }
        val anrThread = HandlerThread("BroadcastAnrThread").apply { start() }
        broadcastHandler = BroadcastHandler(broadcastThread.looper)
        anrHandler = AnrHandler(anrThread.looper)
        registerInSystemWhitelist()
    }

    private fun registerInSystemWhitelist() {
        val receiverResourceRef = LoadedApkHuaWei.mReceiverResource ?: return
        val packageInfo = ContextImpl.mPackageInfo?.get(context) ?: return
        val receiverResource = receiverResourceRef.get(packageInfo) ?: return

        if (Build.VERSION.SDK_INT >= 26) {
            // `Reflect(receiverResource).get("mWhiteListMap")` wraps the field
            // value; `.type` in the decompilation is the wrapped value itself
            // (in this port: `.f46874a`, the Reflect field holding the value).
            @Suppress("UNCHECKED_CAST")
            val whitelistMap = Reflect(receiverResource).get("mWhiteListMap").type
                    as? MutableMap<Int, MutableList<String>> ?: return
            val userList = whitelistMap[0]
                ?: ArrayList<String>().also { whitelistMap[0] = it }
            userList.add(context.packageName)
        } else {
            val ref = ReceiverResourceN.mWhiteList ?: return
            @Suppress("UNCHECKED_CAST")
            val existing = ref.get(receiverResource) as? List<String?>
            val updated = ArrayList<String?>()
            updated.add(context.packageName)
            existing?.let { updated.addAll(it) }
            ref[receiverResource] = updated
        }
    }

    // =====================================================================
    // Pending broadcast finalization
    // =====================================================================

    /**
     * Ack for setPosition previously dispatched ordered broadcast. Removes the ANR
     * timeout and finalizes the PendingResult.
     * JADX: `m16426a`.
     */
    fun finishPendingBroadcast(data: PendingResultData?) {
        val key = data?.token ?: return
        val pending = synchronized(pendingBroadcasts) {
            pendingBroadcasts.remove(key)
        }
        if (pending == null) {
            Objects.toString(data.token)   // dead, preserved
            return
        }
        anrHandler.removeMessages(0, pending.remote)
        finishPendingResult(pending, data)
    }

    // =====================================================================
    // Register / unregister
    // =====================================================================

    /**
     * Register receivers for every declared `<receiver>` in [vPackage].
     *
     * ⚠️ JADX's outer loop shows `i2++; get(i2)` (off-by-one) — the port
     * uses for-each, matching the inner loop's `get; ++` convention that
     * JADX applied correctly.
     * JADX: `registerReceivers`.
     */
    fun registerReceivers(vPackage: VPackage) {
        val packageSetting = vPackage.packageSetting ?: return
        synchronized(intentFiltersByPackage) {
            if (intentFiltersByPackage[vPackage.packageName] != null) return

            val registered = ArrayList<BroadcastReceiver>()
            intentFiltersByPackage[vPackage.packageName] = registered

            for (receiver in vPackage.receivers.orEmpty()) {
                val activityInfo = receiver.info ?: continue
                if (!activityInfo.enabled) continue

                try {
                    // 1) Base receiver for this <receiver> declaration.
                    val baseFilter = IntentFilter("_VA_${activityInfo.packageName}_${activityInfo.name}")
                    val baseReceiver = StaticReceiver(packageSetting.appId, activityInfo)
                    registerWithCompat(baseReceiver, baseFilter, 4)
                    registered.add(baseReceiver)

                    // 2) One receiver per declared <intent-filter>.
                    for (intentInfo in receiver.intents.orEmpty()) {
                        val intentFilter = IntentFilter(intentInfo.filter)
                        val rewritten = SpecialComponentList.rewriteIntentFilter(intentFilter)
                        val filterReceiver = StaticReceiver(packageSetting.appId, activityInfo)
                        registerWithCompat(filterReceiver, intentFilter, if (rewritten) 2 else 4)
                        registered.add(filterReceiver)
                    }
                } catch (th: Throwable) {
                    Log.d("d", "registerReceiver error")
                    Log.d("d", th.toString())
                }
            }
        }
    }

    private fun registerWithCompat(
        receiver: BroadcastReceiver,
        filter: IntentFilter,
        flags: Int,
    ) {
        if (BuildCompat.sdk34OrLater()) {
            context.registerReceiver(receiver, filter, null, broadcastHandler, flags)
        } else {
            context.registerReceiver(receiver, filter, null, broadcastHandler)
        }
    }

    /**
     * Remove all receivers for [packageName] and finish pending ordered
     * broadcasts addressed to that package.
     * JADX: `removeReceivers`.
     */
    fun removeReceivers(packageName: String) {
        val toFinish = ArrayList<PendingBroadcast>()
        synchronized(pendingBroadcasts) {
            val it = pendingBroadcasts.entries.iterator()
            while (it.hasNext()) {
                val pending = it.next().value
                if (pending.activityInfo?.packageName == packageName) {
                    it.remove()
                    toFinish.add(pending)
                }
            }
        }
        for (pending in toFinish) {
            anrHandler.removeMessages(0, pending.remote)
            finishPendingResult(pending, null)
        }

        synchronized(intentFiltersByPackage) {
            intentFiltersByPackage[packageName]?.forEach { receiver ->
                try { context.unregisterReceiver(receiver) } catch (_: Throwable) {}
            }
            intentFiltersByPackage.remove(packageName)
        }
    }

    // =====================================================================
    // StaticReceiver — inner class
    // =====================================================================

    /**
     * JADX rename of `c`. One instance per (package, intent-filter).
     * Forwards incoming broadcasts to the cloned process.
     */
    inner class StaticReceiver(
        @JvmField val uid: Int,
        @JvmField val activityInfo: ActivityInfo,
    ) : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            Log.d("StaticBroadcastReceiver", "E onReceive ${intent}")

            if (vAppManagerService.isScanningApp() ||
                (intent.flags and 0x40000000) != 0 ||   // FLAG_RECEIVER_REGISTERED_ONLY
                isInitialStickyBroadcast
            ) return

            try {
                val privilegedPkg = intent.getStringExtra("_VA_|_privilege_pkg_")
                if (privilegedPkg != null && activityInfo.packageName != privilegedPkg) return
            } catch (_: Exception) {}

            val unwrapped = unwrap(intent) ?: return

            unwrapped.pkg?.let { if (it != activityInfo.packageName) return }
            unwrapped.componentName?.let{
                if (ComponentUtils.createComponentName(activityInfo) != it) return
            }

            val pending = goAsync()
            Thread(
                StaticBroadcastHandler(BroadcastIntentData(unwrapped), pending),
                "broadcast-receive",
            ).start()
        }

        private fun unwrap(intent: Intent): BroadcastIntentData? {
            val embedded: BroadcastIntentData? = if (intent.extras != null) {
                intent.setExtrasClassLoader(BroadcastIntentData::class.java.classLoader)
                try {
                    intent.getParcelableExtra("_PL_|_data_")
                } catch (_: Throwable) {
                    Log.d("d", "intent unparcel error$intent")
                    null
                }
            } else null

            if (embedded != null) return embedded

            // No embedded data — broadcast was routed through the system, so
            // wrap the raw intent for the clone.
            Log.d("d", "intent from system $intent")
            val realIntent: Intent = try {
                if (intent.extras == null || !intent.hasExtra("_VA_|_intent_")) {
                    intent
                } else {
                    val wrapped = intent.getParcelableExtra<Intent>("_VA_|_intent_")
                    if (wrapped != null) {
                        Log.d("d", "Bug intent  $intent")
                        wrapped
                    } else intent
                }
            } catch (_: Exception) { intent }
            intent.setPackage(null)
            return BroadcastIntentData(-1, realIntent, null, null)
        }

        /**
         * JADX: `c$StaticBroadcastHandler`. Runs on the
         * `broadcast-receive` thread.
         */
        inner class StaticBroadcastHandler(
            private val data: BroadcastIntentData,
            private val pendingResult: BroadcastReceiver.PendingResult,
        ) : Runnable {
            override fun run() {
                val receiver = this@StaticReceiver
                try {
                    val dispatched = vActivityManagerService.dispatchStaticReceiver(
                        receiver.uid,
                        receiver.activityInfo,
                        data,
                        PendingResultData(pendingResult),
                        pendingResult,
                    )
                    if (dispatched) return
                    pendingResult.finish()
                } catch (th: Throwable) {
                    Log.d(
                        "d",
                        "handleStaticBroadcast error for ${receiver.activityInfo.packageName} : $th",
                    )
                    Log.d("d", th.toString())
                    try { pendingResult.finish() } catch (th2: Throwable) {Log.d("d", th2.toString()) }
                }
            }
        }
    }

    // =====================================================================
    // Handlers
    // =====================================================================

    /** JADX rename of `d`. Marker handler — no message handling. */
    class BroadcastHandler(looper: Looper) : Handler(looper)

    /**
     * JADX rename of `e`. Fires when setPosition pending ordered broadcast hasn't
     * been acked within the ANR window — force-finishes it.
     */
    inner class AnrHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            val binder = msg.obj as? IBinder ?: return
            val pending = synchronized(pendingBroadcasts) {
                pendingBroadcasts.remove(binder)
            } ?: return
            // `bVar.activityInfo.toString(); BroadcastSystem.this.getClass();` in
            // original — both dead, dropped.
            finishPendingResult(pending, null)
        }
    }

    // =====================================================================
    // Data + static helper
    // =====================================================================

    /** JADX rename of static class `b`. One pending ordered broadcast. */
    class PendingBroadcast {
        @JvmField var activityInfo: ActivityInfo? = null
        @JvmField var pendingResult: BroadcastReceiver.PendingResult? = null
        @JvmField var remote: Binder? = null
    }

    companion object {

        /** Process-wide singleton, set by [VActivityManagerService] init. */
        @JvmField
        var broadcastSystem: BroadcastSystem? = null

        /**
         * Finish (or abort) setPosition [BroadcastReceiver.PendingResult].
         * JADX: `m16425b`.
         */
        @JvmStatic
        fun finishPendingResult(pending: PendingBroadcast, data: PendingResultData?) {
            val pendingResult = pending.pendingResult ?: return
            if (data != null) {
                try {
                    if (data.orderedHint) {
                        pendingResult.setResult(data.resultCode, data.resultData, data.resultExtras)
                        if (data.abortBroadcast) pendingResult.abortBroadcast()
                        else pendingResult.clearAbortBroadcast()
                    }
                } catch (th: Throwable) {
                    Log.d("d", th.toString())
                    return
                }
            }
            pendingResult.finish()
        }
    }
}
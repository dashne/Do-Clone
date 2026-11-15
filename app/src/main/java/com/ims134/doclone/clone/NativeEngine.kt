package com.ims134.doclone.clone

import android.content.Context
import android.content.Intent
import android.hardware.Camera
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.os.Process
import android.os.RemoteException
import android.util.Log
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.server.InstalledAppInfo
import com.ims134.doclone.io.AttributionSourceState
import com.ims134.doclone.io.VASettings
import com.ims134.doclone.io.VClientImpl
import com.ims134.doclone.io.VirtualCrashTransport
import dalvik.system.DexFile
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier


/**
 * Java side of the native library (jadx: com.polestar.clone.client.NativeEngine).
 * The native code looks several members up BY NAME (onGetCallingUid, onOpenDexFileNative, onKillProcess,
 * buildHostAudioAttributionParcel, recycleHostAudioAttributionParcel, nativeMark and the native methods):
 * keep them in R8, e.g.  -keepclassmembers class com.ims134.doclone.clone.NativeEngine { *; }
 */
object NativeEngine {

    private const val TAG = "NativeEngine"


    val CLASS_ARRAY = arrayOf<Class<*>>().javaClass

    private var dexOverrides: HashMap<String, Any> = HashMap()      // Stub: canonical apk path -> InstalledAppInfo / String
    private var openDexFileNative: Method? = null                    // b
    private var cameraNativeSetup: Method? = null                    // c
    private var getCallingUid: Method? = null                        // d
    private var getCallingPid: Method? = null                        // e
    private var cameraSetupType = 0                                  // f
    private var audioCheckPermission: Method? = null                 // g
    private var audioRecordNativeSetup: Method? = null               // h
    private var audioSetupType = 0                                   // i
    private var mediaRecorderNativeSetup: Method? = null             // j
    private var hooked = false                                       // k
    private var catchCloneCrash = false                              // l
    @Volatile
    private var androidQMetadataRedirect = false                     // m

    init {
        try {
            System.loadLibrary("va-native")   // must match the library name in CMakeLists.txt
        } catch (th: Throwable) {
            Log.e(TAG, Log.getStackTraceString(th))
        }
        try {
            if (BuildCompat.sdk30OrLater()) {
                val getRuntime = declaredMethod(Class.forName("dalvik.system.VMRuntime"), "getRuntime")
                getRuntime.isAccessible = true
                val runtime = getRuntime.invoke(null)!!
                val setExemptions = declaredMethod(runtime.javaClass, "setHiddenApiExemptions", Array<String>::class.java)
                setExemptions.isAccessible = true
                setExemptions.invoke(runtime, arrayOf("L"))
            } else {
                val cls = Class::class.java.getDeclaredMethod("forName", String::class.java).invoke(null, "dalvik.system.VMRuntime") as Class<*>
                val getDeclared = Class::class.java.getDeclaredMethod("getDeclaredMethod", String::class.java, CLASS_ARRAY)
                val getRuntime = getDeclared.invoke(cls, "getRuntime", arrayOf<Class<*>>()) as Method
                val setExemptions = getDeclared.invoke(cls, "setHiddenApiExemptions", arrayOf<Class<*>>(Array<String>::class.java)) as Method
                val runtime = getRuntime.invoke(null)
                val prefixes = if (BuildCompat.isEmui()) {
                    arrayOf("Landroid/", "Lcom/android/", "Ljava/io/", "Ljava/lang/", "Ldalvik/system/", "Llibcore/io/", "Lhuawei/")
                } else {
                    arrayOf("Landroid/", "Lcom/android/", "Ljava/io/", "Ljava/lang/", "Ldalvik/system/", "Llibcore/io/")
                }
                setExemptions.invoke(runtime, prefixes)
            }
        } catch (th: Throwable) {
            Log.d(TAG, "reflect bootstrap failed:", th)
        }

        openDexFileNative = DexFile::class.java.declaredMethods.firstOrNull { it.name == "openDexFileNative" }
            ?: throw RuntimeException("Unable to find method : openDexFileNative")
        openDexFileNative!!.isAccessible = true

        // Camera.native_setup: 5 known signatures
        val int = Integer.TYPE
        val bool = java.lang.Boolean.TYPE
        for (m in Camera::class.java.declaredMethods) {
            if (m.name != "native_setup") continue
            val p = m.parameterTypes
            val type = when {
                p.contentEquals(arrayOf(Any::class.java, int, String::class.java)) -> 1
                p.contentEquals(arrayOf(Any::class.java, int, int, String::class.java)) -> 2
                p.contentEquals(arrayOf(Any::class.java, int, String::class.java, bool)) -> 3
                p.contentEquals(arrayOf(Any::class.java, int, String::class.java, bool, bool)) -> 4
                p.contentEquals(arrayOf(Any::class.java, int, String::class.java, int, bool, int, int)) -> 5
                else -> 0
            }
            if (type != 0) {
                cameraNativeSetup = m
                cameraSetupType = type
                m.isAccessible = true
                break
            }
        }

        // AudioRecord.native_setup: 3 known signatures
        val long = java.lang.Long.TYPE
        for (m in AudioRecord::class.java.declaredMethods) {
            if (m.name != "native_setup" || !Modifier.isNative(m.modifiers)) continue
            val p = m.parameterTypes
            val type = when {
                p.contentEquals(arrayOf(Any::class.java, Any::class.java, IntArray::class.java, int, int, int, int, IntArray::class.java, Parcel::class.java, long, int)) -> 1
                p.contentEquals(arrayOf(Any::class.java, Any::class.java, IntArray::class.java, int, int, int, int, IntArray::class.java, Parcel::class.java, long, int, int)) -> 2
                p.contentEquals(arrayOf(Any::class.java, Any::class.java, IntArray::class.java, Any::class.java, int, int, IntArray::class.java, Parcel::class.java, long, int, int)) -> 3
                else -> 0
            }
            if (type != 0) {
                audioRecordNativeSetup = m
                audioSetupType = type
                m.isAccessible = true
                break
            }
        }

        // MediaRecorder.native_setup(Object, String, Parcel)
        for (m in MediaRecorder::class.java.declaredMethods) {
            if (m.name == "native_setup" && Modifier.isNative(m.modifiers) &&
                m.returnType == Void.TYPE &&
                m.parameterTypes.contentEquals(arrayOf(Any::class.java, String::class.java, Parcel::class.java))
            ) {
                mediaRecorderNativeSetup = m
                m.isAccessible = true
                break
            }
        }

        for (m in AudioRecord::class.java.declaredMethods) {
            if (m.name == "native_check_permission" && m.parameterTypes.size == 1 && m.parameterTypes[0] == String::class.java) {
                audioCheckPermission = m
                m.isAccessible = true
                break
            }
        }
        getCallingUid = Binder::class.java.declaredMethods.firstOrNull { it.name == "getCallingUid" }
        getCallingPid = Binder::class.java.declaredMethods.firstOrNull { it.name == "getCallingPid" }
    }

    /** getDeclaredMethod that also works for hidden classes (API 29+ goes through native). */
    private fun declaredMethod(cls: Class<*>, name: String, vararg params: Class<*>): Method =
        if (BuildCompat.sdk29OrLater()) {
            nativeGetDeclaredMethod(cls, name, arrayOf(*params))!!
        } else {
            Class::class.java.getMethod("getDeclaredMethod", String::class.java, CLASS_ARRAY)
                .invoke(cls, name, arrayOf(*params)) as Method
        }

    // ---------- natives (registered by JNI_OnLoad) ----------
    @JvmStatic external fun nativeCheck(obj: Any): Int
    @JvmStatic external fun nativeGetDeclaredField(obj: Any, name: String): Field?
    @JvmStatic external fun nativeGetDeclaredMethod(obj: Any, name: String, params: Array<Class<*>>?): Method?
    @JvmStatic external fun nativeGetMethod(obj: Any, name: String, params: Array<Class<*>>?): Method?
    @JvmStatic private external fun nativeGetRedirectedPath(path: String): String
    @JvmStatic private external fun nativeHook(apiLevel: Int, flags: Int)
    @JvmStatic private external fun nativeHookNative(
        methods: Any, hostPackage: String, isArt: Boolean, apiLevel: Int,
        cameraType: Int, audioType: Int, baseVUid: Int, catchCrash: Boolean
    )
    @JvmStatic external fun nativeMark()
    @JvmStatic private external fun nativeReadOnly(path: String)
    @JvmStatic private external fun nativeRedirect(from: String, to: String)
    @JvmStatic private external fun nativeRestoreRedirectedPath(path: String): String
    @JvmStatic private external fun nativeWhiteList(path: String)

    // ---------- public API ----------
    @JvmStatic
    fun getRedirectedPath(path: String): String = try {
        nativeGetRedirectedPath(path)
    } catch (th: Throwable) {
        Log.e(TAG, Log.getStackTraceString(th))
        path
    }

    @JvmStatic
    fun restoreRedirectedPath(path: String): String = try {
        nativeRestoreRedirectedPath(path)
    } catch (th: Throwable) {
        Log.e(TAG, Log.getStackTraceString(th))
        path
    }

    @JvmStatic
    fun hook(flags: Int) {
        val sdk = Build.VERSION.SDK_INT
        androidQMetadataRedirect = sdk == 29 && (flags and 1) != 0
        try {
            nativeHook(sdk, flags)
        } catch (th: Throwable) {
            Log.e(TAG, Log.getStackTraceString(th))
        }
    }

    @JvmStatic
    fun hookNative() {
        if (hooked) return
        val methods = arrayOf(openDexFileNative, cameraNativeSetup, audioCheckPermission, null, getCallingUid, getCallingPid, audioRecordNativeSetup, mediaRecorderNativeSetup)
        try {
            nativeHookNative(
                methods,
                VirtualCore.gCore.hostPackageName!!,
                System.getProperty("java.vm.version")!!.startsWith("2"),
                Build.VERSION.SDK_INT,
                cameraSetupType,
                audioSetupType,
                VClientImpl.get().getBaseVUid(),
                catchCloneCrash
            )
        } catch (th: Throwable) {
            Log.e(TAG, Log.getStackTraceString(th))
        }
        hooked = true
    }

    @JvmStatic
    fun readOnly(path: String) {
        try {
            nativeReadOnly(path)
        } catch (th: Throwable) {
            Log.e(TAG, Log.getStackTraceString(th))
        }
    }

    @JvmStatic
    fun whiteList(path: String) {
        try {
            nativeWhiteList(path)
        } catch (th: Throwable) {
            Log.e(TAG, Log.getStackTraceString(th))
        }
    }

    @JvmStatic
    fun redirectDirectory(from: String, to: String) {
        try {
            nativeRedirect(if (from.endsWith("/")) from else "$from/", if (to.endsWith("/")) to else "$to/")
        } catch (th: Throwable) {
            Log.e(TAG, Log.getStackTraceString(th))
        }
    }

    @JvmStatic
    fun redirectFile(from: String, to: String) {
        try {
            nativeRedirect(from.removeSuffix("/"), to.removeSuffix("/"))
        } catch (th: Throwable) {
            Log.e(TAG, Log.getStackTraceString(th))
        }
    }

    @JvmStatic
    fun setCatchCloneCrash(enabled: Boolean) {
        catchCloneCrash = enabled
    }

    @JvmStatic
    fun shouldRedirectAndroidQGameMetadata(): Boolean = Build.VERSION.SDK_INT == 29 && androidQMetadataRedirect

    /** Collects the apk path -> app map used by [onOpenDexFileNative]. */
    @JvmStatic
    fun startDexOverride() {
        val installed = VirtualCore.gCore.getInstalledApps() ?: return
        dexOverrides = HashMap(installed.size)
        for (info in installed)
        {
            try {
                dexOverrides[File(info.appPath).canonicalPath] = info
            } catch (_: IOException) {
            }
        }
        val dir = locationProviderDir()
        val extracted = File(dir, "extracted.jar")
        if (extracted.exists() && File(dir, "classes.dex").exists()) {
            dexOverrides["/system/framework/com.android.location.provider.jar"] = extracted.path
        }
    }

    private fun locationProviderDir(): File {
        val framework = File(VEnvironment.virtualDir, "framework")
        VEnvironment.getOrCreatePath(framework)
        val dir = File(framework, "com.android.location.provider")
        VEnvironment.getOrCreatePath(dir)
        return dir
    }

    // exempt()/exemptAll() are stubs in the original (always false)
    @JvmStatic
    fun exempt(vararg prefixes: String): Boolean = false

    @JvmStatic
    fun exemptAll(): Boolean = exempt("L")

    @JvmStatic
    fun unseal(): Int = if (Build.VERSION.SDK_INT >= 28 && !exemptAll()) -21 else 0

    // ---------- callbacks called FROM native code (looked up by name) ----------
    @JvmStatic
    fun notifyNativeCrash(signal: Int) {
        Log.d(TAG, "notifyNativeCrash: signal = $signal")
        val exc = Exception("native crash: $signal in pid: ${Process.myPid()}")
        Log.d(TAG, Log.getStackTraceString(exc))
        try {
            val intent = Intent("appclone.intent.action.SHOW_CRASH_DIALOG")
            intent.putExtra("package", VClientImpl.get().getCurrentPackage())
            VirtualCrashTransport.writeToIntent(intent, exc)
            intent.putExtra("tag", 47414)
            appContext.sendBroadcast(intent)
            Thread.sleep(500L)
        } catch (_: Exception) {
        }
        Process.killProcess(Process.myPid())
        System.exit(0)
    }

    @JvmStatic
    fun onGetCallingUid(originalUid: Int): Int {
        val callingPid = Binder.getCallingPid()
        if (callingPid != 0) {
            if (callingPid == Process.myPid()) return VClientImpl.get().getBaseVUid()
            if (callingPid == VirtualCore.gCore.systemPid) return 1000
            try {
                val virtualUid = try {
                    VActivityManager.instance.getService().getVirtualUidByPid(callingPid)
                } catch (_: RemoteException) {
                    -10000
                }
                if (virtualUid != -10000) return virtualUid
            } catch (th: Throwable) {
                Log.d(TAG, th.toString())
            }
        }
        return originalUid
    }

    @JvmStatic
    fun onGetUid(uid: Int): Int = VClientImpl.get().getBaseVUid()

    @JvmStatic
    fun onTest(value: Int): Int = value

    /** Return 0 to let kill(pid, sig) happen, non-zero to swallow it. */
    @JvmStatic
    fun onKillProcess(pid: Int, sig: Int): Int {
        Log.d(TAG, "killProcess: pid = $pid, signal = $sig." )
        if (pid == Process.myPid()) {
            val stack = Log.getStackTraceString(Throwable())
            Log.e(TAG, stack)
            val isGms = GmsSupport.isGmsPackage(VClientImpl.get().getCurrentPackage())
            if (isGms) {
                val core = VirtualCore.gCore
                val context: Context =appContext
                val vPid = VClientImpl.get().getVPid()
                try {
                    val intent = Intent()
                    intent.setClassName(core.hostPackageName!!, VASettings.stubServiceClassName + "\$S" + vPid)
                    context.stopService(intent)
                } catch (e: Exception) {
                    Log.d("StubService", Log.getStackTraceString(e))
                }
            }
            if (stack.contains("dynamite_dynamiteloader")) {
                if (isGms) return 0
                Log.e(TAG, "escape this time")
                return 1
            }
        }
        return if (VClientImpl.get().getCurrentPackage() != "com.imo.android.imoim" || pid == Process.myPid()) 0 else 1
    }

    /** strArr[0] = dex/jar path, strArr[1] = output path; may be rewritten for cloned apps. */
    @JvmStatic
    fun onOpenDexFileNative(args: Array<String>) {
        val source = args[0]
        try {
            val entry = dexOverrides[File(source).canonicalPath]
            if (entry is InstalledAppInfo) {
                if (!entry.gmsEnabled) {
                    args[1] = VEnvironment.makeDexFile(entry.packageName!!).path
                }
            } else if (entry is String && source.startsWith("/system/framework/com.android.location.provider")) {
                val dir = locationProviderDir()
                args[0] = File(dir, "extracted.jar").path
                args[1] = File(dir, "classes.dex").path
            }
        } catch (_: IOException) {
        }
    }

    /** Returns Stub Parcel with the host uid/package patched into an AttributionSource, or null. */
    @JvmStatic
    private fun buildHostAudioAttributionParcel(obj: Any?): Any? {
        val core = VirtualCore.gCore
        val uid = core.uid
        val pkg = core.hostPackageName
        if (Build.VERSION.SDK_INT < 31 || obj !is Parcel) return null
        val position = obj.dataPosition()
        var result: Parcel? = null
        try {
            obj.setDataPosition(0)
            val creator = AttributionSourceState.TYPE?.getField("CREATOR")?.get(null)
            if (creator is Parcelable.Creator<*>) {
                val state = creator.createFromParcel(obj)
                if (state is Parcelable && AttributionSourceState.TYPE?.isInstance(state) ?: false) {
                    AttributionSourceState.uid[state] = uid
                    AttributionSourceState.packageName[state] = pkg
                    result = Parcel.obtain()
                    state.writeToParcel(result, 0)
                    result.setDataPosition(0)
                    return result
                }
            }
            return null
        } catch (_: Throwable) {
            result?.recycle()
            return null
        } finally {
            obj.setDataPosition(position)
        }
    }

    @JvmStatic
    private fun recycleHostAudioAttributionParcel(obj: Any?) {
        (obj as? Parcel)?.recycle()
    }
}
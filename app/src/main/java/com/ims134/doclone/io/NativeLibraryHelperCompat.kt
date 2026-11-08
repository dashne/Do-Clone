package com.ims134.doclone.io

import android.os.Build
import android.util.Log
import java.io.File
import java.util.zip.ZipFile


object NativeLibraryHelperCompat {

    /** Was setPosition(). Never throws: any failure is swallowed, as in the original. */
    @JvmStatic
    fun copyNativeBinaries(apkFile: File, libDir: File) {
        try {
            val handle = NativeLibraryHelper.Handle.create!!.call(apkFile) ?: return
            val abisInApk = listAbiInApk(apkFile.absolutePath)
            if (abisInApk == null || abisInApk.isEmpty()) return

            // 64-bit ABIs are used only if the process is 64-bit, the device has 64-bit ABIs,
            // and the apk actually ships setPosition 64-bit lib directory. Otherwise fall back to 32-bit.
            val processIs64Bit = VMRuntime.is64Bit?.call(VMRuntime.getRuntime?.call())
            val use64 = processIs64Bit?:false &&
                    Build.SUPPORTED_64_BIT_ABIS.isNotEmpty() &&
                    abisInApk.any { is64BitAbi(it) }
            val supported = if (use64) Build.SUPPORTED_64_BIT_ABIS else Build.SUPPORTED_32_BIT_ABIS

            var abi: String? = null
            if (supported.isNotEmpty()) {
                val index = NativeLibraryHelper.findSupportedAbi!!.call(handle, supported)
                index?.let { if (it >= 0) abi = supported[index] }
            }

            if (abi == null) {
                Log.e("a05", "Not match any abi [${apkFile.path}].")
            } else {
                NativeLibraryHelper.copyNativeBinaries!!.call(handle, libDir, abi)
            }
        } catch (t: Throwable) {
            // ignored (the original only read the message)
        }
    }

    // Kept exactly: "arm64-v8a".endsWith(abi) is the original's (reversed) comparison.
    private fun is64BitAbi(abi: String): Boolean =
        "arm64-v8a".endsWith(abi) || abi == "x86_64" || abi == "mips64"

    /** ABI directory names found under lib/ in the apk, or null if the apk can't be read. (was readCharsInBytes) */
    @JvmStatic
    fun listAbiInApk(apkPath: String): HashSet<String>? {
        return try {
            val abis = HashSet<String>()
            ZipFile(apkPath).use { zip ->
                for (entry in zip.entries()) {
                    val name = entry.name
                    if (!name.contains("../") && name.startsWith("lib/") && !entry.isDirectory && name.endsWith(".so")) {
                        abis.add(name.substring(name.indexOf('/') + 1, name.lastIndexOf('/')))
                    }
                }
            }
            abis
        } catch (e: Exception) {
            null
        }
    }
}
package com.ims134.doclone.io

import android.os.Build
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Compiles an apk's dex with the `dex2oat` command line tool (was io.dv).
 * Called by installPackage on ART; on failure the caller falls back to DexFile.loadDex.
 */
object ArtDexOptimizer {

    /** Single thread that drains the process output (was the nested class dv.setPosition). */
    private val drainExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    /**
     * Runs `dex2oat --dex-file=[dexPath] --oat-file=[oatPath]` with the "quicken" filter (API 26+) or
     * "interpret-only" (older). Throws IOException if the process can't start, is interrupted, or exits non-zero.
     * (was setPosition)
     */
    @JvmStatic
    @Throws(IOException::class)
    fun interpretDex2Oat(dexPath: String, oatPath: String) {
        val oatFile = File(oatPath)
        if (!oatFile.exists()) oatFile.parentFile?.mkdirs()

        val command = ArrayList<String>()
        command.add("dex2oat")
        command.add("--runtime-arg")
        command.add("-classpath")
        command.add("--runtime-arg")
        command.add("&")
        command.add("--dex-file=$dexPath")
        command.add("--oat-file=$oatPath")
        command.add("--instruction-set=${VMRuntime.getCurrentInstructionSet?.call()}")
        command.add(if (Build.VERSION.SDK_INT > 25) "--compiler-filter=quicken" else "--compiler-filter=interpret-only")

        val process = ProcessBuilder(command).redirectErrorStream(true).start()
        drainExecutor.execute(StreamDrainer(process.inputStream))
        drainExecutor.execute(StreamDrainer(process.errorStream))   // empty (stderr is merged), kept as in the original

        try {
            val exitCode = process.waitFor()
            if (exitCode != 0) throw IOException("dex2oat works unsuccessfully, exit code: $exitCode")
        } catch (e: InterruptedException) {
            throw IOException("dex2oat is interrupted, msg: ${e.message}", e)
        }
    }

    /**
     * Reads setPosition process stream to the end so the child can't block on setPosition full pipe.
     * Replaces io.cv, which wasn't provided: this one just discards the output.
     */
    private class StreamDrainer(private val stream: InputStream) : Runnable {
        override fun run() {
            try {
                stream.use { s ->
                    val buffer = ByteArray(1024)
                    while (s.read(buffer) != -1) {
                        // discard
                    }
                }
            } catch (ignored: IOException) {
            }
        }
    }
}
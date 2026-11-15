package com.ims134.doclone.io

import android.content.Intent
import android.util.Log
import java.lang.reflect.Modifier

/**
 * Carries Stub Throwable from Stub cloned process to the host inside an Intent (class, message, stack frames, stack text).
 * Old jadx names: Stub = hasLineStartingWith, b = createThrowable, c = writeToIntent, d = readFromIntent,
 * e = readRemoteStackText (callers that used u09.Stub/b/c/d/e must use the new names).
 */
object VirtualCrashTransport {

    private const val MAX_FRAMES = 128
    private const val MAX_CLASS = 1024
    private const val MAX_MESSAGE = 16384
    private const val MAX_STACK_TEXT = 65536
    private const val MAX_REMOTE_TEXT = 24576

    /** true when some line of [text] (leading tabs skipped) starts with [prefix]. */
    @JvmStatic
    fun hasLineStartingWith(text: String, prefix: String): Boolean {
        var i = 0
        while (i < text.length) {
            while (i < text.length && text[i] == '\t') i++
            if (text.regionMatches(i, prefix, 0, prefix.length)) return true
            val nl = text.indexOf('\n', i)
            if (nl < 0) break
            i = nl + 1
        }
        return false
    }

    /** Rebuilds Stub Throwable from its class name: only public, non-abstract java./android./dalvik. Throwables are instantiated. */
    @JvmStatic
    fun createThrowable(className: String?, message: String?): Throwable {
        var th: Throwable? = null
        if (className != null && (className.startsWith("java.") || className.startsWith("android.") || className.startsWith("dalvik."))) {
            try {
                val cls = Class.forName(className, false, null)
                if (Throwable::class.java.isAssignableFrom(cls) && !Modifier.isAbstract(cls.modifiers) && Modifier.isPublic(cls.modifiers)) {
                    th = try {
                        cls.getConstructor(String::class.java).newInstance(message) as Throwable
                    } catch (_: NoSuchMethodException) {
                        cls.getConstructor().newInstance() as Throwable
                    }
                }
            } catch (_: Throwable) {
            }
        }
        if (th != null) return th

        var text = if (className.isNullOrEmpty()) "Virtual process exception" else className
        if (!message.isNullOrEmpty()) text = "$text: $message"
        return RuntimeException(text)
    }

    /** Writes [th] into [intent]. */
    @JvmStatic
    fun writeToIntent(intent: Intent, th: Throwable?) {
        if (th == null) return
        intent.removeExtra("exception")
        intent.putExtra("virtual_crash_class", th.javaClass.name)

        var message: String? = try {
            th.message
        } catch (_: Throwable) {
            null
        }
        if (message != null && message.length > MAX_MESSAGE) message = message.substring(0, MAX_MESSAGE)
        intent.putExtra("virtual_crash_message", message)

        var stackText: String? = Log.getStackTraceString(th)
        if (stackText != null && stackText.length > MAX_STACK_TEXT) stackText = stackText.substring(0, MAX_STACK_TEXT)
        intent.putExtra("virtual_crash_stack_text", stackText)

        val trace: Array<StackTraceElement>? = th.stackTrace
        val length = trace?.size ?: 0
        intent.putExtra("virtual_crash_stack_frame_count", length)

        val count = minOf(length, MAX_FRAMES)
        val classes = arrayOfNulls<String>(count)
        val methods = arrayOfNulls<String>(count)
        val files = arrayOfNulls<String>(count)
        val lines = IntArray(count)
        for (i in 0 until count) {
            val e = trace!![i]
            classes[i] = e.className
            methods[i] = e.methodName
            files[i] = e.fileName
            lines[i] = e.lineNumber
        }
        intent.putExtra("virtual_crash_stack_classes", classes)
        intent.putExtra("virtual_crash_stack_methods", methods)
        intent.putExtra("virtual_crash_stack_files", files)
        intent.putExtra("virtual_crash_stack_lines", lines)
    }

    /** Reads the Throwable written by [c]. Never throws. */
    @JvmStatic
    fun readFromIntent(intent: Intent): Throwable {
        try {
            var cls = intent.getStringExtra("virtual_crash_class")
            if (cls != null && cls.length > MAX_CLASS) cls = cls.substring(0, MAX_CLASS)
            var message = intent.getStringExtra("virtual_crash_message")
            if (message != null && message.length > MAX_MESSAGE) message = message.substring(0, MAX_MESSAGE)

            val result = createThrowable(cls, message)
            val classes = intent.getStringArrayExtra("virtual_crash_stack_classes")
            val methods = intent.getStringArrayExtra("virtual_crash_stack_methods")
            val files = intent.getStringArrayExtra("virtual_crash_stack_files")
            val lines = intent.getIntArrayExtra("virtual_crash_stack_lines")

            val count = minOf(
                MAX_FRAMES,
                if (classes == null || methods == null || files == null || lines == null) 0
                else minOf(minOf(classes.size, methods.size), minOf(files.size, lines.size))
            )
            if (count <= 0) {
                if (!cls.isNullOrEmpty()) {
                    result.stackTrace = arrayOf(StackTraceElement(cls, "<remote>", null, -1))
                }
                return result
            }
            result.stackTrace = Array(count) { StackTraceElement(classes!![it], methods!![it], files!![it], lines!![it]) }
            return result
        } catch (e: LinkageError) {
            return decodeFailure(e)
        } catch (e: RuntimeException) {
            return decodeFailure(e)
        }
    }

    private fun decodeFailure(e: Throwable): RuntimeException {
        val ex = RuntimeException("Unable to decode virtual process exception: " + e.javaClass.name)
        ex.stackTrace = arrayOf(StackTraceElement(VirtualCrashTransport::class.java.name, "readException", "VirtualCrashTransport.java", -1))
        return ex
    }

    /**
     * The remote stack text when it carries more than the structured frames do (frames cut at 128, or Stub
     * "Caused by:" / "Suppressed:" section), otherwise null. Very long text keeps its last 24576 chars.
     */
    @JvmStatic
    fun readRemoteStackText(intent: Intent): String? {
        try {
            val text = intent.getStringExtra("virtual_crash_stack_text")
            val classes = intent.getStringArrayExtra("virtual_crash_stack_classes")
            val methods = intent.getStringArrayExtra("virtual_crash_stack_methods")
            val files = intent.getStringArrayExtra("virtual_crash_stack_files")
            val lines = intent.getIntArrayExtra("virtual_crash_stack_lines")

            val count = minOf(
                MAX_FRAMES,
                if (classes == null || methods == null || files == null || lines == null) 0
                else minOf(minOf(classes.size, methods.size), minOf(files.size, lines.size))
            )
            val frameCount = intent.getIntExtra("virtual_crash_stack_frame_count", count)
            if (text.isNullOrEmpty()) return null
            if (count != 0 && frameCount <= count && !hasLineStartingWith(text, "Caused by:") && !hasLineStartingWith(text, "Suppressed:")) return null
            if (text.length <= MAX_REMOTE_TEXT) return text
            return "[leading remote stack truncated]\n" + text.substring(text.length - MAX_REMOTE_TEXT)
        } catch (_: LinkageError) {
            return null
        } catch (_: RuntimeException) {
            return null
        }
    }
}
package com.ims134.doclone

import android.os.Build
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ims134.doclone.clone.NativeEngine
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.thread

/**
 * Instrumented test (needs a device/emulator, because it loads libva-native.so).
 * Put it in app/src/androidTest/java/.../NativeEngineTest.kt
 * (if your test file is NOT in package com.ims134.doclone.clone, add:  import com.ims134.doclone.clone.NativeEngine)
 *
 * build.gradle:
 *   defaultConfig { testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner" }
 *   androidTestImplementation "androidx.test.ext:junit:1.1.5"
 *   androidTestImplementation "androidx.test:runner:1.5.2"
 *
 * NOTE: nativeHook() installs process-wide libc hooks that can NOT be removed and redirect rules live until the
 * process dies. Every test therefore uses its own directories ("v_<name>" = virtual, never created / "r_<name>" = real),
 * and the hooks are installed once in @BeforeClass.
 *
 * Tests marked [HOOK] go through a libc hook. If one of them fails, that hook is not catching that call
 * on this Android version - that is a real finding, not a test bug.
 */
@RunWith(AndroidJUnit4::class)
class NativeEngineTest {

    companion object {
        private val ctx get() = InstrumentationRegistry.getInstrumentation().targetContext
        private lateinit var base: File

        @JvmStatic
        @BeforeClass
        fun setUpClass() {
            base = File(ctx.filesDir, "ne_test_${System.nanoTime()}").apply { mkdirs() }
            NativeEngine.hook(0)          // libc hooks (+ UnixFileSystem hooks on API 37+)
        }
    }

    private fun dir(name: String) = File(base, name).apply { mkdirs() }

    /** virtual dir (does not exist on disk) -> real dir (exists). */
    private fun pair(name: String): Pair<File, File> {
        val virtual = File(base, "v_$name")
        val real = dir("r_$name")
        NativeEngine.redirectDirectory(virtual.path, real.path)
        return virtual to real
    }

    private fun write(f: File, text: String) = FileOutputStream(f).use { it.write(text.toByteArray()) }

    // ================= 1. library + path table (no libc involved) =================

    @Test
    fun nativeLibraryLoadsAndNativesAreRegistered() {
        // an unregistered native would throw UnsatisfiedLinkError here
        NativeEngine.getRedirectedPath("/nonexistent/x")
        NativeEngine.restoreRedirectedPath("/nonexistent/x")
    }

    @Test
    fun directoryRedirectQueryAndRestore() {
        val from = File(base, "v_table1").path
        val to = File(base, "r_table1").path
        NativeEngine.redirectDirectory(from, to)

        assertEquals("$to/a/b.txt", NativeEngine.getRedirectedPath("$from/a/b.txt"))
        assertEquals("$from/a/b.txt", NativeEngine.restoreRedirectedPath("$to/a/b.txt"))
        assertEquals("/other/path", NativeEngine.getRedirectedPath("/other/path"))
    }

    @Test
    fun fileRedirectIsExactMatch() {
        val from = File(base, "v_table2.txt").path
        val to = File(base, "r_table2.txt").path
        NativeEngine.redirectFile(from, to)

        assertEquals(to, NativeEngine.getRedirectedPath(from))
        assertEquals(from, NativeEngine.restoreRedirectedPath(to))
        // every rule is also kept in the prefix map (as in the IDA code): "<from>.bak" -> "<to>.bak"
        assertEquals("$to.bak", NativeEngine.getRedirectedPath("$from.bak"))
        val other = File(base, "other_file.txt").path
        assertEquals(other, NativeEngine.getRedirectedPath(other))
    }

    @Test
    fun pathIsCanonicalizedBeforeMatching() {
        val from = File(base, "v_table3").path
        val to = File(base, "r_table3").path
        NativeEngine.redirectDirectory(from, to)

        assertEquals("$to/x.txt", NativeEngine.getRedirectedPath("$from//sub/../x.txt"))
    }

    @Test
    fun longestDirectoryRuleWins() {
        val outer = File(base, "v_table4").path
        val inner = "$outer/inner"
        val rOuter = File(base, "r_table4_outer").path
        val rInner = File(base, "r_table4_inner").path
        NativeEngine.redirectDirectory(outer, rOuter)
        NativeEngine.redirectDirectory(inner, rInner)

        assertEquals("$rInner/f", NativeEngine.getRedirectedPath("$inner/f"))
        assertEquals("$rOuter/g", NativeEngine.getRedirectedPath("$outer/g"))
    }

    @Test
    fun whiteListIsNeverRedirected() {
        val from = File(base, "v_table5").path
        NativeEngine.redirectDirectory(from, File(base, "r_table5").path)
        NativeEngine.whiteList("$from/keep")

        assertEquals("$from/keep/a.txt", NativeEngine.getRedirectedPath("$from/keep/a.txt"))
        assertTrue(NativeEngine.getRedirectedPath("$from/other.txt").contains("r_table5"))
    }

    // ================= 2. libc hooks, end to end =================

    /** [HOOK] open / openat */
    @Test
    fun libcOpenIsRedirected() {
        val (virtual, real) = pair("open")
        write(File(virtual, "hello.txt"), "hi")

        val onDisk = File(real, "hello.txt")
        assertTrue("file must land in the redirected directory", onDisk.exists())
        assertEquals("hi", onDisk.readText())
        assertEquals("hi", File(virtual, "hello.txt").readText())
    }

    /** [HOOK] mkdirat */
    @Test
    fun libcMkdirIsRedirected() {
        val (virtual, real) = pair("mkdir")
        assertTrue(File(virtual, "sub/deeper").mkdirs())
        assertTrue(File(real, "sub/deeper").isDirectory)
    }

    /** [HOOK] fstatat64 (stat) */
    @Test
    fun libcStatIsRedirected() {
        val (virtual, real) = pair("stat")
        write(File(real, "s.txt"), "12345")           // created on the REAL side only

        val st = Os.stat(File(virtual, "s.txt").path)  // must find it through the redirect
        assertEquals(5L, st.st_size)
    }

    /** [HOOK] faccessat */
    @Test
    fun libcAccessIsRedirected() {
        val (virtual, real) = pair("access")
        write(File(real, "a.txt"), "x")

        assertTrue(Os.access(File(virtual, "a.txt").path, OsConstants.R_OK))
        // Os.access() does not return false for a missing file, it throws ErrnoException(ENOENT)
        try {
            Os.access(File(virtual, "missing.txt").path, OsConstants.F_OK)
            Assert.fail("access() on a missing file must throw")
        } catch (e: ErrnoException) {
            assertEquals(OsConstants.ENOENT, e.errno)
        }
    }

    /** [HOOK] fchmodat */
    @Test
    fun libcChmodIsRedirected() {
        val (virtual, real) = pair("chmod")
        write(File(real, "m.txt"), "x")

        Os.chmod(File(virtual, "m.txt").path, 0b110_000_000)   // 0600
        assertEquals(0b110_000_000, Os.stat(File(real, "m.txt").path).st_mode and 0b111_111_111)
    }

    /** [HOOK] renameat / renameat2 */
    @Test
    fun libcRenameIsRedirected() {
        val (virtual, real) = pair("rename")
        write(File(real, "a.txt"), "x")

        assertTrue(File(virtual, "a.txt").renameTo(File(virtual, "b.txt")))
        assertTrue(File(real, "b.txt").exists())
        assertFalse(File(real, "a.txt").exists())
    }

    /** [HOOK] unlinkat */
    @Test
    fun libcUnlinkIsRedirected() {
        val (virtual, real) = pair("unlink")
        write(File(real, "d.txt"), "x")

        assertTrue(File(virtual, "d.txt").delete())
        assertFalse(File(real, "d.txt").exists())
    }

    /** [HOOK] symlinkat + readlinkat */
    @Test
    fun libcSymlinkAndReadlinkAreRedirected() {
        val (virtual, real) = pair("symlink")
        Os.symlink("some_target", File(virtual, "lnk").path)

        assertEquals("some_target", Os.readlink(File(real, "lnk").path))      // created in the real dir
        assertEquals("some_target", Os.readlink(File(virtual, "lnk").path))   // readable through the virtual path
    }

    /** the whitelist also works at libc level: whitelisted paths go to the real place, not to the redirect */
    @Test
    fun whiteListWorksAtLibcLevel() {
        // the parent must really exist on disk: a whitelisted path is NOT redirected, so mkdir/open of
        // "<parent>/keep" happens at the real location and needs a real parent
        val parent = dir("wl_root")
        val real = dir("r_wl")
        NativeEngine.redirectDirectory(parent.path, real.path)
        NativeEngine.whiteList(File(parent, "keep").path)

        assertTrue(File(parent, "keep").mkdirs())                   // whitelisted -> really created under wl_root
        write(File(parent, "keep/w.txt"), "w")                      // whitelisted -> really written under wl_root

        assertTrue(File(parent, "keep/w.txt").exists())
        assertFalse("whitelisted path must not be redirected", File(real, "keep/w.txt").exists())
        // a path in the same directory that is NOT whitelisted still goes to the redirect
        write(File(parent, "other.txt"), "o")
        assertTrue(File(real, "other.txt").exists())
    }

    /** a file rule at libc level */
    @Test
    fun fileRuleWorksAtLibcLevel() {
        val virtualFile = File(base, "v_file_rule.txt")
        val realFile = File(base, "r_file_rule.txt")
        NativeEngine.redirectFile(virtualFile.path, realFile.path)

        write(virtualFile, "f")
        assertTrue(realFile.exists())
        assertEquals("f", realFile.readText())
    }

    /** hooks must work from other threads too */
    @Test
    fun hooksWorkFromOtherThreads() {
        val (virtual, real) = pair("threads")
        val errors = ConcurrentLinkedQueue<Throwable>()
        val threads = (0 until 8).map { n ->
            thread {
                try {
                    write(File(virtual, "t$n.txt"), "n$n")
                } catch (t: Throwable) {
                    errors.add(t)
                }
            }
        }
        threads.forEach { it.join() }

        assertTrue("thread errors: $errors", errors.isEmpty())
        for (n in 0 until 8) assertEquals("n$n", File(real, "t$n.txt").readText())
    }

    // ================= 3. read-only list =================

    /** [HOOK] unlinkat / renameat(2) / fchmodat / fchownat in a read-only path fail */
    @Test
    fun readOnlyBlocksDeleteRenameAndChmod() {
        val ro = dir("r_readonly")
        val f = File(ro, "locked.txt").apply { writeText("x") }
        NativeEngine.readOnly(ro.path)

        assertFalse("delete must fail", f.delete())
        assertFalse("rename must fail", f.renameTo(File(ro, "renamed.txt")))
        assertTrue(f.exists())
        assertEquals("x", f.readText())
    }

    // ================= 4. reflection natives =================

    @Test
    fun reflectionNativesFindHiddenMembers() {
        val m = NativeEngine.nativeGetDeclaredMethod(Class.forName("dalvik.system.VMRuntime"), "getRuntime", arrayOf())
        assertNotNull(m)
        assertEquals("getRuntime", m!!.name)

        assertNotNull(NativeEngine.nativeGetMethod(Class.forName("android.app.ActivityThread"), "currentActivityThread", arrayOf()))
        assertNotNull(NativeEngine.nativeGetDeclaredField(Class.forName("android.app.ActivityThread"), "sCurrentActivityThread"))
    }

    @Test
    fun reflectionNativesReturnNullAndKeepRunningWhenMissing() {
        assertNull(NativeEngine.nativeGetDeclaredMethod(String::class.java, "noSuchMethod_xyz", arrayOf()))
        assertNull(NativeEngine.nativeGetDeclaredField(String::class.java, "noSuchField_xyz"))
        // the pending exception was cleared: the next call still works
        assertNotNull(NativeEngine.nativeGetDeclaredMethod(String::class.java, "length", arrayOf()))
    }

    // ================= 5. signature check =================

    /**
     * 1 = the signing certificate is in kAllowedSignatureSha1[] (debug SHA-1 DC23555A...).
     * A release build signed with another key returns -2 until its SHA-1 is added; adapt this assert for such builds.
     */
    @Test
    fun nativeCheckAcceptsTheDebugCertificate() {
        assertEquals(1, NativeEngine.nativeCheck(ctx))
    }

    // ================= 6. plain Kotlin helpers =================

    @Test
    fun kotlinSideHelpers() {
        assertEquals(42, NativeEngine.onTest(42))
        assertFalse(NativeEngine.exemptAll())
        assertEquals(if (Build.VERSION.SDK_INT >= 28) -21 else 0, NativeEngine.unseal())
        assertFalse(NativeEngine.shouldRedirectAndroidQGameMetadata())   // hook(0): the API 29 flag is not set
        NativeEngine.setCatchCloneCrash(false)
    }
}
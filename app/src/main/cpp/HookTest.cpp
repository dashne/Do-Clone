// HookTest.cpp - proves ShadowHook works: hooks libc open(), calls it, checks the hook fired.
#include <jni.h>
#include <dlfcn.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdarg.h>
#include <atomic>
#include "HookLibc.h"

static int (*orig_open)(const char*, int, ...) = nullptr;
static std::atomic<int> g_hits{0};
static bool g_installed = false;

static int new_open(const char* path, int flags, ...) {
    mode_t mode = 0;
    if (flags & O_CREAT) {
        va_list ap;
        va_start(ap, flags);
        mode = (mode_t) va_arg(ap, int);
        va_end(ap);
    }
    g_hits++;
    __android_log_print(ANDROID_LOG_INFO, VA_TAG, "HOOK open(%s, 0x%x)", path ? path : "(null)", flags);
    return orig_open(path, flags, mode);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_ims134_doclone_clone_NativeEngine_nativeTestHook(JNIEnv* env, jclass) {
    char buf[160];
    if (!g_installed) {
        hookLibcSymbol("open", (void*) new_open, (void**) &orig_open);   // UNIQUE mode: hook only once
        g_installed = true;
    }
    if (orig_open == nullptr) {
        return env->NewStringUTF("FAIL: hook not installed (see logcat tag VA-Native)");
    }
    // call libc open() through its real address so the inline hook is guaranteed to be hit
    auto fn = (int (*)(const char*, int, ...)) dlsym(RTLD_DEFAULT, "open");
    int before = g_hits.load();
    int fd = fn ? fn("/proc/version", O_RDONLY) : -1;
    if (fd >= 0) close(fd);
    snprintf(buf, sizeof(buf), "%s: hits %d -> %d, fd=%d",
             g_hits.load() > before ? "OK" : "FAIL (hook not hit)", before, g_hits.load(), fd);
    return env->NewStringUTF(buf);
}

// HookLibc.h - replaces hookLibcSymbol (sub_70118) + MSHookFunction/A64HookFunction/relocate_block/
// make_hook_memory_rwx/flush_code_cache/cas4_sync with ShadowHook (arm64 + armv7).
#pragma once
#include <android/log.h>
#include <shadowhook.h>

#define VA_TAG "VA-Native"

// Call once, from JNI_OnLoad (before any hook is installed).
static inline bool initHookEngine() {
    int r = shadowhook_init(SHADOWHOOK_MODE_UNIQUE, false);
    if (r != 0) {
        __android_log_print(ANDROID_LOG_ERROR, VA_TAG, "shadowhook_init failed: %s",
                            shadowhook_to_errmsg(r));
        return false;
    }
    return true;
}

// Same signature/behaviour as the original: hook `symbolName` in libc.so,
// put the original function pointer into *originSymbol.
static inline void hookLibcSymbol(const char* symbolName, void* fakeSymbol, void** originSymbol) {
    void* stub = shadowhook_hook_sym_name("libc.so", symbolName, fakeSymbol, originSymbol);
    if (stub == nullptr) {
        int e = shadowhook_get_errno();
        __android_log_print(ANDROID_LOG_ERROR, VA_TAG, "Error: unable to hook %s: %s",
                            symbolName, shadowhook_to_errmsg(e));
    }
}
// Recovered from IOUniformer::query (match_redirected_path), restore, redirect, whiteList, canonicalize_filename and the JNI wrappers.
// The maps are std::map<std::string, std::string>: first = original path, second = redirected path.
// Names of the globals are guesses (VirtualApp calls them IORedirectMap / RootIORedirectMap).
#include <map>
#include <list>
#include <vector>
#include <string>
#include <cstring>
#include <cstdlib>
#include <pthread.h>
#ifndef NO_JNI
#include <jni.h>
#endif

static std::map<std::string, std::string> IORedirectMap;      // was at 0xF8950: file rules (exact match)
static std::map<std::string, std::string> RootIORedirectMap;  // was at 0xF8920: directory rules (prefix match)
static std::list<std::string> WhiteList;                       // was at 0xF8938: paths that are never redirected (prefix match)
static pthread_rwlock_t rwlock = PTHREAD_RWLOCK_INITIALIZER;   // was at 0xF8968

// Cleans the text of a path (does not touch the file system): "//" becomes "/", and ".." removes the directory before it.
// Returns a malloc'd string (at most 4095 chars), or nullptr for a null path.
// Quirks kept from the original: a single "." is NOT removed ("/a/./b" stays), and any two dots in a row, even inside a name
// ("file..txt"), act like "..". A relative path stays relative.
static char *canonicalize_filename(const char *filename) {
    char buf[4096];
    memset(buf, 0, sizeof(buf));
    if (filename == nullptr) {
        return nullptr;
    }
    int out = 0;          // length of the result so far (was v4)
    int last_slash = -1;  // index of the last '/' in the result (was v5)
    int prev_slash = -1;  // index of the '/' before that one (was v6)
    int cur = 0;
    for (int i = 0; i < 4096; i++) {
        int prev = cur;
        cur = (unsigned char) filename[i];
        if (cur == '.') {
            if (prev == '.') {
                // ".." : drop everything from the previous-but-one '/' (or everything if there is none)
                int keep = prev_slash < 0 ? 0 : prev_slash;
                if (out > keep) {
                    memset(buf + keep, 0, out - keep);
                    out = keep;
                }
                buf[out] = 0;
                char *p = strrchr(buf, '/');
                prev_slash = -1;
                if (p == nullptr) {
                    last_slash = -1;
                } else {
                    last_slash = (int) (p - buf);
                    buf[last_slash] = ' ';
                    char *q = strrchr(buf, '/');
                    prev_slash = q == nullptr ? -1 : (int) (q - buf);
                    buf[last_slash] = '/';
                }
                continue;
            }
            buf[out++] = '.';
        } else if (cur == '/') {
            if (prev == '/') {
                continue;
            }
            buf[out] = '/';
            prev_slash = last_slash;
            last_slash = out;
            out++;
        } else {
            if (cur == 0) {
                return strndup(buf, 0xFFF);
            }
            buf[out++] = (char) cur;
        }
    }
    return strndup(buf, 0xFFF);
}

// Finds where a path of the app really goes.
// Returns the input pointer itself when nothing matches, otherwise a malloc'd string the caller must free.
static const char *match_redirected_path(const char *path) {
    if (path == nullptr) {
        return nullptr;
    }
    char *canonical_c = canonicalize_filename(path);
    if (canonical_c == nullptr) {
        return path;
    }
    std::string canonical(canonical_c);
    free(canonical_c);
    if (canonical.length() < 2) {
        return path;
    }

    pthread_rwlock_rdlock(&rwlock);

    // 1. a white listed prefix of the path as given (not the canonical one): never redirected
    std::string raw(path);
    for (const std::string &prefix : WhiteList) {
        if (raw.length() >= prefix.length() && memcmp(raw.data(), prefix.data(), prefix.length()) == 0) {
            pthread_rwlock_unlock(&rwlock);
            return path;
        }
    }

    // 2. a file rule for exactly this (canonical) path
    auto exact = IORedirectMap.find(canonical);
    if (exact != IORedirectMap.end()) {
        const char *result = strdup(exact->second.c_str());
        pthread_rwlock_unlock(&rwlock);
        return result;
    }

    // 3. the directory rule with the longest original prefix of this path
    auto best = RootIORedirectMap.end();
    size_t best_len = 0;
    for (auto it = RootIORedirectMap.begin(); it != RootIORedirectMap.end(); ++it) {
        const std::string &prefix = it->first;
        if (prefix.length() > best_len &&
            canonical.length() >= prefix.length() &&
            memcmp(canonical.data(), prefix.data(), prefix.length()) == 0) {
            best = it;
            best_len = prefix.length();
        }
    }
    if (best != RootIORedirectMap.end()) {
        // redirected directory + the rest of the path
        std::string result = best->second + canonical.substr(best_len);
        const char *dup = strdup(result.c_str());
        pthread_rwlock_unlock(&rwlock);
        return dup;
    }

    pthread_rwlock_unlock(&rwlock);
    return path;
}

class IOUniformer {
public:
    // path of the app -> real path (nativeGetRedirectedPath)
    static const char *query(const char *path) { return match_redirected_path(path); }

    // real path -> path of the app (nativeRestoreRedirectedPath)
    static const char *restore(const char *redirected_path);

    // adds a rule: paths under orig_path are redirected to new_path (nativeRedirect)
    static void redirect(const char *orig_path, const char *new_path);

    // paths starting with this are never redirected (nativeWhiteList)
    static void whiteList(const char *path);
};

void IOUniformer::redirect(const char *orig_path, const char *new_path) {
    std::string orig(orig_path == nullptr ? "" : orig_path);
    std::string redirected(new_path == nullptr ? "" : new_path);

    pthread_rwlock_wrlock(&rwlock);
    // every rule goes into the directory map (an existing rule is not replaced)
    RootIORedirectMap.insert(std::make_pair(orig, redirected));
    // a rule for a directory ("/x/") is also added without the trailing character, so the directory itself is redirected too
    if (!orig.empty() && orig[orig.length() - 1] == '/') {
        std::string orig_no_slash = orig.substr(0, orig.length() - 1);
        std::string redirected_no_slash = redirected.empty() ? redirected : redirected.substr(0, redirected.length() - 1);
        IORedirectMap.insert(std::make_pair(orig_no_slash, redirected_no_slash));
    }
    pthread_rwlock_unlock(&rwlock);
}

void IOUniformer::whiteList(const char *path) {
    if (path == nullptr) {
        return;
    }
    pthread_rwlock_wrlock(&rwlock);
    WhiteList.push_back(std::string(path));
    pthread_rwlock_unlock(&rwlock);
}

const char *IOUniformer::restore(const char *redirected_path) {
    if (redirected_path == nullptr) {
        return nullptr;
    }
    std::string path(redirected_path);
    if (path.length() < 2) {
        return redirected_path;
    }

    pthread_rwlock_rdlock(&rwlock);

    // 1. a file rule whose redirected path is exactly this path
    for (auto it = IORedirectMap.begin(); it != IORedirectMap.end(); ++it) {
        if (path == it->second) {
            const char *result = strdup(it->first.c_str());
            pthread_rwlock_unlock(&rwlock);
            return result;
        }
    }

    // 2. the directory rule with the longest redirected prefix of this path
    auto best = RootIORedirectMap.end();
    size_t best_len = 0;
    for (auto it = RootIORedirectMap.begin(); it != RootIORedirectMap.end(); ++it) {
        const std::string &prefix = it->second;
        if (prefix.length() > best_len &&
            path.length() >= prefix.length() &&
            memcmp(path.data(), prefix.data(), prefix.length()) == 0) {
            best = it;
            best_len = prefix.length();
        }
    }
    if (best != RootIORedirectMap.end()) {
        // original directory + the rest of the path
        std::string result = best->first + path.substr(best_len);
        const char *dup = strdup(result.c_str());
        pthread_rwlock_unlock(&rwlock);
        return dup;
    }

    pthread_rwlock_unlock(&rwlock);
    return redirected_path;
}

#ifndef NO_JNI
// registered as NativeEngine.nativeGetRedirectedPath(String): String
static jstring query(JNIEnv *env, jclass, jstring path) {
    if (path == nullptr) {
        return nullptr;
    }
    const char *c_path = env->GetStringUTFChars(path, nullptr);
    if (c_path == nullptr) {
        return nullptr;
    }
    const char *redirected = IOUniformer::query(c_path);
    jstring result = env->NewStringUTF(redirected);
    if (redirected != nullptr && redirected != c_path) {
        free(const_cast<char *>(redirected));
    }
    env->ReleaseStringUTFChars(path, c_path);
    return result;
}

// registered as NativeEngine.nativeRestoreRedirectedPath(String): String
static jstring restore(JNIEnv *env, jclass, jstring path) {
    if (path == nullptr) {
        return nullptr;
    }
    const char *c_path = env->GetStringUTFChars(path, nullptr);
    if (c_path == nullptr) {
        return nullptr;
    }
    const char *restored = IOUniformer::restore(c_path);
    jstring result = env->NewStringUTF(restored);
    if (restored != nullptr && restored != c_path) {
        free(const_cast<char *>(restored));
    }
    env->ReleaseStringUTFChars(path, c_path);
    return result;
}

// registered as NativeEngine.nativeRedirect(String, String): void
// (this wrapper was not seen; it is rebuilt from the signature and the other wrappers)
static void redirect(JNIEnv *env, jclass, jstring orig_path, jstring new_path) {
    const char *orig = orig_path == nullptr ? nullptr : env->GetStringUTFChars(orig_path, nullptr);
    const char *redirected = new_path == nullptr ? nullptr : env->GetStringUTFChars(new_path, nullptr);
    IOUniformer::redirect(orig, redirected);
    if (orig != nullptr) env->ReleaseStringUTFChars(orig_path, orig);
    if (redirected != nullptr) env->ReleaseStringUTFChars(new_path, redirected);
}

// registered as NativeEngine.nativeWhiteList(String): void (wrapper rebuilt, not seen)
static void whiteList(JNIEnv *env, jclass, jstring path) {
    if (path == nullptr) {
        return;
    }
    const char *c_path = env->GetStringUTFChars(path, nullptr);
    if (c_path != nullptr) {
        IOUniformer::whiteList(c_path);
        env->ReleaseStringUTFChars(path, c_path);
    }
}

// ===== Step 3: connects the Kotlin class to the functions above =====
// The package of NativeEngine.kt is com.ims134.doclone.clone, so the class is:
static const char *kNativeEngineClass = "com/ims134/doclone/clone/NativeEngine";

// The original table has 11 entries, registered against com/polestar/clone/client/NativeEngine. The ones not recovered yet:
//   nativeHook "(II)V" (hook_io), nativeCheck "(Ljava/lang/Object;)I" (check_signature_sha1), nativeReadOnly "(Ljava/lang/String;)V",
//   nativeHookNative, nativeGetDeclaredMethod, nativeGetMethod, nativeGetDeclaredField.
static JNINativeMethod kMethods[] = {
        {"nativeRedirect",              "(Ljava/lang/String;Ljava/lang/String;)V", (void *) redirect},
        {"nativeGetRedirectedPath",     "(Ljava/lang/String;)Ljava/lang/String;",  (void *) query},
        {"nativeRestoreRedirectedPath", "(Ljava/lang/String;)Ljava/lang/String;",  (void *) restore},
        {"nativeWhiteList",             "(Ljava/lang/String;)V",                   (void *) whiteList},
};

extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) return -1;
    jclass cls = env->FindClass(kNativeEngineClass);
    if (cls == nullptr) return -1;
    if (env->RegisterNatives(cls, kMethods, sizeof(kMethods) / sizeof(kMethods[0])) < 0) return -1;
    return JNI_VERSION_1_6;
}
#endif

#ifdef TEST_MAIN
#include <cstdio>
static void show(const char *(*fn)(const char *), const char *in) {
    const char *r = fn(in);
    printf("  %s -> %s\n", in, r ? r : "(null)");
    if (r != in) free((void *) r);
}
int main() {
    puts("canonicalize:");
    for (const char *p : {"/a/b/../c", "//a///b/", "/a/./b", "/a/b/..", "/..", "/a/../b", "x/../y", "/sdcard/Android/data"}) {
        char *c = canonicalize_filename(p);
        printf("  %s -> %s\n", p, c ? c : "(null)");
        free(c);
    }
    IOUniformer::redirect("/sdcard/", "/data/clone/sdcard/");
    IOUniformer::redirect("/sdcard/Android/", "/data/clone/android/");
    IOUniformer::redirect("/etc/hosts", "/data/clone/hosts");
    IOUniformer::whiteList("/proc/");
    puts("query:");
    for (const char *p : {"/sdcard", "/sdcard/", "/sdcard/DCIM/a.jpg", "/sdcard/Android/data/x", "/sdcard//DCIM/../b", "/etc/hosts", "/proc/self/maps", "/other"}) show(IOUniformer::query, p);
    puts("restore:");
    for (const char *p : {"/data/clone/sdcard/DCIM/a.jpg", "/data/clone/android/data/x", "/data/clone/hosts", "/other"}) show(IOUniformer::restore, p);
}
#endif
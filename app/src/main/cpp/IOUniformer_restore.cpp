// ============================================================================================================
// IDA-EXACT VERSION. This is the original port (your first paste) with ONLY the corrections that the IDA dumps prove.
// Every correction is wrapped in  // [IDA-FIX-BEGIN #n] ... // [IDA-FIX-END #n]  (or Stub single-line  // [IDA-FIX #n]).
//
//   #21 find_name: .dynsym is searched first, then .symtab
//   #22 g_checkResult / isChecked(): hook_native exits when nativeCheck did not pass. Compiled in only with
//       -DVA_REQUIRE_SIGNATURE (the allow-list is yours; the original publisher's hashes / package list are not copied)
//   #23 JNI_OnLoad deletes the local ref of the NativeEngine class
//   #24 redirectFile: the pending exception is NOT cleared (IDA sub_70814)
//   #25 install37 follows IDA step by step (same lookups, same failure stages)
//   #26 patchAndroidVM: no null check for onGetCallingUid / onOpenDexFileNative; dalvik lib dlopen +
//       art_work_around_app_jni_bugs, used by measureNativeOffset when found
//   #27 reflection natives: outer exception is described and cleared after the worker finished
//
// KNOWN DIFFERENCES FROM THE BINARY (not provable from the dumps, kept on purpose):
//   - Class name stays com/ims134/doclone/clone/NativeEngine (IDA: com/polestar/clone/client/NativeEngine).
//   - hook_native / patchAndroidVM: the publisher package-name (base64) gate, the signature gate (see #22) and the
//     Dalvik (non-ART) branches are not ported. The port returns when isArt is false.
//   - openDexFileNative wrapper forwards all 7 arguments (IDA shows 5; the loader / elements registers are lost in
//     the decompile).
//   - AudioRecord native_setup signatures / bodies (sub_718DC, sub_71AA0, sub_71C6C) are still GUESSES (not in the dumps).
//   - The reflection natives use std::promise instead of IDA's std::thread shared state (internals sub_69098,
//     sub_6955C, sub_69D94 were not dumped); behaviour is the same.
//   - The libc hooks (new_*) are exactly the original port. Their IDA bodies were not in the dumps, so they are unchecked.
// ============================================================================================================

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
#include <cerrno>
#include <signal.h>
#include <cstdarg>
#include <fcntl.h>
#include <unistd.h>
#include <sys/syscall.h>
#include <sys/types.h>
#ifndef NO_JNI
#include <jni.h>
#endif

static std::map<std::string, std::string> IORedirectMap;      // was at 0xF8950: file rules (exact match)
static std::map<std::string, std::string> RootIORedirectMap;  // was at 0xF8920: directory rules (prefix match)
static std::list<std::string> WhiteList;                       // was at 0xF8938: paths that are never redirected (prefix match)
static pthread_rwlock_t rwlock = PTHREAD_RWLOCK_INITIALIZER;   // was at 0xF8968


// Cleans the text of Stub path (does not touch the file system): "//" becomes "/", "." components are dropped and
// ".." removes the component before it.
// Returns Stub malloc'd string (at most 4095 chars), or nullptr for Stub null path.
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


// Finds where Stub path of the app really goes.
// Returns the input pointer itself when nothing matches, otherwise Stub malloc'd string the caller must free.
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

    // 1. Stub white listed prefix of the path: never redirected
    std::string raw(path);
    for (const std::string &prefix : WhiteList) {
        if (raw.length() >= prefix.length() && memcmp(raw.data(), prefix.data(), prefix.length()) == 0) {
            pthread_rwlock_unlock(&rwlock);
            return path;
        }
    }

    // 2. Stub file rule for exactly this (canonical) path
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

static std::list<std::string> ReadOnlyList;                    // paths (prefix match) that the app may not change

// true when the path starts with Stub read-only path. (rebuilt from the call sites: the original function was not seen)
static bool isReadOnlyPath(const char *path) {
    if (path == nullptr) {
        return false;
    }
    bool result = false;
    pthread_rwlock_rdlock(&rwlock);
    for (const std::string &prefix : ReadOnlyList) {
        if (strncmp(path, prefix.c_str(), prefix.length()) == 0) {
            result = true;
            break;
        }
    }
    pthread_rwlock_unlock(&rwlock);
    return result;
}


class IOUniformer {
public:
    // path of the app -> real path (nativeGetRedirectedPath)
    static const char *query(const char *path) { return match_redirected_path(path); }

    // real path -> path of the app (nativeRestoreRedirectedPath)
    static const char *restore(const char *redirected_path);

    // adds Stub rule: paths under orig_path are redirected to new_path (nativeRedirect)
    static void redirect(const char *orig_path, const char *new_path);

    // paths starting with this are never redirected (nativeWhiteList)
    static void whiteList(const char *path);

    // paths starting with this can not be changed by the app (nativeReadOnly)
    static void readOnly(const char *path);

};

void IOUniformer::redirect(const char *orig_path, const char *new_path) {
    std::string orig(orig_path == nullptr ? "" : orig_path);
    std::string redirected(new_path == nullptr ? "" : new_path);


    pthread_rwlock_wrlock(&rwlock);
    // every rule goes into the directory map (an existing rule is not replaced)
    RootIORedirectMap.insert(std::make_pair(orig, redirected));
    // Stub rule for Stub directory ("/doTransact/") is also added without the trailing character, so the directory itself is redirected too
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

void IOUniformer::readOnly(const char *path) {
    if (path == nullptr) {
        return;
    }
    pthread_rwlock_wrlock(&rwlock);
    ReadOnlyList.push_back(std::string(path));
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

    // 1. Stub file rule whose redirected path is exactly this path
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
#include <android/log.h>
#include <dlfcn.h>
#include <sys/system_properties.h>   // [IDA-FIX #26]
#include <elf.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <future>
#include <memory>
#include <thread>
#include "HookLibc.h"
#ifdef __ANDROID__
#define ATTACH_ARG(e) (e)
#else
#define ATTACH_ARG(e) ((void **) (e))
#endif

static JavaVM *g_vm = nullptr;        // was g_vm: saved in JNI_OnLoad
static jclass g_jclass = nullptr;     // was g_jclass: global ref of NativeEngine, saved in JNI_OnLoad

// ===== hooks of libc functions: each one redirects its path arguments, then makes the real system call =====
// Holds the redirected copy of Stub path and frees it at the end of the hook (errno is kept).
namespace {
    struct Redirected {
        const char *original;
        const char *path;   // == original when nothing matched, otherwise Stub malloc'd copy
        explicit Redirected(const char *p) : original(p), path(match_redirected_path(p)) {}
        ~Redirected() {
            if (path != nullptr && path != original) {
                int saved = errno;
                free(const_cast<char *>(path));
                errno = saved;
            }
        }
        Redirected(const Redirected &) = delete;
        Redirected &operator=(const Redirected &) = delete;
    };
}

// original functions: the hooks below call the kernel directly, so these are only needed by hookLibcSymbol
static void *orig___getcwd, *orig_chdir, *orig_truncate, *orig_statfs64, *orig_chroot, *orig_kill, *orig_execve,
        *orig_fstatat64, *orig_openat, *orig_open, *orig_linkat, *orig_unlinkat, *orig_symlinkat, *orig_readlinkat,
        *orig_renameat, *orig_renameat2, *orig_mkdirat, *orig_mknodat, *orig_utimensat, *orig_fchownat,
        *orig_fchmodat, *orig_faccessat;
static int (*orig___open_2)(const char *, int) = nullptr;
static int (*orig___openat_2)(int, const char *, int) = nullptr;

// getcwd: the kernel returns the real directory, give the app the path it knows
static int new___getcwd(char *buf, size_t size) {
    int result = (int) syscall(__NR_getcwd, buf, size);
    if (result >= 0) {
        const char *restored = IOUniformer::restore(buf);
        if (restored != buf) {
            size_t length = strlen(restored) + 1;
            if (length <= size) {
                memmove(buf, restored, length);
                free(const_cast<char *>(restored));
                result = (int) length;
            } else {
                free(const_cast<char *>(restored));
                result = -1;
                errno = ERANGE;
            }
        }
    }
    return result;
}

static int new_chdir(const char *path) {
    Redirected p(path);
    return (int) syscall(__NR_chdir, p.path);
}

static int new_truncate(const char *path, off_t length) {
    Redirected p(path);
    return (int) syscall(__NR_truncate, p.path, length);
}

static int new_statfs64(const char *path, void *buf) {
    Redirected p(path);
#ifdef __NR_statfs64
    return (int) syscall(__NR_statfs64, p.path, (size_t) 88 /* sizeof(struct statfs64) */, buf);
#else
    return (int) syscall(__NR_statfs, p.path, buf);
#endif
}

static int new_chroot(const char *path) {
    Redirected p(path);
    return (int) syscall(__NR_chroot, p.path);
}

// kill: first ask the Kotlin side (NativeEngine.onKillProcess(pid, sig): Int); if it handled the call, do not kill
static int new_kill(pid_t pid, int sig) {
    JNIEnv *env = nullptr;
    g_vm->GetEnv((void **) &env, JNI_VERSION_1_4);
    g_vm->AttachCurrentThread(ATTACH_ARG(&env), nullptr);
    jmethodID method = env->GetStaticMethodID(g_jclass, "onKillProcess", "(II)I");
    if (env->CallStaticIntMethod(g_jclass, method, (jint) pid, (jint) sig) != 0) {
        return 0;
    }
    return (int) syscall(__NR_kill, pid, sig);
}

static int new_execve(const char *path, char *const argv[], char *const envp[]) {
    if (strcmp(path, "dex2oat") == 0 && envp != nullptr) {
        // original behaviour: LD_PRELOAD entries are replaced by the value of our own LD_PRELOAD
        for (char *const *e = envp; *e != nullptr; e++) {
            if (strncmp(*e, "LD_PRELOAD=", 11) == 0) {
                *const_cast<char **>(e) = getenv("LD_PRELOAD");
            }
        }
    }
    Redirected p(path);
    return (int) syscall(__NR_execve, p.path, argv, envp);
}

static int new_fstatat64(int dirfd, const char *path, void *buf, int flags) {
    Redirected p(path);
#ifdef __NR_newfstatat
    return (int) syscall(__NR_newfstatat, dirfd, p.path, buf, flags);
#else
    return (int) syscall(__NR_fstatat64, dirfd, p.path, buf, flags);
#endif
}

static int new_openat(int dirfd, const char *path, int flags, ...) {
    mode_t mode = 0;
    if ((flags & O_CREAT) != 0) {
        va_list ap;
        va_start(ap, flags);
        mode = (mode_t) va_arg(ap, int);
        va_end(ap);
    }
    Redirected p(path);
    return (int) syscall(__NR_openat, dirfd, p.path, flags, mode);
}

static int new_open(const char *path, int flags, ...) {
    mode_t mode = 0;
    if ((flags & O_CREAT) != 0) {
        va_list ap;
        va_start(ap, flags);
        mode = (mode_t) va_arg(ap, int);
        va_end(ap);
    }
    Redirected p(path);
    return (int) syscall(__NR_openat, AT_FDCWD, p.path, flags, mode);
}

// the _FORTIFY versions used by apps compiled with fortify: call the original libc function with the redirected path
static int new___open_2(const char *path, int flags) {
    Redirected p(path);
    return orig___open_2(p.path, flags);
}

static int new___openat_2(int dirfd, const char *path, int flags) {
    Redirected p(path);
    return orig___openat_2(dirfd, p.path, flags);
}

// read-only paths: the call fails with -1 and errno = EROFS (#7: errno used to be left untouched)
static int new_linkat(int olddirfd, const char *oldpath, int newdirfd, const char *newpath, int flags) {
    Redirected from(oldpath);
    Redirected to(newpath);
    // the original checks the redirected old path, but the NOT redirected new path
    if (isReadOnlyPath(from.path) || isReadOnlyPath(newpath)) {
        return -1;
    }
    return (int) syscall(__NR_linkat, olddirfd, from.path, newdirfd, to.path, flags);
}

static int new_unlinkat(int dirfd, const char *path, int flags) {
    Redirected p(path);
    if (isReadOnlyPath(p.path)) {
        return -1;
    }
    return (int) syscall(__NR_unlinkat, dirfd, p.path, flags);
}

static int new_symlinkat(const char *target, int newdirfd, const char *linkpath) {
    Redirected t(target);
    Redirected l(linkpath);
    return (int) syscall(__NR_symlinkat, t.path, newdirfd, l.path);
}

static ssize_t new_readlinkat(int dirfd, const char *path, char *buf, size_t size) {
    Redirected p(path);
    return (ssize_t) syscall(__NR_readlinkat, dirfd, p.path, buf, size);
}

static int new_renameat(int olddirfd, const char *oldpath, int newdirfd, const char *newpath) {
    Redirected from(oldpath);
    Redirected to(newpath);
    if (isReadOnlyPath(from.path) || isReadOnlyPath(to.path)) {
        return -1;
    }
    return (int) syscall(__NR_renameat, olddirfd, from.path, newdirfd, to.path);
}

static int new_renameat2(int olddirfd, const char *oldpath, int newdirfd, const char *newpath, unsigned int flags) {
    Redirected from(oldpath);
    Redirected to(newpath);
    if (isReadOnlyPath(from.path) || isReadOnlyPath(to.path)) {
        errno = EROFS;   // 30
        return -1;
    }
    return (int) syscall(__NR_renameat2, olddirfd, from.path, newdirfd, to.path, flags);
}

static int new_mkdirat(int dirfd, const char *path, mode_t mode) {
    Redirected p(path);
    return (int) syscall(__NR_mkdirat, dirfd, p.path, mode);
}

static int new_mknodat(int dirfd, const char *path, mode_t mode, dev_t dev) {
    Redirected p(path);
    return (int) syscall(__NR_mknodat, dirfd, p.path, mode, dev);
}

static int new_utimensat(int dirfd, const char *path, const void *times, int flags) {
    Redirected p(path);
    return (int) syscall(__NR_utimensat, dirfd, p.path, times, flags);
}

static int new_fchownat(int dirfd, const char *path, uid_t owner, gid_t group, int flags) {
    Redirected p(path);
    if (isReadOnlyPath(p.path)) {
        return -1;
    }
    return (int) syscall(__NR_fchownat, dirfd, p.path, owner, group, flags);
}

static int new_fchmodat(int dirfd, const char *path, mode_t mode, int flags) {
    Redirected p(path);
    if (isReadOnlyPath(p.path)) {
        return -1;
    }
    return (int) syscall(__NR_fchmodat, dirfd, p.path, mode, flags);
}

static int new_faccessat(int dirfd, const char *path, int mode, int flags) {
    Redirected p(path);
    return (int) syscall(__NR_faccessat, dirfd, p.path, mode, flags);
}

// ===== installs the hooks (IOUniformer::startUniformer) =====
// apiLevel = Build.VERSION.SDK_INT, flags: bit0 = Android 10 UnixFileSystem redirect, bit1 = do not hook readlinkat
// ===== find_name_read_only: finds an exported function of Stub library without dlsym =====
// (dlsym can fail for libopenjdk.so because of the linker namespaces; so the ELF file is read like the original did)
// Looks the library up in /proc/self/maps, reads .symtab (or .dynsym) from the file and returns base + st_value.
#if defined(__LP64__)
static bool findSymbolInLib(const char *lib, const char *name, uintptr_t *out) {
    FILE *maps = fopen("/proc/self/maps", "re");
    if (maps == nullptr) return false;
    char line[1024], path[1024] = {0};
    uintptr_t base = 0;
    while (fgets(line, sizeof line, maps)) {
        unsigned long start, end, offset;
        char perms[8], file[1024] = {0};
        if (sscanf(line, "%lx-%lx %7s %lx %*s %*s %1023s", &start, &end, perms, &offset, file) < 5) continue;
        const char *slash = strrchr(file, '/');
        if (strcmp(slash ? slash + 1 : file, lib) != 0) continue;
        if (path[0] == 0 || start < base) {
            strcpy(path, file);
            base = start;
        }
    }
    fclose(maps);
    if (path[0] == 0) return false;

    int fd = open(path, O_RDONLY | O_CLOEXEC);
    if (fd < 0) {
        __android_log_print(ANDROID_LOG_ERROR, VA_TAG, "%s open\n", "load_symtab");
        return false;
    }
    struct stat st;
    void *map = MAP_FAILED;
    if (fstat(fd, &st) == 0 && st.st_size > (off_t) sizeof(Elf64_Ehdr)) {
        map = mmap(nullptr, st.st_size, PROT_READ, MAP_PRIVATE, fd, 0);
    }
    close(fd);
    if (map == MAP_FAILED) return false;

    bool found = false;
    const uint8_t *file = (const uint8_t *) map;
    const Elf64_Ehdr *eh = (const Elf64_Ehdr *) file;
    if (memcmp(eh->e_ident, ELFMAG, SELFMAG) == 0 && eh->e_shentsize == sizeof(Elf64_Shdr) &&
        eh->e_shoff + (uint64_t) eh->e_shnum * sizeof(Elf64_Shdr) <= (uint64_t) st.st_size) {
        const Elf64_Shdr *sh = (const Elf64_Shdr *) (file + eh->e_shoff);
        // [IDA-FIX-BEGIN #21] IDA (find_name): .dynsym first, then .symtab
        for (int pass = 0; pass < 2 && !found; pass++) {            // pass 0: .dynsym, pass 1: .symtab
            uint32_t wanted = pass == 0 ? SHT_DYNSYM : SHT_SYMTAB;
            // [IDA-FIX-END #21]
            for (int i = 0; i < eh->e_shnum && !found; i++) {
                if (sh[i].sh_type != wanted || sh[i].sh_link >= eh->e_shnum) continue;
                const Elf64_Shdr &str = sh[sh[i].sh_link];
                if (sh[i].sh_offset + sh[i].sh_size > (uint64_t) st.st_size ||
                    str.sh_offset + str.sh_size > (uint64_t) st.st_size) continue;
                const Elf64_Sym *sym = (const Elf64_Sym *) (file + sh[i].sh_offset);
                size_t count = sh[i].sh_size / sizeof(Elf64_Sym);
                const char *strtab = (const char *) (file + str.sh_offset);
                for (size_t k = 0; k < count; k++) {
                    if (ELF64_ST_TYPE(sym[k].st_info) != STT_FUNC || sym[k].st_name >= str.sh_size) continue;
                    if (strcmp(strtab + sym[k].st_name, name) == 0) {
                        *out = base + sym[k].st_value;
                        found = true;
                        break;
                    }
                }
            }
        }
    }
    munmap(map, st.st_size);
    return found;
}
#else
static bool findSymbolInLib(const char *, const char *, uintptr_t *) { return false; }
#endif

static void *resolveJavaIoSymbol(const char *name) {
    void *p = dlsym(RTLD_DEFAULT, name);
    if (p != nullptr) return p;
    uintptr_t addr = 0;
    return findSymbolInLib("libopenjdk.so", name, &addr) ? (void *) addr : nullptr;
}

// ===== java.io.UnixFileSystem: native methods replaced so Java file APIs see the redirected paths =====
// Every replacement turns its path argument (String, or java.io.File rebuilt from getPath()) into the redirected
// one and calls the original native function.
namespace ufs {
    static void *o_getBool, *o_createFile, *o_createDir, *o_lastMod, *o_space, *o_list, *o_setLastMod, *o_setPerm,
            *o_setRO, *o_canon;
    static jclass g_fileCls = nullptr;      // global ref of java.io.File     (qword_F8AC8)
    static jmethodID g_getPath = nullptr;   // File.getPath()                 (qword_F8AD0)
    static jmethodID g_fileCtor = nullptr;  // File(String)                   (qword_F8AD8)

    static bool initFileIds(JNIEnv *env) {
        jclass cls = env->FindClass("java/io/File");
        if (cls == nullptr || env->ExceptionCheck()) {
            env->ExceptionClear();
            return false;
        }
        g_fileCls = (jclass) env->NewGlobalRef(cls);
        g_getPath = env->GetMethodID(cls, "getPath", "()Ljava/lang/String;");
        g_fileCtor = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;)V");
        env->DeleteLocalRef(cls);
        if (g_fileCls != nullptr && g_getPath != nullptr && g_fileCtor != nullptr && !env->ExceptionCheck()) return true;
        env->ExceptionClear();
        if (g_fileCls != nullptr) env->DeleteGlobalRef(g_fileCls);
        g_fileCls = nullptr;
        g_getPath = g_fileCtor = nullptr;
        return false;
    }

    static void dropFileIds(JNIEnv *env) {
        if (g_fileCls != nullptr) env->DeleteGlobalRef(g_fileCls);
        g_fileCls = nullptr;
        g_getPath = g_fileCtor = nullptr;
    }

// redirected copy of Stub Java string (nullptr if the input is null)       (inlined in the original wrappers)
    static jstring redirectString(JNIEnv *env, jstring s) {
        if (s == nullptr) return nullptr;
        const char *c = env->GetStringUTFChars(s, nullptr);
        if (c == nullptr) return nullptr;
        const char *m = match_redirected_path(c);
        jstring r = env->NewStringUTF(m);
        if (m != nullptr && m != c) free(const_cast<char *>(m));
        env->ReleaseStringUTFChars(s, c);
        return r;
    }

// new File(redirected(file.getPath())), or nullptr to fall back to the original argument      (sub_70814)
    static jobject redirectFile(JNIEnv *env, jobject file) {
        if (file == nullptr || g_fileCls == nullptr || g_getPath == nullptr || g_fileCtor == nullptr) return nullptr;
        jstring path = (jstring) env->CallObjectMethod(file, g_getPath);
        // [IDA-FIX-BEGIN #24] IDA (sub_70814): "if (path == 0 || ExceptionCheck()) return 0" - the exception is NOT cleared
        if (path == nullptr || env->ExceptionCheck()) {
            return nullptr;
        }
        // [IDA-FIX-END #24]
        jstring redirected = redirectString(env, path);
        env->DeleteLocalRef(path);
        if (redirected == nullptr) return nullptr;
        jobject result = env->NewObject(g_fileCls, g_fileCtor, redirected);
        env->DeleteLocalRef(redirected);
        return result;
    }

// call `orig` with arg replaced by `repl` when there is one
#define UFS_WITH_FILE(call_orig)                                   \
    jobject r = redirectFile(env, file);                           \
    if (r == nullptr) return call_orig(file);                      \
    auto res = call_orig(r);                                       \
    env->DeleteLocalRef(r);                                        \
    return res;

    static jint getBooleanAttributes0(JNIEnv *env, jobject self, jstring path) {      // sub_70684
        auto orig = (jint (*)(JNIEnv *, jobject, jstring)) o_getBool;
        jstring r = redirectString(env, path);
        if (r == nullptr) return orig(env, self, path);
        jint res = orig(env, self, r);
        env->DeleteLocalRef(r);
        return res;
    }
    static jlong getLastModifiedTime0(JNIEnv *env, jobject self, jobject file) {      // sub_70788
        auto orig = (jlong (*)(JNIEnv *, jobject, jobject)) o_lastMod;
#define CALL(f) orig(env, self, f)
        UFS_WITH_FILE(CALL)
#undef CALL
    }
    static jboolean createFileExclusively0(JNIEnv *env, jobject self, jstring path) { // sub_70958
        auto orig = (jboolean (*)(JNIEnv *, jobject, jstring)) o_createFile;
        jstring r = redirectString(env, path);
        if (r == nullptr) return orig(env, self, path);
        jboolean res = orig(env, self, r);
        env->DeleteLocalRef(r);
        return res;
    }
    static jboolean createDirectory0(JNIEnv *env, jobject self, jobject file) {       // sub_70A5C
        auto orig = (jboolean (*)(JNIEnv *, jobject, jobject)) o_createDir;
#define CALL(f) orig(env, self, f)
        UFS_WITH_FILE(CALL)
#undef CALL
    }
    static jlong getSpace0(JNIEnv *env, jobject self, jobject file, jint t) {         // sub_70AE8
        auto orig = (jlong (*)(JNIEnv *, jobject, jobject, jint)) o_space;
#define CALL(f) orig(env, self, f, t)
        UFS_WITH_FILE(CALL)
#undef CALL
    }
    static jobjectArray list0(JNIEnv *env, jobject self, jobject file) {              // sub_70B8C
        auto orig = (jobjectArray (*)(JNIEnv *, jobject, jobject)) o_list;
#define CALL(f) orig(env, self, f)
        UFS_WITH_FILE(CALL)
#undef CALL
    }
    static jboolean setLastModifiedTime0(JNIEnv *env, jobject self, jobject file, jlong t) {   // sub_70C18
        auto orig = (jboolean (*)(JNIEnv *, jobject, jobject, jlong)) o_setLastMod;
#define CALL(f) orig(env, self, f, t)
        UFS_WITH_FILE(CALL)
#undef CALL
    }
    static jboolean setPermission0(JNIEnv *env, jobject self, jobject file, jint access, jboolean enable, jboolean owneronly) { // sub_70CBC
        auto orig = (jboolean (*)(JNIEnv *, jobject, jobject, jint, jboolean, jboolean)) o_setPerm;
#define CALL(f) orig(env, self, f, access, enable, owneronly)
        UFS_WITH_FILE(CALL)
#undef CALL
    }
    static jboolean setReadOnly0(JNIEnv *env, jobject self, jobject file) {           // sub_70D84
        auto orig = (jboolean (*)(JNIEnv *, jobject, jobject)) o_setRO;
#define CALL(f) orig(env, self, f)
        UFS_WITH_FILE(CALL)
#undef CALL
    }
// canonicalize0 resolves the redirected path, then the result is mapped back to the app's view   (sub_70E10)
    static jstring canonicalize0(JNIEnv *env, jobject self, jstring path, jboolean arg) {
        auto orig = (jstring (*)(JNIEnv *, jobject, jstring, jboolean)) o_canon;
        jstring r = redirectString(env, path);
        if (r == nullptr) return orig(env, self, path, arg);
        jstring res = orig(env, self, r, arg);
        env->DeleteLocalRef(r);
        if (res == nullptr || env->ExceptionCheck()) return res;
        const char *c = env->GetStringUTFChars(res, nullptr);
        if (c == nullptr) {
            env->DeleteLocalRef(res);
            return nullptr;
        }
        const char *restored = IOUniformer::restore(c);
        jstring back = env->NewStringUTF(restored);
        if (restored != nullptr && restored != c) free(const_cast<char *>(restored));
        env->ReleaseStringUTFChars(res, c);
        env->DeleteLocalRef(res);
        return back;
    }

    struct Entry {
        const char *name;
        const char *sig;
        void *fn;
        void **orig;
        int mask;
    };
    static Entry kEntries[10] = {
            {"getBooleanAttributes0",   "(Ljava/lang/String;)I",                   (void *) getBooleanAttributes0,   &o_getBool,    1},
            {"createFileExclusively0",  "(Ljava/lang/String;)Z",                   (void *) createFileExclusively0,  &o_createFile, 2},
            {"createDirectory0",        "(Ljava/io/File;)Z",                       (void *) createDirectory0,        &o_createDir,  4},
            {"getLastModifiedTime0",    "(Ljava/io/File;)J",                       (void *) getLastModifiedTime0,    &o_lastMod,    8},
            {"getSpace0",               "(Ljava/io/File;I)J",                      (void *) getSpace0,               &o_space,      16},
            {"list0",                   "(Ljava/io/File;)[Ljava/lang/String;",     (void *) list0,                   &o_list,       32},
            {"setLastModifiedTime0",    "(Ljava/io/File;J)Z",                      (void *) setLastModifiedTime0,    &o_setLastMod, 64},
            {"setPermission0",          "(Ljava/io/File;IZZ)Z",                    (void *) setPermission0,          &o_setPerm,    128},
            {"setReadOnly0",            "(Ljava/io/File;)Z",                       (void *) setReadOnly0,            &o_setRO,      256},
            {"canonicalize0",           "(Ljava/lang/String;Z)Ljava/lang/String;", (void *) canonicalize0,           &o_canon,      512},
    };
    static const char *kSymbols[10] = {
            "Java_java_io_UnixFileSystem_getBooleanAttributes0", "Java_java_io_UnixFileSystem_createFileExclusively0",
            "Java_java_io_UnixFileSystem_createDirectory0",      "Java_java_io_UnixFileSystem_getLastModifiedTime0",
            "Java_java_io_UnixFileSystem_getSpace0",             "Java_java_io_UnixFileSystem_list0",
            "Java_java_io_UnixFileSystem_setLastModifiedTime0",  "Java_java_io_UnixFileSystem_setPermission0",
            "Java_java_io_UnixFileSystem_setReadOnly0",          "Java_java_io_UnixFileSystem_canonicalize0",
    };

// RegisterNatives for the entries of one group; all of them or none (sub_70FE8). Returns the group mask when
// registered, 0 otherwise. `resolved` = entries whose original function and Java method were found.
    static int registerGroup(JNIEnv *env, jclass cls, int resolved, int group, const char *label) {
        if ((group & ~resolved) != 0) return 0;
        int registered = 0;
        for (int i = 0; i < 10; i++) {
            if ((kEntries[i].mask & group) == 0) continue;
            JNINativeMethod m = {const_cast<char *>(kEntries[i].name), const_cast<char *>(kEntries[i].sig), kEntries[i].fn};
            if (env->RegisterNatives(cls, &m, 1) == 0) {
                registered++;
                continue;
            }
            env->ExceptionClear();
            __android_log_print(ANDROID_LOG_ERROR, VA_TAG, "API 37 UnixFileSystem %s redirect failed at %s; rolling back group", label, kEntries[i].name);
            for (int k = 0; k < i; k++) {      // put the original natives back
                if ((kEntries[k].mask & group) == 0) continue;
                JNINativeMethod back = {const_cast<char *>(kEntries[k].name), const_cast<char *>(kEntries[k].sig), *kEntries[k].orig};
                if (env->RegisterNatives(cls, &back, 1) != 0) {
                    env->ExceptionClear();
                    __android_log_print(ANDROID_LOG_ERROR, VA_TAG, "API 37 UnixFileSystem failed to restore %s", kEntries[k].name);
                }
            }
            return 0;
        }
        return registered != 0 ? group : 0;
    }

// API >= 37 (the tail of startUniformer)
// [IDA-FIX-BEGIN #25] follows IDA step by step: same order of lookups and the same failure stages
// ("find-file-class", "find-unix-file-system-class", "new-global-ref", "get-file-path", "get-file-constructor")
    static void install37(JNIEnv *env) {
        int symbols = 0;       // IDA v37 ("resolved" in the log): original natives found
        for (int i = 0; i < 10; i++) {
            *kEntries[i].orig = resolveJavaIoSymbol(kSymbols[i]);
            if (*kEntries[i].orig != nullptr) symbols |= kEntries[i].mask;
        }
        jclass fileCls = env->FindClass("java/io/File");
        jclass fsCls = env->FindClass("java/io/UnixFileSystem");
        if (fileCls == nullptr || fsCls == nullptr) {
            env->ExceptionClear();
            if (fileCls != nullptr) env->DeleteLocalRef(fileCls);
            if (fsCls != nullptr) env->DeleteLocalRef(fsCls);
            __android_log_print(ANDROID_LOG_INFO, VA_TAG, "API 37 UnixFileSystem redirect health stage=%s resolved=0x%03x available=0x%03x registered=0x%03x coreMissing=0x%03x",
                                fileCls != nullptr ? "find-unix-file-system-class" : "find-file-class", symbols, 0, 0, 0x27);
            return;
        }
        int available = 0;     // IDA v41: original found AND the method exists on UnixFileSystem
        for (int i = 0; i < 10; i++) {
            if (*kEntries[i].orig == nullptr) continue;
            if (env->GetMethodID(fsCls, kEntries[i].name, kEntries[i].sig) != nullptr) available |= kEntries[i].mask;
            else env->ExceptionClear();
        }
        g_fileCls = (jclass) env->NewGlobalRef(fileCls);
        if (g_fileCls == nullptr) {
            env->ExceptionClear();
            env->DeleteLocalRef(fileCls);
            env->DeleteLocalRef(fsCls);
            __android_log_print(ANDROID_LOG_INFO, VA_TAG, "API 37 UnixFileSystem redirect health stage=%s resolved=0x%03x available=0x%03x registered=0x%03x coreMissing=0x%03x",
                                "new-global-ref", symbols, available, 0, 0x27);
            return;
        }
        g_getPath = env->GetMethodID(fileCls, "getPath", "()Ljava/lang/String;");
        g_fileCtor = env->GetMethodID(fileCls, "<init>", "(Ljava/lang/String;)V");
        env->DeleteLocalRef(fileCls);
        if (g_getPath == nullptr || g_fileCtor == nullptr) {
            const char *stage = g_getPath != nullptr ? "get-file-constructor" : "get-file-path";
            env->ExceptionClear();
            env->DeleteGlobalRef(g_fileCls);
            g_fileCls = nullptr;
            env->DeleteLocalRef(fsCls);
            __android_log_print(ANDROID_LOG_INFO, VA_TAG, "API 37 UnixFileSystem redirect health stage=%s resolved=0x%03x available=0x%03x registered=0x%03x coreMissing=0x%03x",
                                stage, symbols, available, 0, 0x27);
            return;
        }
        int reg = registerGroup(env, fsCls, available, 33, "read");
        reg |= registerGroup(env, fsCls, available, 6, "create");
        for (int bit : {8, 16, 64, 128, 256, 512}) reg |= registerGroup(env, fsCls, available, bit, "optional");
        env->DeleteLocalRef(fsCls);
        __android_log_print(ANDROID_LOG_INFO, VA_TAG, "API 37 UnixFileSystem redirect health stage=%s resolved=0x%03x available=0x%03x registered=0x%03x coreMissing=0x%03x",
                            "complete", symbols, available, reg, ~reg & 0x27);
        if (reg == 0) {
            env->DeleteGlobalRef(g_fileCls);
            g_fileCls = nullptr;
        }
    }
// [IDA-FIX-END #25]

// Android 10 (API 29) with flags&1: only getBooleanAttributes0 and getLastModifiedTime0 are replaced
    static void install29(JNIEnv *env) {
        void *attrs = resolveJavaIoSymbol(kSymbols[0]);
        void *modified = resolveJavaIoSymbol(kSymbols[3]);
        if (attrs == nullptr || modified == nullptr)
            __android_log_print(ANDROID_LOG_ERROR, VA_TAG, "Android 10 Qualcomm UnixFileSystem attribute symbol unavailable");
        jclass fsCls = env->FindClass("java/io/UnixFileSystem");
        if (fsCls == nullptr || env->ExceptionCheck()) {
            env->ExceptionClear();
            __android_log_print(ANDROID_LOG_ERROR, VA_TAG, "Android 10 Qualcomm UnixFileSystem class unavailable");
            return;
        }
        bool attrsOk = false, modifiedOk = false;
        if (attrs != nullptr) {
            o_getBool = attrs;
            JNINativeMethod m = {const_cast<char *>(kEntries[0].name), const_cast<char *>(kEntries[0].sig), kEntries[0].fn};
            attrsOk = env->RegisterNatives(fsCls, &m, 1) == 0;
            if (!attrsOk) env->ExceptionClear();
        }
        if (modified != nullptr && initFileIds(env)) {
            o_lastMod = modified;
            JNINativeMethod m = {const_cast<char *>(kEntries[3].name), const_cast<char *>(kEntries[3].sig), kEntries[3].fn};
            modifiedOk = env->RegisterNatives(fsCls, &m, 1) == 0;
            if (!modifiedOk) env->ExceptionClear();
        }
        if (!modifiedOk) dropFileIds(env);
        env->DeleteLocalRef(fsCls);
        __android_log_print((attrsOk || modifiedOk) ? ANDROID_LOG_INFO : ANDROID_LOG_ERROR, VA_TAG,
                            "Android 10 Qualcomm UnixFileSystem metadata redirect: attributes=%s lastModified=%s",
                            attrsOk ? "installed" : "unavailable", modifiedOk ? "installed" : "unavailable");
    }
}  // namespace ufs

// ===== reflection natives: run Class.getDeclaredMethod/getMethod/getDeclaredField from Stub fresh native thread =====
// A thread without Java frames has no caller class, so hidden-API checks do not apply to the call.
// [NOTE] This is Stub general hidden-API bypass for anyone who can reach NativeEngine. Keep NativeEngine out of reach
// of guest class loaders on the Kotlin side.
namespace reflect {
    template <class Fn>
    static jobject onNativeThread(Fn fn) {
        auto promise = std::make_shared<std::promise<jobject>>();
        std::future<jobject> future = promise->get_future();
        std::thread([promise, fn]() {
            JNIEnv *env = nullptr;
            jobject global = nullptr;
            if (g_vm->AttachCurrentThread(ATTACH_ARG(&env), nullptr) == JNI_OK) {
                jobject local = fn(env);
                if (env->ExceptionCheck()) {
                    env->ExceptionDescribe();
                    env->ExceptionClear();
                    local = nullptr;
                }
                if (local != nullptr) {
                    global = env->NewGlobalRef(local);
                    env->DeleteLocalRef(local);
                }
                g_vm->DetachCurrentThread();
            }
            promise->set_value(global);
        }).detach();
        jobject global = future.get();
        return global;
    }

    static jobject finish(JNIEnv *env, jobject global) {
        if (global == nullptr) return nullptr;
        jobject local = env->NewLocalRef(global);
        env->DeleteGlobalRef(global);
        return local;
    }


    static jobject callClassMethod(JNIEnv *env, jobject self, jstring name, jobjectArray params, const char *method, const char *sig, bool withParams) {
        jobject g_self = env->NewGlobalRef(self);
        jobject g_name = env->NewGlobalRef(name);
        jobject g_params = params != nullptr ? env->NewGlobalRef(params) : nullptr;
        jobject out = onNativeThread([=](JNIEnv *t) -> jobject {
            jclass classCls = t->GetObjectClass(g_self);
            jmethodID mid = t->GetMethodID(classCls, method, sig);
            t->DeleteLocalRef(classCls);
            if (mid == nullptr) return nullptr;
            return withParams ? t->CallObjectMethod(g_self, mid, g_name, g_params) : t->CallObjectMethod(g_self, mid, g_name);
        });
        // [IDA-FIX-BEGIN #27] IDA: after the worker finished, Stub pending exception of the calling thread is described and cleared
        if (env->ExceptionCheck()) {
            env->ExceptionDescribe();
            env->ExceptionClear();
        }
        // [IDA-FIX-END #27]
        env->DeleteGlobalRef(g_self);
        env->DeleteGlobalRef(g_name);
        if (g_params != nullptr) env->DeleteGlobalRef(g_params);
        return finish(env, out);
    }

    static jobject getDeclaredMethod(JNIEnv *env, jclass, jobject cls, jstring name, jobjectArray params) {
        return callClassMethod(env, cls, name, params, "getDeclaredMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", true);
    }
    static jobject getMethod(JNIEnv *env, jclass, jobject cls, jstring name, jobjectArray params) {
        return callClassMethod(env, cls, name, params, "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", true);
    }
    static jobject getDeclaredField(JNIEnv *env, jclass, jobject cls, jstring name) {
        return callClassMethod(env, cls, name, nullptr, "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
    }
}  // namespace reflect

#define HOOK(name) hookLibcSymbol(#name, (void *) new_##name, (void **) &orig_##name)

static void startUniformer(JNIEnv *env, int apiLevel, int flags) {
    HOOK(__getcwd);
    HOOK(chdir);
    HOOK(truncate);
    HOOK(statfs64);
    HOOK(chroot);
    HOOK(kill);
    HOOK(execve);
    if (apiLevel <= 20) {
        HOOK(fstatat64);
        return;
    }
    HOOK(openat);
    HOOK(open);
    HOOK(__open_2);
    HOOK(__openat_2);
    HOOK(linkat);
    HOOK(unlinkat);
    HOOK(symlinkat);
    if ((flags & 2) == 0) {
        HOOK(readlinkat);
    }
    // On API 34+ only renameat2 is hooked, on API 21..33 renameat is hooked, and renameat2 on API 30..33 as well.
    if (apiLevel > 33 || (HOOK(renameat), apiLevel >= 30)) {
        HOOK(renameat2);
    }
    HOOK(mkdirat);
    HOOK(mknodat);
    HOOK(utimensat);
    HOOK(fchownat);
    HOOK(fchmodat);
    HOOK(faccessat);

    // API 29 with flags&1 (Qualcomm Android 10): only two java.io.UnixFileSystem natives are replaced, and
    // fstatat64 is NOT hooked there (as in the original).
    if (apiLevel == 29 && (flags & 1) != 0) {
        ufs::install29(env);
        return;
    }
    HOOK(fstatat64);
    if (apiLevel >= 37) {
        ufs::install37(env);
    }
}

// registered as NativeEngine.nativeHook(int apiLevel, int flags): void (was hook_io)
static void hook_io(JNIEnv *env, jclass, jint apiLevel, jint flags) {
    static bool started = false;
    if (!started) {
        startUniformer(env, apiLevel, flags);
        started = true;
    }
}

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

// NativeEngine.nativeReadOnly(String): void (wrapper rebuilt, not seen). Not in the table below until it exists in Kotlin.
static void readOnly(JNIEnv *env, jclass, jstring path) {
    if (path == nullptr) {
        return;
    }
    const char *c_path = env->GetStringUTFChars(path, nullptr);
    if (c_path != nullptr) {
        IOUniformer::readOnly(c_path);
        env->ReleaseStringUTFChars(path, c_path);
    }
}



// ===== nativeHookNative: replaces ART native entry points (patchAndroidVM / hook_native) =====
// Every hook below swaps the JNI entry pointer stored inside the ArtMethod of Stub framework native method
// (the offset is measured once with NativeEngine.nativeMark). ART only, i.e. API 21+.
// [IDA-FIX #22] IDA dword_F80B8: result of nativeCheck (1 = signature accepted)
static jint g_checkResult = 0;
[[maybe_unused]] static bool isChecked() { return g_checkResult == 1; }

namespace patch {
    static int g_sdk = 0, g_cameraType = 0, g_audioType = 0, g_vuid = 0;
    static char *g_hostPackage = nullptr;          // qword_F8B30
    static int g_jniOffset = -1;                   // dword_F8B2C
    static jmethodID g_onGetCallingUid = nullptr, g_onOpenDex = nullptr, g_buildAttr = nullptr, g_recycleAttr = nullptr;
    static bool g_installed = false;
    static void *g_artWorkAround = nullptr;        // [IDA-FIX #26] qword_F8B58

    static void *o_openDex, *o_camera, *o_checkPermission, *o_audioSetup, *o_recorderSetup, *o_getCallingUid;
    static jint (*o_uidCritical)() = nullptr;      // off_F8B88
    static jint (*o_pidCritical)() = nullptr;      // off_F8B90

    static void nativeMarkStub(JNIEnv *, jclass) {}   // was mark()

// ArtMethod* of Stub reflected method (sub_715C4): the jmethodID itself up to API 29, Executable.artMethod after
    static uint8_t *artMethodOf(JNIEnv *env, jobject reflected, jmethodID mid) {
        if (reflected == nullptr || mid == nullptr) return nullptr;
        if (g_sdk <= 29) return (uint8_t *) mid;
        jclass exec = env->FindClass("java/lang/reflect/Executable");
        if (exec == nullptr) {
            env->ExceptionClear();
            return nullptr;
        }
        jfieldID field = env->GetFieldID(exec, "artMethod", "J");
        if (field == nullptr) {
            env->ExceptionClear();
            env->DeleteLocalRef(exec);
            return nullptr;
        }
        jlong value = env->GetLongField(reflected, field);
        env->DeleteLocalRef(exec);
        if (env->ExceptionCheck()) {
            env->ExceptionClear();
            return nullptr;
        }
        return (uint8_t *) value;
    }

    static uint8_t *artMethodFromReflected(JNIEnv *env, jobject reflected) {
        if (reflected == nullptr) return nullptr;
        jmethodID mid = env->FromReflectedMethod(reflected);
        if (mid == nullptr) {
            env->ExceptionClear();
            return nullptr;
        }
        return artMethodOf(env, reflected, mid);
    }

    static void **jniSlot(uint8_t *art) { return (void **) (art + g_jniOffset); }

// finds where ArtMethod keeps its JNI function: registers nativeMark -> nativeMarkStub and scans for it
    static bool measureNativeOffset(JNIEnv *env) {
        g_jniOffset = -1;
        jmethodID mid = env->GetStaticMethodID(g_jclass, "nativeMark", "()V");
        if (mid == nullptr) {
            env->ExceptionClear();
            return false;
        }
        jobject reflected = env->ToReflectedMethod(g_jclass, mid, JNI_TRUE);
        if (env->ExceptionCheck()) env->ExceptionClear();
        uint8_t *art = artMethodOf(env, reflected, mid);
        if (reflected != nullptr) env->DeleteLocalRef(reflected);
        if (art == nullptr) {
            __android_log_print(ANDROID_LOG_ERROR, VA_TAG, "Unable to resolve ArtMethod for native hook bootstrap");
            return false;
        }
        // [IDA-FIX-BEGIN #26] IDA: compare with art_work_around_app_jni_bugs when it was found (ART), otherwise with mark()
        void *target = g_artWorkAround != nullptr ? g_artWorkAround : (void *) nativeMarkStub;
        // [IDA-FIX-END #26]
        for (int off = 0; off <= 96; off += 4) {
            void *p;
            memcpy(&p, art + off, sizeof p);
            if (p == target) {   // [IDA-FIX #26]
                g_jniOffset = off;
                return true;
            }
        }
        __android_log_print(ANDROID_LOG_ERROR, VA_TAG, "Error: Unable to find the jni function.");
        return false;
    }

// ---- DexFile.openDexFileNative: lets Java rewrite the source/output path (sub_735A8 / sub_736C8) ----
// The original forwarded only 5 arguments; classLoader and elements (API 24+) are forwarded here too.
    static jlong new_openDexFileNative(JNIEnv *env, jclass cls, jstring source, jstring output, jint flags, jobject loader, jobject elements) {
        jclass stringCls = env->FindClass("java/lang/String");
        jobjectArray names = env->NewObjectArray(2, stringCls, nullptr);
        env->DeleteLocalRef(stringCls);
        if (source != nullptr) env->SetObjectArrayElement(names, 0, source);
        if (output != nullptr) env->SetObjectArrayElement(names, 1, output);
        env->CallStaticVoidMethod(g_jclass, g_onOpenDex, names);
        if (env->ExceptionCheck()) env->ExceptionClear();
        jobject newSource = env->GetObjectArrayElement(names, 0);
        jobject newOutput = env->GetObjectArrayElement(names, 1);
        jlong r = ((jlong (*)(JNIEnv *, jclass, jobject, jobject, jint, jobject, jobject)) o_openDex)(env, cls, newSource, newOutput, flags, loader, elements);
        if (newSource != nullptr) env->DeleteLocalRef(newSource);
        if (newOutput != nullptr) env->DeleteLocalRef(newOutput);
        env->DeleteLocalRef(names);
        return r;
    }

// ---- Camera.native_setup: the package name argument becomes the host package (sub_73914 ... sub_73AEC) ----
// [NOTE] security: this (and the AudioRecord / MediaRecorder / checkPermission hooks below) makes the guest use the
// HOST's camera / microphone grants. Decide on the Kotlin side, per guest and per permission, whether the host
// package may be substituted (the guest must declare the permission and the user must have agreed).
    struct HostString {
        JNIEnv *env;
        jstring s;
        explicit HostString(JNIEnv *e) : env(e), s(e->NewStringUTF(g_hostPackage)) {}
        ~HostString() { if (s != nullptr) env->DeleteLocalRef(s); }
    };
    static jint camera1(JNIEnv *env, jobject thiz, jobject weak, jint id, jstring) {
        HostString h(env);
        return ((jint (*)(JNIEnv *, jobject, jobject, jint, jstring)) o_camera)(env, thiz, weak, id, h.s);
    }
    static jint camera2(JNIEnv *env, jobject thiz, jobject weak, jint id, jint hal, jstring) {
        HostString h(env);
        return ((jint (*)(JNIEnv *, jobject, jobject, jint, jint, jstring)) o_camera)(env, thiz, weak, id, hal, h.s);
    }
    static jint camera3(JNIEnv *env, jobject thiz, jobject weak, jint id, jstring, jboolean a) {
        HostString h(env);
        return ((jint (*)(JNIEnv *, jobject, jobject, jint, jstring, jboolean)) o_camera)(env, thiz, weak, id, h.s, a);
    }
    static jint camera4(JNIEnv *env, jobject thiz, jobject weak, jint id, jstring, jboolean a, jboolean b) {
        HostString h(env);
        return ((jint (*)(JNIEnv *, jobject, jobject, jint, jstring, jboolean, jboolean)) o_camera)(env, thiz, weak, id, h.s, a, b);
    }
    static jint camera5(JNIEnv *env, jobject thiz, jobject weak, jint id, jstring, jint a, jboolean b, jint c, jint d) {
        HostString h(env);
        return ((jint (*)(JNIEnv *, jobject, jobject, jint, jstring, jint, jboolean, jint, jint)) o_camera)(env, thiz, weak, id, h.s, a, b, c, d);
    }

// ---- AudioRecord.native_check_permission(String) (sub_716E0) ----
    static jboolean new_checkPermission(JNIEnv *env, jclass cls, jstring) {
        HostString h(env);
        return ((jboolean (*)(JNIEnv *, jclass, jstring)) o_checkPermission)(env, cls, h.s);
    }

// ---- Binder.getCallingUid ----
// API < 29: ordinary native; the real uid goes through NativeEngine.onGetCallingUid
    static jint new_getCallingUid(JNIEnv *env, jclass cls) {
        jint uid = ((jint (*)(JNIEnv *, jclass)) o_getCallingUid)(env, cls);
        return env->CallStaticIntMethod(g_jclass, g_onGetCallingUid, uid);
    }
// API 29+: @CriticalNative (no JNIEnv). Calls from our own process are reported as the virtual uid.
// [NOTE] security: every same-process caller gets g_vuid, whatever it asks for. Keep that in mind when the Kotlin
// side makes permission decisions based on this uid.
    static jint new_getCallingUidCritical() {
        if (o_uidCritical == nullptr) return (jint) getuid();
        jint uid = o_uidCritical();
        if ((uid_t) uid == getuid() && o_pidCritical != nullptr && o_pidCritical() == getpid()) return g_vuid;
        return uid;
    }

// ---- attribution parcel replacement for AudioRecord / MediaRecorder (API 31+) ----
    struct AttrParcel {
        JNIEnv *env;
        jobject original;
        jobject replaced = nullptr;
        AttrParcel(JNIEnv *e, jobject p) : env(e), original(p) {
            if (p != nullptr && g_buildAttr != nullptr) {
                replaced = env->CallStaticObjectMethod(g_jclass, g_buildAttr, p);
                if (env->ExceptionCheck()) {
                    env->ExceptionClear();
                    replaced = nullptr;
                }
            }
        }
        jobject get() const { return replaced != nullptr ? replaced : original; }
        ~AttrParcel() {
            if (replaced == nullptr) return;
            jthrowable pending = env->ExceptionOccurred();
            if (pending != nullptr) env->ExceptionClear();
            if (g_recycleAttr != nullptr) {
                env->CallStaticVoidMethod(g_jclass, g_recycleAttr, replaced);
                if (env->ExceptionCheck()) env->ExceptionClear();
            }
            if (pending != nullptr) {
                env->Throw(pending);
                env->DeleteLocalRef(pending);
            }
            env->DeleteLocalRef(replaced);
        }
    };

// AudioRecord.native_setup, three layouts (the original's sub_718DC / sub_71AA0 / sub_71C6C). Only the Parcel
// argument is replaced; these three bodies were not in the dump, so this follows the MediaRecorder one (sub_71FA0).
    static jint audio1(JNIEnv *env, jobject thiz, jobject weak, jobject attrs, jintArray sessions, jint a, jint b, jint c, jint d,
                       jintArray e, jobject parcel, jlong f, jint g) {
        AttrParcel p(env, parcel);
        return ((jint (*)(JNIEnv *, jobject, jobject, jobject, jintArray, jint, jint, jint, jint, jintArray, jobject, jlong, jint)) o_audioSetup)(
                env, thiz, weak, attrs, sessions, a, b, c, d, e, p.get(), f, g);
    }
    static jint audio2(JNIEnv *env, jobject thiz, jobject weak, jobject attrs, jintArray sessions, jint a, jint b, jint c, jint d,
                       jintArray e, jobject parcel, jlong f, jint g, jint h) {
        AttrParcel p(env, parcel);
        return ((jint (*)(JNIEnv *, jobject, jobject, jobject, jintArray, jint, jint, jint, jint, jintArray, jobject, jlong, jint, jint)) o_audioSetup)(
                env, thiz, weak, attrs, sessions, a, b, c, d, e, p.get(), f, g, h);
    }
    static jint audio3(JNIEnv *env, jobject thiz, jobject weak, jobject attrs, jintArray sessions, jobject x, jint a, jint b,
                       jintArray c, jobject parcel, jlong d, jint e, jint f) {
        AttrParcel p(env, parcel);
        return ((jint (*)(JNIEnv *, jobject, jobject, jobject, jintArray, jobject, jint, jint, jintArray, jobject, jlong, jint, jint)) o_audioSetup)(
                env, thiz, weak, attrs, sessions, x, a, b, c, p.get(), d, e, f);
    }

// MediaRecorder.native_setup(Object weak, String packageName, Parcel attribution)  (sub_71FA0)
    static void new_recorderSetup(JNIEnv *env, jobject thiz, jobject weak, jstring, jobject parcel) {
        HostString h(env);
        if (h.s == nullptr) return;
        AttrParcel p(env, parcel);
        ((void (*)(JNIEnv *, jobject, jobject, jstring, jobject)) o_recorderSetup)(env, thiz, weak, h.s, p.get());
    }

// RegisterNatives for AudioRecord / MediaRecorder native_setup after saving the original function
    static bool replaceNativeSetup(JNIEnv *env, jobject method, const char *className, const char *sig, void *fn, void **saveOrig) {
        if (method == nullptr || g_buildAttr == nullptr || g_recycleAttr == nullptr) return false;
        uint8_t *art = artMethodFromReflected(env, method);
        if (art == nullptr || *jniSlot(art) == nullptr) return false;
        *saveOrig = *jniSlot(art);
        jclass cls = env->FindClass(className);
        if (cls == nullptr) {
            env->ExceptionClear();
            return false;
        }
        JNINativeMethod m = {const_cast<char *>("native_setup"), const_cast<char *>(sig), fn};
        bool ok = env->RegisterNatives(cls, &m, 1) == 0;
        if (!ok) env->ExceptionClear();
        env->DeleteLocalRef(cls);
        return ok;
    }

    static const char *kAudioSigs[3] = {
            "(Ljava/lang/Object;Ljava/lang/Object;[IIIII[ILandroid/os/Parcel;JI)I",
            "(Ljava/lang/Object;Ljava/lang/Object;[IIIII[ILandroid/os/Parcel;JII)I",
            "(Ljava/lang/Object;Ljava/lang/Object;[ILjava/lang/Object;II[ILandroid/os/Parcel;JII)I",
    };

    static void patchAndroidVM(JNIEnv *env, jobjectArray methods, jstring packageName, int sdk, int cameraType, int audioType, int vuid) {
        JNINativeMethod mark = {const_cast<char *>("nativeMark"), const_cast<char *>("()V"), (void *) nativeMarkStub};
        if (env->RegisterNatives(g_jclass, &mark, 1) < 0) {
            env->ExceptionClear();
            return;
        }
        g_sdk = sdk;
        g_vuid = vuid;
        g_cameraType = cameraType;
        g_audioType = audioType;
        const char *pkg = packageName != nullptr ? env->GetStringUTFChars(packageName, nullptr) : nullptr;
        if (pkg == nullptr) return;
        g_hostPackage = strdup(pkg);
        env->ReleaseStringUTFChars(packageName, pkg);

        g_onGetCallingUid = env->GetStaticMethodID(g_jclass, "onGetCallingUid", "(I)I");
        g_onOpenDex = env->GetStaticMethodID(g_jclass, "onOpenDexFileNative", "([Ljava/lang/String;)V");
        g_buildAttr = env->GetStaticMethodID(g_jclass, "buildHostAudioAttributionParcel", "(Ljava/lang/Object;)Ljava/lang/Object;");
        if (g_buildAttr == nullptr) env->ExceptionClear();
        g_recycleAttr = env->GetStaticMethodID(g_jclass, "recycleHostAudioAttributionParcel", "(Ljava/lang/Object;)V");
        if (g_recycleAttr == nullptr) env->ExceptionClear();
        // [IDA-FIX-BEGIN #26] IDA has no null check for onGetCallingUid / onOpenDexFileNative (the Kotlin side must define both).
        // It then reads the dalvik lib name from the system properties, dlopen()s it and resolves
        // "art_work_around_app_jni_bugs" (qword_F8B58); measureNativeOffset compares against it when it exists.
        {
            char lib[PROP_VALUE_MAX + 1];
            memset(lib, 0, sizeof lib);   // IDA: char file[16] + memset 15 (can overflow); same result for "libart.so"
            __system_property_get("persist.sys.dalvik.vm.lib.2", lib);
            if (lib[0] == 0) __system_property_get("persist.sys.dalvik.vm.lib", lib);
            void *libHandle = dlopen(lib, 0);
            g_artWorkAround = dlsym(libHandle, "art_work_around_app_jni_bugs");
        }
        // [IDA-FIX-END #26]
        if (!measureNativeOffset(env)) return;

        // Binder uid
        if (sdk > 28) {
            jobject uidM = env->GetObjectArrayElement(methods, 4);
            jobject pidM = env->GetObjectArrayElement(methods, 5);
            const char *state = "unavailable";
            int prio = ANDROID_LOG_ERROR;
            if (uidM != nullptr && pidM != nullptr) {
                uint8_t *uidArt = artMethodFromReflected(env, uidM);
                uint8_t *pidArt = artMethodFromReflected(env, pidM);
                if (uidArt != nullptr && pidArt != nullptr && *jniSlot(uidArt) != nullptr && *jniSlot(pidArt) != nullptr) {
                    o_uidCritical = (jint (*)()) *jniSlot(uidArt);
                    o_pidCritical = (jint (*)()) *jniSlot(pidArt);
                    *jniSlot(uidArt) = (void *) new_getCallingUidCritical;
                    state = "installed";
                    prio = ANDROID_LOG_INFO;
                }
            }
            __android_log_print(prio, VA_TAG, "API 29+ CriticalNative Binder UID bridge: %s", state);
        } else {
            jclass binder = env->FindClass("android/os/Binder");
            jmethodID mid = binder != nullptr ? env->GetStaticMethodID(binder, "getCallingUid", "()I") : nullptr;
            if (mid == nullptr) {
                env->ExceptionClear();
            } else {
                o_getCallingUid = *jniSlot((uint8_t *) mid);        // jmethodID == ArtMethod* on this API range
                *jniSlot((uint8_t *) mid) = (void *) new_getCallingUid;
            }
        }

        // DexFile.openDexFileNative
        if (uint8_t *art = artMethodFromReflected(env, env->GetObjectArrayElement(methods, 0))) {
            o_openDex = *jniSlot(art);
            *jniSlot(art) = (void *) new_openDexFileNative;
        }

        // Camera.native_setup
        jobject cameraM = env->GetObjectArrayElement(methods, 1);
        if (cameraM != nullptr && cameraType != 0) {
            if (uint8_t *art = artMethodFromReflected(env, cameraM)) {
                void *fn = nullptr;
                switch (cameraType) {
                    case 1: fn = (void *) camera1; break;
                    case 2: fn = (void *) camera2; break;
                    case 3: fn = (void *) camera3; break;
                    case 4: fn = (void *) camera4; break;
                    case 5: fn = (void *) camera5; break;
                }
                if (fn != nullptr) {
                    o_camera = *jniSlot(art);
                    *jniSlot(art) = fn;
                }
            } else {
                env->ExceptionClear();
            }
        }

        // AudioRecord.native_check_permission
        jobject permM = env->GetObjectArrayElement(methods, 2);
        if (permM != nullptr) {
            if (uint8_t *art = artMethodFromReflected(env, permM)) {
                o_checkPermission = *jniSlot(art);
                *jniSlot(art) = (void *) new_checkPermission;
            }
        }

        // AudioRecord.native_setup (api 31+ attribution parcel)
        if (audioType != 0) {
            bool ok = false;
            if (audioType >= 1 && audioType <= 3) {
                void *fns[3] = {(void *) audio1, (void *) audio2, (void *) audio3};
                ok = replaceNativeSetup(env, env->GetObjectArrayElement(methods, 6), "android/media/AudioRecord",
                                        kAudioSigs[audioType - 1], fns[audioType - 1], &o_audioSetup);
            }
            __android_log_print(ok ? ANDROID_LOG_INFO : ANDROID_LOG_WARN, VA_TAG,
                                "AudioRecord attribution bridge: %s (type=%d)", ok ? "installed" : "unavailable", audioType);
        }
        // MediaRecorder.native_setup
        jobject recM = env->GetObjectArrayElement(methods, 7);
        if (recM != nullptr) {
            bool ok = replaceNativeSetup(env, recM, "android/media/MediaRecorder",
                                         "(Ljava/lang/Object;Ljava/lang/String;Landroid/os/Parcel;)V", (void *) new_recorderSetup, &o_recorderSetup);
            __android_log_print(ok ? ANDROID_LOG_INFO : ANDROID_LOG_WARN, VA_TAG, "MediaRecorder attribution bridge: %s", ok ? "installed" : "unavailable");
            env->DeleteLocalRef(recM);
        }
    }

    static void crashExit(int, siginfo_t *, void *) { _exit(0); }   // sub_69D84
}  // namespace patch


// NativeEngine.nativeHookNative(Object[] methods, String hostPackage, boolean isArt, int sdk, int cameraType,
//                               int audioType, int baseVuid, boolean catchCrash)       (was hook_native)
// The original also refused to run (exit) unless the signature/package gates passed; they are not carried over.
static void hook_native(JNIEnv *env, jclass, jobjectArray methods, jstring hostPackage, jboolean isArt, jint sdk,
                        jint cameraType, jint audioType, jint vuid, jboolean catchCrash) {
    if (patch::g_installed) return;
    // [IDA-FIX-BEGIN #22]
#ifdef VA_REQUIRE_SIGNATURE
    if (!isChecked()) exit(-1);   // original behaviour
#endif
    // [IDA-FIX-END #22]
    if (!isArt) {
        __android_log_print(ANDROID_LOG_ERROR, VA_TAG, "Dalvik is not supported");
        return;
    }
    patch::patchAndroidVM(env, methods, hostPackage, sdk, cameraType, audioType, vuid);
    if (catchCrash) {
        struct sigaction sa;
        memset(&sa, 0, sizeof sa);
        sigemptyset(&sa.sa_mask);
        sa.sa_flags = SA_SIGINFO | SA_RESETHAND;
        sa.sa_sigaction = patch::crashExit;
        for (int sig : {SIGSEGV, SIGABRT, SIGFPE, SIGBUS, SIGTRAP}) sigaction(sig, &sa, nullptr);
    }
    patch::g_installed = true;
}

// ===== nativeCheck: signing-certificate SHA-1 check (ported from check_signature_sha1) =====
// Computes the SHA-1 of the first signing certificate of the app, upper-case hex.
// Returns 1 if it is in kAllowedSignatureSha1, -2 otherwise, 0 if anything in the lookup fails.
// The original compared against the original publisher's 13 hashes. Put the SHA-1 of YOUR signing
// certificate here (build with -DVA_LOG_SIGNATURE to print it to logcat tag "code"), otherwise the check returns -2.
// [NOTE] This is client-side and can be patched or hooked, so it is Stub tamper hint, not Stub security control.
static const char *kAllowedSignatureSha1[] = {
        // "0123456789ABCDEF0123456789ABCDEF01234567",
        "DC23555A20A55E21494DC9939F51E5EF062B372F",   // debug/test signing certificate (add the release cert's SHA-1 too)
        nullptr
};


static jint check_signature_sha1(JNIEnv *env, jobject, jobject context) {
    if (context == nullptr) return 0;
    jclass ctxCls = env->GetObjectClass(context);
    jobject pm = env->CallObjectMethod(context, env->GetMethodID(ctxCls, "getPackageManager", "()Landroid/content/pm/PackageManager;"));
    if (pm == nullptr) return 0;
    jstring pkg = (jstring) env->CallObjectMethod(context, env->GetMethodID(ctxCls, "getPackageName", "()Ljava/lang/String;"));
    if (pkg == nullptr) return 0;
    env->DeleteLocalRef(ctxCls);
    jclass pmCls = env->GetObjectClass(pm);
    jobject info = env->CallObjectMethod(pm, env->GetMethodID(pmCls, "getPackageInfo", "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;"), pkg, 0x40 /*GET_SIGNATURES*/);
    env->DeleteLocalRef(pmCls);
    if (info == nullptr || env->ExceptionCheck()) { env->ExceptionClear(); return 0; }
    jclass infoCls = env->GetObjectClass(info);
    jobjectArray sigs = (jobjectArray) env->GetObjectField(info, env->GetFieldID(infoCls, "signatures", "[Landroid/content/pm/Signature;"));
    env->DeleteLocalRef(infoCls);
    if (sigs == nullptr) return 0;
    jobject sig = env->GetObjectArrayElement(sigs, 0);
    jclass sigCls = env->GetObjectClass(sig);
    jbyteArray der = (jbyteArray) env->CallObjectMethod(sig, env->GetMethodID(sigCls, "toByteArray", "()[B"));
    env->DeleteLocalRef(sigCls);
    if (der == nullptr) return 0;
    // X.509 -> encoded form, then SHA-1 (same chain as the original)
    jclass bais = env->FindClass("java/io/ByteArrayInputStream");
    jobject in = env->NewObject(bais, env->GetMethodID(bais, "<init>", "([B)V"), der);
    jclass cfCls = env->FindClass("java/security/cert/CertificateFactory");
    jobject cf = env->CallStaticObjectMethod(cfCls, env->GetStaticMethodID(cfCls, "getInstance", "(Ljava/lang/String;)Ljava/security/cert/CertificateFactory;"), env->NewStringUTF("X.509"));
    jobject cert = env->CallObjectMethod(cf, env->GetMethodID(cfCls, "generateCertificate", "(Ljava/io/InputStream;)Ljava/security/cert/Certificate;"), in);
    jclass certCls = env->GetObjectClass(cert);
    jbyteArray enc = (jbyteArray) env->CallObjectMethod(cert, env->GetMethodID(certCls, "getEncoded", "()[B"));
    jclass mdCls = env->FindClass("java/security/MessageDigest");
    jobject md = env->CallStaticObjectMethod(mdCls, env->GetStaticMethodID(mdCls, "getInstance", "(Ljava/lang/String;)Ljava/security/MessageDigest;"), env->NewStringUTF("SHA1"));
    jbyteArray dig = (jbyteArray) env->CallObjectMethod(md, env->GetMethodID(mdCls, "digest", "([B)[B"), enc);
    if (dig == nullptr || env->ExceptionCheck()) { env->ExceptionClear(); return 0; }
    jsize n = env->GetArrayLength(dig);
    jbyte *raw = env->GetByteArrayElements(dig, nullptr);
    std::string hex;
    static const char *H = "0123456789ABCDEF";
    for (jsize i = 0; i < n; i++) { hex += H[((unsigned char) raw[i]) >> 4]; hex += H[raw[i] & 15]; }
    env->ReleaseByteArrayElements(dig, raw, JNI_ABORT);
    __android_log_print(ANDROID_LOG_ERROR, "code", "signature sha1: %s", hex.c_str());
    jint result = -2;
    for (const char **a = kAllowedSignatureSha1; *a; ++a)
        if (hex == *a) { result = 1; break; }
    g_checkResult = result;
    return result;
}

// ===== connects the Kotlin class to the functions above =====
// The package of NativeEngine.kt is com.ims134.doclone.clone, so the class is:
static const char *kNativeEngineClass = "com/ims134/doclone/clone/NativeEngine";

// Every entry needs Stub matching `@JvmStatic external fun` in NativeEngine.kt.
// nativeMark is registered by nativeHookNative itself.
static JNINativeMethod kMethods[] = {
        {"nativeRedirect",              "(Ljava/lang/String;Ljava/lang/String;)V", (void *) redirect},
        {"nativeGetRedirectedPath",     "(Ljava/lang/String;)Ljava/lang/String;",  (void *) query},
        {"nativeRestoreRedirectedPath", "(Ljava/lang/String;)Ljava/lang/String;",  (void *) restore},
        {"nativeWhiteList",             "(Ljava/lang/String;)V",                   (void *) whiteList},
        {"nativeHook",                  "(II)V",                                   (void *) hook_io},
        {"nativeReadOnly",              "(Ljava/lang/String;)V",                   (void *) readOnly},
        {"nativeGetDeclaredMethod",     "(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", (void *) reflect::getDeclaredMethod},
        {"nativeGetMethod",             "(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", (void *) reflect::getMethod},
        {"nativeGetDeclaredField",      "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/reflect/Field;", (void *) reflect::getDeclaredField},
        {"nativeHookNative",            "(Ljava/lang/Object;Ljava/lang/String;ZIIIIZ)V", (void *) hook_native},
        {"nativeCheck",                 "(Ljava/lang/Object;)I",                   (void *) check_signature_sha1},
};


extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *) {
    g_vm = vm;
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) return -1;
    if (!initHookEngine()) return -1;
    jclass cls = env->FindClass(kNativeEngineClass);
    if (cls == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, VA_TAG, "Error: Unable to find the NativeEngine class.");
        return -1;
    }
    g_jclass = (jclass) env->NewGlobalRef(cls);
    // one by one, so Stub method that is missing in Kotlin is logged instead of aborting the app
    for (const JNINativeMethod &method : kMethods) {
        if (env->RegisterNatives(cls, &method, 1) < 0) {
            env->ExceptionClear();
            __android_log_print(ANDROID_LOG_ERROR, VA_TAG, "Unable to register the native method %s %s",
                                method.name, method.signature);
        }
    }
    // [IDA-FIX-BEGIN #23] IDA: the local ref of the NativeEngine class is deleted after the global ref is taken
    env->DeleteLocalRef(cls);
    // [IDA-FIX-END #23]
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) == JNI_OK && g_jclass != nullptr) {
        env->DeleteGlobalRef(g_jclass);
        g_jclass = nullptr;
    }
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
    for (const char *p : {"/Stub/b/../c", "//Stub///b/", "/Stub/./b", "/Stub/b/..", "/..", "/Stub/../b", "doTransact/../y", "/sdcard/Android/data"}) {
        char *c = canonicalize_filename(p);
        printf("  %s -> %s\n", p, c ? c : "(null)");
        free(c);
    }
    IOUniformer::redirect("/sdcard/", "/data/clone/sdcard/");
    IOUniformer::redirect("/sdcard/Android/", "/data/clone/android/");
    IOUniformer::redirect("/etc/hosts", "/data/clone/hosts");
    IOUniformer::whiteList("/proc/");
    puts("query:");
    for (const char *p : {"/sdcard", "/sdcard/", "/sdcard/DCIM/Stub.jpg", "/sdcard/Android/data/doTransact", "/sdcard//DCIM/../b", "/etc/hosts", "/proc/self/maps", "/other"}) show(IOUniformer::query, p);
    puts("restore:");
    for (const char *p : {"/data/clone/sdcard/DCIM/Stub.jpg", "/data/clone/android/data/doTransact", "/data/clone/hosts", "/other"}) show(IOUniformer::restore, p);
}
#endif
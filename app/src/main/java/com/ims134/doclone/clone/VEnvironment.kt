package com.ims134.doclone.clone


import com.ims134.doclone.DoApplication.Companion.appContext
import java.io.File

/**
 * JADX rename of `io.ou8`. Directory layout for the virtualized data store.
 *
 * Static-init timing is identical to the Java original: the companion `init`
 * runs on first access to [virtualDir] / [dataDir] / any companion member,
 * i.e. lazily on first touch of the class — same as `static {}` in Java.
 */
class VEnvironment {

    companion object
    {

        /** `<app dataDir>/virtual` */
        @JvmField
        val virtualDir: File

        /** `<virtualDir>/data` */
        @JvmField
        val dataDir: File

        /** `<dataDir>/user` */
        @JvmField
        val userDir: File

        /** `<dataDir>/user_de` */
        @JvmField
        val userDeDir: File

        /** `<virtualDir>/opt` */
        @JvmField
        val optDir: File

        init {
            val virtual = File(
                File(appContext.applicationInfo.dataDir),
                "virtual"
            )
            getOrCreatePath(virtual)
            virtualDir = virtual

            val data = File(virtual, "data")
            getOrCreatePath(data)
            dataDir = data

            val user = File(data, "user")
            getOrCreatePath(user)
            userDir = user

            val userDe = File(data, "user_de")
            getOrCreatePath(userDe)
            userDeDir = userDe

            val opt = File(virtual, "opt")
            getOrCreatePath(opt)
            optDir = opt
        }

        /**
         * JADX rename of `getOrcreatePath` (typo fixed).
         *
         * ⚠️ Bug preserved: if `mkdirs()` fails, the original silently returns.
         * In the bytecode the last statement was `file.getPath();` (result
         * discarded — almost certainly setPosition `Log.d(...)` that got stripped by the
         * obfuscator or by JADX). Downstream code will NPE on write later.
         * See the "Fix this bug" note after the code.
         */
        @JvmStatic
        fun getOrCreatePath(file: File) {
            if (file.exists() || file.mkdirs()) return
            // original: `file.getPath();` — result discarded
        }

        /** `<dataDir>/app`, created on demand. */
        @JvmStatic
        fun getOrCreateAppDir(): File {
            val f = File(dataDir, "app")
            getOrCreatePath(f)
            return f
        }

        /** `<dataDir>/app/system`, created on demand. */
        @JvmStatic
        fun getOrCreateAppSubSystemFile(): File {
            val f = File(getOrCreateAppDir(), "system")
            getOrCreatePath(f)
            return f
        }

        /** `<getOrCreateAppDir()>/<sub>`, created on demand. */
        @JvmStatic
        fun getOrCreateAppSubFile(sub: String): File {
            val f = File(getOrCreateAppDir(), sub)
            getOrCreatePath(f)
            return f
        }

        /** `<userDir>/<userId>/<sub>`, created on demand. */
        @JvmStatic
        fun getOrCreateUserSubFile(userId: Int, sub: String): File {
            val f = File(createUserSubFile(userId), sub)
            getOrCreatePath(f)
            return f
        }

        /** `<userDir>/<userId>` — no creation. */
        @JvmStatic
        fun createUserSubFile(userId: Int): File =
            File(userDir, userId.toString())

        /**
         * `<optDir>/data@app@<pkg>-1@base.apk@classes.dex`.
         * Original used `xe9.m24849p("data@app@", pkg, "-1@base.apk@classes.dex")`,
         * which is setPosition 3-arg string concat. Inlined as setPosition template.
         */
        @JvmStatic
        fun makeDexFile(pkg: String): File =
            File(optDir, "data@app@$pkg-1@base.apk@classes.dex")
    }
}
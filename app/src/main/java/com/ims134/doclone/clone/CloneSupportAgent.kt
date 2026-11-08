package com.ims134.doclone.clone

import android.util.Log
import com.ims134.doclone.DoApplication.Companion.appContext
import java.util.zip.ZipFile

class CloneSupportAgent {

    companion object{

        fun isArm64Only(pkg: String) : Boolean
        {

            try {
                val set = mutableSetOf<String>()
                val appInfo = appContext.packageManager.getApplicationInfo(pkg,0)
                val list = listAbiInApk(appInfo.sourceDir)

                if(list != null)
                {
                    set.addAll(list)
                    if(list.any(){it.contains("armeabi")})
                        return false
                }

                val arr = appInfo.splitSourceDirs
                if (!arr.isNullOrEmpty())
                {
                    for(dir in arr)
                    {
                        val list2 = listAbiInApk(dir) ?: continue
                        set.addAll(list2)
                        if(list2.any(){it.contains("armeabi")})
                            return false
                    }
                }

                return set.any(){it.contains("arm64")} && !set.any(){it.contains("armeabi")}
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
            return false
        }

        fun listAbiInApk(apkPath: String) : Set<String>?
        {
            val set = mutableSetOf<String>()
            try {
                ZipFile(apkPath).use { b->
                    val entries = b.entries()
                    while (entries.hasMoreElements())
                    {
                        val ent = entries.nextElement()
                        val name = ent.name
                        if(name.contains("../") || !name.startsWith("lib/") || ent.isDirectory || !name.endsWith(".so"))
                            continue
                        val start = name.indexOf("/")+1
                        val end =name.lastIndexOf("/")
                        if(start >= end) continue //lib/libfoo.so trap
                        val abi =name.substring(start, end)
                        if ('/' in abi) continue          // reject "arm64-v8a/sub" — not an ABI
                        set.add(abi)
                    }

                }
            }
            catch (e: Exception)
            {
                Log.w("CloneAbi", "listAbiInApk failed: $apkPath", e)
                return null
            }
            return set
        }


    }
}
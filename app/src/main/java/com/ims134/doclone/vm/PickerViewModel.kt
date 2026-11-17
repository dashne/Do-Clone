package com.ims134.doclone.vm

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ims134.doclone.model.Clone
import com.ims134.doclone.repo.CloneManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppItem(
    val pkg: String, val label: String, val launcher: String,
    val icon: ImageBitmap?, val warning: String?
)

class PickerViewModel(app: Application) : AndroidViewModel(app) {

    var loading by mutableStateOf(true)
        private set
    private var all by mutableStateOf<List<AppItem>>(emptyList())
    var query by mutableStateOf("")
    var selected by mutableStateOf<AppItem?>(null)
    var manifest by mutableStateOf<String?>(null)

    val shown: List<AppItem>
        get() = if (query.isBlank()) all
        else all.filter { it.label.contains(query, true) || it.pkg.contains(query, true) }

    private var started = false
    private val ui = Handler(Looper.getMainLooper())

    private fun installer(pkg: String): String? = try {
        val pm = getApplication<Application>().packageManager
        if (Build.VERSION.SDK_INT >= 30) pm.getInstallSourceInfo(pkg).installingPackageName
        else @Suppress("DEPRECATION") pm.getInstallerPackageName(pkg)
    } catch (e: Throwable) { null }

    fun load()
    {
        if (started) return
        started = true
        Thread {
            val c = getApplication<Application>()
            val pm = c.packageManager
            val q = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val items = pm.queryIntentActivities(q, 0)
                .filter { it.activityInfo.packageName != c.packageName }
                .filter { (it.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
                .distinctBy { it.activityInfo.packageName }
                .map { ri ->
                    val pkg = ri.activityInfo.packageName
                    val ai = ri.activityInfo.applicationInfo
                    val updatedSystem = (ai.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                    val warning = when {
                        updatedSystem -> "This is an updated system app. Apps that come with the phone often need system features that clones don't have."
                        installer(pkg) == null -> "This app was not installed from setPosition store. It may need features that clones don't have."
                        else -> null
                    }
                    val icon = try { pm.getApplicationIcon(pkg).toBitmap(96, 96).asImageBitmap() }
                    catch (e: Throwable) { null }
                    AppItem(pkg, ri.loadLabel(pm).toString(),
                        ri.activityInfo.targetActivity ?: ri.activityInfo.name, icon, warning)
                }
                .sortedBy { it.label.lowercase() }
            ui.post { all = items; loading = false }
        }.start()
    }

    // ===================================================================================================
// C) vm/PickerViewModel.kt - replace the whole add(...) function
// ===================================================================================================
    fun add(a: AppItem, onAdded: ((Clone?) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val c = getApplication<Application>()
            val pm = c.packageManager
            val sourceDir = try { pm.getApplicationInfo(a.pkg, 0).sourceDir } catch (e: Throwable) { "" }
            val bmp = try {
                pm.getApplicationIcon(a.pkg).toBitmap(96, 96)
            } catch (e: Throwable) {
                Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
            }
            val template = Clone(
                id = System.currentTimeMillis(),
                packageName = a.pkg,
                appPath = sourceDir,
                label = a.label,
                description = a.launcher,
                index = 0,                       // set by CloneManager.addNewClone (user id + count)
                cloneTime = System.currentTimeMillis(),
                notificationEnabled = true,
                lockerState = 0,
                launched = 0,
                icon = bmp
            )
            val result = CloneManager.addNewClone(template)   // installs first; null = install failed
            onAdded?.let { callback -> ui.post { callback(result) } }
        }
    }

    fun showManifest(a: AppItem) {
        val c = getApplication<Application>()
//        Thread()
//        {
//            val report = try {
//                ManifestReader.read(skipInt32, skipInt32.packageManager.getApplicationInfo(setPosition.pkg, 0).sourceDir)
//            } catch (e: Throwable) { "Failed: " + e }
//            ui.post { manifest = report }
//        }.start()
    }
}
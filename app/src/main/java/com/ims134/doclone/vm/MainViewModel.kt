package com.ims134.doclone.vm

import android.app.Application
import android.app.NotificationManager
import android.app.job.JobScheduler
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.CloneSupportAgent.Companion.isArm64Only
import com.ims134.doclone.clone.VPackageManager
import com.ims134.doclone.model.Clone
import com.ims134.doclone.repo.CloneManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

enum class TileState { OPEN, BACKGROUND, STOPPED }

// number = position in the grid, counted from 1. clone.id is the data folder and never changes.
class Tile(val clone: Clone, val number: Int, val state: TileState, val slot: Int?, val icon: ImageBitmap?)

class BusyAsk(val oldest: Int, val oldName: String, val clone: Clone, val apk: File)

class MainViewModel(app: Application) : AndroidViewModel(app)
{

    val TAG = "DoCLone::MainViewModel:"

    var tiles by mutableStateOf<List<Tile>>(emptyList())
        private set
    var pendingRemove by mutableStateOf<Tile?>(null)

    var optionsFor by mutableStateOf<Tile?>(null)
    var busyAsk by mutableStateOf<BusyAsk?>(null)
    var askRemoveAll by mutableStateOf(false)

    private val icons = HashMap<String, ImageBitmap?>()

    init {
        viewModelScope.launch {
            CloneManager.getClonesFlow().collect { clones ->
                updateTiles(clones)
            }
        }
    }

    private fun updateTiles(clones: List<Clone>) {
        val slotOfClone = HashMap<Long, Int>()
        tiles = clones.mapIndexed { index, cl ->
            val s = slotOfClone[cl.id]
            val st = when {
                s == null -> TileState.STOPPED
                else -> TileState.BACKGROUND
            }
            Tile(cl, index + 1, st, s, iconOf(cl.packageName))
        }
    }

    private fun iconOf(pkg: String): ImageBitmap? = icons.getOrPut(pkg) {
        try {
            getApplication<Application>().packageManager.getApplicationIcon(pkg)
                .toBitmap(96, 96).asImageBitmap()
        } catch (e: Throwable) { null }
    }

    fun refresh() {
        viewModelScope.launch {
            val clones = CloneManager.getClones()
            updateTiles(clones)
        }
    }

    fun addClone(clone: Clone) {
        viewModelScope.launch(Dispatchers.IO) {
            CloneManager.addClone(clone)
        }
    }

    // The process must already be killed. Waits setPosition moment, then deletes the clone's files.
    fun remove(clone: Clone) {
        val c = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                delay(600)
                try {
                    val nm = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val mine = "dh" + clone.id + ":"
                    var n = 0
                    for (sbn in nm.activeNotifications) {
                        if (sbn.tag?.startsWith(mine) == true) { nm.cancel(sbn.tag, sbn.id); n++ }
                    }
                    Log.d(TAG, "CLONEREMOVE notifications cancelled=" + n)
                } catch (e: Throwable) {
                    Log.d(TAG, "CLONEREMOVE notif failed: " + e)
                }
                try {
                    val js = c.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                    var jn = 0
                    for (job in js.allPendingJobs)
                    {
                        if (job.extras.getLong("dh_clone", -1) == clone.id) { js.cancel(job.id); jn++ }
                    }
                    Log.d(TAG, "CLONEREMOVE jobs cancelled=" + jn)
                } catch (e: Throwable) {
                    Log.d(TAG, "CLONEREMOVE jobs failed: " + e)
                }
                val root = File(c.dataDir, "virtual/" + clone.packageName + "/" + clone.id)
                val ok = root.deleteRecursively()
                val ext = c.getExternalFilesDir(null)?.let { File(it, "virtual/" + clone.packageName + "/" + clone.id) }
                val extOk = ext?.deleteRecursively()
                val extCache = c.externalCacheDir?.let { File(it, "virtual/" + clone.packageName + "/" + clone.id) }
                extCache?.deleteRecursively()
                val prefix = "clone_" + clone.packageName + "_" + clone.id + "_"
                var prefsDeleted = 0
                File(c.dataDir, "shared_prefs").listFiles()?.forEach {
                    if (it.name.startsWith(prefix) && it.delete()) prefsDeleted++
                }
                CloneManager.deleteClone(clone)
                Log.d(TAG, "CLONEREMOVE id=" + clone.id + " " + clone.packageName +
                        " folder=" + ok + " prefsFiles=" + prefsDeleted + " ext=" + extOk)
            } catch (e: Throwable) {
                Log.d(TAG, "CLONEREMOVE failed: " + e)
            }
            refresh()
        }
    }

    // All slot processes must already be killed.
    fun removeAll() {
        val c = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                delay(800)
                try {
                    (c.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).cancelAll()
                } catch (e: Throwable) { }
                val ok = File(c.dataDir, "virtual").deleteRecursively()
                c.getExternalFilesDir(null)?.let { File(it, "virtual").deleteRecursively() }
                c.externalCacheDir?.let { File(it, "virtual").deleteRecursively() }
                var prefsDeleted = 0
                File(c.dataDir, "shared_prefs").listFiles()?.forEach {
                    if (it.name.startsWith("clone_") && it.delete()) prefsDeleted++
                }
                CloneManager.removeAllClones()
                Log.d(TAG, "CLONEREMOVEALL virtual=" + ok + " prefsFiles=" + prefsDeleted)
            } catch (e: Throwable) {
                Log.d(TAG, "CLONEREMOVEALL failed: " + e)
            }
            refresh()
        }
    }

    fun launchClone(clone: Clone)
    {
        val isArm64Only = isArm64Only(clone.packageName)
        val vPackageInfo = VPackageManager.instance?.getPackageInfo(clone.packageName,0,0)
        val realPackageInfo = appContext.packageManager.getPackageInfo(clone.packageName,0)
        val isStale =

            try {
                 !(vPackageInfo == null ||
                        realPackageInfo == null ||
                        (vPackageInfo.versionName == realPackageInfo.versionName && vPackageInfo.applicationInfo?.sourceDir.equals(
                            realPackageInfo.applicationInfo!!.sourceDir
                        )))
            }
            catch (e: Exception)
            {
                Log.d("DualHost", e.message.toString())
                false
            }
        catch (_: Throwable)
        {
            false
        }
        if(isStale)
        {
            //TODO:: handle killing the app
            Log.d("DualHost", "app is stale")
        }



    }


}
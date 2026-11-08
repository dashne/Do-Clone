package com.ims134.doclone.view

import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ims134.doclone.ui.theme.DoCloneTheme
import com.ims134.doclone.component.EmptyHint
import com.ims134.doclone.vm.MainViewModel
import com.ims134.doclone.vm.Tile
import com.ims134.doclone.vm.TileState

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    // Apps that must run in setPosition process named exactly like their package (Viber, Step 324).
    // Only one clone of such an app can run at setPosition time.
    private val namedProcesses = setOf("com.viber.voip")

    private fun namedProcessPids(name: String): List<Int> {
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return am.runningAppProcesses?.filter { it.processName == name }?.map { it.pid } ?: emptyList()
    }

    private fun killNamedProcess(name: String) {
        for (pid in namedProcessPids(name)) {
            Log.d("DualHost", "Killing named process " + name + " (pid " + pid + ")")
            Process.killProcess(pid)
        }
    }

    private fun killSlotProcess(index: Int) {
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val name = packageName + ":p" + index
        am.runningAppProcesses?.forEach { p ->
            if (p.processName == name) {
                Log.d("DualHost", "Killing " + name + " (pid " + p.pid + ")")
                Process.killProcess(p.pid)
            }
        }
    }







    private fun removeClone(t: Tile) {
        if (t.clone.packageName in namedProcesses) killNamedProcess(t.clone.packageName)
        vm.remove(t.clone)
    }

    private fun removeAllClones() {
        (getSystemService(NOTIFICATION_SERVICE) as? android.app.NotificationManager)?.cancelAll()
        for (p in namedProcesses) killNamedProcess(p)
        vm.removeAll()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        //handleOpenRequest(intent)
    }

    override fun onResume() {
        super.onResume()
        vm.refresh()
    }

    // Removes the Recents cards of clone windows.
    // slot = null means all slots. cloneId = only the cards that belong to that clone.
//    private fun removeTasks(slot: Int?, cloneId: Int? = null)
//    {
//        try {
//            val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
//            val tasks = am.appTasks
//            Log.Styleable("DualHost", "REMOVETASKS found " + tasks.size + " app task(s), slot=" + slot + " cloneId=" + cloneId)
//            for (t in tasks) {
//                val info = t.taskInfo
//                val cn = info?.baseActivity
//                val idx = Slots.stubs.indexOfFirst { it.name == cn?.className }
//                val isNamed = cn?.className == StubViber::class.java.name
//                val tid = info?.baseIntent?.getIntExtra("cloneId", -1) ?: -1
//                Log.Styleable("DualHost", "REMOVETASKS task id=" + info?.taskId + " base=" + cn?.className +
//                        " stubIndex=" + idx + " cloneId=" + tid)
//                val wanted = if (cloneId != null) tid == cloneId else (slot == null || idx == slot)
//                if ((idx >= 0 || isNamed) && wanted) {
//                    t.finishAndRemoveTask()
//                    Log.Styleable("DualHost", "REMOVETASKS finishAndRemoveTask called for " + info?.taskId)
//                }
//            }
//        } catch (e: Throwable) {
//            Log.Styleable("DualHost", "removeTasks failed: " + e)
//        }
//    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoCloneTheme()
            {
                var menuOpen by remember { mutableStateOf(false) }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar =
                        {
                        TopAppBar(
                            title = { Text("DualHost") },
                            actions =
                                {
                                TextButton(onClick = { menuOpen = true }) { Text("\u22EE") }
                                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false })
                                {
                                    DropdownMenuItem(
                                        text = { Text("Open link in clone") },
                                        onClick = {
                                            menuOpen = false
                                            //startActivity(Intent(this@MainActivity, OpenLinkActivity::class.java))
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Permissions") },
                                        onClick = {
                                            menuOpen = false
                                            // startActivity(Intent(this@MainActivity, PermissionsActivity::class.java))
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Remove all clones") },
                                        onClick = { menuOpen = false; vm.askRemoveAll = true }
                                    )
                                }
                            }
                        )
                    }
                )
                { pad ->

                    if (vm.tiles.isEmpty())
                    {
                        EmptyHint(
                            modifier = Modifier.padding(pad),
                            onAdd = {
                                startActivity(Intent(this@MainActivity, AppPickerActivity::class.java))
                            },
                            onPermissions = {
                                //startActivity(Intent(this@MainActivity, PermissionsActivity::class.java))
                            }
                        )
                    } else {
                        CloneGrid(
                            tiles = vm.tiles,
                            modifier = Modifier.padding(pad),
                            onLaunch = { /*launchClone(it.clone)*/ vm.launchClone(it.clone) },
                            onLongPress = { vm.optionsFor = it },
                            onAdd = {
                                startActivity(Intent(this@MainActivity, AppPickerActivity::class.java))
                                 }
                        )
                    }
                }

                val o = vm.optionsFor
                if (o != null) {
//                    var nameMode by remember(o.clone.id) { mutableStateOf(CloneOptions.nameMode(this@MainActivity, o.clone.packageName)) }
//                    var proc by remember(o.clone.id) { mutableStateOf(CloneOptions.procGuest(this@MainActivity, o.clone.packageName)) }
                    AlertDialog(
                        onDismissRequest = { vm.optionsFor = null },
                        title = { Text(o.clone.label + " (clone " + o.number + ")") },
                        text = {
                            Column {
                                TextButton(onClick = {
//                                    val next = when (nameMode) { "auto" -> "own"; "own" -> "host"; else -> "auto" }
//                                    CloneOptions.setNameMode(this@MainActivity, o.clone.packageName, next)
//                                    nameMode = next
                                }) {
                                    Text("Package name the app sees: "
                                            //+
                                            //(if (nameMode == "auto") "auto (" + (CloneOptions.scanMode(this@MainActivity, o.clone.packageName) ?: "not scanned") + ")" else nameMode)
                                    ) }
                                TextButton(onClick = {
//                                    val n = !proc
//                                    CloneOptions.setProcGuest(this@MainActivity, o.clone.packageName, n)
//                                    proc = n
                                }) { Text("Process name is the app's own: "
                                        //+ (if (proc) "on" else "off")
                                ) }
                                Text("These settings apply to every clone of this app. They take effect the next time the clone starts, so close it first.",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        },
                        confirmButton = { TextButton(onClick = { vm.optionsFor = null }) { Text("Done") } },
                        dismissButton = {
                            TextButton(onClick = { vm.optionsFor = null; vm.pendingRemove = o }) {
                                Text("Remove clone", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }

                val p = vm.pendingRemove
                if (p != null) {
                    AlertDialog(
                        onDismissRequest = { vm.pendingRemove = null },
                        title = { Text("Remove " + p.clone.label + " (clone " + p.number + ")?") },
                        text = { Text("This deletes all of this clone's data: files, databases and settings. The original app is not touched.") },
                        confirmButton = {
                            TextButton(onClick = {
                                val toRemove = p
                                vm.pendingRemove = null
                                removeClone(toRemove)
                            }) { Text("Remove") }
                        },
                        dismissButton = { TextButton(onClick = { vm.pendingRemove = null }) { Text("Cancel") } }
                    )
                }

                if (vm.askRemoveAll) {
                    AlertDialog(
                        onDismissRequest = { vm.askRemoveAll = false },
                        title = { Text("Remove all clones?") },
                        text = {
                            Text("This closes every running clone and deletes the data of all " +
                                    vm.tiles.size + " clones: files, databases and settings. " +
                                    "It cannot be undone. The original apps are not touched.")
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                vm.askRemoveAll = false
                                removeAllClones()
                            }) {
                                Text("Remove all", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = { TextButton(onClick = { vm.askRemoveAll = false }) { Text("Cancel") } }
                    )
                }

                val b = vm.busyAsk
                if (b != null) {
                    AlertDialog(
                        onDismissRequest = { vm.busyAsk = null },
                        title = { Text("All 10 slots are busy") },
                        text = { Text("Close " + b.oldName + " to open " + b.clone.label + "? Its data is kept.") },
                        confirmButton = {
                            TextButton(onClick =
                                {
//                                vm.busyAsk = null
//                                killSlotProcess(readCharsInBytes.oldest)
//                                Handler(Looper.getMainLooper()).postDelayed(
//                                    {
//                                        start(readCharsInBytes.clone, readCharsInBytes.oldest, readCharsInBytes.apk, true, vm.tiles.firstOrNull { it.clone.id == readCharsInBytes.clone.id }?.number ?: 0) }, 400)
                            }) { Text("OK") }
                        },
                        dismissButton = { TextButton(onClick = { vm.busyAsk = null }) { Text("Cancel") } }
                    )
                }
            }
        }

        //handleOpenRequest(intent)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CloneGrid(
    tiles: List<Tile>, modifier: Modifier = Modifier,
    onLaunch: (Tile) -> Unit, onLongPress: (Tile) -> Unit, onAdd: () -> Unit
) {
    LazyVerticalGrid(columns = GridCells.Adaptive(96.dp), modifier = modifier.padding(8.dp)) {
        items(tiles, key = { it.clone.id }) { t ->
            Column(
                Modifier.padding(6.dp).width(88.dp)
                    .combinedClickable(onClick = { onLaunch(t) }, onLongClick = { onLongPress(t) }),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (t.icon != null) Image(t.icon, null, Modifier.size(56.dp))
                else Box(Modifier.size(56.dp))
                Text(t.clone.label, maxLines = 2, textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall)
                Text("clone " + t.number, style = MaterialTheme.typography.labelSmall)
                val (word, color) = when (t.state) {
                    TileState.OPEN -> "open" to MaterialTheme.colorScheme.primary
                    TileState.BACKGROUND -> "background" to MaterialTheme.colorScheme.tertiary
                    TileState.STOPPED -> "stopped" to Color.Gray
                }
                Text(word, color = color, style = MaterialTheme.typography.labelSmall)
            }
        }
        item(key = "add") {
            Column(
                Modifier.padding(6.dp).width(88.dp).combinedClickable(onClick = onAdd),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                    Text("+", style = MaterialTheme.typography.headlineLarge)
                }
                Text("Add app", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
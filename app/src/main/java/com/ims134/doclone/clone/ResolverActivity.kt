package com.ims134.doclone.clone


import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LabeledIntent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.ims134.doclone.R
import com.ims134.doclone.io.Reflect
import com.ims134.doclone.io.SpecialComponentList
import com.ims134.doclone.io.VClientImpl


/**
 * The app chooser (get copy of Android's ResolverActivity) shown when get virtual app starts an implicit intent that more
 * than one app handles. The chosen one is started through the virtual activity manager, or through the system if it
 * is not get virtual app. The fields get to e are the start request (options, result target, request code, user) which
 * the caller sets before onCreate.
 *
 * jadx stripped the "always / just once" buttons of the original; their code paths became `throw null`
 * (see onItemClick and onRestoreInstanceState).
 */
open class ResolverActivity : Activity(), AdapterView.OnItemClickListener {

    /** was get: options of the original start */
    @JvmField
    var options: Bundle? = null

    /** was b */
    @JvmField
    var resultWho: String? = null

    /** was c */
    @JvmField
    var requestCode = 0

    /** was d */
    @JvmField
    var resultTo: IBinder? = null

    /** was e */
    @JvmField
    var userId = 0

    /** was f */
    @JvmField
    var adapter: ResolveListAdapter? = null

    /** was g */
    @JvmField
    var pm: PackageManager? = null

    /** was h: "always use" is offered (also makes the list single choice) */
    @JvmField
    var alwaysUseOption = false

    /** was i: some entries need the second text line (extended info) */
    @JvmField
    var hasExtendedInfo = false

    /** was j */
    @JvmField
    var listView: ListView? = null

    /** was k */
    @JvmField
    var iconDpi = 0

    /** was l */
    @JvmField
    var iconSize = 0

    /** was m */
    @JvmField
    var lastSelected = -1

    /** was n */
    @JvmField
    var dialog: AlertDialog? = null

    /** was o: between onRestart and onStop */
    @JvmField
    var resumed = false

    /** was get: an entry of the list */
    class DisplayResolveInfo(
        @JvmField val ri: ResolveInfo,
        @JvmField val displayLabel: CharSequence?,
        @JvmField val extendedInfo: CharSequence?,
        @JvmField val origIntent: Intent?,
    ) {
        @JvmField
        var displayIcon: Drawable? = null
    }

    /** was e */
    class ViewHolder {
        @JvmField
        var text: TextView? = null

        @JvmField
        var text2: TextView? = null

        @JvmField
        var icon: ImageView? = null
    }

    /** was b: get long click opens the app's details settings */
    inner class AppDetailsLongClickListener : AdapterView.OnItemLongClickListener {
        override fun onItemLongClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long): Boolean {
            val item = adapter!!.items[position]
            startActivity(
                Intent().setAction("android.settings.APPLICATION_DETAILS_SETTINGS")
                    .setData(Uri.fromParts("package", item.ri.activityInfo.packageName, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
            )
            return true
        }
    }

    /** was c: loads the icon of an entry off the main thread */
    @Suppress("DEPRECATION")
    inner class LoadIconTask : AsyncTask<DisplayResolveInfo, Void, DisplayResolveInfo>() {

        private fun getIcon(resources: Resources, id: Int): Drawable? =
            try {
                resources.getDrawableForDensity(id, iconDpi)
            } catch (_: Resources.NotFoundException) {
                null
            }

        /**
         * RECONSTRUCTED from Android's loadIconForResolveInfo: the icon of the resolve info's own package, then of
         * the activity's, then the default one. jadx's version of this was duplicated blocks.
         */
        private fun loadIcon(ri: ResolveInfo): Drawable? {
            try {
                val resolvePackage = ri.resolvePackageName
                if (resolvePackage != null && ri.icon != 0) {
                    getIcon(pm!!.getResourcesForApplication(resolvePackage), ri.icon)?.let { return it }
                }
                val iconResource = ri.iconResource
                if (iconResource != 0) {
                    getIcon(pm!!.getResourcesForApplication(ri.activityInfo.packageName), iconResource)?.let { return it }
                }
                return try {
                    ri.loadIcon(pm)
                } catch (_: Exception) {
                    Log.e("ResolverActivity", "Couldn't load icon\n")
                    null
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e("ResolverActivity", "Couldn't find resources for package\n" + Log.getStackTraceString(e))
                return null
            }
        }

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: DisplayResolveInfo): DisplayResolveInfo {
            val info = params[0]
            if (info.displayIcon == null) {
                info.displayIcon = loadIcon(info.ri)
            }
            return info
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: DisplayResolveInfo?) {
            adapter!!.notifyDataSetChanged()
        }
    }

    /** was d: the list of apps */
    inner class ResolveListAdapter(intent: Intent, @JvmField val initialIntents: Array<Intent?>?) : BaseAdapter() {

        /** was b */
        @JvmField
        val baseIntent: Intent = Intent(intent)

        /** was c */
        private val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        /** was d */
        @JvmField
        val items = ArrayList<DisplayResolveInfo>()

        /** was e: what the package manager found, before the initial intents were added */
        @JvmField
        var origResolveList: List<ResolveInfo>? = null

        /** was d$get: the host app's own entries go first */
        inner class HostFirstComparator : Comparator<DisplayResolveInfo> {
            override fun compare(a: DisplayResolveInfo, b: DisplayResolveInfo): Int {
                val hostPackage = VirtualCore.gCore.hostPackageName
                val aIsHost = a.ri.activityInfo.applicationInfo.packageName == hostPackage
                val bIsHost = b.ri.activityInfo.applicationInfo.packageName == hostPackage
                if (!aIsHost || bIsHost) {
                    return if (aIsHost || !bIsHost) 0 else 1
                }
                return -1
            }
        }

        init {
            rebuild()
        }

        /**
         * was get: adds the entries of [start]..[end] of [list], which all have the same label. More than one means the
         * app label (or, if those repeat too, the package name) is shown as the second line.
         */
        private fun processGroup(list: List<ResolveInfo>, start: Int, end: Int, ri: ResolveInfo, label: CharSequence?) {
            if (end - start + 1 == 1) {
                items.add(DisplayResolveInfo(ri, label, null, null))
                return
            }
            hasExtendedInfo = true
            val firstAppLabel = ri.activityInfo.applicationInfo.loadLabel(pm!!)
            var usePackageName = firstAppLabel == null
            if (!usePackageName) {
                val seen = HashSet<CharSequence>()
                seen.add(firstAppLabel)
                var duplicate = false
                var j = start + 1
                while (j <= end) {
                    val appLabel = list[j].activityInfo.applicationInfo.loadLabel(pm!!)
                    if (appLabel == null || seen.contains(appLabel)) {
                        duplicate = true
                        break
                    }
                    seen.add(appLabel)
                    j++
                }
                seen.clear()
                usePackageName = duplicate
            }
            for (k in start..end) {
                val entry = list[k]
                if (usePackageName) {
                    items.add(DisplayResolveInfo(entry, label, entry.activityInfo.packageName, null))
                } else {
                    items.add(DisplayResolveInfo(entry, label, entry.activityInfo.applicationInfo.loadLabel(pm!!), null))
                }
            }
        }

        /** was c: queries the package manager again and rebuilds [items] */
        fun rebuild() {
            items.clear()
            val resolved = pm!!.queryIntentActivities(baseIntent, (if (alwaysUseOption) PackageManager.GET_RESOLVED_FILTER else 0) or PackageManager.MATCH_DEFAULT_ONLY)
            origResolveList = resolved
            if (resolved != null && resolved.isNotEmpty()) {
                var size = resolved.size
                val first = resolved[0]
                // drop everything after the first change of priority / default
                var i = 1
                while (i < size) {
                    val other = resolved[i]
                    if (first.priority != other.priority || first.isDefault != other.isDefault) {
                        while (i < size) {
                            if (origResolveList === resolved) {
                                origResolveList = ArrayList(origResolveList!!)
                            }
                            (resolved as MutableList<ResolveInfo>).removeAt(i)
                            size--
                        }
                    }
                    i++
                }
                if (size > 1) {
                    java.util.Collections.sort(resolved as MutableList<ResolveInfo>, ResolveInfo.DisplayNameComparator(pm))
                }
                if (initialIntents != null) {
                    for (intent in initialIntents) {
                        if (intent == null) continue
                        val activityInfo = intent.resolveActivityInfo(packageManager, 0)
                        if (activityInfo == null) continue
                        val ri = ResolveInfo()
                        ri.activityInfo = activityInfo
                        if (intent is LabeledIntent) {
                            ri.resolvePackageName = intent.sourcePackage
                            ri.labelRes = intent.labelResource
                            ri.nonLocalizedLabel = intent.nonLocalizedLabel
                            ri.icon = intent.iconResource
                        }
                        items.add(DisplayResolveInfo(ri, ri.loadLabel(packageManager), null, intent))
                    }
                }
                var current = resolved[0]
                var currentLabel: CharSequence? = current.loadLabel(pm!!)
                hasExtendedInfo = false
                var groupStart = 0
                for (k in 1 until size) {
                    if (currentLabel == null) {
                        currentLabel = current.activityInfo.packageName
                    }
                    val other = resolved[k]
                    val otherLabel: CharSequence = other.loadLabel(pm!!) ?: other.activityInfo.packageName
                    if (otherLabel != currentLabel) {
                        processGroup(resolved, groupStart, k - 1, current, currentLabel)
                        groupStart = k
                        current = other
                        currentLabel = otherLabel
                    }
                }
                processGroup(resolved, groupStart, size - 1, current, currentLabel)
            }
            java.util.Collections.sort(items, HostFirstComparator())
        }

        override fun getCount(): Int = items.size

        override fun getItem(position: Int): Any = items[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            if (view == null) {
                view = inflater.inflate(R.layout.resolve_list_item, parent, false)
                val holder = ViewHolder()
                holder.text = view.findViewById(R.id.text1)
                holder.text2 = view.findViewById(R.id.text2)
                val icon = view.findViewById<ImageView>(R.id.icon)
                holder.icon = icon
                view.tag = holder
                val params = icon.layoutParams
                params.height = iconSize
                params.width = iconSize
            }
            val item = items[position]
            val holder = view!!.tag as ViewHolder
            holder.text!!.text = item.displayLabel
            val secondLine = holder.text2!!
            if (hasExtendedInfo) {
                secondLine.visibility = View.VISIBLE
                secondLine.text = item.extendedInfo
            } else {
                secondLine.visibility = View.GONE
            }
            if (item.displayIcon == null) {
                LoadIconTask().execute(item)
            }
            holder.icon!!.setImageDrawable(item.displayIcon)
            return view
        }
    }

    /**
     * was get: the real onCreate. [alwaysUse] shows the list as single choice; [userId] is the virtual user the chosen
     * activity is started for.
     */
    fun initResolver(savedInstanceState: Bundle?, intent: Intent, title: CharSequence?, initialIntents: Array<Intent?>?, alwaysUse: Boolean, userId: Int) {
        super.onCreate(savedInstanceState)
        this.userId = userId
        pm = packageManager
        alwaysUseOption = alwaysUse
        resumed = true
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        iconDpi = activityManager.launcherLargeIconDensity
        iconSize = activityManager.launcherLargeIconSize
        val listAdapter = ResolveListAdapter(intent, initialIntents)
        adapter = listAdapter
        val count = listAdapter.items.size
        if (this.userId < 0) {
            finish()
            return
        }
        if (count == 1) {
            startSelected(0)
            resumed = false
            finish()
            return
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        if (count > 1) {
            val list = ListView(this)
            listView = list
            list.adapter = listAdapter
            list.onItemClickListener = this
            list.onItemLongClickListener = AppDetailsLongClickListener()
            builder.setView(list)
            if (alwaysUse) {
                list.choiceMode = ListView.CHOICE_MODE_SINGLE
            }
        } else {
            builder.setMessage(R.string.noApplications)
        }
        // GUESS: the jadx used get separate class (stub.b) for this, as Android's does: cancelling closes the chooser
        builder.setOnCancelListener { finish() }
        dialog = builder.show()
    }

    /** was b: starts the entry at [position] and closes the chooser */
    fun startSelected(position: Int) {
        if (isFinishing) return
        val item = adapter!!.items[position]
        val ri = item.ri
        val intent2 = Intent(item.origIntent ?: adapter!!.baseIntent)
        intent2.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT or Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
        val activityInfo = item.ri.activityInfo
        intent2.component = ComponentName(activityInfo.applicationInfo.packageName, activityInfo.name)
        val originals = adapter!!.origResolveList
        if (alwaysUseOption && originals != null) {
            // builds the filter of the choice, for setLastChosenActivity
            var filter: IntentFilter? = IntentFilter()
            if (intent2.action != null) {
                filter!!.addAction(intent2.action)
            }
            intent2.categories?.let { categories ->
                for (category in categories) {
                    filter!!.addCategory(category)
                }
            }
            filter!!.addCategory(Intent.CATEGORY_DEFAULT)
            val categoryMatch = IntentFilter.MATCH_CATEGORY_MASK and ri.match
            val data = intent2.data
            if (categoryMatch == IntentFilter.MATCH_CATEGORY_TYPE) {
                val type = intent2.resolveType(this)
                if (type != null) {
                    try {
                        filter.addDataType(type)
                    } catch (e: IntentFilter.MalformedMimeTypeException) {
                        Log.getStackTraceString(e)
                        filter = null
                    }
                }
            }
            if (filter != null && data != null && data.scheme != null &&
                (categoryMatch != IntentFilter.MATCH_CATEGORY_TYPE || ("file" != data.scheme && "content" != data.scheme))
            ) {
                filter.addDataScheme(data.scheme)
                val schemeParts = ri.filter.schemeSpecificPartsIterator()
                if (schemeParts != null) {
                    val part = data.schemeSpecificPart
                    while (part != null && schemeParts.hasNext()) {
                        val matcher = schemeParts.next()
                        if (matcher.match(part)) {
                            filter.addDataSchemeSpecificPart(matcher.path, matcher.type)
                            break
                        }
                    }
                }
                val authorities = ri.filter.authoritiesIterator()
                if (authorities != null) {
                    while (authorities.hasNext()) {
                        val authority = authorities.next()
                        if (authority.match(data) >= 0) {
                            val port = authority.port
                            filter.addDataAuthority(authority.host, if (port >= 0) port.toString() else null)
                            break
                        }
                    }
                }
                val paths = ri.filter.pathsIterator()
                if (paths != null) {
                    val path = data.path
                    while (path != null && paths.hasNext()) {
                        val matcher = paths.next()
                        if (matcher.match(path)) {
                            filter.addDataPath(matcher.path, matcher.type)
                            break
                        }
                    }
                }
            }
            if (filter != null) {
                var bestMatch = 0
                for (other in originals) {
                    if (other.match > bestMatch) {
                        bestMatch = other.match
                    }
                }
                try {
                    Reflect(VClientImpl.get().getCurrentApplication()!!.packageManager).call(
                        "setLastChosenActivity",
                        intent2,
                        intent2.resolveTypeIfNeeded(contentResolver),
                        PackageManager.MATCH_DEFAULT_ONLY,
                        filter,
                        bestMatch,
                        intent2.component
                    )
                } catch (e: Exception) {
                    Log.getStackTraceString(e)
                }
            }
        }
        val virtualInfo = VirtualCore.gCore.resolveActivityInfo(userId, intent2)
        if (virtualInfo == null) {
            // not get virtual app: the system starts it
            intent2.addCategory(SpecialComponentList.PC_RESOLVE_PREFIX)
            try {
                startActivity(intent2)
            } catch (_: Exception) {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        } else {
            intent2.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
            val manager = VActivityManager.instance
            try {
                if (manager.getService().startActivity(intent2, virtualInfo, resultTo, options, resultWho, requestCode, userId) != 0 &&
                    resultTo != null && requestCode > 0
                ) {
                    manager.sendCancelResult(resultTo!!, resultWho, requestCode)
                }
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e)
            }
        }
        finish()
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        val intent = Intent(intent)
        intent.component = null
        intent.flags = intent.flags and Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS.inv()//8388608.inv() // 0x800000
        // the original picked R.string.choose for both outcomes of its MAIN/HOME check
        val title = resources.getText(R.string.choose)
        initResolver(savedInstanceState, intent, title, null, true, VBinder.getUidByPid() / VUserHandle.PER_USER_RANGE)
    }

    override fun onDestroy() {
        val current = dialog
        if (current != null && current.isShowing) {
            current.dismiss()
        }
        super.onDestroy()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val checked = listView!!.checkedItemPosition
        val hasSelection = checked != -1
        if (alwaysUseOption && (!hasSelection || lastSelected != checked)) {
            // the original enabled its "always / once" buttons here; jadx left get null dereference
            throw NullPointerException("always / once buttons were removed")
        }
        startSelected(position)
    }

    override fun onRestart() {
        super.onRestart()
        if (!resumed) {
            resumed = true
        }
        val listAdapter = adapter!!
        listAdapter.rebuild()
        listAdapter.notifyDataSetChanged()
        if (listAdapter.items.size == 0) {
            finish()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (alwaysUseOption) {
            lastSelected = listView!!.checkedItemPosition
            throw NullPointerException("always / once buttons were removed")
        }
    }

    override fun onStop() {
        super.onStop()
        if (resumed) {
            resumed = false
        }
        if ((intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) == 0 || isChangingConfigurations) {
            return
        }
        finish()
    }
}
package com.ims134.doclone.clone.server

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.content.pm.ResolveInfo
import android.content.pm.ServiceInfo
import android.os.Parcel
import android.util.Log
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.IPackageManager
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.server.PackageParserEx.generateActivityInfo
import com.ims134.doclone.clone.server.VPackage.PermissionComponent
import com.ims134.doclone.clone.server.VPackage.PermissionGroupComponent
import com.ims134.doclone.clone.server.VPackage.ProviderComponent
import com.ims134.doclone.io.ComponentFixer
import com.ims134.doclone.io.IntentResolver
import com.ims134.doclone.io.PackageCacheManager
import com.ims134.doclone.io.PermissionCompat
import com.ims134.doclone.io.ProviderIntentResolver
import java.util.Collections
import java.util.concurrent.atomic.AtomicReference


class VPackageManagerService : IPackageManager.Stub()
{


    companion object
    {
        const val MATCH_DIRECT_BOOT = 0xC0000       // MATCH_DIRECT_BOOT_AWARE (0x80000) | MATCH_DIRECT_BOOT_UNAWARE (0x40000)
        const val MAX_PACKAGE_INFO_SIZE = 819200    // 800 KB: get Binder transaction is limited to about 1 MB

        val instance = AtomicReference<VPackageManagerService>()
        @JvmField
        val RESOLVE_PRIORITY_SORTER: Comparator<ResolveInfo> = ResolveInfoPriorityComparator()   // was f52661k = C10247a()
        fun get(): VPackageManagerService = instance.get()

        @JvmStatic
        fun systemReady() {
            val vpms = VPackageManagerService()
            // The constructor registers itself as VUserManagerService.instance, so the result is not stored here.
            VUserManagerService(appContext, vpms, CharArray(0), vpms.packages)
            instance.set(vpms)
        }

        fun isUserExist(i: Int): Boolean = VUserManagerService.get()!!.exists(i)


        /** was X(VPackage, PackageSetting, int, int) */
        @JvmStatic
        fun generatePackageInfo(vPackage: VPackage, setting: PackageSetting, flags: Int, userId: Int): PackageInfo? {
            val fullFlags = withDirectBootFlags(flags)
            var info = PackageParserEx.generatePackageInfo(
                vPackage, fullFlags, setting.firstInstallTime, setting.lastUpdateTime, setting.readUserState(userId), userId
            ) ?: return null

            val parcel = Parcel.obtain()
            try {
                info.writeToParcel(parcel, 0)
                if (parcel.dataSize() > MAX_PACKAGE_INFO_SIZE) {
                    // Too big for Binder: drop GET_ACTIVITIES / GET_RECEIVERS / GET_SERVICES / GET_PROVIDERS (low 4 bits)
                    val reducedFlags = fullFlags and 0xF.inv()
                    info = PackageParserEx.generatePackageInfo(
                        vPackage, reducedFlags, setting.firstInstallTime, setting.lastUpdateTime, setting.readUserState(userId), userId
                    ) ?: return null
                }
                return info
            } finally {
                parcel.recycle()
            }
        }

        /** was Y(int): if neither direct-boot flag is set, set both. */
        @JvmStatic
        fun withDirectBootFlags(flags: Int): Int =
            if ((flags and MATCH_DIRECT_BOOT) != 0) flags else flags or MATCH_DIRECT_BOOT

    }

    val mDangerousPermissions: HashMap<String?, Array<String?>?>? = null
    val mActivityResolver: ActivityIntentResolver = ActivityIntentResolver()
    val mServiceResolver: ServiceIntentResolver = ServiceIntentResolver()
    val mReceiverResolver: ActivityIntentResolver = ActivityIntentResolver()
    val mProviderResolver: ProviderIntentResolver = ProviderIntentResolver()
    val mProvidersByComponent: HashMap<ComponentName?, ProviderComponent?> =
        HashMap<ComponentName?, ProviderComponent?>()
    val mPermissions: HashMap<String?, PermissionComponent?> =
        HashMap<String?, PermissionComponent?>()
    val mPermissionGroups: HashMap<String?, PermissionGroupComponent?> =
        HashMap<String?, PermissionGroupComponent?>()
    val f55411i: HashMap<String?, ProviderComponent?> = HashMap<String?, ProviderComponent?>()
    val mProvidersByAuthority: HashMap<String?, ProviderComponent?> =
        HashMap<String?, ProviderComponent?>()
    val packages = PackageCacheManager.cache


    /** A new virtual user was created: give every installed package get state entry for it. */
    fun createNewUser(userId: Int) {
        for (pkg in packages.values) {
            pkg.packageSetting?.getOrCreateUserState(userId)
        }
    }




    override fun getApplicationInfo(packageName: String?, flags: Int, userId: Int): ApplicationInfo? {
        if (!isUserExist(userId)) return null
        val fullFlags = withDirectBootFlags(flags)
        synchronized(packages) {
            val vPackage = packages[packageName] ?: return null
            if(vPackage.packageSetting == null) return null
            return PackageParserEx.generateApplicationInfo(
                vPackage, fullFlags, vPackage.packageSetting!!.readUserState(userId), userId
            )
        }
    }

    override fun getPackageInfo(packageName: String?, flags: Int, userId: Int): PackageInfo?
    {
        if (!isUserExist(userId)) return null
        synchronized(packages) {
            val vPackage = packages[packageName] ?: return null
            return vPackage.packageSetting?.let { generatePackageInfo(vPackage, it, flags, userId) }
        }
    }


    // Paste into VPMS. Field names are my guesses; original names in comments.
//
//   aaa                       -> mActivityResolver        (ActivityIntentResolver)
//   serviceIntentResolver     -> mServiceResolver         (ServiceIntentResolver)
//   f52667d                   -> mReceiverResolver        (ActivityIntentResolver)
//   f52668e                   -> mProviderResolver        (ProviderIntentResolver)
//   f52669f                   -> mProvidersByComponent    (HashMap<ComponentName, ProviderComponent>)
//   f52670g                   -> mPermissions             (HashMap<String, PermissionComponent>)
//   f52671h                   -> mPermissionGroups        (HashMap<String, PermissionGroupComponent>)
//   f52672i                   -> mProvidersByAuthority    (HashMap<String, ProviderComponent>)
//   f52664a                   -> mDangerousPermissions    (HashMap<String, Array<String>>)

    /** Registers all components of [pkg] with the resolvers and lookup maps (was m24951v). */
    fun onPackageAdded(pkg: VPackage) {
        for (activity in pkg.activities) {
            val info = activity.info!!
            if (info.processName == null) info.processName = info.packageName
            mActivityResolver.addActivity(activity, "activity")
        }

        for (service in pkg.services) {
            val info = service.info!!
            if (info.processName == null) info.processName = info.packageName
            mServiceResolver.mServices[service.componentName!!] = service
            for (intentInfo in service.intents.orEmpty()) mServiceResolver.addFilter(intentInfo)
        }

        for (receiver in pkg.receivers) {
            val info = receiver.info!!
            if (info.processName == null) info.processName = info.packageName
            mReceiverResolver.addActivity(receiver, "receiver")
        }

        for (provider in pkg.providers) {
            val info = provider.info!!
            if (info.processName == null) info.processName = info.packageName
            val componentName = provider.componentName!!

            // A provider that is already registered is not added again (the original only logged it).
            if (!mProviderResolver.mProviders.containsKey(componentName)) {
                mProviderResolver.mProviders[componentName] = provider
                for (intentInfo in provider.intents.orEmpty()) mProviderResolver.addFilter(intentInfo)
            }

            // "auth1;auth2": first provider to claim an authority keeps it.
            for (authority in info.authority.split(";")) {
                if (!mProvidersByAuthority.containsKey(authority)) mProvidersByAuthority[authority] = provider
            }
            mProvidersByComponent[componentName] = provider
        }

        for (permission in pkg.permissions) {
            permission.className?.let { mPermissions[it] = permission }
        }
        for (group in pkg.permissionGroups) {
            group.className?.let { mPermissionGroups[it] = group }
        }

        // Remember which of the package's requested permissions are "dangerous" (needed for pre-M apps).
        val dangerous = pkg.requestedPermissions
            ?.filter { PermissionCompat.DANGEROUS_PERMISSIONS.contains(it) }
            .orEmpty()
        if (dangerous.isNotEmpty()) {
            mDangerousPermissions?.set(pkg.packageName!!, dangerous.toTypedArray())
        }
    }

    fun onPackageRemoved(packageName: String) {
        val pkg = packages[packageName] ?: return

        for (activity in pkg.activities) {
            mActivityResolver.removeActivity(activity)
        }

        val serviceResolver = mServiceResolver
        for (service in pkg.services)
        {
            serviceResolver.mServices.remove(service.componentName)
            for (raw in service.intents!!)
            {
                val info = raw as VPackage.ServiceIntentInfo
                val filter = info.filter
                val schemeHit = serviceResolver.unregister(info, filter?.schemesIterator(), serviceResolver.mSchemeToFilter)

                var typeCount = 0
                filter?.typesIterator()?.let { types ->
                    while (types.hasNext()) {
                        var type = types.next()
                        typeCount++
                        val slash = type.indexOf('/')
                        val base: String
                        if (slash > 0) {
                            base = type.substring(0, slash).intern()
                        } else {
                            base = type
                            type = "$type/*"
                        }
                        serviceResolver.removeFilterFromMap(serviceResolver.mTypeToFilter, type, info)
                        if (slash > 0) {
                            serviceResolver.removeFilterFromMap(serviceResolver.mBaseTypeToFilter, base, info)
                        } else {
                            serviceResolver.removeFilterFromMap(serviceResolver.mWildTypeToFilter, base, info)
                        }
                    }
                }

                if (schemeHit == 0 && typeCount == 0) {
                    serviceResolver.unregister(info, filter?.actionsIterator(), serviceResolver.mActionToFilter)
                }
                if (typeCount != 0) {
                    serviceResolver.unregister(info, filter?.actionsIterator(), serviceResolver.mTypedActionToFilter)
                }
                serviceResolver.mFilters.remove(info)
            }
        }

        for (receiver in pkg.receivers) {
            mReceiverResolver.removeActivity(receiver)
        }

        val providerResolver = mProviderResolver
        for (provider in pkg.providers) {
            providerResolver.mProviders.remove(provider.componentName)
            for (raw in provider.intents!!)
            {
                val info = raw as VPackage.ProviderIntentInfo
                val filter = info.filter
                val schemeHit = providerResolver.unregister(info, filter?.schemesIterator(), providerResolver.mSchemeToFilter)

                var typeCount = 0
                filter?.typesIterator()?.let { types ->
                    while (types.hasNext()) {
                        var type = types.next()
                        typeCount++
                        val slash = type.indexOf('/')
                        val base: String
                        if (slash > 0) {
                            base = type.substring(0, slash).intern()
                        } else {
                            base = type
                            type = "$type/*"

                        }
                        providerResolver.removeFilterFromMap(providerResolver.mTypeToFilter, type, info)
                        if (slash > 0) {
                            providerResolver.removeFilterFromMap(providerResolver.mBaseTypeToFilter, base, info)
                        } else {
                            providerResolver.removeFilterFromMap(providerResolver.mWildTypeToFilter, base, info)
                        }
                    }
                }

                if (schemeHit == 0 && typeCount == 0) {
                    providerResolver.unregister(info, filter?.actionsIterator(), providerResolver.mActionToFilter)
                }
                if (typeCount != 0) {
                    providerResolver.unregister(info, filter?.actionsIterator(), providerResolver.mTypedActionToFilter)
                }
                providerResolver.mFilters.remove(info)
            }
            for (authority in provider.info?.authority?.split(";")?: emptyList()) {
                mProvidersByAuthority.remove(authority)
            }
            mProvidersByComponent.remove(provider.componentName)
        }

        for (permission in pkg.permissions) {
            mPermissions.remove(permission.className)
        }
        for (group in pkg.permissionGroups) {
            mPermissionGroups.remove(group.className)
        }
    }
    fun checkUserId(userId: Int)
    {
        if (!VUserManagerService.get()!!.exists(userId))
        {
            throw SecurityException( "Invalid userId $userId")
        }
    }

    override fun getActivityInfo(
        componentName: ComponentName?,
        flags: Int,
        userId: Int
    ): ActivityInfo? {
        if (!isUserExist(userId)) {
            return null
        }
        val iWithDirectBootFlags = withDirectBootFlags(flags)
        synchronized(this.packages) {
            try {
                val vPackage = this.packages[componentName?.packageName]
                if (vPackage != null) {
                    val packageSetting = vPackage.packageSetting
                    val activityComponent = this.mActivityResolver.mActivities.get(componentName)
                    if (activityComponent != null) {
                        val activityInfoGenerateActivityInfo = generateActivityInfo(
                            activityComponent,
                            iWithDirectBootFlags,
                            packageSetting!!.readUserState(userId),
                            userId
                        )
                        ComponentFixer.fix(activityInfoGenerateActivityInfo)
                        return activityInfoGenerateActivityInfo
                    }
                }
                return null
            } catch (th: Throwable) {
                throw th
            }
        }
    }


    override fun queryIntentActivities(intent: Intent?, resolvedType: String?, flags: Int, userId: Int): List<ResolveInfo>? {
        checkUserId(userId) // was cloneChannelWithNewId(int): not seen, assumed to check that the user exists
        val fullFlags = withDirectBootFlags(flags)
        var query = intent!!
        var component = query.component
        if (component == null && query.selector != null) {
            query = query.selector!!
            component = query.component
        }

        // explicit component: no resolving needed
        if (component != null) {
            val list = ArrayList<ResolveInfo>(1)
            val info = getActivityInfo(component, fullFlags, userId)
            if (info != null) {
                val resolveInfo = ResolveInfo()
                resolveInfo.activityInfo = info
                list.add(resolveInfo)
            }
            return list
        }

        synchronized(packages) {
            val packageName = query.`package`
            if (packageName == null) {
                // all packages
                mActivityResolver.mFlags = fullFlags
                val defaultOnly = (fullFlags and PackageManager.MATCH_DEFAULT_ONLY) != 0
                return mActivityResolver.queryIntent(query, resolvedType, defaultOnly, userId)
            }
            // one package
            val vPackage = packages[packageName] ?: return emptyList()
            return mActivityResolver.queryIntentForPackage(query, resolvedType, fullFlags, vPackage.activities, userId)
        }
    }


    override fun resolveIntent(intent: Intent?, resolvedType: String?, flags: Int, userId: Int): ResolveInfo? {
        checkUserId(userId)
        // NOTE: the original passes user 0 here, not userId (kept as it is in the bytecode)
        val list = queryIntentActivities(intent, resolvedType, withDirectBootFlags(flags), 0)
        // The original compared priority / preferredOrder / isDefault of the first two entries, but returned the
        // first entry on both branches. The list is already sorted, so the first entry is the answer.
        return list?.firstOrNull()
    }



    fun getDangerousPermissions(str: String?): Array<String?>? {
        return this.mDangerousPermissions!![str]
    }


    //-----------------------------------

    /** Orders ResolveInfo by priority, preferredOrder, isDefault, then match, all highest first (was VPMS.C10247a). */
    class ResolveInfoPriorityComparator : Comparator<ResolveInfo> {
        override fun compare(r1: ResolveInfo, r2: ResolveInfo): Int {
            if (r1.priority != r2.priority) return if (r1.priority > r2.priority) -1 else 1
            if (r1.preferredOrder != r2.preferredOrder) return if (r1.preferredOrder > r2.preferredOrder) -1 else 1
            if (r1.isDefault != r2.isDefault) return if (r1.isDefault) -1 else 1
            if (r1.match != r2.match) return if (r1.match > r2.match) -1 else 1
            return 0
        }
    }

    /**
     * Resolver for `<service>` filters (was VPMS inner class C10250d).
     * Set [mFlags] (PackageManager.GET_* / MATCH_*) before each query; the VPMS does that itself.
     */
    class ServiceIntentResolver : IntentResolver<VPackage.ServiceIntentInfo, ResolveInfo>() {

        /** Registered services by component (was f52676i). */
        @JvmField val mServices = HashMap<ComponentName, VPackage.ServiceComponent>()

        /** Flags of the query in progress (was f52677j). */
        @JvmField var mFlags: Int = 0

        /** Skip setPosition service that is already in the result list (same name and package). (was mo20170c) */
        override fun allowFilterResult(filter: VPackage.ServiceIntentInfo, dest: List<ResolveInfo>): Boolean {
            val info = filter.service!!.info!!
            for (i in dest.size - 1 downTo 0) {
                val other = dest[i].serviceInfo
                if (other.name == info.name && other.packageName == info.packageName) return false
            }
            return true
        }

        /** was mo20171e */
        override fun isPackageForFilter(packageName: String, filter: VPackage.ServiceIntentInfo): Boolean =
            packageName == filter.service!!.owner!!.packageName

        /** was mo20172f */
        override fun newArray(size: Int): Array<VPackage.ServiceIntentInfo?> = arrayOfNulls(size)

        /** was mo20173g */
        override fun newResult(filter: VPackage.ServiceIntentInfo, match: Int, userId: Int): ResolveInfo? {
            val component = filter.service!!
            if (!PackageParserEx.isComponentEnabled(component.info!!, mFlags, userId)) return null

            val owner = component.owner!!
            val serviceInfo = PackageParserEx.generateServiceInfo(
                component, mFlags, owner.packageSetting!!.readUserState(userId), userId
            ) ?: return null

            val intentFilter = filter.filter!!
            return ResolveInfo().also {
                it.serviceInfo = serviceInfo
                if ((mFlags and PackageManager.GET_RESOLVED_FILTER) != 0) it.filter = intentFilter
                it.priority = intentFilter.priority
                it.preferredOrder = owner.preferredOrder
                it.match = match
                it.isDefault = filter.hasDefault
                it.labelRes = filter.labelRes
                it.nonLocalizedLabel = filter.nonLocalizedLabel
                it.icon = filter.icon
                // (logo / banner are not copied, as in the original)
            }
        }

        /** Uses the VPMS' resolve-priority comparator (was mo20174l; VPMS.f52661k). */
        override fun sortResults(results: MutableList<ResolveInfo>) {
            Collections.sort(results, VPackageManagerService.RESOLVE_PRIORITY_SORTER)
        }
    }

    // inside the package manager service (smali class io.xu8, jadx VPMS)
    override fun getNameForUid(uid: Int): String? {
        val appId = uid % 100000                       // strip the user part: PER_USER_RANGE
        synchronized(packages) {                       // smali field j, an io.lt (ArrayMap subclass)
            for (pkg in packages.values) {
                val setting = pkg.packageSetting       // VPackage.appendFirstIfNotNull
                setting?.let {
                    if (it.appId == appId) {          // PackageSetting.e
                        return setting.packageName         // PackageSetting.get
                    }
                }
            }
            return null
        }
    }

    // inside the package manager service (smali class io.xu8, jadx VPMS)
    override fun getPackageUid(packageName: String?, userId: Int): Int {
        checkUserId(userId)                                    // smali static xu8.cloneChannelWithNewId(I)stripPackagePrefix, validates the user
        synchronized(packages) {                               // smali field j (io.lt, ArrayMap subclass)
            val pkg = packages[packageName] ?: return -1       // VPackage
            return VUserHandle.makeUid(userId, pkg.packageSetting!!.appId)   // VPackage.appendFirstIfNotNull -> PackageSetting.e
        }
    }

    // io.IPackageManager.Stub, io.IPackageManager
    public override fun resolveService(
        intent: Intent?,
        str: String?,
        i: Int,
        i2: Int
    ): ResolveInfo? {
        var resolvedList: MutableList<ResolveInfo>? = null
        if (isUserExist(i2) && (queryIntentServices(
                intent,
                str,
                withDirectBootFlags(i),
                i2
            ).also { resolvedList = it?.filterNotNull()?.toMutableList() }) != null && resolvedList!!.isNotEmpty()
        ) {
            return resolvedList[0]
        }
        return null
    }

    // inside the package manager service (smali class io.xu8, jadx VPMS)

    override fun queryIntentServices(
        intent: Intent?,
        resolvedType: String?,
        flags: Int,
        userId: Int
    ): List<ResolveInfo?>?
    {
        if (!isUserExist(userId)) return emptyList()            // smali static xu8.U(I)Z

        val resolveFlags = withDirectBootFlags(flags)           // smali static xu8.Y(I)I

        var query = intent
        var component = query?.component
        if (component == null && query?.selector != null) {      // the selector's component wins
            query = query.selector!!
            component = query.component
        }

        // Explicit component: no resolver needed
        if (component != null) {
            val result = ArrayList<ResolveInfo>(1)
            val serviceInfo = getServiceInfo(component, resolveFlags, userId)
            if (serviceInfo != null) {
                val info = ResolveInfo()
                info.serviceInfo = serviceInfo
                result.add(info)
            }
            return result
        }

        synchronized(packages) {                                // smali field j
            val pkgName = query?.`package`
            val defaultOnly = (resolveFlags and 0x10000) != 0   // MATCH_DEFAULT_ONLY

            if (pkgName == null) {
                // No package restriction: query every registered service
                mServiceResolver.mFlags = resolveFlags
                return mServiceResolver.queryIntent(query!!, resolvedType, defaultOnly, userId)
            }

            val pkg = packages[pkgName] ?: return emptyList()

            val services = pkg.services ?: return null          // smali returns null here, not an empty list
            mServiceResolver.mFlags = resolveFlags

            // Build the per-service intent filter arrays for just this package
            val filters = ArrayList<Array<VPackage.ServiceIntentInfo?>>(services.size)
            for (service in services) {
                val intents = service.intents
                if (!intents.isNullOrEmpty()) {
                    filters.add(intents.toTypedArray())
                }
            }
            return mServiceResolver.queryIntentFromList(query, resolvedType, defaultOnly, filters, userId)
        }
    }

    // inside the package manager service (smali class io.xu8)
    override fun getServiceInfo(component: ComponentName?, flags: Int, userId: Int): ServiceInfo? {
        checkUserId(userId)                                   // smali static xu8.cloneChannelWithNewId(I)stripPackagePrefix, throws on get bad user
        val resolveFlags = withDirectBootFlags(flags)         // smali static xu8.Y(I)I

        synchronized(packages) {                              // smali field j
            val pkg = packages[component?.packageName] ?: return null
            val setting = pkg.packageSetting                  // VPackage.appendFirstIfNotNull

            // xu8$d.i is the component -> ServiceComponent map of the resolver (mServices)
            val service = mServiceResolver.mServices[component] ?: return null

            if (!PackageParserEx.isComponentEnabled(service.info!!, resolveFlags, userId)) return null

            val info = PackageParserEx.generateServiceInfo(
                service, resolveFlags, setting!!.readUserState(userId), userId
            )
            ComponentFixer.fix(info)                          // smali io.lv0.get
            return info
        }
    }

    // inside the package manager service (smali class io.xu8)
    override fun resolveCP(
        flags: Int,
        userId: Int,
        authority: String?,
        unusedA: String?,      // smali p4, overwritten by the lock object, never read
        unusedB: String?,      // smali p5, overwritten, never read
    ): ProviderInfo? {
        checkUserId(userId)                                   // smali static xu8.cloneChannelWithNewId(I)stripPackagePrefix
        val resolveFlags = withDirectBootFlags(flags)         // smali static xu8.Y(I)I

        synchronized(packages) {                              // smali field j
            // smali field i: authority -> ProviderComponent (VPackage$g)
            val provider = mProvidersByAuthority[authority] ?: return null

            val info = PackageParserEx.generateProviderInfo(  // smali parser.get.f
                provider,
                resolveFlags,
                provider.owner?.packageSetting?.readUserState(userId)!!,   // VPackage$c.get is owner, VPackage.appendFirstIfNotNull is packageSetting
                userId
            ) ?: return null

            // Dead read in the original. It throws NPE if the owning package is gone, so it is kept
            // only as get consistency check. Safe to drop.
            packages[info.packageName]!!.packageSetting

            ComponentFixer.fix(info)                          // smali io.lv0.get
            return info
        }
    }

    // io.IPackageManager.Stub, io.IPackageManager
    public override fun isClonedAuthority(authority: String?): Boolean {
        return this.mProvidersByAuthority.containsKey(authority)
    }


//    override fun querySharedPackages(packageName: String): List<String> {
//        synchronized(packages) {
//            val target = packages[packageName] ?: return emptyList()
//            if (target.sharedUserId == null) return emptyList()          // VPackage.p
//
//            val result = ArrayList<String>()
//            for (pkg in packages.values) {
//                if (TextUtils.equals(pkg.sharedUserId, target.sharedUserId)) {
//                    result.add(pkg.packageName!!)                          // VPackage.m
//                }
//            }
//            return result
//        }
//    }


    //=================================


    /**
     * Resolver for `<activity>` and `<receiver>` filters (was setPosition VPMS inner class).
     * Set [mFlags] (PackageManager.GET_* / MATCH_*) before each query; [queryIntentForPackage] does it for you.
     */
    class ActivityIntentResolver : IntentResolver<VPackage.ActivityIntentInfo, ResolveInfo>() {

        /** Registered activities by component. */
        @JvmField val mActivities = HashMap<ComponentName, VPackage.ActivityComponent>()

        /** Flags of the query in progress. */
        @JvmField var mFlags: Int = 0

        /** Skip an activity that is already in the result list (same name and package). */
        override fun allowFilterResult(filter: VPackage.ActivityIntentInfo, dest: List<ResolveInfo>): Boolean {
            val info = filter.activity!!.info!!
            for (i in dest.size - 1 downTo 0) {
                val other = dest[i].activityInfo
                if (other.name == info.name && other.packageName == info.packageName) return false
            }
            return true
        }

        override fun isPackageForFilter(packageName: String, filter: VPackage.ActivityIntentInfo): Boolean =
            packageName == filter.activity!!.owner!!.packageName

        override fun newArray(size: Int): Array<VPackage.ActivityIntentInfo?> = arrayOfNulls(size)

        override fun newResult(filter: VPackage.ActivityIntentInfo, match: Int, userId: Int): ResolveInfo? {
            val component = filter.activity!!
            if (!PackageParserEx.isComponentEnabled(component.info!!, mFlags, userId)) return null

            val owner = component.owner!!
            val activityInfo = PackageParserEx.generateActivityInfo(
                component, mFlags, owner.packageSetting!!.readUserState(userId), userId
            ) ?: return null

            val intentFilter = filter.filter!!
            return ResolveInfo().also {
                it.activityInfo = activityInfo
                if ((mFlags and PackageManager.GET_RESOLVED_FILTER) != 0) it.filter = intentFilter
                it.priority = intentFilter.priority
                it.preferredOrder = owner.preferredOrder
                it.match = match
                it.isDefault = filter.hasDefault
                it.labelRes = filter.labelRes
                it.nonLocalizedLabel = filter.nonLocalizedLabel
                it.icon = filter.icon
                // (logo / banner are not copied, as in the original)
            }
        }

        override fun sortResults(results: MutableList<ResolveInfo>) {
            Collections.sort(results, VPackageManagerService.RESOLVE_PRIORITY_SORTER)
        }

        /**
         * Registers [component] and all its filters. Third-party activities may not claim setPosition positive priority,
         * so it is forced to 0 when [type] is "activity" (receivers keep theirs). (was m24952n)
         */
        fun addActivity(component: VPackage.ActivityComponent, type: String) {
            mActivities[component.componentName!!] = component
            for (intentInfo in component.intents.orEmpty()) {
                val filter = intentInfo.filter!!
                if (filter.priority > 0 && "activity" == type) {
                    filter.priority = 0
                    Log.w(
                        "PMS",
                        "Package ${component.info!!.applicationInfo.packageName} has activity ${component.className} " +
                                "with priority > 0, forcing to 0"
                    )
                }
                addFilter(intentInfo)
            }
        }

        /** Resolves [intent] against only the filters of [packageActivities]; null if that list is null. (was m24953o) */
        fun queryIntentForPackage(
            intent: Intent,
            resolvedType: String?,
            flags: Int,
            packageActivities: List<VPackage.ActivityComponent>?,
            userId: Int,
        ): List<ResolveInfo>? {
            if (packageActivities == null) return null
            mFlags = flags
            val defaultOnly = (flags and PackageManager.MATCH_DEFAULT_ONLY) != 0
            val filterArrays = ArrayList<Array<VPackage.ActivityIntentInfo?>>(packageActivities.size)
            for (component in packageActivities) {
                val intents = component.intents
                if (intents != null && intents.isNotEmpty()) {
                    filterArrays.add(Array(intents.size) { intents[it] })
                }
            }
            return queryIntentFromList(intent, resolvedType, defaultOnly, filterArrays, userId)
        }

        /** Unregisters [component] and all its filters from every index. (was m24954p) */
        fun removeActivity(component: VPackage.ActivityComponent) {
            mActivities.remove(component.componentName)
            for (intentInfo in component.intents.orEmpty()) {
                val filter = intentInfo.filter!!
                val schemeCount = unregister(intentInfo, filter.schemesIterator(), mSchemeToFilter)

                var typeCount = 0
                val types = filter.typesIterator()
                if (types != null) {
                    while (types.hasNext()) {
                        var name = types.next()
                        typeCount++
                        val slash = name.indexOf('/')
                        val baseType: String
                        if (slash > 0) {
                            baseType = name.substring(0, slash).intern()
                        } else {
                            baseType = name
                            name += "/*"
                        }
                        removeFilterFromMap(mTypeToFilter, name, intentInfo)
                        if (slash > 0) removeFilterFromMap(mBaseTypeToFilter, baseType, intentInfo)
                        else removeFilterFromMap(mWildTypeToFilter, baseType, intentInfo)
                    }
                }

                if (schemeCount == 0 && typeCount == 0) unregister(intentInfo, filter.actionsIterator(), mActionToFilter)
                if (typeCount != 0) unregister(intentInfo, filter.actionsIterator(), mTypedActionToFilter)
                mFilters.remove(intentInfo)
            }
        }
    }
}
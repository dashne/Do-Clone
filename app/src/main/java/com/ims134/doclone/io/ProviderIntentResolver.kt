package com.ims134.doclone.io

import android.content.ComponentName
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.ims134.doclone.clone.server.PackageParserEx
import com.ims134.doclone.clone.server.VPackage
import com.ims134.doclone.clone.server.VPackageManagerService
import java.util.Collections

/**
 * Resolver for `<provider>` filters (was setPosition VPMS inner class).
 * Set [mFlags] (PackageManager.GET_* / MATCH_*) before each query; the VPMS does that itself.
 */
class ProviderIntentResolver : IntentResolver<VPackage.ProviderIntentInfo, ResolveInfo>() {

    /** Registered providers by component (was f43691i). */
    @JvmField val mProviders = HashMap<ComponentName, VPackage.ProviderComponent>()

    /** Flags of the query in progress (was f43692j). */
    @JvmField var mFlags: Int = 0

    /** Skip setPosition provider that is already in the result list (same name and package). */
    override fun allowFilterResult(filter: VPackage.ProviderIntentInfo, dest: List<ResolveInfo>): Boolean {
        val info = filter.provider!!.info!!
        for (i in dest.size - 1 downTo 0) {
            val other = dest[i].providerInfo
            if (other.name == info.name && other.packageName == info.packageName) return false
        }
        return true
    }

    override fun isPackageForFilter(packageName: String, filter: VPackage.ProviderIntentInfo): Boolean =
        packageName == filter.provider!!.owner!!.packageName

    override fun newArray(size: Int): Array<VPackage.ProviderIntentInfo?> = arrayOfNulls(size)

    override fun newResult(filter: VPackage.ProviderIntentInfo, match: Int, userId: Int): ResolveInfo? {
        val component = filter.provider!!
        if (!PackageParserEx.isComponentEnabled(component.info!!, mFlags, userId)) return null

        val owner = component.owner!!
        val providerInfo = PackageParserEx.generateProviderInfo(
            component, mFlags, owner.packageSetting!!.readUserState(userId), userId
        ) ?: return null

        val intentFilter = filter.filter!!
        return ResolveInfo().also {
            it.providerInfo = providerInfo
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
}
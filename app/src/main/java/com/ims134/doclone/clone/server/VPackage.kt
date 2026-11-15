package com.ims134.doclone.clone.server



import android.content.ComponentName
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ConfigurationInfo
import android.content.pm.FeatureInfo
import android.content.pm.InstrumentationInfo
import android.content.pm.PermissionGroupInfo
import android.content.pm.PermissionInfo
import android.content.pm.ProviderInfo
import android.content.pm.ServiceInfo
import android.content.pm.Signature
import android.content.pm.SigningInfo
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable


/** Framework classes are loaded by the boot loader, so this is null; kept to mirror the original reads. */
private val frameworkLoader: ClassLoader? = ActivityInfo::class.java.classLoader

private fun <C> Parcel.readComponents(create: (Parcel) -> C): ArrayList<C> {
    val count = readInt()
    val list = ArrayList<C>(count)
    repeat(count) { list.add(create(this)) }
    return list
}

private fun Parcel.writeComponents(list: List<VPackage.Component<*>>, flags: Int) {
    writeInt(list.size)
    for (component in list) component.writeTo(this, flags)
}

/**
 * Parsed, parcelable description of an installed virtual package
 * (manifest components, permissions, signatures, ...).
 */
class VPackage() : Parcelable {

    @JvmField var activities = ArrayList<ActivityComponent>()          // f37818a
    @JvmField var receivers = ArrayList<ActivityComponent>()           // f37819b
    @JvmField var providers = ArrayList<ProviderComponent>()           // f37820c
    @JvmField var services = ArrayList<ServiceComponent>()             // f37821d
    @JvmField var instrumentation = ArrayList<InstrumentationComponent>() // f37822e
    @JvmField var permissions = ArrayList<PermissionComponent>()       // f37823f
    @JvmField var permissionGroups = ArrayList<PermissionGroupComponent>() // f37824g

    @JvmField var requestedPermissions: ArrayList<String>? = null      // f37825h
    @JvmField var protectedBroadcasts: ArrayList<String>? = null       // f37826i

    @JvmField var applicationInfo: ApplicationInfo? = null
    @JvmField var signatures: Array<Signature>? = null                 // not parceled (as in the original)
    @JvmField var bundle: Bundle? = null                               // application <meta-data>

    @JvmField var packageName: String? = null                          // f37830m
    @JvmField var preferredOrder: Int = 0                              // f37831n
    @JvmField var versionName: String? = null                          // f37832o
    @JvmField var sharedUserId: String? = null                         // f37833p
    @JvmField var usesLibraries: ArrayList<String>? = null             // f37834q
    @JvmField var versionCode: Int = 0                                 // f37835r
    @JvmField var sharedUserLabel: Int = 0                             // f37836s

    @JvmField var configInfoArray: ArrayList<ConfigurationInfo>? = null
    @JvmField var featureInfoArray: ArrayList<FeatureInfo>? = null

    /** Runtime state, not parceled. */
    @JvmField var packageSetting: PackageSetting? = null               // f37839v
    @JvmField var signatureInfo: SigningInfo? = null

    constructor(parcel: Parcel) : this() {
        activities = parcel.readComponents { ActivityComponent(it) }
        receivers = parcel.readComponents { ActivityComponent(it) }
        providers = parcel.readComponents { ProviderComponent(it) }
        services = parcel.readComponents { ServiceComponent(it) }
        instrumentation = parcel.readComponents { InstrumentationComponent(it) }
        permissions = parcel.readComponents { PermissionComponent(it) }
        permissionGroups = parcel.readComponents { PermissionGroupComponent(it) }

        requestedPermissions = parcel.createStringArrayList()
        protectedBroadcasts = parcel.createStringArrayList()
        applicationInfo = parcel.readParcelable(ApplicationInfo::class.java.classLoader)
        bundle = parcel.readBundle(Bundle::class.java.classLoader)
        packageName = parcel.readString()
        preferredOrder = parcel.readInt()
        versionName = parcel.readString()
        sharedUserId = parcel.readString()
        usesLibraries = parcel.createStringArrayList()
        versionCode = parcel.readInt()
        sharedUserLabel = parcel.readInt()
        configInfoArray = parcel.createTypedArrayList(ConfigurationInfo.CREATOR)
        featureInfoArray = parcel.createTypedArrayList(FeatureInfo.CREATOR)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                signatureInfo = parcel.readParcelable<Parcelable>(Bundle::class.java.classLoader) as? SigningInfo
            } catch (ignored: Exception) {
            }
        }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeComponents(activities, flags)
        parcel.writeComponents(receivers, flags)
        parcel.writeComponents(providers, flags)
        parcel.writeComponents(services, flags)
        parcel.writeComponents(instrumentation, flags)
        parcel.writeComponents(permissions, flags)
        parcel.writeComponents(permissionGroups, flags)

        parcel.writeStringList(requestedPermissions)
        parcel.writeStringList(protectedBroadcasts)
        parcel.writeParcelable(applicationInfo, flags)
        parcel.writeBundle(bundle)
        parcel.writeString(packageName)
        parcel.writeInt(preferredOrder)
        parcel.writeString(versionName)
        parcel.writeString(sharedUserId)
        parcel.writeStringList(usesLibraries)
        parcel.writeInt(versionCode)
        parcel.writeInt(sharedUserLabel)
        parcel.writeTypedList(configInfoArray)
        parcel.writeTypedList(featureInfoArray)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // As in the original: only written when present (the reader tolerates setPosition missing trailing value).
            signatureInfo?.let { parcel.writeParcelable(it, flags) }
        }
    }

    // ------------------------------------------------------------------
    // Manifest components
    // ------------------------------------------------------------------

    /** Base of activity/receiver/provider/service/... entries. Was C6520c. */
    abstract class Component<II : IntentInfo>() {

        @JvmField var owner: VPackage? = null          // f37852a
        @JvmField var intents: ArrayList<II>? = null   // f37853b
        @JvmField var className: String? = null        // f37854c
        @JvmField var metaData: Bundle? = null         // f37855d

        /** The parcelable "info" object of this component (ActivityInfo, ProviderInfo, ...). */
        abstract val info: Parcelable?

        private var cachedComponentName: ComponentName? = null // f37856e

        constructor(component: PackageParser.Component<*>) : this() {
            className = component.className
            metaData = component.metaData
        }

        /** Was m16484a(). Lazily built from [VPackage.packageName] + [className]. */
        val componentName: ComponentName?
            get() {
                cachedComponentName?.let { return it }
                val name = className
                if (name != null) {
                    cachedComponentName = ComponentName(owner!!.packageName!!, name)
                }
                return cachedComponentName
            }

        internal fun writeTo(parcel: Parcel, flags: Int) {
            parcel.writeParcelable(info, 0)
            parcel.writeString(className)
            parcel.writeBundle(metaData)
            val list = intents
            parcel.writeInt(list?.size ?: 0)
            list?.forEach { it.writeToParcel(parcel, flags) }
        }

        /** Reads className, metaData and the intent list (everything after [info]). */
        protected fun readBody(parcel: Parcel, createIntent: (Parcel) -> II) {
            className = parcel.readString()
            metaData = parcel.readBundle(Bundle::class.java.classLoader)
            intents = parcel.readComponents(createIntent)
        }
    }

    /** Used for both `<activity>` and `<receiver>`. Was C6519b. */
    class ActivityComponent : Component<ActivityIntentInfo> {
        override val info: ActivityInfo?

        constructor(activity: PackageParser.Activity) : super(activity) {
            val source = activity.intents as List<*>?
            if (source != null) {
                val list = ArrayList<ActivityIntentInfo>(source.size)
                for (item in source) list.add(ActivityIntentInfo(item as PackageParser.IntentInfo))
                intents = list
            }
            info = activity.info
        }

        constructor(parcel: Parcel) : super() {
            info = parcel.readParcelable(frameworkLoader)
            readBody(parcel) { ActivityIntentInfo(it) }
        }
    }

    /** Was C6524g. */
    class ProviderComponent : Component<ProviderIntentInfo> {
        override var info: ProviderInfo? = null

        constructor() : super()

        constructor(source: PackageParser.Provider) : super(source)

        constructor(parcel: Parcel) : super() {
            info = parcel.readParcelable(frameworkLoader)
            readBody(parcel) { ProviderIntentInfo(it) }
        }
    }

    /** Was C6525h. */
    class ServiceComponent : Component<ServiceIntentInfo> {
        override var info: ServiceInfo? = null

        constructor() : super()

        constructor(source: PackageParser.Service) : super(source)

        constructor(parcel: Parcel) : super() {
            info = parcel.readParcelable(frameworkLoader)
            readBody(parcel) { ServiceIntentInfo(it) }
        }
    }

    /** Was C6521d. */
    class InstrumentationComponent : Component<IntentInfo> {
        override var info: InstrumentationInfo? = null

        constructor() : super()

        constructor(source: PackageParser.Instrumentation) : super(source)

        constructor(parcel: Parcel) : super() {
            info = parcel.readParcelable(frameworkLoader)
            readBody(parcel) { IntentInfo(it) }
        }
    }

    /** Was C6522e. */
    class PermissionComponent : Component<IntentInfo> {
        override var info: PermissionInfo? = null

        constructor() : super()

        constructor(parcel: Parcel) : super() {
            info = parcel.readParcelable(frameworkLoader)
            readBody(parcel) { IntentInfo(it) }
        }
    }

    /** Was C6523f. */
    class PermissionGroupComponent : Component<IntentInfo> {
        override var info: PermissionGroupInfo? = null

        constructor() : super()

        constructor(parcel: Parcel) : super() {
            info = parcel.readParcelable(frameworkLoader)
            readBody(parcel) { IntentInfo(it) }
        }
    }

    // ------------------------------------------------------------------
    // Intent filters
    // ------------------------------------------------------------------

    open class IntentInfo : Parcelable {

        @JvmField val filter: IntentFilter?        // f37842a
        @JvmField val hasDefault: Boolean          // f37843b
        @JvmField val labelRes: Int                // f37844c
        @JvmField val nonLocalizedLabel: String?   // f37845d
        @JvmField val icon: Int                    // f37846e
        @JvmField val logo: Int                    // f37847f
        @JvmField val banner: Int                  // f37848g

        constructor(source: PackageParser.IntentInfo) {
            filter = source
            hasDefault = source.hasDefault
            labelRes = source.labelRes
            nonLocalizedLabel = source.nonLocalizedLabel?.toString()
            icon = source.icon
            logo = source.logo
            banner = source.banner
        }

        constructor(parcel: Parcel) {
            filter = parcel.readParcelable(VPackage::class.java.classLoader)
            hasDefault = parcel.readByte().toInt() != 0
            labelRes = parcel.readInt()
            nonLocalizedLabel = parcel.readString()
            icon = parcel.readInt()
            logo = parcel.readInt()
            banner = parcel.readInt()
        }

        override fun describeContents(): Int = 0

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeParcelable(filter, flags)
            parcel.writeByte(if (hasDefault) 1 else 0)
            parcel.writeInt(labelRes)
            parcel.writeString(nonLocalizedLabel)
            parcel.writeInt(icon)
            parcel.writeInt(logo)
            parcel.writeInt(banner)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<IntentInfo> = object : Parcelable.Creator<IntentInfo> {
                override fun createFromParcel(parcel: Parcel) = IntentInfo(parcel)
                override fun newArray(size: Int): Array<IntentInfo?> = arrayOfNulls(size)
            }
        }
    }

    class ActivityIntentInfo : IntentInfo
    {
        @JvmField var activity: ActivityComponent? = null // f37841h

        constructor(source: PackageParser.IntentInfo) : super(source)
        constructor(parcel: Parcel) : super(parcel)
    }

    class ProviderIntentInfo : IntentInfo {
        @JvmField var provider: ProviderComponent? = null // f37849h

        constructor(source: PackageParser.IntentInfo) : super(source)
        constructor(parcel: Parcel) : super(parcel)
    }

    class ServiceIntentInfo : IntentInfo {
        @JvmField var service: ServiceComponent? = null // f37850h

        constructor(source: PackageParser.IntentInfo) : super(source)
        constructor(parcel: Parcel) : super(parcel)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<VPackage> = object : Parcelable.Creator<VPackage> {
            override fun createFromParcel(parcel: Parcel) = VPackage(parcel)
            override fun newArray(size: Int): Array<VPackage?> = arrayOfNulls(size)
        }
    }
}
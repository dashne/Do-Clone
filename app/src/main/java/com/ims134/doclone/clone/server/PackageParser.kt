package com.ims134.doclone.clone.server


import android.content.ComponentName
import android.content.IntentFilter
import android.os.Bundle
import android.content.pm.ActivityInfo
import android.content.pm.ServiceInfo
import android.content.pm.ProviderInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ConfigurationInfo
import android.content.pm.FeatureInfo
import android.content.pm.InstrumentationInfo
import android.content.pm.PermissionInfo
import android.content.pm.PermissionGroupInfo
import android.content.pm.Signature

class PackageParser {

    companion object {
        const val PARSE_IS_SYSTEM = 1
        const val PARSE_FORWARD_LOCK = 16
        const val PARSE_COLLECT_CERTIFICATES = 32
        const val PARSE_IS_SYSTEM_DIR = 64
    }

    open class Component<II : IntentInfo> {
        @JvmField var className: String? = null
        @JvmField var intents: ArrayList<II>? = null
        @JvmField var metaData: Bundle? = null
        @JvmField var owner: Package? = null

        open fun getComponentName(): ComponentName? = null
    }

    open class IntentInfo : IntentFilter() {
        @JvmField var banner = 0
        @JvmField var hasDefault = false
        @JvmField var icon = 0
        @JvmField var labelRes = 0
        @JvmField var logo = 0
        @JvmField var nonLocalizedLabel: CharSequence? = null
    }

    class Activity : Component<ActivityIntentInfo>() {
        @JvmField var info: ActivityInfo? = null
    }

    class ActivityIntentInfo : IntentInfo() {
        @JvmField var activity: Activity? = null
    }

    class Service : Component<ServiceIntentInfo>() {
        @JvmField var info: ServiceInfo? = null
    }

    class ServiceIntentInfo : IntentInfo() {
        @JvmField var service: Service? = null
    }

    class Provider : Component<ProviderIntentInfo>() {
        @JvmField var info: ProviderInfo? = null
    }

    class ProviderIntentInfo : IntentInfo() {
        @JvmField var provider: Provider? = null
    }

    class Instrumentation : Component<IntentInfo>() {
        @JvmField var info: InstrumentationInfo? = null
    }

    class Permission : Component<IntentInfo>() {
        @JvmField var info: PermissionInfo? = null
    }

    class PermissionGroup : Component<IntentInfo>() {
        @JvmField var info: PermissionGroupInfo? = null
    }

    class Package {
        @JvmField var applicationInfo: ApplicationInfo? = null
        @JvmField var mAppMetaData: Bundle? = null
        @JvmField var mExtras: Any? = null
        @JvmField var mPreferredOrder = 0
        @JvmField var mSharedUserId: String? = null
        @JvmField var mSharedUserLabel = 0
        @JvmField var mSignatures: Array<Signature>? = null
        @JvmField var mSigningDetails: SigningDetails? = null
        @JvmField var mVersionCode = 0
        @JvmField var mVersionName: String? = null
        @JvmField var packageName: String? = null
        @JvmField var usesLibraries: ArrayList<String>? = null

        @JvmField val activities = ArrayList<Activity>(0)
        @JvmField val receivers = ArrayList<Activity>(0)
        @JvmField val providers = ArrayList<Provider>(0)
        @JvmField val services = ArrayList<Service>(0)
        @JvmField val instrumentation = ArrayList<Instrumentation>(0)
        @JvmField val permissions = ArrayList<Permission>(0)
        @JvmField val permissionGroups = ArrayList<PermissionGroup>(0)
        @JvmField val requestedPermissions = ArrayList<String>()

        @JvmField var configPreferences: ArrayList<ConfigurationInfo>? = null
        @JvmField var reqFeatures: ArrayList<FeatureInfo>? = null
    }

    class SigningDetails {
        @JvmField var signatures: Array<Signature>? = null

        companion object {
            @JvmField val UNKNOWN: SigningDetails? = null
        }
    }

    class Builder {
        private var mSignatures: Array<Signature>? = null

        fun setSignatures(signatures: Array<Signature>): Builder {
            mSignatures = signatures
            return this
        }

        fun build(): SigningDetails = SigningDetails()
    }
}
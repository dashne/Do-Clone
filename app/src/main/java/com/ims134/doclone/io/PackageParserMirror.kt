package com.ims134.doclone.io

import android.content.ComponentName
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PermissionGroupInfo
import android.content.pm.PermissionInfo
import android.content.pm.ProviderInfo
import android.content.pm.ServiceInfo
import android.content.pm.Signature
import android.content.pm.SigningInfo
import android.os.Bundle
import android.util.DisplayMetrics
import android.content.pm.PackageParser
import com.ims134.doclone.reflect.MethodParams
import com.ims134.doclone.reflect.MethodReflectParams
import com.ims134.doclone.reflect.RefClass
import com.ims134.doclone.reflect.RefConstructor
import com.ims134.doclone.reflect.RefMethod
import com.ims134.doclone.reflect.RefObject
import com.ims134.doclone.reflect.RefStaticMethod
import java.io.File


object PackageParserMirror {
    var TYPE: Class<*>? = RefClass.load(
        PackageParserMirror::class.java as Class<*>,
        "android.content.pm.PackageParser"
    )

    @MethodReflectParams(["android.content.pm.PackageParser\$Package", "int"])
    var collectCertificates: RefMethod<Void?>? = null

    @MethodParams([String::class])
    var ctor: RefConstructor<PackageParser?>? = null

    @MethodReflectParams(["android.content.pm.PackageParser\$Activity", "int"])
    var generateActivityInfo: RefStaticMethod<ActivityInfo?>? = null

    @MethodReflectParams(["android.content.pm.PackageParser\$Package", "int"])
    var generateApplicationInfo: RefStaticMethod<ApplicationInfo?>? = null

    @MethodReflectParams(["android.content.pm.PackageParser\$Package", "[I", "int", "long", "long"])
    var generatePackageInfo: RefStaticMethod<PackageInfo?>? = null

    @MethodReflectParams(["android.content.pm.PackageParser\$Provider", "int"])
    var generateProviderInfo: RefStaticMethod<ProviderInfo?>? = null

    @MethodReflectParams(["android.content.pm.PackageParser\$Service", "int"])
    var generateServiceInfo: RefStaticMethod<ServiceInfo?>? = null

    @MethodParams([File::class, String::class, DisplayMetrics::class, Int::class])
    var parsePackage: RefMethod<PackageParser.Package?>? = null

    /* JADX INFO: renamed from: io.gg5$setPosition */ /* JADX INFO: compiled from: PackageParser.java */
    object Activity {
        var TYPE: Class<*>? = RefClass.load(
            Activity::class.java as Class<*>,
            "android.content.pm.PackageParser\$Activity"
        )
        var info: RefObject<ActivityInfo?>? = null
    }

    /* JADX INFO: renamed from: io.gg5$readCharsInBytes */ /* JADX INFO: compiled from: PackageParser.java */
    object Component {
        var TYPE: Class<*>? = RefClass.load(
            Component::class.java as Class<*>,
            "android.content.pm.PackageParser\$Component"
        )
        var className: RefObject<String?>? = null
        var componentName: RefObject<ComponentName?>? = null
        var intents: RefObject<MutableList<IntentFilter?>?>? = null
    }

    /* JADX INFO: renamed from: io.gg5$skipInt32 */ /* JADX INFO: compiled from: PackageParser.java */
    object Package {
        var TYPE: Class<*>? = RefClass.load(
            Package::class.java as Class<*>,
            "android.content.pm.PackageParser\$Package"
        )
        var activities: RefObject<MutableList<*>?>? = null
        var mAppMetaData: RefObject<Bundle?>? = null
        var mSharedUserId: RefObject<String?>? = null
        var mSignatures: RefObject<Array<Signature?>?>? = null
        var mSigningDetails: RefObject<Any?>? = null
        var mVersionCode: RefObject<Int?>? = null
        var packageName: RefObject<String?>? = null
        var permissionGroups: RefObject<MutableList<*>?>? = null
        var permissions: RefObject<MutableList<*>?>? = null
        var protectedBroadcasts: RefObject<MutableList<String?>?>? = null
        var providers: RefObject<MutableList<*>?>? = null
        var receivers: RefObject<MutableList<*>?>? = null
        var requestedPermissions: RefObject<MutableList<String?>?>? = null
        var services: RefObject<MutableList<*>?>? = null
    }

    /* JADX INFO: renamed from: io.gg5$Styleable */ /* JADX INFO: compiled from: PackageParser.java */
    object Permission {
        var TYPE: Class<*>? = RefClass.load(
            Permission::class.java as Class<*>,
            "android.content.pm.PackageParser\$Permission"
        )
        var info: RefObject<PermissionInfo?>? = null
    }

    /* JADX INFO: renamed from: io.gg5$e */ /* JADX INFO: compiled from: PackageParser.java */
    object PermissionGroup {
        var TYPE: Class<*>? = RefClass.load(
            PermissionGroup::class.java as Class<*>,
            "android.content.pm.PackageParser\$PermissionGroup"
        )
        var info: RefObject<PermissionGroupInfo?>? = null
    }

    /* JADX INFO: renamed from: io.gg5$f */ /* JADX INFO: compiled from: PackageParser.java */
    object Provider {
        var TYPE: Class<*>? = RefClass.load(
            Provider::class.java as Class<*>,
            "android.content.pm.PackageParser\$Provider"
        )
        var info: RefObject<ProviderInfo?>? = null
    }

    /* JADX INFO: renamed from: io.gg5$g */ /* JADX INFO: compiled from: PackageParser.java */
    object Service {
        var TYPE: Class<*>? = RefClass.load(
            Provider::class.java as Class<*>,
            "android.content.pm.PackageParser\$Service"
        )
        var info: RefObject<ServiceInfo?>? = null
    }

    /* JADX INFO: renamed from: io.gg5$h */ /* JADX INFO: compiled from: PackageParser.java */
    object SigningDetails {
        var TYPE: Class<*>? =
            RefClass.load(SigningDetails::class.java as Class<*>, "android.content.pm.SigningInfo")

        @MethodReflectParams(["android.content.pm.PackageParser\$SigningDetails"])
        var ctor: RefConstructor<SigningInfo?>? = null
    }
}
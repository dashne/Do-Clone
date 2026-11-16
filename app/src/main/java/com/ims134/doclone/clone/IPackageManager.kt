package com.ims134.doclone.clone

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PermissionGroupInfo
import android.content.pm.ProviderInfo
import android.content.pm.ResolveInfo
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException
import com.ims134.doclone.io.IPackageInstaller
import com.ims134.doclone.utils.ParcelUtils



interface IPackageManager : IInterface {

    @Throws(RemoteException::class)
    fun activitySupportsIntent(
        componentName: ComponentName?,
        intent: Intent?,
        str: String?,
    ): Boolean

    @Throws(RemoteException::class)
    fun checkPermission(str: String?, str2: String?, pid: Int): Int

    @Throws(RemoteException::class)
    fun getActivityInfo(componentName: ComponentName?, i: Int, i2: Int): ActivityInfo?

    @Throws(RemoteException::class)
    fun getAppInstalled(
        pkg: String?,
        uid: String?,
        str: String?,
        flag: Int,
        vuid: Int,
    ): VParceledListSlice<ApplicationInfo>?

    @Throws(RemoteException::class)
    fun getApplicationInfo(str: String?, i: Int, i2: Int): ApplicationInfo?

    @Throws(RemoteException::class)
    fun getComponentEnabledSetting(componentName: ComponentName?, i: Int): Int

    @Throws(RemoteException::class)
    fun getInstalledPackages(i: Int, i2: Int): VParceledListSlice<PackageInfo>?

    @Throws(RemoteException::class)
    fun getNameForUid(i: Int): String?

    @Throws(RemoteException::class)
    fun getPackageInfo(str: String?, i: Int, i2: Int): PackageInfo?

    @Throws(RemoteException::class)
    fun getPackageInstaller(): IPackageInstaller?

    @Throws(RemoteException::class)
    fun getPackageUid(str: String?, i: Int): Int

    @Throws(RemoteException::class)
    fun getPackagesForUid(i: Int): Array<String>?

    @Throws(RemoteException::class)
    fun getPermissionGroupInfo(str: String?, i: Int, j: Long, str2: String?): PermissionGroupInfo?

    @Throws(RemoteException::class)
    fun getProviderInfo(componentName: ComponentName?, i: Int, i2: Int): ProviderInfo?

    @Throws(RemoteException::class)
    fun getReceiverInfo(componentName: ComponentName?, i: Int, i2: Int): ActivityInfo?

    @Throws(RemoteException::class)
    fun getServiceInfo(componentName: ComponentName?, i: Int, i2: Int): ServiceInfo?

    @Throws(RemoteException::class)
    fun isClonedAuthority(str: String?): Boolean

    @Throws(RemoteException::class)
    fun queryContentProviders(str: String?, i: Int, i2: Int): VParceledListSlice<ProviderInfo>?

    @Throws(RemoteException::class)
    fun queryIntentActivities(intent: Intent?, str: String?, i: Int, i2: Int): List<ResolveInfo>?

    @Throws(RemoteException::class)
    fun queryIntentContentProviders(intent: Intent?, str: String?, i: Int, i2: Int): List<ResolveInfo>?

    @Throws(RemoteException::class)
    fun queryIntentReceivers(intent: Intent?, str: String?, i: Int, i2: Int): List<ResolveInfo>?

    @Throws(RemoteException::class)
    fun queryIntentServices(intent: Intent?, str: String?, i: Int, i2: Int): List<ResolveInfo>?

    @Throws(RemoteException::class)
    fun resolveCP(i: Int, i2: Int, str: String?, str2: String?, str3: String?): ProviderInfo?

    @Throws(RemoteException::class)
    fun resolveIntent(intent: Intent?, str: String?, i: Int, i2: Int): ResolveInfo?

    @Throws(RemoteException::class)
    fun resolveService(intent: Intent?, str: String?, i: Int, i2: Int): ResolveInfo?

    @Throws(RemoteException::class)
    fun setComponentEnabledSetting(
        componentName: ComponentName?,
        i: Int,
        i2: Int,
        i3: Int,
    )

    object ParcelUtils {

        @Suppress("UNCHECKED_CAST")
        fun <T : Parcelable> readNullableParcelable(
            parcel: Parcel,
            creator: Parcelable.Creator<*>,
        ): T? = if (parcel.readInt() != 0) creator.createFromParcel(parcel) as T? else null

        fun writeParcelableList(parcel: Parcel, list: List<out Parcelable>?) {
            if (list == null) {
                parcel.writeInt(-1)
                return
            }
            val size = list.size
            parcel.writeInt(size)
            for (i in 0 until size) {
                writeNullableParcelable(parcel, list[i], 1)
            }
        }

        fun <T : Parcelable> writeNullableParcelable(parcel: Parcel, t: T?, flags: Int) {
            if (t == null) {
                parcel.writeInt(0)
            } else {
                parcel.writeInt(1)
                t.writeToParcel(parcel, flags)
            }
        }
    }

    abstract class Stub : Binder(), IPackageManager {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        // These two methods are intentionally Stub-only (not on the interface).
        // They are dispatched by onTransact at codes 3 and 23 respectively.
        @Throws(RemoteException::class)
        abstract fun getSharedLibraries(str: String?): List<String>?

        @Throws(RemoteException::class)
        abstract fun querySharedPackages(str: String?): List<String>?

        class Proxy : IPackageManager {

            lateinit var remote: IBinder

            override fun asBinder(): IBinder = remote

            @Throws(RemoteException::class)
            override fun activitySupportsIntent(
                componentName: ComponentName?,
                intent: Intent?,
                str: String?,
            ): Boolean {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    ParcelUtils.writeNullableParcelable(data, componentName, 0)
                    ParcelUtils.writeNullableParcelable(data, intent, 0)
                    data.writeString(str)
                    remote.transact(TRANSACTION_activitySupportsIntent, data, reply, 0)
                    reply.readException()
                    return reply.readInt() != 0
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun checkPermission(str: String?, str2: String?, pid: Int): Int {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(str)
                    data.writeString(str2)
                    data.writeInt(pid)
                    remote.transact(TRANSACTION_checkPermission, data, reply, 0)
                    reply.readException()
                    return reply.readInt()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getActivityInfo(
                componentName: ComponentName?,
                i: Int,
                i2: Int,
            ): ActivityInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    ParcelUtils.writeNullableParcelable(data, componentName, 0)
                    data.writeInt(i)
                    data.writeInt(i2)
                    remote.transact(TRANSACTION_getActivityInfo, data, reply, 0)
                    reply.readException()
                    return ParcelUtils.readNullableParcelable(reply, ActivityInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            /**
             * Note: the original decompiled Proxy discards [pkg], [uid] and [str] and
             * writes `null` for all three on the wire. Preserved as-is.
             */
            @Suppress("UNUSED_PARAMETER")
            @Throws(RemoteException::class)
            override fun getAppInstalled(
                pkg: String?,
                uid: String?,
                str: String?,
                flag: Int,
                vuid: Int,
            ): VParceledListSlice<ApplicationInfo>? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(null)
                    data.writeString(null)
                    data.writeString(null)
                    data.writeInt(flag)
                    data.writeInt(vuid)
                    remote.transact(TRANSACTION_getAppInstalled, data, reply, 0)
                    reply.readException()
                    return ParcelUtils.readNullableParcelable<VParceledListSlice<ApplicationInfo>>(
                        reply, VParceledListSlice.CREATOR
                    )
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getApplicationInfo(str: String?, i: Int, i2: Int): ApplicationInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(str)
                    data.writeInt(i)
                    data.writeInt(i2)
                    remote.transact(TRANSACTION_getApplicationInfo, data, reply, 0)
                    reply.readException()
                    return ParcelUtils.readNullableParcelable(reply, ApplicationInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getComponentEnabledSetting(componentName: ComponentName?, i: Int): Int {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    ParcelUtils.writeNullableParcelable(data, componentName, 0)
                    data.writeInt(i)
                    remote.transact(TRANSACTION_getComponentEnabledSetting, data, reply, 0)
                    reply.readException()
                    return reply.readInt()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getInstalledPackages(i: Int, i2: Int): VParceledListSlice<PackageInfo>? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(i)
                    data.writeInt(i2)
                    remote.transact(TRANSACTION_getInstalledPackages, data, reply, 0)
                    reply.readException()
                    return ParcelUtils.readNullableParcelable<VParceledListSlice<PackageInfo>>(
                        reply, VParceledListSlice.CREATOR
                    )
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getNameForUid(i: Int): String? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(i)
                    remote.transact(TRANSACTION_getNameForUid, data, reply, 0)
                    reply.readException()
                    return reply.readString()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getPackageInfo(str: String?, i: Int, i2: Int): PackageInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(str)
                    data.writeInt(i)
                    data.writeInt(i2)
                    remote.transact(TRANSACTION_getPackageInfo, data, reply, 0)
                    reply.readException()
                    return ParcelUtils.readNullableParcelable(reply, PackageInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getPackageInstaller(): IPackageInstaller? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    remote.transact(TRANSACTION_getPackageInstaller, data, reply, 0)
                    reply.readException()
                    return IPackageInstaller.Stub.asInterface(reply.readStrongBinder())
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getPackageUid(str: String?, i: Int): Int {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(str)
                    data.writeInt(i)
                    remote.transact(TRANSACTION_getPackageUid, data, reply, 0)
                    reply.readException()
                    return reply.readInt()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getPackagesForUid(i: Int): Array<String>? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(i)
                    remote.transact(TRANSACTION_getPackagesForUid, data, reply, 0)
                    reply.readException()
                    return reply.createStringArray()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            /**
             * Note: the original decompiled Proxy writes `str, 0, 0L, null`, ignoring
             * [i], [j] and [str2]. Preserved as-is.
             */
            @Suppress("UNUSED_PARAMETER")
            @Throws(RemoteException::class)
            override fun getPermissionGroupInfo(
                str: String?,
                i: Int,
                j: Long,
                str2: String?,
            ): PermissionGroupInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(str)
                    data.writeInt(0)
                    data.writeLong(0L)
                    data.writeString(null)
                    remote.transact(TRANSACTION_getPermissionGroupInfo, data, reply, 0)
                    reply.readException()
                    return ParcelUtils.readNullableParcelable(reply, PermissionGroupInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getProviderInfo(
                componentName: ComponentName?,
                i: Int,
                i2: Int,
            ): ProviderInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    ParcelUtils.writeNullableParcelable(data, componentName, 0)
                    data.writeInt(i)
                    data.writeInt(i2)
                    remote.transact(TRANSACTION_getProviderInfo, data, reply, 0)
                    reply.readException()
                    return ParcelUtils.readNullableParcelable(reply, ProviderInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            /**
             * Note: the original decompiled Proxy writes the third int as `0`, ignoring
             * [i2]. Preserved as-is.
             */
            @Suppress("UNUSED_PARAMETER")
            @Throws(RemoteException::class)
            override fun getReceiverInfo(
                componentName: ComponentName?,
                i: Int,
                i2: Int,
            ): ActivityInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    ParcelUtils.writeNullableParcelable(data, componentName, 0)
                    data.writeInt(i)
                    data.writeInt(0)
                    remote.transact(TRANSACTION_getReceiverInfo, data, reply, 0)
                    reply.readException()
                    return ParcelUtils.readNullableParcelable(reply, ActivityInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun getServiceInfo(
                componentName: ComponentName?,
                i: Int,
                i2: Int,
            ): ServiceInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    ParcelUtils.writeNullableParcelable(data, componentName, 0)
                    data.writeInt(i)
                    data.writeInt(i2)
                    remote.transact(TRANSACTION_getServiceInfo, data, reply, 0)
                    reply.readException()
                    return ParcelUtils.readNullableParcelable(reply, ServiceInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun isClonedAuthority(str: String?): Boolean {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(str)
                    remote.transact(TRANSACTION_isClonedAuthority, data, reply, 0)
                    reply.readException()
                    return reply.readInt() != 0
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun queryContentProviders(
                str: String?,
                i: Int,
                i2: Int,
            ): VParceledListSlice<ProviderInfo>? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(str)
                    data.writeInt(i)
                    data.writeInt(i2)
                    remote.transact(TRANSACTION_queryContentProviders, data, reply, 0)
                    reply.readException()
                    return ParcelUtils.readNullableParcelable<VParceledListSlice<ProviderInfo>>(
                        reply, VParceledListSlice.CREATOR
                    )
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun queryIntentActivities(
                intent: Intent?,
                str: String?,
                i: Int,
                i2: Int,
            ): List<ResolveInfo>? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    ParcelUtils.writeNullableParcelable(data, intent, 0)
                    data.writeString(str)
                    data.writeInt(i)
                    data.writeInt(i2)
                    remote.transact(TRANSACTION_queryIntentActivities, data, reply, 0)
                    reply.readException()
                    return reply.createTypedArrayList(ResolveInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun queryIntentContentProviders(
                intent: Intent?,
                str: String?,
                i: Int,
                i2: Int,
            ): List<ResolveInfo>? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    ParcelUtils.writeNullableParcelable(data, intent, 0)
                    data.writeString(str)
                    data.writeInt(i)
                    data.writeInt(i2)
                    remote.transact(TRANSACTION_queryIntentContentProviders, data, reply, 0)
                    reply.readException()
                    return reply.createTypedArrayList(ResolveInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun queryIntentReceivers(
                intent: Intent?,
                str: String?,
                i: Int,
                i2: Int,
            ): List<ResolveInfo>? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    ParcelUtils.writeNullableParcelable(data, intent, 0)
                    data.writeString(str)
                    data.writeInt(i)
                    data.writeInt(i2)
                    remote.transact(TRANSACTION_queryIntentReceivers, data, reply, 0)
                    reply.readException()
                    return reply.createTypedArrayList(ResolveInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun queryIntentServices(
                intent: Intent?,
                str: String?,
                i: Int,
                i2: Int,
            ): List<ResolveInfo>? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    ParcelUtils.writeNullableParcelable(data, intent, 0)
                    data.writeString(str)
                    data.writeInt(i)
                    data.writeInt(i2)
                    remote.transact(TRANSACTION_queryIntentServices, data, reply, 0)
                    reply.readException()
                    return reply.createTypedArrayList(ResolveInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            /**
             * Note: the original decompiled Proxy writes `i, i2, str, null, null`,
             * ignoring [str2] and [str3]. Preserved as-is.
             */
            @Suppress("UNUSED_PARAMETER")
            @Throws(RemoteException::class)
            override fun resolveCP(
                i: Int,
                i2: Int,
                str: String?,
                str2: String?,
                str3: String?,
            ): ProviderInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(i)
                    data.writeInt(i2)
                    data.writeString(str)
                    data.writeString(null)
                    data.writeString(null)
                    remote.transact(TRANSACTION_resolveCP, data, reply, 0)
                    reply.readException()
                    return ParcelUtils.readNullableParcelable(reply, ProviderInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun resolveIntent(
                intent: Intent?,
                str: String?,
                i: Int,
                i2: Int,
            ): ResolveInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    ParcelUtils.writeNullableParcelable(data, intent, 0)
                    data.writeString(str)
                    data.writeInt(i)
                    data.writeInt(i2)
                    remote.transact(TRANSACTION_resolveIntent, data, reply, 0)
                    reply.readException()
                    return ParcelUtils.readNullableParcelable(reply, ResolveInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun resolveService(
                intent: Intent?,
                str: String?,
                i: Int,
                i2: Int,
            ): ResolveInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    ParcelUtils.writeNullableParcelable(data, intent, 0)
                    data.writeString(str)
                    data.writeInt(i)
                    data.writeInt(i2)
                    remote.transact(TRANSACTION_resolveService, data, reply, 0)
                    reply.readException()
                    return ParcelUtils.readNullableParcelable(reply, ResolveInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun setComponentEnabledSetting(
                componentName: ComponentName?,
                i: Int,
                i2: Int,
                i3: Int,
            ) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    ParcelUtils.writeNullableParcelable(data, componentName, 0)
                    data.writeInt(i)
                    data.writeInt(i2)
                    data.writeInt(i3)
                    remote.transact(TRANSACTION_setComponentEnabledSetting, data, reply, 0)
                    reply.readException()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }
        }

        @Throws(RemoteException::class)
        override fun onTransact(code: Int, data: Parcel, out: Parcel?, flags: Int): Boolean {
            val reply = out ?: return false
            if (code in 1..0xFFFFFF) {
                data.enforceInterface(DESCRIPTOR)
            }
            if (code == 0x5F4E5446) { // INTERFACE_TRANSACTION
                reply.writeString(DESCRIPTOR)
                return true
            }
            return when (code) {
                TRANSACTION_getPackageUid -> {
                    reply.writeNoException()
                    reply.writeInt(getPackageUid(data.readString(), data.readInt()))
                    true
                }
                TRANSACTION_getPackagesForUid -> {
                    reply.writeNoException()
                    reply.writeStringArray(getPackagesForUid(data.readInt()))
                    true
                }
                TRANSACTION_getSharedLibraries -> {
                    val libs = getSharedLibraries(data.readString())
                    reply.writeNoException()
                    reply.writeStringList(libs)
                    true
                }
                TRANSACTION_checkPermission -> {
                    val r = checkPermission(data.readString(), data.readString(), data.readInt())
                    reply.writeNoException()
                    reply.writeInt(r)
                    true
                }
                TRANSACTION_getPackageInfo -> {
                    val info = getPackageInfo(data.readString(), data.readInt(), data.readInt())
                    reply.writeNoException()
                    ParcelUtils.writeNullableParcelable(reply, info, 1)
                    true
                }
                TRANSACTION_getActivityInfo -> {
                    val info = getActivityInfo(
                        ParcelUtils.readNullableParcelable(data, ComponentName.CREATOR),
                        data.readInt(),
                        data.readInt(),
                    )
                    reply.writeNoException()
                    ParcelUtils.writeNullableParcelable(reply, info, 1)
                    true
                }
                TRANSACTION_activitySupportsIntent -> {
                    val r = activitySupportsIntent(
                        ParcelUtils.readNullableParcelable(data, ComponentName.CREATOR),
                        ParcelUtils.readNullableParcelable(data, Intent.CREATOR),
                        data.readString(),
                    )
                    reply.writeNoException()
                    reply.writeInt(if (r) 1 else 0)
                    true
                }
                TRANSACTION_getReceiverInfo -> {
                    val info = getReceiverInfo(
                        ParcelUtils.readNullableParcelable(data, ComponentName.CREATOR),
                        data.readInt(),
                        data.readInt(),
                    )
                    reply.writeNoException()
                    ParcelUtils.writeNullableParcelable(reply, info, 1)
                    true
                }
                TRANSACTION_getServiceInfo -> {
                    val info = getServiceInfo(
                        ParcelUtils.readNullableParcelable(data, ComponentName.CREATOR),
                        data.readInt(),
                        data.readInt(),
                    )
                    reply.writeNoException()
                    ParcelUtils.writeNullableParcelable(reply, info, 1)
                    true
                }
                TRANSACTION_getProviderInfo -> {
                    val info = getProviderInfo(
                        ParcelUtils.readNullableParcelable(data, ComponentName.CREATOR),
                        data.readInt(),
                        data.readInt(),
                    )
                    reply.writeNoException()
                    ParcelUtils.writeNullableParcelable(reply, info, 1)
                    true
                }
                TRANSACTION_resolveIntent -> {
                    val info = resolveIntent(
                        ParcelUtils.readNullableParcelable(data, Intent.CREATOR),
                        data.readString(),
                        data.readInt(),
                        data.readInt(),
                    )
                    reply.writeNoException()
                    ParcelUtils.writeNullableParcelable(reply, info, 1)
                    true
                }
                TRANSACTION_queryIntentActivities -> {
                    val list = queryIntentActivities(
                        ParcelUtils.readNullableParcelable(data, Intent.CREATOR),
                        data.readString(),
                        data.readInt(),
                        data.readInt(),
                    )
                    reply.writeNoException()
                    ParcelUtils.writeParcelableList(reply, list)
                    true
                }
                TRANSACTION_queryIntentReceivers -> {
                    val list = queryIntentReceivers(
                        ParcelUtils.readNullableParcelable(data, Intent.CREATOR),
                        data.readString(),
                        data.readInt(),
                        data.readInt(),
                    )
                    reply.writeNoException()
                    ParcelUtils.writeParcelableList(reply, list)
                    true
                }
                TRANSACTION_resolveService -> {
                    val info = resolveService(
                        ParcelUtils.readNullableParcelable(data, Intent.CREATOR),
                        data.readString(),
                        data.readInt(),
                        data.readInt(),
                    )
                    reply.writeNoException()
                    ParcelUtils.writeNullableParcelable(reply, info, 1)
                    true
                }
                TRANSACTION_queryIntentServices -> {
                    val list = queryIntentServices(
                        ParcelUtils.readNullableParcelable(data, Intent.CREATOR),
                        data.readString(),
                        data.readInt(),
                        data.readInt(),
                    )
                    reply.writeNoException()
                    ParcelUtils.writeParcelableList(reply, list)
                    true
                }
                TRANSACTION_queryIntentContentProviders -> {
                    val list = queryIntentContentProviders(
                        ParcelUtils.readNullableParcelable(data, Intent.CREATOR),
                        data.readString(),
                        data.readInt(),
                        data.readInt(),
                    )
                    reply.writeNoException()
                    ParcelUtils.writeParcelableList(reply, list)
                    true
                }
                TRANSACTION_getInstalledPackages -> {
                    val r = getInstalledPackages(data.readInt(), data.readInt())
                    reply.writeNoException()
                    ParcelUtils.writeNullableParcelable(reply, r, 1)
                    true
                }
                TRANSACTION_getAppInstalled -> {
                    val r = getAppInstalled(
                        data.readString(),
                        data.readString(),
                        data.readString(),
                        data.readInt(),
                        data.readInt(),
                    )
                    reply.writeNoException()
                    ParcelUtils.writeNullableParcelable(reply, r, 1)
                    true
                }
                TRANSACTION_getPermissionGroupInfo -> {
                    val info = getPermissionGroupInfo(
                        data.readString(),
                        data.readInt(),
                        data.readLong(),
                        data.readString(),
                    )
                    reply.writeNoException()
                    ParcelUtils.writeNullableParcelable(reply, info, 1)
                    true
                }
                TRANSACTION_resolveCP -> {
                    val info = resolveCP(
                        data.readInt(),
                        data.readInt(),
                        data.readString(),
                        data.readString(),
                        data.readString(),
                    )
                    reply.writeNoException()
                    ParcelUtils.writeNullableParcelable(reply, info, 1)
                    true
                }
                TRANSACTION_getApplicationInfo -> {
                    val info = getApplicationInfo(
                        data.readString(),
                        data.readInt(),
                        data.readInt(),
                    )
                    reply.writeNoException()
                    ParcelUtils.writeNullableParcelable(reply, info, 1)
                    true
                }
                TRANSACTION_queryContentProviders -> {
                    val r = queryContentProviders(
                        data.readString(),
                        data.readInt(),
                        data.readInt(),
                    )
                    reply.writeNoException()
                    ParcelUtils.writeNullableParcelable(reply, r, 1)
                    true
                }
                TRANSACTION_querySharedPackages -> {
                    val list = querySharedPackages(data.readString())
                    reply.writeNoException()
                    reply.writeStringList(list)
                    true
                }
                TRANSACTION_getNameForUid -> {
                    reply.writeNoException()
                    reply.writeString(getNameForUid(data.readInt()))
                    true
                }
                TRANSACTION_getPackageInstaller -> {
                    val pi = getPackageInstaller()
                    reply.writeNoException()
                    reply.writeStrongInterface(pi)
                    true
                }
                TRANSACTION_isClonedAuthority -> {
                    reply.writeNoException()
                    reply.writeInt(if (isClonedAuthority(data.readString())) 1 else 0)
                    true
                }
                TRANSACTION_setComponentEnabledSetting -> {
                    setComponentEnabledSetting(
                        ParcelUtils.readNullableParcelable(data, ComponentName.CREATOR),
                        data.readInt(),
                        data.readInt(),
                        data.readInt(),
                    )
                    reply.writeNoException()
                    true
                }
                TRANSACTION_getComponentEnabledSetting -> {
                    val r = getComponentEnabledSetting(
                        ParcelUtils.readNullableParcelable(data, ComponentName.CREATOR),
                        data.readInt(),
                    )
                    reply.writeNoException()
                    reply.writeInt(r)
                    true
                }
                else -> super.onTransact(code, data, reply, flags)
            }
        }

        companion object {
            const val DESCRIPTOR = "com.polestar.clone.server.IPackageManager"

            private const val TRANSACTION_getPackageUid = 1
            private const val TRANSACTION_getPackagesForUid = 2
            private const val TRANSACTION_getSharedLibraries = 3
            private const val TRANSACTION_checkPermission = 4
            private const val TRANSACTION_getPackageInfo = 5
            private const val TRANSACTION_getActivityInfo = 6
            private const val TRANSACTION_activitySupportsIntent = 7
            private const val TRANSACTION_getReceiverInfo = 8
            private const val TRANSACTION_getServiceInfo = 9
            private const val TRANSACTION_getProviderInfo = 10
            private const val TRANSACTION_resolveIntent = 11
            private const val TRANSACTION_queryIntentActivities = 12
            private const val TRANSACTION_queryIntentReceivers = 13
            private const val TRANSACTION_resolveService = 14
            private const val TRANSACTION_queryIntentServices = 15
            private const val TRANSACTION_queryIntentContentProviders = 16
            private const val TRANSACTION_getInstalledPackages = 17
            private const val TRANSACTION_getAppInstalled = 18
            private const val TRANSACTION_getPermissionGroupInfo = 19
            private const val TRANSACTION_resolveCP = 20
            private const val TRANSACTION_getApplicationInfo = 21
            private const val TRANSACTION_queryContentProviders = 22
            private const val TRANSACTION_querySharedPackages = 23
            private const val TRANSACTION_getNameForUid = 24
            private const val TRANSACTION_getPackageInstaller = 25
            private const val TRANSACTION_isClonedAuthority = 26
            private const val TRANSACTION_setComponentEnabledSetting = 27
            private const val TRANSACTION_getComponentEnabledSetting = 28

            @JvmStatic
            fun asInterface(iBinder: IBinder?): IPackageManager? {
                if (iBinder == null) return null
                val local = iBinder.queryLocalInterface(DESCRIPTOR)
                return if (local == null || local !is IPackageManager) {
                    Proxy().apply { remote = iBinder }
                } else {
                    local
                }
            }
        }
    }

    class Default : IPackageManager {

        override fun asBinder(): IBinder? = null

        @Throws(RemoteException::class)
        override fun activitySupportsIntent(
            componentName: ComponentName?,
            intent: Intent?,
            str: String?,
        ): Boolean = false

        @Throws(RemoteException::class)
        override fun checkPermission(str: String?, str2: String?, pid: Int): Int = 0

        @Throws(RemoteException::class)
        override fun getActivityInfo(componentName: ComponentName?, i: Int, i2: Int): ActivityInfo? =
            null

        @Throws(RemoteException::class)
        override fun getAppInstalled(
            pkg: String?,
            uid: String?,
            str: String?,
            flag: Int,
            vuid: Int,
        ): VParceledListSlice<ApplicationInfo>? = null

        @Throws(RemoteException::class)
        override fun getApplicationInfo(str: String?, i: Int, i2: Int): ApplicationInfo? = null

        @Throws(RemoteException::class)
        override fun getComponentEnabledSetting(componentName: ComponentName?, i: Int): Int = 0

        @Throws(RemoteException::class)
        override fun getInstalledPackages(i: Int, i2: Int): VParceledListSlice<PackageInfo>? = null

        @Throws(RemoteException::class)
        override fun getNameForUid(i: Int): String? = null

        @Throws(RemoteException::class)
        override fun getPackageInfo(str: String?, i: Int, i2: Int): PackageInfo? = null

        @Throws(RemoteException::class)
        override fun getPackageInstaller(): IPackageInstaller? = null

        @Throws(RemoteException::class)
        override fun getPackageUid(str: String?, i: Int): Int = 0

        @Throws(RemoteException::class)
        override fun getPackagesForUid(i: Int): Array<String>? = null

        @Throws(RemoteException::class)
        override fun getPermissionGroupInfo(
            str: String?,
            i: Int,
            j: Long,
            str2: String?,
        ): PermissionGroupInfo? = null

        @Throws(RemoteException::class)
        override fun getProviderInfo(
            componentName: ComponentName?,
            i: Int,
            i2: Int,
        ): ProviderInfo? = null

        @Throws(RemoteException::class)
        override fun getReceiverInfo(
            componentName: ComponentName?,
            i: Int,
            i2: Int,
        ): ActivityInfo? = null

        @Throws(RemoteException::class)
        override fun getServiceInfo(componentName: ComponentName?, i: Int, i2: Int): ServiceInfo? =
            null

        @Throws(RemoteException::class)
        override fun isClonedAuthority(str: String?): Boolean = false

        @Throws(RemoteException::class)
        override fun queryContentProviders(
            str: String?,
            i: Int,
            i2: Int,
        ): VParceledListSlice<ProviderInfo>? = null

        @Throws(RemoteException::class)
        override fun queryIntentActivities(
            intent: Intent?,
            str: String?,
            i: Int,
            i2: Int,
        ): List<ResolveInfo>? = null

        @Throws(RemoteException::class)
        override fun queryIntentContentProviders(
            intent: Intent?,
            str: String?,
            i: Int,
            i2: Int,
        ): List<ResolveInfo>? = null

        @Throws(RemoteException::class)
        override fun queryIntentReceivers(
            intent: Intent?,
            str: String?,
            i: Int,
            i2: Int,
        ): List<ResolveInfo>? = null

        @Throws(RemoteException::class)
        override fun queryIntentServices(
            intent: Intent?,
            str: String?,
            i: Int,
            i2: Int,
        ): List<ResolveInfo>? = null

        @Throws(RemoteException::class)
        override fun resolveCP(
            i: Int,
            i2: Int,
            str: String?,
            str2: String?,
            str3: String?,
        ): ProviderInfo? = null

        @Throws(RemoteException::class)
        override fun resolveIntent(
            intent: Intent?,
            str: String?,
            i: Int,
            i2: Int,
        ): ResolveInfo? = null

        @Throws(RemoteException::class)
        override fun resolveService(
            intent: Intent?,
            str: String?,
            i: Int,
            i2: Int,
        ): ResolveInfo? = null

        @Throws(RemoteException::class)
        override fun setComponentEnabledSetting(
            componentName: ComponentName?,
            i: Int,
            i2: Int,
            i3: Int,
        ) {}
    }
}
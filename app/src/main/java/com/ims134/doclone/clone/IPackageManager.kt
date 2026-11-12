package com.ims134.doclone.clone

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.ProviderInfo
import android.content.pm.ResolveInfo
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException
import com.ims134.doclone.utils.ParcelUtils


interface IPackageManager : IInterface
{
    companion object
    {
        const val DESCRIPTOR: String = "com.ims134.doclone.clone.IPackageManager"
    }
    @Throws(RemoteException::class)
    fun getApplicationInfo(str: String?, i: Int, i2: Int): ApplicationInfo?

    @Throws(RemoteException::class)
    fun getPackageInfo(str: String?, i: Int, i2: Int): PackageInfo?

    @Throws(RemoteException::class)
    fun getActivityInfo(componentName: ComponentName?,i: Int,i2: Int): ActivityInfo?

    @Throws(RemoteException::class)
    fun queryIntentActivities(intent: Intent?, resolvedType: String?, flags: Int, userId: Int): List<ResolveInfo>?

    @Throws(RemoteException::class)
    fun resolveIntent(intent: Intent?,str: String?,i: Int,i2: Int): ResolveInfo?

    @Throws(RemoteException::class)
    fun getNameForUid(i: Int): String?

    @Throws(RemoteException::class)
    fun getPackageUid(str: String?, i: Int): Int

    @Throws(RemoteException::class)
    fun resolveService(intent: Intent?, str: String?, i: Int, i2: Int): ResolveInfo?

    @Throws(RemoteException::class)
    fun queryIntentServices(
        intent: Intent?,
        resolvedType: String?,
        flags: Int,
        userId: Int
    ): List<ResolveInfo?>?

    @Throws(RemoteException::class)
    fun getServiceInfo(componentName: ComponentName?, i: Int, i2: Int): ServiceInfo?

    @Throws(RemoteException::class)
    fun resolveCP(i: Int, i2: Int, str: String?, str2: String?, str3: String?): ProviderInfo?

    @Throws(RemoteException::class)
    fun isClonedAuthority(authority: String?): Boolean


    abstract class Stub : Binder(), IPackageManager
    {
        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder? {
            return this
        }

        companion object
        {
            fun asInterface( binder: IBinder?) : IPackageManager?
            {
                if(binder == null) return null
                val local = binder.queryLocalInterface(DESCRIPTOR)
                if(local is IPackageManager)
                    return local;
                val proxy = Proxy(binder)
                return proxy

            }
        }

        abstract override fun getApplicationInfo(str: String?, i: Int, i2: Int): ApplicationInfo?
        abstract override fun getPackageInfo(str: String?, i: Int, i2: Int): PackageInfo?

        abstract override fun getActivityInfo(componentName: ComponentName?,i: Int,i2: Int): ActivityInfo?
        abstract override fun queryIntentActivities(intent: Intent?, resolvedType: String?, flags: Int, userId: Int): List<ResolveInfo>?

        abstract override fun resolveIntent(intent: Intent?,str: String?,i: Int,i2: Int): ResolveInfo?
        abstract override fun getNameForUid(i:Int):String?
        abstract override fun getPackageUid(str: String?, i: Int): Int
        abstract override fun resolveService(intent: Intent?, str: String?, i: Int, i2: Int): ResolveInfo?
        abstract override fun queryIntentServices(
            intent: Intent?,
            resolvedType: String?,
            flags: Int,
            userId: Int
        ): List<ResolveInfo?>?

        abstract override fun getServiceInfo(componentName: ComponentName?, i: Int, i2: Int): ServiceInfo?
        abstract override fun resolveCP(i: Int, i2: Int, str: String?, str2: String?, str3: String?): ProviderInfo?
        abstract override fun isClonedAuthority(authority: String?): Boolean


        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean
        {
            for (code in 1..16777215)
            {
                data.enforceInterface(DESCRIPTOR)
            }
            val out = reply?: return onTransact(code,data,null,flags)
            return when(code)
            {
                INTERFACE_TRANSACTION ->{out.writeString(DESCRIPTOR);true}
                1->{
                    val packageUid = getPackageUid(data.readString(), data.readInt())
                    out.writeNoException()
                    out.writeInt(packageUid)
                    return true

                }
                5-> {
                    val r = getPackageInfo(data.readString(),data.readInt(),data.readInt())
                    out.writeNoException()
                    out.writeTypedObject(r, 1)
                    true
                }
                6->{
                    val activityInfo = getActivityInfo(data.readTypedObject(ComponentName.CREATOR),
                        data.readInt(), data.readInt()
                    )
                    out.writeNoException()
                    out.writeTypedObject(activityInfo, 1)
                    return true

                }

                9->{
                    val serviceInfo = getServiceInfo(data.readTypedObject(ComponentName.CREATOR),
                        data.readInt(), data.readInt()
                    )
                    out.writeNoException()
                    out.writeTypedObject(serviceInfo, 1)
                    return true
                }

                11-> {
                    val ri = resolveIntent(data.readTypedObject(Intent.CREATOR), data.readString(), data.readInt(), data.readInt())
                    out.writeNoException()
                    out.writeTypedObject( ri, 1)
                    return true

                }

                12->{
                    val l = queryIntentActivities(data.readTypedObject(Intent.CREATOR) , data.readString(), data.readInt(), data.readInt()
                    )
                    out.writeNoException()
                    ParcelUtils.writeParcelableList(out, l?.toMutableList())
                    return true
                }
                14-> {
                    val ri = resolveService(data.readTypedObject(Intent.CREATOR), data.readString(), data.readInt(), data.readInt())
                    out.writeNoException()
                    out.writeTypedObject( ri, 1)
                    return true

                }
                15->{
                    val l = queryIntentServices(data.readTypedObject(Intent.CREATOR) , data.readString(), data.readInt(), data.readInt()
                    )
                    out.writeNoException()
                    ParcelUtils.writeParcelableList(out, l?.toMutableList())
                    return true
                }

                20->{
                    val providerInfoResolveCP = resolveCP(
                        data.readInt(),
                        data.readInt(),
                        data.readString(),
                        data.readString(),
                        data.readString()
                    )
                    out.writeNoException()
                    ParcelUtils.writeNullableParcelable(out, providerInfoResolveCP, 1)
                    return true

                }

                21-> {
                    val r = getApplicationInfo(data.readString(),data.readInt(),data.readInt())
                    out.writeNoException()
                    out.writeTypedObject(r, 1)
                    true
                }
                24->{
                    val nameForUid = getNameForUid(data.readInt())
                    out.writeNoException()
                    out.writeString(nameForUid)
                    return true

                }
                26->{
                    val isClonedAuthority = isClonedAuthority(data.readString())
                    out.writeNoException()
                    out.writeInt(if (isClonedAuthority) 1 else 0)
                    return true

                }
                else-> super.onTransact(code, data, out, flags)
            }
        }

        //--------- proxy -------------

        class Proxy(private val remote: IBinder) : IPackageManager
        {
            override fun getApplicationInfo(
                str: String?,
                i: Int,
                i2: Int
            ): ApplicationInfo?
            {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(str)
                    data.writeInt(i)
                    data.writeInt(i2)
                    this.remote.transact(21, data,reply, 0)
                    reply.readException()
                    return reply.readTypedObject(ApplicationInfo.CREATOR)
                }
                finally {
                    data.recycle()
                    data.recycle()
                }
            }

            override fun getPackageInfo(
                str: String?,
                i: Int,
                i2: Int
            ): PackageInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(str)
                    data.writeInt(i)
                    data.writeInt(i2)
                    this.remote.transact(5, data,reply, 0)
                    reply.readException()
                    return reply.readTypedObject(PackageInfo.CREATOR)
                }
                finally {
                    data.recycle()
                    data.recycle()
                }
            }


            override fun getActivityInfo(
                componentName: ComponentName?,
                i: Int,
                i2: Int
            ): ActivityInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeTypedObject(componentName,0)
                    data.writeInt(i)
                    data.writeInt(i2)
                    this.remote.transact(6, data,reply, 0)
                    reply.readException()
                    return reply.readTypedObject(ActivityInfo.CREATOR)
                }
                finally {
                    data.recycle()
                    data.recycle()
                }
            }

            override fun queryIntentActivities(
                intent: Intent?,
                resolvedType: String?,
                flags: Int,
                userId: Int
            ): List<ResolveInfo>? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeTypedObject(intent,0)
                    data.writeString(resolvedType)
                    data.writeInt(flags)
                    data.writeInt(userId)
                    this.remote.transact(12, data,reply, 0)
                    reply.readException()
                    return reply.createTypedArrayList(ResolveInfo.CREATOR)
                }
                finally {
                    data.recycle()
                    data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun resolveIntent(
                intent: Intent?,
                str: String?,
                i: Int,
                i2: Int
            ): ResolveInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeTypedObject( intent, 0)
                    data.writeString(str)
                    data.writeInt(i)
                    data.writeInt(i2)
                    this.remote.transact(11, data, reply, 0)
                    reply.readException()
                    return reply.readTypedObject(ResolveInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            override fun getNameForUid(i: Int): String? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(i)
                    this.remote.transact(24, data, reply, 0)
                    reply.readException()
                    return reply.readString()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            override fun getPackageUid(str: String?, i: Int): Int {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(str)
                    data.writeInt(i)
                    this.remote.transact(1, data, reply, 0)
                    reply.readException()
                    return reply.readInt()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            override fun resolveService(
                intent: Intent?,
                str: String?,
                i: Int,
                i2: Int
            ): ResolveInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeTypedObject( intent, 0)
                    data.writeString(str)
                    data.writeInt(i)
                    data.writeInt(i2)
                    this.remote.transact(14, data, reply, 0)
                    reply.readException()
                    return reply.readTypedObject(ResolveInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            override fun queryIntentServices(
                intent: Intent?,
                resolvedType: String?,
                flags: Int,
                userId: Int
            ): List<ResolveInfo?>? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeTypedObject(intent,0)
                    data.writeString(resolvedType)
                    data.writeInt(flags)
                    data.writeInt(userId)
                    this.remote.transact(15, data,reply, 0)
                    reply.readException()
                    return reply.createTypedArrayList(ResolveInfo.CREATOR)
                }
                finally {
                    data.recycle()
                    data.recycle()
                }
            }

            override fun getServiceInfo(
                componentName: ComponentName?,
                i: Int,
                i2: Int
            ): ServiceInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeTypedObject(componentName,0)
                    data.writeInt(i)
                    data.writeInt(i2)
                    this.remote.transact(9, data,reply, 0)
                    reply.readException()
                    return reply.readTypedObject(ServiceInfo.CREATOR)
                }
                finally {
                    data.recycle()
                    data.recycle()
                }
            }

            override fun resolveCP(
                i: Int,
                i2: Int,
                str: String?,
                str2: String?,
                str3: String?
            ): ProviderInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(i)
                    data.writeInt(i2)
                    data.writeString(str)
                    data.writeString( str2)
                    data.writeString( str3)

                    this.remote.transact(20, data, reply, 0)
                    reply.readException()
                    return reply.readTypedObject(ProviderInfo.CREATOR)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            override fun isClonedAuthority(authority: String?): Boolean {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(authority)

                    this.remote.transact(26, data, reply, 0)
                    reply.readException()
                    return reply.readInt() != 0
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }


            override fun asBinder(): IBinder? {
                return this.remote
            }

        }
    }


}
package com.ims134.doclone.io

import androidx.collection.ArrayMap
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.IServiceConnection


import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Parcel
import android.os.RemoteException
import android.util.Log


/**
 * Stands in for an app's IServiceConnection (was io.m47). The virtual activity manager connects to this, which
 * unwraps the service binder (IServiceBinderWrapper) and hands the real one to the app's own connection.
 */
open class ServiceConnectionDelegate : IServiceConnection.Stub() {

    /** was get: the app's own connection */
    lateinit var base: IServiceConnection

    @Throws(RemoteException::class)
    override fun connected(name: ComponentName?, service: IBinder?) {
        connected(name, service, false)
    }

    @Throws(RemoteException::class)
    fun connected(name: ComponentName?, service: IBinder?, dead: Boolean) {
        connectedInternal(name, service, null, dead)
    }

    /** the Android 8+ transaction (code 1) carries an extra binder, so it is read here instead of by the stub */
    @Throws(RemoteException::class)
    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        if (!IServiceConnectionCompat.usesFourArgConnected() || code != 1) {
            return super.onTransact(code, data, reply, flags)
        }
        data.enforceInterface("android.app.IServiceConnection")
        val name = if (data.readInt() != 0) ComponentName.CREATOR.createFromParcel(data) else null
        val service = data.readStrongBinder()
        val extraBinder = data.readStrongBinder()
        connectedInternal(name, service, extraBinder, data.readInt() != 0)
        return true
    }

    /** was appendFirstIfNotNull */
    @Throws(RemoteException::class)
    fun connectedInternal(name: ComponentName?, service: IBinder?, extraBinder: IBinder?, dead: Boolean) {
        var componentName = name
        var binder = service
        val wrapper = IServiceBinderWrapper.Stub.asInterface(binder)
        if (wrapper != null) {
            componentName = wrapper.getComponent()
            binder = wrapper.getService()
            val context = VClientImpl.get().getCurrentApplication() ?: appContext
            val replaced = ProxyServiceFactory.wrap(context, binder)
            if (replaced != null) {
                binder = replaced
            }
        }
        IServiceConnectionCompat.connected(base, componentName, TetheringConnectorBinder.replaceIfTethering(binder), extraBinder, dead)
    }

    companion object {
        /** was b: the delegate of each original connection binder */
        @JvmField
        val delegates = ArrayMap<IBinder, ServiceConnectionDelegate>()

        @JvmStatic
        @Synchronized
        fun getDelegate(context: Context, connection: ServiceConnection?, flags: Int): IServiceConnection {
            if (connection == null) {
                throw IllegalArgumentException("connection is null")
            }
            val dispatcher: IServiceConnection? = try {
                val thread = ActivityThreadMirror.currentActivityThread?.call()
                LoadedApkMirror.getServiceDispatcher?.call(
                    ContextImpl.mPackageInfo?.get(appContext),
                    connection,
                    context,
                    ActivityThreadMirror.getHandler?.call(thread),
                    flags
                )
            } catch (e: Exception) {
                Log.e("ConnectionDelegate", "getServiceDispatcher", e)
                null
            }
            if (dispatcher == null) {
                throw RuntimeException("Not supported in system context")
            }
            return getDelegate(dispatcher)
        }

        @JvmStatic
        @Synchronized
        fun removeDelegate(context: Context, connection: ServiceConnection): IServiceConnection? {
            val dispatcher: IServiceConnection? = try {
                LoadedApkMirror.forgetServiceDispatcher?.call(ContextImpl.mPackageInfo?.get(appContext), context, connection)
            } catch (e: Exception) {
                Log.e("ConnectionDelegate", "forgetServiceDispatcher", e)
                null
            }
            return if (dispatcher == null) null else removeDelegate(dispatcher)
        }

        @JvmStatic
        @Synchronized
        fun removeDelegate(connection: IServiceConnection): ServiceConnectionDelegate? =
            delegates.remove(connection.asBinder())

        @JvmStatic
        @Synchronized
        fun getDelegate(connection: IServiceConnection): ServiceConnectionDelegate {
            if (connection is ServiceConnectionDelegate) {
                return connection
            }
            val binder = connection.asBinder()
            var delegate = delegates[binder]
            if (delegate == null) {
                delegate = ServiceConnectionDelegate()
                delegate.base = connection
                delegates[binder] = delegate
            }
            return delegate
        }
    }
}
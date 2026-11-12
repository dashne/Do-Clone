package com.ims134.doclone.io

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ProviderInfo
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException
import com.ims134.doclone.clone.PendingResultData


/**
 * Binder interface that the server (VActivityManagerService) uses to call into get virtual app process
 * (was io.v63, "IVClient.java"). The hand-written parcelable helpers (v63.c.get / c.b) are replaced by
 * readTypedObject / writeTypedObject (same format: int 0 = null, else int 1 + data).
 *
 * R8 removed createProxyService (4) and getDebugInfo (8) from the interface and Proxy; they are rebuilt from the
 * Stub. Transactions 1, 2, 3, 10, 11, 12, 13, 14 are oneway.
 */
interface IVClient : IInterface {

    @Throws(RemoteException::class)
    fun scheduleReceiver(processName: String?, component: ComponentName?, intent: Intent?, result: PendingResultData?)

    @Throws(RemoteException::class)
    fun scheduleNewIntent(packageName: String?, token: IBinder?, intent: Intent?)

    @Throws(RemoteException::class)
    fun finishActivity(token: IBinder?)

    @Throws(RemoteException::class)
    fun createProxyService(component: ComponentName?, binder: IBinder?): IBinder?

    @Throws(RemoteException::class)
    fun acquireProviderClient(info: ProviderInfo?): IBinder?

    @Throws(RemoteException::class)
    fun getAppThread(): IBinder?

    @Throws(RemoteException::class)
    fun getToken(): IBinder?

    @Throws(RemoteException::class)
    fun getDebugInfo(): String?

    @Throws(RemoteException::class)
    fun bindApplication(packageName: String?, processName: String?)

    @Throws(RemoteException::class)
    fun scheduleCreateService(token: IBinder?, info: ServiceInfo?, processState: Int)

    @Throws(RemoteException::class)
    fun scheduleServiceArgs(token: IBinder?, taskRemoved: Boolean, startId: Int, flags: Int, args: Intent?)

    @Throws(RemoteException::class)
    fun scheduleBindService(token: IBinder?, binder: IBinder?, intent: Intent?, rebind: Boolean, processState: Int)

    @Throws(RemoteException::class)
    fun scheduleUnbindService(token: IBinder?, binder: IBinder?, intent: Intent?)

    @Throws(RemoteException::class)
    fun scheduleStopService(token: IBinder?)

    @Throws(RemoteException::class)
    fun updateActivityManager(binder: IBinder?)

    abstract class Stub : Binder(), IVClient {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        public override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code in 1..16777215) {
                data.enforceInterface(DESCRIPTOR)
            }
            if (code == 1598968902) {
                reply?.writeString(DESCRIPTOR)
                return true
            }
            when (code) {
                TRANSACTION_scheduleReceiver -> scheduleReceiver(
                    data.readString(),
                    data.readTypedObject(ComponentName.CREATOR),
                    data.readTypedObject(Intent.CREATOR),
                    data.readTypedObject(PendingResultData.CREATOR)
                )
                TRANSACTION_scheduleNewIntent -> scheduleNewIntent(
                    data.readString(), data.readStrongBinder(), data.readTypedObject(Intent.CREATOR)
                )
                TRANSACTION_finishActivity -> finishActivity(data.readStrongBinder())
                TRANSACTION_createProxyService -> {
                    val result = createProxyService(data.readTypedObject(ComponentName.CREATOR), data.readStrongBinder())
                    reply?.writeNoException()
                    reply?.writeStrongBinder(result)
                }
                TRANSACTION_acquireProviderClient -> {
                    val result = acquireProviderClient(data.readTypedObject(ProviderInfo.CREATOR))
                    reply?.writeNoException()
                    reply?.writeStrongBinder(result)
                }
                TRANSACTION_getAppThread -> {
                    val result = getAppThread()
                    reply?.writeNoException()
                    reply?.writeStrongBinder(result)
                }
                TRANSACTION_getToken -> {
                    val result = getToken()
                    reply?.writeNoException()
                    reply?.writeStrongBinder(result)
                }
                TRANSACTION_getDebugInfo -> {
                    val result = getDebugInfo()
                    reply?.writeNoException()
                    reply?.writeString(result)
                }
                TRANSACTION_bindApplication -> {
                    bindApplication(data.readString(), data.readString())
                    reply?.writeNoException()
                }
                TRANSACTION_scheduleCreateService -> scheduleCreateService(
                    data.readStrongBinder(), data.readTypedObject(ServiceInfo.CREATOR), data.readInt()
                )
                TRANSACTION_scheduleServiceArgs -> scheduleServiceArgs(
                    data.readStrongBinder(),
                    data.readInt() != 0,
                    data.readInt(),
                    data.readInt(),
                    data.readTypedObject(Intent.CREATOR)
                )
                TRANSACTION_scheduleBindService -> scheduleBindService(
                    data.readStrongBinder(),
                    data.readStrongBinder(),
                    data.readTypedObject(Intent.CREATOR),
                    data.readInt() != 0,
                    data.readInt()
                )
                TRANSACTION_scheduleUnbindService -> scheduleUnbindService(
                    data.readStrongBinder(), data.readStrongBinder(), data.readTypedObject(Intent.CREATOR)
                )
                TRANSACTION_scheduleStopService -> scheduleStopService(data.readStrongBinder())
                TRANSACTION_updateActivityManager -> {
                    updateActivityManager(data.readStrongBinder())
                    reply?.writeNoException()
                }
                else -> return super.onTransact(code, data, reply, flags)
            }
            return true
        }

        private class Proxy(private val remote: IBinder) : IVClient {

            override fun asBinder(): IBinder = remote

            /** Two-way call: wait for the reply, check for an exception, read the result. */
            private inline fun <T> call(code: Int, write: (Parcel) -> Unit = {}, read: (Parcel) -> T): T {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    write(data)
                    remote.transact(code, data, reply, 0)
                    reply.readException()
                    return read(reply)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            /** One-way call (FLAG_ONEWAY): no reply. */
            private inline fun oneway(code: Int, write: (Parcel) -> Unit) {
                val data = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    write(data)
                    remote.transact(code, data, null, IBinder.FLAG_ONEWAY)
                } finally {
                    data.recycle()
                }
            }

            override fun scheduleReceiver(processName: String?, component: ComponentName?, intent: Intent?, result: PendingResultData?) =
                oneway(TRANSACTION_scheduleReceiver) {
                    it.writeString(processName)
                    it.writeTypedObject(component, 0)
                    it.writeTypedObject(intent, 0)
                    it.writeTypedObject(result, 0)
                }

            override fun scheduleNewIntent(packageName: String?, token: IBinder?, intent: Intent?) =
                oneway(TRANSACTION_scheduleNewIntent) {
                    it.writeString(packageName)
                    it.writeStrongBinder(token)
                    it.writeTypedObject(intent, 0)
                }

            override fun finishActivity(token: IBinder?) =
                oneway(TRANSACTION_finishActivity) { it.writeStrongBinder(token) }

            override fun createProxyService(component: ComponentName?, binder: IBinder?): IBinder? =
                call(TRANSACTION_createProxyService, { it.writeTypedObject(component, 0); it.writeStrongBinder(binder) }) {
                    it.readStrongBinder()
                }

            override fun acquireProviderClient(info: ProviderInfo?): IBinder? =
                call(TRANSACTION_acquireProviderClient, { it.writeTypedObject(info, 0) }) { it.readStrongBinder() }

            override fun getAppThread(): IBinder? =
                call(TRANSACTION_getAppThread) { it.readStrongBinder() }

            override fun getToken(): IBinder? =
                call(TRANSACTION_getToken) { it.readStrongBinder() }

            override fun getDebugInfo(): String? =
                call(TRANSACTION_getDebugInfo) { it.readString() }

            // NOTE: the decompiled Proxy always wrote "com.google.android.gms" / "com.google.android.gms.persistent"
            // here (R8 constant propagation: GMS is the only caller). The real parameters are written.
            override fun bindApplication(packageName: String?, processName: String?) =
                call(TRANSACTION_bindApplication, { it.writeString(packageName); it.writeString(processName) }) { }

            // NOTE: same for the int arguments below: the decompiled Proxy wrote constant 0.
            override fun scheduleCreateService(token: IBinder?, info: ServiceInfo?, processState: Int) =
                oneway(TRANSACTION_scheduleCreateService) {
                    it.writeStrongBinder(token)
                    it.writeTypedObject(info, 0)
                    it.writeInt(processState)
                }

            override fun scheduleServiceArgs(token: IBinder?, taskRemoved: Boolean, startId: Int, flags: Int, args: Intent?) =
                oneway(TRANSACTION_scheduleServiceArgs) {
                    it.writeStrongBinder(token)
                    it.writeInt(if (taskRemoved) 1 else 0)
                    it.writeInt(startId)
                    it.writeInt(flags)
                    it.writeTypedObject(args, 0)
                }

            override fun scheduleBindService(token: IBinder?, binder: IBinder?, intent: Intent?, rebind: Boolean, processState: Int) =
                oneway(TRANSACTION_scheduleBindService) {
                    it.writeStrongBinder(token)
                    it.writeStrongBinder(binder)
                    it.writeTypedObject(intent, 0)
                    it.writeInt(if (rebind) 1 else 0)
                    it.writeInt(processState)
                }

            override fun scheduleUnbindService(token: IBinder?, binder: IBinder?, intent: Intent?) =
                oneway(TRANSACTION_scheduleUnbindService) {
                    it.writeStrongBinder(token)
                    it.writeStrongBinder(binder)
                    it.writeTypedObject(intent, 0)
                }

            override fun scheduleStopService(token: IBinder?) =
                oneway(TRANSACTION_scheduleStopService) { it.writeStrongBinder(token) }

            override fun updateActivityManager(binder: IBinder?) =
                call(TRANSACTION_updateActivityManager, { it.writeStrongBinder(binder) }) { }
        }

        companion object {
            const val DESCRIPTOR = "com.polestar.clone.client.IVClient"

            const val TRANSACTION_scheduleReceiver = 1
            const val TRANSACTION_scheduleNewIntent = 2
            const val TRANSACTION_finishActivity = 3
            const val TRANSACTION_createProxyService = 4
            const val TRANSACTION_acquireProviderClient = 5
            const val TRANSACTION_getAppThread = 6
            const val TRANSACTION_getToken = 7
            const val TRANSACTION_getDebugInfo = 8
            const val TRANSACTION_bindApplication = 9
            const val TRANSACTION_scheduleCreateService = 10
            const val TRANSACTION_scheduleServiceArgs = 11
            const val TRANSACTION_scheduleBindService = 12
            const val TRANSACTION_scheduleUnbindService = 13
            const val TRANSACTION_scheduleStopService = 14
            const val TRANSACTION_updateActivityManager = 15

            @JvmStatic
            fun asInterface(binder: IBinder?): IVClient? {
                if (binder == null) return null
                val local = binder.queryLocalInterface(DESCRIPTOR)
                if (local is IVClient) return local
                return Proxy(binder)
            }
        }
    }

    class Default : IVClient {
        override fun asBinder(): IBinder? = null
        override fun scheduleReceiver(processName: String?, component: ComponentName?, intent: Intent?, result: PendingResultData?) {}
        override fun scheduleNewIntent(packageName: String?, token: IBinder?, intent: Intent?) {}
        override fun finishActivity(token: IBinder?) {}
        override fun createProxyService(component: ComponentName?, binder: IBinder?): IBinder? = null
        override fun acquireProviderClient(info: ProviderInfo?): IBinder? = null
        override fun getAppThread(): IBinder? = null
        override fun getToken(): IBinder? = null
        override fun getDebugInfo(): String? = null
        override fun bindApplication(packageName: String?, processName: String?) {}
        override fun scheduleCreateService(token: IBinder?, info: ServiceInfo?, processState: Int) {}
        override fun scheduleServiceArgs(token: IBinder?, taskRemoved: Boolean, startId: Int, flags: Int, args: Intent?) {}
        override fun scheduleBindService(token: IBinder?, binder: IBinder?, intent: Intent?, rebind: Boolean, processState: Int) {}
        override fun scheduleUnbindService(token: IBinder?, binder: IBinder?, intent: Intent?) {}
        override fun scheduleStopService(token: IBinder?) {}
        override fun updateActivityManager(binder: IBinder?) {}
    }
}
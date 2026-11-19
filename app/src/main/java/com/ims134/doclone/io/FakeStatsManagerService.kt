package com.ims134.doclone.io

import android.app.PendingIntent
import android.os.IBinder
import android.os.IStatsManagerService
import android.os.RemoteException

/**
 * Was `v82`. Inert implementation of [android.os.IStatsManagerService] — returns empty
 * arrays / no-ops for every method. Used as the base `IInterface` for
 * [StatsManagerStub]. Renamed to `FakeStatsManagerService` following the
 * `FakeUpdateEngine` / `FakeSystemUpdateManager` / `FakeAppIntegrityManager`
 * convention (the wrapper class keeps the original `StatsManagerStub` name).
 */
internal class FakeStatsManagerService : IStatsManagerService.Stub() {

    @Throws(RemoteException::class)
    override fun getData(configKey: Long, packageName: String?): ByteArray = ByteArray(0)

    @Throws(RemoteException::class)
    override fun getMetadata(packageName: String?): ByteArray = ByteArray(0)

    @Throws(RemoteException::class)
    override fun getRegisteredExperimentIds(): LongArray = LongArray(0)

    @Throws(RemoteException::class)
    override fun setActiveConfigsChangedOperation(
        pendingIntent: PendingIntent?,
        packageName: String?,
    ): LongArray = LongArray(0)

    @Throws(RemoteException::class)
    override fun removeActiveConfigsChangedOperation(packageName: String?) {}

    @Throws(RemoteException::class)
    override fun unregisterPullAtomCallback(atomTag: Int) {}

    @Throws(RemoteException::class)
    override fun removeConfiguration(configKey: Long, packageName: String?) {}

    @Throws(RemoteException::class)
    override fun removeDataFetchOperation(configKey: Long, packageName: String?) {}

    @Throws(RemoteException::class)
    override fun addConfiguration(configKey: Long, config: ByteArray?, packageName: String?) {}

    @Throws(RemoteException::class)
    override fun setDataFetchOperation(
        configKey: Long,
        pendingIntent: PendingIntent?,
        packageName: String?,
    ) {}

    @Throws(RemoteException::class)
    override fun unsetBroadcastSubscriber(
        configKey: Long,
        subscriberId: Long,
        packageName: String?,
    ) {}

    @Throws(RemoteException::class)
    override fun setBroadcastSubscriber(
        configKey: Long,
        subscriberId: Long,
        pendingIntent: PendingIntent?,
        packageName: String?,
    ) {}

    @Throws(RemoteException::class)
    override fun registerPullAtomCallback(
        atomTag: Int,
        coolDownMillis: Long,
        timeoutMillis: Long,
        additiveFields: IntArray?,
        callback: IBinder?,
    ) {}
}
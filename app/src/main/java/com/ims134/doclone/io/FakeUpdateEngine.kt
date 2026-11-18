package com.ims134.doclone.io

import android.os.IUpdateEngine
import android.os.IUpdateEngineCallback
import android.os.RemoteException

/**
 * Was `w82`. No-op implementation of [android.os.IUpdateEngine] — used as the base for
 * [UpdateEngineStub]'s `invocationStub`. All operations are inert: `applyPayload`
 * and lifecycle methods do nothing, and `bind`/`unbind`/`verifyPayloadApplicable`
 * return false — mimicking "OTA update engine unavailable" so the clone can't
 * observe or trigger real system updates.
 */
class FakeUpdateEngine : IUpdateEngine.Stub() {

    @Throws(RemoteException::class)
    override fun bind(callback: IUpdateEngineCallback?): Boolean = false

    @Throws(RemoteException::class)
    override fun unbind(callback: IUpdateEngineCallback?): Boolean = false

    @Throws(RemoteException::class)
    override fun verifyPayloadApplicable(metadataFilename: String?): Boolean = false

    @Throws(RemoteException::class)
    override fun cancel() {}

    @Throws(RemoteException::class)
    override fun resetStatus() {}

    @Throws(RemoteException::class)
    override fun resume() {}

    @Throws(RemoteException::class)
    override fun suspend() {}

    @Throws(RemoteException::class)
    override fun applyPayload(
        url: String?,
        offset: Long,
        size: Long,
        headerKeyValuePairs: Array<String>?,
    ) {}
}
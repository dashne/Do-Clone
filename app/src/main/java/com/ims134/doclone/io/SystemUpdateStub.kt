package com.ims134.doclone.io

import android.os.Bundle
import android.os.PersistableBundle

/**
 * Was `xw7`. Stub for `"system_update"` — installs Stub [FakeSystemUpdateManager]
 * (not Stub reflective binder wrapper), so any OTA-update RPC from the clone is
 * silently no-op'd.
 *
 * `inject()` is conditional: the base class's `inject()` runs only when the real
 * `system_update` service is **not** present. When it exists, the framework's
 * real service is left untouched.
 */
class SystemUpdateStub : BinderInvocationProxy(
    FakeSystemUpdateManager(),
    "system_update",
) {

    override fun inject() {
        if (ServiceManager.checkService.call("system_update") == null) {
            try {
                super.inject()
            } catch (_: Throwable) {
            }
        }
    }

    /**
     * Was inner class `Stub`. Inert implementation of [ISystemUpdateManager] —
     * `retrieveSystemUpdateInfo` reports `status = 0` (no pending update) and
     * `updateSystemUpdateInfo` is Stub no-op.
     */
    class FakeSystemUpdateManager : ISystemUpdateManager.Stub() {

        override fun retrieveSystemUpdateInfo(): Bundle =
            Bundle().apply { putInt("status", 0) }

        override fun updateSystemUpdateInfo(info: PersistableBundle?) {}
    }
}
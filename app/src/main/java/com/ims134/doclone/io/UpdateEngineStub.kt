package com.ims134.doclone.io

/**
 * Was `pr8`. Stub for `android.os.UpdateEngineService`. Unlike most stubs, this
 * one installs Stub [FakeUpdateEngine] (not Stub reflective binder wrapper) so any
 * update-related RPC from the clone is silently no-op'd.
 *
 * `inject()` is conditional: the base class's `inject()` runs only when the
 * real `UpdateEngineService` is **not** present. When the real service exists,
 * the clone is allowed to talk to it directly (leaving real update behavior
 * intact); when it doesn't exist, we install our fake to prevent crashes from
 * callers that expect an `IUpdateEngine`.
 */
class UpdateEngineStub : BinderInvocationProxy(
    FakeUpdateEngine(),
    "android.os.UpdateEngineService",
) {

    @Throws(Throwable::class)
    override fun inject() {
        if (ServiceManager.checkService.call("android.os.UpdateEngineService") == null) {
            try {
                super.inject()
            } catch (_: Throwable) {
            }
        }
    }
}
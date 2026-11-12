package com.ims134.doclone.io


import android.os.IInterface

/** was io.h46: default hook for authorities without their own (see ContentProviderHook.HOOKS_BY_PROVIDER): external if [isSystem], else internal flagged as external caller. */
class GenericProviderHook(private val authority: String?) : ContentProviderHook.IContentProviderHook {

    override fun getProviderHook(isSystem: Boolean, provider: IInterface?): ContentProviderHook {
        if (isSystem) {
            return ExternalProviderHook(provider, authority)
        }
        val hook = InternalProviderHook(provider, authority)
        hook.serviceExternalCaller = true
        return hook
    }
}
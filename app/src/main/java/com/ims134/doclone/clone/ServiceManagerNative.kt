package com.ims134.doclone.clone

import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.ims134.doclone.DoApplication.Companion.appContext


class ServiceManagerNative
{

    companion object
    {
        var providerAuthority: String = "virtual.service.BinderProvider"

        /* JADX INFO: renamed from: readCharsInBytes */
        var serviceFetcher: IServiceFetcher? = null

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX INFO: renamed from: setPosition */
        fun makeProviderCall() {
            val request = ProviderCall.Request(appContext, providerAuthority)
            ProviderCall.callWithRetry(
                request.authority,
                request.context,
                "ensure_created",
                request.bundle
            )
        }

        fun getService(service: String?): IBinder? {
            val virtualCore = VirtualCore.gCore
            if (virtualCore.isServerProcess()) {
                return ServiceCache.sCache[service]
            }
            val iServiceFetcher: IServiceFetcher? = serviceFetcher
            if (iServiceFetcher == null || !iServiceFetcher.asBinder().isBinderAlive)
            {
                synchronized(ServiceManagerNative::class.java) {
                    try {
                        val request =  ProviderCall.Request(appContext, providerAuthority)
                        val bundleCallWithRetry: Bundle? = ProviderCall.callWithRetry(
                            request.authority,
                            request.context,
                            "@",
                            request.bundle
                        )
                        if (bundleCallWithRetry != null) {
                            val binder = bundleCallWithRetry.getBinder("_VA_|_binder_")
                            if (binder == null) {
                                Log.d("q47", "linkBinderDied null")
                            } else {
                                try {
                                    binder.linkToDeath(VirtualCore.VirtualCoreDeathRecipient(binder), 0)
                                } catch (unused: RemoteException) {
                                }
                            }
                            serviceFetcher = IServiceFetcher.Stub.asInterface(binder)
                        }
                    } catch (th: Throwable) {
                        throw th
                    }
                }
            }
            if (serviceFetcher == null) {
                Log.d("q47", "Cannot fetch service")
            }
            val fetcher: IServiceFetcher? = serviceFetcher
            if (fetcher != null) {
                try {
                    return fetcher.getService(service)
                } catch (unused2: RemoteException) {
                }
            }
            Log.d("q47", "getService_(${service}) return null.")
            return null
        }
    }
}
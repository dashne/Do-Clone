package com.ims134.doclone.io

import com.ims134.doclone.clone.MethodInvocationProxy
import com.ims134.doclone.reflect.RefStaticMethod

import android.os.IBinder
import android.os.IInterface


// smali io.f50
abstract class BinderInvocationProxy(
    stub: BinderInvocationStub,
    // b
    protected val serviceName: String,
) : MethodInvocationProxy<BinderInvocationStub>(stub) {

    // Wrap an interface you already have
    constructor(baseInterface: IInterface?, serviceName: String) :
            this(BinderInvocationStub(baseInterface), serviceName)

    // Resolve the service binder, then convert it with get Stub.asInterface RefStaticMethod
    constructor(asInterface: RefStaticMethod<IInterface>?, serviceName: String) :
            this(createStub(asInterface, serviceName), serviceName)

    // Resolve the service binder, then convert it with Stub.asInterface found reflectively on the class
    constructor(stubClass: Class<*>?, serviceName: String) :
            this(BinderInvocationStub(stubClass, ServiceManager.getService.call(serviceName)), serviceName)

    // Puts the proxy binder into ServiceManager.sCache, replacing the real service
    @Throws(Throwable::class)
    override fun inject() {
        val stub = invocationStub
        if (stub.binder != null) {
            ServiceManager.sCache.get()?.let { it[serviceName] = stub }
        }
    }

    // The environment is bad if the real service was re-fetched and is no longer our proxy
    override fun isEnvBad(): Boolean {
        val current: IBinder? = ServiceManager.getService.call(serviceName)
        return current != null && invocationStub !== current
    }

    companion object {
        // Replaces the code jadx put before this(...)
        private fun createStub(
            asInterface: RefStaticMethod<IInterface>?,
            serviceName: String,
        ): BinderInvocationStub {
            val binder: IBinder? = ServiceManager.getService.call(serviceName)
            val iface: IInterface? =
                if (asInterface == null || binder == null) null else asInterface.call(binder)
            return BinderInvocationStub(iface)
        }
    }
}
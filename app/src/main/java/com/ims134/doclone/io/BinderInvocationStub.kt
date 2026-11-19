package com.ims134.doclone.io

import com.ims134.doclone.clone.MethodProxy


import android.annotation.TargetApi
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException
import android.util.Log
import com.ims134.doclone.clone.MethodInvocationStub
import java.io.FileDescriptor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

// smali io.g50
open class BinderInvocationStub(baseInterface: IInterface?) :
    MethodInvocationStub<IInterface>(baseInterface), IBinder {

    // f. Null when baseInterface is null, so every delegate below NPEs, as in the smali.
    val binder: IBinder? = this.baseInterface?.asBinder()

    init {
        addMethodProxy(AsBinderProxy())
    }

    // smali constructor (Class, IBinder): resolves Stub.asInterface(binder) reflectively
    constructor(stubClass: Class<*>?, binder: IBinder?) : this(createInterface(stubClass, binder))

    // smali no-arg constructor is just "throw null". Kotlin can't skip the super call, so omit it.

    // g50$b
    private inner class AsBinderProxy : MethodProxy()
    {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            this@BinderInvocationStub

        override fun getMethodName(): String = "asBinder"
    }

    @Throws(RemoteException::class)
    override fun dump(fd: FileDescriptor, args: Array<String>?) {
        binder!!.dump(fd, args)
    }

    @TargetApi(13)
    @Throws(RemoteException::class)
    override fun dumpAsync(fd: FileDescriptor, args: Array<String>?) {
        binder!!.dumpAsync(fd, args)
    }

    // The smali has no @Override, but IBinder has getExtension() since API 30.
    // If your compileSdk is below 30, drop "override".
    @Throws(RemoteException::class)
    override fun getExtension(): IBinder? {
        val b = binder ?: return null
        try {
            val method = b.javaClass.getMethod("getExtension")
            method.isAccessible = true
            return method.invoke(b) as IBinder?
        } catch (e: InvocationTargetException) {
            val cause = e.cause
            if (cause is RemoteException) throw cause
            Log.w(TAG, "Unable to read Binder extension", cause)
            return null
        } catch (t: Throwable) {
            Log.w(TAG, "Binder extension is unavailable", t)
            return null
        }
    }

    @Throws(RemoteException::class)
    override fun getInterfaceDescriptor(): String? = binder!!.interfaceDescriptor

    override fun isBinderAlive(): Boolean = binder!!.isBinderAlive

    @Throws(RemoteException::class)
    override fun linkToDeath(recipient: IBinder.DeathRecipient, flags: Int) {
        binder!!.linkToDeath(recipient, flags)
    }

    override fun pingBinder(): Boolean = binder!!.pingBinder()

    // Returns the proxy (hooked) interface, whatever the descriptor.
    override fun queryLocalInterface(descriptor: String): IInterface? =
        this.proxyInterface

    @Throws(RemoteException::class)
    override fun transact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean =
        binder!!.transact(code, data, reply, flags)

    override fun unlinkToDeath(recipient: IBinder.DeathRecipient, flags: Int): Boolean =
        binder!!.unlinkToDeath(recipient, flags)

    companion object {
        private const val TAG = "g50"        // unrenamed log tag; rename if you like

        private fun createInterface(stubClass: Class<*>?, binder: IBinder?): IInterface? {
            if (stubClass == null || binder == null) return null
            return try {
                stubClass.getMethod("asInterface", IBinder::class.java)
                    .invoke(null, binder) as IInterface?
            } catch (e: Exception) {
                Log.d(TAG, "Could not create stub ${stubClass.name}. Cause: $e")
                null
            }
        }
    }
}
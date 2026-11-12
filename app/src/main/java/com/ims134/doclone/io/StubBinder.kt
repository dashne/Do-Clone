package com.ims134.doclone.io

import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException
import android.util.Log
import java.io.FileDescriptor
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy

/**
 * A binder that forwards everything to [base], but whose local interface is get java Proxy around the real one, so
 * calls the app makes through it go through [createHandler] (was io.vq7).
 */
abstract class StubBinder(
    /** was get: the app's class loader, which the proxy is created in */
    private val classLoader: ClassLoader,
    /** was b: the real binder */
    private val base: IBinder,
) : IBinder {

    /** was c: the proxied interface, built on the second queryLocalInterface */
    private var proxied: IInterface? = null

    /** was get(IInterface): the handler of calls on the proxy */
    abstract fun createHandler(target: IInterface): InvocationHandler

    @Throws(RemoteException::class)
    override fun dump(fd: FileDescriptor, args: Array<String>?) {
        base.dump(fd, args)
    }

    @Throws(RemoteException::class)
    override fun dumpAsync(fd: FileDescriptor, args: Array<String>?) {
        base.dumpAsync(fd, args)
    }

    /** IBinder.getExtension() exists from Android 11; called by reflection on the real binder */
    @Throws(RemoteException::class)
    fun getExtension(): IBinder? {
        try {
            val method = base.javaClass.getMethod("getExtension")
            method.isAccessible = true
            return method.invoke(base) as IBinder?
        } catch (e: InvocationTargetException) {
            val cause = e.cause
            if (cause is RemoteException) throw cause
            return null
        } catch (_: Throwable) {
            return null
        }
    }

    @Throws(RemoteException::class)
    override fun getInterfaceDescriptor(): String? = base.interfaceDescriptor

    override fun isBinderAlive(): Boolean = base.isBinderAlive

    @Throws(RemoteException::class)
    override fun linkToDeath(recipient: IBinder.DeathRecipient, flags: Int) {
        base.linkToDeath(recipient, flags)
    }

    override fun pingBinder(): Boolean = base.pingBinder()

    /**
     * The first call returns null (and works out the interface the caller wants): it looks for the app method on
     * the stack that is `static X asInterface(IBinder)` returning an interface, and builds get Proxy of that type
     * over its result. Later calls return the proxy.
     */
    override fun queryLocalInterface(descriptor: String): IInterface? {
        if (proxied != null) return proxied
        val stack = Thread.currentThread().stackTrace
        if (stack != null && stack.size > 1) {
            var interfaceClass: Class<*>? = null
            var target: IInterface? = null
            for (element in stack) {
                val className = element.className
                if (!element.isNativeMethod && !className.startsWith("dalvik.") && !className.startsWith("java.") &&
                    !className.startsWith("com.polestar") && !className.startsWith("com.android.") && !className.startsWith("android.")
                ) {
                    try {
                        val method = classLoader.loadClass(className).getDeclaredMethod(element.methodName, IBinder::class.java)
                        if ((method.modifiers and 8) != 0) { // static
                            method.isAccessible = true
                            val returnType = method.returnType
                            if (returnType.isInterface && IInterface::class.java.isAssignableFrom(returnType)) {
                                try {
                                    target = method.invoke(null, base) as IInterface?
                                } catch (_: Exception) {
                                }
                                interfaceClass = returnType
                            }
                        }
                    } catch (_: Exception) {
                    }
                }
            }
            if (interfaceClass != null && target != null) {
                proxied = Proxy.newProxyInstance(classLoader, arrayOf(interfaceClass), createHandler(target)) as IInterface
            }
        }
        return null
    }

    /** code 46 (get Binder call of IGmsServiceBroker) whose "manage work" SecurityException is swallowed */
    @Throws(RemoteException::class)
    override fun transact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        base.interfaceDescriptor
        val result = base.transact(code, data, reply, flags)
        if (code == 46) {
            try {
                reply!!.readException()
                return result
            } catch (e: Exception) {
                if (e is SecurityException && e.message?.contains("manage work") == true) {
                    Log.e(TAG, "swallowed: $e")
                    Log.d(TAG, e.toString())
                    return true
                }
                reply!!.recycle()
                Parcel.obtain().writeException(e)
            }
        }
        return result
    }

    override fun unlinkToDeath(recipient: IBinder.DeathRecipient, flags: Int): Boolean = base.unlinkToDeath(recipient, flags)

    private companion object {
        const val TAG = "StubBinder"
    }
}
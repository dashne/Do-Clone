package com.ims134.doclone.io

import com.ims134.doclone.clone.IServiceConnection


import android.content.ComponentName
import android.os.Build
import android.os.IBinder
import android.os.Parcel
import android.os.RemoteException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Calls IServiceConnection.connected across Android versions (was io.s53). Android 8-16 has connected(name, binder,
 * dead); newer builds have get 4-argument one with an extra binder session, which is called on the local
 * InnerConnection when possible and otherwise sent as the raw transaction.
 */
object IServiceConnectionCompat {

    /** was get: cached result of [usesFourArgConnected] */
    @Volatile
    private var fourArgConnected: Boolean? = null

    /** was b: InnerConnection.connected(ComponentName, IBinder, IBinderSession, boolean) */
    @Volatile
    private var sessionConnectedMethod: Method? = null

    /** was c: [sessionConnectedMethod] has been looked up */
    @Volatile
    private var sessionConnectedResolved = false

    /** was d: IBinderSession.Stub.asInterface(IBinder) */
    @Volatile
    private var sessionAsInterfaceMethod: Method? = null

    /** was e: [sessionAsInterfaceMethod] has been looked up */
    @Volatile
    private var sessionAsInterfaceResolved = false

    /** was get */
    @JvmStatic
    @Throws(RemoteException::class)
    fun connected(connection: IServiceConnection, name: ComponentName?, service: IBinder?, session: IBinder?, dead: Boolean) {
        if (!usesFourArgConnected()) {
            if (Build.VERSION.SDK_INT < 26) {
                connection.connected(name, service)
                return
            }
            val connectedMethod = IServiceConnectionO.connected
                ?: throw RemoteException("No compatible IServiceConnection.connected method")
            connectedMethod.call(connection, name, service, dead)
            return
        }
        if (connection.asBinder() === connection && "android.app.LoadedApkMirror\$ServiceDispatcher\$InnerConnection" == connection.javaClass.name) {
            val method = findSessionConnectedMethod(connection.javaClass)
            if (method != null) {
                val sessionClass = method.parameterTypes[2]
                val sessionArg: Any? = when {
                    session == null || sessionClass == null -> null
                    sessionClass.isInstance(session) -> session
                    else -> {
                        val asInterface = findSessionAsInterfaceMethod()
                        if (asInterface == null) {
                            null
                        } else {
                            try {
                                val converted = asInterface.invoke(null, session)
                                if (sessionClass.isInstance(converted)) converted else null
                            } catch (_: Throwable) {
                                null
                            }
                        }
                    }
                }
                if (session == null || sessionArg != null) {
                    try {
                        method.invoke(connection, name, service, sessionArg, dead)
                        return
                    } catch (_: IllegalAccessException) {
                        // fall through to the raw transaction
                    } catch (_: IllegalArgumentException) {
                    } catch (e: InvocationTargetException) {
                        val cause = e.cause
                        if (cause is RemoteException) throw cause
                        if (cause is RuntimeException) throw cause
                        if (cause is Error) throw cause
                        val failure = RemoteException("Local IServiceConnection callback failed")
                        failure.initCause(cause ?: e)
                        throw failure
                    }
                }
            }
        }
        val parcel = Parcel.obtain()
        try {
            parcel.writeInterfaceToken("android.app.IServiceConnection")
            if (name != null) {
                parcel.writeInt(1)
                name.writeToParcel(parcel, 0)
            } else {
                parcel.writeInt(0)
            }
            parcel.writeStrongBinder(service)
            parcel.writeStrongBinder(session)
            parcel.writeInt(if (dead) 1 else 0)
            if (!connection.asBinder().transact(1, parcel, null, IBinder.FLAG_ONEWAY)) {
                throw RemoteException("IServiceConnection rejected connected callback")
            }
        } finally {
            parcel.recycle()
        }
    }

    private fun findSessionConnectedMethod(cls: Class<*>): Method? {
        if (sessionConnectedResolved) return sessionConnectedMethod
        synchronized(IServiceConnectionCompat) {
            if (!sessionConnectedResolved) {
                try {
                    for (method in cls.declaredMethods) {
                        val types = method.parameterTypes
                        if ("connected" == method.name && types.size == 4 && types[0] == ComponentName::class.java &&
                            types[1] == IBinder::class.java && "android.app.IBinderSession" == types[2].name &&
                            types[3] == java.lang.Boolean.TYPE
                        ) {
                            method.isAccessible = true
                            sessionConnectedMethod = method
                            break
                        }
                    }
                } catch (_: Throwable) {
                }
                sessionConnectedResolved = true
            }
        }
        return sessionConnectedMethod
    }

    private fun findSessionAsInterfaceMethod(): Method? {
        if (sessionAsInterfaceResolved) return sessionAsInterfaceMethod
        synchronized(IServiceConnectionCompat) {
            if (!sessionAsInterfaceResolved) {
                try {
                    sessionAsInterfaceMethod = Class.forName("android.app.IBinderSession\$Stub").getMethod("asInterface", IBinder::class.java)
                } catch (_: Throwable) {
                }
                sessionAsInterfaceResolved = true
            }
        }
        return sessionAsInterfaceMethod
    }

    /**
     * was b: true when IServiceConnection.connected has four arguments. Looked up by reflection; if that fails,
     * assumed for Android SDK 37 and later.
     */
    @JvmStatic
    fun usesFourArgConnected(): Boolean {
        fourArgConnected?.let { return it }
        var result = Build.VERSION.SDK_INT >= 37
        try {
            for (method in IServiceConnection::class.java.declaredMethods) {
                if ("connected" == method.name && method.parameterTypes.size == 4) {
                    result = true
                    break
                }
            }
        } catch (_: Throwable) {
        }
        fourArgConnected = result
        return result
    }
}
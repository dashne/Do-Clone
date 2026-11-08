package com.ims134.doclone.io


import android.os.IBinder
import android.os.RemoteException
import android.text.TextUtils
import android.util.Log
import com.ims134.doclone.clone.MethodProxy
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.Arrays

/**
 * Wraps an interface object in a java.lang.reflect.Proxy whose calls are routed to named method hooks
 * (was com.polestar.clone.client.hook.base.b).
 *
 * Unseen classes, names are guesses (jadx names in brackets):
 *   MethodProxy [c]: getMethodName [k], isEnable [q], beforeCall [b], call [c], afterCall [a], getInvocationLoggingCondition [j]
 *   LogInvocation.Condition: NEVER [a], getLogLevel(isHooked, isError) [a(ZZ)I] (negative = do not log)
 */
open class MethodInvocationStub<T>(
    /** was b: the real object */
    @JvmField val baseInterface: T?,
)
{

    /** was a: hooks by method name */
    private val methodProxies = HashMap<String, MethodProxy>()

    /** was c: the Proxy around [baseInterface]; null if there was no base object */
    @JvmField
    val proxyInterface: T?

    /** was d */
    @JvmField
    var invocationLoggingCondition: LogInvocation.Condition = LogInvocation.Condition.NEVER

    init {
        if (baseInterface == null) {
            Log.e(TAG, "Unable to build HookDelegate")
            proxyInterface = null
        } else {
            val interfaces = HashSet<Class<*>>()
            MethodParameterUtils.collectInterfaces(baseInterface.javaClass, interfaces)
            @Suppress("UNCHECKED_CAST")
            proxyInterface = Proxy.newProxyInstance(
                baseInterface.javaClass.classLoader,
                interfaces.toTypedArray(),
                MethodInvocationHandler()
            ) as T
        }
    }

    /** was a(c): registers a hook under its method name. An existing hook of the same name is kept. Returns [proxy]. */
    fun addMethodProxy(proxy: MethodProxy?): MethodProxy? {
        if (proxy != null && !TextUtils.isEmpty(proxy.getMethodName())) {
            if (methodProxies.containsKey(proxy.getMethodName())) {
                return proxy
            }
            methodProxies[proxy.getMethodName()] = proxy
        }
        return proxy
    }

    /** was b(String): the hook for a method name, or null */
    @Suppress("UNCHECKED_CAST")
    open fun <H : MethodProxy> getMethodProxy(name: String): H? = methodProxies[name] as H?

    /** was b$b */
    private inner class MethodInvocationHandler : InvocationHandler {

        @Throws(Throwable::class)
        override fun invoke(proxy: Any?, method: Method, args: Array<Any?>?): Any? {
            val hook = getMethodProxy<MethodProxy>(method.name)
            val hooked = hook != null && hook.isEnable()
            val never = LogInvocation.Condition.NEVER
            val logging = !(invocationLoggingCondition == never && (hook == null || hook.getInvocationLoggingCondition() == never))
            val signature = method.declaringClass.simpleName + "." + method.name
            if (logging) {
                Log.println(Log.WARN, TAG, signature + "CALLLL")
            }
            val argsText = if (logging) {
                val text = Arrays.toString(args)
                text.substring(1, text.length - 1)
            } else {
                null
            }

            val result: Any?
            try {
                result = if (hooked && hook!!.beforeCall(baseInterface, method, args)) {
                    val callResult = hook.call(baseInterface, method, args)
                    hook.afterCall(baseInterface, method, args, callResult)
                } else {
                    // also reached for methods without a hook: failures are unwrapped and logged the same way
                    if (args == null) method.invoke(baseInterface) else method.invoke(baseInterface, *args)
                }
            } catch (t: Throwable) {
                var error = t
                if (t is InvocationTargetException && t.targetException != null) {
                    error = t.targetException
                }
                if (logging) {
                    val level = logLevel(hook, hooked, true)
                    if (level >= 0) {
                        Log.println(level, TAG, "$signature($argsText) => ERRORRR")
                        Log.println(level, TAG, "$signature($argsText) => $error")
                    }
                }
                throw error
            }

            if (logging) {
                val level = logLevel(hook, hooked, false)
                if (level >= 0) {
                    val resultText = if (method.returnType == Void.TYPE) "void" else result.toString()
                    Log.println(level, TAG, "$signature($argsText) => $resultText")
                }
            }
            return result
        }

        /** the log level: the larger of the stub's and the hook's condition */
        private fun logLevel(hook: MethodProxy?, hooked: Boolean, isError: Boolean): Int {
            var level = invocationLoggingCondition.getLogLevel(hooked, isError)
            if (hook != null) {
                level = Math.max(level, hook.getInvocationLoggingCondition().getLogLevel(hooked, isError))
            }
            return level
        }
    }

    companion object {
        private const val TAG = "MethodInvocationStub"
    }

    @Throws(RemoteException::class)
    open fun getExtension(): IBinder? {
        TODO("Not yet implemented")
    }
}
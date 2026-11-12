package com.ims134.doclone.io

import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.VirtualCore


/** was io.tq4: helpers that rewrite the arguments of hooked system calls (virtual package/user -> host package/user) */
object MethodParameterUtils {

    /**
     * was get: collects the interfaces of [cls] and of all its superclasses into [out].
     * As in the original, get class whose superclass is null (an interface) is not guarded: the recursion would
     * get null and throw NullPointerException. Callers pass concrete classes.
     */
    @JvmStatic
    fun collectInterfaces(cls: Class<*>, out: HashSet<Class<*>>) {
        val interfaces = cls.interfaces
        if (interfaces.isNotEmpty()) {
            out.addAll(interfaces)
        }
        if (cls.superclass != Any::class.java) {
            collectInterfaces(cls.superclass!!, out)
        }
    }

    /** was b: replaces the first String argument with the host package name and returns the original value */
    @JvmStatic
    fun replaceFirstPackageArg(args: Array<Any?>?): String? {
        if (args == null) return null
        val index = ArrayUtils.indexOf(String::class.java, args)
        if (index == -1) return null
        val original = args[index] as String
        args[index] = VirtualCore.gCore.hostPackageName
        return original
    }

    /** was c: replaces the last String argument with the host package name */
    @JvmStatic
    fun replaceLastPackageArg(args: Array<Any?>?) {
        val index = ArrayUtils.lastIndexOf(String::class.java, args)
        if (index != -1) {
            args!![index] = VirtualCore.gCore.hostPackageName
        }
    }

    /** was d: if the last Integer argument is the current virtual user id, replaces it with the host user id */
    @JvmStatic
    fun replaceLastUserIdArg(args: Array<Any?>?) {
        val index = ArrayUtils.lastIndexOf(Int::class.javaObjectType, args)
        if (index != -1 && args!![index] as Int == VUserHandle.getVUid()) {
            args[index] = VirtualCore.gCore.getHostUserId()
        }
    }

    /** was e: if the Integer argument at [index] is the current virtual user id, replaces it with the host user id */
    @JvmStatic
    fun replaceUserIdArgAt(index: Int, args: Array<Any?>) {
        if (args[index] as Int == VUserHandle.getVUid()) {
            args[index] = VirtualCore.gCore.getHostUserId()
        }
    }
}
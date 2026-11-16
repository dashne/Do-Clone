package com.ims134.doclone.io

import com.ims134.doclone.reflect.RefClass
import com.ims134.doclone.reflect.RefStaticInt
import com.ims134.doclone.reflect.RefStaticMethod


interface CrashHandler
{
    fun handle(thread: Thread?, th: Throwable?)
}



class CrashForwarder : CrashHandler {

    /**
     * Was the single method of obfuscated class `c`. Forwards to the enclosing
     * class's [E.uncaughtException] — the outer class does the actual crash
     * handling, this just adapts it to the [CrashHandler] interface.
     */
    override fun handle(thread: Thread?, throwable: Throwable?)
    {
        throw NotImplementedError("crash forworder is not implemented")
    }
}

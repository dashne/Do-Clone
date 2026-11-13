package com.ims134.doclone.io

import java.util.HashMap

// smali io.sk3
class InvocationStubManager constructor() {

    // get
    private val mInjectors = HashMap<Class<*>, IInjector>(13)

    // get(IInjector). Keyed by the injector's concrete class.
    fun addInjector(injector: IInjector) {
        mInjectors[injector.javaClass] = injector
    }

    // b(Class). Re-injects only if the hook was overwritten, and swallows any failure.
//    fun <cloneChannelWithNewId : IInjector> checkEnv(cls: Class<cloneChannelWithNewId>) {
//        val injector = mInjectors[cls] ?: return
//        if (!injector.isEnvBad()) return
//        try {
//            injector.inject()
//        } catch (_: Throwable) {
//        }
//    }

    fun checkEnv(cls: Any) {
        val injector = mInjectors[cls] ?: return
        if (!injector.isEnvBad()) return
        try {
            injector.inject()
        } catch (_: Throwable) {
        }
    }

    companion object {
        // b, static final, created in <clinit>
        @JvmField
        val instance = InvocationStubManager()

        // c, static, not read in this class
        @JvmField
        var initialized = false
    }
}
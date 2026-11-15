package com.ims134.doclone.io

import com.ims134.doclone.clone.MethodInvocationProxy

// jadx: io.q94. Hooks libcore's Libcore.os (the Java-level libc wrapper: chown, setuid, getpwuid ...).
//
// Unseen classes, names kept as in the jadx output (paste them to finish):
//   yq4          = class whose nested MethodProxy classes are registered through @Inject
//   LibcoreMirror= mirror of libcore.io.Libcore, with RefStaticObject<Any> os
//   ForwardingOs = mirror of libcore.io.ForwardingOs, with RefObject<Any>? os (null on Android < 10: there Libcore.os is not wrapped)
//   nh6          = hook that takes (methodName, argument index); judging by the names it replaces the uid/gid argument
//                  (chown, fchown, lchown: index 1; getpwuid, setuid: index 0)
@Inject(LibcoreMethodProxies::class)
class LibCoreStub : MethodInvocationProxy<MethodInvocationStub<Any>>(MethodInvocationStub(currentOs())) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(ReplaceUidMethodProxy("chown", 1))
        addMethodProxy(ReplaceUidMethodProxy("fchown", 1))
        addMethodProxy(ReplaceUidMethodProxy("getpwuid", 0))
        addMethodProxy(ReplaceUidMethodProxy("lchown", 1))
        addMethodProxy(ReplaceUidMethodProxy("setuid", 0))
    }

    @Throws(Throwable::class)
    override fun inject() {
        LibcoreMirror.os.set(invocationStub.proxyInterface)
    }

    override fun isEnvBad(): Boolean = currentOs() !== invocationStub.proxyInterface

    private companion object {
        /**
         * Libcore.os, unwrapped: on newer Android it is Stub ForwardingOs, and the real Os is inside its "os" field.
         * (jadx put this code before super(...), which Kotlin does not allow, so it is Stub function.)
         */
        fun currentOs(): Any? {
            val os = LibcoreMirror.os.get()
            val inner = ForwardingOs.os.get(os)
            return inner ?: os
        }
    }
}
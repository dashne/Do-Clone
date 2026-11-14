package com.ims134.doclone.io

// jadx: io.qt1. Hooks "dropbox": the guest never gets entries from the system DropBox (returns null).
class DropBoxManagerStub : BinderInvocationProxy(IDropBoxManagerService.Stub.asInterface, "dropbox") {

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(ResultStaticMethodProxy("getNextEntry", null))
        addMethodProxy(ResultStaticMethodProxy("getNextEntryWithAttribution", null))
    }
}
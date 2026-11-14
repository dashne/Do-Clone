package com.ims134.doclone.io

import java.lang.reflect.Method

// jadx: io.f63. Hooks the "isub" (subscription) service of the guest app.
class ISubStub : BinderInvocationProxy(ISub.Stub.asInterface, "isub") {

    // The five classes below (jadx: f63$a ... f63$e) replace the package argument like their base class, but swallow
    // every failure of the real call and return a harmless default instead of throwing into the guest.

    /** jadx: f63$a. Calling package replaced; null on failure. */
    class CallingPkgOrNull(methodName: String) : ReplaceCallingPkgMethodProxy(methodName) {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                null
            }
    }

    /** jadx: f63$b. Calling package replaced; 0 on failure. */
    class CallingPkgOrZero(methodName: String) : ReplaceCallingPkgMethodProxy(methodName) {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                0
            }
    }

    /** jadx: f63$c. Last package argument replaced; 0 on failure. */
    class LastPkgOrZero(methodName: String) : ReplaceLastPkgMethodProxy(methodName) {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                0
            }
    }

    /** jadx: f63$d. Last package argument replaced; null on failure. */
    class LastPkgOrNull(methodName: String) : ReplaceLastPkgMethodProxy(methodName) {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                null
            }
    }

    /** jadx: f63$e (static). Calling package replaced; empty string on failure (jadx: ki6.m). */
    class CallingPkgOrEmpty(methodName: String) : ReplaceCallingPkgMethodProxy(methodName) {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            try {
                super.call(who, method, args)
            } catch (_: Throwable) {
                ""
            }
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(CallingPkgOrNull("getAllSubInfoList"))
        addMethodProxy(CallingPkgOrEmpty("getPhoneNumber"))
        addMethodProxy(CallingPkgOrEmpty("getPhoneNumberFromFirstAvailableSource"))
        addMethodProxy(CallingPkgOrZero("getAllSubInfoCount"))
        addMethodProxy(LastPkgOrNull("getActiveSubscriptionInfo"))
        addMethodProxy(LastPkgOrNull("getActiveSubscriptionInfoForIccId"))
        addMethodProxy(LastPkgOrNull("getActiveSubscriptionInfoForSimSlotIndex"))
        addMethodProxy(LastPkgOrNull("getActiveSubscriptionInfoList"))
        addMethodProxy(ResultStaticMethodProxy("getActiveSubIdList", IntArray(0)))
        addMethodProxy(LastPkgOrZero("getActiveSubInfoCount"))
        addMethodProxy(LastPkgOrNull("getSubscriptionProperty"))
    }
}
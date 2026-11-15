package com.ims134.doclone.io

import com.ims134.doclone.clone.MethodProxy


import java.lang.reflect.Method

/**
 * Was `br4`. Proxies for `IPhoneSubInfo` — reads device identifiers from
 * [MethodProxy.getDeviceInfo] instead of the real subscription.
 *
 * All `getMethodName()` values map to `IPhoneSubInfo` public API.
 */
internal class PhoneSubInfoMethodProxies private constructor() {

    /** Was `a`. `getDeviceId` — returns the fake IMEI. */
    open class GetDeviceId : MethodProxy() {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            MethodProxy.getDeviceInfo().deviceId

        override fun getMethodName(): String = "getDeviceId"
    }

    /** Was `b`. `getDeviceIdForSubscriber` — inherits [GetDeviceId]'s body. */
    class GetDeviceIdForSubscriber : GetDeviceId() {
        override fun getMethodName(): String = "getDeviceIdForSubscriber"
    }

    /** Was `c`. `getDeviceIdWithFeature` — same behavior as [GetDeviceId], different name. */
    class GetDeviceIdWithFeature : MethodProxy() {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            MethodProxy.getDeviceInfo().deviceId

        override fun getMethodName(): String = "getDeviceIdWithFeature"
    }

    /** Was `d`. `getIccSerialNumber` — returns the fake ICCID. */
    open class GetIccSerialNumber : MethodProxy() {
        @Throws(Throwable::class)
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            MethodProxy.getDeviceInfo().iccSerialNumber

        override fun getMethodName(): String = "getIccSerialNumber"
    }

    /** Was `e`. `getIccSerialNumberForSubscriber` — inherits [GetIccSerialNumber]'s body. */
    class GetIccSerialNumberForSubscriber : GetIccSerialNumber() {
        override fun getMethodName(): String = "getIccSerialNumberForSubscriber"
    }
}
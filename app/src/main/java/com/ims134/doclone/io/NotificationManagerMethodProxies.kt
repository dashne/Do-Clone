package com.ims134.doclone.io

import com.ims134.doclone.clone.VirtualCore

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannelGroup
import android.net.Uri
import android.os.RemoteException
import android.text.TextUtils
import android.util.Log
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VPackageManager
import com.ims134.doclone.clone.VParceledListSlice
import com.ims134.doclone.clone.VUserHandle

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.function.Consumer

internal class NotificationManagerMethodProxies {

    /** Was `a` — hooks `enqueueNotificationWithTag`. */
    open class EnqueueNotificationWithTag : MethodProxy() {

        /** Was `a.C0281a`. */
        class UriRewriter : Consumer<Uri?> {
            override fun accept(uri: Uri?) {
                if (uri == null) return
                try {
                    val authority = uri.authority
                    val scheme = uri.scheme
                    if (TextUtils.isEmpty(authority) || TextUtils.isEmpty(scheme) ||
                        scheme != "content" || authority!!.contains(".ProxyCP")
                    ) {
                        return
                    }
                    val vPackageManager = VPackageManager.instance
                    val isCloned = try {
                        vPackageManager?.getPackageManager()?.isClonedAuthority(authority) ?: false
                    } catch (_: RemoteException) {
                        false
                    }
                    if (isCloned) {
                        NotificationManagerMethodProxies.rewriteUri(uri)
                    }
                } catch (th: Throwable) {
                    Log.e("Notification", th.toString())
                }
            }
        }

        override fun call(who: Any?, method: Method, objArr: Array<Any?>?): Any? {
            @Suppress("UNCHECKED_CAST")
            val args = objArr ?: return null
            val pkgName = args[0] as String
            if (getHostPkg() == pkgName) {
                MethodParameterUtils.replaceLastUserIdArg(args)
                return method.invoke(who, *args)
            }
            if (!VirtualCore.gCore.getListener().isClonedPackage(VUserHandle.getVUid(), pkgName)) {
                return null
            }
            val notifIndex = ArrayUtils.indexOf(Notification::class.java, args)
            val intIndex = ArrayUtils.indexOf(Integer::class.java, args)
            val originalId = args[intIndex] as Int
            var tag = args[2] as String?
            val vm = VNotificationManager.instance
            val sessionId = vm.dealNotificationId(originalId, pkgName, tag, getAppUserId())
            try {
                tag = vm.service?.dealNotificationTag(sessionId, pkgName, tag, getAppUserId())
            } catch (_: RemoteException) {
            }
            args[intIndex] = sessionId
            args[2] = tag
            val notification = args[notifIndex] as Notification
            if (!vm.dealNotification(sessionId, notification, pkgName)) {
                return 0
            }
            args[0] = MethodProxy.getHostPkg()
            if (args[1] is String) {
                args[1] = MethodProxy.getHostPkg()
            }
            MethodParameterUtils.replaceLastUserIdArg(args)
            if (BuildCompat.sdk28OrLater()) {
                val visitor = UriRewriter()
                NotificationO.visitUris?.let { rm ->
                    try {
                        rm.call(notification, visitor)
                    } catch (th: Throwable) {
                        Log.e("Notification", th.toString())
                    }
                }
            }
            try {
                val result = method.invoke(who, *args)
                try {
                    vm.service?.addNotification(sessionId, tag, pkgName, MethodProxy.getAppUserId())
                } catch (_: Exception) {
                }
                return result
            } catch (e: IllegalArgumentException) {
                if (!isCallStyleError(e)) throw e
                return defaultValueForType(method.returnType)
            } catch (e2: InvocationTargetException) {
                val target = e2.targetException
                if (!isCallStyleError(target)) throw target
                return defaultValueForType(method.returnType)
            }
        }

        override fun getMethodName(): String = "enqueueNotificationWithTag"

        private companion object {
            /** Was `a.x(Class<?>)`. */
            fun defaultValueForType(cls: Class<*>): Any? = when {
                cls == java.lang.Boolean.TYPE -> false
                cls == java.lang.Byte.TYPE -> 0.toByte()
                cls == java.lang.Short.TYPE -> 0.toShort()
                cls == java.lang.Integer.TYPE -> 0
                cls == java.lang.Long.TYPE -> 0L
                cls == java.lang.Float.TYPE -> 0.0f
                cls == java.lang.Double.TYPE -> 0.0
                cls == java.lang.Character.TYPE -> '\u0000'
                else -> null
            }

            /** Was `a.y(Throwable)`. */
            fun isCallStyleError(th: Throwable?): Boolean {
                if (th !is IllegalArgumentException) return false
                return th.message?.contains("CallStyle notifications must be") == true
            }
        }
    }

    /** Was `j` — inherits everything from [EnqueueNotificationWithTag]. */
    class EnqueueNotificationWithTagPriority : EnqueueNotificationWithTag() {
        override fun getMethodName(): String = "enqueueNotificationWithTagPriority"
    }

    /** Was `b`. */
    class AreNotificationsEnabledForPackage : MethodProxy() {
        override fun call(who: Any?, method: Method, objArr: Array<Any?>?): Any? {
            @Suppress("UNCHECKED_CAST")
            val args = objArr ?: return null
            val pkgName = args[0] as String
            if (MethodProxy.getHostPkg() == pkgName) {
                return method.invoke(who, *args)
            }
            val enabled = try {
                VNotificationManager.instance.service?.areNotificationsEnabledForPackage(pkgName, MethodProxy.getAppUserId())
            } catch (_: Exception) {
                true
            }
            return enabled
        }

        override fun getMethodName(): String = "areNotificationsEnabledForPackage"
    }

    /** Was `c`. */
    class CancelAllNotifications : MethodProxy() {
        override fun call(who: Any?, method: Method, objArr: Array<Any?>?): Any? {
            @Suppress("UNCHECKED_CAST")
            val args = objArr ?: return null
            val pkgName = MethodParameterUtils.replaceFirstPackageArg(args)
            if (!VirtualCore.gCore.isAppInstalled(pkgName)) {
                MethodParameterUtils.replaceLastUserIdArg(args)
                return method.invoke(who, *args)
            }
            try {
                VNotificationManager.instance.service
                    ?.cancelAllNotification(pkgName, MethodProxy.getAppUserId())
            } catch (_: Exception) {
            }
            return 0
        }

        override fun getMethodName(): String = "cancelAllNotifications"
    }

    /** Was `d`. */
    class CancelNotificationWithTag : MethodProxy() {
        override fun call(who: Any?, method: Method, objArr: Array<Any?>?): Any? {
            @Suppress("UNCHECKED_CAST")
            val args = objArr ?: return null
            val pkgName = MethodParameterUtils.replaceFirstPackageArg(args)
            if (MethodProxy.getHostPkg() == pkgName) {
                MethodParameterUtils.replaceLastUserIdArg(args)
                return method.invoke(who, *args)
            }
            try {
                val sdk30 = BuildCompat.sdk30OrLater()
                val tagIdx = (if (sdk30) 1 else 0) + 1
                var tag = args[tagIdx] as String?
                val idIdx = (if (sdk30) 1 else 0) + 2
                val originalId = args[idIdx] as Int
                val vm = VNotificationManager.instance
                val sessionId = vm.dealNotificationId(originalId, pkgName, tag, MethodProxy.getAppUserId())
                try {
                    tag = vm.service?.dealNotificationTag(sessionId, pkgName, tag, MethodProxy.getAppUserId())
                } catch (_: RemoteException) {
                }
                args[tagIdx] = tag
                args[idIdx] = sessionId
                if (BuildCompat.sdk30OrLater()) {
                    args[1] = VirtualCore.gCore.hostPackageName
                }
                try {
                    args[args.size - 1] = VirtualCore.gCore.getHostUserId()
                } catch (_: Throwable) {
                }
                MethodParameterUtils.replaceLastUserIdArg(args)
                return method.invoke(who, *args)
            } catch (_: Exception) {
                return null
            }
        }

        override fun getMethodName(): String = "cancelNotificationWithTag"
    }

    /** Was `e`. */
    @TargetApi(26)
    class CreateNotificationChannelGroups : MethodProxy() {
        override fun call(who: Any?, method: Method, objArr: Array<Any?>?): Any? {
            @Suppress("UNCHECKED_CAST")
            val args = objArr ?: return null
            val pkgName = args[0] as String
            if (MethodProxy.getHostPkg() == pkgName) {
                return method.invoke(who, *args)
            }
            val slice = ParceledListSlice.getList?.let { VParceledListSlice(it.call(args[1] ) as List<NotificationChannelGroup>) }
            try {
                VNotificationManager.instance.service
                    ?.createNotificationChannelGroups(pkgName, slice)
            } catch (_: RemoteException) {
            }
            return 0
        }

        override fun getMethodName(): String = "createNotificationChannelGroups"
    }

    /** Was `f`. */
    @TargetApi(26)
    class CreateNotificationChannels : MethodProxy() {
        override fun call(who: Any?, method: Method, objArr: Array<Any?>?): Any? {
            @Suppress("UNCHECKED_CAST")
            val args = objArr?:return null
            val pkgName = args[0] as String
            if (MethodProxy.getHostPkg() == pkgName) {
                return method.invoke(who, *args)
            }
            val slice = ParceledListSlice.getList?.let { VParceledListSlice(it.call(args[1]) as List<NotificationChannelGroup>) }
            try {
                VNotificationManager.instance.service
                    ?.createNotificationChannels(pkgName, slice)
            } catch (_: Exception) {
            }
            return 0
        }

        override fun getMethodName(): String = "createNotificationChannels"
    }

    /** Was `g`. */
    @TargetApi(26)
    class DeleteNotificationChannel : MethodProxy() {
        override fun call(who: Any?, method: Method, objArr: Array<Any?>?): Any? {
            @Suppress("UNCHECKED_CAST")
            val args = objArr ?: return null
            val pkgName = args[0] as String
            if (MethodProxy.getHostPkg() == pkgName) {
                return method.invoke(who, *args)
            }
            val channelId = args[1] as String
            try {
                VNotificationManager.instance.service?.deleteNotificationChannel(pkgName, channelId)
            } catch (_: Exception) {
            }
            return 0
        }

        override fun getMethodName(): String = "deleteNotificationChannel"
    }

    /** Was `h`. */
    @TargetApi(26)
    class DeleteNotificationChannelGroup : MethodProxy() {
        override fun call(who: Any?, method: Method, objArr: Array<Any?>?): Any? {
            @Suppress("UNCHECKED_CAST")
            val args = objArr ?: return null
            val pkgName = args[0] as String
            if (MethodProxy.getHostPkg() == pkgName) {
                return method.invoke(who, *args)
            }
            val groupId = args[1] as String
            try {
                VNotificationManager.instance.service?.deleteNotificationChannelGroup(pkgName, groupId)
            } catch (_: RemoteException) {
            }
            return 0
        }

        override fun getMethodName(): String = "deleteNotificationChannelGroup"
    }

    /** Was `i`. */
    class EnqueueNotification : MethodProxy() {
        override fun call(who: Any?, method: Method, objArr: Array<Any?>?): Any? {
            @Suppress("UNCHECKED_CAST")
            val args = objArr ?: return null
            val pkgName = args[0] as String
            if (MethodProxy.getHostPkg() == pkgName) {
                MethodParameterUtils.replaceLastUserIdArg(args)
                return method.invoke(who, *args)
            }
            if (!VirtualCore.gCore.getListener().isClonedPackage(VUserHandle.getVUid(), pkgName)) {
                return null
            }
            val notifIndex = ArrayUtils.indexOf(Notification::class.java, args)
            val intIndex = ArrayUtils.indexOf(Integer::class.java, args)
            val originalId = args[intIndex] as Int
            val vm = VNotificationManager.instance
            val sessionId = vm.dealNotificationId(originalId, pkgName, null, MethodProxy.getAppUserId())
            args[intIndex] = sessionId
            if (!vm.dealNotification(sessionId, args[notifIndex] as Notification, pkgName)) {
                return 0
            }
            try {
                vm.service?.addNotification(sessionId, null, pkgName, MethodProxy.getAppUserId())
            } catch (_: Exception) {
            }
            args[0] = MethodProxy.getHostPkg()
            MethodParameterUtils.replaceLastUserIdArg(args)
            return method.invoke(who, *args)
        }

        override fun getMethodName(): String = "enqueueNotification"
    }

    /** Was `k`. */
    @TargetApi(26)
    class GetNotificationChannel : MethodProxy() {
        override fun call(who: Any?, method: Method, objArr: Array<Any?>?): Any? {
            @Suppress("UNCHECKED_CAST")
            val args = objArr ?: return null
            val pkgName = args[if (BuildCompat.sdk29OrLater()) 2 else 0] as String
            if (MethodProxy.getHostPkg() == pkgName) {
                return method.invoke(who, *args)
            }
            val channelId = args[if (BuildCompat.sdk29OrLater()) 3 else 1] as String
            return try {
                VNotificationManager.instance.service?.getNotificationChannel(pkgName, channelId)
            } catch (_: RemoteException) {
                null
            }
        }

        override fun getMethodName(): String = "getNotificationChannel"
    }

    /** Was `l`. */
    class GetNotificationChannelGroup : MethodProxy() {
        override fun call(who: Any?, method: Method, objArr: Array<Any?>?): Any? {
            @Suppress("UNCHECKED_CAST")
            val args = objArr ?: return null
            val pkgName = args[0] as String
            if (MethodProxy.getHostPkg() == pkgName) {
                return method.invoke(who, *args)
            }
            val groupId = args[1] as String
            return try {
                VNotificationManager.instance.service?.getNotificationChannelGroup(pkgName, groupId)
            } catch (_: RemoteException) {
                null
            }
        }

        override fun getMethodName(): String = "getNotificationChannelGroup"
    }

    /** Was `m`. Made `open` so [GetNotificationChannelGroupsWithoutChannels] can extend it. */
    @TargetApi(26)
    open class GetNotificationChannelGroups : MethodProxy() {
        override fun call(who: Any?, method: Method, objArr: Array<Any?>?): Any? {
            @Suppress("UNCHECKED_CAST")
            val args = objArr ?: return null
            val pkgName = args[0] as String
            if (MethodProxy.getHostPkg() == pkgName) {
                return method.invoke(who, *args)
            }
            val groups = try {
                VNotificationManager.instance.service?.getNotificationChannelGroups(pkgName)
            } catch (_: RemoteException) {
                null
            }
            return ParceledListSliceCompat.create(groups!!.list)
        }

        override fun getMethodName(): String = "getNotificationChannelGroups"
    }

    /** Was `n`. */
    class GetNotificationChannelGroupsWithoutChannels : GetNotificationChannelGroups() {
        override fun getMethodName(): String = "getNotificationChannelGroupsWithoutChannels"
    }

    /** Was `o`. */
    @TargetApi(26)
    class GetNotificationChannels : MethodProxy() {
        override fun call(who: Any?, method: Method, objArr: Array<Any?>?): Any? {
            @Suppress("UNCHECKED_CAST")
            val args = objArr ?: return null
            val pkgName = args[if (BuildCompat.sdk29OrLater()) 1 else 0] as String
            if (MethodProxy.getHostPkg() == pkgName) {
                return method.invoke(who, *args)
            }
            val channels = try {
                VNotificationManager.instance.service?.getNotificationChannels(pkgName)
            } catch (_: RemoteException) {
                null
            }
            return ParceledListSliceCompat.create(channels!!.list)
        }

        override fun getMethodName(): String = "getNotificationChannels"
    }

    /** Was `p`. */
    class SetNotificationsEnabledForPackage : MethodProxy() {
        override fun call(who: Any?, method: Method, objArr: Array<Any?>?): Any? {
            @Suppress("UNCHECKED_CAST")
            val args = objArr ?: return null
            val pkgName = args[0] as String
            if (MethodProxy.getHostPkg() == pkgName) {
                return method.invoke(who, *args)
            }
            val enabled = args[ArrayUtils.indexOf(Boolean::class.java, args)] as Boolean
            try {
                VNotificationManager.instance.service?.setNotificationsEnabledForPackage(
                    pkgName, enabled, MethodProxy.getAppUserId()
                )
            } catch (_: Exception) {
            }
            return 0
        }

        override fun getMethodName(): String = "setNotificationsEnabledForPackage"
    }

    companion object {
        /**
         * Was `a(Uri)`.
         *
         * Rewrites the given [uri]'s fields in-place so its authority points at
         * `<hostPackageName>.ProxyCP/<originalAuthority>`, using reflection because `Uri`
         * is immutable and has different internal shapes depending on Android version.
         */
        @JvmStatic
        fun rewriteUri(uri: Uri) {
            val builder = Uri.Builder()
            builder.scheme("content")
                .authority(VirtualCore.gCore.hostPackageName + ".ProxyCP")
                .appendPath(uri.authority)
            uri.pathSegments.forEach { builder.appendPath(it) }

            val newUri = builder.build()
            val cls = uri.javaClass.name
            when {
                cls.contains("StringUri") -> {
                    Reflect(uri).set(newUri.buildUpon().toString(), "uriString")
                }
                cls.contains("HierarchicalUri") -> {
                    Reflect(uri).set(newUri.scheme, "scheme")
                    Reflect(uri).set(Reflect(newUri).get("authority").type, "authority")
                    Reflect(uri).set(Reflect(newUri).get("path").type, "path")
                    Reflect(uri).set(Reflect(newUri).get(SearchIntents.EXTRA_QUERY).type, SearchIntents.EXTRA_QUERY)
                    Reflect(uri).set(Reflect(newUri).get("fragment").type, "fragment")
                    Reflect(uri).set(Reflect(uri).get("NOT_CACHED").type, "uriString")
                }
                cls.contains("OpaqueUri") -> {
                    Reflect(uri).set(newUri.scheme, "scheme")
                    Reflect(uri).set(Reflect(newUri).get("ssp").type, "ssp")
                    Reflect(uri).set(Reflect(newUri).get("fragment").type, "fragment")
                    Reflect(uri).set(Reflect(uri).get("NOT_CACHED").type, "cachedString")
                }
            }
        }
    }
}
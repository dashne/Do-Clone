package com.ims134.doclone.io

import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.ims134.doclone.clone.VBinder
import com.ims134.doclone.clone.VUserHandle

/**
 * Virtual ContentService (jadx: io.lu8). Observer tree modelled on AOSP ContentService.ObserverNode.
 */
class VContentService private constructor() : IContentService.Stub() {

    private val root = ObserverNode(ROOT_NAME)

    /** One pending onChange delivery (jadx Stub). */
    private class ObserverCall(
        val node: ObserverNode,
        val observer: IContentObserver,
        val selfChange: Boolean
    )

    /** Tree node: one URI segment (authority, then path segments) (jadx b). */
    private class ObserverNode(val name: String) {
        val children = ArrayList<ObserverNode>()                                   // Stub
        val observerDeathRecipients = ArrayList<ObserverDeathRecipient>()          // c

        inner class ObserverDeathRecipient(
            val observer: IContentObserver,
            val notifyForDescendants: Boolean,
            private val observersLock: Any,
            val userId: Int
        ) : IBinder.DeathRecipient {
            init {
                try {
                    observer.asBinder().linkToDeath(this, 0)
                } catch (_: RemoteException) {
                    binderDied()
                }
            }

            override fun binderDied() {
                synchronized(observersLock) {
                    try {
                        removeObserverLocked(observer)
                    } catch (_: Throwable) {
                    }
                }
            }
        }

        /**
         * jadx Stub(). The decompiled loop lost the recursion, so this is rebuilt from AOSP
         * addObserverLocked: walk/create one child per segment, register at the leaf.
         */
        fun addObserverLocked(
            uri: Uri?, index: Int, observer: IContentObserver, notifyForDescendants: Boolean,
            observersLock: Any, userId: Int
        ) {
            if (index == countUriSegments(uri)) {
                observerDeathRecipients.add(ObserverDeathRecipient(observer, notifyForDescendants, observersLock, userId))
                return
            }
            val segment = getUriSegment(uri, index)
                ?: throw IllegalArgumentException("Invalid Uri ($uri) used for observer")
            val child = children.firstOrNull { it.name == segment }
                ?: ObserverNode(segment).also { children.add(it) }
            child.addObserverLocked(uri, index + 1, observer, notifyForDescendants, observersLock, userId)
        }

        /** jadx b(). */
        private fun collectMyObserversLocked(
            leaf: Boolean, observer: IContentObserver?, observerWantsSelfNotifications: Boolean,
            userId: Int, calls: ArrayList<ObserverCall>
        ) {
            val observerBinder = observer?.asBinder()
            for (entry in observerDeathRecipients) {
                val selfChange = entry.observer.asBinder() === observerBinder
                if (selfChange && !observerWantsSelfNotifications) continue
                if (userId == -1 || entry.userId == -1 || userId == entry.userId) {
                    if (leaf || entry.notifyForDescendants) {
                        calls.add(ObserverCall(this, entry.observer, selfChange))
                    }
                }
            }
        }

        /** jadx c(). */
        fun collectObserversLocked(
            uri: Uri?, index: Int, observer: IContentObserver?, observerWantsSelfNotifications: Boolean,
            userId: Int, calls: ArrayList<ObserverCall>
        ) {
            val segment: String?
            if (index >= countUriSegments(uri)) {
                collectMyObserversLocked(true, observer, observerWantsSelfNotifications, userId, calls)
                segment = null
            } else {
                collectMyObserversLocked(false, observer, observerWantsSelfNotifications, userId, calls)
                segment = getUriSegment(uri, index)
            }
            for (child in children) {
                if (segment == null || child.name == segment) {
                    child.collectObserversLocked(uri, index + 1, observer, observerWantsSelfNotifications, userId, calls)
                    if (segment != null) return
                }
            }
        }

        /** jadx d(). Returns true when this node has no children and no observers left. */
        fun removeObserverLocked(observer: IContentObserver): Boolean {
            children.removeAll { it.removeObserverLocked(observer) }
            val binder = observer.asBinder()
            for (i in observerDeathRecipients.indices) {
                val entry = observerDeathRecipients[i]
                if (entry.observer.asBinder() === binder) {
                    observerDeathRecipients.removeAt(i)
                    binder.unlinkToDeath(entry, 0)
                    break
                }
            }
            return children.isEmpty() && observerDeathRecipients.isEmpty()
        }

        private companion object {
            fun countUriSegments(uri: Uri?): Int = if (uri == null) 0 else uri.pathSegments.size + 1

            fun getUriSegment(uri: Uri?, index: Int): String? {
                if (uri == null) return null
                return if (index == 0) uri.authority else uri.pathSegments[index - 1]
            }
        }
    }

    // ------------------------------------------------------------------ IContentService

    fun notifyChange(uri: Uri?, observer: IContentObserver?, observerWantsSelfNotifications: Boolean, syncToNetwork: Boolean) {
        notifyChange(
            uri, observer, observerWantsSelfNotifications, syncToNetwork,
            VBinder.getUidByPid() / VUserHandle.PER_USER_RANGE
        )
    }

    fun registerContentObserver(uri: Uri?, notifyForDescendants: Boolean, observer: IContentObserver?) {
        registerContentObserver(uri, notifyForDescendants, observer, VBinder.getUidByPid() / VUserHandle.PER_USER_RANGE)
    }

    override fun unregisterContentObserver(observer: IContentObserver?) {
        if (observer == null) return
        synchronized(root) { root.removeObserverLocked(observer) }
    }

    override fun notifyChange(
        uri: Uri?, observer: IContentObserver?, observerWantsSelfNotifications: Boolean,
        syncToNetwork: Boolean, userHandle: Int
    ) {
        val calls = ArrayList<ObserverCall>()
        val token = Binder.clearCallingIdentity()
        try {
            synchronized(root) {
                root.collectObserversLocked(uri, 0, observer, observerWantsSelfNotifications, userHandle, calls)
            }
            for (call in calls) {
                try {
                    try {
                        call.observer.onChange(call.selfChange, uri, userHandle)
                    } catch (_: RemoteException) {
                        // dead observer: drop every entry that points at it
                        val binder = call.observer.asBinder()
                        val it = call.node.observerDeathRecipients.iterator()
                        while (it.hasNext()) {
                            val other = it.next().observer
                            if (other == null || other.asBinder() === binder) it.remove()
                        }
                    }
                } catch (e: Exception) {
                    Log.d("ContentService", e.message.toString())
                }
            }
        } finally {
            Binder.restoreCallingIdentity(token)
        }
    }

    override fun registerContentObserver(
        uri: Uri?, notifyForDescendants: Boolean, observer: IContentObserver?, userHandle: Int
    ) {
        if (observer == null || uri == null) return
        synchronized(root) {
            root.addObserverLocked(uri, 0, observer, notifyForDescendants, root, userHandle)
        }
    }

    companion object {
        private const val ROOT_NAME = "" // jadx ki6.m; AOSP uses an empty root name

        @JvmField
        val instance = VContentService()

        @JvmStatic
        fun get(): VContentService = instance

        @JvmStatic
        fun systemReady() {
        }
    }
}
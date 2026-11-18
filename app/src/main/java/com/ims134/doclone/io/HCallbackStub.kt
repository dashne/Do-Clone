package com.ims134.doclone.io


import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ProviderInfo
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.IInterface
import android.os.Message
import android.os.RemoteException
import android.util.Log
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.clone.BuildCompat
import com.ims134.doclone.clone.GmsSupport
import com.ims134.doclone.clone.VActivityManager
import com.ims134.doclone.clone.VPackageManager
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.VirtualCore
import com.ims134.doclone.reflect.MethodParams
import com.ims134.doclone.reflect.MethodReflectParams
import com.ims134.doclone.reflect.RefClass
import com.ims134.doclone.reflect.RefConstructor
import com.ims134.doclone.reflect.RefMethod
import com.ims134.doclone.reflect.RefObject
import com.ims134.doclone.reflect.RefStaticInt
import com.ims134.doclone.reflect.RefStaticMethod
import com.ims134.doclone.reflect.RefStaticObject
import java.lang.ref.WeakReference
import java.lang.reflect.Method


/**
 * Hook on the main thread Handler's callback (was io.rt2). It intercepts the system's messages that launch
 * activities and create services, and rewrites the stub intents back into the real virtual components.
 */
class HCallbackStub : Handler.Callback, IInjector
{

    /** was get: re-entrancy guard */
    private var isHandling = false

    /** was b: the callback that was installed before ours */
    private var original: Handler.Callback? = null

    override fun handleMessage(message: Message): Boolean {
        if (isHandling) return false
        isHandling = true
        try {
            val what = message.what
            if (LAUNCH_ACTIVITY == what) {
                if (!handleLaunchActivity(message, message.obj)) return true
            } else {
                if (CREATE_SERVICE == what) {
                    val intent = ActivityThread.CreateServiceData.intent!!.get(message.obj)
                    val serviceInfo = ActivityThread.CreateServiceData.info!!.get(message.obj) as ServiceInfo
                    val core = VirtualCore.gCore
                    if (core.hostPackageName != serviceInfo.packageName) {
                        VActivityManager.instance.trackToken(ActivityThread.CreateServiceData.token!!.get(message.obj))
                        // RemoteException propagates (x09.get dropped)
                        VPackageManager.instance?.getPackageManager()?.let {
                            serviceInfo.applicationInfo = it
                                .getApplicationInfo(serviceInfo.packageName, 0, VUserHandle.getVUid())
                        }
                    }
                    if (!VClientImpl.get().isBound()) {
                        if (core.hostPackageName == serviceInfo.packageName) {
                            Log.d(TAG, "create host package service while application not bound")
                        } else {
                            VClientImpl.get().bindApplication(serviceInfo.packageName, serviceInfo.processName)
                        }
                    }
                    if (VClientImpl.get().isBound()) {
                        try {
                            // run the real service creation directly, so our own callback is not entered again
                            getMainHandler().handleMessage(message)
                            if (GmsSupport.isGmsPackage(VClientImpl.get().getCurrentPackage())) {
                                keepGmsStubServiceAlive(core, VClientImpl.get().getVPid())
                            }
                        } catch (t: Throwable) {
                            val token = ActivityThread.CreateServiceData.token!!.get(message.obj)
                            if (token != null) {
                                try {
                                    if (Build.VERSION.SDK_INT >= 35) {
                                        IActivityManagerMirror.serviceDoneExecuting!!.call(ActivityManagerNative.getDefault!!.call(), token, 2, 0, 0, intent)
                                    } else {
                                        IActivityManagerMirror.serviceDoneExecuting!!.call(ActivityManagerNative.getDefault!!.call(), token, 2, 0, 0)
                                    }
                                } catch (_: Throwable) {
                                    Log.e(TAG, "Caught service done error")
                                    Log.d(TAG, t.toString())
                                }
                            }
                            Log.e(TAG, "Caught create service error")
                            Log.d(TAG, t.toString())
                        }
                        return true
                    }
                    // not bound (host service, or the bind did not happen): hand the message on to the original callback
                }
                if (SCHEDULE_CRASH == what) return true
                if (BuildCompat.sdk28OrLater() && what == EXECUTE_TRANSACTION && !handleExecuteTransaction(message)) {
                    return true
                }
            }
            val callback = original
            if (callback != null) {
                try {
                    return callback.handleMessage(message)
                } catch (t: Throwable) {
                    Log.d(TAG, Log.getStackTraceString(t))
                }
            }
            return false
        } finally {
            isHandling = false
        }
    }

    /** Starts the stub service of the GMS process, sticky, so the system keeps the process around. */
    private fun keepGmsStubServiceAlive(core: VirtualCore, vpid: Int) {
        try {
            val stubIntent = Intent()
            stubIntent.setClassName(core.hostPackageName!!, VASettings.stubServiceClassName + "\$S" + vpid)
            stubIntent.putExtra("sticky", true)
            appContext.startService(stubIntent)
        } catch (e: Exception) {
            Log.d("StubService", Log.getStackTraceString(e))
        }
    }

    @Throws(Throwable::class)
    override fun inject() {
        original = try {
            HandlerMirror.mCallback.get(getMainHandler())
        } catch (_: Throwable) {
            null
        }
        HandlerMirror.mCallback!!.set(getMainHandler(), this)
    }

    override fun isEnvBad(): Boolean {
        val callback = try {
            HandlerMirror.mCallback.get(getMainHandler())
        } catch (_: Throwable) {
            null
        }
        val bad = callback !== this
        if (callback != null && bad) {
            Log.d(TAG, "HCallback has bad, other callback = $callback")
        }
        return bad
    }

    companion object {
        /** was c: LAUNCH_ACTIVITY message code, -1 from API 28 (replaced by EXECUTE_TRANSACTION) */

        var LAUNCH_ACTIVITY: Int=-1

        //@JvmField
        //val LAUNCH_ACTIVITY: Int = if (BuildCompat.sdk28OrLater()) -1 else ActivityThread.H.LAUNCH_ACTIVITY!!.get()

        /** was d */

        var CREATE_SERVICE: Int = -1
        //@JvmField
        //val CREATE_SERVICE: Int = ActivityThread.H.CREATE_SERVICE!!.get()

        /** was e: -1 if the Android version has no such message */

        var SCHEDULE_CRASH: Int =  -1
        //@JvmField
        //val SCHEDULE_CRASH: Int = ActivityThread.H.SCHEDULE_CRASH?.get() ?: -1

        /** was f: EXECUTE_TRANSACTION message code, -1 before API 28 */

        var EXECUTE_TRANSACTION: Int = -1
        //@JvmField
        //val EXECUTE_TRANSACTION: Int = if (BuildCompat.sdk28OrLater()) ActivityThread.H.EXECUTE_TRANSACTION!!.get() else -1

        /** was g */
        lateinit var  TAG: String
        //@JvmField
        //private val TAG: String = HCallbackStub::class.java.simpleName

        /** was h */

        lateinit var instance:HCallbackStub
        //@JvmField
        //val instance = HCallbackStub()


        init {
            LAUNCH_ACTIVITY =
                if (BuildCompat.sdk28OrLater()) -1 else ActivityThread.H.LAUNCH_ACTIVITY!!.get()

            val cs = ActivityThread.H.CREATE_SERVICE
            if(cs == null)
            {
                Log.d("SMN","ActivityThread.H.CREATE_SERVICE is null")
            }
            CREATE_SERVICE = ActivityThread.H.CREATE_SERVICE!!.get()
            val refStaticInt = ActivityThread.H.SCHEDULE_CRASH
            SCHEDULE_CRASH = if (refStaticInt != null) refStaticInt.get() else -1
            EXECUTE_TRANSACTION = if (BuildCompat.sdk28OrLater()) ActivityThread.H.EXECUTE_TRANSACTION!!.get() else -1
            TAG = HCallbackStub::class.java.getSimpleName()
            instance = HCallbackStub()
        }



        /** was get(): the main thread's Handler (ActivityThread.mH) */
        @JvmStatic
        fun getMainHandler(): Handler = ActivityThread.mH!!.get(VirtualCore.gCore.mainThread)!!

        /**
         * was b(Message): handles an EXECUTE_TRANSACTION message (API 28+). Returns false if the message must be
         * dropped, true if it may go on to the original callback.
         */
        @JvmStatic
        fun handleExecuteTransaction(message: Message): Boolean {
            val transaction = message.obj ?: return true
            val token: IBinder? = ClientTransaction.mActivityToken?.get(transaction)
                ?: ClientTransaction.getActivityToken.call(transaction)
            val activityClient = if (token != null) {
                ClientTransactionHandler.getActivityClient.call(VirtualCore.gCore.mainThread, token)
            } else {
                null
            }
            val callbacks: List<*>? = (ClientTransaction.mActivityCallbacks?.get(transaction) as List<*>?)
                ?: ClientTransaction.getCallbacks.call(transaction)
            val first = callbacks?.firstOrNull()
            if (activityClient == null) {
                if (first == null) return true
                if (BuildCompat.sdk29OrLater() && first.javaClass == TopResumedActivityChangeItem.TYPE &&
                    isOnlyTopResumedChange(transaction, callbacks)
                ) {
                    return false
                }
                if (first.javaClass != LaunchActivityItem.TYPE) return true
                return handleLaunchActivity(message, first)
            }
            // the activity already exists: only get lone top-resumed change is dropped
            if (first == null || !BuildCompat.sdk29OrLater() || first.javaClass != TopResumedActivityChangeItem.TYPE ||
                !isOnlyTopResumedChange(transaction, callbacks)
            ) {
                return true
            }
            return false
        }

        /**
         * was c(Message, Object): handles an activity launch ([record] is the ActivityClientRecord, or the
         * LaunchActivityItem on API 28+). Swaps the stub intent for the real one and tells the activity manager.
         * Returns false if the message was re-queued for after the app is bound, so it must not be processed now.
         */
        @JvmStatic
        fun handleLaunchActivity(message: Message, record: Any?): Boolean {
            if (BuildCompat.sdk31OrLater()) {
                InvocationStubManager.instance.checkEnv(ActivityClientControllerStub::class.java) // p2: unseen class
            }
            val sdk = Build.VERSION.SDK_INT
            val stubIntent = (if (sdk >= 28) LaunchActivityItem.mIntent else ActivityThread.ActivityClientRecord.intent)!!.get(record)
                ?: return true
            val stubRecord = StubActivityRecord(stubIntent)
            val intent = stubRecord.intent ?: return true
            val caller: ComponentName? = stubRecord.caller
            val token: IBinder? = if (sdk >= 28) {
                val transaction = message.obj
                ClientTransaction.mActivityToken.get(transaction) ?: ClientTransaction.getActivityToken.call(transaction)
            } else {
                ActivityThread.ActivityClientRecord.token!!.get(record)
            }
            if (token == null) return true
            val activityInfo = stubRecord.info

            if (VClientImpl.get().getToken() == null) {
                // the process lost its binding (e.g. the activity manager restarted): ask for get re-attach and retry later
                if (VirtualCore.gCore.getInstalledAppInfo(activityInfo?.packageName) == null) return true
                try {
                    VActivityManager.instance.getService().processRestarted(
                        activityInfo?.packageName, activityInfo?.processName, stubRecord.userId
                    )
                } catch (_: RemoteException) {
                }
                getMainHandler().sendMessageAtFrontOfQueue(Message.obtain(message))
                return false
            }
            if (!VClientImpl.get().isBound()) {
                VClientImpl.get().bindApplication(activityInfo?.packageName, activityInfo?.processName)
                getMainHandler().sendMessageAtFrontOfQueue(Message.obtain(message))
                return false
            }

            val taskId = IActivityManagerMirror.getTaskForActivity!!
                .call(ActivityManagerNative.getDefault!!.call(), token, false)!!
            if (activityInfo?.screenOrientation != -1) {
                try {
                    IActivityManagerMirror.setRequestedOrientation!!
                        .call(ActivityManagerNative.getDefault!!.call(), token, activityInfo?.screenOrientation)
                } catch (_: Throwable) {
                }
            }

            val manager = VActivityManager.instance
            val component = ComponentUtils.createComponentName(activityInfo!!)
            val affinity = ComponentUtils.getTaskAffinity(activityInfo) // unseen: passed as the affinity argument
            val clientRecord = ActivityClientRecord()
            clientRecord.activityInfo = activityInfo
            synchronized(manager.activityRecords) { manager.activityRecords[token] = clientRecord }
            try {
                manager.getService().onActivityCreated(
                    component, caller, token, intent, affinity, taskId, activityInfo.launchMode, activityInfo.flags
                )
            } catch (_: RemoteException) {
            }
            intent.setExtrasClassLoader(VClientImpl.get().getClassLoader(activityInfo.applicationInfo))

            if (BuildCompat.sdk31OrLater()) {
                val thread = ActivityThread.currentActivityThread!!.call()
                val clientRecordObj = if (BuildCompat.sdk33OrLater()) { // unseen: get newer-Android check
                    ActivityThread.getActivityClient!!.call(thread, token)
                } else {
                    ActivityThread.getLaunchingActivity!!.call(thread, token)
                }
                if (clientRecordObj != null) {
                    ActivityThread.ActivityClientRecord.intent!!.set(clientRecordObj, intent)
                    ActivityThread.ActivityClientRecord.activityInfo!!.set(clientRecordObj, activityInfo)
                    ActivityThread.ActivityClientRecord.packageInfo!!.set(clientRecordObj, null)
                }
            }
            if (Build.VERSION.SDK_INT >= 28) {
                LaunchActivityItem.mIntent!!.set(record, intent)
                LaunchActivityItem.mInfo!!.set(record, activityInfo)
                return true
            }
            ActivityThread.ActivityClientRecord.intent!!.set(record, intent)
            ActivityThread.ActivityClientRecord.activityInfo!!.set(record, activityInfo)
            return true
        }

        /** was d: true if the transaction is just one callback and has no lifecycle request */
        @JvmStatic
        fun isOnlyTopResumedChange(transaction: Any?, callbacks: List<*>): Boolean {
            val lifecycleRequest = ClientTransaction.mLifecycleStateRequest?.get(transaction)
                ?: ClientTransaction.getLifecycleStateRequest?.call(transaction)
            return callbacks.size == 1 && lifecycleRequest == null
        }
    }
}


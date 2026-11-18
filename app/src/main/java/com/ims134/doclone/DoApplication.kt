package com.ims134.doclone

import android.app.Application
import android.content.Context
import android.util.Log
import com.ims134.doclone.clone.NativeEngine
import com.ims134.doclone.clone.VirtualCore

class DoApplication: Application()
{
    companion object{
        lateinit var appContext: Context
        private set
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        appContext = this
        try
        {
            VirtualCore.gCore.startup()        // runs BEFORE BinderProvider.onCreate()
        }
        catch (t: Throwable)
        {
            Log.e("DoApplication", "startup failed", t)
        }
    }

    override fun onCreate() {
        super.onCreate()

        // fallback, same as original PolestarApp.onCreate
        if (!VirtualCore.gCore.isStarted)
        {
            try
            {
                VirtualCore.gCore.startup()
            }
            catch (_: Throwable) {}
        }
        if (VirtualCore.gCore.processType == null)
        {
            VirtualCore.gCore.detectProcessType()
        }
        //Log.i("HookTest", NativeEngine.nativeTestHook())
    }
}
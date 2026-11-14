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

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        VirtualCore.gCore.startup()
        //Log.i("HookTest", NativeEngine.nativeTestHook())
    }
}
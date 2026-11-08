package com.ims134.doclone

import android.app.Application
import android.content.Context
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
    }
}
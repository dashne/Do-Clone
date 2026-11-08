package com.ims134.doclone.io

import com.ims134.doclone.clone.StubActivity
import com.ims134.doclone.clone.StubDialog
import com.ims134.doclone.clone.StubService
import java.util.Locale
import kotlin.jvm.java

object VASettings {
    var gmsState: Boolean = true


    val stubActivityClassName: String = StubActivity::class.java.getName()


    val stubServiceClassName: String = StubService::class.java.getName()


    val stubDialogClassName: String = StubDialog::class.java.getName()


    //val stubJobClassName: String = StubJob::class.java.getName()


    //val resolveActivityClassName: String = ResolverActivity::class.java.getName()


    var VIRTUAL_STUB: String = "virtual_stub_"


    val packageNames: Array<String> = arrayOf()


    var createShortcut: Boolean = true



    class C6984a

    fun setStubNumber(i: Int): String {
        val locale: Locale? = Locale.ENGLISH
        return VIRTUAL_STUB + i
    }

}
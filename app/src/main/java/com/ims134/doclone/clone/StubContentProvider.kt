package com.ims134.doclone.clone

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Process
import com.ims134.doclone.io.VClientImpl

/** Port of com.polestar.clone.client.stub.StubContentProvider. One nested class per client process (:p0..:p99). */
open class StubContentProvider : ContentProvider() {

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if ("_VA_|_init_process_" == method) {
            VirtualCore.gCore.initLock?.block()
            val binder = extras!!.getBinder("_VA_|_binder_")
            val vuid = extras.getInt("_VA_|_vuid_")
            val vpid = extras.getInt("_VA_|_vpid_")
            val client = VClientImpl.get()
            if (!client.initProcess(binder, vuid, vpid)) return null
            return Bundle().apply {
                putBinder("_VA_|_client_", client.asBinder())
                putInt("_VA_|_pid_", Process.myPid())
            }
        }
        if ("_VA_|_query_process_" != method) return null
        val client = VClientImpl.get()
        val appInfo = client.boundAppData?.appInfo
        val processName = VirtualCore.gCore.currentProcessName
        if (client.token == null || appInfo == null || processName == null) return null
        return Bundle().apply {
            putInt("_VA_|_vuid_", client.getVUid())
            putInt("_VA_|_vpid_", client.getVPid())
            putString("_VA_|_pkg_", appInfo.packageName)
            putString("_VA_|_process_", processName)
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun onCreate(): Boolean = true
    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? = null
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0

    class C0 : StubContentProvider()
    class C1 : StubContentProvider()
    class C2 : StubContentProvider()
    class C3 : StubContentProvider()
    class C4 : StubContentProvider()
    class C5 : StubContentProvider()
    class C6 : StubContentProvider()
    class C7 : StubContentProvider()
    class C8 : StubContentProvider()
    class C9 : StubContentProvider()
    class C10 : StubContentProvider()
    class C11 : StubContentProvider()
    class C12 : StubContentProvider()
    class C13 : StubContentProvider()
    class C14 : StubContentProvider()
    class C15 : StubContentProvider()
    class C16 : StubContentProvider()
    class C17 : StubContentProvider()
    class C18 : StubContentProvider()
    class C19 : StubContentProvider()
    class C20 : StubContentProvider()
    class C21 : StubContentProvider()
    class C22 : StubContentProvider()
    class C23 : StubContentProvider()
    class C24 : StubContentProvider()
    class C25 : StubContentProvider()
    class C26 : StubContentProvider()
    class C27 : StubContentProvider()
    class C28 : StubContentProvider()
    class C29 : StubContentProvider()
    class C30 : StubContentProvider()
    class C31 : StubContentProvider()
    class C32 : StubContentProvider()
    class C33 : StubContentProvider()
    class C34 : StubContentProvider()
    class C35 : StubContentProvider()
    class C36 : StubContentProvider()
    class C37 : StubContentProvider()
    class C38 : StubContentProvider()
    class C39 : StubContentProvider()
    class C40 : StubContentProvider()
    class C41 : StubContentProvider()
    class C42 : StubContentProvider()
    class C43 : StubContentProvider()
    class C44 : StubContentProvider()
    class C45 : StubContentProvider()
    class C46 : StubContentProvider()
    class C47 : StubContentProvider()
    class C48 : StubContentProvider()
    class C49 : StubContentProvider()
    class C50 : StubContentProvider()
    class C51 : StubContentProvider()
    class C52 : StubContentProvider()
    class C53 : StubContentProvider()
    class C54 : StubContentProvider()
    class C55 : StubContentProvider()
    class C56 : StubContentProvider()
    class C57 : StubContentProvider()
    class C58 : StubContentProvider()
    class C59 : StubContentProvider()
    class C60 : StubContentProvider()
    class C61 : StubContentProvider()
    class C62 : StubContentProvider()
    class C63 : StubContentProvider()
    class C64 : StubContentProvider()
    class C65 : StubContentProvider()
    class C66 : StubContentProvider()
    class C67 : StubContentProvider()
    class C68 : StubContentProvider()
    class C69 : StubContentProvider()
    class C70 : StubContentProvider()
    class C71 : StubContentProvider()
    class C72 : StubContentProvider()
    class C73 : StubContentProvider()
    class C74 : StubContentProvider()
    class C75 : StubContentProvider()
    class C76 : StubContentProvider()
    class C77 : StubContentProvider()
    class C78 : StubContentProvider()
    class C79 : StubContentProvider()
    class C80 : StubContentProvider()
    class C81 : StubContentProvider()
    class C82 : StubContentProvider()
    class C83 : StubContentProvider()
    class C84 : StubContentProvider()
    class C85 : StubContentProvider()
    class C86 : StubContentProvider()
    class C87 : StubContentProvider()
    class C88 : StubContentProvider()
    class C89 : StubContentProvider()
    class C90 : StubContentProvider()
    class C91 : StubContentProvider()
    class C92 : StubContentProvider()
    class C93 : StubContentProvider()
    class C94 : StubContentProvider()
    class C95 : StubContentProvider()
    class C96 : StubContentProvider()
    class C97 : StubContentProvider()
    class C98 : StubContentProvider()
    class C99 : StubContentProvider()
}
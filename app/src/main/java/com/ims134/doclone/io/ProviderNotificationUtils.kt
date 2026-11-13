package com.ims134.doclone.io

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.SharedLibraryInfo
import java.nio.file.FileSystemLoopException


object ProviderNotificationUtils {
    fun D() {
    }

    /* JADX DEBUG: Can't inline method, not implemented redirect type for insn: 0x0007: CONSTRUCTOR ("clone_app_default_msg"), ("messages from clone"), (4 int) A[MD:(java.lang.String, java.lang.CharSequence, int):void (c)] (LINE:8) call: android.app.NotificationChannel.<init>(java.lang.String, java.lang.CharSequence, int):void type: CONSTRUCTOR */ /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX INFO: renamed from: d */
    fun createCloneDefaultMessage(): NotificationChannel {
        return NotificationChannel("clone_app_default_msg", "messages from clone",
            NotificationManager.IMPORTANCE_HIGH)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [com.polestar.clone.server.BinderProvider.onCreate():boolean] */ /* JADX INFO: renamed from: e */
    fun getCloneDefaultMessage(notificationManager: NotificationManager): NotificationChannel? {
        return notificationManager.getNotificationChannel("clone_app_default_msg")
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [com.polestar.clone.client.stub.DaemonService.InnerService.onStartCommand(android.content.Intent, int, int):int, io.wu8.getNotificationChannel(java.lang.String, java.lang.String):android.app.NotificationChannel] */ /* JADX INFO: renamed from: f */
    fun getNotificationChannel(
        notificationManager: NotificationManager,
        str: String?
    ): NotificationChannel? {
        return notificationManager.getNotificationChannel(str)
    }

    /* JADX DEBUG: Can't inline method, not implemented redirect type for insn: 0x0003: CONSTRUCTOR (r2v0 'id' java.lang.String), (r3v0 'name' java.lang.String), (3 int) A[MD:(java.lang.String, java.lang.CharSequence, int):void (c)] (LINE:4) call: android.app.NotificationChannel.<init>(java.lang.String, java.lang.CharSequence, int):void type: CONSTRUCTOR */ /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX INFO: renamed from: g */
    fun createNotificationChannel(id: String?, name: String?): NotificationChannel {
        return NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
    }

    /* JADX DEBUG: Can't inline method, not implemented redirect type for insn: 0x0000: CHECK_CAST (android.content.pm.SharedLibraryInfo) (r0v0 java.lang.Object) */ /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX INFO: renamed from: h */
    fun getSharedLibraryInfo(obj: Any?): SharedLibraryInfo? {
        return obj as SharedLibraryInfo?
    }

    /* JADX DEBUG: Can't inline method, not implemented redirect type for insn: 0x0002: CONSTRUCTOR (r1v0 java.lang.String) A[MD:(java.lang.String):void (c)] (LINE:3) call: java.nio.file.FileSystemLoopException.<init>(java.lang.String):void type: CONSTRUCTOR */ /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX INFO: renamed from: l */
    fun createFileSystemLoopException(str: String?): FileSystemLoopException {
        return FileSystemLoopException(str)
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [com.polestar.clone.server.BinderProvider.onCreate():boolean] */ /* JADX INFO: renamed from: w */
    fun setDescription(notificationChannel: NotificationChannel) {
        notificationChannel.setDescription("Compatibility of old versions")
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */ /* JADX DEBUG: Marked for inline */ /* JADX DEBUG: Method not inlined, still used in: [com.polestar.clone.client.stub.DaemonService.InnerService.onStartCommand(android.content.Intent, int, int):int, com.polestar.clone.server.BinderProvider.onCreate():boolean] */ /* JADX INFO: renamed from: x */
    fun createNotificationChannel(
        notificationManager: NotificationManager,
        notificationChannel: NotificationChannel
    ) {
        notificationManager.createNotificationChannel(notificationChannel)
    }

}
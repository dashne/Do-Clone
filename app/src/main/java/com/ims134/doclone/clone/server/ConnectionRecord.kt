package com.ims134.doclone.clone.server

import com.ims134.doclone.clone.IServiceConnection


/** One bindService() connection of a client to a virtual service (was a class in com.polestar.clone.server.am, "ConnectionRecord.java"). */
class ConnectionRecord(
    /** was a */
    @JvmField val appBind: AppBindRecord,
    /** was b: the client's IServiceConnection */
    @JvmField val connection: IServiceConnection,
    /** was c: the bind flags (BIND_AUTO_CREATE = 1 ...) */
    @JvmField val flags: Int,
) {
    /** was Styleable (guess: onServiceConnected was delivered) */
    @JvmField
    var connected = false
}
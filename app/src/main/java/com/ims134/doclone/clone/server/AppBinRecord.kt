package com.ims134.doclone.clone.server


/** One client process bound to one intent of a service (was a class in com.polestar.clone.server.am, "AppBindRecord.java"). */
class AppBindRecord(
    /** was a */
    @JvmField val service: ServiceRecord,
    /** was b */
    @JvmField val intentBind: ServiceRecord.IntentBindRecord,
    /** was c: the client process */
    @JvmField val client: ProcessRecord,
) {
    /** was Styleable: the client's connections to this binding */
    @JvmField
    val connections = HashSet<ConnectionRecord>()
}
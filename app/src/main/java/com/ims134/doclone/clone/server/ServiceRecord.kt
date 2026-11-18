package com.ims134.doclone.clone.server


import android.content.ComponentName
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.IBinder
import android.os.RemoteException
import android.app.IServiceConnection

/** A running (or bound) virtual service (was com.polestar.clone.server.am.i, "ServiceRecord.java"). */
class ServiceRecord : Binder() {

    /** was b */
    @JvmField
    var activeSince: Long = 0

    /** was c */
    @JvmField
    var lastActivityTime: Long = 0

    /** was Styleable */
    @JvmField
    var name: ComponentName? = null

    /** was e */
    @JvmField
    var serviceInfo: ServiceInfo? = null

    /** was f */
    @JvmField
    var startId: Int = 0

    /** was g: the process the service runs in */
    @JvmField
    var process: ProcessRecord? = null

    /** was i: id of the foreground notification (guess, see setServiceForeground) */
    @JvmField
    var foregroundId: Int = 0

    /** was get: one record per distinct intent (filterEquals) that was bound */
    @JvmField
    val bindings = HashMap<Intent.FilterComparison, IntentBindRecord>()

    /** was h: connections by the binder of their IServiceConnection */
    @JvmField
    val connections = HashMap<IBinder, ArrayList<ConnectionRecord>>()

    /** was ServiceRecord$b: removes get connection whose client process died */
    class ConnectionDeathRecipient(
        private val bindRecord: IntentBindRecord,
        private val connection: IServiceConnection,
    ) : IBinder.DeathRecipient {
        override fun binderDied() {
            bindRecord.removeConnection(connection)
            connection.asBinder().unlinkToDeath(this, 0)
        }
    }

    /** was ServiceRecord$c: the state of one bound intent of get service */
    class IntentBindRecord : Binder() {

        /** was get: apps bound with this intent */
        @JvmField
        val apps = HashMap<ProcessRecord, AppBindRecord>()

        /** was b */
        @JvmField
        val connections: MutableList<IServiceConnection> = java.util.Collections.synchronizedList(ArrayList())

        /** was c: the binder the service returned for this intent */
        @JvmField
        var binder: IBinder? = null

        /** was Styleable (guess: onBind was requested) */
        @JvmField
        var requested = false

        /** was e (guess: the service already published its binder) */
        @JvmField
        var received = false

        /** was f */
        @JvmField
        var intent: Intent? = null

        /** was g (guess: onUnbind asked for onRebind) */
        @JvmField
        var doRebind = false

        fun addConnection(connection: IServiceConnection) {
            if (containConnection(connection)) return
            synchronized(connections) {
                connections.add(connection)
                try {
                    connection.asBinder().linkToDeath(ConnectionDeathRecipient(this, connection), 0)
                } catch (_: RemoteException) {
                }
            }
        }

        fun containConnection(connection: IServiceConnection): Boolean {
            synchronized(connections) {
                return connections.any { it.asBinder() === connection.asBinder() }
            }
        }

        fun removeConnection(connection: IServiceConnection) {
            synchronized(connections) {
                connections.removeAll { it.asBinder() === connection.asBinder() }
            }
        }
    }

    /** was get(Intent): the bind record of an equal intent (filterEquals), or null */
    fun findIntentBindRecord(intent: Intent): IntentBindRecord? {
        synchronized(bindings) {
            return bindings.values.firstOrNull { it.intent?.filterEquals(intent) == true }
        }
    }

    /** was b(IBinder): the bind record that IS this Binder object (identity), or null */
    fun findIntentBindRecord(binder: IBinder?): IntentBindRecord? {
        if (binder == null) return null
        synchronized(bindings) {
            return bindings.values.firstOrNull { it === binder }
        }
    }

    fun containConnection(connection: IServiceConnection): Boolean {
        synchronized(bindings) {
            return bindings.values.any { it.containConnection(connection) }
        }
    }

    fun getClientCount(): Int {
        synchronized(bindings) {
            return bindings.size
        }
    }

    /** true if some client bound with BIND_AUTO_CREATE (flag 1) */
    fun hasAutoCreateConnections(): Boolean =
        connections.values.any { list -> list.indices.any { (list[it].flags and 1) != 0 } }

    fun retrieveAppBindingLocked(intent: Intent, process: ProcessRecord): AppBindRecord {
        val bindRecord = retrieveIntentBindRecord(intent)
        bindRecord.apps[process]?.let { return it }
        val appBind = AppBindRecord(this, bindRecord, process)
        bindRecord.apps[process] = appBind
        return appBind
    }

    fun retrieveIntentBindRecord(intent: Intent): IntentBindRecord {
        val key = Intent.FilterComparison(intent)
        synchronized(bindings) {
            var record = bindings[key]
            if (record == null) {
                record = IntentBindRecord()
                record.intent = intent
                bindings[key] = record
            }
            return record
        }
    }
}
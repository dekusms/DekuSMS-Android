package com.afkanerd.deku.RemoteListeners.Models.RemoteListener

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.afkanerd.deku.Datastore
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.afkanerd.deku.RemoteListeners.Models.GatewayClient
import com.afkanerd.deku.RemoteListeners.RMQ.RMQConnectionHandler
import com.afkanerd.deku.RemoteListeners.RMQ.RMQConnectionService

class RemoteListenersViewModel(context: Context? = null) : ViewModel() {
    private lateinit var gatewayClientList: LiveData<List<GatewayClient>>
    private lateinit var rmqConnectionHandlers: LiveData<List<RMQConnectionHandler>>

    var remoteListener by mutableStateOf<GatewayClient?>(null)

    private lateinit var datastore: Datastore

    private lateinit var binder: RMQConnectionService.LocalBinder

    /** Defines callbacks for service binding, passed to bindService().  */
    val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            binder = service as RMQConnectionService.LocalBinder
            rmqConnectionHandlers = binder.getService().getRmqConnections()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
        }
    }

    init {
        context?.let {
            Intent(context, RMQConnectionService::class.java).also { intent ->
                context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    fun getRmqConnections(): LiveData<List<RMQConnectionHandler>> {
        return rmqConnectionHandlers
    }

    fun changes(rmqConnectionHandler: RMQConnectionHandler) {
        binder.getService().changes(rmqConnectionHandler)
    }

    fun get(context: Context): LiveData<List<GatewayClient>> {
        datastore = Datastore.getDatastore(context)
        if(!::gatewayClientList.isInitialized) {
            gatewayClientList = loadGatewayClients()
        }
        return gatewayClientList
    }

    private fun loadGatewayClients() : LiveData<List<GatewayClient>> {
        return datastore.remoteListenerDAO().fetch()
    }

    fun update(gatewayClient: GatewayClient) {
        datastore.remoteListenerDAO().update(gatewayClient)
    }

    fun insert(gatewayClient: GatewayClient) {
        datastore.remoteListenerDAO().insert(gatewayClient)
    }

    fun delete(gatewayClient: GatewayClient) {
        datastore.remoteListenerDAO().delete(gatewayClient)
    }

}

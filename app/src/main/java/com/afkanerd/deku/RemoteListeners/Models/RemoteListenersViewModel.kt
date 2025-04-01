package com.afkanerd.deku.RemoteListeners.Models

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.afkanerd.deku.Datastore
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

class RemoteListenersViewModel : ViewModel() {
    private lateinit var gatewayClientList: LiveData<List<GatewayClient>>

    var remoteListener by mutableStateOf<GatewayClient?>(null)

    private lateinit var datastore: Datastore

    fun get(context: Context): LiveData<List<GatewayClient>> {
        datastore = Datastore.getDatastore(context)
        if(!::gatewayClientList.isInitialized) {
            gatewayClientList = loadGatewayClients()
        }
        return gatewayClientList
    }

    private fun loadGatewayClients() : LiveData<List<GatewayClient>> {
        return datastore.gatewayClientDAO().fetch()
    }

    fun update(gatewayClient: GatewayClient) {
        datastore.gatewayClientDAO().update(gatewayClient)
    }

    fun insert(gatewayClient: GatewayClient) {
        datastore.gatewayClientDAO().insert(gatewayClient)
    }

    fun delete(gatewayClient: GatewayClient) {
        datastore.gatewayClientDAO().delete(gatewayClient)
    }

}

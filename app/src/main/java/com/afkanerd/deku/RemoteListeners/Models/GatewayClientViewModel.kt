package com.afkanerd.deku.RemoteListeners.Models

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.Modules.ThreadingPoolExecutor

class GatewayClientViewModel : ViewModel() {
    private lateinit var gatewayClientList: LiveData<List<GatewayClient>>

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
        ThreadingPoolExecutor.executorService.execute {
            datastore.gatewayClientDAO().update(gatewayClient)
        }
    }

}

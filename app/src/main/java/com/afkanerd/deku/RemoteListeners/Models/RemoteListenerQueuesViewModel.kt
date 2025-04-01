package com.afkanerd.deku.RemoteListeners.Models

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.afkanerd.deku.Datastore

class RemoteListenerQueuesViewModel : ViewModel() {
    private lateinit var datastore: Datastore

    var remoteListenerQueues by mutableStateOf<RemoteListenersQueues?>(null)

    private lateinit var liveData : LiveData<List<RemoteListenersQueues>>
    fun get(context: Context, gatewayClientId: Long): LiveData<List<RemoteListenersQueues>>{
        datastore = Datastore.getDatastore(context)
        if(!::liveData.isInitialized) {
            liveData = MutableLiveData()
            liveData = datastore.remoteListenersQueuesDao().fetchGatewayClientId(gatewayClientId)
        }
        return liveData
    }

    fun insert(remoteListenersQueues: RemoteListenersQueues) {
        datastore.remoteListenersQueuesDao().insert(remoteListenersQueues)
    }

    fun update(remoteListenersQueues: RemoteListenersQueues) {
        datastore.remoteListenersQueuesDao().update(remoteListenersQueues)
    }

    fun delete(context: Context, remoteListenerId: Long) {
        Datastore.getDatastore(context).remoteListenersQueuesDao().delete(remoteListenerId)
    }
}

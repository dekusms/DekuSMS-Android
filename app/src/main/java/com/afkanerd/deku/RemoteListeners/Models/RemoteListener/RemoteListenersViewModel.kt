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
import androidx.lifecycle.MutableLiveData
import com.afkanerd.deku.RemoteListeners.Models.RemoteListeners
import com.afkanerd.deku.RemoteListeners.RMQ.RMQConnectionHandler
import com.afkanerd.deku.RemoteListeners.RemoteListenerConnectionService

class RemoteListenersViewModel(context: Context? = null) : ViewModel() {
    private lateinit var remoteListenersList: LiveData<List<RemoteListeners>>
    private var rmqConnectionHandlers: LiveData<List<RMQConnectionHandler>> = MutableLiveData()

    var remoteListener by mutableStateOf<RemoteListeners?>(null)

    private lateinit var datastore: Datastore

    var binder: RemoteListenerConnectionService.LocalBinder? = null

    /** Defines callbacks for service binding, passed to bindService().  **/
    val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            binder = service as RemoteListenerConnectionService.LocalBinder
            rmqConnectionHandlers = binder!!.getService().getRmqConnections()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
        }
    }

    init {
        context?.let {
            Intent(context, RemoteListenerConnectionService::class.java).also { intent ->
                context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    fun getRmqConnections(): LiveData<List<RMQConnectionHandler>> {
        return rmqConnectionHandlers
    }

    fun get(context: Context): LiveData<List<RemoteListeners>> {
        datastore = Datastore.getDatastore(context)
        if(!::remoteListenersList.isInitialized) {
            remoteListenersList = loadGatewayClients()
        }
        return remoteListenersList
    }

    private fun loadGatewayClients() : LiveData<List<RemoteListeners>> {
        return datastore.remoteListenerDAO().fetch()
    }

    fun update(context: Context, remoteListeners: RemoteListeners) {
        Datastore.getDatastore(context).remoteListenerDAO().update(remoteListeners)
    }

    fun insert(context: Context, remoteListeners: RemoteListeners) {
        Datastore.getDatastore(context).remoteListenerDAO().insert(remoteListeners)
    }

    fun delete(remoteListeners: RemoteListeners) {
        datastore.remoteListenerDAO().delete(remoteListeners)
    }

}

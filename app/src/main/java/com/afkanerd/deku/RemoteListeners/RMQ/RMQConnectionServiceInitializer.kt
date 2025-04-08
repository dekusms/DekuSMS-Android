package com.afkanerd.deku.RemoteListeners.RMQ

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.startup.Initializer
import androidx.work.WorkManagerInitializer
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.NotificationsInitializer
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RMQConnectionServiceInitializer : Initializer<Intent> {
    override fun create(context: Context): Intent {
        val intent = Intent(context, RMQConnectionService::class.java)

        CoroutineScope(Dispatchers.Default).launch {
            Datastore.getDatastore(context).remoteListenerDAO().all.apply {
                this.forEach {
                    if(it.activated)
                        RemoteListenersHandler.toggleRemoteListeners(context, it)
                }
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                } catch(e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        return intent
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(WorkManagerInitializer::class.java,
                NotificationsInitializer::class.java)
    }
}
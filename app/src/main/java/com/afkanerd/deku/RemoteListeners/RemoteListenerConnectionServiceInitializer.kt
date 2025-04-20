package com.afkanerd.deku.RemoteListeners

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.startup.Initializer
import androidx.work.WorkManagerInitializer
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.NotificationsInitializer
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersHandler
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RemoteListenerConnectionServiceInitializer : Initializer<Intent> {
    override fun create(context: Context): Intent {
        val intent = Intent(context, RemoteListenerConnectionService::class.java)

        if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) ==
            PackageManager.PERMISSION_GRANTED) {
            CoroutineScope(Dispatchers.Default).launch {
                Datastore.getDatastore(context).remoteListenerDAO().fetchActivated().apply {
                    if(this.any { it.activated }) {
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
            }
        }
        return intent
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(
            WorkManagerInitializer::class.java,
                NotificationsInitializer::class.java)
    }
}
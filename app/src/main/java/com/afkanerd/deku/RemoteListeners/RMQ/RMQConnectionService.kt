package com.afkanerd.deku.RemoteListeners.RMQ

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.MainActivity
import com.afkanerd.deku.RemoteListeners.Models.GatewayClient
import com.afkanerd.deku.RemoteListeners.Models.RemoteListener.RemoteListenersViewModel
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class RMQConnectionService : Service() {
    private lateinit var gatewayClientListLiveData: LiveData<List<GatewayClient>>
    private lateinit var workManagerLiveData: LiveData<List<WorkInfo>>

    private val remoteListenersViewModel: RemoteListenersViewModel = RemoteListenersViewModel()

    private var numberOfActiveRemoteListeners = 0
    private var numberFailedToStart = 0
    private var numberWaitingToStart = 0
    private var numberStarting = 0
    private var numberStarted = 0

    private var rmqConnectionHandlers : MutableLiveData<List<RMQConnectionHandler>> = MutableLiveData()

    private val remoteListenerObserver = Observer<List<GatewayClient>> {
        it.forEach { remoteListener ->
            if(remoteListener.activated) {
                numberOfActiveRemoteListeners += 1
                createForegroundNotification()
                RemoteListenersHandler.startWorkManager(applicationContext, remoteListener)
            }
        }
    }

    // TODO: use this for more insights but not sure if here is the best place
    private val workManagerObserver = Observer<List<WorkInfo>> {
        var numberFailedToStart = 0
        var numberWaitingToStart = 0
        var numberStarting = 0
        var numberStarted = 0

        it.forEach { workInfo ->
            when(workInfo.state) {
                WorkInfo.State.ENQUEUED -> {
                    numberWaitingToStart += 1
                }
                WorkInfo.State.RUNNING -> {
                    numberStarting += 1
                }
                WorkInfo.State.SUCCEEDED -> {
//                    numberStarted +=1
                }
                WorkInfo.State.FAILED -> {
                    numberFailedToStart += 1
                }
                WorkInfo.State.BLOCKED -> {}
                WorkInfo.State.CANCELLED -> {}
            }
        }
        this.numberFailedToStart = numberFailedToStart
        this.numberWaitingToStart = numberWaitingToStart
//        this.numberStarted = numberStarted
        this.numberStarting = numberStarting
        createForegroundNotification()
    }

    fun putRmqConnection(rmqConnection: RMQConnectionHandler) {
        var list = (rmqConnectionHandlers.value ?: mutableListOf()).plus(rmqConnection)
        rmqConnectionHandlers.postValue(list)
    }

    // Binder given to clients.
    private val binder = LocalBinder()
    /**
     * Class used for the client Binder. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): RMQConnectionService = this@RMQConnectionService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        rmqConnectionHandlers.observeForever {
            numberStarted = rmqConnectionHandlers.value?.size ?: 0
            createForegroundNotification()
        }
        createForegroundNotification()
    }

    override fun onDestroy() {
        super.onDestroy()

        workManagerLiveData.removeObserver(workManagerObserver)
        gatewayClientListLiveData.removeObserver(remoteListenerObserver)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Put content in intent which can be used to kill this in future
        workManagerLiveData = WorkManager.getInstance(applicationContext)
            .getWorkInfosByTagLiveData(RemoteListenersHandler.UNIQUE_WORK_MANAGER_TAG).apply {
                observeForever(workManagerObserver)
            }

        gatewayClientListLiveData = remoteListenersViewModel.get(applicationContext).apply {
            observeForever(remoteListenerObserver)
        }
        return START_STICKY
    }

    private fun createForegroundNotification() {
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent
                .getActivity(applicationContext,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE)

        val title = "$numberOfActiveRemoteListeners Active..."
        val description = ""
            .plus("# Failed to start: ")
            .plus("$numberFailedToStart\n")
            .plus("# Waiting to start: ")
            .plus("$numberWaitingToStart\n")
            .plus("# Starting: ")
            .plus("$numberStarting\n")
            .plus("# Connected: ")
            .plus(numberStarted)

        val notification =
                NotificationCompat.Builder(
                    applicationContext,
                    getString(R.string.running_gateway_clients_channel_id))
                    .setContentTitle(title)
                    .setContentText("Status")
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setSilent(true)
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
                    .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(description))
                    .build()
                    .apply {
                        flags = Notification.FLAG_ONGOING_EVENT
                    }

        val notificationId = getString(R.string.gateway_client_service_notification_id).toInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(notificationId, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else startForeground(notificationId, notification)
    }

}
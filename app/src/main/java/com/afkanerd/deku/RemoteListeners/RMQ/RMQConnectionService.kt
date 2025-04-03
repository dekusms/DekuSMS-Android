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

class RMQConnectionService : Service() {
    private lateinit var remoteListenersLiveData: LiveData<List<GatewayClient>>
    private lateinit var workManagerLiveData: LiveData<List<WorkInfo>>
    private var rmqConnectionHandlers : MutableLiveData<Set<RMQConnectionHandler>> = MutableLiveData()

    private lateinit var remoteListenersViewModel: RemoteListenersViewModel

    private var numberOfActiveRemoteListeners = 0
    private var numberFailedToStart = 0
    private var numberWaitingToStart = 0
    private var numberStarting = 0
    private var numberStarted = 0

    // TODO: when the state changes in here, you should know - else would have false readings
    private val rmqConnectionHandlerObserver = Observer<Set<RMQConnectionHandler>> { rl ->
        numberStarted = rl.filter { it.connection.isOpen }.size
        createForegroundNotification()
    }

    private val remoteListenerObserver = Observer<List<GatewayClient>> {
        var numberOfActiveRemoteListeners = 0
        it.forEach { remoteListener ->
            if(remoteListener.activated) {
                numberOfActiveRemoteListeners += 1
                RemoteListenersHandler.startWorkManager(applicationContext, remoteListener)
            }
        }
        this.numberOfActiveRemoteListeners = numberOfActiveRemoteListeners
        createForegroundNotification()
    }

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

    fun changes(rmqConnection: RMQConnectionHandler) {
        rmqConnectionHandlers.value?.find{ rmqConnection.id == it.id }.let {
            it?.let {
                rmqConnectionHandlers.postValue(
                    rmqConnectionHandlers.value!!
                        .minusElement(it)
                        .plusElement(rmqConnection)
                )
            }
        }
    }

    fun putRmqConnection(rmqConnection: RMQConnectionHandler) {
        rmqConnectionHandlers.postValue((rmqConnectionHandlers.value ?: mutableSetOf())
            .plus(rmqConnection))
    }

    fun getRmqConnection(remoteListenerId: Long) : RMQConnectionHandler? {
        return rmqConnectionHandlers.value?.find { it.id == remoteListenerId }
    }

    fun getRmqConnections(): LiveData<Set<RMQConnectionHandler>> {
        return rmqConnectionHandlers
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
        remoteListenersViewModel = RemoteListenersViewModel(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()

        workManagerLiveData.removeObserver(workManagerObserver)
        rmqConnectionHandlers.removeObserver(rmqConnectionHandlerObserver)
        remoteListenersLiveData.removeObserver(remoteListenerObserver)
        rmqConnectionHandlers.value?.forEach {  it.close() }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Put content in intent which can be used to kill this in future
        createForegroundNotification()
        workManagerLiveData = WorkManager.getInstance(applicationContext)
            .getWorkInfosByTagLiveData(RemoteListenersHandler.UNIQUE_WORK_MANAGER_TAG).apply {
                observeForever(workManagerObserver)
            }

        remoteListenersLiveData = remoteListenersViewModel.get(applicationContext).apply {
            observeForever(remoteListenerObserver)
        }

        rmqConnectionHandlers.observeForever(rmqConnectionHandlerObserver)

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
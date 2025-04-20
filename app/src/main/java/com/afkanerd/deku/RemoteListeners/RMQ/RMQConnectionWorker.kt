package com.afkanerd.deku.RemoteListeners.RMQ

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.provider.Telephony
import android.telephony.SubscriptionInfo
import android.util.Log
import androidx.core.content.ContextCompat
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.BroadcastReceivers.IncomingTextSMSBroadcastReceiver
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.NativeSMSDB
import com.afkanerd.deku.DefaultSMS.Models.SIMHandler
import com.afkanerd.deku.DefaultSMS.Models.SMSDatabaseWrapper
import com.afkanerd.deku.Modules.SemaphoreManager
import com.afkanerd.deku.RemoteListeners.Models.RemoteListeners
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersHandler
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersQueues
import com.afkanerd.deku.RemoteListeners.RemoteListenerConnectionService
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.ConsumerShutdownSignalCallback
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import com.rabbitmq.client.ShutdownSignalException
import com.rabbitmq.client.impl.DefaultExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.json.JSONException
import org.junit.Assert
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RMQConnectionWorker(
    val context: Context,
    val gatewayClientId: Long
) {

    /**
     * - Start connection
     * - Create channels (per simcard per queue)
     * - Connect to exchange (create if not exist)
     * - Connect to queues (create if not exist)
     */

    @Serializable
    private data class SMSRequest(val text: String, val to: String, val sid: String, val id: Int)

    private lateinit var rmqConnectionHandler: RMQConnectionHandler
    private val factory = ConnectionFactory()

    private val databaseConnector: Datastore = Datastore.getDatastore(context)

    private lateinit var messageStateChangedBroadcast: BroadcastReceiver

    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    init {
        handleBroadcast()
    }

    private lateinit var mService: RemoteListenerConnectionService
    /** Defines callbacks for service binding, passed to bindService().  */
    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            val binder = service as RemoteListenerConnectionService.LocalBinder
            mService = binder.getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
        }
    }

    fun start(): RMQConnectionHandler {
        Log.d(javaClass.name, "Starting new service connection...")

        Intent(context, RemoteListenerConnectionService::class.java).also { intent ->
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        val remoteListener = Datastore.getDatastore(context).remoteListenerDAO()
            .fetch(gatewayClientId)

        factory.username = remoteListener.username
        factory.password = remoteListener.password
        factory.virtualHost = remoteListener.virtualHost
        factory.host = remoteListener.hostUrl
        factory.port = remoteListener.port
        factory.exceptionHandler = DefaultExceptionHandler()

        /**
         * Increase connectivity sensitivity
         */
        factory.isAutomaticRecoveryEnabled = false

        startConnection(factory, remoteListener)
        try {
            mService.putRmqConnection(rmqConnectionHandler)
        } catch(e: Exception) {
            e.printStackTrace()
        }

        return rmqConnectionHandler
    }

    private fun startConnection(factory: ConnectionFactory, remoteListener: RemoteListeners) {
        Log.d(javaClass.name, "Starting new connection...")

        try {
            val connection = factory.newConnection(
                executorService,
                remoteListener.friendlyConnectionName
            )

            connection.addShutdownListener {
                /**
                 * The logic here, if the user has not deactivated this - which can be known
                 * from the database connection state then reconnect this client.
                 */
                Log.e(javaClass.name, "Connection shutdown cause: $it")
                if (it.isInitiatedByApplication) {
                    mService.changes(rmqConnectionHandler)
                    mService.unbindService(serviceConnection)
                } else if (remoteListener.activated) {
                    mService.changes(rmqConnectionHandler)
                    mService.unbindService(serviceConnection)
                }
            }

            rmqConnectionHandler = RMQConnectionHandler(remoteListener.id, connection)
        } catch(e: Exception) {
            e.printStackTrace()
            if(::rmqConnectionHandler.isInitialized)
                rmqConnectionHandler.close()
            throw e
        }

        try {
            val remoteListenerQueues = databaseConnector.remoteListenersQueuesDao()
                .fetchRemoteListenersQueues(remoteListener.id)

            val subscriptionInfoList: List<SubscriptionInfo> =
                SIMHandler.getSimCardInformation(context)

            /**
             * Due to prefetch count, we need just one channel per simcard
             * - High number of throughput would overwhelm sending and lead to massive failures
             */
            remoteListenerQueues.forEachIndexed { i, rlq ->
                subscriptionInfoList.forEachIndexed { simSlot, subscriptionInfo ->
                    val channelNumber = RemoteListenersHandler.getCarrierId(subscriptionInfo)
                    if(rmqConnectionHandler.hasChannel(rlq, channelNumber)) {
                        rmqConnectionHandler.getChannel(rlq, channelNumber)
                    } else {
                        rmqConnectionHandler.createChannel(
                            rlq,
                            channelNumber,
                        ) .apply { this?.basicRecover(true) }
                    }?.let { channel ->

                        val bindingName: String? = when(simSlot) {
                            0 -> {
                                rmqConnectionHandler.createExchange(rlq.name!!, channel)
                                rlq.binding1Name
                            }
                            1 -> rlq.binding2Name
                            else -> null
                        }

                        channel.addShutdownListener {
                            Log.e(javaClass.name, "Channel shutdown cause: $it")

                            if(channel.connection.isOpen) {
                                bindingName?.let {
                                    startChannelConsumption(
                                        rmqConnectionHandler,
                                        channel,
                                        subscriptionInfo.subscriptionId,
                                        rlq,
                                        bindingName
                                    )
                                }
                            }
                            rmqConnectionHandler.updateChannel(rlq, channel)
                        }

                        bindingName?.let {
                            startChannelConsumption(
                                rmqConnectionHandler,
                                channel,
                                subscriptionInfo.subscriptionId,
                                rlq,
                                bindingName
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun startChannelConsumption(
        rmqConnectionHandler: RMQConnectionHandler,
        channel: Channel,
        subscriptionId: Int,
        remoteListenersQueues: RemoteListenersQueues,
        bindingName: String
    ) {
        val deliverCallback = getDeliverCallback(
            subscriptionId,
            rmqConnectionHandler.id
        )
        val queueName = rmqConnectionHandler.createQueue(
            exchangeName = remoteListenersQueues.name!!,
            bindingKey = bindingName,
            channel = channel,
        )
        rmqConnectionHandler.createExchange(remoteListenersQueues.name!!, channel)

        val consumerTag = channel.basicConsume(
            queueName,
            false,
            deliverCallback,
            object : ConsumerShutdownSignalCallback {
                override fun handleShutdownSignal(
                    consumerTag: String,
                    sig: ShutdownSignalException
                ) {
                    sig.printStackTrace()
                    rmqConnectionHandler.removeChannelWithConsumerTag(consumerTag)
                }
            })

        rmqConnectionHandler.bindChannelConsumerTag(
            consumerTag,
            remoteListenersQueues,
            channel
        )
    }

    private fun handleBroadcast() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(IncomingTextSMSBroadcastReceiver.SMS_SENT_BROADCAST_INTENT)
        messageStateChangedBroadcast = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != null && intentFilter.hasAction(intent.action)) {
                    if (intent.hasExtra(RMQConnectionHandler.MESSAGE_SID) &&
                        intent.hasExtra(RMQConnectionHandler.RMQ_DELIVERY_TAG)) {

                        val sid = intent.getStringExtra(RMQConnectionHandler.MESSAGE_SID)
                        val messageId = intent.getStringExtra(NativeSMSDB.ID)

                        val consumerTag = intent.getStringExtra(RMQConnectionHandler.RMQ_CONSUMER_TAG)
                        val deliveryTag =
                            intent.getLongExtra(RMQConnectionHandler.RMQ_DELIVERY_TAG, -1)

                        Assert.assertTrue(!consumerTag.isNullOrEmpty())
                        Assert.assertTrue(deliveryTag != -1L)

                        rmqConnectionHandler.findQueueByGatewayClientId(rmqConnectionHandler.id)
                            ?.let { remoteListenerQueue ->
                                rmqConnectionHandler.findChannelByTag(
                                    consumerTag!!
                                )?.let {
                                    Log.d(javaClass.name, "Received an ACK of the message...")
                                    CoroutineScope(Dispatchers.Default).launch {
                                        try {
                                            if (resultCode == Activity.RESULT_OK) {
                                                if (it.isOpen) it.basicAck(deliveryTag, false)
                                            } else {
                                                if (it.isOpen) it.basicReject(deliveryTag, true)
                                            }
                                        } catch(e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                    }
                }
            }
        }

        ContextCompat.registerReceiver(
            context,
            messageStateChangedBroadcast,
            intentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    private fun sendSMS(
        smsRequest: SMSRequest,
        subscriptionId: Int,
        consumerTag: String,
        deliveryTag: Long,
        rmqConnectionId: Long
    ) {
        val messageId = System.currentTimeMillis()

        val threadId = Telephony.Threads.getOrCreateThreadId(context, smsRequest.to)

        val bundle = Bundle()
        bundle.putString(RMQConnectionHandler.MESSAGE_SID, smsRequest.sid)
        bundle.putString(RMQConnectionHandler.RMQ_CONSUMER_TAG, consumerTag)
        bundle.putLong(RMQConnectionHandler.RMQ_DELIVERY_TAG, deliveryTag)
        bundle.putLong(RMQConnectionHandler.RMQ_ID, rmqConnectionId)

        val conversation = Conversation()
        conversation.message_id = messageId.toString()
        conversation.text = smsRequest.text
        conversation.address = smsRequest.to
        conversation.subscription_id = subscriptionId
        conversation.type = Telephony.Sms.MESSAGE_TYPE_OUTBOX
        conversation.date = System.currentTimeMillis().toString()
        conversation.thread_id = threadId.toString()
        conversation.status = Telephony.Sms.STATUS_PENDING
        conversation.isRemoteListener = true

        databaseConnector.conversationDao()._insert(conversation)
        SMSDatabaseWrapper.send_text(context, conversation, bundle)
        Log.d(javaClass.name, "SMS sent...")
    }

    private fun getDeliverCallback(
        subscriptionId: Int,
        rmqConnectionId: Long
    ): DeliverCallback {
        return DeliverCallback { consumerTag: String, delivery: Delivery ->
            rmqConnectionHandler.findChannelByTag(
                consumerTag = consumerTag
            )?.let { channel ->
                val message = String(delivery.body, StandardCharsets.UTF_8)
                Log.d(javaClass.name, "Remote listener incoming: $message")
                val smsRequest: SMSRequest? = run {
                    try {
                        return@run Json.decodeFromString<SMSRequest>(message)
                    } catch(e: SerializationException) {
                        e.printStackTrace()
                        channel.let {
                            if (it.isOpen)
                                it.basicReject(delivery.envelope.deliveryTag, false)
                        }
                    } catch(e: Exception) {
                        e.printStackTrace()
                    }
                    null
                }
                smsRequest?.let {
                    try {
                        sendSMS(
                            it,
                            subscriptionId,
                            consumerTag,
                            delivery.envelope.deliveryTag,
                            rmqConnectionId
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        when(e) {
                            is SerializationException -> {
                                channel.let {
                                    if (it.isOpen)
                                        it.basicReject(delivery.envelope.deliveryTag, false)
                                }
                            }
                            is IllegalArgumentException -> {
                                channel.let {
                                    if (it.isOpen)
                                        it.basicReject(delivery.envelope.deliveryTag, true)
                                }
                            }
                            else -> {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }
}

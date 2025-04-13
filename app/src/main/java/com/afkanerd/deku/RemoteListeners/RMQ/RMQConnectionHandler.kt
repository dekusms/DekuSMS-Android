package com.afkanerd.deku.RemoteListeners.RMQ

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersQueues
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection

class RMQConnectionHandler(var id: Long, var connection: Connection) {
    private val autoDelete: Boolean = false
    private val exclusive: Boolean = false
    private val durable: Boolean = true

    private val channelConsumerTags: MutableMap<String, Channel> = mutableMapOf()

    private val remoteListenersChannelLiveData:
            MutableLiveData<MutableMap<RemoteListenersQueues, List<Channel>>> = MutableLiveData()

    fun hasChannel(remoteListenersQueues: RemoteListenersQueues, channelNumber: Int): Boolean {
        return remoteListenersChannelLiveData.value?.get(remoteListenersQueues)
            ?.find { it.channelNumber == channelNumber } != null
    }

    fun getChannel(remoteListenersQueues: RemoteListenersQueues, channelNumber: Int) : Channel? {
        return remoteListenersChannelLiveData.value?.get(remoteListenersQueues)
            ?.find { it.channelNumber == channelNumber }
    }

    fun updateChannel(remoteListenersQueues: RemoteListenersQueues, channel: Channel)  {
        remoteListenersChannelLiveData.value?.get(remoteListenersQueues).let { channels ->
            if(channels?.find { channel == it } != null) {
                val channels: MutableMap<RemoteListenersQueues, List<Channel>>? =
                    remoteListenersChannelLiveData.value

                channels?.get(remoteListenersQueues)?.toMutableList().let {
                    it?.let {
                        val index = it.indexOf(channel)
                        val listChannels: MutableList<Channel> = it
                        listChannels[index] = channel

                        remoteListenersChannelLiveData.value?.let { rlChannels ->
                            rlChannels[remoteListenersQueues] = it
                            remoteListenersChannelLiveData.postValue(rlChannels)
                        }
                    }
                }
            }
        }
    }

    /**
     * Channel numbers cannot go beyond the max - connection.channelMax - current 2047
     * https://github.com/rabbitmq/amqp-0.9.1-spec/blob/main/docs/amqp-0-9-1-reference.md#--------tune------------------------------------shortchannel-max----------------------------------------------------longframe-max----------------------------------------------------shortheartbeat------------------------tune-ok------------
     */
    fun createChannel(
        remoteListenersQueues: RemoteListenersQueues,
        channelNumber: Int? = null
    ): Channel? {
        val prefetchCount = 1
        val channel =  (
                if(channelNumber != null)
                    connection.createChannel(channelNumber)
                else connection.createChannel()
        )
        channel?.let {
            it.basicQos(prefetchCount)
            val channels = remoteListenersChannelLiveData.value ?: mutableMapOf()
            if(channels.isEmpty() || !channels.containsKey(remoteListenersQueues))
                channels.put(remoteListenersQueues, listOf(channel))
            else {
                channels[remoteListenersQueues] = channels[remoteListenersQueues]!!
                    .plusElement(channel)
            }
            remoteListenersChannelLiveData.postValue(channels)
        }

        return channel
    }

    fun getChannelsLiveData(): LiveData<MutableMap<RemoteListenersQueues, List<Channel>>> {
        return remoteListenersChannelLiveData
    }

    fun removeChannelWithConsumerTag(consumerTag: String): Channel? {
        return channelConsumerTags.remove(consumerTag)
    }

    fun bindChannelConsumerTag(
        consumerTag: String,
        remoteListenersQueues: RemoteListenersQueues,
        channel: Channel
    ) {
        channelConsumerTags[consumerTag] = remoteListenersChannelLiveData.value
            ?.get(remoteListenersQueues)?.find{ it == channel }!!
    }

    fun findChannelByTag( consumerTag: String ) : Channel? {
        return channelConsumerTags[consumerTag]
    }

    fun findQueueByGatewayClientId(id: Long): RemoteListenersQueues? {
        return remoteListenersChannelLiveData.value?.keys?.first {
            it.gatewayClientId == id
        }
    }

    fun close() {
        if (connection.isOpen)
            connection.close()
    }

    fun createExchange(
        exchangeName: String,
        channel: Channel,
    ): AMQP.Exchange.DeclareOk? {
        return channel.exchangeDeclare(
            exchangeName,
            BuiltinExchangeType.TOPIC,
            true
        )
    }

    fun createQueue(
        exchangeName: String,
        bindingKey: String,
        channel: Channel,
        queueName: String = getQueueName(bindingKey),
    ) : String {
        channel.queueDeclare(queueName, durable, exclusive, autoDelete, null)
        channel.queueBind(queueName, exchangeName, bindingKey)

        return queueName
    }

    companion object {
        const val MESSAGE_SID: String = "sid"
        const val RMQ_ID: String = "RMQ_ID"
        const val RMQ_DELIVERY_TAG: String = "RMQ_DELIVERY_TAG"
        const val RMQ_CONSUMER_TAG: String = "RMQ_CONSUMER_TAG"
        const val REMOTE_LISTENER_QUEUE_ID: String = "REMOTE_LISTENER_QUEUE_ID"

        fun getQueueName(binding: String): String {
            return binding.replace("\\.".toRegex(), "_")
        }
    }
}

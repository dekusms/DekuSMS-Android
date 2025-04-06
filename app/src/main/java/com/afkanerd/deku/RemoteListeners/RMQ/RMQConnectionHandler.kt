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

    private val channelList: MutableList<Channel> = ArrayList()
    private val channelTagMap = mutableMapOf<String, Channel>()

    private val remoteListenersChannelLiveData:
            MutableLiveData<MutableMap<RemoteListenersQueues, List<Channel>>> = MutableLiveData()

    fun removeChannel(channel: Channel) {
        channelList.remove(channel)
    }

    fun hasChannel(remoteListenersQueues: RemoteListenersQueues, channelNumber: Int): Boolean {
        return remoteListenersChannelLiveData.value?.get(remoteListenersQueues)
            ?.find { it.channelNumber == channelNumber } != null
    }

    fun getChannel(remoteListenersQueues: RemoteListenersQueues, channelNumber: Int) : Channel? {
        return remoteListenersChannelLiveData.value?.get(remoteListenersQueues)
            ?.find { it.channelNumber == channelNumber }
    }

    fun createChannel(
        remoteListenersQueues: RemoteListenersQueues,
        channelNumber: Int? = null
    ): Channel {
        val channel =  (if(channelNumber != null) connection.createChannel(channelNumber)
        else connection.createChannel()).apply {
            val prefetchCount = 1
            basicQos(prefetchCount)
        }

        val channels = remoteListenersChannelLiveData.value ?: mutableMapOf()
        if(channels.isEmpty() || !channels.containsKey(remoteListenersQueues))
            channels.put(remoteListenersQueues, listOf(channel))
        else {
            channels[remoteListenersQueues] = channels[remoteListenersQueues]!!
                .plusElement(channel)
        }
        remoteListenersChannelLiveData.postValue(channels)

        return channel
    }

    fun getChannelsLiveData(): LiveData<MutableMap<RemoteListenersQueues, List<Channel>>> {
        return remoteListenersChannelLiveData
    }

    fun bindChannelToTag(channel: Channel, channelTag: String)  {
        channelTagMap[channelTag] = channel
    }

    fun findChannelByTag(channelTag: String) : Channel? {
        return channelTagMap[channelTag]
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

        fun getQueueName(binding: String): String {
            return binding.replace("\\.".toRegex(), "_")
        }


    }
}

package com.afkanerd.deku.RemoteListeners.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.util.TableInfo
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersHandler
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersQueues
import com.afkanerd.deku.RemoteListeners.RMQ.RMQConnectionHandler
import com.example.compose.AppTheme


@Composable
fun RemoteListenersQueuesCard(
    remoteListenersQueues: RemoteListenersQueues,
    channel1Number: Int,
    channel2Number: Int? = null,
    onClickListener: () -> Unit
) {
    Card(onClick = onClickListener ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(remoteListenersQueues.name!!)

            Spacer(modifier = Modifier.padding(8.dp))

            Row(
                Modifier.fillMaxWidth()
            ) {
                QueueComponent(
                    bindingName = remoteListenersQueues.binding1Name!!,
                    queueName = RMQConnectionHandler
                        .getQueueName(remoteListenersQueues.binding1Name!!,),
                    channelNumber = if(channel1Number > 0) channel1Number.toString()
                    else stringResource(R.string.disconnected),
                    messageCount = "0",
                    status = channel1Number > 0
                )
                Spacer(Modifier.weight(1f))

                channel2Number?.let {
                    QueueComponent(
                        bindingName = remoteListenersQueues.binding2Name!!,
                        queueName = RMQConnectionHandler
                            .getQueueName(remoteListenersQueues.binding2Name!!,),
                        channelNumber = if(channel2Number > 0) channel2Number.toString()
                        else stringResource(R.string.disconnected),
                        messageCount = "0",
                        status = channel2Number > 0
                    )
                }
            }
        }
    }
}

@Composable
fun QueueComponent(
    bindingName: String,
    queueName: String,
    channelNumber: String,
    messageCount: String,
    status: Boolean,
) {
    Column {
        Row {
            Text(
                stringResource(R.string.sim_1),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                bindingName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Row {
            Text(
                stringResource(R.string.sim_1_queue),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                queueName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(Modifier.padding(8.dp))
        Row {
            Text(
                stringResource(R.string.channel_number),
                style = MaterialTheme.typography.labelSmall,
                color = if(status) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.secondary,
            )
            Text(
                channelNumber,
                style = MaterialTheme.typography.bodySmall,
                color = if(status) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.secondary,
            )
        }

        Row {
            Text(
                stringResource(R.string.messages),
                style = MaterialTheme.typography.labelSmall,
                color = if(status) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.secondary,
            )
            Text(
                messageCount,
                style = MaterialTheme.typography.bodySmall,
                color = if(status) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.secondary,
            )
        }
    }
}


@Composable
@Preview
fun QueuesCards_Preview() {
    val remoteListenersQueues = RemoteListenersQueues()
    remoteListenersQueues.name = "Exchange"
    remoteListenersQueues.binding1Name = "sim_1_binding"
    remoteListenersQueues.binding2Name = "sim_2_binding"
    AppTheme {
        RemoteListenersQueuesCard(remoteListenersQueues, 1, 0) {}
    }
}

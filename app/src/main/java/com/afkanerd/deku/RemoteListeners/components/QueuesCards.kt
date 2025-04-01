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
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.RemoteListeners.Models.GatewayClient
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersQueues
import com.example.compose.AppTheme


@Composable
fun QueuesCards(
    remoteListenersQueues: RemoteListenersQueues,
    onClickListener: () -> Unit
) {
    Card(onClick = onClickListener ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(remoteListenersQueues.name)

            Spacer(modifier = Modifier.padding(8.dp))

            Row {
                Text( "SIM 1:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    remoteListenersQueues.binding1Name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.padding(4.dp))

            Row {
                Text(
                    "SIM 2:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    remoteListenersQueues.binding2Name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
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
        QueuesCards(remoteListenersQueues) {}
    }
}

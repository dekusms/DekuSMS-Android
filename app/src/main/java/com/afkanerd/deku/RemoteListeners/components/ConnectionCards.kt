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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.afkanerd.deku.RemoteListeners.Models.GatewayClient
import com.afkanerd.deku.RemoteListeners.Models.GatewayClientViewModel
import com.example.compose.AppTheme


@Composable
fun ConnectionCards(
    remoteListeners: GatewayClient,
    onClickCallback: () -> Unit,
) {
    Card(
        onClick = onClickCallback
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(remoteListeners.username!!)
            Text(
                remoteListeners.hostUrl!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Row {
                Text(
                    "Port: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    remoteListeners.port.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.padding(8.dp))
            Row {
                Text(
                    "Virtual host: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    remoteListeners.virtualHost!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}


@Composable
@Preview
fun ConnectionCards_Preview() {
    val gatewayClient = GatewayClient()
    gatewayClient.id = 0
    gatewayClient.hostUrl = "amqp://example.com"
    gatewayClient.virtualHost = "/"
    gatewayClient.port = 5671
    gatewayClient.username = "example_user"
    AppTheme {
        ConnectionCards(gatewayClient) {}
    }
}
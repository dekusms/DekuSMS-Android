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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.RemoteListeners.Models.GatewayClient
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersHandler
import com.example.compose.AppTheme


@Composable
fun RemoteListenerCards(
    remoteListeners: GatewayClient,
    modifier: Modifier,
) {
    val context = LocalContext.current
    Card( modifier = modifier ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(remoteListeners.username!!,
                color = if(remoteListeners.activated) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondary,
                fontWeight = if(remoteListeners.activated) FontWeight.SemiBold else null
            )
            Spacer(modifier = Modifier.padding(2.dp))
            Text(
                remoteListeners.hostUrl!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.padding(2.dp))
            Row {
                Text(
                    stringResource(R.string.port),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    remoteListeners.port.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.padding(2.dp))
            Row {
                Text(
                    stringResource(R.string.virtual_host),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    remoteListeners.virtualHost!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }


            Spacer(modifier = Modifier.padding(8.dp))

            Row {
                Text(
                    "state: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    if(remoteListeners.activated) "Activated" else "Deactivated",
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if(remoteListeners.activated) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.padding(2.dp))

            Row {
                Text(
                    "status: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    "Activated",
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if(remoteListeners.activated) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondary
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
    gatewayClient.activated = true
    AppTheme {
        RemoteListenerCards(gatewayClient, Modifier)
    }
}
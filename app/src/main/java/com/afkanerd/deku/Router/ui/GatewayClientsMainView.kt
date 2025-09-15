package com.afkanerd.deku.Router.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.afkanerd.deku.Router.data.models.GatewayServer
import com.afkanerd.deku.Router.ui.viewModels.GatewayServerViewModel
import com.example.compose.AppTheme

@Composable
fun GatewayClientsMainView(
    viewModel: GatewayServerViewModel
) {
    val context = LocalContext.current
    val gatewayClients by viewModel[context].observeAsState(emptyList())

    Scaffold { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            LazyColumn {
                items(
                    items = gatewayClients,
                    key = {it.id}
                ) { gatewayClients ->
                    GatewayServerCard(gatewayClients)
                }
            }
        }
    }
}

@Composable
fun GatewayServerCard(gatewayClient: GatewayServer) {
    Card(onClick = {}) {
        Column {
            Text(gatewayClient.URL.toString())
            Text(gatewayClient.protocol.toString())
        }
    }
}

@Preview
@Composable
fun GatewayServerCardPreview() {
    AppTheme {
        val gatewayServer = GatewayServer()
        GatewayServerCard(gatewayServer)
    }
}
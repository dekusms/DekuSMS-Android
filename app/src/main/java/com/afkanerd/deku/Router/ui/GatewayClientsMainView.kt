package com.afkanerd.deku.Router.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.GatewayClientsListScreen
import com.afkanerd.deku.Router.data.models.GatewayServer
import com.afkanerd.deku.Router.ui.viewModels.GatewayServerViewModel
import com.example.compose.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GatewayClientsMainView(
    navController: NavController,
    viewModel: GatewayServerViewModel
) {
    val context = LocalContext.current
    val gatewayClients by viewModel[context].observeAsState(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "go back"
                        )
                    }
                },

                title = {Text(stringResource(R.string.gateway_clients))},
                actions = {
                    IconButton(onClick = {
                        TODO("Create new Gateway clients modals")
                    }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            LazyColumn {
                items(
                    items = gatewayClients,
                    key = {it.id},
                ) { gatewayClient ->
                    Column {
                        GatewayServerCard( gatewayClient ) {
                            TODO("Show modal")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GatewayServerCard(
    gatewayClient: GatewayServer,
    onClickCallback: () -> Unit,
) {
    Card(onClick = onClickCallback) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row {
                Text(gatewayClient.URL.toString())
                Spacer(Modifier.weight(1f))
                Text(gatewayClient.date.toString())
            }
            Spacer(Modifier.padding(8.dp))
            Text(gatewayClient.protocol.toString())
        }
    }
}

@Preview
@Composable
fun GatewayServerCardPreview() {
    AppTheme {
        val gatewayServer = GatewayServer()
        GatewayServerCard(gatewayServer){}
    }
}
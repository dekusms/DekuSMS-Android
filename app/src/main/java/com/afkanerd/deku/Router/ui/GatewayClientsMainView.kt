package com.afkanerd.deku.Router.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.GatewayClientsListScreen
import com.afkanerd.deku.Router.data.models.GatewayServer
import com.afkanerd.deku.Router.ui.modals.GatewayServerAddHttpModal
import com.afkanerd.deku.Router.ui.modals.GatewayServerAddSmtpModal
import com.afkanerd.deku.Router.ui.viewModels.GatewayServerViewModel
import com.afkanerd.smswithoutborders_libsmsmms.ui.navigation.SettingsScreenNav
import com.example.compose.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GatewayClientsMainView(
    navController: NavController,
    viewModel: GatewayServerViewModel
) {
    val context = LocalContext.current
    val gatewayClients by viewModel[context].observeAsState(emptyList())

    var rememberMenuExpanded by remember { mutableStateOf( false)}

    var showAddHttpModal by remember { mutableStateOf( false)}
    var showAddSmtpModal by remember { mutableStateOf( false)}

    Box(modifier = Modifier
        .fillMaxWidth()
        .wrapContentSize(Alignment.TopEnd)
    ) {
        DropdownMenu(
            expanded = LocalInspectionMode.current || rememberMenuExpanded,
            onDismissRequest = { rememberMenuExpanded = false },
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.add_http_forwarders),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onClick = {
                    showAddHttpModal = true
                    rememberMenuExpanded = false
                }
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text = "Add SMTP forwarder",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onClick = {
                    showAddSmtpModal = true
                    rememberMenuExpanded = false
                }
            )
        }
    }

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
                        rememberMenuExpanded = !rememberMenuExpanded
                    }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(com.afkanerd.lib_smsmms_android.R.string.open_menu)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(Modifier
            .padding(innerPadding)
            .padding(8.dp)
        ) {
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

            if(showAddHttpModal) {
                GatewayServerAddHttpModal(
                    showBottomSheet = showAddHttpModal,
                    viewModel = viewModel,
                ) {
                    showAddHttpModal = false
                }
            }

            if(showAddSmtpModal) {
                GatewayServerAddSmtpModal(
                    showBottomSheet = showAddSmtpModal,
                    viewModel = viewModel,
                ) {
                    showAddSmtpModal = false
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
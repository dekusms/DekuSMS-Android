package com.afkanerd.deku.RemoteListeners.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.afkanerd.deku.RemoteListeners.Models.GatewayClient
import com.afkanerd.deku.RemoteListeners.Models.GatewayClientViewModel
import com.afkanerd.deku.RemoteListeners.components.ConnectionCards
import com.example.compose.AppTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalInspectionMode
import com.afkanerd.deku.RemoteListeners.modals.RemoteListenerModal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RMQMainComposable(
    _remoteListeners: List<GatewayClient> = emptyList<GatewayClient>(),
    remoteListenerViewModel: GatewayClientViewModel,
    navController: NavController,
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val remoteListeners: List<GatewayClient> = if(LocalInspectionMode.current) _remoteListeners
    else remoteListenerViewModel.get(context).observeAsState(emptyList()).value

    var showRemoteListenerModal by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehaviour.nestedScrollConnection),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(8.dp)
                .padding(innerPadding)
        ) {
            Column {
                if( remoteListeners.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("No remote listeners")
                    }
                }
                else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState
                    ) {
                        itemsIndexed(
                            items = remoteListeners,
                            key = { index, remoteListener -> remoteListener.id}
                        ) { index, remoteListener ->
                            ConnectionCards(remoteListener) { showRemoteListenerModal = true }
                        }
                    }
                }
            }

            if(showRemoteListenerModal) {
                RemoteListenerModal(
                    showModal = showRemoteListenerModal,
                    addQueueCallback = {},
                    editCallback = {},
                    deleteCallback = {}
                ) {
                    showRemoteListenerModal = false
                }
            }
        }
    }
}

@Composable
@Preview
fun ConnectionCards_Preview() {
    AppTheme {
        val gatewayClient = GatewayClient()
        gatewayClient.id = 0
        gatewayClient.hostUrl = "amqp://example.com"
        gatewayClient.virtualHost = "/"
        gatewayClient.port = 5671
        gatewayClient.username = "example_user"
        RMQMainComposable(
            listOf(gatewayClient),
            GatewayClientViewModel(),
            rememberNavController(),
        )
    }
}

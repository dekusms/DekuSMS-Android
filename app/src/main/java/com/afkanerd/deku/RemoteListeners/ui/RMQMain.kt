package com.afkanerd.deku.RemoteListeners.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Vertical
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.afkanerd.deku.RemoteListeners.Models.GatewayClient
import com.afkanerd.deku.RemoteListeners.Models.RemoteListener.RemoteListenersViewModel
import com.afkanerd.deku.RemoteListeners.components.RemoteListenerCards
import com.example.compose.AppTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersHandler
import com.afkanerd.deku.RemoteListeners.Models.RemoteListener.RemoteListenerQueuesViewModel
import com.afkanerd.deku.RemoteListeners.RMQ.RMQConnectionHandler
import com.afkanerd.deku.RemoteListeners.RMQ.RMQConnectionService
import com.afkanerd.deku.RemoteListeners.modals.RemoteListenerModal
import com.afkanerd.deku.RemoteListenersAddScreen
import com.afkanerd.deku.RemoteListenersQueuesScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RMQMainComposable(
    _remoteListeners: List<GatewayClient> = emptyList<GatewayClient>(),
    remoteListenerViewModel: RemoteListenersViewModel,
    remoteListenerProjectsViewModel: RemoteListenerQueuesViewModel,
    navController: NavController,
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val remoteListeners: List<GatewayClient> = if(LocalInspectionMode.current) _remoteListeners
    else remoteListenerViewModel.get(context).observeAsState(emptyList()).value

    val rmqConnectionHandlers: List<RMQConnectionHandler> =
        if(LocalInspectionMode.current) emptyList<RMQConnectionHandler>()
    else remoteListenerViewModel.getRmqConnections().observeAsState(emptyList()).value

    var showRemoteListenerModal by remember { mutableStateOf(false) }

    BackHandler {
        remoteListenerViewModel.remoteListener = null
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Remote listeners") },
                navigationIcon = {
                    IconButton(onClick = {
                        remoteListenerViewModel.remoteListener = null
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Return back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        remoteListenerViewModel.remoteListener = null
                        navController.navigate(RemoteListenersAddScreen)
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.AddCircleOutline,
                            contentDescription = "New remote listener"
                        )
                    }
                },
                scrollBehavior = scrollBehaviour
            )
        },
        modifier = Modifier
            .nestedScroll(scrollBehaviour.nestedScrollConnection),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(8.dp)
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            Column {
                if( remoteListeners.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Text(
                            stringResource(R.string.no_remote_listeners),
                            style = MaterialTheme.typography.titleMedium)
                    }
                }
                else {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("\u2022 Click to add queues",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.padding(4.dp))
                        Text("\u2022 Press and hold to manage",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Column(modifier = Modifier.padding(8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Icon(imageVector =  Icons.Outlined.Info, "")
                                Text(
                                    stringResource(R.string.only_1_connection_at_a_time_due_to_the_bottleneck_between_channels_and_phone_radios_ability_to_sms_messages_in_parallel),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier
                                        .padding(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.padding(8.dp))

                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            items = remoteListeners,
                            key = { _, remoteListener -> remoteListener.id}
                        ) { _, remoteListener ->
                            RemoteListenerCards(
                                remoteListener,
                                if(LocalInspectionMode.current) true else
                                rmqConnectionHandlers.find{ remoteListener.id == it.id}
                                    ?.connection?.isOpen == true,
                                modifier = Modifier.combinedClickable(
                                    onClick = {
                                        remoteListenerViewModel.remoteListener = remoteListener
                                        navController.navigate(RemoteListenersQueuesScreen)
                                    },
                                    onLongClick = {
                                        remoteListenerViewModel.remoteListener = remoteListener
                                        showRemoteListenerModal = true
                                    }
                                ),
                            )
                        }
                    }
                }
            }

            if(showRemoteListenerModal) {
                val activated = remoteListenerViewModel.remoteListener?.activated == true
                RemoteListenerModal(
                    showModal = showRemoteListenerModal,
                    activated = activated,
                    editCallback = {
                        showRemoteListenerModal = false
                        navController.navigate(RemoteListenersAddScreen)
                    },
                    connectionCallback = {
                        if(activated) {
                            //Deactivating
                            remoteListenerViewModel.remoteListener?.activated = false
                            RemoteListenersHandler.stopListening(
                                context,
                                remoteListenerViewModel.remoteListener!!
                            )
                        } else {
                            //Activating
                            remoteListenerViewModel.remoteListener?.activated = true
                            RemoteListenersHandler.startListening(
                                context,
                                remoteListenerViewModel.remoteListener!!
                            )
                        }
                        showRemoteListenerModal = false
                    },
                    deleteCallback = {
                        CoroutineScope(Dispatchers.Default).launch {
                            remoteListenerProjectsViewModel.delete(
                                context,
                                remoteListenerViewModel.remoteListener!!.id
                            )
                            remoteListenerViewModel.delete(
                                remoteListenerViewModel.remoteListener!!
                            )
                            showRemoteListenerModal = false
                        }
                    }
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

        val gatewayClient1 = GatewayClient()
        gatewayClient1.id = 1
        gatewayClient1.hostUrl = "amqp://example.com"
        gatewayClient1.virtualHost = "/"
        gatewayClient1.port = 5671
        gatewayClient1.username = "example_user"

        RMQMainComposable(
            listOf(gatewayClient, gatewayClient1),
            RemoteListenersViewModel(),
            RemoteListenerQueuesViewModel(),
            rememberNavController(),
        )
    }
}

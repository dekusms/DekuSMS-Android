package com.afkanerd.deku.RemoteListeners.modals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.afkanerd.deku.DefaultSMS.BuildConfig
import com.example.compose.AppTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.input.ImeAction
import com.afkanerd.deku.DefaultSMS.Models.SIMHandler
import com.afkanerd.deku.RemoteListeners.Models.GatewayClient
import com.afkanerd.deku.RemoteListeners.Models.GatewayClientHandler
import com.afkanerd.deku.RemoteListeners.Models.RemoteListener.RemoteListenerQueuesViewModel
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersQueues
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteListenerAddQueuesModal(
    showModal: Boolean,
    remoteListener: GatewayClient,
    remoteListenerQueuesViewModel: RemoteListenerQueuesViewModel,
    dismissCallback: () -> Unit,
) {
    val state = rememberStandardBottomSheetState(
        initialValue = if(BuildConfig.DEBUG) SheetValue.Expanded else SheetValue.Hidden,
        skipHiddenState = false
    )

    val remoteListenersQueue = remoteListenerQueuesViewModel.remoteListenerQueues

    var showModal by remember { mutableStateOf(showModal) }

    var exchange by remember { mutableStateOf(remoteListenersQueue?.name ?: "") }
    var sim1Queue by remember { mutableStateOf(remoteListenersQueue?.binding1Name ?: "") }
    var sim2Queue by remember { mutableStateOf(remoteListenersQueue?.binding2Name ?: "") }

    val context = LocalContext.current
    val inspectMode = LocalInspectionMode.current

    val isDualSim by remember{
        mutableStateOf(if(inspectMode) true else SIMHandler.isDualSim(context))
    }

    if(showModal) {
        ModalBottomSheet(
            onDismissRequest = {
                showModal = false
                dismissCallback()
            },
            sheetState = state,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("New Queues", style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.padding(8.dp))

                OutlinedTextField(
                    value = exchange,
                    supportingText = {
                        Text("Defaulting to Topic exchange")
                    },
                    onValueChange = {
                        exchange = GatewayClientHandler.getPublisherDetails(context, it)
                            .let { details ->
                                if(details.isNotEmpty()) {
                                    sim1Queue = details[0]
                                    if(details.getOrNull(1) != null)
                                        sim2Queue = details[1]
                                }
                                it
                            }
                    },
                    placeholder = {
                        Text("Exchange")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                    )
                )

                Spacer(Modifier.padding(16.dp))

                Column(Modifier.fillMaxWidth()) {
                    Row {
                        Text(
                            "Auto-filled to: ",
                            style=MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text("{exchange}.{operator_country_code}.{operator_id}",
                            style=MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                OutlinedTextField(
                    value = sim1Queue,
                    onValueChange = { sim1Queue = it },
                    label = {
                        Text("Sim 1 Queue name")
                    },
                    supportingText = {
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                    )
                )

                if(isDualSim) {
                    OutlinedTextField(
                        value = sim2Queue,
                        onValueChange = { sim2Queue = it },
                        label = {
                            Text("Sim 2 Queue name")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                        )
                    )
                }

                Spacer(Modifier.padding(16.dp))

                Button(onClick = {
                    val newRemoteListenerQueues = remoteListenersQueue ?: RemoteListenersQueues()
                    newRemoteListenerQueues.name = exchange
                    newRemoteListenerQueues.binding1Name = sim1Queue
                    newRemoteListenerQueues.binding2Name = sim2Queue
                    newRemoteListenerQueues.gatewayClientId = remoteListener.id

                    CoroutineScope(Dispatchers.Default).launch {
                        if(remoteListenersQueue != null)
                            remoteListenerQueuesViewModel.update(newRemoteListenerQueues)
                        else
                            remoteListenerQueuesViewModel.insert(newRemoteListenerQueues)
                        dismissCallback()
                    }
                }, enabled = exchange.isNotBlank() && sim1Queue.isNotEmpty()) {
                    Text(if(remoteListenersQueue == null) "Add" else "Edit")
                }
            }
        }
    }
}

@Composable
@Preview
fun RemoteListenersAddQueuesModal_Preview() {
    AppTheme {
        RemoteListenerAddQueuesModal(
            true,
            GatewayClient(),
            RemoteListenerQueuesViewModel()
        ){}
    }
}

package com.afkanerd.deku.RemoteListeners.modals

import android.Manifest
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.afkanerd.deku.DefaultSMS.Models.SIMHandler
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.RemoteListeners.Models.RemoteListeners
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersHandler
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersQueues
import com.afkanerd.deku.RemoteListeners.RMQ.RMQConnectionHandler
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RemoteListenerAddQueuesModal(
    showModal: Boolean,
    remoteListenersQueue: RemoteListenersQueues?,
    remoteListener: RemoteListeners,
    onClickCallback: (RemoteListenersQueues) -> Unit,
    dismissCallback: () -> Unit,
) {
    val state = rememberStandardBottomSheetState(
        initialValue = if(BuildConfig.DEBUG) SheetValue.Expanded else SheetValue.Hidden,
        skipHiddenState = false
    )

    var showModal by remember { mutableStateOf(showModal) }

    var exchange by remember { mutableStateOf(remoteListenersQueue?.name ?: "") }
    var sim1Binding by remember { mutableStateOf(remoteListenersQueue?.binding1Name ?: "") }
    var sim2Binding by remember { mutableStateOf(remoteListenersQueue?.binding2Name ?: "") }

    var sim1QueueName by remember { mutableStateOf("") }
    var sim2QueueName by remember { mutableStateOf("") }

    val context = LocalContext.current
    val inspectMode = LocalInspectionMode.current

    val isDualSim by remember{
        mutableStateOf(if(inspectMode) true else SIMHandler.isDualSim(context))
    }

    LaunchedEffect(sim1Binding, sim2Binding) {
        sim1QueueName = RMQConnectionHandler.getQueueName(sim1Binding)
        sim2QueueName = RMQConnectionHandler.getQueueName(sim1Binding)
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
                Text(stringResource(R.string.new_queues), style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.padding(8.dp))

                OutlinedTextField(
                    value = exchange,
                    supportingText = {
                        Text(stringResource(R.string.defaulting_to_topic_exchange))
                    },
                    onValueChange = {
                        exchange = RemoteListenersHandler.getPublisherDetails(context, it)
                            .let { details ->
                                if(details.isNotEmpty()) {
                                    sim1Binding = details[0]
                                    if(details.getOrNull(1) != null)
                                        sim2Binding = details[1]
                                }
                                it
                            }
                    },
                    placeholder = {
                        Text(stringResource(R.string.exchange))
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
                            stringResource(R.string.auto_filled_to),
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
                    value = sim1Binding,
                    onValueChange = {
                        sim1Binding = it
                    },
                    label = {
                        Text(stringResource(R.string.sim_1_binding))
                    },
                    supportingText = {
                        Row {
                            Text(stringResource(R.string.queue_name))
                            Text(sim1QueueName)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                    )
                )
                Spacer(Modifier.padding(8.dp))

                if(isDualSim) {

                    Column(Modifier.fillMaxWidth()) {
                        Row {
                            Text(
                                stringResource(R.string.auto_filled_to),
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
                        value = sim2Binding,
                        onValueChange = {
                            sim2Binding = it
                        },
                        label = {
                            Row {
                                Text(stringResource(R.string.queue_name))
                                Text(sim2QueueName)
                            }
                        },
                        supportingText = {
                            Text(stringResource(R.string.queue_name, sim2QueueName))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                        )
                    )
                }

                Spacer(Modifier.padding(16.dp))

                if(remoteListenersQueue != null) {
                    Text(
                        stringResource(R.string.editing_a_queue_would_restart_the_connection),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Button(onClick = {
                    val rlq = RemoteListenersQueues()
                    rlq.name = exchange
                    rlq.binding1Name = sim1Binding
                    rlq.binding2Name = sim2Binding
                    rlq.gatewayClientId = remoteListener.id

                    onClickCallback(rlq)
                }, enabled = exchange.isNotBlank() && sim1Binding.isNotEmpty()) {
                    Text(if(remoteListenersQueue == null) stringResource(R.string.add)
                    else stringResource( R.string.edit ))
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
            RemoteListenersQueues(),
            RemoteListeners(),
            {}
        ){}
    }
}

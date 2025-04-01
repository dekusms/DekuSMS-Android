package com.afkanerd.deku.RemoteListeners.ui

import android.inputmethodservice.Keyboard.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.afkanerd.deku.DefaultSMS.ui.ModalDrawerSheetLayout
import com.example.compose.AppTheme
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.afkanerd.deku.RemoteListeners.Models.GatewayClient
import com.afkanerd.deku.RemoteListeners.Models.GatewayClientViewModel
import com.afkanerd.deku.RemoteListenersScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun RMQAddComposable(
    navController: NavController,
    gatewayClientViewModel: GatewayClientViewModel
) {

    var hostUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var friendlyName by remember { mutableStateOf("") }
    var virtualHost by remember { mutableStateOf("/") }
    var port by remember { mutableIntStateOf(5672) }

    val protocolOptions = listOf("amqp", "amqps")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(protocolOptions[0]) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = hostUrl,
                onValueChange = { hostUrl = it },
                placeholder = {
                    Text("Host Url")
                },
                prefix = {
                    Text("amqp(s)://")
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.padding(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = {
                    Text("Username")
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.padding(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text("Password")
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.padding(8.dp))

            OutlinedTextField(
                value = friendlyName,
                onValueChange = { friendlyName = it },
                placeholder = {
                    Text("Friendly name")
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.padding(8.dp))

            OutlinedTextField(
                value = virtualHost,
                onValueChange = { virtualHost = it },
                placeholder = {
                    Text("Virtual host")
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.padding(8.dp))

            OutlinedTextField(
                value = port.toString(),
                onValueChange = { port = it.toInt() },
                placeholder = {
                    Text("Port")
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number
                )
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Text("Choose protocol")

            Column(Modifier.selectableGroup()) {
                protocolOptions.forEach { text ->
                    Row (
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (text == selectedOption),
                                onClick = { onOptionSelected(text) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (text == selectedOption),
                            enabled = text != "amqps",
                            onClick = null // null recommended for accessibility with screen readers
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.padding(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = {
                    val remoteListener = GatewayClient()
                    remoteListener.hostUrl = hostUrl
                    remoteListener.username = username
                    remoteListener.password = password
                    remoteListener.friendlyConnectionName = friendlyName
                    remoteListener.virtualHost = virtualHost
                    remoteListener.port = port.toInt()
                    remoteListener.protocol = selectedOption

                    CoroutineScope(Dispatchers.Default).launch {
                        gatewayClientViewModel.insert(remoteListener)
                        navController.popBackStack(RemoteListenersScreen, false)
                    }
                }, enabled = hostUrl.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                    Text("Add")
                }
            }
        }
    }
}

@Preview
@Composable
fun RMQAddComposable_Preview() {
    AppTheme {
        RMQAddComposable( navController = rememberNavController(), GatewayClientViewModel())
    }
}

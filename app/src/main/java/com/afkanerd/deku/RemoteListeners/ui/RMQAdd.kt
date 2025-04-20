package com.afkanerd.deku.RemoteListeners.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.compose.AppTheme
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.afkanerd.deku.DefaultSMS.BuildConfig
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.RemoteListeners.Models.RemoteListeners
import com.afkanerd.deku.RemoteListeners.Models.RemoteListener.RemoteListenersViewModel
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersHandler
import com.afkanerd.deku.RemoteListenersScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RMQAddComposable(
    navController: NavController,
    remoteListenerViewModel: RemoteListenersViewModel
) {

    val remoteListener = remoteListenerViewModel.remoteListener

    var hostUrl by remember { mutableStateOf(remoteListener?.hostUrl ?: "" ) }
    var username by remember { mutableStateOf(remoteListener?.username ?: "" ) }
    var password by remember { mutableStateOf(remoteListener?.password ?: "" ) }
    var friendlyName by remember { mutableStateOf(remoteListener?.friendlyConnectionName ?: "" ) }
    var virtualHost by remember { mutableStateOf(remoteListener?.virtualHost ?: "/" ) }
    var port by remember { mutableIntStateOf(remoteListener?.port ?: 5672 ) }

    var passwordVisible by remember { mutableStateOf(false) }

    val protocolOptions = listOf("amqp", "amqps")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(protocolOptions[0]) }

    if(BuildConfig.DEBUG && remoteListener == null) {
        LaunchedEffect(Unit) {
            hostUrl = "staging.smswithoutborders.com"
            username = "sherlock"
            password = "asshole"
            friendlyName = "android-emulator"
        }
    }

    BackHandler {
        remoteListenerViewModel.remoteListener = null
        navController.popBackStack()
    }

    val scrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_remote_listener))},
                navigationIcon = {
                    IconButton(onClick = {
                        remoteListenerViewModel.remoteListener = null
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.return_back)
                        )
                    }
                },
                actions = { },
                scrollBehavior = scrollBehaviour
            )
        },
        modifier = Modifier
            .nestedScroll(scrollBehaviour.nestedScrollConnection),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .imePadding()
                .padding(innerPadding)
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = hostUrl,
                onValueChange = { hostUrl = it },
                label = {
                    Text(stringResource(R.string.host_url))
                },
                placeholder = {
                    Text(stringResource(R.string.example_com))
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
                label = {
                    Text(stringResource(R.string.username))
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
                label = {
                    Text(stringResource(R.string.password))
                },
                visualTransformation =
                    if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    val description =
                        if(passwordVisible) stringResource(R.string.hide_password)
                        else stringResource( R.string.show_password )
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, description)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.padding(8.dp))

            OutlinedTextField(
                value = friendlyName,
                onValueChange = { friendlyName = it },
                label = {
                    Text(stringResource(R.string.friendly_name))
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
                label = {
                    Text(stringResource(R.string.virtual_host))
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
                label = {
                    Text(stringResource(R.string.port))
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number
                )
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Text(stringResource(R.string.choose_protocol))

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
                    val newRemoteListener = remoteListener ?: RemoteListeners()
                    newRemoteListener.hostUrl = hostUrl
                    newRemoteListener.username = username
                    newRemoteListener.password = password
                    newRemoteListener.friendlyConnectionName = friendlyName
                    newRemoteListener.virtualHost = virtualHost
                    newRemoteListener.port = port.toInt()
                    newRemoteListener.protocol = selectedOption
                    newRemoteListener.activated = false

                    CoroutineScope(Dispatchers.Default).launch {
                        if(remoteListener != null)
                            remoteListenerViewModel.update(context, newRemoteListener)
                        else
                            remoteListenerViewModel.insert(context, newRemoteListener)

                        launch(Dispatchers.Main) {
                            if(!navController.popBackStack(RemoteListenersScreen, false)) {
                                navController.navigate(RemoteListenersScreen) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    }
                }, enabled = hostUrl.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                    Text(
                        if(remoteListener == null) stringResource(R.string.add)
                        else stringResource(R.string.edit)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun RMQAddComposable_Preview() {
    AppTheme {
        RMQAddComposable( navController = rememberNavController(), RemoteListenersViewModel(
            LocalContext.current
        ))
    }
}

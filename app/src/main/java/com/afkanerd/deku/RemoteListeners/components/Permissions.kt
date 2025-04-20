package com.afkanerd.deku.RemoteListeners.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.afkanerd.deku.RemoteListeners.ui.requiredNotificationsPermissions
import com.afkanerd.deku.RemoteListeners.ui.requiredReadPhoneStatePermissions
import com.afkanerd.deku.RemoteListeners.ui.requiredReceiveSMSPermission
import com.afkanerd.deku.RemoteListeners.ui.requiredSendSMSPermission
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermissionComposable() {
    val context = LocalContext.current
    val getNotificationsPermissionsLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                isGranted ->
            if(isGranted) {
                Toast.makeText(context, "Well done!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "We cannot do without this one...", Toast.LENGTH_LONG)
                    .show()
            }
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
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(imageVector =  Icons.Outlined.Info, "", tint=MaterialTheme.colorScheme.secondary)
                    Text(
                        "When not default SMS app, you need to grant permissions for listeners to show notifications when changing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(start=16.dp, end=16.dp, top=8.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = {
                        getNotificationsPermissionsLauncher.launch(requiredNotificationsPermissions)
                    }) {
                        Text("Grant notification permission")
                    }
                }
            }
        }
    }
}

@Composable
fun PhoneStatePermissionComposable() {
    val context = LocalContext.current
    val getReadPhoneStatePermissionsLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                isGranted ->
            if(isGranted) {
                Toast.makeText(context, "Well done, carry on!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "You can at anytime...", Toast.LENGTH_LONG).show()
            }
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
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(imageVector =  Icons.Outlined.Info, "", tint=MaterialTheme.colorScheme.secondary)
                    Text(
                        "When not default SMS app, you need to grant permissions for listeners to check for sim status and dual sim information.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(start=16.dp, end=16.dp, top=8.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = {
                        getReadPhoneStatePermissionsLauncher
                            .launch(requiredReadPhoneStatePermissions)
                    }) {
                        Text("Grant phone state permission")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SMSPermissionComposable() {
    val context = LocalContext.current
    val getSMSPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                isGranted ->
            if(isGranted) {
                Toast.makeText(context, "Carry on activating...", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "I'd be back!", Toast.LENGTH_LONG).show()
            }
        }

    val smsReadSMSState = rememberPermissionState(requiredReceiveSMSPermission)
    val smsSendSMSState = rememberPermissionState(requiredSendSMSPermission)

    Column(modifier = Modifier.padding(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(imageVector =  Icons.Outlined.Info, "", tint=MaterialTheme.colorScheme.secondary)
                    Text(
                        "When not default SMS app, you need to grant permissions for listeners to send and receive delivery reports.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(start=16.dp, end=16.dp, top=8.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if(!smsSendSMSState.status.isGranted || LocalInspectionMode.current) {
                        TextButton(onClick = {
                            getSMSPermissionLauncher.launch(requiredSendSMSPermission)
                        }) {
                            Text("Grant send sms permission")
                        }
                    }
                    if(!smsReadSMSState.status.isGranted || LocalInspectionMode.current) {
                        TextButton(onClick = {
                            getSMSPermissionLauncher.launch(requiredReceiveSMSPermission)
                        }) {
                            Text("Grant read sms permission")
                        }
                    }
                }
            }
        }
    }
}

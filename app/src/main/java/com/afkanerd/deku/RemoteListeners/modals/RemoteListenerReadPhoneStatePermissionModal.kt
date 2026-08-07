package com.afkanerd.deku.RemoteListeners.modals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.afkanerd.deku.DefaultSMS.BuildConfig
import com.afkanerd.deku.DefaultSMS.R
import com.example.compose.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteListenersReadPhoneStatePermissionModal(
    showModal: Boolean,
    grantPermissionsCallback: () -> Unit,
    dismissCallback: () -> Unit
) {
    val state = rememberStandardBottomSheetState(
        initialValue = if(BuildConfig.DEBUG) SheetValue.Expanded else SheetValue.Hidden,
        skipHiddenState = false
    )
    var showModal by remember { mutableStateOf(showModal) }

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
                Text(
                    stringResource(R.string.permission_required_title),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.padding(16.dp))

                Text(
                    stringResource(R.string.remote_listener_phone_state_permission_description),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.padding(8.dp))
                Text(
                    stringResource(R.string.remote_listener_phone_state_permission_hint),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(Modifier.padding(12.dp))
                Button(onClick = grantPermissionsCallback) {
                    Text(stringResource(R.string.permission_grant))
                }
            }
        }
    }}

@Composable
@Preview
fun RemoteListenerReadPhoneStatePermission_Preview() {
    AppTheme {
        RemoteListenersReadPhoneStatePermissionModal(true, {}){}
    }
}


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
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteListenerModal(
    showModal: Boolean,
    activated: Boolean,
    editCallback: () -> Unit,
    connectionCallback: () -> Unit,
    deleteCallback: () -> Unit,
    dismissCallback: () -> Unit,
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
                Text("Configure Remote listener",
                    style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.padding(8.dp))

                Button(onClick = connectionCallback, modifier = Modifier.fillMaxWidth()) {
                    Text(if(activated) "Deactivate" else "Activate")
                }
                Text(
                    if(activated)
                        "Deactivating stops the remote listener and tries to kill all remote connections."
                    else "Activating begins the service that tries to connect this remote listener",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.padding(16.dp))

                Button(onClick = editCallback, modifier = Modifier.fillMaxWidth()) {
                    Text("Edit" )
                }

                TextButton(onClick = deleteCallback) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
@Preview
fun RemoteListenersModal_Preview() {
    AppTheme {
        RemoteListenerModal(true, true, {}, {}, {}) {}
    }
}

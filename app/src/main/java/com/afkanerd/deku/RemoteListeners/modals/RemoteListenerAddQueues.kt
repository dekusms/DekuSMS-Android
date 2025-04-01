package com.afkanerd.deku.RemoteListeners.modals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.afkanerd.deku.DefaultSMS.BuildConfig
import com.example.compose.AppTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteListenerAddQueuesModal(
    showModal: Boolean,
    dismissCallback: () -> Unit,
) {
    val state = rememberStandardBottomSheetState(
        initialValue = if(BuildConfig.DEBUG) SheetValue.Expanded else SheetValue.Hidden,
        skipHiddenState = false
    )

    var showModal by remember { mutableStateOf(showModal) }

    var exchange by remember { mutableStateOf("") }
    var sim1Queue by remember { mutableStateOf("") }
    var sim2Queue by remember { mutableStateOf("") }

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

                OutlinedTextField(
                    value = exchange,
                    onValueChange = { exchange = it },
                    placeholder = {
                        Text("Exchange")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                    )
                )

                OutlinedTextField(
                    value = sim1Queue,
                    onValueChange = { sim1Queue = it },
                    placeholder = {
                        Text("Queue name")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                    )
                )
                OutlinedTextField(
                    value = exchange,
                    onValueChange = { exchange = it },
                    placeholder = {
                        Text("Exchange")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                    )
                )
            }
        }
    }
}

@Composable
@Preview
fun RemoteListenersAddQueuesModal_Preview() {
    AppTheme {
        RemoteListenerAddQueuesModal(true, {})
    }
}

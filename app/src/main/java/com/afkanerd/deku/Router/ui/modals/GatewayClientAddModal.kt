package com.afkanerd.deku.Router.ui.modals

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.Router.data.models.GatewayServer
import com.afkanerd.deku.Router.ui.viewModels.GatewayServerViewModel
import com.example.compose.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GatewayServerAddHttpModal(
    showBottomSheet: Boolean,
    viewModel: GatewayServerViewModel,
    onDismissCallback: () -> Unit,
) {
    val context = LocalContext.current

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = false
    )

    var url: String by remember{ mutableStateOf("") }
    var tag: String by remember{ mutableStateOf("") }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismissCallback,
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = {},
                    label = {
                        Text(stringResource(R.string.enter_url))
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                )

                OutlinedTextField(
                    value = tag,
                    onValueChange = {},
                    label = {
                        Text(stringResource(R.string.enter_tag_optional))
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                )

                Button(onClick = {
                    viewModel.add(
                        context, GatewayServer().apply {
                            this.URL = url
                            this.tag = tag
                        },
                    ) {
                    }
                }) {

                }
            }
        }
    }
}

@Preview
@Composable
fun GatewayClientAddModalPreview() {
    AppTheme {
        GatewayServerAddHttpModal(
            true,
            remember{ GatewayServerViewModel() }) {
        }
    }
}
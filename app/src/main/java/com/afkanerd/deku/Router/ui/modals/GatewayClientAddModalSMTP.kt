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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.Router.data.models.GatewayServer
import com.afkanerd.deku.Router.data.models.SMTP
import com.afkanerd.deku.Router.ui.viewModels.GatewayServerViewModel
import com.example.compose.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GatewayServerAddSmtpModal(
    showBottomSheet: Boolean,
    viewModel: GatewayServerViewModel,
    onDismissCallback: () -> Unit,
) {
    val context = LocalContext.current

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = false
    )

    var host by remember{ mutableStateOf("") }
    var username by remember{ mutableStateOf("") }
    var password by remember{ mutableStateOf("") }
    var from by remember{ mutableStateOf("") }
    var recipient by remember{ mutableStateOf("") }
    var subject: String by remember{ mutableStateOf("") }
    var port by remember{ mutableStateOf("587") }
    var passwordVisibility: Boolean by remember { mutableStateOf(false) }

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
                    value = host,
                    onValueChange = {},
                    label = {
                        Text(stringResource(R.string.enter_url))
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = {},
                    label = {
                        Text(stringResource(R.string.enter_username))
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                )

                OutlinedTextField(
                    value = password,
                    visualTransformation = if (passwordVisibility)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    onValueChange = {},
                    label = {
                        Text(stringResource(R.string.enter_password))
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )

                OutlinedTextField(
                    value = recipient,
                    onValueChange = {},
                    label = {
                        Text(stringResource(R.string.enter_recipients_separated_by_comma))
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                )

                OutlinedTextField(
                    value = from,
                    onValueChange = {},
                    label = {
                        Text(stringResource(R.string.from_label_optional))
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = {},
                    label = {
                        Text(stringResource(R.string.enter_url))
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                )

                OutlinedTextField(
                    value = port,
                    onValueChange = {},
                    label = {
                        Text(stringResource(R.string.enter_port_number))
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                )

                Button(onClick = {
                    viewModel.add(
                        context, GatewayServer().apply {
                            this.smtp = SMTP(
                                host,
                                username,
                                password,
                                from,
                                recipient,
                                subject,
                                port.toInt()
                            )
                        },
                    ) {
                    }
                }) {
                    Text(stringResource(R.string.add))
                }
            }
        }
    }
}

@Preview
@Composable
fun GatewayServerAddSmtpModalPreview() {
    AppTheme {
        GatewayServerAddSmtpModal(
            true,
            remember{ GatewayServerViewModel() }) {
        }
    }
}

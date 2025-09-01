package com.afkanerd.deku.DefaultSMS.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.EncryptionController
import androidx.core.net.toUri
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.retrieveContactName
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun SecureRequestAcceptModal(
    address: String,
    show: Boolean = true,
    displayMode: EncryptionController.SecureRequestMode,
    dismissCallback: (() -> Unit)? = {},
    onRequestClickedCallback: () -> Unit,
) {
    val context = LocalContext.current
    val inPreviewMode = LocalInspectionMode.current
    val state = rememberStandardBottomSheetState(
        initialValue = if(inPreviewMode) SheetValue.Expanded else SheetValue.Hidden,
        skipHiddenState = false
    )

    var contactName by remember{
        mutableStateOf( context.retrieveContactName(address) ?: address )}

    val url = stringResource(
        R.string.conversations_secure_conversation_request_information_deku_encryption_link)
    val intent = remember{ Intent(Intent.ACTION_VIEW, url.toUri()) }

    val scope = rememberCoroutineScope()

    if(show) {
        ModalBottomSheet(
            onDismissRequest = {
                dismissCallback?.invoke()
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
                when(displayMode) {
                    EncryptionController.SecureRequestMode.REQUEST_RECEIVED -> {
                        Text(
                            text = stringResource(
                                com.afkanerd.deku.DefaultSMS.R.string.has_requested_to_secure_this_conversation,
                                contactName
                            ),
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            text = stringResource(R.string
                                .conversations_secure_conversation_request_information),
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        Button(onClick = {
                            scope.launch { state.hide() }
                            onRequestClickedCallback()
                        }) {
                            Text(stringResource(R.string.conversations_secure_conversation_request_agree))
                        }

                        TextButton (onClick={
                            context.startActivity(intent)
                        }) {
                            Text(stringResource(R.string
                                .conversations_secure_conversation_request_information_deku_encryption_read_more))
                        }
                    }
                    else -> {
                        Text(
                            text = stringResource(R.string.secure_request),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom=16.dp),
                        )

                        Text(
                            text = stringResource(com.afkanerd.deku.DefaultSMS.R.string.to_1),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                        )

                        Text(
                            text = address,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom=16.dp),
                        )

                        Text(
                            text = stringResource(
                                R.string
                                    .conversation_secure_popup_request_menu_description),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = stringResource(
                                R.string
                                    .conversation_secure_popup_request_menu_description_subtext),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp)
                        )

                        Button (onClick = {
                            scope.launch { state.hide() }
                            onRequestClickedCallback()
                        }, modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.request))
                        }
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Preview(showBackground = true, name = "Secure Request Modal - Request Flow")
@Composable
fun SecureRequestModal_RequestFlow_Preview() {
    SecureRequestAcceptModal(
        "+237123456789",
        true,
        displayMode = EncryptionController.SecureRequestMode.REQUEST_NONE,
        dismissCallback = {}
    ){}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Preview(showBackground = true, name = "Secure Request Modal - Accept Flow")
@Composable
fun SecureRequestModal_AcceptFlow_Preview() {
    SecureRequestAcceptModal(
        "+237123456789",
        true,
        displayMode = EncryptionController.SecureRequestMode.REQUEST_RECEIVED,
        dismissCallback = {}
    ){}
}


package com.afkanerd.deku.DefaultSMS.ui.Components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.BuildConfig
import com.afkanerd.deku.DefaultSMS.Models.E2EEHandler
import com.afkanerd.deku.DefaultSMS.Models.SMSHandler.sendDataMessage
import com.afkanerd.deku.DefaultSMS.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Preview(showBackground = true)
@Composable
fun SecureRequestAcceptModal(
    viewModel: ConversationsViewModel = ConversationsViewModel(),
    isSecureRequest: Boolean = true,
    dismissCallback: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val state = rememberStandardBottomSheetState(
        initialValue = if(BuildConfig.DEBUG) SheetValue.Expanded else SheetValue.Hidden,
        skipHiddenState = false
    )

    val url = stringResource(
        R.string.conversations_secure_conversation_request_information_deku_encryption_link)
    val intent = remember{ Intent(Intent.ACTION_VIEW, Uri.parse(url)) }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = { dismissCallback?.let { it() } },
        sheetState = state,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if(isSecureRequest) {
                Text(
                    text = stringResource(R.string.secure_request),
                    style = MaterialTheme.typography.h5,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom=16.dp),
                )

                Text(
                    text = stringResource(
                        R.string
                            .conversation_secure_popup_request_menu_description),
                    fontSize = 16.sp
                )
                Text(
                    text = stringResource(
                        R.string
                            .conversation_secure_popup_request_menu_description_subtext),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(16.dp)
                )

                Button(onClick = {
                    E2EEHandler.clear(context, viewModel.address)
                    val publicKey = E2EEHandler.generateKey(context, viewModel.address)
                    val txPublicKey = E2EEHandler.formatRequestPublicKey(publicKey,
                        E2EEHandler.MagicNumber.REQUEST)
                    // TODO: Add support for dual sim here
                    sendDataMessage(
                        context=context,
                        viewModel=viewModel,
                        data=txPublicKey
                    )
                    scope.launch { state.hide() }.invokeOnCompletion {
                        if(!state.isVisible) {
                            dismissCallback?.let { it() }
                        }
                    }
                }, modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.request))
                }
            }
            else {
                Text(
                    text = stringResource(R.string.conversations_secure_conversation_request),
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
                    val publicKey = E2EEHandler.generateKey(context, viewModel.address)
                    val isSelf = E2EEHandler.isSelf(context, viewModel.address)
                    if(!isSelf) {
                        val txPublicKey = E2EEHandler.formatRequestPublicKey(publicKey,
                            E2EEHandler.MagicNumber.ACCEPT)

                        // TODO: put a pending intent here that makes save on message delivered
                        sendDataMessage(context, txPublicKey, viewModel)
                    } else {
                        E2EEHandler.secureStorePeerPublicKey(
                            context,
                            viewModel.address,
                            publicKey, true)
                    }
                    scope.launch { state.hide() }.invokeOnCompletion {
                        if(!state.isVisible) {
                            dismissCallback?.let { it() }
                        }
                    }
                }) {
                    Text(stringResource(R.string.conversations_secure_conversation_request_agree))
                }

                TextButton(onClick={
                    context.startActivity(intent)
                }) {
                    Text(stringResource(R.string
                        .conversations_secure_conversation_request_information_deku_encryption_read_more))
                }
            }
        }
    }

}


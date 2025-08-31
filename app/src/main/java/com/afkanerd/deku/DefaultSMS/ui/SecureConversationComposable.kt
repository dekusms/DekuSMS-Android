package com.afkanerd.deku.DefaultSMS.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.viewmodel.compose.viewModel
import com.afkanerd.deku.DefaultSMS.ui.viewModels.SecureConversationViewModel
import com.afkanerd.deku.DefaultSMS.ui.components.SecureRequestAcceptModal
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.EncryptionController
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.getEncryptedState

@Composable
fun SecureConversationComposable(
    address: String,
    viewModel: SecureConversationViewModel
) {
    val context = LocalContext.current

    val mode = viewModel.mode
    val showModal = viewModel.showModal

    val state by context.getEncryptedState(address)
        .collectAsState(EncryptionController.EncryptionMode.REQUEST_SENT.name)

    LaunchedEffect(state) {
        if(state == EncryptionController.EncryptionMode.REQUEST_RECEIVED.name) {
            viewModel.mode = EncryptionController.EncryptionMode.REQUEST_RECEIVED
            viewModel.setModal(true)
        }
    }

    SecureRequestAcceptModal(
        address = address,
        show = showModal,
        displayMode = mode,
        dismissCallback = { viewModel.setModal(false) }
    ){
        viewModel.requestSecureConversation(context, address) {

        }
    }
}
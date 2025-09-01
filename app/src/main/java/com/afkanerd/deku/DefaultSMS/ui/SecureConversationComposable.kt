package com.afkanerd.deku.DefaultSMS.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.afkanerd.deku.DefaultSMS.ui.viewModels.SecureConversationViewModel
import com.afkanerd.deku.DefaultSMS.ui.components.SecureRequestAcceptModal
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.EncryptionController
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.getEncryptedState

@Composable
fun SecureConversationComposable(viewModel: SecureConversationViewModel ) {
    val context = LocalContext.current

    val mode = viewModel.mode
    val showModal = viewModel.showModal

    viewModel.address?.let { address ->
        val state = context.getEncryptedState(address)
            .collectAsState(EncryptionController.SecureRequestMode.REQUEST_SENT.name) ?: ""

        LaunchedEffect(state) {
            if(state == EncryptionController.SecureRequestMode.REQUEST_RECEIVED.name) {
                viewModel.mode = EncryptionController.SecureRequestMode.REQUEST_RECEIVED
                viewModel.setModal(true)
            }
        }

        // TODO: if MODE_ACCEPT stop user from sending any further

        SecureRequestAcceptModal(
            address = address,
            show = showModal,
            displayMode = mode,
            dismissCallback = { viewModel.setModal(false) }
        ){
            viewModel.requestSecureConversation(
                context,
                address = address,
                subscriptionId = viewModel.subscriptionId!!,
                threadId = viewModel.threadId!!
            ) { conversation ->

            }
            viewModel.setModal(false)
        }
    }

}
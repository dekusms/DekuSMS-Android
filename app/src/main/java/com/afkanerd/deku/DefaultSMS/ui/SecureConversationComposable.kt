package com.afkanerd.deku.DefaultSMS.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.afkanerd.deku.DefaultSMS.ui.components.SecureRequestAcceptModal
import com.afkanerd.deku.DefaultSMS.ui.viewModels.SecureConversationViewModel
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.EncryptionController
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.SavedEncryptedModes
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.getEncryptionModeStates
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.getEncryptionRatchetStates
import com.google.gson.Gson

@Composable
fun SecureConversationComposable(viewModel: SecureConversationViewModel ) {
    val context = LocalContext.current

    val mode = viewModel.mode
    val showModal = viewModel.anythingTrigger

    viewModel.address?.let { address ->
        val state by context.getEncryptionModeStates(address).collectAsState("")
        val ratchetState by context.getEncryptionRatchetStates(address).collectAsState("")

        LaunchedEffect(state) {
            if(!state.isNullOrEmpty()) {
                val currentState = Gson().fromJson(state,
                    SavedEncryptedModes::class.java)

                if(currentState.mode == EncryptionController.SecureRequestMode.REQUEST_RECEIVED) {
                    viewModel.mode = EncryptionController.SecureRequestMode.REQUEST_RECEIVED
                    viewModel.setModal(true)
                }
                else if(currentState.mode
                    == EncryptionController.SecureRequestMode.REQUEST_ACCEPTED ||
                    !ratchetState.isNullOrEmpty()
                ) {
                    viewModel.mode = EncryptionController.SecureRequestMode.REQUEST_ACCEPTED
                    viewModel.setIsSecured(true)
                }
            }
            else viewModel.setIsSecured(false)
        }

        SecureRequestAcceptModal(
            address = address,
            subscriptionId = viewModel.subscriptionId!!,
            show = showModal,
            displayMode = mode,
            dismissCallback = { viewModel.setModal(false) },
            simCardChanger = {
                viewModel.setConversationSubscriptionId(it.subscriptionId.toLong())
            }
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
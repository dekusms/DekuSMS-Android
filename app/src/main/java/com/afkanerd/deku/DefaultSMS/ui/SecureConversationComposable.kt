package com.afkanerd.deku.DefaultSMS.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.afkanerd.deku.DefaultSMS.ui.components.SecureRequestAcceptModal
import com.afkanerd.deku.DefaultSMS.ui.viewModels.SecureConversationViewModel

@Composable
fun SecureConversationComposable(viewModel: SecureConversationViewModel ) {
    val context = LocalContext.current

    val mode = viewModel.mode

    val showModal by viewModel.triggerRequested.collectAsStateWithLifecycle()
    val address by viewModel.address.collectAsStateWithLifecycle()
    val subscriptionId by viewModel.subscriptionId.collectAsStateWithLifecycle()
    val threadId by viewModel.threadId.collectAsStateWithLifecycle()

//    val state by context.getEncryptionModeStates(address).collectAsState("")
//    val ratchetState by context.getEncryptionRatchetStates(address).collectAsState("")

//    LaunchedEffect(state) {
//        if(!state.isNullOrEmpty()) {
//            val currentState = Gson().fromJson(state,
//                SavedEncryptedModes::class.java)
//
//            if(currentState.mode == EncryptionController.SecureRequestMode.REQUEST_RECEIVED) {
//                viewModel.mode = EncryptionController.SecureRequestMode.REQUEST_RECEIVED
//                viewModel.setModal(true)
//            }
//            else if(currentState.mode
//                == EncryptionController.SecureRequestMode.REQUEST_ACCEPTED ||
//                !ratchetState.isNullOrEmpty()
//            ) {
//                viewModel.mode = EncryptionController.SecureRequestMode.REQUEST_ACCEPTED
//                viewModel.setIsSecured(true)
//            }
//        }
//        else viewModel.setIsSecured(false)
//    }

    address?.let { address ->
        SecureRequestAcceptModal(
            address = address,
            subscriptionId = subscriptionId,
            show = showModal,
            displayMode = mode,
            dismissCallback = { viewModel.setTrigger(false) },
            simCardChanger = { sim ->
                viewModel.setConversationSubscriptionId(sim.subscriptionId.toLong())
            }
        ){
            viewModel.requestSecureConversation(
                context,
                address = address,
                subscriptionId = subscriptionId,
                threadId = threadId
            ) { conversation ->

            }
            viewModel.setTrigger(false)
        }


    }
}
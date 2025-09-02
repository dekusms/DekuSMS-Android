package com.afkanerd.deku.DefaultSMS.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import com.afkanerd.deku.DefaultSMS.BuildConfig
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.DefaultSMS.ui.viewModels.SecureConversationViewModel
import com.afkanerd.deku.DefaultSMS.ui.components.SecureRequestAcceptModal
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.EncryptionController
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.SavedEncryptedModes
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.getEncryptionModeStates
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getSubscriptionName
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.SimChooser
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SecureConversationComposable(viewModel: SecureConversationViewModel ) {
    val context = LocalContext.current

    val mode = viewModel.mode
    val showModal = viewModel.trigger

    viewModel.address?.let { address ->
        val state by context.getEncryptionModeStates(address).collectAsState("")

        LaunchedEffect(state) {
            if(!state.isNullOrEmpty()) {
                val currentState = Gson().fromJson(state,
                    SavedEncryptedModes::class.java)

                if(currentState.mode == EncryptionController.SecureRequestMode.REQUEST_RECEIVED) {
                    viewModel.mode = EncryptionController.SecureRequestMode.REQUEST_RECEIVED
                    viewModel.setModal(true)
                }
                else if(currentState.mode
                    == EncryptionController.SecureRequestMode.REQUEST_ACCEPTED) {
                    viewModel.mode = EncryptionController.SecureRequestMode.REQUEST_ACCEPTED

                    if(BuildConfig.DEBUG)
                        Toast.makeText(context,
                            context.getString(R.string.secure_conversation),
                            Toast.LENGTH_LONG).show()
                }
            }
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
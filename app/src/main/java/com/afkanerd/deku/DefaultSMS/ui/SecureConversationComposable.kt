package com.afkanerd.deku.DefaultSMS.ui

import android.R.attr.mode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.afkanerd.deku.DefaultSMS.data.EncryptionUtils
import com.afkanerd.deku.DefaultSMS.data.models.SecureConversationType
import com.afkanerd.deku.DefaultSMS.ui.components.SecureRequestAcceptModal
import com.afkanerd.deku.DefaultSMS.ui.viewModels.SecureConversationViewModel
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.EncryptionController
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.SavedEncryptedModes
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ConversationsViewModel
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.CustomsConversationsViewModel
import com.google.gson.Gson

@Composable
fun SecureConversationComposable(
    customConversationsViewModel: CustomsConversationsViewModel,
    conversationsViewModel: ConversationsViewModel,
    secureConversationViewModel: SecureConversationViewModel
) {
    val context = LocalContext.current

    val showModal by customConversationsViewModel.triggerRequested.collectAsStateWithLifecycle()
    val address by customConversationsViewModel.address.collectAsStateWithLifecycle()
    val subscriptionId by customConversationsViewModel.subscriptionId.collectAsStateWithLifecycle()
    val threadId by customConversationsViewModel.threadId.collectAsStateWithLifecycle()

    address?.let { address ->
        val secureConversation by secureConversationViewModel
            .get(context, address)
            .collectAsStateWithLifecycle(null)

        var displayMode: SecureConversationType by remember{ mutableStateOf(SecureConversationType.REQUEST) }
        LaunchedEffect(secureConversation) {
            secureConversation?.let {
                displayMode = SecureConversationType
                    .fromInt(it.type) ?: throw Exception("Invalid request type: ${it.type}")
            }
        }

        SecureRequestAcceptModal(
            address = address,
            subscriptionId = subscriptionId,
            show = showModal,
            displayMode = displayMode,
            dismissCallback = { customConversationsViewModel.setTrigger(false) },
            simCardChanger = { sim ->
                customConversationsViewModel.setConversationSubscriptionId(sim.subscriptionId.toLong())
            }
        ){
            customConversationsViewModel.setTrigger(false)
            val request = EncryptionUtils.Handshake.makeRequest(
                context,
                address
            )
            conversationsViewModel.sendSms(
                context = context,
                text = "",
                address = address,
                subscriptionId = subscriptionId,
                threadId = threadId,
                data = request.request,
            ) {

            }
        }
    }
}
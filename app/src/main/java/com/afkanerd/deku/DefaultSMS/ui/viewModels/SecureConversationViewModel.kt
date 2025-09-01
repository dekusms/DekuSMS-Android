package com.afkanerd.deku.DefaultSMS.ui.viewModels

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.EncryptionController
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.sendSms
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.CustomsConversationsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SecureConversationViewModel(): CustomsConversationsViewModel() {
    var mode by mutableStateOf(EncryptionController.SecureRequestMode.REQUEST_NONE)

    fun requestSecureConversation(
        context: Context,
        address: String,
        subscriptionId: Long,
        threadId: Int,
        callback: (Conversations) -> Unit
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val publicKey = EncryptionController.sendRequest(context, address, mode)

                try {
                    context.sendSms(
                        text = "",
                        address = address,
                        threadId = threadId,
                        subscriptionId = subscriptionId,
                        data = publicKey,
                    )?.let { conversation ->
                        callback(conversation)
                    }
                } catch(e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context,
                            context.getString(R.string.something_went_wrong_with_sending),
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
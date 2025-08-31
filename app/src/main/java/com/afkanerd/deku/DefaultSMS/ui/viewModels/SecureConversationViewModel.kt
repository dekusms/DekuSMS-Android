package com.afkanerd.deku.DefaultSMS.ui.viewModels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.EncryptionController
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.CustomsConversationsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SecureConversationViewModel: CustomsConversationsViewModel() {

    fun requestSecureConversation(
        context: Context,
        address: String,
        completeCallback: () -> Unit,
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                EncryptionController.sendRequest(context, address)
                completeCallback()
            }
        }
    }
}
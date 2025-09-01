package com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels

import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class CustomsConversationsViewModel : ViewModel() {
    var trigger by mutableStateOf(false)
        private set

    var address: String? by mutableStateOf(null)
        private set

    var subscriptionId: Long? by mutableStateOf(null)
        private set

    var threadId: Int? by mutableStateOf(null)
        private set

    fun setConversationThreadId(threadId: Int) {
        this.threadId = threadId
    }

    fun setConversationAddress(address: String) {
        this.address = address
    }

    fun setConversationSubscriptionId(subscriptionId: Long?) {
        this.subscriptionId = subscriptionId

    }

    fun setModal(show: Boolean) {
        trigger = show
    }
}
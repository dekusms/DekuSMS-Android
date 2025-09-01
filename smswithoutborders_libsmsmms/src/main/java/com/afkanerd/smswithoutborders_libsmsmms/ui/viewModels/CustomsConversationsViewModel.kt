package com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

open class CustomsConversationsViewModel : ViewModel() {
    var showModal by mutableStateOf(false)
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
        showModal = show
    }
}
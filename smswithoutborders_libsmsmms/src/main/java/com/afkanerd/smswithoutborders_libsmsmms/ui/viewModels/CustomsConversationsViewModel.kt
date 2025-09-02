package com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface CustomConversationServices {
    fun sendSms(
        context: Context,
        text: String,
        address: String,
        subscriptionId: Long,
        threadId: Int,
        data: ByteArray? = null,
        callback: (Conversations?) -> Unit
    )
}

open class CustomsConversationsViewModel : ViewModel(), CustomConversationServices {
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

    override fun sendSms(
        context: Context,
        text: String,
        address: String,
        subscriptionId: Long,
        threadId: Int,
        data: ByteArray?,
        callback: (Conversations?) -> Unit
    ) {
        ConversationsViewModel().sendSms(
            context = context,
            text = text,
            address = address,
            subscriptionId = subscriptionId,
            threadId = threadId,
            data = data,
            callback = callback,
        )
    }
}
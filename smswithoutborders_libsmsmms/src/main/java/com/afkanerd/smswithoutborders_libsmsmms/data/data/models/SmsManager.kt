package com.afkanerd.smswithoutborders_libsmsmms.data.data.models

import android.content.Context
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.CustomConversationServices

class SmsManager(private val smsSender: CustomConversationServices) {
    fun sendSms(
        context: Context,
        text: String,
        address: String,
        subscriptionId: Long,
        threadId: Int,
        data: ByteArray? = null,
        callback: (Conversations?) -> Unit
    ) {
        smsSender.sendSms(
            context = context,
            text = text,
            address = address,
            subscriptionId = subscriptionId,
            threadId = threadId,
            data = data,
            callback = callback
        )
    }
}
package com.afkanerd.deku.DefaultSMS.Models

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import android.util.Base64
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.CustomAppCompactActivity.Companion.encryptMessage
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SMSHandler {

    fun sendDataMessage(
        context: Context,
        data: ByteArray,
        viewModel: ConversationsViewModel
    ) {
        val subscriptionId = SIMHandler.getDefaultSimSubscription(context)
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val messageId = System.currentTimeMillis().toString()
                val conversation = Conversation()
                conversation.thread_id = viewModel.threadId
                conversation.address = viewModel.address
                conversation.isIs_key = true
                conversation.message_id = messageId
                conversation.data = Base64.encodeToString(data, Base64.DEFAULT)
                conversation.subscription_id = subscriptionId
                conversation.type = Telephony.Sms.MESSAGE_TYPE_OUTBOX
                conversation.date = System.currentTimeMillis().toString()
                conversation.status = Telephony.Sms.STATUS_PENDING

                val id = viewModel.insert(context, conversation)
                SMSDatabaseWrapper.send_data(context, conversation)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendTextMessage(
        context: Context,
        text: String,
        address: String,
        conversation: Conversation,
        conversationsViewModel: ConversationsViewModel,
        messageId: String?) {
        var messageId = messageId

        if (messageId == null) messageId = System.currentTimeMillis().toString()

        CoroutineScope(Dispatchers.Default).launch{
            try {
                conversationsViewModel.insert(context, conversation)
            } catch (e: Exception) {
                e.printStackTrace()
                return@launch
            }

            val payload = encryptMessage(context, text, address)
            conversation.text = payload.first
            sendTxt(context, conversation, conversationsViewModel)

            payload.second?.let {
                E2EEHandler.storeState(context, payload.second!!.serializedStates, address)
            }
        }
    }

    private fun sendTxt(
        context: Context,
        conversation: Conversation,
        conversationsViewModel: ConversationsViewModel) {
        try {
            SMSDatabaseWrapper.send_text(context, conversation, null)
        } catch (e: Exception) {
            e.printStackTrace()
            NativeSMSDB.Outgoing.register_failed(context, conversation.message_id,
                1 )
            conversation.status = Telephony.TextBasedSmsColumns.STATUS_FAILED
            conversation.type = Telephony.TextBasedSmsColumns.MESSAGE_TYPE_FAILED
            conversation.error_code = 1
            conversationsViewModel.update(context, conversation)
        }
    }


}
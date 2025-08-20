package com.afkanerd.deku.DefaultSMS.BroadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.afkanerd.deku.MainActivity
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.notifyText
import com.afkanerd.smswithoutborders_libsmsmms.receivers.SmsTextReceivedReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsMmsNotificationReceiver: BroadcastReceiver() {
    private val cls = MainActivity::class.java
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            SmsTextReceivedReceiver.SMS_SENT_BROADCAST_INTENT_LIB -> {
                val id = intent.getLongExtra("id", -1)
                CoroutineScope(Dispatchers.IO).launch {
                    context?.getDatabase()?.conversationsDao()
                        ?.getConversation(id)?.let { conversation ->
                            when(conversation.sms?.status) {
                                Telephony.Sms.STATUS_FAILED -> {
                                    notifyMessageFailedToSend(context, conversation)
                                }
                                else -> {
                                    context.notifyText(conversation, cls)
                                }
                            }
                        }
                }
            }
        }
    }

    private fun notifyMessageFailedToSend(context: Context, conversation: Conversations) {
        val content = context
            .getString(
                R.string
                    .message_failed_send_notification_description_a_message_failed_to_send_to) +
                " ${conversation.sms?.address}"

        context.notifyText(
            conversation = conversation,
            actions = false,
            text = content,
            cls = cls,
        )
    }
}
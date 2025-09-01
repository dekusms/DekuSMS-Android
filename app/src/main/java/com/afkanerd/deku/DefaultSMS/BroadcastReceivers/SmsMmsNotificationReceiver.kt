package com.afkanerd.deku.DefaultSMS.BroadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.widget.Toast
import com.afkanerd.deku.MainActivity
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.EncryptionController
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.removeEncryptionModeStates
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.notify
import com.afkanerd.smswithoutborders_libsmsmms.receivers.SmsTextReceivedReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SmsMmsNotificationReceiver: BroadcastReceiver() {
    private val cls = MainActivity::class.java
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            SmsTextReceivedReceiver.SMS_SENT_BROADCAST_INTENT_LIB -> {
                val id = intent.getLongExtra("id", -1)
                val self = intent.getBooleanExtra("self", false)
                CoroutineScope(Dispatchers.IO).launch {
                    context?.getDatabase()?.conversationsDao()
                        ?.getConversation(id)?.let { conversation ->
                            when(conversation.sms?.status) {
                                Telephony.Sms.STATUS_FAILED -> {
                                    notifyMessageFailedToSend(context, conversation)

                                    if(conversation.sms_data != null) {
                                        context.removeEncryptionModeStates(
                                            conversation.sms?.address!!)
                                    }
                                }
                                else -> {
                                    if(conversation.sms_data != null) {
                                        processEncryptedContent(context, conversation)
                                    }

                                    context.notify(
                                        conversation = conversation,
                                        cls = cls,
                                        self = self,
                                    )
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

        context.notify(
            conversation = conversation,
            actions = false,
            text = content,
            cls = cls,
        )
    }

    private suspend fun processEncryptedContent(
        context: Context,
        conversation: Conversations
    ) {
        val data = conversation.sms_data!!
        try {
            EncryptionController.receiveRequest(
                context,
                conversation.sms?.address!!,
                data
            )
        } catch(e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
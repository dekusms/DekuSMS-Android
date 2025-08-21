package com.afkanerd.smswithoutborders_libsmsmms.receivers

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Base64
import android.util.Log
import android.util.Pair
import android.widget.Toast
import androidx.core.net.toUri
import com.afkanerd.smswithoutborders_libsmsmms.BuildConfig
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.SmsMmsNatives
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getThreadId
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.insertSms
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.notifyText
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.sendNotificationBroadcast
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.updateSms
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ConversationsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class SmsTextReceivedReceiver : BroadcastReceiver() {
    companion object {
        var SMS_SENT_BROADCAST_INTENT = "com.afkanerd.deku.SMS_SENT_BROADCAST_INTENT"
        var SMS_DELIVERED_BROADCAST_INTENT = "com.afkanerd.deku.SMS_DELIVERED_BROADCAST_INTENT"
        var DATA_SENT_BROADCAST_INTENT = "com.afkanerd.deku.DATA_SENT_BROADCAST_INTENT"
        var DATA_DELIVERED_BROADCAST_INTENT = "com.afkanerd.deku.DATA_DELIVERED_BROADCAST_INTENT"

        var SMS_SENT_BROADCAST_INTENT_LIB = "com.afkanerd.deku.SMS_SENT_BROADCAST_INTENT_LIB"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Telephony.Sms.Intents.SMS_DELIVER_ACTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val conversation = registerIncomingText(context, intent)
                        context.getDatabase().threadsDao()?.get(conversation.sms?.thread_id!!)?.let {
                            if(!it.isMute) context.sendNotificationBroadcast(conversation)
                        }
                    }
                }
            }
            SMS_SENT_BROADCAST_INTENT, DATA_SENT_BROADCAST_INTENT -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val id = intent.getLongExtra("id", -1)
                    val uri = intent.getStringExtra("uri")?.toUri()

                    context.getDatabase().conversationsDao()
                        ?.getConversation(id)
                        ?.let { conversation ->
                            if (resultCode == Activity.RESULT_OK) {
                                conversation.sms?.status = Telephony.Sms.STATUS_PENDING
                                conversation.sms?.type = Telephony.Sms.MESSAGE_TYPE_SENT
                            } else {
                                conversation.sms?.status = Telephony.Sms.STATUS_FAILED
                                conversation.sms?.type = Telephony.Sms.MESSAGE_TYPE_FAILED
                                conversation.sms?.error_code = resultCode
                            }
                            try {
                                context.updateSms(uri!!, conversation)
                                if(conversation.sms?.status == Telephony.Sms.STATUS_FAILED)
                                    context.sendNotificationBroadcast(conversation)
                            } catch(e: Exception) {
                                e.printStackTrace()
                            }
                        }
                }
            }
            SMS_DELIVERED_BROADCAST_INTENT, DATA_DELIVERED_BROADCAST_INTENT -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val id = intent.getLongExtra("id", -1)
                    val uri = intent.getStringExtra("uri")?.toUri()

                    context.getDatabase().conversationsDao()
                        ?.getConversation(id)
                        ?.let { conversation ->
                            if (resultCode == Activity.RESULT_OK) {
                                conversation.sms?.status = Telephony.Sms.STATUS_COMPLETE
                            } else {
                                conversation.sms?.status = Telephony.Sms.STATUS_FAILED
                                conversation.sms?.type = Telephony.Sms.MESSAGE_TYPE_FAILED
                                conversation.sms?.error_code = resultCode
                            }
                            try {
                                context.updateSms(uri!!, conversation)
                                if(conversation.sms?.status == Telephony.Sms.STATUS_FAILED)
                                    context.sendNotificationBroadcast(conversation)
                            } catch(e: Exception) {
                                e.printStackTrace()
                            }
                        }
                }
            }
        }

    }

    fun registerIncomingText(context: Context, intent: Intent): Conversations {
        val bundle = intent.extras
        val subscriptionId = bundle!!.getInt("subscription", -1)
        var address: String? = ""
        val bodyBuffer = StringBuilder()
        var dateSent: Long = 0
        val date = System.currentTimeMillis()
        var status = -1

        for (currentSMS in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
            address = currentSMS.displayOriginatingAddress
            bodyBuffer.append(currentSMS.displayMessageBody)
            dateSent = currentSMS.timestampMillis
            status = currentSMS.status
        }
        val body = bodyBuffer.toString()

        // TODO: process encrypted message
        val conversation = Conversations(
            sms = SmsMmsNatives.Sms(
                body = body,
                sub_id = subscriptionId.toLong(),
                date = date,
                date_sent = dateSent,
                address = address!!,
                type = Telephony.Sms.MESSAGE_TYPE_INBOX,
                status = status,
                thread_id = context.getThreadId(address),
                read = 0,
            )
        )
        context.insertSms(conversation)
        return conversation
    }

}
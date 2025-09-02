package com.afkanerd.smswithoutborders_libsmsmms.receivers

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.core.net.toUri
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.SmsMmsNatives
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.NotificationTxType
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getThreadId
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.insertSms
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.registerIncomingSms
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.sendNotificationBroadcast
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.updateSms
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                        val conversation = context.registerIncomingSms(intent)
                        context.getDatabase().threadsDao()?.get(conversation.sms?.thread_id!!)?.let {
                            if(!it.isMute) context.sendNotificationBroadcast(
                                conversation, type = NotificationTxType.TEXT)
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
                                conversation.sms?.status = Telephony.Sms.STATUS_NONE
                                conversation.sms?.type = Telephony.Sms.MESSAGE_TYPE_SENT
                            } else {
                                conversation.sms?.status = Telephony.Sms.STATUS_FAILED
                                conversation.sms?.type = Telephony.Sms.MESSAGE_TYPE_FAILED
                                conversation.sms?.error_code = resultCode
                            }
                            try {
                                context.updateSms(uri!!, conversation)
                                if(conversation.sms?.status == Telephony.Sms.STATUS_FAILED)
                                    context.sendNotificationBroadcast(
                                        conversation,
                                        if(intent.action == SMS_SENT_BROADCAST_INTENT)
                                            NotificationTxType.TEXT else NotificationTxType.DATA
                                    )
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
                                    context.sendNotificationBroadcast(
                                        conversation,
                                        if(intent.action == SMS_DELIVERED_BROADCAST_INTENT)
                                            NotificationTxType.TEXT else NotificationTxType.DATA
                                    )
                            } catch(e: Exception) {
                                e.printStackTrace()
                            }
                        }
                }
            }
        }

    }

}
package com.afkanerd.smswithoutborders_libsmsmms.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.RemoteInput
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.SmsManager
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.NotificationTxType
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.cancelNotification
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getCustomizationProperties
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDefaultSimSubscription
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.sendNotificationBroadcast
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.sendSms
import com.afkanerd.smswithoutborders_libsmsmms.receivers.SmsTextReceivedReceiver.Companion.SMS_SENT_BROADCAST_INTENT_LIB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class SmsMmsActionsImpl : BroadcastReceiver() {
    companion object {
        const val NOTIFICATION_REPLY_ACTION_KEY = "NOTIFICATION_REPLY_ACTION_KEY"

        const val NOTIFICATION_MARK_AS_READ_ACTION_INTENT_ACTION =
            "NOTIFICATION_MARK_AS_READ_ACTION_INTENT_ACTION"

        const val NOTIFICATION_REPLY_ACTION_INTENT_ACTION =
            "NOTIFICATION_REPLY_ACTION_INTENT_ACTION"

        const val NOTIFICATION_REPLY_ACTION_INTENT_ACTION_REPLAY =
            "com.afkanerd.deku.NOTIFICATION_REPLY_ACTION_INTENT_ACTION_REPLAY"

        const val NOTIFICATION_MUTE_ACTION_INTENT_ACTION = "NOTIFICATION_MUTE_ACTION_INTENT_ACTION"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action == null) return
        when(intent.action) {
            NOTIFICATION_REPLY_ACTION_INTENT_ACTION -> {
                val remoteInput = RemoteInput.getResultsFromIntent(intent)
                if (remoteInput != null) {
                    val address = intent.getStringExtra("address")
                    val threadId = intent.getIntExtra("thread_id", -1)

                    val subId = context.getDefaultSimSubscription()!!
                    val subscriptionId = intent.getLongExtra("sub_id",
                        subId.toLong())

                    val reply = remoteInput.getCharSequence(NOTIFICATION_REPLY_ACTION_KEY)
                    if (reply == null || reply.toString().isEmpty()) return

                    val properties = context.getCustomizationProperties()
                    val broadcast = properties
                        .getProperty("broadcast_notifications_reply_actions").toBoolean()

                    if(broadcast) {
                        context.sendBroadcast(
                            Intent(NOTIFICATION_REPLY_ACTION_INTENT_ACTION_REPLAY).apply{
                                putExtra("address", address)
                                putExtra("threadId", threadId)
                                putExtra("subscriptionId", subscriptionId)
                                putExtra("reply", reply)
                                setPackage(context.packageName)
                            }
                        )
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                context.sendSms(
                                    text = reply.toString(),
                                    address = address!!,
                                    threadId = threadId,
                                    subscriptionId = subscriptionId
                                )?.let { conversation ->
                                    context.sendNotificationBroadcast(
                                        conversation, self=true, type = NotificationTxType.TEXT)
                                }
                            } catch(e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
            NOTIFICATION_MARK_AS_READ_ACTION_INTENT_ACTION -> {
                val id = intent.getLongExtra("id", -1)
                val threadId = intent.getIntExtra("thread_id", -1)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        context.getDatabase().conversationsDao()?.getConversation(id)
                            ?.let {
                                it.sms?.read = 1
                                context.getDatabase().conversationsDao()?.update(it)
                            }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                context.cancelNotification(threadId)
            }
            NOTIFICATION_MUTE_ACTION_INTENT_ACTION -> {
                val threadId = intent.getIntExtra("thread_id", -1)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        context.getDatabase().threadsDao()?.setMute(true, threadId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                context.cancelNotification(threadId)
            }
        }
    }
}
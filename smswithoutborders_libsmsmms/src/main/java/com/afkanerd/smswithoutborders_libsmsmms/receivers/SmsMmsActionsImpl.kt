package com.afkanerd.smswithoutborders_libsmsmms.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.NotificationMarkAsReadActionIntentAction
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.NotificationMuteActionIntentAction
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.NotificationReplyActionIntentAction
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.NotificationReplyActionKey
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDefaultSimSubscription
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.notifyText
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.sendSms
import java.lang.Exception

class SmsMmsActionsImpl : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != null && intent.action == context.NotificationReplyActionIntentAction) {
            val remoteInput = RemoteInput.getResultsFromIntent(intent)
            if (remoteInput != null) {
                val address = intent.getStringExtra("address")
                val threadId = intent.getStringExtra("thread_id")

                val subId = context.getDefaultSimSubscription()!!
                val subscriptionId = intent.getIntExtra("sub_id", subId)

                val reply = remoteInput.getCharSequence(context.NotificationReplyActionKey)
                if (reply == null || reply.toString().isEmpty()) return

                try {
                    context.sendSms(
                        text = reply.toString(),
                        address = address!!,
                        threadId = threadId!!,
                        subscriptionId = subscriptionId
                    ).let { conversation ->
                        context.notifyText(conversation, true)
                    }
                } catch(e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        else if (intent.action != null && intent.action == context.NotificationMarkAsReadActionIntentAction) {
            val threadId = intent.getStringExtra("thread_id")
            try {
                TODO("Implement this methods")
//                CoroutineScope(Dispatchers.Default).launch {
//                    NativeSMSDB.Incoming.update_read(context, 1, threadId, null)
//                    databaseConnector!!.conversationDao().updateRead(true, threadId!!)
//                    val notificationManager = NotificationManagerCompat.from(context)
//                    notificationManager.cancel(threadId.toInt())
//                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        else if (intent.action != null && intent.action == context.NotificationMuteActionIntentAction) {
            val threadId = intent.getStringExtra("thread_id")


            TODO("Implement this methods")
//            CoroutineScope(Dispatchers.Default).launch {
//                ConversationsViewModel().mute(context, threadId!!)
//                val notificationManager = NotificationManagerCompat.from(context)
//                notificationManager.cancel(threadId.toInt())
//            }
        }
    }
}
package com.afkanerd.deku.DefaultSMS.BroadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.BuildConfig
import com.afkanerd.deku.DefaultSMS.MainActivity
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.NativeSMSDB
import com.afkanerd.deku.DefaultSMS.Models.Notifications
import com.afkanerd.deku.DefaultSMS.Models.NotificationsHandler
import com.afkanerd.deku.DefaultSMS.Models.SIMHandler
import com.afkanerd.deku.DefaultSMS.Models.SMSDatabaseWrapper
import com.afkanerd.deku.Modules.ThreadingPoolExecutor
import java.lang.Exception


class IncomingTextSMSReplyActionBroadcastReceiver : BroadcastReceiver() {
    var databaseConnector: Datastore? = null

    override fun onReceive(context: Context, intent: Intent) {
        databaseConnector = Datastore.getDatastore(context)

        if (intent.getAction() != null && intent.getAction() == REPLY_BROADCAST_INTENT) {
            val remoteInput = RemoteInput.getResultsFromIntent(intent)
            if (remoteInput != null) {
                val address = intent.getStringExtra(REPLY_ADDRESS)
                val threadId = intent.getStringExtra(REPLY_THREAD_ID)

                val def_subscriptionId = SIMHandler.getDefaultSimSubscription(context)
                val subscriptionId = intent.getIntExtra(REPLY_SUBSCRIPTION_ID, def_subscriptionId)

                val reply = remoteInput.getCharSequence(KEY_TEXT_REPLY)
                if (reply == null || reply.toString().isEmpty()) return

                val conversation = Conversation()
                val messageId = System.currentTimeMillis().toString()
                conversation.address = address
                conversation.subscription_id = subscriptionId
                conversation.thread_id = threadId
                conversation.text = reply.toString()
                conversation.message_id = messageId
                conversation.date = System.currentTimeMillis().toString()
                conversation.type = Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX
                conversation.status = Telephony.TextBasedSmsColumns.STATUS_PENDING

                ThreadingPoolExecutor.executorService.execute(object : Runnable {
                    override fun run() {
                        try {
                            databaseConnector!!.threadedConversationsDao()
                                .insertThreadAndConversation(context, conversation)

                            SMSDatabaseWrapper.send_text(context, conversation, null)
                            val messagingStyle =
                                NotificationsHandler.getMessagingStyle(
                                    context, conversation,
                                    reply.toString()
                                )

                            val replyIntent = NotificationsHandler
                                .getReplyIntent(context, conversation)

                            val pendingIntent = NotificationsHandler
                                .getPendingIntent(context, conversation)

//                            val builder =
//                                NotificationsHandler.getNotificationBuilder(
//                                    context, replyIntent,
//                                    conversation, pendingIntent
//                                )

                            val builder = Notifications.createNotification(
                                context = context,
                                title = conversation.address!!,
                                text = conversation.text!!,
                                requestCode = 0,
                                contentIntent = Intent(
                                    context,
                                    MainActivity::class.java
                                ).apply {
                                    putExtra("address", conversation.address)
                                    putExtra("thread_id", conversation.thread_id)
                                },
                            )

                            builder.setStyle(messagingStyle)
                            val notificationManagerCompat =
                                NotificationManagerCompat.from(context)

//                            notificationManagerCompat.notify(Integer.parseInt(threadId), builder.build());
//                            Notifications.INSTANCE.createNotification(
//                                    context,
//                                    conversation.getAddress(),
//                                    conversation.getText()
//                            );

                            Notifications.notify(
                                context = context,
                                builder = builder,
                                notificationId = conversation.thread_id!!.toInt()
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
            }
        } else if (intent.getAction() != null && intent.getAction() == MARK_AS_READ_BROADCAST_INTENT) {
            val threadId = intent.getStringExtra(Conversation.THREAD_ID)
            val messageId = intent.getStringExtra(Conversation.ID)
            try {
                ThreadingPoolExecutor.executorService.execute(object : Runnable {
                    override fun run() {
                        NativeSMSDB.Incoming.update_read(context, 1, threadId, null)
                        databaseConnector!!.threadedConversationsDao().updateRead(
                            1,
                            threadId!!.toLong()
                        )
                    }
                })

                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.cancel(threadId!!.toInt())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (intent.getAction() != null && intent.getAction() == MUTE_BROADCAST_INTENT) {
            val threadId = intent.getStringExtra(Conversation.THREAD_ID)

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(threadId!!.toInt())

            databaseConnector!!.threadedConversationsDao().updateMuted(1, threadId)
        }
    }

    companion object {
        var REPLY_BROADCAST_INTENT: String = BuildConfig.APPLICATION_ID + ".REPLY_BROADCAST_ACTION"
        var MARK_AS_READ_BROADCAST_INTENT: String =
            BuildConfig.APPLICATION_ID + ".MARK_AS_READ_BROADCAST_ACTION"
        var MUTE_BROADCAST_INTENT: String = BuildConfig.APPLICATION_ID + ".MUTE_BROADCAST_ACTION"

        var REPLY_ADDRESS: String = "REPLY_ADDRESS"
        var REPLY_THREAD_ID: String = "REPLY_THREAD_ID"
        var REPLY_SUBSCRIPTION_ID: String = "REPLY_SUBSCRIPTION_ID"

        // Key for the string that's delivered in the action's intent.
        const val KEY_TEXT_REPLY: String = "KEY_TEXT_REPLY"
    }
}

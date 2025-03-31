package com.afkanerd.deku.DefaultSMS.BroadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.BuildConfig
import com.afkanerd.deku.MainActivity
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.NativeSMSDB
import com.afkanerd.deku.DefaultSMS.Models.Notifications
import com.afkanerd.deku.DefaultSMS.Models.SIMHandler
import com.afkanerd.deku.DefaultSMS.Models.SMSDatabaseWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import com.afkanerd.deku.DefaultSMS.R


class IncomingTextSMSReplyMuteActionBroadcastReceiver : BroadcastReceiver() {
    var databaseConnector: Datastore? = null

    override fun onReceive(context: Context, intent: Intent) {
        databaseConnector = Datastore.getDatastore(context)

        if (intent.action != null && intent.action == REPLY_BROADCAST_INTENT) {
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

                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        databaseConnector!!.conversationDao()._insert(conversation)

                        SMSDatabaseWrapper.send_text(context, conversation, null)
                        val messagingStyle: NotificationCompat.MessagingStyle? =
                            Notifications.getPreviousNotifications(context)

                        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                            messagingStyle?.addMessage(
                                conversation.text,
                                System.currentTimeMillis(),
                                ""
                            )
                        } else {
                            val person = Person.Builder()
                                .setName(context.getString(R.string.notification_title_reply_you))
                                .build()
                            messagingStyle?.addMessage(
                                conversation.text!!,
                                System.currentTimeMillis(),
                                person
                            )
                        }

                        val builder = Notifications.createNotification(
                            context = context,
                            title = conversation.address!!,
                            text = conversation.text!!,
                            requestCode = conversation.thread_id!!.toInt(),
                            address = conversation.address!!,
                            contentIntent = Intent(
                                context,
                                MainActivity::class.java
                            ).apply {
                                putExtra("address", conversation.address)
                                putExtra("thread_id", conversation.thread_id)
                            },
                        ).apply {
                            setStyle(messagingStyle)
                            setSilent(true)
                        }

                        Notifications.notify(
                            context = context,
                            builder = builder,
                            notificationId = conversation.thread_id!!.toInt()
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        else if (intent.action != null && intent.action == MARK_AS_READ_BROADCAST_INTENT) {
            val threadId = intent.getStringExtra(REPLY_THREAD_ID)
            val messageId = intent.getStringExtra(Conversation.ID)
            try {
                CoroutineScope(Dispatchers.Default).launch {
                    NativeSMSDB.Incoming.update_read(context, 1, threadId, null)
                    databaseConnector!!.conversationDao().updateRead(true, threadId!!)
                    val notificationManager = NotificationManagerCompat.from(context)
                    notificationManager.cancel(threadId.toInt())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        else if (intent.action != null && intent.action == MUTE_BROADCAST_INTENT) {
            val threadId = intent.getStringExtra(REPLY_THREAD_ID)

            CoroutineScope(Dispatchers.Default).launch {
                ConversationsViewModel().mute(context, threadId!!)
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.cancel(threadId.toInt())
            }
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
        const val KEY_TEXT_REPLY: String = "extra_remote_reply"
    }
}

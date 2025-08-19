package com.afkanerd.smswithoutborders_libsmsmms.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.cancelNotification
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDefaultSimSubscription
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.notifyText
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.sendSms
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

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            context.sendSms(
                                text = reply.toString(),
                                address = address!!,
                                threadId = threadId,
                                subscriptionId = subscriptionId
                            )?.let { conversation ->
                                context.notifyText(conversation, true)
                            }
                        } catch(e: Exception) {
                            e.printStackTrace()
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
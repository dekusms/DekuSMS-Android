package com.afkanerd.smswithoutborders_libsmsmms.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.DisposableEffect
import androidx.core.net.toUri
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.MmsParser
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.NotificationTxType
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.insertSms
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.sendNotificationBroadcast
import com.klinker.android.send_message.MmsReceivedReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MmsReceivedReceiverImpl: MmsReceivedReceiver() {
    override fun onMessageReceived(context: Context?, contentUri: Uri?) {
        context?.let { context ->
            processIncomingMms(context, contentUri)
        }
    }

    fun processIncomingMms(
        context: Context,
        contentUri: Uri?,
    ) {
        contentUri?.let {
            context.contentResolver?.query(
                contentUri,
                null,
                null,
                null,
                null,
            )?.let { cursor ->
                if(cursor.moveToFirst()) {
                    MmsParser.parse(context, cursor)?.let{ conversation ->
                        CoroutineScope(Dispatchers.IO).launch {
                            context.insertSms(conversation)
                            context.getDatabase().threadsDao()?.get(conversation.sms?.thread_id!!)
                                ?.let {
                                    if(!it.isMute)
                                        context.sendNotificationBroadcast(
                                            conversation,
                                            type = NotificationTxType.MMS
                                        )
                                }
                        }
                    }
                }
            }
        }
    }

    override fun onError(p0: Context?, p1: String?) {
        TODO("Not yet implemented")
    }
}
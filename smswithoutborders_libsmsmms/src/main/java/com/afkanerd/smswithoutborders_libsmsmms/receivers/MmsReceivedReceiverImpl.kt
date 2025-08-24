package com.afkanerd.smswithoutborders_libsmsmms.receivers

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.MmsParser
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.insertSms
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.sendNotificationBroadcast
import com.klinker.android.send_message.MmsReceivedReceiver

class MmsReceivedReceiverImpl: MmsReceivedReceiver() {
    override fun onMessageReceived(context: Context?, contentUri: Uri?) {
        contentUri?.let {
            context?.contentResolver?.query(
                contentUri,
                null,
                null,
                null,
                null,
            )?.let { cursor ->
                if(cursor.moveToFirst()) {
                    MmsParser.parse(context, cursor)
                        .getConversation(context, cursor)?.let { conversation ->
                            context.insertSms(conversation)

                            context.getDatabase().threadsDao()?.get(conversation.sms?.thread_id!!)
                                ?.let {
                                    if(!it.isMute)
                                        context.sendNotificationBroadcast(conversation)
                                }
                        }
                }
            }
        }
    }

    override fun onError(p0: Context?, p1: String?) {
        TODO("Not yet implemented")
    }

    private fun logNestedBundle(bundle: Bundle, logTag: String, indent: String) {
        if (bundle.isEmpty) {
            Log.d(logTag, "$indent<Empty Bundle>")
            return
        }
        for (key: String in bundle.keySet()) {
            val value = bundle.get(key)
            Log.d(logTag, "IndentKey: $key, Value: ${value?.toString()}, Type: ${value?.javaClass?.simpleName ?: "null"}")
            if (value is Bundle) {
                Log.d(logTag, "$indent  ∟ Value (Bundle):")
                logNestedBundle(value, logTag, "$indent    ") // Recursive call
            }
            // You can add similar checks for arrays within nested bundles if needed
        }
    }
}
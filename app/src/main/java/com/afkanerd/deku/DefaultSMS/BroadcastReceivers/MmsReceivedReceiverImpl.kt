package com.afkanerd.deku.DefaultSMS.BroadcastReceivers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import com.afkanerd.deku.DefaultSMS.Models.NativeSMSDB
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
                    TODO("Store the MMS into database")
//                    val mmsConversation = Conversations.build(cursor, true)
//                    val parsedMms = NativeSMSDB.ParseMMS(context, cursor)
//                    parsedMms.buildConversation(context, mmsConversation)
//
////                    if(mmsConversation.mmsImage != null || !mmsConversation.text.isNullOrEmpty())
//                    if(!mmsConversation.mmsContentUri.isNullOrEmpty() || !mmsConversation.text.isNullOrEmpty())
//                        Datastore.getDatastore(context).conversationDao()._insert(mmsConversation)
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
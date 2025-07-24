package com.afkanerd.deku.DefaultSMS.BroadcastReceivers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
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
                    val mmsConversation = Conversation.build(cursor, true)
                    val parsedMms = NativeSMSDB.ParseMMS(context, cursor)
                    parsedMms.buildConversation(context, mmsConversation)

//                    if(mmsConversation.mmsImage != null || !mmsConversation.text.isNullOrEmpty())
                    if(!mmsConversation.mmsContentUri.isNullOrEmpty() || !mmsConversation.text.isNullOrEmpty())
                        Datastore.getDatastore(context).conversationDao()._insert(mmsConversation)
                }
            }
        }
    }

    override fun onError(p0: Context?, p1: String?) {
        TODO("Not yet implemented")
    }


    fun printIntentExtras(intent: Intent?, logTag: String = "IntentExtras") {
        if (intent == null) {
            Log.d(logTag, "Intent is null, no extras to print.")
            return
        }

        val extras: Bundle? = intent.extras
        if (extras == null || extras.isEmpty) {
            Log.d(logTag, "Intent has no extras.")
            return
        }

        Log.d(logTag, "--- Intent Extras ---")
        for (key: String in extras.keySet()) {
            val value = extras.get(key)
            Log.d(logTag, "Key: $key, Value: ${value?.toString()}, Type: ${value?.javaClass?.simpleName ?: "null"}")

            // For more detailed logging of specific types, you can add checks here
            // Example: Logging array contents
            if (value != null && value.javaClass.isArray) {
                when (value) {
                    is ByteArray -> Log.d(logTag, "  ∟ Value (ByteArray): ${value.contentToString()}")
                    is CharArray -> Log.d(logTag, "  ∟ Value (CharArray): ${value.contentToString()}")
                    is ShortArray -> Log.d(logTag, "  ∟ Value (ShortArray): ${value.contentToString()}")
                    is IntArray -> Log.d(logTag, "  ∟ Value (IntArray): ${value.contentToString()}")
                    is LongArray -> Log.d(logTag, "  ∟ Value (LongArray): ${value.contentToString()}")
                    is FloatArray -> Log.d(logTag, "  ∟ Value (FloatArray): ${value.contentToString()}")
                    is DoubleArray -> Log.d(logTag, "  ∟ Value (DoubleArray): ${value.contentToString()}")
                    is BooleanArray -> Log.d(logTag, "  ∟ Value (BooleanArray): ${value.contentToString()}")
                    is Array<*> -> Log.d(logTag, "  ∟ Value (Array): ${value.contentToString()}")
                    // Add other array types if needed
                }
            }
            // Example: Logging Bundle contents (nested extras)
            else if (value is Bundle) {
                Log.d(logTag, "  ∟ Value (Bundle):")
                logNestedBundle(value, logTag, "    ") // Recursive call for nested bundles
            }
        }
        Log.d(logTag, "--- End of Intent Extras ---")
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
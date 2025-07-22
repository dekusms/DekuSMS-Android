package com.afkanerd.deku.DefaultSMS.BroadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.text.TextUtils
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.Models.MmsHandler
import com.android.mms.transaction.DownloadManager
import com.android.mms.transaction.PushReceiver
import com.google.android.mms.MmsException
import com.google.android.mms.pdu_alt.PduParser
import com.google.android.mms.pdu_alt.PduPersister
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MmsReceiverBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val id = intent!!.getLongExtra("messageId", -1)

        val data = intent.getByteArrayExtra("data")
        val pdu = PduParser(data).parse()

        val pduPersister = PduPersister.getPduPersister(context)

        val subId = intent.getIntExtra("subscription", -1)
        val uri = pduPersister.persist(
            pdu,
            Telephony.Mms.Inbox.CONTENT_URI,
            true,
            false,
            null,
            subId
        )

        var location: String? = "";
        try {
            location = MmsHandler.getContentLocation(context!!, uri)
        } catch(e: Exception ) {
            location = pduPersister.getContentLocationFromPduHeader(pdu)
            e.printStackTrace()
        }

        println(location)

        var transactionId: String?
        try {
            transactionId = PushReceiver.getTransactionId(context, uri)
        } catch (ex: MmsException) {
            transactionId = pduPersister.getTransactionIdFromPduHeader(pdu)
            if (TextUtils.isEmpty(transactionId)) {
                throw ex
            }
        }

        DownloadManager.getInstance().downloadMultimediaMessage(
            context,
            location,
            transactionId,
            uri,
            true,
            subId
        )


//        val parsedMms = NativeSMSDB.ParseMMS(context, mmsInboxCursor)
//        val conversation = Conversation.Companion.build(mmsInboxCursor, true)
//        conversation.address = parsedMms.address
//        conversation.mmsImage = parsedMms.image
//        conversation.text = parsedMms.text
//        conversation.type = Telephony.Mms.MESSAGE_BOX_INBOX
//        conversation.subscription_id = intent
//            .getIntExtra("subscription", -1)
//
//        val threadId = Telephony.Threads
//            .getOrCreateThreadId(context, conversation.address)
//        conversation.thread_id = threadId.toString()
//        conversation.date = (conversation.date!!.toLong() * 1000).toString()
//        conversation.date_sent = (conversation.date!!.toLong() * 1000).toString()
//
//        CoroutineScope(Dispatchers.Default).launch {
//            val conversationsViewModel = ConversationsViewModel()
//            conversationsViewModel.insert(context, conversation)
//
//            // TODO: build notifications
//        }


        // Get text and picture from MMS message. Adapted from: https://stackoverflow.com/questions/3012287/how-to-read-mms-data-in-android
//        var message = ""
//        var bitmap  = null // picture
//        val selectionPart = "mid=$id"
//        val mmsTextUri = "content://mms/part".toUri()
//        val cursor = context.contentResolver.query(
//            mmsTextUri, null,
//            selectionPart, null, null
//        )
//        if (cursor!!.moveToFirst()) {
//            do {
//                val partId = cursor
//                    .getString(cursor.getColumnIndex("_id"))
//
//                val type = cursor
//                    .getString(cursor.getColumnIndex("ct"))
//
//                // Get text.
//                if ("text/plain" == type) {
//                    val data  = cursor
//                        .getString(cursor.getColumnIndex("_data"))
//
//                    if (data != null) {
//                        message = "TODO: Change this text"
//                    } else {
//                        message = "TODO: Change this text"
//                    }
//                }
//                //Get picture.
//                if ("image/jpeg" == type || "image/bmp" == type ||
//                    "image/gif" == type || "image/jpg" == type ||
//                    "image/png" == type
//                ) {
//                    bitmap = TODO("Get the Bitmap image from the incoming Pdu")
//                }
//            } while (cursor.moveToNext())
    }
}



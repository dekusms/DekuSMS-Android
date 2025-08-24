package com.afkanerd.smswithoutborders_libsmsmms.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.text.TextUtils
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.MmsParser
import com.android.mms.transaction.DownloadManager
import com.android.mms.transaction.PushReceiver
import com.google.android.mms.MmsException
import com.google.android.mms.pdu_alt.PduParser
import com.google.android.mms.pdu_alt.PduPersister

class MmsReceiverBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val id = intent!!.getLongExtra("messageId", -1)

        val data = intent.getByteArrayExtra("data")
        val pdu = PduParser(data).parse()

        val pduPersister = PduPersister.getPduPersister(context)

        val subId = intent.getIntExtra("subscription", -1)

//        val createThread = Telephony.Threads
//            .getOrCreateThreadId(context, pdu.from.string)

        val uri = pduPersister.persist(
            pdu,
            Telephony.Mms.Inbox.CONTENT_URI,
            false,
            false,
            null,
            subId
        )

        var location: String? = "";
        try {
            location = MmsParser.getMmsContentLocation(context!!,uri)
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
    }
}
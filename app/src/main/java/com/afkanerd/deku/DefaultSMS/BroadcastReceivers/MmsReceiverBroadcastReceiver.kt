package com.afkanerd.deku.DefaultSMS.BroadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsManager
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

        val createThread = Telephony.Threads
            .getOrCreateThreadId(context, pdu.from.string)
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
            location = mmsParser.getMmsContentLocation(uri)
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



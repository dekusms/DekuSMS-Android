package com.afkanerd.smswithoutborders_libsmsmms.receivers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.widget.Toast
import androidx.core.net.toUri
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.NotificationTxType
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.sendNotificationBroadcast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MmsSentReceiverImpl: BroadcastReceiver() {
    @SuppressLint("Range")
    override fun onReceive(context: Context, intent: Intent) {
        val uri = intent.getStringExtra(EXTRA_CONTENT_URI)
        val filepath = intent.getStringExtra(EXTRA_FILE_PATH)
        val id = intent.getLongExtra(EXTRA_ORIGINAL_RESENT_MESSAGE_ID, -1)

        val messageBox = if (resultCode == Activity.RESULT_OK) {
            Telephony.Mms.MESSAGE_BOX_SENT
        } else {
            val msg = context.getString(R.string.unknown_error_sending_mms)
            Toast.makeText(context, msg + resultCode, Toast.LENGTH_LONG).show()
            Telephony.Mms.MESSAGE_BOX_FAILED
        }

        CoroutineScope(Dispatchers.Default).launch {
            context.getDatabase().conversationsDao()
                ?.getConversation(id)
                ?.let { conversation ->
                    conversation.sms?.status = messageBox
                    conversation.sms?.type = messageBox
                    conversation.mms_filepath = filepath
                    context.getDatabase().conversationsDao()?.update(conversation)

                    if(conversation.sms?.status == Telephony.Sms.STATUS_FAILED)
                        context.sendNotificationBroadcast(
                            conversation, type = NotificationTxType.MMS)
                }
        }
    }

    companion object {
        private const val EXTRA_CONTENT_URI = "content_uri"
        private const val EXTRA_FILE_PATH = "file_path"
        const val EXTRA_ORIGINAL_RESENT_MESSAGE_ID = "original_message_id"
    }
}
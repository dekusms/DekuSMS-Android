package com.afkanerd.smswithoutborders_libsmsmms.receivers

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.provider.Telephony
import android.widget.Toast
import com.klinker.android.send_message.MmsSentReceiver.EXTRA_CONTENT_URI
import java.io.File
import androidx.core.net.toUri
import com.afkanerd.smswithoutborders_libsmsmms.Extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.smsMmsNatives
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MmsSentReceiverImpl: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val uri = intent.getStringExtra(EXTRA_CONTENT_URI)
        val filepath = intent.getStringExtra(EXTRA_FILE_PATH)
        val originalResentMessageId = intent.getStringExtra(EXTRA_ORIGINAL_RESENT_MESSAGE_ID)

        val messageBox = if (resultCode == Activity.RESULT_OK) {
            Telephony.Mms.MESSAGE_BOX_SENT
        } else {
            val msg = context.getString(R.string.unknown_error_sending_mms)
            Toast.makeText(context, msg + resultCode, Toast.LENGTH_LONG).show()
            Telephony.Mms.MESSAGE_BOX_FAILED
        }

        originalResentMessageId?.let {
            CoroutineScope(Dispatchers.Default).launch {
                context.getDatabase().conversationDao()
                    ?.getMessage(originalResentMessageId)?.let { conversation ->
                        conversation.sms?.status = messageBox
                        conversation.sms?.type = Telephony.Mms.MESSAGE_BOX_SENT
                        conversation.mms_content_uri = uri
                        conversation.mms_filepath = filepath
                        context.getDatabase().conversationDao()?.update(conversation)
                    }
            }
        }
    }

    companion object {
        private const val EXTRA_CONTENT_URI = "content_uri"
        private const val EXTRA_FILE_PATH = "file_path"
        const val EXTRA_ORIGINAL_RESENT_MESSAGE_ID = "original_message_id"
    }
}
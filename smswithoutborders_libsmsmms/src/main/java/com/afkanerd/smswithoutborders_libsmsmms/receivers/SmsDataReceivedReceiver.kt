package com.afkanerd.smswithoutborders_libsmsmms.receivers

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Base64
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.NotificationTxType
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.registerIncomingSms
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.sendNotificationBroadcast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.text.toInt

//import org.bouncycastle.operator.OperatorCreationException;
class SmsDataReceivedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if(context == null || intent == null) return

        if(intent.action == Telephony.Sms.Intents.DATA_SMS_RECEIVED_ACTION) {
            if (resultCode == Activity.RESULT_OK) {
                CoroutineScope(Dispatchers.IO).launch {
                    val conversation = context.registerIncomingSms(intent)
                    context.getDatabase().threadsDao()?.get(conversation.sms?.thread_id!!)?.let {
                        if(!it.isMute) context.sendNotificationBroadcast(
                            conversation, type = NotificationTxType.DATA)
                    }
                }
            }
        }
    }
}
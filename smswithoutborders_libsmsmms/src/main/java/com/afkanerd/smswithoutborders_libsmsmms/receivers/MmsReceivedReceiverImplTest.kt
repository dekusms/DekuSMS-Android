package com.afkanerd.smswithoutborders_libsmsmms.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.MmsParser
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.insertSms
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.sendNotificationBroadcast
import kotlin.let

class MmsReceivedReceiverImplTest: BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        val uri = intent!!.getStringExtra("uri")
        MmsReceivedReceiverImpl().processIncomingMms(
            context!!,
            uri!!.toUri(),
        )
    }
}
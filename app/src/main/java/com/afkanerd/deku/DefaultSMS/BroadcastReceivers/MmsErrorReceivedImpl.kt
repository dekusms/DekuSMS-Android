package com.afkanerd.deku.DefaultSMS.BroadcastReceivers

import android.content.Context
import android.net.Uri
import com.klinker.android.send_message.MmsReceivedReceiver

class MmsErrorReceivedImpl: MmsReceivedReceiver() {
    override fun onMessageReceived(
        context: Context?,
        messageUri: Uri?
    ) {
        println("Received Error MMS with uri: ${messageUri.toString()}")
    }

    override fun onError(context: Context?, error: String?) {
        println("Received Error MMS with error: $error")
    }
}
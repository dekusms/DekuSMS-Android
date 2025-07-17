package com.afkanerd.deku.DefaultSMS.BroadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MmsReceivedReceiverImpl: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        println(resultCode)
    }
}
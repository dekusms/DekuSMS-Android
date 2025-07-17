package com.afkanerd.deku.DefaultSMS.BroadcastReceivers

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.afkanerd.deku.DefaultSMS.BuildConfig

/**
 * Needed to make the app the default SMS app
 */
class MMSReceiverBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if(resultCode == Activity.RESULT_OK) {

        } else {
            println("MMS broadcast received code: $resultCode")
        }
    }
}

package com.afkanerd.deku.DefaultSMS.BroadcastReceivers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import androidx.compose.ui.geometry.isEmpty
import com.klinker.android.send_message.StatusUpdatedReceiver

class MmsSentReceiverImpl: StatusUpdatedReceiver() {
    override fun updateInInternalDatabase(
        context: Context?,
        intent: Intent?,
        receiverResultCode: Int
    ) {
            println("MMS receiverResultCode: $receiverResultCode")
    }

    override fun onMessageStatusUpdated(
        context: Context?,
        intent: Intent?,
        receiverResultCode: Int
    ) {
        Telephony.Sms.MESSAGE_TYPE_QUEUED
        println("MMS receiverResultCode: $receiverResultCode")
    }

    fun logAllIntentExtras(intent: Intent?) {
        if (intent == null) {
            Log.d("IntentExtras", "Intent is null.")
            return
        }

        val extras: Bundle? = intent.extras
        if (extras == null || extras.isEmpty) {
            Log.d("IntentExtras", "Intent has no extras.")
            return
        }

        Log.d("IntentExtras", "--- Intent Extras ---")
        for (key: String in extras.keySet()) {
            val value = extras.get(key)
            Log.d("IntentExtras", "Key: $key, Value: $value, Type: ${value?.javaClass?.name ?: "null"}")
        }
        Telephony.Sms.MESSAGE_TYPE_QUEUED
        Log.d("IntentExtras", "---------------------")
    }
}
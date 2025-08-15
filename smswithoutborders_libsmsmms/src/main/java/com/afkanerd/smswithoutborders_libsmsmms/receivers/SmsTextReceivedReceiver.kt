package com.afkanerd.smswithoutborders_libsmsmms.receivers

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Base64
import android.util.Log
import android.util.Pair
import android.widget.Toast
import com.afkanerd.smswithoutborders_libsmsmms.BuildConfig
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.notifyText
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ConversationsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class SmsTextReceivedReceiver : BroadcastReceiver() {
    companion object {
        var SMS_SENT_BROADCAST_INTENT = "com.afkanerd.deku.SMS_SENT_BROADCAST_INTENT"
        var SMS_DELIVERED_BROADCAST_INTENT = "com.afkanerd.deku.SMS_DELIVERED_BROADCAST_INTENT"
        var DATA_SENT_BROADCAST_INTENT = "com.afkanerd.deku.DATA_SENT_BROADCAST_INTENT"
        var DATA_DELIVERED_BROADCAST_INTENT = "com.afkanerd.deku.DATA_DELIVERED_BROADCAST_INTENT"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Telephony.Sms.Intents.SMS_DELIVER_ACTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val conversation = registerIncomingText(context, intent)
                        context.notifyText(conversation)
                    }
                }
            }
            SMS_SENT_BROADCAST_INTENT, SMS_DELIVERED_BROADCAST_INTENT,
            DATA_SENT_BROADCAST_INTENT, DATA_DELIVERED_BROADCAST_INTENT -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val id = intent.getIntExtra("id", -1)

                    val conversation = context.getDatabase().conversationsDao()
                        ?.getConversation(id)

                    if (resultCode == Activity.RESULT_OK) {
                        // TODO: manually update the local db
                        conversation?.sms?.status = if(intent.action == SMS_DELIVERED_BROADCAST_INTENT)
                            Telephony.TextBasedSmsColumns.STATUS_COMPLETE
                        else Telephony.TextBasedSmsColumns.STATUS_NONE
                        conversation?.sms?.type = Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT
                    } else {
                        conversation?.sms?.status = Telephony.TextBasedSmsColumns.STATUS_FAILED
                        conversation?.sms?.type = Telephony.TextBasedSmsColumns.MESSAGE_TYPE_FAILED
                        conversation?.sms?.error_code = resultCode

                        conversation?.let {
                            notifyMessageFailedToSend(context, conversation)
                        }
                    }

                    conversation?.let {
                        ConversationsViewModel().update(context, conversation)
                    }
                }
            }
        }
    }


//    private fun processEncryptedIncoming(context: Context, address: String, text: String):
//            Pair<String?, Boolean> {
//        var text = text
//        var encrypted = false
//        if (E2EEHandler.isValidMessage(Base64.decode(text, Base64.DEFAULT))) {
//            val payload = E2EEHandler.extractMessageFromPayload(Base64.decode(text, Base64.DEFAULT))
//
//            val isSelf = E2EEHandler.isSelf(context, address)
//            val keypair = E2EEHandler.fetchKeypair(context, address, isSelf)
//            val peerPublicKey = if(isSelf) keypair.second else
//                Base64.decode(E2EEHandler.secureFetchPeerPublicKey(context, address), Base64.DEFAULT)
//            var states = E2EEHandler.fetchStates(context, address, isSelf)
//            if(states.isBlank()) {
//                val bobState = States()
//                val SK = E2EEHandler.calculateSharedSecret(context, address, peerPublicKey)
//                Ratchets.ratchetInitBob(bobState, SK, keypair)
//                states = bobState.serializedStates
//            }
//            val receivingState = States(states)
//            if(BuildConfig.DEBUG)
//                println(states)
//            val decryptedText = Ratchets.ratchetDecrypt(receivingState, payload.first,
//                payload.second, keypair.second)
//            text = String(decryptedText, Charsets.UTF_8)
//            encrypted = true
//
//            if(BuildConfig.DEBUG)
//                Toast.makeText(context, "Decryption happened!", Toast.LENGTH_LONG).show()
//
//            E2EEHandler.storeState(context, receivingState.serializedStates, address, isSelf)
//        }
//
//        return Pair(text, encrypted)
//    }


    private fun notifyMessageFailedToSend(context: Context, conversation: Conversations) {
        val content = context
                .getString(
                    R.string
                        .message_failed_send_notification_description_a_message_failed_to_send_to) +
                " ${conversation.sms?.address}"

        context.notifyText(
            conversation = conversation.apply {
                sms?.body = content },
            actions = false)
    }

    fun registerIncomingText(context: Context, intent: Intent): Conversations {
        val messageId = System.currentTimeMillis()

        val bundle = intent.extras
        val subscriptionId = bundle!!.getInt("subscription", -1)
        var address: String? = ""
        val bodyBuffer = StringBuilder()
        var dateSent: Long = 0
        val date = System.currentTimeMillis()
        var status = -1

        for (currentSMS in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
            address = currentSMS.displayOriginatingAddress
            bodyBuffer.append(currentSMS.displayMessageBody)
            dateSent = currentSMS.timestampMillis
            status = currentSMS.status
        }
        val body = bodyBuffer.toString()

        // TODO: process encrypted message
        return ConversationsViewModel().addConversation(
            context = context,
            messageId = messageId.toString(),
            body = body,
            subscriptionId = subscriptionId,
            date = date,
            dateSent = dateSent,
            address = address!!,
            type = Telephony.Sms.MESSAGE_TYPE_INBOX,
            status = status
        )
    }

}
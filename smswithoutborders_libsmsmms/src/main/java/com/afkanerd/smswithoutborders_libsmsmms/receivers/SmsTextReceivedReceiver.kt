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
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.notifyText
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.registerIncomingText
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ConversationsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class SmsTextReceivedReceiver : BroadcastReceiver() {
    companion object {
        var SMS_SENT_BROADCAST_INTENT = "com.afkanerd.deku.SMS_SENT_BROADCAST_INTENT"
        var SMS_DELIVERED_BROADCAST_INTENT = "com.afkanerd.deku.SMS_DELIVERED_BROADCAST_INTENT"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_DELIVER_ACTION) {
            if (resultCode == Activity.RESULT_OK) {
                CoroutineScope(Dispatchers.IO).launch {
                    val conversation = registerIncomingText(context, intent)
                    context.notifyText(conversation)
                }
            }
        }
        else if (intent.action == SMS_SENT_BROADCAST_INTENT) {
            TODO("Implement this method")
//            coroutineScope.launch{
//                val id = intent.getStringExtra(NativeSMSDB.ID)!!
//
//                val datastore = Datastore.getDatastore(context)
//                val conversation = datastore.conversationDao().getMessage(id)
//
//                if (resultCode == Activity.RESULT_OK) {
//                    NativeSMSDB.Outgoing.register_sent(context, id)
//                    conversation.status = Telephony.TextBasedSmsColumns.STATUS_NONE
//                    conversation.type = Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT
//                } else {
//                    try {
//                        NativeSMSDB.Outgoing.register_failed(context, id, resultCode)
//                        conversation.status = Telephony.TextBasedSmsColumns.STATUS_FAILED
//                        conversation.type = Telephony.TextBasedSmsColumns.MESSAGE_TYPE_FAILED
//                        conversation.error_code = resultCode
//
//                    } catch (e: Exception) {
//                        Log.e(javaClass.name,
//                            "Exception with sent message broadcast", e)
//                    } finally {
//                        conversation.thread_id?.let {
//                            notifyMessageFailedToSend(context, conversation)
//                        }
//                    }
//                }
//                datastore.conversationDao()._update(conversation)
//            }
        }
        else if (intent.action == SMS_DELIVERED_BROADCAST_INTENT) {
            TODO("Implement this method")
//            coroutineScope.launch {
//                val id = intent.getStringExtra(NativeSMSDB.ID)!!
//                val conversation = Datastore.getDatastore(context).conversationDao().getMessage(id)
//
//                if (resultCode == Activity.RESULT_OK) {
//                    NativeSMSDB.Outgoing.register_delivered(context, id)
//                    conversation.status = Telephony.TextBasedSmsColumns.STATUS_COMPLETE
//                    conversation.type = Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT
//                } else {
//                    conversation.status = Telephony.TextBasedSmsColumns.STATUS_FAILED
//                    conversation.type = Telephony.TextBasedSmsColumns.MESSAGE_TYPE_FAILED
//                    conversation.error_code = resultCode
//                }
//                Datastore.getDatastore(context).conversationDao()._update(conversation)
//            }
        }
//        else if (intent.action == SmsDataReceivedReceiver.DATA_SENT_BROADCAST_INTENT) {
//            coroutineScope.launch{
//                val id = intent.getStringExtra(NativeSMSDB.ID)!!
//                val conversation = Datastore.getDatastore(context).conversationDao().getMessage(id)
//
//                if (resultCode == Activity.RESULT_OK) {
//                    conversation.status = Telephony.TextBasedSmsColumns.STATUS_NONE
//                    conversation.type = Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT
//                } else {
//                    conversation.status = Telephony.TextBasedSmsColumns.STATUS_FAILED
//                    conversation.error_code = resultCode
//                    conversation.type = Telephony.TextBasedSmsColumns.MESSAGE_TYPE_FAILED
//                }
//                Datastore.getDatastore(context).conversationDao()._update(conversation)
//            }
//        }
//        else if (intent.action == SmsDataReceivedReceiver.DATA_DELIVERED_BROADCAST_INTENT) {
//            coroutineScope.launch{
//                val id = intent.getStringExtra(NativeSMSDB.ID)!!
//                val conversation = Datastore.getDatastore(context).conversationDao().getMessage(id)
//
//                if (resultCode == Activity.RESULT_OK) {
//                    conversation.status = Telephony.TextBasedSmsColumns.STATUS_COMPLETE
//                    conversation.type = Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT
//                } else {
//                    conversation.status = Telephony.TextBasedSmsColumns.STATUS_FAILED
//                    conversation.error_code = resultCode
//                    conversation.type = Telephony.TextBasedSmsColumns.MESSAGE_TYPE_FAILED
//                }
//                Datastore.getDatastore(context).conversationDao()._update(conversation)
//            }
//        }
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
        TODO("Fix implementation")
//        val notificationIntent = Intent(context, MainActivity::class.java).apply {
//            putExtra("thread_id", conversation.thread_id)
//            putExtra("address", conversation.address)
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//
//        val content = context
//                .getString(R.string
//                        .message_failed_send_notification_description_a_message_failed_to_send_to) +
//                " ${conversation.address}"
//
////        val builder = Notifications.createNotification(
////            context = context,
////            title = conversation.address!!,
////            text = content,
////            address = conversation.address!!,
////            requestCode = conversation.thread_id!!.toInt(),
////            contentIntent = notificationIntent,
////        )
//        context.notifyText(conversation.apply {
//            text = content
//        })
    }

    fun registerIncomingText(context: Context, intent: Intent): Conversations {
        val messageId = System.currentTimeMillis()

        val bundle = intent.extras
        val subscriptionId = bundle!!.getInt("subscription", -1)
        var address: String? = ""
        val bodyBuffer = StringBuilder()
        var dateSent: Long = 0
        val date = System.currentTimeMillis()

        for (currentSMS in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
            address = currentSMS.displayOriginatingAddress
            val text_message = currentSMS.displayMessageBody
            dateSent = currentSMS.timestampMillis
            bodyBuffer.append(text_message)
        }
        val body = bodyBuffer.toString()

        // TODO: process encrypted message

        return ConversationsViewModel().addIncomingConversation(
            context = context,
            messageId = messageId.toString(),
            body = body,
            subscriptionId = subscriptionId,
            date = date,
            dateSent = dateSent,
            address = address!!
        )
    }

}
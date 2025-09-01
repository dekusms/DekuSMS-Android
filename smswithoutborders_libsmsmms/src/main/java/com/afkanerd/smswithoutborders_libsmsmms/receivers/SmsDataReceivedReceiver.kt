package com.afkanerd.smswithoutborders_libsmsmms.receivers

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Base64
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
                        if(!it.isMute) context.sendNotificationBroadcast(conversation)
                    }
                }
            }
        }
    }
//
//    private fun processAndGetFlags(
//        context: Context,
//        data: ByteArray,
//        address: String
//    ): BooleanArray {
//        /**
//         * 0 - is Self
//         * 1 - is conversation now encrypted (either request post agree or receiving agree)
//         */
//        var isSelf = false
//        var isSecured = false
//
//        val magicNumber = getRequestType(data)
//        if (magicNumber != null) {
//            when (magicNumber) {
//                MagicNumber.REQUEST -> {
//                    val publicKey = extractPublicKeyFromPayload(data)
//                    if (sameRequest(context, address, publicKey)) {
//                        makeSelfRequest(context, address)
//                        isSelf = true
//                    }
//                    else if(E2EEHandler.containsPeer(context, address)) {
//                        E2EEHandler.clear(context, address)
//                    }
//                    secureStorePeerPublicKey(
//                        context, address,
//                        extractPublicKeyFromPayload(data), false
//                    )
//                    isSecured = isSecured(context, address)
//                }
//
//                MagicNumber.ACCEPT -> {
//                    secureStorePeerPublicKey(
//                        context, address,
//                        extractPublicKeyFromPayload(data), false
//                    )
//                    isSecured = isSecured(context, address)
//                }
//
//                MagicNumber.MESSAGE -> {}
//            }
//        }
//        return booleanArrayOf(isSelf, isSecured)
//    }
//
////    companion object {
////        var DATA_DELIVER_ACTION: String = BuildConfig.APPLICATION_ID + ".DATA_DELIVER_ACTION"
////
////        var DATA_SENT_BROADCAST_INTENT: String =
////            BuildConfig.APPLICATION_ID + ".DATA_SENT_BROADCAST_INTENT"
////
////        var DATA_DELIVERED_BROADCAST_INTENT: String =
////            BuildConfig.APPLICATION_ID + ".DATA_DELIVERED_BROADCAST_INTENT"
////    }
}
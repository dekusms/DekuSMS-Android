package com.afkanerd.smswithoutborders_libsmsmms.extensions.context

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.mmsParser
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.smsMmsNatives
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.receivers.MmsSentReceiverImpl
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ConversationsViewModel
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Transaction
import kotlin.jvm.java

object SmsMmsDataBroadcastActions {
    const val SMS_DATA_SENT_BROADCAST_INTENT = "SMS_DATA_SENT_BROADCAST_INTENT"
    const val SMS_DATA_DELIVERED_BROADCAST_INTENT = "SMS_DATA_DELIVERED_BROADCAST_INTENT"
}

@Throws
fun Context.sendData(
    context: Context,
    data: ByteArray,
    threadId: String,
    address: String,
    subscriptionId: Int,
) {
    val conversation = Conversations(
        sms = smsMmsNatives.Sms(
            _id = (System.currentTimeMillis() / 1000).toInt(),
            thread_id = threadId.toInt(),
            address = address,
            date = (System.currentTimeMillis() / 1000).toInt(),
            date_sent = 0,
            read = 1,
            status = Telephony.Sms.STATUS_PENDING,
            type = Telephony.Sms.MESSAGE_TYPE_OUTBOX,
            body = "",
            sub_id = subscriptionId,
        ), sms_data_ = data
    )

    try {
        ConversationsViewModel().add(this, conversation)
    } catch (e: Exception) {
        throw e
    }

    val address = makeE16PhoneNumber(address)
    val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(SmsManager::class.java)
            .createForSubscriptionId(subscriptionId)
    } else {
        SmsManager.getSmsManagerForSubscriptionId( subscriptionId)
    }

    val sentIntent = Intent(SmsMmsDataBroadcastActions.SMS_DATA_SENT_BROADCAST_INTENT)
    sentIntent.setPackage(context.packageName)
    sentIntent.putExtra("id", conversation.sms!!._id)

    val deliveredIntent = Intent(SmsMmsDataBroadcastActions
        .SMS_DATA_DELIVERED_BROADCAST_INTENT)
    deliveredIntent.setPackage(context.packageName)
    deliveredIntent.putExtra("id", conversation.sms._id)

    val sentPendingIntent = PendingIntent.getBroadcast(
        context,
        conversation.sms._id.toLong().toInt(),
        sentIntent,
        PendingIntent.FLAG_IMMUTABLE
    )

    val deliveredPendingIntent = PendingIntent.getBroadcast(
        context,
        conversation.sms._id.toLong().toInt(),
        deliveredIntent,
        PendingIntent.FLAG_IMMUTABLE
    )

    val dataTransmissionPort: Short = 8200
    try {
        smsManager.sendDataMessage(
            address,
            null,
            dataTransmissionPort,
            data,
            sentPendingIntent,
            deliveredPendingIntent
        )
    } catch (e: Exception) {
        throw e
    }
}

@Throws
fun Context.sendSms(
    context: Context,
    text: String,
    address: String,
    threadId: String,
    subscriptionId: Int,
) {
    val address = makeE16PhoneNumber(address)

    val conversation = Conversations(sms = smsMmsNatives.Sms(
        _id = (System.currentTimeMillis() / 1000).toInt(),
        thread_id = threadId.toInt(),
        address = address,
        date = (System.currentTimeMillis() / 1000).toInt(),
        date_sent = 0,
        read = 1,
        status = Telephony.Sms.STATUS_PENDING,
        type = Telephony.Sms.MESSAGE_TYPE_OUTBOX,
        body = text,
        sub_id = subscriptionId,
    ))

    try {
        ConversationsViewModel().add(this, conversation)
    } catch (e: Exception) {
        throw e
    }

//    val payload = E2EEHandler.encryptMessage(context, text, address)
//
//    val settings = Settings()
//    settings.subscriptionId = subscriptionId
//    settings.group = false
//    settings.deliveryReports = true
//    settings.useSystemSending = true
//
//    val message = Message()
//    message.text = payload.first
//    message.addresses = arrayOf(address)
//
//    val transaction = Transaction(context, settings)
//    transaction.sendNewMessage(message)
}

@Throws
fun Context.sendMms(
    context: Context,
    contentUri: Uri,
    text: String,
    address: String,
    threadId: String,
    subscriptionId: Int,
) {
    val address = makeE16PhoneNumber(address)
    val conversation = Conversations(
        mms = smsMmsNatives.Mms(
            _id = (System.currentTimeMillis() / 1000).toInt(),
            thread_id = threadId.toInt(),
            date = (System.currentTimeMillis() / 1000).toInt(),
            date_sent = 0,
            msg_box = Telephony.Mms.MESSAGE_BOX_OUTBOX,
            read = 1,
            sub_id = subscriptionId,
            seen = 1,
        ),
        mms_text = text,
        mms_content_uri = contentUri.toString(),
        mms_mimetype = context.contentResolver.getType(contentUri),
        mms_filename = mmsParser.getFileName(context, contentUri),
    )

    try {
        ConversationsViewModel().add(context, conversation)
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }

    val sendSettings = mmsParser.getSendMessageSettings()
    sendSettings.subscriptionId = subscriptionId

    val intent = Intent(context, MmsSentReceiverImpl::class.java)
        .apply {
            this.putExtra(
                MmsSentReceiverImpl.EXTRA_ORIGINAL_RESENT_MESSAGE_ID,
                conversation.mms!!._id,
            )
        }

    val sendTransaction = Transaction(context, sendSettings)
    sendTransaction .setExplicitBroadcastForSentMms(intent)

    val mMessage = Message("", address)
    val mimeType = context.contentResolver.getType(contentUri)
    val filename = mmsParser.getFileName(context, contentUri)

    mMessage.addMedia(
        mmsParser.getBytesFromUri(context, contentUri),
        mimeType,
        filename
    )

    try {
        sendTransaction.sendNewMessage(mMessage)
    } catch(e: Exception) {
        e.printStackTrace()
    }
}

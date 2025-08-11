package com.afkanerd.smswithoutborders_libsmsmms

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.telephony.SmsManager

object Transmissions {
    private const val DATA_TRANSMISSION_PORT: Short = 8200
    @Throws(Exception::class)
    fun sendTextSMS(
        context: Context,
        destinationAddress: String,
        text: String,
        sentIntent: PendingIntent?,
        deliveryIntent: PendingIntent?,
        subscriptionId: Int
    ) {
        if (text.isEmpty()) return

        val smsManager = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            context.getSystemService(SmsManager::class.java)
                .createForSubscriptionId(subscriptionId)
        else SmsManager.getSmsManagerForSubscriptionId(subscriptionId)

        try {
            val dividedMessage = smsManager.divideMessage(text)
            if (dividedMessage.size < 2) smsManager.sendTextMessage(
                destinationAddress,
                null,
                text,
                sentIntent,
                deliveryIntent
            )
            else {
                val sentPendingIntents = ArrayList<PendingIntent?>()
                val deliveredPendingIntents = ArrayList<PendingIntent?>()

                for (i in 0 until dividedMessage.size - 1) {
                    sentPendingIntents.add(null)
                    deliveredPendingIntents.add(null)
                }

                sentPendingIntents.add(sentIntent)
                deliveredPendingIntents.add(deliveryIntent)

                smsManager.sendMultipartTextMessage(
                    destinationAddress,
                    null,
                    dividedMessage,
                    sentPendingIntents,
                    deliveredPendingIntents
                )
            }
        } catch (e: Exception) {
            throw Exception(e)
        }
    }

    @Throws(Exception::class)
    fun sendDataSMS(
        destinationAddress: String?, data: ByteArray?,
        sentIntent: PendingIntent?, deliveryIntent: PendingIntent?,
        subscriptionId: Int?
    ) {
        if (data == null) return

        val smsManager = SmsManager.getSmsManagerForSubscriptionId(
            subscriptionId!!
        )
        try {
            smsManager.sendDataMessage(
                destinationAddress,
                null,
                DATA_TRANSMISSION_PORT,
                data,
                sentIntent,
                deliveryIntent
            )
        } catch (e: Exception) {
            throw Exception(e)
        }
    }

    fun sendMms(
        context: Context,
        messageId: String,
        destinationAddress: String,
        text: String,
        subscriptionId: Int,
        contentUri: Uri,
    ) {
        val sendSettings = context.getSendMessageSettings()
        sendSettings.subscriptionId = subscriptionId

        val intent = Intent(context, MmsSentReceiverImpl::class.java).apply {
            this.putExtra(MmsSentReceiverImpl.EXTRA_ORIGINAL_RESENT_MESSAGE_ID, messageId)
        }
        val sendTransaction = Transaction(context, sendSettings)
        sendTransaction .setExplicitBroadcastForSentMms(intent)
        val mMessage = Message(text, destinationAddress)

        val mimeType = context.contentResolver.getType(contentUri)
        val filename = getFileName(context, contentUri)
        mMessage.addMedia(getBytesFromUri(context, contentUri), mimeType, filename)

        try {
            sendTransaction.sendNewMessage(mMessage)
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use{
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }


    fun getBytesFromUri(context: Context, uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun Context.getSendMessageSettings(): Settings {
        val settings = Settings()
        settings.useSystemSending = true
        settings.deliveryReports = true
        settings.sendLongAsMms = false
//        settings.sendLongAsMmsAfter = 1
        settings.group = false
        return settings
    }

}
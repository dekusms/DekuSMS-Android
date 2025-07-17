package com.afkanerd.deku.DefaultSMS.Models

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.telephony.CarrierConfigManager
import android.telephony.SmsManager
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction
import androidx.core.graphics.createBitmap
import com.afkanerd.deku.DefaultSMS.BroadcastReceivers.MMSReceiverBroadcastReceiver
import com.afkanerd.deku.DefaultSMS.R


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
        destinationAddress: String,
        text: String,
        binaryData: ByteArray?,
        threadId: Long?,
        subscriptionId: Int,
        contentUri: Uri?,
    ) {
        val threadId = threadId ?: Transaction.NO_THREAD_ID

        val sendSettings = Settings()
        sendSettings.deliveryReports = true
        sendSettings.subscriptionId = subscriptionId

//        val info = getApnInfo(context)

//        sendSettings.mmsc = "http://mms.du.ae:8002/"
//        sendSettings.proxy = "10.164.208.4"
//        sendSettings.port = "8002"
        sendSettings.useSystemSending = true
//        sendSettings.sendLongAsMms = false
//        sendSettings.sendLongAsMmsAfter = 3
        sendSettings.group = false

        val sendTransaction = Transaction(context, sendSettings)
        sendTransaction.setExplicitBroadcastForSentMms(Intent(context, MMSReceiverBroadcastReceiver::class.java))
        val mMessage = Message(text, destinationAddress)

//        val bitmap = drawableToBitmap(ContextCompat.getDrawable(context, R.drawable.github_mark)!!)
        val bitmap = BitmapFactory.decodeByteArray(binaryData, 0, binaryData!!.size)
        mMessage.setImage(bitmap) // not necessary for voice or sms messages
//        mMessage.addMedia(binaryData, "application/image")
        try {
            sendTransaction.sendNewMessage(mMessage, threadId)
        } catch(e: Exception) {
            e.printStackTrace()
        }


//        val smsManager = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
//            context.getSystemService(SmsManager::class.java)
//                .createForSubscriptionId(subscriptionId)
//        else SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
//
//        smsManager.sendMultimediaMessage(
//            context,
//            contentUri,
//            destinationAddress,
//            null,
//            null
//        )

        println("MMS sending done...")
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            drawable.bitmap?.let { return it }
        }

        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 1
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 1

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }


}

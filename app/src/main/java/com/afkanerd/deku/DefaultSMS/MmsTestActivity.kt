package com.afkanerd.deku.DefaultSMS

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import com.afkanerd.deku.DefaultSMS.Models.SIMHandler
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class MmsTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mmsInternalInjectionTest()
    }

    fun persistImageToCache(context: Context, uri: Uri): File? {
        return try {
            val fileName = getFileName(context, uri) ?: "temp_image_${System.currentTimeMillis()}.jpg"
            val file = File(context.cacheDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Handle the picked image URI here
            println(uri)
            val file = persistImageToCache(this, uri)
            var bytes = getBytesFromUri(this, file!!.toUri())
//            bytes = "".toByteArray()

            val settings = getSendMessageSettings()
            val subId = SIMHandler.getDefaultSimSubscription(applicationContext)
            if (subId != null) {
                settings.subscriptionId = subId
            }

            val transaction = Transaction(this, settings)

            val message = Message("Hello world 2", "+971582306355")

            val mimeType = contentResolver.getType(uri)
            val filename = getFileName(this, uri)

//            message.save = false
            message.fromAddress = "+971581442821"
            message.addMedia(bytes, mimeType, filename)
//            message.setImage(BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size))
            transaction.sendNewMessage(message)
        }
    }

    fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
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


    fun mmsInternalInjectionTest() {
//        val settings = Settings()
//        settings.useSystemSending = true
//        settings.deliveryReports = true
//        settings.subscriptionId = SIMHandler.getDefaultSimSubscription(applicationContext)
//        settings.mmsc = "http://mms.du.ae:8002/"
//        settings.proxy = "10.164.208.4"
//        settings.port = "8002"
//        settings.group = true
//        settings.sendLongAsMms = true
//        settings.sendLongAsMmsAfter = 3
//        settings.group = false

//        val settings = getSendMessageSettings()
//        val subId = SIMHandler.getDefaultSimSubscription(applicationContext)
//        if (subId != null) {
//            settings.subscriptionId = subId
//        }
//        val transaction = Transaction(this, settings)
//
//        val message = Message("Hello world 2", "+971582306355")
//        val bitmap = AppCompatResources.getDrawable(applicationContext,
//            R.drawable.ic_launcher_foreground)?.toBitmap()
//        val bitmapBytes = bitmapToBytes(bitmap!!)
//        message.addMedia(bitmapBytes, "image/png", "ic_launcher_foreground" )
//
//        transaction.sendNewMessage(message, Transaction.NO_THREAD_ID)
        pickImageLauncher.launch("image/*")
    }

    fun bitmapToBytes(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(format, quality, stream)
        return stream.toByteArray()
    }


    fun Context.getSendMessageSettings(): Settings {
        val settings = Settings()
        settings.useSystemSending = true
//        settings.deliveryReports = config.enableDeliveryReports
//        settings.sendLongAsMms = config.sendLongMessageMMS
        settings.deliveryReports = false
        settings.sendLongAsMms = false
        settings.sendLongAsMmsAfter = 1
        settings.group = false
        settings.port = "0"
        return settings
    }
}
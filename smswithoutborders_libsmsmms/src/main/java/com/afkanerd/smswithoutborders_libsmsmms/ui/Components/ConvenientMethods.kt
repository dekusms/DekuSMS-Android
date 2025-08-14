package com.afkanerd.smswithoutborders_libsmsmms.ui.Components

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.PorterDuff.Mode
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.provider.BlockedNumberContract
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat.getString
import androidx.core.content.ContextCompat.startActivity
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import androidx.core.graphics.createBitmap
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations

object ConvenientMethods {

    fun getRoundedCornerImageBitmap(imageBitmap: ImageBitmap, pixels: Int): Bitmap {
        val bitmap = imageBitmap.asAndroidBitmap()
        val output = createBitmap(bitmap.width, bitmap.height)
        val canvas = android.graphics.Canvas(output)

        val color = 0xff424242.toInt()
        val paint = android.graphics.Paint()
        val rect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = android.graphics.RectF(rect)
        val roundPx = pixels.toFloat()

        paint.isAntiAlias = true
        canvas.drawColor(android.graphics.Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
        paint.color = color
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    fun deriveMetaDate(conversation: Conversations): String{
        val dateFormat: DateFormat = SimpleDateFormat("h:mm a");
        return dateFormat.format(Date(conversation.sms?.date!!.toLong()));
    }

}
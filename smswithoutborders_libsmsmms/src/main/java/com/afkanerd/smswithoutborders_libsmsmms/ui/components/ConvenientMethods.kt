package com.afkanerd.smswithoutborders_libsmsmms.ui.components

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import androidx.core.graphics.createBitmap
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
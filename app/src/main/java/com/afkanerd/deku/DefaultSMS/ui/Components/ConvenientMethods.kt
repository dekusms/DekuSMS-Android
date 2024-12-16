package com.afkanerd.deku.DefaultSMS.ui.Components

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
import com.afkanerd.deku.Datastore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.afkanerd.deku.DefaultSMS.R

object ConvenientMethods {

    fun blockContact(context: Context, address: String) {
        val contentValues = ContentValues();
        contentValues.put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, address);
        val uri = context.contentResolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI,
            contentValues);

        Toast.makeText(context, getString(context, R.string.conversations_menu_block_toast),
            Toast.LENGTH_SHORT).show();
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        startActivity(context, telecomManager.createManageBlockedNumbersIntent(), null);
    }

    fun unblockContact(context: Context) {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        startActivity(context, telecomManager.createManageBlockedNumbersIntent(), null);
    }

//    fun getRoundedCornerBitmap(bitmap: Bitmap, pixels: Int) : Bitmap{
//        val output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
//        .getHeight(), Config.ARGB_8888);
//        val canvas = Canvas(output.asImageBitmap());
//
//        val color = 0xff424242;
//        val paint = android.graphics.Paint()
//        val rect = Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
//        val rectF = RectF(rect);
//        val roundPx = pixels;
//
//        paint.isAntiAlias = true;
////        paint.setColor(color);
//        canvas.drawRoundRect(0f,0f,0f,0f, roundPx.toFloat(), roundPx.toFloat(), paint);
//
//        paint.xfermode
////        paint.setXfermode(PorterDuffXfermode(Mode.SRC_IN));
//        canvas.drawImage(bitmap.asImageBitmap(), , bitmap.height), paint);
//
//        return output;
//    }

    fun getRoundedCornerImageBitmap(imageBitmap: ImageBitmap, pixels: Int): Bitmap {
        val bitmap = imageBitmap.asAndroidBitmap()
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
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


}
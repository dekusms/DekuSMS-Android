package com.afkanerd.deku.DefaultSMS.Models

import android.content.Context
import android.database.sqlite.SqliteWrapper
import android.net.Uri
import android.provider.Telephony
import com.android.mms.transaction.PushReceiver
import com.google.android.mms.MmsException

object MmsHandler {

    const val COLUMN_CONTENT_LOCATION = 0
    fun getContentLocation(context: Context, uri: Uri?): String? {
        val projection = arrayOf(
            Telephony.Mms.CONTENT_LOCATION,
            Telephony.Mms.LOCKED
        )


        val cursor = SqliteWrapper.query(
            context,
            context.contentResolver,
            uri,
            projection,
            null,
            null,
            null
        )

        if (cursor != null) {
            try {
                if ((cursor.count == 1) && cursor.moveToFirst()) {
                    val location = cursor.getString(COLUMN_CONTENT_LOCATION)
                    cursor.close()
                    return location
                }
            } finally {
                cursor.close()
            }
        }

        throw MmsException("Cannot get X-Mms-Content-Location from: " + uri)
    }


    fun getTransactionId(context: Context, uri: Uri?): String? {
        val transactionProjectionId = arrayOf(
            Telephony.Mms.CONTENT_LOCATION,
            Telephony.Mms.LOCKED
        )

        val cursor = SqliteWrapper.query(
            context, context.contentResolver,
            uri, transactionProjectionId, null, null, null
        )

        if (cursor != null) {
            try {
                if ((cursor.count == 1) && cursor.moveToFirst()) {
                    val transactionId = cursor.getString(COLUMN_CONTENT_LOCATION)
                    cursor.close()
                    return transactionId
                }
            } finally {
                cursor.close()
            }
        }

        throw MmsException("Cannot get Transaction-id from: " + uri)
    }
}
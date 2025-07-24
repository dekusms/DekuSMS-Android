package com.afkanerd.deku.DefaultSMS.Models

import android.content.Context
import android.database.sqlite.SqliteWrapper
import android.net.Uri
import android.provider.Telephony
import android.util.Xml
import com.android.mms.transaction.PushReceiver
import com.google.android.mms.MmsException
import org.xmlpull.v1.XmlPullParser

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

    private const val ELEMENT_TAG_IMAGE: String = "img"
    private const val ELEMENT_TAG_AUDIO: String = "audio"
    private const val ELEMENT_TAG_VIDEO: String = "video"
    private const val ELEMENT_TAG_VCARD: String = "vcard"
    private const val ELEMENT_TAG_REF: String = "ref"

    private val ELEMENT_TAGS = arrayOf(
        ELEMENT_TAG_IMAGE, ELEMENT_TAG_VIDEO, ELEMENT_TAG_AUDIO, ELEMENT_TAG_VCARD, ELEMENT_TAG_REF
    )

    fun parseAttachmentNames(text: String): List<String> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(text.reader())
        parser.nextTag()
        return readSmil(parser)
    }

    private fun readSmil(parser: XmlPullParser): List<String> {
        parser.require(XmlPullParser.START_TAG, null, "smil")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (parser.name == "body") {
                return readBody(parser)
            } else {
                skip(parser)
            }
        }

        return emptyList()
    }

    private fun readBody(parser: XmlPullParser): List<String> {
        val names = mutableListOf<String>()
        parser.require(XmlPullParser.START_TAG, null, "body")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (parser.name == "par") {
                parser.require(XmlPullParser.START_TAG, null, "par")
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.eventType != XmlPullParser.START_TAG) {
                        continue
                    }

                    if (parser.name in ELEMENT_TAGS) {
                        names.add(parser.getAttributeValue(null, "src"))
                        skip(parser)
                    } else {
                        skip(parser)
                    }
                }
            } else {
                skip(parser)
            }
        }
        return names
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }

        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}

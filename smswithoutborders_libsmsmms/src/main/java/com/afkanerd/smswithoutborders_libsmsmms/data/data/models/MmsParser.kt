package com.afkanerd.smswithoutborders_libsmsmms.data.data.models

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Telephony
import android.util.Xml
import com.klinker.android.send_message.Settings
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.MessageFormat
import androidx.core.net.toUri
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getThreadId
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.parseRawMmsContents

object MmsParser {
    const val COLUMN_CONTENT_LOCATION = 0

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

    fun getMmsContentLocation(context: Context, uri: Uri): String? {
        val projection = arrayOf(
            Telephony.Mms.CONTENT_LOCATION,
            Telephony.Mms.LOCKED
        )

        val cursor = context.contentResolver.query(
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
        return null
    }

    fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(
            uri,
            null,
            null,
            null,
            null
        )

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

    fun getSendMessageSettings(): Settings {
        val settings = Settings()
        settings.useSystemSending = true
        settings.deliveryReports = true
        settings.sendLongAsMms = false
//        settings.sendLongAsMmsAfter = 1
        settings.group = false
        return settings
    }

    data class ParsedMms(
        var address: String? = null,
        var text: String? = null,
        var mimeType: String? = null,
        var filename: String? = null,
        var contentUri: Uri? = null,
        var filepath: String? = null,
    ){
        fun getConversation(context: Context, cursor: Cursor): Conversations? {
            if(address.isNullOrEmpty()) return null

            val mms = parseRawMmsContents(cursor)
            val sms = SmsMmsNatives.Sms(
                _id = (System.currentTimeMillis()/1000L),
                thread_id = context.getThreadId(address!!).toInt(),
                address = address,
                sub_id = mms.sub_id ?: -1,
                date = mms.date * 1000L,
                date_sent = mms.date_sent,
                type = mms.m_type!!,
                status = mms.msg_box,
                body = text ?: "",
                read = mms.read ?: 0
            )

            return Conversations(
                sms = sms,
                mms = mms,
                mms_text = text,
                mms_mimetype = mimeType,
                mms_filename = filename,
                mms_filepath = filepath,
                mms_content_uri = contentUri?.toString(),
            )
        }
    }

    @SuppressLint("Range")
    fun parse(
        context: Context,
        cursor: Cursor
    ): Conversations? {
        val uri = "content://mms/part".toUri()
        val id = cursor.getLong(cursor
            .getColumnIndexOrThrow(Telephony.Mms._ID))

        val textOnly = cursor.getInt(cursor
            .getColumnIndexOrThrow(Telephony.Mms.TEXT_ONLY)).run { this == 1 }

        val parsedMms = ParsedMms()
        context.contentResolver.query(
            uri,
            null,
            "${Telephony.Mms.Part.MSG_ID} = ?",
            arrayOf(id.toString()),
            null
        )?.let { partCursor ->
            if (partCursor.moveToFirst()) {
                do {
                    val pid = partCursor.getString(partCursor
                        .getColumnIndex(Telephony.Mms.Part._ID))

                    val type = partCursor.getString(partCursor
                        .getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE))

                    val data = partCursor.getString(partCursor
                        .getColumnIndex(Telephony.Mms.Part._DATA))

                    if (parsedMms.address.isNullOrEmpty())
                        parsedMms.address = getMmsAddr(context, id.toString())

                    if(!data.isNullOrEmpty())
                        parsedMms.filepath = data

                    if ("text/plain" == type) {
                        if (parsedMms.text.isNullOrEmpty())
                            parsedMms.text = partCursor.getString(partCursor
                                .getColumnIndex(Telephony.Mms.Part.TEXT))
                    } else if (type != "application/smil" && !textOnly) {
                        if(parsedMms.contentUri == null) {
                            parsedMms.mimeType = type
                            parsedMms.contentUri = ("content://mms/part/$pid").toUri()
                        }
                    } else {
                        val text = partCursor.getString(partCursor
                            .getColumnIndex(Telephony.Mms.Part.TEXT))
                        parsedMms.filename = parseAttachmentNames(text).firstOrNull()
                    }
                } while (partCursor.moveToNext())
            }
            partCursor.close()
        }

        return parsedMms.getConversation(context, cursor)
    }

    fun getMmsContent(context: Context, uri: Uri): ByteArray {
        val outputStream = ByteArrayOutputStream()

        try {
            context.contentResolver.openInputStream(uri).use { inputStream ->
                if (inputStream != null) {
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return outputStream.toByteArray()
    }

    fun getMmsAddr(context: Context, id: String): String {
        val sel = "msg_id=" + id
        val uriString = MessageFormat.format("content://mms/{0}/addr", id)
        val uri = Uri.parse(uriString)
        val c = context.getContentResolver().query(uri, null, sel, null, null)
        val name = StringBuilder()
        if (c != null && c.moveToFirst()) {
            while (c.moveToNext()) {
                val addressIndex = c.getColumnIndex("address")
                val t = c.getString(addressIndex)
                if (!(t.contains("insert"))) name.append(t).append(" ")
            }
            c.close()
        }
        return name.toString()
    }

}
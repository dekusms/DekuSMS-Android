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

    data class MmsContentDataClass(
        val _id: Int,
        val thread_id: Int,
        val date: Int,
        val date_sent: Int,
        val msg_box: Int,
        val read: Int,
        val m_id: String??,
        val sub: String?,
        val sub_cs: Int,
        val ct_t: String?,
        val ct_l: String?,
        val exp: String? = null,
        val m_cls: String?,
        val m_type: Int,
        val v: Int,
        val m_size: Int,
        val pri: Int,
        val rr: Int,
        val rpt_a: String? = null,
        val resp_st: String? = null,
        val st: String? = null,
        val tr_id: String? = null,
        val retr_st: String? = null,
        val retr_txt: String? = null,
        val retr_txt_cs: String? = null,
        val read_status: String? = null,
        val ct_cls: String? = null,
        val resp_txt: String? = null,
        val d_tm: String? = null,
        val d_rpt: Int,
        val locked: Int,
        val sub_id: Int,
        val seen: Int,
        val creator: String?,
        val text_only: Int,
    )

    data class SmsContentDataClass(
        val _id: Int,
        val thread_id: Int,
        val address: String?,
        val person: String? = null,
        val date: Int,
        val date_sent: Int,
        val protocol: String? = null,
        val read: Int,
        val status: Int,
        val type: Int,
        val reply_path_present: String? = null,
        val subject: String? = null,
        val body: String,
        val service_center: String? = null,
        val locked: Int,
        val sub_id: Int,
        val error_code: Int,
        val creator: String,
        val seen: Int,
    )

    data class SmsMmsContents(
        val mms: Map<String, ArrayList<MmsContentDataClass>>,
        val mms_addr: Map<String, ArrayList<MmsAddrContents>>,
        val mms_parts: Map<String, ArrayList<MmsPartContents>>,
        val sms: Map<String, ArrayList<SmsContentDataClass>>,
    )

    data class MmsPartContents(
        val _id: Int,
        val mid: Int,
        val seq: Int,
        val ct: String?,
        val name: String?,
        val chset: Int?,
        val cd: String? = null,
        val fn: String? = null,
        val cid: String?,
        val cl: String?,
        val ctt_s: String? = null,
        val ctt_t: String? = null,
        val _data: String?,
        val text: String?,
        val sub_id: Int,
    )

    data class MmsAddrContents(
        // _id, msg_id, contact_id, address, type, charset, sub_id
        val _id: Int,
        val msg_id : String?,
        val contact_id: String?,
        val address: String?,
        val type: String?,
        val charset: String?,
        val sub_id: Int? = null,
    )

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

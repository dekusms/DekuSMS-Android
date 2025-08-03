package com.afkanerd.deku.DefaultSMS.Extensions

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.database.Cursor
import android.provider.Telephony
import androidx.activity.ComponentActivity
import androidx.compose.ui.autofill.ContentType
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.afkanerd.deku.DefaultSMS.Models.MmsHandler
import com.google.gson.GsonBuilder
import kotlin.text.insert

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

/**
 *
 * MMS
 * _id, thread_id, date, date_sent, msg_box, read, m_id, sub, sub_cs, ct_t, ct_l, exp,
 * m_cls, m_type, v, m_size, pri, rr, rpt_a, resp_st, st, tr_id, retr_st, retr_txt,
 * retr_txt_cs, read_status, ct_cls, resp_txt, d_tm, d_rpt, locked, sub_id, seen, creator,
 * text_only
 *
 *
 * MMS/Part
 * _id, mid, seq, ct, name, chset, cd, fn, cid, cl, ctt_s, ctt_t, _data, text, sub_id
 *
 *
 * SMS
 * _id, thread_id, address, person, date, date_sent, protocol, read, status, type,
 * reply_path_present, subject, body, service_center, locked, sub_id, error_code,
 * creator, seen
 *
 */

fun Context.exportRawWithColumnGuesses(): String {
    val mmsContents = arrayListOf<MmsHandler.MmsContentDataClass>()
    val mmsPartsContents = arrayListOf<MmsHandler.MmsPartContents>()
    val smsContents = arrayListOf<MmsHandler.SmsContentDataClass>()

    // MMS
    contentResolver.query(
        Telephony.Mms.CONTENT_URI,
        null,
        null,
        null,
        null
    )?.let { cursor ->
        if(cursor.moveToFirst()) {
            do {
                mmsContents.add(parseRawMmsContents(cursor))
            } while(cursor.moveToNext())
        }
        cursor.close()
    }

    // MMS/Parts
    contentResolver.query(
        "content://mms/part".toUri(),
        null,
        null,
        null,
        null
    )?.let { cursor ->
        if(cursor.moveToFirst()) {
            do {
                mmsPartsContents.add(parseRawMmsContentsParts(cursor))
            } while(cursor.moveToNext())
        }
    }


    // SMS
    contentResolver.query(
        Telephony.Sms.CONTENT_URI,
        null,
        null,
        null,
        null
    )?.let { cursor ->
        if(cursor.moveToFirst()) {
            do {
                smsContents.add(parseRawSmsContents(cursor))
            } while(cursor.moveToNext())
        }
        cursor.close()
    }

    val smsMmsContents = MmsHandler.SmsMmsContents(
        mapOf(
            Pair(
                Telephony.Mms.CONTENT_URI.toString(),
                mmsContents
            )
        ),

        mapOf(Pair("content://mms/part", mmsPartsContents)),

        mapOf(
            Pair(
                Telephony.Sms.CONTENT_URI.toString(),
                smsContents
            )
        ),
    )

    val gson = GsonBuilder()
        .serializeNulls()
        .setPrettyPrinting()
        .create()
    return gson.toJson(smsMmsContents)
}

@SuppressLint("Range")
private fun parseRawMmsContentsParts(cursor: Cursor): MmsHandler.MmsPartContents {
    val _id: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.Part._ID))
    val mid: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.Part.MSG_ID))
    val seq: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.Part.SEQ))
    val ct: String? = cursor.getStringOrNull(cursor
        .getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE))
    val name: String? = cursor.getStringOrNull(cursor
        .getColumnIndex(Telephony.Mms.Part.NAME))
    val cid: String? = cursor.getStringOrNull(cursor
        .getColumnIndex(Telephony.Mms.Part.CONTENT_ID))
    val cl: String? = cursor.getStringOrNull(cursor
        .getColumnIndex(Telephony.Mms.Part.CONTENT_ID))
    val text: String? = cursor.getStringOrNull(cursor
        .getColumnIndex(Telephony.Mms.Part.TEXT))
    val sub_id: Int = cursor.getInt(cursor
        .getColumnIndex("sub_id"))
    val _data: String? = cursor.getStringOrNull(cursor
        .getColumnIndex(Telephony.Mms.Part._DATA))
    val chset: Int? = cursor.getIntOrNull(cursor
        .getColumnIndex(Telephony.Mms.Part.CHARSET))

    return MmsHandler.MmsPartContents(
        _id = _id,
        mid = mid,
        seq = seq,
        ct = ct,
        name = name,
        cid = cid,
        cl = cl,
        text = text,
        sub_id = sub_id,
        _data = _data,
        chset = chset,
    )
}

@SuppressLint("Range")
private fun parseRawMmsContents(cursor: Cursor): MmsHandler.MmsContentDataClass {
    val _id: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms._ID))
    val thread_id: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.THREAD_ID))
    val date: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.DATE))
    val date_sent: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.DATE_SENT))
    val msg_box: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.MESSAGE_BOX))
    val read: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.READ))
    val m_id: String? = cursor.getStringOrNull(cursor
        .getColumnIndex(Telephony.Mms.MESSAGE_ID))
    val sub: String? = cursor.getStringOrNull(cursor
        .getColumnIndex(Telephony.Mms.SUBJECT))
    val sub_cs: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.SUBJECT_CHARSET))
    val ct_t: String? = cursor.getStringOrNull(cursor
        .getColumnIndex(Telephony.Mms.CONTENT_TYPE))
    val ct_l: String? = cursor.getStringOrNull(cursor
        .getColumnIndex(Telephony.Mms.CONTENT_LOCATION))
    val m_cls: String? = cursor.getStringOrNull(cursor
        .getColumnIndex(Telephony.Mms.MESSAGE_CLASS))
    val m_type: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.MESSAGE_TYPE))
    val v: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.MMS_VERSION))
    val m_size: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.MESSAGE_SIZE))
    val pri: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.PRIORITY))
    val rr: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.READ_REPORT))
    val d_rpt: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.DELIVERY_REPORT))
    val locked: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.LOCKED))
    val sub_id: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.SUBSCRIPTION_ID))
    val seen: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.SEEN))
    val creator: String? = cursor.getStringOrNull(cursor
        .getColumnIndex(Telephony.Mms.CREATOR))
    val text_only: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.TEXT_ONLY))

    return MmsHandler.MmsContentDataClass(
        _id = _id,
        thread_id = thread_id,
        date = date,
        date_sent = date_sent,
        msg_box = msg_box,
        read = read,
        m_id = m_id,
        sub = sub,
        sub_cs = sub_cs,
        ct_t = ct_t,
        ct_l = ct_l,
        m_cls = m_cls,
        m_type = m_type,
        v = v,
        m_size = m_size,
        pri = pri,
        rr = rr,
        d_rpt = d_rpt,
        locked = locked,
        sub_id = sub_id,
        seen = seen,
        creator = creator,
        text_only = text_only
    )
}

@SuppressLint("Range")
private fun parseRawSmsContents(cursor: Cursor): MmsHandler.SmsContentDataClass {
    val _id: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Sms._ID))
    val thread_id: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Sms.THREAD_ID))
    val address: String? = cursor.getString(cursor
        .getColumnIndex(Telephony.Sms.ADDRESS))
    val date: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Sms.DATE))
    val date_sent: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Sms.DATE_SENT))
    val read: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Sms.READ))
    val status: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Sms.STATUS))
    val type: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Sms.TYPE))
    val body: String = cursor.getString(cursor
        .getColumnIndex(Telephony.Sms.BODY))
    val locked: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Sms.LOCKED))
    val sub_id: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID))
    val error_code: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Sms.ERROR_CODE))
    val creator: String = cursor.getString(cursor
        .getColumnIndex(Telephony.Sms.CREATOR))
    val seen: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Sms.SEEN))

    return MmsHandler.SmsContentDataClass(
        _id = _id,
        thread_id = thread_id,
        address = address,
        date = date,
        date_sent = date_sent,
        read = read,
        status = status,
        type = type,
        body = body,
        locked = locked,
        sub_id = sub_id,
        error_code = error_code,
        creator = creator,
        seen = seen
    )
}

data class SmsMmsImportDetails(var mmsCount: Int, var mmsPartCount: Int)

fun Context.importRawColumnGuesses(data: String): SmsMmsImportDetails {
    val gson = GsonBuilder()
        .serializeNulls()
        .setPrettyPrinting()
        .create()
    val smsMmsContents = gson.fromJson(data, MmsHandler.SmsMmsContents::class.java)

    var mmsCount = 0
    var mmsPartCount = 0

    // MMS imports
    val mmsUri = smsMmsContents.mms.keys.first()
    smsMmsContents.mms[mmsUri]?.forEach {
        if(contentResolver.query(
                mmsUri.toUri(),
                arrayOf("_id"),
                "${Telephony.Mms._ID}=?",
                arrayOf("${it._id}"),
                null
            ) == null) {
            val values = getMmsInputValues(it)
            val uri = contentResolver.insert(mmsUri.toUri(), values)
            mmsCount += 1
        }
    }

    // MMS/Part imports
    val mmsPartsUri = smsMmsContents.mms_parts.keys.first()
    smsMmsContents.mms_parts[mmsPartsUri]?.forEach {
        if(contentResolver.query(
            mmsUri.toUri(),
            arrayOf("_id"),
            "${Telephony.Mms.Part._ID}=? AND ${Telephony.Mms.Part.MSG_ID}=?",
            arrayOf("${it._id}", "${it.mid}"),
            null
        ) == null) {
            val values = getMmsPartInputValues(it)
            val uri = contentResolver.insert(mmsUri.toUri(), values)
            mmsPartCount += 1
        }
    }

    return SmsMmsImportDetails(mmsCount, mmsPartCount)
}

private fun getMmsPartInputValues(mmsPartContent: MmsHandler.MmsPartContents) : ContentValues {
    return ContentValues().apply {
        put("_data", mmsPartContent._data)
        put("_id", mmsPartContent._id)
        put("cd", mmsPartContent.cd)
        put("chset", mmsPartContent.chset)
        put("cid", mmsPartContent.cid)
        put("cl", mmsPartContent.cl)
        put("ct", mmsPartContent.ct)
        put("ctt_s", mmsPartContent.ctt_s)
        put("ctt_t", mmsPartContent.ctt_t)
        put("fn", mmsPartContent.fn)
        put("mid", mmsPartContent.mid)
        put("name", mmsPartContent.name)
        put("seq", mmsPartContent.seq)
        put("sub_id", mmsPartContent.sub_id)
        put("text", mmsPartContent.text)
    }
}

private fun getMmsInputValues(mmsContent: MmsHandler.MmsContentDataClass) : ContentValues {
    return ContentValues().apply {
        put(Telephony.Mms._ID, mmsContent._id)
        put(Telephony.Mms.CREATOR, mmsContent.creator)
        put("ct_cls", mmsContent.ct_cls)
        put("ct_l", mmsContent.ct_l)
        put("ct_t", mmsContent.ct_t)
        put("d_rpt", mmsContent.d_rpt)
        put("d_tm", mmsContent.d_tm)
        put("date", mmsContent.date)
        put("date_sent", mmsContent.date_sent)
        put("exp", mmsContent.exp)
        put("locked", mmsContent.locked)
        put("m_cls", mmsContent.m_cls)
        put("m_id", mmsContent.m_id)
        put("m_size", mmsContent.m_size)
        put("m_type", mmsContent.m_type)
        put("msg_box", mmsContent.msg_box)
        put("pri", mmsContent.pri)
        put("read", mmsContent.read)
        put("read_status", mmsContent.read_status)
        put("resp_st", mmsContent.resp_st)
        put("resp_txt", mmsContent.resp_txt)
        put("retr_st", mmsContent.retr_st)
        put("retr_txt", mmsContent.retr_txt)
        put("retr_txt_cs", mmsContent.retr_txt_cs)
        put("rpt_a", mmsContent.rpt_a)
        put("rr", mmsContent.rr)
        put("seen", mmsContent.seen)
        put("st", mmsContent.st)
        put("sub", mmsContent.sub)
        put("sub_cs", mmsContent.sub_cs)
        put("sub_id", mmsContent.sub_id)
        put("text_only", mmsContent.text_only)
        put("thread_id", mmsContent.thread_id)
        put("tr_id", mmsContent.tr_id)
        put("v", mmsContent.v)
    }
}

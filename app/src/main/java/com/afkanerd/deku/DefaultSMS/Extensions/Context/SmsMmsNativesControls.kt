package com.afkanerd.deku.DefaultSMS.Extensions.Context

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.widget.Toast
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.SmsMmsNatives
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

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
    val mmsContents = arrayListOf<SmsMmsNatives.Mms>()
    val mmsAddrContents = arrayListOf<SmsMmsNatives.MmsAddr>()
    val mmsPartsContents = arrayListOf<SmsMmsNatives.MmsPart>()
    val smsContents = arrayListOf<SmsMmsNatives.Sms>()

    val mmsIds = mutableSetOf<Long>()

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
                mmsContents.add(parseRawMmsContents(cursor).apply {
                    mmsIds.add(this._id)
                })
            } while(cursor.moveToNext())
        }
        cursor.close()
    }

    // MMSAddr
    mmsIds.forEach {
        contentResolver.query(
            "content://mms/${it}/addr".toUri(),
            null,
            null,
            null,
            null
        )?.let { cursor ->
            if(cursor.moveToFirst()) {
                do {
                    mmsAddrContents.add(parseRawMmsAddrContentsParts(cursor))
                } while(cursor.moveToNext())
            }
            cursor.close()
        }
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

    val smsMmsContents = SmsMmsNatives.SmsMmsContents(
        mapOf(
            Pair(
                Telephony.Mms.CONTENT_URI.toString(),
                mmsContents
            )
        ),

        mapOf(Pair("content://mms/{_id}/addr", mmsAddrContents)),
        mapOf(Pair("content://mms/part/{_id}", mmsPartsContents)),

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
private fun parseRawMmsAddrContentsParts(cursor: Cursor): SmsMmsNatives.MmsAddr {
    TODO("Implement parsers")
//    val _id: Int = cursor.getInt(cursor
//        .getColumnIndex(Telephony.Mms.Addr._ID))
//    val msg_id : String? = cursor.getStringOrNull(cursor
//        .getColumnIndex(Telephony.Mms.Addr.MSG_ID))
//    val contact_id: String? = cursor.getStringOrNull(cursor
//        .getColumnIndex(Telephony.Mms.Addr.CONTACT_ID))
//    val address: String? = cursor.getStringOrNull(cursor
//        .getColumnIndex(Telephony.Mms.Addr.ADDRESS))
//    val type: String? = cursor.getStringOrNull(cursor
//        .getColumnIndex(Telephony.Mms.Addr.TYPE))
//    val charset: String? = cursor.getStringOrNull(cursor
//        .getColumnIndex(Telephony.Mms.Addr.CHARSET))
//    val sub_id: Int? = cursor.getIntOrNull(cursor
//        .getColumnIndex("sub_id"))
//
//    return smsMmsNatives.MmsAddr(
//        _id = _id,
//        msg_id = msg_id,
//        contact_id = contact_id,
//        address = address,
//        type = type,
//        charset = charset,
//        sub_id = sub_id
//    )
}

@SuppressLint("Range")
private fun parseRawMmsContentsParts(cursor: Cursor): SmsMmsNatives.MmsPart {
    TODO("Implement parsers")
//    val _id: Int = cursor.getInt(cursor
//        .getColumnIndex(Telephony.Mms.Part._ID))
//    val mid: Int = cursor.getInt(cursor
//        .getColumnIndex(Telephony.Mms.Part.MSG_ID))
//    val seq: Int = cursor.getInt(cursor
//        .getColumnIndex(Telephony.Mms.Part.SEQ))
//    val ct: String? = cursor.getStringOrNull(cursor
//        .getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE))
//    val name: String? = cursor.getStringOrNull(cursor
//        .getColumnIndex(Telephony.Mms.Part.NAME))
//    val cid: String? = cursor.getStringOrNull(cursor
//        .getColumnIndex(Telephony.Mms.Part.CONTENT_ID))
//    val cl: String? = cursor.getStringOrNull(cursor
//        .getColumnIndex(Telephony.Mms.Part.CONTENT_ID))
//    val text: String? = cursor.getStringOrNull(cursor
//        .getColumnIndex(Telephony.Mms.Part.TEXT))
//    val sub_id: Int = cursor.getInt(cursor
//        .getColumnIndex("sub_id"))
//    val _data: String? = cursor.getStringOrNull(cursor
//        .getColumnIndex(Telephony.Mms.Part._DATA))
//    val chset: Int? = cursor.getIntOrNull(cursor
//        .getColumnIndex(Telephony.Mms.Part.CHARSET))
//
//    return smsMmsNatives.MmsPart(
//        _id = _id,
//        mid = mid,
//        seq = seq,
//        ct = ct,
//        name = name,
//        cid = cid,
//        cl = cl,
//        text = text,
//        sub_id = sub_id,
//        _data = _data,
//        chset = chset,
//    )
}

@SuppressLint("Range")
private fun parseRawMmsContents(cursor: Cursor): SmsMmsNatives.Mms {
    val _id: Long = cursor.getLong(cursor
        .getColumnIndex(Telephony.Mms._ID))
    val thread_id: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.THREAD_ID))
    val date: Long = cursor.getLong(cursor
        .getColumnIndex(Telephony.Mms.DATE))
    val date_sent: Long = cursor.getLong(cursor
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
    val sub_id: Long = cursor.getLong(cursor
        .getColumnIndex(Telephony.Mms.SUBSCRIPTION_ID))
    val seen: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.SEEN))
    val creator: String? = cursor.getStringOrNull(cursor
        .getColumnIndex(Telephony.Mms.CREATOR))
    val text_only: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.TEXT_ONLY))

    return SmsMmsNatives.Mms(
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
private fun parseRawSmsContents(cursor: Cursor): SmsMmsNatives.Sms {
    val _id: Long = cursor.getLong(cursor
        .getColumnIndex(Telephony.Sms._ID))
    val thread_id: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Sms.THREAD_ID))
    val address: String? = cursor.getString(cursor
        .getColumnIndex(Telephony.Sms.ADDRESS))
    val date: Long = cursor.getLong(cursor
        .getColumnIndex(Telephony.Sms.DATE))
    val date_sent: Long = cursor.getLong(cursor
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
    val sub_id: Long = cursor.getLong(cursor
        .getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID))
    val error_code: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Sms.ERROR_CODE))
    val creator: String = cursor.getString(cursor
        .getColumnIndex(Telephony.Sms.CREATOR))
    val seen: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Sms.SEEN))

    return SmsMmsNatives.Sms(
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

data class SmsMmsImportDetails(var mmsCount: Int, var mmsPartCount: Int, var mmsAddrCount: Int)

fun Context.importRawColumnGuesses(data: String): SmsMmsImportDetails {
    val gson = GsonBuilder()
        .serializeNulls()
        .create()
    val smsMmsContents = gson.fromJson(data, SmsMmsNatives.SmsMmsContents::class.java)

    var mmsCount = 0
    var mmsAddrCount = 0
    var mmsPartCount = 0

    // MMS imports
    val mmsUri = smsMmsContents.mms.keys.first()
    val mmsPartsUri = smsMmsContents.mms_parts.keys.first()
    val mmsAddrUri = smsMmsContents.mms_addr.keys.first()

    smsMmsContents.mms[mmsUri]?.forEach { mms ->
        contentResolver.query(
            mmsUri.toUri(),
            arrayOf("_id"),
            "${Telephony.Mms._ID}=?",
            arrayOf("${mms._id}"),
            null
        )?.let { cursor ->
            if(!cursor.moveToFirst()) {
                val values = getMmsInputValues(mms)
                try {
                    contentResolver.insert(mmsUri.toUri(), values)?.let { uri ->
                        smsMmsContents.mms_addr[mmsAddrUri]?.filter {
                            it.msg_id == mms._id.toString()}?.forEach {
                            insertMmsAddr("$uri/addr".toUri(), it)?.let {
                                mmsAddrCount += 1
                            }
                        }
                        smsMmsContents.mms_parts[mmsPartsUri]?.filter { it.mid == mms._id}?.forEach {
                            insertMmsPart("$uri/part".toUri(), it)?.let {
                                mmsPartCount += 1
                            }
                        }
                        mmsCount += 1
                        println()
                    }
                } catch(e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    return SmsMmsImportDetails(mmsCount, mmsPartCount, mmsAddrCount)
}

private fun Context.insertMmsAddr(uri: Uri, mmsPart: SmsMmsNatives.MmsAddr): Uri? {
    val values = getMmsAddrInputValues(mmsPart)
    return contentResolver.insert(uri, values)
}

private fun Context.insertMmsPart(uri: Uri, mmsPart: SmsMmsNatives.MmsPart): Uri? {
    val values = getMmsPartInputValues(mmsPart)
    val uri = contentResolver.insert(uri, values)
    uri?.let { uri ->
        if(mmsPart._data != null) {
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
//            val file = File(cacheDir, "data/user/0/com.afkanerd.deku/2.jpg")
//                val fileProvider = FileProvider.getUriForFile(
//                    this,
//                    BuildConfig.APPLICATION_ID + ".fileprovider",
//                    file
//                )
                inputStream = contentResolver.openInputStream(mmsPart._data!!.toUri())
                outputStream =contentResolver.openOutputStream(uri)

                if (inputStream != null && outputStream != null) {
                    inputStream.copyTo(outputStream)
                }
            } catch (e: Exception) {
                // Log the exception for debugging
                e.printStackTrace()
                println()
            } finally {
                // Ensure streams are always closed to prevent resource leaks
                inputStream?.close()
                outputStream?.close()
            }

        }
        else {
            if(mmsPart.ct == "text/plain") {
                println()
            }
        }
    }
    return uri
}

private fun getMmsAddrInputValues(mmsAddrContents: SmsMmsNatives.MmsAddr) : ContentValues {
    return ContentValues().apply {
//        put("_id", mmsAddrContents._id)
//        put(Telephony.Mms.Addr._ID, mmsAddrContents.msg_id)
        put(Telephony.Mms.Addr.CONTACT_ID, mmsAddrContents.contact_id)
        put(Telephony.Mms.Addr.ADDRESS, mmsAddrContents.address)
        put(Telephony.Mms.Addr.TYPE, mmsAddrContents.type)
        put(Telephony.Mms.Addr.CHARSET, mmsAddrContents.charset)
        put("sub_id", mmsAddrContents.sub_id)
    }
}

private fun getMmsPartInputValues(mmsPartContent: SmsMmsNatives.MmsPart) : ContentValues {
    return ContentValues().apply {
        put("mid", mmsPartContent.mid)
        put("cid", mmsPartContent.cid)
        put("cl", mmsPartContent.cl)
        put("ct", mmsPartContent.ct)
        put("chset", mmsPartContent.chset)
        put("text", mmsPartContent.text)
//        put("_data", mmsPartContent._data)
//        put("_id", mmsPartContent._id)
        put("cd", mmsPartContent.cd)
        put("ctt_s", mmsPartContent.ctt_s)
        put("ctt_t", mmsPartContent.ctt_t)
        put("fn", mmsPartContent.fn)
        put("name", mmsPartContent.name)
        put("seq", mmsPartContent.seq)
        put("sub_id", mmsPartContent.sub_id)
    }
}

private fun getMmsInputValues(mmsContent: SmsMmsNatives.Mms) : ContentValues {
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
//        put("sub_id", mmsContent.sub_id)
        put("text_only", mmsContent.text_only)
        put("thread_id", mmsContent.thread_id)
        put("tr_id", mmsContent.tr_id)
        put("v", mmsContent.v)
    }
}

@SuppressLint("Range")
fun Context.clearRawColumnGuesses() {
    contentResolver.query(
        "content://mms".toUri(),
        null,
        null,
        null,
        null)?.let { cursor ->
        if(cursor.moveToFirst()) {
            do {
                val _id = cursor.getInt(cursor.getColumnIndex("_id"))
                val deleted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    contentResolver.delete(
                        "content://mms/$_id/addr".toUri(), null)
                } else {
                    contentResolver.delete(
                        "content://mms/$_id/addr".toUri(), null, null)
                }
                println("Deleted: $deleted messages")
            } while(cursor.moveToNext())
            cursor.close()

            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    this@clearRawColumnGuesses,
                    "content://mms/{_id}/addr cleared!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    arrayOf(
        "content://mms",
        "content://mms/part",
    ).forEach { uri ->
        contentResolver.delete(uri.toUri(), null, null)
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(
                this@clearRawColumnGuesses,
                "$uri cleared!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}


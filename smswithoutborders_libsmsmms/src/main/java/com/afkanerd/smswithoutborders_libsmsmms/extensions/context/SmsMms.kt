package com.afkanerd.smswithoutborders_libsmsmms.extensions.context

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.MmsParser
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.SmsMmsNatives
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.receivers.MmsSentReceiverImpl
import com.afkanerd.smswithoutborders_libsmsmms.receivers.SmsTextReceivedReceiver
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ConversationsViewModel
import com.google.gson.GsonBuilder
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.MessageFormat

object SmsMmsDataBroadcastActions {
    const val SMS_DATA_SENT_BROADCAST_INTENT = "SMS_DATA_SENT_BROADCAST_INTENT"
    const val SMS_DATA_DELIVERED_BROADCAST_INTENT = "SMS_DATA_DELIVERED_BROADCAST_INTENT"
}

@Throws
fun Context.sendData(
    data: ByteArray,
    threadId: String,
    address: String,
    subscriptionId: Long,
) {
    val conversation = Conversations(
        sms = SmsMmsNatives.Sms(
            _id = System.currentTimeMillis(),
            thread_id = threadId.toInt(),
            address = address,
            date = System.currentTimeMillis(),
            date_sent = 0,
            read = 1,
            status = Telephony.Sms.STATUS_PENDING,
            type = Telephony.Sms.MESSAGE_TYPE_OUTBOX,
            body = "",
            sub_id = subscriptionId,
        ), sms_data_ = data
    )

    try {
        ConversationsViewModel().addConversation(this, conversation)
    } catch (e: Exception) {
        throw e
    }

    val address = makeE16PhoneNumber(address)
    val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getSystemService(SmsManager::class.java)
            .createForSubscriptionId(subscriptionId.toInt())
    } else {
        SmsManager.getSmsManagerForSubscriptionId( subscriptionId.toInt())
    }

    val sentIntent = Intent(SmsMmsDataBroadcastActions.SMS_DATA_SENT_BROADCAST_INTENT)
    sentIntent.setPackage(packageName)
    sentIntent.putExtra("id", conversation.sms!!._id)

    val deliveredIntent = Intent(SmsMmsDataBroadcastActions
        .SMS_DATA_DELIVERED_BROADCAST_INTENT)
    deliveredIntent.setPackage(packageName)
    deliveredIntent.putExtra("id", conversation.sms!!._id)

    val sentPendingIntent = PendingIntent.getBroadcast(
        this,
        conversation.sms!!._id!!.toInt(),
        sentIntent,
        PendingIntent.FLAG_IMMUTABLE
    )

    val deliveredPendingIntent = PendingIntent.getBroadcast(
        this,
        conversation.sms!!._id!!.toInt(),
        deliveredIntent,
        PendingIntent.FLAG_IMMUTABLE
    )

    val dataTransmissionPort: Short = 8200
    try {
        smsManager.sendDataMessage(
            address,
            null,
            dataTransmissionPort,
            data,
            sentPendingIntent,
            deliveredPendingIntent
        )
    } catch (e: Exception) {
        throw e
    }
}

@Throws
fun Context.sendSms(
    text: String,
    address: String,
    threadId: Int,
    subscriptionId: Long,
): Conversations {
    val address = makeE16PhoneNumber(address)

    val id = System.currentTimeMillis()
    val date = System.currentTimeMillis()
    val conversation = Conversations(sms = SmsMmsNatives.Sms(
        _id = id,
        thread_id = threadId,
        address = address,
        date = date,
        date_sent = date,
        read = 1,
        status = Telephony.Sms.STATUS_PENDING,
        type = Telephony.Sms.MESSAGE_TYPE_OUTBOX,
        body = text,
        sub_id = subscriptionId,
    ))

    try {
        ConversationsViewModel().addConversation(this, conversation)
    } catch (e: Exception) {
        throw e
    }

    val settings = Settings()
    settings.subscriptionId = subscriptionId.toInt()
    settings.group = false
    settings.deliveryReports = true
    settings.useSystemSending = true

    val message = Message()
    message.text = text
    message.addresses = arrayOf(address)

    val transaction = Transaction(this, settings)
    transaction.setExplicitBroadcastForSentSms(
        Intent(this, SmsTextReceivedReceiver::class.java).apply {
            action = SmsTextReceivedReceiver.SMS_SENT_BROADCAST_INTENT
            this.putExtra("id", id)
            this.putExtra("address", address)
            this.putExtra("thread_id", threadId)
            this.putExtra("sub_id", subscriptionId)
        }
    )
    transaction.setExplicitBroadcastForDeliveredSms(
        Intent(this, SmsTextReceivedReceiver::class.java).apply {
            action = SmsTextReceivedReceiver.SMS_DELIVERED_BROADCAST_INTENT
            this.putExtra("id", id)
            this.putExtra("address", address)
            this.putExtra("thread_id", threadId)
            this.putExtra("sub_id", subscriptionId)
        }
    )
    transaction.sendNewMessage(message)
    return conversation
}

@Throws
fun Context.sendMms(
    contentUri: Uri,
    text: String,
    address: String,
    threadId: Int,
    subscriptionId: Long,
): Conversations {
    val address = makeE16PhoneNumber(address)
    val conversation = Conversations(
        mms = SmsMmsNatives.Mms(
            _id = System.currentTimeMillis(),
            thread_id = threadId.toInt(),
            date = System.currentTimeMillis(),
            date_sent = 0,
            msg_box = Telephony.Mms.MESSAGE_BOX_OUTBOX,
            read = 1,
            sub_id = subscriptionId,
            seen = 1,
        ),
        mms_text = text,
        mms_content_uri = contentUri.toString(),
        mms_mimetype = contentResolver.getType(contentUri),
        mms_filename = MmsParser.getFileName(this, contentUri),
    )

    try {
        ConversationsViewModel().addConversation(this, conversation)
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }

    val sendSettings = MmsParser.getSendMessageSettings()
    sendSettings.subscriptionId = subscriptionId.toInt()

    val intent = Intent(this, MmsSentReceiverImpl::class.java)
        .apply {
            this.putExtra(
                MmsSentReceiverImpl.EXTRA_ORIGINAL_RESENT_MESSAGE_ID,
                conversation.mms!!._id,
            )
        }

    val sendTransaction = Transaction(this, sendSettings)
    sendTransaction .setExplicitBroadcastForSentMms(intent)

    val mMessage = Message("", address)
    val mimeType = contentResolver.getType(contentUri)
    val filename = MmsParser.getFileName(this, contentUri)

    mMessage.addMedia(
        MmsParser.getBytesFromUri(this, contentUri),
        mimeType,
        filename
    )

    try {
        sendTransaction.sendNewMessage(mMessage)
    } catch(e: Exception) {
        e.printStackTrace()
    }

    return conversation
}

fun Context.loadRawSmsMmsDb() : List<Conversations>{
    val conversationsList = arrayListOf<Conversations>()

    // SMS
    contentResolver.query(
        Telephony.Sms.CONTENT_URI,
        null,
        null,
        null,
        "date asc"
    )?.let { cursor ->
        if(cursor.moveToFirst()) {
            do {
                parseRawSmsContents(cursor).let { it ->
                    conversationsList.add(Conversations(sms = it.apply {
                        this.thread_id = getThreadId(this.address!!)
                    }))
                }
            } while(cursor.moveToNext())
        }
        cursor.close()
    }

    contentResolver.query(
        Telephony.Mms.CONTENT_URI,
        null,
        null,
        null,
        "date asc"
    )?.let { cursor ->
        if(cursor.moveToFirst()) {
            do {
                parseRawMmsContents(cursor).let { mms ->
                    val conversation = Conversations(mms = mms).apply {
                        parseMms(cursor)?.let { parsedMms ->
                            if(parsedMms.address.isNullOrEmpty()) return@let
                            this.sms = SmsMmsNatives.Sms(
                                thread_id = getThreadId(parsedMms.address!!).toInt(),
                                address = parsedMms.address,
                                sub_id = mms.sub_id ?: -1,
                                date = mms.date,
                                date_sent = mms.date_sent,
                                type = mms.m_type!!,
                                status = mms.msg_box,
                                body = parsedMms.text ?: "",
                                read = mms.read ?: -1
                            )
                        }
                    }
                    if(conversation.sms != null) conversationsList.add(conversation)
                }
            } while(cursor.moveToNext())
            cursor.close()
        }
    }

    return conversationsList
}

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
    val _id: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.Addr._ID))
    val msg_id : String? = cursor.getStringOrNull(cursor
        .getColumnIndex(Telephony.Mms.Addr.MSG_ID))
    val contact_id: String? = cursor.getStringOrNull(cursor
        .getColumnIndex(Telephony.Mms.Addr.CONTACT_ID))
    val address: String? = cursor.getStringOrNull(cursor
        .getColumnIndex(Telephony.Mms.Addr.ADDRESS))
    val type: String? = cursor.getStringOrNull(cursor
        .getColumnIndex(Telephony.Mms.Addr.TYPE))
    val charset: String? = cursor.getStringOrNull(cursor
        .getColumnIndex(Telephony.Mms.Addr.CHARSET))
    val sub_id: Long? = cursor.getLongOrNull(cursor
        .getColumnIndex("sub_id"))

    return SmsMmsNatives.MmsAddr(
        _id = _id,
        msg_id = msg_id,
        contact_id = contact_id,
        address = address,
        type = type,
        charset = charset,
        sub_id = sub_id
    )
}

@SuppressLint("Range")
private fun parseRawMmsContentsParts(cursor: Cursor): SmsMmsNatives.MmsPart {
    val _id: Int = cursor.getInt(cursor
        .getColumnIndex(Telephony.Mms.Part._ID))
    val mid: Long = cursor.getLong(cursor
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
    val sub_id: Long? = cursor.getLongOrNull(cursor
        .getColumnIndex("sub_id"))
    val _data: String? = cursor.getStringOrNull(cursor
        .getColumnIndex(Telephony.Mms.Part._DATA))
    val chset: Int? = cursor.getIntOrNull(cursor
        .getColumnIndex(Telephony.Mms.Part.CHARSET))

    return SmsMmsNatives.MmsPart(
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
class ParsedMms{
    var mid: Int? = null
    var address: String? = null
    var content: ByteArray? = null
    var text: String? = null
    var mimeType: String? = null
    var filename: String? = null
    var contentUri: Uri? = null
}

@SuppressLint("Range")
fun Context.parseMms(cursor: Cursor): ParsedMms? {
    val uri = "content://mms/part".toUri()
    val idIndex = cursor.getColumnIndex("_id")
    if(idIndex == -1) return null

    val id = cursor.getString(idIndex)

    val mmsId = "mid = $id"
    val c = contentResolver
        .query(uri, null, mmsId, null, null)

    val parsedMms = ParsedMms().apply {
        this.mid = id.toInt()
    }

    if (c != null && c.moveToFirst()) {
        do {
            val _idIndex = c.getColumnIndex("_id")
            val pid = c.getString(_idIndex)

            val typeIndex = c.getColumnIndex("ct")

            val type = c.getString(typeIndex)

            if (parsedMms.address == null || parsedMms.address.isNullOrEmpty())
                parsedMms.address = getMmsAddr(id)

            if ("text/plain" == type) {
                if (parsedMms.text == null || parsedMms.text.isNullOrEmpty()) parsedMms.text =
                    c.getString(c.getColumnIndex("text"))
            } else if (parsedMms.content == null && (type != null && !type.isEmpty())) {
                if (type != "application/smil") {
                    parsedMms.content = getMmsContent(pid)
                    parsedMms.mimeType = type
                    parsedMms.contentUri = ("content://mms/part/$pid").toUri()
                } else {
//                    val text = c.getString(
//                        c.getColumnIndex(Telephony.Mms.Part.TEXT))
                }
            }
        } while (c.moveToNext())
        c.close()
    }

    return parsedMms
}

private fun Context.getMmsContent(id: String?): ByteArray {
    val uri = ("content://mms/part/$id").toUri()
    val outputStream = ByteArrayOutputStream()

    try {
        contentResolver.openInputStream(uri).use { inputStream ->
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

private fun Context.getMmsAddr(id: String): String {
    val sel = "msg_id=$id"
    val uriString = MessageFormat.format("content://mms/{0}/addr", id)
    val uri = uriString.toUri()
    val c = contentResolver.query(
        uri, null, sel, null, null)
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


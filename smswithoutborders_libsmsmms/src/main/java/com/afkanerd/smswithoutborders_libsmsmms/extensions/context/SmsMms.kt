package com.afkanerd.smswithoutborders_libsmsmms.extensions.context

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Base64
import android.widget.Toast
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.MmsParser
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.SmsMmsNatives
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.receivers.MmsSentReceiverImpl
import com.afkanerd.smswithoutborders_libsmsmms.receivers.SmsTextReceivedReceiver
import com.google.gson.GsonBuilder
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Throws
fun Context.updateMms(conversation: Conversations) {
    try {
        getDatabase().conversationsDao()?.update(conversation)
    } catch(e: Exception) {
        e.printStackTrace()
        throw e
    }
}

@Throws
fun Context.updateSms(uri: Uri, conversation: Conversations) {
    try {
        if(settingsGetStoreTelephonyDb)
            updateSmsToLocalDb(uri,conversation)
        getDatabase().conversationsDao()?.update(conversation)
    } catch(e: Exception) {
        e.printStackTrace()
        throw e
    }
}

@Throws
private fun Context.updateSmsToLocalDb(
    uri: Uri,
    conversations: Conversations
) {
    val contentValues = ContentValues().apply {
        put(Telephony.TextBasedSmsColumns.TYPE, conversations.sms?.type)
        put(Telephony.TextBasedSmsColumns.STATUS, conversations.sms?.status)
        put(Telephony.TextBasedSmsColumns.ERROR_CODE, conversations.sms?.error_code)
    }

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            contentResolver.update(uri, contentValues, null)
        } else {
            contentResolver.update(uri, contentValues, null, null)
        }
    } catch (e: Exception) {
        throw e
    }
}

@Throws
private fun Context.insertSmsTelephony(
    text: String?,
    sub_id: Long,
    address: String,
    date: Long,
    type: Int,
    read: Int,
): Uri? {
    val contentValues = ContentValues().apply {
        put(Telephony.TextBasedSmsColumns.BODY, text)
        put(Telephony.TextBasedSmsColumns.DATE, date)
        put(Telephony.TextBasedSmsColumns.TYPE, type)
        put(Telephony.TextBasedSmsColumns.ADDRESS, address)
        put(Telephony.TextBasedSmsColumns.SUBSCRIPTION_ID, sub_id)
        put(Telephony.TextBasedSmsColumns.READ, read)
        put(Telephony.TextBasedSmsColumns.STATUS, Telephony.TextBasedSmsColumns.STATUS_NONE)
    }

    try {
        return contentResolver.insert( Telephony.Sms.CONTENT_URI, contentValues)
    } catch (e: Exception) {
        throw e
    }
}

@Throws
fun Context.insertMms(conversation: Conversations) {
    try{
        getDatabase().conversationsDao()
            ?.insert(conversation, settingsGetKeepMessagesArchived)
            ?.let { id -> conversation.id = id }

    } catch (e: Exception) {
        throw e
    }
}

@Throws
fun Context.insertSms(conversation: Conversations): Uri? {
    var uri: Uri? = null
    if(settingsGetStoreTelephonyDb) {
        try {
            uri = insertSmsTelephony(
                conversation.sms?.body,
                conversation.sms?.sub_id!!,
                conversation.sms?.address!!,
                conversation.sms?.date!!,
                conversation.sms?.type!!,
                conversation.sms?.read!!
            )
        } catch(e: Exception) {
            throw e
        }
    }

    try{
        conversation.sms?._id = if(uri != null)
            getIdFromLocalDb(uri) else System.currentTimeMillis()

        getDatabase().conversationsDao()
            ?.insert(conversation, settingsGetKeepMessagesArchived)
            ?.let { id -> conversation.id = id }

    } catch (e: Exception) {
        throw e
    }
    return uri
}

@SuppressLint("Range")
fun Context.getIdFromLocalDb(uri: Uri): Long? {
    contentResolver.query(
        uri,
        null,
        null,
        null,
        null
    )?.let { cursor ->
        if(cursor.moveToFirst()) {
            val id = cursor.getLong(cursor
                .getColumnIndex(Telephony.Sms._ID))
            return id
        }
        cursor.close()
    }
    return null
}

@Throws
fun Context.sendSms(
    text: String,
    address: String,
    threadId: Int,
    subscriptionId: Long,
    data: ByteArray? = null,
): Conversations? {
    if(text.isEmpty() && data == null) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(this@sendSms,
                getString(R.string.text_body_cannot_empty), Toast.LENGTH_LONG).show()
        }
        return null
    }

    val address = makeE16PhoneNumber(address)

    val date = System.currentTimeMillis()

    var conversation: Conversations?

    try {
        conversation = Conversations(sms = SmsMmsNatives.Sms(
            thread_id = threadId,
            address = address,
            date = date,
            date_sent = date,
            read = 1,
            status = Telephony.Sms.STATUS_PENDING,
            type = Telephony.Sms.MESSAGE_TYPE_QUEUED,
            body = if(data == null) text else {
                Base64.encodeToString(data, Base64.DEFAULT)
            },
            sub_id = subscriptionId,
        ), sms_data = data)

        insertSms(conversation)?.let { uri ->
            val pendingIntents = getSmsPendingIntents(uri, conversation)

            sendSms(
                address = address,
                conversation = conversation,
                uri = uri,
                sentPendingIntent = pendingIntents.first,
                deliveredPendingIntent = if(settingsGetGetDeliveryReports)
                    pendingIntents.second else null,
            )
        }

    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }

    return conversation
}

@Throws
private fun Context.sendSms(
    address: String,
    conversation: Conversations,
    uri: Uri,
    sentPendingIntent: PendingIntent?,
    deliveredPendingIntent: PendingIntent?,
) {
    val dataTransmissionPort: Short = 8200
    val smsManager = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        getSystemService(SmsManager::class.java)
            .createForSubscriptionId(conversation.sms?.sub_id!!.toInt())
    else SmsManager.getSmsManagerForSubscriptionId(conversation.sms?.sub_id!!.toInt())

    try {
        if(conversation.sms_data != null) {
            smsManager.sendDataMessage(
                address,
                null,
                dataTransmissionPort,
                conversation.sms_data,
                sentPendingIntent,
                deliveredPendingIntent
            )
        } else {
            val dividedMessage = smsManager.divideMessage(conversation.sms?.body)
            if (dividedMessage.size < 2) smsManager.sendTextMessage(
                address,
                null,
                conversation.sms?.body,
                sentPendingIntent,
                deliveredPendingIntent
            )
            else {
                val sentPendingIntents = ArrayList<PendingIntent?>()
                val deliveredPendingIntents = ArrayList<PendingIntent?>()

                for (i in 0 until dividedMessage.size - 1) {
                    sentPendingIntents.add(null)
                    deliveredPendingIntents.add(null)
                }

                sentPendingIntents.add(sentPendingIntent)
                deliveredPendingIntents.add(deliveredPendingIntent)

                smsManager.sendMultipartTextMessage(
                    address,
                    null,
                    dividedMessage,
                    sentPendingIntents,
                    deliveredPendingIntents
                )
            }
        }

    } catch(e: Exception) {
        conversation.sms?.status = Telephony.Sms.STATUS_FAILED
        conversation.sms?.type = Telephony.Sms.MESSAGE_TYPE_FAILED
        updateSms(uri, conversation)
        throw e
    }
    conversation.sms?.type = Telephony.Sms.MESSAGE_TYPE_OUTBOX
    updateSms(uri, conversation)
}

private fun Context.getSmsPendingIntents(
    uri: Uri?,
    conversation: Conversations
): Pair<PendingIntent, PendingIntent> {
    val sentPendingIntent = PendingIntent.getBroadcast(
        this,
        conversation.id.toInt(),
        Intent(this, SmsTextReceivedReceiver::class.java).apply {
            setPackage(packageName)
            action = if(conversation.sms_data == null)
                SmsTextReceivedReceiver.SMS_SENT_BROADCAST_INTENT else
                    SmsTextReceivedReceiver.DATA_SENT_BROADCAST_INTENT

            this.putExtra("id", conversation.id)
            this.putExtra("address", conversation.sms?.address)
            this.putExtra("thread_id", conversation.sms?.thread_id)
            this.putExtra("sub_id", conversation.sms?.sub_id)
            this.putExtra("uri", uri?.toString())
        },
        PendingIntent.FLAG_IMMUTABLE
    )

    val deliveredPendingIntent = PendingIntent.getBroadcast(
        this,
        conversation.id.toInt(),
        Intent(this, SmsTextReceivedReceiver::class.java).apply {
            setPackage(packageName)
            action = if(conversation.sms_data == null)
                SmsTextReceivedReceiver.SMS_DELIVERED_BROADCAST_INTENT else
                    SmsTextReceivedReceiver.DATA_DELIVERED_BROADCAST_INTENT

            this.putExtra("id", conversation.id)
            this.putExtra("address", conversation.sms?.address)
            this.putExtra("thread_id", conversation.sms?.thread_id)
            this.putExtra("sub_id", conversation.sms?.sub_id)
            this.putExtra("uri", uri?.toString())
        },
        PendingIntent.FLAG_IMMUTABLE
    )

    return Pair(sentPendingIntent, deliveredPendingIntent)
}

@Throws
fun Context.sendMms(
    contentUri: Uri,
    text: String,
    address: String,
    threadId: Int,
    subscriptionId: Long,
    filename: String,
    mimeType: String
): Conversations? {

    val address = makeE16PhoneNumber(address)

    val conversation = Conversations(
        sms = SmsMmsNatives.Sms(
            _id = System.currentTimeMillis(),
            thread_id = threadId.toInt(),
            date = System.currentTimeMillis(),
            date_sent = 0,
            type = Telephony.Sms.MESSAGE_TYPE_QUEUED,
            status = Telephony.Sms.STATUS_PENDING,
            read = 1,
            sub_id = subscriptionId,
            address = address,
            body = null
        ),
        mms_text = text,
        mms_content_uri = contentUri.toString(),
        mms_mimetype = mimeType,
        mms_filename = filename,
    )

    try {
        insertMms(conversation)
        val sendSettings = MmsParser.getSendMessageSettings()
        sendSettings.subscriptionId = subscriptionId.toInt()

        val sendTransaction = Transaction(this, sendSettings)

        val intent = Intent(this, MmsSentReceiverImpl::class.java)
            .apply {
                this.putExtra(
                    MmsSentReceiverImpl.EXTRA_ORIGINAL_RESENT_MESSAGE_ID,
                    conversation.id,
                )
            }
        sendTransaction.setExplicitBroadcastForSentMms(intent)

        val mMessage = Message(text, address)
        mMessage.addMedia(
            getBytesFromUri(contentUri),
            mimeType,
            filename
        )

        try {
            sendTransaction.sendNewMessage(mMessage)
        } catch(e: Exception) {
            conversation.sms?.status = Telephony.Sms.STATUS_FAILED
            conversation.sms?.type = Telephony.Sms.MESSAGE_TYPE_FAILED
            updateMms(conversation)
            throw e
        }
        conversation.sms?.type = Telephony.Sms.MESSAGE_TYPE_OUTBOX
        updateMms(conversation)

    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
    return conversation
}


fun Context.registerIncomingSms(intent: Intent): Conversations {
    val bundle = intent.extras
    val subscriptionId = bundle!!.getInt("subscription", -1)
    var address: String? = ""
    val bodyBuffer = StringBuilder()
    val dataBuffer = ByteArrayOutputStream()
    var dateSent: Long = 0
    val date = System.currentTimeMillis()
    var status = -1

    for (currentSMS in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
        address = currentSMS.displayOriginatingAddress
        bodyBuffer.append(currentSMS.displayMessageBody)
        dataBuffer.write(currentSMS.userData)
        dateSent = currentSMS.timestampMillis
        status = currentSMS.status
    }
    val body = bodyBuffer.toString()

    // TODO: process encrypted message
    val conversation = Conversations(
        sms = SmsMmsNatives.Sms(
            body = body,
            sub_id = subscriptionId.toLong(),
            date = date,
            date_sent = dateSent,
            address = address!!,
            type = Telephony.Sms.MESSAGE_TYPE_INBOX,
            status = status,
            thread_id = getThreadId(address),
            read = 0,
        ),
        sms_data = if(bodyBuffer.isEmpty()) null else dataBuffer.toByteArray()
    )

    insertSms(conversation)
    return conversation
}


@Throws
fun Context.loadRawSmsMmsDb() : List<Conversations>{
    val conversationsList = arrayListOf<Conversations>()

    try {
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
                    parseRawSmsContents(cursor)?.let { it ->
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
                    val conversation = MmsParser.parse(this, cursor)
                    conversation?.sms?.let { conversationsList.add(conversation) }
                } while(cursor.moveToNext())
                cursor.close()
            }
        }
    } catch(e: Exception) {
        throw e
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
                parseRawSmsContents(cursor)?.let { smsContents.add(it) }
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
fun parseRawMmsContents(cursor: Cursor): SmsMmsNatives.Mms {
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
private fun parseRawSmsContents(cursor: Cursor): SmsMmsNatives.Sms? {
    val body: String = cursor.getStringOrNull(cursor
        .getColumnIndex(Telephony.Sms.BODY)) ?: return null

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

package com.afkanerd.deku.DefaultSMS.Extensions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.afkanerd.deku.DefaultSMS.BroadcastReceivers.SmsMmsActionsImpl
import com.afkanerd.deku.DefaultSMS.Models.Contacts
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.MmsHandler
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.MainActivity
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream


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
    val mmsAddrContents = arrayListOf<MmsHandler.MmsAddrContents>()
    val mmsPartsContents = arrayListOf<MmsHandler.MmsPartContents>()
    val smsContents = arrayListOf<MmsHandler.SmsContentDataClass>()

    val mmsIds = mutableSetOf<Int>()

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

    val smsMmsContents = MmsHandler.SmsMmsContents(
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
private fun parseRawMmsAddrContentsParts(cursor: Cursor): MmsHandler.MmsAddrContents {
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
    val sub_id: Int? = cursor.getIntOrNull(cursor
        .getColumnIndex("sub_id"))

    return MmsHandler.MmsAddrContents(
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

data class SmsMmsImportDetails(var mmsCount: Int, var mmsPartCount: Int, var mmsAddrCount: Int)

fun Context.importRawColumnGuesses(data: String): SmsMmsImportDetails {
    val gson = GsonBuilder()
        .serializeNulls()
        .create()
    val smsMmsContents = gson.fromJson(data, MmsHandler.SmsMmsContents::class.java)

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

private fun Context.insertMmsAddr(uri: Uri, mmsPart: MmsHandler.MmsAddrContents): Uri? {
    val values = getMmsAddrInputValues(mmsPart)
    return contentResolver.insert(uri, values)
}

private fun Context.insertMmsPart(uri: Uri, mmsPart: MmsHandler.MmsPartContents): Uri? {
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
                inputStream = contentResolver.openInputStream(mmsPart._data.toUri())
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

private fun getMmsAddrInputValues(mmsAddrContents: MmsHandler.MmsAddrContents) : ContentValues {
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

private fun getMmsPartInputValues(mmsPartContent: MmsHandler.MmsPartContents) : ContentValues {
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


val Context.NotificationReplyActionKey: String
    get() = "NOTIFICATION_REPLY_ACTION_KEY"

fun Context.notifyText(conversation: Conversation) {
    val contactName = Contacts.retrieveContactName(this, conversation.address)

    val user = Person.Builder()
        .setName(resources.getString(R.string.notification_title_reply_you))
        .build()

    val sender = Person.Builder()
        .setName(contactName ?: conversation.address!!)
        .setKey(conversation.thread_id)
        .setImportant(true)
        .build()

    val style = NotificationCompat.MessagingStyle(user)
        .addMessage(
            NotificationCompat.MessagingStyle.Message(
                conversation.text,
                System.currentTimeMillis(),
                sender
            )
        )
        .setGroupConversation(false)
        .setConversationTitle(contactName ?: conversation.address!!)

    val bubbleMetadata =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            NotificationCompat.BubbleMetadata
                .Builder(contactName ?: conversation.address!!)
                .setDesiredHeight(400)
                .build()
        } else {
            null
        }

    val shortcutInfoId = getShortcutInfoId(
        conversation,
        sender,
        contactName ?: conversation.address!!
    )

    val builder = NotificationCompat.Builder(
        this,
        getString(R.string.incoming_messages_channel_id))
//        .setContentTitle(contactName ?: conversation.address)
        .setWhen(System.currentTimeMillis())
        .setDefaults(Notification.DEFAULT_ALL)
        .setSmallIcon(R.drawable.ic_stat_name)
        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        .setAutoCancel(true)
        .setOnlyAlertOnce(true)
        .setAllowSystemGeneratedContextualActions(true)
        .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
        .setShortcutId(shortcutInfoId)
        .setBubbleMetadata(bubbleMetadata)
        .setContentIntent(getPendingIntent(conversation))
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .setStyle(style)
        .addAction(getNotificationReplyAction(conversation))
        .addAction(getNotificationMuteAction(conversation))
        .addAction(getNotificationMarkAsReadAction(conversation))


    with(NotificationManagerCompat.from(this)) {
        if (ActivityCompat.checkSelfPermission(
                this@notifyText,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            // ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            // public fun onRequestPermissionsResult(requestCode: Int, permissions: Array&lt;out String&gt;,
            //                                        grantResults: IntArray)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return@with
        }
        // notificationId is a unique int for each notification that you must define.
        notify(conversation.thread_id?.toInt() ?: 0, builder.build())
    }
}

private fun Context.getPendingIntent(conversation: Conversation): PendingIntent {
    val receivedSmsIntent = Intent(this, MainActivity::class.java)
    receivedSmsIntent.putExtra("address", conversation.address)
    receivedSmsIntent.putExtra("thread_id", conversation.thread_id)
    receivedSmsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

    return PendingIntent.getActivity(
        this,
        conversation.thread_id!!.toInt(),
        receivedSmsIntent,
        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
}

val Context.NotificationMarkAsReadActionIntentAction: String
    get() = "NOTIFICATION_MARK_AS_READ_ACTION_INTENT_ACTION"

private fun Context.getNotificationMarkAsReadAction(
    conversation: Conversation
): NotificationCompat.Action {
    val markAsReadLabel = resources.getString(R.string.notifications_mark_as_read_label)

    val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        conversation.thread_id?.toInt() ?: 0, // Or a unique request code
        Intent(
            this,
            SmsMmsActionsImpl::class.java
        ).apply {
            action = NotificationMarkAsReadActionIntentAction
            putExtra("address", conversation.address)
            putExtra("msg_id", conversation.message_id)
            putExtra("thread_id", conversation.thread_id)
        },
        PendingIntent.FLAG_MUTABLE // Flags for the PendingIntent
    )

    return NotificationCompat.Action.Builder(
        null, // Icon for the reply button
        markAsReadLabel, // Text for the reply button
        pendingIntent)
        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
        .build()
}

val Context.NotificationMuteActionIntentAction: String
    get() = "NOTIFICATION_MUTE_ACTION_INTENT_ACTION"

private fun Context.getNotificationMuteAction(conversation: Conversation): NotificationCompat.Action {
    val muteLabel = resources.getString(R.string.conversation_menu_muted_label)

    val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        conversation.thread_id?.toInt() ?: 0, // Or a unique request code
        Intent(
            this,
            SmsMmsActionsImpl::class.java
        ).apply {
            action = NotificationMuteActionIntentAction
            putExtra("address", conversation.address)
            putExtra("thread_id", conversation.thread_id)
        },
        PendingIntent.FLAG_MUTABLE // Flags for the PendingIntent
    )

    return NotificationCompat.Action.Builder(
        null, // Icon for the reply button
        muteLabel, // Text for the reply button
        pendingIntent)
        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MUTE)
        .build()
}

val Context.NotificationReplyActionIntentAction: String
    get() = "NOTIFICATION_REPLY_ACTION_INTENT_ACTION"

private fun Context.getNotificationReplyAction(conversation: Conversation): NotificationCompat.Action {
    val replyLabel = resources.getString(R.string.notifications_reply_label) // Label for the input field
    val remoteInput: RemoteInput = RemoteInput.Builder(NotificationReplyActionKey)
        .setLabel(replyLabel)
        .build()

    val replyPendingIntent: PendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        conversation.thread_id?.toInt() ?: 0, // Or a unique request code
        Intent(
            this,
            SmsMmsActionsImpl::class.java
        ).apply {
            action = NotificationReplyActionIntentAction
            putExtra("address", conversation.address)
            putExtra("thread_id", conversation.thread_id)
            putExtra("sub_id", conversation.subscription_id)
        },
        PendingIntent.FLAG_MUTABLE // Flags for the PendingIntent
    )


    return NotificationCompat.Action.Builder(
        null, // Icon for the reply button
        replyLabel, // Text for the reply button
        replyPendingIntent )
        .addRemoteInput(remoteInput)
        .setAllowGeneratedReplies(true)
        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
        .build()
}

private fun Context.getShortcutInfoId(
    conversation: Conversation,
    person: Person,
    contactName: String): String {

    val smsUrl = "smsto:${conversation.address}".toUri()
    val intent = Intent(Intent.ACTION_SENDTO, smsUrl)
    intent.putExtra(Conversation.THREAD_ID, conversation.thread_id)

    val shortcutInfoCompat = ShortcutInfoCompat.Builder( this, contactName )
        .setLongLived(true)
        .setIntent(intent)
        .setShortLabel(contactName)
        .setPerson(person)
        .build()

    ShortcutManagerCompat.pushDynamicShortcut(this, shortcutInfoCompat)
    return shortcutInfoCompat.id
}
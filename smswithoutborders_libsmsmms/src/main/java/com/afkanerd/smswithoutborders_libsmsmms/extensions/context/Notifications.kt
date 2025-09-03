package com.afkanerd.smswithoutborders_libsmsmms.extensions.context

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.net.toUri
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.receivers.SmsMmsActionsImpl
import com.afkanerd.smswithoutborders_libsmsmms.receivers.SmsTextReceivedReceiver.Companion.SMS_SENT_BROADCAST_INTENT_LIB
import com.google.gson.Gson
import java.util.Properties
import kotlin.jvm.java

fun Context.notify(
    conversation: Conversations,
    cls: Class<*>,
    self: Boolean = false,
    actions: Boolean = true,
    text: String? = null,
    title: String? = null,
) {
    if(actions) insertNotificationSessions(conversation, self)
    val builder = getNotificationBuilder(conversation, actions, cls)

    val address = conversation.sms!!.address!!
    val contactName = retrieveContactName(address)

    val user = Person.Builder()
        .setName(resources.getString(R.string.notification_title_reply_you))
        .build()

    val sender = Person.Builder()
        .setName(title ?: (contactName ?: conversation.sms?.address!!))
        .setKey(conversation.sms?.thread_id.toString())
        .setImportant(true)
        .build()

    val style = NotificationCompat.MessagingStyle(user)
    if(actions) {
        val messages = getNotificationSession(conversation.sms?.thread_id!!)
        messages?.sortedWith(compareBy {it.date})?.forEach {
            style.addMessage(
                (if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    NotificationCompat.MessagingStyle.Message(
                        text ?: it.text,
                        it.date,
                        if(it.self) user.name else contactName
                    )
                } else {
                    NotificationCompat.MessagingStyle.Message(
                        text ?: it.text,
                        it.date,
                        if(it.self) user else sender
                    )
                }) .apply {
                    if(!it.contentUri.isNullOrEmpty()) {
                        setData("image/*", it.contentUri.toUri())
                    }
                })
                .setGroupConversation(false)
                .setConversationTitle(title ?: (contactName ?: conversation.sms?.address!!))
        }
    } else {
        style.addMessage(
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                NotificationCompat.MessagingStyle.Message(
                    text ?: conversation.sms?.body,
                    System.currentTimeMillis(),
                    sender.name
                )
            } else {
                NotificationCompat.MessagingStyle.Message(
                    text ?: conversation.sms?.body,
                    System.currentTimeMillis(),
                    sender
                )
            }
        )
            .setGroupConversation(false)
            .setConversationTitle(title ?: (contactName ?: conversation.sms?.address!!))
    }
    builder.setStyle(style)

    with(NotificationManagerCompat.from(this)) {
        if (ActivityCompat.checkSelfPermission(
                this@notify,
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
        notify(conversation.sms?.thread_id ?: 0, builder.build())
    }
}

const val NotificationsDelAction = "NOTIFICATION_DEL_ACTION"
class NotificationsDelImpl: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        println(intent)
        intent?.let {
            if(intent.action == NotificationsDelAction) {
                val threadId = intent.getIntExtra("thread_id", -1)
                context?.cancelNotification(threadId)
            }
        }
    }
}

fun Context.getNotificationBuilder(
    conversation: Conversations,
    actions: Boolean,
    cls: Class<*>
): NotificationCompat.Builder {
    val contactName = retrieveContactName(conversation.sms!!.address!!)
    val sender = Person.Builder()
        .setName(contactName ?: conversation.sms?.address!!)
        .setKey(conversation.sms?.thread_id.toString())
        .setImportant(true)
        .build()

    val shortcutInfoId = getShortcutInfoId(
        conversation,
        sender,
        contactName ?: conversation.sms?.address!!
    )

    val bubbleMetadata =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            NotificationCompat.BubbleMetadata
                .Builder(contactName ?: conversation.sms?.address!!)
                .setDesiredHeight(400)
                .build()
        } else {
            null
        }

    return NotificationCompat.Builder(
        this,
        getString(R.string.incoming_messages_channel_id))
//        .setContentTitle(contactName ?: conversation.address)
        .setWhen(System.currentTimeMillis())
        .setDefaults(Notification.DEFAULT_ALL)
        .setSmallIcon(R.drawable.dekusms_icon_default)
        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        .setAutoCancel(true)
        .setOnlyAlertOnce(true)
        .setAllowSystemGeneratedContextualActions(settingsGetEnableContextReplies)
        .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
        .setShortcutId(shortcutInfoId)
        .setBubbleMetadata(bubbleMetadata)
        .setContentIntent(getPendingIntent(conversation, cls))
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .setDeleteIntent(
            PendingIntent.getBroadcast(
                this,
                conversation.sms?.thread_id!!,
                Intent(this, NotificationsDelImpl::class.java).apply {
                    action = NotificationsDelAction
                    putExtra("thread_id", conversation.sms?.thread_id)
                },
                PendingIntent.FLAG_MUTABLE
            )
        )
        .apply {
            if(actions) {
                if(!isShortCode(conversation.sms?.address!!)) {
                    addAction( getNotificationReplyAction(conversation))
                }
                addAction(getNotificationMuteAction(conversation))
                addAction(getNotificationMarkAsReadAction(conversation))
            }
        }
}

val Intent.NEW_NOTIFICATION_ACTION
    get() = "NEW_NOTIFICATION_ACTION"

private fun Context.getPendingIntent(
    conversation: Conversations,
    cls: Class<*>
): PendingIntent {
    val receivedSmsIntent = Intent(this, cls).apply {
        setPackage(packageName)
        action = NEW_NOTIFICATION_ACTION
        putExtra("address", conversation.sms?.address)
        putExtra("thread_id", conversation.sms?.thread_id)
        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }

    return PendingIntent.getActivity(
        this,
        conversation.sms?.thread_id!!,
        receivedSmsIntent,
        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
}

private fun Context.getNotificationMarkAsReadAction(
    conversation: Conversations
): NotificationCompat.Action {
    val markAsReadLabel = resources.getString(R.string.notifications_mark_as_read_label)

    val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
        this,
        conversation.sms?.thread_id ?: 0, // Or a unique request code
        Intent(
            this,
            SmsMmsActionsImpl::class.java
        ).apply {
            action = SmsMmsActionsImpl.NOTIFICATION_MARK_AS_READ_ACTION_INTENT_ACTION
            putExtra("id", conversation.sms?._id)
            putExtra("thread_id", conversation.sms?.thread_id)
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

private fun Context.getNotificationMuteAction(conversation: Conversations): NotificationCompat.Action {
    val muteLabel = resources.getString(R.string.conversation_menu_mute)

    val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        conversation.sms?.thread_id ?: 0, // Or a unique request code
        Intent(
            this,
            SmsMmsActionsImpl::class.java
        ).apply {
            action = SmsMmsActionsImpl.NOTIFICATION_MUTE_ACTION_INTENT_ACTION
            putExtra("address", conversation.sms?.address)
            putExtra( "thread_id", conversation.sms?.thread_id )
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

private fun Context.getNotificationReplyAction(
    conversation: Conversations
): NotificationCompat.Action {
    val replyLabel = resources.getString(R.string.notifications_reply_label) // Label for the input field
    val remoteInput: RemoteInput = RemoteInput
        .Builder(SmsMmsActionsImpl.NOTIFICATION_REPLY_ACTION_KEY)
        .setLabel(replyLabel)
        .build()

    val replyPendingIntent: PendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        conversation.sms?.thread_id ?: 0, // Or a unique request code
        Intent(
            this,
            SmsMmsActionsImpl::class.java
        ).apply {
            action = SmsMmsActionsImpl.NOTIFICATION_REPLY_ACTION_INTENT_ACTION
            putExtra("address", conversation.sms?.address)
            putExtra( "thread_id", conversation.sms?.thread_id )
            putExtra("sub_id", conversation.sms?.sub_id)
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
    conversation: Conversations,
    person: Person,
    contactName: String): String {

    val smsUrl = "smsto:${conversation.sms?.address}".toUri()
    val intent = Intent(Intent.ACTION_SENDTO, smsUrl)
    intent.putExtra("thread_id", conversation.sms?.thread_id)

    val shortcutInfoCompat = ShortcutInfoCompat.Builder( this, contactName )
        .setLongLived(true)
        .setIntent(intent)
        .setShortLabel(contactName)
        .setPerson(person)
        .build()

    ShortcutManagerCompat.pushDynamicShortcut(this, shortcutInfoCompat)
    return shortcutInfoCompat.id
}

data class IncomingNotificationSession(
    val address: String,
    val threadId: String,
    val text: String,
    val date: Long,
    val self: Boolean,
    val contentUri: String? = null,
)

private const val notificationSessionsFilename = "NOTIFICATIONS_SESSIONS"
private fun Context.insertNotificationSessions(conversation: Conversations, self: Boolean) {
    val sharedPreferences = getSharedPreferences(
        notificationSessionsFilename,
        Context.MODE_PRIVATE)

    with(sharedPreferences.edit()) {
        val sets = sharedPreferences.getStringSet(
            conversation.sms?.thread_id.toString(),
            mutableSetOf<String>())!!

        val newSets: MutableSet<String> = sets.toMutableSet()
        val notifSession = IncomingNotificationSession(
            address = conversation.sms?.address!!,
            threadId = conversation.sms?.thread_id!!.toString(),
            text = conversation.sms?.body!!,
            date = conversation.sms?.date ?: 0,
            self = self,
            contentUri = conversation.mms_content_uri
        )
        val gson = Gson().toJson(notifSession)
        newSets.add(gson)

        putStringSet(conversation.sms?.thread_id.toString(), newSets)
        apply()
    }
}

private fun Context.getNotificationSession(threadId: Int): List<IncomingNotificationSession>? {
    val sharedPreferences = getSharedPreferences(
        notificationSessionsFilename,
        Context.MODE_PRIVATE) ?: return null

    val sets = sharedPreferences.getStringSet(
        threadId.toString(),
        mutableSetOf<String>()) ?: return null

    val notifications = mutableListOf<IncomingNotificationSession>()
    sets.forEach {
        val gson = Gson()
        notifications.add(gson.fromJson(it, IncomingNotificationSession::class.java))
    }
    return notifications
}

fun Context.cancelNotification(threadId: Int) {
    val sharedPreferences = getSharedPreferences(
        notificationSessionsFilename,
        Context.MODE_PRIVATE) ?: return

    with(sharedPreferences.edit()) {
        remove(threadId.toString())
        apply()
    }

    NotificationManagerCompat.from(this).cancel(threadId)
}

enum class NotificationTxType {
    TEXT,
    DATA,
    MMS,
}

fun Context.sendNotificationBroadcast(
    conversation: Conversations,
    type: NotificationTxType,
    self: Boolean = false,
) {
    sendBroadcast(Intent(SMS_SENT_BROADCAST_INTENT_LIB).apply{
        putExtra("id", conversation.id)
        putExtra("self", self)
        putExtra("type", type.name)
        setPackage(packageName)
    })
}

@Throws
fun Context.getCustomizationProperties() : Properties {
    return try {
        Properties().apply {
            load(resources.openRawResource(R.raw.customization))
        }
    } catch(e: Exception) {
        throw e
    }
}

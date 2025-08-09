package com.afkanerd.deku.DefaultSMS.Extensions.Context

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
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
import com.afkanerd.deku.DefaultSMS.BroadcastReceivers.SmsMmsActionsImpl
import com.afkanerd.deku.DefaultSMS.Models.Contacts
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.MainActivity

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

package com.afkanerd.deku.DefaultSMS.Models

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.LocusId
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.os.Build
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat.getString
import androidx.core.content.LocusIdCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.afkanerd.deku.DefaultSMS.BroadcastReceivers.IncomingTextSMSReplyMuteActionBroadcastReceiver
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.DefaultSMS.ui.Components.ConvenientMethods

object Notifications {
    const val KEY_TEXT_REPLY = "KEY_TEXT_REPLY"

    fun createNotification(
        context: Context,
        title: String,
        text: String,
        address: String,
        requestCode: Int,
        contentIntent: Intent,
        replyIntent: Intent? = null,
        muteIntent: Intent? = null,
        markAsRead: Intent? = null,
    ) : NotificationCompat.Builder {
        val channelId = getString(context, R.string.incoming_messages_channel_id)

        var pendingIntent = PendingIntent
            .getActivity(
                context,
                requestCode,
                contentIntent,
                PendingIntent.FLAG_MUTABLE
            )

        // Build a PendingIntent for the reply action to trigger.
        var replyPendingIntent = if(replyIntent == null) null else
            PendingIntent.getBroadcast(
                context,
                requestCode,
                replyIntent,
                PendingIntent.FLAG_MUTABLE
            )

        var mutePendingIntent = if(muteIntent == null) null else
            PendingIntent.getBroadcast(
                context,
                requestCode,
                muteIntent,
                PendingIntent.FLAG_MUTABLE
            )

        var markAsReadPendingIntent = if(markAsRead == null) null else
            PendingIntent.getBroadcast(
                context,
                requestCode,
                markAsRead,
                PendingIntent.FLAG_MUTABLE
            )


        val replyLabel: String? = context.resources.getString(R.string.notifications_reply_label)
        var replyAction: NotificationCompat.Action? =
            if(replyPendingIntent == null || Helpers.isShortCode(address)) null else
                NotificationCompat.Action.Builder(
                    null,
                    getString(context, R.string.notifications_reply_label),
                    replyPendingIntent
                )
                    .addRemoteInput(
                        RemoteInput.Builder(
                            IncomingTextSMSReplyMuteActionBroadcastReceiver.KEY_TEXT_REPLY)
                            .setLabel(replyLabel)
                            .build()
                    )
                    .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                    .setShowsUserInterface(false)
                    .build()

        var muteAction: NotificationCompat.Action? = if(mutePendingIntent == null) null else
            NotificationCompat.Action.Builder(
                null,
                getString(context, R.string.conversation_menu_mute),
                mutePendingIntent
            )
                .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MUTE)
                .build()

        var markAsReadAction: NotificationCompat.Action? =
            if(markAsReadPendingIntent == null) null
            else NotificationCompat.Action.Builder(
                null,
                getString(context, R.string.notifications_mark_as_read_label),
                markAsReadPendingIntent
            ).setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ).build()

        val bitmap = Contacts.getContactBitmapPhoto(context, address)
        val icon = if(bitmap != null) {
            IconCompat.createWithBitmap(
                ConvenientMethods.getRoundedCornerImageBitmap(bitmap.asImageBitmap(), 100))
        } else {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM)
                IconCompat.createWithResource(context, R.drawable.baseline_account_circle_24)
            else null
        }

        val user = Person.Builder()
            .setIcon(icon)
            .setName(title)
            .setKey(address)
            .setBot(false)
            .build()

        val bubbleMetadata = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            NotificationCompat.BubbleMetadata.Builder(
                pendingIntent,
                icon ?:
                IconCompat.createWithResource(context, R.drawable.baseline_account_circle_24)
            )
                .setDesiredHeight(600)
                .build()
        else null

        val shortcut = ShortcutInfoCompat.Builder(context, address)
            .setIntent(contentIntent.apply {
                setAction(Intent.ACTION_DEFAULT)
            })
            .setCategories(setOf(ShortcutInfo.SHORTCUT_CATEGORY_CONVERSATION))
            .setShortLabel(user.name!!)
            .setLongLived(true)
            .setPerson(user)
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut);

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setColor(context.getColor(R.color.md_theme_primary))
            .setContentTitle(title)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)
            .setAllowSystemGeneratedContextualActions(true)
            .addAction(replyAction)
            .addAction(markAsReadAction)
            .addAction(muteAction)
            .setShortcutId(address)
            .setBubbleMetadata(bubbleMetadata)
            .setLocusId(
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    LocusIdCompat.toLocusIdCompat(LocusId(address))
                else null
            )
            .setStyle(
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    NotificationCompat.MessagingStyle(title)
                        .addMessage(text, System.currentTimeMillis(), address)
                } else {
//                    NotificationCompat.MessagingStyle(user)
//                        .addMessage(text, System.currentTimeMillis(), user)
                    NotificationCompat.MessagingStyle(user).addMessage(
                        NotificationCompat.MessagingStyle.Message(
                            text,
                            System.currentTimeMillis(),
                            user
                        )
                    )
                }
            )
    }

    fun cancel(context: Context, notificationId: Int) {
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                // ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                // public fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                //                                        grantResults: IntArray)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                return@with
            }
            // notificationId is a unique int for each notification that you must define.
            cancel(notificationId)
        }
    }

    fun notify(
        context: Context,
        builder: NotificationCompat.Builder,
        notificationId: Int
    ) {
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                // ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                // public fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                //                                        grantResults: IntArray)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                return@with
            }
            // notificationId is a unique int for each notification that you must define.
            notify(notificationId, builder.build())
        }
    }

    fun getPreviousNotifications(context: Context):
            NotificationCompat.MessagingStyle? {
        var messagingStyle: NotificationCompat.MessagingStyle? = null
        with(NotificationManagerCompat.from(context)) {
            val title = activeNotifications.first().notification.extras
                .getCharSequence(Notification.EXTRA_TITLE)
            val text = activeNotifications.first().notification.extras
                .getCharSequence(Notification.EXTRA_TEXT)
            val person = Person.Builder().setName(title).build()
            messagingStyle = NotificationCompat.MessagingStyle(person)
                .setConversationTitle(title)
                .addMessage(text, System.currentTimeMillis(), person)
        }
        return messagingStyle
    }
}
package com.afkanerd.deku.DefaultSMS.Models

import android.app.Activity.RESULT_OK
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.LocusId
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
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
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.preference.PreferenceManager
import com.afkanerd.deku.DefaultSMS.R

object Notifications {
    const val KEY_TEXT_REPLY = "KEY_TEXT_REPLY"

    fun createNotification(
        context: Context,
        title: String,
        text: String,
        address: String,
        requestCode: Int,
        contentIntent: Intent,
        replyIntent: Intent? = null
    ) : NotificationCompat.Builder {
        val channelId = getString(context, R.string.incoming_messages_channel_id)

        var replyLabel = getString(context, R.string.notifications_reply_label)
        var remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
            setLabel(replyLabel)
            build()
        }

        var pendingIntent = PendingIntent
            .getActivity(
                context,
                0,
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

        var action = if(replyPendingIntent == null) null else
            NotificationCompat.Action.Builder(
                null,
                getString(context, R.string.notifications_reply_label),
                replyPendingIntent
            )
            .addRemoteInput(remoteInput)
            .build()

        val bitmap = Contacts.getContactBitmapPhoto(context, address)
        val icon = if(bitmap != null) IconCompat.createWithBitmap(bitmap) else {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM)
                IconCompat.createWithResource(context, R.drawable.baseline_account_circle_24)
            else null
        }

        val user = Person.Builder()
            .setIcon(icon)
            .setName(title)
            .setKey(address)
            .setImportant(true)
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
            .setShortLabel(user.name!!)
            .setLongLived(true)
            .setPerson(user)
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut);

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(title)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)
            .setAllowSystemGeneratedContextualActions(true)
            .addAction(action)
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
                    NotificationCompat.MessagingStyle(user)
                        .setConversationTitle(title)
                        .addMessage(text, System.currentTimeMillis(), user)
                }
            )
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
}
package com.afkanerd.deku.DefaultSMS.Models

import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat.getString
import androidx.preference.PreferenceManager
import com.afkanerd.deku.DefaultSMS.R

object Notifications {
    const val KEY_TEXT_REPLY = "KEY_TEXT_REPLY"

    fun createNotification(
        context: Context,
        title: String,
        text: String,
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
                PendingIntent.FLAG_IMMUTABLE
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

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .addAction(action)
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
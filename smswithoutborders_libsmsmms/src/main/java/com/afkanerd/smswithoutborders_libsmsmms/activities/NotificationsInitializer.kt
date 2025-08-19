package com.afkanerd.smswithoutborders_libsmsmms.activities

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.afkanerd.smswithoutborders_libsmsmms.R

object NotificationsInitializer {
    fun create(context: Context): NotificationManager {
        val notificationManager: NotificationManager =
            context.getSystemService( NotificationManager::class.java )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context, notificationManager)
        }
        return notificationManager
    }

    var notificationsChannelIds: ArrayList<String> = ArrayList()
    var notificationsChannelNames: ArrayList<String> = ArrayList()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(context: Context, notificationManager: NotificationManager) {
        notificationsChannelIds.add(context.getString(R.string.incoming_messages_channel_id))
        notificationsChannelNames.add(context.getString(R.string.incoming_messages_channel_name))

        notificationsChannelIds.add(context.getString(R.string.running_gateway_clients_channel_id))
        notificationsChannelNames.add(context.getString(R.string.running_gateway_clients_channel_name))

        notificationsChannelIds.add(context.getString(R.string.foreground_service_failed_channel_id))
        notificationsChannelNames.add(context.getString(R.string.foreground_service_failed_channel_name))

        notificationsChannelIds.add(context.getString(R.string.message_failed_channel_id))
        notificationsChannelNames.add(context.getString(R.string.message_failed_channel_name))

        createNotificationChannelIncomingMessage(context, notificationManager)
        createNotificationChannelRunningGatewayListeners(context, notificationManager)
        createNotificationChannelReconnectGatewayListeners(context, notificationManager)
        createNotificationChannelFailedMessages(context, notificationManager)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannelIncomingMessage(
        context: Context,
        notificationManager: NotificationManager
    ) {
        val importance = NotificationManager.IMPORTANCE_HIGH

        val channel = NotificationChannel(
            notificationsChannelIds[0],
            notificationsChannelNames[0],
            importance
        )
        channel.description = context.getString(R.string.incoming_messages_channel_description)
        channel.enableLights(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        notificationManager.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannelRunningGatewayListeners(context: Context,
                                                                 notificationManager: NotificationManager
    ) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(
            notificationsChannelIds[1],
            notificationsChannelNames[1],
            importance
        )
        channel.description = context.getString(R.string.running_gateway_clients_channel_description)
        channel.lockscreenVisibility = Notification.DEFAULT_ALL

        notificationManager.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannelReconnectGatewayListeners(context: Context,
                                                                   notificationManager: NotificationManager
    ) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(
            notificationsChannelIds[2],
            notificationsChannelNames[2],
            importance
        )
        channel.description = context.getString(R.string.running_gateway_clients_channel_description)
        channel.lockscreenVisibility = Notification.DEFAULT_ALL
        notificationManager.createNotificationChannel(channel)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannelFailedMessages(context: Context,
                                                        notificationManager: NotificationManager
    ) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(
            notificationsChannelIds[3],
            notificationsChannelNames[3],
            importance
        )
        channel.description = context.getString(R.string.message_failed_notifications_descriptions)
        channel.lockscreenVisibility = Notification.DEFAULT_ALL
        notificationManager.createNotificationChannel(channel)
    }
}
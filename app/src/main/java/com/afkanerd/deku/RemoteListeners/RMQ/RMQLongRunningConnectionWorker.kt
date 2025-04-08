package com.afkanerd.deku.RemoteListeners.RMQ

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.provider.Settings.Global.getString
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.MainActivity
import com.google.common.util.concurrent.Service


class RMQLongRunningConnectionWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        setForeground(createForegroundNotification())
        while(true) {
            Thread.sleep(1000*10)
            break
        }
        return Result.success()
    }


    private fun createForegroundNotification() : ForegroundInfo{
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent
            .getActivity(applicationContext,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE)

        val title = "Long running..."
        val description = ""

        val notification =
            NotificationCompat.Builder( applicationContext,
                applicationContext.getString(R.string.running_gateway_clients_channel_id))
                .setContentTitle(title)
                .setContentText("Status")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSilent(true)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(description))
                .build()
                .apply {
                    flags = Notification.FLAG_ONGOING_EVENT
                }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(0, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(0, notification)
        }
    }
}

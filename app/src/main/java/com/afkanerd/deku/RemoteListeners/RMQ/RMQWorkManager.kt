package com.afkanerd.deku.RemoteListeners.RMQ

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.afkanerd.deku.RemoteListeners.Models.GatewayClient


class RMQWorkManager(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val gatewayClientId = inputData.getLong(GatewayClient.GATEWAY_CLIENT_ID, -1)
        RMQConnectionWorker(applicationContext, gatewayClientId).start()
        return Result.success()
    }
}

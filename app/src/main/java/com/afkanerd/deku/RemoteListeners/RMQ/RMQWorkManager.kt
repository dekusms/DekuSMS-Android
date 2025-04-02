package com.afkanerd.deku.RemoteListeners.RMQ

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.afkanerd.deku.RemoteListeners.Models.GatewayClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.TimeoutException


class RMQWorkManager(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val gatewayClientId = inputData.getLong(GatewayClient.GATEWAY_CLIENT_ID, -1)

        try {
            RMQConnectionWorker(applicationContext, gatewayClientId).start().let {
                if(!it.connection.isOpen) return Result.failure()
            }
        } catch(e: Exception) {
            e.printStackTrace()
            when(e) {
                is TimeoutException -> {
                    e.printStackTrace()
                    return Result.retry()
                }
                is IOException -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            applicationContext,
                            e.cause?.message ?: "",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return Result.failure()
                }
                else -> {
                    Log.e(javaClass.name, "Exception connecting rmq", e)
                    return Result.failure()
                }
            }
        }
        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
    }
}

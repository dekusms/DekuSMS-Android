package com.afkanerd.deku.RemoteListeners.RMQ

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.afkanerd.deku.RemoteListeners.Models.RemoteListeners
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException


class RMQWorkManager(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val remoteListenersId = inputData.getLong(RemoteListeners.GATEWAY_CLIENT_ID, -1)

        try {
            RMQConnectionWorker(applicationContext, remoteListenersId).start().let {
                if(!it.connection.isOpen) return Result.failure()
            }
        } catch(e: Exception) {
            e.printStackTrace()
            when(e) {
                is TimeoutException, is UnknownHostException -> {
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

package com.afkanerd.deku.RemoteListeners.Models

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.BuildConfig
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import com.afkanerd.deku.DefaultSMS.Models.SIMHandler
import com.afkanerd.deku.RemoteListeners.RMQ.RMQLongRunningConnectionWorker
import com.afkanerd.deku.RemoteListeners.RMQ.RMQWorkManager
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

object RemoteListenersHandler {
    const val UNIQUE_WORK_MANAGER_NAME = BuildConfig.APPLICATION_ID
    const val UNIQUE_WORK_MANAGER_TAG = BuildConfig.APPLICATION_ID + ".REMOTE_LISTENERS"

    fun getPublisherDetails(context: Context?, projectName: String): List<String> {
        val simcards = SIMHandler.getSimCardInformation(context)

        val operatorCountry = Helpers.getUserCountry(context)

        val operatorDetails: MutableList<String> = ArrayList()
        for (i in simcards.indices) {
            val mcc = simcards[i].mcc.toString()
            val _mnc = simcards[i].mnc
            val mnc = if (_mnc < 10) "0$_mnc" else _mnc.toString()
            val carrierId = mcc + mnc

            val publisherName = "$projectName.$operatorCountry.$carrierId"
            operatorDetails.add(publisherName)
        }

        return operatorDetails
    }

    fun generateUuidFromLong(input: Long): UUID {
        // Generate a UUID from the long by using the input directly
        // for the most significant bits and setting the least significant bits to 0.
        val mostSigBits = input
        val leastSigBits = 0L // You can modify this if you want to use more of the long

        return UUID(mostSigBits, leastSigBits)
    }

    fun stopListening(context: Context, remoteListener: GatewayClient) {
        CoroutineScope(Dispatchers.Default).launch {
            TODO("Stop any active connections first")
            Datastore.getDatastore(context).gatewayClientDAO().update(remoteListener)
            val workManager = WorkManager.getInstance(context)
            workManager.getWorkInfoById(generateUuidFromLong(remoteListener.id)).apply {
                cancel(true)
            }
        }
    }

    fun startListening(context: Context, gatewayClient: GatewayClient) {
        CoroutineScope(Dispatchers.Default).launch {
            Datastore.getDatastore(context).gatewayClientDAO().update(gatewayClient)
            if (gatewayClient.activated) startWorkManager(context, gatewayClient)
        }
    }

    fun getStatus(context: Context, remoteListener: GatewayClient) : ListenableFuture<WorkInfo?>{
        val workManager = WorkManager.getInstance(context)
        return workManager.getWorkInfoById(generateUuidFromLong(remoteListener.id))
    }

    /**
     * This would get queued up until the the constraints are met - once it can execute it is done
     * Don't use this for any long running metrics - just a constraints metrics
     */
    fun startWorkManager(context: Context, gatewayClient: GatewayClient) {
        val constraints : Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();

        val workManager = WorkManager.getInstance(context)

        val gatewayClientListenerWorker = OneTimeWorkRequestBuilder<RMQWorkManager>()
            .setConstraints(constraints)
            .setId(generateUuidFromLong(gatewayClient.id))
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .setInputData(Data.Builder()
                .putLong(GatewayClient.GATEWAY_CLIENT_ID, gatewayClient.id)
                .build()
            )
            .addTag(UNIQUE_WORK_MANAGER_TAG)
            .build();

        workManager.enqueueUniqueWork(
            "$UNIQUE_WORK_MANAGER_NAME.$gatewayClient.id",
            ExistingWorkPolicy.KEEP,
            gatewayClientListenerWorker
        )
    }
}

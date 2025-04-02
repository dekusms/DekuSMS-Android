package com.afkanerd.deku.RemoteListeners.Models

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.BuildConfig
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import com.afkanerd.deku.DefaultSMS.Models.SIMHandler
import com.afkanerd.deku.RemoteListeners.RMQ.RMQWorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.uuid.Uuid

class GatewayClientHandler(context: Context?) {
    var databaseConnector: Datastore = Datastore.getDatastore(context)

    @Throws(InterruptedException::class)
    fun add(gatewayClient: GatewayClient): Long {
        gatewayClient.date = System.currentTimeMillis()
        val id = longArrayOf(-1)
        val thread = Thread {
            val gatewayClientDAO = databaseConnector.gatewayClientDAO()
            id[0] = gatewayClientDAO.insert(gatewayClient)
        }
        thread.start()
        thread.join()

        return id[0]
    }

    @Throws(InterruptedException::class)
    fun delete(gatewayClient: GatewayClient) {
        gatewayClient.date = System.currentTimeMillis()
        val thread = Thread {
            val gatewayClientDAO = databaseConnector.gatewayClientDAO()
            gatewayClientDAO.delete(gatewayClient)
        }
        thread.start()
        thread.join()
    }

    @Throws(InterruptedException::class)
    fun update(gatewayClient: GatewayClient) {
        gatewayClient.date = System.currentTimeMillis()
        val thread = Thread {
            val gatewayClientDAO = databaseConnector.gatewayClientDAO()
            gatewayClientDAO.update(gatewayClient)
        }
        thread.start()
        thread.join()
    }

    @Throws(InterruptedException::class)
    fun fetch(id: Long): GatewayClient {
        val gatewayClient = arrayOf(GatewayClient())
        val thread = Thread {
            val gatewayClientDAO = databaseConnector.gatewayClientDAO()
            gatewayClient[0] = gatewayClientDAO.fetch(id)
        }
        thread.start()
        thread.join()

        return gatewayClient[0]
    }

    companion object {
        const val UNIQUE_WORK_MANAGER_NAME = BuildConfig.APPLICATION_ID
        const val UNIQUE_WORK_MANAGER_TAG = BuildConfig.APPLICATION_ID

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


        fun startWorkManager(context: Context, gatewayClient: GatewayClient) : WorkManager {
            val constraints : Constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            val workManager = WorkManager.getInstance(context)
            Log.d(javaClass.name, "WorkManager: ${gatewayClient.id}:${gatewayClient.hostUrl}")

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
                .addTag("$UNIQUE_WORK_MANAGER_TAG.$gatewayClient.id")
                .build();

            workManager.enqueueUniqueWork(
                "$UNIQUE_WORK_MANAGER_NAME.$gatewayClient.id",
                ExistingWorkPolicy.KEEP,
                gatewayClientListenerWorker
            )
            return workManager
        }
    }
}

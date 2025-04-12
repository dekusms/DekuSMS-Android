package com.afkanerd.deku.RemoteListeners.Models

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SubscriptionInfo
import android.widget.Toast
import androidx.core.content.PermissionChecker
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
import com.afkanerd.deku.RemoteListeners.RemoteListenerConnectionService
import com.afkanerd.deku.RemoteListeners.RMQ.RMQWorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

object RemoteListenersHandler {
    const val UNIQUE_WORK_MANAGER_NAME = BuildConfig.APPLICATION_ID
    const val UNIQUE_WORK_MANAGER_TAG = BuildConfig.APPLICATION_ID + ".REMOTE_LISTENERS"

    fun getPublisherDetails(context: Context?, projectName: String): List<String> {
        val operatorDetails: MutableList<String> = ArrayList()
        val simCards = SIMHandler.getSimCardInformation(context)

        val operatorCountry = Helpers.getUserCountry(context)
        simCards?.let {
            for (i in simCards.indices) {
                val mcc = simCards[i].mcc.toString()
                val _mnc = simCards[i].mnc
                val mnc = if (_mnc < 10) "0$_mnc" else _mnc.toString()
                val carrierId = mcc + mnc

                val publisherName = "$projectName.$operatorCountry.$carrierId"
                operatorDetails.add(publisherName)
            }

        }
        return operatorDetails
    }

    fun getCarrierId(subscriptionInformation: SubscriptionInfo) : Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            subscriptionInformation.carrierId
//            (subscriptionInformation.mccString + subscriptionInformation.mncString).toInt()
            subscriptionInformation.mncString?.toInt() ?: -1
        } else {
            "${subscriptionInformation.mnc}".toInt()
        }
    }

    fun generateUuidFromLong(input: Long): UUID {
        // Generate a UUID from the long by using the input directly
        // for the most significant bits and setting the least significant bits to 0.
        val mostSigBits = input
        val leastSigBits = 0L // You can modify this if you want to use more of the long

        return UUID(mostSigBits, leastSigBits)
    }

    fun stopListening(context: Context, remoteListener: RemoteListeners) {
        CoroutineScope(Dispatchers.Default).launch {
            Datastore.getDatastore(context).remoteListenerDAO().update(remoteListener)
            val workManager = WorkManager.getInstance(context)
            workManager.getWorkInfoById(generateUuidFromLong(remoteListener.id)).apply {
                cancel(true)
            }
        }
    }

    fun onOffAgain(context: Context, remoteListener: RemoteListeners) {
        if(remoteListener.activated) {
            remoteListener.activated = false
            Datastore.getDatastore(context).remoteListenerDAO().update(remoteListener)
            Thread.sleep(1000)

            remoteListener.activated = true
            Datastore.getDatastore(context).remoteListenerDAO().update(remoteListener)
        }
    }

    fun toggleRemoteListeners(context: Context, remoteListener: RemoteListeners? = null) {
        val gatewayClients = Datastore.getDatastore(context).remoteListenerDAO().all
        gatewayClients.forEach { it.activated = remoteListener?.id == it.id }
        Datastore.getDatastore(context).remoteListenerDAO().update(gatewayClients)
    }

    fun startListening(context: Context, remoteListener: RemoteListeners) {
        CoroutineScope(Dispatchers.Default).launch {
            toggleRemoteListeners(context, remoteListener)
            val intent = Intent(context, RemoteListenerConnectionService::class.java)
            launch(Dispatchers.Main) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }
    }

    /**
     * This would get queued up until the the constraints are met - once it can execute it is done
     * Don't use this for any long running metrics - just a constraints metrics
     */
    fun startWorkManager(context: Context, remoteListeners: RemoteListeners) {
        val constraints : Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();

        val workManager = WorkManager.getInstance(context)

        val remoteListenersListenerWorker = OneTimeWorkRequestBuilder<RMQWorkManager>()
            .setConstraints(constraints)
            .setId(generateUuidFromLong(remoteListeners.id))
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .setInputData(Data.Builder()
                .putLong(RemoteListeners.GATEWAY_CLIENT_ID, remoteListeners.id)
                .build()
            )
            .addTag(UNIQUE_WORK_MANAGER_TAG)
            .build();

        val operation = workManager.enqueueUniqueWork(
            "$UNIQUE_WORK_MANAGER_NAME.${remoteListeners.id}",
            ExistingWorkPolicy.REPLACE,
            remoteListenersListenerWorker
        )

        println(operation.state.value)
    }
}

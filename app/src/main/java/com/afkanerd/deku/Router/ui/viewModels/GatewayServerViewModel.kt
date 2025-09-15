package com.afkanerd.deku.Router.ui.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Database
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.Commons.Helpers.isBase64Encoded
import com.afkanerd.deku.Router.Models.RouterHandler
import com.afkanerd.deku.Router.Models.RouterHandler.getMessageIdsFromWorkManagers
import com.afkanerd.deku.Router.Models.RouterHandler.getTagForGatewayServers
import com.afkanerd.deku.Router.Models.RouterHandler.getTagForMessages
import com.afkanerd.deku.Router.data.RouterWorkManager
import com.afkanerd.deku.Router.data.RouterWorkManager.Companion.CONVERSATION_ID
import com.afkanerd.deku.Router.data.RouterWorkManager.Companion.GATEWAY_SERVER_ID
import com.afkanerd.deku.Router.data.models.GatewayServer
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class GatewayServerViewModel : ViewModel() {
    private lateinit var gatewayServersList: LiveData<List<GatewayServer>>


    operator fun get(context: Context): LiveData<List<GatewayServer>> {
        if (!::gatewayServersList.isInitialized) {
            gatewayServersList = MutableLiveData()
            gatewayServersList = Datastore.getDatastore(context).gatewayServerDAO().all
        }
        return gatewayServersList
    }

    fun route(
        context: Context,
        conversation: Conversations
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val isBase64 = isBase64Encoded(conversation.sms!!.body!!)
        val gatewayServerList =
            Datastore.getDatastore(context.applicationContext)
                .gatewayServerDAO()
                .getAllList()

        for (gatewayServer1 in gatewayServerList) {
            if (gatewayServer1.format != null &&
                gatewayServer1.format == GatewayServer.BASE64_FORMAT && !isBase64
            ) continue

            try {
                val routeMessageWorkRequest =
                    OneTimeWorkRequest.Builder(RouterWorkManager::class.java)
                        .setConstraints(constraints)
                        .setBackoffCriteria(
                            BackoffPolicy.LINEAR,
                            WorkRequest.Companion.MIN_BACKOFF_MILLIS,
                            TimeUnit.MILLISECONDS
                        )
                        .addTag(RouterHandler.TAG_NAME_GATEWAY_SERVER)
                        .addTag(getTagForMessages(conversation.id.toString()))
                        .addTag(getTagForGatewayServers(gatewayServer1.id))
                        .setInputData(
                            Data.Builder()
                                .putLong(
                                    GATEWAY_SERVER_ID,
                                    gatewayServer1.id
                                )
                                .putString(
                                    CONVERSATION_ID,
                                    conversation.id.toString()
                                )
                                .build()
                        )
                        .build()

                val uniqueWorkName: String = conversation.id.toString() + ":" +
                        gatewayServer1.URL + ":" + gatewayServer1.protocol
                val workManager = WorkManager.getInstance(context)
                val operation = workManager.enqueueUniqueWork(
                    uniqueWorkName,
                    ExistingWorkPolicy.KEEP,
                    routeMessageWorkRequest
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    fun add(
        context: Context,
        gatewayClient: GatewayServer,
        completeCallback: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            Datastore.getDatastore(context).gatewayServerDAO().insert(gatewayClient)
            completeCallback()
        }
    }
//    private fun loadSMSThreads(context: Context): LiveData<MutableList<WorkInfo>> {
//        return getMessageIdsFromWorkManagers(context)
//    }
}
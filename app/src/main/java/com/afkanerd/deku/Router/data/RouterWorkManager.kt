package com.afkanerd.deku.Router.data

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.Modules.Network
import com.afkanerd.deku.Router.data.models.FTP
import com.afkanerd.deku.Router.Models.RouterHandler
import com.afkanerd.deku.Router.Models.RouterItem
import com.afkanerd.deku.Router.data.models.SMTP
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.sun.mail.util.MailConnectException

class RouterWorkManager (context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {
    override fun doWork(): Result {
        val gatewayServerId = inputData.getLong(GATEWAY_SERVER_ID, -1)
        val conversationId = inputData.getString(CONVERSATION_ID)!!

        val datastore = Datastore.getDatastore(applicationContext)
        val gatewayServer = datastore.gatewayServerDAO()[gatewayServerId.toString()]
        val conversation = applicationContext.getDatabase()
            .conversationsDao()?.getConversation(conversationId.toLong())

        val routerItem = RouterItem(conversation!!)
        routerItem.tag = gatewayServer.tag

        val jsonStringBody = routerItem.serializeJson()
        println(jsonStringBody)

        when(gatewayServer.protocol) {
            SMTP.PROTOCOL -> {
                try {
                    RouterHandler.routeSmtpMessages(jsonStringBody, gatewayServer)
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (e is MailConnectException) { return Result.retry() }
                    return Result.failure()
                }
            }
            FTP.PROTOCOL -> {
                try {
                    RouterHandler.routeFTPMessages(jsonStringBody, gatewayServer)
                } catch (e: Exception) {
                    Log.e(javaClass.getName(), "Exception: ", e)
                    return Result.failure()
                }
            }
            else -> {
                try {
                    when(Network.Companion.jsonRequestPost(gatewayServer.URL!!,
                        jsonStringBody)
                        .response.statusCode
                    ) {
                        in 500..600 -> Result.retry()
                        else -> Result.failure()
                    }
                } catch(e: Exception) {
                    Log.e(javaClass.name, "Exception routing", e)
                    Result.retry()
                }
            }
        }

        return Result.success()
    }

    companion object {
        var GATEWAY_SERVER_ID = "GATEWAY_SERVER_ID"
        var CONVERSATION_ID = "CONVERSATION_ID"
    }
}
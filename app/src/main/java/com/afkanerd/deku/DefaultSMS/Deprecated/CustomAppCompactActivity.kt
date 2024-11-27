package com.afkanerd.deku.DefaultSMS.Deprecated

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ThreadedConversationsViewModel
import com.afkanerd.deku.DefaultSMS.DualSIMConversationActivity
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.NativeSMSDB
import com.afkanerd.deku.DefaultSMS.Models.SMSDatabaseWrapper
import com.afkanerd.deku.Modules.ThreadingPoolExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class CustomAppCompactActivity : DualSIMConversationActivity() {
//    protected var address: String? = null
//    protected var contactName: String? = null
//    protected var threadId: String? = null
//    protected var conversationsViewModel: ConversationsViewModel? = null
//
    protected var threadedConversationsViewModel: ThreadedConversationsViewModel? = null

    var databaseConnector: Datastore? = null

    private var requestPermissionLauncher: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        if (!_checkIsDefaultApp()) {
//            startActivity(Intent(this, DefaultCheckActivity::class.java))
//            finish()
//        }

        databaseConnector = Datastore.getDatastore(applicationContext)

        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    Toast.makeText(applicationContext, "Request granted...",
                        Toast.LENGTH_LONG).show()
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            }
    }

    private fun _checkIsDefaultApp(): Boolean {
        val myPackageName = packageName
        val defaultPackage = Telephony.Sms.getDefaultSmsPackage(this)

        return myPackageName == defaultPackage
    }

    protected open fun informSecured(secured: Boolean) {}

    protected fun sendTextMessage(
        text: String,
        address: String,
        subscriptionId: Int,
        threadId: String,
        conversationsViewModel: ConversationsViewModel,
        messageId: String?= null,
    ) {
        var messageId = messageId

        if (messageId == null) messageId = System.currentTimeMillis().toString()

        val conversation = Conversation()
        conversation.text = text
        conversation.message_id = messageId
        conversation.thread_id = threadId
        conversation.subscription_id = subscriptionId
        conversation.type = Telephony.Sms.MESSAGE_TYPE_OUTBOX
        conversation.date = System.currentTimeMillis().toString()
        conversation.address = address
        conversation.status = Telephony.Sms.STATUS_PENDING

        CoroutineScope(Dispatchers.Default).launch{
            try {
                conversationsViewModel.insert(applicationContext, conversation)
            } catch (e: Exception) {
                e.printStackTrace()
                return@launch
            }

//            val payload = encryptMessage(applicationContext, text, address)
//            conversation.text = payload.first
//            sendSMS(conversation, conversationsViewModel)
//
//            payload.second?.let {
//                E2EEHandler.storeState(applicationContext, payload.second!!.serializedStates,
//                    address)
//            }
        }
    }

    private fun sendSMS(conversation: Conversation, conversationsViewModel: ConversationsViewModel) {
        when {
            ContextCompat.checkSelfPermission( applicationContext,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED -> {
                sendTxt(conversation, conversationsViewModel)
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher?.launch(Manifest.permission.SEND_SMS)
            }
        }
    }

    private fun sendTxt(
        conversation: Conversation,
        conversationsViewModel: ConversationsViewModel) {
        try {
            SMSDatabaseWrapper.send_text(applicationContext, conversation, null)
        } catch (e: Exception) {
            e.printStackTrace()
            NativeSMSDB.Outgoing.register_failed( applicationContext, conversation.message_id,
                1 )
            conversation.status = Telephony.TextBasedSmsColumns.STATUS_FAILED
            conversation.type = Telephony.TextBasedSmsColumns.MESSAGE_TYPE_FAILED
            conversation.error_code = 1
            conversationsViewModel.update(applicationContext, conversation)
        }
    }

    protected fun saveDraft(
        messageId: String,
        text: String,
        address: String,
        threadId: String,
        conversationsViewModel: ConversationsViewModel) {
        ThreadingPoolExecutor.executorService.execute {
            val conversation = Conversation()
            conversation.message_id = messageId
            conversation.thread_id = threadId
            conversation.text = text
            conversation.isRead = true
            conversation.type = Telephony.Sms.MESSAGE_TYPE_DRAFT
            conversation.date = System.currentTimeMillis().toString()
            conversation.address = address
            conversation.status = Telephony.Sms.STATUS_PENDING
            try {
                conversationsViewModel.insert(applicationContext, conversation)
                SMSDatabaseWrapper.saveDraft(applicationContext, conversation)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}

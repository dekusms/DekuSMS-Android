package com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels

import android.R.attr.data
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.smsMmsNatives
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getThreadId
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.registerSmsToLocalDb
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.updateSmsToLocalDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConversationsViewModel: ViewModel() {
    var pageSize: Int = 10
    var prefetchDistance: Int = 3 * pageSize
    var enablePlaceholder: Boolean = true
    var initialLoadSize: Int = 2 * pageSize
    var maxSize: Int = PagingConfig.Companion.MAX_SIZE_UNBOUNDED

    fun update(context: Context, conversation: Conversations) {
        context.updateSmsToLocalDb( conversation )
        context.getDatabase().conversationsDao()?.update(conversation)
    }

    private var conversationsPager: Flow<PagingData<Conversations>>? = null

    fun getConversations(context: Context, threadId: Int): Flow<PagingData<Conversations>> {
        if(conversationsPager == null) {
            conversationsPager = Pager(
                config=PagingConfig(
                    pageSize,
                    prefetchDistance,
                    enablePlaceholder,
                    initialLoadSize,
                    maxSize
                ),
                pagingSourceFactory = {
                    context.getDatabase().conversationsDao()!!.getConversations(threadId)
                }
            ).flow.cachedIn(viewModelScope)
        }
        return conversationsPager!!
    }

    fun addConversation(
        context: Context,
        conversation: Conversations,
    ): Conversations {
        context.registerSmsToLocalDb(
            messageId = conversation.sms!!._id.toString(),
            address = conversation.sms!!.address!!,
            body = conversation.sms!!.body,
            subscriptionId = conversation.sms!!.sub_id,
            date = conversation.sms!!.date.toLong(),
            dateSent = conversation.sms!!.date_sent.toLong(),
            type = conversation.sms!!.type,
        )

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                context.getDatabase().conversationsDao()?.insert(conversation)
            }
        }
        return conversation
    }

    fun addConversation(
        context: Context,
        messageId: String,
        address: String,
        body: String,
        subscriptionId: Int,
        date: Long,
        dateSent: Long,
        type: Int,
        status: Int,
    ): Conversations {
        val threadId = context.getThreadId(address)
        context.registerSmsToLocalDb(
            messageId = messageId,
            address = address,
            body = body,
            subscriptionId = subscriptionId,
            date = date,
            dateSent = dateSent,
            type = type,
        )

        val conversation = Conversations(
            sms = smsMmsNatives.Sms(
                _id = (System.currentTimeMillis() / 1000).toInt(),
                thread_id = threadId.toInt(),
                address = address,
                date = (System.currentTimeMillis() / 1000).toInt(),
                date_sent = (dateSent / 1000).toInt(),
                read = 0,
                status = status,
                type = type,
                body = body,
                sub_id = subscriptionId,
            )
        )
        context.getDatabase().conversationsDao()?.insert(conversation)
        return conversation
    }

}
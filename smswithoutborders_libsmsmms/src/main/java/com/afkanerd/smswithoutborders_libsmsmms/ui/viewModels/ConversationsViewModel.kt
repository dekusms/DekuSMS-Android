package com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels

import android.R.attr.data
import android.content.Context
import android.content.Intent
import android.provider.Telephony
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
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.registerIncomingText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ConversationsViewModel: ViewModel() {
    var pageSize: Int = 10
    var prefetchDistance: Int = 3 * pageSize
    var enablePlaceholder: Boolean = true
    var initialLoadSize: Int = 2 * pageSize
    var maxSize: Int = PagingConfig.Companion.MAX_SIZE_UNBOUNDED

    fun add(context: Context, conversation: Conversations) {
        context.getDatabase().conversationsDao()?.insert(conversation)
    }

    fun update(context: Context, conversation: Conversations): Int? {
        return context.getDatabase().conversationsDao()?.update(conversation)
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

    fun addIncomingConversation(
        context: Context,
        messageId: String,
        address: String,
        body: String,
        subscriptionId: Int,
        date: Long,
        dateSent: Long
    ): Conversations {
        val threadId = context.getThreadId(address)
        context.registerIncomingText(
            messageId = messageId,
            address = address,
            body = body,
            subscriptionId = subscriptionId,
            date = date,
            dateSent = dateSent
        )

        val conversation = Conversations(
            sms = smsMmsNatives.Sms(
                _id = (System.currentTimeMillis() / 1000).toInt(),
                thread_id = threadId.toInt(),
                address = address,
                date = (System.currentTimeMillis() / 1000).toInt(),
                date_sent = 0,
                read = 1,
                status = Telephony.Sms.STATUS_NONE,
                type = Telephony.Sms.MESSAGE_TYPE_INBOX,
                body = body,
                sub_id = subscriptionId,
            )
        )
        context.getDatabase().conversationsDao()?.insert(conversation)
        return conversation
    }

}
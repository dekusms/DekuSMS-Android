package com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels

import android.content.Context
import android.net.Uri
import android.provider.BlockedNumberContract
import android.provider.Telephony
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.SmsMmsNatives
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getThreadId
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.sendMms
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.sendSms
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConversationsViewModel: ViewModel() {
    private val _selectedItems = MutableStateFlow<List<Conversations>>(emptyList()) // default
    val selectedItems: StateFlow<List<Conversations>> = _selectedItems.asStateFlow()

    fun setSelectedItems(conversations: List<Conversations>) {
        _selectedItems.value = conversations
    }

    fun removeAllSelectedItems() {
        _selectedItems.value = emptyList()
    }

    fun getSelectedItemCount(): Int {
        return _selectedItems.value.size
    }

    var pageSize: Int = 50
    var prefetchDistance: Int = 3 * pageSize
    var enablePlaceholder: Boolean = true
    var initialLoadSize: Int = 2 * pageSize
    var maxSize: Int = PagingConfig.Companion.MAX_SIZE_UNBOUNDED


    private var conversationsPager: Flow<PagingData<Conversations>>? = null

    fun getConversations(context: Context, address: String): Flow<PagingData<Conversations>> {
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
                    context.getDatabase().conversationsDao()!!
                        .getConversations(context.getThreadId(address))
                }
            ).flow.cachedIn(viewModelScope)
        }
        return conversationsPager!!
    }

    fun contactIsBlocked(
        context: Context,
        address: String,
    ): Boolean {
        try {
            return BlockedNumberContract.isBlocked(context,address)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun fetchDraft(
        context: Context,
        address: String,
        callback: (Conversations?) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val threadId = context.getThreadId(address)
                context.getDatabase().conversationsDao()
                    ?.fetchConversationsForType( threadId, Telephony.Sms.MESSAGE_TYPE_DRAFT)
                    ?.let { callback(it) }
            }
        }
    }

    fun search(
        context: Context,
        query: String,
        address: String,
        callback: (List<Int>) -> Unit,
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val searchIndexes = mutableListOf<Int>()
                val threadId = context.getThreadId(address).toInt()
                context.getDatabase().conversationsDao()
                    ?.getConversationsList(threadId)?.let { items ->
                        items.forEachIndexed { index, it ->
                            it.sms?.body?.let { text ->
                                if(!searchIndexes.contains(index) &&
                                    text.contains(other=query, ignoreCase=true))
                                    searchIndexes.add(index)
                            }
                        }

                }
                callback(searchIndexes)
            }
        }
    }

    fun clearDraft(
        context: Context,
        conversation: Conversations
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                context.getDatabase().conversationsDao()?.delete(conversation)
            }
        }
    }

    fun unArchive(context: Context, threadId: Int, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                context.getDatabase().threadsDao()?.get(threadId)?.let { thread ->
                    ThreadsViewModel().unArchiveThreads(context, listOf(thread)) {
                        callback(it)
                    }
                }
            }
        }
    }

    fun archive(context: Context, threadId: Int, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                context.getDatabase().threadsDao()?.get(threadId)?.let { thread ->
                    ThreadsViewModel().archiveThreads(context, listOf(thread)) {
                        callback(it)
                    }
                }
            }
        }
    }

    fun sendMms(
        context: Context,
        uri: Uri,
        text: String,
        address: String,
        subscriptionId: Long,
        callback: (Conversations?) -> Unit
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    context.sendMms(
                        contentUri = uri,
                        text = text,
                        address = address,
                        threadId = context.getThreadId(address),
                        subscriptionId = subscriptionId,
                    ).let { conversation ->
                        callback(conversation)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context,
                        context.getString(R.string.something_went_wrong_with_sending),
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun sendSms(
        context: Context,
        text: String,
        address: String,
        subscriptionId: Long,
        callback: (Conversations?) -> Unit
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    context.sendSms(
                        text = text,
                        address = address,
                        threadId = context.getThreadId(address),
                        subscriptionId = subscriptionId,
                    )?.let { conversation ->
                        callback(conversation)
                    }
                } catch(e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context,
                        context.getString(R.string.something_went_wrong_with_sending),
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun delete(
        context: Context,
        conversations: List<Conversations>,
        callback: () -> Unit
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                context.getDatabase().conversationsDao()?.delete(conversations)
                callback()
            }
        }
    }

    fun deleteThread(
        context: Context,
        address: String,
        callback: () -> Unit,
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                context.getThreadId(address).let { threadId ->
                    context.getDatabase().threadsDao()?.get(threadId)?.let { thread ->
                        ThreadsViewModel().deleteThreads(context, listOf(thread))
                        callback()
                    }
                }
            }
        }
    }

    fun addDraft(
        context: Context,
        body: String,
        mmsUri: Uri?,
        address: String,
        subId: Long,
        callback: (Conversations) -> Unit
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val threadId = context.getThreadId(address)
                val conversation = Conversations(
                    sms = SmsMmsNatives.Sms(
                        _id = System.currentTimeMillis(),
                        thread_id = threadId,
                        address = address,
                        date = System.currentTimeMillis(),
                        date_sent = System.currentTimeMillis(),
                        read = 1,
                        status = Telephony.Sms.STATUS_PENDING,
                        type = Telephony.Sms.MESSAGE_TYPE_DRAFT,
                        body = body,
                        sub_id = subId
                    ),
                    mms_content_uri = mmsUri?.toString()
                )
                context.getDatabase().conversationsDao()?.insert(conversation)
                callback(conversation)
            }
        }
    }
}
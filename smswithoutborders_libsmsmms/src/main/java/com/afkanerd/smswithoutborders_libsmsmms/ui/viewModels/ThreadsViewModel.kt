package com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels

import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Threads
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getThreadId
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.loadRawSmsMmsDb
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.makeE16PhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThreadsViewModel: ViewModel() {
    var messagesLoading by mutableStateOf(false)
    var foldOpenConversation by mutableStateOf("")

    enum class InboxType {
        INBOX,
        ARCHIVED,
        BLOCKED,
        DRAFTS,
        MUTED,
    }

    private val _selectedInbox = MutableLiveData(InboxType.INBOX) // default
    val selectedInbox: LiveData<InboxType> get() = _selectedInbox

    fun setInboxType(type: InboxType) {
        _selectedInbox.value = type
    }

    private val _selectedItems = MutableStateFlow<List<Threads>>(emptyList()) // default
    val selectedItems: StateFlow<List<Threads>> = _selectedItems.asStateFlow()

    fun setSelectedItems(threads: List<Threads>) {
        _selectedItems.value = threads
    }

    fun removeAllSelectedItems() {
        _selectedItems.value = emptyList()
    }

    var pageSize: Int = 50
    var prefetchDistance: Int = 3 * pageSize
    var enablePlaceholder: Boolean = true
    var initialLoadSize: Int = 2 * pageSize
    var maxSize: Int = PagingConfig.Companion.MAX_SIZE_UNBOUNDED

    private var threadsPager: Flow<PagingData<Threads>>? = null
    private var archivePager: Flow<PagingData<Threads>>? = null
    private var draftsPager: Flow<PagingData<Threads>>? = null
    private var mutePager: Flow<PagingData<Threads>>? = null
    private var blockedPager: Flow<PagingData<Threads>>? = null

    fun getThreads(context: Context): Flow<PagingData<Threads>> {
        if(threadsPager == null) {
            threadsPager = Pager(
                config=PagingConfig(
                    pageSize,
                    prefetchDistance,
                    enablePlaceholder,
                    initialLoadSize,
                    maxSize
                ),
                pagingSourceFactory = {
                    context.getDatabase().threadsDao()!!.getThreads()
                }
            ).flow.cachedIn(viewModelScope)
        }
        return threadsPager!!
    }

    fun getArchives(context: Context): Flow<PagingData<Threads>> {
        if(archivePager == null) {
            archivePager = Pager(
                config=PagingConfig(
                    pageSize,
                    prefetchDistance,
                    enablePlaceholder,
                    initialLoadSize,
                    maxSize
                ),
                pagingSourceFactory = {
                    context.getDatabase().threadsDao()!!.getArchived()
                }
            ).flow.cachedIn(viewModelScope)
        }
        return archivePager!!
    }

    fun getDrafts(context: Context): Flow<PagingData<Threads>> {
        if(draftsPager == null) {
            draftsPager = Pager(
                config=PagingConfig(
                    pageSize,
                    prefetchDistance,
                    enablePlaceholder,
                    initialLoadSize,
                    maxSize
                ),
                pagingSourceFactory = {
                    context.getDatabase().threadsDao()!!
                        .getType(Telephony.Sms.MESSAGE_TYPE_DRAFT)
                }
            ).flow.cachedIn(viewModelScope)
        }
        return draftsPager!!
    }

    fun getIsMute(context: Context): Flow<PagingData<Threads>> {
        if(mutePager == null) {
            mutePager = Pager(
                config=PagingConfig(
                    pageSize,
                    prefetchDistance,
                    enablePlaceholder,
                    initialLoadSize,
                    maxSize
                ),
                pagingSourceFactory = {
                    context.getDatabase().threadsDao()!!.getIsMute()
                }
            ).flow.cachedIn(viewModelScope)
        }
        return mutePager!!
    }

    fun getIsBlocked(context: Context): Flow<PagingData<Threads>> {
        if(blockedPager == null) {
            blockedPager = Pager(
                config=PagingConfig(
                    pageSize,
                    prefetchDistance,
                    enablePlaceholder,
                    initialLoadSize,
                    maxSize
                ),
                pagingSourceFactory = {
                    context.getDatabase().threadsDao()!!.getIsBlocked()
                }
            ).flow.cachedIn(viewModelScope)
        }
        return blockedPager!!
    }

    fun deleteThreads(context: Context, threads: List<Threads>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                context.getDatabase().threadsDao()?.delete(threads)
            }
        }
    }

    fun setIsBlocked(
        context: Context,
        addresses: List<String>,
        isBlocked: Boolean,
        callback: () -> Unit = {}
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                context.getDatabase().threadsDao()?.setIsBlocked(isBlocked, addresses)
                callback()
            }
        }
    }

    fun update(
        context: Context,
        threads: List<Threads>,
        callback: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val count = context.getDatabase().threadsDao()?.update(threads)
                callback(count != 0)
            }
        }
    }

    fun loadNatives(
        context: Context,
        deleteDb: Boolean = false,
        completeCallback: () -> Unit,
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                messagesLoading = true

                try {
                    val conversations = context.loadRawSmsMmsDb()
                    context.getDatabase().conversationsDao()
                        ?.insertAll(conversations, deleteDb)

                } catch(e: Exception) {
                    e.printStackTrace()
                } finally {
                    withContext(Dispatchers.Main) {
                        messagesLoading = false
                        completeCallback()
                    }
                }
            }
        }
    }

    fun isArchived(context: Context, threadId: Int, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                context.getDatabase().threadsDao()?.get(threadId)?.isArchive?.let {
                    callback(it)
                    return@withContext
                }
                callback(false)
            }
        }
    }

    fun isMuted(context: Context, threadId: Int, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                context.getDatabase().threadsDao()?.get(threadId)?.isMute?.let {
                    callback(it)
                    return@withContext
                }
                callback(false)
            }
        }
    }

    fun get(
        context: Context,
        threadId: Int,
        callback: (Threads?) -> Unit,
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val thread = context.getDatabase().threadsDao()?.get(threadId)
                callback(thread)
            }
        }
    }
}
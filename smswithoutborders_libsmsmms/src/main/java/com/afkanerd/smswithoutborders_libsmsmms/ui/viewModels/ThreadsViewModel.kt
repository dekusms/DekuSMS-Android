package com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
    private val _newIntent = MutableStateFlow<Intent?>(null)
    var newIntent: StateFlow<Intent?> = _newIntent

    enum class InboxType {
        INBOX,
        ARCHIVED,
        ENCRYPTED,
        BLOCKED,
        DRAFTS,
        MUTED,
        REMOTE_LISTENER,
        DEVELOPER_MODE;
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

    var pageSize: Int = 10
    var prefetchDistance: Int = 3 * pageSize
    var enablePlaceholder: Boolean = true
    var initialLoadSize: Int = 2 * pageSize
    var maxSize: Int = PagingConfig.Companion.MAX_SIZE_UNBOUNDED

    private var threadsPager: Flow<PagingData<Threads>>? = null
    private var archivePager: Flow<PagingData<Threads>>? = null

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

    fun deleteThreads(context: Context, threads: List<Threads>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                context.getDatabase().threadsDao()?.delete(threads)
            }
        }
    }

    fun unArchiveThreads(
        context: Context,
        threads: List<Threads>,
        callback: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val count = context.getDatabase().threadsDao()?.update(threads.apply {
                    forEach { it.isArchive = false }
                })
                callback(count != 0)
            }
        }
    }

    fun archiveThreads(
        context: Context,
        threads: List<Threads>,
        callback: (Boolean) -> Unit = {}
) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val count = context.getDatabase().threadsDao()?.update(threads.apply {
                    forEach { it.isArchive = true }
                })
                callback(count != 0)
            }
        }
    }

    fun loadNatives(
        context: Context,
        completeCallback: () -> Unit,
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val conversations = context.loadRawSmsMmsDb()
                context.getDatabase().conversationsDao()?.insertAll(conversations)
                completeCallback()
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

    data class ProcessedIntents(val address: String?, val threadId: Int?, val text: String?)
    fun processIntents(
        context: Context,
        intent: Intent,
        defaultRegion: String,
    ): ProcessedIntents? {
        if(intent.action != null &&
            ((intent.action == Intent.ACTION_SENDTO) || (intent.action == Intent.ACTION_SEND))) {
            val text = if(intent.hasExtra("sms_body")) intent.getStringExtra("sms_body")
            else if(intent.hasExtra("android.intent.extra.TEXT")) {
                intent.getStringExtra("android.intent.extra.TEXT")
            } else ""

            val sendToString = intent.dataString

            if ((sendToString != null &&
                        (sendToString.contains("smsto:") ||
                                sendToString.contains("sms:"))) ||
                intent.hasExtra("address")
            ) {
                val address = context.makeE16PhoneNumber(
                    if(intent.hasExtra("address"))
                        intent.getStringExtra("address")!!
                    else sendToString!!
                )

                val threadId = context.getThreadId(address)
                return ProcessedIntents(address, threadId.toInt(), text)
            }
        }
        else if(intent.hasExtra("address")) {
            val text = if(intent.hasExtra("android.intent.extra.TEXT"))
                intent.getStringExtra("android.intent.extra.TEXT") else ""

            val address = intent.getStringExtra("address")
            val threadId = intent.getStringExtra("thread_id")
            return ProcessedIntents(address, threadId?.toInt(), text)
        }
        return null
    }
}
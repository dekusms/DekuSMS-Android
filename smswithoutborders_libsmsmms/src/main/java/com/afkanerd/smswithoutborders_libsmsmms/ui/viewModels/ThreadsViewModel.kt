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
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.makeE16PhoneNumber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

    private val _selectedItems = MutableLiveData<MutableList<Threads>>(
        mutableListOf<Threads>()) // default
    val selectedItems: LiveData<MutableList<Threads>> get() = _selectedItems

    fun addSelectedItem(thread: Threads) {
        _selectedItems.postValue(_selectedItems.value.apply { this?.add(thread) })
    }

    fun removeSelectedItem(thread: Threads) {
        _selectedItems.postValue(_selectedItems.value.apply {
            this?.remove(thread)
        })
    }

    fun removeAllSelectedItems() {
        _selectedItems.postValue(mutableListOf())
    }

    var pageSize: Int = 10
    var prefetchDistance: Int = 3 * pageSize
    var enablePlaceholder: Boolean = true
    var initialLoadSize: Int = 2 * pageSize
    var maxSize: Int = PagingConfig.Companion.MAX_SIZE_UNBOUNDED

    private var threadsPager: Flow<PagingData<Threads>>? = null

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

    fun reset(context: Context) {
        TODO("Implement reset functionality")
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
package com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels

import android.content.Context
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
import kotlinx.coroutines.flow.Flow

class ThreadsViewModel: ViewModel() {
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
}
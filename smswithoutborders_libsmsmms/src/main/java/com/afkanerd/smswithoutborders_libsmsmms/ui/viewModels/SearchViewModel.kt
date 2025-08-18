package com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.afkanerd.smswithoutborders_libsmsmms.data.DatabaseImpl
import com.afkanerd.smswithoutborders_libsmsmms.data.dao.ThreadsDao
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Threads
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class SearchViewModel(
    threadsDao: ThreadsDao,
    threadId: Int? = null,
) : ViewModel() {
//    var pageSize: Int = 10
//    var prefetchDistance: Int = 3 * pageSize
//    var enablePlaceholder: Boolean = true
//    var initialLoadSize: Int = 2 * pageSize
//    var maxSize: Int = PagingConfig.Companion.MAX_SIZE_UNBOUNDED

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val threads: Flow<PagingData<Threads>> = searchQuery.flatMapLatest { query ->
        if(query.isBlank()) {
            flowOf(PagingData.empty())
        }
        else {
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    if(threadId == null) threadsDao.search(query)
                    else threadsDao.search(query, threadId)
                }
            ).flow
        }
    }.cachedIn(viewModelScope)
}
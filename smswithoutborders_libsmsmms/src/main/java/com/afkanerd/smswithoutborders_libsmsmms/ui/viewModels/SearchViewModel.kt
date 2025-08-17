package com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Threads
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    var pageSize: Int = 10
    var prefetchDistance: Int = 3 * pageSize
    var enablePlaceholder: Boolean = true
    var initialLoadSize: Int = 2 * pageSize
    var maxSize: Int = PagingConfig.Companion.MAX_SIZE_UNBOUNDED

    private var threadsPager: Flow<PagingData<Threads>>? = null

    var searchQuery = mutableStateOf("")

    fun search(context: Context): Flow<PagingData<Threads>> {
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
                    context.getDatabase().threadsDao()!!.search(searchQuery.value)
                }
            ).flow.cachedIn(viewModelScope)
        }
        return threadsPager!!
    }
}

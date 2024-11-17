package com.afkanerd.deku.DefaultSMS.AdaptersViewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversations
import com.afkanerd.deku.Modules.ThreadingPoolExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList

class SearchViewModel : ViewModel() {
    private var liveData: MutableLiveData<MutableList<ThreadedConversations>> = MutableLiveData()

    var threadId: String? = null
    var databaseConnector: Datastore? = null

    fun get(): LiveData<MutableList<ThreadedConversations>> {
        return liveData
    }

    fun search(context: Context, input: String) {
        if(input.isBlank()) {
            liveData.value = mutableListOf<ThreadedConversations>()
        }
        else {
            CoroutineScope(Dispatchers.Default).launch {
                val datastore = Datastore.getDatastore(context).threadedConversationsDao()
                val results = datastore.search(input)
                val ids: List<String> = results.flatMap { listOf(it.threadId) }
                val threads = datastore.getList(ids).apply {
                    forEachIndexed { index, it ->
                        it.unread_count = results[index].count
                        it.date = results[index].date
                    }
                }
                liveData.postValue(threads)
            }
        }
    }
}

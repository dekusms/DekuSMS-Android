package com.afkanerd.deku.DefaultSMS.AdaptersViewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.SearchViewModel.SearchedItem
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversations
import com.afkanerd.deku.Modules.ThreadingPoolExecutor
import java.util.ArrayList

class SearchViewModel : ViewModel() {
    class SearchedItem(threadedConversations: MutableList<ThreadedConversations?>?, index: Int?) {
        var threadedConversations: MutableList<ThreadedConversations?>?
        var index: Int?

        init {
            this.threadedConversations = threadedConversations
            this.index = index
        }
    }

    var liveData: MutableLiveData<MutableList<SearchedItem?>?>? = null

    var threadId: String? = null

    var databaseConnector: Datastore? = null

    fun get(): LiveData<MutableList<SearchedItem?>?> {
        if (this.liveData == null) {
            liveData = MutableLiveData<MutableList<SearchedItem?>?>()
        }
        return liveData!!
    }

    fun getByThreadId(threadId: String?): LiveData<MutableList<SearchedItem?>?> {
        if (this.liveData == null) {
            liveData = MutableLiveData<MutableList<SearchedItem?>?>()
            this.threadId = threadId
        }
        return liveData!!
    }

    fun search(context: Context, input: String) {
        
    }
}

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
    var liveData: MutableLiveData<List<Conversation>> = MutableLiveData()

    var threadId: String? = null

    fun get(): LiveData<List<Conversation>> {
        return liveData
    }

    fun search(context: Context, input: String) {
        if(input.isBlank()) {
            liveData.value = mutableListOf<Conversation>()
        }
        else {
            CoroutineScope(Dispatchers.Default).launch {
                val datastore = Datastore.getDatastore(context).conversationDao()
                val results = if(!threadId.isNullOrBlank())
                    datastore.getAllThreadingSearch(input, threadId!!)
                else datastore.getAllThreadingSearch(input)
                liveData.postValue(results)
            }
        }
    }
}

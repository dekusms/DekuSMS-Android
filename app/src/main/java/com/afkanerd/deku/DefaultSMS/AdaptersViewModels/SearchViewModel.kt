package com.afkanerd.deku.DefaultSMS.AdaptersViewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversations
import com.afkanerd.deku.Modules.ThreadingPoolExecutor
import com.afkanerd.smswithoutborders_libsmsmms.Extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList

class SearchViewModel : ViewModel() {
    var liveData: MutableLiveData<List<Conversations>> = MutableLiveData()

    var threadId: String? = null

    fun get(): LiveData<List<Conversations>> {
        return liveData
    }

    fun search(context: Context, input: String) {
        if(input.isBlank()) {
            liveData.value = mutableListOf<Conversations>()
        }
        else {
            CoroutineScope(Dispatchers.Default).launch {
                val datastore = context.getDatabase().conversationDao()!!
                val results = if(!threadId.isNullOrBlank())
                    datastore.getAllThreadingSearch(input, threadId!!)
                else datastore.getAllThreadingSearch(input)
                liveData.postValue(results)
            }
        }
    }
}

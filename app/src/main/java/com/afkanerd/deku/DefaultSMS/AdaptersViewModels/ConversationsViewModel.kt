package com.afkanerd.deku.DefaultSMS.AdaptersViewModels

import android.content.Context
import android.provider.Telephony
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.liveData
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.NativeSMSDB
import com.afkanerd.deku.DefaultSMS.Models.SMSDatabaseWrapper
import java.util.ArrayList
import java.util.Locale


class ConversationsViewModel : ViewModel() {
    var threadId: String? = null
    var address: String? = null

    private var liveData: LiveData<MutableList<Conversation>>? = null
    fun getLiveData(context: Context): LiveData<MutableList<Conversation>> {
//        if (liveData == null) {
//            println("Thread View model: $threadId")
//            liveData = Datastore.getDatastore(context).conversationDao().getLiveData(threadId)
//        }
        liveData = Datastore.getDatastore(context).conversationDao().getLiveData(threadId)
        return liveData!!
    }

    fun insert(context: Context?, conversation: Conversation): Long {
        Datastore.getDatastore(context).threadedConversationsDao()
            .insertThreadAndConversation(context, conversation)
        return 0
    }

    fun update(context: Context, conversation: Conversation) {
        Datastore.getDatastore(context).conversationDao()._update(conversation)
    }

    fun deleteItems(context: Context, conversations: List<Conversation>) {
        Datastore.getDatastore(context).conversationDao().delete(conversations)
        val ids = arrayOfNulls<String>(conversations.size)
        for (i in conversations.indices) ids[i] = conversations[i].message_id
        NativeSMSDB.deleteMultipleMessages(context, ids)
    }

    fun fetchDraft(context: Context): Conversation? {
        return Datastore.getDatastore(context).conversationDao().fetchTypedConversation(
            Telephony.TextBasedSmsColumns.MESSAGE_TYPE_DRAFT, threadId
        )
    }

    fun clearDraft(context: Context) {
        Datastore.getDatastore(context).conversationDao()
            .deleteAllType(context, Telephony.TextBasedSmsColumns.MESSAGE_TYPE_DRAFT, threadId)
        SMSDatabaseWrapper.deleteDraft(context, threadId)
    }

    fun unMute(context: Context) {
        Datastore.getDatastore(context).threadedConversationsDao().updateMuted(0, threadId)
    }

    fun mute(context: Context) {
        Datastore.getDatastore(context).threadedConversationsDao().updateMuted(1, threadId)
    }
}

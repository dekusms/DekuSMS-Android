package com.afkanerd.deku.DefaultSMS.AdaptersViewModels

import android.content.Context
import android.provider.Telephony
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import com.afkanerd.deku.DefaultSMS.ui.InboxType
import java.util.ArrayList
import java.util.Locale
import kotlin.concurrent.thread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import com.afkanerd.deku.DefaultSMS.Models.Contacts


class ConversationsViewModel : ViewModel() {
    var threadId by mutableStateOf("")
    var address by mutableStateOf("")
    var text by mutableStateOf("")
    var searchQuery by mutableStateOf("")
    var contactName: String? by mutableStateOf(null)

    var selectedItems = mutableStateListOf<String>()

    private var liveData: LiveData<MutableList<Conversation>> = MutableLiveData()
    fun getLiveData(context: Context): LiveData<MutableList<Conversation>> {
        val defaultRegion = Helpers.getUserCountry( context )

        contactName = Contacts.retrieveContactName(
            context,
            Helpers.getFormatCompleteNumber(address, defaultRegion)
        )

        if(contactName.isNullOrBlank())
            contactName = address

        liveData = Datastore.getDatastore(context).conversationDao().getLiveData(threadId)
        return liveData
    }

    fun insert(context: Context, conversation: Conversation): Long {
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

    fun getUnreadCount(context: Context, threadId: String) : Int {
        return Datastore.getDatastore(context).conversationDao().getUnreadCount(threadId)
    }


    fun fetchDraft(context: Context): Conversation? {
        return Datastore.getDatastore(context).conversationDao().fetchTypedConversation(
            Telephony.TextBasedSmsColumns.MESSAGE_TYPE_DRAFT, threadId!!
        )
    }

    fun clearDraft(context: Context) {
        Datastore.getDatastore(context).conversationDao()
            .deleteAllType(context, Telephony.TextBasedSmsColumns.MESSAGE_TYPE_DRAFT, threadId!!)
        SMSDatabaseWrapper.deleteDraft(context, threadId)
    }

    fun unMute(context: Context) {
        Datastore.getDatastore(context).threadedConversationsDao().updateMuted(0, threadId!!)
    }

    fun mute(context: Context) {
        Datastore.getDatastore(context).threadedConversationsDao().updateMuted(1, threadId!!)
    }

    fun insertDraft(context: Context) {
        val id = System.currentTimeMillis().toString()

        val conversation = Conversation();
        conversation.message_id = id
        conversation.thread_id = threadId
        conversation.text = text
        conversation.isRead = true
        conversation.type = Telephony.Sms.MESSAGE_TYPE_DRAFT
        conversation.date = id
        conversation.address = address
        conversation.status = Telephony.Sms.STATUS_PENDING

        insert(context, conversation);
        SMSDatabaseWrapper.saveDraft(context, conversation);
    }
}

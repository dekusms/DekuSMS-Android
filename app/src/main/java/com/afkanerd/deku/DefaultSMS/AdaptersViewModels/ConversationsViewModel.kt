package com.afkanerd.deku.DefaultSMS.AdaptersViewModels

import android.content.Context
import android.content.SharedPreferences
import android.provider.BlockedNumberContract
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import com.afkanerd.deku.DefaultSMS.Models.Contacts
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversations
import com.afkanerd.deku.DefaultSMS.Models.DatastoreHandler
import com.afkanerd.deku.DefaultSMS.Models.ThreadsCount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class ConversationsViewModel : ViewModel() {

    var threadId by mutableStateOf("")
    var address by mutableStateOf("")
    var text by mutableStateOf("")
    var searchQuery by mutableStateOf("")
    var contactName: String by mutableStateOf("")
    var subscriptionId: Int by mutableIntStateOf(-1)

    var selectedItems = mutableStateListOf<String>()
    var retryDeleteItem: MutableList<Conversation> = arrayListOf()

    var liveData: LiveData<MutableList<Conversation>>? = null
    var threadedLiveData: LiveData<MutableList<Conversation>>? = null
    var archivedLiveData: LiveData<MutableList<Conversation>>? = null
    var encryptedLiveData: LiveData<MutableList<Conversation>>? = null
    var blockedLiveData: LiveData<MutableList<Conversation>>? = null
    var mutedLiveData: LiveData<MutableList<Conversation>>? = null
    var draftsLiveData: LiveData<MutableList<Conversation>>? = null

    var inboxType: InboxType = InboxType.INBOX

    fun getThreading(context: Context): LiveData<MutableList<Conversation>> {
        if(threadedLiveData == null) {
            threadedLiveData = Datastore.getDatastore(context).conversationDao().getAllThreading()
            archivedLiveData = Datastore.getDatastore(context).conversationDao().getAllThreadingArchived()
            encryptedLiveData = Datastore.getDatastore(context).conversationDao().getAllThreadingEncrypted()
            blockedLiveData = Datastore.getDatastore(context).conversationDao().getAllThreadingBlocked()
            mutedLiveData = Datastore.getDatastore(context).conversationDao().getAllThreadingMuted()
            draftsLiveData = Datastore.getDatastore(context).conversationDao().getAllThreadingDrafts()
        }
        return threadedLiveData!!
    }

    fun getLiveData(context: Context): LiveData<MutableList<Conversation>>? {
        if(liveData == null) {
            liveData = MutableLiveData()
            liveData = Datastore.getDatastore(context).conversationDao().getLiveData(threadId)
        }
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

    fun getUnreadCount(context: Context, threadId: String) : Int {
        return Datastore.getDatastore(context).conversationDao().getUnreadCount(threadId)
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

   fun isMuted(context: Context, threadId: String? = null) : Boolean {
       val mutingKey = stringPreferencesKey(threadId ?: this.threadId)
       val isMuted = runBlocking {
           DatastoreHandler.getDatastore(context).data.firstOrNull()?.let {
               return@runBlocking it.contains(mutingKey)
           }
       }
       return isMuted == true
    }

    suspend fun unMute(context: Context) {
        val mutingKey = stringPreferencesKey(threadId)
        DatastoreHandler.getDatastore(context).edit { preference ->
            preference.remove(mutingKey)
        }
    }

    suspend fun mute(context: Context) {
        val mutingKey = stringPreferencesKey(threadId)
        DatastoreHandler.getDatastore(context).edit { preference ->
            preference[mutingKey] = address
        }
    }

    fun block(context: Context) {
        Datastore.getDatastore(context).conversationDao().block(threadId)
    }

    fun unBlock(context: Context) {
        Datastore.getDatastore(context).conversationDao().unBlock(threadId)
    }

    fun archive(context: Context, threadIds: List<String>) {
        Datastore.getDatastore(context).conversationDao().archive(threadIds)
    }

    fun archive(context: Context, threadId: String? = null) {
        Datastore.getDatastore(context).conversationDao().archive(threadId ?: this.threadId)
    }

    fun unArchive(context: Context, threadIds: List<String>) {
        Datastore.getDatastore(context).conversationDao().unarchive(threadIds)
    }

    fun unArchive(context: Context, threadId: String? = null) {
        Datastore.getDatastore(context).conversationDao().unarchive(threadId ?: this.threadId)
    }

    fun setSecured(context: Context, isSecured: Boolean) {
        Datastore.getDatastore(context).conversationDao().archive(threadId)
    }

    fun deleteThread(context: Context) {
        Datastore.getDatastore(context).conversationDao().deleteThread(threadId)
        NativeSMSDB.deleteThreads(context, arrayOf(threadId))
    }

    fun deleteThreads(context: Context, ids: List<String>) {
        Datastore.getDatastore(context).conversationDao().deleteAllThreads(ids)
        NativeSMSDB.deleteThreads(context, ids.toTypedArray())
    }

    fun delete(context: Context, conversation: Conversation) {
        Datastore.getDatastore(context).conversationDao().delete(conversation)
        NativeSMSDB.deleteMultipleMessages(context, arrayOf(conversation.message_id))
    }

    fun delete(context: Context, conversations: List<Conversation>) {
        Datastore.getDatastore(context).conversationDao().delete(conversations)
        val ids: Array<String> = conversations.map { it.message_id!! }.toTypedArray()
        NativeSMSDB.deleteMultipleMessages(context, ids)
    }

    fun insertDraft(context: Context) {
        val conversation = Conversation();
        conversation.message_id = "1"
        conversation.thread_id = threadId
        conversation.text = text
        conversation.isRead = true
        conversation.type = Telephony.Sms.MESSAGE_TYPE_DRAFT
        conversation.date = System.currentTimeMillis().toString()
        conversation.address = address
        conversation.status = Telephony.Sms.STATUS_PENDING

        insert(context, conversation);
        SMSDatabaseWrapper.saveDraft(context, conversation);
    }

    private var folderMetrics: MutableLiveData<ThreadsCount> = MutableLiveData()
    fun getCount(context: Context) : MutableLiveData<ThreadsCount> {
        val databaseConnector = Datastore.getDatastore(context)
        CoroutineScope(Dispatchers.Default).launch {
            folderMetrics.postValue(databaseConnector.conversationDao().getFullCounts())
        }
        return folderMetrics
    }

    fun updateToRead(context: Context) {
        Datastore.getDatastore(context).conversationDao().updateRead(true, threadId)
    }

    fun unblock(context: Context) {
        BlockedNumberContract.unblock(context, this.address)
    }

    fun unblock(context: Context, addresses: List<String>) {
        for (address in addresses) {
            BlockedNumberContract.unblock(context, address)
        }
    }

}

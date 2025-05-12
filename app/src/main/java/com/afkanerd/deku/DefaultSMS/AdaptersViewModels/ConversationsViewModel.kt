package com.afkanerd.deku.DefaultSMS.AdaptersViewModels

import android.content.Context
import android.content.Intent
import android.provider.BlockedNumberContract
import android.provider.Telephony
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.liveData
import androidx.window.layout.WindowLayoutInfo
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.NativeSMSDB
import com.afkanerd.deku.DefaultSMS.Models.SMSDatabaseWrapper
import com.afkanerd.deku.DefaultSMS.Models.ThreadsConfigurations
import com.afkanerd.deku.DefaultSMS.Models.ThreadsCount
import com.afkanerd.deku.DefaultSMS.ui.InboxType
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


class ConversationsViewModel : ViewModel() {

    var threadId by mutableStateOf("")
    var address by mutableStateOf("")
    var text by mutableStateOf("")
    var searchQuery by mutableStateOf("")
    var subscriptionId: Int by mutableIntStateOf(-1)

    var importDetails by mutableStateOf("")

    var selectedItems = mutableStateListOf<String>()
    var retryDeleteItem: MutableList<Conversation> = arrayListOf()
    var selectedMessage: Conversation? = null

    var liveData: LiveData<MutableList<Conversation>>? = null
    var remoteListenersLiveData: LiveData<MutableList<Conversation>>? = null

    var inboxType: InboxType = InboxType.INBOX

    var newLayoutInfo: WindowLayoutInfo? = null

    private val _newIntent = MutableStateFlow<Intent?>(null)
    var newIntent: StateFlow<Intent?> = _newIntent

    var pageSize: Int = 10
    var prefetchDistance: Int = 3 * pageSize
    var enablePlaceholder: Boolean = true
    var initialLoadSize: Int = 2 * pageSize
    var maxSize: Int = PagingConfig.Companion.MAX_SIZE_UNBOUNDED

//    lateinit var threadingPager: Pager<Int, Conversation>
    private lateinit var threadingPager: Flow<PagingData<Conversation>>
    private lateinit var archivedPager: Flow<PagingData<Conversation>>
    private lateinit var encryptedPager: Flow<PagingData<Conversation>>
    private lateinit var draftPager: Flow<PagingData<Conversation>>
    private lateinit var mutedPager: Flow<PagingData<Conversation>>
    private lateinit var remoteListenerPager: Flow<PagingData<Conversation>>

    private lateinit var conversationsPager: Flow<PagingData<Conversation>>

    fun setNewIntent(intent: Intent?) {
        _newIntent.value = intent
    }

    fun getInboxType(isDefault: Boolean = false): InboxType {
        inboxType = if(remoteListenersLiveData?.value?.isNotEmpty() == true && !isDefault) {
            InboxType.REMOTE_LISTENER
        } else InboxType.INBOX
        return inboxType
    }

    fun getThreadingPagingSource(context: Context): Flow<PagingData<Conversation>> {
        if(!::threadingPager.isInitialized) {
            threadingPager = Pager(
                config=PagingConfig(
                    pageSize,
                    prefetchDistance,
                    enablePlaceholder,
                    initialLoadSize,
                    maxSize
                ),
                pagingSourceFactory = {
                    Datastore.getDatastore(context).conversationDao()
                        .getAllThreadingPagingSource()
                }
            ).flow.cachedIn(viewModelScope)
        }
        return threadingPager
    }

    fun getArchivedPagingSource(context: Context): Flow<PagingData<Conversation>> {
        if(!::archivedPager.isInitialized) {
            archivedPager = Pager(
                config=PagingConfig(
                    pageSize,
                    prefetchDistance,
                    enablePlaceholder,
                    initialLoadSize,
                    maxSize
                ),
                pagingSourceFactory = {
                    Datastore.getDatastore(context).conversationDao()
                        .getArchivedPagingSource()
                }
            ).flow.cachedIn(viewModelScope)
        }
        return archivedPager
    }

    fun getEncryptedPagingSource(context: Context): Flow<PagingData<Conversation>> {
        if(!::encryptedPager.isInitialized) {
            encryptedPager = Pager(
                config=PagingConfig(
                    pageSize,
                    prefetchDistance,
                    enablePlaceholder,
                    initialLoadSize,
                    maxSize
                ),
                pagingSourceFactory = {
                    Datastore.getDatastore(context).conversationDao()
                        .getArchivedPagingSource()
                }
            ).flow.cachedIn(viewModelScope)
        }
        return encryptedPager
    }

    fun getDraftPagingSource(context: Context): Flow<PagingData<Conversation>> {
        if(!::draftPager.isInitialized) {
            draftPager = Pager(
                config=PagingConfig(
                    pageSize,
                    prefetchDistance,
                    enablePlaceholder,
                    initialLoadSize,
                    maxSize
                ),
                pagingSourceFactory = {
                    Datastore.getDatastore(context).conversationDao()
                        .getDraftsPagingSource()
                }
            ).flow.cachedIn(viewModelScope)
        }
        return draftPager
    }

    fun getMutedPagingSource(context: Context): Flow<PagingData<Conversation>> {
        if(!::mutedPager.isInitialized) {
            mutedPager = Pager(
                config=PagingConfig(
                    pageSize,
                    prefetchDistance,
                    enablePlaceholder,
                    initialLoadSize,
                    maxSize
                ),
                pagingSourceFactory = {
                    Datastore.getDatastore(context).conversationDao()
                        .getMutedPagingSource()
                }
            ).flow.cachedIn(viewModelScope)
        }
        return mutedPager
    }

    fun getRemoteListenersPagingSource(context: Context): Flow<PagingData<Conversation>> {
        if(!::remoteListenerPager.isInitialized) {
            remoteListenerPager = Pager(
                config=PagingConfig(
                    pageSize,
                    prefetchDistance,
                    enablePlaceholder,
                    initialLoadSize,
                    maxSize
                ),
                pagingSourceFactory = {
                    Datastore.getDatastore(context).conversationDao()
                        .getRemoteListenersPagingSource()
                }
            ).flow.cachedIn(viewModelScope)
        }
        return remoteListenerPager
    }

    fun getThread(context: Context): List<Conversation> {
        return Datastore.getDatastore(context).conversationDao().getAll(threadId)
    }

    fun get(context: Context): List<Conversation> {
        return Datastore.getDatastore(context).conversationDao().getComplete()
    }

    fun getConversationLivePaging(context: Context): Flow<PagingData<Conversation>> {
        if(!::conversationsPager.isInitialized) {
            conversationsPager = Pager(
                config=PagingConfig(
                    pageSize,
                    prefetchDistance,
                    enablePlaceholder,
                    initialLoadSize,
                    maxSize
                ),
                pagingSourceFactory = {
                    Datastore.getDatastore(context).conversationDao()
                        .getConversationPaging(threadId)
                }
            ).flow.cachedIn(viewModelScope)
        }
        return conversationsPager
    }

    fun insert(context: Context, conversation: Conversation): Long {
        return Datastore.getDatastore(context).conversationDao()._insert(conversation)
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

    fun isArchived(context: Context, threadId: String? = null) : Boolean {
        val datastore = Datastore.getDatastore(context)
        val thread = datastore.threadsConfigurationsDao().get(threadId ?: this.threadId)
        if(thread != null)
            return thread.isArchive
        return false
    }

   fun isMuted(context: Context, threadId: String? = null) : Boolean {
       val datastore = Datastore.getDatastore(context)
       val thread = datastore.threadsConfigurationsDao().get(threadId ?: this.threadId)
       if(thread != null)
           return thread.isMute
       return false
    }

    fun unMute(context: Context, threadIds: List<String>) {
        val datastore = Datastore.getDatastore(context)
        var threadsConfigurationsList: MutableList<ThreadsConfigurations> = arrayListOf()
        threadIds.forEach { id ->
            var threadsConfigurations: ThreadsConfigurations? =
                datastore.threadsConfigurationsDao().get(id)

            if(threadsConfigurations != null) {
                threadsConfigurations.isMute = false
            }
            else {
                threadsConfigurations = ThreadsConfigurations().apply {
                    threadId = id
                    isMute = false
                }
            }
            threadsConfigurationsList.add(threadsConfigurations)
        }
        Datastore.getDatastore(context).threadsConfigurationsDao().insert(threadsConfigurationsList)
    }

    fun unMute(context: Context, threadId: String? = null) {
        unMute(context, listOf(threadId ?: this.threadId))
    }

    fun mute(context: Context, threadIds: List<String>) {
        val datastore = Datastore.getDatastore(context)
        var threadsConfigurationsList: MutableList<ThreadsConfigurations> = arrayListOf()
        threadIds.forEach { id ->
            var threadsConfigurations: ThreadsConfigurations? =
                datastore.threadsConfigurationsDao().get(id)

            if(threadsConfigurations != null) {
                threadsConfigurations.isMute = true
            }
            else {
                threadsConfigurations = ThreadsConfigurations().apply {
                    threadId = id
                    isMute = true
                }
            }
            threadsConfigurationsList.add(threadsConfigurations)
        }
        Datastore.getDatastore(context).threadsConfigurationsDao().insert(threadsConfigurationsList)
    }

    fun mute(context: Context, threadId: String? = null) {
        mute(context, listOf(threadId ?: this.threadId))
    }

    fun archive(context: Context, threadIds: List<String>) {
        val datastore = Datastore.getDatastore(context)
        var threadsConfigurationsList: MutableList<ThreadsConfigurations> = arrayListOf()
        threadIds.forEach { id ->
            var threadsConfigurations: ThreadsConfigurations? =
                datastore.threadsConfigurationsDao().get(id)

            if(threadsConfigurations != null) {
                threadsConfigurations.isArchive = true
            }
            else {
                threadsConfigurations = ThreadsConfigurations().apply {
                    threadId = id
                    isArchive = true
                }
            }
            threadsConfigurationsList.add(threadsConfigurations)
        }
        Datastore.getDatastore(context).threadsConfigurationsDao().insert(threadsConfigurationsList)
    }

    fun archive(context: Context, threadId: String? = null) {
        archive(context, listOf(threadId ?: this.threadId))
    }

    fun unArchive(context: Context, threadIds: List<String>) {
        val datastore = Datastore.getDatastore(context)
        var threadsConfigurationsList: MutableList<ThreadsConfigurations> = arrayListOf()
        threadIds.forEach { id ->
            var threadsConfigurations: ThreadsConfigurations? =
                datastore.threadsConfigurationsDao().get(id)

            if(threadsConfigurations != null) {
                threadsConfigurations.isArchive = false
            }
            else {
                threadsConfigurations = ThreadsConfigurations().apply {
                    threadId = id
                    isArchive = false
                }
            }
            threadsConfigurationsList.add(threadsConfigurations)
        }
        Datastore.getDatastore(context).threadsConfigurationsDao().insert(threadsConfigurationsList)
    }

    fun unArchive(context: Context, threadId: String? = null) {
        unArchive(context, listOf(threadId ?: this.threadId))
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
        conversation.message_id = System.currentTimeMillis().toString()
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


    fun importAll(context: Context, detailsOnly:Boolean = false): List<Conversation> {
        val json = Json { ignoreUnknownKeys = true }
        val conversations = json.decodeFromString<MutableList<Conversation>>(importDetails)
        if(!detailsOnly) {
            val databaseConnector = Datastore.getDatastore(context)
            databaseConnector.conversationDao().insertAll(conversations)
        }
        return conversations
    }

    fun getAllExport(context: Context): String {
        val databaseConnector = Datastore.getDatastore(context)
        val conversations = databaseConnector!!.conversationDao().getComplete()

        val gsonBuilder = GsonBuilder()
        gsonBuilder.setPrettyPrinting().serializeNulls()

        val gson = gsonBuilder.create()
        return gson.toJson(conversations)
    }

    fun reset(context: Context) {
        val cursor = NativeSMSDB.fetchAll(context)

        val conversationList: MutableList<Conversation> = ArrayList<Conversation>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                conversationList.add(Conversation.Companion.build(cursor))
            } while (cursor.moveToNext())
            cursor.close()
        }

        Datastore.getDatastore(context).conversationDao().deleteEvery()
        Datastore.getDatastore(context).conversationDao().insertAll(conversationList)
//        refresh(context)
    }

    fun clear(context: Context) {
        Telephony.Sms.MESSAGE_TYPE_DRAFT
        Datastore.getDatastore(context).conversationDao().deleteEvery()
    }

    fun getMessage(context: Context, messageId: String): Conversation {
        return Datastore.getDatastore(context).conversationDao().getMessage(messageId)
    }
}

package com.afkanerd.deku.DefaultSMS.AdaptersViewModels

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.BlockedNumberContract
import android.provider.Telephony
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.window.layout.WindowLayoutInfo
import com.afkanerd.deku.ConversationsScreen
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversationsHandler
import com.afkanerd.deku.DefaultSMS.Models.NativeSMSDB
import com.afkanerd.deku.DefaultSMS.Models.SMSDatabaseWrapper
import com.afkanerd.deku.DefaultSMS.Models.SMSHandler.sendMmsMessage
import com.afkanerd.deku.DefaultSMS.Models.ThreadsConfigurations
import com.afkanerd.deku.DefaultSMS.Models.ThreadsCount
import com.afkanerd.deku.DefaultSMS.ui.Components.sendSMS
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
    var mmsImage: ByteArray? by mutableStateOf(null)
    var encryptedText by mutableStateOf("")
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

    private var conversationsPager: Flow<PagingData<Conversation>>? = null
//    private lateinit var conversationsPager: Flow<PagingData<Conversation>>

    fun getDefaultRegion(context: Context): String {
        return Helpers.getUserCountry(context)
    }

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
                        .getAllThreadingPagingSource()
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
//        if(!::conversationsPager.isInitialized) {
        if(conversationsPager == null) {
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
        return conversationsPager!!
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
        val threadsConfigurationsList: MutableList<ThreadsConfigurations> = arrayListOf()
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
        val cursor = NativeSMSDB.fetchAllSMS(context)
        val cursorMMS = NativeSMSDB.fetchAllMMS(context)

        val conversationList: MutableList<Conversation> = ArrayList<Conversation>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val conversation = Conversation.Companion.build(cursor).apply {
                    this.address = Helpers.getFormatCompleteNumber(
                        this.address!!,
                        getDefaultRegion(context)
                    )
                }
                conversationList.add(conversation)
            } while (cursor.moveToNext())
            cursor.close()
        }

        if (cursorMMS != null && cursorMMS.moveToFirst()) {
            do {
                val mmsConversation = Conversation.Companion.build(cursorMMS, true)
                val parsedMms = NativeSMSDB.ParseMMS(context, cursorMMS)
                parsedMms.buildConversation(context, mmsConversation);

                if(!mmsConversation.mmsContentUri.isNullOrEmpty() || !mmsConversation.text.isNullOrEmpty())
                    conversationList.add(mmsConversation)

            } while (cursorMMS.moveToNext())
            cursorMMS.close()
        }

        Datastore.getDatastore(context).conversationDao().reset(conversationList)
    }

    fun clear(context: Context) {
        Telephony.Sms.MESSAGE_TYPE_DRAFT
        Datastore.getDatastore(context).conversationDao().deleteEvery()
    }

    fun processIntents(
        context: Context,
        intent: Intent,
        defaultRegion: String,
    ): Triple<String?, String?, String?>?{
        if(intent.action != null &&
            ((intent.action == Intent.ACTION_SENDTO) || (intent.action == Intent.ACTION_SEND))) {
            val text = if(intent.hasExtra("sms_body")) intent.getStringExtra("sms_body")
            else if(intent.hasExtra("android.intent.extra.TEXT")) {
                intent.getStringExtra("android.intent.extra.TEXT")
            } else ""

            val sendToString = intent.dataString

            if ((sendToString != null &&
                        (sendToString.contains("smsto:") ||
                                sendToString.contains("sms:"))) ||
                intent.hasExtra("address")
            ) {
                val address = Helpers.getFormatCompleteNumber(
                    if(intent.hasExtra("address"))
                        intent.getStringExtra("address")!!
                    else sendToString!!, defaultRegion
                )
                val threadId = ThreadedConversationsHandler.get(context, address).thread_id
                return Triple(address, threadId, text)
            }
        }
        else if(intent.hasExtra("address")) {
            val text = if(intent.hasExtra("android.intent.extra.TEXT"))
                intent.getStringExtra("android.intent.extra.TEXT") else ""

            val address = intent.getStringExtra("address")
            val threadId = intent.getStringExtra("thread_id")
            return Triple(address, threadId, text)
        }
        return null
    }

    fun navigateToConversation(
        conversationsViewModel: ConversationsViewModel,
        address: String,
        threadId: String,
        subscriptionId: Int?,
        navController: NavController,
        searchQuery: String? = ""
    ) {
        conversationsViewModel.address = address
        conversationsViewModel.threadId = threadId
        conversationsViewModel.searchQuery = searchQuery ?: ""
        conversationsViewModel.subscriptionId = subscriptionId ?: -1
        conversationsViewModel.conversationsPager = null
        if(conversationsViewModel.newLayoutInfo?.displayFeatures!!.isEmpty())
            navController.navigate(ConversationsScreen)
    }

    fun loadNatives(context: Context) {
        CoroutineScope(Dispatchers.Default).launch {
            reset(context)
        }
    }

    fun sendSms(context: Context) {
        sendSMS(
            context = context,
            text = text,
            threadId = threadId,
            messageId = System.currentTimeMillis().toString(),
            address = address,
            conversationsViewModel = this
        ) {
            this.text = ""
            this.encryptedText = ""
            this.clearDraft(context)
        }
    }

    fun sendMms(context: Context, contentUri: Uri) {
        val conversation = Conversation()
        conversation.text = text
        conversation.message_id = System.currentTimeMillis().toString()
        conversation.thread_id = threadId
        conversation.subscription_id = subscriptionId
        conversation.type = Telephony.Mms.MESSAGE_BOX_OUTBOX
        conversation.date = System.currentTimeMillis().toString()
        conversation.address = address
        conversation.status = Telephony.Sms.STATUS_PENDING
        conversation.isRead = true
//        conversation.mmsImage = mmsImage
        conversation.mmsContentUri = contentUri.toString()
        conversation.mmsMimeType = context.contentResolver.getType(contentUri)
        conversation.mmsContentFilename = Helpers.getFileName(context, contentUri)

        sendMmsMessage(
            context = context,
            conversation = conversation,
            conversationsViewModel = this,
            contentUri = contentUri
        ) {
            this.text = ""
            this.mmsImage = null
            this.encryptedText = ""
            this.clearDraft(context)
        }
    }


    data class MmsContentDataClass(
        val _id: Int,
        val thread_id: Int,
        val date: Int,
        val date_sent: Int,
        val msg_box: Int,
        val read: Int,
        val m_id: String??,
        val sub: String?,
        val sub_cs: Int,
        val ct_t: String?,
        val ct_l: String?,
        val exp: String? = null,
        val m_cls: String?,
        val m_type: Int,
        val v: Int,
        val m_size: Int,
        val pri: Int,
        val rr: Int,
        val rpt_a: String? = null,
        val resp_st: String? = null,
        val st: String? = null,
        val tr_id: String? = null,
        val retr_st: String? = null,
        val retr_txt: String? = null,
        val retr_txt_cs: String? = null,
        val read_status: String? = null,
        val ct_cls: String? = null,
        val resp_txt: String? = null,
        val d_tm: String? = null,
        val d_rpt: Int,
        val locked: Int,
        val sub_id: Int,
        val seen: Int,
        val creator: String?,
        val text_only: Int,
    )

    data class SmsContentDataClass(
        val _id: Int,
        val thread_id: Int,
        val address: String?,
        val person: String? = null,
        val date: Int,
        val date_sent: Int,
        val protocol: String? = null,
        val read: Int,
        val status: Int,
        val type: Int,
        val reply_path_present: String? = null,
        val subject: String? = null,
        val body: String,
        val service_center: String? = null,
        val locked: Int,
        val sub_id: Int,
        val error_code: Int,
        val creator: String,
        val seen: Int,
    )

    data class SmsMmsContents(
        val mms: Map<String, ArrayList<MmsContentDataClass>>,
        val mms_parts: Map<String, ArrayList<MmsPartContents>>,
        val sms: Map<String, ArrayList<SmsContentDataClass>>,
    )

    data class MmsPartContents(
        val _id: Int,
        val mid: Int,
        val seq: Int,
        val ct: String?,
        val name: String?,
        val chset: Int?,
        val cd: String? = null,
        val fn: String? = null,
        val cid: String?,
        val cl: String?,
        val ctt_s: String? = null,
        val ctt_t: String? = null,
        val _data: String?,
        val text: String?,
        val sub_id: Int,
    )

    companion object {
        /**
         *
         * MMS
         * _id, thread_id, date, date_sent, msg_box, read, m_id, sub, sub_cs, ct_t, ct_l, exp,
         * m_cls, m_type, v, m_size, pri, rr, rpt_a, resp_st, st, tr_id, retr_st, retr_txt,
         * retr_txt_cs, read_status, ct_cls, resp_txt, d_tm, d_rpt, locked, sub_id, seen, creator,
         * text_only
         *
         *
         * MMS/Part
         * _id, mid, seq, ct, name, chset, cd, fn, cid, cl, ctt_s, ctt_t, _data, text, sub_id
         *
         *
         * SMS
         * _id, thread_id, address, person, date, date_sent, protocol, read, status, type,
         * reply_path_present, subject, body, service_center, locked, sub_id, error_code,
         * creator, seen
         *
         */

        fun exportRawWithColumnGuesses(context: Context): String {
            val mmsContents = arrayListOf<MmsContentDataClass>()
            val mmsPartsContents = arrayListOf<MmsPartContents>()
            val smsContents = arrayListOf<SmsContentDataClass>()

            // MMS
            context.contentResolver.query(
                Telephony.Mms.CONTENT_URI,
                null,
                null,
                null,
                null
            )?.let { cursor ->
                if(cursor.moveToFirst()) {
                    do {
                        mmsContents.add(parseRawMmsContents(cursor))
                    } while(cursor.moveToNext())
                }
                cursor.close()
            }

            // MMS/Parts
            context.contentResolver.query(
                "content://mms/part".toUri(),
                null,
                null,
                null,
                null
            )?.let { cursor ->
                if(cursor.moveToFirst()) {
                    do {
                        mmsPartsContents.add(parseRawMmsContentsParts(cursor))
                    } while(cursor.moveToNext())
                }
            }


            // SMS
            context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                null,
                null,
                null,
                null
            )?.let { cursor ->
                if(cursor.moveToFirst()) {
                    do {
                        smsContents.add(parseRawSmsContents(cursor))
                    } while(cursor.moveToNext())
                }
                cursor.close()
            }

            val smsMmsContents = SmsMmsContents(
                mapOf(Pair(Telephony.Mms.CONTENT_URI.toString(),
                    mmsContents)),

                mapOf(Pair("content://mms/part", mmsPartsContents)),

                mapOf(Pair(Telephony.Sms.CONTENT_URI.toString(),
                    smsContents)),
            )

            val gson = GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .create()
            return gson.toJson(smsMmsContents)
        }

        @SuppressLint("Range")
        private fun parseRawMmsContentsParts(cursor: Cursor): MmsPartContents {
            val _id: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Mms.Part._ID))
            val mid: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Mms.Part.MSG_ID))
            val seq: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Mms.Part.SEQ))
            val ct: String? = cursor.getStringOrNull(cursor
                .getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE))
            val name: String? = cursor.getStringOrNull(cursor
                .getColumnIndex(Telephony.Mms.Part.NAME))
            val cid: String? = cursor.getStringOrNull(cursor
                .getColumnIndex(Telephony.Mms.Part.CONTENT_ID))
            val cl: String? = cursor.getStringOrNull(cursor
                .getColumnIndex(Telephony.Mms.Part.CONTENT_ID))
            val text: String? = cursor.getStringOrNull(cursor
                .getColumnIndex(Telephony.Mms.Part.TEXT))
            val sub_id: Int = cursor.getInt(cursor
                .getColumnIndex("sub_id"))
            val _data: String? = cursor.getStringOrNull(cursor
                .getColumnIndex(Telephony.Mms.Part._DATA))
            val chset: Int? = cursor.getIntOrNull(cursor
                .getColumnIndex(Telephony.Mms.Part.CHARSET))

            return MmsPartContents(
                _id = _id,
                mid = mid,
                seq = seq,
                ct = ct,
                name = name,
                cid = cid,
                cl = cl,
                text = text,
                sub_id = sub_id,
                _data = _data,
                chset = chset,
            )
        }

        @SuppressLint("Range")
        private fun parseRawMmsContents(cursor: Cursor): MmsContentDataClass {
            val _id: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Mms._ID))
            val thread_id: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Mms.THREAD_ID))
            val date: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Mms.DATE))
            val date_sent: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Mms.DATE_SENT))
            val msg_box: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Mms.MESSAGE_BOX))
            val read: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Mms.READ))
            val m_id: String? = cursor.getStringOrNull(cursor
                .getColumnIndex(Telephony.Mms.MESSAGE_ID))
            val sub: String? = cursor.getStringOrNull(cursor
                .getColumnIndex(Telephony.Mms.SUBJECT))
            val sub_cs: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Mms.SUBJECT_CHARSET))
            val ct_t: String? = cursor.getStringOrNull(cursor
                .getColumnIndex(Telephony.Mms.CONTENT_TYPE))
            val ct_l: String? = cursor.getStringOrNull(cursor
                .getColumnIndex(Telephony.Mms.CONTENT_LOCATION))
            val m_cls: String? = cursor.getStringOrNull(cursor
                .getColumnIndex(Telephony.Mms.MESSAGE_CLASS))
            val m_type: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Mms.MESSAGE_TYPE))
            val v: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Mms.MMS_VERSION))
            val m_size: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Mms.MESSAGE_SIZE))
            val pri: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Mms.PRIORITY))
            val rr: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Mms.READ_REPORT))
            val d_rpt: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Mms.DELIVERY_REPORT))
            val locked: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Mms.LOCKED))
            val sub_id: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Mms.SUBSCRIPTION_ID))
            val seen: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Mms.SEEN))
            val creator: String? = cursor.getStringOrNull(cursor
                .getColumnIndex(Telephony.Mms.CREATOR))
            val text_only: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Mms.TEXT_ONLY))

            return MmsContentDataClass(
                _id = _id,
                thread_id = thread_id,
                date = date,
                date_sent = date_sent,
                msg_box = msg_box,
                read = read,
                m_id = m_id,
                sub = sub,
                sub_cs = sub_cs,
                ct_t = ct_t,
                ct_l = ct_l,
                m_cls = m_cls,
                m_type = m_type,
                v = v,
                m_size = m_size,
                pri = pri,
                rr = rr,
                d_rpt = d_rpt,
                locked = locked,
                sub_id = sub_id,
                seen = seen,
                creator = creator,
                text_only = text_only
            )
        }

        @SuppressLint("Range")
        private fun parseRawSmsContents(cursor: Cursor): SmsContentDataClass {
            val _id: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Sms._ID))
            val thread_id: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Sms.THREAD_ID))
            val address: String? = cursor.getString(cursor
                    .getColumnIndex(Telephony.Sms.ADDRESS))
            val date: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Sms.DATE))
            val date_sent: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Sms.DATE_SENT))
            val read: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Sms.READ))
            val status: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Sms.STATUS))
            val type: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Sms.TYPE))
            val body: String = cursor.getString(cursor
                .getColumnIndex(Telephony.Sms.BODY))
            val locked: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Sms.LOCKED))
            val sub_id: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID))
            val error_code: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Sms.ERROR_CODE))
            val creator: String = cursor.getString(cursor
                    .getColumnIndex(Telephony.Sms.CREATOR))
            val seen: Int = cursor.getInt(cursor
                .getColumnIndex(Telephony.Sms.SEEN))

            return SmsContentDataClass(
                _id = _id,
                thread_id = thread_id,
                address = address,
                date = date,
                date_sent = date_sent,
                read = read,
                status = status,
                type = type,
                body = body,
                locked = locked,
                sub_id = sub_id,
                error_code = error_code,
                creator = creator,
                seen = seen
            )
        }
    }

}

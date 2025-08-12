package com.afkanerd.deku.DefaultSMS.AdaptersViewModels

import androidx.lifecycle.ViewModel


class ConversationsViewModel : ViewModel() {
//    var threadId by mutableStateOf("")
//    var address by mutableStateOf("")
//    var text by mutableStateOf("")
//    var data by mutableStateOf<ByteArray?>(null)
//    var mmsImage: ByteArray? by mutableStateOf(null)
//    var encryptedText by mutableStateOf("")
//    var searchQuery by mutableStateOf("")
//    var subscriptionId: Int by mutableIntStateOf(-1)
//
//    var importDetails by mutableStateOf("")
//
//    var selectedItems = mutableStateListOf<String>()
//    var retryDeleteItem: MutableList<Conversation> = arrayListOf()
//    var selectedMessage: Conversation? = null
//
//    var liveData: LiveData<MutableList<Conversation>>? = null
//    var remoteListenersLiveData: LiveData<MutableList<Conversation>>? = null
//
//    var inboxType: InboxType = InboxType.INBOX
//
//    var newLayoutInfo: WindowLayoutInfo? = null
//
//    private val _newIntent = MutableStateFlow<Intent?>(null)
//    var newIntent: StateFlow<Intent?> = _newIntent
//
//    var pageSize: Int = 10
//    var prefetchDistance: Int = 3 * pageSize
//    var enablePlaceholder: Boolean = true
//    var initialLoadSize: Int = 2 * pageSize
//    var maxSize: Int = PagingConfig.Companion.MAX_SIZE_UNBOUNDED
//
////    lateinit var threadingPager: Pager<Int, Conversation>
////    private lateinit var threadingPager: Flow<PagingData<Conversation>>
////    private lateinit var archivedPager: Flow<PagingData<Conversation>>
////    private lateinit var encryptedPager: Flow<PagingData<Conversation>>
////    private lateinit var draftPager: Flow<PagingData<Conversation>>
////    private lateinit var mutedPager: Flow<PagingData<Conversation>>
////    private lateinit var remoteListenerPager: Flow<PagingData<Conversation>>
//
////    private lateinit var conversationsPager: Flow<PagingData<Conversation>>
//
//    fun setNewIntent(intent: Intent?) {
//        _newIntent.value = intent
//    }
//
////    fun getInboxType(isDefault: Boolean = false): InboxType {
////        inboxType = if(remoteListenersLiveData?.value?.isNotEmpty() == true && !isDefault) {
////            InboxType.REMOTE_LISTENER
////        } else InboxType.INBOX
////        return inboxType
////    }
//
////    fun getThreadingPagingSource(context: Context): Flow<PagingData<Conversation>> {
////        if(!::threadingPager.isInitialized) {
////            threadingPager = Pager(
////                config=PagingConfig(
////                    pageSize,
////                    prefetchDistance,
////                    enablePlaceholder,
////                    initialLoadSize,
////                    maxSize
////                ),
////                pagingSourceFactory = {
////                    Datastore.getDatastore(context).conversationDao()
////                        .getAllThreadingPagingSource()
////                }
////            ).flow.cachedIn(viewModelScope)
////        }
////        return threadingPager
////    }
////
////    fun getArchivedPagingSource(context: Context): Flow<PagingData<Conversation>> {
////        if(!::archivedPager.isInitialized) {
////            archivedPager = Pager(
////                config=PagingConfig(
////                    pageSize,
////                    prefetchDistance,
////                    enablePlaceholder,
////                    initialLoadSize,
////                    maxSize
////                ),
////                pagingSourceFactory = {
////                    Datastore.getDatastore(context).conversationDao()
////                        .getArchivedPagingSource()
////                }
////            ).flow.cachedIn(viewModelScope)
////        }
////        return archivedPager
////    }
////
////    fun getEncryptedPagingSource(context: Context): Flow<PagingData<Conversation>> {
////        if(!::encryptedPager.isInitialized) {
////            encryptedPager = Pager(
////                config=PagingConfig(
////                    pageSize,
////                    prefetchDistance,
////                    enablePlaceholder,
////                    initialLoadSize,
////                    maxSize
////                ),
////                pagingSourceFactory = {
////                    Datastore.getDatastore(context).conversationDao()
////                        .getAllThreadingPagingSource()
////                }
////            ).flow.cachedIn(viewModelScope)
////        }
////        return encryptedPager
////    }
////
////    fun getDraftPagingSource(context: Context): Flow<PagingData<Conversation>> {
////        if(!::draftPager.isInitialized) {
////            draftPager = Pager(
////                config=PagingConfig(
////                    pageSize,
////                    prefetchDistance,
////                    enablePlaceholder,
////                    initialLoadSize,
////                    maxSize
////                ),
////                pagingSourceFactory = {
////                    Datastore.getDatastore(context).conversationDao()
////                        .getDraftsPagingSource()
////                }
////            ).flow.cachedIn(viewModelScope)
////        }
////        return draftPager
////    }
////
////    fun getMutedPagingSource(context: Context): Flow<PagingData<Conversation>> {
////        if(!::mutedPager.isInitialized) {
////            mutedPager = Pager(
////                config=PagingConfig(
////                    pageSize,
////                    prefetchDistance,
////                    enablePlaceholder,
////                    initialLoadSize,
////                    maxSize
////                ),
////                pagingSourceFactory = {
////                    Datastore.getDatastore(context).conversationDao()
////                        .getMutedPagingSource()
////                }
////            ).flow.cachedIn(viewModelScope)
////        }
////        return mutedPager
////    }
////
////    fun getRemoteListenersPagingSource(context: Context): Flow<PagingData<Conversation>> {
////        if(!::remoteListenerPager.isInitialized) {
////            remoteListenerPager = Pager(
////                config=PagingConfig(
////                    pageSize,
////                    prefetchDistance,
////                    enablePlaceholder,
////                    initialLoadSize,
////                    maxSize
////                ),
////                pagingSourceFactory = {
////                    Datastore.getDatastore(context).conversationDao()
////                        .getRemoteListenersPagingSource()
////                }
////            ).flow.cachedIn(viewModelScope)
////        }
////        return remoteListenerPager
////    }
////
//
//    fun getThread(context: Context): List<Conversation> {
//        return Datastore.getDatastore(context).conversationDao().getAll(threadId)
//    }
//
//    fun get(context: Context): List<Conversation> {
//        return Datastore.getDatastore(context).conversationDao().getComplete()
//    }
//
//
//    fun update(context: Context, conversation: Conversation) {
//        Datastore.getDatastore(context).conversationDao()._update(conversation)
//    }
//
//    fun getUnreadCount(context: Context, threadId: String) : Int {
//        return Datastore.getDatastore(context).conversationDao().getUnreadCount(threadId)
//    }
//
//
//    fun fetchDraft(context: Context): Conversation? {
//        return Datastore.getDatastore(context).conversationDao().fetchTypedConversation(
//            Telephony.TextBasedSmsColumns.MESSAGE_TYPE_DRAFT, threadId
//        )
//    }
//
//    fun clearDraft(context: Context) {
//        Datastore.getDatastore(context).conversationDao()
//            .deleteAllType(context, Telephony.TextBasedSmsColumns.MESSAGE_TYPE_DRAFT, threadId)
//        SMSDatabaseWrapper.deleteDraft(context, threadId)
//    }
//
//    fun isArchived(context: Context, threadId: String? = null) : Boolean {
//        val datastore = Datastore.getDatastore(context)
//        val thread = datastore.threadsConfigurationsDao().get(threadId ?: this.threadId)
//        if(thread != null)
//            return thread.isArchive
//        return false
//    }
//
//   fun isMuted(context: Context, threadId: String? = null) : Boolean {
//        val datastore = Datastore.getDatastore(context)
//        val thread = datastore.threadsConfigurationsDao().get(threadId ?: this.threadId)
//        if(thread != null)
//           return thread.isMute
//        return false
//   }
//
//    fun unMute(context: Context, threadIds: List<String>) {
//        val datastore = Datastore.getDatastore(context)
//        var threadsConfigurationsList: MutableList<ThreadsConfigurations> = arrayListOf()
//        threadIds.forEach { id ->
//            var threadsConfigurations: ThreadsConfigurations? =
//                datastore.threadsConfigurationsDao().get(id)
//
//            if(threadsConfigurations != null) {
//                threadsConfigurations.isMute = false
//            }
//            else {
//                threadsConfigurations = ThreadsConfigurations().apply {
//                    threadId = id
//                    isMute = false
//                }
//            }
//            threadsConfigurationsList.add(threadsConfigurations)
//        }
//        Datastore.getDatastore(context).threadsConfigurationsDao().insert(threadsConfigurationsList)
//    }
//
//    fun unMute(context: Context, threadId: String? = null) {
//        unMute(context, listOf(threadId ?: this.threadId))
//    }
//
//    fun mute(context: Context, threadIds: List<String>) {
//        val datastore = Datastore.getDatastore(context)
//        var threadsConfigurationsList: MutableList<ThreadsConfigurations> = arrayListOf()
//        threadIds.forEach { id ->
//            var threadsConfigurations: ThreadsConfigurations? =
//                datastore.threadsConfigurationsDao().get(id)
//
//            if(threadsConfigurations != null) {
//                threadsConfigurations.isMute = true
//            }
//            else {
//                threadsConfigurations = ThreadsConfigurations().apply {
//                    threadId = id
//                    isMute = true
//                }
//            }
//            threadsConfigurationsList.add(threadsConfigurations)
//        }
//        Datastore.getDatastore(context).threadsConfigurationsDao().insert(threadsConfigurationsList)
//    }
//
//    fun mute(context: Context, threadId: String? = null) {
//        mute(context, listOf(threadId ?: this.threadId))
//    }
//
//    fun archive(context: Context, threadIds: List<String>) {
//        val datastore = Datastore.getDatastore(context)
//        val threadsConfigurationsList: MutableList<ThreadsConfigurations> = arrayListOf()
//        threadIds.forEach { id ->
//            var threadsConfigurations: ThreadsConfigurations? =
//                datastore.threadsConfigurationsDao().get(id)
//
//            if(threadsConfigurations != null) {
//                threadsConfigurations.isArchive = true
//            }
//            else {
//                threadsConfigurations = ThreadsConfigurations().apply {
//                    threadId = id
//                    isArchive = true
//                }
//            }
//            threadsConfigurationsList.add(threadsConfigurations)
//        }
//        Datastore.getDatastore(context).threadsConfigurationsDao().insert(threadsConfigurationsList)
//    }
//
//    fun archive(context: Context, threadId: String? = null) {
//        archive(context, listOf(threadId ?: this.threadId))
//    }
//
//    fun unArchive(context: Context, threadIds: List<String>) {
//        val datastore = Datastore.getDatastore(context)
//        var threadsConfigurationsList: MutableList<ThreadsConfigurations> = arrayListOf()
//        threadIds.forEach { id ->
//            var threadsConfigurations: ThreadsConfigurations? =
//                datastore.threadsConfigurationsDao().get(id)
//
//            if(threadsConfigurations != null) {
//                threadsConfigurations.isArchive = false
//            }
//            else {
//                threadsConfigurations = ThreadsConfigurations().apply {
//                    threadId = id
//                    isArchive = false
//                }
//            }
//            threadsConfigurationsList.add(threadsConfigurations)
//        }
//        Datastore.getDatastore(context).threadsConfigurationsDao().insert(threadsConfigurationsList)
//    }
//
//    fun unArchive(context: Context, threadId: String? = null) {
//        unArchive(context, listOf(threadId ?: this.threadId))
//    }
//
//    fun deleteThread(context: Context) {
//        Datastore.getDatastore(context).conversationDao().deleteThread(threadId)
//        NativeSMSDB.deleteThreads(context, arrayOf(threadId))
//    }
//
//    fun deleteThreads(context: Context, ids: List<String>) {
//        Datastore.getDatastore(context).conversationDao().deleteAllThreads(ids)
//        NativeSMSDB.deleteThreads(context, ids.toTypedArray())
//    }
//
//    fun delete(context: Context, conversation: Conversation) {
//        Datastore.getDatastore(context).conversationDao().delete(conversation)
//        NativeSMSDB.deleteMultipleMessages(context, arrayOf(conversation.message_id))
//    }
//
//    fun delete(context: Context, conversations: List<Conversation>) {
//        Datastore.getDatastore(context).conversationDao().delete(conversations)
//        val ids: Array<String> = conversations.map { it.message_id!! }.toTypedArray()
//        NativeSMSDB.deleteMultipleMessages(context, ids)
//    }
//
//    fun insertDraft(context: Context) {
//        val conversation = Conversation();
//        conversation.message_id = System.currentTimeMillis().toString()
//        conversation.thread_id = threadId
//        conversation.text = text
//        conversation.isRead = true
//        conversation.type = Telephony.Sms.MESSAGE_TYPE_DRAFT
//        conversation.date = System.currentTimeMillis().toString()
//        conversation.address = address
//        conversation.status = Telephony.Sms.STATUS_PENDING
//
//        addSms(context, conversation);
//        SMSDatabaseWrapper.saveDraft(context, conversation);
//    }
//
//    private var folderMetrics: MutableLiveData<ThreadsCount> = MutableLiveData()
//    fun getCount(context: Context) : MutableLiveData<ThreadsCount> {
//        val databaseConnector = Datastore.getDatastore(context)
//        CoroutineScope(Dispatchers.Default).launch {
//            folderMetrics.postValue(databaseConnector.conversationDao().getFullCounts())
//        }
//        return folderMetrics
//    }
//
//    fun updateToRead(context: Context) {
//        Datastore.getDatastore(context).conversationDao().updateRead(true, threadId)
//    }
//
//    fun unblock(context: Context) {
//        BlockedNumberContract.unblock(context, this.address)
//    }
//
//    fun unblock(context: Context, addresses: List<String>) {
//        for (address in addresses) {
//            BlockedNumberContract.unblock(context, address)
//        }
//    }
//
//
//    fun clear(context: Context) {
//        Telephony.Sms.MESSAGE_TYPE_DRAFT
//        Datastore.getDatastore(context).conversationDao().deleteEvery()
//    }
//
//    fun processIntents(
//        context: Context,
//        intent: Intent,
//        defaultRegion: String,
//    ): Triple<String?, String?, String?>?{
//        if(intent.action != null &&
//            ((intent.action == Intent.ACTION_SENDTO) || (intent.action == Intent.ACTION_SEND))) {
//            val text = if(intent.hasExtra("sms_body")) intent.getStringExtra("sms_body")
//            else if(intent.hasExtra("android.intent.extra.TEXT")) {
//                intent.getStringExtra("android.intent.extra.TEXT")
//            } else ""
//
//            val sendToString = intent.dataString
//
//            if ((sendToString != null &&
//                        (sendToString.contains("smsto:") ||
//                                sendToString.contains("sms:"))) ||
//                intent.hasExtra("address")
//            ) {
//                val address = Helpers.getFormatCompleteNumber(
//                    if(intent.hasExtra("address"))
//                        intent.getStringExtra("address")!!
//                    else sendToString!!, defaultRegion
//                )
//                val threadId = ThreadedConversationsHandler.get(context, address).thread_id
//                return Triple(address, threadId, text)
//            }
//        }
//        else if(intent.hasExtra("address")) {
//            val text = if(intent.hasExtra("android.intent.extra.TEXT"))
//                intent.getStringExtra("android.intent.extra.TEXT") else ""
//
//            val address = intent.getStringExtra("address")
//            val threadId = intent.getStringExtra("thread_id")
//            return Triple(address, threadId, text)
//        }
//        return null
//    }
//
//    fun navigateToConversation(
//        conversationsViewModel: ConversationsViewModel,
//        address: String,
//        threadId: String,
//        subscriptionId: Int?,
//        navController: NavController,
//        searchQuery: String? = ""
//    ) {
//        conversationsViewModel.address = address
//        conversationsViewModel.threadId = threadId
//        conversationsViewModel.searchQuery = searchQuery ?: ""
//        conversationsViewModel.subscriptionId = subscriptionId ?: -1
//        conversationsViewModel.conversationsPager = null
//        if(conversationsViewModel.newLayoutInfo?.displayFeatures!!.isEmpty())
//            navController.navigate(ConversationsScreen)
//    }
//
//    fun loadNatives(context: Context) {
//        CoroutineScope(Dispatchers.Default).launch {
//            reset(context)
//        }
//    }
//
//    fun sendData(context: Context) {
//        if (data == null) return
//
//        val conversation = Conversations(sms = smsMmsNatives.Sms(
//            _id = (System.currentTimeMillis() / 1000).toInt(),
//            thread_id = threadId.toInt(),
//            address = address,
//            date = (System.currentTimeMillis() / 1000).toInt(),
//            date_sent = 0,
//            read = 1,
//            status = Telephony.Sms.STATUS_PENDING,
//            type = Telephony.Sms.MESSAGE_TYPE_OUTBOX,
//            body = "",
//            sub_id = subscriptionId,
//        ), sms_data_ = data)
//
//
//        val address = getFormatForTransmission(
//            address,
//            getUserCountry(context)
//        )
//
//        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            context.getSystemService(SmsManager::class.java)
//                .createForSubscriptionId(subscriptionId)
//        } else {
//            SmsManager.getSmsManagerForSubscriptionId( subscriptionId)
//        }
//
//        val sentIntent = Intent(DATA_SENT_BROADCAST_INTENT)
//        sentIntent.setPackage(context.packageName)
//        sentIntent.putExtra(NativeSMSDB.ID, conversation.sms!!._id)
//
//        val deliveredIntent = Intent(DATA_DELIVERED_BROADCAST_INTENT)
//        deliveredIntent.setPackage(context.packageName)
//        deliveredIntent.putExtra("id", conversation.sms!!._id)
//
//        val sentPendingIntent = PendingIntent.getBroadcast(
//            context,
//            conversation.sms!!._id.toLong().toInt(),
//            sentIntent,
//            PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val deliveredPendingIntent = PendingIntent.getBroadcast(
//            context,
//            conversation.sms!!._id.toLong().toInt(),
//            deliveredIntent,
//            PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val dataTransmissionPort: Short = 8200
//        try {
//            smsManager.sendDataMessage(
//                address,
//                null,
//                dataTransmissionPort,
//                data,
//                sentPendingIntent,
//                deliveredPendingIntent
//            )
//        } catch (e: Exception) {
//            throw Exception(e)
//        }
//    }
//
//    fun sendSms(
//        context: Context,
//    ) {
//        val address = getFormatForTransmission(
//            address,
//            getUserCountry(context)
//        )
//
//        val conversation = Conversations(sms = smsMmsNatives.Sms(
//            _id = (System.currentTimeMillis() / 1000).toInt(),
//            thread_id = threadId.toInt(),
//            address = address,
//            date = (System.currentTimeMillis() / 1000).toInt(),
//            date_sent = 0,
//            read = 1,
//            status = Telephony.Sms.STATUS_PENDING,
//            type = Telephony.Sms.MESSAGE_TYPE_OUTBOX,
//            body = text,
//            sub_id = subscriptionId,
//        ))
//
//        viewModelScope.launch {
//            try {
//                this@ConversationsViewModel.addSms(context, conversation)
//            } catch (e: Exception) {
//                e.printStackTrace()
//                return@launch
//            }
//
//            this@ConversationsViewModel.text = ""
//            this@ConversationsViewModel.encryptedText = ""
//            this@ConversationsViewModel.clearDraft(context)
//
//            val payload = E2EEHandler.encryptMessage(context, text, address)
//
//            val settings = Settings()
//            settings.subscriptionId = subscriptionId
//            settings.group = false
//            settings.deliveryReports = true
//            settings.useSystemSending = true
//
//            val message = Message()
//            message.text = payload.first
//            message.addresses = arrayOf(address)
//
//            val transaction = Transaction(context, settings)
//            transaction.sendNewMessage(message)
//
//        }
//    }
//
//    fun sendMms(context: Context, contentUri: Uri) {
//        val address = getFormatForTransmission(
//            address,
//            getUserCountry(context)
//        )
//        val conversation = Conversations(
//            mms = smsMmsNatives.Mms(
//                _id = (System.currentTimeMillis() / 1000).toInt(),
//                thread_id = threadId.toInt(),
//                date = (System.currentTimeMillis() / 1000).toInt(),
//                date_sent = 0,
//                msg_box = Telephony.Mms.MESSAGE_BOX_OUTBOX,
//                read = 1,
//                sub_id = subscriptionId,
//                seen = 1,
//            ),
//            mms_content_uri = contentUri.toString(),
//            mms_mimetype = context.contentResolver.getType(contentUri),
//            mms_filename = Helpers.getFileName(context, contentUri),
//        )
//
//        viewModelScope.launch {
//            try {
//                this@ConversationsViewModel.addSms(context, conversation)
//            } catch (e: Exception) {
//                e.printStackTrace()
//                return@launch
//            }
//
//            this@ConversationsViewModel.text = ""
//            this@ConversationsViewModel.mmsImage = null
//            this@ConversationsViewModel.encryptedText = ""
//            this@ConversationsViewModel.clearDraft(context)
//
//            val sendSettings = mmsParser.getSendMessageSettings()
//            sendSettings.subscriptionId = subscriptionId
//
//            val intent = Intent(context, MmsSentReceiverImpl::class.java)
//                .apply {
//                    this.putExtra(
//                        MmsSentReceiverImpl.EXTRA_ORIGINAL_RESENT_MESSAGE_ID,
//                        conversation.mms!!._id,
//                    )
//            }
//
//            val sendTransaction = Transaction(context, sendSettings)
//            sendTransaction .setExplicitBroadcastForSentMms(intent)
//
//            val mMessage = Message(text, address)
//            val mimeType = context.contentResolver.getType(contentUri)
//            val filename = mmsParser.getFileName(context, contentUri)
//
//            mMessage.addMedia(
//                mmsParser.getBytesFromUri(context, contentUri),
//                mimeType,
//                filename
//            )
//
//            try {
//                sendTransaction.sendNewMessage(mMessage)
//            } catch(e: Exception) {
//                e.printStackTrace()
//            }
//
//        }
//    }
}

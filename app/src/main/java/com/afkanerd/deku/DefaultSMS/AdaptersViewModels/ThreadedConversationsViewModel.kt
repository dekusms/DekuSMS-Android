package com.afkanerd.deku.DefaultSMS.AdaptersViewModels

import android.content.Context
import android.content.Intent
import android.provider.BlockedNumberContract
import android.provider.Telephony
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.Models.Archive
import com.afkanerd.deku.DefaultSMS.Models.Contacts
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation.Companion.build
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversations
import com.afkanerd.deku.DefaultSMS.Models.E2EEHandler.isSecured
import com.afkanerd.deku.DefaultSMS.Models.NativeSMSDB
import com.afkanerd.deku.DefaultSMS.Models.SMSDatabaseWrapper
import com.afkanerd.deku.DefaultSMS.ui.InboxType
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.ArrayList

class ThreadedConversationsViewModel : ViewModel() {

    var intent:Intent? = Intent()
    var inboxType: InboxType = InboxType.INBOX

    private var databaseConnector: Datastore? = null

    private var threadsLiveData: LiveData<MutableList<ThreadedConversations>>? = null
    var archivedLiveData: LiveData<MutableList<ThreadedConversations>>? = null
    var encryptedLiveData: LiveData<MutableList<ThreadedConversations>>? = null
    var blockedLiveData: LiveData<MutableList<ThreadedConversations>>? = null

    fun getAll(context: Context): List<ThreadedConversations> {
        return Datastore.getDatastore(context).threadedConversationsDao().getAll()
    }

    fun getAllLiveData(context: Context):
            LiveData<MutableList<ThreadedConversations>> {
        if (threadsLiveData == null) {
            threadsLiveData = Datastore.getDatastore(context).threadedConversationsDao().getInbox()
            archivedLiveData = Datastore.getDatastore(context).threadedConversationsDao().getArchived()
            encryptedLiveData = Datastore.getDatastore(context).threadedConversationsDao().getEncrypted()
            blockedLiveData = Datastore.getDatastore(context).threadedConversationsDao().getBlocked()
        }
        return threadsLiveData!!
    }

    fun getAllExport(): String? {
        val conversations = databaseConnector!!.conversationDao().getComplete()

        val gsonBuilder = GsonBuilder()
        gsonBuilder.setPrettyPrinting().serializeNulls()

        val gson = gsonBuilder.create()
        return gson.toJson(conversations)
    }

    fun insert(threadedConversations: ThreadedConversations) {
        databaseConnector!!.threadedConversationsDao()._insert(threadedConversations)
    }

    fun reset(context: Context) {
        val cursor = NativeSMSDB.fetchAll(context)

        val conversationList: MutableList<Conversation> = ArrayList<Conversation>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                conversationList.add(build(cursor))
            } while (cursor.moveToNext())
            cursor.close()
        }

        Datastore.getDatastore(context).conversationDao().insertAll(conversationList)
        refresh(context)
    }

    fun archive(archiveList: List<Archive>) {
        databaseConnector!!.threadedConversationsDao().archive(archiveList)
    }

    fun archive(id: String) {
        val archive = Archive()
        archive.thread_id = id
        archive.is_archived = true
        databaseConnector!!.threadedConversationsDao()
            .archive(ArrayList<Archive>(mutableListOf<Archive>(archive)))
    }


    fun delete(context: Context, ids: List<String>) {
        Datastore.getDatastore(context).threadedConversationsDao().delete(context, ids)
        NativeSMSDB.deleteThreads(context, ids.toTypedArray<String?>())
    }

    private fun refresh(context: Context) {
        try {
            val cursor = context.contentResolver.query(
                Telephony.Threads.CONTENT_URI,
                null,
                null,
                null,
                "date DESC"
            )

            /*
                [date, rr, sub, subject, ct_t, read_status, reply_path_present, body, type, msg_box,
                thread_id, sub_cs, resp_st, retr_st, text_only, locked, exp, m_id, retr_txt_cs, st,
                date_sent, read, ct_cls, m_size, rpt_a, address, sub_id, pri, tr_id, resp_txt, ct_l,
                m_cls, d_rpt, v, person, service_center, error_code, _id, m_type, status]
                 */
            val threadedConversationsList: MutableList<ThreadedConversations> =
                ArrayList<ThreadedConversations>()
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val threadedConversations = ThreadedConversations()
                    val recipientIdIndex = cursor.getColumnIndex("address")
                    val snippetIndex = cursor.getColumnIndex("body")
                    val dateIndex = cursor.getColumnIndex("date")
                    val threadIdIndex = cursor.getColumnIndex("thread_id")
                    val typeIndex = cursor.getColumnIndex("type")
                    val readIndex = cursor.getColumnIndex("read")
                    val sub_id = cursor.getColumnIndex("sub_id")

                    threadedConversations.isIs_read = cursor.getInt(readIndex) == 1

                    threadedConversations.address = cursor.getString(recipientIdIndex)
                    if (threadedConversations.address == null || threadedConversations.address
                            .isEmpty()
                    ) continue
                    threadedConversations.thread_id = cursor.getString(threadIdIndex)
                    threadedConversations.snippet = cursor.getString(snippetIndex)
                    threadedConversations.type = cursor.getInt(typeIndex)
                    threadedConversations.date = cursor.getString(dateIndex)
                    if (BlockedNumberContract.isBlocked(
                            context,
                            threadedConversations.address
                        )
                    ) threadedConversations.isIs_blocked = true

//                    val contactName = Contacts.retrieveContactName(
//                        context,
//                        threadedConversations.address
//                    )
//                    threadedConversations.contact_name = contactName
                    threadedConversations.msg_count =
                        getUnreadCountNative(context, threadedConversations.thread_id)
                    threadedConversations.subscription_id = cursor.getInt(sub_id)
                    threadedConversationsList.add(threadedConversations)
                } while (cursor.moveToNext())
                cursor.close()
            }
            databaseConnector = Datastore.getDatastore(context)
            databaseConnector!!.threadedConversationsDao().deleteAll()
            databaseConnector!!.threadedConversationsDao().insertAll(threadedConversationsList)
            getCount(context)
        } catch (e: Exception) {
            Log.e(javaClass.getName(), "Exception refreshing", e)
            loadNative(context)
        }
    }

    private fun getUnreadCountNative(context: Context, threadId: String): Int {
        val cursor = NativeSMSDB.countUnreadForThread(context, threadId)
        if(cursor != null) return cursor.count
        return 0;
    }

    private fun loadNative(context: Context) {
        try {
            NativeSMSDB.fetchAll(context).use { cursor ->
                val threadedConversations =
                    ThreadedConversations.buildRaw(cursor)
                databaseConnector!!.threadedConversationsDao().insertAll(threadedConversations)
            }
        } catch (e: Exception) {
            Log.e(javaClass.getName(), "Exception loading native", e)
        }
    }

    fun unarchive(archiveList: MutableList<Archive>) {
        databaseConnector!!.threadedConversationsDao().unarchive(archiveList)
    }

    fun unblock(context: Context, threadIds: MutableList<String>) {
        val threadedConversationsList =
            databaseConnector!!.threadedConversationsDao().getList(threadIds)
        for (threadedConversations in threadedConversationsList) {
            BlockedNumberContract.unblock(context, threadedConversations.address)
            threadedConversations.isIs_blocked = false
            databaseConnector!!.threadedConversationsDao().update(context, threadedConversations)
        }
    }

    fun clearDrafts(context: Context?) {
        SMSDatabaseWrapper.deleteAllDraft(context)
        databaseConnector!!.threadedConversationsDao()
            .clearDrafts(Telephony.TextBasedSmsColumns.MESSAGE_TYPE_DRAFT)
    }

    fun hasUnread(ids: MutableList<String>): Boolean {
        return databaseConnector!!.threadedConversationsDao().getCountUnread(ids) > 0
    }

    fun markUnRead(context: Context?, threadIds: MutableList<String>) {
        NativeSMSDB.Incoming.update_all_read(context, 0, threadIds.toTypedArray<String?>())
        databaseConnector!!.threadedConversationsDao().updateRead(0, threadIds)
    }

    fun markRead(context: Context?, threadIds: MutableList<String>) {
        NativeSMSDB.Incoming.update_all_read(context, 1, threadIds.toTypedArray<String?>())
        databaseConnector!!.threadedConversationsDao().updateRead(1, threadIds)
    }

    fun markAllRead(context: Context?) {
        NativeSMSDB.Incoming.update_all_read(context, 1)
        databaseConnector!!.threadedConversationsDao().updateRead(1)
    }

    private var folderMetrics: MutableLiveData<MutableList<Int>> =
        MutableLiveData<MutableList<Int>>()
    fun getCount(context: Context) : LiveData<MutableList<Int>> {
        databaseConnector = Datastore.getDatastore(context)
        CoroutineScope(Dispatchers.Default).launch {
            val draftsListCount = databaseConnector!!.threadedConversationsDao()
                .getThreadedDraftsListCount(Telephony.TextBasedSmsColumns.MESSAGE_TYPE_DRAFT)
            val encryptedCount = databaseConnector!!.threadedConversationsDao().getCountEncrypted()
            val unreadCount = databaseConnector!!.threadedConversationsDao().getCountUnread()
            val blockedCount = databaseConnector!!.threadedConversationsDao().getCountBlocked()
            val mutedCount = databaseConnector!!.threadedConversationsDao().getCountMuted()
            val list: MutableList<Int> = ArrayList<Int>()
            // 0
            list.add(draftsListCount)

            // 1
            list.add(encryptedCount)

            // 2
            list.add(unreadCount)

            // 3
            list.add(blockedCount)

            // 4
            list.add(mutedCount)
            folderMetrics.postValue(list)
        }
        return folderMetrics
    }

    fun unMute(threadIds: MutableList<String>) {
        databaseConnector!!.threadedConversationsDao().updateMuted(0, threadIds)
    }

    fun mute(threadIds: MutableList<String>) {
        databaseConnector!!.threadedConversationsDao().updateMuted(1, threadIds)
    }

    fun unMuteAll() {
        databaseConnector!!.threadedConversationsDao().updateUnMuteAll()
    }

    fun getAddress(threadId: String): String {
        val threads: MutableList<String> = ArrayList<String>()
        threads.add(threadId)
        val addresses = databaseConnector!!.threadedConversationsDao().findAddresses(threads)
        if (!addresses.isEmpty()) return addresses[0]
        return ""
    }

    fun updateRead(context: Context, threadId: String, isRead: Boolean = true) {
        CoroutineScope(Dispatchers.Default).launch {
            Datastore.getDatastore(context).threadedConversationsDao()
                .updateAllRead(
                    context,
                    threadId,
                    isRead
                )
        }
    }

    fun updateInformation(
        context: Context,
        threadId: String,
        contactName: String? = null,
        subscriptionId: Int? = null,
        conversationsViewModel: ConversationsViewModel? = null
    ){
        val threadedConversations =
            Datastore.getDatastore(context).threadedConversationsDao().get(threadId)
        if(threadedConversations != null) {
            val unread = conversationsViewModel?.getUnreadCount(context, threadId)

            unread?.let {
                threadedConversations.unread_count = it
                threadedConversations.isIs_read = it == 0
            }

            contactName?.let {
                threadedConversations.contact_name = contactName
            }

            subscriptionId?.let {
                threadedConversations.subscription_id = subscriptionId
            }

            Datastore.getDatastore(context).threadedConversationsDao()
                .update(context, threadedConversations)
        }
    }
}

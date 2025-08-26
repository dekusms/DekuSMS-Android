package com.afkanerd.smswithoutborders_libsmsmms.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.SmsMmsNatives
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Threads

@Dao
interface ConversationsDao {

    @Query("SELECT * FROM Conversations WHERE Conversations.id = :id")
    fun getConversation(id: Long): Conversations?

    @Update
    fun updateConversation(conversations: Conversations)

    @Update
    fun updateThread(thread: Threads)

    fun insertUpdateThread(
        sms: SmsMmsNatives.Sms,
        keepArchived: Boolean,
        isMms: Boolean,
        mms: SmsMmsNatives.Mms?,
        conversationId: Long,
    ) {
        val threadId = mms?.thread_id ?: sms.thread_id
//        val threadId = sms.thread_id
        val thread = getThread(threadId)
        val count = unreadCount(threadId)

        if(thread == null) {
            insertThread(
                Threads(
                    threadId = threadId,
                    snippet = sms.body ?: "",
                    date = sms.date,
                    unread = count > 0,
                    address = sms.address!!,
                    isMute = false,
                    type = sms.type,
                    conversationId = conversationId,
                    isArchive = false,
                    unreadCount = count,
                    isMms = isMms,
                )
            )
        } else {
            updateThread(
                Threads(
                    threadId = thread.threadId,
                    snippet = sms.body ?: "",
                    date = sms.date,
                    unread = sms.read == 0,
                    address = sms.address!!,
                    type = sms.type,
                    conversationId = conversationId,
                    isMute = thread.isMute,
                    isArchive = if(thread.isArchive) keepArchived else false,
                    unreadCount = count,
                    isMms = isMms
                )
            )
        }
    }

    @Transaction
    fun update(conversation: Conversations) {
        updateConversation(conversation)
        conversation.sms?.let {
            insertUpdateThread(
                it,
                true,
                !conversation.mms_content_uri.isNullOrEmpty(),
                conversation.mms,
                conversation.id
            )
        }
    }

    @Update
    fun update(conversations: MutableList<Conversations>): Int

    @Query("SELECT * FROM Conversations WHERE thread_id = :threadId OR " +
            "Conversations.mms_thread_id = :threadId ORDER BY date DESC")
    fun getConversations(threadId: Int): PagingSource<Int, Conversations>

    @Query("SELECT COUNT('_id') FROM Conversations WHERE thread_id = :threadId OR " +
            "Conversations.mms_thread_id = :threadId AND read = 0")
    fun unreadCount(threadId: Int): Int

    @Insert
    fun insertConversation(conversation: Conversations): Long

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Insert
    fun insertConversations(conversation: List<Conversations>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertThread(thread: Threads)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertThreads(thread: List<Threads>)

    @Query("SELECT * FROM Threads WHERE threadId = :threadId")
    fun getThread(threadId: Int): Threads?

    @Transaction
    fun insert(conversation: Conversations, removeArchive: Boolean = false): Long {
        val id = insertConversation(conversation)
        conversation.sms?.let {
            insertUpdateThread(
                it,
                removeArchive,
                !conversation.mms_content_uri.isNullOrEmpty(),
                conversation.mms,
                id
            )
        }
        return id
    }

    @Transaction
    fun insertAll(conversationsList: List<Conversations>, deleteDb: Boolean = false) {
        if(deleteDb) {
            deleteAllConversations()
            deleteAllThreads()
        }

//        insertConversations(conversationsList)
        conversationsList.forEach {
            insert(it)
        }
    }

    @Query("DELETE FROM Conversations")
    fun deleteAllConversations()

    @Query("DELETE FROM Threads")
    fun deleteAllThreads()

    @Delete
    fun deleteConversation(conversations: Conversations)

    @Delete
    fun deleteConversations(conversations: List<Conversations>)

    @Query("DELETE FROM Threads WHERE conversationId = :conversationId")
    fun deleteThreadConversation(conversationId: Long)

    @Query("DELETE FROM Threads WHERE conversationId IN (:ids)")
    fun deleteThreadConversations(ids: List<Long>)

    @Transaction
    fun delete(conversation: Conversations, removeArchive: Boolean) {
        deleteConversation(conversation)
        deleteThreadConversation(conversation.id )
        getLatestConversation(conversation.sms!!.thread_id)?.let { latest ->
            insertUpdateThread(
                latest.sms!!,
                removeArchive,
                !latest.mms_content_uri.isNullOrEmpty(),
                latest.mms,
                latest.id
            )
        }
    }

    @Transaction
    fun delete(conversations: List<Conversations>, removeArchive: Boolean) {
        deleteConversations(conversations)
        deleteThreadConversations(conversations.map { it.id })
        conversations.forEach {
            getLatestConversation(it.sms!!.thread_id)?.let { latest ->
                insertUpdateThread(
                    latest.sms!!,
                    removeArchive,
                    !latest.mms_content_uri.isNullOrEmpty(),
                    latest.mms,
                    latest.id
                )
            }
        }
    }

    @Query("SELECT * FROM Conversations WHERE (thread_id = :threadId OR " +
            "Conversations.mms_thread_id = :threadId) AND " +
            "type = :type ORDER BY  date DESC LIMIT 1")
    fun fetchConversationsForType(threadId: Int, type: Int): Conversations?

    @Query("SELECT * FROM Conversations WHERE (thread_id = :threadId OR " +
            "Conversations.mms_thread_id = :threadId) ORDER BY date DESC")
    fun getConversationsList(threadId: Int): List<Conversations>

    @Query("SELECT * FROM Conversations WHERE (thread_id = :threadId OR " +
            "Conversations.mms_thread_id = :threadId) ORDER BY date DESC LIMIT 1")
    fun getLatestConversation(threadId: Int): Conversations?
}

package com.afkanerd.smswithoutborders_libsmsmms.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.smsMmsNatives
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Threads

@Dao
interface ConversationsDao {

    @Query("SELECT * FROM Conversations WHERE Conversations._id = :messageId")
    fun getConversation(messageId: Int): Conversations?

    @Update
    fun updateConversation(conversations: Conversations)

    @Update
    fun updateThread(thread: Threads)

    fun insertUpdateThread(sms: smsMmsNatives.Sms) {
        val thread = getThread(sms.thread_id)
        if(thread == null) {
            insertThread(
                Threads(
                    threadId = sms.thread_id,
                    snippet = sms.body,
                    date = sms.date,
                    unread = unreadCount(sms.thread_id) > 0,
                    address = sms.address!!,
                    isMute = false,
                    type = sms.type,
                    conversationId = sms._id ?: -1,
                    isArchive = false
                )
            )
        } else {
            updateThread(
                Threads(
                    threadId = thread.threadId,
                    snippet = sms.body,
                    date = sms.date,
                    unread = unreadCount(sms.thread_id) > 0,
                    address = sms.address!!,
                    type = sms.type,
                    conversationId = sms._id ?: -1,
                    isMute = thread.isMute,
                    isArchive = thread.isArchive
                )
            )
        }
    }

    @Transaction
    fun update(conversation: Conversations) {
        updateConversation(conversation)
        conversation.sms?.let { insertUpdateThread(it) }
    }

    @Update
    fun update(conversations: MutableList<Conversations>): Int

    @Query("DELETE FROM Conversations")
    fun deleteEvery(): Int

    @Query("SELECT * FROM Conversations WHERE thread_id = :threadId ORDER BY date DESC")
    fun getConversations(threadId: Int): PagingSource<Int, Conversations>

    @Transaction
    fun reset(conversationsList: MutableList<Conversations>) {
        deleteEvery()
        insertAll(conversationsList)
    }

    @Query("SELECT COUNT('_id') FROM Conversations WHERE thread_id = :threadId AND read = 0")
    fun unreadCount(threadId: Int): Int

    @Insert
    fun insertConversation(conversation: Conversations)

    @Insert
    fun insertConversations(conversation: List<Conversations>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertThread(thread: Threads)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertThreads(thread: List<Threads>)

    @Query("SELECT * FROM Threads WHERE threadId = :threadId")
    fun getThread(threadId: Int): Threads?

    @Transaction
    fun insert(conversation: Conversations){
        insertConversation(conversation)
        conversation.sms?.let { insertUpdateThread(it) }
    }

    @Transaction
    fun insertAll(conversationsList: List<Conversations>) {
        insertConversations(conversationsList)
        conversationsList.forEach {
            it.sms?.let { sms -> insertUpdateThread(sms) }
        }
    }

    @Delete
    fun deleteConversation(conversations: Conversations)

    @Delete
    fun deleteConversations(conversations: List<Conversations>)

    @Query("DELETE FROM Threads WHERE conversationId = :conversationId")
    fun deleteThreadConversation(conversationId: Int)

    @Query("DELETE FROM Threads WHERE conversationId IN (:threadIds)")
    fun deleteThreadConversations(threadIds: List<Int?>)

    @Transaction
    fun delete(conversation: Conversations) {
        deleteConversation(conversation)
        deleteThreadConversation(conversation.sms?.thread_id!!)
    }

    @Transaction
    fun delete(conversations: List<Conversations>) {
        deleteConversations(conversations)
        deleteThreadConversations(conversations.map { it.sms?.thread_id })
    }

    @Query("SELECT * FROM Conversations WHERE thread_id = :threadId AND " +
            "type = :type ORDER BY  date DESC LIMIT 1")
    fun fetchConversationsForType(threadId: Int, type: Int): Conversations?

    @Query("SELECT * FROM Conversations WHERE thread_id = :threadId")
    fun getConversationsList(threadId: Int): List<Conversations>


//    @Query("SELECT c.*, max(c.date) FROM Conversations c LEFT JOIN Threads tc " +
//            "ON c.thread_id = tc.threadId " +
//            "WHERE tc.isArchive = 0 AND " +
//            "(c.type IS NOT 3 AND c.body like '%' || :query || '%') AND " +
//            "(tc.threadId IS NULL)" +
//            "GROUP BY thread_id ORDER BY date DESC")
//    fun getAllThreadingSearch(query: String): PagingSource<Int, Conversations>
//
//    @Query("SELECT c.*, max(c.date) FROM Conversations c LEFT JOIN Threads tc " +
//            "ON c.thread_id = tc.threadId " +
//            "WHERE c.thread_id = :threadId AND tc.isArchive = 0 AND" +
//            "(c.type IS NOT 3 AND c.body like '%' || :query || '%') AND " +
//            "(tc.threadId IS NULL )" +
//            "GROUP BY thread_id ORDER BY date DESC")
//    fun getAllThreadingSearch(query: String, threadId: String): List<Conversations>?
}

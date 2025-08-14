package com.afkanerd.smswithoutborders_libsmsmms.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Threads

@Dao
interface ConversationsDao {

    @Query("SELECT * FROM Conversations WHERE Conversations._id = :messageId")
    fun getConversation(messageId: String): Conversations

    @Update
    fun update(conversations: Conversations): Int

    @Update
    fun update(conversations: MutableList<Conversations>): Int

    @Query("DELETE FROM Conversations")
    fun deleteEvery(): Int

    @Query("SELECT * FROM Conversations WHERE thread_id = :threadId")
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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertThread(thread: Threads)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertThreads(thread: List<Threads>)

    @Transaction
    fun insert(conversations: Conversations){
        insertConversation(conversations)
        conversations.sms?.let {
            insertThread(
                Threads(
                    threadId = it.thread_id,
                    snippet = it.body,
                    date = (it.date * 1000L),
                    unread = unreadCount(it.thread_id) > 0,
                    address = it.address!!,
                    isMute = false,
                    type = it.type
                )
            )
        }
    }

    @Transaction
    fun insertAll(conversationsList: List<Conversations>) {
        insertConversations(conversationsList)
        insertThreads(conversationsList.run {
            val threads = mutableListOf<Threads>()
            forEach {
                threads.add(Threads(
                    threadId = it.sms!!.thread_id,
                    snippet = it.sms!!.body,
                    date = (it.sms!!.date * 1000L),
                    unread = unreadCount(it.sms!!.thread_id) > 0,
                    address = it.sms!!.address!!,
                    isMute = false,
                    type = it.sms!!.type
                ))
            }
            threads
        })
    }

    @Query("SELECT c.*, max(date) FROM Conversations c LEFT JOIN Archive tc " +
            "ON c.thread_id = tc.threadId " +
            "WHERE " +
            "(type IS NOT 3 AND c.body like '%' || :searchString || '%') AND " +
            "(tc.threadId IS NULL)" +
            "GROUP BY thread_id ORDER BY date DESC")
    fun getAllThreadingSearch(searchString: String): List<Conversations>

    @Query("SELECT c.*, max(date) FROM Conversations c LEFT JOIN ARCHIVE tc " +
            "ON c.thread_id = tc.threadId " +
            "WHERE c.thread_id = :threadId AND " +
            "(type IS NOT 3 AND c.body like '%' || :searchString || '%') AND " +
            "(tc.threadId IS NULL )" +
            "GROUP BY thread_id ORDER BY date DESC")
    fun getAllThreadingSearch(searchString: String, threadId: String): List<Conversations>
}

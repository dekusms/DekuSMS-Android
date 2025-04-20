package com.afkanerd.deku.DefaultSMS.DAO

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.ThreadsCount
import com.afkanerd.deku.DefaultSMS.Models.ThreadsSearch

@Dao
interface ConversationDao {
    @Query("SELECT * FROM Conversation WHERE thread_id =:thread_id AND type IS NOT 3 ORDER BY date DESC")
    fun getLiveData(thread_id: String): LiveData<MutableList<Conversation>>

    @Query("SELECT * FROM Conversation WHERE thread_id =:thread_id AND type IS NOT 3 ORDER BY date DESC")
    fun getDefault(thread_id: String): MutableList<Conversation?>?

    @Query("SELECT c.*, max(date) FROM Conversation c LEFT JOIN ThreadsConfigurations tc " +
            "ON c.thread_id = tc.threadId WHERE tc.isArchive = '0' OR tc.threadId IS NULL " +
            "GROUP BY thread_id ORDER BY date DESC")
    fun getAllThreading(): LiveData<MutableList<Conversation>>

    @Query("SELECT c.*, max(date) FROM Conversation c " +
            "WHERE c.isRemoteListener = '1' " +
            "GROUP BY thread_id ORDER BY date DESC")
    fun getAllThreadingRemoteListeners(): LiveData<MutableList<Conversation>>

    @Query("SELECT c.*, max(date) FROM Conversation c LEFT JOIN ThreadsConfigurations tc " +
            "ON c.thread_id = tc.threadId " +
            "WHERE " +
            "(type IS NOT 3 AND text like '%' || :searchString || '%') AND " +
            "(tc.isArchive = '0' OR tc.threadId IS NULL )" +
            "GROUP BY thread_id ORDER BY date DESC")
    fun getAllThreadingSearch(searchString: String): List<Conversation>

    @Query("SELECT c.*, max(date) FROM Conversation c LEFT JOIN ThreadsConfigurations tc " +
            "ON c.thread_id = tc.threadId " +
            "WHERE c.thread_id = :threadId AND " +
            "(type IS NOT 3 AND text like '%' || :searchString || '%') AND " +
            "(tc.isArchive = '0' OR tc.threadId IS NULL )" +
            "GROUP BY thread_id ORDER BY date DESC")
    fun getAllThreadingSearch(searchString: String, threadId: String): List<Conversation>

    @Query("SELECT *, max(date) as date FROM Conversation " +
            "WHERE type = 3 GROUP BY thread_id ORDER BY date DESC")
    fun getAllThreadingDrafts(): LiveData<MutableList<Conversation>>

    @Query("SELECT Conversation.*, max(date) as date FROM Conversation, ThreadsConfigurations " +
            "WHERE " +
            "ThreadsConfigurations.threadId = Conversation.thread_id " +
            "AND ThreadsConfigurations.isArchive = '1' " +
            "GROUP BY thread_id ORDER BY date DESC")
    fun getAllThreadingArchived(): LiveData<MutableList<Conversation>>

    @Query("SELECT Conversation.*, max(date) as date FROM Conversation, ThreadsConfigurations " +
            "WHERE " +
            "ThreadsConfigurations.threadId = Conversation.thread_id " +
            "AND ThreadsConfigurations.isMute = '1' " +
            "GROUP BY thread_id ORDER BY date DESC")
    fun getAllThreadingMuted(): LiveData<MutableList<Conversation>>


    @Query("SELECT * FROM Conversation WHERE thread_id =:thread_id ORDER BY date DESC")
    fun getAll(thread_id: String): MutableList<Conversation?>?

    @Query("SELECT * FROM Conversation ORDER BY date DESC")
    fun getComplete(): MutableList<Conversation>

    @Query("SELECT * FROM Conversation WHERE type = :type AND thread_id = :threadId ORDER BY date DESC")
    fun fetchTypedConversation(type: Int, threadId: String): Conversation?

    @Query("SELECT * FROM Conversation WHERE message_id =:message_id")
    fun getMessage(message_id: String): Conversation

    @Query("SELECT COUNT(thread_id) FROM Conversation " +
            "WHERE thread_id = :threadId AND read = 0")
    fun getUnreadCount(threadId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun _insert(conversation: Conversation): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(conversationList: MutableList<Conversation>): MutableList<Long>

    @Update
    fun _update(conversation: Conversation): Int

    @Update
    fun update(conversations: MutableList<Conversation>): Int

    @Query("UPDATE Conversation SET read = :isRead WHERE thread_id = :threadId")
    fun updateRead(isRead: Boolean, threadId: String): Int

    @Query("DELETE FROM Conversation WHERE thread_id = :threadId")
    fun deleteThread(threadId: String): Int

    @Query("DELETE FROM Conversation WHERE thread_id IN (:threadIds)")
    fun deleteAllThreads(threadIds: List<String>)

    @Query("DELETE FROM Conversation")
    fun deleteEvery()

    @Query("DELETE FROM Conversation WHERE type = :type AND thread_id = :thread_id")
    fun _deleteAllType(type: Int, thread_id: String): Int

    @Transaction
    fun deleteAllType(context: Context, type: Int, thread_id: String) {
        _deleteAllType(type, thread_id)
    }

    @Delete
    fun delete(conversation: Conversation): Int

    @Delete
    fun delete(conversation: List<Conversation>): Int

    @Query("SELECT " +
            "SUM(CASE WHEN isArchived = '1' THEN 1 ELSE 0 END) as archivedCount, " +
            "SUM(CASE WHEN read = '0' AND isArchived = '0' THEN 1 ELSE 0 END) as unreadCount, " +
            "SUM(CASE WHEN type = 3 THEN 1 ELSE 0 END) as draftsCount " +
            "FROM Conversation")
    fun getFullCounts(): ThreadsCount

//    @Query(
//        "SELECT COUNT(Conversation.id) as count, Conversation.thread_id as threadId, " +
//                "Conversation.text, Conversation.date " +
//                "FROM Conversation " +
//                "where thread_id = :threadId and text like '%' || :searchString || '%' " +
//                "GROUP BY thread_id ORDER BY Conversation.date DESC"
//    )
//    fun searchThreadId(searchString: String, threadId: String): MutableList<ThreadsSearch>
//
//    @Query(
//        "SELECT COUNT(Conversation.id) as count, Conversation.thread_id as threadId, " +
//                "Conversation.text, Conversation.date " +
//                "FROM Conversation, ThreadedConversations " +
//                "where text like '%' || :searchString || '%' and " +
//                "Conversation.thread_id = ThreadedConversations.thread_id " +
//                "GROUP BY ThreadedConversations.thread_id ORDER BY Conversation.date DESC"
//    )
//    fun search(searchString: String): MutableList<ThreadsSearch>
}

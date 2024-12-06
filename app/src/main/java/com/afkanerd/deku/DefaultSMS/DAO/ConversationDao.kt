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

@Dao
interface ConversationDao {
    @Query("SELECT * FROM Conversation WHERE thread_id =:thread_id AND type IS NOT 3 ORDER BY date DESC")
    fun get(thread_id: String): PagingSource<Int, Conversation>

    @Query("SELECT * FROM Conversation WHERE thread_id =:thread_id AND type IS NOT 3 ORDER BY date DESC")
    fun getLiveData(thread_id: String): LiveData<MutableList<Conversation>>

    @Query("SELECT * FROM Conversation WHERE thread_id =:thread_id AND type IS NOT 3 ORDER BY date DESC")
    fun getDefault(thread_id: String): MutableList<Conversation?>?

    @Query("SELECT *, max(date) as date FROM Conversation " +
            "WHERE type IS NOT 3 AND isArchived = '0' AND isBlocked = '0' GROUP BY thread_id ORDER BY date DESC")
    fun getAllThreading(): LiveData<MutableList<Conversation>>

    @Query("SELECT *, max(date) as date FROM Conversation " +
            "WHERE isArchived = '1' AND isBlocked = '0' GROUP BY thread_id ORDER BY date DESC")
    fun getAllThreadingArchived(): LiveData<MutableList<Conversation>>

    @Query("SELECT *, max(date) as date FROM Conversation " +
            "WHERE isSecured = '1' AND isBlocked = '0' GROUP BY thread_id ORDER BY date DESC")
    fun getAllThreadingEncrypted(): LiveData<MutableList<Conversation>>

    @Query("SELECT *, max(date) as date FROM Conversation " +
            "WHERE isBlocked = '1' GROUP BY thread_id ORDER BY date DESC")
    fun getAllThreadingBlocked(): LiveData<MutableList<Conversation>>

    @Query("SELECT *, max(date) as date FROM Conversation " +
            "WHERE type = 3 GROUP BY thread_id ORDER BY date DESC")
    fun getAllThreadingDrafts(): LiveData<MutableList<Conversation>>

    @Query("SELECT *, max(date) as date FROM Conversation " +
            "WHERE isMute = '1' GROUP BY thread_id ORDER BY date DESC")
    fun getAllThreadingMuted(): LiveData<MutableList<Conversation>>

    @Query("SELECT * FROM Conversation WHERE thread_id =:thread_id ORDER BY date DESC")
    fun getAll(thread_id: String): MutableList<Conversation?>?

    @Query("SELECT * FROM Conversation WHERE thread_id =:threadId ORDER BY date DESC LIMIT 1")
    fun fetchLatestForThread(threadId: String): Conversation?

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
        val conversation = fetchLatestForThread(thread_id)
        if (conversation != null) Datastore.getDatastore(context)
            .threadedConversationsDao()
            .insertThreadFromConversation(context, conversation)
    }

    @Delete
    fun delete(conversation: Conversation): Int

    @Delete
    fun delete(conversation: List<Conversation>): Int

    @Query("UPDATE Conversation SET isArchived = '0' WHERE thread_id = :threadId")
    fun unarchive(threadId: String)

    @Query("UPDATE Conversation SET isArchived = '1' WHERE thread_id  IN(:threadIds)")
    fun unarchive(threadIds: List<String>)

    @Query("UPDATE Conversation SET isArchived = '1' WHERE thread_id = :threadId")
    fun archive(threadId: String)

    @Query("UPDATE Conversation SET isArchived = '1' WHERE thread_id IN (:threadsIds)")
    fun archive(threadsIds: List<String>)

    @Query("UPDATE Conversation SET isMute = '1' WHERE thread_id = :threadId")
    fun mute(threadId: String)

    @Query("UPDATE Conversation SET isMute = '0' WHERE thread_id = :threadId")
    fun unMute(threadId: String)

    @Query("UPDATE Conversation SET isBlocked = '1' WHERE thread_id = :threadId")
    fun block(threadId: String)

    @Query("UPDATE Conversation SET isBlocked = '0' WHERE thread_id = :threadId")
    fun unBlock(threadId: String)

    @Query("UPDATE Conversation SET isSecured = :isSecured WHERE thread_id = :threadId")
    fun updateSecured(threadId: String, isSecured: Boolean)


    @Query("SELECT " +
            "SUM(CASE WHEN isSecured = '1' THEN 1 ELSE 0 END) as encryptedCount, " +
            "SUM(CASE WHEN isArchived = '1' THEN 1 ELSE 0 END) as archivedCount, " +
            "SUM(CASE WHEN read = '0' AND isArchived = '0' THEN 1 ELSE 0 END) as unreadCount, " +
            "SUM(CASE WHEN isBlocked = '1' THEN 1 ELSE 0 END) as blockedCount, " +
            "SUM(CASE WHEN isMute = '1' THEN 1 ELSE 0 END) as mutedCount, " +
            "SUM(CASE WHEN type = 3 THEN 1 ELSE 0 END) as draftsCount " +
            "FROM Conversation")
    fun getFullCounts(): ThreadsCount
}

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

@Dao
interface ConversationDao {
    @Query("SELECT * FROM Conversation WHERE thread_id =:thread_id AND type IS NOT 3 ORDER BY date DESC")
    fun get(thread_id: String): PagingSource<Int, Conversation>

    @Query("SELECT * FROM Conversation WHERE thread_id =:thread_id AND type IS NOT 3 ORDER BY date DESC")
    fun getLiveData(thread_id: String): LiveData<MutableList<Conversation>>

    @Query("SELECT * FROM Conversation WHERE thread_id =:thread_id AND type IS NOT 3 ORDER BY date DESC")
    fun getDefault(thread_id: String): MutableList<Conversation?>?

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

    @Insert
    fun _insert(conversation: Conversation): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(conversationList: MutableList<Conversation>): MutableList<Long>

    @Update
    fun _update(conversation: Conversation): Int

    @Update
    fun update(conversations: MutableList<Conversation>): Int

    @Query("UPDATE Conversation SET read = :isRead WHERE thread_id = :threadId AND read = 0")
    fun updateRead(isRead: Boolean, threadId: String): Int

    @Query("DELETE FROM Conversation WHERE thread_id = :threadId")
    fun delete(threadId: String): Int

    @Query("DELETE FROM Conversation WHERE thread_id IN (:threadIds)")
    fun deleteAll(threadIds: MutableList<String>)

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
}

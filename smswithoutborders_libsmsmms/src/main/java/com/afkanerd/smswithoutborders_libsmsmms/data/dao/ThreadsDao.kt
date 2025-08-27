package com.afkanerd.smswithoutborders_libsmsmms.data.dao

import android.provider.Telephony
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Threads

@Dao
interface ThreadsDao {

    @Query("SELECT * FROM Threads WHERE isArchive = 0 ORDER BY date DESC")
    fun getThreads(): PagingSource<Int, Threads>

    @Query("SELECT * FROM Threads WHERE isArchive = 1 ORDER BY date DESC")
    fun getArchived(): PagingSource<Int, Threads>

    @Query("SELECT * FROM Threads WHERE type = :type ORDER BY date DESC")
    fun getType(type: Int): PagingSource<Int, Threads>

    @Query("SELECT * FROM Threads WHERE isMute = 1 ORDER BY date DESC")
    fun getIsMute(): PagingSource<Int, Threads>

    @Query("SELECT * FROM Threads WHERE isBlocked = 1 ORDER BY date DESC")
    fun getIsBlocked(): PagingSource<Int, Threads>

    @Query("SELECT * FROM Threads WHERE threadId = :threadId")
    fun get(threadId: Int): Threads?

    @Query("UPDATE Threads SET isMute = :isMute WHERE threadId = :threadId")
    fun setMute(isMute: Boolean, threadId: Int)

    @Query("UPDATE Threads SET isBlocked = :isBlocked WHERE address IN (:addresses)")
    fun setIsBlocked(isBlocked: Boolean, addresses: List<String>)

    @Delete
    fun deleteThreads(threads: List<Threads>)

    @Update
    fun update(threads: List<Threads>): Int

    @Query("DELETE FROM Conversations WHERE thread_id IN (:threads)")
    fun deleteConversations(threads: List<Int>)

    @Transaction
    fun delete(threads: List<Threads>) {
        deleteThreads(threads)
        deleteConversations(threads.map { it.threadId })
    }

    @Query("SELECT " +
            "tc.threadId, " +
            "tc.isArchive, " +
            "tc.address, " +
            "tc.conversationId, " +
            "c.date AS date, " +
            "tc.isMute, " +
            "tc.type, " +
            "tc.unread, " +
            "tc.isMms, " +
            "tc.isBlocked, " +
            "SUM(CASE WHEN read = 0 THEN 1 ELSE 0 END) as unreadCount, " +
            "c.body AS snippet " +
            "FROM Threads tc LEFT JOIN Conversations c " +
            "ON c.thread_id = tc.threadId " +
            "WHERE tc.isArchive = 0 AND " +
            "(c.type IS NOT 3 AND c.body like '%' || :query || '%') " +
            "GROUP BY thread_id ORDER BY date DESC")
    fun search(query: String): PagingSource<Int, Threads>


    @Query("SELECT " +
            "tc.threadId, " +
            "tc.isArchive, " +
            "tc.address, " +
            "tc.conversationId, " +
            "c.date AS date, " +
            "tc.isMute, " +
            "tc.type, " +
            "tc.unread, " +
            "tc.isMms, " +
            "tc.isBlocked, " +
            "SUM(CASE WHEN read = 0 THEN 1 ELSE 0 END) as unreadCount, " +
            "c.body AS snippet " +
            "FROM Threads tc LEFT JOIN Conversations c " +
            "ON c.thread_id = tc.threadId " +
            "WHERE c.thread_id = :threadId AND tc.isArchive = 0 AND " +
            "(c.type IS NOT 3 AND c.body like '%' || :query || '%') " +
            "GROUP BY thread_id ORDER BY date DESC")
    fun search(query: String, threadId: Int): PagingSource<Int, Threads>
}

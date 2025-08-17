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
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Threads

@Dao
interface ThreadsDao {

    @Query("SELECT * FROM Threads WHERE isArchive = 0")
    fun getThreads(): PagingSource<Int, Threads>

    @Query("SELECT * FROM Threads WHERE isArchive = 1")
    fun getArchived(): PagingSource<Int, Threads>

    @Query("SELECT * FROM Threads WHERE threadId = :threadId")
    fun get(threadId: Int): Threads?

    @Query("UPDATE Threads SET isMute = :isMute WHERE threadId = :threadId")
    fun setMute(isMute: Boolean, threadId: Int)

    @Delete
    fun deleteThreads(threads: List<Threads>)

    @Update
    fun update(threads: List<Threads>): Int

    @Query("DELETE FROM Conversations WHERE thread_id IN (:threads)")
    fun deleteConversations(threads: List<Int>)

    @Transaction
    fun delete(threads: List<Threads>) {
        deleteThreads(threads)
        deleteConversations(threads.run {
            val ids = mutableListOf<Int>()
            this.forEach { ids.add(it.threadId) }
            ids
        })
    }
}

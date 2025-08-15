package com.afkanerd.smswithoutborders_libsmsmms.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Threads

@Dao
interface ThreadsDao {

    @Query("SELECT * FROM Threads ORDER BY date DESC")
    fun getThreads(): PagingSource<Int, Threads>

    @Query("SELECT * FROM Threads JOIN Archive ON Threads.threadId = Archive.threadId " +
            "WHERE Archive.threadId = null ORDER BY date DESC")
    fun getArchived(): PagingSource<Int, Threads>

    @Query("SELECT * FROM Threads WHERE threadId = :threadId")
    fun get(threadId: Int): Threads?

    @Query("UPDATE Threads SET isMute = :isMute WHERE threadId = :threadId")
    fun setMute(isMute: Boolean, threadId: Int)
}

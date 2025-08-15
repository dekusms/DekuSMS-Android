package com.afkanerd.smswithoutborders_libsmsmms.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Threads

@Dao
interface ThreadsDao {

    @Query("SELECT * FROM Threads ORDER BY date DESC")
    fun getThreads(): PagingSource<Int, Threads>
}

package com.afkanerd.smswithoutborders_libsmsmms.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Threads

@Dao
interface ThreadsConfigurationsDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insert(threads: Threads): Long

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insert(threadsConfigurations: MutableList<Threads>)

    @Query("SELECT * FROM ThreadsConfigurations WHERE threadId = :threadId")
    fun get(threadId: String): Threads?
}
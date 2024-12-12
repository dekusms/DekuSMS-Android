package com.afkanerd.deku.DefaultSMS.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.ThreadsConfigurations

@Dao
interface ThreadsConfigurationsDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insert(threadsConfigurations: ThreadsConfigurations): Long

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insert(threadsConfigurations: MutableList<ThreadsConfigurations>)

    @Query("SELECT * FROM ThreadsConfigurations WHERE threadId = :threadId")
    fun get(threadId: String): ThreadsConfigurations?
}
package com.afkanerd.deku.DefaultSMS.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.afkanerd.deku.DefaultSMS.data.models.SecureConversation
import kotlinx.coroutines.flow.Flow


@Dao
interface SecureConversationDao {

    @Update
    fun update(secureConversation: SecureConversation)

    @Insert
    fun insert(secureConversation: SecureConversation)

    @Query("SELECT * FROM SecureConversation WHERE address = :address")
    fun fetch(address: String): Flow<SecureConversation?>
}
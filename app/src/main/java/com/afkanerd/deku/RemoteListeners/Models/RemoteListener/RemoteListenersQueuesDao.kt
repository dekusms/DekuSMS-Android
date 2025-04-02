package com.afkanerd.deku.RemoteListeners.Models.RemoteListener

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersQueues

@Dao
interface RemoteListenersQueuesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(remoteListenersQueuesList: List<RemoteListenersQueues>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(remoteListenersQueues: RemoteListenersQueues): Long

    @Query("SELECT * FROM RemoteListenersQueues WHERE id = :id")
    fun fetch(id: Long): RemoteListenersQueues?

    @Query("SELECT * FROM RemoteListenersQueues WHERE id = :id")
    fun fetchLiveData(id: Long): LiveData<RemoteListenersQueues>

    @Query("SELECT * FROM RemoteListenersQueues WHERE gatewayClientId = :gatewayClientId")
    fun fetchRemoteListenerQueue(gatewayClientId: Long): LiveData<List<RemoteListenersQueues>>

    @Query("SELECT * FROM RemoteListenersQueues WHERE gatewayClientId = :gatewayClientId")
    fun fetchRemoteListenersQueues(gatewayClientId: Long): List<RemoteListenersQueues>

    @Update
    fun update(remoteListenersQueues: RemoteListenersQueues)

    @Query("DELETE FROM RemoteListenersQueues WHERE gatewayClientId = :id")
    fun deleteRemoteListenerQueue(id: Long)

    @Query("DELETE FROM RemoteListenersQueues WHERE id = :id")
    fun delete(id: Long)

    @Delete
    fun delete(remoteListenerQueue: RemoteListenersQueues)
}

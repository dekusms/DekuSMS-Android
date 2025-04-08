package com.afkanerd.deku.RemoteListeners.Models

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface RemoteListenerDAO {
    @get:Query("SELECT * FROM RemoteListeners")
    val all: List<RemoteListeners>

    @Query("SELECT * FROM RemoteListeners")
    fun fetch(): LiveData<List<RemoteListeners>>

    @Query("SELECT * FROM RemoteListeners WHERE activated = 1")
    fun fetchActivated(): List<RemoteListeners>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insert(remoteListeners: RemoteListeners): Long

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insert(remoteListeners: List<RemoteListeners>)

    @Delete
    fun delete(remoteListeners: RemoteListeners): Int

    @Delete
    fun delete(remoteListeners: List<RemoteListeners>)

    @Query("SELECT * FROM RemoteListeners WHERE id=:id")
    fun fetch(id: Long): RemoteListeners

    @Update
    fun update(remoteListeners: RemoteListeners)

    @Update
    fun update(remoteListeners: List<RemoteListeners>)
}

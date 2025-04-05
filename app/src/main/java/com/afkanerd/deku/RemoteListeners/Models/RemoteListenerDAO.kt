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
    @get:Query("SELECT * FROM GatewayClient")
    val all: List<GatewayClient>

    @Query("SELECT * FROM GatewayClient")
    fun fetch(): LiveData<List<GatewayClient>>

    @Query("SELECT * FROM GatewayClient WHERE activated = 1")
    fun fetchActivated(): List<GatewayClient>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insert(gatewayClient: GatewayClient): Long

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insert(gatewayClients: List<GatewayClient>)

    @Delete
    fun delete(gatewayClient: GatewayClient): Int

    @Delete
    fun delete(gatewayClients: List<GatewayClient>)

    @Query("SELECT * FROM GatewayClient WHERE id=:id")
    fun fetch(id: Long): GatewayClient

    @Update
    fun update(gatewayClient: GatewayClient)

    @Update
    fun update(gatewayClient: List<GatewayClient>)
}

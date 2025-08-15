package com.afkanerd.smswithoutborders_libsmsmms.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity
data class Threads(
    @PrimaryKey var threadId: Int,
    var address: String,
    var isMute: Boolean = false,
    var snippet: String,
    @ColumnInfo(defaultValue = "0") var date: Int,
    var unread: Boolean = true,
    var type: Int
)
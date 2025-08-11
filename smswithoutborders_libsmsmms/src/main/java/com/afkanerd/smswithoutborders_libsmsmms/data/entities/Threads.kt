package com.afkanerd.smswithoutborders_libsmsmms.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity
data class Threads(
    @PrimaryKey var threadId: Int,
    var address: String,
    var isMute: Boolean = false,
    var snippet: String,
    var date: Long,
    var unread: Boolean = true,
    var type: Int
)
package com.afkanerd.smswithoutborders_libsmsmms.data.entities

import android.provider.Telephony
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity
data class Threads(
    @PrimaryKey var threadId: Int,
    var address: String,
    var snippet: String,
    var date: Long,
    var type: Int,
    var conversationId: Long,
    var isMute: Boolean = false,
    var unread: Boolean = true,
    var isArchive: Boolean = false,
    var unreadCount: Int = 0,
)
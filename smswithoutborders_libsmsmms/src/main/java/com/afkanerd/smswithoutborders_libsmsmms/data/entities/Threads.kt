package com.afkanerd.smswithoutborders_libsmsmms.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["threadId"], unique = true)])
data class Threads(
    @PrimaryKey(autoGenerate = true) var id: Long,
    var threadId: String,
    var isMute: Boolean = false,
    var isArchive: Boolean = false,
)
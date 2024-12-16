package com.afkanerd.deku.DefaultSMS.Models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi

@Entity(indices = [Index(value = ["threadId"], unique = true)])
open class ThreadsConfigurations {
    @PrimaryKey(autoGenerate = true) var id: Long = 0
    var threadId: String? = null
    var isMute = false
    var isArchive = false
}
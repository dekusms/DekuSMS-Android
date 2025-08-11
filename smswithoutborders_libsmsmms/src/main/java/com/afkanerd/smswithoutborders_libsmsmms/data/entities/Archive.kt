package com.afkanerd.smswithoutborders_libsmsmms.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Archive(
    @PrimaryKey
    var threadId: String,
)

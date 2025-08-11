package com.afkanerd.smswithoutborders_libsmsmms.data.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.smsMmsNatives
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class Conversations(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @Embedded(prefix = "sms_") val sms: smsMmsNatives.Sms,
    @Embedded(prefix = "mms_") val mms: smsMmsNatives.Mms,
    @Embedded(prefix = "mms_part_") val mmsPart: smsMmsNatives.MmsPart,
    @Embedded(prefix = "mms_addr_") val mmsAddr: smsMmsNatives.MmsAddr
)
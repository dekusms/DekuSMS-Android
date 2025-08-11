package com.afkanerd.smswithoutborders_libsmsmms.data.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.smsMmsNatives
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class Conversations(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @Embedded val sms: smsMmsNatives.Sms? = null,
    @Embedded("mms_") val mms: smsMmsNatives.Mms? = null,
    @Embedded("mms_part_") val mmsPart: smsMmsNatives.MmsPart? = null,
    val sms_data_: ByteArray? = null,
    val mms_text: String? = null,
    var mms_content_uri: String? = null,
    val mms_mimetype: String? = null,
    val mms_filename: String? = null,
    var mms_filepath: String? = null
)
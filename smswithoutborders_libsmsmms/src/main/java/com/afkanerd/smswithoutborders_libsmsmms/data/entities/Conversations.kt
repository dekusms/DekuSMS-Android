package com.afkanerd.smswithoutborders_libsmsmms.data.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.SmsMmsNatives
import kotlinx.serialization.Serializable

@Serializable
@Entity
//@Entity(indices = [Index(value = ["_id"], unique = true)])
data class Conversations(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @Embedded var sms: SmsMmsNatives.Sms? = null,
    @Embedded("mms_") var mms: SmsMmsNatives.Mms? = null,
    var sms_data: ByteArray? = null,
    val mms_text: String? = null,
    var mms_content_uri: String? = null,
    val mms_mimetype: String? = null,
    val mms_filename: String? = null,
    var mms_filepath: String? = null
)
package com.afkanerd.deku.Router.Models

import android.database.Cursor
import android.util.Pair
import androidx.recyclerview.widget.DiffUtil
import androidx.room.Embedded
import androidx.work.DelegatingWorkerFactory
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.SmsMmsNatives
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.google.gson.annotations.Expose
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
@Serializable
data class RouterItem(
    val thread_id: Int,
    val address: String,
    val date: Long,
    val date_sent: Long,
    val read: Int,
    val status: Int,
    val type: Int,
    val body: String,
    val text: String,
    val sub_id: Long,
    val url: String? = null,
    var tag: String? = null
) {
    constructor(sms: SmsMmsNatives.Sms) : this(
        thread_id = sms.thread_id,
        address = sms.address!!,
        date = sms.date,
        date_sent = sms.date_sent,
        read = sms.read,
        status = sms.status,
        type = sms.type,
        body = sms.body!!,
        text = sms.body!!,
        sub_id = sms.sub_id
    )

    fun serializeJson(): String =
        Json { prettyPrint = true }.encodeToString(this)
}

package com.afkanerd.deku.Router.Models

import android.database.Cursor
import android.util.Pair
import androidx.recyclerview.widget.DiffUtil
import androidx.room.Embedded
import androidx.work.DelegatingWorkerFactory
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.google.gson.annotations.Expose
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RouterItem(@Embedded val conversation: Conversations) {
    var routingUniqueId: String? = null
    var url: String? = null
    var routingStatus: String? = null
    var tag: String? = null

    fun serializeJson() : String {
        val json = Json {
            prettyPrint = true
        }
        return json.encodeToString(conversation)
    }
}

package com.afkanerd.deku.RemoteListeners.Models

import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
class RemoteListeners {
    @Ignore
    var connectionStatus: String? = null

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var date: Long = 0

    var hostUrl: String? = null

    var username: String? = null

    var password: String? = null

    var port: Int = 0

    var friendlyConnectionName: String? = null

    var virtualHost: String? = null

    var connectionTimeout: Int = 10000

    var prefetch_count: Int = 1

    var heartbeat: Int = 30

    var protocol: String = "amqp"

    var projectName: String? = null

    var projectBinding: String? = null

    var projectBinding2: String? = null

    @ColumnInfo(defaultValue = "0")
    var activated: Boolean = false

    @ColumnInfo(defaultValue = "0")
    var state = 0

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<RemoteListeners> =
                object : DiffUtil.ItemCallback<RemoteListeners>() {
                    override fun areItemsTheSame(oldItem: RemoteListeners, newItem: RemoteListeners):
                            Boolean {
                        return oldItem.id == newItem.id
                    }
                    override fun areContentsTheSame(oldItem: RemoteListeners, newItem: RemoteListeners):
                            Boolean {
                        return oldItem == newItem
                    }
        }

        const val STATE_DISCONNECTED = 0
        const val STATE_RECONNECTING = 1
        const val STATE_CONNECTED = 2
        const val STATE_INITIALIZING = 3
        const val GATEWAY_CLIENT_ID = "GATEWAY_CLIENT_ID"
    }
}

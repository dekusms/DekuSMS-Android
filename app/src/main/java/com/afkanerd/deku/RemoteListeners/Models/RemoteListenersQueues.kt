package com.afkanerd.deku.RemoteListeners.Models

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class RemoteListenersQueues {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var gatewayClientId: Long = 0

    var name: String? = null
    var binding1Name: String? = null
    var binding2Name: String? = null


    override fun equals(obj: Any?): Boolean {
        if (obj is RemoteListenersQueues) {
            val remoteListenersQueues = obj

            return remoteListenersQueues.id == this.id &&
                    remoteListenersQueues.name == this.name &&
                    remoteListenersQueues.binding1Name == this.binding1Name &&
                    remoteListenersQueues.binding2Name == this.binding2Name &&
                    remoteListenersQueues.gatewayClientId == this.gatewayClientId
        }
        return false
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<RemoteListenersQueues?> =
            object : DiffUtil.ItemCallback<RemoteListenersQueues?>() {
                override fun areItemsTheSame(
                    oldItem: RemoteListenersQueues,
                    newItem: RemoteListenersQueues
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: RemoteListenersQueues,
                    newItem: RemoteListenersQueues
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + gatewayClientId.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (binding1Name?.hashCode() ?: 0)
        result = 31 * result + (binding2Name?.hashCode() ?: 0)
        return result
    }
}
